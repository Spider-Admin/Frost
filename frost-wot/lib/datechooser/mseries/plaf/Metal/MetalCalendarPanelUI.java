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
package mseries.plaf.Metal;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

/**
*    The Metal Look and Feel UIDelagate for CalendarPanel
*
*    A Look and Feel delegate for the CalenderPanel. This version renders the
*    selected date with a raised border and dashed focus indicator. TAB moves
*    around the calendar, Shift-Tab jumps out to the next component. The arrow
*    keys move around the panel stopping (i.e. not rolling over) at the ends.
*/
public class MetalCalendarPanelUI extends mseries.plaf.basic.BasicCalendarPanelUI
{
    /**
    *    This method is called by the UIManager to get an instance of
    *    this class and must be overridden in subclasses.
    */
    public static ComponentUI createUI(JComponent x)
    {
        return new MetalCalendarPanelUI();
    }

    /*
    *    This is where we would draw/remove borders, focus highlights,
    *    colours etc. Override this method in a subclass to render the
    *    selected date.
    */
    protected void drawSelectedEffect(Graphics g, boolean selected)
    {
        int strWidth=0;
        int strHeight=0;
        int x,y;
        int width, height;

        width = getCellSize().width;
        height = getCellSize().height;

        if(isOpaque())
        {
            g.setColor(background);
            g.fillRect(0,0,width, height);
        }

        if (selected)
        {

            g.setColor( MetalLookAndFeel.getControlDarkShadow() );
            g.drawRect( 0, 0, width-2, height-2 );
            g.setColor( MetalLookAndFeel.getControlHighlight() );
            g.drawRect( 1, 1, width-2, height-2 );
            g.setColor( MetalLookAndFeel.getControl() );
            g.drawLine( 0, height-1, 1, height-2 );
            g.drawLine( width-1, 0, width-2, 1 );
        }
    }

    /**
    *    Draws the dashed rectangle around the number in the cell which is selected
    *    when the calendar panel has focus. Over ride this method in a subclass
    *    to change the appearance.
    */
    protected void drawFocusedEffect(Graphics g, boolean focused, boolean selected)
    {
        int width, height;

        width = getCellSize().width;
        height = getCellSize().height;
        if (focused)
        {
            g.setColor(UIManager.getColor("Button.focus"));
            drawFocus(g, 3, 2, width-5, height-5);
        }

    }

    public void drawFocus(Graphics g,int x,int y,int width,int height)
    {
        int vx,vy;

        g.drawLine(x, y, width, y);
        g.drawLine(x, y, x, height);
        g.drawLine(x, height, width, height);
        g.drawLine(width, height, width, y);
    }
}
