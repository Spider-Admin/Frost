package frost.gui.objects;

import java.io.File;

import frost.FileAccess;
import frost.gui.model.TableMember;
import frost.messages.*;

public class FrostMessageObject extends VerifyableMessageObject implements TableMember
{
	
	protected String dateAndTime = null;
	protected Boolean messageIsNew = null;
    	
	/**
     * This constructor can be used to build a messageobject from
     * an existing file.
     * 
     * @param file  The xml file to read
     * @throws Exception  If the file could'nt be loaded
     */
    FrostMessageObject(File file) throws Exception
    {
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
    
    public boolean isMessageNew()
    {
        if( this.messageIsNew == null )
        {
            File newMessage = new File(getFile().getPath() + ".lck");
            if (newMessage.isFile()) 
            {
                this.messageIsNew = new Boolean(true);
                return true;
            }
            this.messageIsNew = new Boolean(false);
            return false;
        }
        
        return this.messageIsNew.booleanValue();
    }
    
    public void setMessageNew(boolean newMsg)
    {
        final String newMsgIndicator = getFile().getPath() + ".lck";
        Runnable ioworker = null;
        if( newMsg )
        {
            this.messageIsNew = new Boolean(true);
            ioworker = new Runnable() {
                public void run() {
                    FileAccess.writeFile("This message is new!", newMsgIndicator);
                } };
        }
        else
        {
            this.messageIsNew = new Boolean(false);
            ioworker = new Runnable() {
                public void run() {
                    new File(newMsgIndicator).delete();
                } };
        }
        
        new Thread( ioworker ).start(); // do IO in another thread, not here in Swing thread
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
            case 3: return getStatus();
            case 4: return getDateAndTime();
            default: return "*ERR*"; 
        }
    }
    
    public String getDateAndTime()
    {
        return this.dateAndTime;
    }

}