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

import frost.messages.*;

public class PerstAttachments extends Persistent {

    private IPersistentList<PerstBoardAttachment> boardAttachments;
    private IPersistentList<PerstFileAttachment> fileAttachments;
    
    public PerstAttachments() {}
    
    public PerstAttachments(Storage store, AttachmentList boards, AttachmentList files) {
        
        if( boards != null && boards.size() > 0 ) {
            boardAttachments = store.createScalableList(boards.size());
            for( Iterator i=boards.iterator(); i.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)i.next();
                PerstBoardAttachment pba = new PerstBoardAttachment(ba);
                boardAttachments.add(pba);
            }
        } else {
            boardAttachments = null;
        }

        if( files != null && files.size() > 0 ) {
            fileAttachments = store.createScalableList(files.size());
            for( Iterator i=files.iterator(); i.hasNext(); ) {
                FileAttachment ba = (FileAttachment)i.next();
                PerstFileAttachment pba = new PerstFileAttachment(ba);
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
            for(PerstBoardAttachment ba : getBoardAttachments()) {
                ba.deallocate();
            }
            getBoardAttachments().clear();
            getBoardAttachments().deallocate();
        }
        if( getFileAttachments() != null ) {
            for(PerstFileAttachment fa : getFileAttachments()) {
                fa.deallocate();
            }
            getFileAttachments().clear();
            getFileAttachments().deallocate();
        }
        super.deallocate();
    }
}
