/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application: here we specify the path of the application.
 * 
 * If you plan to have multiple Application's, you should specify which RestResourcesEndpoint class to use.
 * 
 */
@ApplicationPath("/resources.svc")
public class JAXRSApplication extends AbstractJAXRSApplication {

}