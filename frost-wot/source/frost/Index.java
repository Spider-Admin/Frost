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

import frost.identities.*;
import frost.messages.*;

//import org.w3c.dom.*;

import frost.gui.model.DownloadTableModel;
import frost.gui.objects.*;

public class Index
{
    /**
     * Calculates keys that should be uploaded to the keyindex
     * @param board The boardsname (in filename type)
     * @return Vector with SharedFileObject objects
     */

    private static final String fileSeparator =
        System.getProperty("file.separator");
    public static SharedFileObject getKey(String SHA1, FrostBoardObject board)
    {
        return getKey(SHA1, board.getBoardFilename());
    }

    public static SharedFileObject getKey(String SHA1, String board)
    {
        
        //final String fileSeparator = System.getProperty("file.separator");

        File keyFile =
            new File(frame1.keypool + board + fileSeparator + "files.xml");

        //if no such file exists, return null
        if (!keyFile.exists())
        {
            Core.getOut().println("keyfile didn't exist??");
            return null;
        }

        FrostIndex idx = FileAccess.readKeyFile(keyFile);
		if (idx.getFilesMap().containsKey(SHA1))
			return (SharedFileObject)idx.getFilesMap().get(SHA1);
        
//		then try the recently uploaded files
		keyFile =
			  new File(
				  frame1.keypool + board + fileSeparator + "new_files.xml");
		idx = FileAccess.readKeyFile(keyFile);
		if (idx.getFilesMap().containsKey(SHA1))
				return (SharedFileObject)idx.getFilesMap().get(SHA1);
		
        return null;
       
    }

    //this method puts the SharedFileObjects into the target set and 
    //returns the number of the files shared by the user himself
    public static Map getUploadKeys(String board)
    {


        
        boolean reSharing = false;
        boolean newFiles = false;
        
        FrostIndex totalIdx=null;
        FrostIndex _toUpload = null;
        
        Core.getOut().println("Index.getUploadKeys(" + board + ")");
        
        //final String fileSeparator = System.getProperty("file.separator");

        // Abort if boardDir does not exist
        File boardNewUploads =
            new File(frame1.keypool + board + fileSeparator + "new_files.xml");
        // if( !boardNewUploads.exists() )
        //   return 0;

        File boardFiles =
            new File(frame1.keypool + board + fileSeparator + "files.xml");
    
        totalIdx = FileAccess.readKeyFile(boardFiles);
        
		_toUpload =FileAccess.readKeyFile(boardNewUploads);
		
		if (boardNewUploads.exists()) {
	 
			 newFiles = true;
			 boardNewUploads.delete();
		}
		
			 
        Map toUpload = _toUpload.getFilesMap();
		
        //add friends's files 
        // TODO:  add a limit
        
        Iterator i = totalIdx.getFiles().iterator();
        int downloadBack =
            frame1.frostSettings.getIntValue("maxMessageDownload");
        Core.getOut().println(
            "re-sharing files shared before " + DateFun.getDate(downloadBack));
        while (i.hasNext())
        {
            SharedFileObject current = (SharedFileObject)i.next();
            if (current.getOwner() != null
                && //not anonymous
            frame1.getMyId().getUniqueName().compareTo(
                current.getOwner())
                    != 0
                && //not myself
            frame1.frostSettings.getBoolValue("helpFriends")
                && //and helping is enabled
             (
                    frame1.getFriends().containsKey(
                        mixed.makeFilename(
                            current.getOwner())))) //and marked GOOD
            {
                toUpload.put(current.getSHA1(),current);
                Core.getOut().print("f"); //f means added file from friend
            }
            //also add the file if its been shared too long ago
            if (current.getOwner() != null
                && //not anonymous 
            current.getOwner().compareTo(
                frame1.getMyId().getUniqueName())
                    == 0
                && //from myself
            current.getLastSharedDate() != null)
            { //not from the old format

                if (DateFun
                    .getDate(downloadBack)
                    .compareTo(current.getLastSharedDate())
                    > 0)
                {
                    current.setLastSharedDate(DateFun.getDate());
                    toUpload.put(current.getSHA1(),current);
                    Core.getOut().print("d");
                    reSharing=true;
                    //d means it was shared too long ago
                }
            }
        }

        //update the lastSharedDate of the shared files
        if (reSharing)
        	FileAccess.writeKeyFile(totalIdx, board);
        

		//return anything only if we either re-shared old files or
		//have new files to upload.
        if (reSharing || newFiles)
            return toUpload;
        else
            return null;
    }

    public static void add(SharedFileObject key, FrostBoardObject board)
    {
        //final String fileSeparator = System.getProperty("file.separator");
        File boardDir = new File(frame1.keypool+board.getBoardFilename());
        
        if (!(boardDir.exists() && boardDir.isDirectory()))
            boardDir.mkdir();
        if (key.getKey() != null)
            updateDownloadTable(key);
        add(
            key,
            new File(
                frame1.keypool
                    + board.getBoardFilename()
                    + fileSeparator
                    + "files.xml"));
    }

    public static void addMine(SharedFileObject key, FrostBoardObject board)
    {
        //final String fileSeparator = System.getProperty("file.separator");
        File boardDir = new File(frame1.keypool + board.getBoardFilename());
         
        if (!(boardDir.exists() && boardDir.isDirectory()))
            boardDir.mkdir();
        add(
            key,
            new File(boardDir.getPath() + fileSeparator + "new_files.xml"));
    }

    public static void add(
        File keyFile,
        FrostBoardObject board,
        Identity owner)
    {
        add(
            keyFile,
            new File(
                frame1.keypool
                    + board.getBoardFilename()
                    + fileSeparator
                    + "files.xml"),
            owner);
    }
    public static void add(File keyFile, FrostBoardObject board, String owner)
    {
        add(
            keyFile,
            new File(
                frame1.keypool
                    + board.getBoardFilename()
                    + fileSeparator
                    + "files.xml"),
            owner);
    }
    /**
     * Adds a key object to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param key the key to add to the index
     * @param target directory containing index
     */
    public static void add(SharedFileObject key, File target)
    {
        //final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
        //final String fileSeparator = System.getProperty("file.separator");
        final String hash = key.getSHA1();

        if (key.getKey() != null)
            updateDownloadTable(key);
            
        final Map chk = Collections.synchronizedMap(new HashMap());

        // File indexFile = new File(target.getPath()  + fileSeparator + "files.xml");
        File indexFile = target;
        try
        {
            if (!indexFile.exists())
                indexFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FrostIndex idx = FileAccess.readKeyFile(indexFile);
        if (idx == null) idx = new FrostIndex(new HashMap());
        if (idx.getFiles().contains(key))
            idx.getFiles().remove(key);
        idx.getFilesMap().put(key.getSHA1(),key);
        FileAccess.writeKeyFile(idx, indexFile);
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
    public static void add(File keyfile, File target, Identity owner)
    {

        

        try
        {
            if (!target.exists())
                target.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FrostIndex chunk = FileAccess.readKeyFile(keyfile);
        Iterator it = chunk.getFiles().iterator();
        if (!owner.getUniqueName().equals(Core.getMyId().getUniqueName()))
            while (it.hasNext())
            {
                SharedFileObject current = (SharedFileObject)it.next();
                if (!current.getOwner().equals(owner.getUniqueName()))
                    owner.getTrustees().add(current.getOwner());
                //FIXME: find a way to count the files each person has shared
                //without counting dublicates
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
    public static void add(File keyfile, File target, String owner)
    {
        try
        {
            if (!target.exists())
                target.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FrostIndex idx = FileAccess.readKeyFile(keyfile);

        add(idx, target, owner);
    }
    
    public static void add(FrostIndex a, File b){
    	add(a.getFilesMap(),b);
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
        //final Map whole = Collections.synchronizedMap(new HashMap());

        FrostIndex whole = FileAccess.readKeyFile(target);

        //if( !target.isDirectory() && !target.getPath().endsWith("xml"))
        //  target.mkdir();

        Iterator i = chunk.values().iterator();
        while (i.hasNext())
        {
            SharedFileObject current = (SharedFileObject)i.next();

            //update the download table
            if (current.getKey() != null)
                updateDownloadTable(current);

            SharedFileObject old =
                (SharedFileObject)whole.getFilesMap().get(current.getSHA1());

            if (old == null)
            {
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

	public static void add(FrostIndex a, FrostBoardObject b, String owner){
		add(a.getFilesMap(),new File(b.getBoardFilename()),owner);
	}
	public static void add(FrostIndex a, File b, String owner){
		add(a.getFilesMap(),b,owner);
	}
	
    protected static void add(Map chunk, File target, String owner)
    {
        
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
        while (i.hasNext())
        {
            SharedFileObject current = (SharedFileObject)i.next();
            if (current.getOwner() != null
                && !current.getOwner().equals(owner))
                continue;
            //update the download table
            if (current.getKey() != null)
                updateDownloadTable(current);

            SharedFileObject old =
                (SharedFileObject)idx.getFilesMap().get(current.getSHA1());

            if (old == null)
            {
                idx.getFilesMap().put(current.getSHA1(), current);
                continue;
            }
            old.setDate(current.getDate());
            old.setKey(current.getKey());
            //TODO: allow unsigned files to be appropriated
        }

        FileAccess.writeKeyFile(idx, target);
    }

    private static void updateDownloadTable(SharedFileObject key)
    {
        //this really shouldn't happen
        if (key == null || key.getSHA1() == null)
        {
            Core.getOut().println("null value in index.updateDownloadTable");
            if (key != null)
                Core.getOut().println("SHA1 null!");
            else
                Core.getOut().println("key null!");
            return;
        }

        DownloadTableModel dlModel =
            (DownloadTableModel)frame1
                .getInstance()
                .getDownloadTable()
                .getModel();
        for (int i = 0; i < dlModel.getRowCount(); i++)
        {
            FrostDownloadItemObject dlItem =
                (FrostDownloadItemObject)dlModel.getRow(i);
            if (dlItem.getState() == FrostDownloadItemObject.STATE_REQUESTED
                && dlItem.getSHA1() != null
                && dlItem.getSHA1().compareTo(key.getSHA1()) == 0)
            {
                dlItem.setKey(key.getKey());
                dlItem.setDate(key.getDate());
                break;
            }

        }
    }
}
