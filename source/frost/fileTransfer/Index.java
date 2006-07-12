/*
  Index.java / Database Access
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

/**
 * This class maintains the board index files.
 * All methods MUST be called externally synchronized:
 *
 *   Index index = Index.getInstance();
 *   synchronized(index) {
 *       index.add(...);
 *   }
 *
 * This way we synchronize all Index calls for all boards,
 * this is ok i think. If we need even more performance we
 * would have to synchronize on Index calls for one board.
 *
 * Scan for calls to Index.getInstance() to find all points
 * where Index is used.
 */
public class Index {

    private static Logger logger = Logger.getLogger(Index.class.getName());

    private DownloadModel downloadModel;
    private UploadModel uploadModel;

    /**
     * The unique instance of this class.
     */
    private static Index instance = null;

    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static Index getInstance() {
        return instance;
    }

    /**
     * Prevent instances of this class from being created from the outside.
     */
    private Index(DownloadModel downloadModel, UploadModel uploadModel) {
        super();
        this.downloadModel = downloadModel;
        this.uploadModel = uploadModel;
    }

    /**
     * This method initializes the Index.
     * If it has already been initialized, this method does nothing.
     */
    public static void initialize(DownloadModel downloadModel, UploadModel uploadModel) {
        if( instance == null ) {
            instance = new Index(downloadModel, uploadModel);
        }
    }

    /**
     * Adds given filesToAdd to the FILELIST.
     * If owner is null, all files are added, else only files from owner.
     * Files in downloadtable are updated if a key arrives.
     *
     * @param chunk    Map of SharedFileObjects to add to index
     * @param addOnlyFromThisOwner    if unique name then only files from this name are added, if null ALL files in index are added
     */
    public void add(Collection filesToAdd, String addOnlyFromThisOwner) {

        if( filesToAdd.size() == 0 ) {
            return; // nothing to add
        }

        for(Iterator i = filesToAdd.iterator(); i.hasNext(); ) {

            SharedFileXmlFile current = (SharedFileXmlFile) i.next();

            if (addOnlyFromThisOwner != null &&
                current.getOwner() != null &&
                !current.getOwner().equals(addOnlyFromThisOwner))
            {
                continue;
            }

            // update the download table
            if (current.getKey() != null) {
                updateDownloadTable(current);
            }

            FrostSharedFileObject fo = new FrostSharedFileObject(current);
            fo.setLastReceived(DateFun.getCurrentSqlDateGMT());
            
            try {
                AppLayerDatabase.getFileListDatabaseTable().insertOrUpdateFrostSharedFileObject(fo);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "insert/update to database failed", e);
            }
        }
    }
    
    /**
     * Return the sha1 of the files that wait to be requested on this board.
     */
    public List getRequestKeys(Board board) {
        
        List items = new LinkedList();
        
        for (int i = 0; i < downloadModel.getItemCount(); i++) {
            FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(i);
            if (dlItem.getState() == FrostDownloadItem.STATE_REQUESTING
                && dlItem.getSourceBoard().getName().equals(board.getName())    
                && dlItem.getSHA1() != null )
            {
                items.add( dlItem );
            }
        }
        return items;
    }
    
    public void processRequests(List requestList) {
        
        int rowCount = uploadModel.getItemCount();
        for (int i = 0; i < rowCount; i++) {
            FrostUploadItem ulItem = (FrostUploadItem) uploadModel.getItemAt(i);
            String SHA1 = ulItem.getSHA1();
            if (SHA1 == null) {
                continue;
            }

            for(Iterator j=requestList.iterator(); j.hasNext(); ) {
                
                String content = (String)j.next();
                content = content.trim();

                if (SHA1.equals(content)) {
                    java.sql.Date lastUploaded = ulItem.getLastUploadDate();
                    boolean startUpload = false;
                    // start upload if not uploaded in last 3 days
                    if( lastUploaded == null ) {
                        startUpload = true;
                    } else {
                        long minDiff = 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds
                        java.sql.Date now = DateFun.getCurrentSqlDateGMT();
                        if( lastUploaded.getTime() + minDiff < now.getTime() ) {
                            startUpload = true;
                        }
                    }

                    if(startUpload) {
                        // for handling of ENCODING state see ulItem.getNextState() javadoc
                        // changing state ENCODING_REQUESTED to REQUESTED is ok!
                        if (ulItem.getState() != FrostUploadItem.STATE_UPLOADING &&
                            ulItem.getState() != FrostUploadItem.STATE_PROGRESS)
                        {
                            if (ulItem.getState() == FrostUploadItem.STATE_ENCODING) {
                                ulItem.setNextState(FrostUploadItem.STATE_REQUESTED);
                            } else {
                                ulItem.setState(FrostUploadItem.STATE_REQUESTED);
                            }
                        } else {
                            logger.fine("file already in state uploading/progress");
                        }
                    } else {
                        logger.info("File with hash "+SHA1+" was requested, but already uploaded today");
                    }
                }
            }
        }
    }

    /**
     * This method puts the SharedFileObjects into the target set and
     * returns the number of the files shared by the user
     */
    public List getUploadKeys(Board board) {
        // get files to share from UPLOADFILES
        // - max. 250 keys per fileindex
        // - get keys of only 1 owner/anonymous, next time get keys from different owner

        List localIdentities = Core.getIdentities().getLocalIdentities();
        int identityCount = localIdentities.size() + 1; // +1 anonymous 
        int maxKeys = 250;
        while(identityCount > 0) {

            LocalIdentity idToUpdate = null;
            long minUpdateMillis = LocalIdentity.getAnonymousLastFilesSharedMillis(board.getName());
            
            for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
                LocalIdentity id = (LocalIdentity)i.next();
                long lastShared = id.getLastFilesSharedMillis(board.getName());
                if( lastShared < minUpdateMillis ) {
                    minUpdateMillis = lastShared;
                    idToUpdate = id;
                }
            }

            // idToUpdate is id, or null for anonymous
            // mark that we tried this owner
            if( idToUpdate == null ) {
                LocalIdentity.updateAnonymousLastFilesSharedMillis(board.getName());
            } else {
                idToUpdate.updateLastFilesSharedMillis(board.getName());
            }

            String owner = null;
            if( idToUpdate != null ) {
                owner = idToUpdate.getUniqueName();
            }
            List filesToShare = uploadModel.getUploadItemsToShare(board, owner, maxKeys);
            if( filesToShare.size() > 0 ) {
                return filesToShare;
            }
            // else try next owner
            identityCount--;
        }
        return null;
    }

    /**
     * If the file is currently in download table, we update its key and date.
     */
    private void updateDownloadTable(SharedFileXmlFile key) {
        // don't update with invalid data
        if (key == null || key.getSHA1() == null || key.getKey() == null && key.getKey().length() == 0) {
            return;
        }

        for (int i = 0; i < downloadModel.getItemCount(); i++) {
            FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(i);
            if( dlItem.getSHA1() != null
                && dlItem.getSHA1().compareTo(key.getSHA1()) == 0
                && dlItem.getKey() == null )
            {
                dlItem.setKey(key.getKey());
//                dlItem.setState(FrostDownloadItem.STATE_WAITING);
                break;
            }
        }
    }
}
