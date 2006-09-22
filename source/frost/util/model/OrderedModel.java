/*
 OrderedModel.java / Frost
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
package frost.util.model;

import java.util.*;

/**
 * This class is a Model that stores ModelItems in a certain order. That does not
 * mean that it is sorted.
 * 
 * Its implementation is thread-safe
 */
public class OrderedModel extends Model {

	protected List data;
	
	private OrderedModelChangeSupport changeSupport;

	public OrderedModel() {
		super();
		data = new ArrayList();
	}
	
	/* (non-Javadoc)
	 * @see frost.util.Model#addItem(frost.util.ModelItem)
	 */
	protected void addItem(ModelItem item) {
		synchronized(data) {
			data.add(item);
			fireItemAdded(item);
		}
		item.setModel(this);
	}
	
	protected void addItem(ModelItem item, int position) {
		synchronized(data) {
			data.add(position, item);
			fireItemAdded(item);
		}
		item.setModel(this);
	}
	
	/**
	 * Adds an OrderedModelListener to the listener list for a specific
	 * field. 
	 * <p>
	 * If listener is null, no exception is thrown and no action is performed.
	 *
	 * @param fieldID the ID of the field
	 * @param listener the OrderedModelChangeListener to be added
	 */
	public synchronized void addOrderedModelListener(int fieldID, OrderedModelListener listener) {
		if (listener == null) {
			return;
		}
		if (changeSupport == null) {
			changeSupport = new OrderedModelChangeSupport(this);
		}
		changeSupport.addModelListener(fieldID, listener);
	}
	


	/**
	 * Adds an OrderedModelListener to the listener list. 
	 * <p>
	 * If listener is null, no exception is thrown and no action is performed.
	 *
	 * @param    listener  the OrderedModelListener to be added
	 */
	public synchronized void addOrderedModelListener(OrderedModelListener listener) {
		if (listener == null) {
			return;
		}
		if (changeSupport == null) {
			changeSupport = new OrderedModelChangeSupport(this);
		}
		changeSupport.addModelListener(listener);
	}

	/* (non-Javadoc)
	 * @see frost.util.model.Model#clear()
	 */
	public synchronized void clear() {
		synchronized (data) {
			Iterator iterator = data.iterator();
			while (iterator.hasNext()) {
				ModelItem item = (ModelItem) iterator.next();
				item.setModel(null);
			}
			data.clear();
			fireModelCleared();
		}
	}
	

	/**
	 * @param item
	 */
	protected void fireItemAdded(ModelItem item) {
		super.fireItemAdded(item);
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemAdded(item);
	}
	



	/**
	 * @param item
	 */
	protected void fireItemChanged(ModelItem item) {
		super.fireItemChanged(item);
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemChanged(item);
	}
	



	/**
	 * @param item
	 */
	protected void fireItemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
		super.fireItemChanged(item, fieldID, oldValue, newValue);
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemChanged(item, fieldID, oldValue, newValue);
	}
	

	/**
	 * @param positions
	 * @param items
	 */
	private void fireItemsRemoved(int[] positions, ModelItem[] items) {
		super.fireItemsRemoved(items);
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemsRemoved(positions, items);		
	}



	/**
	 * @param item
	 */
	protected void fireModelCleared() {
		super.fireModelCleared();
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireModelCleared();
	}

	/**
	 * @param position
	 * @return
	 */
	public ModelItem getItemAt(int position) {
		return (ModelItem) data.get(position);
	}

	/* (non-Javadoc)
	 * @see frost.util.model.Model#getItemCount()
	 */
	public int getItemCount() {
		return data.size();
	}
    
    /**
     * @return  a new List containing all items of this model, in unspecific order
     */
    public List getItems() {
        return new ArrayList(data);
    }
	
	/**
	 * Returns the index in this model of the first occurrence of the specified
     * item, or -1 if this model does not contain this element.
     * 
     * @param item item to search for.
	 * @return the index in this model of the first occurrence of the specified
     * 	       item, or -1 if this model does not contain this element.
	 */
	public int indexOf(ModelItem item) {
		return data.indexOf(item);
	}

	/* (non-Javadoc)
	 * @see frost.util.Model#removeItems(frost.util.ModelItem)
	 */
	public boolean removeItems(ModelItem[] items) {
		//We clear the link to the model of each item
		for (int i = 0; i < items.length; i++) {
			items[i].setModel(null);
		}
		//We remove the first occurrence of each item from the model
		int[] removedPositions = new int[items.length];
		ModelItem[] removedItems = new ModelItem[items.length];
		int count = 0;
		synchronized (data) {
			for (int i = 0; i < items.length; i++) {
				int position = data.indexOf(items[i]);
				if (position != -1) {
					data.remove(position);
					removedItems[count] = items[i];
					removedPositions[count] = position;
					count++;
				}
			}
		}
		//We send an items removed event. Only those items that actually
		//were in the model and thus were removed are included in the event.
		if (count != 0) {
			int[] croppedPositions = new int[count];
			ModelItem[] croppedItems = new ModelItem[count];
			System.arraycopy(removedPositions, 0, croppedPositions, 0, count);
			System.arraycopy(removedItems, 0, croppedItems, 0, count);
			fireItemsRemoved(croppedPositions, croppedItems);
			return true;
		} else {
			return false;	
		}
	}
}
