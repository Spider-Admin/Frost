/*
*   Copyright (c) 2003 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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
*
*   Created on Jan 15, 2003 at 7:36:31 PM by martin
*   Commited on $Date$
*/
package mseries.plaf.Mac;

import javax.swing.*;
import java.awt.*;

public class MacArrowButton extends JButton
{
    int direction;

    public MacArrowButton(int dir)
    {
        super();
        setDirection(dir);
        setRequestFocusEnabled(false);
        setIcon(new Triangle(dir, 4));
    }

    public int getDirection()
    {
        return direction;
    }

    public void setDirection(int dir)
    {
        direction = dir;
    }

    class Triangle implements Icon
    {
        int size = 6;
        int dir = NORTH;

        public Triangle(int dir, int size)
        {
            this.dir = dir;
            this.size = size;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Color oldColor = g.getColor();
            int mid, i, j;

            j = 0;
            size = Math.max(size, 2);
            mid = size / 2;

            g.translate(x, y);

            g.setColor(UIManager.getColor("textText"));
            switch (dir)
            {
                case NORTH:
                    for (i = 0; i < size; i++)
                    {
                        g.drawLine(mid - i, i, mid + i, i);
                    }

                    break;
                case SOUTH:


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

                    break;
                case EAST:


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

        public int getIconWidth()
        {
            return size;
        }

        public int getIconHeight()
        {
            return size;
        }
    }
}
