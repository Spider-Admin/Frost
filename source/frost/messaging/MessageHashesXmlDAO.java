/*
 * Created on 27-ene-2005
 * 
 */
package frost.messaging;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.storage.StorageException;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessageHashesXmlDAO implements MessageHashesDAO {
	
	private static Logger logger = Logger.getLogger(MessageHashesXmlDAO.class.getName());
	
	private static final String OLD_FILENAME = "hashes";
	private static final String XML_FILENAME = "hashes.xml";
	private static final String TMP_FILENAME = "hashes.xml.tmp";
	private static final String BAK_FILENAME = "hashes.xml.bak";

	/* (non-Javadoc)
	 * @see frost.messaging.MessageHashesDAO#exists()
	 */
	public boolean exists() {
		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
		if (oldFile.length() == 0) {
			oldFile.delete();
		}
		if (xmlFile.length() == 0) {
			xmlFile.delete();
		}
		if (oldFile.exists() || xmlFile.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see frost.messaging.MessageHashesDAO#load(frost.messaging.MessageHashes)
	 */
	public void load(MessageHashes messageHashes) throws StorageException {
		File oldFile = new File(OLD_FILENAME);
		File xmlFile = new File(XML_FILENAME);
		
		if (xmlFile.exists()) {
			try {
				loadNewFormat(messageHashes);
				oldFile.delete();		//In case we have an old file hanging around, we delete it.
			} catch (Exception e) {
				throw new StorageException("Exception while loading the new message hashes format.", e);
			}
		} else {
			try {
				loadOldFormat(messageHashes, oldFile);
			} catch (Exception ioe) {
				throw new StorageException("Exception while loading the old message hashes format.", ioe);
			}
		}		
	}

	/**
	 * @param messageHashes
	 * @param oldFile
	 */
	private void loadOldFormat(MessageHashes messageHashes, File oldFile) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(oldFile));
		HashSet hashesSet = (HashSet) ois.readObject();
		ois.close();
		Iterator hashes = hashesSet.iterator();
		while (hashes.hasNext()) {
			messageHashes.add(hashes.next().toString());
		}
		logger.info("loaded " + hashesSet.size() + " message hashes");
	}

	/**
	 * @param messageHashes
	 */
	private void loadNewFormat(MessageHashes messageHashes) throws SAXException, IllegalArgumentException, StorageException {
		logger.info("Loading " + XML_FILENAME);
		Document doc = XMLTools.parseXmlFile(XML_FILENAME, false);
		Element rootNode = doc.getDocumentElement();
		
		if (rootNode.getTagName().equals("MessageHashes") == false) {
			throw new StorageException(
				"The message hashes XML file is invalid: does not contain the root tag MessageHashes.");
		}
		// check if rootnode contains only a single entry wich must be MessageHashesList
		ArrayList nodelist =
			XMLTools.getChildElementsByTagName(rootNode, "MessageHashesList");
		if (nodelist.size() != 1) {
			throw new StorageException(
				"The message hashes XML file is invalid: MessageHashesList not found or duplicated.");
		}
		
		Element hashesListRootNode = (Element) nodelist.get(0);
		nodelist = XMLTools.getChildElementsByTagName(hashesListRootNode, "MessageHash");
		if (nodelist.size() == 0) {
			logger.info("The message hashes XML file has no hashes.");	
		} else {
			for (int x = 0; x < nodelist.size(); x++) {
				Element element = (Element) nodelist.get(x);
				String value = element.getAttribute("value");
				messageHashes.add(value);
			}
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
			try {
				FileAccess.copyFile(XML_FILENAME, BAK_FILENAME);
			} catch (IOException exception) {
				logger.log(Level.SEVERE, 
							"Error while copying " + XML_FILENAME + " to " + BAK_FILENAME, 
							exception);
			}
		}

		//We delete "hashes.xml.tmp"
		File hashesTmpFile = new File(TMP_FILENAME);
		if (hashesTmpFile.exists()) {
			hashesTmpFile.delete();
		}
		
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
			element.setAttribute("value", hash);
			listRoot.appendChild(element);
		}		
		
		// We save message hashes to "hashes.xml.tmp"
		if (XMLTools.writeXmlFile(doc, TMP_FILENAME)) {
			//Success
			if (hashesTmpFile.exists()) {
				//We replace "hashes.xml" by "hashes.xml.tmp"
				hashesFile.delete();
				if (!hashesTmpFile.renameTo(hashesFile)) {
					//Replacement failed. We try to restore "hashes.xml"
					// from "hashes.xml.bak"
					try {
						FileAccess.copyFile(BAK_FILENAME, XML_FILENAME);
					} catch (IOException exception) {
						//Uh, oh, we are having a bad, bad day.
						throw new StorageException(
								"Error while restoring " + XML_FILENAME, exception);
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
