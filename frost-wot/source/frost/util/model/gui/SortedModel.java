/*
 * Created on May 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
