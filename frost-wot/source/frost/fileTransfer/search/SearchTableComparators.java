/*
  SearchTableComparators.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer.search;

import java.util.*;

public class SearchTableComparators {

    private final static FileNameComparator fileNameComparator = new FileNameComparator();
    private final static SizeComparator sizeComparator = new SizeComparator();
    private final static StateComparator stateComparator = new StateComparator();
    private final static LastUploadedComparator lastUploadedComparator = new LastUploadedComparator();
    private final static LastReceivedComparator lastReceivedComparator = new LastReceivedComparator();
    private final static RatingComparator ratingComparator = new RatingComparator();
    private final static CommentComparator commentComparator = new CommentComparator();
    private final static KeywordsComparator keywordsComparator = new KeywordsComparator();
    private final static SourcesComparator sourcesComparator = new SourcesComparator();

    private static class StateComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final int i1 = o1.getState();
            final int i2 = o2.getState();
            if( i1 < i2 ) {
                return -1;
            }
            if( i1 > i2 ) {
                return 1;
            }
            return 0;
        }
    }

    private static class SizeComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            return o1.getSize().compareTo(o2.getSize());
        }
    }

    private static class FileNameComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            return o1.getFilename().compareToIgnoreCase(o2.getFilename());
        }
    }

    private static class CommentComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final String comment1 = o1.getComment();
            final String comment2 = o2.getComment();
            return comment1.compareToIgnoreCase(comment2);
        }
    }

    private static class KeywordsComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final String keywords1 = o1.getKeywords();
            final String keywords2 = o2.getKeywords();
            return keywords1.compareToIgnoreCase(keywords2);
        }
    }

    private static class RatingComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final Integer rating1 = o1.getRating();
            final Integer rating2 = o2.getRating();
            return rating1.compareTo(rating2);
        }
    }

    private static class LastReceivedComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final long l1 = o1.getFrostFileListFileObject().getLastReceived();
            final long l2 = o2.getFrostFileListFileObject().getLastReceived();
            if( l1 < l2 ) {
                return -1;
            }
            if( l1 > l2 ) {
                return 1;
            }
            return 0;
        }
    }

    private static class LastUploadedComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final long l1 = o1.getFrostFileListFileObject().getLastUploaded();
            final long l2 = o2.getFrostFileListFileObject().getLastUploaded();
            if( l1 < l2 ) {
                return -1;
            }
            if( l1 > l2 ) {
                return 1;
            }
            return 0;
        }
    }

    private static class SourcesComparator implements Comparator<FrostSearchItem> {
        public int compare(final FrostSearchItem o1, final FrostSearchItem o2) {
            final Integer sources1 = o1.getSourceCount();
            final Integer sources2 = o2.getSourceCount();
            return sources1.compareTo(sources2);
        }
    }

    public static FileNameComparator getFileNameComparator() {
        return fileNameComparator;
    }

    public static SizeComparator getSizeComparator() {
        return sizeComparator;
    }

    public static StateComparator getStateComparator() {
        return stateComparator;
    }

    public static LastUploadedComparator getLastUploadedComparator() {
        return lastUploadedComparator;
    }

    public static LastReceivedComparator getLastReceivedComparator() {
        return lastReceivedComparator;
    }

    public static RatingComparator getRatingComparator() {
        return ratingComparator;
    }

    public static CommentComparator getCommentComparator() {
        return commentComparator;
    }

    public static KeywordsComparator getKeywordsComparator() {
        return keywordsComparator;
    }

    public static SourcesComparator getSourcesComparator() {
        return sourcesComparator;
    }
}
