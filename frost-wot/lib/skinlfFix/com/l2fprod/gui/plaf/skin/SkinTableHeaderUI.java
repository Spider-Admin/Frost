/* ====================================================================
 *
 * Skin Look And Feel 1.2.5 License.
 *
 * Copyright (c) 2000-2003 L2FProd.com.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by L2FProd.com
 *        (http://www.L2FProd.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Skin Look And Feel", "SkinLF" and "L2FProd.com" must not
 *    be used to endorse or promote products derived from this software
 *    without prior written permission. For written permission, please
 *    contact info@L2FProd.com.
 *
 * 5. Products derived from this software may not be called "SkinLF"
 *    nor may "SkinLF" appear in their names without prior written
 *    permission of L2FProd.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL L2FPROD.COM OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.l2fprod.gui.plaf.skin;

import java.beans.*;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.*;

/**
 * @author    $Author$
 * @created   27 avril 2002
 * @version   $Revision$, $Date$
 */
public class SkinTableHeaderUI extends BasicTableHeaderUI {

	/** 
	 * 
	 */
	private class Listener implements TableColumnModelListener, PropertyChangeListener {
		/* (non-Javadoc)
		 * @see javax.swing.event.TableColumnModelListener#columnAdded(javax.swing.event.TableColumnModelEvent)
		 */
		public void columnAdded(TableColumnModelEvent e) {
			TableColumn aColumn = columnModel.getColumn(e.getToIndex());
			installColumnRenderer(aColumn);
			columns.add(aColumn);
		}
		/* (non-Javadoc)
		 * @see javax.swing.event.TableColumnModelListener#columnMarginChanged(javax.swing.event.ChangeEvent)
		 */
		public void columnMarginChanged(ChangeEvent e) {
			// Nothing here		
		}
		/* (non-Javadoc)
		 * @see javax.swing.event.TableColumnModelListener#columnMoved(javax.swing.event.TableColumnModelEvent)
		 */
		public void columnMoved(TableColumnModelEvent e) {
			int fromIndex = e.getFromIndex();
			int toIndex = e.getToIndex();
			if (fromIndex != toIndex) {
				TableColumn aColumn = (TableColumn) columns.remove(fromIndex);
				columns.insertElementAt(aColumn, toIndex);
			}
		}
		/* (non-Javadoc)
		 * @see javax.swing.event.TableColumnModelListener#columnRemoved(javax.swing.event.TableColumnModelEvent)
		 */
		public void columnRemoved(TableColumnModelEvent e) {
			int columnIndex = e.getFromIndex();
			TableColumn aColumn = (TableColumn) columns.remove(columnIndex);
			uninstallColumnRenderer(aColumn);
		}
		/* (non-Javadoc)
		 * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void columnSelectionChanged(ListSelectionEvent e) {
			// Nothing here		
		}

		/**
		 * 
		 */
		public Listener() {
			super();
		}
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			uninstallColumnRenderers();
			columnModel = header.getColumnModel();
			installColumnRenderers();
		}

	}

	Listener listener = new Listener();

	Hashtable previousRenderers = new Hashtable();

	TableCellRenderer renderer;
	Skin skin = SkinLookAndFeel.getSkin();

	TableColumnModel columnModel = null;
	Vector columns = new Vector();

	/**
	 * Constructor for the SkinTableHeaderUI object
	 */
	public SkinTableHeaderUI() {
		super();
		renderer = skin.getPersonality().getTableHeaderRenderer();
	}

	/**
	 * Description of the Method
	 *
	 * @param c  Description of Parameter
	 */
	public void installUI(JComponent c) {
		super.installUI(c);

		//First we install the default renderer of the header
		Object cellRenderer = header.getDefaultRenderer();
		previousRenderers.put(c, cellRenderer);
		header.setDefaultRenderer(renderer);

		//And then we install the default renderers of each column
		installColumnRenderers();
	}

	/**
	 * 
	 */
	private void installColumnRenderers() {
		Enumeration enumeration = columnModel.getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn aColumn = (TableColumn) enumeration.nextElement();
			installColumnRenderer(aColumn);
			columns.add(aColumn);
		}
	}

	/**
	 * 
	 */
	private void installColumnRenderer(TableColumn aColumn) {
		TableCellRenderer cellRenderer = aColumn.getHeaderRenderer();
		if (cellRenderer != null) {
			previousRenderers.put(aColumn, cellRenderer);
		} else {
			previousRenderers.put(aColumn, "null");
		}
		aColumn.setHeaderRenderer(renderer);
	}

	/**
	   * Description of the Method
	   *
	   * @param h  Description of Parameter
	   * @return   Description of the Returned Value
	   */
	public static ComponentUI createUI(JComponent h) {
		return new SkinTableHeaderUI();
	}

	/* (non-Javadoc)
		   * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
		   */
	public void uninstallUI(JComponent c) {

		// First we uninstall the default renderer of the header
		if (previousRenderers.containsKey(c)) {
			TableCellRenderer cellRenderer = (TableCellRenderer) previousRenderers.remove(c);
			header.setDefaultRenderer(cellRenderer);
		}

		// Then we uninstall the default renderers of each column
		uninstallColumnRenderers();

		super.uninstallUI(c);
	}

	/**
	 * 
	 */
	private void uninstallColumnRenderers() {
		Enumeration enumeration = columnModel.getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn aColumn = (TableColumn) enumeration.nextElement();
			uninstallColumnRenderer(aColumn);
			columns.remove(aColumn);
		}
	}

	/**
	 * 
	 */
	private void uninstallColumnRenderer(TableColumn aColumn) {
		if (previousRenderers.containsKey(aColumn)) {
			Object cellRenderer = previousRenderers.remove(aColumn);
			if (cellRenderer instanceof TableCellRenderer) {
				aColumn.setHeaderRenderer((TableCellRenderer) cellRenderer);
			} else {
				aColumn.setHeaderRenderer(null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTableHeaderUI#installListeners()
	 */
	protected void installListeners() {
		super.installListeners();

		columnModel = header.getColumnModel();
		header.addPropertyChangeListener("columnModel", listener);
		columnModel.addColumnModelListener(listener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicTableHeaderUI#uninstallListeners()
	 */
	protected void uninstallListeners() {
		super.uninstallListeners();

		header.removePropertyChangeListener("columnModel", listener);
		columnModel.removeColumnModelListener(listener);
		columnModel = null;
	}

}

