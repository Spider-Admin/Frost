/*
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;

import java.util.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
class ModelChangeSupport {

	private Model source;
	private Vector listeners;
	private Hashtable children;

	/**
	 * Constructs a <code>ModelChangeSupport</code> object.
	 *
	 * @param sourceModel  The model to be given as the source for any events.
	 */
	public ModelChangeSupport(Model sourceModel) {
		if (sourceModel == null) {
			throw new NullPointerException();
		}
		source = sourceModel;
	}

	/**
	 * Report an item field update to any registered listeners.
	 * No event is fired if old and new are equal and non-null.
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	public void fireItemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue) {
		
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		
		Vector targets = null;
		ModelChangeSupport child = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
			if (children != null) {
				child = (ModelChangeSupport)children.get(new Integer(fieldID));
			}
		}
		
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				ModelListener target = (ModelListener) targets.elementAt(i);
				target.itemChanged(item, fieldID, oldValue, newValue);
			}
		}
		
		if (child != null) {
			child.fireItemChanged(item, fieldID, oldValue, newValue);
		}

	}

	/**
	 * @param item
	 */
	public void fireItemChanged(ModelItem item) {
		Vector targets = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
		}
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				ModelListener target = (ModelListener) targets.elementAt(i);
				target.itemChanged(item);
			}
		}
	}
	
	/**
	 * @param item
	 */
	public void fireItemAdded(ModelItem item) {
		Vector targets = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
		}
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				ModelListener target = (ModelListener) targets.elementAt(i);
				target.itemAdded(item);
			}
		}
	}
	
	/**
	 * @param items
	 */
	public void fireItemsRemoved(ModelItem[] items) {
		Vector targets = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
		}
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				ModelListener target = (ModelListener) targets.elementAt(i);
				target.itemsRemoved(items);
			}
		}
	}
	
	/**
	 * @param item
	 */
	public void fireModelCleared() {
		Vector targets = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
		}
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				ModelListener target = (ModelListener) targets.elementAt(i);
				target.modelCleared();
			}
		}
	}
	
	/**
	 * Add a ModelListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The ModelListener to be added
	 */
	public synchronized void addModelListener(ModelListener listener) {
		if (listener instanceof ModelListenerProxy) {
			ModelListenerProxy proxy = (ModelListenerProxy) listener;
			// Call two argument add method.
			addModelListener(
				proxy.getFieldID(),
				(ModelListener) proxy.getListener());
			} else {
			if (listeners == null) {
				listeners = new Vector();
			}
			listeners.addElement(listener);
		}
	}
	
	/**
	 * Add a ModelListener for a specific field.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param fieldID  The ID of the property to listen on.
	 * @param listener  The ModelListener to be added
	 */

	public synchronized void addModelListener(int fieldID, ModelListener listener) {
		if (children == null) {
			children = new java.util.Hashtable();
		}
		Integer integerFieldID = new Integer(fieldID);
		ModelChangeSupport child = (ModelChangeSupport) children.get(integerFieldID);
		if (child == null) {
			child = new ModelChangeSupport(source);
			children.put(integerFieldID, child);
		}
		child.addModelListener(listener);
	}
	
	

}
