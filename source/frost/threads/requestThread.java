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
import java.util.Date;

import frost.*;
import frost.gui.DownloadTable;
import frost.gui.model.DownloadTableModel;
import frost.gui.objects.*;

public class requestThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    public static final String KEYCOLL_INDICATOR = "ERROR: key collision";

    final boolean DEBUG = true;
    private String filename;
    private Long size;
    private String key;
    private String SHA1;
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

	//if we don't have the CHK, means the key was not inserted
	//request it by SHA1
	if (key == null) {
		System.out.println("FILEDN: Requesting " + filename);
		downloadItem.setState( FrostDownloadItemObject.STATE_REQUESTING );
                tableModel.updateRow( downloadItem );
		    
		//request the file itself
		try {
                        request(SHA1.trim(), board);
                        if( DEBUG ) System.out.println("FILEDN: Uploaded request for " + filename);
                    }
               catch(Throwable t) {
                        System.out.println("FILEDN: Uploading request failed for "+filename);
			t.printStackTrace();
		}
		downloadItem.setState( FrostDownloadItemObject.STATE_REQUESTED );
		tableModel.updateRow( downloadItem );
		synchronized(frame1.threadCountLock)
        	{
         	   frame1.activeDownloadThreads--;
       		}
                return;
	} 
	
	//otherwise, proceed as usual
	
        System.out.println("FILEDN: Download of '" + filename + "' started.");

        	// Download file
        boolean success = false;
	
	try {
            success = FcpRequest.getFile(key, size, newFile, 25, true, false, downloadItem);
        }
        catch(Throwable t) { t.printStackTrace(); }  //please don't do like this { ; }--zab
	

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
            downloadItem.setRetries( downloadItem.getRetries() + 1 );

            System.out.println("FILEDN: Download of " + filename + " failed.");
            if( inTable == true )
            {
                // Upload request to request stack
                if( frame1.frostSettings.getBoolValue("downloadEnableRequesting") &&
                    downloadItem.getRetries() >= frame1.frostSettings.getIntValue("downloadRequestAfterTries") &&
                    board != null && board.isFolder() == false )
                {
                    if( DEBUG ) System.out.println("FILEDN: Download failed, uploading request for " + filename);
                    downloadItem.setState( FrostDownloadItemObject.STATE_REQUESTING );
                    tableModel.updateRow( downloadItem );

                    // We may not do the request here due to the synchronize
                    // -> no lock needed, using models
                    // doing it after this , the table states Waiting and there are threads running,
                    // so download seems to stall
                    try {
                        request(SHA1.trim(), board);
                        if( DEBUG ) System.out.println("FILEDN: Uploaded request for " + filename);
                    }
                    catch(Throwable t) {
                        System.out.println("FILEDN: Uploading request failed for "+filename);
                    }
                }
                else
                {
                    if( DEBUG ) System.out.println("FILEDN: Download failed (file is NOT requested).");
                }

                // set new state -> failed or waiting for another try
                if( downloadItem.getRetries() > frame1.frostSettings.getIntValue("downloadMaxRetries") )
                {
                    if( frame1.frostSettings.getBoolValue("downloadRestartFailedDownloads" ) )
                    {
                        downloadItem.setState( FrostDownloadItemObject.STATE_WAITING );
                        downloadItem.setRetries( 0 );
                    }
                    else
                    {
                        downloadItem.setState( FrostDownloadItemObject.STATE_FAILED );
                    }
                }
                else
                {
                    downloadItem.setState( FrostDownloadItemObject.STATE_WAITING );
                }

                tableModel.updateRow( downloadItem );
            }
        }
        // download successfull
        else
        {
            if( board != null && board.isFolder() == false )
            {
                // Add successful downloaded key to database
                KeyClass newKey = new KeyClass(key);
                newKey.setFilename(filename);
                newKey.setSize(newFile.length());
                newKey.setDate(date);
                newKey.setExchange(false);
                Index.add(newKey, new File(frame1.keypool + board.getBoardFilename()));
            }

            downloadItem.setFileSize( newFile.length() );
            downloadItem.setState( FrostDownloadItemObject.STATE_DONE );
            downloadItem.setEnableDownload( Boolean.valueOf(false) );

            tableModel.updateRow( downloadItem );
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
        downloadItem.setLastDownloadStopTimeMillis( System.currentTimeMillis() );
    }

    // Request a certain CHK from a board
    private void request(String SHA1, FrostBoardObject board)
    {
        String messageUploadHtl = frame1.frostSettings.getValue("tofUploadHtl");
        boolean requested = false;

        if( DEBUG ) System.out.println("FILEDN: Uploading request for '"+filename+"' to board '" + board.toString()+"'");

        String fileSeparator = System.getProperty("file.separator");
        String destination = new StringBuffer().append(frame1.keypool)
                                               .append(board.getBoardFilename())
                                               .append(fileSeparator)
                                               .append(DateFun.getDate())
                                               .append(fileSeparator)
                                               .toString();
        File checkDestination = new File(destination);
        if( !checkDestination.isDirectory() )
            checkDestination.mkdirs();

        // Check if file was already requested
        // ++ check only in req files
        File[] files = checkDestination.listFiles( new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        if( name.endsWith(".req") )
                            return true;
                        return false;
                    } });
        for( int i = 0; i < files.length; i++ )
        {
            String content = (FileAccess.readFile(files[i])).trim();
            if( content.equals(key) )
            {
                requested = true;
                System.out.println("FILEDN: File '"+filename+"' was already requested");
                break;
            }
        }

        if( !requested )
        {
            String date = DateFun.getDate();
            String time = DateFun.getFullExtendedTime() + "GMT";

            // Generate file to upload
            File requestFile = null;
            try {
                requestFile = File.createTempFile("reqUpload_", null, new File( frame1.frostSettings.getValue("temp.dir") ) );
            }
            catch(Exception ex)
            {
                requestFile = new File( frame1.frostSettings.getValue("temp.dir") + System.currentTimeMillis()+".tmp" );
            }
	    //TOTHINK: we can also encrypt the request
            FileAccess.writeFile(SHA1, requestFile); // Write requested key to disk

            // Search empty slot
            boolean success = false;
            int index = 0;
            String output = new String();
            int tries = 0;
            boolean error = false;
            File testMe = null;
            while( !success )
            {
                // Does this index already exist?
                testMe = new File(new StringBuffer().append(destination)
                                       .append(date)
                                       .append("-")
                                       .append(board.getBoardFilename())
                                       .append("-")
                                       .append(index)
                                       .append(".req.sha")
                                       .toString());
                if( testMe.length() > 0 )
                { // already downloaded
                    index++;
                    //if( DEBUG ) System.out.println("FILEDN: File exists, increasing index to " + index);
                    continue; // while
                }
                else
                {
                    // probably empty, check if other threads currently try to insert to this index
                    File lockRequestIndex = new File( testMe.getPath() + ".lock" );
                    boolean lockFileCreated = false;
                    try { lockFileCreated = lockRequestIndex.createNewFile(); }
                    catch(IOException ex) {
                        System.out.println("ERROR: requestThread.request(): unexpected IOException, terminating thread ...");
                        ex.printStackTrace();
                        return;
                    }

                    if( lockFileCreated == false )
                    {
                        // another thread tries to insert using this index, try next
                        index++;
                        if( DEBUG ) System.out.println("FILEDN: Other thread tries this index, increasing index to " + index);
                        continue; // while
                    }
                    else
                    {
                        // we try this index
                        lockRequestIndex.deleteOnExit();
                    }

                    // try to insert
		    //TOTHINK: we could add the ability files from a specific user to be
		    //requested on separate channels - good protection vs.spam
                    String[] result = new String[2];
                    String upKey = new StringBuffer().append("KSK@frost/request/")
                                   .append(frame1.frostSettings.getValue("messageBase"))
                                   .append("/")
                                   .append(date)
                                   .append("-")
                                   .append(board.getBoardFilename())
                                   .append("-")
                                   .append(index)
                                   .append(".req.sha")
                                   .toString();
                    if( DEBUG ) System.out.println(upKey);
                    result = FcpInsert.putFile(upKey,
                                               requestFile.getPath(),
                                               messageUploadHtl,
                                               false,
                                               true,
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
                    else if( result[0].equals("KeyCollision") )
                    {
                        // Check if the collided key is perhapes the requested one
                        File compareMe = null;
                        try {
                            compareMe = File.createTempFile("reqUploadCmpDnload_", null, new File( frame1.frostSettings.getValue("temp.dir") ) );
                        }
                        catch(Exception ex)
                        {
                            compareMe = new File( frame1.frostSettings.getValue("temp.dir") + System.currentTimeMillis()+".tmp" );
                        }
                        compareMe.deleteOnExit();

                        String requestMe = upKey;

                        if( FcpRequest.getFile(requestMe,
                                               null,
                                               compareMe,
                                               25,
                                               false) )
                        {
                            File numberOne = compareMe;
                            File numberTwo = requestFile;
                            String contentOne = (FileAccess.readFile(numberOne)).trim();
                            String contentTwo = (FileAccess.readFile(numberTwo)).trim();

                            //if( DEBUG ) System.out.println(contentOne);
                            //if( DEBUG ) System.out.println(contentTwo);

                            if( contentOne.equals(contentTwo) )
                            {
                                if( DEBUG ) System.out.println("FILEDN: Key Collision and file was already requested");
                                success = true;
                            }
                            else
                            {
                                index++;
                                System.out.println("FILEDN: Request Upload collided, increasing index to " + index);

                                if( frame1.frostSettings.getBoolValue("disableRequests") == true )
                                {
                                    // uploading is disabled, therefore already existing requests are not
                                    // written to disk, causing key collosions on every request insert.

                                    // this write a .req file to inform others to not try this index again
                                    // if user switches to uploading enabled, this dummy .req files should
                                    // be silently deleted to enable receiving of new requests
                                    FileAccess.writeFile(KEYCOLL_INDICATOR, testMe);
                                }
                            }
                        }
                        else
                        {
                            System.out.println("FILEDN: Request upload failed (" + tries + "), retrying index " + index);
                            if( tries > 5 )
                            {
                                success = true;
                                error = true;
                            }
                            tries++;
                        }
                        compareMe.delete();
                    }
                    // finally delete the index lock file
                    lockRequestIndex.delete();
                }
            }

            if( !error )
            {
                requestFile.renameTo(testMe);

                System.out.println("*********************************************************************");
                System.out.println("Request for '"+filename+"' successfully uploaded to board '" + board + "'.");
                System.out.println("*********************************************************************");
            }
            else
            {
                System.out.println("\nFILEDN: Error while uploading request for '"+filename+"' to board '" + board + "'.");
                requestFile.delete();
            }
            System.out.println("FILEDN: Request Upload Thread finished");
        }
    }

    /**Constructor*/
    public requestThread( FrostDownloadItemObject dlItem, DownloadTable downloadTable )
    {
        this.filename = dlItem.getFileName();
        this.size = dlItem.getFileSize();
        this.key = dlItem.getKey();
        this.board = dlItem.getSourceBoard();
	this.SHA1 = dlItem.getSHA1();
	

        this.downloadItem = dlItem;

        this.downloadTable = downloadTable;
    }
}

