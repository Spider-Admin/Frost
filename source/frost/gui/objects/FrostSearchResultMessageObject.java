/*
  FrostSearchResultMessageObject.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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

import frost.gui.model.*;
import frost.messages.*;

public class FrostSearchResultMessageObject implements TableMember {

    private FrostMessageObject messageObject;

    public FrostSearchResultMessageObject(FrostMessageObject mo) {
        messageObject = mo;
    }

    public FrostMessageObject getMessageObject() {
        return messageObject;
    }
    
    /*
     * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember,
     *      int)
     */
    public int compareTo(TableMember another, int tableColumnIndex) {
        String c1 = (String) getValueAt(tableColumnIndex);
        String c2 = (String) another.getValueAt(tableColumnIndex);
        if (tableColumnIndex == 5) {
            return c1.compareTo(c2);
        } else {
            // If we are sorting by anything but date...
            if (tableColumnIndex == 3) {
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
    public Object getValueAt(int column) {
        switch(column) {
            case 0: return ""+messageObject.getIndex();
            case 1: return messageObject.getFromName();
            case 2: return messageObject.getBoard().getName();
            case 3: return messageObject.getSubject();
            case 4: return messageObject.getMessageStatusString();
            case 5: return messageObject.getDateAndTime();
            default: return "*ERR*";
        }
    }
}
