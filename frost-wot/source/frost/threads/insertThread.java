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
import java.util.Random;

import frost.*;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.*;
import frost.messages.*;
import frost.FcpTools.*;

public class insertThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    
    public static final int MODE_GENERATE_SHA1 = 1;
    public static final int MODE_GENERATE_CHK  = 2;
    public static final int MODE_UPLOAD        = 3;

    private int nextState; // the state to set on uploadItem when finished, or -1 for default (IDLE)
    private String destination;
    private File file;
    private int htl;
    private FrostBoardObject board;
    private int mode;
    private static int fileIndex=1;
    private static Random r = new Random();
    //this is gonna be ugly
    private static String batchId = frame1.getMyBatches().values().size() == 0 ?
    				(new Long(r.nextLong())).toString() :
				(String) frame1.getMyBatches().values().iterator().next();
    private static final int batchSize = 100; //TODO: get this from options
    //private static final Object putIt = frame1.getMyBatches().put(batchId,batchId);
    //^^ ugly trick to put the initial batch number

    FrostUploadItemObject uploadItem;

    public void run()
    {
    	if (batchId==null) {
    		Error er = new Error();
    		er.fillInStackTrace();
    		er.printStackTrace(Core.getOut());
    	}
    	if (Core.getMyBatches().values().size()==0)
    		Core.getMyBatches().put(batchId,batchId);
    		
        try
        {
            String lastUploadDate = null; // NEVER uploaded
            boolean success = false;
            String[] result = { "Error", "Error" };
            String currentDate = DateFun.getExtendedDate();
            boolean sign = frame1.frostSettings.getBoolValue("signUploads");

            if( mode == MODE_UPLOAD )
            { //real upload
                Core.getOut().println("Upload of " + file + " with HTL " + htl + " started.");
                synchronized (frame1.threadCountLock)
                {
                    frame1.activeUploadThreads++;
                }

                result =
                    FcpInsert.putFile(
                        "CHK@",
                        file,
                        null,// metadata
                        htl,
                        true, // doRedirect
                        uploadItem); // provide the uploadItem to indicate that this upload is contained in table

                if( result[0].equals("Success") ||
                    result[0].equals("KeyCollision") )
                {
                    success = true;
                    Core.getOut().println("Upload of " + file + " was successful.");
                    uploadItem.setKey(result[1]);
                    lastUploadDate = currentDate;
                }
                else
                {
                    Core.getOut().println("Upload of " + file + " was NOT successful.");
                }

                // item uploaded (maybe)
                // REDFLAG: but this also resets upload date of yesterday, ... !
                if( lastUploadDate != null ) 
                    uploadItem.setLastUploadDate(lastUploadDate);
                // if NULL then upload failed -> shows NEVER in table

                uploadItem.setState(this.nextState);

                synchronized (frame1.threadCountLock)
                {
                    frame1.activeUploadThreads--;
                }

                //now update the files.xml with the CHK
                // REDFLAG: was this really intented to run even if upload failed?
                //what was it before?
                if( success == true )
                {
                    SharedFileObject current = new SharedFileObject(uploadItem.getKey());
                    if (sign)
                        current.setOwner(frame1.getMyId().getUniqueName());
                    current.setFilename(uploadItem.getFileName());
                    current.setSHA1(uploadItem.getSHA1());
                    current.setBatch(uploadItem.getBatch());
                    current.setSize(uploadItem.getFileSize().longValue());
                    current.setDate(lastUploadDate);
                    current.setLastSharedDate(lastUploadDate);
                    Index.addMine(current, board);
                    Index.add(current, board);
                }
            }
            else if( mode == MODE_GENERATE_SHA1 )
            {
                frame1.setGeneratingCHK(true);

                //if this is the first startup, put at least one batch in
                /*   if (frame1.getMyBatches().values().size() == 0) //
                   	frame1.getMyBatches().put(batchId,batchId);
                   else {
                   	//take the first patch we come upon
                   }*/

                if (fileIndex % batchSize == 0)
                {
                    frame1.getMyBatches().put(batchId, batchId);
                    while (frame1.getMyBatches().contains(batchId))
                        batchId = (new Long(r.nextLong())).toString();
                    frame1.getMyBatches().put(batchId, batchId);
                }

                long now = System.currentTimeMillis();
                String SHA1 = frame1.getCrypto().digest(file);
                Core.getOut().println(
                    "digest generated in "
                        + (System.currentTimeMillis() - now)
                        + "  "
                        + SHA1);

                //create new KeyClass
                SharedFileObject newKey = new SharedFileObject();
                newKey.setKey(null);
                newKey.setDate(null);
                newKey.setLastSharedDate(DateFun.getDate());
                newKey.setSHA1(SHA1);
                newKey.setFilename(destination);
                newKey.setSize(file.length());
                newKey.setBatch(batchId);
                if (sign)
                    newKey.setOwner(frame1.getMyId().getUniqueName());

                //update the gui
                uploadItem.setSHA1(SHA1);
                uploadItem.setKey(null);
                uploadItem.setLastUploadDate(null);
                uploadItem.setBatch(batchId);
                fileIndex++;
                //add to index
                Index.addMine(newKey, board);
                Index.add(newKey, board);

                uploadItem.setState(this.nextState);
                frame1.setGeneratingCHK(false);
            }
            else if( mode == MODE_GENERATE_CHK )
            {
                frame1.setGeneratingCHK(true);
                Core.getOut().println("CHK generation started for file: " + file);
                String chkkey = null;
                
                if( file.length() <= FcpInsert.smallestChunk )
                {
                    System.out.println("File too short, does'nt need encoding.");
                    // generate only CHK
                    chkkey = FecTools.generateCHK(file);
                }
                else
                {
                    FecSplitfile splitfile = new FecSplitfile( file );
                    boolean alreadyEncoded = splitfile.uploadInit();
                    if( !alreadyEncoded )
                    {
                        try {
                            splitfile.encode();
                        }
                        catch(Throwable t)
                        {
                            Core.getOut().println("Encoding failed:");
                            t.printStackTrace();
                            uploadItem.setState(FrostUploadItemObject.STATE_IDLE);
                            ((UploadTableModel)frame1.getInstance().getUploadTable().getModel()).updateRow(uploadItem);
                            return;
                        }
                    }
                    // yes, this destroys any upload progress, but we come only here if 
                    // chkKey == null, so the file should'nt be uploaded until now 
                    splitfile.createRedirectFile(false); // gen normal redirect file for CHK generation
                    
                    chkkey = FecTools.generateCHK(splitfile.getRedirectFile(), splitfile.getRedirectFile().length());
                }
                
                if( chkkey != null )
                {
                    String prefix = new String("freenet:");
                    if( chkkey.startsWith(prefix) ) chkkey = chkkey.substring(prefix.length());
                }
                else
                {
                    Core.getOut().println("Could not generate CHK key for redirect file.");
                }
                uploadItem.setKey(chkkey);
                
                // test if the GetRequestsThread did set us the nextState field...
                if( uploadItem.getNextState() > 0 )
                {
                    uploadItem.setState( uploadItem.getNextState() );
                    uploadItem.setNextState(0); // reset nextState
                }
                else
                {
                    uploadItem.setState( this.nextState );
                }
                frame1.setGeneratingCHK(false);
            }
            ((UploadTableModel)frame1.getInstance().getUploadTable().getModel()).updateRow(uploadItem);
        }
        catch (Throwable t)
        {
            t.printStackTrace(Core.getOut());
        }
        frame1.setGeneratingCHK(false);
    }

    /**Constructor*/
    public insertThread(FrostUploadItemObject ulItem, SettingsClass config, int mode)
    {
        this(ulItem, config, mode, -1);
    }
    public insertThread(FrostUploadItemObject ulItem, SettingsClass config, int mode, int nextState)
    {
        this.destination = ulItem.getFileName();
        this.file = new File(ulItem.getFilePath());

        this.uploadItem = ulItem;

        this.htl = config.getIntValue("htlUpload");
        this.board = ulItem.getTargetBoard();
        this.mode = mode; // true=upload file false=generate chk (do not upload)
        this.nextState = nextState;
        if( this.nextState < 0 )
        {
            this.nextState = FrostUploadItemObject.STATE_IDLE;
        }
    }
}
