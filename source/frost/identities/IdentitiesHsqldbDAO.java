/*
 * Created on Nov 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.identities;

import frost.storage.StorageException;

/**
 * @author $author$
 * @version $revision$
 */
public class IdentitiesHsqldbDAO implements IdentitiesDAO {

	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#exists()
	 */
	public boolean exists() {
		return true;	//TODO: incomplete.
	}

	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#create()
	 */
	public void create() throws StorageException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#load(frost.identities.FrostIdentities)
	 */
	public void load(FrostIdentities identities) throws StorageException {
		// TODO Auto-generated method stub
		
	}

}
