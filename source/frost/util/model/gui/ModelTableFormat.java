/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import javax.swing.JTable;

import frost.util.model.ModelItem;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ModelTableFormat {
	
	/**
	 * @return
	 */
	public int getFieldCount();

	/**
	 * @param column
	 * @return
	 */
	public String getFieldName(int column);

	/**
	 * @param item
	 * @param columnIndex
	 * @return
	 */
	public Object getFieldValue(ModelItem item, int columnIndex);
	
	/**
	 * @param table
	 */
	public void customizeTable(JTable table);
	
	/**
	 * @param table
	 */
	public int getColumnNumber(int fieldID);
}
