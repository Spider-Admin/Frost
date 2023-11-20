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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import frost.messaging.frost.boards.Board;
import frost.util.TextSearchFun;
import frost.util.TextSearchFun.SPLIT;

/**
 * This class contains all configured search options.
 */
public class SearchMessagesConfig {

    private static final List<String> emptyList = Collections.emptyList();

    public boolean senderMakeLowercase = true;
    public boolean subjectMakeLowercase = true;
    public boolean contentMakeLowercase = true;

    public List<String> sender = emptyList; // List of String
    public List<String> subject = emptyList; // List of String
    public List<String> content = emptyList; // List of String

    public List<String> notSender = emptyList; // List of String
    public List<String> notSubject = emptyList; // List of String
    public List<String> notContent = emptyList; // List of String

    public Boolean searchPrivateMsgsOnly = null;
    public Boolean searchFlaggedMsgsOnly = null;
    public Boolean searchStarredMsgsOnly = null;
    public Boolean searchRepliedMsgsOnly = null;

    public static final int BOARDS_DISPLAYED  = 1;
//    public static final int BOARDS_EXISTING_DIRS = 2;
    public static final int BOARDS_CHOSED = 3;
    public int searchBoards = 0;
    public List<Board> chosedBoards = null; // list of Board objects

    public static final int DATE_DISPLAYED = 1;
    public static final int DATE_ALL = 2;
    public static final int DATE_BETWEEN_DATES = 3;
    public static final int DATE_DAYS_BACKWARD = 4;
    public int searchDates;
    public long startDate, endDate;
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

    public boolean msgMustContainBoards = false;
    public boolean msgMustContainFiles = false;

	public void setSenderString(final String s, final boolean makeLowerCase) {
		senderMakeLowercase = makeLowerCase;
		final EnumMap<SPLIT, ArrayList<String>> res = TextSearchFun.splitStrings(s, makeLowerCase);
		sender = res.get(SPLIT.SEARCH);
		notSender = res.get(SPLIT.NOT_SEARCH);
	}

	public void setSubjectString(final String s, final boolean makeLowerCase) {
		subjectMakeLowercase = makeLowerCase;
		final EnumMap<SPLIT, ArrayList<String>> res = TextSearchFun.splitStrings(s, makeLowerCase);
		subject = res.get(SPLIT.SEARCH);
		notSubject = res.get(SPLIT.NOT_SEARCH);
	}

	public void setContentString(final String s, final boolean makeLowerCase) {
		contentMakeLowercase = makeLowerCase;
		final EnumMap<SPLIT, ArrayList<String>> res = TextSearchFun.splitStrings(s, makeLowerCase);
		content = res.get(SPLIT.SEARCH);
		notContent = res.get(SPLIT.NOT_SEARCH);
	}
}
