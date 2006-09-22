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

import frost.*;
import frost.fileTransfer.*;
import frost.util.model.*;

public class FrostSearchItem extends ModelItem {

    private FrostFileListFileObject fo;
    private int state;
    
    private Long sizeLong = null;
    private String dateStr = null;
    private Integer rating = null;
    private Integer sourceCount = null;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

    public FrostSearchItem(FrostFileListFileObject newKey, int newState) {
        fo = newKey;
        state = newState;
    }

    public String getFilename() {
        return fo.getDisplayName();
    }

    public String getComment() {
        if( fo.getDisplayComment() == null ) {
            return "";
        }
        return fo.getDisplayComment();
    }

    public Integer getRating() {
        if( rating == null ) {
            rating = new Integer(fo.getDisplayRating());
        }
        return rating;
    }

    public Long getSize() {
        if( sizeLong == null ) {
            sizeLong = new Long(fo.getSize());
        }
        return sizeLong;
    }

    public String getDate() {
        if( dateStr == null ) {
            long lastUploaded = fo.getDisplayLastUploaded(); 
            dateStr = (lastUploaded==0?"Never":DateFun.getExtendedDateFromMillis(lastUploaded));
        }
        return dateStr;
    }

    public String getKey() {
        return fo.getKey();
    }

    public int getState() {
        return state;
    }

    public String getSha() {
        return fo.getSha();
    }
    
    public FrostFileListFileObject getFrostFileListFileObject() {
        return fo;
    }
    
    public Integer getSourceCount() {
        if( sourceCount == null ) {
            sourceCount = new Integer(fo.getFrostFileListFileObjectOwnerList().size());
        }
        return sourceCount;
    }
}
