/*
  MessageObject.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.io.File;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.gui.objects.FrostBoardObject;

public class MessageObject implements XMLizable 
{
	AttachmentList attachments; // this is never null!
	
	public Element getXMLElement(Document d){
		return messageObjectPopulateElement(d);
	}
	
	protected Element messageObjectPopulateElement(Document d){
		Element el = d.createElement("FrostMessage");
		
		CDATASection cdata;
		Element current;
		
		//from
		current = d.createElement("From");
		cdata = d.createCDATASection(getFrom());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//subject
		current = d.createElement("Subject");
		cdata = d.createCDATASection(getSubject());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//date
		current = d.createElement("Date");
		cdata = d.createCDATASection(getDate());
		current.appendChild(cdata);
		el.appendChild(current);
		
		 //time
		 current = d.createElement("Time");
		 cdata = d.createCDATASection(getTime());
		 current.appendChild(cdata);
		 el.appendChild(current);
		 
		//body
		current = d.createElement("Body");
		cdata = d.createCDATASection(getContent());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//board
		current = d.createElement("Board");
		cdata = d.createCDATASection(getBoard());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//public Key
		if (publicKey!=null) {
			current = d.createElement("pubKey");
			cdata = d.createCDATASection(getPublicKey());
			current.appendChild(cdata);
			el.appendChild(current);
		}
	
		//attachments
        if( attachments.size() > 0 )
        {
            el.appendChild(attachments.getXMLElement(d));
        }
		
		return el;
	}
	
	public void loadXMLElement(Element e) throws SAXException {
		messageObjectPopulateFromElement(e);
	}
	
	protected void messageObjectPopulateFromElement(Element e)
		throws SAXException {
			
			from = XMLTools.getChildElementsCDATAValue(e,"From");
			date = XMLTools.getChildElementsCDATAValue(e,"Date");
			subject = XMLTools.getChildElementsCDATAValue(e,"Subject");
			time = XMLTools.getChildElementsCDATAValue(e,"Time");
			publicKey = XMLTools.getChildElementsCDATAValue(e,"pubKey");
			board = XMLTools.getChildElementsCDATAValue(e,"Board");
			content = XMLTools.getChildElementsCDATAValue(e,"Body");
			
            List l = XMLTools.getChildElementsByTagName(e,"AttachmentList");
            if( l.size() > 0 )
            {
                Element _attachments = (Element)l.get(0); 
                attachments = new AttachmentList();
                attachments.loadXMLElement(_attachments);
            }
	}

    static final char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _
			//FIXME: this one is missing the "?" char as opposed to mixed.makeFilename
    String board, content, from, subject, date, time, index, publicKey, newContent;
    
    File file;

  /**
   * Creates a Vector of Vectors which contains data for the
   * attached files table.
   */    
    public Vector getFileAttachments() {
        Vector table = new Vector();
        AttachmentList boards = attachments.getAllOfType(Attachment.FILE);
        Iterator i = boards.iterator();
        while(i.hasNext())
        {
            FileAttachment fa = (FileAttachment)i.next();
            SharedFileObject sfo = fa.getFileObj();
            
            if( sfo.getKey() != null && sfo.getKey().length() > 40 &&
                sfo.getFilename() != null && sfo.getFilename().length() > 0 )
            {
                Vector rows = new Vector();
                rows.add(sfo.getFilename());
                rows.add(sfo.getKey());
                table.add(rows);
            }
        }
        return table;
    }

    /**
     * Creates raw string for each attached board in format
     * boardname * pubkey|"N/A" * privkey|"N/A"
     *  
     * @return list of the attached boards as raw strings
     */
	public Collection getBoardsAsStrings(){
		Collection result = new Vector();
        AttachmentList boards = attachments.getAllOfType(Attachment.BOARD);
        Iterator i = boards.iterator();
        while(i.hasNext())
        {
            BoardAttachment ba = (BoardAttachment)i.next();
            StringBuffer sb = new StringBuffer();
            FrostBoardObject aBoard = ba.getBoardObj();
            String pubkey = (aBoard.getPublicKey()==null)?"N/A":aBoard.getPublicKey();
            String privkey = (aBoard.getPrivateKey()==null)?"N/A":aBoard.getPrivateKey();
            sb.append(aBoard.getBoardName())
              .append(" * ")
              .append(pubkey)
              .append(" * ")
              .append(privkey);
              
            result.add( sb.toString() );
        }
		return result;
	}

    /**
     * Creates a Vector of Vectors which contains data for the
     * attached boards table.
     */    
    public Vector getBoardAttachments()
    {
        Vector table = new Vector();
        AttachmentList boards = attachments.getAllOfType(Attachment.BOARD);
        Iterator i = boards.iterator();
        while(i.hasNext())
        {
            BoardAttachment ba = (BoardAttachment)i.next();
            FrostBoardObject aBoard = ba.getBoardObj();
            String pubkey = (aBoard.getPublicKey()==null)?"N/A":aBoard.getPublicKey();
            String privkey = (aBoard.getPrivateKey()==null)?"N/A":aBoard.getPrivateKey();
            
            Vector rows = new Vector();
            rows.add(aBoard.getBoardName());
            rows.add(pubkey);
            rows.add(privkey);
            table.add(rows);
        }
        return table;
    }

    public String getPublicKey() {
    return publicKey;
    }
    public void setPublicKey(String pk) {
    publicKey=pk;
    }
    public String getBoard() {
    return board;
    }
    public String getContent() {
    return content;
    }
    public String getFrom() {
    return from;
    }
    public String getSubject() {
    return subject;
    }
    public String getDate() {
    return date;
    }
    public String getTime() {
    return time;
    }
    public String getIndex() {
    return index;
    }
    public File getFile() {
    return file;
    }

    public String[] getRow() {
    String fatFrom = from;
    File newMessage = new File(file.getPath() + ".lck");

    if (newMessage.isFile()) {
        // this is the point where new messages get its bold look,
        // this is resetted in TOF.evalSelection to non-bold on first view
        fatFrom = new StringBuffer().append("<html><b>").append(from).append("</b></html>").toString();
    }
    if( attachments.getAllOfType(Attachment.BOARD).size() > 0 || 
        attachments.getAllOfType(Attachment.BOARD).size() > 0 )
    {
        if (fatFrom.startsWith("<html><b>"))
            fatFrom = "<html><b><font color=\"blue\">" + from + "</font></b></html>";
        else
            fatFrom = "<html><font color=\"blue\">" + from + "</font></html>";
    }
    String[] row = {index, fatFrom, subject, date, time};
    return row;
    }

    /**Set*/
    public void setBoard(String board) {
    this.board = board;
    }
    public void setContent(String content) {
    this.content = content;
    }
    public void setFrom(String from) {
    this.from = from;
    }
    public void setSubject(String subject) {
    this.subject = subject;
    }
    public void setDate(String date) {
    this.date = date;
    }
    public void setTime(String time) {
    this.time = time;
    }
    public void setFile(File file) {
    this.file = file;
    }
    public void setIndex(String index) {
    this.index = index;
    }

    public boolean isValid() {

    if (date.equals(""))
        return false;
    if (time.equals(""))
        return false;
    if (subject.equals(""))
        return false;
    if (board.equals(""))
        return false;
    if (from.equals(""))
        return false;

    if (from.length() > 256)
        return false;
    if (subject.length() > 256)
        return false;
    if (board.length() > 256)
        return false;
    if (date.length() > 22)
        return false;
    // FIXED: no, this is freenet limit (actually its 32k, not 32000)
        /*
    if (content.length() > 32000)
        return false;*/

    return true;
    }

    /**Set all values*/
    public void analyzeFile() throws Exception
    {
        // set index for this msg from filename
        String filename = file.getName();
        this.index = (filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf(".xml"))).trim();
        // ensure all needed fields are properly filled
        if( from == null || date == null || subject == null || time == null ||
            board == null || content == null ||
            !isValid() )
        {
            throw new Exception("Message have invalid or missing fields.");
        }
        // replace evil chars
        for( int i = 0; i < evilChars.length; i++ )
        {
            this.from = this.from.replace(evilChars[i], '_');
            this.subject = this.subject.replace(evilChars[i], '_');
            this.date = this.date.replace(evilChars[i], '_');
            this.time = this.time.replace(evilChars[i], '_');
        }
    }

    /**
     * Parses the XML file and passes the FrostMessage element to
     * XMLize load method.
     */    
    protected void loadFile() throws Exception
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(this.file, false);
        } catch(Exception ex) { ex.printStackTrace(Core.getOut()); } // xml format error

        if( doc == null )
        {
            throw new Exception("Error - MessageObject.loadFile: could'nt parse XML Document.");
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostMessage") == false )
        {
            throw new Exception("Error - invalid message: does not contain the root tag 'FrostMessage'");
        }
        
        // load the message load itself
        loadXMLElement(rootNode);
    }

    /**
     * Constructor.
     * Used to construct an instance for an existing messagefile.
     */
    public MessageObject(File file) throws Exception
    {
        this();
        if( file == null || 
            file.exists() == false ||
            file.length() < 20 ) // prolog+needed tags are always > 20, but we need to filter 
        {                        // out the messages containing "Empty" (encrypted for someone else)   
            throw new Exception("Invalid input file for MessageObject");
        }
        this.file = file;
        loadFile();
        // ensure basic contents and formats
        analyzeFile();
    }

    /**
     * Constructor.
     * Used to contruct an instance for a new message.
     */
    public MessageObject() {
    this.board = "";
    this.from = "";
    this.subject = "";
    this.board = "";
    this.date = "";
    this.time = "";
    this.content = "";
    this.publicKey = "";
    this.attachments = new AttachmentList();
    }
    
    public List getOfflineFiles() {
    	if (attachments == null) return null;
    	
    	List result = new LinkedList();
    	
    	List fileAttachments = attachments.getAllOfType(Attachment.FILE);
    	Iterator it = fileAttachments.iterator();
    	while (it.hasNext()) {
    		SharedFileObject sfo = ((FileAttachment)it.next()).getFileObj();
    		if (!sfo.isOnline())
    			result.add(sfo);
    	}
    	return result;
    }
    
    public AttachmentList getAttachmentList()
    {
        return attachments;
    }
}
