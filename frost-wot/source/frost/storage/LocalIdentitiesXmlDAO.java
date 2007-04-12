/*
  LocalIdentitiesXmlDAO.java / Frost
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
package frost.storage;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.identities.*;
import frost.util.*;

public class LocalIdentitiesXmlDAO {

    private static final Logger logger = Logger.getLogger(KnownBoardsXmlDAO.class.getName());

    public static List<LocalIdentity> loadLocalidentities(File file) {

        LinkedList<LocalIdentity> localIdentities = new LinkedList<LocalIdentity>();
        if( file.exists() ) {
            Document doc = null;
            try {
                doc = XMLTools.parseXmlFile(file, false);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error reading localidentities xml", ex);
                return localIdentities;
            }
            Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostLocalIdentities") == false ) {
                logger.severe("Error - invalid localidentities xml: does not contain the root tag 'FrostLocalIdentities'");
                return null;
            }
            
            List localIdentitiesElements = XMLTools.getChildElementsByTagName(rootNode, "MyIdentity");
            for( Iterator i = localIdentitiesElements.iterator(); i.hasNext(); ) {
                Element lidElement = (Element) i.next();
                LocalIdentity myId = new LocalIdentity(lidElement);
                localIdentities.add(myId);
            }
        }
        return localIdentities;
    }

    /**
     * ATTN: appends private key!
     */
    public static boolean saveLocalIdentities(File file, List<LocalIdentity> localIdentities) {
        Document doc = XMLTools.createDomDocument();
        if (doc == null) {
            logger.severe("Error - saveLocalIdentities: factory couldn't create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostLocalIdentities");
        doc.appendChild(rootElement);

        Iterator i = localIdentities.iterator();
        while (i.hasNext()) {
            LocalIdentity b = (LocalIdentity)i.next();
            Element anAttachment = b.getExportXMLElement(doc);
            rootElement.appendChild(anAttachment);
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, file.getPath());
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Exception while writing localidentities xml:", ex);
        }
        if (!writeOK) {
            logger.severe("Error exporting localidentities, file was not saved");
        }
        return writeOK;
    }
}
