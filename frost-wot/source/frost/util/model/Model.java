/*
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.Collection;

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
	 * @param item
	 */
	protected void fireItemRemoved(ModelItem item) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.fireItemRemoved(item);
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
	 * Removes several items from the model
	 * @param items
	 */
	public abstract void removeItems(ModelItem[] items);
	
	/**
	 * Removes an item from the model
	 * @param item
	 */
	public abstract void removeItem(ModelItem items);  

	/**
	 *	Returns the nomber of items the model has.
	 */
	public abstract int getItemCount();

	/**
	 * Removes all items from the model
	 */
	public abstract void clear();

}
