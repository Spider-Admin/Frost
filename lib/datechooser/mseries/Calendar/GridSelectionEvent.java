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
package mseries.Calendar;

import java.awt.Point;

/**
*   An Event class used to notify listeners that ther has been a change
*   in the Selceted grid square
*/
public class GridSelectionEvent extends java.util.EventObject
{
    int x;
    int y;
    boolean quit=false;

    public GridSelectionEvent(Object source, int x, int y)
    {
        super(source);
        this.x=x;
        this.y=y;
    }

    public GridSelectionEvent(Object source, Point cell)
    {
        super(source);
        this.x=cell.x;
        this.y=cell.y;
    }

    public GridSelectionEvent(Object source, boolean quit)
    {
        super(source);
        this.quit=quit;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public boolean isExitEvent()
    {
        return quit;
    }

    public String toString()
    {
        String buff;
        buff = "["+getClass().getName()+": x="+getX()+", y="+getY()+"]";
        return buff;
    }
}

