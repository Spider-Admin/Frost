/*
 * Created on May 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model;

import java.util.EventListener;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface OrderedModelListener extends EventListener {

	/**
	 * @param position
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	void itemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue);

	/**
	 * @param position
	 * @param item 
	 */
	void itemChanged(int position, ModelItem item);

	/**
	 * @param position
	 * @param item
	 */
	void itemAdded(int position, ModelItem item);

	/**
	 * @param position
	 * @param item
	 */
	void itemRemoved(int position, ModelItem item);
	
	/**
	 * @param item
	 */
	void modelCleared();

}
