/*
  FrostMessageObject.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui.objects;

import java.io.File;

import frost.FileAccess;
import frost.gui.model.TableMember;
import frost.messages.*;

public class FrostMessageObject extends VerifyableMessageObject implements TableMember {
    
	protected String dateAndTime = null;
    	
	/**
     * This constructor can be used to build a messageobject from
     * an existing file.
     * 
     * @param file  The xml file to read
     * @throws MessageCreationException  If the file couldn't be loaded
     */
    FrostMessageObject(File file) throws MessageCreationException {
        super(file);
        buildVisibleStrings();
    }
    /**
     * This constructor can be used to build a messageobject from
     * an existing file.
     * 
     * @param file  The xml file to read
     * @throws Exception  If the file could'nt be loaded
     */
    FrostMessageObject(String filename) throws Exception
    {
        this(new File(filename));
    }
    
    /*
     * Build a String of format yyyy.mm.dd hh:mm:ssGMT
     */
    private void buildVisibleStrings()
    {
        // this is date format xxxx.x.x , but we want xxxx.xx.xx , so lets convert it
        String date = getDate();
        String time = getTime();

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

        this.dateAndTime = datetime.toString();
    }
    
    /**
	 * @return
	 */
	public boolean containsAttachments() {
		if ((getAttachmentsOfType(Attachment.BOARD).size() > 0) ||
			(getAttachmentsOfType(Attachment.FILE).size() > 0)) {
			return true;
		}
		return false;
	}
    
	/*
	 * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember,
	 *      int)
	 */
	public int compareTo(TableMember another, int tableColumnIndex) {
		String c1 = (String) getValueAt(tableColumnIndex);
		String c2 = (String) another.getValueAt(tableColumnIndex);
		if (tableColumnIndex == 4) {
			return c1.compareTo(c2);
		} else {
			// If we are sorting by anything but date...
			if (tableColumnIndex == 2) {
				//If we are sorting by subject...
				if (c1.indexOf("Re: ") == 0) {
					c1 = c1.substring(4);
				}
				if (c2.indexOf("Re: ") == 0) {
					c2 = c2.substring(4);
				}
			}
			int result = c1.compareToIgnoreCase(c2);
			if (result == 0) { // Items are the same. Date and time decides
				String d1 = (String) getValueAt(4);
				String d2 = (String) another.getValueAt(4);
				return d1.compareTo(d2);
			} else {
				return result;
			}
		}
	}

    /* 
     * @see frost.gui.model.TableMember#getValueAt(int)
     */
    public Object getValueAt(int column)
    {
        switch(column)
        {
            case 0: return getIndex();
            case 1: return getFrom();
            case 2: return getSubject();
            case 3: return getMsgStatusString();
            case 4: return getDateAndTime();
            default: return "*ERR*"; 
        }
    }
    
    public String getDateAndTime()
    {
        return this.dateAndTime;
    }

}