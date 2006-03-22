/*
 SortedModel.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.model.gui;

import java.util.*;

import frost.util.model.*;

/**
 * @author Administrator
 *
 * This subclass of OrderedModel keeps a copy of the items of the OrderedModel that
 * is passed to it as a parameter of the constructor. It keeps them sorted with the
 * comparators of the SortedTableFormat that is passed to it as a parameter of the
 * constructor and the input that it receives through the sort method.
 */
public class SortedModel extends OrderedModel {
	
	/**
	 * This inner class listens to the events of the source model (to keep both
	 * in synth)
	 */
	private class Listener implements OrderedModelListener {
	
		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem, int, java.lang.Object, java.lang.Object)
		 */
		public void itemChanged(
			int position,
			ModelItem item,
			int fieldID,
			Object oldValue,
			Object newValue) {

				if (columnNumber == -1) {
					fireItemChanged(item, fieldID, oldValue, newValue);
				} else {
					reinsertItem(item);
				}
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem)
		 */
		public void itemChanged(int position, ModelItem item) {
			if (columnNumber == -1) {
				fireItemChanged(item);
			} else {
				reinsertItem(item);
			}	
		}
	
		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemAdded(int, frost.util.model.ModelItem)
		 */
		public void itemAdded(int position, ModelItem item) {
			if (columnNumber == -1) {
				addItem(item);
			} else {
				addItem(item, getInsertionPoint(item));
			}
		}
	
		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemsRemoved(int[], frost.util.model.ModelItem[])
		 */
		public void itemsRemoved(int[] positions, ModelItem[] items) {
			removeItems(items);	
		}
	
		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#modelCleared()
		 */
		public void modelCleared() {
			clear();	
		}
	
	}
	
	private boolean ascending;

	private int columnNumber = -1;

	private SortedTableFormat tableFormat;
	private OrderedModel sourceModel;
	
	private Listener listener = new Listener();

	/**
	 * @param newSourceModel the source model
	 * @param newFormat the format with the comparators
	 */
	public SortedModel(OrderedModel newSourceModel, SortedTableFormat newFormat) {
		super();
		sourceModel = newSourceModel; 
		tableFormat = newFormat;
		
		for (int i = 0; i < sourceModel.getItemCount(); i++) {
			addItem(sourceModel.getItemAt(i));
		}
		
		sourceModel.addOrderedModelListener(listener);
	}

	/**
	 * @param columnNumber
	 * @param ascending
	 */
	protected void sort(int newColumnNumber, boolean newAscending) {
		columnNumber = newColumnNumber;
		ascending = newAscending;
		Collections.sort(data, getComparator());		
	}
	
	/**
	 * @return
	 */
	private Comparator getComparator() {
		if (ascending) { 
			return tableFormat.getComparator(columnNumber);
		} else {
			return tableFormat.getReverseComparator(columnNumber);
		}
	}


	/**
	 * @param item
	 */
	private int getInsertionPoint(ModelItem item) {
		int position = Collections.binarySearch(data, item, getComparator());
		if (position < 0) {
			//No similar item was in the list
			position = (position + 1) * -1;
		} else {
			//There was already a similar item (or more) in the list.
			//We find out the end position of that sublist and use it as insertion point.
			position =
				Collections.lastIndexOfSubList(data, Collections.singletonList(data.get(position)));
			position++;
		}
		return position;
	}	
	
	/**
	 * @param item
	 */
	private void reinsertItem(ModelItem item) {
		removeItems(new ModelItem[] {item});	
		addItem(item, getInsertionPoint(item));
	}	
	
}
