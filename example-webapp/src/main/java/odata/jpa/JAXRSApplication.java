/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import javax.ws.rs.ApplicationPath;

/**
 * Configures a JAX-RS endpoint.
 * 
 * Moreover, here is where we set correct PersistenceContext.
 */
@ApplicationPath("/resources.svc")
public class JAXRSApplication extends AbstractJAXRSApplication {

}