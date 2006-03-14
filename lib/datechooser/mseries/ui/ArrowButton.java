/*
*   Copyright (c) 2002 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.ui;

import javax.swing.*;
import java.awt.*;


/**
 *   An arrow button with no border, until the mouse rolls over when the L&F border is drawn
 */
public class ArrowButton extends JButton implements SwingConstants
{
    int direction;

    public ArrowButton(int dir)
    {
        super();
        setDirection(dir);
        setRequestFocusEnabled(false);
        //setRolloverEnabled(true);
    }

    public int getDirection()
    {
        return direction;
    }

    public void setDirection(int dir)
    {
        direction = dir;
    }


    public void paint(Graphics g)
    {
        int w, h, size;
        boolean isEnabled;
        Color origColor;

        h = getSize().height;
        setMinimumSize(new Dimension(h, h));
        setPreferredSize(new Dimension(h, h));
        w = getSize().width;
        isEnabled = isEnabled();
        origColor = g.getColor();
        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);

        // Draw the arrow
        size = Math.min((h - 4) / 3, (w - 4) / 3);
        size = Math.max(size, 2);
        paintTriangle(g, (w - size) / 2, (h - size) / 2, size, direction, isEnabled);

        // Reset the Graphics back to it's original settings
        g.setColor(origColor);

        paintBorder(g);
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

        g.setColor(isEnabled() ? UIManager.getColor("Button.foreground") :
                UIManager.getColor("controlShadow"));
        switch (direction)
        {
            case NORTH:
                for (i = 0; i < size; i++)
                {
                    g.drawLine(mid - i, i, mid + i, i);
                }
                if (!isEnabled)
                {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(mid - i + 2, i, mid + i, i);
                }
                break;
            case SOUTH:
                if (!isEnabled)
                {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for (i = size - 1; i >= 0; i--)
                    {
                        g.drawLine(mid - i, j, mid + i, j);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(UIManager.getColor("controlShadow"));
                }

                j = 0;
                for (i = size - 1; i >= 0; i--)
                {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                break;
            case WEST:
                for (i = 0; i < size; i++)
                {
                    g.drawLine(i, mid - i, i, mid + i);
                }
                if (!isEnabled)
                {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    g.drawLine(i, mid - i + 2, i, mid + i);
                }
                break;
            case EAST:
                if (!isEnabled)
                {
                    g.translate(1, 1);
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                    for (i = size - 1; i >= 0; i--)
                    {
                        g.drawLine(j, mid - i, j, mid + i);
                        j++;
                    }
                    g.translate(-1, -1);
                    g.setColor(UIManager.getColor("controlShadow"));
                }

                j = 0;
                for (i = size - 1; i >= 0; i--)
                {
                    g.drawLine(j, mid - i, j, mid + i);
                    j++;
                }
                break;
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    public boolean isFocusable()
    {
        return false;
    }

    public boolean isFocusPainted()
    {
        return false;
    }

    public void setEnabled(boolean enabled)
    {
        if (!enabled)
        {
            getModel().setRollover(false);
        }
        super.setEnabled(enabled);
    }
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.4  2003/03/26 23:29:49  martin
// Changed email address
//
// Revision 1.3  2003/01/18 16:53:53  martin
// *** empty log message ***
//
// Revision 1.2  2003/01/07 22:15:55  martin
// *** empty log message ***
//
// Revision 1.1  2002/12/21 22:53:16  martin
// *** empty log message ***
//
// Revision 1.3  2002/07/21 16:25:20  martin
// no message
//
// Revision 1.2  2002/06/18 21:32:55  martin
// no message
//
// Revision 1.1  2002/06/16 21:46:43  martin
// new file
//
