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

/**
 * Downloads file requests
 */
public class GetRequestsThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    public String board;
    private String downloadHtl;
    private String keypool;
    private String destination;
    final String[] block = {"_boardlist", "frost_message_system"};
    private String fileSeparator = System.getProperty("file.separator");
    private JTable uploadTable;

    public void run()
    {
        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        String dirdate = DateFun.getDate();
        //new StringBuffer().append(cal.get(Calendar.YEAR)).append(".")
                         //.append(cal.get(Calendar.MONTH) + 1).append(".").append(cal.get(Calendar.DATE)).toString();

        destination = new StringBuffer().append(keypool).append(board).append(fileSeparator)
                      .append(dirdate).append(fileSeparator).toString();

        File makedir = new File(destination);
        if( !makedir.exists() )
        {
            System.out.println("Creating directory: " + destination);
            makedir.mkdirs();
        }

        int index = 0;
        int failures = 0;
        int maxFailures = 2;

        if( isInterrupted() )
            return;

        while( failures < maxFailures )
        {
            String val = new StringBuffer().append(destination).append(dirdate).append("-")
                .append(board).append("-").append(index).append(".req").toString();


            //File testMe = new File(destination + dirdate + "-" + board + "-" + index + ".req");
            File testMe = new File(val);

            boolean justDownloaded = false;

            val = new StringBuffer().append("GetRequestsThread.run, file = ")
                                    .append(testMe.getName())
                                    .append(", failures = ")
                                    .append(failures).toString();
            System.out.println( val );

            if( testMe.length() > 0 )
            { // already downloaded
                index++;
                failures = 0;
            }
            else
            {
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
                // Normal boards or _boardlist?
                if( !mixed.isElementOf(board, block) )
                {
                    String content = (FileAccess.readFile(testMe)).trim();
                    System.out.println("Request content is " + content);
                    DefaultTableModel tableModel = (DefaultTableModel)uploadTable.getModel();
                    synchronized (uploadTable)
                    {
                        try
                        {
                            int rowCount = tableModel.getRowCount();
                            for( int i = 0; i < rowCount; i++ )
                            {
                                String chk = ((String)tableModel.getValueAt(i, 5)).trim();
                                if( chk.equals(content) )
                                {
                                    File requestLock = new File(destination + chk + ".lck");
                                    if( !requestLock.exists() )
                                    {
                                        String state = (String)tableModel.getValueAt(i, 2);
                                        if( !state.equals(LangRes.getString("Uploading")) && (state.indexOf("Kb") == -1) )
                                        {
                                            System.out.println("Request matches row " + i);
                                            tableModel.setValueAt(LangRes.getString("Requested"), i, 2);
                                        }
                                    }
                                    else
                                    {
                                        System.out.println("File with key " + chk + " was requested, but already uploaded today");
                                    }
                                }
                            }
                        }
                        catch( Exception e )
                        {
                            System.out.println("getRequestsThread.run NOT GOOD "+e.toString());
                        }
                    }
                }
                index++;
                failures = 0;
            }
            else
            {
                failures++;
            }
            if( isInterrupted() )
                break;
        }
        synchronized(frame1.GRTThreads)
        {
            frame1.GRTThreads.removeElement(this);
        }
    }

    /**Constructor*/
    public GetRequestsThread(String boa, String dlHtl, String kpool, JTable uploadTable)
    {
        this.board = boa;
        this.downloadHtl = dlHtl;
        this.keypool = kpool;
        this.uploadTable = uploadTable;
    }

}
