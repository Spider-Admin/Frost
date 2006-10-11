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
import frost.util.*;

class SearchThread extends Thread implements FileListDatabaseTableCallback {

    private static Logger logger = Logger.getLogger(SearchThread.class.getName());
    
    private SearchParameters searchParams;

    private String[] audioExtension;
    private String[] videoExtension;
    private String[] imageExtension;
    private String[] documentExtension;
    private String[] executableExtension;
    private String[] archiveExtension;

    boolean hideBad;

    private int allFileCount;
    private int maxSearchResults;
    
//    private java.sql.Date currentDate;

    private SearchTable searchTable;
    private DownloadModel downloadModel;
    private SharedFilesModel sharedFilesModel;

    private boolean isStopRequested = false;
    
    private boolean isStopRequested() {
        return isStopRequested;
    }
    private void requestStop() {
        isStopRequested = true;
    }
    
    private String lowerCase(String s) {
        if( s == null ) {
            return "";
        } else {
            return s.toLowerCase();
        }
    }
    
    /**
     * Check search options, step 1.
     */
    private boolean searchFile1(FrostFileListFileObject fo) {

        // check if file has a key
        if( searchParams.getWithKeyOnly() ) {
            if( fo.getKey() == null || fo.getKey().length() == 0 ) {
                return false;
            }
        }

        // hideBadFiles: show file if no bad owner; or if at least 1 owner is good/observe
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
                        if( id.isGOOD() || id.isOBSERVE() ) {
                            accept = true;
                            break; // break, one GOOD is enough to accept
                        }
                    }                
                }
            }
            if( !accept ) {
                return false;
            }
        }

        // check file extension. if extension of ONE file is ok the file matches
        if( searchParams.getExtensions() != SearchParameters.EXTENSIONS_ALL ) {
            boolean accept = false;
            for( Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
                FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner) i.next();
                String name = lowerCase(ob.getName());
                // check for search type
                if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_AUDIO ) {
                    accept = checkType(audioExtension, name);
                } else if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_VIDEO ) {
                    accept = checkType(videoExtension, name);
                } else if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_IMAGES ) {
                    accept = checkType(imageExtension, name);
                } else if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_DOCUMENTS ) {
                    accept = checkType(documentExtension, name);
                } else if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_ARCHIVES ) {
                    accept = checkType(archiveExtension, name);
                } else if( searchParams.getExtensions() == SearchParameters.EXTENSIONS_EXECUTABLES ) {
                    accept = checkType(executableExtension, name);
                }
                if( accept == true ) {
                    break; // break, one correct extension is enough to accept
                }
            }
            if( !accept ) {
                return false;
            }
        }

        return true; // accepted
    }
    
    /**
     * Check search options, step 2.
     */
    private boolean searchFile2(FrostFileListFileObject fo) {
        if( searchParams.isSimpleSearch() ) {
            return (searchFile2NotStringsSimple(fo) && searchFile2StringsSimple(fo));
        } else {
            return (searchFile2NotStringsAdvanced(fo) && searchFile2StringsAdvanced(fo));
        }
    }

    /**
     * Check all NOT strings, if a not string occurs for a file it is not accepted.
     */
    private boolean searchFile2NotStringsAdvanced(FrostFileListFileObject fo) {
        
        if( searchParams.getNotName().isEmpty()
                && searchParams.getNotComment().isEmpty()
                && searchParams.getNotKeyword().isEmpty()
                && searchParams.getNotOwner().isEmpty() )
        {
            return true; // no not strings given
        }

        for( Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner) i.next();

            String name = lowerCase(ob.getName());
            String comment = lowerCase(ob.getComment());
            String keyword = lowerCase(ob.getKeywords());
            String owner = lowerCase(ob.getOwner()); 
            
            // check notName
            if( TextSearchFun.containsAnyString(name, searchParams.getNotName()) ) {
                return false;
            }
            // check notComment
            if( TextSearchFun.containsAnyString(comment, searchParams.getNotComment()) ) {
                return false;
            }
            // check notKeyword
            if( TextSearchFun.containsAnyString(keyword, searchParams.getNotKeyword()) ) {
                return false;
            }
            // check notOwner
            if( TextSearchFun.containsAnyString(owner, searchParams.getNotOwner()) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * If a NOT string occurs inside name, comment, keyword then do not accept this file.
     */
    private boolean searchFile2NotStringsSimple(FrostFileListFileObject fo) {
        
        if( searchParams.getSimpleSearchNotStrings().isEmpty() ) {
            return true;
        }

        for( Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner) i.next();

            String name = lowerCase(ob.getName());
            String comment = lowerCase(ob.getComment());
            String keyword = lowerCase(ob.getKeywords());
            
            for(Iterator j=searchParams.getSimpleSearchNotStrings().iterator(); j.hasNext(); ) {
                String notString = (String) j.next();
                if( name.indexOf(notString) > -1 ) {
                    return false;
                }
                if( comment.indexOf(notString) > -1 ) {
                    return false;
                }
                if( keyword.indexOf(notString) > -1 ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Advanced string search: if the given string does not occur in the field (e.g. comment)
     * then the file is not accepted.
     */
    private boolean searchFile2StringsAdvanced(FrostFileListFileObject fo) {

        boolean nameFound = false;
        boolean commentFound = false;
        boolean keywordFound = false;
        boolean ownerFound = false;
        
        if( searchParams.getName().isEmpty() ) {
            nameFound = true;
        }
        if( searchParams.getComment().isEmpty() ) {
            commentFound = true;
        }
        if( searchParams.getKeyword().isEmpty() ) {
            keywordFound = true;
        }
        if( searchParams.getOwner().isEmpty() ) {
            ownerFound = true;
        }

        if (nameFound && commentFound && keywordFound && ownerFound) {
            return true; // find all
        }
        
        for( Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner) i.next();

            String name = lowerCase(ob.getName());
            String comment = lowerCase(ob.getComment());
            String keyword = lowerCase(ob.getKeywords());
            String owner = lowerCase(ob.getOwner()); 
            
            // then check for strings, if strings are given they must match

            // check name
            if( !searchParams.getName().isEmpty() && name.length() > 0 ) {
                if( TextSearchFun.containsEachString(name, searchParams.getName()) ) {
                    nameFound = true;
                }
            }
            // check comment
            if( !searchParams.getComment().isEmpty() && comment.length() > 0 ) {
                if( TextSearchFun.containsEachString(comment, searchParams.getComment()) ) {
                    commentFound = true;
                }
            }
            // check keyword
            if( !searchParams.getKeyword().isEmpty() && keyword.length() > 0 ) {
                if( TextSearchFun.containsEachString(keyword, searchParams.getKeyword()) ) {
                    keywordFound = true;
                }
            }
            // check owner
            if( !searchParams.getOwner().isEmpty() && owner.length() > 0 ) {
                if( TextSearchFun.containsEachString(owner, searchParams.getOwner()) ) {
                    ownerFound = true;
                }
            }
        }
        
        return (nameFound && commentFound && keywordFound && ownerFound);
    }

    /**
     * Simple string search: each given string must be found at least once in name, comment or keyword.
     */
    private boolean searchFile2StringsSimple(FrostFileListFileObject fo) {
        
        if( searchParams.getSimpleSearchStrings().isEmpty() ) {
            return true; // find all
        }

        // mark all strings not found
        Hashtable searchStrings = new Hashtable();
        for(Iterator i=searchParams.getSimpleSearchStrings().iterator(); i.hasNext(); ) {
            String s = (String) i.next();
            searchStrings.put(s, Boolean.FALSE);
        }

        for( Iterator i=fo.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner ob = (FrostFileListFileObjectOwner) i.next();

            String name = lowerCase(ob.getName());
            String comment = lowerCase(ob.getComment());
            String keyword = lowerCase(ob.getKeywords());
            
            for(Iterator j=searchParams.getSimpleSearchStrings().iterator(); j.hasNext(); ) {
                String string = (String) j.next();
                if( name.indexOf(string) > -1 ) {
                    searchStrings.put(string, Boolean.TRUE);
                }
                if( comment.indexOf(string) > -1 ) {
                    searchStrings.put(string, Boolean.TRUE);
                }
                if( keyword.indexOf(string) > -1 ) {
                    searchStrings.put(string, Boolean.TRUE);
                }
            }
        }
        
        // finally check if all words were found
        boolean allFound = true;
        for(Iterator i=searchStrings.values().iterator(); i.hasNext(); ) {
            Boolean b = (Boolean) i.next();
            if( b.booleanValue() == false ) {
                allFound = false;
                break;
            }
        }
        
        return allFound;
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

//    private boolean filterSearchResults(FrostFileListFileObject fo) {
//        if (request.indexOf("*age") != -1) {
//            int agePos = request.indexOf("*age");
//            int nextSpacePos = request.indexOf(" ", agePos);
//            if (nextSpacePos == -1) {
//                nextSpacePos = request.length();
//            }
//
//            int age = 1;
//            try {
//                age = Integer.parseInt(request.substring(agePos + 4, nextSpacePos));
//            } catch (NumberFormatException e) {
//                logger.warning("Did not recognice age, using default 1.");
//            }
//
//            long diffMillis = age * 24 * 60 * 60 * 1000;
//            long minDateMillis = currentDate.getTime() - diffMillis;
//            
//            if( fo.getLastReceived() < minDateMillis ) {
//                return false; // older than age
//            }
//        }
//    }

    /**
     * Displays search results in search table
     */
    private void addSearchResult(FrostFileListFileObject fo) {

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
        searchTable.addSearchItem(searchItem);
    }
    
    public boolean fileRetrieved(FrostFileListFileObject fo) {
        if( searchFile1(fo) && searchFile2(fo) ) {
            addSearchResult(fo);
        }
        return isStopRequested();
    }

    public void run() {

        allFileCount = 0;

        try {
            AppLayerDatabase.getFileListDatabaseTable().retrieveFiles(this);
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Catched exception:", e);
        }

        searchTable.searchFinished();
    }

    /**Constructor*/
    public SearchThread(SearchParameters searchParams, SearchTable searchTable) {

        this.searchParams = searchParams; 
        
        this.searchTable = searchTable;
        
        audioExtension      = Core.frostSettings.getArrayValue("audioExtension");
        videoExtension      = Core.frostSettings.getArrayValue("videoExtension");
        documentExtension   = Core.frostSettings.getArrayValue("documentExtension");
        executableExtension = Core.frostSettings.getArrayValue("executableExtension");
        archiveExtension    = Core.frostSettings.getArrayValue("archiveExtension");
        imageExtension      = Core.frostSettings.getArrayValue("imageExtension");
        maxSearchResults    = Core.frostSettings.getIntValue("maxSearchResults");
        
        if( maxSearchResults <= 0 ) {
            maxSearchResults = 10000; // default
        }
//        currentDate = DateFun.getCurrentSqlDateGMT();
        hideBad = Core.frostSettings.getBoolValue("hideBadFiles");
        
        downloadModel = FileTransferManager.getInstance().getDownloadManager().getModel();
        sharedFilesModel = FileTransferManager.getInstance().getSharedFilesManager().getModel();
    }
}
