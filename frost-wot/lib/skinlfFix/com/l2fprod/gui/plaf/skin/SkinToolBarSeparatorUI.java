/*
 * Created on Nov 7, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.l2fprod.gui.plaf.skin;

import swingwt.awt.Dimension;

import swingwtx.swing.*;
import swingwtx.swing.plaf.ComponentUI;
import swingwtx.swing.plaf.basic.BasicToolBarSeparatorUI;


/**
 * @author Administrator
 *
 * This class was added so Toolbar separators don't lose their size 
 * when going back to the Swing L&F
 */
public class SkinToolBarSeparatorUI extends BasicToolBarSeparatorUI {

	/* (non-Javadoc)
	 * @see swingwtx.swing.plaf.ComponentUI#getMaximumSize(javax.swing.JComponent)
	 */
	public Dimension getMaximumSize(JComponent c) {
		Dimension pref = getPreferredSize(c);
		if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
			return new Dimension(pref.width, Short.MAX_VALUE);
		} else {
			return new Dimension(Short.MAX_VALUE, pref.height);
		}
	}

	/* (non-Javadoc)
	 * @see swingwtx.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
	 */
	public Dimension getPreferredSize(JComponent c) {
		Dimension size = ((JToolBar.Separator) c).getSeparatorSize();
		if (size != null) {
			size = size.getSize();
		} else {
			size = new Dimension(6, 6);
			if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
				size.height = 0;
			} else {
				size.width = 0;
			}
		}
		return size;
	}

	/* (non-Javadoc)
	 * @see swingwtx.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new SkinToolBarSeparatorUI();
	}

}
