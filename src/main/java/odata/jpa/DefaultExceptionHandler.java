package odata.jpa;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * I don't like the standard implementation: it gives printStackTrace() for
 * manually launched exceptions (which is useless) and not for uncatched
 * exceptions (which is very useful).
 * 
 * Response is returned in JSON. A better implementation would proceduce XML or
 * JSON according to accepted headers.
 */
@Provider
public class DefaultExceptionHandler implements ExceptionMapper<Exception> {

	@Override
	public Response toResponse(Exception e) {

		int status;
		if (e instanceof WebApplicationException) {
			// don't print stack trace
			// Unluckily, someone prints it anyway.
			status = ((WebApplicationException) e).getResponse().getStatus();
		} else {
			e.printStackTrace();
			status = Status.INTERNAL_SERVER_ERROR.getStatusCode();
		}

		OdataExceptionBean errorMessage = new OdataExceptionBean(status, e.getMessage());

		return Response.status(status).entity(errorMessage).type(MediaType.APPLICATION_JSON).build();
	}
}