package odata.jpa;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * JAX-RS Resources endpoint EJB: here we specify the correct AbstractDataManager to
 * use
 */
@Stateless
public class RestResourcesEndpoint extends AbstractRestResourcesEndpoint {

	@Inject
	DataManager manager;

	@Override
	public AbstractDataManager manager() {
		return manager;
	}

}
