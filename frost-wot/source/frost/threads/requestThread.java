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

public class requestThread extends Thread {
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    final boolean DEBUG = true;
    private String filename;
    private String size;
    private String key;
    private String htl;
    private JTable downloadTable;
    private JTable uploadTable;
    private String board;

    public void run() {

    // increase thread counter
    synchronized(frame1.threadCountLock) {
    frame1.activeDownloadThreads++;
    }

    // some vars
    DefaultTableModel tableModel = (DefaultTableModel)downloadTable.getModel();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
    Date today = new Date();
    String date = formatter.format(today);
    File newFile = new File(frame1.frostSettings.getValue("downloadDirectory") + filename);
    boolean do_request = false;

    System.out.println("Download of " + filename + " with HTL " + htl + " started.");

    // Download file
    boolean success = FcpRequest.getFile(key, size, newFile, htl, true);

    // We need to synchronize accesses to the table
    synchronized (downloadTable){
        // Does an exception prevent release of the lock, better catch them
        try{
        // file might be erased from table during download...
        int row = getTableEntry(tableModel);

        // download failed
        if (!success) {
            System.out.println("Download of " + filename + " failed.");
            if (row != -1) {
            frame1.downloadTableModel.setValueAt(LangRes.getString("Failed"), row, 3);

            // Upload request to request stack
            int intHtl = 15;
            try {
                intHtl = Integer.parseInt(htl);
            }
            catch (NumberFormatException e) {}

            if ( intHtl > 10) {
                if (DEBUG) System.out.println("Download failed, uploading request for " + filename);
                // We may not do the request here due to the synchronize
                do_request = true;
            }
            else {
                if (DEBUG) System.out.println("Download failed, but htl is too low to request it.");
            }
            }
        }
        // download successfull
        else {

            // Add successful downloaded key to database
            KeyClass newKey = new KeyClass(key);
            newKey.setFilename(filename);
            newKey.setSize(newFile.length());
            newKey.setDate(date);
            newKey.setExchange(false);
            Index.add(newKey, new File(frame1.keypool + board));

            // Add this file to the upload table (so that it can be requested again)
            File file = new File(System.getProperty("user.dir") +
                     System.getProperty("file.separator") +
                     newFile.getPath());

    //      UploadTableFun.add(uploadTable, file, new File(""), board);

            if (row != -1) {// Entry was not deleted from download table
            tableModel.setValueAt(LangRes.getString("Done"), row, 3);
            frame1.updateDownloads = true;
            }
        }
        }
        catch (Exception e){
            System.out.println("Exception " + e.toString() + " occured in requestThread.run() for file " + filename);
        }
    }
    if (do_request)
        request(key.trim(), board);
    synchronized(frame1.threadCountLock) {
    frame1.activeDownloadThreads--;
    }
    }

    public int getTableEntry(DefaultTableModel tableModel) {
    for (int i = 0; i < tableModel.getRowCount(); i++)
        if (key.equals(tableModel.getValueAt(i,6)))
        return i;
    return -1;
    }

    // Request a certain CHK from a board
    private void request(String key, String board) {

    String messageUploadHtl = frame1.frostSettings.getValue("tofUploadHtl");
    boolean requested = false;

    if (DEBUG) System.out.println("Uploading request of " + key + " from " + board);

    String fileSeparator = System.getProperty("file.separator");
    String destination = frame1.keypool + board + fileSeparator + DateFun.getDate() + fileSeparator;

    File checkDestination = new File(destination);
    if (!checkDestination.isDirectory())
        checkDestination.mkdirs();

    // Check if file was already requested
    File[] files = checkDestination.listFiles();
    for (int i = 0; i < files.length; i++) {
        String content = (FileAccess.readFile(files[i])).trim();
        if (content.equals(key)) {
        requested = true;
        System.out.println("File was already requested");
        }
    }

    if (!requested) {
        String date = DateFun.getDate();
/*
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = cal.get(Calendar.YEAR) + ".";
        int month = cal.get(Calendar.MONTH) + 1;
        date += month + ".";
        int day = cal.get(Calendar.DATE);
        date += day;
*/
        String time = DateFun.getFullExtendedTime() + "GMT";
/*
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
        time +=  "0" + hour + ":";
        } else {
        time +=  hour + ":";
        }
        int minute = cal.get(Calendar.MINUTE);
        if (minute < 10) {
        time +=  "0" + minute + ":";
        } else {
        time +=  minute + ":";
        }
        int second = cal.get(Calendar.SECOND);
        if (second < 10) {
        time +=  "0" + second + "GMT";
        } else {
        time +=  second + "GMT";
        }
*/

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
        while (!success) {
        // Does this index already exist?
        File testMe = new File(new StringBuffer().append(destination).append(date).append("-")
                               .append(board).append("-").append(index).append(".req").toString());
        if (testMe.length() > 0) { // already downloaded
            index++;
            if (DEBUG) System.out.println("File exists, increasing index to " + index);
        }
        else { // probably empty
            String[] result = new String[2];
            String upKey = new StringBuffer().append("KSK@frost/request/")
                                             .append(frame1.frostSettings.getValue("messageBase"))
                                             .append("/").append(date).append("-")
                                             .append(board).append("-").append(index).append(".req").toString();
            if (DEBUG) System.out.println(upKey);
            result = FcpInsert.putFile(upKey, destination + uploadMe, messageUploadHtl, false, true);
            System.out.println("FcpInsert result[0] = " + result[0] + " result[1] = " + result[1]);

            if (result[0] == null || result[1] == null) {
            result[0] = "Error";
            result[1] = "Error";
            }

            if (result[0].equals("Success")) {
            success = true;
            } else {
            if (result[0].equals("KeyCollision")) {

                // Check if the collided key is perhapes the requested one
                String compareMe = String.valueOf(System.currentTimeMillis()) + ".txt";
                String requestMe = new StringBuffer().append("KSK@frost/request/")
                    .append(frame1.frostSettings.getValue("messageBase")).append("/")
                    .append(date).append("-").append(board).append("-").append(index).append(".req").toString();

                if (FcpRequest.getFile(requestMe,
                           "Unknown",
                           frame1.keypool + compareMe,
                           htl,
                           false)) {

                File numberOne = new File(frame1.keypool + compareMe);
                File numberTwo = new File(destination + uploadMe);
                String contentOne = (FileAccess.readFile(numberOne)).trim();
                String contentTwo = (FileAccess.readFile(numberTwo)).trim();

                if (DEBUG) System.out.println(contentOne);
                if (DEBUG) System.out.println(contentTwo);

                if (contentOne.equals(contentTwo)) {
                    if (DEBUG) System.out.println("Key Collision and file was already requested");
                    success = true;
                }
                else {
                    index++;
                    System.out.println("Request Upload collided, increasing index to " + index);
                }
                }
                else {
                System.out.println("Request upload failed (" + tries + "), retrying index " + index);
                if (tries > 5) {
                    success = true;
                    error = true;
                }
                tries++;
                }
            }
            }
        }
        }

        if (!error) {

        File killMe = new File(destination + uploadMe);
        File newMessage = new File(destination + date + "-" + board + "-" + index + ".req");
        killMe.renameTo(newMessage);

        //frame1.updateTof = true;
        TOF.addNewMessageToTable( newMessage, board );

        System.out.println("*********************************************************************");
        System.out.println("Request successfuly uploaded to the '" + board + "' board.");
        System.out.println("*********************************************************************");
        }
        else {
        System.out.println("Error while uploading message.");
        messageFile.delete();
        }

        System.out.println("Request Upload Thread finished");
    }
    }

    /**Constructor*/
    public requestThread(String filename,
             String size,
             JTable downloadTable,
             JTable uploadTable,
             String htl,
             String key,
             String board) {
    this.filename = filename;
    this.size = size;
    this.downloadTable = downloadTable;
    this.uploadTable = uploadTable;
    this.htl = htl;
    this.key = key;
    this.board = board.toLowerCase();
    }
}

