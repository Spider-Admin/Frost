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

import java.io.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;

import frost.*;
import frost.gui.objects.*;
import frost.gui.model.*;

/**
 * Downloads file requests
 */
public class GetRequestsThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    public FrostBoardObject board;
    private String downloadHtl;
    private String keypool;
    private String destination;
    private String fileSeparator = System.getProperty("file.separator");
    private JTable uploadTable;

    public int getThreadType()
    {
        return BoardUpdateThread.BOARD_FILE_UPLOAD;
    }

    public void run()
    {
        notifyThreadStarted(this);
        try {

        // Wait some random time to speed up the update of the TOF table
        // ... and to not to flood the node
        int waitTime = (int)(Math.random() * 5000); // wait a max. of 5 seconds between start of threads
        mixed.wait(waitTime);

        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        String dirdate = DateFun.getDate();

        destination = new StringBuffer().append(keypool)
                        .append(board.getBoardFilename()).append(fileSeparator)
                        .append(dirdate).append(fileSeparator).toString();

        File makedir = new File(destination);
        if( !makedir.exists() )
        {
            System.out.println("Creating directory: " + destination);
            makedir.mkdirs();
        }

        if( isInterrupted() )
        {
            notifyThreadFinished(this);
            return;
        }

        int index = 0;
        int failures = 0;
        int maxFailures = 3; // increased, skips now up to 3 request indicies (in case if gaps occured)
        while( failures < maxFailures )
        {
            String val = new StringBuffer().append(destination).append(dirdate).append("-")
                .append(board.getBoardFilename()).append("-").append(index).append(".req").toString();

            File testMe = new File(val);
            boolean justDownloaded = false;

            // already downloaded ?
            if( testMe.length() > 0 )
            {
                index++;
                failures = 0;
            }
            else
            {
                String tmp = new StringBuffer().append("GetRequestsThread.run, file = ")
                                        .append(testMe.getName())
                                        .append(", failures = ")
                                        .append(failures).toString();
                System.out.println( tmp );

                FcpRequest.getFile("KSK@frost/request/" +
                                   frame1.frostSettings.getValue("messageBase") + "/" + testMe.getName(),
                                   "Unknown",
                                   testMe,
                                   downloadHtl,
                                   false);
                justDownloaded = true;
            }

            // Download successful?
            if( testMe.length() > 0 /* && justDownloaded */ )
            {
                System.out.println("Received request " + testMe.getName());

                String content = (FileAccess.readFile(testMe)).trim();
                System.out.println("Request content is " + content);
                UploadTableModel tableModel = (UploadTableModel)uploadTable.getModel();
                int rowCount = tableModel.getRowCount();

                for( int i = 0; i < rowCount; i++ )
                {
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow(i);
                    String chk = ulItem.getKey().trim();
                    if( chk.equals(content) )
                    {
                        File requestLock = new File(destination + chk + ".lck");
                        if( !requestLock.exists() )
                        {
                            if( ulItem.getState() != ulItem.STATE_UPLOADING &&
                                ulItem.getState() != ulItem.STATE_PROGRESS )
                            {
                                System.out.println("Request matches row " + i);
                                ulItem.setState( ulItem.STATE_REQUESTED );
                                tableModel.updateRow( ulItem );
                            }
                        }
                        else
                        {
                            System.out.println("File with key " + chk + " was requested, but already uploaded today");
                        }
                    }
                }
                index++;
                failures = 0;
            }
            else
            {
                index++; // this now skips gaps in requests, but gives each download only 1 try
                failures++;
            }
            if( isInterrupted() )
            {
                break;
            }
        }

        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in GetRequestsThread:");
            t.printStackTrace();
        }
        notifyThreadFinished(this);
    }

    /**Constructor*/
    public GetRequestsThread(FrostBoardObject boa, String dlHtl, String kpool, JTable uploadTable)
    {
        super(boa);
        this.board = boa;
        this.downloadHtl = dlHtl;
        this.keypool = kpool;
        this.uploadTable = uploadTable;
    }
}
