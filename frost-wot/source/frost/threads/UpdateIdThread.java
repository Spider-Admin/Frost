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

import java.util.*;
import java.io.*;

import frost.*;
import frost.gui.objects.*;

public class UpdateIdThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    private static boolean DEBUG = true;
    private static int maxFailures = 3;
    private static int keyCount = 0;
    private static int minKeyCount = 50;
    private static int maxKeysPerFile = 5000;

    private int maxKeys;
    private String date;
    private String oldDate;
    private String requestHtl;
    private String insertHtl;
    private String keypool;
    private FrostBoardObject board;
    private String publicKey;
    private String privateKey;
    private String requestKey;
    private String insertKey;
    private String boardState;

    public int getThreadType() { return BoardUpdateThread.BOARD_FILE_DNLOAD; }

    /**
     * Generates a new index file containing keys to upload.
     * @return true if index file was created, else false.
     */
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
            String tozip = FileAccess.readFile(indexFile);
            FileAccess.writeZipFile(tozip, "entry", indexFile);

            // search empty slot
            int index = 0;
            while( !success && tries <= maxFailures )
            {
                // Does this index already exist?
                String testFilename = new StringBuffer().append(keypool)
                                                        .append(date)
                                                        .append("-")
                                                        .append(board.getBoardFilename())
                                                        .append("-")
                                                        .append(index)
                                                        .append(".idx").toString();
                File testMe = new File(testFilename);
                if( testMe.length() > 0 )
                {
                    index++;
                    if( DEBUG ) System.out.println("FILEDN: File exists, increasing index to " + index);
                    continue;
                }

                tries++;
                result = FcpInsert.putFile(insertKey + index + ".idx.zip",
                                           keypool + board.getBoardFilename() + "_upload.txt",
                                           insertHtl,
                                           true,
                                           true,
                                           board.getBoardFilename());

                if( result[0].equals("Success") )
                {
                    success = true;
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
    private void adjustMaxAge(int count) {
    //if (DEBUG) System.out.println("FILEDN: AdjustMaxAge: old value = " + frame1.frostSettings.getValue("maxAge"));

    int lowerLimit = 10 * maxKeys / 100;
    int upperLimit = 90 * maxKeys / 100;
    int maxAge = frame1.frostSettings.getIntValue("maxAge");

    if (count < lowerLimit && maxAge < 21)
        maxAge++;
    if (count > upperLimit && maxAge > 1)
        maxAge--;

    frame1.frostSettings.setValue("maxAge", maxAge);
    //if (DEBUG) System.out.println("FILEDN: AdjustMaxAge: new value = " + maxAge);
    }

    public void run()
    {
        notifyThreadStarted(this);
        try {

        // Wait some random time to speed up the update of the TOF table
        // ... and to not to flood the node
        int waitTime = (int)(Math.random() * 5000); // wait a max. of 5 seconds between start of threads
        mixed.wait(waitTime);

        int index = 0;
        int failures = 0;

        while( failures < maxFailures )
        {
            String filename = new StringBuffer().append(keypool)
                                                .append(date)
                                                .append("-")
                                                .append(board.getBoardFilename())
                                                .append("-")
                                                .append(index)
                                                .append(".idx").toString();
            File target = new File(filename);
            // First look if this keyfile has already been downloaded
            // and increase the index until we found the last index
            if( target.isFile() && target.length() > 0 )
            {
                index++;
            }
            else
            {
                if( DEBUG ) System.out.println("FILEDN: Requesting index " + index);
                // Download the keyfile
                FcpRequest.getFile(requestKey + index + ".idx.zip",
                                   "Unknown",
                                   target,
                                   requestHtl,
                                   true);
                if( target.length() > 0 )
                {
                    // Add it to the index
                    String unzipped = FileAccess.readZipFile(target);
                    FileAccess.writeFile(unzipped,target);
                    Index.add(target, new File(keypool + board.getBoardFilename()));

                    index++;
                    failures = 0;
                }
                else
                {
                    // download failed. Sometimes there are some 0 byte
                    // files left, we better remove them now.
                    target.delete();
                    failures++;
                    index++;
                }
            }
            if( isInterrupted() ) // check if thread should stop
            {
                notifyThreadFinished(this);
                return;
            }
        }

        // Ok, we're done with downloading the keyfiles
        // Now calculate whitch keys we want to upload.
        // We only upload own keyfiles if:
        // 1. We've got more than minKeyCount keys to upload
        // 2. We don't upload any more files
        //index -= maxFailures;
        if( makeIndexFile() )
        {
            if( !frame1.generateCHK || keyCount >= minKeyCount )
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
    }

    /**Constructor*/
    public UpdateIdThread(FrostBoardObject board)
    {
        super(board);
        this.board = board;
        date = DateFun.getExtendedDate();
        requestHtl = frame1.frostSettings.getValue("keyDownloadHtl");
        insertHtl = frame1.frostSettings.getValue("keyUploadHtl");
        keypool = frame1.frostSettings.getValue("keypool.dir");
        maxKeys = frame1.frostSettings.getIntValue("maxKeys");

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

