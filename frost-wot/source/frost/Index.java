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
        final Map keys = Collections.synchronizedMap(new HashMap());
        //final String fileSeparator = System.getProperty("file.separator");

        File keyFile =
            new File(frame1.keypool + board + fileSeparator + "files.xml");

        //if no such file exists, return null
        if (!keyFile.exists())
        {
            Core.getOut().println("keyfile didn't exist??");
            return null;
        }

        FileAccess.readKeyFile(keyFile, keys);
        SharedFileObject result = (SharedFileObject)keys.get(SHA1);
        if (result == null)
        {
            //try the recently uploaded files
            keyFile =
                new File(
                    frame1.keypool + board + fileSeparator + "new_files.xml");
            if (!keyFile.exists())
            {
                Core.getOut().println(keyFile.getPath() + " didn't exist");
                return null;
            }
            keys.clear();
            FileAccess.readKeyFile(keyFile, keys);
        }
        return (SharedFileObject)keys.get(SHA1);

    }

    //this method puts the SharedFileObjects into the target set and 
    //returns the number of the files shared by the user himself
    public static Set getUploadKeys(String board)
    {

        final Map mine = Collections.synchronizedMap(new HashMap());
        final Map total = Collections.synchronizedMap(new HashMap());
        final Map updated = Collections.synchronizedMap(new HashMap());
        Core.getOut().println("Index.getUploadKeys(" + board + ")");
        Vector keys = new Vector();
        //final String fileSeparator = System.getProperty("file.separator");

        // Abort if boardDir does not exist
        File boardNewUploads =
            new File(frame1.keypool + board + fileSeparator + "new_files.xml");
        // if( !boardNewUploads.exists() )
        //   return 0;

        File boardFiles =
            new File(frame1.keypool + board + fileSeparator + "files.xml");
        if (boardFiles.exists())
        {
            FileAccess.readKeyFile(boardFiles, total);
        }

        FileAccess.readKeyFile(boardNewUploads, mine);

        //add friends's files 
        // TODO:  add a limit
        Iterator i = total.values().iterator();
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
                mine.put(current.getSHA1(), current);
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
                    mine.put(current.getSHA1(), current);
                    Core.getOut().print("d");
                    //d means it was shared too long ago
                    updated.put(current.getSHA1(), current);
                }
            }
        }

        //update the lastSharedDate of the shared files    
        add(
            updated,
            new File(frame1.keypool + board + fileSeparator + "files.xml"));

        //StringBuffer keyFile = new StringBuffer();
        //	boolean signUploads = frame1.frostSettings.getBoolValue("signUploads");
        int keyCount = 0;

        // FIXME: TEST XML code, especially the removing of "owner"
        //  Document doc = XMLTools.createDomDocument();
        // if( doc == null )
        //{
        //   System.out.println("Error - getUploadKeys: factory could'nt create XML Document.");
        //     return -1;
        // }

        // Element rootElement = doc.createElement("Filelist");
        //only add personal info if we chose to sign
        // if (signUploads)
        //{
        //  rootElement.setAttribute("sharer", frame1.getMyId().getUniqueName());
        // rootElement.setAttribute("pubkey", frame1.getMyId().getKey());
        //  }
        //  doc.appendChild(rootElement);

        synchronized (mine)
        {
            Iterator j = mine.values().iterator();
            while (j.hasNext())
            {
                SharedFileObject current = (SharedFileObject)j.next();
                boolean my =
                    current.getOwner() != null
                        && frame1.getMyId().getUniqueName().compareTo(
                            current.getOwner())
                            == 0;

                if (my)
                {
                    keyCount++;
                }

                //Element element = current.getXMLElement(doc);

                // if( current.getOwner() != null && my && !signUploads )
                // {  //REDFLAG: don't forget to remove these
                // remove owner, we don't want to sign ...
                //     ArrayList lst = XMLTools.getChildElementsByTagName(element, "owner");
                //     if( lst.size() > 0 )
                //    {
                //        Element r = (Element)lst.get(0);          
                //       element.removeChild(r);                }
                //   else
                //   {
                //       System.out.println("ERROR - getUploadKeys: Could not locate the 'owner' tag in XML tree!");
                //   }
                //  }
                //   rootElement.appendChild( element );
            }
        }
        /*        synchronized(mine)
                {
                     Iterator j = mine.values().iterator();
                     while( j.hasNext() )
                     {
                          SharedFileObject current = (SharedFileObject)j.next();
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
        */
        //String signed = frame1.getCrypto().sign(keyFile.toString(),frame1.getMyId().getPrivKey());
        // Make keyfile
        //if( keyCount > 0 )
        // {
        //     boolean writeOK = false;
        //     try {
        //         writeOK = XMLTools.writeXmlFile(doc, frame1.keypool + board + "_upload.txt");
        //     } catch(Throwable t)
        //     {
        //         System.out.println("Exception - getUploadKeys:");
        //         t.printStackTrace(System.out);
        //         return -1; // keep newUploads file
        //     }
        //  }

        //clear the new uploads
        //	boardNewUploads.delete();
        if (keyCount > 0)
            return new HashSet(mine.values());
        else
            return null;
    }

    public static void add(SharedFileObject key, FrostBoardObject board)
    {
        //final String fileSeparator = System.getProperty("file.separator");
        File boardDir = new File(board.getBoardFilename());
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
        try
        {
            if (!indexFile.exists())
                indexFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FileAccess.readKeyFile(indexFile, chk);
        if (chk.get(hash) != null)
            chk.remove(hash);
        chk.put(hash, key);
        FileAccess.writeKeyFile(chk, indexFile);
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

        final Map chunk = Collections.synchronizedMap(new HashMap());

        try
        {
            if (!target.exists())
                target.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FileAccess.readKeyFile(keyfile, chunk);
        Iterator it = chunk.values().iterator();
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
        final Map chunk = Collections.synchronizedMap(new HashMap());

        try
        {
            if (!target.exists())
                target.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace(Core.getOut());
        }
        FileAccess.readKeyFile(keyfile, chunk);

        add(chunk, target, owner);
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

        FileAccess.readKeyFile(target, whole);

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
                (SharedFileObject)whole.get(current.getSHA1());

            if (old == null)
            {
                whole.put(current.getSHA1(), current);
                continue;
            }
            old.setDate(current.getDate());
            old.setLastSharedDate(current.getLastSharedDate());
            old.setKey(current.getKey());
            //TODO: allow unsigned files to be appropriated
        }

        FileAccess.writeKeyFile(whole, target);
    }

    protected static void add(Map chunk, File target, String owner)
    {
        final Map whole = Collections.synchronizedMap(new HashMap());
        if (owner == null)
            owner = "Anonymous";
        FileAccess.readKeyFile(target, whole);

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
                (SharedFileObject)whole.get(current.getSHA1());

            if (old == null)
            {
                whole.put(current.getSHA1(), current);
                continue;
            }
            old.setDate(current.getDate());
            old.setKey(current.getKey());
            //TODO: allow unsigned files to be appropriated
        }

        FileAccess.writeKeyFile(whole, target);
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
