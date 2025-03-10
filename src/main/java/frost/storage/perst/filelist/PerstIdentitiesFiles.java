/*
  PerstIdentitiesFiles.java / Frost
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

public class PerstIdentitiesFiles extends Persistent {

	private static final long serialVersionUID = 1L;

    private String uniqueName;
    private IPersistentList<FrostFileListFileObjectOwner> filesFromIdentity;

    public PerstIdentitiesFiles() {}
    public PerstIdentitiesFiles(final String un, final Storage storage) {
        uniqueName = un;
        filesFromIdentity = storage.createScalableList();
    }
    public String getUniqueName() {
        return uniqueName;
    }
    public IPersistentList<FrostFileListFileObjectOwner> getFilesFromIdentity() {
        return filesFromIdentity;
    }
    public void addFileToIdentity(final FrostFileListFileObjectOwner pmo) {
        filesFromIdentity.add(pmo);
    }
    public void removeFileFromIdentity(final FrostFileListFileObjectOwner pmo) {
        filesFromIdentity.remove(pmo);
    }

    @Override
    public void deallocate() {
        if( filesFromIdentity != null ) {
            filesFromIdentity.deallocate();
            filesFromIdentity = null;
        }
        super.deallocate();
    }
}
