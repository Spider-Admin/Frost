/*
 ImmutableArea.java / Frost
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

import javax.swing.text.*;
import javax.swing.text.Position;

public class ImmutableArea {

	private boolean enabled = true;
	
	private Position startPosition = null;
	private Position endPosition = null;
	private Document document = null;

	public ImmutableArea(Document newDocument) {
		super();
		document = newDocument;
	}	

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		enabled = b;
	}

	public int getEndPos() {
		return endPosition.getOffset() + 1;
	}

	public int getStartPos() {
		return startPosition.getOffset() - 1;
	}

	public void setEndPos(int pos) throws IllegalArgumentException {
		try {
			endPosition = document.createPosition(pos - 1);
		} catch (Exception exception) {
			throw new IllegalArgumentException();
		}
	}

	public void setStartPos(int pos) throws IllegalArgumentException {
		try {
			// + 1, because a Position in pos 0 doesn't keep track of inserts before it. 
			startPosition = document.createPosition(pos + 1);	
		} catch (Exception exception) {
			throw new IllegalArgumentException();
		}
	}
}