/*
*   Copyright (c) 2001 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
*   Spinner model to manage a range of dates. This one is a little special in that in allows
*   each part (month, day, year etc) of a date to be spun independantly. In other words
*   the step is variable .The value is always a Date but the step changes usually by the
*   editor according to where the user places the cursor.
*/
public class MDateSpinnerModel extends DefaultSpinnerModel
{
    private int step;
    //private Date value;
    private Calendar m_calendar=Calendar.getInstance();

    private Date maxDate, minDate;

    private DateFormat df;
    private static final int UP=1;
    private static final int DOWN=-1;

    /**
    *   Constructor.
    *   @param start the initial value
    *   @param max the maximum value allowed
    *   @param min the minimum value allowed
    */
    public MDateSpinnerModel(Date start, Comparable max, Comparable min)
    {
        setMaximum(max);
        setMinimum(min);
        setValue(start);
    }

    /**
    *   Constructor with default minimum and maximum values (1/1/1970 and 31/12/2037 respectively)
    *   @param start the initial value
    */
    public MDateSpinnerModel(Date start)
    {
        try
        {
            df = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("en", "GB"));
            maxDate=df.parse("31/12/2037");
            minDate=df.parse("1/1/1970");
        }
        catch(Exception e)
        {
        }
        setValue(start);
    }

    /**
    *   Defualt constructor initialised to todays date and default maximum and minimum
    */
    public MDateSpinnerModel()
    {
        this(new Date());
    }

    /**
    *   Sets the step which is a constant from java.text.DateFormat
    *   @param step the step value
    */
    public void setStep(int step)
    {
        this.step=step;
    }

    /**
    *   @return the step value
    */
    private int getStep()
    {
        return step;
    }
    /**
    *   Returns the current value of the field
    *   @return the current value of the field
    */
    public Object getValue()
    {
        return m_calendar.getTime();
    }

    /**
    *   Sets a new value in the model
    *   @param newValue the new value
    */
    public void setValue(Object newValue)
    {
        Date x = (Date)newValue;
        if (!x.before(minDate) && !x.after(maxDate))
        {
            m_calendar.setTime(x);
            notifyListeners();
        }
    }

    /**
    *   Sets the maximum allowed value in the model
    *   @param max the maximum value
    */
    public void setMaximum(Comparable max)
    {
        if (max instanceof java.util.Date)
        {
            maxDate=(Date)max;
        }
        Date d = (Date)getValue();
        if (d.after(maxDate))
        {
            setValue(maxDate);
        }
    }

    /**
    *   Returns the maximum value
    *   @return the maximum value
    */
    public Comparable getMaximum()
    {
        return maxDate;
    }

    /**
    *   Sets the minimum allowed value in the model
    *   @param min the minimum value
    */
    public void setMinimum(Comparable min)
    {
        if (min instanceof java.util.Date)
        {
            minDate=(Date)min;
        }
        Date d = (Date)getValue();
        if (d.before(minDate))
        {
            setValue(minDate);
        }
    }

    /**
    *   Returns the minimum value
    *   @return the minimum value
    */
    public Comparable getMinimum()
    {
        return minDate;
    }

    /**
    *   Advance and return the next value in the sequence according to the step value and
    *   maximum
    *   @return the next value
    */
    public Object getNextValue()
    {
        Date d = (Date)changeValue(getStep(), UP);

        if (!d.after(maxDate))
        {
            setValue(d);
        }
        return getValue();
    }

    /**
    *   Advance and return the previous value in the sequence according to the step value and
    *   minimum
    *   @return the previous value
    */
    public Object getPreviousValue()
    {
        Date d = (Date)changeValue(getStep(), DOWN);

        if (!d.before(minDate))
        {
            setValue(d);
        }
        return getValue();
    }

    private Object changeValue(int step, int dir)
    {
        Calendar cal=(Calendar)m_calendar.clone();
        Date m_lastDate=cal.getTime();
        int d=cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        boolean dateSet = true;

        switch (step)
        {

            case DateFormat.AM_PM_FIELD:
                // If the time is AM, add 12 hours, if it is PM subtract 12 hours
                int ampm=cal.get(Calendar.AM_PM);
                int mult= (ampm==Calendar.AM) ? 12 : -12;

                cal.set(Calendar.HOUR,
                               cal.get(Calendar.HOUR) + mult);

                break;

            case DateFormat.DATE_FIELD:

            case DateFormat.DAY_OF_WEEK_FIELD:

            case DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD:

            case DateFormat.DAY_OF_YEAR_FIELD:
                cal.set(Calendar.DAY_OF_YEAR,
                               cal.get(Calendar.DAY_OF_YEAR)
                               + dir);
                break;

            case DateFormat.ERA_FIELD:
                dateSet = false;

                break;

            case DateFormat.HOUR0_FIELD:

            case DateFormat.HOUR1_FIELD:

            case DateFormat.HOUR_OF_DAY0_FIELD:

            case DateFormat.HOUR_OF_DAY1_FIELD:

                cal.set(Calendar.HOUR,
                               cal.get(Calendar.HOUR)
                               + dir);
                break;

            case DateFormat.MILLISECOND_FIELD:
                cal.set(Calendar.MILLISECOND,
                               cal.get(Calendar.MILLISECOND)
                               + dir);
                break;

            case DateFormat.MINUTE_FIELD:
                cal.set(Calendar.MINUTE,
                               cal.get(Calendar.MINUTE)
                               + dir);

                break;

            case DateFormat.MONTH_FIELD:
                cal.set(Calendar.MONTH,
                               cal.get(Calendar.MONTH)
                               + dir);
                int max=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, d > max ? max : d);

                m_lastDate = cal.getTime();

                break;

            case DateFormat.SECOND_FIELD:
                cal.set(Calendar.SECOND,
                               cal.get(Calendar.SECOND)
                               + dir);

                break;

            case DateFormat.WEEK_OF_MONTH_FIELD:
                cal.set(Calendar.WEEK_OF_MONTH,
                               cal.get(Calendar.WEEK_OF_MONTH)
                               + dir);

                break;

            case DateFormat.WEEK_OF_YEAR_FIELD:
                cal.set(Calendar.WEEK_OF_MONTH,
                               cal.get(Calendar.WEEK_OF_MONTH)
                               + dir);

                break;

            case DateFormat.YEAR_FIELD:
                cal.set(Calendar.YEAR,
                               cal.get(Calendar.YEAR)
                               + dir);

                break;

            default:
                dateSet = false;
        }

        if (dateSet)
        {
            m_lastDate = cal.getTime();
        }
        return m_lastDate;
    }
}
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.8  2003/08/22 21:52:45  martin
// no message
//
// Revision 1.7  2003/08/22 20:38:28  martin
// no message
//
// Revision 1.5  2003/03/26 23:29:50  martin
// Changed email address
//
// Revision 1.4  2002/08/17 19:55:54  martin
// Added CVS log tag
//
