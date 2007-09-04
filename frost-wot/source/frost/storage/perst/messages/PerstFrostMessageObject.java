/*
  PerstFrostMessageObject.java / Frost
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

import frost.boards.*;
import frost.messages.*;

/**
 * Holds all nessecary data for a FrostMessageObject and allows
 * to be stored in a perst Storage.
 */
public class PerstFrostMessageObject extends Persistent {

    String messageId;
    String inReplyTo; // FIXME: list!

    long dateAndTime;
    int msgIndex;

    String invalidReason;

    String fromName;

    String subject;
    String recipientName;
    int signatureStatus;

    boolean isDeleted;
    boolean isNew;
    boolean isReplied;
    boolean isJunk;
    boolean isFlagged;
    boolean isStarred;
    
    boolean hasBoardAttachments;
    boolean hasFileAttachments;

    int idLinePos;
    int idLineLen;

    @Override
    public void deallocate() {
        MessageContentStorage.inst().deallocateForOid(getOid());
        super.deallocate();
    }
    
    @Override
    public boolean recursiveLoading() {
        return false;
    }

    public PerstFrostMessageObject() {}

    public PerstFrostMessageObject(FrostMessageObject mo, Storage store) {
        
        makePersistent(store); // assign oid

        messageId =  mo.getMessageId();
        // FIXME: inReplyTo vs. inReplyToList: beide speichern?
        inReplyTo = mo.getInReplyTo();

        invalidReason = mo.getInvalidReason();
        dateAndTime = mo.getDateAndTime().getMillis();
        msgIndex = mo.getIndex();
        fromName = mo.getFromName();
        subject = mo.getSubject();
        recipientName = (mo.getRecipientName()!=null&&mo.getRecipientName().length()==0)?null:mo.getRecipientName();
        if( mo.getSignatureV2() == null || mo.getSignatureV2().length() == 0 ) {
            if( mo.getSignatureV1() != null && mo.getSignatureV1().length() > 0 ) {
                MessageContentStorage.inst().addSignatureForOid(getOid(), mo.getSignatureV1());
            }
        } else if( mo.getSignatureV2().length() > 0 ) {
            MessageContentStorage.inst().addSignatureForOid(getOid(), mo.getSignatureV2());
        }
        signatureStatus = mo.getSignatureStatus();
        if( mo.getPublicKey() != null && mo.getPublicKey().length() > 0 ) {
            MessageContentStorage.inst().addPublickeyForOid(getOid(), mo.getPublicKey());
        }
        isDeleted = mo.isDeleted();
        isNew = mo.isNew();
        isReplied = mo.isReplied();
        isJunk = mo.isJunk();
        isFlagged = mo.isFlagged();
        isStarred = mo.isStarred();
        idLinePos = mo.getIdLinePos();
        idLineLen = mo.getIdLineLen();

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

        MessageContentStorage.inst().addAttachmentsForOid(getOid(), boards, files);

        if( boards != null && boards.size() > 0 ) {
            hasBoardAttachments = true;
        } else {
            hasBoardAttachments = false;
        }

        if( files != null && files.size() > 0 ) {
            hasFileAttachments = true;
        } else {
            hasFileAttachments = false;
        }

        MessageContentStorage.inst().addContentForOid(getOid(), mo.getContent());
        
        modify();
    }

    public void retrieveMessageContent(FrostMessageObject mo) {
        mo.setContent(MessageContentStorage.inst().getContentForOid(getOid()));
    }

    public void retrievePublicKey(FrostMessageObject mo) {
        mo.setPublicKey(MessageContentStorage.inst().getPublickeyForOid(getOid()));
    }

    public void retrieveSignature(FrostMessageObject mo) {
        mo.setSignatureV2(MessageContentStorage.inst().getSignatureForOid(getOid()));
    }

    public void retrieveAttachments(FrostMessageObject mo) {
        PerstAttachments pa = MessageContentStorage.inst().getAttachmentsForOid(getOid());
        if( pa != null ) {
            if( pa.getBoardAttachments() != null ) {
                for( Iterator<PerstBoardAttachment> i = pa.getBoardAttachments().iterator(); i.hasNext(); ) {
                    PerstBoardAttachment p = i.next();
                    Board b = new Board(p.name, p.pubKey, p.privKey, p.description);
                    BoardAttachment ba = new BoardAttachment(b);
                    mo.addAttachment(ba);
                }
            }
            if( pa.getFileAttachments() != null ) {
                for( Iterator<PerstFileAttachment> i = pa.getFileAttachments().iterator(); i.hasNext(); ) {
                    PerstFileAttachment p = i.next();
                    FileAttachment fa = new FileAttachment(p.name, p.chkKey, p.size);
                    mo.addAttachment(fa);
                }
            }
        }
    }

    public FrostMessageObject toFrostMessageObject(
            Board board, 
            boolean isValidMessage, 
            boolean withContent, 
            boolean withAttachments) 
    {
        FrostMessageObject mo = new FrostMessageObject();

        // add reference to this perst obj for later updates
        mo.setPerstFrostMessageObject(this);

        mo.setBoard(board);
        // SELECT retrieves only valid messages:
        mo.setValid(isValidMessage);
        mo.setInvalidReason(invalidReason);

        mo.setMessageId(messageId);
        mo.setInReplyTo(inReplyTo);
        mo.setDateAndTime(new DateTime(dateAndTime, DateTimeZone.UTC));
        mo.setIndex(msgIndex);
        mo.setFromName(fromName);
        mo.setSubject(subject);
        if( recipientName != null && recipientName.length() == 0 ) {
            recipientName = null;
        }
        mo.setRecipientName(recipientName);
        mo.setSignatureStatus(signatureStatus);
        mo.setDeleted(isDeleted);

        mo.setNew(isNew);
        mo.setReplied(isReplied);
        mo.setJunk(isJunk);
        mo.setFlagged(isFlagged);
        mo.setStarred(isStarred);

        mo.setHasFileAttachments( hasFileAttachments );
        mo.setHasBoardAttachments( hasBoardAttachments );

        mo.setIdLinePos(idLinePos); // idlinepos
        mo.setIdLineLen(idLineLen); // idlinelen

        if( withContent ) {
            retrieveMessageContent(mo);
        }

        if( withAttachments ) {
            retrieveAttachments(mo);
        }
        return mo;
    }
}
