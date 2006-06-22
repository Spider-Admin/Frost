package frost.messages;

import frost.identities.*;

public abstract class AbstractMessageStatusProvider {

    // the message states
    private static final int xGOOD     = 1;
    private static final int xCHECK    = 2;
    private static final int xBAD      = 3;
    private static final int xOBSERVE  = 4;
    private static final int xTAMPERED = 5;
    private static final int xOLD      = 6;

    private static final int SIGNATURESTATUS_UNSET    = 0; // status not set
    private static final int SIGNATURESTATUS_TAMPERED = 1; // wrong signature
    private static final int SIGNATURESTATUS_OLD      = 2; // no signature
    private static final int SIGNATURESTATUS_VERIFIED = 3; // signature was OK

    private static final String SIGNATURESTATUS_TAMPERED_STR = "TAMPERED"; // wrong signature
    private static final String SIGNATURESTATUS_OLD_STR      = "OLD";      // no signature
    private static final String SIGNATURESTATUS_VERIFIED_STR = "VERIFIED"; // signature was OK

    public abstract int getSignatureStatus();
    
    protected int messageStatus = -1;
    protected String messageStatusString = null;
    
    protected int signatureStatus = SIGNATURESTATUS_UNSET;
    
    protected void initializeMessageStatus(Identity fromIdentity) {
        messageStatus = getMessageStatus(fromIdentity);
        messageStatusString = getMessageStatusString(messageStatus);
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
    
    protected int getMessageStatus(Identity fromIdentity) {
        if( getSignatureStatus() == SIGNATURESTATUS_VERIFIED ) {
            // get state of user
            if( fromIdentity == null ) {
                return xOLD;
            }
            int state = fromIdentity.getState();
            if( state == FrostIdentities.NEUTRAL ) {
                return xCHECK;
            }
            if( state == FrostIdentities.OBSERVE ) {
                return xOBSERVE;
            }
            if( state == FrostIdentities.FRIEND ) {
                return xGOOD;
            }
            if( state == FrostIdentities.ENEMY ) {
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

    protected String getMessageStatusString(int msgStatus) {
        if( msgStatus == xGOOD ) {
            return "GOOD"; // dark green
        } else if( msgStatus == xCHECK ) {
            return "CHECK"; // yellow
        } else if( msgStatus == xBAD ) {
            return "BAD"; // red
        } else if( msgStatus == xOBSERVE ) {
            return "OBSERVE"; // a lighter green
        } else if( msgStatus == xOLD ) {
            return "NONE";
        } else if( msgStatus == xTAMPERED ) {
            return "FAKE";
        }
        return "*err*"; // never come here
    }
    
    public int getMessageStatus() {
        return messageStatus;
    }

    public String getMessageStatusString() {
        return messageStatusString;
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
}
