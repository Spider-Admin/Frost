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
package frost.fileTransfer.filerequest;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

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
    public static boolean writeRequestFile(final FileRequestFileContent content, final File targetFile) {

        final Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - writeRequestFile: factory could'nt create XML Document.");
            return false;
        }

        final Element rootElement = doc.createElement(TAG_FrostFileRequestFile);
        doc.appendChild(rootElement);

        final Element timeStampElement = doc.createElement(TAG_timestamp);
        final Text timeStampText = doc.createTextNode( Long.toString(content.getTimestamp()) );
        timeStampElement.appendChild(timeStampText);
        rootElement.appendChild( timeStampElement );

        final Element rootChkElement = doc.createElement(TAG_shaList);
        rootElement.appendChild( rootChkElement );

        for( final String chkKey : content.getShaStrings() ) {

            final Element nameElement = doc.createElement(TAG_sha);
            final Text text = doc.createTextNode( chkKey );
            nameElement.appendChild( text );

            rootChkElement.appendChild( nameElement );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, targetFile);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception in writeRequestFile/writeXmlFile", t);
        }

        return writeOK;
    }

    public static void main(final String[] args) {
        final File targetFile = new File("D:\\abc.def");
        final File tmp = new File(targetFile.getPath() + ".frftmp");
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
    public static FileRequestFileContent readRequestFile(final File sourceFile) {
        if( !sourceFile.isFile() || !(sourceFile.length() > 0) ) {
            return null;
        }
        Document d = null;
        try {
            d = XMLTools.parseXmlFile(sourceFile.getPath());
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Exception in readRequestFile, during XML parsing", t);
            return null;
        }

        if( d == null ) {
            logger.log(Level.SEVERE, "Could'nt parse the request file");
            return null;
        }

        final Element rootNode = d.getDocumentElement();

        if( rootNode.getTagName().equals(TAG_FrostFileRequestFile) == false ) {
            logger.severe("Error: xml request file does not contain the root tag '"+TAG_FrostFileRequestFile+"'");
            return null;
        }

        final String timeStampStr = XMLTools.getChildElementsTextValue(rootNode, TAG_timestamp);
        if( timeStampStr == null ) {
            logger.severe("Error: xml file does not contain the tag '"+TAG_timestamp+"'");
            return null;
        }
        final long timestamp = Long.parseLong(timeStampStr);

        final List<Element> nodelist = XMLTools.getChildElementsByTagName(rootNode, TAG_shaList);
        if( nodelist.size() != 1 ) {
            logger.severe("Error: xml request files must contain only one element '"+TAG_shaList+"'");
            return null;
        }

        final Element rootShaNode = nodelist.get(0);

        final List<String> shaList = new LinkedList<String>();
        final List<Element> xmlKeys = XMLTools.getChildElementsByTagName(rootShaNode, TAG_sha);
        for( final Element el : xmlKeys ) {

            final Text txtname = (Text) el.getFirstChild();
            if( txtname == null ) {
                continue;
            }

            final String sha = txtname.getData();
            shaList.add(sha);
        }

        final FileRequestFileContent content = new FileRequestFileContent(timestamp, shaList);
        return content;
    }
}
