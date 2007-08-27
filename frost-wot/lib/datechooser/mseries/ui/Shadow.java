/*
*   Copyright (c) 2000 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary
*   that makes use of this code and that some acknowedgement is given. Comments,
*   questions and requests for change will be welcomed.
*/
package mseries.ui;

import java.awt.*;
import javax.swing.*;

/**
*   A simple GUI component that displays a semi transparent strip. It is either
*   HORIZONATAL or VERTICAL and alwats four pixels wide.
*/
public class Shadow extends JComponent
{
    int shadowWidth=6;
    int orientation;
    /**
    *   Constructor. Use SwingUtilities.HORIZONTAL or SwingUtilities.VERTICAL to
    *   specify the orientation
    */
    public Shadow(int orientation)
    {
        this.orientation=orientation;
    }

    public void paintComponent(Graphics g)
    {

        int width=getSize().width;
        int height=getSize().height;

        g.setColor(Color.black);

        if (orientation==SwingUtilities.HORIZONTAL)
        {
            for (int i=width; i>shadowWidth; i-=shadowWidth)
            {
                g.drawLine(i,1,i,1);
                g.drawLine(i,3,i,3);
                g.drawLine(i,5,i,5);

                g.drawLine(i-1,0,i-1,0);
                g.drawLine(i-1,2,i-1,2);
                g.drawLine(i-1,4,i-1,4);

                g.drawLine(i-2,1,i-2,1);
                g.drawLine(i-2,3,i-2,3);
                g.drawLine(i-2,5,i-2,5);

                g.drawLine(i-3,0,i-3,0);
                g.drawLine(i-3,2,i-3,2);
                g.drawLine(i-3,4,i-3,4);

                g.drawLine(i-4,1,i-4,1);
                g.drawLine(i-4,3,i-4,3);
                g.drawLine(i-4,5,i-4,5);

                g.drawLine(i-5,0,i-5,0);
                g.drawLine(i-5,2,i-5,2);
                g.drawLine(i-5,4,i-5,4);

            }
        }
        else
        {
            for (int i=height; i>shadowWidth; i-=shadowWidth)
            {
                g.drawLine(1,i,1,i);
                g.drawLine(3,i,3,i);
                g.drawLine(5,i,5,i);

                g.drawLine(0,i-1,0, i-1);
                g.drawLine(2,i-1,2,i-1);
                g.drawLine(4,i-1,4,i-1);

                g.drawLine(1,i-2,1,i-2);
                g.drawLine(3,i-2,3,i-2);
                g.drawLine(5,i-2,5,i-2);

                g.drawLine(0,i-3,0, i-3);
                g.drawLine(2,i-3,2, i-3);
                g.drawLine(4,i-3,4, i-3);

                g.drawLine(1,i-4,1,i-4);
                g.drawLine(3,i-4,3,i-4);
                g.drawLine(5,i-4,5,i-4);

                g.drawLine(0,i-5,0, i-5);
                g.drawLine(2,i-5,2, i-5);
                g.drawLine(4,i-5,4, i-5);
            }

        }
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(shadowWidth, shadowWidth);
    }
}

