/*
  FrostSearchItem.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

public class FrostSearchItem extends ModelItem {

    private FrostSharedFileObject fo;
    private FrostSharedFileObjectOwnerBoard ob;
    private int state;
    
    private Long sizeLong = null;
    private String dateStr = null;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

    public FrostSearchItem(
        FrostSharedFileObject newKey,
        int newState) 
    {
        fo = newKey;
        // FIXME: show multiple
        ob = (FrostSharedFileObjectOwnerBoard)fo.getFrostSharedFileObjectOwnerBoardList().get(0);
        
        state = newState;
    }

    public String getFilename() {
        return ob.getName();
    }

    public Long getSize() {
        if( sizeLong == null ) {
            sizeLong = new Long(fo.getSize());
        }
        return sizeLong;
    }

    public String getDate() {
        if( dateStr == null ) {
            dateStr = (ob.getLastUploaded()==null?"Never":ob.getLastUploaded().toString());
        }
        return dateStr;
    }

    public String getKey() {
        return fo.getKey();
    }

    public Board getBoard() {
        return ob.getBoard();
    }

    public int getState() {
        return state;
    }

    public String getOwner() {
        return ob.getOwner();
    }

    public String getSHA1() {
        return fo.getSha1();
    }
    
    public FrostSharedFileObject getFrostSharedFileObject() {
        return fo;
    }
}
