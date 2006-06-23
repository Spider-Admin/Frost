package frost.messages;

import java.util.*;

import frost.*;
import frost.identities.*;

/**
 * This class holds the basic variables of a message.
 * It is used by the MessageObjectFile (XML file representation)
 * and the FrostMessageObject (database representation).
 */
public abstract class AbstractMessageObject extends AbstractMessageStatusProvider {

    protected AttachmentList attachments = null;
    private String content = "";
    private String fromName = "";
    private String subject = "";
    private String publicKey  = "";
    private String recipientName = ""; // set if msg was encrypted
    private String signature = ""; // set if message is signed
    private String messageId = null;
    private String inReplyTo = null;

    private boolean isFromIdentityInitialized = false;
    private Identity fromIdentity = null;

    public Identity getFromIdentity() {
        if( isFromIdentityInitialized == false ) {
            // set Identity for FROM, or null
            fromIdentity = Core.getIdentities().getIdentity(getFromName());
            isFromIdentityInitialized = true;
        }
        return fromIdentity;
    }
    
    public String getContent() {
        return content;
    }
    public String getFromName() {
        return fromName;
    }
    public String getRecipientName() {
        return recipientName;
    }
    public String getSignature() {
        return signature;
    }
    public String getPublicKey() {
        return publicKey;
    }
    public String getSubject() {
        return subject;
    }
    public String getMessageId() {
        return messageId;
    }
    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setFromName(String from) {
        this.fromName = from;
    }
    public void setPublicKey(String pk) {
        publicKey = pk;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setRecipientName(String rec) {
        recipientName = rec;
    }
    public void setSignature(String sig) {
        signature = sig;
    }
    public void setInReplyTo(String s) {
        this.inReplyTo = s;
    }
    public void setMessageId(String s) {
        this.messageId = s;
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

    /**
     * This method returns the AttachmentList. If no one exists, it creates a new one.
     * @return the AttachmentList
     */
    public AttachmentList getAttachmentList() {
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
            attachments = new AttachmentList();
        }
        return attachments.getAllOfType(type);
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
    
    public boolean containsAttachments() {
        if ((getAttachmentsOfType(Attachment.BOARD).size() > 0) ||
            (getAttachmentsOfType(Attachment.FILE).size() > 0)) {
            return true;
        }
        return false;
    }
}
