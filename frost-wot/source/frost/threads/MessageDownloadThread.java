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
import javax.swing.*;
import java.awt.*;
import java.text.*;

import frost.*;
import frost.identities.*;
import frost.gui.objects.*;

/**
 * Downloads messages
 */
public class MessageDownloadThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    public FrostBoardObject board;
    private String downloadHtl;
    private String keypool;
    private int maxMessageDownload;
    private String destination;
    private boolean secure;
    private String publicKey;
    private boolean flagNew;
    private VerifyableMessageObject currentMsg;
    private Identity currentId;

    public int getThreadType()
    {
        if( flagNew )
        {
            return BoardUpdateThread.MSG_DNLOAD_TODAY;
        }
        else
        {
            return BoardUpdateThread.MSG_DNLOAD_BACK;
        }
    }

    public void run()
    {
        notifyThreadStarted(this);

        try {

        String tofType;
        if( flagNew )
            tofType="TOF Download";
        else
            tofType="TOF Download Back";

        // Wait some random time to speed up the update of the TOF table
        // ... and to not to flood the node
        int waitTime = (int)(Math.random() * 5000); // wait a max. of 5 seconds between start of threads
        mixed.wait(waitTime);

        System.out.println("TOFDN: "+tofType + " Thread started for board "+board.toString());

        if( isInterrupted() )
        {
            notifyThreadFinished(this);
            return;
        }

        // switch public / secure board
        if( board.isPublicBoard() == false )
        {
            publicKey = board.getPublicKey();
            secure = true;
        }
        else // public board
        {
            secure = false;
        }

        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        if( this.flagNew )
        {
            // download only actual date
            downloadDate(cal);
        }
        else
        {
            // download up to maxMessages days to the past
            GregorianCalendar firstDate = new GregorianCalendar();
            firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            firstDate.set(Calendar.YEAR, 2001);
            firstDate.set(Calendar.MONTH, 5);
            firstDate.set(Calendar.DATE, 11);
            int counter=0;
            while( !isInterrupted() && cal.after(firstDate) && counter < maxMessageDownload )
            {
                counter++;
                cal.add(Calendar.DATE, -1); // Yesterday
                downloadDate(cal);
            }
        }
        System.out.println("TOFDN: "+tofType+" Thread stopped for board "+board.toString());
        }
        catch(Throwable t)
        {
            System.out.println("Oo. Exception in MessageDownloadThread:");
            t.printStackTrace();
        }
        notifyThreadFinished(this);
    }

    /**Returns true if message is duplicate*/
    private boolean exists(File file)
    {
        File[] fileList = (file.getParentFile()).listFiles();

        if( fileList != null )
        {
            for( int i = 0; i < fileList.length; i++ )
            {
                if( ! fileList[i].equals(file) &&
                    fileList[i].getName().indexOf(board.getBoardFilename()) != -1 &&
                    file.getName().indexOf(board.getBoardFilename()) != -1 )
                {
                    String one = FileAccess.readFile(file);
                    String two = FileAccess.readFile(fileList[i]);
                    if( one.equals(two) )
                        return true;
                }
            }
        }
        return false;
    }

    private void verify(GregorianCalendar dirDate)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

        System.out.println("TOFDN: ****** Verifying incoming message ******");
        try { // if something fails here, set msg. to N/A (maybe harmful message)

            // TODO:
            // first check for valid date:
            // USE: date of msg. url: 'keypool\public\2003.6.9\2003.6.9-public-1.txt'   = given value 'dirDate'
            // USE:  date in message  ( date=2003.6.9 ; time=09:32:31GMT )              = extracted from message
            //
            // - if date in msg. is greater than in url (in days), set msg. date the url date+put txt that it was changed into msg.
            // - if date in msg. is smaller than url date, replace with url date (allow 2 days difference)
            String msgDateStr = currentMsg.getDate();
            Date msgDateTmp = null;
            try {
                msgDateTmp = dateFormat.parse( msgDateStr );
            } catch(Exception ex) { }
            if( msgDateTmp == null )
            {
                System.out.println("TOFDN: Invalid date string found, will discard message: "+msgDateStr);
                currentMsg.setDate(""); // -> leads to isValid()==false + msg. file is written with content = "Empty"
                return;
            }
            GregorianCalendar msgDate = new GregorianCalendar();
            msgDate.setTime(msgDateTmp);
            // set both dates to same time to allow computing millis
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
            if( Math.abs(diffDays) <= 1 )
            {
                // message is of this day (less than 1 day difference)
                // msg is OK, do nothing here
                System.out.println("TOFDN: Checked message date, seems to be OK.");
            }
            else if( diffDays < 0 )
            {
                // msgDate is later than dirDate
                System.out.println("TOFDN: Date in message is later than date in URL, will discard message: "+msgDateStr);
                currentMsg.setDate(""); // -> leads to isValid()==false + msg. file is written with content = "Empty"
                return;
            }
            else if( diffDays > 1 ) // more than 1 day older
            {
                // dirDate is later than msgDate
                System.out.println("TOFDN: Date in message is earlier than date in URL, will discard message: "+msgDateStr);
                currentMsg.setDate(""); // -> leads to isValid()==false + msg. file is written with content = "Empty"
                return;
            }

            // now as the date is correct, go on to verify
            if( (currentMsg.getKeyAddress() == "none") || (currentMsg.getFrom().indexOf("@") == -1) )
            {
                System.out.println("TOFDN: *** Message is NOT signed at all: "+currentMsg.getFrom());
                currentMsg.setStatus(VerifyableMessageObject.OLD);
            }
            else
            { //the message contains the CHK of a public key
                // see if we have this name on our list
                if( frame1.getFriends().containsKey(currentMsg.getFrom()) )
                {
                    //yes, we have that person, see if the addreses are the same
                    currentId = frame1.getFriends().Get(currentMsg.getFrom());
                    //check if the key addreses are the same, verify
                    if( (currentId.getKeyAddress().compareTo(currentMsg.getKeyAddress()) == 0) &&
                        frame1.getCrypto().verify(currentMsg.getContent(), currentId.getKey()) )
                    {
                        System.out.println("TOFDN: *** Message is signed by a FRIEND, set state to GOOD: "+currentMsg.getFrom());
                        currentMsg.setStatus(VerifyableMessageObject.VERIFIED);
                    }
                    else // verification FAILED!
                    {
                        System.out.println("TOFDN: *** Message seems to be from a FRIEND (from is equal), but signature is wrong; set state to BAD: "+currentMsg.getFrom());
                        currentMsg.setStatus(VerifyableMessageObject.FAILED);
                    }
                }
                else if( frame1.getEnemies().containsKey(currentMsg.getFrom()) ) //we have the person, but he is blacklisted
                {
                    System.out.println("TOFDN: *** Message is from an EMEMY, set state to BAD: "+currentMsg.getFrom());
                    currentMsg.setStatus(VerifyableMessageObject.FAILED);
                }
                else
                {
                    //we don't have that person
                    //check if the message is authentic anyways
                    System.out.println("TOFDN: *** Don't found sender of message in our lists, checking message: "+currentMsg.getFrom() );
                    try {
                        currentId =new Identity(currentMsg.getFrom(),currentMsg.getKeyAddress());
                    }
                    catch( IllegalArgumentException e ) {
                        System.out.println("TODDN: *** IllegalArgumentException, set message state to N/A.");
                        currentMsg.setStatus(VerifyableMessageObject.NA);
                        return;
                    }

                    if( currentId.getKey() == Identity.NA )
                    {
                        System.out.println("TOFDN: *** Don't found public key of unknown sender, set state to N/A: "+currentMsg.getFrom() );
                        currentMsg.setStatus(VerifyableMessageObject.NA);
                    }
                    else if( frame1.getCrypto().verify(currentMsg.getContent(), currentId.getKey()) )
                    {
                        System.out.println("TOFDN: *** Message of unknown sender is signed correctly, set state to CHECK: "+currentMsg.getFrom() );
                        currentMsg.setStatus(VerifyableMessageObject.PENDING);
                    }
                    else //failed authentication, don't ask the user
                    {
                        System.out.println("TOFDN: *** Message of unknown sender is NOT signed correctly, set state to BAD: "+currentMsg.getFrom() );
                        currentMsg.setStatus(VerifyableMessageObject.FAILED);
                    }
                }
            }
        }
        catch(Throwable t)
        {
            System.out.println("Oo. Exception in verify() - settings message state to N/A.");
            t.printStackTrace();
            currentMsg.setStatus(VerifyableMessageObject.NA);
        }
    }

    protected void downloadDate(GregorianCalendar calDL)
    {
        String dirdate = DateFun.getDateOfCalendar(calDL);
        String fileSeparator = System.getProperty("file.separator");

        destination = new StringBuffer().append(keypool).append(board.getBoardFilename())
            .append(fileSeparator)
            .append(dirdate).append(fileSeparator).toString();

        File makedir = new File(destination);
        if( !makedir.exists() )
        {
            makedir.mkdirs();
        }

        File checkLockfile = new File(destination + "locked.lck");
        int index = 0;
        int failures = 0;
        int maxFailures = 3; // skip a maximum of 2 empty slots

        while( failures < maxFailures && (flagNew || !checkLockfile.exists()) )
        {
            String val = new StringBuffer().append(destination)
                                           .append(System.currentTimeMillis())
                                           .append(".txt.msg").toString();
            File testMe = new File(val);
            val = new StringBuffer().append(destination)
                                    .append(dirdate)
                                    .append("-")
                                    .append(board.getBoardFilename())
                                    .append("-")
                                    .append(index)
                                    .append(".txt").toString();
            File testMe2 = new File(val);
            if( testMe2.length() > 0 ) // already downloaded
            {
                index++;
                failures = 0;
            }
            else
            {
                String downKey = null;
                if( secure )
                {
                    downKey = new StringBuffer().append(publicKey)
                                                .append("/")
                                                .append(board.getBoardFilename())
                                                .append("/")
                                                .append(dirdate)
                                                .append("-")
                                                .append(index)
                                                .append(".txt").toString();
                }
                else
                {
                    downKey = new StringBuffer().append("KSK@frost/message/")
                                                .append(frame1.frostSettings.getValue("messageBase"))
                                                .append("/")
                                                .append(dirdate)
                                                .append("-")
                                                .append(board.getBoardFilename())
                                                .append("-")
                                                .append(index)
                                                .append(".txt").toString();
                }

                try { FcpRequest.getFile(downKey, "Unknown", testMe, downloadHtl, false); }
                catch(Throwable t)
                {
                    System.out.println("TOFDN - Error in run()/FcpRequest.getFile:");
                    t.printStackTrace();
                }

                // Download successful?
                if( testMe.length() > 0 )
                {
                    testMe.renameTo(testMe2);
                    testMe=testMe2;
                    // Does a duplicate message exist?
                    if( !exists(testMe) )
                    {
                        //test if encrypted and decrypt
                        String contents = FileAccess.readFile(testMe);
                        //System.out.println(contents);
                        String plaintext;
                        int encstart = contents.indexOf("==== Frost Signed+Encrypted Message ====");

                        if( encstart != -1 )
                        {
                            System.out.println("TOFDN: Decrypting message ...");
                            plaintext = frame1.getCrypto().decrypt(contents.substring(encstart,contents.length()),
                                                                   frame1.getMyId().getPrivKey());
                            contents = contents.substring(0,encstart) + plaintext;
                            //  System.out.println(contents);
                            FileAccess.writeFile(contents,testMe);
                        }

                        currentMsg = new FrostMessageObject(testMe);
                        if( currentMsg.getSubject().trim().indexOf("ENCRYPTED MSG FOR") != -1 &&
                            currentMsg.getSubject().indexOf(frame1.getMyId().getName()) == -1 )
                        {
                            System.out.println("TOFDN: Message is encrypted for someone else.");
                            //testMe.delete();
                            FileAccess.writeFile("Empty", testMe); // no more checking if for me, no more downloading
                            index++;
                            continue;
                        }
                        // verify the message
                        verify( calDL );

                        File sig = new File(testMe.getPath() + ".sig");

                        // Is this a valid message?
                        if( currentMsg.isValid() )
                        {
                            if( TOF.blocked(currentMsg,board) && testMe.length() > 0 )
                            {
                                board.incBlocked();
                                System.out.println("\nTOFDN: ########### blocked message for board '"+board.toString()+"' #########\n");
                            }
                            else
                            {
                                frame1.displayNewMessageIcon(true);
                                String[] header = {SettingsFun.getValue(testMe, "board"),
                                    SettingsFun.getValue(testMe, "from"),
                                    SettingsFun.getValue(testMe, "subject"),
                                    SettingsFun.getValue(testMe, "date") + " " +
                                    SettingsFun.getValue(testMe, "time")};
                                if( header.length == 4 )
                                    frame1.newMessageHeader = new StringBuffer().append("   ")
                                        .append(header[0]).append(" : ").append(header[1]).append(" - ")
                                        .append(header[2]).append(" (").append(header[3]).append(")").toString();
                                FileAccess.writeFile("This message is new!", testMe.getPath() + ".lck");

                                // add new message or notify of arrival
                                TOF.addNewMessageToTable(testMe, board);
                            }
                        }
                        else
                        {
                            FileAccess.writeFile("Empty", testMe);
                        }
                    }
                    else
                    { // duplicate message
                        System.out.println("TOFDN: ****** Duplicate Message : " + testMe.getName() + " *****");
                        FileAccess.writeFile("Empty", testMe);
                    }
                    index++;
                    failures = 0;
                }
                else
                {
                    if( !flagNew )
                    {
                        System.out.println("TOFDN: *** Increased TOF index for board '"+board.toString()+"' ***");
                    }
                    failures++;
                    index++;
                }
            }
            if( isInterrupted() )
                return;
        } // end-of: while
    }

    /**Constructor*/ //
    public MessageDownloadThread(boolean fn, FrostBoardObject boa, String dlHtl, String kpool, String maxmsg)
    {
        super(boa);
        this.flagNew = fn;
        this.board = boa;
        this.downloadHtl = dlHtl;
        this.keypool = kpool;
        this.maxMessageDownload = Integer.parseInt(maxmsg);
    }
}
