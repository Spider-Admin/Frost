/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;

import java.util.EventListenerProxy;

/**
 * This class forwards model change events to a listener delegate. Its purpose
 * is to be able to associate a certain field ID to that delegate.
 */
public class OrderedModelListenerProxy extends EventListenerProxy implements OrderedModelListener {
	
	private int fieldID;

	/**
	 * Constructor which binds the given OrderedModelListener to a specific
	 * field.
	 * 
	 * @param newListener The listener object
	 * @param newFieldID The ID of the field to listen on. 
	 */
	public OrderedModelListenerProxy(int newFieldID, OrderedModelListener newListener) {
		super(newListener);
		fieldID = newFieldID;
	}
	
	/**
	 * Returns the ID of the field associated with the listener.
	 */
	public int getFieldID() {
		return fieldID;
	}
	
	/**
	 * Forwards the field item change event to the listener delegate.
	 *
	 * @param position the position in the model of the item that has
	 * 		  changed
	 * @param item the item that has changed
	 * @param fieldID the ID of the field of the item that has changed
	 * @param oldValue the value the field had before the change
	 * @param newValue the value the field has after the change
	 */
	public void itemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue) {
		((OrderedModelListener)getListener()).itemChanged(position, item, fieldID, oldValue, newValue);	
	}

	/**
	 * Forwards the item change event to the listener delegate.
	 *
	 * @param position the position in the model of the item that has
	 * 		  changed
	 * @param item the item that has changed
	 */
	public void itemChanged(int position, ModelItem item) {
		((OrderedModelListener)getListener()).itemChanged(position, item);			
	}

	/**
	 * Forwards the item added event to the listener delegate.
	 *
	 * @param position the position in the model the new item has been
	 * 		  added into
	 * @param item the item that has been added
	 */
	public void itemAdded(int position, ModelItem item) {
		((OrderedModelListener)getListener()).itemAdded(position, item);			
	}

	/**
	 * Forwards the items removed event to the listener delegate.
	 *
	 * @param positions the positions in the model the removed items had
	 * 		  prior to their removal
	 * @param items the items that have been removed
	 */
	public void itemsRemoved(int[] positions, ModelItem[] items) {
		((OrderedModelListener)getListener()).itemsRemoved(positions, items);			
	}
	
	/**
	 * Forwards the model cleared event to the listener delegate.
	 */
	public void modelCleared() {
		((OrderedModelListener)getListener()).modelCleared();			
	}

}
