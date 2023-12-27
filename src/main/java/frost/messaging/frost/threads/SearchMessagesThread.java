/*
  SearchMessagesThread.java / Frost
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
package frost.messaging.frost.threads;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.MainFrame;
import frost.gui.SearchMessagesConfig;
import frost.gui.SearchMessagesDialog;
import frost.messaging.frost.FrostMessageObject;
import frost.messaging.frost.FrostSearchResultMessageObject;
import frost.messaging.frost.boards.Board;
import frost.storage.MessageCallback;
import frost.storage.perst.messagearchive.ArchiveMessageStorage;
import frost.storage.perst.messages.MessageStorage;
import frost.util.DateFun;
import frost.util.TextSearchFun;

public class SearchMessagesThread extends Thread implements MessageCallback {

	private static final Logger logger = LoggerFactory.getLogger(SearchMessagesThread.class);

    SearchMessagesDialog searchDialog; // used to add found messages
    SearchMessagesConfig searchConfig;

    private final TrustStates trustStates = new TrustStates();

    private boolean stopRequested = false;

    public SearchMessagesThread(final SearchMessagesDialog searchDlg, final SearchMessagesConfig searchCfg) {
        searchDialog = searchDlg;
        searchConfig = searchCfg;
    }

    @Override
    public void run() {

        try {
            // select board dirs
            List<Board> boardsToSearch;
            if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_DISPLAYED ) {
                boardsToSearch = MainFrame.getInstance().getMessagingTab().getTofTreeModel().getAllBoards();
            } else if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_CHOSED ) {
                boardsToSearch = searchConfig.chosedBoards;
            } else {
                boardsToSearch = Collections.emptyList(); // paranoia
            }

            final DateRange dateRange = new DateRange();

            for( final Board board : boardsToSearch ) {

                if( isStopRequested() ) {
                    break;
                }

                // build date and trust state info for this board
                updateDateRangeForBoard(board, dateRange);
                updateTrustStatesForBoard(board, trustStates);

                searchBoard(board, dateRange);

                if( isStopRequested() ) {
                    break;
                }
            }
        } catch(final Throwable t) {
            logger.error("Catched exception:", t);
        }
        searchDialog.notifySearchThreadFinished();
    }


    public boolean messageRetrieved(final FrostMessageObject mo) {
        // search this xml file
        searchMessage(mo);

        return isStopRequested();
    }

    // Format: boards\2006.3.1\2006.3.1-boards-0.xml
    private void searchBoard(final Board board, final DateRange dr) {
        logger.debug("startDate = {}", dr.startDate);
        logger.debug("endDate = {}", dr.endDate);
        if( searchConfig.searchInKeypool ) {
            try {
                // if we search displayed messages, we must search all new and flagged/starred too
                final boolean retrieveDisplayedMessages = (searchConfig.searchDates == SearchMessagesConfig.DATE_DISPLAYED);
                MessageStorage.inst().retrieveMessagesForSearch(
                        board,
                        dr.startDate,
                        dr.endDate,
                        retrieveDisplayedMessages,
                        (((searchConfig.content==null)||(searchConfig.content.size()==0))?false:true), // withContent
                        false, // withAttachment
                        false, // showDeleted
                        this);
            } catch(final Throwable e) {
                logger.error("Catched exception during getMessageTable().retrieveMessagesForSearch:", e);
            }
        }
        if( searchConfig.searchInArchive ) {
            try {
                ArchiveMessageStorage.inst().retrieveMessagesForSearch(
                        board,
                        dr.startDate,
                        dr.endDate,
                        this);
            } catch(final Throwable e) {
                logger.error("Catched exception during getMessageArchiveTable().retrieveMessagesForSearch:", e);
            }
        }
    }

    private void searchMessage(final FrostMessageObject mo) {

        // check private, flagged, starred, replied only
        if( searchConfig.searchPrivateMsgsOnly != null ) {
            if( (mo.getRecipientName() == null) || (mo.getRecipientName().length() == 0) ) {
                return;
            }
        }
        if( searchConfig.searchFlaggedMsgsOnly != null ) {
            if( mo.isFlagged() != searchConfig.searchFlaggedMsgsOnly.booleanValue() ) {
                return;
            }
        }
        if( searchConfig.searchStarredMsgsOnly != null ) {
            if( mo.isStarred() != searchConfig.searchStarredMsgsOnly.booleanValue() ) {
                return;
            }
        }
        if( searchConfig.searchRepliedMsgsOnly != null ) {
            if( mo.isReplied() != searchConfig.searchRepliedMsgsOnly.booleanValue() ) {
                return;
            }
        }

        // check trust states
        if( matchesTrustStates(mo, trustStates) == false ) {
            return;
        }

        // check attachments
        if( searchConfig.msgMustContainBoards && !mo.hasBoardAttachments() ) {
            return;
        }
        if( searchConfig.msgMustContainFiles && !mo.hasFileAttachments() ) {
            return;
        }

        if( !matchText(mo.getFromName(), searchConfig.senderMakeLowercase, searchConfig.sender, searchConfig.notSender) ) {
            return;
        }

        if( !matchText(mo.getSubject(), searchConfig.subjectMakeLowercase, searchConfig.subject, searchConfig.notSubject) ) {
            return;
        }

        if( !searchConfig.content.isEmpty() || !searchConfig.notContent.isEmpty() ) {
            if( !matchText(mo.getContent(), searchConfig.contentMakeLowercase, searchConfig.content, searchConfig.notContent) ) {
                return;
            }
        }

        // match, add to result table
        searchDialog.addFoundMessage(new FrostSearchResultMessageObject(mo));
    }

    /**
     * @return  true if text was accepted, false if not
     */
    private boolean matchText(
            final String origText,
            final boolean makeLowerCase,
            final List<String> strings,
            final List<String> notStrings)
    {

        if( !notStrings.isEmpty() || !strings.isEmpty() ) {
            String text;
            if( makeLowerCase ) {
                text = origText.toLowerCase();
            } else {
                text = origText;
            }

            // check NOT strings
            if( !notStrings.isEmpty() ) {
                if( TextSearchFun.containsAnyString(text, notStrings) ) {
                    return false;
                }
            }
            // check strings
            if( !strings.isEmpty() ) {
                if( !TextSearchFun.containsEachString(text, strings) ) {
                    return false;
                }
            }
        }
        return true; // ok!
    }

    private boolean matchesTrustStates(final FrostMessageObject msg, final TrustStates ts) {

        if( msg.isMessageStatusGOOD() && (ts.trust_good == false) ) {
            return false;
        }
        if( msg.isMessageStatusOBSERVE() && (ts.trust_observe == false) ) {
            return false;
        }
        if( msg.isMessageStatusCHECK() && (ts.trust_check == false) ) {
            return false;
        }
        if( msg.isMessageStatusBAD() && (ts.trust_bad == false) ) {
            return false;
        }
        if( msg.isMessageStatusOLD() && (ts.trust_none == false) ) {
            return false;
        }
        if( msg.isMessageStatusTAMPERED() && (ts.trust_tampered == false) ) {
            return false;
        }

        return true;
    }

    private void updateTrustStatesForBoard(final Board b, final TrustStates ts) {
        if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_ALL ) {
            // use all trust states
            ts.trust_good = true;
            ts.trust_observe = true;
            ts.trust_check = true;
            ts.trust_bad = true;
            ts.trust_none = true;
            ts.trust_tampered = true;
        } else if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_CHOSED ) {
            // use specified trust states
            ts.trust_good = searchConfig.trust_good;
            ts.trust_observe = searchConfig.trust_observe;
            ts.trust_check = searchConfig.trust_check;
            ts.trust_bad = searchConfig.trust_bad;
            ts.trust_none = searchConfig.trust_none;
            ts.trust_tampered = searchConfig.trust_tampered;
        } else if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_DISPLAYED ) {
            // use trust states configured for board
            ts.trust_good = true;
            ts.trust_observe = !b.getHideObserve();
            ts.trust_check = !b.getHideCheck();
            ts.trust_bad = !b.getHideBad();
            ts.trust_none = !b.getShowSignedOnly();
            ts.trust_tampered = !b.getShowSignedOnly();
        }
    }

    private void updateDateRangeForBoard(final Board b, final DateRange dr) {
		final DateTime nowLocalDate = new DateTime(DateFun.getTimeZone());
		final long todayMillis = nowLocalDate.plusDays(1).withTimeAtStartOfDay().getMillis();
        if( searchConfig.searchDates == SearchMessagesConfig.DATE_DISPLAYED ) {
			dr.startDate = nowLocalDate.minusDays(b.getMaxMessageDisplay()).withTimeAtStartOfDay().getMillis();
            dr.endDate = todayMillis;
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_DAYS_BACKWARD ) {
			dr.startDate = nowLocalDate.minusDays(searchConfig.daysBackward).withTimeAtStartOfDay().getMillis();
            dr.endDate = todayMillis;
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_BETWEEN_DATES ) {
			dr.startDate = new DateTime(searchConfig.startDate, DateFun.getTimeZone()).withTimeAtStartOfDay()
					.getMillis();
			dr.endDate = new DateTime(searchConfig.endDate, DateFun.getTimeZone()).plusDays(1).withTimeAtStartOfDay()
					.getMillis();
        } else {
            // all dates
            dr.startDate = 0;
            dr.endDate = todayMillis;
        }
    }

    public synchronized boolean isStopRequested() {
        return stopRequested;
    }
    public synchronized void requestStop() {
        stopRequested = true;
    }

    private class DateRange {
        long startDate;
        long endDate;
    }

    private class TrustStates {
        // current trust status to search into
        public boolean trust_good = false;
        public boolean trust_observe = false;
        public boolean trust_check = false;
        public boolean trust_bad = false;
        public boolean trust_none = false;
        public boolean trust_tampered = false;
    }
}
