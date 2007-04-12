/*
 SortedTableHeader.java / Frost
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

import java.awt.Component;
import java.awt.event.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;

/**
 * This is subclass of ModelTableHeader that listens for mouse clicks on it.
 * When the user clicks on the header of one column, it notifies the associated
 * SortedModelTable of the event, so that it can change its sorting.
 * It also paints arrows on the header depending of that sorting.
 */
class SortedTableHeader extends ModelTableHeader {
	/**
	 * This inner class paints an arrow on the header of the column the model
	 * table is sorted by. The arrow will point upwards or downwards depending 
	 * if the sorting is ascending or descending.
	 */
	private class ArrowRenderer implements TableCellRenderer {
					
		/**
		 * This constructor creates a new instance of ArrowRenderer
		 */
		public ArrowRenderer() {
			super();
		}
	
		/** 
		 * This method assumes that this is the renderer of the header of a column. 
		 * If the defaultRenderer of the JTableHeader is an instance of JLabel 
		 * (like DefaultTableCellRenderer), it paints an arrow if necessary. Then, 
		 * it calls the defaultRenderer so that it finishes the job. 
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 	 */
		public Component getTableCellRendererComponent(JTable lTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			TableCellRenderer defaultRenderer = lTable.getTableHeader().getDefaultRenderer();
			if (defaultRenderer instanceof JLabel) {
				JLabel labelRenderer = (JLabel)defaultRenderer;
				// This translation is done so the real column number is used when the user moves columns around.
				int modelIndex = lTable.getColumnModel().getColumn(column).getModelIndex();
				if (sortedTable.getSortedColumn() == modelIndex) {
					if (sortedTable.isSortedAscending()) {
						labelRenderer.setIcon(ascendingIcon);	
					} else {
						labelRenderer.setIcon(descendingIcon);			
					}
					labelRenderer.setHorizontalTextPosition(JLabel.LEADING);
				} else {
					labelRenderer.setIcon(null);	
				}
                labelRenderer.setToolTipText(value.toString());
			}
			return defaultRenderer.getTableCellRendererComponent(lTable, value, isSelected, hasFocus, row, column);
		}
	
	}
	
	private static final Logger logger = Logger.getLogger(SortedTableHeader.class.getName());	
	
	private static Icon ascendingIcon;
	private static Icon descendingIcon;
	
	private ArrowRenderer arrowRenderer = new ArrowRenderer();
	
	private SortedModelTable sortedTable;
	
	/**
	 * This static initializer loads the images of the arrows (both ascending and descending)
	 */
	static {
		URL ascencingURL = SortedModelTable.class.getResource("/data/SortedTable_ascending.png");
		if (ascencingURL != null) {
			ascendingIcon = new ImageIcon(ascencingURL);
		} else {
			logger.severe("Could not load /data/SortedTable_ascending.png icon.");
		}
		URL descendingURL = SortedModelTable.class.getResource("/data/SortedTable_descending.png");
		if (descendingURL != null) {
			descendingIcon = new ImageIcon(descendingURL);
		} else {
			logger.severe("Could not load /data/SortedTable_descending.png icon.");
		}
	}

	/**
	 * This constructor creates a new instance of ModelTableHeader associated
	 * to the SortedModelTable that is passed as a parameter.
	 * @param cm the SortedModelTable that is going to have this header
	 */
	public SortedTableHeader(SortedModelTable newSortedTable) {
		super(newSortedTable);
		
		sortedTable = newSortedTable;
		//The defaultRenderer of the JTableHeader is not touched because that is what
		//the skins system will change (if is is enabled).
		Enumeration enumeration = sortedTable.getTable().getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn column = (TableColumn) enumeration.nextElement();
			column.setHeaderRenderer(arrowRenderer);
		}
	}

	/**
	 * This method is called by the superclass when the user clicks on a column (the mouse
	 * button is pressed). It gets the number of the column whose header was clicked and
	 * notifies the associated SortedModelTable of the event.
	 * @see frost.util.model.ModelTableHeader#headerClicked(java.awt.event.MouseEvent)
	 */
	protected void headerClicked(MouseEvent e) {
		super.headerClicked(e);
		if (e.getButton() == MouseEvent.BUTTON1) {
			TableColumnModel lColumnModel = getTable().getColumnModel();
			int columnNumber = lColumnModel.getColumnIndexAtX(e.getX());
			if (columnNumber != -1) {
				//This translation is done so the real column number is used when the user moves columns around.
				int modelIndex = lColumnModel.getColumn(columnNumber).getModelIndex();
				sortedTable.columnClicked(modelIndex);
			}
		}
	}

}
