/*
 AbstractTableFormat.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.model;

import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.*;


public abstract class AbstractTableFormat implements ModelTableFormat {

	private static final Logger logger = Logger.getLogger(AbstractTableFormat.class.getName());

	private int columnCount;
	private String columnNames[];
	private boolean columnEditable[];
	
	protected Vector tables;

	protected AbstractTableFormat(int newColumnCount) {
		super();
		columnCount = newColumnCount;
		columnNames = new String[columnCount];
		
		columnEditable = new boolean[columnCount];
		for (int i = 0; i < columnEditable.length; i++) {
			columnEditable[i] = false;			
		}
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(frost.util.model.gui.ModelTable)
	 */
	public void customizeTable(ModelTable modelTable) {
		// Nothing here. Override in subclasses if necessary.
	}

    public void customizeTableAfterInitialize(ModelTable modelTable) {
        // Nothing here. Override in subclasses if necessary.
    }

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#addTable(javax.swing.JTable)
	 */
	public synchronized void addTable(JTable table) {
		if (tables == null) { 
			tables = new Vector();
		}
		tables.add(table);
	}
	
	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	/**
	 * @param index
	 * @param name
	 */
	protected void setColumnName(int index, String name) {
		columnNames[index] = name; 
	}
	
	protected synchronized void refreshColumnNames() {
		if (tables != null) {
			Iterator iterator = tables.iterator();
			while (iterator.hasNext()) {
				JTable table = (JTable) iterator.next();
				TableColumnModel columnModel = table.getColumnModel();
				for (int i = 0; i < table.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					column.setHeaderValue(columnNames[column.getModelIndex()]);					
				};
			}			
		}	
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#isColumnEditable(int)
	 */
	public boolean isColumnEditable(int column) {
		return columnEditable[column];
	}
	
	/** 
	 * This methods sets if the column whose index is passed as a parameter
	 * is editable or not.
	 * @param column index of the column
	 * @param editable true if the column is editable. False if it is not.
	 **/
	public void setColumnEditable(int column, boolean editable) {
		columnEditable[column] = editable;
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#setCellValue(java.lang.Object, frost.util.model.ModelItem, int)
	 */
	public void setCellValue(Object value, ModelItem item, int columnIndex) {
		//By default all columns are not editable. Override in subclasses when needed.
		logger.warning("The column number " + columnIndex + "is not editable.");
	}

}
