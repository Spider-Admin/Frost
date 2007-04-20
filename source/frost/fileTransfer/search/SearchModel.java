/*
  SearchModel.java / Frost
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
import frost.util.model.*;

public class SearchModel extends SortedModel {

    public SearchModel(SortedTableFormat f) {
        super(f);
    }

    public void addSearchItem(FrostSearchItem searchItem) {
        addItem(searchItem);
    }

    public void addItemsToDownloadTable(ModelItem[] selectedItems) {

        if( selectedItems == null ) {
            return;
        }

        final DownloadModel downloadModel = FileTransferManager.inst().getDownloadManager().getModel();

        for (int i = selectedItems.length - 1; i >= 0; i--) {
            FrostSearchItem searchItem = (FrostSearchItem) selectedItems[i];
            FrostFileListFileObject flf = searchItem.getFrostFileListFileObject();
            
            FrostDownloadItem dlItem = new FrostDownloadItem(flf, flf.getDisplayName());
            downloadModel.addDownloadItem(dlItem);
            searchItem.updateState();
        }
    }
}
