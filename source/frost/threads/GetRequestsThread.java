/*
  GetRequestsThread.java / Frost
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
import java.util.*;
import java.util.logging.*;

import javax.swing.JTable;

import frost.*;
import frost.FcpTools.FcpRequest;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.FrostUploadItemObject;
/**
 * Downloads file requests
 */

public class GetRequestsThread extends Thread
{
    static java.util.ResourceBundle LangRes =
        java.util.ResourceBundle.getBundle("res.LangRes");

	private static Logger logger = Logger.getLogger(GetRequestsThread.class.getName());

    //public FrostBoardObject board;
    private int downloadHtl;
    private String keypool;
    private String destination;
    private String fileSeparator = System.getProperty("file.separator");
    private JTable uploadTable;
    private String date;

    /*public int getThreadType()
    {
        return BoardUpdateThread.BOARD_FILE_UPLOAD;
    }*/

    public void run()
    {   
    	//first, start today's thread.
    	if (DateFun.getDate().compareTo(date)!=0) {
    	
			GetRequestsThread todaysRequests = new GetRequestsThread(downloadHtl,keypool,uploadTable, DateFun.getDate());
			todaysRequests.start();
			try{
				todaysRequests.join();
			}catch (InterruptedException e){
				logger.log(Level.SEVERE, "Exception thrown in run()", e);
			}
    	} 	
        // notifyThreadStarted(this);
        try
        {

            // Wait some random time to speed up the update of the TOF table
            // ... and to not to flood the node
            int waitTime = (int) (Math.random() * 5000);
            // wait a max. of 5 seconds between start of threads
            mixed.wait(waitTime);

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            String dirdate = date;

            /*destination = new StringBuffer().append(keypool)
                            .append(board.getBoardFilename()).append(fileSeparator)
                            .append(dirdate).append(fileSeparator).toString();*/

            //yes mister spammer, this is a special for you!
            destination =
                new StringBuffer()
                    .append("requests")
                    .append(fileSeparator)
                    .append(
                        mixed.makeFilename(frame1.getMyId().getUniqueName()))
                    .toString();

            File makedir = new File(destination);
            if (!makedir.exists())
            {
               logger.info(Thread.currentThread().getName() + ": Creating directory: " + destination);
                makedir.mkdirs();
            }

            if (isInterrupted())
            {
                //notifyThreadFinished(this);
                return;
            }

            //start the request loop
            mixed.wait(60 * 1000); // wait 1 min before first start
            boolean firstRun = true;
            while (true)
            {
                dirdate = date;
                if( firstRun )
                {
                    firstRun = false;
                }
                else
                {
                    mixed.wait(15 * 60 * 1000);
                }
                if (Core.getMyBatches().isEmpty())
                    continue;
                //do not start requesting until the user has shared something
                try
                {
                    Iterator it = frame1.getMyBatches().keySet().iterator();
                    while (it.hasNext())
                    {
                        String currentBatch = (String)it.next();
                        int index = 0;
                        int failures = 0;
                        int maxFailures = 3;
                        // increased, skips now up to 3 request indicies (in case if gaps occured)
                        while (failures < maxFailures)
                        {
                            String val =
                                new StringBuffer()
                                    .append(destination)
                                    .append(fileSeparator)
                                    .append(currentBatch.trim())
                                    .append("-")
                                    .append(dirdate)
                                    .append("-")
                                    .append(index)
                                    .append(".req.sha")
                                    .toString();
                            File testMe = new File(val);
                            boolean justDownloaded = false;

                            // already downloaded ?
                            if (testMe.length() > 0)
                            {
                                index++;
                                failures = 0;
                                continue;
                            }
                            else
                            {
                                String tmp =
                                    new StringBuffer()
                                        .append("GetRequestsThread.run, file = ")
                                        .append(testMe.getName())
                                        .append(", failures = ")
                                        .append(failures)
                                        .toString();
                                logger.fine(tmp);

                                FcpRequest.getFile(
                                    "KSK@frost/request/"
                                        + frame1.frostSettings.getValue(
                                            "messageBase")
                                        + "/"
                                        + mixed.makeFilename(frame1.getMyId().getUniqueName())
                                        + "-"
                                        + testMe.getName(),
                                    null,
                                    testMe,
                                    downloadHtl,
                                    false);
                                justDownloaded = true;
                            }

                            // Download successful?
                            if (testMe.length() > 0 /* && justDownloaded */
                                )
                            {
                                logger.fine(
                                    Thread.currentThread().getName()
                                        + " Received request "
                                        + testMe.getName());

                                String content =
                                    (FileAccess.readFileRaw(testMe)).trim();
                                logger.fine(
                                    "Request content is " + content);
                                UploadTableModel tableModel =
                                    (UploadTableModel)uploadTable.getModel();
                                int rowCount = tableModel.getRowCount();

                                for (int i = 0; i < rowCount; i++)
                                {
                                    FrostUploadItemObject ulItem =
                                        (
                                            FrostUploadItemObject)tableModel
                                                .getRow(
                                            i);
                                    String SHA1 = ulItem.getSHA1();
                                    if (SHA1 == null)
                                        continue;
                                    else
                                        SHA1 = SHA1.trim();
                                    //Core.getOut().println("comparing requested "+content + " with "+SHA1);
                                    if (SHA1.equals(content))
                                    {
                                    	logger.fine("content matched!");
                                        // ? is'nt it possible to use uploadItem.getLastUploadData for this?
                                        // probably, this .lck thing is jantho's style 
                                        File requestLock =
                                            new File(
                                                destination + SHA1 + ".lck");
                                        if (!requestLock.exists())
                                        {
                                            // for handling of ENCODING state see ulItem.getNextState() javadoc                            
                                            // changing state ENCODING_REQUESTED to REQUESTED is ok!
                                            if (ulItem.getState()
                                                != FrostUploadItemObject
                                                    .STATE_UPLOADING
                                                && ulItem.getState()
                                                    != FrostUploadItemObject
                                                        .STATE_PROGRESS) //TOTHINK: this is optional
                                            {
                                                logger.fine(
                                                    "Request matches row " + i);
                                                if (ulItem.getState()
                                                    == FrostUploadItemObject
                                                        .STATE_ENCODING)
                                                {
                                                    ulItem.setNextState(
                                                        FrostUploadItemObject
                                                            .STATE_REQUESTED);
                                                }
                                                else
                                                {
                                                    ulItem.setState(
                                                        FrostUploadItemObject
                                                            .STATE_REQUESTED);
                                                }
                                                tableModel.updateRow(ulItem);
                                            } else logger.fine("file was in state uploading/progress");
                                        }
                                        else
                                        {
                                            logger.info(
                                                "File with hash "
                                                    + SHA1
                                                    + " was requested, but already uploaded today");
                                        }
                                    } //else Core.getOut().println("content didn't match");
                                }
                                index++;
                                failures = 0;
                            }
                            else
                            {
                                index++;
                                // this now skips gaps in requests, but gives each download only 1 try
                                failures++;
                            }
                            if (isInterrupted())
                            {
                                break;
                            }
                        }

                    }

                }
                catch (ConcurrentModificationException e)
                {
                    continue;
                }
            }
        } //people with nice ides can refactor :-P
        catch (Throwable t)
        {
			logger.log(Level.SEVERE,  Thread.currentThread().getName() +
						": Oo. EXCEPTION in GetRequestsThread:", t);
        }

        //notifyThreadFinished(this);
    }

    /**Constructor*/
    public GetRequestsThread(int dlHtl, String kpool, JTable uploadTable)
    {
        //super(boa);
        //this.board = boa;
        this(dlHtl,kpool,uploadTable, DateFun.getDate(1));
        
    }
    public GetRequestsThread(int htl, String kpool, JTable table, String date){
		this.downloadHtl = htl;
		this.keypool = kpool;
		this.uploadTable = table;
    	this.date = date;
    }
    
}
