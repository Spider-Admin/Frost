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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateFun {

	private static ZoneOffset timeZone = ZoneOffset.UTC;

	public static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy.M.d").withZone(getTimeZone());
	public static final DateTimeFormatter FORMAT_DATE_EXT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
			.withZone(getTimeZone());
	public static final DateTimeFormatter FORMAT_DATE_VISIBLE = DateTimeFormatter.ofPattern("dd.MM.yyyy")
			.withZone(getTimeZone());

	public static final DateTimeFormatter FORMAT_TIME_PLAIN = DateTimeFormatter.ofPattern("HH:mm:ss")
			.withZone(getTimeZone());
	public static final DateTimeFormatter FORMAT_TIME = DateTimeFormatter.ofPattern("H:m:s'GMT'")
			.withZone(getTimeZone());
	public static final DateTimeFormatter FORMAT_TIME_EXT = DateTimeFormatter.ofPattern("HH:mm:ss'GMT'")
			.withZone(getTimeZone());
	public static final DateTimeFormatter FORMAT_TIME_VISIBLE = DateTimeFormatter.ofPattern("HH:mm:ss' GMT'")
			.withZone(getTimeZone());

	public static final DateTimeFormatter FORMAT_DATE_TIME_VISIBLE = DateTimeFormatter
			.ofPattern("dd.MM.yyyy HH:mm:ss' GMT'").withZone(getTimeZone());

	public static ZoneOffset getTimeZone() {
		return timeZone;
	}

	public static long toMilli(OffsetDateTime dateTime) {
		return dateTime.toInstant().toEpochMilli();
	}

	public static OffsetDateTime toOffsetDateTime(long milli, ZoneOffset timeZone) {
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(milli), timeZone);
	}

	public static long toStartOfDayInMilli(OffsetDateTime dateTime) {
		return toMilli(toStartOfDay(dateTime));
	}

	public static long toStartOfDayInMilli(long milli, ZoneOffset timeZone) {
		return toStartOfDayInMilli(toOffsetDateTime(milli, timeZone));
	}

	public static OffsetDateTime toStartOfDay(OffsetDateTime dateTime) {
		return dateTime.truncatedTo(ChronoUnit.DAYS);
	}

	public static OffsetDateTime parseDate(String value, DateTimeFormatter formatter, ZoneOffset timeZone) {
		return OffsetDateTime.of(LocalDate.parse(value, formatter), LocalTime.MIDNIGHT, timeZone);
	}

	public static OffsetDateTime parseTime(String value, DateTimeFormatter formatter, ZoneOffset timeZone) {
		return OffsetDateTime.of(LocalDate.EPOCH, LocalTime.parse(value, formatter), timeZone);
	}

	/**
	 * Returns date with leading zeroes
	 * 
	 * @return Date as String yyyy.MM.dd in GMT with leading zeros
	 */
	public static String getExtendedDateFromMillis(final long millis) {
		return FORMAT_DATE_EXT.format(Instant.ofEpochMilli(millis));
	}
}
