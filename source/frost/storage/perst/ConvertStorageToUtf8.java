/*
  ConvertStorageToUtf8.java / Frost
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

import org.garret.perst.*;

public class ConvertStorageToUtf8 {

    // storageName is e.g. "sfChkKeys", without extension or dir!
    public static boolean convertStorageToUtf8(String storageName) {
        System.out.println("Converting "+storageName+" storage into UTF-8 format...");
        // open old storage
        String databaseFilePath = "store/"+storageName+".dbs"; // path to the database file
        int pagePoolSize = 1*1024*1024; // size of page pool in bytes

        {
            Storage oldStorage = StorageFactory.getInstance().createStorage();
            oldStorage.open(databaseFilePath, pagePoolSize);
    
            if (oldStorage.getRoot() == null) { 
                // Storage was not (!) initialized yet, no need to migrate
                return true;
            }
            // export to XML
            System.out.println("Exporting storage to XML...");
            try {
                FileWriter xw = new FileWriter(new File("store/"+storageName+".xml"));
                oldStorage.exportXML(xw);
                xw.close();
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
            // close storage
            oldStorage.close();
        }
        {
            // delete (rename?) store file
            System.out.println("Renaming old storage file...");
            File oldStoreFile = new File("store/"+storageName+".dbs");
            boolean wasOk = oldStoreFile.renameTo(new File("store/"+storageName+".old"));
            if( !wasOk ) {
                System.out.println("Rename failed!");
                return false;
            }
        }
        {
            // define new storage with utf-8 encoding, and open
            Storage newStorage = StorageFactory.getInstance().createStorage();
            newStorage.setProperty("perst.string.encoding", "UTF-8"); // now use UTF-8 to store strings
            newStorage.open(databaseFilePath, pagePoolSize);
            // import exported xml data
            System.out.println("Importing XML into new storage...");
            try {
                FileReader xw = new FileReader(new File("store/"+storageName+".xml"));
                newStorage.importXML(xw);
                xw.close();
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
            // close storage
            newStorage.close();
        }
        {
            // delete old storage file and xml file
            System.out.println("Deleting old files...");
            if( ! new File("store/"+storageName+".old").delete() ) {
                System.out.println("Delete of file '"+"store/"+storageName+".old"+"' failed!");
            }
            if( ! new File("store/"+storageName+".xml").delete() ) {
                System.out.println("Delete of file '"+"store/"+storageName+".xml"+"' failed!");
            }
        }

        System.out.println("Finished to convert "+storageName+" storage into UTF-8 format.");
        return true;
    }
}
