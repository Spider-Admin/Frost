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
	public int getColumnCount();

	/**
	 * @param column
	 * @return
	 */
	public String getColumnName(int column);

	/**
	 * @param item
	 * @param columnIndex
	 * @return
	 */
	public Object getCellValue(ModelItem item, int columnIndex);
	
	/**
	 * @param value
	 * @param item
	 * @param columnIndex
	 */
	public void setCellValue(Object value, ModelItem item, int columnIndex);
	
	/**
	 * @param modelTable
	 */
	public void customizeTable(ModelTable modelTable);
	
	/**
	 * This method returns the numbers of the columns that reflect
	 * the information of the model field passed as a parameter
	 * 
	 * @param fieldID the ID of the field
	 * @return the number of the columns of the table that reflect
	 * 		   the information of the given model field
	 */
	public int[] getColumnNumbers(int fieldID);
	
	/**
	 * @param column
	 * @return
	 */
	public boolean isColumnEditable(int column);

	/**
	 * This method adds a new table to the list of tables affected
	 * by this format (so that it can notify them when, for instance, 
	 * the language of the application changes).
	 * @param table a new table affected by this format
	 */
	public void addTable(JTable table);
}
