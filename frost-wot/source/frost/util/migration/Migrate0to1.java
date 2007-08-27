package frost.util.migration;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.storage.database.applayer.*;
import frost.storage.perst.*;
import frost.util.migration.migrate0to1.*;

/**
 * Migration from migrate.version 0 to 1.
 * 
 * Migrate various tables from McKoi to perst and drop McKoi tables.
 */
public class Migrate0to1 {
    
    private static final Logger logger = Logger.getLogger(Migrate0to1.class.getName());

    public boolean run() {
        
        deleteMessageHashesFile();

        boolean ok;
        System.out.println("Migrating filelist indexslots...");
        ok = migrateGlobalIndexSlots(GlobalIndexSlotsDatabaseTable.FILELISTS, IndexSlotsStorage.FILELISTS);
        if(!ok) return ok;
        System.out.println("Migrating request indexslots...");
        ok = migrateGlobalIndexSlots(GlobalIndexSlotsDatabaseTable.REQUESTS, IndexSlotsStorage.REQUESTS);
        
        ok = dropTable("GLOBALINDEXSLOTS");
        if(!ok) return ok;
        
        System.out.println("Migrating message indexslots...");
        ok = migrateIndexSlots();
        if(!ok) return ok;
        
        System.out.println("Migrating SharedFilesCHKKeys...");
        ok = migrateSharedFilesCHKKeys();
        if(!ok) return ok;
        
        System.out.println("Migrating DownloadFiles...");
        ok = migrateDownloadFiles();
        if(!ok) return ok;
        
        System.out.println("Migrating UploadFiles...");
        ok = migrateUploadFiles();
        if(!ok) return ok;

        System.out.println("Migrating SharedFiles...");
        ok = migrateSharedFiles();
        if(!ok) return ok;

        System.out.println("Migrating NewUploadFiles...");
        ok = migrateNewUploadFiles();
        if(!ok) return ok;
        
        return ok;
    }
    
    private void deleteMessageHashesFile() {
//        private static final String XML_FILENAME = "hashes.xml";
//        private static final String TMP_FILENAME = "hashes.xml.tmp";
//        private static final String BAK_FILENAME = "hashes.xml.bak";
        
        File xmlFile;
        
        xmlFile = new File("hashes.xml");
        if( xmlFile.isFile() ) {
            xmlFile.delete();
        }

        xmlFile = new File("hashes.xml.tmp");
        if( xmlFile.isFile() ) {
            xmlFile.delete();
        }

        xmlFile = new File("hashes.xml.bak");
        if( xmlFile.isFile() ) {
            xmlFile.delete();
        }
    }
    
    private boolean dropTable(String tableName) {
        try {
            Statement stmt = AppLayerDatabase.getInstance().createStatement();
            String sql = "DROP TABLE "+tableName;
            stmt.executeUpdate(sql);
            AppLayerDatabase.getInstance().commit();
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return true;
    }

    public boolean migrateNewUploadFiles() {
        FrostFilesStorage perstStorage = FrostFilesStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            List<NewUploadFile> nufItems = NewUploadFilesDatabaseTable.migrateNewUploadFiles();
            int cnt = nufItems.size();
            perstStorage.saveNewUploadFiles(nufItems);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("NEWUPLOADFILES");
    }

    public boolean migrateSharedFiles() {
        FrostFilesStorage perstStorage = FrostFilesStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            List<PerstFrostSharedFileItem> sfItems = SharedFilesDatabaseTable.migrateSharedFiles();
            int cnt = sfItems.size();
            perstStorage.savePerstFrostSharedFiles(sfItems);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("SHAREDFILES");
    }

    public boolean migrateUploadFiles() {
        FrostFilesStorage perstStorage = FrostFilesStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            List<PerstFrostUploadItem> dlItems = UploadFilesDatabaseTable.migrateUploadFiles();
            int cnt = dlItems.size();
            perstStorage.savePerstFrostUploadFiles(dlItems);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("UPLOADFILES");
    }

    public boolean migrateDownloadFiles() {
        FrostFilesStorage perstStorage = FrostFilesStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            List<PerstFrostDownloadItem> dlItems = DownloadFilesDatabaseTable.migrateDownloadFiles();
            int cnt = dlItems.size();
            perstStorage.savePerstFrostDownloadFiles(dlItems);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("DOWNLOADFILES");
    }

    public boolean migrateSharedFilesCHKKeys() {
        SharedFilesCHKKeyStorage perstStorage = SharedFilesCHKKeyStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            int cnt = SharedFilesCHKKeysDatabaseTable.migrateAllData(perstStorage);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("SHAREDFILESCHK");
    }

    public boolean migrateIndexSlots() {
        IndexSlotsDatabaseTable isDb = new IndexSlotsDatabaseTable();
        IndexSlotsStorage perstStorage = IndexSlotsStorage.inst();
        // read all data from McKoi and insert into perst
        try {
            int cnt = isDb.migrateAllData(perstStorage);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return dropTable("INDEXSLOTS");
    }
    
    public boolean migrateGlobalIndexSlots(int sourceIndexName, int targetIndexName) {
        GlobalIndexSlotsDatabaseTable gixMcKoiTable = new GlobalIndexSlotsDatabaseTable();
        IndexSlotsStorage perstStorage = IndexSlotsStorage.inst();
        
        // read all data from McKoi and insert into perst
        try {
            int cnt = gixMcKoiTable.migrateAllDataForIndexName(perstStorage, sourceIndexName, targetIndexName);
            System.out.println("...ready, migrated "+cnt+" SQL table rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Migration error!", e);
            return false;
        }
        return true;
    }
}
