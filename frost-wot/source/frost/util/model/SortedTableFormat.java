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

import frost.util.ReverseComparator;

public abstract class SortedTableFormat extends AbstractTableFormat {

	private Comparator[] comparators;
	private Comparator[] reverseComparators;

	protected SortedTableFormat(int newColumnCount) {
		super(newColumnCount);
		comparators = new Comparator[newColumnCount];
		reverseComparators = new Comparator[newColumnCount];
	}
	
	/**
	 * @param comparator
	 * @param columnNumber
	 */
	public void setComparator(Comparator comparator, int columnNumber) {
		comparators[columnNumber] = comparator;
		reverseComparators[columnNumber] = new ReverseComparator(comparator);
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public Comparator getComparator(int columnNumber) {
		return comparators[columnNumber];
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public Comparator getReverseComparator(int columnNumber) {
		return reverseComparators[columnNumber];
	}

}
