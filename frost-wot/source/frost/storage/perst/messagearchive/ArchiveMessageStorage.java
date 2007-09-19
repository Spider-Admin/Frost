/*
  MessageArchiveStorage.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.messagearchive;

import java.util.*;
import java.util.logging.*;

import org.garret.perst.*;

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;

public class ArchiveMessageStorage extends AbstractFrostStorage implements Savable {

    private static final Logger logger = Logger.getLogger(ArchiveMessageStorage.class.getName());

    public static final int INSERT_OK        = 1;
    public static final int INSERT_DUPLICATE = 2;
    public static final int INSERT_ERROR     = 3;

    private ArchiveMessageStorageRoot storageRoot = null;

    private static ArchiveMessageStorage instance = new ArchiveMessageStorage();

    protected ArchiveMessageStorage() {
        super();
    }

    public static ArchiveMessageStorage inst() {
        return instance;
    }

    @Override
    public boolean initStorage() {
        final String databaseFilePath = getStorageFilename("messageArchive.dbs"); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_MESSAGEARCHIVE);

        open(databaseFilePath, pagePoolSize, true, false, false);

        storageRoot = (ArchiveMessageStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new ArchiveMessageStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    public void save() {
        close();
        storageRoot = null;
        System.out.println("INFO: MessageArchiveStorage closed.");
    }

    public void silentClose() {
        close();
        storageRoot = null;
    }

    public int getMessageCount() {
        int msgCount = 0;
        for(final PerstFrostArchiveBoardObject bo : storageRoot.getBoardsByName()) {
            if( bo.getMessageIndex() != null ) {
                msgCount += bo.getMessageIndex().size();
            }
        }
        return msgCount;
    }

    private void addBoard(final String boardName) {

        if( boardName == null ) {
            return;
        }

        // prevent duplicate board names
        if( storageRoot.getBoardsByName().contains(boardName) ) {
            return; // dup!
        }

        final PerstFrostArchiveBoardObject pfbo = new PerstFrostArchiveBoardObject(getStorage(), boardName);
        storageRoot.getBoardsByName().put(boardName, pfbo);

        logger.severe("Added archive board: "+boardName);

        commit();
    }

    public synchronized int insertMessage(final FrostMessageObject mo, final boolean doCommit) {
        final Board targetBoard = mo.getBoard();
        if( targetBoard == null ) {
            logger.severe("msgInsertError: no board in msg");
            return INSERT_ERROR; // skip msg
        }
        return insertMessage(mo, targetBoard.getNameLowerCase(), doCommit);
    }

    public synchronized int insertMessage(final FrostMessageObject mo, final String boardName, final boolean doCommit) {

        if( !mo.isValid() ) {
            return INSERT_OK; // ignore invalid msgs
        }
        if( mo.isDeleted() ) {
            return INSERT_OK; // ignore deleted msgs
        }

        // add to indices, check for duplicate msgId

        PerstFrostArchiveBoardObject bo = storageRoot.getBoardsByName().get(boardName);
        if( bo == null ) {
            // create new board
            addBoard(boardName);
            bo = storageRoot.getBoardsByName().get(boardName);
            if( bo == null ) {
                logger.severe("Error: still no board???");
                return INSERT_ERROR;
            }
        }

        final PerstFrostArchiveMessageObject pmo = new PerstFrostArchiveMessageObject(mo, getStorage());

        if( mo.getMessageId() != null ) {
            if( !bo.getMessageIdIndex().put(mo.getMessageId(), pmo) ) {
                // duplicate messageId!
                return INSERT_DUPLICATE; // skip msg
            }
        }

        bo.getMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);

        if( doCommit ) {
            commit();
        }

        return INSERT_OK;
    }

    public void retrieveMessagesForSearch(
            final Board board,
            final long startDate,
            final long endDate,
            final MessageCallback mc)
    {
        final PerstFrostArchiveBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for archive search");
            return;
        }
        // normal messages in date range
        final Iterator<PerstFrostArchiveMessageObject> i = bo.getMessageIndex().iterator(startDate, endDate, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            final PerstFrostArchiveMessageObject p = i.next();
            final FrostMessageObject mo = p.toFrostMessageObject(board);
            final boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }
}
