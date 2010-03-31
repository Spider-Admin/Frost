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

import java.util.*;

import org.garret.perst.*;

import frost.messaging.frost.*;

public class PerstAttachments extends Persistent {

    private IPersistentList<PerstBoardAttachment> boardAttachments;
    private IPersistentList<PerstFileAttachment> fileAttachments;

    public PerstAttachments() {}

    public PerstAttachments(final Storage store, final AttachmentList boards, final AttachmentList files) {

        if( boards != null && boards.size() > 0 ) {
            boardAttachments = store.createScalableList(boards.size());
            for( final Iterator i=boards.iterator(); i.hasNext(); ) {
                final BoardAttachment ba = (BoardAttachment)i.next();
                final PerstBoardAttachment pba = new PerstBoardAttachment(ba);
                boardAttachments.add(pba);
            }
        } else {
            boardAttachments = null;
        }

        if( files != null && files.size() > 0 ) {
            fileAttachments = store.createScalableList(files.size());
            for( final Iterator i=files.iterator(); i.hasNext(); ) {
                final FileAttachment ba = (FileAttachment)i.next();
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
