/*
 MessageHashesXmlDAO.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.storage.*;
import frost.util.*;

public class MessageHashesXmlDAO implements MessageHashesDAO {

    private static final Logger logger = Logger.getLogger(MessageHashesXmlDAO.class.getName());

    private static final String XML_FILENAME = "hashes.xml";
    private static final String TMP_FILENAME = "hashes.xml.tmp";
    private static final String BAK_FILENAME = "hashes.xml.bak";

    /* (non-Javadoc)
     * @see frost.messaging.MessageHashesDAO#exists()
     */
    public boolean exists() {
        File xmlFile = new File(XML_FILENAME);
        if (xmlFile.length() == 0) {
            xmlFile.delete();
        }
        if (xmlFile.isFile()) {
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see frost.messaging.MessageHashesDAO#load(frost.messaging.MessageHashes)
     */
    public void load(MessageHashes messageHashes) throws StorageException {

        if( exists() == false ) {
            return;
        }

        try {
            logger.info("Loading " + XML_FILENAME);
            Document doc = XMLTools.parseXmlFile(XML_FILENAME, false);
            Element rootNode = doc.getDocumentElement();

            if (rootNode.getTagName().equals("MessageHashes") == false) {
                throw new StorageException("The message hashes XML file is invalid: does not contain the root tag MessageHashes.");
            }

            // check if rootnode contains only a single entry wich must be MessageHashesList
            List nodelist = XMLTools.getChildElementsByTagName(rootNode, "MessageHashesList");
            if (nodelist.size() != 1) {
                throw new StorageException("The message hashes XML file is invalid: MessageHashesList not found or duplicated.");
            }

            // not longer used, free
            rootNode = null;
            doc = null;

            Element hashesListRootNode = (Element) nodelist.get(0);
            nodelist = XMLTools.getChildElementsByTagName(hashesListRootNode, "MessageHash");
            if (nodelist.size() == 0) {
                logger.info("The message hashes XML file has no hashes.");
            } else {
                // read hashes, first one is the oldest
                for (int x = 0; x < nodelist.size(); x++) {
                    Element element = (Element) nodelist.get(x);

                    String value = element.getAttribute("value");
                    if( value == null || value.length() == 0 ) {
                        CDATASection txtname = (CDATASection)element.getFirstChild();
                        if( txtname == null ) {
                            logger.warning("No hash value found in MessageHash, continuing.");
                        }
                        value = txtname.getData().trim();
                    }
                    messageHashes.add(value);
                }
                logger.info("Loaded "+nodelist.size()+" hashes.");
            }
        } catch (Throwable e) {
            throw new StorageException("Exception while loading the new message hashes format. Starting empty hashes list.", e);
        }
    }

    /* (non-Javadoc)
     * @see frost.messaging.MessageHashesDAO#save(frost.messaging.MessageHashes)
     */
    public void save(MessageHashes messageHashes) throws StorageException {
        logger.info("Saving " + XML_FILENAME);

        // First we copy "hashes.xml" to "hashes.xml.bak"
        File hashesFile = new File(XML_FILENAME);
        if (hashesFile.exists()) {
            File bakFile = new File(BAK_FILENAME);
            bakFile.delete();
            if( !FileAccess.copyFile(XML_FILENAME, BAK_FILENAME) ) {
                logger.log(Level.SEVERE, "Error while copying " + XML_FILENAME + " to " + BAK_FILENAME);
            }
        }

        // prepare xml
        Document doc = XMLTools.createDomDocument();
        Element rootElement = doc.createElement("MessageHashes");
        doc.appendChild(rootElement);
        Element listRoot = doc.createElement("MessageHashesList");
        rootElement.appendChild(listRoot);

        // now add all hashes to listRoot
        Iterator hashes = messageHashes.getHashes();
        while (hashes.hasNext()) {
            String hash = hashes.next().toString();
            Element element = doc.createElement("MessageHash");
            CDATASection cdata = doc.createCDATASection(hash);
            element.appendChild(cdata);
            listRoot.appendChild(element);
        }

        //We delete "hashes.xml.tmp"
        File hashesTmpFile = new File(TMP_FILENAME);
        if (hashesTmpFile.isFile()) {
            hashesTmpFile.delete();
        }
        // We save message hashes to "hashes.xml.tmp"
        if (XMLTools.writeXmlFile(doc, TMP_FILENAME)) {
            //Success
            if (hashesTmpFile.isFile()) {
                //We replace "hashes.xml" by "hashes.xml.tmp"
                hashesFile.delete();
                if (!hashesTmpFile.renameTo(hashesFile)) {
                    //Replacement failed. We try to restore "hashes.xml" from "hashes.xml.bak"
                    if( !FileAccess.copyFile(BAK_FILENAME, XML_FILENAME) ) {
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

    /* (non-Javadoc)
     * @see frost.messaging.MessageHashesDAO#create()
     */
    public void create() throws StorageException {
        File xmlHashes = new File(XML_FILENAME);
        try {
            boolean success = xmlHashes.createNewFile();
            if (!success) {
                throw new StorageException("There was a problem while creating the storage.");
            }
        } catch (IOException ioe) {
            throw new StorageException("There was a problem while creating the storage.", ioe);
        }
    }
}
