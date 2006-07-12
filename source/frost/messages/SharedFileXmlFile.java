/*
KeyClass.java / Frost
Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messages;

import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.gui.objects.*;

public class SharedFileXmlFile implements XMLizable
{
    private static Logger logger = Logger.getLogger(SharedFileXmlFile.class.getName());

    private final static String[] invalidChars = {"/", "\\", "?", "*", "<", ">", "\"", ":", "|"};

    // following fields must be unique in the FILELIST
    String SHA1 = null;  //SHA1 of the file
    Long size = new Long(0); // Filesize
    String key = null; // Name of this key

    // following fields can be different in a FrostSharedFileObject
    String lastUploaded = null; // Last uploaded by sender
    String owner = null;  // person that uploaded the file, null=anonymous
    String filename = new String();
    Board board;

    public SharedFileXmlFile() {
    }

    /**Tests if the filename is valid*/
    public boolean checkFilename() {

        if( filename==null || filename.length() == 0 || filename.length() > 255 ) {
            return false;
        }
        for( int i = 0; i < invalidChars.length; i++ ) {
            if( filename.indexOf(invalidChars[i]) != -1 ) {
                return false;
            }
        }
        return true;
    }

    /**Tests if key is valid*/
    public boolean checkKey() {
        if (key == null) {
            return true;
        }
//        if( key.startsWith("CHK@") && key.length() == 58 ) return true;
        if( key.startsWith("CHK@") ) { 
            return true;
        }
        return false;
    }

    /**Returns true if key is valid*/
    private boolean isSharedFileValid() {
        if( size != null && SHA1 != null && checkFilename() && checkKey() ) {
            return true;
        }
        return false;
    }

    /** Set filename */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** Get filename */
    public String getFilename() {
        return filename.trim();
    }

    /** Get SHA1 */
    public String getSHA1() {
        return SHA1;
    }

    /** Set SHA1 */
    public void setSHA1(String s) {
        SHA1 = s;
    }

    /** Set owner */
    public void setOwner(String owner_id) {
        owner = owner_id;
    }

    /** Get owner */
    public String getOwner() {
        return owner;
    }

    /** Set key */
    public void setKey(String key) {
        this.key = key;
    }

    /** Get key */
    public String getKey() {
        if( key == null ) {
            return key;
        }
        return key.trim();
    }

    /** Set date */
    public void setLastUploaded(String date) {
        this.lastUploaded = date;
    }

    /** Get date */
    public String getLastUploaded() {
        if( lastUploaded == null )
            return lastUploaded;
        return lastUploaded.trim();
    }

    /** Set size */
    public void setSize(String size) {
        try {
            this.size = new Long(size);
        } catch (NumberFormatException ex) {
            this.size = null;
        }
    }

    /** Set size */
    public void setSize(long size) {
        this.size = new Long(size);
    }

    /** Get size */
    public Long getSize() {
        return size;
    }

//    /**
//     * Creates a sharedFileObject to be uploaded.
//     * it can be used both from the uploadTable and from attachments.
//     * @param file the file to be uploaded
//     * @param board the board to which index to add the file.  If null, the file will
//     * not be added to any index and won't participate in the request system.
//     */
//    public SharedFileXmlFile(File file, Board board) {
//        SHA1 = Core.getCrypto().digest(file);
//        size = new Long(file.length());
//        filename = file.getName();
//        lastUploaded = DateFun.getDate(); 
//        key = null; // if key == null means file is offline.
//        this.board = board;
//    }

    public Element getXMLElement(Document doc) {

        // we do not add keys who are not signed by people we marked as GOOD!
        // but we add unsigned keys for now; this will probably change soon

        Element fileelement = doc.createElement("File");

        Element element = doc.createElement("name");
        CDATASection cdata = doc.createCDATASection(getFilename());
        element.appendChild(cdata);
        fileelement.appendChild(element);

        element = doc.createElement("SHA1");
        cdata = doc.createCDATASection(getSHA1());
        element.appendChild(cdata);
        fileelement.appendChild(element);

        element = doc.createElement("size");
        Text textnode = doc.createTextNode("" + getSize());
        element.appendChild(textnode);
        fileelement.appendChild(element);

        if( getOwner() != null ) {
            element = doc.createElement("owner");
            cdata = doc.createCDATASection(getOwner());
            element.appendChild(cdata);
            fileelement.appendChild(element);
        }
        if( getKey() != null ) {
            element = doc.createElement("key");
            textnode = doc.createTextNode(getKey());
            element.appendChild(textnode);
            fileelement.appendChild(element);
        }
        if( getLastUploaded() != null ) {
            element = doc.createElement("date");
            textnode = doc.createTextNode(getLastUploaded());
            element.appendChild(textnode);
            fileelement.appendChild(element);
        }
        return fileelement;
    }

    public void loadXMLElement(Element current) throws SAXException {
        setFilename(XMLTools.getChildElementsCDATAValue(current, "name"));
        setSHA1(XMLTools.getChildElementsCDATAValue(current, "SHA1"));
        setOwner(XMLTools.getChildElementsCDATAValue(current, "owner"));
        setKey(XMLTools.getChildElementsTextValue(current, "key"));
        setLastUploaded(XMLTools.getChildElementsTextValue(current, "date"));
        setSize(XMLTools.getChildElementsTextValue(current, "size"));
    }

    /**
     * @return the board this file will be uploaded to, if any
     */
    public Board getBoard() {
        return board;
    }

    /**
     * @param object
     */
    public void setBoard(Board object) {
        board = object;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        SharedFileXmlFile other = (SharedFileXmlFile) obj;
        return SHA1.equals(other.getSHA1());
    }

    /**
     * factory method
     * @param e the element
     * @return the sharedFileObject created according to the element.
     */
    public static SharedFileXmlFile getInstance(Element e, Board board){
        try {
            SharedFileXmlFile result = new SharedFileXmlFile();
            result.loadXMLElement(e);
            if( !result.isSharedFileValid() ) {
                logger.log(Level.SEVERE, "shared file is invalid (missing fields or wrong contents).");
                return null;
            }
            result.setBoard(board);
            return result;
        } catch(SAXException ex) {
            logger.log(Level.SEVERE, "parsing file failed.", ex);
            return null;
        }
    }
}
