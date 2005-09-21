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

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.objects.Board;
import frost.identities.*;
import frost.messages.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class Index {
	
	private static Logger logger = Logger.getLogger(Index.class.getName());
	
	private static final String fileSeparator = System.getProperty("file.separator");
	
	private static boolean initialized = false;
	
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
		if (!initialized) {
			initialized = true;
			instance = new Index(downloadModel);
		}
	}

	/**
	 * Adds a keyfile to another counts the number of files shared
	 * and establishes the proper trust relationships
	 * @param keyfile the keyfile to add to the index
	 * @param target file containing index
	 * @param owner the trusted identity of the person sharing the files
	 */
	//REDFLAG: this method is called only from UpdateIdThread and that's why
	//I put the accounting for trustmap here.  Be careful when you change it!!
	private void add(File keyfile, File target, Identity owner) {
		try {
			if (!target.exists()) {
				target.createNewFile();
            }
		} catch (IOException e) {
			logger.log(
				Level.SEVERE,
				"Exception thrown in add(File keyfile, File target, Identity owner)",
				e);
		}
		FrostIndex chunk = FileAccess.readKeyFile(keyfile);
		Iterator it = chunk.getFiles().iterator();
		if (!owner
			.getUniqueName()
			.equals(Core.getInstance().getIdentities().getMyId().getUniqueName()))
        {
			while (it.hasNext()) {
				SharedFileObject current = (SharedFileObject) it.next();
				if (!current.getOwner().equals(owner.getUniqueName())) {
					owner.getTrustees().add(current.getOwner());
                }
				//FIXME: find a way to count the files each person has shared
				//without counting duplicates
			}
        }
		add(chunk, target);
	}

	/**
	 * adds the files from an index shared by an untrusted identity.  
	 * only those files shared directly by the person who inserted the index
	 * are considered.
	 * @param keyfile the newly downloaded keyfile
	 * @param target the already existing keyfile
	 * @param owner the unique name of the person who shared the file
	 */
	private void add(File keyfile, File target, String owner) {
		try {
			if (!target.exists())
				target.createNewFile();
		} catch (IOException e) {
			logger.log(
				Level.SEVERE,
				"Exception thrown in add(File keyfile, File target, String owner)",
				e);
		}
		FrostIndex idx = FileAccess.readKeyFile(keyfile);

		add(idx, target, owner);
	}

	/**
	 * @param keyFile
	 * @param board
	 * @param owner
	 */
	public void add(File keyFile, Board board, Identity owner) {
		add(
			keyFile,
			new File(MainFrame.keypool + board.getBoardFilename() + fileSeparator + "files.xml"),
			owner);
	}

	/**
	 * @param a
	 * @param b
	 */
	private void add(FrostIndex a, File b) {
		add(a.getFilesMap(), b);
	}
	/**
	 * @param a
	 * @param b
	 * @param owner
	 */
	private void add(FrostIndex a, File b, String owner) {
		add(a.getFilesMap(), b, owner);
	}

	/**
	 * @param a
	 * @param b
	 * @param owner
	 */
	public void add(FrostIndex a, Board b, String owner) {
		add(
			a.getFilesMap(),
			new File(MainFrame.keypool + b.getBoardFilename() + File.separator + "files.xml"),
			owner);
	}
	/**
	 * Adds a Map to an index located at target dir.
	 * Target dir will be created if it does not exist
	 * @param chunk the map to add to the index
	 * @param target directory containing index
	 * @param firstLetter identifier for the keyfile
	 */
	private void add(Map chunk, File target) {
		//final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
		//        final String fileSeparator = System.getProperty("file.separator");
		//final Map whole = Collections.synchronizedMap(new HashMap());

		FrostIndex whole = FileAccess.readKeyFile(target);

		//if( !target.isDirectory() && !target.getPath().endsWith("xml"))
		//  target.mkdir();

		Iterator i = chunk.values().iterator();
		while (i.hasNext()) {
			SharedFileObject current = (SharedFileObject) i.next();

			//update the download table
			if (current.getKey() != null) {
				updateDownloadTable(current);
            }

			SharedFileObject old = (SharedFileObject) whole.getFilesMap().get(current.getSHA1());

			if (old == null) {
				whole.getFilesMap().put(current.getSHA1(), current);
				continue;
			}
			old.setDate(current.getDate());
			old.setLastSharedDate(current.getLastSharedDate());
			old.setKey(current.getKey());
			//TODO: allow unsigned files to be appropriated
		}

		FileAccess.writeKeyFile(whole, target);
	}

	/**
	 * @param chunk
	 * @param target
	 * @param owner
	 */
	private void add(Map chunk, File target, String owner) {

		if (owner == null)
			owner = "Anonymous";
		FrostIndex idx = null;
		if (target.exists())
			idx = FileAccess.readKeyFile(target);
		else
			idx = new FrostIndex(new HashMap());

		//if( !target.isDirectory() && !target.getPath().endsWith("xml"))
		//  target.mkdir();

		Iterator i = chunk.values().iterator();
		while (i.hasNext()) {
			SharedFileObject current = (SharedFileObject) i.next();
			if (current.getOwner() != null && !current.getOwner().equals(owner))
				continue;
			//update the download table
			if (current.getKey() != null)
				updateDownloadTable(current);

			SharedFileObject old = (SharedFileObject) idx.getFilesMap().get(current.getSHA1());

			if (old == null) {
				idx.getFilesMap().put(current.getSHA1(), current);
				continue;
			}
			old.setDate(current.getDate());
			old.setLastSharedDate(current.getLastSharedDate());
			old.setKey(current.getKey());
			//TODO: allow unsigned files to be appropriated
		}

		FileAccess.writeKeyFile(idx, target);
	}
	/**
	 * Adds a key object to an index located at target dir.
	 * Target dir will be created if it does not exist
	 * @param key the key to add to the index
	 * @param target directory containing index
	 */
	private void add(SharedFileObject key, File target) {
		//final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
		//final String fileSeparator = System.getProperty("file.separator");
//		final String hash = key.getSHA1();

		if (key.getKey() != null)
			updateDownloadTable(key);

//		final Map chk = Collections.synchronizedMap(new HashMap());

		// File indexFile = new File(target.getPath()  + fileSeparator + "files.xml");
		File indexFile = target;
		try {
			if (!indexFile.exists())
				indexFile.createNewFile();
		} catch (IOException e) {
			logger.log(
				Level.SEVERE,
				"Exception thrown in add(SharedFileObject key, File target)",
				e);
		}
		FrostIndex idx = FileAccess.readKeyFile(indexFile);
		if (idx == null)
			idx = new FrostIndex(new HashMap());
		if (idx.getFiles().contains(key))
			idx.getFiles().remove(key);
		idx.getFilesMap().put(key.getSHA1(), key);
		FileAccess.writeKeyFile(idx, indexFile);
	}

	/**
	 * @param key
	 * @param board
	 */
	public void add(SharedFileObject key, Board board) {
		//final String fileSeparator = System.getProperty("file.separator");
		File boardDir = new File(MainFrame.keypool + board.getBoardFilename());

		if (!(boardDir.exists() && boardDir.isDirectory()))
			boardDir.mkdir();
		if (key.getKey() != null)
			updateDownloadTable(key);
		add(
			key,
			new File(MainFrame.keypool + board.getBoardFilename() + fileSeparator + "files.xml"));
	}

	/**
	 * @param key
	 * @param board
	 */
	public void addMine(SharedFileObject key, Board board) {
		//final String fileSeparator = System.getProperty("file.separator");
		File boardDir = new File(MainFrame.keypool + board.getBoardFilename());

		if (!(boardDir.exists() && boardDir.isDirectory()))
			boardDir.mkdir();
		add(key, new File(boardDir.getPath() + fileSeparator + "new_files.xml"));
	}
	/**
	 * This method puts the SharedFileObjects into the target set and 
	 * returns the number of the files shared by the user himself
	 * @param board
	 * @return
	 */
	public Map getUploadKeys(String board) {

		boolean reSharing = false;
		boolean newFiles = false;

		FrostIndex totalIdx = null;
		FrostIndex _toUpload = null;

		logger.fine("Index.getUploadKeys(" + board + ")");

		// Abort if boardDir does not exist
		File boardNewUploads = new File(MainFrame.keypool + board + fileSeparator + "new_files.xml");
		// if( !boardNewUploads.exists() )
		//   return 0;

		File boardFiles = new File(MainFrame.keypool + board + fileSeparator + "files.xml");

		totalIdx = FileAccess.readKeyFile(boardFiles);
		_toUpload = FileAccess.readKeyFile(boardNewUploads);

		if (boardNewUploads.exists()) {
			newFiles = true;
			boardNewUploads.delete();
		}

		Map toUpload = _toUpload.getFilesMap();

		//add friends's files and maybe reshare my files
        
		// add a limit -> key file could grow above 30.000 bytes which is the appr. maximum for KSK uploads!
        //  (metadata size must be added to data size)
        final int MAX_FILES = 100; // 100 seems to be a good size
        // if not all files fit into this index, we send the next list on next update

		String myUniqueName = Core.getInstance().getIdentities().getMyId().getUniqueName();
		int downloadBack = MainFrame.frostSettings.getIntValue("maxAge");
		logger.info("re-sharing files shared before " + DateFun.getDate(downloadBack));
        
		// first add all of my files
        
		for(Iterator i = totalIdx.getFiles().iterator(); i.hasNext(); ) {
			SharedFileObject current = (SharedFileObject) i.next();

			if( current.getOwner() != null && //not anonymous 
                current.getOwner().compareTo(myUniqueName) == 0 && //from myself
                current.getLastSharedDate() != null) { //not from the old format
                
			    // add my file if its been shared too long ago
				if (DateFun.getExtendedDate(downloadBack).compareTo(current.getLastSharedDate()) > 0) {
					// if the file has been uploaded too long ago, set it to offline again
					if (!current.checkDate()) {
						current.setDate(null);
						//current.setKey(null); // TODO: could we keep the chk key? I try it :)
						logger.fine("o"); //o means assumed fallen off freenet
						//NOTE: This will not remove the CHK from the upload table. 
						//however, when the other side receives the index they will see the file "offline"
                        // -> but now they still have the key...
					}
                    current.setLastSharedDate(DateFun.getExtendedDate());
					toUpload.put(current.getSHA1(), current);
					logger.fine("d");
					reSharing = true;
					//d means it was shared too long ago
                    
                    if( toUpload.size() >= MAX_FILES ) {
                        break; // index file full
                    }
				}
			}
		}
        
        // if no own files must be send, don't send only friends files
        if( toUpload.size() == 0 ) {
            return null;
        }

        // if there is space in the file, add random files of friends
        // TODO: we could mark helped files and send some other on next run (use e.g. a lastHelpedDate)
        //       for now we collect all possible files and shuffle them

        if( toUpload.size() < MAX_FILES ) {

            // collect ALL friends files
            ArrayList friendsFiles = new ArrayList();
            for(Iterator i = totalIdx.getFiles().iterator(); i.hasNext(); ) {
                
                SharedFileObject current = (SharedFileObject) i.next();
                Identity id = Core.getInstance().getIdentities().getIdentity(current.getOwner());
                if( id != null && //not anonymous
                    myUniqueName.compareTo(current.getOwner()) != 0 && //not myself
                    MainFrame.frostSettings.getBoolValue("helpFriends") && //and helping is enabled
                    id.getState() == FrostIdentities.FRIEND ) //and marked GOOD 
                {
                    // add friends files
                    friendsFiles.add( current );
                }
            }
            
            // shuffle ALL files
            Collections.shuffle(friendsFiles);
            
            // add files until index file is full
            for(Iterator i = friendsFiles.iterator(); i.hasNext(); ) {
                SharedFileObject current = (SharedFileObject) i.next();
                toUpload.put(current.getSHA1(), current);
                logger.fine("f"); //f means added file from friend
                
                if( toUpload.size() >= MAX_FILES ) {
                    break;
                }
            }
        }

		// update the lastSharedDate of MY shared files
		if (reSharing) {
			FileAccess.writeKeyFile(totalIdx, boardFiles);
		}

		//return anything only if we either re-shared old files or have new files to upload.
		if (reSharing || newFiles) {
			//update the last shared date
			for(Iterator it2 = toUpload.values().iterator(); it2.hasNext();  ) {
				SharedFileObject obj = (SharedFileObject) it2.next();
                // TODO: why do we change the dateShared for files of friends? the friend did not reshare the file!
				obj.setLastSharedDate(DateFun.getExtendedDate());
			}
			return toUpload;
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 */
	private void updateDownloadTable(SharedFileObject key) {
		//this really shouldn't happen
		if (key == null || key.getSHA1() == null) {
			logger.warning("null value in index.updateDownloadTable");
			if (key != null)
				logger.warning("SHA1 null!");
			else
				logger.warning("key null!");
			return;
		}

		for (int i = 0; i < downloadModel.getItemCount(); i++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(i);
			if (dlItem.getState() == FrostDownloadItem.STATE_REQUESTED
				&& dlItem.getSHA1() != null
				&& dlItem.getSHA1().compareTo(key.getSHA1()) == 0) {
				dlItem.setKey(key.getKey());
				dlItem.setFileAge(key.getDate());
				break;
			}

		}
	}
}
