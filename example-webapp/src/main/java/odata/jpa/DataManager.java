package odata.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Data manager EJB: here we specify the correct EntityManager to use.
 */
@Stateless
public class DataManager extends AbstractDataManager {

	@PersistenceContext
	EntityManager em;

	@Override
	public EntityManager em() {
		return em;
	}

}
