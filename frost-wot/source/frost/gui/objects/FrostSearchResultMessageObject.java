package frost.gui.objects;

import java.io.*;

import frost.gui.model.*;
import frost.messages.*;

public class FrostSearchResultMessageObject extends FrostMessageObject {

    private boolean messageIsArchived;
    
    public FrostSearchResultMessageObject(File file, boolean archived) throws MessageCreationException {
        super(file);
        messageIsArchived = archived;
    }
    
    public boolean isMessageArchived() {
        return messageIsArchived;
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
            case 0: return getIndex();
            case 1: return getFrom();
            case 2: return getBoard();
            case 3: return getSubject();
            case 4: return getMsgStatusString();
            case 5: return getDateAndTime();
            default: return "*ERR*"; 
        }
    }

}
