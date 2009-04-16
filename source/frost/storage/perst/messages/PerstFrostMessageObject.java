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

import org.garret.perst.*;
import org.joda.time.*;

import frost.messaging.frost.*;
import frost.messaging.frost.boards.*;

/**
 * Holds all nessecary data for a FrostMessageObject and allows
 * to be stored in a perst Storage.
 */
public class PerstFrostMessageObject extends Persistent {

    String messageId;
    String inReplyTo;

    long dateAndTime;
    int msgIndex;

    String invalidReason; // if set, the message is invalid

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

    public PerstFrostMessageObject(final FrostMessageObject mo, final Storage store, final boolean useTransaction) {

        if( useTransaction ) {
            MessageContentStorage.inst().beginExclusiveThreadTransaction();
        }
        try {
            makePersistent(store); // assign oid

            messageId =  mo.getMessageId();
            // FIXME: inReplyTo vs. inReplyToList: save both?
            inReplyTo = mo.getInReplyTo();

            // in toFrostMessageObject() we use only invalidReason as indicator for valid or invalid,
            // so we ensure that invalidReason is correctly set
            if( mo.isValid() == false ) {
                invalidReason = mo.getInvalidReason();
                if( invalidReason == null || invalidReason.length() == 0 ) {
                    invalidReason = "AutoSet";
                }
            } else {
                invalidReason = null;
            }
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

            final AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
            final AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

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
        } finally {
            if( useTransaction ) {
                MessageContentStorage.inst().endThreadTransaction();
            }
        }
    }

    public void retrieveMessageContent(final FrostMessageObject mo) {
        mo.setContent(MessageContentStorage.inst().getContentForOid(getOid()));
    }

    public void retrievePublicKey(final FrostMessageObject mo) {
        mo.setPublicKey(MessageContentStorage.inst().getPublickeyForOid(getOid()));
    }

    public void retrieveSignature(final FrostMessageObject mo) {
        mo.setSignatureV2(MessageContentStorage.inst().getSignatureForOid(getOid()));
    }

    public void retrieveAttachments(final FrostMessageObject mo) {
        final PerstAttachments pa = MessageContentStorage.inst().getAttachmentsForOid(getOid());
        if( pa != null ) {
            if( pa.getBoardAttachments() != null ) {
                for( final PerstBoardAttachment p : pa.getBoardAttachments() ) {
                    final Board b = new Board(p.name, p.pubKey, p.privKey, p.description);
                    final BoardAttachment ba = new BoardAttachment(b);
                    mo.addAttachment(ba);
                }
            }
            if( pa.getFileAttachments() != null ) {
                for( final PerstFileAttachment p : pa.getFileAttachments() ) {
                    final FileAttachment fa = new FileAttachment(p.name, p.chkKey, p.size);
                    mo.addAttachment(fa);
                }
            }
        }
    }

    public DateTime getDateTime() {
        return new DateTime(dateAndTime, DateTimeZone.UTC);
    }

    public FrostMessageObject toFrostMessageObject(
            final Board board,
            final boolean withContent,
            final boolean withAttachments)
    {
        final FrostMessageObject mo = new FrostMessageObject();

        // add reference to this perst obj for later updates
        mo.setPerstFrostMessageObject(this);

        mo.setBoard(board);

        if( invalidReason != null && invalidReason.length() > 0 ) {
            mo.setValid(false);
            mo.setInvalidReason(invalidReason);
        } else {
            mo.setValid(true);
        }

        mo.setMessageId(messageId);
        mo.setInReplyTo(inReplyTo);
        mo.setDateAndTime(getDateTime());
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
