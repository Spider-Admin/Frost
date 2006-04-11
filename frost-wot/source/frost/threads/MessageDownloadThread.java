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
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.transferlayer.*;

/**
 * Download messages.
 */
public class MessageDownloadThread extends BoardUpdateThreadObject implements BoardUpdateThread {

    private Board board;
    private int maxMessageDownload;
    private boolean downloadToday;

    private static Logger logger = Logger.getLogger(MessageDownloadThread.class.getName());

    public MessageDownloadThread(boolean downloadToday, Board boa, int maxmsgdays) {
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

    public void run() {

        notifyThreadStarted(this);

        try {
            String tofType;
            if (downloadToday) {
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

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (this.downloadToday) {
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

    protected void downloadDate(GregorianCalendar calDL) {

        String dirdate = DateFun.getDateOfCalendar(calDL);
        String fileSeparator = System.getProperty("file.separator");

        String destination =
            new StringBuffer()
                .append(Core.frostSettings.getValue("keypool.dir"))
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
        int maxFailures = 2; // skip a maximum of 2 empty slots

        while (failures < maxFailures) {

            if (isInterrupted()) {
                return;
            }
            
            File destFile = null;
            String logInfo = null;

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
                destFile = new File(val);

                if (destFile.length() > 0) { // already downloaded
                    index++;
                    failures = 0;
                    continue;
                }

                File checkUploadLockfile = new File(destFile.getPath() + ".lock");
                if( checkUploadLockfile.exists() ) {
                    // this file is currently uploaded, don't try to download it now
                    index++;
                    failures = 0;
                    continue;
                }

                String downKey = null;
                // switch public / secure board
                if (board.isPublicBoard() == false) {
                    downKey = new StringBuffer()
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
                    downKey = new StringBuffer()
                            .append("KSK@frost/message/")
                            .append(Core.frostSettings.getValue("messageBase"))
                            .append("/")
                            .append(dirdate)
                            .append("-")
                            .append(board.getBoardFilename())
                            .append("-")
                            .append(index)
                            .append(".xml")
                            .toString();
                }

                logInfo = " board="+board.getName()+", key="+downKey;
                
                // for backload use fast download, deep for today
                boolean fastDownload = !downloadToday;

                MessageDownloaderResult mdResult = 
                    new MessageDownloader05().downloadMessage(downKey, index, fastDownload, logInfo);

                index++; // whatever happened, try next index next time
                
                Mixed.wait(1111); // don't hurt node

                if( mdResult == null ) {
                    // file not found
                    failures++;
                    continue;
                }

                failures = 0;

                if( mdResult.errorMsg != null ) {
                    // some error occured, don't try this file again
                    FileAccess.writeFile(mdResult.errorMsg, destFile); // this file is ignored by the gui
                } else if( mdResult.message != null ) {
                    // method saves the XML file to destFile
                    File oldTmpFile = mdResult.message.getFile();
                    mdResult.message.setFile(destFile);
                    
                    addMessageToGui(mdResult.message, destFile, true, calDL, mdResult.messageState);
                    
                    oldTmpFile.delete();
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 1."+logInfo, t);
                // download failed, try next file
                destFile.delete();
            }
        } // end-of: while
    }

    /**
     * Checks if the provided message is valid, and adds valid messages
     * to the GUI.
     *
     * @param currentMsg  message
     * @param destFile      message file in keypool
     * @param markAsNew   new message?
     * @param calDL       Calendar with date of download to check for valid date in message
     * @param signatureStatus   a status from MessageObject that should be set IF the message is added
     */
    private void addMessageToGui(
        VerifyableMessageObject currentMsg,
        File destFile,
        boolean markAsNew,
        GregorianCalendar calDL,
        int signatureStatus)
    {
        if (currentMsg.isValid() && currentMsg.isValidFormat(calDL)) {

            currentMsg.setSignatureStatus(signatureStatus);
            if( currentMsg.save() == false ) {
                logger.log(Level.SEVERE, "TOFDN: Could not save the XML file after setting the signatureState!");
            }

            if (destFile.length() > 0 && TOF.getInstance().blocked(currentMsg, board) ) {
                board.incBlocked();
                logger.info("TOFDN: Blocked message for board '"+board.getName()+"': "+destFile.getPath());
            } else {

                // check if msg would be displayed (maxMessageDays)
                GregorianCalendar minDate = new GregorianCalendar();
                minDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                minDate.add(Calendar.DATE, -1*board.getMaxMessageDisplay());
                Calendar msgDate = DateFun.getCalendarFromDate(currentMsg.getDate());
                if( msgDate == null ) {
                    logger.log(Level.SEVERE, "TOFDN: invalid date in filename, message dropped:"+destFile.getPath());
                    FileAccess.writeFile(MessageDownloaderResult.INVALID_MSG, destFile);
                    return;
                }
                if( !msgDate.before(minDate) ) {
                    // add new message or notify of arrival
                    TOF.getInstance().addNewMessageToTable(destFile, board, markAsNew);
                } else {
                    logger.log(Level.SEVERE, "TOFDN: received message from the past, not displayed due to maxMessageDays to display:"+
                            destFile.getPath());
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
                    logger.info("Boards from unsigned message blocked: "+destFile.getPath());
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xBAD &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_BAD) == true )
                {
                    logger.info("Boards from BAD message blocked: "+destFile.getPath());
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xCHECK &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_CHECK) == true )
                {
                    logger.info("Boards from CHECK message blocked: "+destFile.getPath());
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xOBSERVE &&
                           Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_OBSERVE) == true )
                {
                    logger.info("Boards from OBSERVE message blocked: "+destFile.getPath());
                } else if( currentMsg.getSignatureStatus() == VerifyableMessageObject.xTAMPERED ) {
                    logger.info("Boards from TAMPERED message blocked: "+destFile.getPath());
                } else {
                    // either GOOD user or not blocked by user
                    Core.addNewKnownBoards(currentMsg.getAttachmentsOfType(Attachment.BOARD));
                }
            }
        } else {
            // format validation failed
            FileAccess.writeFile(MessageDownloaderResult.INVALID_MSG, destFile);
            logger.warning("TOFDN: Message "+destFile.getName()+" was dropped, format validation failed.");
        }
    }
}
