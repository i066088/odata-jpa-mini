package odata.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class RestResourcesEndpointImpl extends RestResourcesEndpoint {

	@PersistenceContext
	EntityManager em;

	@Override
	public EntityManager em() {
		return em;
	}

}
