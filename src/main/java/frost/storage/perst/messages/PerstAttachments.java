/*
  PerstAttachments.java / Frost
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

import java.util.Iterator;

import org.garret.perst.IPersistentList;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

import frost.messaging.frost.AttachmentList;
import frost.messaging.frost.BoardAttachment;
import frost.messaging.frost.FileAttachment;

public class PerstAttachments extends Persistent {

    private IPersistentList<PerstBoardAttachment> boardAttachments;
    private IPersistentList<PerstFileAttachment> fileAttachments;

    public PerstAttachments() {}

    public PerstAttachments(final Storage store, final AttachmentList<BoardAttachment> boardAttachmentList, final AttachmentList<FileAttachment> fileAttachmentList) {

        if( boardAttachmentList != null && boardAttachmentList.size() > 0 ) {
            boardAttachments = store.createScalableList(boardAttachmentList.size());
            final Iterator<BoardAttachment> boardAttachmentIterator = boardAttachmentList.iterator();
            while(  boardAttachmentIterator.hasNext() ) {
                final PerstBoardAttachment pba = new PerstBoardAttachment(boardAttachmentIterator.next());
                boardAttachments.add(pba);
            }
        } else {
            boardAttachments = null;
        }

        if( fileAttachmentList != null && fileAttachmentList.size() > 0 ) {
            fileAttachments = store.createScalableList(fileAttachmentList.size());
            final Iterator<FileAttachment> fileAttachmentIterator = fileAttachmentList.iterator();
            while(  fileAttachmentIterator.hasNext() ) {
                final FileAttachment ba = (FileAttachment)fileAttachmentIterator.next();
                final PerstFileAttachment pba = new PerstFileAttachment(ba);
                fileAttachments.add(pba);
            }
        } else {
            fileAttachments = null;
        }
    }

    public IPersistentList<PerstBoardAttachment> getBoardAttachments() {
        return boardAttachments;
    }

    public IPersistentList<PerstFileAttachment> getFileAttachments() {
        return fileAttachments;
    }

    @Override
    public void deallocate() {
        if( getBoardAttachments() != null ) {
            for(final PerstBoardAttachment ba : getBoardAttachments()) {
                ba.deallocate();
            }
            getBoardAttachments().clear();
            getBoardAttachments().deallocate();
        }
        if( getFileAttachments() != null ) {
            for(final PerstFileAttachment fa : getFileAttachments()) {
                fa.deallocate();
            }
            getFileAttachments().clear();
            getFileAttachments().deallocate();
        }
        super.deallocate();
    }
}
