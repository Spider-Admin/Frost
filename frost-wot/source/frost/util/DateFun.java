/*
  DateFun.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util;

import java.util.*;
import java.util.logging.*;

public class DateFun {

    private static Logger logger = Logger.getLogger(DateFun.class.getName());
    
    private static long GMTOffset = -1;
    
    private static long getGMTOffset() {
        if( GMTOffset < 0 ) {
            Calendar cal = Calendar.getInstance();
            long milliDeltaToGMT = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
            GMTOffset = milliDeltaToGMT;
        }
        return GMTOffset;
    }

    /**
     * @return SQL Date object with current date in GMT.
     */
    public static java.sql.Date getCurrentSqlDateGMT() {
        Calendar cal = Calendar.getInstance();
        return getSqlDateOfCalendar(cal);
    }

    /**
     * @return SQL Date object with current date in GMT - daysAgo days.
     */
    public static java.sql.Date getSqlDateGMTDaysAgo(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-daysAgo);
        return getSqlDateOfCalendar(cal);
    }

    /**
     * @return SQL Time object with current time in GMT.
     */
    public static java.sql.Time getCurrentSqlTimeGMT() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);
        return new java.sql.Time( cal.getTimeInMillis() - getGMTOffset() );
    }
    
    /**
     * Returns the SQL date from Calendar
     */
    public static java.sql.Date getSqlDateOfCalendar(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.clear();
        c.set(year, month, day, 0, 0, 0);
        return new java.sql.Date( c.getTime().getTime() );
    }
    
    public static void main(String[] args) {
        
        System.out.println("->"+getCurrentSqlTimeGMT());
        System.out.println(getExtendedTimeFromSqlTime(getCurrentSqlTimeGMT()));
        System.out.println(getVisibleExtendedDate());
        System.out.println(getExtendedTime());
        
        System.out.println("---");
        
        System.out.println("->"+getCurrentSqlDateGMT());
        System.out.println(getExtendedDateFromSqlDate(getCurrentSqlDateGMT()));
        
        System.out.println(getSqlTimeFromString("08:52:11GMT"));
        
        System.out.println(getDate());
        
        Calendar c = getCalendarFromDateAndTime("2006.08.21 12:12:12GMT");
        System.out.println(getSqlDateOfCalendar(c));
        
        System.out.println(getFullExtendedTime());
        
        System.out.println("---");
        
        java.sql.Date sd = DateFun.getSqlDateOfCalendar(DateFun.getCalendarFromDate("2006.08.21"));
        System.out.println(sd+" ; "+DateFun.getExtendedDateFromSqlDate(sd));
        java.sql.Time st = DateFun.getSqlTimeFromString("12:13:14GMT");
        System.out.println(st+" ; "+DateFun.getExtendedTimeFromSqlTime(st));

//        System.out.println("-->"+getCurrentSqlDateGMT());
        
//        long v = System.currentTimeMillis() / MILLIS_PER_DAY;
//        System.out.println("res="+v);
//        java.sql.Date dd = new java.sql.Date(v * MILLIS_PER_DAY);
//        System.out.println("dd="+dd);
//        
//        
//        java.sql.Date dd2 = new java.sql.Date(2006-1900,8-1,23);
//        long v2 = dd2.getTime()/MILLIS_PER_DAY;
//        System.out.println("dd2="+dd2+", "+v2);
//        java.sql.Date dd3 = new java.sql.Date(v2 * MILLIS_PER_DAY);
//        System.out.println("dd3="+dd3);
//        long v3 = dd3.getTime() / MILLIS_PER_DAY;
//        System.out.println("v3="+v3);
//        dd3 = new java.sql.Date(v3 * MILLIS_PER_DAY);
//        System.out.println("dd3="+dd3);
//
//        java.sql.Date dd4 = java.sql.Date.valueOf("2006-08-23");
//        System.out.println("dd4="+dd4);
//        System.out.println(" = "+dd4.getTime()/MILLIS_PER_DAY);
//        
//        long v4 = Date.UTC(2006-1900, 8-1, 23, 0, 0, 0);
//        System.out.println("ddd="+v4/MILLIS_PER_DAY);
//        
//        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        c.clear();
//        c.set(2006, 8-1, 23, 0, 0, 0);
//        long v5 = c.getTime().getTime();
//        System.out.println("ddd="+v5);
//        System.out.println("ddd="+v5/MILLIS_PER_DAY);
//        
//        dd4 = new java.sql.Date(v5);
//        System.out.println("dd4="+dd4);
    }

    /**
     * Creates a java.sql.Time object from provided string in format "hh:mm:ssGMT".
     */
    public static java.sql.Time getSqlTimeFromString(String timeStr) {
        String hours = timeStr.substring(0, 2);
        String minutes = timeStr.substring(3, 5);
        String seconds = timeStr.substring(6, 8);
        int ihours = -1;
        int iminutes = -1;
        int iseconds = -1;
        try {
            ihours = Integer.parseInt( hours );
            iminutes = Integer.parseInt( minutes );
            iseconds = Integer.parseInt( seconds );
        } catch(Exception ex) {
            logger.warning("Could not parse the time");
            return null;
        }
        if( ihours < 0 || ihours > 23 ||
            iminutes < 0 || iminutes > 59 ||
            iseconds < 0 || iseconds > 59 )
        {
            logger.warning("Time is invalid");
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1970, 0, 1, ihours, iminutes, iseconds);
        return new java.sql.Time(cal.getTime().getTime());
    }
    
    public static String getExtendedTimeFromSqlTime(java.sql.Time time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time.getTime());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        StringBuffer sb = new StringBuffer(12);
        if( hour<10 )
            sb.append('0');
        sb.append(hour).append(':');
        if( minute < 10 )
            sb.append('0');
        sb.append(minute).append(':');
        if( second < 10 )
            sb.append('0');
        sb.append(second);
        sb.append("GMT");
        return sb.toString();
    }

    public static String getExtendedDateFromSqlDate(java.sql.Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return getExtendedDateOfCalendar(cal);
//        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        cal.setTimeInMillis(date.getTime());
//        return getExtendedDateOfCalendar(cal);
    }

    /**
     * Returns date
     * @return Date as String yyyy.m.d in GMT without leading zeros
     */
    public static String getDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return getDateOfCalendar(cal);
    }
    
    /**
     * Returns date with leading zeroes
     * @return Date as String yyyy.MM.dd in GMT with leading zeros
     */
    public static String getExtendedDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return getExtendedDateOfCalendar(cal);
    }

    /**
     * Returns date with leading zeroes
     * @return Date as String yyyy.MM.dd in GMT with leading zeros
     */
    public static String getExtendedDateFromMillis(long millis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millis);
        return getExtendedDateOfCalendar(cal);
    }

    /**
     * Returns date -n days.
     * @return Date as String yyyy.m.d in GMT without leading zeros
     */
    public static String getDate(int daysAgo) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.DATE,-daysAgo);
        return getDateOfCalendar(cal);
    }

    /**
     * Returns date -n days.
     * @return Date as String yyyy.mm.dd in GMT WITH leading zeros
     */
    public static String getExtendedDate(int daysAgo) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.DATE,-daysAgo);
        return getExtendedDateOfCalendar(cal);
    }

    /**
     * Returns date with leading zeroes
     * @return Date as String dd.mm.yyyy in GMT with leading zeros
     */
    public static String getVisibleExtendedDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        StringBuffer sb = new StringBuffer(11);
        if( day < 10 )
            sb.append('0');
        sb.append(day).append('.');
        if( month < 10 )
            sb.append('0');
        sb.append(month).append('.');
        sb.append(year);
        return sb.toString();
    }

    /**
     * Returns time
     * @return Time as String h:m:s in GMT without leading zeros
     */
    public static String getTime() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new StringBuffer(9).append(cal.get(Calendar.HOUR_OF_DAY)).append(':')
        .append(cal.get(Calendar.MINUTE)).append(':').append(cal.get(Calendar.SECOND)).toString();
    }

    /**
     * Returns time with leading zeroes
     * @return Time as String h:mm:ss in GMT with leading zeros
     */
    public static String getExtendedTime() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return getExtendedTimeOfCalendar(cal);
    }

    public static String getExtendedTimeOfCalendar(Calendar cal) {

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        StringBuffer sb = new StringBuffer(9);
        //    if( hour<10 )  // commented out to keep old behaviour, see below
        //        sb.append('0');
        sb.append(hour).append(':');
        if( minute < 10 )
            sb.append('0');
        sb.append(minute).append(':');
        if( second < 10 )
            sb.append('0');
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
    public static String getFullExtendedTime() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        StringBuffer sb = new StringBuffer(9);
        if( hour<10 )
            sb.append('0');
        sb.append(hour).append(':');
        if( minute < 10 )
            sb.append('0');
        sb.append(minute).append(':');
        if( second < 10 )
            sb.append('0');
        sb.append(second);
        return sb.toString();
    }

    /**
     * Converts a String with format: DATE.MONTH.YEAR HOUR:MINUTE:SECONDGMT
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
    public static Calendar getCalendarFromDateAndTime(String text) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        if( text.indexOf(' ') != -1 ) {
            String date = text.substring(0, text.indexOf(' '));
            int firstPoint = date.indexOf('.');
            int secondPoint = date.lastIndexOf('.');
            if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
                int year = Integer.parseInt(date.substring(0, firstPoint));
                int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
                int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                cal.set(Calendar.DATE, day);
                logger.fine("TOF Date: " + year + "." + month + "." + day);
            }
        }
        return cal;
    }

    /**
     * Converts a String with format: YEAR.MONTH.DATE
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
    public static Calendar getCalendarFromDate(String date) throws NumberFormatException {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int firstPoint = date.indexOf('.');
        int secondPoint = date.lastIndexOf('.');
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
            int year = Integer.parseInt(date.substring(0, firstPoint));
            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DATE, day);
        } else {
            throw new NumberFormatException("Invalid date: "+date);
        }
        return cal;
    }

    /**
     * Returns the date from Calendar as String with format yyyy.m.d
     */
    public static String getDateOfCalendar(Calendar calDL) {
        String date = new StringBuffer(11).append(calDL.get(Calendar.YEAR)).append('.')
                      .append(calDL.get(Calendar.MONTH) + 1).append('.')
                      .append(calDL.get(Calendar.DATE)).toString();
        return date;
    }

    /**
     * Returns the date from Calendar as String with format yyyy.mm.dd
     */
    public static String getExtendedDateOfCalendar(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        StringBuffer sb = new StringBuffer(11);
        sb.append(year).append('.');
        if( month < 10 )
            sb.append('0');
        sb.append(month).append('.');
        if( day < 10 )
            sb.append('0');
        sb.append(day);
        return sb.toString();
    }

    /**
     * 2005.9.3 -> 2005.09.03 (for comparisions)
     */
    public static String buildExtendedDate(String date) {
        int firstPoint = date.indexOf('.');
        int secondPoint = date.lastIndexOf('.');
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
            int year = Integer.parseInt(date.substring(0, firstPoint));
            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
            StringBuffer sb = new StringBuffer(11);
            sb.append(year).append('.');
            if( month < 10 )
                sb.append('0');
            sb.append(month).append('.');
            if( day < 10 )
                sb.append('0');
            sb.append(day);
            return sb.toString();
        }
        return null;
    }
}
