/*
  Board.java / Frost
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
package frost.boards;

import java.util.*;

import frost.*;
import frost.storage.perst.messages.*;
import frost.util.*;
import frost.util.gui.translation.*;

/**
 * Represents a board in the board tree.
 */
public class Board extends AbstractNode {

    private static Language language = Language.getInstance();

    private PerstFrostBoardObject perstFrostBoardObject = null;

    private boolean autoUpdateEnabled = true; // must apply, no default
    private String boardDescription = null;
    private final String boardFileName;
    private Boolean hideBad = null;
    private Boolean hideCheck = null;
    private Boolean hideObserve = null;

    private Boolean storeSentMessages = null;

    // if isConfigured=true then below options may apply
    private boolean isConfigured = false;

    private boolean isUpdating = false;
    private long lastUpdateStartMillis = -1; // never updated
    private long lastBackloadUpdateFinishedMillis = -1; // never finished
    // following: if set to null then the default will be returned
    private Integer maxMessageDisplay = null;
    private Integer maxMessageDownload = null;

    private int newMessageCount = 0;
    private int numberBlocked = 0; // number of blocked messages for this board
    private String privateKey = null;

    private String publicKey = null;
    private Boolean showSignedOnly = null;

    private boolean spammed = false;

    private int timesUpdatedCount = 0;

    private boolean hasFlaggedMessages = false;
    private boolean hasStarredMessages = false;

    // String is the dirdate used in MessageThread, a unique String per day
    private final Hashtable<String,BoardUpdateInformation> boardUpdateInformations = new Hashtable<String,BoardUpdateInformation>();

    private static final BoardUpdateInformationComparator boardUpdateInformationComparator = new BoardUpdateInformationComparator();

    /**
     * Constructs a new Board
     */
    public Board(final String name, final String description) {
        this(name, null, null, description);
    }

    /**
     * Constructs a new FrostBoardObject wich is a Board.
     * @param name
     * @param pubKey
     * @param privKey
     * @param description the description of the board, or null if none.
     */
    public Board(final String name, final String pubKey, final String privKey, final String description) {
        super(name);
        boardDescription = description;
        boardFileName = Mixed.makeFilename(getNameLowerCase());
        setPublicKey(pubKey);
        setPrivateKey(privKey);
    }

    /**
     * This method returns true if this board has new messages. In case
     * this board is a folder, it recurses all folders and boards within
     * and returns true if any of them have new messages. It returns false
     * otherwise.
     * @return true if there are new messages. False otherwise.
     */
    @Override
    public boolean containsNewMessages() {
        if (getNewMessageCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void decNewMessageCount() {
        newMessageCount--;
    }

    public void incTimesUpdatedCount() {
        timesUpdatedCount++;
    }

    public boolean getAutoUpdateEnabled() {
        if (!isConfigured()) {
            return true;
        }
        return autoUpdateEnabled;
    }

    public int getBlockedCount() {
        return numberBlocked;
    }

    public String getBoardFilename() {
        return boardFileName;
    }

    public String getDescription() {
        return boardDescription;
    }

    public void setDescription(final String desc) {
        boardDescription = desc;
    }

    public boolean getStoreSentMessages() {
        if (!isConfigured() || storeSentMessages == null) {
            // return default
            return Core.frostSettings.getBoolValue(SettingsClass.STORAGE_STORE_SENT_MESSAGES);
        }
        return storeSentMessages.booleanValue();
    }

    public Boolean getStoreSentMessagesObj() {
        return storeSentMessages;
    }

    public boolean getHideBad() {
        if (!isConfigured() || hideBad == null) {
            // return default
            return Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_HIDE_BAD);
        }
        return hideBad.booleanValue();
    }

    public Boolean getHideBadObj() {
        return hideBad;
    }

    public boolean getHideCheck() {
        if (!isConfigured() || hideCheck == null) {
            // return default
            return Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_HIDE_CHECK);
        }
        return hideCheck.booleanValue();
    }

    public Boolean getHideCheckObj() {
        return hideCheck;
    }

    public boolean getHideObserve() {
        if (!isConfigured() || hideObserve == null) {
            // return default
            return Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_HIDE_OBSERVE);
        }
        return hideObserve.booleanValue();
    }

    public Boolean getHideObserveObj() {
        return hideObserve;
    }

    public long getLastUpdateStartMillis() {
        return lastUpdateStartMillis;
    }
    public long getLastBackloadUpdateFinishedMillis() {
        return lastBackloadUpdateFinishedMillis;
    }

    public int getMaxMessageDisplay() {
        if (!isConfigured() || maxMessageDisplay == null) {
            // return default
            return Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DISPLAY);
        }
        return maxMessageDisplay.intValue();
    }
    public Integer getMaxMessageDisplayObj() {
        return maxMessageDisplay;
    }

    public int getMaxMessageDownload() {
        if (!isConfigured() || maxMessageDownload == null) {
            // return default
            return Core.frostSettings.getIntValue(SettingsClass.MAX_MESSAGE_DOWNLOAD);
        }
        return maxMessageDownload.intValue();
    }
    public Integer getMaxMessageDownloadObj() {
        return maxMessageDownload;
    }

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public int getTimesUpdatedCount() {
        return timesUpdatedCount;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean getShowSignedOnly() {
        if (!isConfigured() || showSignedOnly == null) {
            // return default
            return Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_HIDE_UNSIGNED);
        }
        return showSignedOnly.booleanValue();
    }

    public Boolean getShowSignedOnlyObj() {
        return showSignedOnly;
    }

    public String getStateString() {
        if (isReadAccessBoard()) {
            return language.getString("Board.boardState.readAccess");
        } else if (isWriteAccessBoard()) {
            return language.getString("Board.boardState.writeAccess");
        } else if (isPublicBoard()) {
            return language.getString("Board.boardState.publicBoard");
        }
        return language.getString("Board.boardState.invalid");
    }

    //////////////////////////////////////////////
    // From BoardStats

    public void incBlocked() {
        numberBlocked++;
    }

    public void incNewMessageCount() {
        newMessageCount++;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    @Override
    public boolean isBoard() {
        return true;
    }

    public boolean isPublicBoard() {
        if (publicKey == null && privateKey == null) {
            return true;
        }
        return false;
    }

    public boolean isReadAccessBoard() {
        if (publicKey != null && privateKey == null) {
            return true;
        }
        return false;
    }

    public boolean isWriteAccessBoard() {
        if (publicKey != null && privateKey != null) {
            return true;
        }
        return false;
    }

    public boolean isSpammed() {
        return spammed;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void resetBlocked() {
        numberBlocked = 0;
    }

    public void setAutoUpdateEnabled(final boolean val) {
        autoUpdateEnabled = val;
    }

    public void setConfigured(final boolean val) {
        isConfigured = val;
    }

    public void setStoreSentMessages(final Boolean val) {
        storeSentMessages = val;
    }

    public void setHideBad(final Boolean val) {
        hideBad = val;
    }

    public void setHideCheck(final Boolean val) {
        hideCheck = val;
    }

    public void setHideObserve(final Boolean val) {
        hideObserve = val;
    }

    public void setLastUpdateStartMillis(final long millis) {
        lastUpdateStartMillis = millis;
    }
    public void setLastBackloadUpdateFinishedMillis(final long millis) {
        lastBackloadUpdateFinishedMillis = millis;
    }

    public void setMaxMessageDays(final Integer val) {
        maxMessageDisplay = val;
    }

    public void setMaxMessageDownload(final Integer val) {
        maxMessageDownload = val;
    }

    public void setNewMessageCount(final int val) {
        newMessageCount = val;
    }

    public void setPrivateKey(String val) {
        if (val != null) {
            val = val.trim();
            if( val.length() == 0 ) {
                val = null;
            }
        }
        privateKey = val;
    }

    public void setPublicKey(String val) {
        if (val != null) {
            val = val.trim();
            if( val.length() == 0 ) {
                val = null;
            }
        }
        publicKey = val;
    }

    public void setShowSignedOnly(final Boolean val) {
        showSignedOnly = val;
    }

    public void setSpammed(final boolean val) {
        spammed = val;
    }

    public void setUpdating(final boolean val) {
        isUpdating = val;
    }

    /**
     * Returns true if board is allowed to be updated.
     * If a board is already updating only not running threads will be started.
     */
    public boolean isManualUpdateAllowed() {
        if ( !isBoard() || isSpammed() ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns true if board is allowed to be updated.
     * Also checks if board update is already running.
     */
    public boolean isAutomaticUpdateAllowed() {
        if ( !isBoard() || isSpammed() || isUpdating() ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Tells the board that a new message was received right now.
     * Needed for selective board update.
     * We can't use newMessageCount for this because this field is updated
     * also if a message is mark unread.
     */
    public void newMessageReceived() {
    }

    public boolean hasFlaggedMessages() {
        return hasFlaggedMessages;
    }
    public void hasFlaggedMessages(final boolean newHasFlaggedMessages) {
        this.hasFlaggedMessages = newHasFlaggedMessages;
    }

    public boolean hasStarredMessages() {
        return hasStarredMessages;
    }
    public void hasStarredMessages(final boolean newHasStarredMessages) {
        this.hasStarredMessages = newHasStarredMessages;
    }

    public PerstFrostBoardObject getPerstFrostBoardObject() {
        return perstFrostBoardObject;
    }

    public void setPerstFrostBoardObject(final PerstFrostBoardObject perstFrostBoardObject) {
        this.perstFrostBoardObject = perstFrostBoardObject;
    }

    /////// BoardUpdateInformation methods //////

    public BoardUpdateInformation getBoardUpdateInformationForDay(final String dirDate) {
        return boardUpdateInformations.get(dirDate);
    }

    public BoardUpdateInformation getOrCreateBoardUpdateInformationForDay(final String dateString, final long dateMillis) {
        BoardUpdateInformation bui = getBoardUpdateInformationForDay(dateString);
        if( bui == null ) {
            bui = new BoardUpdateInformation(this, dateString, dateMillis);
            boardUpdateInformations.put(dateString, bui);
        }
        return bui;
    }

    /**
     * @return  a List of all available BoardUpdateInformation object, sorted by day (latest first)
     */
    public List<BoardUpdateInformation> getBoardUpdateInformationList() {
        final List<BoardUpdateInformation> buiList = new ArrayList<BoardUpdateInformation>(boardUpdateInformations.size());
        buiList.addAll( boardUpdateInformations.values() );
        Collections.sort(buiList, boardUpdateInformationComparator);
        return buiList;
    }

    public boolean hasBoardUpdateInformations() {
        return boardUpdateInformations.size() > 0;
    }

    /**
     * Sort BoardUpdateInformation descending by dateMillis.
     */
    private static class BoardUpdateInformationComparator implements Comparator<BoardUpdateInformation> {
        public int compare(final BoardUpdateInformation o1, final BoardUpdateInformation o2) {
            return Mixed.compareLong(o2.getDateMillis(), o1.getDateMillis());
        }
    }
}
