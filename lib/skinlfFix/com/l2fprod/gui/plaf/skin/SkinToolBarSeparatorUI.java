/*
 * Created on Nov 7, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.l2fprod.gui.plaf.skin;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;


public class SkinToolBarSeparatorUI extends BasicToolBarSeparatorUI {

	/* (non-Javadoc)
	 * @see javax.swing.plaf.ComponentUI#getMaximumSize(javax.swing.JComponent)
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
	 * @see javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
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

	public static ComponentUI createUI(JComponent c) {
		return new SkinToolBarSeparatorUI();
	}

}
