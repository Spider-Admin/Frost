/*
 * Created on Nov 4, 2004
 * 
 */
package frost.storage;

import frost.identities.*;
import frost.identities.IdentitiesHsqldbDAO;

/**
 * @author $author$
 * @version $revision$
 */
public class HsqldbDAOFactory extends DAOFactory {
	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getIdentitiesDAO()
	 */
	public IdentitiesDAO getIdentitiesDAO() {
		return new IdentitiesHsqldbDAO();
	}
}
