/*
 * Created on Apr 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util;

/**
 * This is an implementation of the typical Observer pattern. It differs from Sun's version in
 * the first parameter of the update method not being an Observable, but an Object.
 */
public interface FlexibleObserver {

	/**
	 * This method is called whenever the observed object is changed. An application calls an 
	 * Observable object's notifyObservers method to have all the object's 
	 * observers notified of the change.
	 * 
	 * @param o the observable object
	 * @param arg an argument passed to the notifyObservers  method
	 */
	void update(Object o, Object arg); 

}
