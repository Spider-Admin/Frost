/*
  requestThread.java / Frost
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
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;

import frost.*;
import frost.gui.*;
import frost.gui.objects.*;
import frost.gui.model.*;

public class requestThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    final boolean DEBUG = true;
    private String filename;
    private Long size;
    private String key;
    private Integer htl;
    private DownloadTable downloadTable;
    private FrostBoardObject board;

    private FrostDownloadItemObject downloadItem;

    public void run()
    {
        // increase thread counter
        synchronized(frame1.threadCountLock)
        {
            frame1.activeDownloadThreads++;
        }
        try {
        // some vars
        final DownloadTableModel tableModel = (DownloadTableModel)downloadTable.getModel();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        Date today = new Date();
        String date = formatter.format(today);
        File newFile = new File(frame1.frostSettings.getValue("downloadDirectory") + filename);
        boolean do_request = false;

        System.out.println("Download of " + filename + " with HTL " + htl.toString() + " started.");

        // Download file
        boolean success = false;
        try {
            success = FcpRequest.getFile(key, size.toString(), newFile, htl.toString(), true);
        }
        catch(Throwable t) { ; }

        // file might be erased from table during download...
        boolean inTable = false;
        for( int x=0; x<tableModel.getRowCount(); x++ )
        {
            FrostDownloadItemObject actItem = (FrostDownloadItemObject)tableModel.getRow(x);
            if( actItem.getKey().equals( downloadItem.getKey() ) )
            {
                inTable = true;
                break;
            }
        }

        // download failed
        if( !success )
        {
            System.out.println("Download of " + filename + " failed.");
            if( inTable == true )
            {
                // Upload request to request stack
                int intHtl = htl.intValue();

                if( intHtl > frame1.frostSettings.getIntValue("startRequestingAfterHtl") )
                {
                    if( DEBUG ) System.out.println("Download failed, uploading request for " + filename);
                    SwingUtilities.invokeLater(new Runnable()
                           {
                               public void run()
                               {
                                   downloadItem.setState( "Requesting" ); // TODO: translate
                                   tableModel.updateRow( downloadItem );
                               }
                           });
                    // We may not do the request here due to the synchronize
                    // -> no lock needed, using models
                    // doing it after this , the table states Waiting and there are threads running,
                    // so download seems to stall
                    try {
                        request(key.trim(), board);
                        if( DEBUG ) System.out.println("Uploaded request for " + filename);
                    }
                    catch(Throwable t) {
                        System.out.println("Uploading request failed for "+filename);
                    }

                }
                else
                {
                    // leave state at FAILED, will be resettet in frame1
                    if( DEBUG ) System.out.println("Download failed, but htl is too low to request it.");
                }
                // Download / restart failed downloads
                int columnDataHtl = downloadItem.getHtl().intValue();
                if( columnDataHtl < frame1.frostSettings.getIntValue("htlMax") )
                {
                    columnDataHtl += 1;
                    downloadItem.setHtl( columnDataHtl );
                    downloadItem.setState( LangRes.getString("Waiting") );
                }
                else
                {
                    downloadItem.setState( LangRes.getString("Failed") ); // max htl reached, no more updating
                }

                SwingUtilities.invokeLater(new Runnable()
                       {
                           public void run()
                           {
                               tableModel.updateRow( downloadItem );
                           }
                       });
            }
        }
        // download successfull
        else
        {
            // Add successful downloaded key to database
            KeyClass newKey = new KeyClass(key);
            newKey.setFilename(filename);
            newKey.setSize(newFile.length());
            newKey.setDate(date);
            newKey.setExchange(false);
            Index.add(newKey, new File(frame1.keypool + board.getBoardFilename()));

            SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            downloadItem.setState( LangRes.getString("Done") );
                            tableModel.updateRow( downloadItem );
                            frame1.updateDownloads = true;
                        }
                    });
        }
        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in requestThread.run:");
            t.printStackTrace();
        }

        synchronized(frame1.threadCountLock)
        {
            frame1.activeDownloadThreads--;
        }
    }

    // Request a certain CHK from a board
    private void request(String key, FrostBoardObject board)
    {

        String messageUploadHtl = frame1.frostSettings.getValue("tofUploadHtl");
        boolean requested = false;

        if( DEBUG ) System.out.println("Uploading request of " + key + " from " + board.toString());

        String fileSeparator = System.getProperty("file.separator");
        String destination = frame1.keypool + board.getBoardFilename() + fileSeparator + DateFun.getDate() + fileSeparator;

        File checkDestination = new File(destination);
        if( !checkDestination.isDirectory() )
            checkDestination.mkdirs();

        // Check if file was already requested
        File[] files = checkDestination.listFiles();
        for( int i = 0; i < files.length; i++ )
        {
            String content = (FileAccess.readFile(files[i])).trim();
            if( content.equals(key) )
            {
                requested = true;
                System.out.println("File was already requested");
            }
        }

        if( !requested )
        {
            String date = DateFun.getDate();
            String time = DateFun.getFullExtendedTime() + "GMT";

            // Generate file to upload
            String uploadMe = String.valueOf(System.currentTimeMillis()) + ".txt"; // new filename

            File messageFile = new File(destination + uploadMe);
            FileAccess.writeFile(key, messageFile); // Write to disk

            // Search empty slut (hehe)
            boolean success = false;
            int index = 0;
            String output = new String();
            int tries = 0;
            boolean error = false;
            while( !success )
            {
                // Does this index already exist?
                File testMe = new File(new StringBuffer().append(destination).append(date).append("-")
                                       .append(board.getBoardFilename()).append("-").append(index).append(".req").toString());
                if( testMe.length() > 0 )
                { // already downloaded
                    index++;
                    if( DEBUG ) System.out.println("File exists, increasing index to " + index);
                }
                else
                { // probably empty
                    String[] result = new String[2];
                    String upKey = new StringBuffer().append("KSK@frost/request/")
                                   .append(frame1.frostSettings.getValue("messageBase"))
                                   .append("/").append(date).append("-")
                                   .append(board.getBoardFilename()).append("-").append(index).append(".req").toString();
                    if( DEBUG ) System.out.println(upKey);
                    result = FcpInsert.putFile(upKey, destination + uploadMe, messageUploadHtl, false, true,
                                               board.getBoardFilename());
                    System.out.println("FcpInsert result[0] = " + result[0] + " result[1] = " + result[1]);

                    if( result[0] == null || result[1] == null )
                    {
                        result[0] = "Error";
                        result[1] = "Error";
                    }

                    if( result[0].equals("Success") )
                    {
                        success = true;
                    }
                    else
                    {
                        if( result[0].equals("KeyCollision") )
                        {

                            // Check if the collided key is perhapes the requested one
                            String compareMe = String.valueOf(System.currentTimeMillis()) + ".txt";
                            String requestMe = new StringBuffer().append("KSK@frost/request/")
                                               .append(frame1.frostSettings.getValue("messageBase")).append("/")
                                               .append(date).append("-").append(board.getBoardFilename()).append("-").append(index).append(".req").toString();

                            if( FcpRequest.getFile(requestMe,
                                                   "Unknown",
                                                   frame1.keypool + compareMe,
                                                   htl.toString(),
                                                   false) )
                            {

                                File numberOne = new File(frame1.keypool + compareMe);
                                File numberTwo = new File(destination + uploadMe);
                                String contentOne = (FileAccess.readFile(numberOne)).trim();
                                String contentTwo = (FileAccess.readFile(numberTwo)).trim();

                                if( DEBUG ) System.out.println(contentOne);
                                if( DEBUG ) System.out.println(contentTwo);

                                if( contentOne.equals(contentTwo) )
                                {
                                    if( DEBUG ) System.out.println("Key Collision and file was already requested");
                                    success = true;
                                }
                                else
                                {
                                    index++;
                                    System.out.println("Request Upload collided, increasing index to " + index);
                                }
                            }
                            else
                            {
                                System.out.println("Request upload failed (" + tries + "), retrying index " + index);
                                if( tries > 5 )
                                {
                                    success = true;
                                    error = true;
                                }
                                tries++;
                            }
                        }
                    }
                }
            }

            if( !error )
            {

                File killMe = new File(destination + uploadMe);
                File newMessage = new File(destination + date + "-" + board.getBoardFilename() + "-" + index + ".req");
                killMe.renameTo(newMessage);

                TOF.addNewMessageToTable( newMessage, board );

                System.out.println("*********************************************************************");
                System.out.println("Request successfuly uploaded to the '" + board + "' board.");
                System.out.println("*********************************************************************");
            }
            else
            {
                System.out.println("Error while uploading message.");
                messageFile.delete();
            }
            System.out.println("Request Upload Thread finished");
        }
    }

    /**Constructor*/
    public requestThread( FrostDownloadItemObject dlItem, DownloadTable downloadTable )
    {
        this.filename = dlItem.getFileName();
        this.size = dlItem.getFileSize();
        this.htl = dlItem.getHtl();
        this.key = dlItem.getKey();
        this.board = dlItem.getSourceBoard();

        this.downloadItem = dlItem;

        this.downloadTable = downloadTable;
    }
}

