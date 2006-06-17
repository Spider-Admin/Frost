/*
  MessageObject.java / Frost
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.bouncycastle.util.encoders.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.identities.*;

public class MessageObject implements XMLizable
{
    private static Logger logger = Logger.getLogger(MessageObject.class.getName());
    
    //FIXME: this one is missing the "?" char as opposed to mixed.makeFilename
    private static final char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _

    public static final String NEW_MSG_INDICATOR_STR = "NewMessage";

    public static final int SIGNATURESTATUS_UNSET    = 0; // status not set
    public static final int SIGNATURESTATUS_TAMPERED = 1; // wrong signature
    public static final int SIGNATURESTATUS_OLD      = 2; // no signature
    public static final int SIGNATURESTATUS_VERIFIED = 3; // signature was OK

    private static final String SIGNATURESTATUS_TAMPERED_STR = "TAMPERED"; // wrong signature
    private static final String SIGNATURESTATUS_OLD_STR      = "OLD"; // no signature
    private static final String SIGNATURESTATUS_VERIFIED_STR = "VERIFIED"; // signature was OK

    private AttachmentList attachments;
    private String board = "";
    private String content = "";
    private String from = "";
    private String subject = "";
    private String date = "";
    private String time = "";
    private String index = "";
    private String publicKey  = "";
    private String recipient = ""; // set if msg was encrypted
    private String signature = ""; // set if message is signed
    private boolean deleted = false;
    private int signatureStatus = SIGNATURESTATUS_UNSET;
    
    private String messageId = null;
    private String inReplyTo = null;

    protected File file;

    private Boolean messageIsNew = null;

    /**
     * Constructor.
     * Used to contruct an instance for a new message.
     */
    public MessageObject(String replyTo) {
        
        inReplyTo = replyTo;

        // new message, create a new unique msg id
        StringBuffer idStrSb = new StringBuffer();
        idStrSb.append(Long.toString(System.currentTimeMillis())); // millis
        idStrSb.append(DateFun.getExtendedDate());
        idStrSb.append(Long.toString(Runtime.getRuntime().freeMemory())); // free java mem
        idStrSb.append(DateFun.getExtendedTime());
        byte[] idStrPart = idStrSb.toString().getBytes();
        
        // finally add some random bytes
        byte[] idRandomPart = new byte[64];
        Core.getCrypto().getSecureRandom().nextBytes(idRandomPart);

        // concat both parts
        byte[] idBytes = new byte[idStrPart.length + idRandomPart.length];
        System.arraycopy(idStrPart, 0, idBytes, 0, idStrPart.length);
        System.arraycopy(idRandomPart, 0, idBytes, idStrPart.length-1, idRandomPart.length);
        
        String uniqueId = Core.getCrypto().computeChecksumSHA256(idBytes);

        messageId = uniqueId;
    }

    /**
     * Constructor.
     * Used to construct an instance for an existing messagefile.
     * @param file
     * @throws MessageCreationException
     */
    public MessageObject(File file) throws MessageCreationException {

        if (file == null) {
        	throw new MessageCreationException("Invalid input file for MessageObject. File is null.");
        } else if (!file.exists()) {
            throw new MessageCreationException(
        					"Invalid input file '" + file.getName() + "' for MessageObject. File doesn't exist.");
        } else if (file.length() < 20) { // prolog+needed tags are always > 20, but we need to filter
                                         // out the messages containing "Empty", "Invalid", "Double", "Broken"
                                         // (encrypted for someone else OR completely invalid)
            throw new MessageCreationException(
                            "Info only: Empty input file '" + file.getName() + "' for MessageObject (size < 20).", true);
        }
        this.file = file;
        try {
            loadFile();
            // ensure basic contents and formats
            analyzeFile();
        } catch (MessageCreationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MessageCreationException(
                            "Invalid input file '" + file.getName() + "' for MessageObject (load/analyze failed).", exception);
        }
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
        for( int i = 0; i < evilChars.length; i++ ) {
            this.from = this.from.replace(evilChars[i], '_');
            this.subject = this.subject.replace(evilChars[i], '_');
            this.date = this.date.replace(evilChars[i], '_');
            this.time = this.time.replace(evilChars[i], '_');
        }
    }

    /**
     * This method returns the AttachmentList. If no one exists, it creates a new one.
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
     *  Attachment.FILE
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

    public String getBoard() {
        return board;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public File getFile() {
        return file;
    }

    public String getFrom() {
        return from;
    }

    public String getIndex() {
        return index;
    }

    public String getRecipient() {
        return recipient;
    }
    
    public String getSignature() {
        return signature;
    }
    
    /**
     * Signs message and sets signature.
     * 
     * @param privateKey
     */
    public void signMessage(String privateKey) {
        String sig = Core.getCrypto().detachedSign(getSignableContent(), privateKey);
        setSignature(sig);
    }
    
    /**
     * Returns true if the message signature is valid.
     * 
     * @param pubKey
     * @return
     */
    public boolean verifyMessageSignature(String pubKey) {
        boolean sigIsValid = Core.getCrypto().detachedVerify(getSignableContent(), pubKey, getSignature());
        return sigIsValid;
    }
    
    /**
     * Returns a String containing all content that is used to sign/verify the message. 
     * @return
     */
    private String getSignableContent() {
        
        StringBuffer allContent = new StringBuffer();
        allContent.append(getDate());
        allContent.append(getTime());
        allContent.append(getSubject());
        allContent.append(getContent());
        // attachments
        for(Iterator it = getAllAttachments().iterator(); it.hasNext(); ) {
            Attachment a = (Attachment)it.next();
            if( a.getType() == Attachment.BOARD ) {
                BoardAttachment ba = (BoardAttachment)a;
                allContent.append( ba.getBoardObj().getBoardFilename() );
                if( ba.getBoardObj().getPublicKey() != null ) {
                    allContent.append( ba.getBoardObj().getPublicKey() );
                }
                if( ba.getBoardObj().getPrivateKey() != null ) {
                    allContent.append( ba.getBoardObj().getPrivateKey() );
                }
            } else if( a.getType() == Attachment.FILE ) {
                FileAttachment fa = (FileAttachment)a;
                allContent.append( fa.getFileObj().getFilename() );
                allContent.append( fa.getFileObj().getKey() );
            }
        }
        return allContent.toString();
    }

    /**
     * Get a list of all attached files that are currently offline.
     */
    public List getOfflineFiles() {
        List result = new LinkedList();
        if (attachments != null) {
            List fileAttachments = attachments.getAllOfType(Attachment.FILE);
            Iterator it = fileAttachments.iterator();
            while (it.hasNext()) {
                SharedFileObject sfo = ((FileAttachment) it.next()).getFileObj();
                if (!sfo.isOnline()) {
                    result.add(sfo);
                }
            }
        }
        return result;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSubject() {
        return subject;
    }

    public String getTime() {
        return time;
    }

    /**
     * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
     */
    public Element getXMLElement(Document d) {
        Element el = d.createElement("FrostMessage");

        CDATASection cdata;
        Element current;

        if( getMessageId() != null ) {
            current = d.createElement("MessageId");
            cdata = d.createCDATASection(getMessageId());
            current.appendChild(cdata);
            el.appendChild(current);
        }

        if( getInReplyTo() != null ) {
            current = d.createElement("InReplyTo");
            cdata = d.createCDATASection(getInReplyTo());
            current.appendChild(cdata);
            el.appendChild(current);
        }

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
        if (publicKey != null && publicKey.length() > 0) {
            current = d.createElement("pubKey");
            cdata = d.createCDATASection(Mixed.makeSafeXML(getPublicKey()));
            current.appendChild(cdata);
            el.appendChild(current);
        }

        // recipient
        if (recipient != null && recipient.length() > 0) {
            current = d.createElement("recipient");
            cdata = d.createCDATASection(Mixed.makeSafeXML(getRecipient()));
            current.appendChild(cdata);
            el.appendChild(current);
        }

        // signature
        if (signature != null && signature.length() > 0) {
            current = d.createElement("Signature");
            cdata = d.createCDATASection(Mixed.makeSafeXML(getSignature()));
            current.appendChild(cdata);
            el.appendChild(current);
        }

        //is deleted?
        if (deleted) {
            current = d.createElement("Deleted");
            el.appendChild(current);
        }

        // signature status
        if( signatureStatus != SIGNATURESTATUS_UNSET ) {
            current = d.createElement("signatureStatus");
            if( signatureStatus == SIGNATURESTATUS_TAMPERED ) {
                cdata = d.createCDATASection(SIGNATURESTATUS_TAMPERED_STR);
            } else if( signatureStatus == SIGNATURESTATUS_OLD ) {
                cdata = d.createCDATASection(SIGNATURESTATUS_OLD_STR);
            } else if( signatureStatus == SIGNATURESTATUS_VERIFIED ) {
                cdata = d.createCDATASection(SIGNATURESTATUS_VERIFIED_STR);
            }
            current.appendChild(cdata);
            el.appendChild(current);
        }

        //attachments
        if ((attachments != null) && (attachments.size() > 0)) {
            el.appendChild(attachments.getXMLElement(d));
        }

        return el;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @return true if the message is basically valid
     */
    public boolean isValid() {

        if (date == null || date.length() == 0 || date.length() > 22 ) {
            return false;
        }
        if (time == null || time.length() == 0) {
            return false;
        }
        if (board == null || board.length() == 0 || board.length() > 256 ) {
            return false;
        }
        if (from == null || from.length() == 0 || from.length() > 256 ) {
            return false;
        }

        if (subject == null) {
            subject = ""; // we accept empty subjects
        } else if ( subject.length() > 256 ) {
            return false;
        }

        if (content == null) {
            content = "";
        } else if (content.length() > (64 * 1024)) { // 64k or whatever fits in zipped data
            return false;
        }

        return true;
    }

    /**
     * Parses the XML file and passes the FrostMessage element to XMLize load method.
     */
    protected void loadFile() throws Exception {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(this.file, false);
        } catch(Exception ex) {  // xml format error
            File badMessage = new File("badmessage.xml");
            if (file.renameTo(badMessage)) {
                logger.log(Level.SEVERE, "Error - send the file badmessage.xml to a dev for analysis, more details below:", ex);
            }
        }

        if( doc == null ) {
            throw new Exception("Error - MessageObject.loadFile: couldn't parse XML Document - " +
                                "File name: '" + file.getName() + "'");
        }

        Element rootNode = doc.getDocumentElement();

        // transparently decrypt and continue load on success
        if( rootNode.getTagName().equals("EncryptedFrostMessage") ) {
            // get recipient (must be I to continue)
            recipient = XMLTools.getChildElementsCDATAValue(rootNode, "recipient");
            if( recipient == null ) {
                // no recipient
                throw new Exception("Error - encrypted message contains no 'recipient' section.");
            }
            FrostIdentities identities = Core.getInstance().getIdentities();
            if( !recipient.equals(identities.getMyId().getUniqueName()) ) {
                // not for me
                throw new MessageCreationException("Info: Encrypted message is not for me.",
                        MessageCreationException.MSG_NOT_FOR_ME);
            }
            String base64enc = XMLTools.getChildElementsCDATAValue(rootNode, "content");
            if( base64enc == null ) {
                // no content
                throw new Exception("Error - encrypted message contains no 'content' section.");
            }
            byte[] base64bytes = base64enc.getBytes("ISO-8859-1");
            byte[] encBytes = Base64.decode(base64bytes);
            // decrypt content
            byte[] decContent = Core.getCrypto().decrypt(encBytes, identities.getMyId().getPrivKey());
            if( decContent == null ) {
                logger.log(Level.SEVERE, "TOFDN: Encrypted message could not be decrypted!");
                throw new MessageCreationException("Error: Encrypted message could not be decrypted.",
                        MessageCreationException.DECRYPT_FAILED);
            }
            // decContent is an complete XML file, save it to file and load it
            FileAccess.writeFile(decContent, this.file);
            // try to load again
            try {
                doc = XMLTools.parseXmlFile(this.file, false);
            } catch(Exception ex) {  // xml format error
                File badMessage = new File("badmessage.xml");
                if (file.renameTo(badMessage)) {
                    logger.log(Level.SEVERE, "Error - send the file badmessage.xml to a dev for analysis, more details below:", ex);
                }
            }

            if( doc == null ) {
                throw new Exception("Error - MessageObject.loadFile: couldn't parse XML Document - " +
                                    "File name: '" + file.getName() + "'");
            }
            rootNode = doc.getDocumentElement();
        }
        if( rootNode.getTagName().equals("FrostMessage") == false ) {
            File badMessage = new File("badmessage.xml");
            if (file.renameTo(badMessage)) {
                logger.severe("Error - send the file badmessage.xml to a dev for analysis.");
            }
            throw new Exception("Error - invalid message: does not contain the root tag 'FrostMessage'");
        }
        // load the message load itself
        loadXMLElement(rootNode);
    }

    /**
     * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
     */
    public void loadXMLElement(Element e) throws SAXException {
        messageId = XMLTools.getChildElementsCDATAValue(e, "MessageId");
        inReplyTo = XMLTools.getChildElementsCDATAValue(e, "InReplyTo");
        from = XMLTools.getChildElementsCDATAValue(e, "From");
        date = XMLTools.getChildElementsCDATAValue(e, "Date");
        subject = XMLTools.getChildElementsCDATAValue(e, "Subject");
        time = XMLTools.getChildElementsCDATAValue(e, "Time");
        publicKey = XMLTools.getChildElementsCDATAValue(e, "pubKey");
        recipient = XMLTools.getChildElementsCDATAValue(e, "recipient");
        board = XMLTools.getChildElementsCDATAValue(e, "Board");
        content = XMLTools.getChildElementsCDATAValue(e, "Body");
        signature = XMLTools.getChildElementsCDATAValue(e, "Signature");

        if (!XMLTools.getChildElementsByTagName(e, "Deleted").isEmpty()) {
            deleted = true;
        } else {
            deleted = false;
        }

        String sigstat = XMLTools.getChildElementsCDATAValue(e, "signatureStatus");
        signatureStatus = SIGNATURESTATUS_UNSET; // default
        if( sigstat != null && (sigstat=sigstat.trim()).length() > 0 ) {
            if( sigstat.equalsIgnoreCase(SIGNATURESTATUS_TAMPERED_STR) ) {
                signatureStatus = SIGNATURESTATUS_TAMPERED;
            } else if( sigstat.equalsIgnoreCase(SIGNATURESTATUS_OLD_STR) ) {
                signatureStatus = SIGNATURESTATUS_OLD;
            } else if( sigstat.equalsIgnoreCase(SIGNATURESTATUS_VERIFIED_STR) ) {
                signatureStatus = SIGNATURESTATUS_VERIFIED;
            }
        }

        List l = XMLTools.getChildElementsByTagName(e, "AttachmentList");
        if (l.size() > 0) {
            Element attachmentsElement = (Element) l.get(0);
            attachments = new AttachmentList();
            attachments.loadXMLElement(attachmentsElement);
        }
    }

    public void setFile(File f) {
        file = f;
    }
    
    /**
     * Save the message.
     */
    public boolean save() {
        if( file == null ) {
            logger.log(Level.SEVERE, "Error: internal File pointer is not set");
            return false;
        }
        return saveToFile(file);
    }

    /**
     * Save the message to the specified file.
     * Does not change the internal File pointer.
     */
    public boolean saveToFile(File f) {
        File tmpFile = new File(f.getPath() + "sav.tmp");
        boolean success = false;
        try {
            Document doc = XMLTools.createDomDocument();
            doc.appendChild(getXMLElement(doc));
            success = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while saving message.", e);
        }
        if (success && tmpFile.length() > 0) {
            if( f.isFile() && f.delete() == false ) {
                logger.log(Level.SEVERE, "Error while saving message, delete failed.");
            }
            if( tmpFile.renameTo(f) == false ) {
                logger.log(Level.SEVERE, "Error while saving message, renameTo failed.");
                return false;
            }
        } else {
            tmpFile.delete();
        }
        return success;
    }
    
    /**
     * Encrypt the complete XML message file with public key of recipient and
     * create an EncryptedFrostMessage XML file that contains the encryted content
     * in base64 format. Saves the resulting XML file into targetFile.
     * 
     * @param msgFile  the message xml file that is the input for encryption
     * @param recipientPublicKey  recipients public key
     * @param targetFile  the target xml file for the encrypted message
     * @return
     */
    public static boolean encryptForRecipientAndSaveCopy(File msgFile, Identity recipient, File targetFile) {
        byte[] xmlContent = FileAccess.readByteArray(msgFile);
        byte[] encContent = Core.getCrypto().encrypt(xmlContent, recipient.getKey());
        String base64enc;
        try {
            base64enc = new String(Base64.encode(encContent), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "ISO-8859-1 encoding is not supported.", ex);
            return false;
        }
        
        Document doc = XMLTools.createDomDocument();
        Element el = doc.createElement("EncryptedFrostMessage");

        CDATASection cdata;
        Element current;

        // recipient
        current = doc.createElement("recipient");
        cdata = doc.createCDATASection(Mixed.makeSafeXML(recipient.getUniqueName()));
        current.appendChild(cdata);
        el.appendChild(current);

        // base64 content
        current = doc.createElement("content");
        cdata = doc.createCDATASection(base64enc);
        current.appendChild(cdata);
        el.appendChild(current);

        doc.appendChild(el);
        return XMLTools.writeXmlFile(doc, targetFile.getPath());
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        if( deleted == true ) {
            setMessageNew(false);
        }
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setPublicKey(String pk) {
        publicKey = pk;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setRecipient(String rec) {
        recipient = rec;
    }
    
    private void setSignature(String sig) {
        signature = sig;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public String getInReplyTo() {
        return inReplyTo;
    }
	
    /**
     * This method adds a new Attachment to the attachments list.
     * @param attachment the new Attachment to add to the attachments list.
     */
    public void addAttachment(Attachment attachment) {
        getAttachmentList().add(attachment);
    }

    public int getSignatureStatus() {
        return signatureStatus;
    }

    public void setSignatureStatus(int signatureStatus) {
        this.signatureStatus = signatureStatus;
    }

    public boolean isMessageNew() {
        if( this.messageIsNew == null ) {
            File newMessage = new File(getFile().getPath() + ".lck");
            if( newMessage.isFile() ) {
                this.messageIsNew = new Boolean(true);
                return true;
            } else {
                this.messageIsNew = new Boolean(false);
                return false;
            }
        }
        return this.messageIsNew.booleanValue();
    }

    /**
     * Sets the new-message status to true or false.
     * @param newMsg  the new-message status to set
     */
    public void setMessageNew(boolean newMsg) {
        final String newMsgIndicator = getFile().getPath() + ".lck";
        Runnable ioworker = null;
        if( newMsg ) {
            this.messageIsNew = new Boolean(true);
            ioworker = new Runnable() {
                public void run() {
                    FileAccess.writeFile(NEW_MSG_INDICATOR_STR, newMsgIndicator);
                }
            };
        } else {
            this.messageIsNew = new Boolean(false);
            ioworker = new Runnable() {
                public void run() {
                    new File(newMsgIndicator).delete();
                }
            };
        }
        new Thread(ioworker).start(); // do IO in another thread, not here in Swing thread
    }
    
    /**
     * Compares the given message in otherMsgFile with this message.
     * Compares content (body), subject, from and attachments.
     */
    public boolean compareTo(File otherMsgFile) {
        try {
            MessageObject otherMessage = new MessageObject(otherMsgFile);
            return compareTo(otherMessage);
        } catch(Throwable t) {
            logger.log(Level.WARNING, "Handled Exception in compareTo(File otherMsgFile)", t);
            return false; // We assume that the other message is different (it may be corrupted)
        }
    }
    
    /**
     * Compares the given otherMsg with this message.
     * Compares content (body), subject, from and attachments.
     */
    public boolean compareTo(MessageObject otherMsg) {
        try {
            // We compare the messages by content (body), subject, from and attachments
            if (!getContent().equals(otherMsg.getContent())) {
                return false;
            }
            if (!getSubject().equals(otherMsg.getSubject())) {
                return false;
            }
            if (!getFrom().equals(otherMsg.getFrom())) {
                return false;
            }
            AttachmentList attachments1 = otherMsg.getAllAttachments();
            AttachmentList attachments2 = getAllAttachments();
            if (attachments1.size() != attachments2.size()) {
                return false;
            }
            Iterator iterator1 = attachments1.iterator();
            Iterator iterator2 = attachments2.iterator();
            while (iterator1.hasNext()) {
                Attachment attachment1 = (Attachment) iterator1.next();
                Attachment attachment2 = (Attachment) iterator2.next();
                if (attachment1.compareTo(attachment2) != 0) {
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Handled Exception in compareTo(MessageObject otherMsg)", t);
            return false; // We assume that the local message is different (it may be corrupted)
        }
    }
}
