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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public abstract class AbstractTableFormat<ModelItemType extends ModelItem<ModelItemType>> implements ModelTableFormat<ModelItemType> {

	private static final Logger logger = Logger.getLogger(AbstractTableFormat.class.getName());

	private Map<Integer,String> columnNames;
	private Map<Integer,Boolean> columnEditable;
	
	protected Vector<JTable> tables;

	protected AbstractTableFormat() {
		super();
		columnNames = new HashMap<Integer,String>();
		
		columnEditable = new HashMap<Integer,Boolean>();
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(frost.util.model.gui.ModelTable)
	 */
	public void customizeTable(ModelTable<ModelItemType> modelTable){
		// Nothing here. Override in subclasses if necessary.
	}

    public void customizeTableAfterInitialize(ModelTable<ModelItemType> modelTable) {
        // Nothing here. Override in subclasses if necessary.
    }

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columnNames.size();
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#addTable(javax.swing.JTable)
	 */
	public synchronized void addTable(JTable table) {
		if (tables == null) { 
			tables = new Vector<JTable>();
		}
		tables.add(table);
	}
	
	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return columnNames.get(column);
	}
	
	/**
	 * @param index
	 * @param name
	 */
	protected void setColumnName(int index, String name) {
		columnNames.put(index, name); 
	}
	
	protected synchronized void refreshColumnNames() {
		if (tables != null) {
			Iterator<JTable> iterator = tables.iterator();
			while (iterator.hasNext()) {
				JTable table = iterator.next();
				TableColumnModel columnModel = table.getColumnModel();
				for (int i = 0; i < table.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					column.setHeaderValue(columnNames.get(column.getModelIndex()));
				};
			}
		}	
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#isColumnEditable(int)
	 */
	public boolean isColumnEditable(int column) {
		if( columnEditable.containsKey(column)) {
			return columnEditable.get(column);
		}
		return false;
	}
	
	/** 
	 * This methods sets if the column whose index is passed as a parameter
	 * is editable or not.
	 * @param column index of the column
	 * @param editable true if the column is editable. False if it is not.
	 **/
	public void setColumnEditable(int column, boolean editable) {
		columnEditable.put(column, editable);
	}

	/***
	 * By default all columns are not editable. Override in subclasses when needed.
	 * @see frost.util.model.gui.ModelTableFormat#setCellValue(java.lang.Object, frost.util.model.ModelItem, int)
	 */
	public void setCellValue(Object value, ModelItemType item, int columnIndex) {
		logger.warning("The column number " + columnIndex + "is not editable.");
		throw new RuntimeException("Method setCellValue not implemented, needs to be deined by subclass if it has editable columns");
	}

}
