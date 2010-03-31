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

import java.text.*;
import java.util.*;

import javax.swing.*;


/**
    Date entry widget with built in Formatter/Parser and Calendar popup to
    facilitate input. The earliest and latest possible values may be set using
    the setMinimum and setMaximum methods, this causes the calendar popup to
    allow date within the range to be selected. Dates outside of the range may
    be entered by typing directly into the field.
*/
public class MDateDisplay extends JTextField implements MMonthListener
{
    Date date;
    Date minDate=null;
    Date maxDate=null;

    DateFormat shortFormatter = DateFormat.getDateInstance();


    public MDateDisplay(int text)
    {
        super(text);
        initialise();
    }

    public MDateDisplay()
    {
        initialise();
    }

    public void dataChanged(MMonthEvent e)
    {

        if ((e.getType() == MMonthEvent.NEW_DATE)
        || (e.getType() == MMonthEvent.NEW_MONTH)
        || (e.getType() == MMonthEvent.SELECTED))
        {
            setValue(e.getNewDate().getTime());
        }

    }

    private void initialise()
    {
    }

    /** Sets the earliest value that may be selected for this field when
    *   the poup calendar in invoked. The default is 1 January 1900
    *   @param date, the ealiest date
    */
    public void setMinimum(Date date)
    {
        minDate=date;
    }

    /** Sets the latest value that may be selected for this field when
    *   the poup calendar in invoked. The default is 31 December 2037
    *   @param date, the latest date
    */
    public void setMaximum(Date date)
    {
        maxDate=date;
    }

    public Date getValue() throws ParseException
    {
        date = shortFormatter.parse(getText());
        return date;
    }

    public void setValue(Date date)
    {
        setText(date==null ? "" : shortFormatter.format(date));
    }
}
