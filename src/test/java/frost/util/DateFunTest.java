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

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.jupiter.api.Test;

public class DateFunTest {

	@Test
	public void testDateTime() {
		// Original object
		DateTime dateTimeOrg = new DateTime(2006, 1, 2, 3, 4, 5, 6, DateFun.getTimeZone());
		assertEquals("2006-01-02T03:04:05.006Z", dateTimeOrg.toString());
		assertEquals(1136171045006L, dateTimeOrg.getMillis());

		// Copy using original object
		DateTime dateTimeCopyObj = new DateTime(dateTimeOrg);
		assertEquals(dateTimeOrg.toString(), dateTimeCopyObj.toString());
		assertEquals(dateTimeOrg.getMillis(), dateTimeCopyObj.getMillis());

		// Copy using timestamp + timezone
		DateTime dateTimeCopyMillis = new DateTime(dateTimeOrg.getMillis(), DateFun.getTimeZone());
		assertEquals(dateTimeOrg.toString(), dateTimeCopyMillis.toString());
		assertEquals(dateTimeOrg.getMillis(), dateTimeCopyMillis.getMillis());

		// Plus days
		DateTime dateTimePlusDays = dateTimeOrg.plusDays(35);
		assertEquals("2006-02-06T03:04:05.006Z", dateTimePlusDays.toString());
		assertEquals(dateTimeOrg.getMillis() + Duration.standardDays(35).getMillis(), dateTimePlusDays.getMillis());

		// Minus days
		DateTime dateTimeMinusDays = dateTimePlusDays.minusDays(35);
		assertEquals(dateTimeOrg.toString(), dateTimeMinusDays.toString());
		assertEquals(dateTimePlusDays.getMillis() - Duration.standardDays(35).getMillis(),
				dateTimeMinusDays.getMillis());

		// Without time
		DateTime dateTimeWithoutTime = dateTimeOrg.withTimeAtStartOfDay();
		assertEquals("2006-01-02T00:00:00.000Z", dateTimeWithoutTime.toString());
		assertEquals(
				dateTimeOrg.getMillis() - Duration.standardHours(3).getMillis()
						- Duration.standardMinutes(4).getMillis() - Duration.standardSeconds(5).getMillis() - 6,
				dateTimeWithoutTime.getMillis());

		// Format
		assertEquals("2006.1.2", DateFun.FORMAT_DATE.print(dateTimeOrg));
		assertEquals("2006.01.02", DateFun.FORMAT_DATE_EXT.print(dateTimeOrg));
		assertEquals("02.01.2006", DateFun.FORMAT_DATE_VISIBLE.print(dateTimeOrg));
		assertEquals("03:04:05", DateFun.FORMAT_TIME_PLAIN.print(dateTimeOrg));
		assertEquals("3:4:5GMT", DateFun.FORMAT_TIME.print(dateTimeOrg));
		assertEquals("03:04:05GMT", DateFun.FORMAT_TIME_EXT.print(dateTimeOrg));
		assertEquals("03:04:05 GMT", DateFun.FORMAT_TIME_VISIBLE.print(dateTimeOrg));
		assertEquals("02.01.2006 03:04:05 GMT", DateFun.FORMAT_DATE_TIME_VISIBLE.print(dateTimeOrg));

		// Parse date
		DateTime dateTimeParsedDate = DateFun.FORMAT_DATE.parseDateTime("2006.1.2");
		assertEquals("2006-01-02T00:00:00.000Z", dateTimeParsedDate.toString());
		assertEquals(dateTimeWithoutTime.getMillis(), dateTimeParsedDate.getMillis());

		// Parse time
		DateTime dateTimeParsedTime = DateFun.FORMAT_TIME.parseDateTime("03:04:05GMT");
		assertEquals("1970-01-01T03:04:05.000Z", dateTimeParsedTime.toString());
		assertEquals(dateTimeOrg.getMillis() - dateTimeWithoutTime.getMillis() - 6, dateTimeParsedTime.getMillis());
	}
}
