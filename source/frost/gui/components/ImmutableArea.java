/*
 * Created on Jan 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.components;

import javax.swing.text.*;
import javax.swing.text.Position;


/**
 * 
 */
public class ImmutableArea {

	private boolean enabled = true;
	
	private Position startPosition = null;
	private Position endPosition = null;
	private Document document = null;

	/**
	 * 
	 */
	public ImmutableArea(Document newDocument) {
		super();
		document = newDocument;
	}	

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param b
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}

	/**
	 * @return
	 */
	public int getEndPos() {
		return endPosition.getOffset() + 1;
	}


	/**
	 * @return
	 */
	public int getStartPos() {
		return startPosition.getOffset() - 1;
	}

	/**
	 * @param i
	 */
	public void setEndPos(int pos) throws IllegalArgumentException {
		try {
			endPosition = document.createPosition(pos - 1);
		} catch (Exception exception) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @param i
	 */
	public void setStartPos(int pos) throws IllegalArgumentException {
		try {
			// + 1, because a Position in pos 0 doesn't keep track of inserts before it. 
			startPosition = document.createPosition(pos + 1);	
		} catch (Exception exception) {
			throw new IllegalArgumentException();
		}
	}

}