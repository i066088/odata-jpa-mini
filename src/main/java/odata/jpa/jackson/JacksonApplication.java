package odata.jpa.jackson;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;

import odata.jpa.GenericRestResources;

/**
 * Replace standard JSON serializer with Jackson.
 * 
 * In Glassfish 4, MOXy is used by default.
 * 
 * @see https://stackoverflow.com/questions/23730062
 *
 */
public class JacksonApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();

		// Add root resources.
		classes.add(GenericRestResources.class);

		// Add JacksonFeature.
		classes.add(JacksonFeature.class);

		return classes;
	}
}
