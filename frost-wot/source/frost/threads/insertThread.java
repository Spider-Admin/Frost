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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.gui.objects.*;
import frost.gui.model.*;

public class insertThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    private String destination;
    private String date;
    private File file;
    private String htl;
    private FrostBoardObject board;
    private boolean mode;

    FrostUploadItemObject uploadItem;

    public void run()
    {
        System.out.println("Upload of " + file + " with HTL " + htl + " started.");

        if( mode )
            synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads++;
            }
        else
            frame1.generateCHK = true;

        try {

        String status = LangRes.getString("Never");
        boolean success = false;
        String[] result = {"Error", "Error"};

        if( file.length() > 0 && file.isFile() )
        {

            result = FcpInsert.putFile("CHK@", file, htl, true, mode,board.getBoardFilename());

            if( result[0].equals("Success") )
            {
                success = true;
                System.out.println("Upload of " + file + " successfull.");
            }
            if( result[0].equals("KeyCollision") )
            {
                success = true;
                System.out.println("Upload of " + file + " collided.");
            }

            if( success )
            {
                status = date;
                KeyClass newKey = new KeyClass(result[1]);
                newKey.setFilename(destination);
                newKey.setSize(file.length());
                newKey.setDate(date);
                Index.add(newKey, new File(frame1.keypool + board.getBoardFilename()));
            }

            final String finalStatus = status;
            final String finalKey = result[1];
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    UploadTableModel tableModel = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();
                    if( mode )
                    {
                        uploadItem.setState( finalStatus );
                    }
                    else
                    {
                        uploadItem.setKey( finalKey );
                    }
                    tableModel.updateRow( uploadItem );
                } });
        }
        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in insertThread.run:");
            t.printStackTrace();
        }

        if( mode )
        {
            synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads--;
            }
        }
        else
        {
            frame1.generateCHK = false;
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
        this.date = DateFun.getExtendedDate();
        this.mode = mode; // true=upload file false=generate chk (do not upload)
    }
}
