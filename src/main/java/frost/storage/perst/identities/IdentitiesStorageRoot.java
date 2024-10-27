/*
  IdentitiesStorageRoot.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.identities;

import org.garret.perst.IPersistentList;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

import frost.identities.Identity;
import frost.identities.LocalIdentity;

public class IdentitiesStorageRoot extends Persistent {

	private static final long serialVersionUID = 1L;

    public static final transient int MIGRATION_LEVEL_1 = 1;

    private IPersistentList<Identity> identities;
    private IPersistentList<LocalIdentity> localIdentities;

    private int migrationLevel;

    public IdentitiesStorageRoot() {}

    public IdentitiesStorageRoot(final Storage store) {
        identities  = store.createScalableList();
        localIdentities = store.createScalableList();
    }

    public IPersistentList<Identity> getIdentities() {
        return identities;
    }

    public IPersistentList<LocalIdentity> getLocalIdentities() {
        return localIdentities;
    }

    public int getMigrationLevel() {
        return migrationLevel;
    }

    public void setMigrationLevel(final int migrationLevel) {
        this.migrationLevel = migrationLevel;
    }
}
