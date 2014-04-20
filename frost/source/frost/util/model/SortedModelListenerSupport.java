/*
 SortedModelChangeSupport.java / Frost
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
package frost.util.model;

import java.util.*;

/**
 * This class provides support for sending change events of models 
 * that are ordered. 
 */
public class SortedModelListenerSupport<T extends ModelItem<T>> {

	private Vector<SortedModelListener<T>> listeners;

	/**
	 * @param item
	 */
	public void fireItemChanged(T item, int position) {
        if( listeners == null ) {
            return;
        }
        synchronized (listeners) {
    		for (int i = 0; i < listeners.size(); i++) {
    			SortedModelListener<T> target = listeners.elementAt(i);
    			target.itemChanged(position, item);
    		}
        }
	}
	
	/**
	 * @param item
	 */
	public void fireItemAdded(T item, int position) {
        if( listeners == null ) {
            return;
        }
		
		synchronized(listeners) {
			for (int i = 0; i < listeners.size(); i++) {
				SortedModelListener<T> target = listeners.elementAt(i);
				target.itemAdded(position, item);
			}
		}
	}
	
	/**
	 * @param items
	 */
	public void fireItemsRemoved(int[] positions, ArrayList<T> items) {
        if( listeners == null ) {
            return;
        }
        synchronized(listeners) {
			for (int i = 0; i < listeners.size(); i++) {
				
				SortedModelListener<T> target = listeners.elementAt(i);
				
				target.itemsRemoved(positions, items);
			}
		}
	}
	
	/**
	 * @param item
	 */
	public void fireModelCleared() {
        if( listeners == null ) {
            return;
        }
        synchronized(listeners) {
			for (int i = 0; i < listeners.size(); i++) {
				SortedModelListener<T> target = listeners.elementAt(i);
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
	public synchronized void addModelListener(SortedModelListener<T> listener) {
		if (listeners == null) {
			listeners = new Vector<SortedModelListener<T>>();
		}
		listeners.addElement(listener);
	}
}
