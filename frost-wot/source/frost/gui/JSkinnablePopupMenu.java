/*
 * 
 */
package frost.gui;

import java.lang.ref.WeakReference;
import java.util.Vector;

import javax.swing.JPopupMenu;

/**
 * 
 */
public class JSkinnablePopupMenu extends JPopupMenu {

	/*
	 * We insert a weak reference into the Vector of all Frames
	 * instead of 'this' so that garbage collection can still take
	 * place correctly.
	 */
	transient private WeakReference weakThis;

	private static Vector popupList = new Vector();

	/**
	 * 
	 */
	public JSkinnablePopupMenu() {
		super();
		weakThis = new WeakReference(this);
		addToPopupList();
	}

	/**
	 * 
	 */
	private void addToPopupList() {
		synchronized (JSkinnablePopupMenu.class) {
			popupList.addElement(weakThis);
		}
	}

	/**
	 * Returns an array containing all JSkinnablePopupMenus created by 
	 * the application.
	 */
	public static JSkinnablePopupMenu[] getSkinnablePopupMenus() {
		synchronized (JSkinnablePopupMenu.class) {
			JSkinnablePopupMenu realCopy[];
			// Recall that popupList is actually a Vector of WeakReferences
			// and calling get() on one of these references may return
			// null. Make two arrays-- one the size of the Vector 
			// (fullCopy with size fullSize), and one the size of all
			// non-null get()s (realCopy with size realSize).
			int fullSize = popupList.size();
			int realSize = 0;
			JSkinnablePopupMenu fullCopy[] = new JSkinnablePopupMenu[fullSize];

			for (int i = 0; i < fullSize; i++) {
				fullCopy[realSize] =
					(JSkinnablePopupMenu) (((WeakReference) (popupList.elementAt(i))).get());

				if (fullCopy[realSize] != null) {
					realSize++;
				}
			}

			if (fullSize != realSize) {
				realCopy = new JSkinnablePopupMenu[realSize];
				System.arraycopy(fullCopy, 0, realCopy, 0, realSize);
			} else {
				realCopy = fullCopy;
			}
			return realCopy;
		}
	}

	/**
	 * @param label
	 */
	public JSkinnablePopupMenu(String label) {
		super(label);
		weakThis = new WeakReference(this);
		addToPopupList();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		removeFromPopupList();
		super.finalize();
	}

	/**
	 * 
	 */
	private void removeFromPopupList() {
		synchronized (JSkinnablePopupMenu.class) {
			popupList.removeElement(weakThis);
		}
	}

}
