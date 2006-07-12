package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.threads.*;

public class ImportXmlMessages {

    private static Logger logger = Logger.getLogger(ImportXmlMessages.class.getName());
    
    private TofTreeModel tofTreeModel;
    
    public void importXmlMessages(TofTreeModel ttm) {
        tofTreeModel = ttm;
        importKeypool();
        importArchive();
        importSentMessages();
    }
    
    private void importKeypool() {
        File keypoolDir = new File(Core.frostSettings.getValue("keypool.dir"));
        importDir(keypoolDir);
    }

    private void importArchive() {
        String archiveDirName = Core.frostSettings.getValue("archive.dir");
        if( archiveDirName == null || archiveDirName.length() == 0 ) {
            logger.severe("no ARCHIVE DIR specified!");
        } else {
            // append messages subfolder
            archiveDirName += "messages";
            File archiveDir = new File(archiveDirName);
            if( archiveDir.isDirectory() == false ) {
                logger.severe("no archive dir found");
                archiveDir = null;
            } else {
                importDir(archiveDir);
            }
        }
    }
    
    private void importSentMessages() {
        File sentDir = new File(Core.frostSettings.getValue("sent.dir"));
        if( sentDir.isDirectory() == false ) {
            logger.severe("no sent dir found");
        } else {
            importSentDir(sentDir);
        }
        
        String archiveDirName = Core.frostSettings.getValue("archive.dir");
        if( archiveDirName == null || archiveDirName.length() == 0 ) {
            logger.severe("no ARCHIVE DIR specified!");
        } else {
            archiveDirName += "sent";
            File archiveDir = new File(archiveDirName);
            if( archiveDir.isDirectory() == false ) {
                logger.severe("no archive sent dir found");
                archiveDir = null;
            } else {
                File[] dateDirs = archiveDir.listFiles();
                if( dateDirs == null || dateDirs.length == 0 ) {
                    logger.warning("no files in archive sent dir");
                } else {
                    for(int i=0; i<dateDirs.length; i++) {
                        File dateDir = dateDirs[i];
                        if( !dateDir.isDirectory() ) {
                            continue;
                        }
                        importSentDir(dateDir);
                    }
                }
            }
        }
    }
    
    private void importSentDir(File sentDir) {
        // inside sent folder:
        // 2006.6.22-test-3.xml
        // 2006.6.23-frost-14.xml
        File[] sentFiles = sentDir.listFiles();
        if( sentFiles == null || sentFiles.length == 0 ) {
            logger.warning("no files in sent dir");
            return;
        }
        for(int i=0; i<sentFiles.length; i++) {
            File sentFile = sentFiles[i];
            String fname = sentFile.getName();
            
            int index = -1;
            try {
                // fname: "2006.3.1-boards-0.xml" - we need the index
                int p1 = fname.lastIndexOf("-");
                int p2 = fname.lastIndexOf(".xml");
                if( p1>0 && p2>0 ) {
                    String ixStr = fname.substring(p1+1, p2);
                    index = Integer.parseInt(ixStr);
                }
            } catch(Throwable t) {
                t.printStackTrace();
            }
            if( index < 0 ) {
                logger.severe("Error getting index from filename: "+fname);
                continue;
            }
            
            MessageXmlFile mof = null;
            try {
                mof = new MessageXmlFile(sentFile);
            } catch (MessageCreationException e) {
                logger.severe("Error reading sent xml file: "+sentFile.getPath());
                continue;
            }
            
            String boardName = mof.getBoardName();
            Board board = tofTreeModel.getBoardByName( boardName );
            if( board == null ) {
                logger.warning("board is not in boardlist, skipping import: "+boardName);
                continue;
            }

            FrostMessageObject mo = new FrostMessageObject(mof, board, index);
            try {
                AppLayerDatabase.getSentMessageTable().insertMessage(mo);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error inserting sent message into database", e);
            }
        }
    }

    private void importDir(File impDir) {
        File[] boardDirs = impDir.listFiles();
        if( boardDirs == null || boardDirs.length == 0 ) {
            logger.severe("no board dirs found: "+impDir.getPath());
            return;
        }
        
        // set indexslots only until max days back, not for all old messages
        java.sql.Date maxDateBack = DateFun.getSqlDateGMTDaysAgo(Core.frostSettings.getIntValue("maxMessageDisplay")+1);
        
        for(int i=0; i<boardDirs.length; i++) {
            File boardDir = boardDirs[i];
            if( boardDir.isDirectory() == false ) {
                continue;
            }

            String boardName = boardDir.getName();
            Board board = tofTreeModel.getBoardByName( boardName );
            if( board == null ) {
                logger.warning("board is not in boardlist, skipping import: "+boardName);
                continue;
            }
            
            File[] dateDirs = boardDir.listFiles();
            if( dateDirs == null || dateDirs.length == 0 ) {
                logger.severe("no date dirs in keypool for "+boardDir.getName());
                continue;
            }
            
            IndexSlots indexSlots = new IndexSlots(IndexSlots.MESSAGES, board.getName());

            for(int j=0; j<dateDirs.length; j++) {
                File dateDir = dateDirs[j];
                if( dateDir.isDirectory() == false ) {
                    continue;
                }
                
                // its a dir, we expect a name like '2006.3.1'
                Calendar dateDirCal = null;
                try {
                    dateDirCal = DateFun.getCalendarFromDate(dateDir.getName());
                } catch(NumberFormatException ex) {
                    logger.warning("Incorrect board date folder name, must be a date: "+dateDir);
                    continue;
                }
                
                File[] msgFiles = dateDir.listFiles();
                if( msgFiles == null || msgFiles.length == 0 ) {
                    continue;
                }
                for(int k=0; k<msgFiles.length; k++) {
                    File msgFile = msgFiles[k];
                    if( msgFile.isFile() == false ) {
                        continue;
                    }
                    // import msgFile
                    importMessageFile(msgFile, board, dateDirCal, indexSlots, maxDateBack);
                }
            }
            indexSlots.close();
        }
    }
    
    private boolean importMessageFile(File msgFile, Board board, Calendar calDL, IndexSlots indexSlots, java.sql.Date maxBack) {
        String fname = msgFile.getName();
        int index = -1;
        try {
            // fname: "2006.3.1-boards-0.xml" - we need the index
            int p1 = fname.lastIndexOf("-");
            int p2 = fname.lastIndexOf(".xml");
            if( p1>0 && p2>0 ) {
                String ixStr = fname.substring(p1+1, p2);
                index = Integer.parseInt(ixStr);
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        if( index < 0 ) {
            System.out.println("Error getting index from filename: "+fname);
            return false;
        }
        
        MessageXmlFile mof = null;
        String invalidReason = null;
        try {
            mof = new MessageXmlFile(msgFile);
        } catch (MessageCreationException e) {
            if( e.isEmpty() ) {
                // read notify file
                invalidReason = FileAccess.readFile(msgFile).trim();
            }
        }
        
        if( mof != null ) {
            // valid msg, insert
            FrostMessageObject mo = new FrostMessageObject(mof, board, index);
            mo.setNew( mof.isMessageNew() );
            try {
                AppLayerDatabase.getMessageTable().insertMessage(mo);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error inserting message into database", e);
                return false;
            }
        } else if( invalidReason != null ) {
            // invalid message, get date from filename
            FrostMessageObject invalidMsg = new FrostMessageObject(board, calDL, index, invalidReason);
            try {
                AppLayerDatabase.getMessageTable().insertMessage(invalidMsg);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error inserting invalid message into database", e);
                return false;
            }
        }
        
        // set indexslot used (only for dates until maxMessageDisplay)
        java.sql.Date sqlDate = DateFun.getSqlDateOfCalendar(calDL);
        if( sqlDate.after(maxBack) ) {
            try {
                indexSlots.setDownloadSlotUsed(index, sqlDate);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error inserting message index into database", e);
            }
        }

        return true;
    }
}
