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

import java.io.File;
import java.util.*;

public class Index
{
    /**
     * Calculates keys that should be uploaded to the keyindex
     * @param board The boardsname (in filename type)
     * @return Vector with KeyClass objects
     */
    public static int getUploadKeys(String board)
    {
        System.out.println("Index.getUploadKeys(" + board + ")");
        Vector keys = new Vector();
        final String fileSeparator = System.getProperty("file.separator");

        // Abort if boardDir does not exist
        File boardDir = new File(frame1.keypool + board);
        if( !boardDir.isDirectory() )
            return 0;

        // Create boards tempDir if it does not exists
        File tempDir = new File(boardDir + fileSeparator + "temp");
        if( !tempDir.isDirectory() )
            tempDir.mkdir();

        // Get a list of this boards index files
        // Abort if there are none
        File[] index = boardDir.listFiles();
        if( index == null )
            return 0;

        // Generate temporary index from keyfiles
        File[] keypoolFiles = (new File(frame1.keypool)).listFiles();
        if( keypoolFiles != null )
        {
            for( int i = 0; i < keypoolFiles.length; i++ )
            {
                if( keypoolFiles[i].getName().indexOf("-" + board + "-") != -1 &&
                    keypoolFiles[i].getName().endsWith(".idx") )
                {
                    File lockfile = new File(keypoolFiles[i].getPath() + ".loc");
                    if( !lockfile.exists() )
                    {
                        // Clear the tempIndex if we add keyfile #0
                        // This way we only have to do this fuckin slow
                        // add once per board / day.
                        if( keypoolFiles[i].getName().endsWith("-" + board + "-0.idx") )
                        {

                            // Get a list of this boards temp files
                            // and remove existing files in the tempDir
                            File[] tempFiles = tempDir.listFiles();
                            if( tempFiles != null )
                            {
                                for( int j = 0; j < tempFiles.length; j++ )
                                    tempFiles[j].delete();
                            }

                        }
                        // create tempIndex
                        add(keypoolFiles[i], tempDir);
                        FileAccess.writeFile("Locked", lockfile);
                    }
                }
            }
        }

        // Now we compare each file of the original index
        // with it's tempIndex counterpart
        // Maps are funny and fast, we better use one here.
        final Map chk = Collections.synchronizedMap(new TreeMap());
        StringBuffer keyFile = new StringBuffer();
        File[] tempFiles = tempDir.listFiles();
        int keyCount = 0;
	keyFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	keyFile.append("<Filelist sharer = \""+frame1.getMyId().getUniqueName()+"\">\n");
        for( int i = 0; i < index.length; i++ )
        {
            if( index[i].getName().endsWith(".exc") )
            {
                chk.clear();
                FileAccess.readKeyFile(index[i], chk, true);
                FileAccess.readKeyFile(tempDir.getPath() + fileSeparator + index[i].getName(), chk, false);

                // Add keys that are still getExchange() == true to the
                // keys Vector
		
                synchronized(chk)
                {
                    Iterator j = chk.values().iterator();
                    while( j.hasNext() )
                    {
                        KeyClass current = (KeyClass)j.next();
                        if( current.getExchange() && 
				(current.getOwner() == null || 
					frame1.getFriends().Get(current.getOwner()) != null))
                        {
                            keyCount++;
                            
		    	    keyFile.append("<name>" + current.getFilename()+"</name>\n");
		    	    keyFile.append("<SHA1>" + current.getSHA1()+"</SHA1>\n");
			    keyFile.append("<size>" + current.getSize()+"</size>\n");
		    
			    if (current.getOwner() != null)
			    	keyFile.append("<owner>" + current.getOwner() + "</owner>\n");
		    	    if (current.getKey() != null)
			    	keyFile.append("<key>" + current.getKey() + "</key>\n");
			    if (current.getDate() != null)
			    	keyFile.append("<date>" + current.getDate() + "</date>\n");
		    
		    	    keyFile.append("</File>\n");
                        }
                    }
                }
            }
        }
        chk.clear();
	keyFile.append("</Filelist>");
	
	//String signed = frame1.getCrypto().sign(keyFile.toString(),frame1.getMyId().getPrivKey());
        // Make keyfile
        if( keyCount > 0 )
        {
            File file = new File(frame1.keypool + board + "_upload.txt");
            FileAccess.writeFile(keyFile.toString(), file);
        }

        return keyCount;
    }

    /**
     * Adds a key object to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param key the key to add to the index
     * @param target directory containing index
     */
    public static void add(KeyClass key, File target)
    {
        final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
        final String fileSeparator = System.getProperty("file.separator");
        final String hash = key.getSHA1();
	String firstLetter = ")";
	if (key.getKey() != null)
        	firstLetter = (key.getKey().substring(4, 5)).toLowerCase();
        final Map chk = Collections.synchronizedMap(new TreeMap());

        if( !target.isDirectory() )
            target.mkdir();
        if( split.indexOf(firstLetter) == -1 )
            firstLetter = "other";

        File indexFile = new File(target.getPath()  + fileSeparator + firstLetter + ".exc");

        FileAccess.readKeyFile(indexFile, chk, true);
        chk.put(hash, key);
        FileAccess.writeKeyFile(chk, indexFile);
    }

    /**
     * Adds a keyfile to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param keyfile the keyfile to add to the index
     * @param target directory containing index
     */
    public static void add(File keyfile, File target)
    {
        String oldFirstLetter = "";
        final Map chk = Collections.synchronizedMap(new TreeMap());
        final Map chunk = Collections.synchronizedMap(new TreeMap());

        if( !target.isDirectory() )
            target.mkdir();

        FileAccess.readKeyFile(keyfile, chk, true);

        synchronized(chk)
        {
            Iterator i = chk.keySet().iterator();
            while( i.hasNext() )
            {
                KeyClass key = (KeyClass)chk.get((String)i.next());
                String hash = key.getSHA1();
                String firstLetter = (hash.substring(4, 5)).toLowerCase();
                if( !firstLetter.equals(oldFirstLetter) )
                {
                    // System.out.print(".");
                    if( chunk.size() > 0 )
                    {
                        add(chunk, target, oldFirstLetter);
                        chunk.clear();
                    }
                    oldFirstLetter = firstLetter;
                }
                chunk.put(hash, key);
            }
        }
        add(chunk, target, oldFirstLetter);
    }

    /**
     * Adds a Map to an index located at target dir.
     * Target dir will be created if it does not exist
     * @param chunk the map to add to the index
     * @param target directory containing index
     * @param firstLetter identifier for the keyfile
     */
    protected static void add(Map chunk, File target, String firstLetter)
    {
        final String split = "abcdefghijklmnopqrstuvwxyz1234567890";
        final String fileSeparator = System.getProperty("file.separator");

        if( !target.isDirectory() )
            target.mkdir();

        if( chunk.size() > 0 )
        {
            if( split.indexOf(firstLetter) == -1 )
                firstLetter = "other";
            FileAccess.readKeyFile(new File(target.getPath() + fileSeparator + firstLetter + ".exc"), chunk, false);
            FileAccess.writeKeyFile(chunk, new File(target.getPath() + fileSeparator + firstLetter + ".exc"));
            chunk.clear();
        }
    }
}
