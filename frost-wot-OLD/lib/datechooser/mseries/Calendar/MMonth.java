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

import java.awt.*;
import java.util.*;

/**
 *   The business end of the DateSelector component. This class maintains the state of the
 *   the component
 */
public class MMonth
{

    private Calendar today;
    private Calendar currentDate;     // As passed in,
    private Point currentPoint;       // The point in the model/table
    // of the current date
    private int calMonth;     // The month of the current calendar
    private Calendar startOfMonth;  // The 1st of the month
    private Calendar date;          // Working date
    protected Vector listeners = new Vector();

    private Calendar minC;          // Minimum value allowed
    private Calendar maxC;          // Maximum value allowed

    private Toolkit tk;

    int offset;
    int firstDay = 1;
    private Date minDate;
    private Date maxDate;

    public MMonth(Date min, Date max)
    {
        this.minDate=min;
        this.maxDate=max;
        init();
    }
    public MMonth()
    {
        init();
    }
    private void init()
    {
        today = Calendar.getInstance();
        today.setTime(new Date());

        currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        startOfMonth = Calendar.getInstance();
        minC = Calendar.getInstance();
        maxC = Calendar.getInstance();
        currentPoint = new Point(0, 0);
        tk = Toolkit.getDefaultToolkit();

        if(minDate==null)
        {
            minC.set(1900, 0, 1, 0, 0, 0);
        }
        else
        {
            minC.setTime(minDate);
        }

        if(maxDate==null)
        {
        maxC.set(2037, 11, 31, 23, 59, 59);
        }
        else
        {
            maxC.setTime(maxDate);
        }


        setFirstDay(Calendar.SUNDAY);
    }

    /**
     *   The first column on the calendar grid shows the dates for
     *   the day passed here.
     *   @param first a day constant from java.util.Calendar such as Calendar.SUNDAY
     */
    public void setFirstDay(int first)
    {
        if (first < Calendar.SUNDAY || first > Calendar.SATURDAY)
        {
            return;
        }
        this.firstDay = first;
        try
        {
            setDate(getCurrentDate());
        }
        catch (MDateOutOfRangeException ex)
        {
        }
        notifyListeners(new MMonthEvent(this, MMonthEvent.NEW_FIRST_DAY, currentDate));
    }

    /**
     *   @return the first day on display
     */
    public int getFirstDay()
    {
        return firstDay;
    }


    /**
     *   Sets the date and preserves the time that the model previously has
     *   @param inDate the new value whose Hour, Minute and Second elements are
     *   ignored
     */
    protected void setDMY(Date inDate) throws MDateOutOfRangeException
    {
        int h = currentDate.get(Calendar.HOUR_OF_DAY);
        int m = currentDate.get(Calendar.MINUTE);
        int s = currentDate.get(Calendar.SECOND);

        Calendar newC = Calendar.getInstance();
        newC.setTime(inDate);
        newC.set(Calendar.HOUR_OF_DAY, h);
        newC.set(Calendar.MINUTE, m);
        newC.set(Calendar.SECOND, s);
        setDate(newC.getTime());
    }

    public void setDate(Date inDate) throws MDateOutOfRangeException
    {
        long currentInMillis = 0;
        long startInMillis = 0;

        if (compDates(inDate, minC) < 0 || compDates(inDate, maxC) > 0)
        //if (inDate.before(minC.getTime()) || inDate.after(maxC.getTime()))
        {
            throw new MDateOutOfRangeException(inDate);
        }

        currentDate.setTime(inDate);

        currentInMillis = inDate.getTime();

        calMonth = currentDate.get(Calendar.MONTH);

        startOfMonth.setTime(inDate);
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1); // First of month

        int startDay = startOfMonth.get(Calendar.DAY_OF_WEEK);

        // Calculate the first date to appear on the top line
        // of the calendar
        int x = firstDay - startDay;
        if (x > 0) x = x - 7;
//System.out.println("first="+firstDay+", StartDay="+startDay+", x="+x);
        startOfMonth.add(Calendar.DATE, x);
//System.out.println("S="+startOfMonth.getTime());
        startInMillis = startOfMonth.getTime().getTime();

        /* Now calculate the cell of the current date, there are 86,400,000
        *  milliseconds in a day
        */
        Double daysD = new Double(((currentInMillis - startInMillis) / 86400000d) + 0.5d);
        int daysI = daysD.intValue(); // #days from start of calendar
        if (startInMillis < 0 && currentInMillis > 0)
        {
            daysI++;
        }
        int row = Math.abs(daysI / 7);
        int col = daysI - row * 7;

        currentPoint.x = col;
        currentPoint.y = row;

        // Fire MMonthEvent as all data has changed
        notifyListeners(new MMonthEvent(this, MMonthEvent.NEW_MONTH, currentDate));
    }

    /** Sets the earliest possible date for the calendar
     *   @param min the earliest possible date
     */
    public void setMinimum(Date min)
    {
        if(compDates(min, maxC)<0)
        {
             minC.setTime(min);
            notifyListeners(new MMonthEvent(this, MMonthEvent.NEW_MIN, minC));
        }
    }

    /** Returns the earliest possible date the model can be set to
     *   @return earliest date
     */
    public Date getMinimum()
    {
        return minC.getTime();
    }

    /** Sets the latest possible date for the calendar
     *   @param max the latest possible date
     */
    public void setMaximum(Date max)
    {
        if(compDates(max, minC)>0)
        {
            maxC.setTime(max);
            notifyListeners(new MMonthEvent(this, MMonthEvent.NEW_MAX, maxC));
        }
    }

    /** Returns the latest possible date the model can be set to
     *   @return latest date
     */
    public Date getMaximum()
    {
        return maxC.getTime();
    }

    /** Adds a number of months to the minimum date
     *   @param increment the number of months to add
     */
    public void addToMin(int increment) throws MDateOutOfRangeException
    {
        addToM(increment);
    }

    private void addToM(int increment) throws MDateOutOfRangeException
    {
        Calendar currVal = (Calendar) minC.clone();
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        int hour = currentDate.get(Calendar.HOUR_OF_DAY);
        int minute = currentDate.get(Calendar.MINUTE);
        int second = currentDate.get(Calendar.SECOND);


        currVal.add(Calendar.MONTH, increment);
        currVal.set(Calendar.DAY_OF_MONTH, day);
        currVal.set(Calendar.HOUR_OF_DAY, hour);
        currVal.set(Calendar.MINUTE, minute);
        currVal.set(Calendar.SECOND, second);

        int day2 = currVal.get(Calendar.DAY_OF_MONTH);
        //  If the dates are the same do nothing
        if (compDates(currVal, currentDate) == 0)
        {
            return;
        }

        /*  Detect if we have skipped a month which occurs when the current date is 31 and the
        *   following month has only 30 days (or less)
        */
        if (day != day2)
        {
            currVal.set(Calendar.DAY_OF_MONTH, 1);
            currVal.add(Calendar.DAY_OF_MONTH, -1);
        }
        setDate(currVal.getTime());
    }

    /**
     *    Returns the current date in the calendar. This gets set as
     *    the calendar is clicked on
     *    @return the currently selected date
     */
    public Date getCurrentDate()
    {
        return currentDate.getTime();
    }

    public Point getCurrentPoint()
    {
        return currentPoint;
    }

    /**
     *    Sets the current date in the model
     */
    public void setCurrentDate(int row, int col) throws MDateOutOfRangeException
    {
        Calendar value = (Calendar) getValueAt(row, col);

        if (compDates(value, minC) < 0|| compDates(value, maxC) >0)
        {
            tk.beep();
            throw new MDateOutOfRangeException(value.getTime());
        }
        else
        {
            currentDate = value;
            currentPoint.x = col;
            currentPoint.y = row;
            notifyListeners(new MMonthEvent(this, MMonthEvent.NEW_DATE, currentDate));

        }

    }

    /**
     *    returns the month index of the current calendar
     */
    public int getMonth()
    {
        return calMonth;
    }

    public int getColumnCount()
    {
        return 7;
    }

    public int getRowCount()
    {
        return 6;
    }

    /**
     *   calculates the date in a particular cell based on the date at 0,0
     *   (the start of the displayed calendar) and the cell being returned
     */
    public Object getValueAt(int r, int c)
    {
        offset = r * 7 + c;
        date = (Calendar) startOfMonth.clone();
        date.add(Calendar.DATE, offset);

        return date;
    }

    protected Date getAsDate(int r, int c)
    {
        Calendar cal = (Calendar) getValueAt(r, c);
        return cal.getTime();
    }

    /** Registers the listeners of the table changes
     *   @param listener - MMonthListener
     */
    public void addMMonthListener(MMonthListener listener)
    {
        listeners.addElement(listener);

    }

    /** Removes the listener from the registered list
     *   of listeners
     *   @param listener - MMonthListener
     */
    public void removeMMonthListener(MMonthListener listener)
    {
        listeners.removeElement(listener);

    }

    /** Notifies registered listeners of a change to the data model
     *   @param event - a MMonthEvent describing the change
     */
    private void notifyListeners(MMonthEvent event)
    {
        // Pass these events on to the registered listener

        Vector list = (Vector) listeners.clone();
        for (int i = 0; i < list.size(); i++)
        {
            MMonthListener l = (MMonthListener) listeners.elementAt(i);
            l.dataChanged(event);
        }
    }

    public boolean isInRange(Date date)
    {
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        return isInRange(c);
    }
    public boolean isInRange(Calendar date)
    {
        return compDates(date, minC)>=0 && compDates(date, maxC)<=0;
    }

    public void exitEvent()
    {
        notifyListeners(new MMonthEvent(this, MMonthEvent.SELECTED, currentDate));
    }

    public void lostFocus()
    {
        notifyListeners(new MMonthEvent(this, MMonthEvent.EXITED, currentDate));
    }

    public boolean isCellEditable(int r, int c)
    {
        return false;
    }

    /**
     *   Compares the Day, Month and Year in the Calendar objects passed in
     *   @return 0 if the D, M and Y are the same in d2 and d1, < 0 if d1 is
     *   before d2, > 0 if d1 is after d2
     */
    private static int compDates(Calendar d1, Calendar d2)
    {
        int day1 = d1.get(Calendar.DATE);
        int month1 = d1.get(Calendar.MONTH);
        int year1 = d1.get(Calendar.YEAR);
        int day2 = d2.get(Calendar.DATE);
        int month2 = d2.get(Calendar.MONTH);
        int year2 = d2.get(Calendar.YEAR);


        if (year1 != year2)
            return year1 - year2;
        if (month1 != month2)
            return month1 - month2;
        if (day1 != day2)
            return day1 - day2;

        return 0;
    }

    protected static int compDates(Date d1, Calendar c2)
    {
        Calendar c1 = Calendar.getInstance();
        c1.setTime((Date) d1.clone());
        return compDates(c1, c2);
    }

    public boolean isCurrentDate(int d, int w)
    {
        Calendar date = (Calendar) getValueAt(d, w);
        return (compDates(date, today) == 0);
    }
}

/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.17  2003/10/11 10:28:46  martin
*** empty log message ***

Revision 1.16  2003/10/04 09:41:40  martin
*** empty log message ***

Revision 1.15  2003/06/20 21:55:32  martin
*** empty log message ***

Revision 1.14  2003/04/01 19:42:23  martin
Fixed DST problem

Revision 1.13  2003/03/31 20:35:39  martin
no message

Revision 1.12  2003/03/26 23:29:48  martin
Changed email address

Revision 1.11  2002/08/20 21:38:06  martin
no message

Revision 1.10  2002/08/20 20:53:07  martin
no message

Revision 1.9  2002/07/21 16:24:39  martin
no message

Revision 1.8  2002/06/07 21:53:03  martin
Fixed bugs introduced in earlier fixes

Revision 1.7  2002/06/06 21:13:46  martin
Fixed bug with changing the year

Revision 1.6  2002/05/31 18:51:33  martin
no message

Revision 1.5  2002/05/31 17:20:53  martin
Added support for incrementing dates when current date is the last of the month

Revision 1.4  2002/02/09 12:54:39  martin
Partial support for 'Special Days'

Revision 1.3  2002/02/03 12:49:08  martin
Added support for curret date highlighted in different colour

*/
