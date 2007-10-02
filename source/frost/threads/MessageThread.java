/*
  MessageDownloadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.perst.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * Download and upload messages for a board.
 */
public class MessageThread extends BoardUpdateThreadObject implements BoardUpdateThread, MessageUploaderCallback {

    private final Board board;
    private final int maxMessageDownload;
    private boolean downloadToday;

    private static final Logger logger = Logger.getLogger(MessageThread.class.getName());

    public MessageThread(final boolean downloadToday, final Board boa, final int maxmsgdays) {
        super(boa);
        this.downloadToday = downloadToday;
        this.board = boa;
        this.maxMessageDownload = maxmsgdays;
    }

    public int getThreadType() {
        if (downloadToday) {
            return BoardUpdateThread.MSG_DNLOAD_TODAY;
        } else {
            return BoardUpdateThread.MSG_DNLOAD_BACK;
        }
    }

    @Override
    public void run() {

        notifyThreadStarted(this);

        try {
            String tofType;
            if (downloadToday) {
                tofType = "TOF Download";
            } else {
                tofType = "TOF Download Back";
            }

            // wait a max. of 5 seconds between start of threads
            Mixed.waitRandom(5000);

            logger.info("TOFDN: " + tofType + " Thread started for board " + board.getName());

            if (isInterrupted()) {
                IndexSlotsStorage.inst().commit();
                notifyThreadFinished(this);
                return;
            }

            LocalDate localDate = new LocalDate(DateTimeZone.UTC);
            long date = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

            if (this.downloadToday) {
                // get IndexSlot for today
                final IndexSlot gis = IndexSlotsStorage.inst().getSlotForDate(board.getPerstFrostBoardObject().getBoardId(), date);
                // download only current date
                downloadDate(localDate, gis);
                // after update check if there are messages for upload and upload them
                uploadMessages(gis); // doesn't get a message when message upload is disabled
                // store index slot
                IndexSlotsStorage.inst().storeSlot(gis);
            } else {
                // download up to maxMessages days to the past
                int daysBack = 0;
                while (!isInterrupted() && daysBack < maxMessageDownload) {
                    daysBack++;
                    localDate = localDate.minusDays(1);
                    date = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();
                    final IndexSlot gis = IndexSlotsStorage.inst().getSlotForDate(board.getPerstFrostBoardObject().getBoardId(), date);
                    downloadDate(localDate, gis);
                    IndexSlotsStorage.inst().storeSlot(gis);
                }
                // after a complete backload run, remember finish time.
                // this ensures we ever update the complete backload days.
                if( !isInterrupted() ) {
                    board.setLastBackloadUpdateFinishedMillis(System.currentTimeMillis());
                }
            }
            logger.info("TOFDN: " + tofType + " Thread stopped for board " + board.getName());
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, Thread.currentThread().getName() + ": Oo. Exception in MessageDownloadThread:", t);
        }
        IndexSlotsStorage.inst().commit();
        notifyThreadFinished(this);
    }

    protected String composeDownKey(final int index, final String dirdate) {
        String downKey = null;
        // switch public / secure board
        if (board.isPublicBoard() == false) {
            downKey = new StringBuilder()
                    .append(board.getPublicKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(dirdate)
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            downKey = new StringBuilder()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(dirdate)
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return downKey;
    }

    protected void downloadDate(final LocalDate localDate, final IndexSlot gis) throws SQLException {

        final String dirdate = DateFun.FORMAT_DATE.print(localDate);

        int index = -1;
        int failures = 0;
        final int maxFailures = 2; // skip a maximum of 2 empty slots at the end of known indices

        while (failures < maxFailures) {

            if (isInterrupted()) {
                return;
            }

            if( index < 0 ) {
                index = gis.findFirstDownloadSlot();
            } else {
                index = gis.findNextDownloadSlot(index);
            }

            String logInfo = null;

            try { // we don't want to die for any reason

                Mixed.waitRandom(3000); // don't hurt node

                final String downKey = composeDownKey(index, dirdate);
                logInfo = " board="+board.getName()+", key="+downKey;

                // for backload use fast download, deep for today
                final boolean fastDownload = !downloadToday;

                final MessageDownloaderResult mdResult = MessageDownloader.downloadMessage(downKey, index, fastDownload, logInfo);
                if( mdResult == null ) {
                    // file not found
                    if( gis.isDownloadIndexBehindLastSetIndex(index) ) {
                        // we stop if we tried maxFailures indices behind the last known index
                        failures++;
                    }
                    continue;
                }

                failures = 0;

                if( mdResult.isFailure()
                        && mdResult.getErrorMessage() != null
                        && mdResult.getErrorMessage().equals(MessageDownloaderResult.ALLDATANOTFOUND) )
                {
                    // don't set slot used, try to retrieve the file again
                    System.out.println("TOFDN: Skipping index "+index+" for now, will try again later.");
                    continue;
                }

                gis.setDownloadSlotUsed(index);

                if( mdResult.isFailure() ) {
                    // some error occured, don't try this file again
                    receivedInvalidMessage(board, localDate, index, mdResult.getErrorMessage());
                } else if( mdResult.getMessage() != null ) {
                    // message is loaded, delete underlying received file
                    mdResult.getMessage().getFile().delete();
                    // basic validation
                    if (mdResult.getMessage().isValid() && isValidFormat(mdResult.getMessage(), localDate, board)) {
                        receivedValidMessage(mdResult.getMessage(), board, index);
                    } else {
                        receivedInvalidMessage(board, localDate, index, MessageDownloaderResult.INVALID_MSG);
                        logger.warning("TOFDN: Message was dropped, format validation failed: "+logInfo);
                    }
                }
            } catch(final Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate: "+logInfo, t);
                // download failed, try next file
            }
        } // end-of: while
    }

    private void receivedInvalidMessage(final Board b, final LocalDate calDL, final int index, final String reason) {
        TOF.getInstance().receivedInvalidMessage(b, calDL.toDateTimeAtMidnight(), index, reason);
    }

    private void receivedValidMessage(final MessageXmlFile mo, final Board b, final int index) {
        TOF.getInstance().receivedValidMessage(mo, b, index);
    }

    //////////////////////////////////////////////////
    ///  validation after receive
    //////////////////////////////////////////////////

    /**
     * First time verify.
     */
    public boolean isValidFormat(final MessageXmlFile mo, final LocalDate dirDate, final Board b) {
        try {
            final DateTime dateTime;
            try {
                dateTime = mo.getDateAndTime();
            } catch(final Throwable ex) {
                logger.log(Level.SEVERE, "Exception in isValidFormat() - skipping message.", ex);
                return false;
            }

            // e.g. "E6936D085FC1AE75D43275161B50B0CEDB43716C1CE54E420F3C6FEB9352B462" (len=64)
            if( mo.getMessageId() == null || mo.getMessageId().length() < 60 || mo.getMessageId().length() > 68 ) {
                logger.log(Level.SEVERE, "Message has no unique message id - skipping Message: "+dirDate+";"+dateTime);
                return false;
            }

            // ensure that time/date of msg is max. 1 day before/after dirDate
            final DateMidnight dm = dateTime.toDateMidnight();
            if( dm.isAfter(dirDate.plusDays(1).toDateMidnight(DateTimeZone.UTC))
                    || dm.isBefore(dirDate.minusDays(1).toDateMidnight(DateTimeZone.UTC)) )
            {
                logger.log(Level.SEVERE, "Invalid date - skipping Message: "+dirDate+";"+dateTime);
                return false;
            }

            // ensure that board inside xml message is the board we currently download
            if( mo.getBoardName() == null ) {
                logger.log(Level.SEVERE, "No or invalid boardname in message - skipping message: (null)");
                return false;
            }
            final String boardNameInMsg = mo.getBoardName().toLowerCase();
            final String downloadingBoardName = b.getName().toLowerCase();
            if( boardNameInMsg.equals(downloadingBoardName) == false ) {
                logger.log(Level.SEVERE, "Different boardnames - skipping message: "+mo.getBoardName().toLowerCase()+";"+b.getName().toLowerCase());
                return false;
            }

        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Exception in isValidFormat() - skipping message.", t);
            return false;
        }
        return true;
    }

    /**
     * Upload pending messages for this board.
     */
    protected void uploadMessages(final IndexSlot gis) {

        FrostUnsentMessageObject unsendMsg = UnsentMessagesManager.getUnsentMessage(board);
        if( unsendMsg == null ) {
            // currently no msg to send for this board
            return;
        }

        final String fromName = unsendMsg.getFromName();
        while( unsendMsg != null ) {

            // create a MessageXmlFile, sign, and send

            Identity recipient = null;
            if( unsendMsg.getRecipientName() != null && unsendMsg.getRecipientName().length() > 0) {
                recipient = Core.getIdentities().getIdentity(unsendMsg.getRecipientName());
                if( recipient == null ) {
                    logger.severe("Can't send Message '" + unsendMsg.getSubject() + "', the recipient is not longer in your identites list!");
                    UnsentMessagesManager.deleteMessage(unsendMsg);
                    continue;
                }
            }

            UnsentMessagesManager.incRunningMessageUploads();

            uploadMessage(unsendMsg, recipient, gis);

            UnsentMessagesManager.decRunningMessageUploads();

            Mixed.waitRandom(6000); // wait some time

            // get next message to upload
            unsendMsg = UnsentMessagesManager.getUnsentMessage(board, fromName);
        }
    }

    private void uploadMessage(final FrostUnsentMessageObject mo, final Identity recipient, final IndexSlot gis) {

        logger.info("Preparing upload of message to board '" + board.getName() + "'");

        mo.setCurrentUploadThread(this);

        try {
            // prepare upload

            LocalIdentity senderId = null;
            if( mo.getFromName().indexOf("@") > 0 ) {
                // not anonymous
                if( mo.getFromIdentity() instanceof LocalIdentity ) {
                    senderId = (LocalIdentity) mo.getFromIdentity();
                } else {
                    // apparently the LocalIdentity used to write the msg was deleted
                    logger.severe("The LocalIdentity used to write this unsent msg was deleted: "+mo.getFromName());
                    mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
                    UnsentMessagesManager.deleteMessage(mo);
                    return;
                }
            }

            final MessageXmlFile message = new MessageXmlFile(mo);

            final DateTime now = new DateTime(DateTimeZone.UTC);
            message.setDateAndTime(now);

            final File unsentMessageFile = FileAccess.createTempFile("unsendMsg", ".xml");
            message.setFile(unsentMessageFile);
            if (!message.save()) {
                logger.severe("This was a HARD error and the file to upload is lost, please report to a dev!");
                mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
                return;
            }
            unsentMessageFile.deleteOnExit();

            // start upload, this signs and encrypts if needed

            final MessageUploaderResult result = MessageUploader.uploadMessage(
                    message,
                    recipient,
                    senderId,
                    this,
                    gis,
                    MainFrame.getInstance(),
                    board.getName());

            // file is not any longer needed
            message.getFile().delete();

            if( !result.isSuccess() ) {
                // upload failed, unsend message was handled by MessageUploader (kept or deleted, user choosed)
                mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
                if( !result.isKeepMessage() ) {
                    // user choosed to drop the message
                    UnsentMessagesManager.deleteMessage(mo);
                } else {
                    // user choosed to retry after next startup, dequeue message now and find it again on next startup
                    UnsentMessagesManager.dequeueMessage(mo);
                }
                return;
            }

            final int index = result.getUploadIndex();

            // upload was successful, store message in sentmessages database
            final FrostMessageObject sentMo = new FrostMessageObject(message, board, index);

            if( !SentMessagesManager.addSentMessage(sentMo) ) {
                // not added to gui, perst obj is not needed
                sentMo.setPerstFrostMessageObject(null);
            }

            // save own private messages into the message table
            if( sentMo.getRecipientName() != null && sentMo.getRecipientName().length() > 0 ) {
                // maybe sentMo has a perst obj set
                final FrostMessageObject moForMsgTable;
                if( sentMo.getPerstFrostMessageObject() != null ) {
                    // create a new FrostMessageObject
                    moForMsgTable = new FrostMessageObject(message, board, index);;
                } else {
                    // reuseable
                    moForMsgTable = sentMo;
                }
                moForMsgTable.setSignatureStatusVERIFIED_V2();
                TOF.getInstance().receivedValidMessage(moForMsgTable, board, index);
            }

            // finally delete the message in unsend messages db table
            mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
            UnsentMessagesManager.deleteMessage(mo);

        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "Catched exception", t);
        }
        mo.setCurrentUploadThread(null); // paranoia

        logger.info("Message upload finished");
    }

    /**
     * This method composes the downloading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    public String composeDownloadKey(final MessageXmlFile message, final int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuilder()
                    .append(board.getPublicKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuilder()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }

    /**
     * This method composes the uploading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    public String composeUploadKey(final MessageXmlFile message, final int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuilder()
                    .append(board.getPrivateKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuilder()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }
}
