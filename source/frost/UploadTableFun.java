/*
UploadTableFun.java
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

public class UploadTableFun {
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    public static void restoreDefaultFilenames(JTable table) {
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	Date today = new Date();
	String date = formatter.format(today);
	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();

	// We need to synchronize accesses to the table
	synchronized (table){
	    // Does an exception prevent release of the lock, better catch them
	    try{
		int[] selectedRows = table.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
		    File check = new File((String)tableModel.getValueAt(selectedRows[i], 3));
		    if (check.isFile()) {
			String newName = (check.getName());
			tableModel.setValueAt(newName, selectedRows[i], 0);
		    }
		}
	    }
	    catch (Exception e){}
	}
    }

    public static void setPrefixForSelectedFiles(JTable table) {
	String prefix = JOptionPane.showInputDialog(LangRes.getString("Please enter the prefix you want to use for your files."));

	if (prefix != null) {
	    DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
	    // We need to synchronize accesses to the table
	    synchronized (table){
		// Does an exception prevent release of the lock, better catch them
		try{
		    int[] selectedRows = table.getSelectedRows();
		    for (int i = 0; i < selectedRows.length; i++) {
			String newName = prefix + tableModel.getValueAt(selectedRows[i], 0);
			tableModel.setValueAt(mixed.makeFilename(newName), selectedRows[i], 0);
		    }
		}
		catch (Exception e){}
	    }
	}
    }

    /**Adds a file to the upload table*/
    public static void add(JTable table, File file, File parentDirectory, String board) {
	String path = file.getPath();
	String filename = file.getName();

	if (parentDirectory.isDirectory()) {
	    String parent = parentDirectory.getParent();
	    File grandfather = new File(parent);
	    int offset = 1;
	    if ((grandfather.getParentFile()) == null)
		offset = 0;
	 //   filename = path.substring(parent.length() + offset, path.length());
	}

	// We need to synchronize accesses to the table
	synchronized (table){
	    // Does an exception prevent release of the lock, better catch them
	    try{
	    	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
		if (!TableFun.exists(table, path, 3)) {

		    String[] row = {mixed.makeFilename(filename),
				    String.valueOf(file.length()),
				    LangRes.getString("Never"),
				    path,
				    board,
				    LangRes.getString("Unknown")};
		    tableModel.addRow(row);
		}
	    }
	    catch (Exception e){System.out.println("uploadtablefun.add NOT GOOD "+e.toString());}
	}
    }

    public static void addFilesToBoard(JTable uploadTable) {
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
	Date today = new Date();
	String date = formatter.format(today);
	// We need to synchronize accesses to the table
	synchronized (uploadTable){

	    // Does an exception prevent release of the lock, better catch them
	    try{
	    	DefaultTableModel tableModel = (DefaultTableModel)uploadTable.getModel();
		for (int i = 0; i < tableModel.getRowCount(); i++) {

		    if (((String)tableModel.getValueAt(i, 5)).startsWith("CHK@")) {

			KeyClass newKey = new KeyClass((String)tableModel.getValueAt(i, 5));
			newKey.setFilename((String)tableModel.getValueAt(i, 0));
			newKey.setSize((String)tableModel.getValueAt(i, 1));
			newKey.setDate(date);
			newKey.setExchange(false);
			Index.add(newKey, new File(frame1.keypool + (String)tableModel.getValueAt(i, 4)));

		    }

		}
	    }
	    catch (Exception e){System.out.println("uploadtablefun.addfilestoboard NOT GOOD "+e.toString());}
	}
    }

    /**
     * Check if all files are still where they used to be
     */
    public static void update(JTable uploadTable) {
	// We need to synchronize accesses to the table
	synchronized (uploadTable){
	    // Does an exception prevent release of the lock, better catch them
	    try{
	    	DefaultTableModel tableModel = (DefaultTableModel)uploadTable.getModel();

		for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {

		    File checkMePleaseOhYeahBabyDoItNow = new File((String)tableModel.getValueAt(i, 3));
		    if (!checkMePleaseOhYeahBabyDoItNow.exists())
			tableModel.removeRow(i);

		    // Sometimes the node returns errors or other messages.
		    // They have to be replaced with 'Unknown' in order
		    // to generate the chk keys.
	// 	    if (! ((String)tableModel.getValueAt(i, 5)).startsWith("CHK@") &&
	// 		! ((String)tableModel.getValueAt(i, 5)).equals(LangRes.getString("Unknown")))
	// 		tableModel.setValueAt(LangRes.getString("Unknown"), i, 5);
		}
	    }
	    catch (Exception e){System.out.println("uploadtablefun.update NOT GOOD "+e.toString());}
	}
    }

    /**Loads the upload table from disk*/
    public static void load(JTable table) {
	Vector tmp = FileAccess.readLines("upload.txt");
	String[] row = new String[6];
	boolean uploadTableError = false;

	// We need to synchronize accesses to the table
	// Is not optimized, but should occur only at startup
	synchronized (table){

	    // Does an exception prevent release of the lock, better catch them
	    try{
	    	DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
		for (int i = 0; i < tmp.size(); i += 6) {
		    if (i + 5 < tmp.size()) {
			row[0] = (String)tmp.elementAt(i);
			row[1] = (String)tmp.elementAt(i + 1);
			row[2] = (String)tmp.elementAt(i + 2);
			row[3] = (String)tmp.elementAt(i + 3);
			row[4] = (String)tmp.elementAt(i + 4);
			row[5] = (String)tmp.elementAt(i + 5);
//			for (int j = 0; j < 6; j++)
//			    System.out.println(row[j]);
/*
			String date = row[2];
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeZone(TimeZone.getTimeZone("GMT"));
			int firstPoint = date.indexOf(".");
			int secondPoint = date.lastIndexOf(".");
			System.out.println("Dots in date at " + firstPoint + " and " + secondPoint);

			if (firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint) {
			    try {
				int year = Integer.parseInt(date.substring(0, firstPoint));
				int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
				int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month - 1);
				cal.set(Calendar.DATE, day + frame1.frostSettings.getIntValue("uploadInterval"));
			    }
			    catch(NumberFormatException e){
				System.out.println("NumberFormatException in UploadTableFun.load(JTable table)");
				uploadTableError = true;
			    }

			    GregorianCalendar today = new GregorianCalendar();
			    today.setTimeZone(TimeZone.getTimeZone("GMT"));

			    if (today.after(cal)) {
				System.out.println("File " + row[0] + " will be uploaded again");
				row[2] = LangRes.getString("Never");
			    }
			}
*/
			if (row[2].indexOf("Kb") != -1 || row[2].equals(LangRes.getString("Uploading")))
			    row[2] = LangRes.getString("Requested");

			if (!row[5].startsWith("CHK@"))
			    row[5] = LangRes.getString("Unknown");

			File uploadFile = new File(row[3]);
			if (uploadFile.isFile() && uploadFile.length() > 0)
			    tableModel.addRow(row);
		    }
		    else {
			TableFun.removeAllRows(table);
		    }
		}

		// Probably an old uploadTable file
		if (uploadTableError) {
		    System.out.println("Invalid number of lines in upload.txt, clearing upload table");
		    TableFun.removeAllRows(table);
		}
	    }
	    catch (Exception e){System.out.println("uploadtablefun.load NOT GOOD "+e.toString());}
	}
    }
}
