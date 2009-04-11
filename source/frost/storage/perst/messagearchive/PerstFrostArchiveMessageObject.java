/*
  PerstFrostMessageArchiveObject.java / Frost
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

import org.garret.perst.*;
import org.joda.time.*;

import frost.boards.*;
import frost.messaging.frost.*;

public class PerstFrostArchiveMessageObject extends Persistent {

    @Override
    public boolean recursiveLoading() {
        // load Links manually
        return false;
    }

    String messageId;
    String inReplyTo;

    long dateAndTime;
    int msgIndex;

    String fromName;

    String subject;
    String recipientName;
    int signatureStatus;

    boolean isReplied;
    boolean isFlagged;
    boolean isStarred;

    int idLinePos;
    int idLineLen;

    Link<PerstFrostArchiveBoardAttachment> boardAttachments;
    Link<PerstFrostArchiveFileAttachment> fileAttachments;

    String content;
    String publicKey;
//    String signature;

    public PerstFrostArchiveMessageObject() {}

    public PerstFrostArchiveMessageObject(final FrostMessageObject mo, final Storage store) {

        messageId =  mo.getMessageId();
        inReplyTo = mo.getInReplyTo();

        dateAndTime = mo.getDateAndTime().getMillis();
        msgIndex = mo.getIndex();
        fromName = mo.getFromName();
        subject = mo.getSubject();
        recipientName = (mo.getRecipientName()!=null&&mo.getRecipientName().length()==0)?null:mo.getRecipientName();
//        if( mo.getSignatureV2() == null || mo.getSignatureV2().length() == 0 ) {
//            if( mo.getSignatureV1() != null && mo.getSignatureV1().length() > 0 ) {
//                signature = mo.getSignatureV1();
//            }
//        } else if( mo.getSignatureV2().length() > 0 ) {
//            signature = mo.getSignatureV2();
//        }
        signatureStatus = mo.getSignatureStatus();
        if( mo.getPublicKey() != null && mo.getPublicKey().length() > 0 ) {
            publicKey = mo.getPublicKey();
        }
        isReplied = mo.isReplied();
        isFlagged = mo.isFlagged();
        isStarred = mo.isStarred();
        idLinePos = mo.getIdLinePos();
        idLineLen = mo.getIdLineLen();

        final AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);
        if( boards != null && boards.size() > 0 ) {
            boardAttachments = store.createLink(boards.size());
            for( final Iterator i=boards.iterator(); i.hasNext(); ) {
                final BoardAttachment ba = (BoardAttachment)i.next();
                boardAttachments.add( new PerstFrostArchiveBoardAttachment(ba) );
            }
        } else {
            boardAttachments = null;
        }

        final AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        if( files != null && files.size() > 0 ) {
            fileAttachments = store.createLink(files.size());
            for( final Iterator i=files.iterator(); i.hasNext(); ) {
                final FileAttachment ba = (FileAttachment)i.next();
                fileAttachments.add( new PerstFrostArchiveFileAttachment(ba) );
            }
        } else {
            fileAttachments = null;
        }

        content = mo.getContent();
    }

    public void retrieveAttachments(final FrostMessageObject mo) {
        if( mo.hasFileAttachments() && fileAttachments != null ) {
            for( final PerstFrostArchiveFileAttachment p : fileAttachments ) {
                final FileAttachment fa = new FileAttachment(p.name, p.chkKey, p.size);
                mo.addAttachment(fa);
            }
        }
        if( mo.hasBoardAttachments() && boardAttachments != null ) {
            for( final PerstFrostArchiveBoardAttachment p : boardAttachments ) {
                final Board b = new Board(p.name, p.pubKey, p.privKey, p.description);
                final BoardAttachment ba = new BoardAttachment(b);
                mo.addAttachment(ba);
            }
        }
    }

    public FrostMessageObject toFrostMessageObject(final Board board) {
        final FrostMessageObject mo = new FrostMessageObject();

        mo.setBoard(board);

        mo.setValid(true);

        mo.setNew(false);
        mo.setJunk(false);
        mo.setDeleted(false);

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

        mo.setReplied(isReplied);
        mo.setFlagged(isFlagged);
        mo.setStarred(isStarred);

        mo.setContent(content);
        mo.setPublicKey(publicKey);

        mo.setHasFileAttachments( fileAttachments != null );
        mo.setHasBoardAttachments( boardAttachments != null );

        mo.setIdLinePos(idLinePos); // idlinepos
        mo.setIdLineLen(idLineLen); // idlinelen

        retrieveAttachments(mo);

        return mo;
    }
}
