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

import frost.*;
import frost.gui.objects.FrostBoardObject;
import frost.identities.*;

public class UpdateIdThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    private static boolean DEBUG = true;
    private static int maxFailures = 3;
    private static int keyCount = 0;
    private static int minKeyCount = 50;
    private static int maxKeysPerFile = 5000;
    
    private static final int MAX_TRIES = 2; //number of times each index will be tried -1

    private Vector indices;
    private File indicesFile;
    private int maxKeys;
    private String date;
    private String oldDate;
    private int requestHtl;
    private int insertHtl;
    private String keypool;
    private FrostBoardObject board;
    private String publicKey;
    private String privateKey;
    private String requestKey;
    private String insertKey;
    private String boardState;
    private final static String fileSeparator = System.getProperty("file.separator");

    public int getThreadType() { return BoardUpdateThread.BOARD_FILE_DNLOAD; }

    /**
     * Generates a new index file containing keys to upload.
     * @return true if index file was created, else false.
     */
     
     
    private void commit() {
    	try{
		indicesFile.delete();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(indicesFile));
		out.writeObject(indices);
		out.flush();
		out.close();
	}catch(IOException e) {
		e.printStackTrace();
	}
    }
    private int findFreeUploadIndex() {
    	for (int i = 0;i<indices.size();i++){
		Integer current = (Integer)indices.elementAt(i);
		if (current.intValue() > -1)
			return i;
	}
	return -1;
    }
    
    private int findFreeDownloadIndex() {
    	for (int i = 0;i<indices.size();i++){
		Integer current = (Integer)indices.elementAt(i);
		if (current.intValue() > -1 && current.intValue() < MAX_TRIES)
			return i;
	}
	return -1;
    }
    
    private void setIndexFailed(int i) {
    	int current = ((Integer)indices.elementAt(i)).intValue();
	
	if (current == -1 || current >= MAX_TRIES) {
		System.err.println("\n\nWARNING - index sequence screwed. report to a dev\n\n");
		return;
	}
	
	indices.setElementAt(new Integer(current++),i);
	
	commit();
		
    }
    
    private void setIndexSuccessfull(int i) {
    	int current = ((Integer)indices.elementAt(i)).intValue();
	if (current == -1 || current > MAX_TRIES) {
		System.err.println("\n\nWARNING - index sequence screwed. report to a dev\n\n");
		return;
	}
	
	indices.setElementAt(new Integer(-1),i);
	
	commit();
    }
    
    private boolean makeIndexFile()
    {
        if( DEBUG ) System.out.println("FILEDN: UpdateIdThread.makeIndexFile for " + board.toString());

        // Calculate the keys to be uploaded
        keyCount = Index.getUploadKeys(board.getBoardFilename());

        // Adjust maxAge
        adjustMaxAge(keyCount);

        if( keyCount > 0 )
            return true;
        else
            return false;
    }

    private void uploadIndexFile()//int i)
    {
        File indexFile = new File(keypool + board.getBoardFilename() + "_upload.txt");
        boolean success = false;
        int tries = 0;
        String[] result = {"Error", "Error"};

        if( indexFile.length() > 0 && indexFile.isFile() )
        {
            String tozip = frame1.getCrypto().sign(FileAccess.readFileRaw(indexFile),
	    			frame1.getMyId().getPrivKey());
            FileAccess.writeZipFile(tozip, "entry", indexFile);

            // search empty slot
            int index = findFreeUploadIndex();
            while( !success && tries <= MAX_TRIES )
            {
                // Does this index already exist?
		           
                result = FcpInsert.putFile(insertKey + index + ".idx.sha.zip",
                                           new File(keypool + board.getBoardFilename() + "_upload.txt"),
                                           insertHtl,
                                           true,
                                           true,
                                           board.getBoardFilename());

                if( result[0].equals("Success") )
                {
                    success = true;
		    setIndexSuccessfull(index);
                    if( DEBUG ) System.out.println("FILEDN:***** Index file successfully uploaded *****");
                }
                else
                {
                    if( result[0].equals("KeyCollision") )
                    {
                        index++;
                        tries=0; // reset tries
                        if( DEBUG ) System.out.println("FILEDN:***** Index file collided, increasing index. *****");
                    }
                    else
                    {
                        String tv = result[0];
                        if( tv == null ) tv="";
                        if( DEBUG ) System.out.println("FILEDN:***** Unknown upload error (#" + tries + ", '"+tv+"'), retrying. *****");
                    }
                }
            }
        }
    }

    // If we're getting too much files on a board, we lower
    // the maxAge of keys. That way older keys get removed
    // sooner. With the new index system it should be possible
    // to work with large numbers of keys because they are
    // no longer kept in memory, but on disk.
    private void adjustMaxAge(int count) {/*  //this is not used
    //if (DEBUG) System.out.println("FILEDN: AdjustMaxAge: old value = " + frame1.frostSettings.getValue("maxAge"));

    int lowerLimit = 10 * maxKeys / 100;
    int upperLimit = 90 * maxKeys / 100;
    int maxAge = frame1.frostSettings.getIntValue("maxAge");

    if (count < lowerLimit && maxAge < 21)
        maxAge++;
    if (count > upperLimit && maxAge > 1)
        maxAge--;

    frame1.frostSettings.setValue("maxAge", maxAge);
    //if (DEBUG) System.out.println("FILEDN: AdjustMaxAge: new value = " + maxAge);*/
    }

    public void run()
    {
        notifyThreadStarted(this);
        try {

        // Wait some random time to speed up the update of the TOF table
        // ... and to not to flood the node
        int waitTime = (int)(Math.random() * 5000); // wait a max. of 5 seconds between start of threads
        mixed.wait(waitTime);

        int index = findFreeDownloadIndex();
        int failures = 0;

        while( failures < maxFailures )
        {
            
		File target = File.createTempFile("frost-index-"+index,board.getBoardFilename(),
					new File(frame1.frostSettings.getValue("temp.dir")));
                if( DEBUG ) System.out.println("FILEDN: Requesting index " + index);
                // Download the keyfile
                FcpRequest.getFile(requestKey + index + ".idx.sha.zip",
                                   null,
                                   target,
                                   requestHtl,
                                   true);
                if( target.length() > 0 )
                {
			//mark it as successful
			setIndexSuccessfull(index);
                    // Add it to the index
                    try {
                        // maybe the file is corrupted ... so try
                        String unzipped = FileAccess.readZipFile(target);
			
			//verify the file 
			if (unzipped.startsWith("===")) {
				int name_index = unzipped.indexOf("sharer = \"");
				name_index = unzipped.indexOf("\"",name_index)+1;
				//get the unique name of the person sharing the files
				String _sharer = unzipped.substring(name_index,
							unzipped.indexOf("\"",name_index));
				_sharer = _sharer.trim();
				Identity sharer = null;
				if (frame1.getMyId().getUniqueName().trim().compareTo(_sharer)==0) {
				
					System.out.println("received index from myself");
					
					sharer = frame1.getMyId();
					
				} else {
					
					System.out.println("received index from "+_sharer);			
				
					sharer = frame1.getFriends().Get(_sharer);
				}
				
				//we have the person
				if (sharer==null) { //we don't have it, use the provided key
					int key_index = unzipped.indexOf("pubkey =");
					
					if (key_index == -1) {
						System.out.println("file didn't contain public key!");
						target.delete();
						index++;
						continue;
					}
					
					key_index = unzipped.indexOf("\"",key_index)+1;
					
					//get the key
					String pubKey = unzipped.substring(key_index,
							unzipped.indexOf("\"",key_index));
							
					//check if the digest matches
					String given_digest = _sharer.substring(_sharer.indexOf("@")+1,_sharer.length());
					if (given_digest.trim().compareTo(frame1.getCrypto().digest(pubKey.trim()).trim()) != 0) {
						System.out.println("pubkey in index file didn't match digest");
						System.out.println("given digest "+ given_digest.trim());
						System.out.println("pubkey " +pubKey.trim());
						System.out.println("calculated digest "+frame1.getCrypto().digest(pubKey).trim());
						target.delete();
						index++;
						continue;
					}
					
					//create the identity of the sharer
					sharer = new Identity(_sharer.substring(0,_sharer.indexOf("@")),
								null,
								pubKey);
				}
				
				//verify the archive
				if (!frame1.getCrypto().verify(unzipped,sharer.getKey())) {
					System.out.println("index file failed verification!");
					target.delete();
					index++;
					continue;
				}
				
				//strip the sig
				unzipped = unzipped.substring(frame1.getCrypto().MSG_HEADER_SIZE,
							unzipped.lastIndexOf("\n=== Frost message signature: ===\n"));
			}
			
                        FileAccess.writeFile(unzipped,target);
                        Index.add(target, 
				new File(frame1.keypool+board.getBoardFilename()+fileSeparator+"files.xml"));
			target.delete();
                    }
                    catch(Throwable t)
                    {
                        System.out.println("Error in UpdateIdThread: "+t.getMessage());
                        // delete the file and try a re download???
                    }

                    index = findFreeDownloadIndex();
                    failures = 0;
                }
                else
                {
                    // download failed. Sometimes there are some 0 byte
                    // files left, we better remove them now.
                    target.delete();
		    setIndexFailed(index);
                    failures++;
                    index++;
                }
            }
            if( isInterrupted() ) // check if thread should stop
            {
                notifyThreadFinished(this);
                return;
            }
        

        // Ok, we're done with downloading the keyfiles
        // Now calculate whitch keys we want to upload.
        // We only upload own keyfiles if:
        // 1. We've got more than minKeyCount keys to upload
        // 2. We don't upload any more files
        //index -= maxFailures;
        if( makeIndexFile() )
        {
            if( frame1.isGeneratingCHK() == false || keyCount >= minKeyCount )
            {
                if( DEBUG ) System.out.println("FILEDN: Starting upload of index file to board '"+board.toString()+"'; uploadFiles = " + keyCount);
                uploadIndexFile();
            }
        }
        else
        {
            if( DEBUG ) System.out.println("FILEDN: No keys to upload, stopping UpdateIdThread for " + board.toString());
        }

        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in UpdateIdThread:");
            t.printStackTrace();
        }

        notifyThreadFinished( this );
	commit();
    }

    /**Constructor*/
    public UpdateIdThread(FrostBoardObject board)
    {
        super(board);
        this.board = board;
        date = DateFun.getExtendedDate();
        requestHtl = frame1.frostSettings.getIntValue("keyDownloadHtl");
        insertHtl = frame1.frostSettings.getIntValue("keyUploadHtl");
        keypool = frame1.frostSettings.getValue("keypool.dir");
        maxKeys = frame1.frostSettings.getIntValue("maxKeys");
	
	indicesFile = new File(frame1.keypool + board.getBoardFilename() + fileSeparator + "indices-"+date);
	
	//indices = new Vector();
	
	try {
		if (indicesFile.exists()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(indicesFile));
			indices = (Vector)in.readObject();
			in.close();
		}else {
			indices = new Vector(100);
			for (int i = 0;i < 100;i++)
				indices.add(new Integer(0));
		}
	}catch(IOException e) {
		e.printStackTrace();
	}catch(ClassNotFoundException e) {
		e.printStackTrace();
	}

        publicKey = board.getPublicKey();
        privateKey = board.getPrivateKey();

        if( board.isPublicBoard()==false && publicKey != null )
        {
            requestKey = new StringBuffer().append(publicKey).append("/").append(date).append("/").toString();
        }
        else
        {
            requestKey = new StringBuffer().append("KSK@frost/index/")
                         .append(board.getBoardFilename())
                         .append("/")
                         .append(date)
                         .append("/").toString();
        }

        if( board.isPublicBoard()==false && privateKey != null )
            insertKey = new StringBuffer().append(privateKey).append("/").append(date).append("/").toString();
        else
            insertKey = new StringBuffer().append("KSK@frost/index/").append(board.getBoardFilename())
                        .append("/").append(date).append("/").toString();
    }
}

