package frost.messages;

import javax.swing.tree.*;

import frost.*;
import frost.identities.*;

public abstract class AbstractMessageStatusProvider extends DefaultMutableTreeNode {

    // the message states
    private static final int xGOOD     = 1;
    private static final int xCHECK    = 2;
    private static final int xBAD      = 3;
    private static final int xOBSERVE  = 4;
    private static final int xTAMPERED = 5;
    private static final int xOLD      = 6;
    
    private static String[] messageStateStrings = {
        "*err*",
        "GOOD",
        "CHECK",
        "BAD",
        "OBSERVE",
        "FAKE",
        "NONE"
    };

    private static final int SIGNATURESTATUS_UNSET    = 0; // status not set
    private static final int SIGNATURESTATUS_TAMPERED = 1; // wrong signature
    private static final int SIGNATURESTATUS_OLD      = 2; // no signature
    private static final int SIGNATURESTATUS_VERIFIED = 3; // signature was OK

    private static final String SIGNATURESTATUS_TAMPERED_STR = "TAMPERED"; // wrong signature
    private static final String SIGNATURESTATUS_OLD_STR      = "OLD";      // no signature
    private static final String SIGNATURESTATUS_VERIFIED_STR = "VERIFIED"; // signature was OK
    
    private boolean isFromIdentityInitialized = false;
    private Identity fromIdentity = null;
    
    private String fromName = "";

    private int signatureStatus = SIGNATURESTATUS_UNSET;

    public Identity getFromIdentity() {
        if( isFromIdentityInitialized == false ) {
            // set Identity for FROM, or null
            fromIdentity = Core.getIdentities().getIdentity(getFromName());
            isFromIdentityInitialized = true;
        }
        return fromIdentity;
    }

    public String getFromName() {
        return fromName;
    }
    public void setFromName(String from) {
        this.fromName = from;
    }
    
    /**
     * Converts the signature status string contained in local XML message file
     * into the internal constant.
     */
    public boolean setSignatureStatusFromString(String sigStatusStr) {
        if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_VERIFIED_STR) ) {
            setSignatureStatusVERIFIED();
            return true;
        } else if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_OLD_STR) ) {
            setSignatureStatusOLD();
            return true;
        } else if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_TAMPERED_STR) ) {
            setSignatureStatusTAMPERED();
            return true;
        }
        return false;
    }
    
    private int getMessageStatus(Identity fromIdent) {
        if( getSignatureStatus() == SIGNATURESTATUS_VERIFIED ) {
            // get state of user
            if( fromIdent == null ) {
                return xOLD;
            }
            if( fromIdent.isCHECK() ) {
                return xCHECK;
            }
            if( fromIdent.isOBSERVE() ) {
                return xOBSERVE;
            }
            if( fromIdent.isGOOD() ) {
                return xGOOD;
            }
            if( fromIdent.isBAD() ) {
                return xBAD;
            }
        } else if( getSignatureStatus() == SIGNATURESTATUS_OLD ) {
            // no signature
            return xOLD;
        } else if( getSignatureStatus() == SIGNATURESTATUS_TAMPERED ) {
            // invalid signature
            return xTAMPERED;
        }
        // signature status unset
        return xOLD;
    }

    // TODO: we could make this faster by caching the value and changing it only if the trust state
    //  for the identity changed. this is called by the renderer!
    private int getMessageStatus() {
        return getMessageStatus(getFromIdentity());
    }

    public String getMessageStatusString() {
        return messageStateStrings[getMessageStatus(getFromIdentity())];
    }
    
    public boolean isMessageStatusGOOD() {
        return (getMessageStatus() == xGOOD );
    }
    public boolean isMessageStatusOBSERVE() {
        return (getMessageStatus() == xOBSERVE );
    }
    public boolean isMessageStatusCHECK() {
        return (getMessageStatus() == xCHECK );
    }
    public boolean isMessageStatusBAD() {
        return (getMessageStatus() == xBAD );
    }
    public boolean isMessageStatusTAMPERED() {
        return (getMessageStatus() == xTAMPERED );
    }
    public boolean isMessageStatusOLD() {
        return (getMessageStatus() == xOLD );
    }
    
    public boolean isSignatureStatusVERIFIED() {
        return (getSignatureStatus() == SIGNATURESTATUS_VERIFIED);
    }
    public boolean isSignatureStatusOLD() {
        return (getSignatureStatus() == SIGNATURESTATUS_OLD);
    }
    public boolean isSignatureStatusTAMPERED() {
        return (getSignatureStatus() == SIGNATURESTATUS_TAMPERED);
    }
    
    public void setSignatureStatusVERIFIED() {
        signatureStatus = SIGNATURESTATUS_VERIFIED;
    }
    public void setSignatureStatusOLD() {
        signatureStatus = SIGNATURESTATUS_OLD;
    }
    public void setSignatureStatusTAMPERED() {
        signatureStatus = SIGNATURESTATUS_TAMPERED;
    }
    
    public int getSignatureStatus() {
        return signatureStatus;
    }
    public void setSignatureStatus(int s) {
        signatureStatus = s;
    }
}
