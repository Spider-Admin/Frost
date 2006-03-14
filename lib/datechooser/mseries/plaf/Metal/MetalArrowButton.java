package mseries.plaf.Metal;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;

import javax.swing.*;
import javax.swing.plaf.metal.*;

/**
 * JButton object that draws a scaled Arrow in one of the cardinal directions.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.19 02/02/00
 */
public class MetalArrowButton extends JButton implements SwingConstants
{
    protected int direction;

    public MetalArrowButton(int direction)
    {
        super();
        setRequestFocusEnabled(false);
        setDirection(direction);
        setBackground(UIManager.getColor("control"));
    }

    public int getDirection() { return direction; }

    public void setDirection(int dir) { direction = dir; }

    protected boolean top, left, bottom, right;
    /**
    *   Draws the border on the side which is true. Only left is implemented
    */
    public void drawBorder(boolean top, boolean left, boolean bottom, boolean right)
    {
        this.top=top;
        this.left=left;
        this.bottom=bottom;
        this.right=right;
    }

	public void paint(Graphics g)
    {
	    Color origColor;
	    boolean isPressed, isEnabled;
	    int w, h, size;

        w = getSize().width;
        h = getSize().height;
	    origColor = g.getColor();
	    isPressed = getModel().isPressed();
	    isEnabled = isEnabled();

        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);

        /// Draw the proper Border
        if (left)
        {
            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawLine(0,0,0,h);
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine(1,0,1,h);
        }
        if (top)
        {
            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawLine(0,0,w-2,0);
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine(1,1,w-1,1);
        }
        if (bottom)
        {
            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawLine(1,h-2,w,h-2);
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine(0,h-1,w,h-1);
        }
        if (right)
        {
            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawLine(w-2,0,w-2,h-2);
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawLine(w-1,1,w-1,h-1);
        }
        if (isPressed)
        {
            g.setColor(UIManager.getColor("controlShadow"));
            g.fillRect(0, 0, w, h);
        }

        // If there's no room to draw arrow, bail
        if(h < 5 || w < 5)
        {
            g.setColor(origColor);
            return;
        }

        // Draw the arrow
        size = Math.min((h - 4) / 3, (w - 4) / 3);
        size = Math.max(size, 2)+2;
        paintTriangle(g, (w - size) / 2, (h - size) / 2, size, direction, isEnabled);

            // Reset the Graphics back to it's original settings
	    g.setColor(origColor);

    }

    public Dimension getPreferredSize()
    {
        return new Dimension(16, 16);
    }

    public Dimension getMinimumSize()
    {
        return new Dimension(5, 5);
    }

    public Dimension getMaximumSize()
    {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public boolean isFocusable()
    {
        return false;
    }

	public void paintTriangle(Graphics g, int x, int y, int size,
        int direction, boolean isEnabled)
    {
	    Color oldColor = g.getColor();
	    int mid, i, j;

	    j = 0;
            size = Math.max(size, 2);
	    mid = size / 2;

	    g.translate(x, y);

        g.setColor(isEnabled() ? MetalLookAndFeel.getControlInfo() : MetalLookAndFeel.getControlShadow() );
        switch(direction)
        {
            case NORTH:
                for(i = 0; i < size; i++)
                {
                    g.drawLine(mid-i, i, mid+i, i);
                }
                if(!isEnabled)
                {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(mid-i+2, i, mid+i, i);
                }
                break;
            case SOUTH:
                if(!isEnabled)
                {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for(i = size-1; i >= 0; i--)
                    {
                        g.drawLine(mid-i, j, mid+i, j);
                        j++;
                    }
		            g.translate(-1, -1);
		            g.setColor(UIManager.getColor("controlShadow"));
		        }

		        j = 0;
                for(i = size-1; i >= 0; i--)
                {
                    g.drawLine(mid-i, j, mid+i, j);
                    j++;
                }
                break;
            case WEST:
                for(i = 0; i < size; i++)
                {
                    g.drawLine(i, mid-i, i, mid+i);
                }
                if(!isEnabled)
                {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(i, mid-i+2, i, mid+i);
                }
                break;
            case EAST:
                if(!isEnabled)
                {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for(i = size-1; i >= 0; i--)
                    {
                        g.drawLine(j, mid-i, j, mid+i);
                        j++;
                    }
		            g.translate(-1, -1);
		            g.setColor(UIManager.getColor("controlShadow"));
                }

		        j = 0;
                for(i = size-1; i >= 0; i--)
                {
                    g.drawLine(j, mid-i, j, mid+i);
                    j++;
                }
		        break;
        }
	    g.translate(-x, -y);
	    g.setColor(oldColor);
	}
}
/*
$Log$
Revision 1.1  2006-03-14 14:09:45  bback
new date chooser component

Revision 1.6  2004/08/29 17:12:09  martin
*** empty log message ***

Revision 1.5  2004/03/07 17:10:27  martin
*** empty log message ***

Revision 1.4  2003/03/24 19:45:07  martin
Latest 1.4 version

Revision 1.2  2003/03/11 22:35:15  martin
Upgraded to Java 1.4 on 11/03/03

Revision 1.1.1.1.2.1  2002/02/02 15:41:59  martin
Removed depredated method for 1.4
/
*/
