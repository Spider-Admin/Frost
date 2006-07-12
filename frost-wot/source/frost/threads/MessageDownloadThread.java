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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
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
    
    private IndexSlots indexSlots;

    private static Logger logger = Logger.getLogger(MessageDownloadThread.class.getName());

    public MessageDownloadThread(boolean downloadToday, Board boa, int maxmsgdays) {
        super(boa);
        this.downloadToday = downloadToday;
        this.board = boa;
        this.maxMessageDownload = maxmsgdays;
        
        this.indexSlots = new IndexSlots(IndexSlots.MESSAGES, board.getName());
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
                indexSlots.close();
                notifyThreadFinished(this);
                return;
            }

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

            if (this.downloadToday) {
                // download only current date
                downloadDate(cal);
            } else {
                // download up to maxMessages days to the past
                Calendar firstDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
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
        indexSlots.close();
        notifyThreadFinished(this);
    }
    
    protected String composeDownKey(int index, String dirdate) {
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
        return downKey;
    }

    protected void downloadDate(Calendar calDL) throws SQLException {

        String dirdate = DateFun.getDateOfCalendar(calDL);
        java.sql.Date date = DateFun.getSqlDateOfCalendar(calDL);

        int index = -1;
        int failures = 0;
        int maxFailures = 2; // skip a maximum of 2 empty slots

        while (failures < maxFailures) {

            if (isInterrupted()) {
                return;
            }

            if( index < 0 ) {
                index = indexSlots.findFirstDownloadSlot(date);
            } else {
                index = indexSlots.findNextDownloadSlot(index, date);
            }
            
            String logInfo = null;

            try { // we don't want to die for any reason
                
                String downKey = composeDownKey(index, dirdate);
                logInfo = " board="+board.getName()+", key="+downKey;
                
                // for backload use fast download, deep for today
                boolean fastDownload = !downloadToday;

                MessageDownloaderResult mdResult = MessageDownloader.downloadMessage(downKey, index, fastDownload, logInfo);
                
                Mixed.wait(1111); // don't hurt node

                if( mdResult == null ) {
                    // file not found
                    failures++;
                    continue;
                }

                failures = 0;
                
                indexSlots.setDownloadSlotUsed(index, date);

                if( mdResult.errorMsg != null ) {
                    // some error occured, don't try this file again
                    receivedInvalidMessage(board, calDL, index, mdResult.errorMsg);
                } else if( mdResult.message != null ) {
                    // message is loaded, delete underlying received file
                    mdResult.message.getFile().delete();
                    // basic validation
                    if (mdResult.message.isValid() && isValidFormat(mdResult.message, calDL)) {
                        receivedValidMessage(mdResult.message, board, index);
                    } else {
                        receivedInvalidMessage(board, calDL, index, MessageDownloaderResult.INVALID_MSG);
                        logger.warning("TOFDN: Message was dropped, format validation failed: "+logInfo);
                    }
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate: "+logInfo, t);
                // download failed, try next file
            }
        } // end-of: while
    }
    
    private void receivedInvalidMessage(Board b, Calendar calDL, int index, String reason) {
        TOF.getInstance().receivedInvalidMessage(b, calDL, index, reason);
    }
    
    private void receivedValidMessage(MessageXmlFile mo, Board b, int index) {
        TOF.getInstance().receivedValidMessage(mo, b, index);
    }
    
    //////////////////////////////////////////////////
    ///  validation after receive 
    //////////////////////////////////////////////////
    
    /**
     * First time verify.
     * @param dirDate
     * @return
     */
    public boolean isValidFormat(MessageXmlFile mo, Calendar dirDate) {
        String timeStr = mo.getTimeStr();
        String msgDateStr = mo.getDateStr();
        try { // if something fails here, set msg. to N/A (maybe harmful message)
            if (verifyDate(msgDateStr,dirDate) == false || verifyTime(timeStr) == false) {
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
    private boolean verifyDate(String msgDateStr, Calendar dirDate) {
        // first check for valid date:
        // USES: date of msg. url: 'keypool\public\2003.6.9\2003.6.9-public-1.txt'  = given value 'dirDate'
        // USES: date in message  ( date=2003.6.9 ; time=09:32:31GMT )              = extracted from message
        
        Calendar msgDate = DateFun.getCalendarFromDate(msgDateStr);
        if( msgDate == null ) {
            logger.warning("* verifyDate(): Invalid date string found, will block message: " + msgDateStr);
            return false;
        }
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
    private boolean verifyTime(String timeStr) {
        // time=06:52:48GMT  <<-- expected format
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
