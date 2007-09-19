/*
  AbstractFrostStorage.java / Frost
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
package frost.storage.perst;

import org.garret.perst.*;

public abstract class AbstractFrostStorage {

    private Storage storage = null;

    protected AbstractFrostStorage() {}

    public abstract boolean initStorage();
    public abstract boolean initStorage(int pagePoolSize);

    protected void open(
            final String databaseFilePath,
            final int pagePoolSize,
            final boolean utf8Encoding,
            final boolean concurrentIterator,
            final boolean serializeTransientObjects)
    {
        storage = StorageFactory.getInstance().createStorage();
        if( utf8Encoding ) {
            storage.setProperty("perst.string.encoding", "UTF-8");
        }
        if( concurrentIterator ) {
            storage.setProperty("perst.concurrent.iterator", Boolean.TRUE);
        }
        if( serializeTransientObjects ) {
            storage.setProperty("perst.serialize.transient.objects", Boolean.TRUE);
        }
        storage.open(databaseFilePath, pagePoolSize);
    }

    protected Storage getStorage() {
        return storage;
    }

    public int gc() {
        if( storage == null ) {
            return 0;
        } else {
            return storage.gc();
        }
    }

    public synchronized void commitStore() {
        if( storage != null ) {
            storage.commit();
        }
    }

    public void close() {
        if( storage != null ) {
            storage.close();
            storage = null;
        }
    }
}
