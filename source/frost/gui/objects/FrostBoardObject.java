/*
  FrostBoardObject.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui.objects;

import java.io.File;
import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

import frost.*;

public class FrostBoardObject extends DefaultMutableTreeNode implements Comparable {

	private boolean autoUpdateEnabled = true; // must apply, no default
	private String b64FileName = null;
	private String boardDescription = null;
	private String boardFileName = null;
	private String boardName = null;
	private Boolean hideBad = null;
	private Boolean hideCheck = null;
	private Boolean hideNA = null;

	// if isConfigured=true then below options may apply
	private boolean isConfigured = false;

	private boolean isFolder = false;

	private boolean isUpdating = false;
	private long lastUpdateStartMillis = -1; // never updated
	// following: if set to null then the default will be returned
	private Integer maxMessageDisplay = null;

	private int newMessageCount = 0;
	private int numberBlocked = 0; // number of blocked messages for this board
	private String privateKey = null;

	private String publicKey = null;
	private Boolean showSignedOnly = null;

	private boolean spammed = false;

	/**
	 * Constructs a new FrostBoardObject wich is a Board.
	 */
	public FrostBoardObject(String name, String description) {
		super();
		boardName = name;
		boardDescription = description;
		boardFileName = Mixed.makeFilename(boardName.toLowerCase());

		if (Mixed.containsForeign(name)) {
			b64FileName = Core.getCrypto().encode64(name);
		}
	}
	/**
	 * Constructs a new FrostBoardObject.
	 * @param name
	 * @param isFold if true, this will be a folder, else a board.
	 */
	public FrostBoardObject(String name, boolean isFold) {
		this(name, null);
		isFolder = isFold;
	}
	/**
	 * Constructs a new FrostBoardObject wich is a Board.
	 * @param name
	 * @param pubKey
	 * @param privKey
	 * @param description the description of the board, or null if none.
	 */
	public FrostBoardObject(String name, String pubKey, String privKey, String description) {
		this(name, description);
		setPublicKey(pubKey);
		setPrivateKey(privKey);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof FrostBoardObject) { 
			FrostBoardObject board = (FrostBoardObject) o;
			if (board.isFolder() == isFolder()) {
				//If both objects are of the same kind, sort by name
				return toString().toLowerCase().compareTo(o.toString().toLowerCase());
			} else {
				//If they are of a different kind, the folder is first.
				return isFolder() ? -1 : 1;
			}
		} else {
			return 0;
		}
	}

	/**
	 * @return
	 */
	public boolean containsFolderNewMessages() {
		FrostBoardObject board = this;
		int childs = getChildCount(); 
		boolean newMessage = false;

		for (int c = 0; c < childs; c++) {
			FrostBoardObject childBoard = (FrostBoardObject) board.getChildAt(c);
			if ((childBoard.isFolder() && childBoard.containsFolderNewMessages())
				|| childBoard.containsNewMessage()) {
				newMessage = true;
				break;
			}
		}
		return newMessage;
	}

	/**
	 * @return
	 */
	public boolean containsNewMessage() {
		if (getNewMessageCount() > 0)
			return true; 
		return false;
	}
	
	/**
	 * 
	 */
	public void decNewMessageCount() { 
		newMessageCount--;
	}

	/**
	 * @return
	 */
	public boolean getAutoUpdateEnabled() {
		if (!isConfigured())
			return true;
		return autoUpdateEnabled;
	}

	/**
	 * @return
	 */
	public int getBlockedCount() {
		return numberBlocked;
	}
	
	/**
	 * @return
	 */
	public String getBoardFilename() {
		return b64FileName == null ? boardFileName : b64FileName;
	}

	/**
	 * @return
	 */
	public String getBoardName() {
		return boardName;
	}
	

	/**
	 * @return
	 */
	public String getDescription() {
		return boardDescription;
	}

	/**
	 * @return
	 */
	public boolean getHideBad() {
		if (!isConfigured() || hideBad == null) {
			// return default
			return MainFrame.frostSettings.getBoolValue("hideBadMessages");
		}
		return hideBad.booleanValue();
	}
	
	/**
	 * @return
	 */
	public Boolean getHideBadObj() {
		return hideBad;
	}

	/**
	 * @return
	 */
	public boolean getHideCheck() {
		if (!isConfigured() || hideCheck == null) {
			// return default
			return MainFrame.frostSettings.getBoolValue("hideCheckMessages");
		}
		return hideCheck.booleanValue();
	}
	
	/**
	 * @return
	 */
	public Boolean getHideCheckObj() {
		return hideCheck;
	}

	/**
	 * @return
	 */
	public boolean getHideNA() {
		if (!isConfigured() || hideNA == null) {
			// return default
			return MainFrame.frostSettings.getBoolValue("hideNAMessages");
		}
		return hideNA.booleanValue();
	}
	
	/**
	 * @return
	 */
	public Boolean getHideNAObj() {
		return hideNA;
	}

	/**
	 * @return
	 */
	public long getLastUpdateStartMillis() {
		return lastUpdateStartMillis;
	}

	/**
	 * @return
	 */
	public int getMaxMessageDisplay() {
		if (!isConfigured() || maxMessageDisplay == null) {
			// return default
			return MainFrame.frostSettings.getIntValue("maxMessageDisplay");
		}
		return maxMessageDisplay.intValue();
	}
	/**
	 * @return
	 */
	public Integer getMaxMessageDisplayObj() {
		return maxMessageDisplay;
	}

	/**
	 * @return
	 */
	public int getNewMessageCount() {
		return newMessageCount;
	}
	
	/**
	 * @return
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * @return
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @return
	 */
	public boolean getShowSignedOnly() {
		if (!isConfigured() || showSignedOnly == null) {
			// return default
			return MainFrame.frostSettings.getBoolValue("signedOnly");
		}
		return showSignedOnly.booleanValue();
	}
	/**
	 * @return
	 */
	public Boolean getShowSignedOnlyObj() {
		return showSignedOnly;
	}

	/**
	 * @return
	 */
	public String getStateString() {
		// TODO: translate
		if (isReadAccessBoard()) {
			return "read access";
		} else if (isWriteAccessBoard()) {
			return "write access";
		} else if (isPublicBoard()) {
			return "public board";
		}
		return "*ERROR*";
	}

	/**
	 * Returns the String that is shown in tree.
	 * Appends new message count in brackets behind board name.
	 */
	public String getVisibleText() {
		if (getNewMessageCount() == 0) {
			return toString();
		}

		StringBuffer sb = new StringBuffer();
		sb.append(toString()).append(" (").append(getNewMessageCount()).append(")");

		return sb.toString();
	}

	/**
	 * 
	 */
	//////////////////////////////////////////////
	// From BoardStats

	public void incBlocked() {
		numberBlocked++;
	}
	/**
	 * 
	 */
	public void incNewMessageCount() {
		newMessageCount++;
	}

	/**
	 * @return
	 */
	public boolean isConfigured() {
		return isConfigured;
	}

	/**
	 * @return
	 */
	public boolean isFolder() {
		return isFolder;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		return (isFolder() == false);
	}
	
	/**
	 * @return
	 */
	public boolean isPublicBoard() {
		if (publicKey == null && privateKey == null)
			return true;
		return false;
	}
	
	/**
	 * @return
	 */
	public boolean isReadAccessBoard() {
		if (publicKey != null && privateKey == null)
			return true;
		return false;
	}

	/**
	 * @return
	 */
	public boolean isSpammed() {
		return spammed;
	}

	/**
	 * @return
	 */
	public boolean isUpdating() {
		return isUpdating;
	}

	/**
	 * @return
	 */
	public boolean isWriteAccessBoard() {
		if (publicKey != null && privateKey != null)
			return true;
		return false;
	}

	/**
	 * 
	 */
	public void resetBlocked() {
		numberBlocked = 0;
	}
	
	/**
	 * @param val
	 */
	public void setAutoUpdateEnabled(boolean val) {
		autoUpdateEnabled = val;
	}

	/**
	 * @param newname
	 */
	public void setBoardName(String newname) {
		boardName = newname;
		boardFileName = Mixed.makeFilename(boardName.toLowerCase());

		if (Mixed.containsForeign(newname))
			b64FileName = Core.getCrypto().encode64(newname);
		else
			b64FileName = null;

		File boardFile = new File(MainFrame.keypool + boardFileName);
		if (!boardFile.exists()
			|| !boardFile.isDirectory()) //if it doesn't exist already, strip foreign chars
			boardFileName = Core.getCrypto().encode64(newname);
	}
	
	/**
	 * @param val
	 */
	public void setConfigured(boolean val) {
		isConfigured = val;
	}
	
	/**
	 * @param val
	 */
	public void setHideBad(Boolean val) {
		hideBad = val;
	}
	
	/**
	 * @param val
	 */
	public void setHideCheck(Boolean val) {
		hideCheck = val;
	}
	
	/**
	 * @param val
	 */
	public void setHideNA(Boolean val) {
		hideNA = val;
	}

	/**
	 * @param millis
	 */
	public void setLastUpdateStartMillis(long millis) {
		lastUpdateStartMillis = millis;
	}
	
	/**
	 * @param val
	 */
	public void setMaxMessageDays(Integer val) {
		maxMessageDisplay = val;
	}
	
	/**
	 * @param val
	 */
	public void setNewMessageCount(int val) {
		newMessageCount = val;
	}
	
	/**
	 * @param val
	 */
	public void setPrivateKey(String val) {
		if (val != null)
			val = val.trim();
		privateKey = val;
	}
	
	/**
	 * @param val
	 */
	public void setPublicKey(String val) {
		if (val != null)
			val = val.trim();
		publicKey = val;
	}
	
	/**
	 * @param val
	 */
	public void setShowSignedOnly(Boolean val) {
		showSignedOnly = val;
	}

	/**
	 * @param val
	 */
	public void setSpammed(boolean val) {
		spammed = val;
	}
	
	/**
	 * @param val
	 */
	public void setUpdating(boolean val) {
		isUpdating = val;
	}

	/**
	 * 
	 */
	public void sortChildren() {
		Collections.sort(children);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return boardName;
	}

}
