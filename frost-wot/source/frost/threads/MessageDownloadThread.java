/*
  MessageDownloadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.boards.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.messaging.*;

/**
 * Download messages.
 */
public class MessageDownloadThread
    extends BoardUpdateThreadObject
    implements BoardUpdateThread
{
    private Board board;
    private int downloadHtl;
    private String keypool;
    private int maxMessageDownload;
    private String destination;
    private boolean secure;
    private String publicKey;
    private boolean flagNew;
    
	private static Logger logger = Logger.getLogger(MessageDownloadThread.class.getName());
	private MessageHashes messageHashes;

    /**
     * Constants for content of XML files if the message was dropped for some reason.
     * .xml files with this content are ignored by the gui (in fact all .xml files with length smaller than 20) 
     */ 
    public static final String DUPLICATE_MSG   = "DuplicateMsg"; // msg was already received
    public static final String BROKEN_METADATA = "BrokenMetaData"; // could not load metadata
    public static final String BROKEN_MSG      = "BrokenMsg"; // could not load xml
    public static final String MSG_NOT_FOR_ME  = "NotForMe"; // encrypted for someone other
    public static final String DECRYPT_FAILED  = "DecryptFailed"; // encrypted for me, but decrypt failed
    public static final String INVALID_MSG     = "InvalidMsg"; // message format validation failed


    public int getThreadType() {
        if (flagNew) {
            return BoardUpdateThread.MSG_DNLOAD_TODAY;
        } else {
            return BoardUpdateThread.MSG_DNLOAD_BACK;
        }
    }

    public void run() {

        notifyThreadStarted(this);

        try {
            String tofType;
            if (flagNew) {
                tofType = "TOF Download";
            } else {
                tofType = "TOF Download Back";
            }

            // Wait some random time to speed up the update of the TOF table
            // ... and to not to flood the node
            int waitTime = (int) (Math.random() * 5000);
            // wait a max. of 5 seconds between start of threads
            Mixed.wait(waitTime);

			logger.info("TOFDN: " + tofType + " Thread started for board " + board.getName());

            if (isInterrupted()) {
                notifyThreadFinished(this);
                return;
            }

            // switch public / secure board
            if (board.isPublicBoard() == false) {
                publicKey = board.getPublicKey();
                secure = true;
            } else { // public board
                secure = false;
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (this.flagNew) {
                // download only current date
                downloadDate(cal);
            } else {
                // download up to maxMessages days to the past
                GregorianCalendar firstDate = new GregorianCalendar();
                firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                firstDate.set(Calendar.YEAR, 2001);
                firstDate.set(Calendar.MONTH, 5);
                firstDate.set(Calendar.DATE, 11);
                int counter = 0;
                while (!isInterrupted() &&
                       cal.after(firstDate) &&
                       counter < maxMessageDownload)
                {
                    counter++;
                    cal.add(Calendar.DATE, -1); // Yesterday
                    downloadDate(cal);
                }
            }
			logger.info("TOFDN: " + tofType + " Thread stopped for board " + board.getName());
        } catch (Throwable t) {
        	logger.log(Level.SEVERE, Thread.currentThread().getName() + ": Oo. Exception in MessageDownloadThread:", t);
        }
        notifyThreadFinished(this);
    }

    protected void downloadDate(GregorianCalendar calDL)
    {
        VerifyableMessageObject currentMsg = null;
        String dirdate = DateFun.getDateOfCalendar(calDL);
        String fileSeparator = System.getProperty("file.separator");

        destination =
            new StringBuffer()
                .append(keypool)
                .append(board.getBoardFilename())
                .append(fileSeparator)
                .append(dirdate)
                .append(fileSeparator)
                .toString();

        File makedir = new File(destination);
        if (!makedir.exists()) {
            makedir.mkdirs();
        }

        int index = 0;
        int failures = 0;
        int maxFailures;

        // this works, but could lead into problems if you don't run frost 24h a day
//        if (flagNew) {
//            maxFailures = 2; // skip a maximum of 2 empty slots for today
//        } else {
//            maxFailures = 1; // skip a maximum of 1 empty slots for backload
//        }
        
        maxFailures = 2; // skip a maximum of 2 empty slots

        while (failures < maxFailures) {
            
            if (isInterrupted()) {
                return;
            }
            File testMe = null, testMe2 = null;
            FcpResults results = null;

            try { // we don't want to die for any reason
                String val = new StringBuffer()
                        .append(destination)
                        .append(dirdate)
                        .append("-")
                        .append(board.getBoardFilename())
                        .append("-")
                        .append(index)
                        .append(".xml")
                        .toString();
                testMe2 = new File(val);
                if (testMe2.length() > 0) { // already downloaded
                    index++;
                    failures = 0;
                    continue;
                }

                val = new StringBuffer()
                        .append(destination)
                        .append(System.currentTimeMillis())
                        .append(".xml.msg")
                        .toString();
                testMe = new File(val);

                File checkUploadLockfile = new File(testMe2.getPath() + ".lock");
                if( checkUploadLockfile.exists() ) {
                    // this file is currently uploaded, don't try to download it now
                    index++;
                    failures = 0;
                    continue;
                }

                String downKey = null;
                if (secure) {
                    downKey = new StringBuffer()
                            .append(publicKey)
                            .append("/")
                            .append(board.getBoardFilename())
                            .append("/")
                            .append(dirdate)
                            .append("-")
                            .append(index)
                            .append(".xml")
                            .toString();
                } else {
                    downKey = new StringBuffer()
                            .append("KSK@frost/message/")
                            .append(MainFrame.frostSettings.getValue("messageBase"))
                            .append("/")
                            .append(dirdate)
                            .append("-")
                            .append(board.getBoardFilename())
                            .append("-")
                            .append(index)
                            .append(".xml")
                            .toString();
                }

                // for backload use fast download, deep for today
                boolean fastDownload = !flagNew; 
                results = FcpRequest.getFile(
                        downKey,
                        null,
                        testMe,
                        downloadHtl,
                        false,
                        fastDownload);

            } catch(Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate(GregorianCalendar calDL)", t);
                // download failed, try next file
                testMe.delete();
            }

            Mixed.wait(1111); // wait some time to not to hurt the node on next retry

            index++; // whatever happened, try next index next time

            try { // we don't want to die for any reason

                if (results == null) {
                    // download failed, try next file
                    testMe.delete();
                    continue;
                }
                
                // we downloaded something
                failures = 0;

                testMe.renameTo(testMe2);
                testMe = testMe2;
                
                // either null (unsigned) or signed and maybe encrypted message
                byte[] metadata = results.getRawMetadata();

                if( testMe.length() == 0 ) {
                    // Frosts message files do always contain data, so the received content is wrong
                    if( metadata != null && metadata.length > 0 ) {
                        logger.severe("TOFDN: Received metadata without data, maybe faked message.");
                    } else if( metadata == null || metadata.length == 0 ) {
                        // paranoia checking, should never happen if FcpResults != null
                        logger.severe("TOFDN: Received neither metadata nor data, maybe a bug or a faked message.");
                    } else {
                        // something bad happened if we ever come here :)
                        logger.severe("TOFDN: Received something, but bad things happened in code, maybe a bug or a faked message.");
                    }
                    FileAccess.writeFile(BROKEN_MSG, testMe); // this file is ignored by the gui
                    continue;
                }

                // compute the sha1 checksum of the original msg file
                // this digest is ONLY used to check for incoming exact duplicate files, because
                // the locally stored message xml file could be changed later by Frost
                String messageId = Core.getCrypto().digest(testMe);
                // Does a duplicate message exist?
                boolean isDuplicateMsg = messageHashes.contains(messageId);
                // add to the list of message hashes to track this received message
                messageHashes.add(messageId);

                if( isDuplicateMsg ) {
                    logger.info(Thread.currentThread().getName()+": TOFDN: *** Duplicate Message : "+testMe.getName()+" ***");
                    if( Core.frostSettings.getBoolValue(SettingsClass.RECEIVE_DUPLICATE_MESSAGES) == false ) {
                        // user don't want to see the duplicate messages
                        FileAccess.writeFile(DUPLICATE_MSG, testMe); // this file is ignored by the gui
                        continue;
                    }
                }

                // if no metadata, message wasn't signed
                if (metadata == null) {
                    
                    byte[] unzippedXml = FileAccess.readZipFileBinary(testMe);
                    if( unzippedXml == null ) {
                        logger.log(Level.SEVERE, "TOFDN: Unzip of unsigned xml failed");
                        FileAccess.writeFile(BROKEN_MSG, testMe); // this file is ignored by the gui
                        continue;
                    }
                    FileAccess.writeFile(unzippedXml, testMe);

                    try {
                        currentMsg = new VerifyableMessageObject(testMe);
                        // check and maybe add msg to gui, set to unsigned
                        addMessageToGui(currentMsg, testMe, true, calDL, MessageObject.SIGNATURESTATUS_OLD);

                    } catch (Exception ex) {
						logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate(GregorianCalendar calDL)", ex);
                        // file could not be read, mark it invalid not to confuse gui
                        FileAccess.writeFile(BROKEN_MSG, testMe); // this file is ignored by the gui
                    }
                    continue;
                } 
                
                // verify the zipped message
                MetaData _metaData = null;
                try {
                    Document doc = XMLTools.parseXmlContent(metadata, false);
                    if( doc != null ) { // was metadata xml ok?
                        _metaData = MetaData.getInstance( doc.getDocumentElement() );
                    }
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "TOFDN: Exeption in MetaData.getInstance(): ", t);
                    _metaData = null;
                }
                if( _metaData == null ) {
                    // metadata failed, do something
                    logger.log(Level.SEVERE, "TOFDN: Metadata couldn't be read. " +
                                    "Offending file saved as badmetadata.xml - send to a dev for analysis");
                    File badmetadata = new File("badmetadata.xml");
                    FileAccess.writeFile(metadata, badmetadata);
                    // don't try this file again
                    FileAccess.writeFile(BROKEN_METADATA, testMe); // this file is ignored by the gui
                    continue;
                }
                	
                assert _metaData.getType() == MetaData.SIGN || _metaData.getType() == MetaData.ENCRYPT :
                	"TOFDN: unknown type of metadata";
                
                // now the msg could be signed OR signed and encrypted
                // first check sign, later decrypt if msg was for me
                
				SignMetaData metaData = (SignMetaData)_metaData;
                
                //check if we have the owner already on the lists
                String _owner = metaData.getPerson().getUniqueName();
                Identity owner = identities.getIdentity(_owner);
                // if not on any list, use the parsed id and add to our identities list
                if (owner == null) {
                    owner = metaData.getPerson();
                    owner.setState(FrostIdentities.NEUTRAL);
					identities.addIdentity(owner);
                }

                // verify signature
                byte[] plaintext = FileAccess.readByteArray(testMe);
                boolean sigIsValid = Core.getCrypto().detachedVerify(plaintext, owner.getKey(), metaData.getSig());

                // only for correct owner (no faking allowed here)
                if( sigIsValid ) {
                    // update lastSeen for this Identity
                    owner.updateLastSeenTimestamp();
                }
                
                // now check if msg is encrypted and for me, if yes decrypt the zipped data
                if (_metaData.getType() == MetaData.ENCRYPT) {
                    EncryptMetaData encMetaData = (EncryptMetaData)metaData;
                    
                    // 1. check if the message is for me
                    if (!encMetaData.getRecipient().equals(identities.getMyId().getUniqueName())) {
                        logger.fine("TOFDN: Encrypted message was not for me.");
                        FileAccess.writeFile(MSG_NOT_FOR_ME, testMe); // this file is ignored by the gui
                        continue;
                    }
                    
                    // 2. if yes, decrypt the content
                    byte[] cipherText = FileAccess.readByteArray(testMe);
                    byte[] zipData = Core.getCrypto().decrypt(cipherText,identities.getMyId().getPrivKey());
                    
                    if( zipData == null ) {
                        logger.log(Level.SEVERE, "TOFDN: Encrypted message from "+encMetaData.getPerson().getUniqueName()+
                                                 " could not be decrypted!");
                        FileAccess.writeFile(DECRYPT_FAILED, testMe); // this file is ignored by the gui
                        continue;
                    }
                    
                    testMe.delete();
                    FileAccess.writeFile(zipData, testMe);
                    
                    logger.fine("TOFDN: Decrypted an encrypted message for me, sender was "+encMetaData.getPerson().getUniqueName());
                    
                    // now continue as for signed files
                    
                } //endif encrypted message

                // unzip
                byte[] unzippedXml = FileAccess.readZipFileBinary(testMe);
                if( unzippedXml == null ) {
                    logger.log(Level.SEVERE, "TOFDN: Unzip of signed xml failed");
                    FileAccess.writeFile(BROKEN_MSG, testMe); // this file is ignored by the gui
                    continue;
                }
                FileAccess.writeFile(unzippedXml, testMe);

                // create object
                try {
                    currentMsg = new VerifyableMessageObject(testMe);
                } catch (Exception ex) {
					logger.log(Level.SEVERE, "TOFDN: Exception when creating message object", ex);
                    // file could not be read, mark it invalid not to confuse gui
                    FileAccess.writeFile(BROKEN_MSG, testMe); // this file is ignored by the gui
                    continue;
                }

                //then check if the signature was ok
                if (!sigIsValid) {
                    // TODO: should'nt we drop this msg instead of adding it to the gui?
                    logger.warning("TOFDN: message failed verification, status set to TAMPERED.");
                    addMessageToGui(currentMsg, testMe, false, calDL, MessageObject.SIGNATURESTATUS_TAMPERED);    
                    continue;
                }

                //make sure the pubkey and from fields in the xml file are the same as those in the metadata
                String metaDataHash = Mixed.makeFilename(Core.getCrypto().digest(metaData.getPerson().getKey()));
                String messageHash = Mixed.makeFilename(
                        currentMsg.getFrom().substring(
                            currentMsg.getFrom().indexOf("@") + 1,
                            currentMsg.getFrom().length()));

                if (!metaDataHash.equals(messageHash)) {
                    // TODO: should'nt we drop this msg instead of adding it to the gui?
                    logger.warning("TOFDN: Hash in metadata doesn't match hash in message!\n" +
                    			   "metadata : "+metaDataHash+" , message: " + messageHash+
                                   ". Message failed verification, status set to TAMPERED.");
                    addMessageToGui(currentMsg, testMe, false, calDL, MessageObject.SIGNATURESTATUS_TAMPERED);
                    continue;
                }

                addMessageToGui(currentMsg, testMe, true, calDL, MessageObject.SIGNATURESTATUS_VERIFIED);

            } catch (Throwable t) {
				logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate(GregorianCalendar calDL)", t);
                // index is already increased for next try
            }
        } // end-of: while
    }

    /**
     * Checks if the provided message is valid, and adds valid messages
     * to the GUI. 
     *   
     * @param currentMsg  message
     * @param testMe      message file in keypool
     * @param markAsNew   new message?
     * @param calDL       Calendar with date of download to check for valid date in message
     * @param signatureStatus   a status from MessageObject that should be set IF the message is added
     */
    private void addMessageToGui(
        VerifyableMessageObject currentMsg,
        File testMe,
        boolean markAsNew,
        GregorianCalendar calDL,
        int signatureStatus)
    {
        if (currentMsg.isValid() && currentMsg.isValidFormat(calDL)) {

            currentMsg.setSignatureStatus(signatureStatus);
            if( currentMsg.save() == false ) {
                logger.log(Level.SEVERE, "TOFDN: Could not save the XML file after setting the signatureState! signatureState keeps UNSET.");
            }
            
            if (testMe.length() > 0 && TOF.getInstance().blocked(currentMsg, board) ) {
                board.incBlocked();
                logger.info("TOFDN: Blocked message for board '"+board.getName()+"'");
            } else {

                // check if msg would be displayed (maxMessageDays)
                GregorianCalendar minDate = new GregorianCalendar();
                minDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                minDate.add(Calendar.DATE, -1*board.getMaxMessageDisplay());
                Calendar msgDate = DateFun.getCalendarFromDate(currentMsg.getDate());
                if( msgDate == null ) {
                    logger.log(Level.SEVERE, "TOFDN: invalid date in filename, message dropped:"+testMe.getPath());
                    FileAccess.writeFile(INVALID_MSG, testMe);
                    return;
                }
                if( !msgDate.before(minDate) ) {
                    // add new message or notify of arrival
                    TOF.getInstance().addNewMessageToTable(testMe, board, markAsNew);
                } else {
                    logger.log(Level.SEVERE, "TOFDN: received message from the past, not displayed due to maxMessageDays to display:"+
                            testMe.getPath());
                }
                // add all files indexed files, but never for BAD users
                if( currentMsg.getSignatureStatus() != VerifyableMessageObject.xBAD ) {
                    Iterator it = currentMsg.getAttachmentsOfType(Attachment.FILE).iterator();
                    while (it.hasNext()) {
                        SharedFileObject current = ((FileAttachment)it.next()).getFileObj();
                        if (current.getOwner() != null) {
                            Index index = Index.getInstance();
                            synchronized(index) {
                                index.add(current, board);
                            }
                        }
                    }
                }
                // add all boards to the list of known boards
                if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xOLD &&
                    Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_UNSIGNED) == true )
                {
                    logger.info("Boards from unsigned message blocked.");
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xBAD &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_BAD) == true )
                {
                    logger.info("Boards from BAD message blocked.");
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xCHECK &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_CHECK) == true )
                {
                    logger.info("Boards from CHECK message blocked.");
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xOBSERVE &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_OBSERVE) == true )
                {
                    logger.info("Boards from OBSERVE message blocked.");
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xTAMPERED ) {
                    logger.info("Boards from TAMPERED message blocked.");
                } else {
                    // either GOOD user or not blocked by user
                    Core.addNewKnownBoards(currentMsg.getAttachmentsOfType(Attachment.BOARD));
                }
            }
        } else {
            // format validation failed
            FileAccess.writeFile(INVALID_MSG, testMe);
            logger.warning("TOFDN: Message "+testMe.getName()+" was dropped, format validation failed.");
        }
    }

	/**Constructor*/ //
	public MessageDownloadThread(
		boolean fn,
		Board boa,
		int dlHtl,
		String kpool,
		String maxmsg,
		FrostIdentities newIdentities) {
			
		super(boa, newIdentities);
		
		this.flagNew = fn;
		this.board = boa;
		this.downloadHtl = dlHtl;
		this.keypool = kpool;
		this.maxMessageDownload = Integer.parseInt(maxmsg);
	}

	/**
	 * @param messageHashes
	 */
	public void setMessageHashes(MessageHashes messageHashes) {
		this.messageHashes = messageHashes;		
	}
}
