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
package frost.gui.messagetreetable;

import java.util.*;

import frost.messages.*;

public class MessageTreeTableSortStateBean {

    private static boolean isThreaded;
    
    private final static int defaultSortedColumn = 6; // default: date
    private final static boolean defaultIsAscending = false; // default: descending
    
    private static int sortedColumn = 6; // default: date
    private static boolean isAscending = false; // default: descending
    
    public static boolean isAscending() {
        return isAscending;
    }
    public static void setAscending(boolean isAscending) {
        MessageTreeTableSortStateBean.isAscending = isAscending;
    }
    public static boolean isThreaded() {
        return isThreaded;
    }
    public static void setThreaded(boolean isThreaded) {
        MessageTreeTableSortStateBean.isThreaded = isThreaded;
    }
    public static int getSortedColumn() {
        return sortedColumn;
    }
    public static void setSortedColumn(int sortedColumn) {
        MessageTreeTableSortStateBean.sortedColumn = sortedColumn;
    }
    public static void setDefaults() {
        setSortedColumn(defaultSortedColumn);
        setAscending(defaultIsAscending);
    }
    
    public static Comparator getComparator(int column, boolean ascending) {
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

    private static Comparator[] ascendingComparators = new Comparator[] {
        flaggedComparatorAscending,
        starredComparatorAscending,
        subjectComparatorAscending,
        fromComparatorAscending,
        indexComparatorAscending,
        trustStateComparatorAscending,
        dateComparatorAscending
    };
    private static Comparator[] descendingComparators = new Comparator[] {
        flaggedComparatorDescending,
        starredComparatorDescending,
        subjectComparatorDescending,
        fromComparatorDescending,
        indexComparatorDescending,
        trustStateComparatorDescending,
        dateComparatorDescending
    };
    
    private static class DateComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public DateComparator(boolean ascending) {
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
        public int compare(Object o1, Object o2) {
            FrostMessageObject t1 = (FrostMessageObject)o1; 
            FrostMessageObject t2 = (FrostMessageObject)o2;
            
            long l1 = t1.getDateAndTime().getMillis();
            long l2 = t2.getDateAndTime().getMillis();
            if( l1 > l2 ) {
                return retvalGreater;
            }
            if( l1 < l2 ) {
                return retvalSmaller;
            }
            return 0;
        }
    }
    
    private static class SubjectComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public SubjectComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            String s1 = t1.getSubject();
            String s2 = t2.getSubject();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            int r = s1.toLowerCase().compareTo(s2.toLowerCase());
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

    private static class FromComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public FromComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            String s1 = t1.getFromName();
            String s2 = t2.getFromName();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            int r = s1.compareTo(s2);
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

    private static class TrustStateComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public TrustStateComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            String s1 = t1.getMessageStatusString();
            String s2 = t2.getMessageStatusString();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            int r = s1.compareTo(s2);
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

    private static class FlaggedComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public FlaggedComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            boolean s1 = t1.isFlagged();
            boolean s2 = t2.isFlagged();
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
    
    private static class StarredComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public StarredComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            boolean s1 = t1.isStarred();
            boolean s2 = t2.isStarred();
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
    
    private static class IndexComparator implements Comparator {
        private int retvalGreater;
        private int retvalSmaller;
        public IndexComparator(boolean ascending) {
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
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            int s1 = t1.getIndex();
            int s2 = t2.getIndex();
            if( s1 == s2 ) {
                return 0;
            } else if( s1 > s2 ) {
                return retvalGreater;
            } else {
                return retvalSmaller;
            }
        }
    }
}
