/*
 * Created on 27-nov-2004
 * 
 */
package frost.util.gui;

import javax.swing.*;
import javax.swing.RepaintManager;

/**
 * The purpose of this class is to check for calls to Swing outside of the Swing thread. To use,
 * just instance it and install as the current repaint manager like this:
 * 
 * RepaintManager.setCurrentManager(new DebugRepaintManager());
 * 
 * Idea taken from http://www.clientjava.com/blog/2004/08/20/1093059428000.html
 * 
 * @author $author$
 * @version $revision$
 */
public class DebugRepaintManager extends RepaintManager {

	/* (non-Javadoc)
	 * @see javax.swing.RepaintManager#addDirtyRegion(javax.swing.JComponent, int, int, int, int)
	 */
	public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
		checkThread();
		super.addDirtyRegion(c, x, y, w, h);
	}
	/* (non-Javadoc)
	 * @see javax.swing.RepaintManager#addInvalidComponent(javax.swing.JComponent)
	 */
	public synchronized void addInvalidComponent(JComponent invalidComponent) {
		checkThread();
		super.addInvalidComponent(invalidComponent);
	}
	/**
	 * 
	 */
	private void checkThread() {
		 if (!SwingUtilities.isEventDispatchThread()) {
            System.out.println("Wrong Thread");
            Thread.dumpStack();
        }		
	}
}
