package mseries.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Class created to "work-around" an apparent bug with sun.util.BuddhistCalendar (hardcoded to be returned
 * by Calendar.getInstance() calls for the Thai locale (language)).  The bug causes the Calendar date to become incorrect
 * (e.g. 1998 + 5 years becomes 1460) whenever a .add(...) call forces a year change.
 *
 * @author  S. Ruman
 * @version 1.0
 * @see java.util.Calendar
 */

public class SafeCalendarUtils
{
    public static String squeeze(String input)
    {
        StringBuffer insb=new StringBuffer(input);
        StringBuffer outsb=new StringBuffer(input.length());
        for (int i=0; i<insb.length(); i++)
        {
            if (insb.charAt(i)!=' ')
            {
                outsb.append(insb.charAt(i));
            }
        }
        return new String(outsb);
    }
    /**
     Creates a temporary GregorianCalendar, sets its time, uses it for the addition, and then sets the modified
     time to the passed in Calendar.
     Note:  This convenience performs a .add(...) call (as opposed to a .roll(...) call) on the Calendar.
     @param cal     The calendar to be modified by adding the <i>amount</i> of <i>field</i> to it
     @param field   A constant from the Calendar class (DAY, MONTH, etc.)
     @param amount  The amount of <i>field</i> to add to the calendar.
     */
    public static final void doSafeAddition(Calendar cal, int field, int amount)
    {
        if (cal == null)
            return;

        // sun.util.BuddhistCalendar inherits from GregorianCalendar, so an instanceof doesn't work and
        // a security access exception occurs if sun.util.BuddhistCalendar is mentioned by name (unfortunately, this forces
        // brittleness if there are "legitimate" GregorianCalendar sub-classes, but no choice)
        if (!cal.getClass().equals(GregorianCalendar.class))
        {           // Creating GregorianCalendar's is relatively expensive
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(cal.getTime());

            gc.add(field, amount);

            cal.setTime(gc.getTime());
        }
        else
            cal.add(field, amount);
    }
}