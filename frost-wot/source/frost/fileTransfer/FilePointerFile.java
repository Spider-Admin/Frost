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
    public static boolean writePointerFile(final FilePointerFileContent content, final File targetFile) {

        final Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writePointerFile: factory could'nt create XML Document.");
            return false;
        }

        final Element rootElement = doc.createElement(TAG_FrostFilePointerFile);
        doc.appendChild(rootElement);

        final Element timeStampElement = doc.createElement(TAG_timestamp);
        final Text timeStampText = doc.createTextNode( Long.toString(content.getTimestamp()) );
        timeStampElement.appendChild(timeStampText);
        rootElement.appendChild( timeStampElement );

        final Element rootChkElement = doc.createElement(TAG_CHKKeys);
        rootElement.appendChild( rootChkElement );

        for( final String chkKey : content.getChkKeyStrings() ) {

            final Element nameElement = doc.createElement(TAG_chk);
            final Text text = doc.createTextNode( chkKey );
            nameElement.appendChild( text );

            rootChkElement.appendChild( nameElement );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in writePointerFile/writeXmlFile", t);
        }

        return writeOK;
    }

    /**
     * @param sourceFile  File to read from
     * @return  List of String objects with the CHK keys
     */
    public static FilePointerFileContent readPointerFile(final File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        }
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(sourceFile.getPath());
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Exception in readPointerFile, during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the pointer file");
            return null;
        }

        final Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals(TAG_FrostFilePointerFile) == false ) {
            logger.severe("Error: xml pointer file does not contain the root tag '"+TAG_FrostFilePointerFile+"'");
            return null;
        }

        final String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, TAG_timestamp);
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_timestamp+"'");
            return null;
        }
        final long timestamp = Long.parseLong(timeStampStr);

        final List<Element> nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_CHKKeys);
        if( nodelist.size() != 1 ) {
            logger.severe("Error: xml pointer files must contain only one element '"+TAG_CHKKeys+"'");
            return null;
        }

        final Element rootChkNode = nodelist.get(0);

        final List<String> chkKeyList = new LinkedList<String>();
        final List<Element> xmlKeys = XMLTools.getChildElementsByTagName(rootChkNode, TAG_chk);
        for( final Element el : xmlKeys ) {

            final Text txtname = (Text) el.getFirstChild();
            if( txtname == null ) {
                continue;
            }

            final String chkKey = txtname.getData();
            chkKeyList.add(chkKey);
        }

        final FilePointerFileContent content = new FilePointerFileContent(timestamp, chkKeyList);
        return content;
    }
}
