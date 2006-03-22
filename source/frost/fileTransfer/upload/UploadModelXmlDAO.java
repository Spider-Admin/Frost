/*
  UploadModelXmlDAO.java / Frost
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
package frost.fileTransfer.upload;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Logger;

import org.w3c.dom.*;

import frost.*;
import frost.gui.objects.Board;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UploadModelXmlDAO implements UploadModelDAO {

    private static Logger logger = Logger.getLogger(UploadModelXmlDAO.class.getName());

    private static ResourceBundle langRes = java.util.ResourceBundle.getBundle("res.LangRes");

    private static final String XML_FILENAME = "uploads.xml";
    private static final String TMP_FILENAME = "uploads.xml.tmp";
    private static final String BAK_FILENAME = "uploads.xml.bak";

    private String directory;

    /**
     * @param settings
     */
    public UploadModelXmlDAO(SettingsClass settings) {
        directory = settings.getValue("config.dir");
    }

    /* (non-Javadoc)
     * @see frost.fileTransfer.upload.UploadModelDAO#exists()
     */
    public boolean exists() {
        File xmlFile = new File(directory + XML_FILENAME);
        if (xmlFile.length() == 0) {
            xmlFile.delete();
        }
        if (xmlFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see frost.fileTransfer.upload.UploadModelDAO#load(frost.fileTransfer.upload.UploadModel)
     */
    public void load(UploadModel uploadModel) throws StorageException {
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
                FrostUploadItem uploadItem = createUploadItem(element);
                if (uploadItem != null) {
                    uploadModel.addUploadItem(uploadItem);
                }
            }
            logger.info("Loaded " + nodelist.size() + " items into the uploads model.");
        }
    }

    /**
     * @param element
     * @return
     */
    private FrostUploadItem createUploadItem(Element element) {
        String filename = XMLTools.getChildElementsCDATAValue(element, "filename");
        String filepath = XMLTools.getChildElementsCDATAValue(element, "filepath");
        String targetboardname = XMLTools.getChildElementsTextValue(element, "targetboard");
        String state = XMLTools.getChildElementsTextValue(element, "state");
        String lastUploadDate = XMLTools.getChildElementsTextValue(element, "lastuploaddate");
        String sharedDate = XMLTools.getChildElementsTextValue(element, "dateShared");
        String key = XMLTools.getChildElementsCDATAValue(element, "key");
        String SHA1 = XMLTools.getChildElementsCDATAValue(element, "SHA1");
        String batch = XMLTools.getChildElementsTextValue(element, "batch");
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

        if (iState < 0) {
            // old format: states are saved in XML as LangRes Strings
            if (state.indexOf("Kb") != -1 || state.equals(langRes.getString("Uploading"))) {
                iState = FrostUploadItem.STATE_REQUESTED;
            }
        } else {
            // new format: states are saved in XML as numbers
            if ((iState == FrostUploadItem.STATE_PROGRESS) ||
                    (iState == FrostUploadItem.STATE_UPLOADING)) {
                iState = FrostUploadItem.STATE_REQUESTED;
            } else if ((iState == FrostUploadItem.STATE_ENCODING) ||
                        (iState == FrostUploadItem.STATE_ENCODING_REQUESTED)) {
                iState = FrostUploadItem.STATE_IDLE;
            }
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
        Board board = MainFrame.getInstance().getTofTreeModel().getBoardByName(targetboardname);
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
        FrostUploadItem ulItem = new FrostUploadItem(filename, filepath, uploadFile.length(),
                                                        board, iState, lastUploadDate, key, SHA1);
        ulItem.setBatch(batch);
        ulItem.setEnabled(new Boolean(uploadEnabled));
        ulItem.setRetries(retriesInt);
        return ulItem;
    }

    /* (non-Javadoc)
     * @see frost.fileTransfer.upload.UploadModelDAO#save(frost.fileTransfer.upload.UploadModel)
     */
    public void save(UploadModel uploadModel) throws StorageException {

        Document doc = XMLTools.createDomDocument();
        if (doc == null) {
            throw new StorageException("Could not create the XML document.");
        }

        Element rootElement = doc.createElement("FrostUploadTable");
        doc.appendChild(rootElement);
        Element itemsRoot = doc.createElement("FrostUploadTableItemList");
        rootElement.appendChild(itemsRoot);

        // now add all items to itemsRoot
        for (int x = 0; x < uploadModel.getItemCount(); x++) {
            FrostUploadItem uploadItem = (FrostUploadItem) uploadModel.getItemAt(x);
            Element element = createElement(uploadItem, doc);
            itemsRoot.appendChild(element);
        }

        //Now we copy "uploads.xml" to "uploads.xml.bak"
        File uploadsFile = new File(directory + XML_FILENAME);
        if (uploadsFile.exists()) {
            File bakFile = new File(directory + BAK_FILENAME);
            bakFile.delete();
            if( !FileAccess.copyFile(directory + XML_FILENAME, directory + BAK_FILENAME )) {
                logger.log(Level.SEVERE, "Error while copying " + XML_FILENAME + " to " + BAK_FILENAME);
            }
        }

        //We delete "uploads.xml.tmp"
        File uploadsTmpFile = new File(directory + TMP_FILENAME);
        if (uploadsTmpFile.exists()) {
            uploadsTmpFile.delete();
        }

        //We save identities to "uploads.xml.tmp"
        if (XMLTools.writeXmlFile(doc, directory + TMP_FILENAME)) {
            //Success
            if (uploadsTmpFile.exists()) {
                //We replace "uploads.xml" by "uploads.xml.tmp"
                uploadsFile.delete();
                if (!uploadsTmpFile.renameTo(uploadsFile)) {
                    //Replacement failed. We try to restore "uploads.xml"
                    // from "uploads.xml.bak"
                    if( !FileAccess.copyFile(directory + BAK_FILENAME, directory + XML_FILENAME) ) {
                        //Uh, oh, we are having a bad, bad day.
                        throw new StorageException("Error while restoring " + XML_FILENAME);
                    }
                }
            } else {
                //This shouldn't happen, but...
                throw new StorageException("Could not save " + XML_FILENAME);
            }
        } else {
            //Failure
            throw new StorageException("Could not save " + XML_FILENAME);
        }
    }

    /**
     * @param uploadItem
     * @return
     */
    private Element createElement(FrostUploadItem uploadItem, Document document) {

        Element itemElement = document.createElement("FrostUploadTableItem");
        Element element;
        Text text;
        CDATASection cdata;
        // filename
        element = document.createElement("filename");
        cdata = document.createCDATASection(uploadItem.getFileName());
        element.appendChild(cdata);
        itemElement.appendChild(element);
        // filepath
        element = document.createElement("filepath");
        cdata = document.createCDATASection(uploadItem.getFilePath());
        element.appendChild(cdata);
        itemElement.appendChild(element);
        // targetboard
        element = document.createElement("targetboard");
        text = document.createTextNode(uploadItem.getTargetBoard().getName());
        element.appendChild(text);
        itemElement.appendChild(element);
        // state
        element = document.createElement("state");
        text = document.createTextNode(String.valueOf(uploadItem.getState()));
        element.appendChild(text);
        itemElement.appendChild(element);
        // batch -all upload elements have it
        element = document.createElement("batch");
        text = document.createTextNode(uploadItem.getBatch());
        element.appendChild(text);
        itemElement.appendChild(element);
        // key
        if (uploadItem.getKey() != null) {
            element = document.createElement("key");
            cdata = document.createCDATASection(uploadItem.getKey());
            element.appendChild(cdata);
            itemElement.appendChild(element);
        }
        // sha1
        if (uploadItem.getSHA1() != null) {
            element = document.createElement("SHA1");
            cdata = document.createCDATASection(uploadItem.getSHA1());
            element.appendChild(cdata);
            itemElement.appendChild(element);
        }
        // lastUploadDate
        if (uploadItem.getLastUploadDate() != null) {
            element = document.createElement("lastuploaddate");
            text = document.createTextNode(uploadItem.getLastUploadDate());
            element.appendChild(text);
            itemElement.appendChild(element);
        }
        // enabled
        String enabled;
        if (uploadItem.isEnabled() == null) {
            enabled = "true";
        } else {
            enabled = uploadItem.isEnabled().toString();
        }
        itemElement.setAttribute("enabled", enabled);
        // retries
        element = document.createElement("retries");
        text = document.createTextNode(String.valueOf(uploadItem.getRetries()));
        element.appendChild(text);
        itemElement.appendChild(element);

        return itemElement;
    }

    /*
     * (non-Javadoc)
     *
     * @see frost.fileTransfer.upload.UploadModelDAO#create()
     */
    public void create() throws StorageException {
        File xmlFile = new File(directory + XML_FILENAME);
        try {
            boolean success = xmlFile.createNewFile();
            if (!success) {
                throw new StorageException("There was a problem while creating the storage.");
            }
        } catch (IOException ioe) {
            throw new StorageException("There was a problem while creating the storage.", ioe);
        }
    }

}
