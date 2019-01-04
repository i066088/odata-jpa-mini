/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import java.util.Set;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application: here we specify the path of the application.
 * 
 * If you plan to have multiple Application's, you should specify which RestResourcesEndpoint class to use.
 * 
 */
@ApplicationPath("/resources.svc")
public class JAXRSApplication extends AbstractJAXRSApplication {

	@Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = super.getClasses();

        // Add root resources.
        classes.add(RestResourcesEndpoint.class);

        return classes;
    }

}