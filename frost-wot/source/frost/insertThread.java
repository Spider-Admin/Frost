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

package frost;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class insertThread extends Thread {
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    private String destination;
    private String date;
    private File file;
    private String htl;
    private String board;
    private boolean mode;

    public void run() {

    System.out.println("Upload of " + file + " with HTL " + htl + " started.");

    if (mode)
        synchronized(frame1.threadCountLock) {
            frame1.activeUploadThreads++;
        }
    else
        frame1.generateCHK = true;

    String status = LangRes.getString("Never");
    boolean success = false;
    String[] result = {"Error", "Error"};

    if (file.length() > 0 && file.isFile()) {

        result = FcpInsert.putFile("CHK@", file, htl, true, mode);

        if (result[0].equals("Success")) {
        success = true;
        System.out.println("Upload of " + file + " successfull.");
        }
        if (result[0].equals("KeyCollision")) {
        success = true;
        System.out.println("Upload of " + file + " collided.");
        }

        if (success) {
        status = date;
        KeyClass newKey = new KeyClass(result[1]);
        newKey.setFilename(destination);
        newKey.setSize(file.length());
        newKey.setDate(date);
        Index.add(newKey, new File(frame1.keypool + board));
        }

        synchronized (frame1.uploadTable){
            try {
            int row = getTableEntry();
            if (row != -1) {
            if (mode)
                frame1.uploadTableModel.setValueAt(status, row, 2);
            else
                frame1.uploadTableModel.setValueAt(result[1], row, 5);
            }
            }
        catch (Exception e) {System.out.println("insertThread NOT GOOD "+e.toString());}
        }

    }

    if (mode)
        synchronized(frame1.threadCountLock) {
            frame1.activeUploadThreads--;
        }
    else
        frame1.generateCHK = false;
    }

    public int getTableEntry() {
    for (int i = 0; i < frame1.uploadTableModel.getRowCount(); i++)
        if ((file.getPath()).equals(frame1.uploadTableModel.getValueAt(i, 3)))
        return i;
    return -1;
    }

    /**Constructor*/
    public insertThread(String destination, File file, String htl, String board, boolean mode) {
    this.destination = destination;
    this.file = file;
    this.htl = htl;
    this.board = board.toLowerCase();
    this.date = DateFun.getExtendedDate();
    this.mode = mode; // true=upload file false=generate chk (do not upload)
    }
}
