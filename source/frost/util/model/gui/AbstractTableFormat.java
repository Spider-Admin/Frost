/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.util.*;

import javax.swing.JTable;
import javax.swing.table.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractTableFormat implements ModelTableFormat {

	private int columnCount;
	private String columnNames[];
	
	protected Vector tables;

	/**
	 * 
	 */
	protected AbstractTableFormat(int newColumnCount) {
		super();
		columnCount = newColumnCount;
		columnNames = new String[columnCount];
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(javax.swing.JTable)
	 */
	public void customizeTable(JTable table) {
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

}
