/*
 * Created on Nov 4, 2004
 *
 */
package frost.storage;

import frost.identities.*;
import frost.identities.IdentitiesXmlDAO;

/**
 * @author $author$
 * @version $revision$
 */
public class XmlDAOFactory extends DAOFactory {
	/* (non-Javadoc)
	 * @see frost.storage.DAOFactory#getIdentitiesDAO()
	 */
	public IdentitiesDAO getIdentitiesDAO() {
		return new IdentitiesXmlDAO();
	}
}
