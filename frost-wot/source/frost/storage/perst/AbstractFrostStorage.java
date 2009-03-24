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

import java.io.*;
import java.text.*;
import java.util.logging.*;

import org.garret.perst.*;

import frost.*;

public abstract class AbstractFrostStorage {

    private static final Logger logger = Logger.getLogger(AbstractFrostStorage.class.getName());

    private Storage storage = null;

    protected AbstractFrostStorage() {}

    public abstract String getStorageFilename();

    public abstract boolean initStorage();

    protected void open(
            final String databaseFilePath,
            final long pagePoolSize,
            final boolean utf8Encoding,
            final boolean concurrentIterator,
            final boolean serializeTransientObjects)
    {
        if( storage != null ) {
            System.out.println("Storage is already opened!");
            return;
        }
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

    public void exportToXml() throws Exception {
        final File xmlFile = new File( buildStoragePath(getStorageFilename()+".xml") );

        logger.warning("Exporting storage file '"+getStorageFilename()+"' to XML...");

        // open storage
        initStorage();

        // backup storage contents (=compact)
        FileWriter writer = null;
        try {
            writer = new FileWriter(xmlFile);
            getStorage().exportXML(writer);
            writer.close();
        } catch(final Exception t) {
            // error occured, delete bakFile
            if( writer != null ) {
                try { writer.close(); } catch(final Exception t2) {}
            }
            xmlFile.delete();
            throw t;
        }

        // close storage
        close();

        logger.warning("Finished XML export of storage file "+getStorageFilename()+" into "+xmlFile.getPath());
    }

    public long compactStorage() throws Exception {
        final File storageFile = new File( buildStoragePath(getStorageFilename()) );
        final File bakFile = new File( buildStoragePath(getStorageFilename()+".bak") );
        final File oldFile = new File( buildStoragePath(getStorageFilename()+".old") );

        logger.warning("Compacting storage file '"+getStorageFilename()+"'...");

        final long beforeStorageSize = storageFile.length();

        // open storage
        initStorage();

        // backup storage contents (=compact)
        BufferedOutputStream bakStream = null;
        try {
            bakStream = new BufferedOutputStream(new FileOutputStream(bakFile));
            getStorage().backup(bakStream);
            bakStream.close();
        } catch(final Exception t) {
            // error occured, delete bakFile
            if( bakStream != null ) {
                try { bakStream.close(); } catch(final Exception t2) {}
            }
            bakFile.delete();
            throw t;
        }

        // close storage
        close();

        // make backup version to current version, delete old storage
        if( !storageFile.renameTo(oldFile) ) {
            throw new Exception("Failed to rename '"+storageFile.getPath()+"' into '"+oldFile.getPath()+"'!");
        }
        if( !bakFile.renameTo(storageFile) ) {
            // try to rename original storage back
            if( !oldFile.renameTo(storageFile) ) {
                throw new Exception("URGENT: Failed to rename '"+oldFile.getPath()+"' back into '"+storageFile.getPath()+"'!");
            } else {
                throw new Exception("Failed to rename '"+bakFile.getPath()+"' into '"+storageFile.getPath()+"'!");
            }
        }

        final long afterStorageSize = storageFile.length();

        // all went well, delete old file
        oldFile.delete();

        final long savedBytes = beforeStorageSize - afterStorageSize;

        final NumberFormat nf = NumberFormat.getInstance();
        logger.warning("Finished compacting storage file "+getStorageFilename()+", released "+nf.format(savedBytes)+" bytes.");

        return savedBytes;
    }

    protected Storage getStorage() {
        return storage;
    }

    public boolean beginCooperativeThreadTransaction() {
        if( getStorage() != null ) {
            getStorage().beginThreadTransaction(Storage.COOPERATIVE_TRANSACTION);
            return true;
        }
        return false;
    }

    public boolean beginExclusiveThreadTransaction() {
        if( getStorage() != null ) {
            getStorage().beginThreadTransaction(Storage.EXCLUSIVE_TRANSACTION);
            return true;
        }
        return false;
    }

    public boolean endThreadTransaction() {
        if( getStorage() != null ) {
            getStorage().endThreadTransaction();
            return true;
        }
        return false;
    }

    public boolean rollbackTransaction() {
        if( getStorage() != null ) {
            getStorage().rollbackThreadTransaction();
            return true;
        }
        return false;
    }

    public int gc() {
        if( storage == null ) {
            return 0;
        } else {
            return storage.gc();
        }
    }

    public synchronized void commit() {
        if( storage != null ) {
            storage.commit();
        }
    }

    public void close() {
        if( storage != null ) {
            beginExclusiveThreadTransaction();
            storage.close();
            storage = null;
        } else {
            System.out.println("Storage is already closed!");
        }
    }

    /**
     * Retrieves the configured page pool size for the provided key (in KiB),
     * returns the value in bytes.
     */
    protected long getPagePoolSize(final String configKey) {
        long pagePoolSize = Core.frostSettings.getLongValue(configKey);
        if( pagePoolSize <= 0 ) {
            pagePoolSize = 1024;
        }
        pagePoolSize *= 1024L; // provided pagePoolSize is in kb, we want bytes
        return pagePoolSize;
    }

    /**
     * Gets the provided filename and constructs the final filename (preceedes filename with store directory).
     */
    protected String buildStoragePath(final String filename) {
        final String storeDir = Core.frostSettings.getValue(SettingsClass.DIR_STORE);
        return storeDir + filename; // path to the database file
    }
}
