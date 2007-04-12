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

import frost.fcp.*;
import frost.util.*;

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

    private static final Logger logger = Logger.getLogger(FilePointerFile.class.getName());
    
    private static final String TAG_FrostFilePointerFile = "FrostFilePointerFile";
    private static final String TAG_timestamp = "timestamp";
    private static final String TAG_CHKKeys = "CHKKeys";
    private static final String TAG_chk = "chk";

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

        Element rootElement = doc.createElement(TAG_FrostFilePointerFile);
        doc.appendChild(rootElement);
        
        Element timeStampElement = doc.createElement(TAG_timestamp);
        Text timeStampText = doc.createTextNode( Long.toString(content.getTimestamp()) );
        timeStampElement.appendChild(timeStampText);
        rootElement.appendChild( timeStampElement );

        Element rootChkElement = doc.createElement(TAG_CHKKeys);
        rootElement.appendChild( rootChkElement );
        
        for( Iterator i = content.getChkKeyStrings().iterator(); i.hasNext(); ) {
            String chkKey = (String) i.next();
            
            Element nameElement = doc.createElement(TAG_chk);
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
        
        // compress file if running on 0.5
        if( writeOK && FcpHandler.isFreenet05() ) {
            File tmp = new File(targetFile.getPath() + ".wfpftmp");
            if( !FileAccess.compressFileGZip(targetFile, tmp) ) {
                return false; // error, already logged
            }
            targetFile.delete();
            if( !tmp.renameTo(targetFile) ) {
                logger.severe("Error: rename failed: "+tmp.getPath()+"','"+targetFile.getPath()+"'");
                return false;
            }
        }

        return writeOK;
    }
    
    /**
     * @param sourceFile  File to read from
     * @return  List of String objects with the CHK keys
     */
    public static FilePointerFileContent readPointerFile(File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        }
        // decompress file if running on 0.5
        if( FcpHandler.isFreenet05() ) {
            File tmp = new File(sourceFile.getPath() + ".fpftmp");
            if( !FileAccess.decompressFileGZip(sourceFile, tmp) ) {
                return null; // error, already logged
            }
            sourceFile.delete();
            if( !tmp.renameTo(sourceFile) ) {
                logger.severe("Error: rename failed: "+tmp.getPath()+"','"+sourceFile.getPath()+"'");
                return null;
            }
        }
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(sourceFile.getPath(), false);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in readPointerFile, during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the pointer file");
            return null;
        }
        
        Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals(TAG_FrostFilePointerFile) == false ) {
            logger.severe("Error: xml pointer file does not contain the root tag '"+TAG_FrostFilePointerFile+"'");
            return null;
        }

        String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, TAG_timestamp);
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_timestamp+"'");
            return null;
        }
        long timestamp = Long.parseLong(timeStampStr);
        
        List nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_CHKKeys);
        if( nodelist.size() != 1 ) {
            logger.severe("Error: xml pointer files must contain only one element '"+TAG_CHKKeys+"'");
            return null;
        }
        
        Element rootChkNode = (Element)nodelist.get(0);
        
        List<String> chkKeyList = new LinkedList<String>();
        List xmlKeys = XMLTools.getChildElementsByTagName(rootChkNode, TAG_chk);
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
