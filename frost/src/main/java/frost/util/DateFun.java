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

import org.joda.time.*;
import org.joda.time.format.*;

public class DateFun {

//    private static final Logger logger = Logger.getLogger(DateFun.class.getName());

//    private static long GMTOffset = -1;

    public static final DateTimeFormatter FORMAT_DATE = DateTimeFormat.forPattern("yyyy.M.d").withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter FORMAT_DATE_EXT = DateTimeFormat.forPattern("yyyy.MM.dd").withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter FORMAT_DATE_VISIBLE = DateTimeFormat.forPattern("dd.MM.yyyy").withZone(DateTimeZone.UTC);

    public static final DateTimeFormatter FORMAT_TIME_PLAIN = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter FORMAT_TIME = DateTimeFormat.forPattern("H:m:s'GMT'").withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter FORMAT_TIME_EXT = DateTimeFormat.forPattern("HH:mm:ss'GMT'").withZone(DateTimeZone.UTC);
    public static final DateTimeFormatter FORMAT_TIME_VISIBLE = DateTimeFormat.forPattern("HH:mm:ss' GMT'").withZone(DateTimeZone.UTC);

//    private static long getGMTOffset() {
//        if( GMTOffset < 0 ) {
//            Calendar cal = Calendar.getInstance();
//            long milliDeltaToGMT = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
//            GMTOffset = milliDeltaToGMT;
//        }
//        return GMTOffset;
//    }

    /**
     * @return SQL Date object with current date in GMT.
     */
//    public static java.sql.Date getCurrentSqlDateGMT() {
//        Calendar cal = Calendar.getInstance();
//        return getSqlDateOfCalendar(cal);
//    }

    /**
     * @return SQL Date object with current date in GMT - daysAgo days.
     */
//    public static java.sql.Date getSqlDateGMTDaysAgo(int daysAgo) {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE,-daysAgo);
//        return getSqlDateOfCalendar(cal);
//    }

    /**
     * @return SQL Time object with current time in GMT.
     */
//    public static java.sql.Time getCurrentSqlTimeGMT() {
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, 1970);
//        cal.set(Calendar.MONTH, 0);
//        cal.set(Calendar.DATE, 1);
//        return new java.sql.Time( cal.getTimeInMillis() - getGMTOffset() );
//    }

    /**
     * Returns the SQL date from Calendar
     */
//    public static java.sql.Date getSqlDateOfCalendar(Calendar cal) {
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        int day = cal.get(Calendar.DATE);
//        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        c.clear();
//        c.set(year, month, day, 0, 0, 0);
//        return new java.sql.Date( c.getTime().getTime() );
//    }

//    public static void main(String[] args) {
//
//        String date = "2006.10.14";
//        String time = "12:13:14GMT";
//
//        DateTimeFormatter fmtd = DateTimeFormat.forPattern("yyyy.MM.dd");
//        DateTimeFormatter fmtt = DateTimeFormat.forPattern("HH:mm:ss'GMT'");
//
//        DateTime dtd = fmtd.withZone(DateTimeZone.UTC).parseDateTime(date);
//        DateTime dtt = fmtt.withZone(DateTimeZone.UTC).parseDateTime(time);
//
//        System.out.println("dtd="+dtd+", millis="+dtd.getMillis());
//        System.out.println("dtt="+dtt+", millis="+dtt.getMillis());
//
//        long allMillis = dtd.getMillis() + dtt.getMillis();
//        DateTime adt = new DateTime(allMillis).withZone(DateTimeZone.UTC);
//        System.out.println("ADT="+adt);
//
//        DateTime nd = new DateTime(new Long(dtd.getMillis())).withZone(DateTimeZone.UTC);
//        System.out.println("nd="+nd);
//        DateTime nt = new DateTime(new Long(dtt.getMillis())).withZone(DateTimeZone.UTC);
//        System.out.println("nt="+nt);
//
//        System.out.println("txt="+fmtd.print(nd));
//        System.out.println("txt="+fmtt.print(nt));
//
//        DateTime n1 = new DateTime(DateTimeZone.UTC).minusDays(3);
//        System.out.println("n1="+n1);
//
//        LocalDate ld = new LocalDate();
//        System.out.println("ld="+ld);
//        DateTime x = ld.toDateTimeAtMidnight(DateTimeZone.UTC);
//        System.out.println("  ="+x+" ; "+x.getMillis());
//
//        DateTime now = new DateTime(DateTimeZone.UTC);
//        System.out.println("now="+now);
//        DateMidnight nowDate = now.toDateMidnight();
//        System.out.println("nowDate="+nowDate+" ; "+nowDate.getMillis());
//        TimeOfDay nowTime = now.toTimeOfDay();
//        System.out.println("nowTime="+nowTime);
//        System.out.println("nowTime="+fmtt.print(nowTime));
//        System.out.println("nowTime="+fmtt.print(now));
//
//        LocalDate localDate = new LocalDate(2006, 8, 1).minusDays(0);
//        String s2 = DateFun.FORMAT_DATE.print(localDate);
//        System.out.println("s2="+s2);
//
//        System.out.println("s1="+new LocalDate());
//        System.out.println("s2="+new LocalDate(DateTimeZone.UTC).toDateMidnight());
//        System.out.println("s3="+new LocalDate().toDateMidnight(DateTimeZone.UTC));
//        System.out.println("s4="+new LocalDate(DateTimeZone.UTC).toDateMidnight(DateTimeZone.UTC));
//    }

    /**
     * Creates a java.sql.Time object from provided string in format "hh:mm:ssGMT".
     */
//    public static java.sql.Time getSqlTimeFromString(String timeStr) {
//        String hours = timeStr.substring(0, 2);
//        String minutes = timeStr.substring(3, 5);
//        String seconds = timeStr.substring(6, 8);
//        int ihours = -1;
//        int iminutes = -1;
//        int iseconds = -1;
//        try {
//            ihours = Integer.parseInt( hours );
//            iminutes = Integer.parseInt( minutes );
//            iseconds = Integer.parseInt( seconds );
//        } catch(Exception ex) {
//            logger.warning("Could not parse the time");
//            return null;
//        }
//        if( ihours < 0 || ihours > 23 ||
//            iminutes < 0 || iminutes > 59 ||
//            iseconds < 0 || iseconds > 59 )
//        {
//            logger.warning("Time is invalid");
//            return null;
//        }
//        Calendar cal = Calendar.getInstance();
//        cal.clear();
//        cal.set(1970, 0, 1, ihours, iminutes, iseconds);
//        return new java.sql.Time(cal.getTime().getTime());
//    }

//    public static String getExtendedTimeFromSqlTime(java.sql.Time time) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(time.getTime());
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        int second = cal.get(Calendar.SECOND);
//        StringBuilder sb = new StringBuilder(12);
//        if( hour<10 )
//            sb.append('0');
//        sb.append(hour).append(':');
//        if( minute < 10 )
//            sb.append('0');
//        sb.append(minute).append(':');
//        if( second < 10 )
//            sb.append('0');
//        sb.append(second);
//        sb.append("GMT");
//        return sb.toString();
//    }

//    public static String getExtendedDateFromSqlDate(java.sql.Date date) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(date.getTime());
//        return getExtendedDateOfCalendar(cal);
////        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
////        cal.setTimeInMillis(date.getTime());
////        return getExtendedDateOfCalendar(cal);
//    }

    /**
     * Returns date
     * @return Date as String yyyy.m.d in GMT without leading zeros
     */
//    public static String getDate() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        return getDateOfCalendar(cal);
//    }

    /**
     * Returns date with leading zeroes
     * @return Date as String yyyy.MM.dd in GMT with leading zeros
     */
//    public static String getExtendedDate() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        return getExtendedDateOfCalendar(cal);
//    }

    /**
     * Returns date with leading zeroes
     * @return Date as String yyyy.MM.dd in GMT with leading zeros
     */
    public static String getExtendedDateFromMillis(final long millis) {
        return FORMAT_DATE_EXT.print(millis);
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        cal.setTimeInMillis(millis);
//        return getExtendedDateOfCalendar(cal);
    }

    /**
     * Returns date -n days.
     * @return Date as String yyyy.m.d in GMT without leading zeros
     */
//    public static String getDate(int daysAgo) {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        cal.add(Calendar.DATE,-daysAgo);
//        return getDateOfCalendar(cal);
//    }

    /**
     * Returns date -n days.
     * @return Date as String yyyy.mm.dd in GMT WITH leading zeros
     */
//    public static String getExtendedDate(int daysAgo) {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        cal.add(Calendar.DATE,-daysAgo);
//        return getExtendedDateOfCalendar(cal);
//    }

    /**
     * Returns date with leading zeroes
     * @return Date as String dd.mm.yyyy in GMT with leading zeros
     */
//    public static String getVisibleExtendedDate() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH) + 1;
//        int day = cal.get(Calendar.DATE);
//        StringBuilder sb = new StringBuilder(11);
//        if( day < 10 )
//            sb.append('0');
//        sb.append(day).append('.');
//        if( month < 10 )
//            sb.append('0');
//        sb.append(month).append('.');
//        sb.append(year);
//        return sb.toString();
//    }

    /**
     * Returns time
     * @return Time as String h:m:s in GMT without leading zeros
     */
//    public static String getTime() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        return new StringBuilder(9).append(cal.get(Calendar.HOUR_OF_DAY)).append(':')
//        .append(cal.get(Calendar.MINUTE)).append(':').append(cal.get(Calendar.SECOND)).toString();
//    }

    /**
     * Returns time with leading zeroes
     * @return Time as String h:mm:ss in GMT with leading zeros
     */
//    public static String getExtendedTime() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        return getExtendedTimeOfCalendar(cal);
//    }

//    public static String getExtendedTimeOfCalendar(Calendar cal) {
//
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        int second = cal.get(Calendar.SECOND);
//        StringBuilder sb = new StringBuilder(9);
//        //    if( hour<10 )  // commented out to keep old behaviour, see below
//        //        sb.append('0');
//        sb.append(hour).append(':');
//        if( minute < 10 )
//            sb.append('0');
//        sb.append(minute).append(':');
//        if( second < 10 )
//            sb.append('0');
//        sb.append(second);
//        return sb.toString();
//        /*
//            String sHour = String.valueOf(hour);
//            String sMinute = String.valueOf(minute);
//            String sSecond = String.valueOf(second);
//            if (hour < 10)
//                sHour = "0" + sHour;
//            if (minute < 10)
//                sMinute = "0" + sMinute;
//            if (second < 10)
//                sSecond = "0" + sSecond;
//            return hour + ":" + sMinute +":" + sSecond; <-- bug or feature? uses hour instead of sHour as prepared
//        */
//    }
    /**
     * Returns time with leading zeroes
     * @return Time as String hh:mm:ss in GMT with leading zeros
     *
     * **** getExtendedDate() returns h:mm:ss, this returns the correct hh:mm:ss
     *
     */
//    public static String getFullExtendedTime() {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        int second = cal.get(Calendar.SECOND);
//        StringBuilder sb = new StringBuilder(9);
//        if( hour<10 )
//            sb.append('0');
//        sb.append(hour).append(':');
//        if( minute < 10 )
//            sb.append('0');
//        sb.append(minute).append(':');
//        if( second < 10 )
//            sb.append('0');
//        sb.append(second);
//        return sb.toString();
//    }

    /**
     * Converts a String with format: DATE.MONTH.YEAR HOUR:MINUTE:SECONDGMT
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
//    public static Calendar getCalendarFromDateAndTime(String text) {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        if( text.indexOf(' ') != -1 ) {
//            String date = text.substring(0, text.indexOf(' '));
//            int firstPoint = date.indexOf('.');
//            int secondPoint = date.lastIndexOf('.');
//            if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
//                int year = Integer.parseInt(date.substring(0, firstPoint));
//                int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
//                int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
//                cal.set(Calendar.YEAR, year);
//                cal.set(Calendar.MONTH, month - 1);
//                cal.set(Calendar.DATE, day);
//                logger.fine("TOF Date: " + year + "." + month + "." + day);
//            }
//        }
//        return cal;
//    }

    /**
     * Converts a String with format: YEAR.MONTH.DATE
     * to a Calendar object.
     * @param text the String to convert to a Calendar object
     */
//    public static Calendar getCalendarFromDate(String date) throws NumberFormatException {
//        GregorianCalendar cal = new GregorianCalendar();
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        int firstPoint = date.indexOf('.');
//        int secondPoint = date.lastIndexOf('.');
//        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
//            int year = Integer.parseInt(date.substring(0, firstPoint));
//            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
//            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
//            cal.set(Calendar.YEAR, year);
//            cal.set(Calendar.MONTH, month - 1);
//            cal.set(Calendar.DATE, day);
//        } else {
//            throw new NumberFormatException("Invalid date: "+date);
//        }
//        return cal;
//    }

    /**
     * Returns the date from Calendar as String with format yyyy.m.d
     */
//    public static String getDateOfCalendar(Calendar calDL) {
//        String date = new StringBuilder(11).append(calDL.get(Calendar.YEAR)).append('.')
//                      .append(calDL.get(Calendar.MONTH) + 1).append('.')
//                      .append(calDL.get(Calendar.DATE)).toString();
//        return date;
//    }

    /**
     * Returns the date from Calendar as String with format yyyy.mm.dd
     */
//    public static String getExtendedDateOfCalendar(Calendar cal) {
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH) + 1;
//        int day = cal.get(Calendar.DATE);
//        StringBuilder sb = new StringBuilder(11);
//        sb.append(year).append('.');
//        if( month < 10 )
//            sb.append('0');
//        sb.append(month).append('.');
//        if( day < 10 )
//            sb.append('0');
//        sb.append(day);
//        return sb.toString();
//    }

    /**
     * 2005.9.3 -> 2005.09.03 (for comparisions)
     */
//    public static String buildExtendedDate(String date) {
//        int firstPoint = date.indexOf('.');
//        int secondPoint = date.lastIndexOf('.');
//        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint ) {
//            int year = Integer.parseInt(date.substring(0, firstPoint));
//            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
//            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
//            StringBuilder sb = new StringBuilder(11);
//            sb.append(year).append('.');
//            if( month < 10 )
//                sb.append('0');
//            sb.append(month).append('.');
//            if( day < 10 )
//                sb.append('0');
//            sb.append(day);
//            return sb.toString();
//        }
//        return null;
//    }
}
