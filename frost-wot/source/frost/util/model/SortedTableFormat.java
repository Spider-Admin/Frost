/*
 SortedTableFormat.java / Frost
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

import frost.util.*;

public abstract class SortedTableFormat<T extends ModelItem> extends AbstractTableFormat<T> {

	private List<Comparator<T>> comparators;
	private List<ReverseComparator<T>> reverseComparators;

	protected SortedTableFormat(int newColumnCount) {
		super(newColumnCount);
		comparators = new ArrayList<Comparator<T>>(newColumnCount);
		for(int i = 0; i < newColumnCount; i++) {
			comparators.add(null);
		}
		reverseComparators = new ArrayList<ReverseComparator<T>>(newColumnCount);
		for(int i = 0; i < newColumnCount; i++) {
			reverseComparators.add(null);
		}
	}
	
	/**
	 * @param comparator
	 * @param columnNumber
	 */
	public void setComparator(Comparator<T> comparator, int columnNumber) {
		comparators.set(columnNumber, comparator);
		reverseComparators.set(columnNumber, new ReverseComparator<T>(comparator));
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public Comparator<T> getComparator(int columnNumber) {
		return comparators.get(columnNumber);
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public ReverseComparator<T> getReverseComparator(int columnNumber) {
		return reverseComparators.get(columnNumber);
	}

}
