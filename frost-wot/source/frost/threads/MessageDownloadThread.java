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

import java.io.File;
import java.util.*;

import frost.*;
import frost.gui.objects.*;

/**
 * Downloads messages
 */
public class MessageDownloadThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    public FrostBoardObject board;
    private int downloadHtl;
    private String keypool;
    private int maxMessageDownload;
    private String destination;
    private boolean secure;
    private String publicKey;
    private boolean flagNew;

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
            System.out.println(Thread.currentThread().getName()+": Oo. Exception in MessageDownloadThread:");
            t.printStackTrace(System.out);
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

    protected void downloadDate(GregorianCalendar calDL)
    {
        VerifyableMessageObject currentMsg=null;
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
        int maxFailures;
        if( flagNew )
        {
            maxFailures = 3; // skip a maximum of 2 empty slots for today
        }
        else
        {
            maxFailures = 2; // skip a maximum of 1 empty slot for backload
        }

        while( failures < maxFailures && (flagNew || !checkLockfile.exists()) )
        {
	    try { //make a wide net so that evil messages don't kill us
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

                try { FcpRequest.getFile(downKey, null, testMe, downloadHtl, false); }
                catch(Throwable t)
                {
                    System.out.println(Thread.currentThread().getName()+" :TOFDN - Error in run()/FcpRequest.getFile:");
                    t.printStackTrace(System.out);
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
                        String contents = FileAccess.readFileRaw(testMe);
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
                        currentMsg.verifyIncoming( calDL );

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
                        System.out.println(Thread.currentThread().getName()+": TOFDN: ****** Duplicate Message : " + testMe.getName() + " *****");
                        FileAccess.writeFile("Empty", testMe);
                    }
                    index++;
                    failures = 0;
                }
                else
                {
/*                    if( !flagNew )
                    {
                        System.out.println("TOFDN: *** Increased TOF index for board '"+board.toString()+"' ***");
                    }*/
                    failures++;
                    index++;
                }
            }
            if( isInterrupted() )
                return;
	 }catch(Throwable t) {
	 	t.printStackTrace(System.out);
		index++;
	 }
        } // end-of: while
    }

    /**Constructor*/ //
    public MessageDownloadThread(boolean fn, FrostBoardObject boa, int dlHtl, String kpool, String maxmsg)
    {
        super(boa);
        this.flagNew = fn;
        this.board = boa;
        this.downloadHtl = dlHtl;
        this.keypool = kpool;
        this.maxMessageDownload = Integer.parseInt(maxmsg);
    }
}
