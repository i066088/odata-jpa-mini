/*
* WebTemplate 1.0
* Luca Vercelli 2017
* Released under MIT license 
*/
package odata.jpa;

import javax.ws.rs.ApplicationPath;

import odata.jpa.jackson.JacksonApplication;

/**
 * Configures a JAX-RS endpoint.
 *
 * @author airhacks.com
 */
@ApplicationPath("/resources.svc")
public class JAXRSApplication extends JacksonApplication {

}