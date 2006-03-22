/*
 FlexibleObserver.java / Frost
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
