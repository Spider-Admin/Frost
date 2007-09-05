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

import org.garret.perst.*;
import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.*;

public class MessageStorage implements Savable {
    
    public static final int INSERT_OK        = 1;
    public static final int INSERT_DUPLICATE = 2;
    public static final int INSERT_ERROR     = 3;

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 6; // page size for the storage in MB
    
    private Storage storage = null;
    private MessageStorageRoot storageRoot = null;
    
    private static MessageStorage instance = new MessageStorage();
    
    private boolean storeInvalidMessages;

    protected MessageStorage() {
        storeInvalidMessages = Core.frostSettings.getBoolValue(SettingsClass.STORAGE_STORE_INVALID_MESSAGES);
    }
    
    public static MessageStorage inst() {
        return instance;
    }
    
    private Storage getStorage() {
        return storage;
    }
    
    public boolean initStorage() {
        String databaseFilePath = "store/messages.dbs"; // path to the database file
        int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.concurrent.iterator", Boolean.TRUE); // remove() during iteration (for cleanup)
        storage.setProperty("perst.string.encoding", "UTF-8");
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (MessageStorageRoot)storage.getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new MessageStorageRoot(storage);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
        if( getStorage() == null ) {
            return;
        }
        getStorage().commit();
        MessageContentStorage.inst().commitStore();
    }

    public void save() throws StorageException {
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: MessagesStorage closed.");
    }
    
    public void importBoards(Hashtable<String, Integer> boardPrimaryKeysByName) {
        int highestBoardId = 0;
        for( String boardName : boardPrimaryKeysByName.keySet() ) {
            Integer boardId = (Integer) boardPrimaryKeysByName.get(boardName);

            // prevent duplicate board names
            if( storageRoot.getBoardsByName().contains(boardName) ) {
                continue; // dup!
            }
            PerstFrostBoardObject pfbo = new PerstFrostBoardObject(storage, boardName, boardId.intValue());
            storageRoot.getBoardsByName().put(boardName, pfbo);
            storageRoot.getBoardsById().put(boardId, pfbo);
            
            highestBoardId = Math.max(highestBoardId, boardId.intValue());
        }
        
        storageRoot.initUniqueBoardId(highestBoardId+1);
        
        storage.commit();
    }
    
    /**
     * Retrieve the primary key of the board, or insert it into database.
     */
    public boolean assignPerstFrostBoardObject(Board newNode) {
        PerstFrostBoardObject pbo = storageRoot.getBoardsByName().get(newNode.getNameLowerCase());
        if( pbo == null ) {
            // not yet in perst, create new one
            addBoard(newNode);

            pbo = storageRoot.getBoardsByName().get(newNode.getNameLowerCase());
            if( pbo == null ) {
                System.out.println("board still not added!");
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

        int boardId = storageRoot.getNextUniqueBoardId();

        pfbo = new PerstFrostBoardObject(storage, board.getNameLowerCase(), boardId);
        storageRoot.getBoardsByName().put(board.getNameLowerCase(), pfbo);
        storageRoot.getBoardsById().put(boardId, pfbo);

        commitStore();
        
        return board;
    }
    
    private void removeAll(Iterator<? extends Persistent> i) {
        while(i.hasNext()) {
            i.next().deallocate();
        }
    }

    /**
     * Removes a board from the board list.
     */
    public synchronized void removeBoard(Board board) {
        PerstFrostBoardObject boardToRemove = board.getPerstFrostBoardObject();
        if( boardToRemove == null ) {
            return;
        }

        // delete ALL valid messages
        for(Iterator<PerstFrostMessageObject> i=boardToRemove.getMessageIndex().iterator(); i.hasNext(); ) {
            PerstFrostMessageObject pmo = i.next();
            if( FrostMessageObject.isSignatureStatusVERIFIED(pmo.signatureStatus) ) {
                PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(pmo.fromName);
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
        
        commitStore();

        // delete ALL invalid messages 
        removeAll(boardToRemove.getInvalidMessagesIndex().iterator());
        boardToRemove.getInvalidMessagesIndex().clear();

        commitStore();

        // delete ALL sent and unsent messages 
        removeAll(boardToRemove.getSentMessagesList().iterator());
        boardToRemove.getSentMessagesList().clear();
        removeAll(boardToRemove.getUnsentMessagesList().iterator());
        boardToRemove.getUnsentMessagesList().clear();

        storageRoot.getBoardsByName().remove( boardToRemove );
        storageRoot.getBoardsById().remove( boardToRemove );
        
        boardToRemove.deallocate();
        
        commitStore();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public int getMessageCount() {
        int msgCount = 0;
        for(PerstFrostBoardObject bo : storageRoot.getBoardsByName()) {
            if( bo.getMessageIndex() != null ) {
                msgCount += bo.getMessageIndex().size();
            }
        }
        return msgCount;
    }

    public int getMessageCount(String uniqueIdentityName) {
        PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(uniqueIdentityName);
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
    public int getMessageCount(Board board, int maxDaysBack) {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return -1;
        }
        if( maxDaysBack < 0 ) {
            return bo.getMessageIndex().size();
        }

        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
        long minDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();
        // normal messages in date range
        Iterator<PerstFrostMessageObject> i1 = bo.getMessageIndex().iterator(minDateTime, Long.MAX_VALUE, Index.ASCENT_ORDER);
        // add ALL unread messages, also those which are not in date range
        Iterator<PerstFrostMessageObject> i2 = bo.getUnreadMessageIndex().iterator();
        // add ALL flagged and starred messages, also those which are not in date range
        Iterator<PerstFrostMessageObject> i3 = bo.getStarredMessageIndex().iterator();
        Iterator<PerstFrostMessageObject> i4 = bo.getFlaggedMessageIndex().iterator();

        // join all results
        Iterator<PerstFrostMessageObject> i = storage.join(new Iterator[] {i1, i2, i3, i4} );
        
        int count = 0;
        
        while(i.hasNext()) {
            ((PersistentIterator)i).nextOid();
            count++;
        }
        return count;
    }
    
    public int getNewMessageCount(Board board) {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return -1;
        }
        // ALL new messages
        return bo.getUnreadMessageIndex().size();
    }
    
    public boolean hasFlaggedMessages(Board board) {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return false;
        }
        return bo.getFlaggedMessageIndex().size() > 0;
    }
    
    public boolean hasStarredMessages(Board board) {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return false;
        }
        return bo.getStarredMessageIndex().size() > 0;
    }

    public synchronized int insertMessage(FrostMessageObject mo, boolean doCommit) {

        // add to indices, check for duplicate msgId

        if( mo.getPerstFrostMessageObject() != null ) {
            // already in store!
            System.out.println("msgInsertError: perst obj already set");
            return INSERT_ERROR; // skip msg
        }
        
        Board targetBoard = mo.getBoard();
        if( targetBoard == null ) {
            // already in store!
            System.out.println("msgInsertError: no board in msg");
            return INSERT_ERROR; // skip msg
        }
        
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(targetBoard.getNameLowerCase());
        if( bo == null ) {
            // create new board
            System.out.println("Creating new perst board: "+targetBoard.getName());
            addBoard(targetBoard);
            bo = storageRoot.getBoardsByName().get(targetBoard.getNameLowerCase());
            if( bo == null ) {
                System.out.println("Error: duplicate board???");
                return INSERT_ERROR;
            }
        }
        
        PerstFrostMessageObject pmo = new PerstFrostMessageObject(mo, storage);

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
            commitStore();
        }

        return INSERT_OK;
    }
    
    public FrostMessageObject retrieveMessageByMessageId(
            Board board,
            String msgId,
            boolean withContent, 
            boolean withAttachments, 
            boolean showDeleted) 
    {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            return null;
        }
        
        PerstFrostMessageObject p = bo.getMessageIdIndex().get(msgId);
        if( p == null ) {
            return null;
        }
        if(!showDeleted && p.isDeleted) {
            return null;
        }        
        FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
        return mo;
    }
    
    public void retrieveMessageContent(FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveMessageContent(mo);
        }
    }

    public void retrievePublicKey(FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrievePublicKey(mo);
        }
    }

    public void retrieveSignature(FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveSignature(mo);
        }
    }

    public void retrieveAttachments(FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {
            mo.getPerstFrostMessageObject().retrieveAttachments(mo);
        }
    }

    public void retrieveMessagesForArchive(
            Board board, 
            int maxDaysOld, 
            boolean archiveKeepUnread,
            boolean archiveKeepFlaggedAndStarred,
            MessageArchivingCallback mc) 
    {
        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysOld);
        long maxDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            System.out.println("error: no perst board for show");
            return;
        }
        // normal messages in date range
        Iterator<PerstFrostMessageObject> i = bo.getMessageIndex().iterator(Long.MIN_VALUE, maxDateTime, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            PerstFrostMessageObject p = i.next();
            if( archiveKeepUnread && p.isNew) {
                continue;
            }
            if( archiveKeepFlaggedAndStarred && (p.isFlagged||p.isStarred) ) {
                continue;
            }
            FrostMessageObject mo = p.toFrostMessageObject(board, true, false, false);
            int mode = mc.messageRetrieved(mo);
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
                    PerstIdentitiesMessages pim = storageRoot.getIdentitiesMessages().get(p.fromName);
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
        Iterator<PerstFrostMessageObject> ii = bo.getInvalidMessagesIndex().iterator(Long.MIN_VALUE, maxDateTime, Index.ASCENT_ORDER);
        while(ii.hasNext()) {
            PerstFrostMessageObject p = ii.next();
            ii.remove();
            p.deallocate();
        }
    }
    
    public void retrieveMessagesForSearch(
            Board board, 
            long startDate, 
            long endDate,
            boolean withContent,
            boolean withAttachments,
            boolean showDeleted, 
            MessageCallback mc) 
    {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            System.out.println("error: no perst board for search");
            return;
        }
        // normal messages in date range
        Iterator<PerstFrostMessageObject> i = bo.getMessageIndex().iterator(startDate, endDate, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            PerstFrostMessageObject p = i.next();
            if(!showDeleted && p.isDeleted) {
                continue;
            }
            FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
            boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }
    
    public synchronized void retrieveMessagesForShow(
            Board board,
            int maxDaysBack, 
            boolean withContent,
            boolean withAttachments,
            boolean showDeleted,
            boolean showUnreadOnly,
            MessageCallback mc) 
    {
        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
        long minDateTime = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();

        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            System.out.println("error: no perst board for show");
            return;
        }
        
        Iterator<PerstFrostMessageObject> i;
        if( showUnreadOnly ) {
            // ALL new messages
            i = bo.getUnreadMessageIndex().iterator();
        } else {
            // normal messages in date range
            Iterator<PerstFrostMessageObject> i1 = bo.getMessageIndex().iterator(minDateTime, Long.MAX_VALUE, Index.ASCENT_ORDER);
            // add ALL unread messages, also those which are not in date range
            Iterator<PerstFrostMessageObject> i2 = bo.getUnreadMessageIndex().iterator();
            // add ALL flagged and starred messages, also those which are not in date range
            Iterator<PerstFrostMessageObject> i3 = bo.getStarredMessageIndex().iterator();
            Iterator<PerstFrostMessageObject> i4 = bo.getFlaggedMessageIndex().iterator();

            // join all results
            i = storage.join(new Iterator[] {i1, i2, i3, i4} );
        }

        while(i.hasNext()) {
            PerstFrostMessageObject p = i.next();
            if(!showDeleted && p.isDeleted) {
                continue;
            }
            FrostMessageObject mo = p.toFrostMessageObject(board, true, withContent, withAttachments);
            boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }
    
    public synchronized void setAllMessagesRead(Board board) {
        PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            System.out.println("error: no perst board for update");
            return;
        }
        Iterator<PerstFrostMessageObject> i = bo.getUnreadMessageIndex().iterator();
        while(i.hasNext()) {
            PerstFrostMessageObject pmo = i.next();
            pmo.isNew = false;
            pmo.modify();
        }
        bo.getUnreadMessageIndex().clear();
        commitStore();
    }
    
    public synchronized void updateMessage(FrostMessageObject mo) {
        if( mo.getPerstFrostMessageObject() != null ) {

            PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(mo.getBoard().getNameLowerCase());
            if( bo == null ) {
                System.out.println("error: no perst board for update");
                return;
            }

            PerstFrostMessageObject p = mo.getPerstFrostMessageObject();
            
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

            MessageStorage.inst().commitStore();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////    
    
    public List<FrostMessageObject> retrieveAllSentMessages(List<Board> allBoards) {
        List<FrostMessageObject> lst = new ArrayList<FrostMessageObject>();
        
        
        for( Board board : allBoards ) {
            PerstFrostBoardObject bo = board.getPerstFrostBoardObject();
            if( bo == null ) {
                System.out.println("no perst board assigned!");
                continue;
            }
            for( PerstFrostMessageObject pmo : bo.getSentMessagesList() ) {
                FrostMessageObject mo = pmo.toFrostMessageObject(board, true, false, false);
                lst.add(mo);
            }
        }
        return lst;
    }

    public boolean addSentMessage(FrostMessageObject sentMo) {
        return addSentMessage(sentMo, true);
    }

    public boolean addSentMessage(FrostMessageObject sentMo, boolean doCommit) {
        PerstFrostBoardObject bo = sentMo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            System.out.println("no board for new sent msg!");
            return false;
        }

        PerstFrostMessageObject pmo = new PerstFrostMessageObject(sentMo, storage);
        sentMo.setPerstFrostMessageObject(pmo);
        
        bo.getSentMessagesList().add( pmo );
        if( doCommit ) {
            commitStore();
        }
        return true;
    }
    
    public int deleteSentMessages(List<FrostMessageObject> msgObjects) {
        int count = 0;
        for( FrostMessageObject mo : msgObjects ) {
            if( mo.getPerstFrostMessageObject() == null ) {
                System.out.println("delete not possible");
                continue;
            }
            PerstFrostBoardObject bo = storageRoot.getBoardsByName().get(mo.getBoard().getNameLowerCase());
            if( bo == null ) {
                System.out.println("board not found");
                continue;
            }
            bo.getSentMessagesList().remove(mo.getPerstFrostMessageObject());
            mo.getPerstFrostMessageObject().deallocate();
            mo.setPerstFrostMessageObject(null);
            count++;
        }
        commitStore();
        return count;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////    

    public List<FrostUnsentMessageObject> retrieveAllUnsentMessages() {
        List<FrostUnsentMessageObject> lst = new ArrayList<FrostUnsentMessageObject>();
        
        for( PerstFrostBoardObject bo : storageRoot.getBoardsByName() ) {
            for( PerstFrostUnsentMessageObject pmo : bo.getUnsentMessagesList() ) {
                FrostUnsentMessageObject mo = pmo.toFrostUnsentMessageObject(bo.getRefBoard());
                lst.add(mo);
            }
        }
        return lst;
    }

    public boolean addUnsentMessage(FrostUnsentMessageObject mo) {
        return addUnsentMessage(mo, true);
    }

    public boolean addUnsentMessage(FrostUnsentMessageObject mo, boolean doCommit) {
        PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            System.out.println("no board for new unsent msg!");
            return false;
        }

        PerstFrostUnsentMessageObject pmo = new PerstFrostUnsentMessageObject(storage, mo);
        
        bo.getUnsentMessagesList().add( pmo );
        if( doCommit ) {
            commitStore();
        }
        return true;
    }
    
    public boolean deleteUnsentMessage(FrostUnsentMessageObject mo) {
        PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            System.out.println("no board for new unsent msg!");
            return false;
        }

        PerstFrostUnsentMessageObject pmo = mo.getPerstFrostUnsentMessageObject();
        if( pmo == null ) {
            System.out.println("no perst unsent msg obj!");
            return false;
        }
        
        bo.getUnsentMessagesList().remove( pmo );
        pmo.deallocate();
        commitStore();
        return true;
    }
    
    /**
     * Updates the CHK keys of fileattachments after upload of attachments.
     */
    public synchronized void updateUnsentMessageFileAttachmentKey(FrostUnsentMessageObject mo, FileAttachment fa) {
        
        PerstFrostBoardObject bo = mo.getBoard().getPerstFrostBoardObject();
        if( bo == null ) {
            System.out.println("no board for new unsent msg update!");
            return;
        }

        PerstFrostUnsentMessageObject pmo = mo.getPerstFrostUnsentMessageObject();
        if( pmo == null ) {
            System.out.println("no perst unsent msg obj for update!");
            return;
        }
        
        pmo.updateUnsentMessageFileAttachmentKey(fa);
        commitStore();
    }
}
