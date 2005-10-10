/*
  UpdateIdThread.java / Frost
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
package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.Index;
import frost.crypt.SignMetaData;
import frost.gui.objects.Board;
import frost.identities.*;
import frost.messages.FrostIndex;
import frost.messaging.MessageHashes;

public class UpdateIdThread extends Thread // extends BoardUpdateThreadObject implements BoardUpdateThread
{
    //private static int keyCount = 0;
    //private static int minKeyCount = 50;
    //private static int maxKeysPerFile = 5000;
//  private int maxKeys;
    
    private static Logger logger = Logger.getLogger(UpdateIdThread.class.getName());
    
    private String date;
    private int requestHtl;
    private int insertHtl;
    private String keypool;
    private Board board;
    private String publicKey;
    private String privateKey;
    private String requestKey;
    private String insertKey;
    private final static String fileSeparator = System.getProperty("file.separator");
	private MessageHashes messageHashes;
    
    private boolean isForToday = false;
    private FrostIdentities identities;
    
    private IndexSlots indexSlots;
    private final static int MAX_SLOTS_PER_DAY = 100;
    
//    public int getThreadType() { 
//        return BoardUpdateThread.BOARD_FILE_DNLOAD; 
//    }

    // TODO: if we fail to upload here, the file to upload should be uploaded next time!
    /**
     * Returns true if no error occured.
     */
    private boolean uploadIndexFile() throws Throwable {
        
        logger.info("FILEDN: UpdateIdThread - makeIndexFile for " + board.getName());
        
        if( indexSlots.findFirstFreeUploadSlot() < 0 ) {
            // no free upload slot, don't continue now, continue tomorrow
            return true;
        }

        // Calculate the keys to be uploaded
        Map files = null;
        Index index = Index.getInstance();
        synchronized(index) {
            // this method checks the final zip size (<=30000) !!!
            files = index.getUploadKeys(board);
        }
        
        if(files == null || files.size() == 0 ) {
            logger.info("FILEDN: No keys to upload, stopping UpdateIdThread for " + board.getName());
            return true;
        }
        
        logger.info("FILEDN: Starting upload of index file to board " + board.getName()+"; files="+files.size());

        FrostIndex frostIndex = new FrostIndex(files);
        files = null;
        File uploadIndexFile = new File(keypool + board.getBoardFilename() + "_upload.zip");

        // zip the xml file before upload
        FileAccess.writeZipFile(XMLTools.getRawXMLDocument(frostIndex), "entry", uploadIndexFile);
        frostIndex = null;
        
        if( !uploadIndexFile.isFile() || uploadIndexFile.length() == 0 ) {
            logger.warning("No index file to upload, save/zip failed.");
            return false; // error
        }

        boolean success = uploadFile(uploadIndexFile);
        if( success ) {
            uploadIndexFile.delete();
        }
        return success;
    }

    /**
     * Uploads the zipped index file.
     * 
     * @param zippedIndexFile
     * @param metadata
     */
    private boolean uploadFile(File zippedIndexFile) {
        
        // TODO: generalize this and use it in MessageUploadThread too??
        
        boolean success = false;

        try {

            // sign zip file if requested
            boolean signUpload = MainFrame.frostSettings.getBoolValue("signUploads");
            byte[] metadata = null;
            if( signUpload ) {
                byte[] zipped = FileAccess.readByteArray(zippedIndexFile);
                SignMetaData md = new SignMetaData(zipped, identities.getMyId());
                metadata = XMLTools.getRawXMLDocument(md);
            }
    
            int tries = 0;
            final int maxTries = 3;
            int index = indexSlots.findFirstFreeUploadSlot();
            while( !success && 
                   tries < maxTries &&
                   index > -1 ) // no free index found 
            {
                logger.info("Trying index file upload to index "+index);
                // Does this index already exist?
                String[] result = FcpInsert.putFile(
                        insertKey + index + ".idx.sha3.zip", // this format is sha3 ;)
                        zippedIndexFile, 
                        metadata, 
                        insertHtl, 
                        false); // doRedirect

                if( result[0].equals("Success") ) {
                    success = true;
                    // my files are already added to totalIdx, we don't need to download this index
                    indexSlots.setSlotUsed(index);
                    logger.info("FILEDN: Index file successfully uploaded.");
                } else {
                    if( result[0].equals("KeyCollision") ) {
                        index = indexSlots.findNextFreeSlot(index); 
                        tries = 0; // reset tries
                        logger.info("FILEDN: Index file collided, increasing index.");
                        continue;
                    } else {
                        String tv = result[0];
                        if( tv == null ) {
                            tv = "";
                        }
                        logger.info("FILEDN: Unknown upload error (#" + tries + ", '" + tv+ "'), retrying.");
                    }
                }
                tries++;
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception in uploadFile", e);
        }
        logger.info("FILEDN: Index file upload finished, file uploaded state is: "+success);
        return success;
    }
    
    // If we're getting too much files on a board, we lower
    // the maxAge of keys. That way older keys get removed
    // sooner. With the new index system it should be possible
    // to work with large numbers of keys because they are
    // no longer kept in memory, but on disk.
//    private void adjustMaxAge(int count) {/*  //this is not used
//    //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: old value = " + frame1.frame1.frostSettings.getValue("maxAge"));
//
//    int lowerLimit = 10 * maxKeys / 100;
//    int upperLimit = 90 * maxKeys / 100;
//    int maxAge = frame1.frame1.frame1.frostSettings.getIntValue("maxAge");
//
//    if (count < lowerLimit && maxAge < 21)
//        maxAge++;
//    if (count > upperLimit && maxAge > 1)
//        maxAge--;
//
//    frame1.frame1.frame1.frostSettings.setValue("maxAge", maxAge);
//    //if (DEBUG) Core.getOut().println("FILEDN: AdjustMaxAge: new value = " + maxAge);*/
//    }

	public void run() {
//		notifyThreadStarted(this);

		try {
			// Wait some random time to speed up the update of the TOF table
			// ... and to not to flood the node
			int waitTime = (int) (Math.random() * 2000);
			// wait a max. of 2 seconds between start of threads
			Mixed.wait(waitTime);

            int maxFailures;
            if (isForToday) {
                maxFailures = 3; // skip a maximum of 2 empty slots for today
            } else {
                maxFailures = 2; // skip a maximum of 1 empty slot for backload
            }
            int index = indexSlots.findFirstFreeDownloadSlot();
            int failures = 0;
			while (failures < maxFailures && index >= 0 ) {
                
				File target = File.createTempFile(
						"frost-index-" + index,
						board.getBoardFilename(),
						new File(MainFrame.frostSettings.getValue("temp.dir"))); 

				logger.info("FILEDN: Requesting index " + index + " for board " + board.getName() + " for date " + date);

				// Download the keyfile
				FcpResults fcpresults = FcpRequest.getFile(
                        requestKey + index + ".idx.sha3.zip", //this format is sha3 ;)
				        null, 
                        target, 
                        requestHtl, // we need it faster, same as for messages
                        false); // doRedirect, like in uploadIndexFile()
                
                if (fcpresults == null || target.length() == 0) {
                    // download failed. Sometimes there are some 0 byte
                    // files left, we better remove them now.
                    target.delete();
                    failures++;
                    // next loop we try next index
                    index = indexSlots.findNextFreeSlot(index);
                    
                } else {
                    
					// download was successful, mark it
					indexSlots.setSlotUsed(index);
                    // next loop we try next index
                    index = indexSlots.findNextFreeSlot(index);
					failures = 0;

					// check if we have received such file before
					String digest = Core.getCrypto().digest(target);
					if( messageHashes.contains(digest) ) {
						// we have.  erase and continue
						target.delete();
						continue;
					} else {
                        // else add it to the set of received files to prevent duplicates
                        messageHashes.add(digest);
                    }

					// Add it to the index
					try {
						// we need to unzip here to check if identity IN FILE == identity IN METADATA
						byte[] unzippedXml = FileAccess.readZipFileBinary(target);
						if (unzippedXml == null) {
							logger.warning("Could not extract received zip file, skipping.");
							target.delete();
							continue;
						}

						File unzippedTarget = new File(target.getPath() + "_unzipped");
						FileAccess.writeFile(unzippedXml, unzippedTarget);
                        unzippedXml = null;

						//create the FrostIndex object
						FrostIndex receivedIndex = null;
						try {
                            Index idx = Index.getInstance();
                            synchronized(idx) {
                                receivedIndex = idx.readKeyFile(unzippedTarget);
                            }
						} catch (Exception ex) {
							logger.log(Level.SEVERE, "Could not parse the index file: ", ex);
						}
                        if( receivedIndex == null || receivedIndex.getFilesMap().size() == 0 ) {
                            logger.log(Level.SEVERE, "Received index file invalid or empty, skipping.");
                            target.delete();
                            unzippedTarget.delete();
                            continue;
                        }

						Identity sharer = null;
						Identity sharerInFile = receivedIndex.getSharer();

						// verify the file if it is signed
						if (fcpresults.getRawMetadata() != null) {
							SignMetaData md;
							try {
								md = new SignMetaData(fcpresults.getRawMetadata());
							} catch (Throwable t) {
								// reading of xml metadata failed, handle
								logger.log(Level.SEVERE, "Could not read the XML metadata, skipping file index.", t);
								target.delete();
								continue;
							}

							//metadata says we're signed.  Check if there is identity in the file
							if (sharerInFile == null) {
								logger.warning("MetaData present, but file didn't contain an identity :(");
								unzippedTarget.delete();
								target.delete();
								continue;
							}

							String _owner = null;
							String _pubkey = null;
							if (md.getPerson() != null) {
								_owner = Mixed.makeFilename(md.getPerson().getUniqueName());
								_pubkey = md.getPerson().getKey();
							}

							//check if metadata is proper
							if (_owner == null || _owner.length() == 0 || _pubkey == null || _pubkey.length() == 0) {
								logger.warning("XML metadata have missing fields, skipping file index.");
								unzippedTarget.delete();
								target.delete();
								continue;
							}

							//check if fields match those in the index file
							if (!_owner.equals(Mixed.makeFilename(sharerInFile.getUniqueName()))
								|| !_pubkey.equals(sharerInFile.getKey())) {

								logger.warning("The identity in MetaData didn't match the identity in File! :(\n" +
												"file owner : " + sharerInFile.getUniqueName() + "\n" +
												"file key : " + sharerInFile.getKey() + "\n" +
												"meta owner: " + _owner + "\n" +
												"meta key : " + _pubkey);
								unzippedTarget.delete();
								target.delete();
								continue;
							}

							//verify! :)
                            byte[] zippedXml = FileAccess.readByteArray(target);
							boolean valid = Core.getCrypto().detachedVerify(zippedXml, _pubkey, md.getSig());
                            zippedXml = null;

							if (valid == false) {
								logger.warning("Invalid signature for index file from " + _owner);
								unzippedTarget.delete();
								target.delete();
								continue;
							}
                            
							//check if we have the owner already on the lists
							if (identities.isMySelf(_owner)) {
								logger.info("Received index file from myself");
								sharer = identities.getMyId();
							} else {
                                logger.info("Received index file from " + _owner);
                                sharer = identities.getIdentity(_owner);
                                
                                if( sharer == null ) {
                                    // a new sharer, put to neutral list
                                    sharer = addNewSharer(_owner, _pubkey);
                                    if (sharer == null) { // digest did not match, block file
                                        logger.info("sharer was null... :(");
                                        unzippedTarget.delete();
                                        target.delete();
                                        continue;
                                    }
                                } else if (sharer.getState() == FrostIdentities.ENEMY ) {
                                    if (MainFrame.frostSettings.getBoolValue("hideBadFiles")) {
                                        logger.info("Skipped index file from BAD user " + _owner);
                                        target.delete();
                                        unzippedTarget.delete();
                                        continue;
                                    }
                                }
                                // update lastSeen for sharer Identity
                                sharer.updateLastSeenTimestamp();
							}
						} // end-of: if metadata != null
						else if (MainFrame.frostSettings.getBoolValue("hideAnonFiles")) {
							unzippedTarget.delete();
							target.delete();
							continue; //do not show index.
						}

						// if the user is not on the GOOD list..
                        String sharerStr;
						if (sharer == null || sharer.getState() != FrostIdentities.FRIEND ) {
							// add only files from that user (not files from his friends)     
							sharerStr = (sharer == null) ? "Anonymous" : sharer.getUniqueName();
							logger.info("adding only files from " + sharerStr);
						} else {
							// if user is GOOD, add all files (user could have sent files from HIS friends in this index)
							logger.info("adding all files from " + sharer.getUniqueName());
                            sharerStr = null;
						}
                        Index idx = Index.getInstance();
                        synchronized(idx) {
                            idx.add(receivedIndex, board, sharerStr);
                        }
                        
						target.delete();
						unzippedTarget.delete();
					} catch (Throwable t) {
						logger.log(Level.SEVERE, "Error in UpdateIdThread", t);
					}
				}
			}

            // FIXED: I assume its enough to do this on current day, not for all days the same
            //   this thread is started up to maxDays times per board update!
            
			// Ok, we're done with downloading the keyfiles
			// Now calculate whitch keys we want to upload.
			// We only upload own keyfiles if:
			// 1. We've got more than minKeyCount keys to upload
			// 2. We don't upload any more files
            if( !isInterrupted() && isForToday ) {
                try {
                    uploadIndexFile();
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception during uploadIndexFile()", t);
                }
            }
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Oo. EXCEPTION in UpdateIdThread", t);
		}

        indexSlots.saveSlotsFile();

//		notifyThreadFinished(this);
	}
    
    /**
     * This method checks if the digest of sharer matches the pubkey,
     * and adds the NEW identity to list of neutrals.   
     * @param _sharer
     * @param _pubkey
     * @return
     */
    private Identity addNewSharer(String _sharer, String _pubkey) {
        
        //check if the digest matches
        String given_digest = _sharer.substring(_sharer.indexOf("@") + 1,
                                                _sharer.length()).trim();
        String calculatedDigest = Core.getCrypto().digest(_pubkey.trim()).trim();
        calculatedDigest = Mixed.makeFilename( calculatedDigest ).trim();
        
        if( ! Mixed.makeFilename(given_digest).equals( calculatedDigest ) ) {
            logger.warning("Warning: public key of sharer didn't match its digest:\n" +
            			   "given digest :'" + given_digest + "'\n" +
            			   "pubkey       :'" + _pubkey.trim() + "'\n" +
           				   "calc. digest :'" + calculatedDigest + "'");
            return null;        
        }
        //create the identity of the sharer
        Identity sharer = new Identity( _sharer.substring(0,_sharer.indexOf("@")), _pubkey);

        //add him to the neutral list (if not already on any list)
        sharer.setState(FrostIdentities.NEUTRAL);
        identities.addIdentity(sharer);

        return sharer;
    }

	/**Constructor*/
	public UpdateIdThread(Board board, String date, FrostIdentities newIdentities, boolean isForToday) {
//		super(board, newIdentities);
		
		this.board = board;
		this.date = date;
        this.identities = newIdentities;
		requestHtl = MainFrame.frostSettings.getIntValue("keyDownloadHtl");
		insertHtl = MainFrame.frostSettings.getIntValue("keyUploadHtl");
		keypool = MainFrame.frostSettings.getValue("keypool.dir");
//		maxKeys = MainFrame.frostSettings.getIntValue("maxKeys");
        this.isForToday = isForToday;
        
        // first load the index with the date we wish to download
        indexSlots = new IndexSlots(board, date, MAX_SLOTS_PER_DAY);

		publicKey = board.getPublicKey();
		privateKey = board.getPrivateKey();

		if (board.isPublicBoard() == false && publicKey != null) {
			requestKey = new StringBuffer()
					.append(publicKey)
					.append("/")
					.append(date)
					.append("/")
					.toString();
		} else {
			requestKey = new StringBuffer()
					.append("KSK@frost/index/")
					.append(board.getBoardFilename())
					.append("/")
					.append(date)
					.append("/")
					.toString();
		}

		// we make all inserts today (isForCurrentDate)
		if (board.isPublicBoard() == false && privateKey != null) {
			insertKey = new StringBuffer()
					.append(privateKey)
					.append("/")
					.append(date)
					.append("/")
					.toString();
        } else {
			insertKey = new StringBuffer()
					.append("KSK@frost/index/")
					.append(board.getBoardFilename())
					.append("/")
					.append(date)
					.append("/")
					.toString();
        }
	}

	/**
	 * @param messageHashes
	 */
	public void setMessageHashes(MessageHashes messageHashes) {
		this.messageHashes = messageHashes;		
	}
    
    /**
     * Class provides functionality to track used index slots
     * for upload and download.
     */
    private static class IndexSlots {

        private static final Integer EMPTY = new Integer(0);
        private static final Integer USED  = new Integer(-1);
        
        private int maxSlotsPerDay;

        private Vector slots;
        private File slotsFile;
        
        private Board targetBoard;
        
        public IndexSlots(Board b, String date, int maxSlotsPerDay) {
            targetBoard = b;
            this.maxSlotsPerDay = maxSlotsPerDay;
            slotsFile = new File(MainFrame.keypool + targetBoard.getBoardFilename() + fileSeparator + "indicesV2-" + date);
            loadSlotsFile(date);
        }

        /**
         * Generates a new index file containing keys to upload.
         */
        private void loadSlotsFile(String loadDate) {

            if( slotsFile.isFile() ) {
                try {
                    // load new format, each int on a line, -1 means USED, all other mean EMPTY
                    slots = new Vector();
                    BufferedReader rdr = new BufferedReader(new FileReader(slotsFile));
                    String line;
                    while( (line=rdr.readLine()) != null ) {
                        line = line.trim();
                        if(line.length() == 0) {
                            continue;
                        }
                        if( line.equals("-1") ) {
                            slots.add(USED);
                        } else {
                            slots.add(EMPTY);
                        }
                        // max MAX_SLOTS_PER_DAY
                        if( slots.size() >= maxSlotsPerDay ) {
                            break; // (allows to lower index slot count)
                        }
                    }
                    rdr.close();
                } catch (Throwable exception) {
                    logger.log(Level.SEVERE, "Exception thrown in loadIndex(String date) - Date: '" + loadDate
                            + "' - Board name: '" + targetBoard.getBoardFilename() + "'", exception);
                }
            }
            // problem with file, start new indices
            if( slots == null ) {
                slots = new Vector();
            }
            // fill up (allows to raise index slot count)
            for (int i = slots.size(); i < maxSlotsPerDay; i++) {
                slots.add( EMPTY );
            }
        }

        /**
         * Returns false if we should stop this thread because board was deleted.
         */
        private boolean isTargetBoardValid() {
            File d = new File(MainFrame.keypool + targetBoard.getBoardFilename());
            if( d.isDirectory() ) {
                return true;
            } else {
                return false;
            }
        }

        public void saveSlotsFile() {
            if( isTargetBoardValid() == false ) {
                return;
            }
            try {
                slotsFile.delete();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(slotsFile)));
                for (int i=0; i < slots.size(); i++) {
                    Integer current = (Integer)slots.elementAt(i);
                    out.println(""+current.intValue());
                }
                out.flush();
                out.close();
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Exception thrown in saveSlotsFile()", e);
            }
        }
        
        public int findFirstFreeDownloadSlot() {
            for (int i=0; i < slots.size(); i++){
                Integer current = (Integer)slots.elementAt(i);
                if (current.intValue() > -1) { 
                    return i;
                }
            }
            return -1;
        }

        /**
         * First free upload slot is right behind last used slot.
         */
        public int findFirstFreeUploadSlot() {
            for (int i=slots.size()-1; i >= 0; i--){
                Integer current = (Integer)slots.elementAt(i);
                if (current.intValue() < 0) {
                    // used slot found
                    if( i+1 < slots.size() ) {
                        return i+1;
                    } else {
                        return -1; // all slots used
                    }
                }
            }
            // no used slot found, return first slot
            return 0;
        }

        public int findNextFreeSlot(int beforeIndex) {
            for (int i = beforeIndex+1; i < slots.size(); i++) {
                Integer current = (Integer)slots.elementAt(i);
                if (current.intValue() > -1) { 
                    return i;
                }
            }
            return -1;
        }
        
        public void setSlotUsed(int i) {
            int current = ((Integer)slots.elementAt(i)).intValue();
            if (current < 0 ) { 
                logger.severe("WARNING - index sequence screwed in setSlotUsed. report to a dev");
                return;
            }
            slots.setElementAt(USED, i);
        }
    }
}
