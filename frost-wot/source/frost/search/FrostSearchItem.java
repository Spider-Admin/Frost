package frost.search;

import frost.gui.model.TableMember;
import frost.gui.objects.FrostBoardObject;
import frost.gui.translation.UpdatingLanguageResource;
import frost.messages.*;


public class FrostSearchItem implements TableMember
{
    private String offlineLocalized;
	private String anonymousLocalized;
	private UpdatingLanguageResource languageResource;
	private FrostBoardObject board;
	private SharedFileObject sfo;
	private int state;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

	public FrostSearchItem(
		UpdatingLanguageResource newLanguageResource,
		FrostBoardObject newBoard,
		SharedFileObject newKey,
		int newState) {
			
		languageResource = newLanguageResource;
		board = newBoard;
		sfo = newKey;
		state = newState;
		
		anonymousLocalized = languageResource.getString("FrostSearchItemObject.Anonymous");
		offlineLocalized = languageResource.getString("FrostSearchItemObject.Offline");
	}

	/**
	 * Returns the object representing value of column. Can be string or icon
	 *
	 * @param   column  Column to be displayed
	 * @return  Object representing table entry.
	 */
	public Object getValueAt(int column) {
		// NEVER add <html> here, add a state or method like isOffline
		// and do it the right way in SearchTable cellRenderer !!!
		switch (column) {
			case 0 :
				return sfo.getFilename();
			case 1 :
				return sfo.getSize();
			case 2 :
				if (getState() == STATE_OFFLINE)
					return offlineLocalized;
				else
					return sfo.getDate();
			case 3 :
				if (sfo.getOwner() == null || sfo.getOwner().length() == 0)
					return anonymousLocalized;
				else
					return sfo.getOwner();
			case 4 :
				return board.toString();
			default :
				return languageResource.getString("FrostSearchItemObject.*ERROR*");
		}
	}
    
    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        // special sort handling for DATE column:
        // sort alphanum chars behind digits always (check first char).
        // this is intended to move the OFFLINE files behind the online files always
        if( tableColumIndex == 2 )
        {
            String c1 = (String)getValueAt(tableColumIndex);
            String c2 = (String)anOther.getValueAt(tableColumIndex);
            if( c1.length() > 0 && c2.length() > 0 )
            {
                if( Character.isDigit(c1.charAt(0)) && !Character.isDigit(c2.charAt(0)) )
                {
                    return 1;
                }
                if( Character.isDigit(c2.charAt(0)) && !Character.isDigit(c1.charAt(0)) )
                {
                    // c2 starts with digit, c1 not
                    return -1;
                }
            }
            // otherwise use standard comparing
        }
        
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

////// Implementing the FrostSearchItem interface //////

    public String getFilename()
    {
        return sfo.getFilename();
    }

    public Long getSize()
    {
        return sfo.getSize();
    }

    public String getDate()
    {
        return sfo.getDate();
    }

    public String getKey()
    {
        return sfo.getKey();
    }

    public FrostBoardObject getBoard()
    {
        return board;
    }

    public int getState()
    {
        return state;
    }
    
    public String getOwner() {
    	return sfo.getOwner();
    }
    
    public String getSHA1() {
    	return sfo.getSHA1();
    }
    public String getBatch() {
    	return sfo.getBatch();
    }
	/**
	 * @return Returns the sfo.
	 */
	public String getRedirect() {
		if (sfo instanceof RedirectFileObject)
			return ((RedirectFileObject)sfo).getRedirect();
		else return null;
	}

}
