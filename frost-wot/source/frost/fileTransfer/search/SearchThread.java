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
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.identities.*;
import frost.storage.database.applayer.*;

class SearchThread extends Thread implements FileListDatabaseTableCallback {

    private static Logger logger = Logger.getLogger(SearchThread.class.getName());

    private String request;
    private String searchType;
    private String[] audioExtension;
    private String[] videoExtension;
    private String[] imageExtension;
    private String[] documentExtension;
    private String[] executableExtension;
    private String[] archiveExtension;
    private int allFileCount;
    private int maxSearchResults;
    private SearchPanel searchPanel = null;
    
    private java.sql.Date currentDate;

    private SearchModel model;
    private DownloadModel downloadModel;
    private SharedFilesModel sharedFilesModel;

    private boolean isStopRequested = false;
    
    private List cachedSingleRequests = null;
    private List cachedRemoveStrings = null;

    private List getSingleRequests() {
        return cachedSingleRequests;
    }
    private List getRemoveStrings() {
        return cachedRemoveStrings;
    }
    private boolean isStopRequested() {
        return isStopRequested;
    }
    private void requestStop() {
        isStopRequested = true;
    }

    /**
     * Splits a String into single parts
     * @return Vector containing the single parts as Strings
     */
    private List prepareSingleRequests(String req) {
        List sRequests = new ArrayList();
        String tmp = req.trim().toLowerCase();

        while( tmp.indexOf(" ") != -1 ) {
            int pos = tmp.indexOf(" ");
            // if (DEBUG) Core.getOut().println("Search request: " + (tmp.substring(0, pos)).trim());
            sRequests.add((tmp.substring(0, pos)).trim());
            tmp = (tmp.substring(pos, tmp.length())).trim();
        }

        if( tmp.length() > 0 ) {
            // if (DEBUG) Core.getOut().println("Search request: " + (tmp));
            sRequests.add(tmp);
        }

        return sRequests;
    }

    /**
     * Reads index file and adds search results to the search table
     */
    private boolean getSearchResults(FrostFileListFileObject fo) {
        List sRequests = getSingleRequests();

        FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner)
                fo.getFrostFileListFileObjectOwnerList().get(0);
        
        // check for name
        String filename = ob.getName().toLowerCase().trim();
        for( int j = 0; j < sRequests.size(); j++ ) {
            String singleRequest = (String) sRequests.get(j);
            if( !singleRequest.startsWith("*") ) {
                if( filename.indexOf(singleRequest) < 0 ) {
                    return false;
                }
            }
        }

        // check for forbidden name
        for (int j = 0; j < getRemoveStrings().size(); j++) {
            String notString = (String) getRemoveStrings().get(j);
            if ((filename.toLowerCase()).indexOf(notString.toLowerCase()) != -1) {
                return false;
            }
        }

        // check for search type
        if( searchType.equals("SearchPane.fileTypes.allFiles") ) {
            return true;
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
            return accept;
        }
    }
    
    private List prepareRemoveStrings(String req) {
        List rStrings = new ArrayList();
        int notPos = req.indexOf("*-");
        if( notPos < 0 ) {
            return rStrings;
        }
        int nextSpacePos = req.indexOf(" ", notPos);
        if (nextSpacePos == -1) {
            nextSpacePos = req.length();
        }

        String notString = req.substring(notPos + 2, nextSpacePos);
        if (notString.indexOf(";") == -1) { // only one notString
            rStrings.add(notString);
        } else { //more notStrings
            while (notString.indexOf(";") != -1) {
                rStrings.add(notString.substring(0, notString.indexOf(";")));
                if (!notString.endsWith(";")) {
                    notString = notString.substring(notString.indexOf(";") + 1, notString.length());
                }
            }
            notString = notString.trim();
            if (notString.length() > 0) {
                rStrings.add(notString);
            }
        }
        return rStrings;
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
    private boolean filterSearchResults(FrostFileListFileObject fo) {
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

            long diffMillis = age * 24 * 60 * 60 * 1000;
            long minDateMillis = currentDate.getTime() - diffMillis;
            
            if( fo.getLastReceived() < minDateMillis ) {
                return false; // older than age
            }
        }

        boolean hideBad = Core.frostSettings.getBoolValue("hideBadFiles");

        // hideBadFiles: show file if no bad owner; or if at least 1 owner is good
        if( hideBad ) {
            boolean accept = true;
            for(Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
                FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner)i.next();
                if( ob.getOwner() != null ) {
                    Identity id = Core.getIdentities().getIdentity(ob.getOwner());
                    if (id != null ) { 
                        if( id.isBAD() ) {
                            accept = false; // dont break, maybe there is a good
                        }
                        if( id.isGOOD() ) {
                            return true;
                        }
                    }                
                }
            }
            if( !accept ) {
                return false;
            }
        }
        // all test passed
        return true;
    }

    /**
     * Displays search results in search table
     */
    private void displaySearchResults(FrostFileListFileObject fo) {

        allFileCount++;

        if (allFileCount > this.maxSearchResults) {
            logger.info("NOTE: maxSearchResults reached (" + maxSearchResults + ")!");
            requestStop();
            return;
        }

        FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner)
                fo.getFrostFileListFileObjectOwnerList().get(0);

        String filename = ob.getName();
        String keyData = fo.getKey();
        String SHA1 = fo.getSha();

        int searchItemState = FrostSearchItem.STATE_NONE;

        // Already downloaded files get a nice color outfit (see renderer in SearchTable)
        File file = new File(Core.frostSettings.getValue("downloadDirectory") + filename);
        if (file.exists()) {
            // file is already downloaded -> light_gray
            searchItemState = FrostSearchItem.STATE_DOWNLOADED;
        } else if (downloadModel.containsItemWithSha(SHA1)) {
            // this file is in download table -> blue
            searchItemState = FrostSearchItem.STATE_DOWNLOADING;
        } else if (sharedFilesModel.containsItemWithSha(SHA1)) {
            // this file is in upload table -> green
            searchItemState = FrostSearchItem.STATE_UPLOADING;
        } else if (keyData == null) {
            // this file is offline -> gray
            searchItemState = FrostSearchItem.STATE_OFFLINE;
        }

        FrostSearchItem searchItem = new FrostSearchItem(fo, searchItemState);
        model.addSearchItem(searchItem);
    }
    
    public boolean fileRetrieved(FrostFileListFileObject fo) {
        if( getSearchResults(fo) && filterSearchResults(fo) ) {
            displaySearchResults(fo);
        }
        return isStopRequested();
    }

    public void run() {
        logger.info("Search for '" + request + "' started.");

        allFileCount = 0;

        try {
            AppLayerDatabase.getFileListDatabaseTable().retrieveFiles(this);
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Catched exception:", e);
        }

        searchPanel.setSearchEnabled(true);
    }

    /**Constructor*/
    public SearchThread(String newRequest,
            String newSearchType,
            SearchManager searchManager)
    {
        request = newRequest.toLowerCase();
        if( request.length() == 0 ) {
            // default: search all
            request = "*";
        }
        model = searchManager.getModel();
        searchType = newSearchType;
        audioExtension = Core.frostSettings.getArrayValue("audioExtension");
        videoExtension = Core.frostSettings.getArrayValue("videoExtension");
        documentExtension = Core.frostSettings.getArrayValue("documentExtension");
        executableExtension = Core.frostSettings.getArrayValue("executableExtension");
        archiveExtension = Core.frostSettings.getArrayValue("archiveExtension");
        imageExtension = Core.frostSettings.getArrayValue("imageExtension");
        maxSearchResults = Core.frostSettings.getIntValue("maxSearchResults");
        if( maxSearchResults <= 0 ) {
            maxSearchResults = 10000; // default
        }
        searchPanel = searchManager.getPanel();
        currentDate = DateFun.getCurrentSqlDateGMT();
        
        cachedSingleRequests = prepareSingleRequests(request);
        cachedRemoveStrings = prepareRemoveStrings(request);
        
        downloadModel = FileTransferManager.getInstance().getDownloadManager().getModel();
        sharedFilesModel = FileTransferManager.getInstance().getSharedFilesManager().getModel();
    }
}
