/*
  SearchTableFun.java
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

public class SearchTableFun {

    /**Adds selected entries in searchTable to downloadTable*/
    public static void downloadSelectedKeys(int htl, JTable searchTable, JTable downloadTable) {
    DefaultTableModel searchTableModel = (DefaultTableModel)searchTable.getModel();
    int[] selectedRows = searchTable.getSelectedRows();

    for (int i = 0; i < selectedRows.length; i++) {
        String filename = (String)searchTableModel.getValueAt(selectedRows[i], 0);
        String size = searchTableModel.getValueAt(selectedRows[i], 1).toString();
        String age = (String)searchTableModel.getValueAt(selectedRows[i], 2);
        String key = (String)searchTableModel.getValueAt(selectedRows[i], 3);
        String board = (String)searchTableModel.getValueAt(selectedRows[i], 4);
        DownloadTableFun.insertDownload(filename, size, age, key, htl, downloadTable, board);
    }
    }

    public static String getSelectedAttachmentsString(JTable searchTable) {
    DefaultTableModel searchTableModel = (DefaultTableModel)searchTable.getModel();
    int[] selectedRows = searchTable.getSelectedRows();
    String attachments = "";
    for (int i = 0; i < selectedRows.length; i++) {
        String key = (String)searchTableModel.getValueAt(selectedRows[i], 3);
        String filename = (String)searchTableModel.getValueAt(selectedRows[i], 0);
        attachments += "<attached>" + filename + " * " + key + "</attached>\n";
    }
    return(attachments);
    }

}
