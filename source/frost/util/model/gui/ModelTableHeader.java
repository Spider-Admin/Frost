/*
 * Created on May 31, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.JTableHeader;

/**
 * This is subclass of JTableHeader that listens for mouse clicks on it.
 * It does nothing with them, but provides a couple of methods (headerClicked 
 * and headerReleased) that subclasses can override as necessary.
 * @author $Author$
 * @version $Revision$
 */
public class ModelTableHeader extends JTableHeader {
	/**
	 * This inner class listens for mouse clicks on the header
	 */
	private class Listener extends MouseAdapter {
	
		/**
		 *	This constructor creates a new instance of Listener
		 */
		public Listener() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			headerClicked(e);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			headerReleased(e);
		}

	}
	
	private Listener listener = new Listener();

	/**
	 * This constructor creates a new instance of ModelTableHeader associated
	 * to the ModelTable that is passed as a parameter.
	 * @param cm the ModelTable that is going to have this header
	 */
	public ModelTableHeader(ModelTable newModelTable) {
		super(newModelTable.getTable().getColumnModel());

		addMouseListener(listener);
	}

	/**
	 * This method is called whenever the user clicks on the header
	 * (when the mouse button is pressed)
	 * @param e the MouseEvent
	 */
	protected void headerClicked(MouseEvent e) {
		//Nothing here for now. Override as necessary.
	}
	
	/**
	 * This method is called whenever the user clicks on the header
	 * (when the mouse button is released)
	 * @param e the MouseEvent
	 */
	protected void headerReleased(MouseEvent e) {
		//Nothing here for now. Override as necessary.
	}

}
