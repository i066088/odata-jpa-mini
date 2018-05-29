/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.beanutils.BeanUtils;

/**
 * This REST WS handles not just one entity, but all possible entities. Entites
 * can lie in different packages, however their names must be different (I argue
 * this is assumed by JPA too). This service assumes all entities have a primary
 * key of type Long, called Id.
 * 
 * REST syntax is inspired to OData. For handling parameters, we follow this
 * pattern: @see https://api.stackexchange.com/docs/users
 * 
 * @author Luca Vercelli 2017-2018
 * @see http://www.odata.org/getting-started/basic-tutorial/
 */
@Stateless
@Path("/")
// TODO @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces(MediaType.APPLICATION_JSON)
public class GenericRestResources {

	public static final Integer ZERO = 0;

	@Inject
	GenericManager manager;
	@Inject
	OdataJPAHelper helper;

	/**
	 * Represent a JSON answer with a List inside the "data" field.
	 */
	public static class DataList {
		private GenericEntity<List<Object>> data;

		public GenericEntity<List<Object>> getData() {
			return data;
		}

		public void setData(GenericEntity<List<Object>> data) {
			this.data = data;
		}
	}

	/**
	 * Represent a JSON answer with a List inside the "data" field, plus a number
	 * inside the "count" field.
	 */
	// @XmlRootElement
	public static class DataListCount extends DataList {
		private Long count;

		// FIXME where "type" comes from?

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}

	}

	/**
	 * Return the Entity Class with given name.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 *             if such entity is not known.
	 */
	public Class<?> getEntityOrThrowException(String entity) throws NotFoundException {
		Class<?> clazz = manager.getEntityClass(entity);
		if (clazz == null)
			throw new NotFoundException("Unknown entity set: " + entity);
		return clazz;
	}

	/**
	 * Return a list of objects (via JSON). We don't know the type of returned
	 * objects, so we must return a generic "Response".
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@GET
	@Path("{entity}")
	public Response find(@PathParam("entity") String entity, @QueryParam("$skip") Integer skip,
			@QueryParam("$top") Integer top, @QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderby, @QueryParam("$count") Boolean count) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		List<?> list;

		list = manager.find(clazz, top, skip, helper.parseFilterClause(filter), helper.parseOrderByClause(orderby));

		// ListType and GenericEntity are needed in order to handle generics
		Type genericType = new ListType(clazz);
		GenericEntity<List<Object>> genericList = new GenericEntity<List<Object>>((List<Object>) list, genericType);

		if (count != null && count) {
			Long numItems = manager.countEntities(clazz);
			DataListCount d = new DataListCount();
			d.setData(genericList);
			d.setCount(numItems);
			return Response.ok(d).build();
		} else {
			DataList d = new DataList();
			d.setData(genericList);
			return Response.ok(d).build();
		}

	}

	@GET
	@Path("{entity}/$count")
	@Produces(MediaType.TEXT_PLAIN)
	public Long count(@PathParam("entity") String entity) throws NotFoundException {
		Class<?> clazz = getEntityOrThrowException(entity);
		return manager.countEntities(clazz);
	}

	/**
	 * Retreive and return (via JSON) a single object by id. We don't know the type
	 * of returned objects, so we must return a generic "Response".
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@GET
	@Path("{entity}({id})")
	public Response findById(@PathParam("entity") String entity, @PathParam("id") Long id) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);

		if (obj == null)
			throw new NotFoundException("");
		System.out.println("DEBUG HERE: " + obj + " CLASS " + obj.getClass());

		return Response.ok(obj).build();
	}

	@GET
	@Path("{entity}({id})/{property}/$value")
	@Produces(MediaType.TEXT_PLAIN)
	public String rawProperty(@PathParam("entity") String entity, @PathParam("id") Long id,
			@PathParam("property") String property) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);
		if (obj == null)
			throw new NotFoundException("");

		String jpqlAttribute = helper.parseAttribute(property);
		if (jpqlAttribute == null)
			throw new BadRequestException("Cannot parse property: " + property);

		// Intended for primitive types...
		String value;
		try {
			value = BeanUtils.getProperty(obj, jpqlAttribute);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new NotFoundException("Entity " + entity + " has no property " + property);
		}

		return value;
	}

	@GET
	@Path("{entity}({id})/{property}")
	public Response getProperty(@PathParam("entity") String entity, @PathParam("id") Long id,
			@PathParam("property") String property) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);
		if (obj == null)
			throw new NotFoundException("");

		String jpqlAttribute = helper.parseAttribute(property);
		if (jpqlAttribute == null)
			throw new BadRequestException("Cannot parse property: " + property);

		// Intended for primitive types...
		String value;
		try {
			value = BeanUtils.getProperty(obj, jpqlAttribute);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new NotFoundException("Entity " + entity + " has no property " + property);
		}

		String context = entity + "(" + id + ")/" + property;
		JsonObject retobj = Json.createObjectBuilder().add("@odata.context", context).add("value", value).build();

		return Response.ok(retobj).build();
	}

	// TODO can @POST single property?

	/**
	 * Create and return a single object (via JSON). We don't know the type of
	 * returned objects, so we must return a generic "Response".
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@POST
	@Path("{entity}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(@PathParam("entity") String entity, Map<String, String> attributes)
			throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		Object obj = manager.bean2object(clazz, attributes);
		obj = manager.save(obj);

		return Response.ok(obj).build();
	}

	/**
	 * Delete a single object by id.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@DELETE
	@Path("{entity}({id})")
	public void delete(@PathParam("entity") String entity, @PathParam("id") Long id) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		manager.remove(clazz, id);
	}

	/**
	 * Update and return a single object by id.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@PUT
	@Path("{entity}({id})")
	public Response update(@PathParam("entity") String entity, @PathParam("id") Long id, Map<String, String> attributes)
			throws NotFoundException {

		// FIXME: right way?

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		attributes.put("id", id.toString());
		Object obj = manager.bean2object(clazz, attributes);
		obj = manager.save(obj);
		return Response.ok(obj).build();
	}

	/**
	 * Duplicate and return (via JSON) a single object by id.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@GET
	@Path("{entity}({id})/Clone")
	public Response duplicate(@PathParam("entity") String entity, @PathParam("id") Long id) throws NotFoundException {

		// FIXME: right way?

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);

		Method setter;
		try {
			setter = obj.getClass().getMethod("setId", Long.class);
			setter.invoke(obj, (Long) null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return Response.ok(obj).build();

	}

}
