/*
  PerstFileListIndexEntry.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.filelist;

import org.garret.perst.IPersistentList;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

import frost.fileTransfer.FrostFileListFileObjectOwner;

public class PerstFileListIndexEntry extends Persistent {

	private static final long serialVersionUID = 1L;

    private IPersistentList<FrostFileListFileObjectOwner> fileOwnersWithText;

    public PerstFileListIndexEntry() {}
    public PerstFileListIndexEntry(final Storage storage) {
        fileOwnersWithText = storage.createScalableList();
    }
    public IPersistentList<FrostFileListFileObjectOwner> getFileOwnersWithText() {
        return fileOwnersWithText;
    }
    public void addFileOwnerWithText(final FrostFileListFileObjectOwner pmo) {
        fileOwnersWithText.add(pmo);
    }
    public void removeFileOwnerWithText(final FrostFileListFileObjectOwner pmo) {
        fileOwnersWithText.remove(pmo);
    }

    @Override
    public void deallocate() {
        if( fileOwnersWithText != null ) {
            fileOwnersWithText.deallocate();
            fileOwnersWithText = null;
        }
        super.deallocate();
    }
}
