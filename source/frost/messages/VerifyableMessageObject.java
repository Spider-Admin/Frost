/*
  VerifyableMessageObject.java / Frost
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

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.identities.*;

public class VerifyableMessageObject extends MessageObject implements Cloneable {

	private static Logger logger = Logger.getLogger(VerifyableMessageObject.class.getName());
    
    public static final int xGOOD     = 1;
    public static final int xCHECK    = 2;
    public static final int xBAD      = 3;
    public static final int xOBSERVE  = 4;
    public static final int xTAMPERED = 5;
    public static final int xOLD      = 6;
    
    private Identity fromIdentity = null;
    
    public Identity getFromIdentity() {
        if( fromIdentity == null ) {
            FrostIdentities identities = Core.getInstance().getIdentities();
            if( identities.isMySelf(getFrom())) {
                fromIdentity = identities.getMyId();
            } else {
                fromIdentity = identities.getIdentity(getFrom());
            }
        }
        return fromIdentity;
    }

    public int getMsgStatus() {
        if( getSignatureStatus() == MessageObject.SIGNATURESTATUS_VERIFIED ) {
            // get state of user
            if( getFromIdentity() == null ) {
                logger.log(Level.SEVERE, "No identity for from found:"+getFrom());
                return xOLD;
            }
            int state = getFromIdentity().getState();
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
        } else if( getSignatureStatus() == MessageObject.SIGNATURESTATUS_OLD ) {
            // no signature
            return xOLD;
        } else if( getSignatureStatus() == MessageObject.SIGNATURESTATUS_TAMPERED ) {
            // invalid signature
            return xTAMPERED;
        } 
        // signature status unset
        return xOLD;
    }
    
    public String getMsgStatusString() {
        int status = getMsgStatus();
        if( status == xGOOD ) {
            return "GOOD"; // dark green
        } else if( status == xCHECK ) {
            return "CHECK"; // yellow
        } else if( status == xBAD ) {
            return "BAD"; // red
        } else if( status == xOBSERVE ) {
            return "OBSERVE"; // a lighter green 
        } else if( status == xOLD ) {
            return "NONE";
        } else if( status == xTAMPERED ) {
            return "FAKE";
        }
        return "*err*"; // never come here
    }
    
    /**
     * @return
     * @throws CloneNotSupportedException
     */
    public VerifyableMessageObject copy() throws CloneNotSupportedException {
		return (VerifyableMessageObject) this.clone();
	}

//    /** 
//     * gets the status of the message
//     * @return
//     */
//    public String getStatus2() {
//		return status;
//	}
//
//    /**
//	 * set the status
//	 * @param status
//	 */
//	public void setStatus(String status) {
//		this.status = status;
//		FileAccess.writeFile(status, file.getPath() + ".sig");
//	}
    
//    /**
//     * Removes the status, deletes .sig file.
//     */
//    public void removeStatus() {
//        new File(file.getPath() + ".sig").delete();
//    }

    /**
     * @param file
     * @throws MessageCreationException
     */
    public VerifyableMessageObject(File file) throws MessageCreationException {
		super(file); // throws exception if loading failed
//		File sigFile = new File(file.getPath() + ".sig");
//		if (!sigFile.exists()) {
//			status = NA;
//		} else {
//			status = FileAccess.readFileRaw(sigFile);
//		}
	}

    /**
	 * First time verify.
     * @param dirDate
     * @return
     */
    public boolean isValidFormat(GregorianCalendar dirDate) {
		try { // if something fails here, set msg. to N/A (maybe harmful message)
			if (verifyDate(dirDate) == false || verifyTime() == false) {
				return false;
			}
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception in isValidFormat() - skipping Message.", t);
			return false;
		}
		return true;
	}

    /**
     * Returns false if the date from inside the message is more than 1 day
     * before/behind the date in the URL of the message.
     *  
     * @param dirDate  date of the url that was used to retrieve the message
     * @return  true if date is valid, or false
     */
    public boolean verifyDate(GregorianCalendar dirDate) {
        VerifyableMessageObject currentMsg = this;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        // first check for valid date:
        // USES: date of msg. url: 'keypool\public\2003.6.9\2003.6.9-public-1.txt'  = given value 'dirDate'
        // USES: date in message  ( date=2003.6.9 ; time=09:32:31GMT )              = extracted from message
        String msgDateStr = currentMsg.getDate();
		Date msgDateTmp = null;
		try {
			msgDateTmp = dateFormat.parse(msgDateStr);
		} catch (Exception ex) {
		}
		if (msgDateTmp == null) {
            logger.warning("* verifyDate(): Invalid date string found, will block message: " + msgDateStr);
            return false;
        }
        GregorianCalendar msgDate = new GregorianCalendar();
        msgDate.setTime(msgDateTmp);
        // set both dates to same _time_ to allow computing millis
        msgDate.set(Calendar.HOUR_OF_DAY, 1);
        msgDate.set(Calendar.MINUTE, 0);
        msgDate.set(Calendar.SECOND, 0);
        msgDate.set(Calendar.MILLISECOND, 0);
        dirDate.set(Calendar.HOUR_OF_DAY, 1);
        dirDate.set(Calendar.MINUTE, 0);
        dirDate.set(Calendar.SECOND, 0);
        dirDate.set(Calendar.MILLISECOND, 0);
        long dirMillis = dirDate.getTimeInMillis();
        long msgMillis = msgDate.getTimeInMillis();
        // compute difference dir - msg
        long ONE_DAY = (1000 * 60 * 60 * 24);
        int diffDays = (int)((dirMillis - msgMillis) / ONE_DAY);
        // now compare dirDate and msgDate using above rules
        if( Math.abs(diffDays) <= 1 ) {
            // message is of this day (less than 1 day difference)
            // msg is OK, do nothing here
        } else if( diffDays < 0 ) {
            // msgDate is later than dirDate
            logger.warning("* verifyDate(): Date in message is later than date in URL, will block message: " + msgDateStr);
            return false;
        } else if( diffDays > 1 ) { // more than 1 day older
            // dirDate is later than msgDate
            logger.warning("* verifyDate(): Date in message is earlier than date in URL, will block message: " + msgDateStr);
            return false;
        }
        return true;
    }

    /**
     * Verifies that the time is valid.
     * 
     * @return  true if time is valid, or false
     */
    public boolean verifyTime() {
        VerifyableMessageObject currentMsg = this;
        // time=06:52:48GMT  <<-- expected format
        String timeStr = currentMsg.getTime();
        if (timeStr == null) {
			logger.warning("* verifyTime(): Time is NULL, blocking message.");
			return false;
		}
		timeStr = timeStr.trim();

		if (timeStr.length() != 11) {
			logger.warning("* verifyTime(): Time string have invalid length (!=11), blocking message: " + timeStr);
            return false;
        }
        // check format
        if( !Character.isDigit(timeStr.charAt(0)) ||
            !Character.isDigit(timeStr.charAt(1)) ||
            !(timeStr.charAt(2) == ':') ||
            !Character.isDigit(timeStr.charAt(3)) ||
            !Character.isDigit(timeStr.charAt(4)) ||
            !(timeStr.charAt(5) == ':') ||
            !Character.isDigit(timeStr.charAt(6)) ||
            !Character.isDigit(timeStr.charAt(7)) ||
            !(timeStr.charAt(8) == 'G') ||
            !(timeStr.charAt(9) == 'M') ||
            !(timeStr.charAt(10) == 'T') )
        {
			logger.warning("* verifyTime(): Time string have invalid format (xx:xx:xxGMT), blocking message: " + timeStr);
            return false;
        }
        // check for valid values :)
        String hours = timeStr.substring(0, 2);
        String minutes = timeStr.substring(3, 5);
        String seconds = timeStr.substring(6, 8);
        int ihours = -1;
        int iminutes = -1;
        int iseconds = -1;
        try {
            ihours = Integer.parseInt( hours );
            iminutes = Integer.parseInt( minutes );
            iseconds = Integer.parseInt( seconds );
        } catch(Exception ex) {
			logger.warning("* verifyTime(): Could not parse the numbers, blocking message: " + timeStr);
            return false;
        }
        if( ihours < 0 || ihours > 23 ||
            iminutes < 0 || iminutes > 59 ||
            iseconds < 0 || iseconds > 59 )
        {
			logger.warning("* verifyTime(): Time is invalid, blocking message: " + timeStr);
            return false;
        }
        return true;
    }
}
