/**
 * A class which extends the <code>EventListenerProxy</code> specifically 
 * for adding a <code>ModelChangeListener</code> that may be associated with
 * a certain field ID. Instances of this class can be added as 
 * <code>ModelChangeListener</code> to a model. 
 * <p>
 * If the model has a <code>getModelChangeListeners()</code>
 * method then the array returned could be a mixture of 
 * <code>ModelChangeListener</code> and
 * <code>ModelChangeListenerProxy</code> objects.
 * 
 * @see java.util.EventListenerProxy
 */
package frost.util.model;

import java.util.EventListenerProxy;

/**
 * This class forwards model change events to a listener delegate. Its purpose
 * is to be able to associate a certain field ID to that delegate.
 */
class ModelListenerProxy extends EventListenerProxy implements ModelListener {

	private int fieldID;

	/**
	 * Constructor which binds the given ModelListener to a specific
	 * field.
	 * 
	 * @param newListener The listener object
	 * @param newFieldID The ID of the field to listen on. 
	 */
	public ModelListenerProxy(int newFieldID, ModelListener newListener) {
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
	 * @param item the item that has changed
	 * @param fieldID the ID of the field of the item that has changed
	 * @param oldValue the value the field had before the change
	 * @param newValue the value the field has after the change
	 */
	public void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
		((ModelListener)getListener()).itemChanged(item, fieldID, oldValue, newValue);
	}
	
	/**
	 * Forwards the item change event to the listener delegate.
	 *
	 * @param item the model item that has changed
	 */
	public void itemChanged(ModelItem item) {
		((ModelListener)getListener()).itemChanged(item);		
	}
	
	/**
	 * Forwards the item added event to the listener delegate.
	 *
	 * @param evt the model item that has been added
	 */
	public void itemAdded(ModelItem item) {
		((ModelListener)getListener()).itemAdded(item);		
	}
	
	/**
	 * Forwards the items removed event to the listener delegate.
	 *
	 * @param items the model items that have been removed
	 */
	public void itemsRemoved(ModelItem[] items) {
		((ModelListener)getListener()).itemsRemoved(items);		
	}

	/* (non-Javadoc)
	 * @see frost.util.model.ModelListener#modelCleared()
	 */
	public void modelCleared() {
		((ModelListener)getListener()).modelCleared();			
	}

}
