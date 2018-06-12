/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configures a JAX-RS endpoint.
 *
 * @author airhacks.com
 */
@ApplicationPath("/resources.svc")
public class JAXRSApplication extends Application {

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> props = new HashMap<>();
		props.put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
		return props;
	}
}