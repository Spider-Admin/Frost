/*
 ImportDownloadFiles.java / Frost
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
package frost.storage.database;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.gui.objects.*;
import frost.storage.*;
import frost.storage.database.applayer.*;

public class ImportDownloadFiles {

    private static Logger logger = Logger.getLogger(ImportDownloadFiles.class.getName());

    public void importDownloadFiles(TofTreeModel tofTreeModel, FileTransferManager man) {
        
        try {
            List dlItems = load(tofTreeModel);
            
            AppLayerDatabase.getDownloadFilesDatabaseTable().saveDownloadFiles(dlItems);
            
            man.setDownloadItemsAfterImport(dlItems);
            
        } catch (StorageException e) {
            logger.log(Level.SEVERE, "Error importing download items", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error importing download items", e);
        }
    }
    
    private static final String XML_FILENAME = "downloads.xml";
    
    private FrostDownloadItem createDownloadItem(Element element, TofTreeModel tofTreeModel) {
        String filename = XMLTools.getChildElementsCDATAValue(element, "filename");
        String filesize = XMLTools.getChildElementsTextValue(element, "filesize");
        String key = XMLTools.getChildElementsCDATAValue(element, "key");
        String retries = XMLTools.getChildElementsTextValue(element, "retries");
        String state = XMLTools.getChildElementsTextValue(element, "state");
        String owner = XMLTools.getChildElementsTextValue(element, "owner");
        String sourceboardname = XMLTools.getChildElementsTextValue(element, "sourceboard");
        String enableDownload = element.getAttribute("enableDownload");
        String SHA1 = XMLTools.getChildElementsTextValue(element, "SHA1");

        // SHA1 val is not available when adding downloads using textbox
        // one of key or SHA1 must be available
        if (filename == null || state == null || (key == null && SHA1 == null)) {
            logger.warning("Invalid download item found. Removed.");
            return null;
        }

        int iState = -1;
        try {
            iState = Integer.parseInt(state);
        } catch (NumberFormatException ex) {
            // string is no number -> old format
            iState = -1;
        }

        if (iState != FrostDownloadItem.STATE_DONE) {
            iState = FrostDownloadItem.STATE_WAITING;
        }

        boolean isDownloadEnabled = false;
        if (enableDownload == null || enableDownload.length() == 0 ||
                enableDownload.toLowerCase().equals("true")) {
            isDownloadEnabled = true; // default is true
        }

        // check if target board exists in board tree

        Board board = null;
        if (sourceboardname != null) {
            board = tofTreeModel.getBoardByName(sourceboardname);
            if (board == null) {
                logger.warning("Download item found (" + filename + ") whose source board (" +
                        sourceboardname + ") does not exist. Removed.");
                return null;
            }
        }

        // create FrostDownloadItem
        FrostDownloadItem dlItem = new FrostDownloadItem(
                filename, 
                (filesize==null?null:new Long(filesize)), 
                key, 
                (retries==null?0:Integer.valueOf(retries).intValue()),
                owner, 
                SHA1, 
                iState, 
                isDownloadEnabled, 
                board,
                0,
                null,
                0);
        return dlItem;
    }

    public List load(TofTreeModel tofTreeModel) throws StorageException {
        Document doc = null;
        LinkedList downloadItems = new LinkedList();
        try {
            String directory = Core.frostSettings.getValue("config.dir");
            doc = XMLTools.parseXmlFile(directory + XML_FILENAME, false);
        } catch (Exception ex) {
            throw new StorageException("Exception while parsing the downloads model XML file.");
        }
        if (doc == null) {
            throw new StorageException("Could not parse the downloads model XML file.");
        }

        Element rootNode = doc.getDocumentElement();

        if (rootNode.getTagName().equals("FrostDownloadTable") == false) {
            throw new StorageException(
                "The downloads model XML file is invalid: does not contain the root tag FrostDownloadTable.");
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist =
            XMLTools.getChildElementsByTagName(rootNode, "FrostDownloadTableItemList");
        if (nodelist.size() != 1) {
            throw new StorageException(
                "The downloads model XML file is invalid: FrostDownloadTableItemList not found or duplicated.");
        }

        Element itemListRootNode = (Element) nodelist.get(0);
        nodelist = XMLTools.getChildElementsByTagName(itemListRootNode, "FrostDownloadTableItem");

        if (nodelist.size() == 0) {
            logger.info("The downloads model XML file has no items.");  
        } else {
            for (int x = 0; x < nodelist.size(); x++) {
                Element element = (Element) nodelist.get(x);
                FrostDownloadItem downloadItem = createDownloadItem(element, tofTreeModel);
                if (downloadItem != null) {
                    downloadItems.add(downloadItem);
                }
            }
            logger.info("Loaded " + nodelist.size() + " items into the downloads model.");
        }
        return downloadItems;
    }
}
