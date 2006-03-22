/*
  SearchMessagesConfig.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui;

import java.util.*;

/**
 * This class contains all configured search options.
 */
public class SearchMessagesConfig {

    public List sender = null; // List of String
    public List subject = null; // List of String
    public List content = null; // List of String
    public boolean searchPrivateMsgsOnly = false;

    public static final int BOARDS_DISPLAYED  = 1;
//    public static final int BOARDS_EXISTING_DIRS = 2;
    public static final int BOARDS_CHOSED = 3;
    public int searchBoards = 0;
    public List chosedBoards = null; // list of Board objects

    public static final int DATE_DISPLAYED = 1;
    public static final int DATE_ALL = 2;
    public static final int DATE_BETWEEN_DATES = 3;
    public static final int DATE_DAYS_BACKWARD = 4;
    public int searchDates;
    public GregorianCalendar startDate, endDate;
    public int daysBackward;

    public static final int TRUST_DISPLAYED = 1;
    public static final int TRUST_ALL = 2;
    public static final int TRUST_CHOSED = 3;
    public int searchTruststates = 0;
    public boolean trust_good = false;
    public boolean trust_observe = false;
    public boolean trust_check = false;
    public boolean trust_bad = false;
    public boolean trust_none = false;
    public boolean trust_tampered = false;

    public boolean searchInKeypool = false;
    public boolean searchInArchive = false;
}
