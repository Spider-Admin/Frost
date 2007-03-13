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

import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.util.*;
import frost.util.model.*;

public class FrostSearchItem extends ModelItem implements CopyToClipboardItem {

    private FrostFileListFileObject fo;
    private int state;

    private Long sizeLong = null;
    private String lastUploadedStr = null;
    private String lastReceivedStr = null;
    private Integer rating = null;
    private Integer sourceCount = null;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table
    public static final int STATE_OFFLINE     = 5; // set if file is offline

    public FrostSearchItem(FrostFileListFileObject newKey) {
        fo = newKey;
        updateState();
    }
    
    public void updateState() {

        // Already downloaded files get a nice color outfit (see renderer in SearchTable)

        DownloadModel downloadModel = FileTransferManager.inst().getDownloadManager().getModel();
        SharedFilesModel sharedFilesModel = FileTransferManager.inst().getSharedFilesManager().getModel();

        int searchItemState = FrostSearchItem.STATE_NONE;

        String SHA1 = fo.getSha();

        if (downloadModel.containsItemWithSha(SHA1)) {
            // this file is in download table -> blue
            searchItemState = FrostSearchItem.STATE_DOWNLOADING;
        } else if ( fo.getLastDownloaded() > 0 ) {
            // file was downloaded before -> light_gray
            searchItemState = FrostSearchItem.STATE_DOWNLOADED;
        } else if (sharedFilesModel.containsItemWithSha(SHA1)) {
            // this file is in upload table -> green
            searchItemState = FrostSearchItem.STATE_UPLOADING;
        } else if (fo.getKey() == null) {
            // this file is offline -> gray
            searchItemState = FrostSearchItem.STATE_OFFLINE;
        }

        state = searchItemState;
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

    public String getKeywords() {
        if( fo.getDisplayKeywords() == null ) {
            return "";
        }
        return fo.getDisplayKeywords();
    }

    public Boolean hasInfosFromMultipleSources() {
        return fo.hasInfosFromMultipleSources();
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

    public long getFileSize() {
        return getSize().longValue();
    }

    public String getLastUploadedStr() {
        if( lastUploadedStr == null ) {
            long lastUploaded = fo.getLastUploaded();
            if( lastUploaded > 0 ) {
                lastUploadedStr = DateFun.getExtendedDateFromMillis(lastUploaded);
            } else {
                lastUploadedStr = "";
            }
        }
        return lastUploadedStr;
    }

    public String getLastReceivedString() {
        if( lastReceivedStr == null ) {
            if( getFrostFileListFileObject().getLastReceived() > 0 ) {
                lastReceivedStr = DateFun.getExtendedDateFromMillis(getFrostFileListFileObject().getLastReceived());
            } else {
                lastReceivedStr = "";
            }
        }
        return lastReceivedStr;
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
    
    public String toString() {
        return getFilename();
    }
}
