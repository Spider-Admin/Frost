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
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
class SortedTableHeader extends JTableHeader {
	/**
	 * 
	 */
	private class ArrowRenderer implements TableCellRenderer {
					
		/**
		 * 
		 */
		public ArrowRenderer() {
			super();
		}
	
		/** 
		 * This method assumes that this is the renderer of the header of a column. 
		 * If the defaultRenderer of the JTableHeader is an instance of JLabel 
		 * (like DefaultTableCellRenderer), it paints an arrow if necessary. Then, 
		 * it calls the defaultRenderer so that it finishes the job. 
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
	
	/**
	 * 
	 */
	private class Listener extends MouseAdapter {
	
		/**
		 * 
		 */
		public Listener() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			TableColumnModel columnModel = getTable().getColumnModel();
			int columnNumber = columnModel.getColumnIndexAtX(e.getX());
			if (columnNumber != -1) {
				//This translation is done so the real column number is used when the user moves columns around.
				int modelIndex = columnModel.getColumn(columnNumber).getModelIndex();
				sortedTable.columnClicked(modelIndex);
			}
		}
	
	}
	
	private static Logger logger = Logger.getLogger(SortedTableHeader.class.getName());	
	
	private static Icon ascendingIcon;
	private static Icon descendingIcon;
	
	private ArrowRenderer arrowRenderer = new ArrowRenderer();
	private Listener listener = new Listener();
	
	private SortedModelTable sortedTable;
	
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
	 * @param cm
	 */
	public SortedTableHeader(SortedModelTable newSortedTable) {
		super(newSortedTable.getTable().getColumnModel());
		
		sortedTable = newSortedTable;
		//The defaultRenderer of the JTableHeader is not touched because that is what
		//the skins system will change (if is is enabled).
		Enumeration enumeration = sortedTable.getTable().getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn column = (TableColumn) enumeration.nextElement();
			column.setHeaderRenderer(arrowRenderer);
		}
		addMouseListener(listener);
	}

}
