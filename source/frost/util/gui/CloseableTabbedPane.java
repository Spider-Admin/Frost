/*
 *  XNap - A P2P framework and client.
 *
 *  See the file AUTHORS for copyright information.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package frost.util.gui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.*;

/**
 * Provides a <code>JTabbedPane</code> with close icons in the tab titles. 
 * If an icon is clicked the tab is removed from the pane.
 */
public class CloseableTabbedPane extends JTabbedPane
{
    
    //--- Data field(s) ---

    private Icon closeIcon;
    
    //--- Constructor(s) ---
    
    /**
     * Constructs a pane.
     *
     * @param closeIcon the icon to use in the tab titles
     */
    public CloseableTabbedPane(Icon closeIcon) 
    {
		this.closeIcon = closeIcon;

		addMouseListener(new ClosingListener());
    }
    
	/**
     * Constructs a pane with the default icon.
     *
     * @see #CloseableTabbedPane(Icon)
     */
    public CloseableTabbedPane() 
    {
		this(new ImageIcon(MainFrame.class.getResource("/data/closeTab.gif")));
    }

    //--- Method(s) ---

	public void addTab(String title, Component component, Icon icon, 
					   boolean closeable) 
    {
		if (closeable) {
			super.addTab(null, new WrapperIcon(title, icon), component);
		}
		else {
			super.addTab(title, icon, component);
		}
		setSelectedComponent(component);
    }

	public void addTab(String title, Component component, Icon icon)
	{
		addTab(title, component, icon, true);
	}

    public void addTab(String title, Component component) 
    {
		addTab(title, component, null, true);
    }

	public void setTitleAt(int index, String newTitle)
	{
		WrapperIcon icon = (WrapperIcon)getIconAt(index);
		if (icon != null) {
			icon.setTitle(newTitle);
			revalidate();
			repaint();
		}
		else {
			super.setTitleAt(index, newTitle);
		}
	}


    //--- Inner Class(es) ---

    
    protected class ClosingListener extends MouseAdapter
    {
	
		public void mouseReleased(MouseEvent e)
		{
			int i = getSelectedIndex();
	    
			// nothing selected
			if (i == -1) {
				return;
			}

			WrapperIcon icon = (WrapperIcon)getIconAt(i);
	    
			// close tab, if icon was clicked
			if (icon != null && icon.contains(e.getX(), e.getY())) {
				removeTabAt(i);
			}
		}
	
    }

    /**
     * Acts as a proxy class for the closing icon. 
     */
    protected class WrapperIcon implements Icon
    {

		//--- Data field(s) ---

		private Icon leftIcon;
		private EventIcon rightIcon;
		private String title;
		private int x = 0;
		private int y = 0;
		private int height = 10;
		private int width = 10;

		//--- Constructor(s) ---
	
		/**
		 * 
		 */
		public WrapperIcon(String title)
		{
			this(title, null);
		}

		public WrapperIcon(String title, Icon leftIcon)
		{
			this.title = title;
			this.leftIcon = leftIcon;

			rightIcon = new EventIcon(closeIcon);

			height = rightIcon.getIconHeight();
			width = rightIcon.getIconWidth();
		}
	
		//--- Method(s) ---

		public void setTitle(String newTitle)
		{
			title = newTitle;
		}

		/**
		 *
		 */
		public int getIconHeight()
		{
			return height;
		}

		/**
		 * 
		 */
		public int getIconWidth()
		{
			int textWidth= SwingUtilities.computeStringWidth
				(CloseableTabbedPane.this.getFontMetrics(CloseableTabbedPane.this.getFont()), 
				 title);
			if (leftIcon != null) {
				return leftIcon.getIconWidth() + rightIcon.getIconWidth()
					+ textWidth + 16;
			}
			else {
				return textWidth + 8 + rightIcon.getIconWidth();
			}
		}

		/**
		 * Overwrites paintIcon to get hold of the coordinates of the icon.
		 */
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			this.x = x + getIconWidth() - rightIcon.getIconWidth();
			this.y = y;

			// compute the correct y coordinate where to put the text
			Rectangle rect = new Rectangle(x, y, getIconWidth(), 
										   getIconHeight());
			Rectangle iconRect = new Rectangle();
			Rectangle textRect = new Rectangle();
			SwingUtilities.layoutCompoundLabel
				(CloseableTabbedPane.this, g.getFontMetrics(),
				 title, rightIcon, SwingUtilities.CENTER,
				 SwingUtilities.CENTER,
				 SwingUtilities.CENTER,
				 SwingUtilities.TRAILING,
				 rect, iconRect, textRect,
				 UIManager.getInt("TabbedPane.textIconGap"));

			if (leftIcon != null) {
				leftIcon.paintIcon(c, g, x, y + 1);
				g.setFont(CloseableTabbedPane.this.getFont());
				g.drawString(title, x + leftIcon.getIconWidth() + 8,
							 textRect.y + g.getFontMetrics().getAscent());
				rightIcon.paintIcon
					(c, g, x + getIconWidth() - rightIcon.getIconWidth(),
					 y + 1);
			}
			else {
				g.drawString(title, x,
							 textRect.y + g.getFontMetrics().getAscent());
				rightIcon.paintIcon
					(c, g, x + getIconWidth() - rightIcon.getIconWidth(),
					 y + 1);
			}
		}    
	
		/**
		 * Verifies if x and y are within the icon's borders.
		 */
		public boolean contains(int xEvent, int yEvent)
		{
			return rightIcon.contains(xEvent, yEvent);
		}
	}

    /**
     * Acts as a proxy class for the closing icon. 
     *
     * <p>The idea for this class stems from limewire's CancelSearchIconProxy
     * class, thanks for going open source guys.
     */
    protected class EventIcon implements Icon
    {

		//--- Data field(s) ---

		private Icon icon;    
		private int x = 0;
		private int y = 0;
		private int height = 10;
		private int width = 10;

		//--- Constructor(s) ---
	
		public EventIcon(Icon icon)
		{
			this.icon = icon;

			if (icon != null) {
				height = icon.getIconHeight();
				width = icon.getIconWidth();
			}
		}
	
		//--- Method(s) ---

		/**
		 *
		 */
		public int getIconHeight()
		{
			return height;
		}

		/**
		 * 
		 */
		public int getIconWidth()
		{
			return width;
		}

		/**
		 * Overwrites paintIcon to get hold of the coordinates of the icon,
		 * this is a rather rude approach just to find out if the icon
		 * was pressed.
		 */
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			this.x = x;
			this.y = y;
	    
			if (icon != null) {
				icon.paintIcon(c, g, x, y);
			}
			else {
				// top left -> bottom right
				g.drawLine(x, y, x + width, y + height);

				// bottom left -> top right
				g.drawLine(x, y + height, x + width, y);
			}
		}    
	
		/**
		 * Verifies if x and y are within the icon's borders.
		 */
		public boolean contains(int xEvent, int yEvent)
		{
			if (!(xEvent >= x) || !(xEvent <= x + width)) {
				return false;
			}
			if (!(yEvent >= y) || !(yEvent <= y + height)) {
				return false;
			}

			return true;
		}
    }

}