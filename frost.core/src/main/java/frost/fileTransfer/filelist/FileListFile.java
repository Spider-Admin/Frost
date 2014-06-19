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
package frost.fileTransfer.filelist;

import java.io.File;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.Core;
import frost.fileTransfer.SharedFileXmlFile;
import frost.identities.Identity;
import frost.util.XMLTools;

/**
 * Signs and writes file list files into an XML file.
 * Reads and validates file list files from an XML file.
 *
 * XML format:
 *
 * <FrostFileListFile>
 *   <timestamp>...</timestamp>
 *   <Identity>....</Identity>
 *   <sign>...</sign>
 *   <files>
 *   ...
 *   </files>
 * </FrostFileListFile>
 */
public class FileListFile {

    private static final Logger logger = Logger.getLogger(FileListFile.class.getName());

    private static final String TAG_FrostFileListFile = "FrostFileListFile";
    private static final String TAG_timestamp = "timestamp";
    private static final String TAG_sign = "sign";
    private static final String TAG_File = "File";
    private static final String TAG_files = "files";
    private static final String TAG_Identity = "Identity";

    /**
     * sign content and create an xml file
     * @param files  List of ... objects
     * @param targetFile  target file
     */
    public static boolean writeFileListFile(final FileListFileContent content, final File targetFile) {

        final Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writeFileListFile: factory could'nt create XML Document.");
            return false;
        }

        final Element rootElement = doc.createElement(TAG_FrostFileListFile);
        doc.appendChild(rootElement);

        {
            final Element timeStampElement = doc.createElement(TAG_timestamp);
            final Text timeStampText = doc.createTextNode( Long.toString(content.getTimestamp()) );
            timeStampElement.appendChild(timeStampText);
            rootElement.appendChild( timeStampElement );
        }
        {
            final Element _sharer = ((Identity)content.getSendOwner()).getXMLElement(doc);
            rootElement.appendChild(_sharer);
        }
        {
            final String signContent = getSignableContent(
                    content.getFileList(),
                    content.getSendOwner().getUniqueName(),
                    content.getTimestamp());
            final String sig = Core.getCrypto().detachedSign(signContent, content.getSendOwner().getPrivateKey());
            if( sig == null ) {
                return false;
            }

            final Element element = doc.createElement(TAG_sign);
            final CDATASection cdata = doc.createCDATASection(sig);
            element.appendChild(cdata);
            rootElement.appendChild(element);
        }
        {
            final Element filesElement = doc.createElement(TAG_files);

            // Iterate through set of files and add them all
            for( final SharedFileXmlFile current : content.getFileList() ) {
                final Element currentElement = current.getXMLElement(doc);
                filesElement.appendChild(currentElement);
            }

            rootElement.appendChild(filesElement);
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in writeFileListFile/writeXmlFile", t);
        }

        return writeOK;
    }

    /**
     * @return  content if file is read and signature is valid, otherwise null
     */
    public static FileListFileContent readFileListFile(final File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        }
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(sourceFile.getPath());
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Exception during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the file");
            return null;
        }

        final Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals(TAG_FrostFileListFile) == false ) {
            logger.severe("Error: xml file does not contain the root tag '"+TAG_FrostFileListFile+"'");
            return null;
        }

        final String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, TAG_timestamp);
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_timestamp+"'");
            return null;
        }
        final long timestamp = Long.parseLong(timeStampStr);

        final String signature = XMLTools.getChildElementsCDATAValue(rootNode, TAG_sign);
        if( signature == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_sign+"'");
            return null;
        }

        Element identityNode = null;
        Element filesNode = null;
        {
            List<Element> nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_Identity);
            if( nodelist.size() != 1 ) {
                logger.severe("Error: xml files must contain exactly one element '"+TAG_Identity+"'");
                return null;
            }
            identityNode = nodelist.get(0);

            nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_files);
            if( nodelist.size() != 1 ) {
                logger.severe("Error: xml files must contain exactly one element '"+TAG_files+"'");
                return null;
            }
            filesNode = nodelist.get(0);
        }

        final LinkedList<SharedFileXmlFile> files = new LinkedList<SharedFileXmlFile>();
        {
            final List<Element> _files = XMLTools.getChildElementsByTagName(filesNode, TAG_File);
            for( final Element el : _files ) {
                final SharedFileXmlFile file = SharedFileXmlFile.getInstance(el);
                if( file == null ) {
                    logger.severe("Error: shared files xml parsing failed, most likely the signature verification will fail!");
                    continue;
                }
                files.add( file );
            }
        }

        final Identity owner = Identity.createIdentityFromXmlElement(identityNode);
        if( owner == null ) {
            logger.severe("Error: invalid identity information");
            return null;
        }

        if( !Core.getIdentitiesManager().isNewIdentityValid(owner) ) {
            // hash of public key does not match the unique name
            logger.severe("Error: identity failed verification, file list from owner: "+owner.getUniqueName());
            return null;
        }

        final String signContent = getSignableContent(files, owner.getUniqueName(), timestamp);
        final boolean sigIsValid = Core.getCrypto().detachedVerify(signContent, owner.getPublicKey(), signature);
        if( !sigIsValid ) {
            logger.severe("Error: invalid file signature from owner "+owner.getUniqueName());
            return null;
        }

        // check each file for validity
        for(final Iterator<SharedFileXmlFile> i=files.iterator(); i.hasNext(); ) {
            final SharedFileXmlFile file = i.next();
            if( !file.isSharedFileValid() ) {
                final String txt = "Shared file is invalid (missing fields or wrong contents):"+
                             "\n  size="+file.getSize()+
                             "\n  sha="+file.getSha()+
                             "\n  name="+file.getFilename()+
                             "\n  key="+file.getKey();
                logger.log(Level.SEVERE, txt);
                i.remove();
            }
        }

        // all is valid
        final FileListFileContent content = new FileListFileContent(timestamp, owner, files);
        return content;
    }

    private static String getSignableContent(final LinkedList<SharedFileXmlFile> files, final String owner, final long timestamp) {
        final StringBuilder signContent = new StringBuilder();
        signContent.append(owner);
        signContent.append(timestamp);
        for( final SharedFileXmlFile sfo : files ) {
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
