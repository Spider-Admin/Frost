/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.util.*;
import java.util.logging.Logger;

import swingwtx.swing.JTable;
import swingwtx.swing.table.*;

import frost.util.model.ModelItem;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractTableFormat implements ModelTableFormat {

	private static Logger logger = Logger.getLogger(AbstractTableFormat.class.getName());

	private int columnCount;
	private String columnNames[];
	private boolean columnEditable[];
	
	protected Vector tables;

	/**
	 * 
	 */
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

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#addTable(swingwtx.swing.JTable)
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
	
	/**
	 * 
	 */
	protected synchronized void refreshColumnNames() {
		if (tables != null) {
			Iterator iterator = tables.iterator();
			while (iterator.hasNext()) {
				JTable table = (JTable) iterator.next();
				TableColumnModel columnModel = table.getColumnModel();
				for (int i = 0; i < table.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					column.setHeaderValue(columnNames[i]);					
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
