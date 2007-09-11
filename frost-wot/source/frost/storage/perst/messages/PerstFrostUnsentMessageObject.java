/*
  PerstFrostUnsentMessageObject.java / Frost
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

import frost.boards.*;
import frost.messages.*;

public class PerstFrostUnsentMessageObject extends Persistent {

    private String messageId;
    private String inReplyTo;

    private String fromName;

    private String subject;
    private String recipientName;

    private int idLinePos;
    private int idLineLen;

    private Link<PerstFrostUnsentBoardAttachment> boardAttachments;
    private Link<PerstFrostUnsentFileAttachment> fileAttachments;

    private String content;

    private long timeAdded;

    private long sendAfterTime;

    @Override
    public void deallocate() {
        if( boardAttachments != null ) {
            for( final PerstFrostUnsentBoardAttachment a : boardAttachments ) {
                a.deallocate();
            }
            boardAttachments.clear();
            boardAttachments = null;
        }
        if( fileAttachments != null ) {
            for( final PerstFrostUnsentFileAttachment a : fileAttachments ) {
                a.deallocate();
            }
            fileAttachments.clear();
            fileAttachments = null;
        }
        super.deallocate();
    }

    class PerstFrostUnsentBoardAttachment extends Persistent {

        String name;
        String pubKey;
        String privKey;
        String description;

        public PerstFrostUnsentBoardAttachment() {}

        public PerstFrostUnsentBoardAttachment(final BoardAttachment ba) {
            name = ba.getBoardObj().getName();
            pubKey = ba.getBoardObj().getPublicKey();
            privKey = ba.getBoardObj().getPrivateKey();
            description = ba.getBoardObj().getDescription();
        }
    }

    class PerstFrostUnsentFileAttachment extends Persistent {
        String name;
        long size;
        String chkKey;

        public PerstFrostUnsentFileAttachment() {}

        public PerstFrostUnsentFileAttachment(final FileAttachment fa) {
            name = fa.getFilename();
            size = fa.getFileSize();
            chkKey = fa.getKey();
        }
    }

    public PerstFrostUnsentMessageObject() {}

    public PerstFrostUnsentMessageObject(final Storage store, final FrostUnsentMessageObject umo) {
        messageId = umo.getMessageId();
        inReplyTo = umo.getInReplyTo();
        fromName = umo.getFromName();
        subject = umo.getSubject();
        recipientName = umo.getRecipientName();
        idLinePos = umo.getIdLinePos();
        idLineLen = umo.getIdLineLen();
        content = umo.getContent();
        timeAdded = umo.getTimeAdded();
        sendAfterTime = umo.getSendAfterTime();

        final AttachmentList files = umo.getAttachmentsOfType(Attachment.FILE);
        final AttachmentList boards = umo.getAttachmentsOfType(Attachment.BOARD);

        if( boards != null && boards.size() > 0 ) {
            boardAttachments = store.createLink();
            for( final Iterator i=boards.iterator(); i.hasNext(); ) {
                final BoardAttachment ba = (BoardAttachment)i.next();
                final PerstFrostUnsentBoardAttachment pba = new PerstFrostUnsentBoardAttachment(ba);
                boardAttachments.add(pba);
            }
        } else {
            boardAttachments = null;
        }

        if( files != null && files.size() > 0 ) {
            fileAttachments = store.createLink();
            for( final Iterator i=files.iterator(); i.hasNext(); ) {
                final FileAttachment ba = (FileAttachment)i.next();
                final PerstFrostUnsentFileAttachment pba = new PerstFrostUnsentFileAttachment(ba);
                fileAttachments.add(pba);
            }
        } else {
            fileAttachments = null;
        }

        umo.setPerstFrostUnsentMessageObject(this);
    }

    public FrostUnsentMessageObject toFrostUnsentMessageObject(final Board board) {
        final FrostUnsentMessageObject mo = new FrostUnsentMessageObject();

        mo.setPerstFrostUnsentMessageObject(this);

        mo.setMessageId(messageId);
        mo.setInReplyTo(inReplyTo);
        mo.setIdLinePos(idLinePos);
        mo.setIdLineLen(idLineLen);
        mo.setFromName(fromName);
        mo.setSubject(subject);
        mo.setRecipientName(recipientName);
        mo.setContent(content);

        mo.setHasFileAttachments( fileAttachments != null );
        mo.setHasBoardAttachments( boardAttachments != null );

        mo.setTimeAdded( timeAdded );
        mo.setSendAfterTime( sendAfterTime );

        mo.setBoard(board);

        retrieveAttachments(mo);

        return mo;
    }

    public void updateUnsentMessageFileAttachmentKey(final FileAttachment fa) {
        if( fileAttachments == null ) {
            return;
        }

        for( final PerstFrostUnsentFileAttachment ufa : fileAttachments ) {
            if( ufa.name.equals(fa.getInternalFile().getPath()) ) {
                ufa.chkKey = fa.getKey();
                ufa.modify();
                return;
            }
        }
    }

    private void retrieveAttachments(final FrostUnsentMessageObject mo) {
        if( mo.hasFileAttachments() && fileAttachments != null ) {
            for( final PerstFrostUnsentFileAttachment p : fileAttachments ) {
                final FileAttachment fa = new FileAttachment(p.name, p.chkKey, p.size);
                mo.addAttachment(fa);
            }
        }
        if( mo.hasBoardAttachments() && boardAttachments != null ) {
            for( final PerstFrostUnsentBoardAttachment p : boardAttachments ) {
                final Board b = new Board(p.name, p.pubKey, p.privKey, p.description);
                final BoardAttachment ba = new BoardAttachment(b);
                mo.addAttachment(ba);
            }
        }
    }
}
