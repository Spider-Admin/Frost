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

//TODO: implement listener for threadStopped, ...

package frost.threads;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

import frost.*;
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

        String tofType;
        if( flagNew )
            tofType="TOF Download";
        else
            tofType="TOF Download Back";

        // Wait some random time to speed up the update of the TOF table
        // ... and to not to flood the node
        int waitTime = waitTime = (int)(Math.random() * 5000); // wait a max. of 5 seconds between start of threads
        mixed.wait(waitTime);

        System.out.println(tofType + " Thread started for board "+board.toString());

        UpdateIdThread uit=null;
        if( flagNew )
        {
            uit= new UpdateIdThread(board);
            uit.start();
            mixed.wait(5000);
        }

        if( isInterrupted() )
        {
            notifyThreadFinished(this);
            return;
        }

        // switch public / secure board
        String val = new StringBuffer().append(frame1.keypool).append(board.getBoardFilename())
                      .append(".key").toString();
        String state = SettingsFun.getValue(val, "state");
        if( state.equals("writeAccess") || state.equals("readAccess") )
        {
            publicKey = SettingsFun.getValue(val, "publicKey");
            secure = true;
        }
        else
        {
            secure = false;
        }

        GregorianCalendar cal= new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        if( this.flagNew )
        {
            // download actual date
            downloadDate(cal);

            // maybe wait for update id thread to finish
            if( uit != null )
            {
                try { uit.join(); }
                catch(InterruptedException ex) {}
            }
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
        System.out.println(tofType + " Thread stopped for board "+board.toString());

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

    private void verify()
    {
        System.out.println("verifying...");
        if( (currentMsg.getKeyAddress() == "none") || (currentMsg.getFrom().indexOf("@") == -1) )
        {
            currentMsg.setStatus(VerifyableMessageObject.OLD);
        }

        else
        { //the message contains the CHK of a public key
            // see if we have this name on our list
            if( frame1.getFriends().containsKey(currentMsg.getFrom()) )
            {
                System.out.println("have this person on our list");
                //yes, we have that person, see if the addreses are the same
                currentId = frame1.getFriends().Get(currentMsg.getFrom());
                //check if the key addreses are the same, verify
                if( (currentId.getKeyAddress().compareTo(currentMsg.getKeyAddress()) == 0) &&
                    frame1.getCrypto().verify(currentMsg.getContent(),currentId.getKey()) )
                    currentMsg.setStatus(VerifyableMessageObject.VERIFIED);
                else // verification FAILED!
                    currentMsg.setStatus(VerifyableMessageObject.FAILED);
            }
            else if( frame1.getEnemies().containsKey(currentMsg.getFrom()) ) //we have the person, but he is blacklisted
                currentMsg.setStatus(VerifyableMessageObject.FAILED);

            else
            { //we don't have that person
                //check if the message is authentic anyways
                System.out.println("trying to add");
                try
                {
                    currentId =new Identity(currentMsg.getFrom(),currentMsg.getKeyAddress());
                }
                catch( IllegalArgumentException e )
                {
                    System.out.println("illegal argument exception");
                    currentMsg.setStatus(VerifyableMessageObject.NA);return;
                }
                if( currentId.getKey() == Identity.NA )
                    currentMsg.setStatus(VerifyableMessageObject.NA);
                else
                    if( frame1.getCrypto().verify(currentMsg.getContent(),currentId.getKey()) )
                {
                    currentMsg.setStatus(VerifyableMessageObject.PENDING);
                    //frame1.getFriends().Add(currentId);
                    //TODO: the thread that will update all the ids
                }

                else //failed authentication, don't ask the user
                    currentMsg.setStatus(VerifyableMessageObject.FAILED);
            }
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
            System.out.println("creating directory: " + destination);
            makedir.mkdirs();
        }

        File checkLockfile = new File(destination + "locked.lck");
        int index = 0;
        int failures = 0;
        int maxFailures = 2;

        while( failures < maxFailures && (flagNew || !checkLockfile.exists()) )
        {

            String val = new StringBuffer().append(destination).append(System.currentTimeMillis())
                .append(".txt.msg").toString();
            File testMe = new File(val);
            val = new StringBuffer().append(destination).append(dirdate).append("-").append(board.getBoardFilename())
                  .append("-").append(index).append(".txt").toString();
            File testMe2 = new File(val);
            if( testMe2.length() > 0 ) // already downloaded
            {
                index++;
                failures = 0;
            }
            else
            {
                if( secure )
                {
                    val=new StringBuffer().append(publicKey).append("/")
                        .append(board.getBoardFilename()).append("/").append(dirdate)
                        .append("-").append(index).append(".txt").toString();
                    String downKey = val;
                    FcpRequest.getFile(downKey, "Unknown", testMe, downloadHtl, false);
                }
                else
                {
                    val=new StringBuffer().append(frame1.frostSettings.getValue("messageBase"))
                        .append("/").append(dirdate)
                        .append("-").append(board.getBoardFilename())
                        .append("-").append(index).append(".txt").toString();

                    FcpRequest.getFile("KSK@sftmeage/"+val, "Unknown", testMe, downloadHtl, false);
// Temporary hack due to namespace error
                    FcpRequest.getFile("KSK@frost/message/"+val, "Unknown", testMe, downloadHtl, false);
// End temporary hack
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
                        String contents =FileAccess.readFile(testMe);
                        //System.out.println(contents);
                        String plaintext;//==== Frost Signed+Encrypted Message ====
                        int encstart =contents.indexOf("==== Frost Signed+Encrypted Message ====");
                        //System.out.println("index of header is " + encstart);
                        if( encstart != -1 )
                        {
                            System.out.println("decrypting...");
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
                            System.out.println("encrypted for someone else");
                            testMe.delete();
                            index++;
                            continue;
                        }
                        verify();
                        File sig = new File(testMe.getPath() + ".sig");

                        // Is this a valid message?
                        if( currentMsg.isValid() )
                        {
                            if( TOF.blocked(currentMsg) && testMe.length() > 0 )
                            {
                                board.incBlocked();
                                System.out.println("\n########### blocked message #########\n");
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
                        System.out.println("****** Duplicate Message : " + testMe.getName() + " *****");
                        FileAccess.writeFile("Empty", testMe);
                    }
                    //if (((BoardStat)frame1.boardStats.get(currentMsg.getBoard())).spammed()) index+=(new Random()).nextInt(100);
                    index++;
                    failures = 0;
                }
                else
                {
                    if( !flagNew )
                        System.out.println("***** Increased TOF index *****");
                    failures++;
                    index++;
                }
            }

            if( isInterrupted() )
                return;

        } // end-of: while

        /*if (!flagNew) {
            String text = String.valueOf(index - 2);
            FileAccess.writeFile(text, destination + "locked.lck");
        }*/
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
