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
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.plaf.basic;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import mseries.Calendar.*;

public class BasicHeaderUI extends ComponentUI
{
    protected Header header;

    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        return new BasicHeaderUI();
    }

    /*
    *    Called by the UIManager to install the UI of the component
    */
    public void installUI(JComponent c)
    {
        header = (Header)c;
        installDefaults();
    }

    public void uninstallUI(JComponent c)
    {
        uninstallDefaults();
    }

    protected void installDefaults()
    {
    }

    protected void uninstallDefaults() {};


    public void update(Graphics g, JComponent c)
    {
        paint(g, c);
    }

    public Font getFont()
    {
        return header.getFont();
    }

    public void paint(Graphics g, JComponent c)
    {
        int cellWidth = getCellSize().width;
        int cellHeight = getCellSize().height;
        int cols = header.getCols();

        String legend;
        Font font = getFont();
        int strWidth=0;
        int strHeight=0;
        int x,y;

        FontMetrics fm;

        for (int w=0; w <cols ; w++)
        {
            if(isOpaque())
            {
                g.setColor(header.getBackground(w));
                g.fillRect(0, 0, cellWidth, cellHeight);
            }

            legend = header.getColumnName(w);
            g.translate(cellWidth*w, 0);
            if (legend != null)
            {
                g.setFont(getFont());
                fm = g.getFontMetrics();
                strWidth = fm.stringWidth(legend);
                strHeight = fm.getHeight();
                x=cellWidth-strWidth-4;
                y=((cellHeight-strHeight)/2)+strHeight-4;

                g.setColor(header.getForeground(w));
                g.drawString(legend, x, y);

            }
            g.translate(-cellWidth*w, 0);
        }
    }

    public boolean isOpaque()
    {
        return header.isOpaque();
    }

    protected Dimension getCellSize()
    {
        return header.getCellSize();
    }
}
