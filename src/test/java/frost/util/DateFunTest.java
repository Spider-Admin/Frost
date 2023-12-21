/*
  DateFunTest.java / Frost
  Copyright (C) 2023  Frost Project <jtcfrost.sourceforge.net>

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.TimeOfDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class DateFunTest {

	@Test
	public void printDates() {
		String date = "2006.10.14";
		String time = "12:13:14GMT";

		DateTimeFormatter fmtd = DateTimeFormat.forPattern("yyyy.MM.dd");
		DateTimeFormatter fmtt = DateTimeFormat.forPattern("HH:mm:ss'GMT'");

		DateTime dtd = fmtd.withZone(DateTimeZone.UTC).parseDateTime(date);
		assertEquals("2006-10-14T00:00:00.000Z", dtd.toString());
		assertEquals(1160784000000L, dtd.getMillis());

		DateTime dtt = fmtt.withZone(DateTimeZone.UTC).parseDateTime(time);
		assertEquals("1970-01-01T12:13:14.000Z", dtt.toString());
		assertEquals(43994000L, dtt.getMillis());

		Long allMillis = dtd.getMillis() + dtt.getMillis();
		DateTime adt = new DateTime(allMillis, DateTimeZone.UTC);
		assertEquals("2006-10-14T12:13:14.000Z", adt.toString());
		assertEquals(1160827994000L, adt.getMillis());

		DateTime nd = new DateTime(dtd, DateTimeZone.UTC);
		assertEquals("2006-10-14T00:00:00.000Z", nd.toString());
		assertEquals(dtd.getMillis(), nd.getMillis());
		assertEquals("2006.10.14", fmtd.print(nd));

		DateTime nt = new DateTime(dtt, DateTimeZone.UTC);
		assertEquals("1970-01-01T12:13:14.000Z", nt.toString());
		assertEquals(dtt.getMillis(), nt.getMillis());
		assertEquals("12:13:14GMT", fmtt.print(nt));

		DateTime n1 = new DateTime(adt, DateTimeZone.UTC).minusDays(3);
		assertEquals("2006-10-11T12:13:14.000Z", n1.toString());

		LocalDate ld = new LocalDate(adt);
		assertEquals("2006-10-14", ld.toString());

		DateTime x = ld.toDateTimeAtMidnight(DateTimeZone.UTC);
		assertEquals("2006-10-14T00:00:00.000Z", x.toString());
		assertEquals(1160784000000L, x.getMillis());

		DateTime now = new DateTime(adt, DateTimeZone.UTC);
		assertEquals("2006-10-14T12:13:14.000Z", now.toString());
		assertEquals("12:13:14GMT", fmtt.print(now));

		DateMidnight nowDate = now.toDateMidnight();
		assertEquals("2006-10-14T00:00:00.000Z", nowDate.toString());
		assertEquals(1160784000000L, nowDate.getMillis());

		TimeOfDay nowTime = now.toTimeOfDay();
		assertEquals("T12:13:14.000", nowTime.toString());
		assertEquals("12:13:14GMT", fmtt.print(nowTime));

		LocalDate localDate = new LocalDate(adt).minusDays(0);
		assertEquals("2006.10.14", DateFun.FORMAT_DATE.print(localDate));
		assertEquals("2006-10-14", new LocalDate(adt).toString());
		assertEquals("2006-10-14T00:00:00.000+02:00", new LocalDate(adt, DateTimeZone.UTC).toDateMidnight().toString());
		assertEquals("2006-10-14T00:00:00.000Z", new LocalDate(adt).toDateMidnight(DateTimeZone.UTC).toString());
		assertEquals("2006-10-14T00:00:00.000Z",
				new LocalDate(adt, DateTimeZone.UTC).toDateMidnight(DateTimeZone.UTC).toString());
	}
}
