/*
  DownloadTableFun.java / Frost
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
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.SimpleDateFormat;

public class DownloadTableFun {
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
    final static boolean DEBUG = true;

    /**
     * Updates the download table
     * @param table the downloadTable
     * @param maxDownloadHtl Request htl's will not exceed this value
     * @param keypoolDirectory This directory should contain the temporary chunks
     * @param downloadDirectory This directory should contain the downloaded file
     */
    public static void update(JTable table,
                  int maxDownloadHtl,
                  File keypoolDirectory,
                  File downloadDirectory) {

    //if (DEBUG) System.out.println("DownloadTableFun.update");

    // Need to synchronize with other places where the table is changed
    // PO required
    synchronized (table){
        // Does an exception prevent release of the lock, better catch them
        try{
        DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        String[] columnData = new String[columnCount];
        File[] chunkList = keypoolDirectory.listFiles();
        String fileSeparator = System.getProperty("file.separator");

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++)
            columnData[j] = (String)tableModel.getValueAt(i,j);

            // Download / bytes read
            if (columnData[3].equals(LangRes.getString("Trying")) || (columnData[3].indexOf(" Kb") != -1)) {
            File newFile = new File(downloadDirectory + fileSeparator + mixed.makeFilename(columnData[0]) + ".tmp");
            if (newFile.exists()) {
                long downloaded = newFile.length();
                if (chunkList != null) {
                for (int j = 0; j < chunkList.length; j++) {
                    if (chunkList[j].getName().startsWith(mixed.makeFilename(columnData[0]) + ".tmp-chunk-"))
                    downloaded += chunkList[j].length();
                    if (chunkList[j].getName().startsWith(mixed.makeFilename(columnData[0]) + ".tmp-check-"))
                    downloaded += chunkList[j].length();
                }
                }
                tableModel.setValueAt(downloaded/1024 + " Kb", i, 3);
            }
            }

            // Download / restart failed downloads
            int columnDataHtl = 0;
            try {
            columnDataHtl = Integer.parseInt(columnData[4]);
            }
            catch (NumberFormatException e) {
            if (DEBUG) System.out.println("Bad HTL in DL table");
            }
            if (columnData[3].equals(LangRes.getString("Failed")) &&
            columnDataHtl < maxDownloadHtl) {
            columnDataHtl += 1;
            columnData[4] = String.valueOf(columnDataHtl); // increase htl
            columnData[3] = LangRes.getString("Waiting");
            tableModel.removeRow(i);
            tableModel.addRow(columnData);
            i--;
            }
        }
        }
        catch (Exception e){System.out.println("updating - NOT GOOD"+e.toString());}
    }
    }

    /**
     * Inserts file into Downloadlist
     */
    public static void insertDownload(String filename,
                      String size,
                      String age,
                      String key,
                      int htl,
                      JTable downloadTable,
                      String board) {
    String state = LangRes.getString("Waiting");

    // Remove the colors, if there are any
    if (filename.startsWith("<html>")) {
        if (filename.length() > 38) {
        filename = filename.substring(24, filename.length() - 14);
        }
    }

    Object[] row = {mixed.makeFilename(filename), size, age, state, String.valueOf(htl), board, key};
    // Need to synchronize with other places where the table is changed
    synchronized (downloadTable){
        // Does an exception prevent release of the lock, better catch them
        try{
        if (!TableFun.exists(downloadTable, (String)row[0], 0)) {
            DefaultTableModel downloadTableModel = (DefaultTableModel)downloadTable.getModel();
            downloadTableModel.addRow(row);
        }
        }
        catch (Exception e){System.out.println("inserting download - NOT GOOD "+ e.toString());}
    }
    }

    /**
     * Removes finished downloads from the download table
     */
    public static void removeFinishedDownloads(JTable downloadTable) {
    // Need to synchronize with other places where the table is changed
    synchronized (downloadTable){
        // Does an exception prevent release of the lock, better catch them
        try{
        DefaultTableModel tableModel = (DefaultTableModel)downloadTable.getModel();
        for (int i = tableModel.getRowCount()  - 1; i >= 0; i--) {
            String state = (String)tableModel.getValueAt(i, 3);
            if (state.equals(LangRes.getString("Done")))
            tableModel.removeRow(i);
        }
        }
        catch (Exception e){System.out.println("finished download - NOT GOOD " + e.toString());}
    }
    }

    /**
     * Removes chunks from keypool
     */
    public static void removeSelectedChunks(JTable table) {
    DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
    String[] selectedFiles = null;
    System.out.println("Removing chunks");
    // Need to synchronize with other places where the table is changed
    synchronized (table){
        // Does an exception prevent release of the lock, better catch them
        try{
        int[] selectedRows = table.getSelectedRows();
        selectedFiles = new String[selectedRows.length];

        for (int i = 0; i < selectedRows.length; i++) {
            selectedFiles[i] = (String)tableModel.getValueAt(selectedRows[i], 0);
        }
        }
        catch (Exception e){System.out.println("chunk removal - NOT GOOD " + e.toString());}
    }
    // Moved actual deletion out of loop above to reduce time
    // spent in synchronized code
    File[] files = (new File(frame1.keypool)).listFiles();
    for (int i = 0; i < selectedFiles.length; i++){
        System.out.println("Searchin chunks for " + selectedFiles[i]);
        for (int j = 0; j < files.length; j++) {
        if ((files[j].getName()).startsWith(selectedFiles[i]) &&
            files[j].isFile() &&
            !(files[j].getName()).endsWith(".idx")) {
            System.out.println("Removing " + files[j].getName());
            files[j].delete();
        }
        }
    }
    }

    /**
     * Load Downloadlist from default file
     */
    public static void load(JTable table) {
    DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
    Vector tmp = FileAccess.readLines("download.txt");
    String[] row = new String[7];
    // Need to synchronize with other places where the table is changed
    synchronized (table){
        // Does an exception prevent release of the lock, better catch them
        try{
        for (int i = 0; i < tmp.size(); i += 7) {
            if (i + 6 < tmp.size()) {
            for (int j = 0; j < row.length; j++){
                row[j] = (String)tmp.elementAt(i + j);
//              System.out.println("download.txt line " + (i+j) + " is " + row[j]);
            }

            if (! (row[3].equals(LangRes.getString("Done"))))
                row[3] = LangRes.getString("Waiting");
            tableModel.addRow(row);
            }
            else {
                System.out.println("Invalid number of lines in download.txt, clearing download table");
            TableFun.removeAllRows(table);
            }
        }
        }
        catch (Exception e){System.out.println("loading NOT GOOD " + e.toString());}
    }
    }
}
