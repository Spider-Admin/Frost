/*
 DebugRepaintManager.java / Frost
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
package frost.util.gui;

import javax.swing.*;

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
	@Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
		checkThread();
		super.addDirtyRegion(c, x, y, w, h);
	}
	/* (non-Javadoc)
	 * @see javax.swing.RepaintManager#addInvalidComponent(javax.swing.JComponent)
	 */
	@Override
    public synchronized void addInvalidComponent(JComponent invalidComponent) {
		checkThread();
		super.addInvalidComponent(invalidComponent);
	}
	private void checkThread() {
		 if (!SwingUtilities.isEventDispatchThread()) {
            System.out.println("Wrong Thread");
            Thread.dumpStack();
        }		
	}
}
