/*
 FilePointerFile.java / Frost
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
package frost.fileTransfer;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;

/**
 * Reads and write pointer files containing CHK keys.
 * 
 * XML format:
 * 
 * <FrostFilePointerFile>
 *   <timestamp>...</timestamp>
 *   <CHKKeys>
 *     <chk>...</chk>
 *     <chk>...</chk>
 *   </CHKKeys>
 * </FrostFilePointerFile>
 */
public class FilePointerFile {

    private static Logger logger = Logger.getLogger(FilePointerFile.class.getName());

    /**
     * @param chkKeys  List of String objects with the CHK keys
     * @param targetFile  target file
     * @return  true if write was successful
     */
    public static boolean writePointerFile(FilePointerFileContent content, File targetFile) {
        
        Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writePointerFile: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostFilePointerFile");
        doc.appendChild(rootElement);
        
        Element timeStampElement = doc.createElement("timestamp");
        Text timeStampText = doc.createTextNode( ""+content.getTimestamp() );
        timeStampElement.appendChild(timeStampText);
        rootElement.appendChild( timeStampElement );

        Element rootChkElement = doc.createElement("CHKKeys");
        rootElement.appendChild( rootChkElement );
        
        for( Iterator i = content.getChkKeyStrings().iterator(); i.hasNext(); ) {
            String chkKey = (String) i.next();
            
            Element nameElement = doc.createElement("chk");
            Text text = doc.createTextNode( chkKey );
            nameElement.appendChild( text );

            rootChkElement.appendChild( nameElement );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in writePointerFile/writeXmlFile", t);
        }
        return writeOK;
    }
    
    /**
     * @param source  File to read from
     * @return  List of String objects with the CHK keys
     */
    public static FilePointerFileContent readPointerFile(File source) {
        if( !source.isFile() || !(source.length() > 0) ) {
            return null;
        } 
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(source.getPath(), false);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in readPointerFile, during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the pointer file");
            return null;
        }
        
        Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals("FrostFilePointerFile") == false ) {
            logger.severe("Error: xml pointer file does not contain the root tag 'FrostFilePointerFile'");
            return null;
        }

        String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, "timestamp");
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag 'timestamp'");
            return null;
        }
        long timestamp = Long.parseLong(timeStampStr);
        
        List nodelist = XMLTools.getChildElementsByTagName(rootNode, "CHKKeys");
        if( nodelist.size() != 1 ) {
            logger.severe("Error: xml pointer files must contain only one element 'CHKKeys'");
            return null;
        }
        
        Element rootChkNode = (Element)nodelist.get(0);
        
        List chkKeyList = new LinkedList();
        List xmlKeys = XMLTools.getChildElementsByTagName(rootChkNode, "chk");
        for( Iterator i = xmlKeys.iterator(); i.hasNext(); ) {
            Element el = (Element) i.next();
            
            Text txtname = (Text) el.getFirstChild();
            if( txtname == null ) {
                continue;
            }
            
            String chkKey = txtname.getData();
            chkKeyList.add(chkKey);
        }
        
        FilePointerFileContent content = new FilePointerFileContent(timestamp, chkKeyList); 
        return content;
    }
}
