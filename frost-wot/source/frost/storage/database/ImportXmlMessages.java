/*
 ImportXmlMessages.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.gui.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.util.*;

public class ImportXmlMessages {

    private static Logger logger = Logger.getLogger(ImportXmlMessages.class.getName());
    
    private Hashtable boardDirNames;
    private long uncommittedBytes = 0;
    
    private Splashscreen splashScreen;
    private String origSplashText;

    // TODO: maybe provide keypooldir/archivedir and allow import from other frosts too?
    public void importXmlMessages(List boards, Splashscreen splash, String origTxt) {
        splashScreen = splash;
        origSplashText = origTxt;
        
        boardDirNames = new Hashtable();
        for( Iterator iter = boards.iterator(); iter.hasNext(); ) {
            Board board = (Board) iter.next();
            boardDirNames.put( board.getBoardFilename(), board );
        }
        
        try {
            AppLayerDatabase.getInstance().setAutoCommitOff();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit off", e);
        }
        
        importKeypool();
        importArchive();
        importSentMessages();
        
        try {
            AppLayerDatabase.getInstance().setAutoCommitOn();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit on", e);
        }
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
        
        splashScreen.setText(origSplashText + " (sent messages)");
        
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
            Board board = (Board)boardDirNames.get( boardName );
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
        DateMidnight maxDateBack = new DateMidnight(DateTimeZone.UTC).minusDays(Core.frostSettings.getIntValue("maxMessageDisplay")+1);
        
        for(int i=0; i<boardDirs.length; i++) {
            File boardDir = boardDirs[i];
            if( boardDir.isDirectory() == false ) {
                continue;
            }

            String boardName = boardDir.getName();
            Board board = (Board)boardDirNames.get( boardName );
            if( board == null ) {
                logger.warning("board is not in boardlist, skipping import: "+boardName);
                continue;
            }
            
            File[] dateDirs = boardDir.listFiles();
            if( dateDirs == null || dateDirs.length == 0 ) {
                logger.severe("no date dirs in keypool for "+boardDir.getName());
                continue;
            }
            
            IndexSlotsDatabaseTable indexSlots = new IndexSlotsDatabaseTable(IndexSlotsDatabaseTable.MESSAGES, board);

            for(int j=0; j<dateDirs.length; j++) {
                File dateDir = dateDirs[j];
                if( dateDir.isDirectory() == false ) {
                    continue;
                }
                // its a dir, we expect a name like '2006.3.1'
                DateMidnight dateDirCal = null;
                try {
                    dateDirCal = DateFun.FORMAT_DATE.parseDateTime(dateDir.getName()).toDateMidnight();
                } catch(NumberFormatException ex) {
                    logger.warning("Incorrect board date folder name, must be a date: "+dateDir);
                    continue;
                }
                
                File[] msgFiles = dateDir.listFiles();
                if( msgFiles == null || msgFiles.length == 0 ) {
                    continue;
                }
                
                splashScreen.setText(origSplashText + " ("+board.getName()+", "+dateDir.getName()+")");
                
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
    
    private boolean importMessageFile(File msgFile, Board board, DateMidnight calDL, IndexSlotsDatabaseTable indexSlots, 
            DateMidnight maxBack) {
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
                if( e.getMessage().indexOf("MSG_ID_UNIQUE_ONLY") > 0 ) {
                    logger.log(Level.WARNING, "Import of duplicate message skipped");
                } else {
                    logger.log(Level.SEVERE, "Error inserting message into database", e);
                }
                return false;
            }
        } else if( invalidReason != null ) {
            // invalid message, get date from filename
            FrostMessageObject invalidMsg = new FrostMessageObject(board, calDL.toDateTime(), index, invalidReason);
            try {
                AppLayerDatabase.getMessageTable().insertMessage(invalidMsg);
            } catch (SQLException e) {
                if( e.getMessage().indexOf("MSG_ID_UNIQUE_ONLY") > 0 ) {
                    logger.log(Level.WARNING, "Import of duplicate message skipped");
                } else {
                    logger.log(Level.SEVERE, "Error inserting invalid message into database", e);
                }
                return false;
            }
        }

        // all 5 MB commit changes to database
        uncommittedBytes += msgFile.length();
        if( uncommittedBytes > 5*1024*1024 ) {
            try {
                AppLayerDatabase.getInstance().commit();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error on commit", e);
            }
            uncommittedBytes = 0;
        }
        
        // set indexslot used (only for dates until maxMessageDisplay)
        if( calDL.isAfter(maxBack) ) {
            try {
                indexSlots.setDownloadSlotUsed(index, calDL.toDateTime().getMillis());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error inserting message index into database", e);
            }
        }
        return true;
    }
}
