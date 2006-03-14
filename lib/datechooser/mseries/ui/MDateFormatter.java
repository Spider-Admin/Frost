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
package mseries.ui;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;

public class MDateFormatter  implements MDateFormat
{
    private DateFormat shortFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private DateFormat mediumFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    private DateFormat longFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);

    private DateFormat shortDFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
    private DateFormat mediumDFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private DateFormat longDFormatter = DateFormat.getDateInstance(DateFormat.LONG);

    private static DateFormat formatter;
    private static MDateFormatter thisFormatter;

    private MDateFormatter()
    {
        formatter = DateFormat.getDateInstance(DateFormat.SHORT);
        formatter.setLenient(false);

        shortFormatter.setLenient(false);
        mediumFormatter.setLenient(false);
        longFormatter.setLenient(false);

        shortDFormatter.setLenient(false);
        mediumDFormatter.setLenient(false);
        longDFormatter.setLenient(false);
    }


    /**
    *   Singleton constructor, there is no public constructor for this class
    */
    public static MDateFormat getInstance()
    {
        if (thisFormatter == null)
        {
             thisFormatter= new MDateFormatter();
        }
        return thisFormatter;
    }


    /**
    *    Returns the date formatted for display in the form of a String.
    *    The format is defined in the constructor of the class.
    */
    public String format(Date date)
    {
        String formattedDate="";
        try
        {
            formattedDate = formatter.format(date);
        }
        catch (NullPointerException npe)
        {
            System.out.println(npe.toString());
        }
        return formattedDate;
    }

    public StringBuffer format(Date date, StringBuffer appendTo, FieldPosition pos)
    {
        return formatter.format(date, appendTo, pos);
    }

   /**
    *  Applies the formatters to parse the input string and attempts
    *  to translate it into a valid date. Valid dates with the last two digits less
    *  than the value of Constants.CENTURY_VALUE are parsed to the 21st Century,
    *  greater than the constant value are the 20th Century.
    */

    public Date parse(String text) throws ParseException
    {
        String inputText=new String(text);
        Date date;

        try
        {
            date = dateTimeParse(inputText);
        }
        catch (ParseException pe)
        {
            // Date/Time didn't work so try date only
            date = dateParse(inputText);
            // date works so add default time and retry
            inputText+=" 00:00:00";
            date=dateTimeParse(inputText);
        }

        return date;
    }


    /** Uses Date formatters to parse the input text
    *   @param text - the string to be parsed
    */
    public Date dateParse(String text) throws ParseException
    {
        Date date=new Date();
        try
        {
            date=longDFormatter.parse(text);

        }
        catch (ParseException le)
        {
            try
            {
                 date=mediumDFormatter.parse(text);
            }
            catch (ParseException me)
            {
                try
                {
                    date=shortDFormatter.parse(text);
                }
                catch (ParseException se)
                {
                    throw se;
                }
            }
        }

        return date;
    }
    /** Uses DateTime formatters to parse the input text
    *   @param text - the string to be parsed
    */
    public Date dateTimeParse(String text) throws ParseException
    {
        Date date=new Date();
        try
        {
            date=longFormatter.parse(text);

        }
        catch (ParseException le)
        {
            try
            {
                 date=mediumFormatter.parse(text);
            }
            catch (ParseException me)
            {
                try
                {
                    date=shortFormatter.parse(text);
                }
                catch (ParseException se)
                {
                    throw se;
                }
            }
        }
        return date;
    }

    public static void main(String args[])
    {
        Date d=new Date();

        MDateFormat df=null;

        try
        {
            df = MDateFormatter.getInstance();
            System.out.println(df.parse(args[0]));
        }
        catch(ParseException pe)
        {
            System.out.println(pe);

        }

        System.out.println(df.format(d));
    }
}

