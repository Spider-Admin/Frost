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
import frost.messages.*;

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
    boolean isJunk;
    boolean isFlagged;
    boolean isStarred;
    
    int idLinePos;
    int idLineLen;
    
    Link<PerstFrostArchiveBoardAttachment> boardAttachments;
    Link<PerstFrostArchiveFileAttachment> fileAttachments;
    
    String content;
    String publicKey;
//    String signature;
    
    class PerstFrostArchiveBoardAttachment extends Persistent {
    
        String name;
        String pubKey;
        String privKey;
        String description;
        
        public PerstFrostArchiveBoardAttachment() {}
        
        public PerstFrostArchiveBoardAttachment(BoardAttachment ba) {
            name = ba.getBoardObj().getName();
            pubKey = ba.getBoardObj().getPublicKey();
            privKey = ba.getBoardObj().getPrivateKey();
            description = ba.getBoardObj().getDescription();
        }
    }

    class PerstFrostArchiveFileAttachment extends Persistent {
        String name;
        long size;
        String chkKey;
        
        public PerstFrostArchiveFileAttachment() {}
        
        public PerstFrostArchiveFileAttachment(FileAttachment fa) {
            name = fa.getFilename();
            size = fa.getFileSize();
            chkKey = fa.getKey();
        }
    }
    
    public PerstFrostArchiveMessageObject() {}
    
    public PerstFrostArchiveMessageObject(FrostMessageObject mo, Storage store) {

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
        isJunk = mo.isJunk();
        isFlagged = mo.isFlagged();
        isStarred = mo.isStarred();
        idLinePos = mo.getIdLinePos();
        idLineLen = mo.getIdLineLen();

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

        if( boards != null && boards.size() > 0 ) {
            boardAttachments = store.createLink();
            for( Iterator i=boards.iterator(); i.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)i.next();
                PerstFrostArchiveBoardAttachment pba = new PerstFrostArchiveBoardAttachment(ba);
                boardAttachments.add(pba);
            }
        } else {
            boardAttachments = null;
        }

        if( files != null && files.size() > 0 ) {
            fileAttachments = store.createLink();
            for( Iterator i=files.iterator(); i.hasNext(); ) {
                FileAttachment ba = (FileAttachment)i.next();
                PerstFrostArchiveFileAttachment pba = new PerstFrostArchiveFileAttachment(ba);
                fileAttachments.add(pba);
            }
        } else {
            fileAttachments = null;
        }
        
        content = mo.getContent();
    }

    public void retrieveAttachments(FrostMessageObject mo) {
        if( mo.hasFileAttachments() && fileAttachments != null ) {
            for( Iterator<PerstFrostArchiveFileAttachment> i = fileAttachments.iterator(); i.hasNext(); ) {
                PerstFrostArchiveFileAttachment p = i.next();
                FileAttachment fa = new FileAttachment(p.name, p.chkKey, p.size);
                mo.addAttachment(fa);
            }
        }
        if( mo.hasBoardAttachments() && boardAttachments != null ) {
            for( Iterator<PerstFrostArchiveBoardAttachment> i = boardAttachments.iterator(); i.hasNext(); ) {
                PerstFrostArchiveBoardAttachment p = i.next();
                Board b = new Board(p.name, p.pubKey, p.privKey, p.description);
                BoardAttachment ba = new BoardAttachment(b);
                mo.addAttachment(ba);
            }
        }
    }

    public FrostMessageObject toFrostMessageObject(Board board) {
        FrostMessageObject mo = new FrostMessageObject();
        
        mo.setBoard(board);

        mo.setValid(true);
    
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
        mo.setDeleted(false);
    
        mo.setNew(false);
        mo.setReplied(isReplied);
        mo.setJunk(isJunk);
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
