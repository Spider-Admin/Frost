/*
  FileListFile.java / Frost
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
import frost.identities.*;

/**
 * Signs and writes file list files into an XML file.
 * Reads and validates file list files from an XML file.
 */
public class FileListFile {

    private static Logger logger = Logger.getLogger(FileListFile.class.getName());
    
    /**
     * sign content and create an xml file
     * @param files  List of ... objects
     * @param targetFile  target file
     * 
     * XML format:
     * <FrostFileListFile>
     *   <timestamp>...</timestamp>
     *   <Identity>....</Identity>
     *   <sign>...</sign>
     *   <files>
     *   ...
     *   </files>
     * </FrostFileListFile>
     */
    public static boolean writeFileListFile(FileListFileContent content, File targetFile) {

        Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writeFileListFile: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostFileListFile");
        doc.appendChild(rootElement);

        {
            Element timeStampElement = doc.createElement("timestamp");
            Text timeStampText = doc.createTextNode( ""+content.getTimestamp() );
            timeStampElement.appendChild(timeStampText);
            rootElement.appendChild( timeStampElement );
        }
        {
            Element _sharer = ((Identity)content.getSendOwner()).getXMLElement(doc);
            rootElement.appendChild(_sharer);
        }
        {
            String signContent = getSignableContent(
                    content.getFileList(), 
                    content.getSendOwner().getUniqueName(),
                    content.getTimestamp());
            String sig = Core.getCrypto().detachedSign(signContent, content.getSendOwner().getPrivKey());
            if( sig == null ) {
                return false;
            }
    
            Element element = doc.createElement("sign");
            CDATASection cdata = doc.createCDATASection(sig);
            element.appendChild(cdata);
            rootElement.appendChild(element);
        }
        {
            Element filesElement = doc.createElement("files");
            
            // Iterate through set of files and add them all
            for(Iterator i = content.getFileList().iterator(); i.hasNext(); ) {
                SharedFileXmlFile current = (SharedFileXmlFile)i.next();
                Element currentElement = current.getXMLElement(doc);
                filesElement.appendChild(currentElement);
            }
            
            rootElement.appendChild(filesElement);
        }        

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception in writeFileListFile/writeXmlFile", t);
        }
        return writeOK;
    }
    
    /**
     * @return  content if file is read and signature is valid, otherwise null
     */
    public static FileListFileContent readFileListFile(File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        } 
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(sourceFile.getPath(), false);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the file");
            return null;
        }
        
        Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals("FrostFileListFile") == false ) {
            logger.severe("Error: xml file does not contain the root tag 'FrostFileListFile'");
            return null;
        }
        
        String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, "timestamp");
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag 'timestamp'");
            return null;
        }
        long timestamp = Long.parseLong(timeStampStr);
        
        String signature = XMLTools.getChildElementsCDATAValue(rootNode, "sign");
        if( signature == null ) {
            logger.severe("Error: xml file does not contain the tag 'sign'");
            return null;
        }
        
        Element identityNode = null;
        Element filesNode = null;
        {        
            List nodelist = XMLTools.getChildElementsByTagName(rootNode, "Identity");
            if( nodelist.size() != 1 ) {
                logger.severe("Error: xml files must contain exactly one element 'Identity'");
                return null;
            }
            identityNode = (Element)nodelist.get(0);
    
            nodelist = XMLTools.getChildElementsByTagName(rootNode, "files");
            if( nodelist.size() != 1 ) {
                logger.severe("Error: xml files must contain exactly one element 'files'");
                return null;
            }
            filesNode = (Element)nodelist.get(0);
        }
        LinkedList files = new LinkedList();
        {        
            List _files = XMLTools.getChildElementsByTagName(filesNode, "File");
            for( Iterator i = _files.iterator(); i.hasNext(); ) {
                Element el = (Element) i.next();
                SharedFileXmlFile file = SharedFileXmlFile.getInstance(el);
                if( file == null ) {
                    logger.severe("Error: invalid shared file ignored");
                    continue;
                }
                files.add( file );
            }
        }

        Identity owner = new Identity(identityNode);
        
        String signContent = getSignableContent(files, owner.getUniqueName(), timestamp);
        boolean sigIsValid = Core.getCrypto().detachedVerify(signContent, owner.getKey(), signature);
        if( !sigIsValid ) {
            logger.severe("Error: invalid file signature from owner "+owner.getUniqueName());
            return null;
        }
        
        // all is valid
        FileListFileContent content = new FileListFileContent(timestamp, owner, files);
        return content;
    }
    
    private static String getSignableContent(LinkedList files, String owner, long timestamp) {
        StringBuffer signContent = new StringBuffer();
        signContent.append(owner);
        signContent.append(timestamp);
        for(Iterator i = files.iterator(); i.hasNext(); ) {
            SharedFileXmlFile sfo = (SharedFileXmlFile)i.next();
            signContent.append( sfo.getSha() );
            signContent.append( sfo.getFilename() );
            signContent.append( sfo.getSize().toString() );
            
            if( sfo.getKey() != null ) {
                signContent.append( sfo.getKey() );
            }
            if( sfo.getLastUploaded() != null ) {
                signContent.append( sfo.getLastUploaded() );
            }
            if( sfo.getComment() != null ) {
                signContent.append( sfo.getComment() );
            }
            if( sfo.getKeywords() != null ) {
                signContent.append( sfo.getKeywords() );
            }
        }
        return signContent.toString();
    }
}
