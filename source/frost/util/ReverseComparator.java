/*
 * Created on May 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util;

import java.util.Comparator;

/**
 * @author Administrator
 *
 * This class implements a comparator that is the reverse of the one passed as the
 * parameter of the constructor.
 */
public class ReverseComparator implements Comparator {

	private Comparator delegate;

	/**
	 * @param newDelegate 
	 */
	public ReverseComparator(Comparator newDelegate) {
		super();
		delegate = newDelegate;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return -delegate.compare(o1, o2);
	}

}
