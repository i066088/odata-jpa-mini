/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Utility methods regarding EntityManager's and beans.
 * 
 * This Stateless EJB <b>does not</b> wrap any EntityManager, because more
 * Persistence Unit could be present in the same environment.
 * 
 * @author Luca Vercelli 2017-2018
 *
 */
@Stateless
public class HighLevelEntityManager {

	private Map<String, Class<?>> entityCache = new HashMap<String, Class<?>>();

	/**
	 * Return a managed entity Class, if any, or null if entity is not managed.
	 * 
	 * Complex Types are ignored.
	 */
	public Class<?> getEntityClass(EntityManager em, String entity) {

		if (entity == null)
			return null;
		entity = entity.trim();
		if (entity.equals(""))
			return null;

		if (entityCache.containsKey(entity)) {
			return entityCache.get(entity);
		}

		for (EntityType<?> entityType : em.getMetamodel().getEntities()) {
			if (entityType.getName().equals(entity)) {
				Class<?> clazz = entityType.getJavaType();
				entityCache.put(entity, clazz);
				return clazz;
			}
		}

		return null;
	}

	/**
	 * Retrieve (single) identity column.
	 * 
	 * @see https://stackoverflow.com/questions/16909236
	 * @param entity
	 * @return
	 */
	public <T> SingularAttribute<? super T, ?> getIdAttribute(EntityManager em, Class<T> entity) {
		Metamodel m = em.getMetamodel();
		IdentifiableType<T> type = (IdentifiableType<T>) m.managedType(entity);
		return type.getId(type.getIdType().getJavaType());
	}

	/**
	 * Retrieve a column.
	 * 
	 * @param entity
	 * @param propertyName
	 * @return
	 */
	public <T> Attribute<? super T, ?> getAttribute(EntityManager em, Class<T> entity, String propertyName) {
		Metamodel m = em.getMetamodel();
		return m.managedType(entity).getAttribute(propertyName);
	}

	/**
	 * Select a single attribute.
	 * 
	 * @param entity
	 * @param propertyName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <U> U getAttributeValue(Attribute<?, U> attribute, Object obj) {
		U value;
		if (attribute.getJavaMember() instanceof Field) {
			Field field = (Field) attribute.getJavaMember();
			try {
				value = (U) field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO use LOG instead
				e.printStackTrace();
				throw new IllegalStateException();
			}
		} else if (attribute.getJavaMember() instanceof Method) {
			Method getter = (Method) attribute.getJavaMember();
			try {
				value = (U) getter.invoke(obj);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO use LOG instead
				e.printStackTrace();
				throw new IllegalStateException();
			}
		} else {
			System.out.println(
					"Attribute " + attribute + " has unknown java getter: " + attribute.getJavaMember().getClass());
			throw new IllegalStateException();
		}
		return value;
	}

	/**
	 * Select a single attribute.
	 * 
	 * @param entity
	 * @param attributeName
	 * @param obj
	 * @return
	 */
	public Object getAttributeValue(EntityManager em, Class<?> entity, String attributeName, Object obj) {
		return getAttributeValue(getAttribute(em, entity, attributeName), obj);
	}

	/**
	 * Return a new object with given attributes set. Class must have a no-argument
	 * constructor.
	 * 
	 * @param entity
	 * @param attributes
	 * @return
	 */
	public <T> T bean2object(Class<T> entity, Map<String, ? extends Object> attributes) {
		T obj;
		try {
			obj = entity.newInstance();
			BeanUtils.populate(obj, attributes);

		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return obj;
	}

	/**
	 * Return object attributes.
	 * 
	 * @param obj
	 * @return
	 */
	public Map<String, String> object2bean(Object obj) {
		try {
			return BeanUtils.describe(obj);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete an object from database, by primary key.
	 */
	public void removeById(EntityManager em, Class<?> entity, Serializable id) {
		Object obj = em.find(entity, id);
		em.remove(obj);
	}

	/**
	 * Load all objects of given entity.
	 */
	public <T> List<T> findAll(EntityManager em, Class<T> entity) {
		return em.createQuery("from " + entity.getName(), entity).getResultList();
	}

	/**
	 * Count number of rows in given table.
	 */
	public Long countEntities(EntityManager em, Class<?> entity) {
		return countEntities(em, entity, null);
	}

	/**
	 * Count number of rows in given table.
	 * 
	 * @param where
	 *            JPQL WHERE clause, without "WHERE".
	 */
	public Long countEntities(EntityManager em, Class<?> entity, String where) {
		String whereCondition = "";
		if (where != null && !where.trim().isEmpty()) {
			whereCondition = " WHERE " + where;
		}
		return em.createQuery("select count(*) from " + entity.getName() + whereCondition, Long.class)
				.getSingleResult();
	}

	/**
	 * Load at most maxResults objects of given entity, starting from firstResult,
	 * and with given ordering. Useful for pagination.
	 * 
	 * Notice that "maxResult" is in fact the size of a page, while "firstResult" =
	 * (pageNumber-1)*pageSize
	 * 
	 * @param maxResult
	 *            max number of elements to retrieve (optional)
	 * @param firstResult
	 *            positional order of first element to retrieve (0-based)
	 *            (optional).
	 * @param where
	 *            WHERE JPQL clause, without "WHERE" (optional).
	 * @param orderby
	 *            ORDER BY JPQL clause, without "ORDER BY" (optional).
	 */
	public <T> List<T> find(EntityManager em, Class<T> entity, Integer maxResults, Integer firstResult, String where,
			String orderby) {

		String whereCondition = "";
		String orderbyCondition = "";

		if (where != null && !where.trim().isEmpty()) {
			whereCondition = " WHERE " + where;
		}

		if (orderby != null && !orderby.trim().isEmpty()) {
			orderbyCondition = " ORDER BY " + orderby;
		}

		TypedQuery<T> query = em
				.createQuery("SELECT u FROM " + entity.getName() + " u " + whereCondition + orderbyCondition, entity);
		if (firstResult != null)
			query.setFirstResult(firstResult);
		if (maxResults != null)
			query.setMaxResults(maxResults);

		return query.getResultList();
	}

	/**
	 * Load all objects of given entity, such that property=value (null supported).
	 */
	public <T> List<T> findByProperty(EntityManager em, Class<T> entity, String propertyName, Object value) {
		if (value != null) {
			return em
					.createQuery("select u from " + entity.getName() + " u where " + propertyName + " = :param", entity)
					.setParameter("param", value).getResultList();
		} else {
			return em.createQuery("select u from " + entity.getName() + " u where " + propertyName + " is null", entity)
					.getResultList();
		}
	}

	/**
	 * Load that object of given entity, such that property=value (null supported),
	 * or null if none.
	 * 
	 * @throws NonUniqueResultException
	 * @throws NoResultException
	 */
	public <T> T findByPropertySingleResult(EntityManager em, Class<T> entity, String propertyName, Object value) {
		if (value != null) {
			return em
					.createQuery("select u from " + entity.getName() + " u where " + propertyName + " = :param", entity)
					.setParameter("param", value).getSingleResult();
		} else {
			return em.createQuery("select u from " + entity.getName() + " u where " + propertyName + " is null", entity)
					.getSingleResult();
		}
	}

	/**
	 * Find (i.e. retrieve) an object from database, by primary key, then detache it
	 * and remove its primary key so that we have a "new" object.
	 * 
	 * The new object is not saved to database.
	 * 
	 * @throws IllegalArgumentException
	 *             if object does not exists
	 */
	public <T> T duplicate(EntityManager em, Class<T> entity, Serializable id) {

		T obj = em.find(entity, id);

		if (obj == null)
			throw new IllegalArgumentException("No object with given id");

		em.detach(obj);

		SingularAttribute<? super T, ?> idAttr = getIdAttribute(em, entity);
		if (idAttr == null) {
			// TODO LOG instead of System.out.println
			System.out.println("getIdAttribute() returned null for entity:" + entity);
			throw new IllegalArgumentException();
		}

		try {
			PropertyUtils.setSimpleProperty(obj, idAttr.getName(), null);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exc) {
			// TODO LOG instead of System.out.println
			System.out.println("Misconfigured class: " + entity);
			exc.printStackTrace();
			throw new IllegalArgumentException("Misconfigured class: " + entity);
		}

		return obj;
	}

}
