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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import frost.util.ReverseComparator;

public abstract class SortedTableFormat<ModelItemType extends ModelItem<ModelItemType>> extends AbstractTableFormat<ModelItemType> {

	private Map<Integer, Comparator<ModelItemType>> comparators;
	private Map<Integer, ReverseComparator<ModelItemType>> reverseComparators;

	protected SortedTableFormat(int newColumnCount) {
		super();
		comparators = new HashMap<Integer, Comparator<ModelItemType>>(newColumnCount);
		for(int i = 0; i < newColumnCount; i++) {
			comparators.put(i, null);
		}
		reverseComparators = new HashMap<Integer, ReverseComparator<ModelItemType>>(newColumnCount);
		for(int i = 0; i < newColumnCount; i++) {
			reverseComparators.put(i, null);
		}
	}
	
	protected SortedTableFormat() {
		super();
		comparators = new HashMap<Integer, Comparator<ModelItemType>>();
		reverseComparators = new HashMap<Integer, ReverseComparator<ModelItemType>>();
	}
	
	/**
	 * @param comparator
	 * @param columnNumber
	 */
	public void setComparator(Comparator<ModelItemType> comparator, int columnNumber) {
		comparators.put(columnNumber, comparator);
		reverseComparators.put(columnNumber, new ReverseComparator<ModelItemType>(comparator));
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public Comparator<ModelItemType> getComparator(int columnNumber) {
		return comparators.get(columnNumber);
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public ReverseComparator<ModelItemType> getReverseComparator(int columnNumber) {
		return reverseComparators.get(columnNumber);
	}

}
