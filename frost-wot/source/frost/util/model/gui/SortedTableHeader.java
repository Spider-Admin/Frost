/*
 * Created on May 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

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
 * @author $Author$
 * @version $Revision$
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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
			if (defaultRenderer instanceof JLabel) {
				JLabel labelRenderer = (JLabel)defaultRenderer;
				// This translation is done so the real column number is used when the user moves columns around.
				int modelIndex = table.getColumnModel().getColumn(column).getModelIndex();
				if (sortedTable.getCurrentColumnNumber() == modelIndex) {
					if (sortedTable.isAscending()) {
						labelRenderer.setIcon(ascendingIcon);	
					} else {
						labelRenderer.setIcon(descendingIcon);			
					}
					labelRenderer.setHorizontalTextPosition(JLabel.LEADING);
				} else {
					labelRenderer.setIcon(null);	
				}						
			}
			return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	
	}
	
	private static Logger logger = Logger.getLogger(SortedTableHeader.class.getName());	
	
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
	 * @see frost.util.model.gui.ModelTableHeader#headerClicked(java.awt.event.MouseEvent)
	 */
	protected void headerClicked(MouseEvent e) {
		super.headerClicked(e);
		if (e.getButton() == MouseEvent.BUTTON1) {
			TableColumnModel columnModel = getTable().getColumnModel();
			int columnNumber = columnModel.getColumnIndexAtX(e.getX());
			if (columnNumber != -1) {
				//This translation is done so the real column number is used when the user moves columns around.
				int modelIndex = columnModel.getColumn(columnNumber).getModelIndex();
				sortedTable.columnClicked(modelIndex);
			}
		}
	}

}
