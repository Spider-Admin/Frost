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

import java.util.Iterator;

import org.garret.perst.Link;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

import frost.messaging.frost.AttachmentList;
import frost.messaging.frost.BoardAttachment;
import frost.messaging.frost.FileAttachment;
import frost.messaging.frost.FrostMessageObject;
import frost.messaging.frost.boards.Board;
import frost.util.DateFun;

public class PerstFrostArchiveMessageObject extends Persistent {

	private static final long serialVersionUID = 1L;

    @Override
    public boolean recursiveLoading() {
        // load Links manually
        return false;
    }

	private String messageId;
	private String inReplyTo;

	private long dateAndTime;
	private int msgIndex;

	private String fromName;

	private String subject;
	private String recipientName;
	private int signatureStatus;

	private boolean isReplied;
	private boolean isFlagged;
	private boolean isStarred;

	private int idLinePos;
	private int idLineLen;

	private Link<PerstFrostArchiveBoardAttachment> boardAttachments;
	private Link<PerstFrostArchiveFileAttachment> fileAttachments;

	private String content;
	private String publicKey;
	// private String signature;

	public PerstFrostArchiveMessageObject() {
	}

    public PerstFrostArchiveMessageObject(final FrostMessageObject mo, final Storage store) {

        messageId =  mo.getMessageId();
        inReplyTo = mo.getInReplyTo();

		dateAndTime = DateFun.toMilli(mo.getDateAndTime());
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

        final AttachmentList<BoardAttachment> boardAttachmentList = mo.getAttachmentsOfTypeBoard();
        if( boardAttachmentList != null && boardAttachmentList.size() > 0 ) {
        	
            boardAttachments = store.createLink(boardAttachmentList.size());
            
            for( final Iterator<BoardAttachment> i=boardAttachmentList.iterator(); i.hasNext(); ) {
                boardAttachments.add( new PerstFrostArchiveBoardAttachment(i.next()) );
            }
        } else {
            boardAttachments = null;
        }

        final AttachmentList<FileAttachment> fileAttachmentList = mo.getAttachmentsOfTypeFile();
        if( fileAttachmentList != null && fileAttachmentList.size() > 0 ) {
        	
            fileAttachments = store.createLink(fileAttachmentList.size());
            
            for( final Iterator<FileAttachment> i=fileAttachmentList.iterator(); i.hasNext(); ) {
                fileAttachments.add( new PerstFrostArchiveFileAttachment(i.next()) );
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
		mo.setDateAndTime(DateFun.toOffsetDateTime(dateAndTime, DateFun.getTimeZone()));
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
