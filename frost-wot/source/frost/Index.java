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
package frost;

import java.io.*;
import java.util.*;

import frost.gui.model.DownloadTableModel;
import frost.gui.objects.*;

public class Index
{
    /**
     * Calculates keys that should be uploaded to the keyindex
     * @param board The boardsname (in filename type)
     * @return Vector with KeyClass objects
     */
 
    private static final String fileSeparator = System.getProperty("file.separator");
    public static KeyClass getKey(String SHA1,FrostBoardObject board) {
    	return getKey(SHA1,board.getBoardFilename());
    }
    
    public static KeyClass getKey(String SHA1,String board) {
    	final Map keys = Collections.synchronizedMap(new HashMap());
	//final String fileSeparator = System.getProperty("file.separator");
	
	File keyFile = new File(frame1.keypool + board + fileSeparator + "files.xml");
	
	//if no such file exists, return null
	if (!keyFile.exists()) {
		System.out.println("keyfile didn't exist??");
		return null;
	}
		
	
	FileAccess.readKeyFile(keyFile, keys);
	KeyClass result = (KeyClass) keys.get(SHA1);
	if (result==null) {
		//try the recently uploaded files
		keyFile = new File(frame1.keypool + board + fileSeparator + "new_files.xml");
		if (!keyFile.exists()) {
			System.out.println(keyFile.getPath() + " didn't exist");
			return null;
		}
		keys.clear();
		FileAccess.readKeyFile(keyFile,keys);
	}
	return (KeyClass)keys.get(SHA1);
	
    }
    
 
    
    //this method transports stuff from new_files.xml and files.xml to
    //boardname_upload.txt
    //it does not use xml serialization because there are some fields
    //that we don't want to upload, like last shared date, etc.
    public static int getUploadKeys(String board)
    {
    
    	final Map mine = Collections.synchronizedMap(new HashMap());
	final Map total = Collections.synchronizedMap(new HashMap());
	final Map updated = Collections.synchronizedMap(new HashMap());
        System.out.println("Index.getUploadKeys(" + board + ")");
        Vector keys = new Vector();
        //final String fileSeparator = System.getProperty("file.separator");

        // Abort if boardDir does not exist
        File boardNewUploads = new File(frame1.keypool + board+fileSeparator+"new_files.xml");
       // if( !boardNewUploads.exists() )
         //   return 0;
	
	File boardFiles = new File (frame1.keypool + board + fileSeparator +"files.xml");
	if (boardFiles.exists()) 
		FileAccess.readKeyFile(boardFiles,total);

	FileAccess.readKeyFile(boardNewUploads,mine);
	
	
	//add friends's files 
	//TODO:  add a limit
	Iterator i = total.values().iterator(); 
	int downloadBack=frame1.frostSettings.getIntValue("maxMessageDownload");
	System.out.println("re-sharing files shared before "+DateFun.getDate(downloadBack));
	while (i.hasNext()) {
		KeyClass current = (KeyClass) i.next();
		if (current.getOwner() != null && //not anonymous
			frame1.getMyId().getUniqueName().compareTo(current.getOwner()) !=0 && //not myself
			frame1.frostSettings.getBoolValue("helpFriends") && //and helping is enabled
			(frame1.getFriends().Get(current.getOwner()) != null || //and marked GOOD
				frame1.getGoodIds().contains(current.getOwner()))) //or marked to be helped 
			{
			mine.put(current.getSHA1(),current);
			System.out.print("f");
			}
		//also add the file if its been shared too long ago
		if (current.getOwner()!=null && //not anonymous 
			current.getOwner().compareTo(frame1.getMyId().getUniqueName())==0 && //from myself
			current.getLastSharedDate() != null) { //not from the old format
				
				if (DateFun.getDate(downloadBack).compareTo(current.getLastSharedDate()) > 0) {
					current.setLastSharedDate(DateFun.getDate());
					mine.put(current.getSHA1(),current);
					System.out.print("d");
					updated.put(current.getSHA1(),current);
				}
		}
			
	}
        
	add(updated, new File(frame1.keypool+board+fileSeparator+"files.xml"));
	
	StringBuffer keyFile = new StringBuffer();
	boolean signUploads = frame1.frostSettings.getBoolValue("signUploads");
	int keyCount = 0;
	
	keyFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	keyFile.append("<Filelist ");
	//only add personal info if we chose to sign
	if (signUploads)
		keyFile.append("sharer = \""+frame1.getMyId().getUniqueName()+"\""+
			" pubkey = \""+frame1.getMyId().getKey()+"\"");
	keyFile.append(">\n");
				
        synchronized(mine)
        {
             Iterator j = mine.values().iterator();
             while( j.hasNext() )
             {
                  KeyClass current = (KeyClass)j.next();
		  boolean my = current.getOwner()!= null &&
		  	frame1.getMyId().getUniqueName().compareTo(current.getOwner())==0;
		  //make an update only if the user has inserted at least one file
                  if(my)                 
                        keyCount++;
			
                  keyFile.append("<File>\n");
		  keyFile.append("<name><![CDATA[" + current.getFilename()+"]]></name>\n");
		  keyFile.append("<SHA1><![CDATA[" + current.getSHA1()+"]]></SHA1>\n");
		  keyFile.append("<size>" + current.getSize()+"</size>\n");
		  keyFile.append("<batch>"+ current.getBatch()+"</batch>\n");
		    
		  if (current.getOwner() != null  &&
		  	!(my && !signUploads))
		    	keyFile.append("<owner>" + current.getOwner() + "</owner>\n");
		  if (current.getKey() != null)
		    	keyFile.append("<key>" + current.getKey() + "</key>\n");
		  if (current.getDate() != null)
		    	keyFile.append("<date>" + current.getDate() + "</date>\n");
		    
		  keyFile.append("</File>\n");
			 
             }
       }
                
            
        
        //keyFile.append("<redundancy>_redNo</redundancy>"); //this will be replaced with redundancy #
	keyFile.append("</Filelist>");
	
	//String signed = frame1.getCrypto().sign(keyFile.toString(),frame1.getMyId().getPrivKey());
        // Make keyfile
        if( keyCount > 0 )
        {
            File file = new File(frame1.keypool + board + "_upload.txt");
            FileAccess.writeFile(keyFile.toString(), file);
        }
	
	//clear the new uploads
	boardNewUploads.delete();

        return keyCount;
	
    }

    public static void add(KeyClass key, FrostBoardObject board) {
        //final String fileSeparator = System.getProperty("file.separator");
	File boardDir = new File(board.getBoardFilename());
	if (!(boardDir.exists() && boardDir.isDirectory())) boardDir.mkdir();
	if (key.getKey() != null)
		updateDownloadTable(key);
    	add(key,new File(frame1.keypool + board.getBoardFilename()+fileSeparator+"files.xml"));
    }
    
    public static void addMine(KeyClass key, FrostBoardObject board) {
        //final String fileSeparator = System.getProperty("file.separator");
	File boardDir = new File(frame1.keypool + board.getBoardFilename());
	if (!(boardDir.exists() && boardDir.isDirectory())) boardDir.mkdir();
    	add(key,new File(boardDir.getPath()+fileSeparator+"new_files.xml"));
    }
    
    public static void add(File keyFile, FrostBoardObject board) {
    	add(keyFile, new File(frame1.keypool + board.getBoardFilename() + fileSeparator+ "files.xml"));
    }
    public static void add(File keyFile, FrostBoardObject board, String owner) {
    	add(keyFile, new File(frame1.keypool + board.getBoardFilename() + fileSeparator+ "files.xml"), owner);
    }
    /**
     * Adds a key object to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param key the key to add to the index
     * @param target directory containing index
     */
    public static void add(KeyClass key, File target)
    {
        //final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
        //final String fileSeparator = System.getProperty("file.separator");
        final String hash = key.getSHA1();
	
	if (key.getKey() != null)
		updateDownloadTable(key);
	
	//I'm removing the entire first letter thing.  
	//String firstLetter = ")";
	//if (key.getKey() != null)
        //	firstLetter = (key.getKey().substring(4, 5)).toLowerCase();
        final Map chk = Collections.synchronizedMap(new HashMap());

        //if( !target.isDirectory() && !target.getPath().endsWith("xml"))
          //  target.mkdir();
       // if( split.indexOf(firstLetter) == -1 )
         //   firstLetter = "other";

       // File indexFile = new File(target.getPath()  + fileSeparator + "files.xml");
	File indexFile = target;
	try {
		if (!indexFile.exists()) indexFile.createNewFile();
	}catch(IOException e) {
		e.printStackTrace(System.out);
	}
        FileAccess.readKeyFile(indexFile, chk);
        if (chk.get(hash) != null)
		chk.remove(hash);
	chk.put(hash, key);
        FileAccess.writeKeyFile(chk, indexFile);
    }

    /**
     * Adds a keyfile to another
     * @param keyfile the keyfile to add to the index
     * @param target file containing index
     */
    public static void add(File keyfile, File target)
    {
     
        final Map chunk = Collections.synchronizedMap(new HashMap());

	try {
        if( !target.exists() )
            target.createNewFile();
	}catch(IOException e) {
		e.printStackTrace(System.out);
	}
        FileAccess.readKeyFile(keyfile, chunk);

        
        add(chunk, target);
    }

    public static void add(File keyfile, File target, String owner) {
    	   final Map chunk = Collections.synchronizedMap(new HashMap());

	try {
        if( !target.exists() )
            target.createNewFile();
	}catch(IOException e) {
		e.printStackTrace(System.out);
	}
        FileAccess.readKeyFile(keyfile, chunk);

        
        add(chunk, target,owner);
    }
    /**
     * Adds a Map to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param chunk the map to add to the index
     * @param target directory containing index
     * @param firstLetter identifier for the keyfile
     */
    protected static void add(Map chunk, File target)
    {
        //final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
//        final String fileSeparator = System.getProperty("file.separator");
	final Map whole = Collections.synchronizedMap(new HashMap());
	
	FileAccess.readKeyFile(target,whole);
	
        //if( !target.isDirectory() && !target.getPath().endsWith("xml"))
          //  target.mkdir();

        Iterator i = chunk.values().iterator();
	while (i.hasNext()) {
		KeyClass current = (KeyClass)i.next();
		
		//update the download table
		if (current.getKey() !=null)
			updateDownloadTable(current);
		
		KeyClass old = (KeyClass)whole.get(current.getSHA1());
		
		if (old == null) {
			whole.put(current.getSHA1(),current);
			continue;
		}
		old.setDate(current.getDate());
		old.setLastSharedDate(current.getLastSharedDate());
		old.setKey(current.getKey());
		//TODO: allow unsigned files to be appropriated
	}
	
	FileAccess.writeKeyFile(whole,target);
    }
    
    protected static void add(Map chunk, File target, String owner){
    	final Map whole = Collections.synchronizedMap(new HashMap());
	if (owner==null)
		owner= "Anonymous";
	FileAccess.readKeyFile(target,whole);
	
        //if( !target.isDirectory() && !target.getPath().endsWith("xml"))
          //  target.mkdir();

        Iterator i = chunk.values().iterator();
	while (i.hasNext()) {
		KeyClass current = (KeyClass)i.next();
		if (current.getOwner() != null &&
			!current.getOwner().equals(owner)) continue;
		//update the download table
		if (current.getKey() !=null)
			updateDownloadTable(current);
		
		KeyClass old = (KeyClass)whole.get(current.getSHA1());
		
		if (old == null) {
			whole.put(current.getSHA1(),current);
			continue;
		}
		old.setDate(current.getDate());
		old.setKey(current.getKey());
		//TODO: allow unsigned files to be appropriated
	}
	
	FileAccess.writeKeyFile(whole,target);
    }
    
    private static void updateDownloadTable(KeyClass key) {
    	DownloadTableModel dlModel = (DownloadTableModel)frame1.getInstance().getDownloadTable().getModel();
	for (int i = 0;i < dlModel.getRowCount();i++) {
		FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( i );
		if (dlItem.getState() == FrostDownloadItemObject.STATE_REQUESTED &&
			dlItem.getSHA1()!=null && dlItem.getSHA1().compareTo(key.getSHA1()) == 0) {
				dlItem.setKey(key.getKey());
				dlItem.setDate(key.getDate());
				break;
		}
		
	}
    }
}
