/*
  DateFun.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost;
import java.util.*;
import java.io.*;


public class DateFun
{
    final static boolean DEBUG = false;

    /**
     * Returns date
     * @return Date as String yyyy.m.d in GMT without leading zeros
     */
    public static String getDate()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new StringBuffer(11).append(cal.get(Calendar.YEAR)).append(".")
        .append(cal.get(Calendar.MONTH) + 1).append(".").append(cal.get(Calendar.DATE)).toString();
    }

    /**
     * Returns time
     * @return Time as String h:m:s in GMT without leading zeros
     */
    public static String getTime()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new StringBuffer(9).append(cal.get(Calendar.HOUR_OF_DAY)).append(":")
        .append(cal.get(Calendar.MINUTE)).append(":").append(cal.get(Calendar.SECOND)).toString();
    }

    /**
     * Returns date with leading zeroes
     * @return Date as String yyyy.MM.dd in GMT with leading zeros
     */
    public static String getExtendedDate()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        StringBuffer sb = new StringBuffer(11);
        sb.append(year).append(".");
        if( month < 10 )
            sb.append("0");
        sb.append(month).append(".");
        if( day < 10 )
            sb.append("0");
        sb.append(day);
        return sb.toString();
    }

    /**
     * Returns time with leading zeroes
     * @return Time as String h:mm:ss in GMT with leading zeros
     */
    public static String getExtendedTime()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        StringBuffer sb = new StringBuffer(9);
        //    if( hour<10 )  // commented out to keep old behaviour, see below
        //        sb.append("0");
        sb.append(hour).append(":");
        if( minute < 10 )
            sb.append("0");
        sb.append(minute).append(":");
        if( second < 10 )
            sb.append("0");
        sb.append(second);
        return sb.toString();
        /*
            String sHour = String.valueOf(hour);
            String sMinute = String.valueOf(minute);
            String sSecond = String.valueOf(second);
            if (hour < 10)
                sHour = "0" + sHour;
            if (minute < 10)
                sMinute = "0" + sMinute;
            if (second < 10)
                sSecond = "0" + sSecond;
            return hour + ":" + sMinute +":" + sSecond; <-- bug or feature? uses hour instead of sHour as prepared
        */
    }
    /**
     * Returns time with leading zeroes
     * @return Time as String hh:mm:ss in GMT with leading zeros
     *
     * **** getExtendedDate() returns h:mm:ss, this returns the correct hh:mm:ss
     *
     */
    public static String getFullExtendedTime()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        StringBuffer sb = new StringBuffer(9);
        if( hour<10 )
            sb.append("0");
        sb.append(hour).append(":");
        if( minute < 10 )
            sb.append("0");
        sb.append(minute).append(":");
        if( second < 10 )
            sb.append("0");
        sb.append(second);
        return sb.toString();
    }

    /**
     * Converts a String with format: DATE.MONTH.YEAR HOUR:MINUTE:SECONDGMT
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
    public static Calendar getCalendarFromDateAndTime(String text)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        if( text.indexOf(" ") != -1 )
        {
            String date = text.substring(0, text.indexOf(" "));
            int firstPoint = date.indexOf(".");
            int secondPoint = date.lastIndexOf(".");
            if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
            {
                int year = Integer.parseInt(date.substring(0, firstPoint));
                int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
                int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                cal.set(Calendar.DATE, day - 1);
                if( DEBUG ) System.out.print("TOF Date: " + year+"."+month+"."+day);
            }
        }
        return cal;
    }

    /**
     * Converts a String with format: DATE.MONTH.YEAR
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
    public static Calendar getCalendarFromDate(String date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int firstPoint = date.indexOf(".");
        int secondPoint = date.lastIndexOf(".");
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
        {
            int year = Integer.parseInt(date.substring(0, firstPoint));
            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DATE, day - 1);
        }
        return cal;
    }

    public static String getDateOfCalendar(GregorianCalendar calDL)
    {
        String date = new StringBuffer(11).append(calDL.get(Calendar.YEAR)).append( ".")
                      .append(calDL.get(Calendar.MONTH) + 1).append( ".")
                      .append(calDL.get(Calendar.DATE)).toString();
        return date;
    }

}
