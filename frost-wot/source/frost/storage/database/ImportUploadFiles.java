package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.database.applayer.*;

public class ImportUploadFiles {

    private static Logger logger = Logger.getLogger(ImportUploadFiles.class.getName());

    public void importUploadFiles(TofTreeModel tofTreeModel, FileTransferManager man) {
        
        try {
            List ulItems = load(tofTreeModel);

            List newUlItems = new LinkedList();
            // if sha1 is null add file to NEWUPLOADFILES table
            for(Iterator i=ulItems.iterator(); i.hasNext(); ) {
                FrostUploadItem ulItem = (FrostUploadItem)i.next();  
                if( ulItem.getSHA1() == null || ulItem.getSHA1().length() == 0 ) {
                    
                    i.remove(); // remove from the list with sha1 values
                    
                    File file = new File(ulItem.getFilePath());
                    if( file.isFile() == false || file.length() == 0 ) {
                        logger.warning("Removed deleted/empty file from upload files: "+ulItem.getFilePath());
                        continue;
                    }
                    Board board = ((FrostUploadItemOwnerBoard)ulItem.getFrostUploadItemOwnerBoardList().get(0)).getTargetBoard();
                    // check setting and use anonymous or myId !
                    // at import time we should have only 1 local identity, use the first one
                    String fromName = null;
                    if( Core.frostSettings.getBoolValue("signUploads") ) {
                        fromName = ((LocalIdentity)Core.getIdentities().getLocalIdentities().get(0)).getUniqueName();
                    }
                    NewUploadFile nuf = new NewUploadFile(file, board, fromName);
                    newUlItems.add(nuf);
                }
            }
            try {
                AppLayerDatabase.getNewUploadFilesTable().saveNewUploadFiles(newUlItems);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error importing new upload items", e);
            }

            // save items with sha1!=null
            AppLayerDatabase.getUploadFilesDatabaseTable().saveUploadFiles(ulItems);
            
            man.setUploadItemsAfterImport(ulItems);
            
            // TODO: start thread to process NEWUPLOADFILES
            
        } catch (StorageException e) {
            logger.log(Level.SEVERE, "Error importing upload items", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error importing upload items", e);
        }
    }

    private static final String XML_FILENAME = "uploads.xml";
    
    /* (non-Javadoc)
     * @see frost.fileTransfer.upload.UploadModelDAO#load(frost.fileTransfer.upload.UploadModel)
     */
    public List load(TofTreeModel tofTreeModel) throws StorageException {
        LinkedList ulItems = new LinkedList();
        
        String directory = Core.frostSettings.getValue("config.dir");
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(directory + XML_FILENAME, false);
        } catch (Exception ex) {
            throw new StorageException("Exception while parsing the uploads model XML file.");
        }
        if (doc == null) {
            throw new StorageException("Could not parse the uploads model XML file.");
        }

        Element rootNode = doc.getDocumentElement();

        if (rootNode.getTagName().equals("FrostUploadTable") == false) {
            throw new StorageException(
                "The uploads model XML file is invalid: does not contain the root tag FrostUploadTable.");
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist =
            XMLTools.getChildElementsByTagName(rootNode, "FrostUploadTableItemList");
        if (nodelist.size() != 1) {
            throw new StorageException(
                "The uploads model XML file is invalid: FrostUploadTableItemList not found or duplicated.");
        }

        Element itemListRootNode = (Element) nodelist.get(0);
        nodelist = XMLTools.getChildElementsByTagName(itemListRootNode, "FrostUploadTableItem");

        if (nodelist.size() == 0) {
            logger.info("The uploads model XML file has no items.");
        } else {
            for (int x = 0; x < nodelist.size(); x++) {
                Element element = (Element) nodelist.get(x);
                FrostUploadItem uploadItem = createUploadItem(element, tofTreeModel);
                if (uploadItem != null) {
                    ulItems.add(uploadItem);
                }
            }
            logger.info("Loaded " + nodelist.size() + " items into the uploads model.");
        }
        return ulItems;
    }

    private FrostUploadItem createUploadItem(Element element, TofTreeModel tofTreeModel) {
        String filename = XMLTools.getChildElementsCDATAValue(element, "filename");
        String filepath = XMLTools.getChildElementsCDATAValue(element, "filepath");
        String targetboardname = XMLTools.getChildElementsTextValue(element, "targetboard");
        String state = XMLTools.getChildElementsTextValue(element, "state");
        String lastUploadDate = XMLTools.getChildElementsTextValue(element, "lastuploaddate");
        String key = XMLTools.getChildElementsCDATAValue(element, "key");
        String SHA1 = XMLTools.getChildElementsCDATAValue(element, "SHA1");
        String enabled = element.getAttribute("enabled");
        String retries = XMLTools.getChildElementsTextValue(element, "retries");

        // batch is allowed to be null, I think
        if (filename == null || filepath == null || targetboardname == null || state == null) {//|| batch==null)
            logger.warning("Invalid upload item found. Removed.");
            return null;
        }

        // retries may be null if we are upgrading from an earlier version of Frost
        int retriesInt = 0;
        if (retries != null) {
            retriesInt = Integer.parseInt(retries);
        }

        int iState = -1;
        try {
            iState = Integer.parseInt(state);
        } catch (NumberFormatException ex) {
            // string is no number -> old format
            iState = -1;
        }

        // new format: states are saved in XML as numbers
        if ((iState == FrostUploadItem.STATE_PROGRESS) || (iState == FrostUploadItem.STATE_UPLOADING)) {
            iState = FrostUploadItem.STATE_REQUESTED;
        } else if ((iState == FrostUploadItem.STATE_ENCODING) || (iState == FrostUploadItem.STATE_ENCODING_REQUESTED)) {
            iState = FrostUploadItem.STATE_IDLE;
        } else {
            // fallback
            iState = FrostUploadItem.STATE_IDLE;
        }

        if (key != null && key.startsWith("CHK@") == false) {
            key = null;
        }

        File uploadFile = new File(filepath);

        if (!uploadFile.isFile() || uploadFile.length() == 0) {
            logger.warning("Upload item found with no corresponding file (" + filepath +
                                "). Removed.");
            return null;
        }

        // check if target board exists in board tree
        Board board = tofTreeModel.getBoardByName(targetboardname);
        if (board == null) {
            logger.warning("Upload item found (" + filepath + ") whose target board (" +
                                targetboardname + ") does not exist. Removed.");
            return null;
        }

        boolean uploadEnabled = false;
        if (enabled == null || enabled.length() == 0 ||
                enabled.toLowerCase().equals("true")) {
            uploadEnabled = true; // default is true
        }

        // create FrostUploadItem
        FrostUploadItem ulItem = new FrostUploadItem(
                SHA1,
                filename, 
                filepath, 
                uploadFile.length(),
                key,
                DateFun.getSqlDateOfCalendar(DateFun.getCalendarFromDate(lastUploadDate)),
                (key==null?0:1),
                null,
                0,
                Integer.parseInt(state),
                uploadEnabled,
                0,
                Integer.parseInt(retries) );
        
        // check setting and use anonymous or myId !
        String fromName = null;
        if( Core.frostSettings.getBoolValue("signUploads") ) {
            fromName = ((LocalIdentity)Core.getIdentities().getLocalIdentities().get(0)).getUniqueName();
        }
        FrostUploadItemOwnerBoard ob = new FrostUploadItemOwnerBoard(ulItem, board, fromName, null);
        ulItem.addFrostUploadItemOwnerBoard(ob);

        return ulItem;
    }
}
