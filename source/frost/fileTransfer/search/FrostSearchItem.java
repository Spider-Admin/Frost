/*
  FrostSearchItem.java / Frost
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
package frost.fileTransfer.search;

import frost.gui.objects.Board;
import frost.messages.*;
import frost.util.model.ModelItem;


public class FrostSearchItem extends ModelItem
{
	private Board board;
	private SharedFileObject sfo;
	private int state;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

	public FrostSearchItem(
		Board newBoard,
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

    public Board getBoard()
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
