package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

public class ImportFiles {

    private static Logger logger = Logger.getLogger(ImportFiles.class.getName());

    public void importFiles() {

        List allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        FileListDatabaseTable dbt = AppLayerDatabase.getFileListDatabaseTable();

        for( Iterator i = allBoards.iterator(); i.hasNext(); ) {
            Board board = (Board) i.next();

            File keypoolDir = new File(MainFrame.keypool + board.getBoardFilename());
            if( keypoolDir.isDirectory() ) {
                File shaIndex = new File(keypoolDir + File.separator + "files.xml");
                if( shaIndex.exists() ) {
                    FrostIndex frostIndex = FrostIndex.readKeyFile(shaIndex, board);
                    if( frostIndex == null ) {
                        logger.log(Level.SEVERE, "Error reading keyfile");
                        continue;
                    }
                    Iterator j = frostIndex.getFilesMap().values().iterator();
                    while( j.hasNext() ) {
                        SharedFileXmlFile key = (SharedFileXmlFile) j.next();

                        FrostSharedFileObject fo = new FrostSharedFileObject(key);
                        fo.setLastReceived(FrostSharedFileObject.defaultDate);

                        try {
                            dbt.insertOrUpdateFrostSharedFileObject(fo);
                        } catch (SQLException e) {
                            logger.log(Level.SEVERE, "Error inserting file", e);
                        }
                    }
                }
            }
        }
    }
}
