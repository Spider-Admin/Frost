/*
 * Created on May 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.gui;

import swingwt.awt.Component;

import swingwtx.swing.*;
import swingwtx.swing.table.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BooleanCell {

	/**
	 * This inner class implements the default editor of a boolean table cell
	 */
	private static class Editor extends DefaultCellEditor implements TableCellEditor {

		/**
		 * 
		 */
		public Editor() {
			super(new JCheckBox());
			JCheckBox checkBox = (JCheckBox) getComponent();
			checkBox.setHorizontalAlignment(JCheckBox.CENTER);
		}
	}
	
	/**
	 * This inner class implements the default renderer of a boolean table cell
	 */
	private static class Renderer extends JCheckBox implements TableCellRenderer {

		/**
		 * 
		 */
		public Renderer() {
			super();
			setHorizontalAlignment(JLabel.CENTER);	
		}

		/* (non-Javadoc)
		 * @see swingwtx.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {

			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));
			return this;
		}

	}
	
	public static Renderer RENDERER = new Renderer();
	public static Editor EDITOR = new Editor();
	
	/**
	 * 
	 */
	public BooleanCell() {
		super();
	}

}
