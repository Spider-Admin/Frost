/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;

import java.util.*;

/**
 * This class provides support for sending change events of models 
 * that are ordered. 
 */
public class OrderedModelChangeSupport {

	private OrderedModel source;
	private Vector listeners;
	private Hashtable children;

	/**
	 * Constructs an <code>OrderedModelChangeSupport</code> object.
	 *
	 * @param sourceModel  The model to be given as the source for any events.
	 */
	public OrderedModelChangeSupport(OrderedModel sourceModel) {
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
		OrderedModelChangeSupport child = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
			if (children != null) {
				child = (OrderedModelChangeSupport) children.get(new Integer(fieldID));
			}
		}

		int position = source.indexOf(item);

		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.itemChanged(position, item, fieldID, oldValue, newValue);
			}
		}

		if (child != null) {
			child.fireItemChanged(position, item, fieldID, oldValue, newValue);
		}
	}
	
	/**
	 * Report an item field update to any registered listeners.
	 * No event is fired if old and new are equal and non-null.
	 * @param position
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	private void fireItemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue) {
		
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		
		Vector targets = null;
		OrderedModelChangeSupport child = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
			if (children != null) {
				child = (OrderedModelChangeSupport) children.get(new Integer(fieldID));
			}
		}
		
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.itemChanged(position, item, fieldID, oldValue, newValue);
			}
		}
			
		if (child != null) {
			child.fireItemChanged(position, item, fieldID, oldValue, newValue);
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
		
		int position = source.indexOf(item);
		
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.itemChanged(position, item);
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
		
		int position = source.indexOf(item);
		
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.itemAdded(position, item);
			}
		}
	}
	
	/**
	 * @param items
	 */
	public void fireItemsRemoved(int[] positions, ModelItem[] items) {
		Vector targets = null;
		synchronized (this) {
			if (listeners != null) {
				targets = (Vector) listeners.clone();
			}
		}
		
		if (targets != null) {
			for (int i = 0; i < targets.size(); i++) {
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.itemsRemoved(positions, items);
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
				OrderedModelListener target = (OrderedModelListener) targets.elementAt(i);
				target.modelCleared();
			}
		}
	}

	/**
	 * Add an OrderedModelListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The OrderedModelListener to be added
	 */
	public synchronized void addModelListener(OrderedModelListener listener) {
		if (listener instanceof ModelListenerProxy) {
			OrderedModelListenerProxy proxy = (OrderedModelListenerProxy) listener;
			// Call two argument add method.
			addModelListener(proxy.getFieldID(), (OrderedModelListener) proxy.getListener());
		} else {
			if (listeners == null) {
				listeners = new Vector();
			}
			listeners.addElement(listener);
		}
	}

	/**
	 * Add an OrderedModelListener for a specific field.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param fieldID  The ID of the property to listen on.
	 * @param listener  The OrderedModelListener to be added
	 */

	public synchronized void addModelListener(int fieldID, OrderedModelListener listener) {
		if (children == null) {
			children = new java.util.Hashtable();
		}
		Integer integerFieldID = new Integer(fieldID);
		OrderedModelChangeSupport child = (OrderedModelChangeSupport) children.get(integerFieldID);
		if (child == null) {
			child = new OrderedModelChangeSupport(source);
			children.put(integerFieldID, child);
		}
		child.addModelListener(listener);
	}

}
