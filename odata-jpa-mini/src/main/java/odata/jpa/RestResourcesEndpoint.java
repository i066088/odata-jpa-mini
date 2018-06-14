/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
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
import javax.ws.rs.GET;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;

import odata.jpa.beans.ODataValueBean;

/**
 * This REST WS handles not just one entity, but all possible entities. Entites
 * can lie in different packages, however their names must be different (I argue
 * this is assumed by JPA too). This service assumes all entities have a primary
 * key of type Long, called Id.
 * 
 * REST syntax is inspired to OData.
 * 
 * @author Luca Vercelli 2017-2018
 * @see http://www.odata.org/getting-started/basic-tutorial/
 */
@Stateless
@Path("/")
// TODO @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces(MediaType.APPLICATION_JSON)
public class RestResourcesEndpoint {

	public static final Integer ZERO = 0;

	private static final String FILENAME_PROPERTY_SUFFIX = "FileName";

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

	/**
	 * Give an error for any unknown $-keyword
	 */
	@GET
	@Path("${anything}")
	public Response unsupported(@PathParam("anything") String anything) {
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
	@JacksonFeatures(serializationDisable = { SerializationFeature.WRITE_DATES_AS_TIMESTAMPS })
	public Map<String, Object> find(@PathParam("entity") String entity, @QueryParam("$skip") Integer skip,
			@QueryParam("$top") Integer top, @QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderby, @QueryParam("$count") Boolean count) throws NotFoundException {

		Class<?> clazz = getEntityOrThrowException(entity); // this is Class<T>

		List<?> list; // this is List<T>

		list = manager.find(clazz, top, skip, helper.parseFilterClause(filter), helper.parseOrderByClause(orderby));

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", list);

		if (count != null && count) {
			Long numItems = manager.countEntities(clazz, filter);
			map.put("count", numItems);
		}

		return map;

	}

	/**
	 * Replace a whole collection: not supported (yet?)
	 */
	@PUT
	@Path("{entity}")
	public void replaceAll() {
		throw new ForbiddenException("Replacing the whole collection is not supported (yet?)");
	}

	/**
	 * Delete a whole collection: not supported (yet?)
	 */
	@DELETE
	@Path("{entity}")
	public void deleteAll() {
		throw new ForbiddenException("Deleting the whole collection is not supported (yet?)");
	}

	/**
	 * Return collection count as plain text
	 */
	@GET
	@Path("{entity}/$count")
	@Produces(MediaType.TEXT_PLAIN)
	public Long count(@PathParam("entity") String entity, @QueryParam("$filter") String filter)
			throws NotFoundException {
		Class<?> clazz = getEntityOrThrowException(entity);
		return manager.countEntities(clazz, helper.parseFilterClause(filter));
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
	@JacksonFeatures(serializationDisable = { SerializationFeature.WRITE_DATES_AS_TIMESTAMPS }) // FIXME not working !?!
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

		return Response.status(Status.CREATED).entity(obj).build();
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
	@Consumes(MediaType.APPLICATION_JSON)
	public <T> Response update(@PathParam("entity") String entity, @PathParam("id") Long id,
			Map<String, String> attributes) throws NotFoundException {

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

	/*
	 * public <T> Response update(@PathParam("entity") String
	 * entity, @PathParam("id") Long id, String json ) throws NotFoundException {
	 * 
	 * @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)
	 * getEntityOrThrowException(entity);
	 * 
	 * if (id == null) throw new BadRequestException(
	 * "Missing id. Creating an object with given id is not recommended nor supported."
	 * );
	 * 
	 * // SingularAttribute<? super T, ?> idAttr = manager.getIdAttribute(clazz); //
	 * attributes.put(idAttr.getName(), id.toString()); // T obj =
	 * manager.bean2object(clazz, attributes);
	 * 
	 * T obj; try { obj = new ObjectMapper().readValue(json, clazz);
	 * System.out.println("Here obj=" + obj); // Qui mi aspetto l'ID già settato }
	 * catch (IOException e) { throw new InternalServerErrorException(e); } obj =
	 * manager.save(obj); System.out.println("Now obj=" + obj); return
	 * Response.ok(obj).build(); }
	 */

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
	 * input. We assume the object has a String field for storing filename.
	 * 
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@POST
	@Path("{entity}({id})/{property}/Upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public <T> Response upload(@PathParam("entity") String entity, @PathParam("id") Long id,
			@PathParam("property") String property, @FormDataParam("file") File uploadedFile,
			@FormDataParam("file") FormDataContentDisposition contentDisposition)
			throws NotFoundException, IOException, IllegalAccessException, InvocationTargetException {

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
		if (!(blobAttrib.getJavaType().isAssignableFrom(byte[].class))) {
			throw new BadRequestException("Property " + property + " is not uploadable/downloadable");
		}

		// JPA does not support Blobs as streams ?!?
		// and also, JPA does not allow accessing Connection.createBlob()

		// DEBUG
		if (uploadedFile == null)
			System.out.println("ERROR - null file");
		System.out.println("Uploading file: " + uploadedFile.getAbsolutePath() + " " + uploadedFile.length());

		// FIXME only work if input file is 1 line long ?!?

		byte[] blob;
		blob = file2array(uploadedFile);

		BeanUtils.setProperty(obj, jpqlAttribute, blob);

		// Now, handle content type
		String filenamePropertyName = property + FILENAME_PROPERTY_SUFFIX;
		String filename = contentDisposition.getFileName();
		if (filenamePropertyName != null && filename != null) {
			String jpqlAttribute2 = helper.parseAttribute(filenamePropertyName);
			if (jpqlAttribute2 == null)
				throw new BadRequestException("Cannot parse property: " + filenamePropertyName);

			try {
				BeanUtils.setProperty(obj, jpqlAttribute2, filename);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new NotFoundException("Entity " + entity + " has no property " + filenamePropertyName);
			}
		}

		// DEBUG
		System.out.println("Just uploaded " + blob.length + " bytes");

		return Response.status(Status.CREATED).build();

	}

	/**
	 * Copy streams.
	 * 
	 * @see https://stackoverflow.com/questions/43157
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	}

	/**
	 * Copy file in memory.
	 * 
	 * @throws IOException
	 * @see https://stackoverflow.com/questions/1264709
	 */
	private byte[] file2array(File f) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		byte[] data = new byte[(int) raf.length()];
		raf.readFully(data);
		raf.close();
		return data;
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
	@Consumes({ MediaType.APPLICATION_JSON })
	public <T> Response download(@PathParam("entity") String entity, @PathParam("id") Long id,
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

		Attribute<?, ?> blobAttrib = manager.getAttribute(clazz, jpqlAttribute);
		if (!(blobAttrib.getJavaType().isAssignableFrom(byte[].class))) {
			throw new BadRequestException("Property " + property + " is not uploadable/downloadable");
		}

		byte[] blob = (byte[]) manager.getAttributeValue(blobAttrib, obj);

		if (blob == null || blob.length == 0) {
			// DEBUG
			System.out.println("Download: NO CONTENT");
			return Response.status(Status.NO_CONTENT).build();
		}

		// DEBUG
		System.out.println("Going to download " + blob.length + " bytes");

		String filenamePropertyName = property + FILENAME_PROPERTY_SUFFIX;
		String filename = null;
		String jpqlAttribute2 = helper.parseAttribute(filenamePropertyName);
		if (jpqlAttribute2 == null)
			throw new BadRequestException("Cannot parse property: " + filenamePropertyName);

		try {
			filename = BeanUtils.getProperty(obj, jpqlAttribute2);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new NotFoundException("Entity " + entity + " has no property " + filenamePropertyName);
		}
		String contentDisposition = (filename == null) ? "attachment"
				: "attachment; filename=\"" + filename.replaceAll("\"", "") + "\"";

		// TODO: mapping file extension to content type
		String contentType = MediaType.APPLICATION_OCTET_STREAM;

		InputStream is;
		is = new ByteArrayInputStream(blob);

		return Response.ok(is).header("Content-Disposition", contentDisposition).type(contentType).build();

	}

}
