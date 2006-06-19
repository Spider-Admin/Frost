/*
  SearchThread.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;

class SearchThread extends Thread {
    private FrostIdentities identities;
    private static Logger logger = Logger.getLogger(SearchThread.class.getName());
    private String request;
    private String board;
    private String date;
    private String keypool;
    private String searchType;
    private Vector results = new Vector();
    private boolean searchAllBoards;
    private Map chk = Collections.synchronizedMap(new TreeMap());
    private String[] audioExtension;
    private String[] videoExtension;
    private String[] imageExtension;
    private String[] documentExtension;
    private String[] executableExtension;
    private String[] archiveExtension;
    private Vector boards;
    private static String fileSeparator = System.getProperty("file.separator");
    int allFileCount;
    int maxSearchResults;
    private SearchPanel searchPanel = null;

    private SearchModel model;
    private DownloadModel downloadModel;
    private UploadModel uploadModel;

    private SettingsClass settings;

    /**
     * Splits a String into single parts
     * @return Vector containing the single parts as Strings
     */
    private Vector getSingleRequests() {
        Vector singleRequests = new Vector();
        String tmp = request.trim().toLowerCase();

        while( tmp.indexOf(" ") != -1 ) {
            int pos = tmp.indexOf(" ");
            // if (DEBUG) Core.getOut().println("Search request: " + (tmp.substring(0, pos)).trim());
            singleRequests.add((tmp.substring(0, pos)).trim());
            tmp = (tmp.substring(pos, tmp.length())).trim();
        }

        if( tmp.length() > 0 ) {
            // if (DEBUG) Core.getOut().println("Search request: " + (tmp));
            singleRequests.add(tmp);
        }

        return singleRequests;
    }

    /**
     * Reads index file and adds search results to the search table
     */
    private void getSearchResults() {
        if( request.length() > 0 ) {
            Vector singleRequests = getSingleRequests();

            synchronized( chk ) {
                Iterator i = chk.values().iterator();
                while( i.hasNext() ) {
                    SharedFileObject key = (SharedFileObject) i.next();
                    String filename = key.getFilename().toLowerCase().trim();
                    boolean acceptFile = true;
                    for( int j = 0; j < singleRequests.size(); j++ ) {
                        String singleRequest = (String) singleRequests.elementAt(j);
                        if( !singleRequest.startsWith("*") ) {
                            if( filename.indexOf(singleRequest) == -1 ) {
                                acceptFile = false;
                            }
                        }
                    }
                    if( acceptFile || request.equals("*") ) {
                        // Check for search type
                        if( searchType.equals("SearchPane.fileTypes.allFiles") ) {
                            results.add(key);
                        } else {
                            boolean accept = false;
                            if( searchType.equals("SearchPane.fileTypes.audio") ) {
                                accept = checkType(audioExtension, filename);
                            } else if( searchType.equals("SearchPane.fileTypes.video") ) {
                                accept = checkType(videoExtension, filename);
                            } else if( searchType.equals("SearchPane.fileTypes.images") ) {
                                accept = checkType(imageExtension, filename);
                            } else if( searchType.equals("SearchPane.fileTypes.documents") ) {
                                accept = checkType(documentExtension, filename);
                            } else if( searchType.equals("SearchPane.fileTypes.executables") ) {
                                accept = checkType(executableExtension, filename);
                            } else if( searchType.equals("SearchPane.fileTypes.archives") ) {
                                accept = checkType(archiveExtension, filename);
                            }
                            if( accept ) {
                                results.add(key);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks extension types
     * @param extension Array with acceptable extensions
     * @param filename Filename to be checked
     * @return True if file gets accepted, else false
     */
    private boolean checkType(String[] extension, String filename) {
        boolean accepted = false;

        for( int i = 0; i < extension.length; i++ ) {
            if( filename.endsWith(extension[i]) )
                accepted = true;
        }

        return accepted;
    }

    /**
     * Removes unwanted keys from results
     */
    private void filterSearchResults() {
        if (request.indexOf("*age") != -1) {
            int agePos = request.indexOf("*age");
            int nextSpacePos = request.indexOf(" ", agePos);
            if (nextSpacePos == -1) {
                nextSpacePos = request.length();
            }

            int age = 1;
            try {
                age = Integer.parseInt(request.substring(agePos + 4, nextSpacePos));
            } catch (NumberFormatException e) {
                logger.warning("Did not recognice age, using default 1.");
            }

            logger.fine("AGE = " + age);

            GregorianCalendar today = new GregorianCalendar();
            today.setTimeZone(TimeZone.getTimeZone("GMT"));

            for (int i = results.size() - 1; i >= 0; i--) {
                SharedFileObject key = (SharedFileObject) results.elementAt(i);
                GregorianCalendar keyCal = null;
                if (key.getDate() != null) {
                    keyCal = key.getCal();

                    keyCal.add(Calendar.DATE, + (age));

                    if (keyCal.before(today)) {
                        results.removeElementAt(i);
                        logger.fine("removing because of keyCal");
                    }
                }
            }
        }

        boolean hideAnon = settings.getBoolValue("hideAnonFiles");
        boolean hideBad = settings.getBoolValue("hideBadFiles");

        //check if file anonymous
        Iterator it = results.iterator();
        while (it.hasNext()) {
            SharedFileObject key = (SharedFileObject) it.next();
            if ((key.getOwner() == null
                || (key.getOwner() != null && key.getOwner().compareToIgnoreCase("anonymous") == 0))
                && hideAnon) {
                //  Core.getOut().println("removing anon result");
                it.remove();
                continue;
            }
            //check if file from someone bad
            Identity id = identities.getIdentity(key.getOwner());
            if (id != null &&
                id.getState() == FrostIdentities.ENEMY &&
                hideBad) {
                //Core.getOut().println("removing bad result");
                it.remove();
                continue;
            }
        }

        if (request.indexOf("*-") != -1) {
            Vector removeStrings = new Vector();
            int notPos = request.indexOf("*-");
            int nextSpacePos = request.indexOf(" ", notPos);
            if (nextSpacePos == -1)
                nextSpacePos = request.length();

            String notString = request.substring(notPos + 2, nextSpacePos);
            if (notString.indexOf(";") == -1) { // only one notString
                removeStrings.add(notString);
            } else { //more notStrings
                while (notString.indexOf(";") != -1) {
                    removeStrings.add(notString.substring(0, notString.indexOf(";")));
                    if (!notString.endsWith(";")) {
                        notString = notString.substring(notString.indexOf(";") + 1, notString.length());
                    }
                }
                if (notString.length() > 0)
                    removeStrings.add(notString);
            }

            for (int j = 0; j < removeStrings.size(); j++) {
                notString = (String) removeStrings.elementAt(j);
                for (int i = results.size() - 1; i >= 0; i--) {
                    SharedFileObject key = (SharedFileObject) results.elementAt(i);
                    if (((key.getFilename()).toLowerCase()).indexOf(notString.toLowerCase()) != -1) {
                        results.removeElementAt(i);
                    }
                }
            }
        }
    }

    /**
     * Filters items by setting of Hide offline, Hide downloaded/downloading.
     * @param state
     * @return
     */
    private boolean filterBySearchItemState( int state ) {
        return true;
    }

    /**
     * Displays search results in search table
     */
    private void displaySearchResults(Board board) {
        for (int i = 0; i < results.size(); i++) {
            allFileCount++;

            if (allFileCount > this.maxSearchResults) {
                logger.info("NOTE: maxSearchResults reached (" + maxSearchResults + ")!");
                return;
            }

            SharedFileObject key = (SharedFileObject) results.elementAt(i);

            String filename = key.getFilename();
            Long size = key.getSize();
            String date = key.getDate();
            String keyData = key.getKey();
            String SHA1 = key.getSHA1();

            if (SHA1 == null) {
                logger.warning("SHA1 null in SearchThread!!! ");
            }

            int searchItemState = FrostSearchItem.STATE_NONE;

            // Already downloaded files get a nice color outfit (see renderer in SearchTable)
            File file = new File(settings.getValue("downloadDirectory") + filename);
            if (file.exists()) {
                // file is already downloaded -> light_gray
                searchItemState = FrostSearchItem.STATE_DOWNLOADED;
            } else if (downloadModel.containsItemWithKey(SHA1)) {
                // this file is in download table -> blue
                searchItemState = FrostSearchItem.STATE_DOWNLOADING;
            } else if (uploadModel.containsItemWithKey(SHA1)) {
                // this file is in upload table -> green
                searchItemState = FrostSearchItem.STATE_UPLOADING;
            } else if (isOffline(key)) {
                // this file is offline -> gray
                searchItemState = FrostSearchItem.STATE_OFFLINE;
            }

            // filter by searchItemState
            if (filterBySearchItemState(searchItemState) == false) {
                continue;
            }

            final FrostSearchItem searchItem = new FrostSearchItem(board, key, searchItemState);

            model.addSearchItem(searchItem);
        }
    }

    private boolean isOffline(SharedFileObject key)
    {
        return !key.isOnline();
    }

    public void run()
    {
        logger.info("Search for '" + request + "' on " + boards.size() + " boards started.");

        if( !request.equals("") )
        {
            int boardCount = boards.size();

            allFileCount = 0;

            for( int j = 0; j < boardCount; j++ )
            {
                Board frostBoard = (Board)boards.elementAt(j);
                String board = frostBoard.getBoardFilename();

                logger.fine("Search for '" + request + "' on " + board + " started.");
                File keypoolDir = new File(keypool + board);
                if( keypoolDir.isDirectory() )
                {
                    File shaIndex = new File(keypoolDir+fileSeparator+"files.xml");
                    if( shaIndex.exists() )
                    {
                         chk.clear();
                         Index idx = Index.getInstance();
                         synchronized(idx) {
                             chk = idx.readKeyFile(shaIndex).getFilesMap();
                         }
                         getSearchResults();
                         logger.fine(shaIndex.getName() + " - " + chk.size() + ";");
                    }
                }
                chk.clear();

                filterSearchResults();
                displaySearchResults(frostBoard);
                results.clear();
            }
        }
        searchPanel.setSearchEnabled(true);
    }

    /**Constructor*/
    public SearchThread(String newRequest,
            Vector newBoards, // a Vector containing all boards to search in
            String newSearchType,
            SearchManager searchManager)
    {
        identities = searchManager.getIdentities();
        settings = searchManager.getSettings();
        request = newRequest.toLowerCase();
        if( request.length() == 0 )
        {
            // default: search all
            request = "*";
        }
        model = searchManager.getModel();
        keypool = searchManager.getKeypool();
        searchType = newSearchType;
        audioExtension = settings.getArrayValue("audioExtension");
        videoExtension = settings.getArrayValue("videoExtension");
        documentExtension = settings.getArrayValue("documentExtension");
        executableExtension = settings.getArrayValue("executableExtension");
        archiveExtension = settings.getArrayValue("archiveExtension");
        imageExtension = settings.getArrayValue("imageExtension");
        boards = newBoards;
        maxSearchResults = settings.getIntValue("maxSearchResults");
        if( maxSearchResults <= 0 ) {
            maxSearchResults = 10000; // default
        }
        searchPanel = searchManager.getPanel();
    }
    /**
     * @param model
     */
    public void setUploadModel(UploadModel model) {
        uploadModel = model;
    }

    /**
     * @param model
     */
    public void setDownloadModel(DownloadModel model) {
        downloadModel = model;
    }
}