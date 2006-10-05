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
package frost.threads;

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.gui.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

public class SearchMessagesThread extends Thread implements MessageDatabaseTableCallback {

    private static Logger logger = Logger.getLogger(SearchMessagesThread.class.getName());

    SearchMessagesDialog searchDialog; // used to add found messages
    SearchMessagesConfig searchConfig;

    private TrustStates trustStates = new TrustStates();

    private boolean stopRequested = false;

    public SearchMessagesThread(SearchMessagesDialog searchDlg, SearchMessagesConfig searchCfg) {
        searchDialog = searchDlg;
        searchConfig = searchCfg;
    }

    public void run() {

        try {
            // select board dirs
            List boardsToSearch;
            if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_DISPLAYED ) {
                boardsToSearch = MainFrame.getInstance().getTofTreeModel().getAllBoards();
            } else if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_CHOSED ) {
                boardsToSearch = searchConfig.chosedBoards;
            } else {
                boardsToSearch = new ArrayList(); // paranoia
            }

            DateRange dateRange = new DateRange();

            for(Iterator i=boardsToSearch.iterator(); i.hasNext(); ) {

                if( isStopRequested() ) {
                    break;
                }

                Board board = (Board)i.next();

                // build date and trust state info for this board
                updateDateRangeForBoard(board, dateRange);
                updateTrustStatesForBoard(board, trustStates);
                
                searchBoard(board, dateRange);

                if( isStopRequested() ) {
                    break;
                }
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Catched exception:", t);
        }
        searchDialog.notifySearchThreadFinished();
    }
    
    
    public boolean messageRetrieved(FrostMessageObject mo) {
        // search this xml file
        searchMessage(mo);

        return isStopRequested();
    }

    // Format: boards\2006.3.1\2006.3.1-boards-0.xml
    private void searchBoard(Board board, DateRange dr) {
System.out.println("startDate="+dr.startDate);
System.out.println("endDate="+dr.endDate);
        if( searchConfig.searchInKeypool ) {
            try {
                AppLayerDatabase.getMessageTable().retrieveMessagesForSearch(
                        board, 
                        dr.startDate, 
                        dr.endDate, 
                        ((searchConfig.content==null)?false:true), // withContent
                        false, // withAttachment
                        false, // showDeleted
                        this);
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Catched exception during getMessageTable().retrieveMessagesForSearch:", e);
            }
        }
        if( searchConfig.searchInArchive ) {
            try {
                AppLayerDatabase.getMessageArchiveTable().retrieveMessagesForSearch(
                        board, 
                        dr.startDate, 
                        dr.endDate, 
                        false, // showDeleted
                        this);
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Catched exception during getMessageArchiveTable().retrieveMessagesForSearch:", e);
            }
        }
    }

    private void searchMessage(FrostMessageObject mo) {

        // check private, flagged, starred, replied only
        if( searchConfig.searchPrivateMsgsOnly != null ) {
            if( mo.getRecipientName() == null || mo.getRecipientName().length() == 0 ) {
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
        if( searchConfig.msgMustContainBoards && !mo.isHasBoardAttachments() ) {
            return;
        }
        if( searchConfig.msgMustContainFiles && !mo.isHasFileAttachments() ) {
            return;
        }

        // check sender
        if( searchConfig.sender != null ) {
            if( searchInText(searchConfig.sender, mo.getFromName()) == false ) {
                // sender not found
                return;
            }
        }

        // check subject
        if( searchConfig.subject != null ) {
            if( searchInText(searchConfig.subject, mo.getSubject()) == false ) {
                // subject not found
                return;
            }
        }

        // check content
        if( searchConfig.content != null ) {
            if( searchInText(searchConfig.content, mo.getContent()) == false ) {
                // content not found
                return;
            }
        }

        // match, add to result table
        searchDialog.addFoundMessage(new FrostSearchResultMessageObject(mo));
    }

    private boolean searchInText(List searchItems, String txt) {
        boolean found = false;
        txt = txt.toLowerCase(); // search items are already lowercase
        for(Iterator i=searchItems.iterator(); i.hasNext(); ) {
            String item = (String)i.next();
            if( txt.indexOf(item) > -1 ) {
                found = true;
                break; // one match is enough
            }
        }
        return found;
    }

    private boolean matchesTrustStates(FrostMessageObject msg, TrustStates ts) {

        if( msg.isMessageStatusGOOD() && ts.trust_good == false ) {
            return false;
        }
        if( msg.isMessageStatusOBSERVE() && ts.trust_observe == false ) {
            return false;
        }
        if( msg.isMessageStatusCHECK() && ts.trust_check == false ) {
            return false;
        }
        if( msg.isMessageStatusBAD() && ts.trust_bad == false ) {
            return false;
        }
        if( msg.isMessageStatusOLD() && ts.trust_none == false ) {
            return false;
        }
        if( msg.isMessageStatusTAMPERED() && ts.trust_tampered == false ) {
            return false;
        }

        return true;
    }

    private void updateTrustStatesForBoard(Board b, TrustStates ts) {
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

    private void updateDateRangeForBoard(Board b, DateRange dr) {
        if( searchConfig.searchDates == SearchMessagesConfig.DATE_DISPLAYED ) {
            dr.startDate = DateFun.getSqlDateGMTDaysAgo(b.getMaxMessageDisplay());
            dr.endDate = DateFun.getCurrentSqlDateGMT(); // today
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_DAYS_BACKWARD ) {
            dr.startDate = DateFun.getSqlDateGMTDaysAgo(searchConfig.daysBackward);
            dr.endDate = DateFun.getCurrentSqlDateGMT(); // today
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_BETWEEN_DATES ) {
            dr.startDate = DateFun.getSqlDateOfCalendar(searchConfig.startDate);
            dr.endDate = DateFun.getSqlDateOfCalendar(searchConfig.endDate);
        } else {
            // all dates
            dr.startDate = new java.sql.Date(1);
            dr.endDate = DateFun.getCurrentSqlDateGMT(); // today
        }
    }

    public synchronized boolean isStopRequested() {
        return stopRequested;
    }
    public synchronized void requestStop() {
        stopRequested = true;
    }

    private class DateRange {
        java.sql.Date startDate;
        java.sql.Date endDate;
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
