/*
 FileRequestFile.java / Frost
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
 * Reads and write request files containing SHA keys.
 * 
 * XML format:
 * 
 * <FrostFileRequestFile>
 *   <timestamp>...</timestamp>
 *   <shaList>
 *     <sha>...</sha>
 *     <sha>...</sha>
 *   </shaList>
 * </FrostRequestFile>
 */
public class FileRequestFile {

    private static final Logger logger = Logger.getLogger(FileRequestFile.class.getName());

    private static final String TAG_FrostFileRequestFile = "FrostFileRequestFile";
    private static final String TAG_timestamp = "timestamp";
    private static final String TAG_shaList = "shaList";
    private static final String TAG_sha = "sha";

    /**
     * @param chkKeys  List of String objects with the shas
     * @param targetFile  target file
     * @return  true if write was successful
     */
    public static boolean writeRequestFile(FileRequestFileContent content, File targetFile) {
        
        Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writeRequestFile: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement(TAG_FrostFileRequestFile);
        doc.appendChild(rootElement);

        Element timeStampElement = doc.createElement(TAG_timestamp);
        Text timeStampText = doc.createTextNode( Long.toString(content.getTimestamp()) );
        timeStampElement.appendChild(timeStampText);
        rootElement.appendChild( timeStampElement );

        Element rootChkElement = doc.createElement(TAG_shaList);
        rootElement.appendChild( rootChkElement );
        
        for( String chkKey : content.getShaStrings() ) {
            
            Element nameElement = doc.createElement(TAG_sha);
            Text text = doc.createTextNode( chkKey );
            nameElement.appendChild( text );

            rootChkElement.appendChild( nameElement );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in writeRequestFile/writeXmlFile", t);
        }
        
        // compress file if running on 0.5
        if( writeOK && FcpHandler.isFreenet05() ) {
            File tmp = new File(targetFile.getPath() + ".frftmp");
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
    
    public static void main(String[] args) {
        File targetFile = new File("D:\\abc.def");
        File tmp = new File(targetFile.getPath() + ".frftmp");
        if( !FileAccess.compressFileGZip(targetFile, tmp) ) {
            return; // error
        }
        targetFile.delete();
        tmp.renameTo(targetFile);
    }
    
    /**
     * @param sourceFile  File to read from
     * @return  List of String objects with the shas
     */
    public static FileRequestFileContent readRequestFile(File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        }
        // decompress file if running on 0.5
        if( FcpHandler.isFreenet05() ) {
            File tmp = new File(sourceFile.getPath() + ".frftmp");
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
            logger.log(Level.SEVERE, "Exception in readRequestFile, during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the request file");
            return null;
        }
        
        Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals(TAG_FrostFileRequestFile) == false ) {
            logger.severe("Error: xml request file does not contain the root tag '"+TAG_FrostFileRequestFile+"'");
            return null;
        }
        
        String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, TAG_timestamp);
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_timestamp+"'");
            return null;
        }
        long timestamp = Long.parseLong(timeStampStr);
        
        List<Element> nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_shaList);
        if( nodelist.size() != 1 ) {
            logger.severe("Error: xml request files must contain only one element '"+TAG_shaList+"'");
            return null;
        }
        
        Element rootShaNode = (Element)nodelist.get(0);
        
        List<String> shaList = new LinkedList<String>();
        List<Element> xmlKeys = XMLTools.getChildElementsByTagName(rootShaNode, TAG_sha);
        for( Element el : xmlKeys ) {
            
            Text txtname = (Text) el.getFirstChild();
            if( txtname == null ) {
                continue;
            }
            
            String sha = txtname.getData();
            shaList.add(sha);
        }
        
        FileRequestFileContent content = new FileRequestFileContent(timestamp, shaList); 
        return content;
    }
}
