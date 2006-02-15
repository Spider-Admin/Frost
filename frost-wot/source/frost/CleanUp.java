/*
 CleanUp.java / Frost
 Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.gui.objects.*;

/**
 * TODO: archive identities.xml too
 * 
 * Clean up the keypool.
 */
public class CleanUp {
    
    private static Logger logger = Logger.getLogger(CleanUp.class.getName());
    
    public static final int DELETE_MESSAGES  = 1;
    public static final int ARCHIVE_MESSAGES = 2;
    public static final int KEEP_MESSAGES = 3;
    
    /**
     * Cleans the keypool during runtime of Frost.
     * Deletes all expired files in keypool.
     * Gets the mode to use from settings.
     */
    public static void processExpiredFiles(List boardList) {
        int mode;
        String strMode = Core.frostSettings.getValue("messageExpirationMode");
        if( strMode.toUpperCase().equals("KEEP") ) {
            mode = KEEP_MESSAGES;
        } else if( strMode.toUpperCase().equals("ARCHIVE") ) {
            mode = ARCHIVE_MESSAGES;
        } else if( strMode.toUpperCase().equals("DELETE") ) {
            mode = DELETE_MESSAGES;
        } else {
            mode = KEEP_MESSAGES;
        }
        processExpiredFiles(boardList, mode);
    }    

    /**
     * Cleans the keypool during runtime of Frost.
     * Deletes all expired files in keypool.
     */
    private static void processExpiredFiles(List boardList, int mode) {
        
        String archiveDir;
        
        if( mode == ARCHIVE_MESSAGES ) {
            logger.info("Expiration mode is ARCHIVE_MESSAGES.");
            archiveDir = Core.frostSettings.getValue("archive.dir");
            if( archiveDir == null || archiveDir.length() == 0 ) {
                logger.severe("ERROR: no ARCHIVE DIR specified!");
                return;
            }
            // append messages subfolder
            archiveDir += ("messages" + File.separator);
            
            File f = new File(archiveDir);
            if( f.isDirectory() == false ) {
                boolean ok = f.mkdirs();
                if( ok == false ) {
                    logger.severe("ERROR: could not create archive directory: "+f.getPath());
                    return;
                }
            }
            
        } else if( mode == DELETE_MESSAGES ) {
            logger.info("Expiration mode is DELETE_MESSAGES.");
            archiveDir = null;
        } else if( mode == KEEP_MESSAGES ) {
            logger.info("Expiration mode is KEEP_MESSAGES.");
            archiveDir = null;
        } else {
            logger.severe("ERROR: invalid MODE specified: "+mode);
            return;
        }
        
        // ALWAYS delete expired indices files
        deleteExpiredIndicesFiles(boardList);
        
        if( mode == KEEP_MESSAGES ) {
            // we are finished now
            return;
        }
        
        // take maximum
        int defaultDaysOld = Core.frostSettings.getIntValue("messageExpireDays") + 1;

        if( defaultDaysOld < Core.frostSettings.getIntValue("maxMessageDisplay") ) {
            defaultDaysOld = Core.frostSettings.getIntValue("maxMessageDisplay") + 1;
        }
        if( defaultDaysOld < Core.frostSettings.getIntValue("maxMessageDownload") ) {
            defaultDaysOld = Core.frostSettings.getIntValue("maxMessageDownload") + 1;
        }

        // sent messages
        logger.info("Starting to process all expired files in SENT folder"+
                " older than "+defaultDaysOld+" days.");
        int deletedSentMsgs = processExpiredSentMessages(defaultDaysOld, mode);
        logger.info("Finished to process expired files in SENT folder, "+
                "deleted "+deletedSentMsgs+" files.");

        // boards
        for(Iterator i=boardList.iterator(); i.hasNext(); ) {
            
            int currentDaysOld = defaultDaysOld;
            Board board = (Board)i.next();
            if( board.isConfigured() && board.getMaxMessageDisplay() > currentDaysOld ) {
                currentDaysOld = board.getMaxMessageDisplay();
            }

            File boardFolder = new File(MainFrame.keypool + board.getBoardFilename());
            if( boardFolder.isDirectory() == false ) {
                logger.warning("No board folder for board "+board.getName());
                continue;
            }

            if( mode == DELETE_MESSAGES ) {
                
                logger.info("Starting to DELETE all expired files in folder "+board.getBoardFilename()+
                        " older than "+currentDaysOld+" days.");

                int deleted = processExpiredMessages(boardFolder, currentDaysOld, mode, archiveDir);
                
                logger.info("Finished to DELETE expired files in folder "+boardFolder.getName()+
                        "deleted "+deleted+" files.");
                
            } else if( mode == ARCHIVE_MESSAGES ) {
                
                logger.info("Starting to ARCHIVE all expired files in folder "+board.getBoardFilename()+
                        " older than "+currentDaysOld+" days into archive folder "+archiveDir+".");
                
                int deleted = processExpiredMessages(boardFolder, currentDaysOld, mode, archiveDir);
                
                logger.info("Finished to ARCHIVE expired files in folder "+boardFolder.getName()+
                        "deleted "+deleted+" files.");
            } 
        }
        logger.info("Finished to process expired files.");
    }
    
    // archive/delete expired SENT messages (Core.frostSettings.getValue("sent.dir"))
    private static int processExpiredSentMessages(int daysOld, int mode) {

        String sentArchiveDir = Core.frostSettings.getValue("archive.dir");
        if( sentArchiveDir == null || sentArchiveDir.length() == 0 ) {
            logger.severe("ERROR: no ARCHIVE DIR specified!");
            return 0;
        }

        sentArchiveDir += ("sent" + File.separator);
        
        File f = new File(sentArchiveDir);
        if( f.isDirectory() == false ) {
            boolean ok = f.mkdirs();
            if( ok == false ) {
                logger.severe("ERROR: could not create archive directory for sent msgs: "+f.getPath());
                return 0;
            }
        }
        f = null;
        
        String sentMsgsDirName = Core.frostSettings.getValue("sent.dir");
        if( sentMsgsDirName == null || sentMsgsDirName.length() == 0 ) {
            logger.severe("ERROR: no SENT DIR specified!");
            return 0;
        }
        
        File sentMsgsDir = new File(sentMsgsDirName);
        if( sentMsgsDir.isDirectory() == false ) {
            logger.severe("ERROR: SENT DIR does not exist!");
            return 0;
        }

        String minDate = DateFun.getExtendedDate(daysOld);

        File[] sentFilesList = sentMsgsDir.listFiles();
        if( sentFilesList == null ) {
            logger.severe("Could not get list of files for folder "+sentMsgsDir.getPath());
            return 0;
        }
        
        int deleted = 0;
        
        for(int x=0; x < sentFilesList.length; x++) {
            // "2006.2.10-freenet-19.xml"
            File sentMsg = sentFilesList[x];
            String fileName = sentMsg.getName();
            if( fileName.endsWith(".xml") == false ) {
                continue;
            }
            int pos = fileName.indexOf('-');
            if( pos < 0 ) {
                continue;
            }
            String fileDate = fileName.substring(0, pos); // "2005.9.1"
            String extDate = DateFun.buildExtendedDate(fileDate); // "2005.09.01"
            if( extDate == null ) {
                continue;
            }
            if( extDate.compareTo( minDate ) < 0 ) {
                // file expired
                if( mode == ARCHIVE_MESSAGES ) {
                    String srcfile = sentMsg.getPath();
                    String targetfile = sentArchiveDir + // "archive/sent/" 
                                        extDate + // "2005.09.01"
                                        File.separator + sentMsg.getName(); // "/msg.xml"
                    File tfile = new File(targetfile);
                    tfile.getParentFile().mkdirs();
                    
                    boolean copyOk = FileAccess.copyFile(srcfile, targetfile);
                    if( copyOk == false ) {
                        logger.severe("Copy of sent msg to archive failed, source="+srcfile+"; target="+targetfile);
                        logger.severe("Processing stopped.");
                        return deleted;
                    }
                }
                // delete file after copy to archive OR if DELETE was requested
                if( sentMsg.delete() == true ) {
                    deleted++;
                } else {
                    logger.severe("Failed to delete expired sent file "+sentMsg.getPath());
                }
            }            
        }
        return deleted;
    }
    
    private static int processExpiredMessages(File boardFolder, int daysOld, int mode, String archiveDir) {

        int deleted = 0;
        
        String minDate = DateFun.getExtendedDate(daysOld);

        File[] boardFolderFiles = boardFolder.listFiles();
        if( boardFolderFiles == null ) {
            logger.severe("Could not get list of files for folder "+boardFolder.getPath());
            return 0;
        }
        for(int x=0; x < boardFolderFiles.length; x++) {
            File boardFolderFile = boardFolderFiles[x];
            if( boardFolderFile.isDirectory() ) {
                String boardDateFolder = boardFolderFile.getName(); // "2005.9.1"
                String extDate = DateFun.buildExtendedDate(boardDateFolder); // "2005.09.01"
                if( extDate == null ) {
                    continue;
                }
                if( extDate.compareTo( minDate ) < 0 ) {
                    // expired date folder
                    // process all contained ".xml" files
                    File[] boardDateFolderFiles = boardFolderFile.listFiles();
                    if( boardDateFolderFiles == null ) {
                        logger.severe("Could not get list of files for folder "+boardFolderFile.getPath());
                        return 0;
                    }
                    for(int y=0; y < boardDateFolderFiles.length; y++) {
                        File boardDateFolderFile = boardDateFolderFiles[y];
                        if( boardDateFolderFile.isFile() && boardDateFolderFile.getName().endsWith(".xml") ) {
                            // process this expired message
                            if( mode == ARCHIVE_MESSAGES ) {
                                String srcfile = boardDateFolderFile.getPath();
                                String targetfile = archiveDir + // "archive/messages/" 
                                                    boardFolder.getName() +   // "frost"
                                                    File.separator + extDate + // "/2005.09.01"
                                                    File.separator + boardDateFolderFile.getName(); // "/msg.xml"
                                File tfile = new File(targetfile);
                                tfile.getParentFile().mkdirs();
                                
                                boolean copyOk = FileAccess.copyFile(srcfile, targetfile);
                                if( copyOk == false ) {
                                    logger.severe("Copy of file to archive failed, source="+srcfile+"; target="+targetfile);
                                    logger.severe("Processing stopped.");
                                    return deleted;
                                }
                            }
                            // delete file after copy to archive OR if DELETE was requested
                            if( boardDateFolderFile.delete() == true ) {
                                deleted++;
                            } else {
                                logger.severe("Failed to delete expired file "+boardDateFolderFile.getPath());
                            }
                        }
                    }
                }
            }
        }
        return deleted;
    }
    
    /**
     * Deletes ALL expired indices files.
     * Expired means outdated file expiration time. 
     */
    private static void deleteExpiredIndicesFiles(List boardList) {
        // keep indices files for maxMessageDownload*2 days, but at least 10 days
        long maxDaysOld = Core.frostSettings.getIntValue("maxMessageDownload") * 2;
        if( maxDaysOld < 10 ) {
            maxDaysOld = 10;
        }
        long expiration = new Date().getTime() - (maxDaysOld * 24 * 60 * 60 * 1000);
        
        for(Iterator i=boardList.iterator(); i.hasNext(); ) {
            
            Board board = (Board)i.next();
            
            logger.info("Starting to delete all expired indices files in folder "+board.getBoardFilename()+
                    " with a modified date older than "+maxDaysOld+" days.");

            File boardFolder = new File(MainFrame.keypool + board.getBoardFilename());
            if( boardFolder.isDirectory() == false ) {
                logger.warning("No board folder for board "+board.getName());
                continue;
            }

            int deleted = 0;
            File[] boardFolderFiles = boardFolder.listFiles();
            for(int x=0; x < boardFolderFiles.length; x++) {
                File f = boardFolderFiles[x];
                if( f.isFile() && f.getName().startsWith("indices") && f.lastModified() < expiration ) {
                    if( f.delete() == true ) {
                        deleted++;
                    }
                }
            }
            logger.info("Finished to delete expired indices files in folder "+board.getBoardFilename()+
                    ", deleted "+deleted+" files.");
        }
    }
    
    /**
     * MUST run only during startup of Frost.
     * Removes all empty date directories in a board directory. 
     */
    public static void deleteEmptyBoardDateDirs(File keypoolFolder) {
        File[] boardDirs = keypoolFolder.listFiles();
        if( boardDirs == null || boardDirs.length == 0 ) {
            return;
        }
        logger.info("Starting to delete all empty board date directories.");
        // all board directories
        for(int x=0; x < boardDirs.length; x++) {
            if( boardDirs[x].isFile() ) {
                continue;
            }
            File[] dateDirs = boardDirs[x].listFiles();
            if( dateDirs == null || dateDirs.length == 0 ) {
                continue;
            }
            // all date directories
            for(int y=0; y < dateDirs.length; y++) {
                if( dateDirs[y].isFile() ) {
                    continue;
                }
                String[] filesList = dateDirs[y].list();
                if( filesList == null ) {
                    continue;
                }
                if( filesList.length == 0 ) {
                    // empty directory
                    dateDirs[y].delete();
                }
            }
        }
        logger.info("Finished to delete all empty board date directories.");
    }

    /**
     * Deletes all expired FILES.
     * Must not delete directories (could be a target for a running download,...). 
     * @param dirItem  Folder for recursion
     */
//    private static void recursDir(File dirItem, long expiration) {
//        
//        if( dirItem.isDirectory() ) {
//            
//            File[] list = dirItem.listFiles();
//            if( list == null ) {
//                return;
//            }
//            for( int i = 0; i < list.length; i++ ) {
//                File f = list[i];
//                recursDir( f, expiration );
//            }
//        } else {
//            // its a file
//            if( dirItem.lastModified() < expiration ) {
//                dirItem.delete();
//            }
//        }
//    }
}
