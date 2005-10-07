/*
  Index.java / Database Access
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;

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
	private Index(DownloadModel downloadModel) {
		super();
		this.downloadModel = downloadModel;
	}
	
	/**
	 * This method initializes the Index.
	 * If it has already been initialized, this method does nothing.
	 */
	public static void initialize(DownloadModel downloadModel) {
		if( instance == null ) {
			instance = new Index(downloadModel);
		}
	}

    /**
     * Adds all files/only files from specified owner to the board index of files.
     * 
     * @param otherIndex
     * @param board
     * @param addOnlyFromOwner
     */
    public void add(FrostIndex otherIndex, Board board, String addOnlyFromOwner) {
        add(otherIndex.getFilesMap().values(), board, "files.xml", addOnlyFromOwner);
    }

    /**
     * Adds a key to the board index of files.
     * 
     * @param key
     * @param board
     */
    public void add(SharedFileObject key, Board board) {
        add(Collections.singletonList(key), board, "files.xml", null);
    }

    /**
     * Adds a key to list of new files that wait for upload.
     * 
     * @param key
     * @param board
     */
    public void addMine(SharedFileObject key, Board board) {
        add(Collections.singletonList(key), board, "new_files.xml", null);
    }

	/**
     * Adds given filesToAdd to the index file given in targetFilename.
     * If owner is null, all files are added, else only files from owner.
     * 
	 * @param chunk    Map of SharedFileObjects to add to index
	 * @param target   target index file (may exist)
	 * @param addOnlyFromThisOwner    if unique name then only files from this name are added, if null ALL files in index are added
	 */
	private void add(Collection filesToAdd, Board b, String targetFilename, String addOnlyFromThisOwner) {

        if( filesToAdd.size() == 0 ) {
            return; // nothing to add
        }

        // ensure board dir
        File boardDir = new File(MainFrame.keypool + b.getBoardFilename());
        if (!(boardDir.exists() && boardDir.isDirectory())) {
            boardDir.mkdir();
        }

        File target = new File(MainFrame.keypool + b.getBoardFilename() + File.separator + targetFilename);

        // load existing index file
		FrostIndex idx = null;
		if (target.isFile()) {
			idx = readKeyFile(target);
        }
        if (idx == null) {
            idx = new FrostIndex(new HashMap());
        }
        
		for(Iterator i = filesToAdd.iterator(); i.hasNext(); ) {

			SharedFileObject current = (SharedFileObject) i.next();

            if( current.getSHA1() == null ) {
                logger.log(Level.WARNING, "Index.add(): keys SHA1 is null: "+current.getFilename());
                continue;
            }

			if (addOnlyFromThisOwner != null &&
                current.getOwner() != null && 
                !current.getOwner().equals(addOnlyFromThisOwner)) 
            {
				continue;
            }
            
            if( !current.isValid() ) {
                logger.info("Index.add(): Refused to add an invalid key to the index "+target.getPath()+
                            " (key="+current.getFilename()+")");
                continue;
            }

			// update the download table
			if (current.getKey() != null) {
				updateDownloadTable(current);
            }

			SharedFileObject old = (SharedFileObject) idx.getFilesMap().get(current.getSHA1());

			if (old == null) {
                // add new file
				idx.getFilesMap().put(current.getSHA1(), current);
			} else {
                // update existing file
                old.setDate(current.getDate());
                old.setLastSharedDate(current.getLastSharedDate());
                old.setKey(current.getKey());
            }
		}
		writeKeyFile(idx, target);
	}
    
    /**
     * Serializes the XML to bytes, zips the bytes and returns the length of zipped content.
     * 
     * @param idx  FrostIndex to zip
     * @return  length of zipped content
     */
    private long determineZippedSize(FrostIndex idx) {
        File tmp = null;
        try {
            tmp = File.createTempFile("index_", ".ziptmp", new File(MainFrame.frostSettings.getValue("temp.dir")));
        } catch(Exception ex) {
            // this should never happen, but for the case ...
            tmp = new File("index_"+System.currentTimeMillis());
        }

        FileAccess.writeZipFile(XMLTools.getRawXMLDocument(idx), "entry", tmp);
        
        long result = tmp.length();
        tmp.delete();
        
        return result;
    }

	/**
     * TODO: the Map returned by this method MUST be uploaded, if uploading
     *       is cancelled (Frost shutdown,...) the files to upload are lost!
     *       
	 * This method puts the SharedFileObjects into the target set and 
	 * returns the number of the files shared by the user
     * 
	 * @param boardFilename
	 * @return
	 */
	public Map getUploadKeys(String boardFilename) {
        
        // TODO: add files until zip is larger than 30000, don't stop at 50 (nice to have)

        // add a limit -> key file could grow above 30.000 bytes which is the appr. maximum for KSK uploads!
        final long MAX_ZIP_SIZE = 30000;
        final int MAX_FILES_CHANGE_INTERVAL = 5;
        final int DEFAULT_MAX_FILES = 50;
        
        int currentMaxFiles = DEFAULT_MAX_FILES; // seems to be a good size to start with
        // (if not all files fit into this index, we send the next list on next update)

		logger.fine("Index.getUploadKeys(" + boardFilename + ")");

		// Abort if boardDir does not exist
        File boardDir = new File(MainFrame.keypool + boardFilename);
        if( !boardDir.exists() || !boardDir.isDirectory() ) {
            return null;
        }

		File boardFiles = new File(MainFrame.keypool + boardFilename + File.separator + "files.xml");
        File boardNewFiles = new File(MainFrame.keypool + boardFilename + File.separator + "new_files.xml");

        Map toUpload = new HashMap();

        // get new files added by me
        FrostIndex newUploadsIdx = readKeyFile(boardNewFiles);
        
        while(true) {

            for(Iterator i = newUploadsIdx.getFilesMap().values().iterator(); i.hasNext(); ) {
                SharedFileObject sfo = (SharedFileObject)i.next();
                if( toUpload.size() < currentMaxFiles ) {
                    toUpload.put(sfo.getSHA1(), sfo);
                } else {
                    break;
                }
            }
            if( toUpload.size() == 0 ) {
                break;
            }
            // determine final zip file size
            long len = determineZippedSize(new FrostIndex(toUpload));
            if( len == 0 ) {
                logger.severe("FATAL ERROR: determineZippedSize() did not create a zip file.");
                return null;
            } else if( len > MAX_ZIP_SIZE ) {
                // zip file too large
                toUpload.clear();
                if( currentMaxFiles == 1 ) {
                    // ERROR, a single file does not fit into the index
                    logger.severe("FATAL ERROR: a single file does not fit into the index file. zipsize="+len);
                    return null;
                } else {
                    currentMaxFiles -= MAX_FILES_CHANGE_INTERVAL; // decrease max files
                    if( currentMaxFiles <= 0 ) {
                        currentMaxFiles = 1;
                    }
                }
            } else {
                break;
            }
        }
        
        // remove those files from newUploadsIdx that we added to toUpload
        for(Iterator i = toUpload.values().iterator(); i.hasNext(); ) {
            SharedFileObject sfo = (SharedFileObject)i.next();
            newUploadsIdx.getFilesMap().remove(sfo.getSHA1());
        }

        // save or delete the newUploadsIdx file
        if( newUploadsIdx.getFilesMap().size() == 0 ) {
            // we put all new files into our toUpload
            if (boardNewFiles.isFile()) {
                boardNewFiles.delete();
            }
        } else {
            // save remaining files for next run
            writeKeyFile(newUploadsIdx, boardNewFiles);
        }
        
        // finished to add my new files
        
        // now add all of my files that need to be reshared
        
        FrostIndex totalIdx = null;
        boolean reSharing = false;
        // check if we already lowered MAX_FILES, if yes we are finished (no more space)
        if( currentMaxFiles == DEFAULT_MAX_FILES ) {
            
            totalIdx = readKeyFile(boardFiles);
            
            String myUniqueName = Core.getInstance().getIdentities().getMyId().getUniqueName();
            int downloadBack = MainFrame.frostSettings.getIntValue("maxAge");
            logger.info("re-sharing files shared before " + DateFun.getDate(downloadBack));

            String minDate = DateFun.getExtendedDate(downloadBack);
            
            Map tmpToUpload = new HashMap();
            ArrayList resharedFilesList = new ArrayList();
            while(true) {

                tmpToUpload.putAll(toUpload);
                
                for(Iterator i = totalIdx.getFilesMap().values().iterator(); i.hasNext(); ) {
                    
                    SharedFileObject current = (SharedFileObject) i.next();

                    if( tmpToUpload.size() >= currentMaxFiles ) {
                        break; // index file full
                    }

                    if( current.getOwner() != null && // not anonymous 
                        current.getOwner().compareTo(myUniqueName) == 0 && // from myself
                        current.getLastSharedDate() != null && // not from the old format
                        minDate.compareTo(current.getLastSharedDate()) > 0 ) // add my file if its been shared too long ago
                    {
                        tmpToUpload.put(current.getSHA1(), current); // we change the lastShared Date for this later, see below
                        resharedFilesList.add(current); // we change the lastShared Date for this later, see below
                    }
                }
                if( resharedFilesList.size() == 0 ) {
                    // no files added to reshare, skip zip check, zipsize was validated before
                    break;
                }
                // determine final zip file size of tmpToUpload map
                long len = determineZippedSize(new FrostIndex(tmpToUpload));
                if( len == 0 ) {
                    logger.severe("FATAL ERROR: determineZippedSize() did not create a zip file.");
                    return null;
                } else if( len > MAX_ZIP_SIZE ) {
                    // zip file too large, reset and restart
                    tmpToUpload.clear();
                    resharedFilesList.clear();
                    
                    if( currentMaxFiles == 1 ) {
                        // ERROR, a single file does not fit into the index
                        logger.severe("FATAL ERROR: a single file does not fit into the index file. zipsize="+len);
                        return null;
                    } else {
                        currentMaxFiles -= MAX_FILES_CHANGE_INTERVAL; // decrease max files
                        if( currentMaxFiles <= 0 ) {
                            currentMaxFiles = 1;
                        } 
                        if( currentMaxFiles <= toUpload.size() ) {
                            break; // toUpload was full enough
                        }
                    }
                } else {
                    // finished, tmpToUpload is the new final map
                    toUpload = tmpToUpload;
                    reSharing = true;
                    // process date of all added reshared entries
                    for(Iterator i = resharedFilesList.iterator(); i.hasNext(); ) {
                        SharedFileObject current = (SharedFileObject) i.next();
                        // if the file has been uploaded too long ago, set it to offline again
                        if (!current.checkDate()) {
                            // NOTE: This will not remove the CHK from the upload table. 
                            // however, when the other side receives the index they will see the file "offline"
                            current.setDate(null);
                        }
                    }
                    break;
                }
            }
        }
        
        // if no own files are to be send, break and don't send only friends files
        if( toUpload.size() == 0 ) {
            return null;
        }

        // update the last shared date for all MY files before we send
        String currentDate = DateFun.getExtendedDate();
        for(Iterator i = toUpload.values().iterator(); i.hasNext();  ) {
            SharedFileObject obj = (SharedFileObject) i.next();
            obj.setLastSharedDate(currentDate);
        }
        
        // save the new lastSharedDate/date of MY re-shared files to disk
        if (reSharing) {
            writeKeyFile(totalIdx, boardFiles);
        }

//      NOTE: due to troubles with too large index files we currently do not send friends shared files
//      imho this did'nt help alot anyway        

        return toUpload;

        // if there is space in the file, add random files of friends
        // TODO: we could mark helped files and send some other on next run (use e.g. a lastHelpedDate)
        //       for now we collect all possible files and shuffle them
//        if( toUpload.size() < MAX_FILES ) {
//
//            // collect ALL friends files
//            ArrayList friendsFiles = new ArrayList();
//            for(Iterator i = totalIdx.getFilesMap().values().iterator(); i.hasNext(); ) {
//                
//                SharedFileObject current = (SharedFileObject) i.next();
//                Identity id = Core.getInstance().getIdentities().getIdentity(current.getOwner());
//                if( id != null && //not anonymous
//                    myUniqueName.compareTo(current.getOwner()) != 0 && //not myself
//                    MainFrame.frostSettings.getBoolValue("helpFriends") && //and helping is enabled
//                    id.getState() == FrostIdentities.FRIEND ) //and marked GOOD 
//                {
//                    // add friends files
//                    friendsFiles.add( current );
//                }
//            }
//            
//            // shuffle ALL files
//            Collections.shuffle(friendsFiles);
//            
//            // add files until index file is full
//            for(Iterator i = friendsFiles.iterator(); i.hasNext(); ) {
//                SharedFileObject current = (SharedFileObject) i.next();
//                toUpload.put(current.getSHA1(), current);
//                logger.fine("f"); //f means added file from friend
//                
//                if( toUpload.size() >= MAX_FILES ) {
//                    break;
//                }
//            }
//        }
	}

	/**
     * If the files is currently in download table, we update
     * its key and date.
     * 
	 * @param key
	 */
	private void updateDownloadTable(SharedFileObject key) {
		// this really shouldn't happen
		if (key == null || key.getSHA1() == null) {
			logger.warning("null value in index.updateDownloadTable");
			if (key != null) {
				logger.warning("SHA1 null!");
            } else {
				logger.warning("key null!");
            }
			return;
		}

		for (int i = 0; i < downloadModel.getItemCount(); i++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(i);
			if (dlItem.getState() == FrostDownloadItem.STATE_REQUESTED
				&& dlItem.getSHA1() != null
				&& dlItem.getSHA1().compareTo(key.getSHA1()) == 0) 
            {
                if( key.getKey() != null && key.getKey().length() > 0 ) {
                    // never clear an (maybe) existing key
                    dlItem.setKey(key.getKey());
                }
				dlItem.setFileAge(key.getDate());
				break;
			}
		}
	}
    
    /**
     * Reads a keyfile from disk.
     * 
     * @param source     keyfile as String or as File
     * @returns          null on error
     */
    public FrostIndex readKeyFile(File source) {
        if( !source.isFile() || !(source.length() > 0) ) {
            return new FrostIndex(new HashMap());
        } else {
            // parse the xml file
            Document d = null;
            try {
                d = XMLTools.parseXmlFile(source.getPath(), false);
            } catch (IllegalArgumentException t) {
                logger.log(Level.SEVERE, "Exception thrown in readKeyFile(File source): \n"
                        + "Offending file saved as badfile.xml - send it to a dev for analysis", t);
                File badfile = new File("badfile.xml");
                source.renameTo(badfile);
            }

            if( d == null ) {
                logger.warning("Couldn't parse index file.");
                return null;
            }

            FrostIndex idx = new FrostIndex(d.getDocumentElement());

            // now go through all the files
            for(Iterator i = idx.getFilesMap().values().iterator(); i.hasNext(); ) {
                SharedFileObject newKey = (SharedFileObject) i.next();

                // validate the key
                if( !newKey.isValid() ) {
                    i.remove();
//                    logger.warning("invalid key found");
                    continue;
                }
            }
            return idx;
        }
    }

    private void writeKeyFile(FrostIndex idx, File destination) {
        if( idx.getFilesMap().size() == 0 ) {
            // no items to write
            return;
        }

        File tmpFile = new File(destination.getPath() + ".tmp");

        // use FrostIndex object

        int itemsAppended = 0;
        synchronized( idx ) {
            for(Iterator i = idx.getFilesMap().values().iterator(); i.hasNext(); ) {
                SharedFileObject current = (SharedFileObject) i.next();
                if( current.getOwner() != null ) {
                    Identity id = Core.getInstance().getIdentities().getIdentity(current.getOwner());
                    if( id != null && id.getState() == FrostIdentities.ENEMY ) {
                        // Core.getOut().println("removing file from BAD user");
                        i.remove();
                        continue;
                    }
                }
                itemsAppended++;
            }
        }

        if( itemsAppended == 0 ) {
            // don't write file
            return;
        }

        // xml tree created, now save

        boolean writeOK = false;
        try {
            Document doc = XMLTools.getXMLDocument(idx);
            writeOK = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception thrown in writeKeyFile(FrostIndex idx, File destination)", t);
        }

        if( writeOK ) {
            File oldFile = new File(destination.getPath() + ".old");
            oldFile.delete();
            destination.renameTo(oldFile);
            tmpFile.renameTo(destination);
        } else {
            // delete incomplete file
            tmpFile.delete();
        }
    }
}
