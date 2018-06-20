package odata.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;

/**
 * Configures a JAX-RS endpoint.
 * 
 * Webapps must extend this.
 */
public abstract class AbstractJAXRSApplication extends Application {

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>();
		props.put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
		return props;
	}

}
