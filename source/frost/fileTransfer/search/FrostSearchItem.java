package frost.fileTransfer.search;

import frost.gui.objects.FrostBoardObject;
import frost.messages.*;
import frost.util.model.ModelItem;


public class FrostSearchItem extends ModelItem
{
	private FrostBoardObject board;
	private SharedFileObject sfo;
	private int state;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

	public FrostSearchItem(
		FrostBoardObject newBoard,
		SharedFileObject newKey,
		int newState) {
			
		board = newBoard;
		sfo = newKey;
		state = newState;
	}

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
