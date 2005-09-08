/*
  Board.java / Frost
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

public class Board extends DefaultMutableTreeNode implements Comparable {

	private boolean autoUpdateEnabled = true; // must apply, no default
	private String b64FileName = null;
	private String boardDescription = null;
	private String boardFileName = null;
	private String boardName = null;
	private Boolean hideBad = null;
	private Boolean hideCheck = null;
	private Boolean hideObserve = null;

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
	public Board(String name, String description) {
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
	public Board(String name, boolean isFold) {
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
	public Board(String name, String pubKey, String privKey, String description) {
		this(name, description);
		setPublicKey(pubKey);
		setPrivateKey(privKey);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof Board) { 
			Board board = (Board) o;
			if (board.isFolder() == isFolder()) {
				//If both objects are of the same kind, sort by name
				return getName().toLowerCase().compareTo(board.getName().toLowerCase());
			} else {
				//If they are of a different kind, the folder is first.
				return isFolder() ? -1 : 1;
			}
		} else {
			return 0;
		}
	}

	/**
	 * This method returns true if this board has new messages. In case
	 * this board is a folder, it recurses all folders and boards within
	 * and returns true if any of them have new messages. It returns false
	 * otherwise.
	 * @return true if there are new messages. False otherwise.
	 */
	public boolean containsNewMessages() {
		if (!isFolder) {
			// This is a board.
			if (getNewMessageCount() > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 0; i < getChildCount(); i++) {
				Board child = (Board) getChildAt(i);
				if (child.containsNewMessages()) {
					return true;
				}
			}
			return false;
		}
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
	public String getName() {
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
	public boolean getHideObserve() {
		if (!isConfigured() || hideObserve == null) {
			// return default
			return MainFrame.frostSettings.getBoolValue("hideObserveMessages");
		}
		return hideObserve.booleanValue();
	}
	
	/**
	 * @return
	 */
	public Boolean getHideObserveObj() {
		return hideObserve;
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
	 * @param name
	 */
	public void setName(String name) {
		boardName = name;
		boardFileName = Mixed.makeFilename(boardName.toLowerCase());

		if (Mixed.containsForeign(name))
			b64FileName = Core.getCrypto().encode64(name);
		else
			b64FileName = null;

		File boardFile = new File(MainFrame.keypool + boardFileName);
		if (!boardFile.exists()
			|| !boardFile.isDirectory()) //if it doesn't exist already, strip foreign chars
			boardFileName = Core.getCrypto().encode64(name);
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
	public void setHideObserve(Boolean val) {
		hideObserve = val;
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

}
