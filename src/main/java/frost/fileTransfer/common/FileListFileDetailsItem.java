/*
  SearchItemPropertiesItem.java / Frost
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
package frost.fileTransfer.common;

import frost.Core;
import frost.fileTransfer.FrostFileListFileObjectOwner;
import frost.identities.Identity;
import frost.util.CopyToClipboardItem;
import frost.util.DateFun;
import frost.util.model.ModelItem;

public class FileListFileDetailsItem extends ModelItem<FileListFileDetailsItem> implements CopyToClipboardItem {

    private FrostFileListFileObjectOwner fileOwner;

    private String displayComment = null;
    private String displayKeywords = null;

    private String displayLastReceived;
    private String displayLastUploaded;

    private Identity ownerIdentity = null;

    public FileListFileDetailsItem(FrostFileListFileObjectOwner o) {
        fileOwner = o;
    }

    public String getDisplayComment() {
        if( displayComment == null ) {
            if( fileOwner.getComment() == null ) {
                displayComment = "";
            } else {
                displayComment = fileOwner.getComment();
            }
        }
        return displayComment;
    }

    public String getDisplayKeywords() {
        if( displayKeywords == null ) {
            if( fileOwner.getKeywords() == null ) {
                displayKeywords = "";
            } else {
                displayKeywords = fileOwner.getKeywords();
            }
        }
        return displayKeywords;
    }

    public String getDisplayLastReceived() {
        if( displayLastReceived == null ) {
            if( fileOwner.getLastReceived() == 0 ) {
                displayLastReceived = "";
            } else {
                displayLastReceived = DateFun.getExtendedDateFromMillis(fileOwner.getLastReceived());
            }
        }
        return displayLastReceived;
    }

    public String getDisplayLastUploaded() {
        if( displayLastUploaded == null ) {
            if( fileOwner.getLastUploaded() == 0 ) {
                displayLastUploaded = "";
            } else {
                displayLastUploaded = DateFun.getExtendedDateFromMillis(fileOwner.getLastUploaded());
            }
        }
        return displayLastUploaded;
    }

    /**
     * @return
     */
    public Identity getOwnerIdentity() {
        if( ownerIdentity == null ) {
            ownerIdentity = Core.getIdentitiesManager().getIdentity(fileOwner.getOwner());
        }
        return ownerIdentity;
    }

    public String getKey() {
        return fileOwner.getKey();
    }

    public String getFileName() {
        return getFileOwner().getName();
    }

    public long getFileSize() {
        return -1; // not used
    }

    public FrostFileListFileObjectOwner getFileOwner() {
        return fileOwner;
    }
}
