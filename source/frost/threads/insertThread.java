/*
  insertThread.java / Frost
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

import java.io.File;

import frost.*;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.*;

public class insertThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    private String destination;
    private File file;
    private String htl;
    private FrostBoardObject board;
    private boolean mode;

    FrostUploadItemObject uploadItem;

    public void run()
    {
    try{    
    
	String lastUploadDate = null; // NEVER uploaded
        boolean success = false;
        String[] result = {"Error", "Error"};
	String currentDate = DateFun.getExtendedDate();
    	
        if( mode ) {  //real upload
	    System.out.println("Upload of " + file + " with HTL " + htl + " started.");
            synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads++;
            }
	    
	    result = FcpInsert.putFile("CHK@",
                                       file,
                                       htl,
                                       true,
                                       mode,
                                       board.getBoardFilename());

            if( result[0].equals("Success") )
            {
                success = true;
                System.out.println("Upload of " + file + " successfull.");
		uploadItem.setKey(result[1]);
		lastUploadDate=currentDate;
            }
            else if( result[0].equals("KeyCollision") )
            {
                // collided means file is already in freenet
                success = true;
                System.out.println("Upload of " + file + " collided.");
		uploadItem.setKey(result[1]);
		lastUploadDate = uploadItem.getLastUploadDate();
            }
	    
	    // item uploaded (maybe)
            uploadItem.setLastUploadDate( lastUploadDate ); // if NULL then upload failed -> shows NEVER in table
	    //uploadItem.setKey(result[1]);
	    
	    uploadItem.setState( FrostUploadItemObject.STATE_IDLE );
	    
	    synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads--;
            }
	    
	    //now update the files.xml with the CHK
	    KeyClass current = new KeyClass(result[1]);
	    current.setOwner(frame1.getMyId().getUniqueName());
	    current.setFilename(uploadItem.getFileName());
	    current.setSHA1(uploadItem.getSHA1());
	    current.setSize(uploadItem.getFileSize().longValue());
	    current.setDate(lastUploadDate);
	    Index.addMine(current,board);
	}
        else
        {
            frame1.setGeneratingCHK( true );
	    long now = System.currentTimeMillis();
	    String  SHA1 = frame1.getCrypto().digest(file);
	    System.out.println("digest generated in "+(System.currentTimeMillis()-now) +
				 "  " + SHA1);
				 
	    //create new KeyClass
	    KeyClass newKey = new KeyClass();
	    newKey.setKey(null);
	    newKey.setDate(null);
	    newKey.setSHA1(SHA1);  
            newKey.setFilename(destination);
            newKey.setSize(file.length());
	    newKey.setOwner(frame1.getMyId().getUniqueName());
	    
	    //update the gui
	    uploadItem.setSHA1( SHA1 );
	    uploadItem.setKey(null);
	    uploadItem.setLastUploadDate(null);
	    
	    //add to index
            Index.addMine(newKey, board);
	    
	    frame1.setGeneratingCHK( false );
	    
        }
	
	    UploadTableModel tableModel = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();
            tableModel.updateRow( uploadItem );
        
	}catch(Throwable t) {
		t.printStackTrace();
	}
        

        
    }

    /**Constructor*/
    public insertThread(FrostUploadItemObject ulItem, SettingsClass config, boolean mode)
    {
        this.destination = ulItem.getFileName();
        this.file = new File(ulItem.getFilePath());

        this.uploadItem = ulItem;

        this.htl = config.getValue("htlUpload");
        this.board = ulItem.getTargetBoard();
        this.mode = mode; // true=upload file false=generate chk (do not upload)
    }
}
