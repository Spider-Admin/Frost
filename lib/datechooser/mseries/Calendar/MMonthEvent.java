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

import java.util.Calendar;

/** An Event class used to notify listeners that ther has been a change
*   in the MMonth data model
*/
public class MMonthEvent extends java.util.EventObject
{
    public static final int NEW_MONTH=0;
    public static final int NEW_DATE=1;
    public static final int NEW_MIN=2;
    public static final int NEW_MAX=3;
    public static final int SELECTED=4;
    public static final int NEW_FIRST_DAY=5;
    public static final int NEW_RB=6;
    public static final int EXITED=7;
    public static final int NEW_AUTO_DATE=8;

    private int type;
    private Calendar date;

    public MMonthEvent(Object source, int type, Calendar date)
    {
        super(source);
        this.type=type;
        this.date=date;
    }

    public int getType()
    {
        return type;
    }

    public Calendar getNewDate()
    {
        return date;
    }
}

