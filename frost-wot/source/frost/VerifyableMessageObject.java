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

import java.io.File;
import frost.crypt.crypt;

public class VerifyableMessageObject extends MessageObject implements Cloneable
{
    public static final String VERIFIED = "<html><b><font color=\"green\">GOOD</font></b></html>";
    public static final String FAILED   = "<html><b><font color=\"red\">BAD</font></b></html>";
    public static final String NA       = "N/A";
    public static final String OLD      = "NONE";
    public static final String PENDING  = "<html><b><font color=#FFCC00>CHECK</font></b></html>";

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
        System.out.println("setting message status to "+newStatus);
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
}
