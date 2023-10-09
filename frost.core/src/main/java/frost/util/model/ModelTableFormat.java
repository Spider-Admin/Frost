/*
 ModelTableFormat.java / Frost
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

import javax.swing.JTable;


public interface ModelTableFormat<ModelItemType extends ModelItem<ModelItemType>> {
	
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
	public Object getCellValue(ModelItemType item, int columnIndex);
	
	/**
	 * @param value
	 * @param item
	 * @param columnIndex
	 */
	public void setCellValue(Object value, ModelItemType item, int columnIndex);
	
	/**
	 * @param modelTable
	 */
	public void customizeTable(ModelTable<ModelItemType> modelTable);

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
