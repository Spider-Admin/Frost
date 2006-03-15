/*
  SearchMessagesThread.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.*;
import frost.gui.objects.*;

public class SearchMessagesThread extends Thread {

    private static Logger logger = Logger.getLogger(SearchMessagesThread.class.getName());

    SearchMessagesDialog searchDialog; // used to add found messages
    SearchMessagesConfig searchConfig;
    
    private boolean stopRequested = false;
    
    String keypoolDir;
    String archiveDir;
    
    TrustStates trustStates = new TrustStates();
    
    
    public SearchMessagesThread(SearchMessagesDialog searchDlg, SearchMessagesConfig searchCfg) {
        searchDialog = searchDlg;
        searchConfig = searchCfg;
        
        keypoolDir = MainFrame.keypool;
        archiveDir = Core.frostSettings.getValue("archive.dir");
        if( archiveDir == null || archiveDir.length() == 0 ) {
            logger.severe("Warning: no ARCHIVE DIR specified!");
            archiveDir = null;
        }
        archiveDir += ("messages" + File.separator);
    }
    
    public void run() {
        
        // select board dirs
        List boardsToSearch;
        if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_DISPLAYED ) {
            boardsToSearch = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        } else if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_DISPLAYED ) {
            boardsToSearch = searchConfig.chosedBoards;
        } else {
            return;
        }
        
        for(Iterator i=boardsToSearch.iterator(); i.hasNext(); ) {
            
            Board board = (Board)i.next();
            
            // build date and trust state info for this board
            updatelTrustStatesToSearchInto(board, trustStates);
            
            if( searchConfig.searchInKeypool ) {
                // search in keypool
                // Format: keypool\boards\2006.3.1\2006.3.1-boards-0.xml
                File boardFolder = new File(keypoolDir + board.getBoardFilename());
                if( boardFolder.isDirectory() == true ) {


                    String date = "2006.1.1";
                    try {
                        Calendar currCal = DateFun.getCalendarFromDate(date);
                    } catch(NumberFormatException ex) {
                        logger.warning("Incorrect date: "+date);
                    }
                    
                    
                } else {
                    logger.warning("No board folder in keypool for board "+board.getName());
                }
            }
            if( searchConfig.searchInArchive && archiveDir != null ) {
                // search in archive
                // Format: keypool-archive.j\messages\boards\2005.12.7\2005.12.7-boards-0.xml
                File boardFolder = new File(archiveDir + board.getBoardFilename());
                if( boardFolder.isDirectory() == true ) {

                } else {
                    logger.warning("No board folder in archive for board "+board.getName());
                }
            }

        }
        
        // scan for dates in dirs
        // instanciate each xml file and search content
        
        /*
        String minDate = DateFun.getExtendedDate(daysOld);

        File[] boardFolderFiles = boardFolder.listFiles();
        if( boardFolderFiles == null ) {
            logger.severe("Could not get list of files for folder "+boardFolder.getPath());
            return 0;
        }
        for(int x=0; x < boardFolderFiles.length; x++) {
            File boardFolderFile = boardFolderFiles[x];
            if( boardFolderFile.isDirectory() ) {
                String boardDateFolder = boardFolderFile.getName(); // "2005.9.1"
                String extDate = DateFun.buildExtendedDate(boardDateFolder); // "2005.09.01"
                if( extDate == null ) {
                    continue;
                }
                if( extDate.compareTo( minDate ) < 0 ) {
                    // expired date folder
                    // process all contained ".xml" files
                    File[] boardDateFolderFiles = boardFolderFile.listFiles();
                    if( boardDateFolderFiles == null ) {
                        logger.severe("Could not get list of files for folder "+boardFolderFile.getPath());
                        return 0;
                    }
                    for(int y=0; y < boardDateFolderFiles.length; y++) {
                        File boardDateFolderFile = boardDateFolderFiles[y];
                        if( boardDateFolderFile.isFile() && boardDateFolderFile.getName().endsWith(".xml") ) {
                            // process this expired message
        
        */
    }
    
    private void updatelTrustStatesToSearchInto(Board b, TrustStates ts) {
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
    
    public synchronized boolean isStopRequested() {
        return stopRequested;
    }
    public synchronized void requestStop() {
        stopRequested = true;
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
