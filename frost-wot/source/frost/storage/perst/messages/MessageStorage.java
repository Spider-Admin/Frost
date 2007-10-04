/*
  MessageStorage.java / Frost
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
package frost.storage.perst.messages;

import java.util.*;
import java.util.logging.*;

import org.garret.perst.*;
import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.*;

public class MessageStorage extends AbstractFrostStorage implements Savable {

    private static transient final Logger logger = Logger.getLogger(MessageStorage.class.getName());

    public static final int INSERT_OK        = 1;
    public static final int INSERT_DUPLICATE = 2;
    public static final int INSERT_ERROR     = 3;

    private MessageStorageRoot storageRoot = null;

    private static MessageStorage instance = new MessageStorage();

    private final boolean storeInvalidMessages;

    protected MessageStorage() {
        super();
        storeInvalidMessages = Core.frostSettings.getBoolValue(SettingsClass.STORAGE_STORE_INVALID_MESSAGES);
    }

    public static MessageStorage inst() {
        return instance;
    }

    @Override
    public boolean initStorage() {
        final String databaseFilePath = getStorageFilename("messages.dbs"); // path to the database file
        final int pagePoolSize = getPagePoolSize(SettingsClass.PERST_PAGEPOOLSIZE_MESSAGES);

        open(databaseFilePath, pagePoolSize, true, true, false);

        storageRoot = (MessageStorageRoot)getStorage().getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new MessageStorageRoot(getStorage());
            getStorage().setRoot(storageRoot);
            commit(); // commit transaction
        }
        return true;
    }

    @Override
    public synchronized void commit() {
        super.commit();
        MessageContentStorage.inst().commit();
    }

    public void save() {
        close();
        storageRoot = null;
        System.out.println("INFO: MessagesStorage closed.");
    }

    public void silentClose() {
        close();
        storageRoot = null;
    }

    public void importBoards(final Hashtable<String, Integer> boardPrimaryKeysByName) {
        int highestBoardId = 0;
        for( final String boardName : boardPrimaryKeysByName.keySet() ) {
            final Integer boardId = boardPrimaryKeysByName.get(boardName);

            // prevent duplicate board names
            if( storageRoot.getBoardsByName().contains(boardName) ) {
                continue; // dup!
            }
            final PerstFrostBoardObject pfbo = new PerstFrostBoardObject(getStorage(), boardName, boardId.intValue());
            storageRoot.getBoardsByName().put(boardName, pfbo);
            storageRoot.getBoardsById().put(boardId, pfbo);

            highestBoardId = Math.max(highestBoardId, boardId.intValue());
        }

        storageRoot.initUniqueBoardId(highestBoardId+1);

        commit();
    }

    /**
     * Retrieve the primary key of the board, or insert it into storage.
     */
    public boolean assignPerstFrostBoardObject(final Board newNode) {
        PerstFrostBoardObject pbo = storageRoot.getBoardsByName().get(newNode.getNameLowerCase());
        if( pbo == null ) {
            // not yet in perst, create new one
            addBoard(newNode);

            pbo = storageRoot.getBoardsByName().get(newNode.getNameLowerCase());
            if( pbo == null ) {
                logger.severe("board still not added!");
                return false;
            }
        }

        newNode.setPerstFrostBoardObject(pbo);
        pbo.setRefBoard(newNode);

        return true;
    }

    /**
     * Adds a new board and returns the Board object with the perst object assigned.
     */
    public synchronized Board addBoard(final Board board) {

        if( board == null ) {
            return null;
        }

        // prevent duplicate board names
        PerstFrostBoardObject pfbo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( pfbo != null ) {
            board.setPerstFrostBoardObject(pfbo);
            return board; // dup!
        }

        final int boardId = storageRoot.getNextUniqueBoardId();

        pfbo = new PerstFrostBoardObject(getStorage(), board.getNameLowerCase(), boardId);
        storageRoot.getBoardsByName().put(board.getNameLowerCase(), pfbo);
        storageRoot.getBoardsById().put(boardId, pfbo);

        commit();

        return board;
    }

    private void removeAll(final Iterator<? extends Persistent> i) {
        while(i.hasNext()) {
            i.next().deallocate();
        }
    }

    /**
     * Removes a board from the board list.
     */
    public synchronized void removeBoard(final Board board) {
        final PerstFrostBoardObject boardToRemove = board.getPerstFrostBoardObject();
        if( boardToRemove == null ) {
            return;
        }

        // delete ALL valid messages
        for( final PerstFrostMessageObject pmo : boardToRemove.getMessageIndex() ) {
            if( FrostMessageObject.isSignatureStatusVERIFIED(pmo.signatureStatus) ) {
                final PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(pmo.fromName);
                if( pim != null ) {
                    pim.getMessagesFromIdentity().remove(pmo);
                    if( pim.getMessagesFromIdentity().size() == 0 ) {
                        storageRoot.getIdentitiesMessages().remove(pmo.fromName);
                        pim.deallocate();
                    }
                }
            }
            pmo.deallocate();
        }
        boardToRemove.getMessageIndex().clear();
        boardToRemove.getUnreadMessageIndex().clear();
        boardToRemove.getFlaggedMessageIndex().clear();
        boardToRemove.getStarredMessageIndex().clear();

        commit();

        // delete ALL invalid messages
        removeAll(boardToRemove.getInvalidMessagesIndex().iterator());
        boardToRemove.getInvalidMessagesIndex().clear();

        commit();

        // delete ALL sent and unsent messages
        removeAll(boardToRemove.getSentMessagesList().iterator());
        boardToRemove.getSentMessagesList().clear();
        removeAll(boardToRemove.getUnsentMessagesList().iterator());
        boardToRemove.getUnsentMessagesList().clear();
        removeAll(boardToRemove.getDraftMessagesList().iterator());
        boardToRemove.getDraftMessagesList().clear();

        storageRoot.getBoardsByName().remove( boardToRemove );
        storageRoot.getBoardsById().remove( boardToRemove );

        boardToRemove.deallocate();

        commit();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public int getMessageCount() {
        int msgCount = 0;
        for(final PerstFrostBoardObject bo : storageRoot.getBoardsByName()) {
            if( bo.getMessageIndex() != null ) {
                msgCount += bo.getMessageIndex().size();
            }
        }
        return msgCount;
    }

    public int getMessageCount(final String uniqueIdentityName) {
        final PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(uniqueIdentityName);
        if( pim != null ) {
            return pim.getMessagesFromIdentity().size();
        } else {
            return 0;
        }
    }

    /**
     * Returns count of all msgs AND all unread msgs (no matter how old they are).
     * If maxDaysBack is < 0 then ALL msgs for this board are counted.
     */
    public int getMessageCount(final Board board, final int maxDaysBack) {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return -1;
        }
        if( maxDaysBack < 0 ) {
            return bo.getMessageIndex().size();
        }

        final LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
        final long minDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();
        // normal messages in date range
        final Iterator<PerstFrostMessageObject> i1 = bo.getMessageIndex().iterator(minDateTime, Long.MAX_VALUE, Index.ASCENT_ORDER);
        // add ALL unread messages, also those which are not in date range
        final Iterator<PerstFrostMessageObject> i2 = bo.getUnreadMessageIndex().iterator();
        // add ALL flagged and starred messages, also those which are not in date range
        final Iterator<PerstFrostMessageObject> i3 = bo.getStarredMessageIndex().iterator();
        final Iterator<PerstFrostMessageObject> i4 = bo.getFlaggedMessageIndex().iterator();

        // join all results
        final Iterator<PerstFrostMessageObject> i = getStorage().join(new Iterator[] {i1, i2, i3, i4} );

        int count = 0;

        while(i.hasNext()) {
            ((PersistentIterator)i).nextOid();
            count++;
        }
        return count;
    }

    public int getNewMessageCount(final Board board) {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return -1;
        }
        // ALL new messages
        return bo.getUnreadMessageIndex().size();
    }

    public boolean hasFlaggedMessages(final Board board) {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return false;
        }
        return bo.getFlaggedMessageIndex().size() > 0;
    }

    public boolean hasStarredMessages(final Board board) {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return false;
        }
        return bo.getStarredMessageIndex().size() > 0;
    }

    public synchronized int insertMessage(final FrostMessageObject mo, final boolean doCommit) {

        // add to indices, check for duplicate msgId

        if( mo.getPerstFrostMessageObject() != null ) {
            // already in store!
            logger.severe("msgInsertError: perst obj already set");
            return INSERT_ERROR; // skip msg
        }

        final Board targetBoard = mo.getBoard();
        if( targetBoard == null ) {
            // already in store!
            logger.severe("msgInsertError: no board in msg");
            return INSERT_ERROR; // skip msg
        }

        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(targetBoard.getNameLowerCase());
        if( bo == null ) {
            // create new board
            logger.severe("Creating new perst board: "+targetBoard.getName());
            addBoard(targetBoard);
            bo = storageRoot.getBoardsByName().get(targetBoard.getNameLowerCase());
            if( bo == null ) {
                logger.severe("Error: duplicate board???");
                return INSERT_ERROR;
            }
        }

        final PerstFrostMessageObject pmo = new PerstFrostMessageObject(mo, getStorage());

        if( !mo.isValid() ) {
            // invalid message
            if( storeInvalidMessages ) {
                // we store it as requested by option. invalid messages are usually not needed.
                bo.getInvalidMessagesIndex().put(mo.getDateAndTime().getMillis(), pmo);
            }
        } else {
            if( mo.getMessageId() != null ) {
                if( !bo.getMessageIdIndex().put(mo.getMessageId(), pmo) ) {
                    // duplicate messageId!
                    return INSERT_DUPLICATE; // skip msg
                }
            }

            mo.setPerstFrostMessageObject(pmo);

            bo.getMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);
            if( pmo.isNew ) {
                bo.getUnreadMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);
            }
            if( pmo.isFlagged ) {
                bo.getFlaggedMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);
            }
            if( pmo.isStarred ) {
                bo.getStarredMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);
            }

            // add to id, maybe create id for this msg
            if( FrostMessageObject.isSignatureStatusVERIFIED(pmo.signatureStatus) ) {
                PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(pmo.fromName);
                if( pim == null ) {
                    pim = new PerstIdentitiesMessages(pmo.fromName, getStorage());
                    storageRoot.getIdentitiesMessages().put(pmo.fromName, pim);
                }
                pim.getMessagesFromIdentity().add( pmo );
            }
        }

        if( doCommit ) {
            commit();
        }

        return INSERT_OK;
    }

    public FrostMessageObject retrieveMessageByMessageId(
            final Board board,
            final String msgId,
            final boolean withContent,
            final boolean withAttachments,
            boolean showDeleted)
    {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return null;
        }

        final PerstFrostMessageObject p = bo.getMessageIdIndex().get(msgId);
        if( p == null ) {
            return null;
        }
        if(!showDeleted && p.isDeleted) {
            return null;
        }
        final FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
        return mo;
    }

    public void retrieveMessageContent(final FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveMessageContent(mo);
        }
    }

    public void retrievePublicKey(final FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrievePublicKey(mo);
        }
    }

    public void retrieveSignature(final FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveSignature(mo);
        }
    }

    public void retrieveAttachments(final FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveAttachments(mo);
        }
    }

    public void retrieveMessagesForArchive(
            final Board board,
            final int maxDaysOld,
            final boolean archiveKeepUnread,
            final boolean archiveKeepFlaggedAndStarred,
            final MessageArchivingCallback mc)
    {
        final LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysOld);
        final long maxDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for archive");
            return;
        }
        // normal messages in date range
        final Iterator<PerstFrostMessageObject> i = bo.getMessageIndex().iterator(Long.MIN_VALUE, maxDateTime, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            final PerstFrostMessageObject p = i.next();
            if( archiveKeepUnread && p.isNew) {
                continue;
            }
            if( archiveKeepFlaggedAndStarred && (p.isFlagged||p.isStarred) ) {
                continue;
            }
            final FrostMessageObject mo = p.toFrostMessageObject(board, true, false, false);
            final int mode = mc.messageRetrieved(mo);
            if( mode == MessageArchivingCallback.STOP_ERROR ) {
                return;
            } else if( mode == MessageArchivingCallback.DELETE_MESSAGE ) {
                // delete msg and internal perst objs, also remove from indices and maybe remove empty identitiesMessages

                i.remove();

                if( p.isNew) {
                    bo.getUnreadMessageIndex().remove(p.dateAndTime, p);
                }
                if( p.isFlagged ) {
                    bo.getFlaggedMessageIndex().remove(p.dateAndTime, p);
                }
                if( p.isStarred ) {
                    bo.getStarredMessageIndex().remove(p.dateAndTime, p);
                }

                if( mo.isSignatureStatusVERIFIED() ) {
                    final PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(p.fromName);
                    if( pim != null ) {
                        pim.getMessagesFromIdentity().remove(p);
                        if( pim.getMessagesFromIdentity().size() == 0 ) {
                            storageRoot.getIdentitiesMessages().remove(p.fromName);
                            pim.deallocate();
                        }
                    }
                }

                if( p.messageId != null ) {
                    bo.getMessageIdIndex().remove(p.messageId);
                }

                p.deallocate();
            }
        }

        // delete invalid messages in date range
        final Iterator<PerstFrostMessageObject> ii = bo.getInvalidMessagesIndex().iterator(Long.MIN_VALUE, maxDateTime, Index.ASCENT_ORDER);
        while(ii.hasNext()) {
            final PerstFrostMessageObject p = ii.next();
            ii.remove();
            p.deallocate();
        }
    }

    public void retrieveMessagesForSearch(
            final Board board,
            final long startDate,
            final long endDate,
            final boolean withContent,
            final boolean withAttachments,
            boolean showDeleted,
            final MessageCallback mc)
    {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for search");
            return;
        }
        // normal messages in date range
        final Iterator<PerstFrostMessageObject> i = bo.getMessageIndex().iterator(startDate, endDate, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            final PerstFrostMessageObject p = i.next();
            if(!showDeleted && p.isDeleted) {
                continue;
            }
            final FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
            final boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }

    public synchronized void retrieveMessagesForShow(
            final Board board,
            final int maxDaysBack,
            final boolean withContent,
            final boolean withAttachments,
            boolean showDeleted,
            final boolean showUnreadOnly,
            final MessageCallback mc)
    {
        final LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
        final long minDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for show");
            return;
        }

        Iterator<PerstFrostMessageObject> i;
        if( showUnreadOnly ) {
            // ALL new messages
            i = bo.getUnreadMessageIndex().iterator();
        } else {
            // normal messages in date range
            final Iterator<PerstFrostMessageObject> i1 = bo.getMessageIndex().iterator(minDateTime, Long.MAX_VALUE, Index.ASCENT_ORDER);
            // add ALL unread messages, also those which are not in date range
            final Iterator<PerstFrostMessageObject> i2 = bo.getUnreadMessageIndex().iterator();
            // add ALL flagged and starred messages, also those which are not in date range
            final Iterator<PerstFrostMessageObject> i3 = bo.getStarredMessageIndex().iterator();
            final Iterator<PerstFrostMessageObject> i4 = bo.getFlaggedMessageIndex().iterator();

            // join all results
            i = getStorage().join(new Iterator[] {i1, i2, i3, i4} );
        }

        while(i.hasNext()) {
            final PerstFrostMessageObject p = i.next();
            if(!showDeleted && p.isDeleted) {
                continue;
            }
            final FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
            final boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }

    public synchronized void setAllMessagesRead(final Board board) {
        final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for update");
            return;
        }
        final Iterator<PerstFrostMessageObject> i = bo.getUnreadMessageIndex().iterator();
        while(i.hasNext()) {
            final PerstFrostMessageObject pmo = i.next();
            pmo.isNew = false;
            pmo.modify();
        }
        bo.getUnreadMessageIndex().clear();
        commit();
    }

    public synchronized void updateMessage(final FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {

            final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(mo.getBoard().getNameLowerCase());
            if( bo == null ) {
                logger.severe("error: no perst board for update");
                return;
            }

            final PerstFrostMessageObject p = mo.getPerstFrostMessageObject();

            if( p.isNew && !mo.isNew() ) {
                // was unread, is read now -> remove from unreadIndex
                bo.getUnreadMessageIndex().remove(p.dateAndTime, p);
            } else if( !p.isNew && mo.isNew() ) {
                // was read, is unread now -> add to unreadIndex
                bo.getUnreadMessageIndex().put(p.dateAndTime, p);
            }

            if( p.isFlagged && !mo.isFlagged() ) {
                // was unread, is read now -> remove from unreadIndex
                bo.getFlaggedMessageIndex().remove(p.dateAndTime, p);
            } else if( !p.isFlagged && mo.isFlagged() ) {
                // was read, is unread now -> add to unreadIndex
                bo.getFlaggedMessageIndex().put(p.dateAndTime, p);
            }

            if( p.isStarred && !mo.isStarred() ) {
                // was unread, is read now -> remove from unreadIndex
                bo.getStarredMessageIndex().remove(p.dateAndTime, p);
            } else if( !p.isStarred && mo.isStarred() ) {
                // was read, is unread now -> add to unreadIndex
                bo.getStarredMessageIndex().put(p.dateAndTime, p);
            }

            p.isDeleted = mo.isDeleted();
            p.isNew = mo.isNew();
            p.isReplied = mo.isReplied();
            p.isJunk = mo.isJunk();
            p.isFlagged = mo.isFlagged();
            p.isStarred = mo.isStarred();

            p.modify();

            commit();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public List<FrostMessageObject> retrieveAllSentMessages() {
        final List<FrostMessageObject> lst = new ArrayList<FrostMessageObject>();

        for( final PerstFrostBoardObject bo : storageRoot.getBoardsByName() ) {
            for( final PerstFrostMessageObject pmo : bo.getSentMessagesList() ) {
                final FrostMessageObject mo = pmo.toFrostMessageObject(bo.getRefBoard(), true, false, false);
                lst.add(mo);
            }
        }

        return lst;
    }

    public boolean addSentMessage(final FrostMessageObject sentMo) {
        return addSentMessage(sentMo, true);
    }

    public boolean addSentMessage(final FrostMessageObject sentMo, final boolean doCommit) {
        final PerstFrostBoardObject bo = sentMo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            logger.severe("no board for new sent msg!");
            return false;
        }

        final PerstFrostMessageObject pmo = new PerstFrostMessageObject(sentMo, getStorage());
        sentMo.setPerstFrostMessageObject(pmo);

        bo.getSentMessagesList().add( pmo );
        if( doCommit ) {
            commit();
        }
        return true;
    }

    public int deleteSentMessages(final List<FrostMessageObject> msgObjects) {
        int count = 0;
        for( final FrostMessageObject mo : msgObjects ) {
            if( mo.getPerstFrostMessageObject() == null ) {
                logger.severe("delete not possible");
                continue;
            }
            final PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(mo.getBoard().getNameLowerCase());
            if( bo == null ) {
                logger.severe("board not found");
                continue;
            }
            bo.getSentMessagesList().remove(mo.getPerstFrostMessageObject());
            mo.getPerstFrostMessageObject().deallocate();
            mo.setPerstFrostMessageObject(null);
            count++;
        }
        commit();
        return count;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public List<FrostUnsentMessageObject> retrieveAllUnsentMessages() {
        final List<FrostUnsentMessageObject> lst = new ArrayList<FrostUnsentMessageObject>();

        for( final PerstFrostBoardObject bo : storageRoot.getBoardsByName() ) {
            for( final PerstFrostUnsentMessageObject pmo : bo.getUnsentMessagesList() ) {
                final FrostUnsentMessageObject mo = pmo.toFrostUnsentMessageObject(bo.getRefBoard());
                lst.add(mo);
            }
        }
        return lst;
    }

    public boolean addUnsentMessage(final FrostUnsentMessageObject mo) {
        return addUnsentMessage(mo, true);
    }

    public boolean addUnsentMessage(final FrostUnsentMessageObject mo, final boolean doCommit) {
        final PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            logger.severe("no board for new unsent msg!");
            return false;
        }

        final PerstFrostUnsentMessageObject pmo = new PerstFrostUnsentMessageObject(getStorage(), mo);

        bo.getUnsentMessagesList().add( pmo );
        if( doCommit ) {
            commit();
        }
        return true;
    }

    public boolean deleteUnsentMessage(final FrostUnsentMessageObject mo) {
        final PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            logger.severe("no board for unsent msg!");
            return false;
        }

        final PerstFrostUnsentMessageObject pmo = mo.getPerstFrostUnsentMessageObject();
        if( pmo == null ) {
            logger.severe("no perst unsent msg obj!");
            return false;
        }

        bo.getUnsentMessagesList().remove( pmo );
        pmo.deallocate();
        commit();
        return true;
    }

    /**
     * Updates the CHK keys of fileattachments after upload of attachments.
     */
    public synchronized void updateUnsentMessageFileAttachmentKey(final FrostUnsentMessageObject mo, final FileAttachment fa) {

        final PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            logger.severe("no board for new unsent msg update!");
            return;
        }

        final PerstFrostUnsentMessageObject pmo = mo.getPerstFrostUnsentMessageObject();
        if( pmo == null ) {
            logger.severe("no perst unsent msg obj for update!");
            return;
        }

        pmo.updateUnsentMessageFileAttachmentKey(fa);
        commit();
    }
}
