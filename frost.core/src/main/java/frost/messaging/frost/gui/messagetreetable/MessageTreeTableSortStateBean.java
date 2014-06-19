/*
  SortStateBean.java / Frost
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
package frost.messaging.frost.gui.messagetreetable;

import java.util.*;

import frost.messaging.frost.*;

public class MessageTreeTableSortStateBean {

    private static boolean isThreaded;

    private final static int defaultSortedColumn = MessageTreeTableModel.COLUMN_INDEX_DATE; // default: date
    private final static boolean defaultIsAscending = false; // default: descending

    private static int sortedColumn = MessageTreeTableModel.COLUMN_INDEX_DATE; // default: date
    private static boolean isAscending = false; // default: descending

    public static boolean isAscending() {
        return isAscending;
    }
    public static void setAscending(final boolean isAscending) {
        MessageTreeTableSortStateBean.isAscending = isAscending;
    }
    public static boolean isThreaded() {
        return isThreaded;
    }
    public static void setThreaded(final boolean isThreaded) {
        MessageTreeTableSortStateBean.isThreaded = isThreaded;
    }
    public static int getSortedColumn() {
        return sortedColumn;
    }
    public static void setSortedColumn(final int sortedColumn) {
        MessageTreeTableSortStateBean.sortedColumn = sortedColumn;
    }
    public static void setDefaults() {
        setSortedColumn(defaultSortedColumn);
        setAscending(defaultIsAscending);
    }

    public static Comparator<FrostMessageObject> getComparator(final int column, final boolean ascending) {
        if( ascending ) {
            return ascendingComparators[column];
        } else {
            return descendingComparators[column];
        }
    }

    // sorting for flat view
    private static FlaggedComparator flaggedComparatorAscending = new FlaggedComparator(true);
    private static FlaggedComparator flaggedComparatorDescending = new FlaggedComparator(false);

    private static StarredComparator starredComparatorAscending = new StarredComparator(true);
    private static StarredComparator starredComparatorDescending = new StarredComparator(false);

    private static JunkComparator junkComparatorAscending = new JunkComparator(true);
    private static JunkComparator junkComparatorDescending = new JunkComparator(false);

    private static SubjectComparator subjectComparatorAscending = new SubjectComparator(true);
    private static SubjectComparator subjectComparatorDescending = new SubjectComparator(false);

    private static FromComparator fromComparatorAscending = new FromComparator(true);
    private static FromComparator fromComparatorDescending = new FromComparator(false);

    private static TrustStateComparator trustStateComparatorAscending = new TrustStateComparator(true);
    private static TrustStateComparator trustStateComparatorDescending = new TrustStateComparator(false);

    public static DateComparator dateComparatorAscending = new DateComparator(true);
    public static DateComparator dateComparatorDescending = new DateComparator(false);

    public static IndexComparator indexComparatorAscending = new IndexComparator(true);
    public static IndexComparator indexComparatorDescending = new IndexComparator(false);

    @SuppressWarnings("unchecked")
    private static Comparator<FrostMessageObject>[] ascendingComparators = new Comparator[] {
        flaggedComparatorAscending,
        starredComparatorAscending,
        subjectComparatorAscending,
        fromComparatorAscending,
        indexComparatorAscending,
        junkComparatorAscending,
        trustStateComparatorAscending,
        dateComparatorAscending
    };
    @SuppressWarnings("unchecked")
    private static Comparator<FrostMessageObject>[] descendingComparators = new Comparator[] {
        flaggedComparatorDescending,
        starredComparatorDescending,
        subjectComparatorDescending,
        fromComparatorDescending,
        indexComparatorDescending,
        junkComparatorDescending,
        trustStateComparatorDescending,
        dateComparatorDescending
    };

    private static class DateComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public DateComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final long l1 = t1.getDateAndTime().getMillis();
            final long l2 = t2.getDateAndTime().getMillis();
            if( l1 > l2 ) {
                return retvalGreater;
            }
            if( l1 < l2 ) {
                return retvalSmaller;
            }
            return 0;
        }
    }

    private static class SubjectComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public SubjectComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final String s1 = t1.getSubject();
            final String s2 = t2.getSubject();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            final int r = s1.toLowerCase().compareTo(s2.toLowerCase());
            if( r == 0 ) {
                return r;
            }
            if( r > 0 ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class FromComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public FromComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final String s1 = t1.getFromName();
            final String s2 = t2.getFromName();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            final int r = s1.compareTo(s2);
            if( r == 0 ) {
                return r;
            }
            if( r > 0 ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class TrustStateComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public TrustStateComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final String s1 = t1.getMessageStatusString();
            final String s2 = t2.getMessageStatusString();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            final int r = s1.compareTo(s2);
            if( r == 0 ) {
                return r;
            }
            if( r > 0 ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class FlaggedComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public FlaggedComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final boolean s1 = t1.isFlagged();
            final boolean s2 = t2.isFlagged();
            if( s1 == s2 ) {
                return 0;
            }
            if( s1 == true ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class StarredComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public StarredComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final boolean s1 = t1.isStarred();
            final boolean s2 = t2.isStarred();
            if( s1 == s2 ) {
                return 0;
            }
            if( s1 == true ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class IndexComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public IndexComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final int s1 = t1.getIndex();
            final int s2 = t2.getIndex();
            if( s1 == s2 ) {
                return 0;
            } else if( s1 > s2 ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }

    private static class JunkComparator implements Comparator<FrostMessageObject> {
        private int retvalGreater;
        private int retvalSmaller;
        public JunkComparator(final boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        public int compare(final FrostMessageObject t1, final FrostMessageObject t2) {
            final boolean s1 = t1.isJunk();
            final boolean s2 = t2.isJunk();
            if( s1 == s2 ) {
                return 0;
            }
            if( s1 == true ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }
}
