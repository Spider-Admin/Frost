/*
 FlexibleObservable.java / Frost
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
package frost.util;

import java.util.Vector;

/**
 * This is an implementation of the typical Observer pattern.  It differs from Sun's version in
 * the observers attribute being protected instead of private.
 */
public class FlexibleObservable {

	private boolean changed = false;
	protected Vector<FlexibleObserver> observers;

	/**
	 * Construct an Observable with zero Observers.
	 */
	public FlexibleObservable() {
		super();
		observers = new Vector<FlexibleObserver>();
	}

	/**
	 * Adds an observer to the set of observers for this object, provided 
	 * that it is not the same as some observer already in the set. 
	 * The order in which notifications will be delivered to multiple 
	 * observers is not specified. See the class comment.
	 *
	 * @param   o   an observer to be added.
	 * @throws NullPointerException   if the parameter o is null.
	 */
	public synchronized void addObserver(FlexibleObserver o) {
		if (o == null)
			throw new NullPointerException();
		if (!observers.contains(o)) {
			observers.addElement(o);
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object. 
	 *
	 * @param   o   the observer to be deleted.
	 */
	public synchronized void deleteObserver(FlexibleObserver o) {
		observers.removeElement(o);
	}

	/**
	 * If this object has changed, as indicated by the 
	 * <code>hasChanged</code> method, then notify all of its observers 
	 * and then call the <code>clearChanged</code> method to 
	 * indicate that this object has no longer changed. 
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and <code>null</code>. In other 
	 * words, this method is equivalent to:
	 * <blockquote><tt>
	 * notifyObservers(null)</tt></blockquote>
	 *
	 * @see     frost.util.FlexibleObservable#clearChanged()
	 * @see     frost.util.FlexibleObservable#hasChanged()
	 * @see     frost.util.FlexibleObserver#update(frost.util.FlexibleObservable, java.lang.Object)
	 */
	public void notifyObservers() {
		notifyObservers(null);
	}

	/**
	 * If this object has changed, as indicated by the 
	 * <code>hasChanged</code> method, then notify all of its observers 
	 * and then call the <code>clearChanged</code> method to indicate 
	 * that this object has no longer changed. 
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and the <code>arg</code> argument.
	 *
	 * @param   arg   any object.
	 * @see     frost.util.FlexibleObservable#clearChanged()
	 * @see     frost.util.FlexibleObservable#hasChanged()
	 * @see     frost.util.FlexibleObserver#update(frost.util.FlexibleObservable, java.lang.Object)
	 */
	public void notifyObservers(Object arg) {
		/*
		 * a temporary array buffer, used as a snapshot of the state of
		 * current Observers.
		 */
		Object[] arrLocal;

		synchronized (this) {
			/* We don't want the Observer doing callbacks into
		 	 * arbitrary code while holding its own Monitor.
	 		 * The code where we extract each Observable from 
			 * the Vector and store the state of the Observer
			 * needs synchronization, but notifying observers
			 * does not (should not).  The worst result of any 
			 * potential race-condition here is that:
			 * 1) a newly-added Observer will miss a
			 *   notification in progress
			 * 2) a recently unregistered Observer will be
			 *   wrongly notified when it doesn't care
			 */
			if (!changed)
				return;
			arrLocal = observers.toArray();
			clearChanged();
		}

		for (int i = arrLocal.length - 1; i >= 0; i--)
			 ((FlexibleObserver) arrLocal[i]).update(this, arg);
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 */
	public synchronized void deleteObservers() {
		observers.removeAllElements();
	}

	/**
	 * Marks this <tt>FlexibleObservable</tt> object as having been changed; the 
	 * <tt>hasChanged</tt> method will now return <tt>true</tt>.
	 */
	protected synchronized void setChanged() {
		changed = true;
	}

	/**
	 * Indicates that this object has no longer changed, or that it has 
	 * already notified all of its observers of its most recent change, 
	 * so that the <tt>hasChanged</tt> method will now return <tt>false</tt>. 
	 * This method is called automatically by the 
	 * <code>notifyObservers</code> methods. 
	 *
	 * @see     frost.util.FlexibleObservable#notifyObservers()
	 * @see     frost.util.FlexibleObservable#notifyObservers(java.lang.Object)
	 */
	protected synchronized void clearChanged() {
		changed = false;
	}

	/**
	 * Tests if this object has changed. 
	 *
	 * @return  <code>true</code> if and only if the <code>setChanged</code> 
	 *          method has been called more recently than the 
	 *          <code>clearChanged</code> method on this object; 
	 *          <code>false</code> otherwise.
	 * @see     frost.util.FlexibleObservable#clearChanged()
	 * @see     frost.util.FlexibleObservable#setChanged()
	 */
	public synchronized boolean hasChanged() {
		return changed;
	}

	/**
	 * Returns the number of observers of this <tt>FlexibleObservable</tt> object.
	 *
	 * @return  the number of observers of this object.
	 */
	public synchronized int countObservers() {
		return observers.size();
	}

}
