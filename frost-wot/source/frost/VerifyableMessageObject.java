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

package frost;

import java.io.*;
import java.text.*;
import java.util.*;
import frost.crypt.crypt;
import frost.identities.*;

public class VerifyableMessageObject extends MessageObject implements Cloneable
{
    public static final String PENDING  = "<html><b><font color=#FFCC00>CHECK</font></b></html>";
    public static final String VERIFIED = "<html><b><font color=\"green\">GOOD</font></b></html>";
    public static final String FAILED   = "<html><b><font color=\"red\">BAD</font></b></html>";
    public static final String NA       = "N/A";
    public static final String OLD      = "NONE";

    private String currentStatus;
    private final boolean isVerifyable;

    public boolean isVerifyable()
    {
        return isVerifyable;
    }

    public VerifyableMessageObject copy() throws CloneNotSupportedException
    {
        return (VerifyableMessageObject)this.clone();
    }

    /** get the verification key*/
    public String getKeyAddress()
    {
        int start = content.lastIndexOf("<key>");
        int end = content.indexOf("</key>",start);
        if( (start == -1) || (end == -1) || (end-start < 55) ) return new String("none");
        return content.substring(start+5,end);
    }

    /**gets the plaintext only */
    public String getPlaintext()
    {
        int offset =0;
        if( isVerifyable() ) offset = crypt.MSG_HEADER_SIZE;
        //if (!isVerifyable()) return content;

        if( content.indexOf("<attached>") == -1  && content.indexOf("<board>") ==-1 )
            if( isVerifyable() ) return content.substring(offset, content.lastIndexOf("<key>"));
            else return content;
        else
        {
            if( content.indexOf("<board>") == -1 )
                return content.substring(offset, content.indexOf("<attached>"));
            if( content.indexOf("<attached>") == -1 )
                return content.substring(offset, content.indexOf("<board>"));
            if( content.indexOf("<board>") < content.indexOf("<attached>") )
                return content.substring(offset, content.indexOf("<board>"));
            else
                return content.substring(offset, content.indexOf("<attached>"));
        }
    }

    /**gets the status of the message*/
    public String getStatus()
    {
        return currentStatus;
    }

    /** is the message verified?*/
    public boolean isVerified()
    {
        return(currentStatus.compareTo(VERIFIED) == 0);
    }

    /** set the status */
    public void setStatus(String newStatus)
    {
        //System.out.println("setting message status to "+newStatus);
        currentStatus = newStatus;
        FileAccess.writeFile(currentStatus,file.getPath() + ".sig");
    }

    /**Constructors*/
    public VerifyableMessageObject()
    {
        super();
        currentStatus = NA;
        isVerifyable=false;
    }

    public VerifyableMessageObject(File file)
    {
        super(file);

        if( from.indexOf("@") == -1 ||
            content.indexOf("===Frost signed message===\n") == -1 ||
            content.indexOf("\n=== Frost message signature: ===\n") == -1 )
        {
            isVerifyable=false;
        }
        else
        {
            isVerifyable = true;
        }

        File sigFile = new File(file.getPath() + ".sig");
        if( !sigFile.exists() )
        {
            currentStatus = NA;
        }
        else
        {
            currentStatus = FileAccess.readFile(sigFile);
        }
    }

    private String[] cachedRow = null;

    public String[] getVRow()
    {
        if( cachedRow == null )
        {
            cachedRow = buildRowData();
        }
        return cachedRow;
    }

    protected String[] buildRowData()
    {
        String []row = new String[5];
        String []temp = ((MessageObject)this).getRow();

        row[0]=temp[0];
        row[1]=temp[1];
        row[2]=temp[2];
        row[3]=currentStatus;
        // row[4]=temp[3]; // date + " " + time

        // this is date format xxxx.x.x , but we want xxxx.xx.xx , so lets convert it
        String date = temp[3];
        String time = temp[4];

        int point1 = date.indexOf(".");
        int point2 = date.lastIndexOf(".");
        String year = date.substring(0, point1);
        String month = date.substring(point1+1, point2);
        String day = date.substring(point2+1, date.length());
        StringBuffer datetime = new StringBuffer(11);
        datetime.append(year).append(".");
        if( month.length() == 1 )
            datetime.append("0");
        datetime.append(month).append(".");
        if( day.length() == 1 )
            datetime.append("0");
        datetime.append(day);
        datetime.append(" ").append( time );

        row[4] = datetime.toString();

        return row;
    }

    /**
     * First time verify.
     */
    public void verifyIncoming(GregorianCalendar dirDate)
    {
        VerifyableMessageObject currentMsg = this;
        System.out.println("TOFDN: ****** Verifying incoming message ******");
        try { // if something fails here, set msg. to N/A (maybe harmful message)

            if( currentMsg.verifyDate(dirDate) == false || currentMsg.verifyTime() == false )
            {
                currentMsg.setDate(""); // -> leads to isValid()==false + msg. file is written with content = "Empty"
                return;
            }

            Identity currentId;

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
                        System.out.println("TOFDN: *** Message seems to be from a FRIEND (from is equal), but signature is wrong; set state to N/A: "+currentMsg.getFrom());
                        currentMsg.setStatus(VerifyableMessageObject.NA);
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

    public boolean verifyDate(GregorianCalendar dirDate)
    {
        VerifyableMessageObject currentMsg = this;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
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
            System.out.println("* verifyDate(): Invalid date string found, will block message: "+msgDateStr);
            return false;
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
            //System.out.println("* verifyDate(): Checked message date, seems to be OK ("+msgDateStr+").");
        }
        else if( diffDays < 0 )
        {
            // msgDate is later than dirDate
            System.out.println("* verifyDate(): Date in message is later than date in URL, will block message: "+msgDateStr);
            return false;
        }
        else if( diffDays > 1 ) // more than 1 day older
        {
            // dirDate is later than msgDate
            System.out.println("* verifyDate(): Date in message is earlier than date in URL, will block message: "+msgDateStr);
            return false;
        }
        return true;
    }

    public boolean verifyTime()
    {
        VerifyableMessageObject currentMsg = this;
        // time=06:52:48GMT  <<-- expected format
        String timeStr = currentMsg.getTime();
        if( timeStr == null )
        {
            System.out.println("* verifyTime(): Time is NULL, blocking message.");
            return false;
        }
        timeStr = timeStr.trim();

        if( timeStr.length() != 11 )
        {
            System.out.println("* verifyTime(): Time string have invalid length (!=11), blocking message: "+timeStr);
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
            System.out.println("* verifyTime(): Time string have invalid format (xx:xx:xxGMT), blocking message: "+timeStr);
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
        } catch(Exception ex)
        {
            System.out.println("* verifyTime(): Could not parse the numbers, blocking message: " + timeStr);
            return false;
        }
        if( ihours < 0 || ihours > 23 ||
            iminutes < 0 || iminutes > 59 ||
            iseconds < 0 || iseconds > 59 )
        {
            System.out.println("* verifyTime(): Time is invalid, blocking message: " + timeStr);
            return false;
        }
//        System.out.println("* verifyTime(): Checked time of message, seems to be OK ("+timeStr+").");

        return true;
    }
}
