package odata.jpa;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestOdata {

	@Deployment(testable = false)
	public static WebArchive createTestArchive() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addPackage("odata.jpa")
				.addPackage("odata.jpa.beans")
				.addPackage("odata.jpa.entity")
				.addAsResource("META-INF/persistence.xml")
				.addAsResource("POPULATE-DATABASE.SQL");
	}

    private WebTarget target;

    @ArquillianResource
    private URL base;

    @Before
    public void setUpClass() throws MalformedURLException {
        Client client = ClientBuilder.newClient();
        target = client.target(URI.create(new URL(base, "resources/Foo").toExternalForm()));
    }

    /**
     * Test of getList method, of class MyResource.
     */
    @Test
    public void testGetList() {
        String result = target.request().get(String.class);
        assertEquals("apple", result);
    }

}