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
import java.util.logging.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;

public class MessageObject implements XMLizable 
{
	private static Logger logger = Logger.getLogger(MessageObject.class.getName());

	//FIXME: this one is missing the "?" char as opposed to mixed.makeFilename
    private static final char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _

	private AttachmentList attachments;
    private String board = "";
    private String content = "";
    private String from = "";
    private String subject = "";
    private String date = "";
    private String time = "";
    private String index = "";
    private String publicKey  = "";
    private boolean deleted = false;
    
    protected File file;

    /**
     * Constructor.
     * Used to contruct an instance for a new message.
     */
    public MessageObject() {
    	//Nothing here
    }

    /**
     * Constructor.
     * Used to construct an instance for an existing messagefile.
     */
    public MessageObject(File file) throws Exception {
        this();
        if (file == null) {
        	throw new Exception("Invalid input file (null) received for MessageObject");
        } else if (!file.exists() || file.length() < 20) { // prolog+needed tags are always > 20, but we need to filter 
        												   // out the messages containing "Empty" (encrypted for someone else)
        	file.renameTo(new File("badMessage"));
        	throw new Exception("Invalid input file '" + file.getName() + "' received for MessageObject. " +
        						"Send the file \"badMessage\" to a dev");
        }
        this.file = file;
        loadFile();
        // ensure basic contents and formats
        analyzeFile();
    }

    /**Set all values*/
    public void analyzeFile() throws Exception
    {
        // set index for this msg from filename
        String filename = file.getName();
        this.index = (filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf(".xml"))).trim();
        // ensure all needed fields are properly filled
        if( from == null || date == null ||  time == null ||
            board == null || !isValid() )
        {
        	String message = "Analyze file failed.  File saved as \"badMessage\", send to a dev.  Reason:\n";
        	if (!isValid()) message = message + "isValid failed";
        	if (content==null) message = message + "content null";
        	logger.severe(message);
        	file.renameTo(new File("badMessage"));
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
     * This method returns the AttachmentList. If no one exists, it
     * creates a new one.
     * @return the AttachmentList
     */
    private AttachmentList getAttachmentList() {
    	if (attachments == null) {
    		attachments = new AttachmentList();
    	}
		return attachments;
	}
    
    /**
     * This method returns an AttachmentList containing all of the 
     * attachments of the given type. The type can be one of those:
     * 	Attachment.FILE
	 *  Attachment.BOARD
	 *  Attachment.PERSON (currently unused)
     * @param type the type of attachments to return in the AttachmentList
	 * @return an AttachmentList containing all of the attachments of the given type.
     */
    public AttachmentList getAttachmentsOfType(int type) {
    	if (attachments == null) {
    		return new AttachmentList();
    	} else {
    		return attachments.getAllOfType(type);
    	}
    }
    
    /**
     * This method returns all of the attachments
	 * @return an AttachmentList containing all of the attachments of the message.
     */
    public AttachmentList getAllAttachments() {
    	if (attachments == null) {
    		return new AttachmentList();
    	} else {
    		return attachments;
    	}
    }

	/**
	 * @return
	 */
	public String getBoard() {
		return board;
	}

	/**
     * @return
     */
    public String getContent() {
		return content;
	}

	/**
	 * @return
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}

  /**
     * @return
     */
    public String getFrom() {
		return from;
	}

	/**
	 * @return
	 */
	public String getIndex() {
		return index;
	}
    
    /**
	 * @return
	 */
	public List getOfflineFiles() {
		List result = new LinkedList();
		if (attachments != null) {
			List fileAttachments = attachments.getAllOfType(Attachment.FILE);
			Iterator it = fileAttachments.iterator();
			while (it.hasNext()) {
				SharedFileObject sfo = ((FileAttachment) it.next()).getFileObj();
				if (!sfo.isOnline())
					result.add(sfo);
			}
		}
		return result;
	}

    /**
	 * @return
	 */
    public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @return
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return
	 */
	public String getTime() {
		return time;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document d) {
		Element el = d.createElement("FrostMessage");

		CDATASection cdata;
		Element current;

		//from
		current = d.createElement("From");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getFrom()));
		current.appendChild(cdata);
		el.appendChild(current);

		//subject
		current = d.createElement("Subject");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getSubject()));
		current.appendChild(cdata);
		el.appendChild(current);

		//date
		current = d.createElement("Date");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getDate()));
		current.appendChild(cdata);
		el.appendChild(current);

		//time
		current = d.createElement("Time");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getTime()));
		current.appendChild(cdata);
		el.appendChild(current);

		//body
		current = d.createElement("Body");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getContent()));
		current.appendChild(cdata);
		el.appendChild(current);

		//board
		current = d.createElement("Board");
		cdata = d.createCDATASection(Mixed.makeSafeXML(getBoard()));
		current.appendChild(cdata);
		el.appendChild(current);

		//public Key
		if (publicKey != null) {
			current = d.createElement("pubKey");
			cdata = d.createCDATASection(Mixed.makeSafeXML(getPublicKey()));
			current.appendChild(cdata);
			el.appendChild(current);
		}

		//is deleted?
		if (deleted) {
			current = d.createElement("Deleted");
			el.appendChild(current);
		}

		//attachments
		if ((attachments != null) && (attachments.size() > 0)) {
			el.appendChild(attachments.getXMLElement(d));
		}

		return el;
	}

    /**
	 * @return
	 */
    public boolean isDeleted() {
    	return deleted;
    }
	
	/**
	 * @return
	 */
    public boolean isValid() {

		if (subject == null)
			subject = new String();
		if (content == null)
			content = new String();
		if (date.equals(""))
			return false;
		if (time.equals(""))
			return false;
		// if (subject.equals(""))
		//    return false;
		if (board.equals(""))
			return false;
		if (from.equals(""))
			return false;

		if (from.length() > 256)
			return false;
		if (subject != null && subject.length() > 256)
			return false;
		if (board.length() > 256)
			return false;
		if (date.length() > 22)
			return false;
		if (content.length() > 32 * 1024)
			return false;

		return true;
	}

    /**
	 * Parses the XML file and passes the FrostMessage element to XMLize load
	 * method.
	 */    
    protected void loadFile() throws Exception
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(this.file, false);
        } catch(Exception ex) {  // xml format error
			File badMessage = new File("badmessage.xml");
			if (file.renameTo(badMessage))
				logger.log(Level.SEVERE, "Error - send the file badmessage.xml to a dev for analysis, more details below:", ex);
        } 

        if( doc == null )
        {
            throw new Exception("Error - MessageObject.loadFile: could'nt parse XML Document.");
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostMessage") == false )
        {
        	File badMessage = new File("badmessage.xml");
        	if (file.renameTo(badMessage))
        		logger.severe("Error - send the file badmessage.xml to a dev for analysis.");
            throw new Exception("Error - invalid message: does not contain the root tag 'FrostMessage'");
        }
        
        // load the message load itself
        loadXMLElement(rootNode);
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		from = XMLTools.getChildElementsCDATAValue(e, "From");
		date = XMLTools.getChildElementsCDATAValue(e, "Date");
		subject = XMLTools.getChildElementsCDATAValue(e, "Subject");
		time = XMLTools.getChildElementsCDATAValue(e, "Time");
		publicKey = XMLTools.getChildElementsCDATAValue(e, "pubKey");
		board = XMLTools.getChildElementsCDATAValue(e, "Board");
		content = XMLTools.getChildElementsCDATAValue(e, "Body");

		if (!XMLTools.getChildElementsByTagName(e, "Deleted").isEmpty()) {
			deleted = true;
		}

		List l = XMLTools.getChildElementsByTagName(e, "AttachmentList");
		if (l.size() > 0) {
			Element attachmentsElement = (Element) l.get(0);
			attachments = new AttachmentList();
			attachments.loadXMLElement(attachmentsElement);
		}
	}
	
	/**
	 *  
	 */
	public void save() {
		File tmpFile = new File(file.getPath() + ".tmp");
		boolean success = false;
		try {
			Document doc = XMLTools.createDomDocument();
			doc.appendChild(getXMLElement(doc));
			success = XMLTools.writeXmlFile(doc, tmpFile.getPath());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while saving message.", e);
		}
		if (success && tmpFile.length() > 0) {
			file.delete();
			tmpFile.renameTo(file);
		} else {
			tmpFile.delete();
		}		
	}

	/**
	 * @param board
	 */
	public void setBoard(String board) {
		this.board = board;
	}

	/**
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}
	
	/**
	 * @param deleted
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @param from
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @param index
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	/**
	 * @param pk
	 */
	public void setPublicKey(String pk) {
		publicKey = pk;
	}

	/**
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @param time
	 */
	public void setTime(String time) {
		this.time = time;
	}
	
	/**
	 * This method adds a new Attachment to the attachments list.
	 * @param attachment the new Attachment to add to the attachments list.
	 */
	public void addAttachment(Attachment attachment) {
		getAttachmentList().add(attachment);
	}
}
