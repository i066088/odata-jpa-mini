/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.beanutils.BeanUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import odata.jpa.beans.ODataValueBean;

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

	// FIXME alla prima request viene sempre risposto un errore:
	// javax.servlet.ServletException:
	// org.glassfish.jersey.server.ContainerException:
	// java.lang.NoClassDefFoundError:
	// com/fasterxml/jackson/module/jaxb/JaxbAnnotationIntrospector

	public static final Integer ZERO = 0;

	@Inject
	GenericManager manager;
	@Inject
	OdataJPAHelper helper;

	/**
	 * Return the Entity Class with given name.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 *             if such entity is not known.
	 */
	protected Class<?> getEntityOrThrowException(String entity) throws NotFoundException {
		Class<?> clazz = manager.getEntityClass(entity);
		if (clazz == null)
			throw new NotFoundException("Unknown entity set: " + entity);
		return clazz;
	}

	@GET
	@Path("${anything}")
	public void unsupported(@PathParam("anything") String anything) {
		throw new BadRequestException("Unknown keyword $" + anything + ".");
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
	public Map<String, Object> find(@PathParam("entity") String entity, @QueryParam("$skip") Integer skip,
			@QueryParam("$top") Integer top, @QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderby, @QueryParam("$count") Boolean count) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity); // this is Class<T>

		List<?> list; // this is List<T>

		list = manager.find(clazz, top, skip, helper.parseFilterClause(filter), helper.parseOrderByClause(orderby));

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", list);

		if (count != null && count) {
			Long numItems = manager.countEntities(clazz);
			map.put("count", numItems);
		}

		return map;

	}

	@PUT
	@Path("{entity}")
	public void replaceAll() {
		throw new ForbiddenException("Replacing the whole collection is not supported (yet?)");
	}

	@DELETE
	@Path("{entity}")
	public void deleteAll() {
		throw new ForbiddenException("Deleting the whole collection is not supported (yet?)");
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
			throw new NotFoundException("No entity with this Id");

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
	public ODataValueBean getProperty(@PathParam("entity") String entity, @PathParam("id") Long id,
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

		ODataValueBean response = new ODataValueBean(value);

		return response;
	}

	@PUT
	@Path("{entity}({id})/{property}")
	public void updateProperty() {
		throw new ForbiddenException("Updating a single property is not supported (yet?)");
	}

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

		return Response.ok(Status.CREATED).entity(obj).build();
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
	// @PATCH does not exist!
	@Path("{entity}({id})")
	public <T> Response update(@PathParam("entity") String entity, @PathParam("id") Long id,
			Map<String, String> attributes) throws NotFoundException {

		// FIXME: right way?

		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException(
					"Missing id. Creating an object with given id is not recommended nor supported.");

		SingularAttribute<? super T, ?> idAttr = manager.getIdAttribute(clazz);
		attributes.put(idAttr.getName(), id.toString());

		T obj = manager.bean2object(clazz, attributes);
		obj = manager.save(obj);
		return Response.ok(obj).build();
	}

	/**
	 * Duplicate and return (via JSON) a single object by id. The new object is not
	 * saved on DB yet.
	 * 
	 * This is an example of OData Function.
	 * 
	 * @param entity
	 * @return
	 * @throws NotFoundException
	 */
	@GET
	@Path("{entity}({id})/Clone")
	public <T> Response duplicate(@PathParam("entity") String entity, @PathParam("id") Long id)
			throws NotFoundException {

		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		T obj = manager.duplicate(clazz, id);

		if (obj == null)
			throw new NotFoundException("No entity with this Id");

		return Response.ok(obj).build();

	}

	/**
	 * Upload a Blob field.
	 * 
	 * For small images you may guess to use BINARY OData fields (not implemented
	 * yet), however for large images this is a bad solution.
	 * 
	 * There is no OData standard for this operation, AFAIK.
	 * 
	 * This is something like an OData Action, however uses a multipart/form-data
	 * input.
	 * 
	 * @throws NotFoundException
	 */
	@POST
	@Path("{entity}({id})/{property}/Upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public <T> Response upload(@PathParam("entity") String entity, @PathParam("id") Long id,
			@PathParam("property") String property,
			@FormDataParam("contentTypePropertyName") String contentTypePropertyName,
			@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") final FormDataBodyPart body)
			throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);
		if (obj == null)
			throw new NotFoundException("");

		String jpqlAttribute = helper.parseAttribute(property);
		if (jpqlAttribute == null)
			throw new BadRequestException("Cannot parse property: " + property);

		Attribute<?, ?> blobAttrib = manager.getAttribute(clazz, jpqlAttribute);
		if (!(blobAttrib.getJavaType().isAssignableFrom(Blob.class))) {
			throw new BadRequestException("Property " + property + " is not uploadable/downloadable");
		}

		Blob blob = (Blob) manager.getAttributeValue(blobAttrib, obj);
		try {
			copy(uploadedInputStream, blob.setBinaryStream(0));
		} catch (SQLException | IOException e) {
			// TODO use LOG instead
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

		// Now, handle content type
		if (contentTypePropertyName != null) {
			String contentType = body.getMediaType().toString();
			if (contentType == null)
				contentType = MediaType.APPLICATION_OCTET_STREAM;
			String jpqlAttribute2 = helper.parseAttribute(contentTypePropertyName);
			if (jpqlAttribute2 == null)
				throw new BadRequestException("Cannot parse property: " + contentTypePropertyName);

			try {
				BeanUtils.setProperty(obj, jpqlAttribute2, contentType);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new NotFoundException("Entity " + entity + " has no property " + contentTypePropertyName);
			}
		}

		return Response.ok(Status.CREATED).build();

	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	}

	/**
	 * Download from a Blob field.
	 * 
	 * For small images you may guess to use BINARY OData fields (not implemented
	 * yet), however for large images this is a bad solution.
	 * 
	 * There is no OData standard for this operation, AFAIK.
	 * 
	 * This is an example of OData Function.
	 * 
	 * @see https://stackoverflow.com/questions/1076972
	 * @return
	 * @throws NotFoundException
	 */
	@GET
	@Path("{entity}({id})/{property}/Download")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED })
	public <T> Response download(@PathParam("entity") String entity, @PathParam("id") Long id,
			@PathParam("property") String property,
			@FormParam("contentTypePropertyName") String contentTypePropertyName) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity);

		if (id == null)
			throw new BadRequestException("Missing id");

		Object obj = manager.findById(clazz, id);
		if (obj == null)
			throw new NotFoundException("");

		String jpqlAttribute = helper.parseAttribute(property);
		if (jpqlAttribute == null)
			throw new BadRequestException("Cannot parse property: " + property);

		Attribute<?, ?> blobAttrib = manager.getAttribute(clazz, jpqlAttribute);
		if (!(blobAttrib.getJavaType().isAssignableFrom(Blob.class))) {
			throw new BadRequestException("Property " + property + " is not uploadable/downloadable");
		}

		Blob blob = (Blob) manager.getAttributeValue(blobAttrib, obj);

		String contentType = null;
		if (contentTypePropertyName != null) {
			String jpqlAttribute2 = helper.parseAttribute(contentTypePropertyName);
			if (jpqlAttribute2 == null)
				throw new BadRequestException("Cannot parse property: " + contentTypePropertyName);

			try {
				contentType = BeanUtils.getProperty(obj, jpqlAttribute2);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new NotFoundException("Entity " + entity + " has no property " + contentTypePropertyName);
			}
		}
		if (contentType == null)
			contentType = MediaType.APPLICATION_OCTET_STREAM;

		if (blob == null) {
			return Response.ok(Status.NO_CONTENT).type(contentType).build();
		}

		InputStream is;
		try {
			is = blob.getBinaryStream();
		} catch (SQLException e) {
			// TODO use LOG instead
			e.printStackTrace();
			throw new InternalServerErrorException();
		}

		return Response.ok(is).type(contentType).build();

	}

}
