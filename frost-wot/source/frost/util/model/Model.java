/*
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Model {

	private ModelChangeSupport changeSupport;

	/**
	 * 
	 */
	public Model() {
		super();
	}

	/**
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
		fireItemChanged(item, fieldID, oldValue, newValue);
	}
	
	/**
	 * @param item
	 */
	void itemChanged(ModelItem item) {
		fireItemChanged(item);
	}
	
	/**
	 * @param item
	 */
	protected void fireItemAdded(ModelItem item) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemAdded(item);
	}
	
	/**
	 * @param item
	 */
	protected void fireModelCleared() {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireModelCleared();
	}
	
	/**
	 * @param items
	 */
	protected void fireItemsRemoved(ModelItem[] items) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemsRemoved(items);
	}
	
	/**
	 * @param item
	 */
	protected void fireItemChanged(ModelItem item) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemChanged(item);
	}
	
	/**
	 * @param item
	 */
	protected void fireItemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemChanged(item, fieldID, oldValue, newValue);
	}
	
	/**
	 * Adds a ModelListener to the listener list. 
	 * <p>
	 * If listener is null, no exception is thrown and no action is performed.
	 *
	 * @param    listener  the ModelListener to be added
	 */
	public synchronized void addModelListener(ModelListener listener) {
		if (listener == null) {
			return;
		}
		if (changeSupport == null) {
			changeSupport = new ModelChangeSupport(this);
		}
		changeSupport.addModelListener(listener);
	}
	
	/**
	 * Adds a ModelListener to the listener list for a specific
	 * field. 
	 * <p>
	 * If listener is null, no exception is thrown and no action is performed.
	 *
	 * @param fieldID the ID of the field
	 * @param listener the ModelChangeListener to be added
	 */
	public synchronized void addModelListener(int fieldID, ModelListener listener) {
		if (listener == null) {
			return;
		}
		if (changeSupport == null) {
			changeSupport = new ModelChangeSupport(this);
		}
		changeSupport.addModelListener(fieldID, listener);
	}
	
	/**
	 * Adds an item to the model
	 * @param item
	 */
	protected abstract void addItem(ModelItem item);
	
	/**
	 * This method removes all of the ModelItems in the given array
	 * from the model. If a ModelItem is not in the model, its link
	 * to its model is cleared, but nothing else is done with it.
	 * If a ModelItem appears more than once in the model, only the
	 * first occurrence is removed from it.
	 * @param items array of items to remove from the model.
	 * @return true if at least one item was removed from the model. False otherwise.
	 */
	public abstract boolean removeItems(ModelItem[] items);
	
	/**
	 *	Returns the number of items the model has.
	 */
	public abstract int getItemCount();

	/**
	 * Removes all items from the model
	 */
	public abstract void clear();

}
