/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.util.Comparator;

import frost.util.model.ModelItem;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class SortedTableFormat extends AbstractTableFormat {

	private Comparator[] comparators;

	/**
	 * 
	 */
	protected SortedTableFormat(int newColumnCount) {
		super(newColumnCount);
		comparators = new Comparator[newColumnCount];
	}
	
	/**
	 * @param comparator
	 * @param columnNumber
	 */
	public void setComparator(Comparator comparator, int columnNumber) {
		comparators[columnNumber] = comparator;
	}
	
	/**
	 * @param columnNumber
	 * @return
	 */
	public Comparator getComparator(int columnNumber) {
		return comparators[columnNumber];
	}

}
