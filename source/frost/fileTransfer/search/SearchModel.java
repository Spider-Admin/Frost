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

import java.util.*;

import frost.SettingsClass;
import frost.fileTransfer.download.*;
import frost.identities.*;
import frost.util.model.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SearchModel extends OrderedModel {

    private FrostIdentities identities;

    private DownloadModel downloadModel;

    private SettingsClass settings;

    /**
     *
     */
    public SearchModel(SettingsClass frostSettings) {
        super();
        settings = frostSettings;
    }

    /**
     * @param searchItem
     */
    public void addSearchItem(FrostSearchItem searchItem) {
        addItem(searchItem);
    }

    /**
     * @param downloadModel
     */
    public void setDownloadModel(DownloadModel model) {
        downloadModel = model;
    }

    /**
     * @param selectedItems
     */
    public void addItemsToDownloadModel(ModelItem[] selectedItems) {
        for (int i = 0; i < selectedItems.length; i++) {
            FrostSearchItem searchItem = (FrostSearchItem) selectedItems[i];
            FrostDownloadItem dlItem = new FrostDownloadItem(searchItem);
            downloadModel.addDownloadItem(dlItem);
        }
    }

    /**
     *
     */
    public synchronized void addAllItemsToDownloadModel() {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostSearchItem searchItem = (FrostSearchItem) iterator.next();
            FrostDownloadItem dlItem = new FrostDownloadItem(searchItem);
            downloadModel.addDownloadItem(dlItem);
        }
    }

    /**
     * @param selectedItems
     * @return
     */
//    public Iterator getSelectedItemsOwners(ModelItem[] selectedItems) {
//        List result = new LinkedList();
//        for (int i = 0; i < selectedItems.length; i++) {
//            FrostSearchItem srItem = (FrostSearchItem) selectedItems[i];
//            String owner = srItem.getOwner();
//            //check if null or from myself
//            if (owner == null || owner.compareTo(identities.getMyId().getUniqueName()) == 0)
//                continue;
//
//            //see if already on some list
//            Identity id = identities.getIdentity(owner);
//            if (id != null) {
//                result.add(id);
//            }
//        }
//        return result.iterator();
//    }

    /**
     * @param newIdentities
     */
    public void setIdentities(FrostIdentities newIdentities) {
        identities = newIdentities;
    }

}
