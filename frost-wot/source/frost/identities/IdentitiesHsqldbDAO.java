/*
 IdentitiesHsqldbDAO.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.identities;

import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
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

	/* (non-Javadoc)
	 * @see frost.identities.IdentitiesDAO#save(frost.identities.FrostIdentities)
	 */
	public void save(FrostIdentities identities) throws StorageException {
		// TODO Auto-generated method stub
		
	}

}
