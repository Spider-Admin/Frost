/*
 * Created on Apr 21, 2004
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
public interface ModelListener extends EventListener {

	/**
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	void itemChanged(ModelItem item, int fieldID, Object oldValue, Object newValue);

	/**
	 * @param item
	 */
	void itemChanged(ModelItem item);

	/**
	 * @param item
	 */
	void itemAdded(ModelItem item);
	
	/**
	 * @param item
	 */
	void modelCleared();

	/**
	 * @param item
	 */
	void itemRemoved(ModelItem item);

}
