/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.util.Comparator;

import frost.util.ReverseComparator;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class SortedTableFormat extends AbstractTableFormat {

	private Comparator[] comparators;
	private Comparator[] reverseComparators;

	/**
	 * 
	 */
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
