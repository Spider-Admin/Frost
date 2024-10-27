/*
  GlobalIndexSlot.java / Frost
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
package frost.storage.perst;

import java.util.BitSet;

import org.garret.perst.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexSlot extends Persistent {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(IndexSlot.class);

    private int indexName;
    private long msgDate;

    // holds 1 bit for each msgIndex
    private BitSet wasDownloaded;
    private BitSet wasUploaded;

    public IndexSlot() {}

    public IndexSlot(final int newIndexName, final long newMsgDate) {
        indexName = newIndexName;
        msgDate = newMsgDate;
        wasDownloaded = new BitSet();
        wasUploaded = new BitSet();
    }

    public int getIndexName() {
        return indexName;
    }
    public long getMsgDate() {
        return msgDate;
    }

//    public String toString() {
//        String result = "";
//        result += "indexName     = "+indexName+"\n";
//        Board b = Core.getInstance().getMainFrame().getTofTreeModel().getBoardByPrimaryKey(indexName);
//        if( b != null ) {
//            result += "board         = "+b.getName()+"\n";
//        }
//        result += "msgDate       = "+msgDate+"\n";
//        result += "msgDate (fmt) = "+DateFun.getExtendedDateFromMillis(msgDate)+"\n";
//        result += "wasDownloaded = "+wasDownloaded+"\n";
//        result += "wasUploaded   = "+wasUploaded+"\n";
//        return result;
//    }

    public void setDownloadSlotUsed(final int index) {
        this.wasDownloaded.set(index);
    }
    public void setUploadSlotUsed(final int index) {
        this.wasUploaded.set(index);
    }

    // find first not downloaded
    public int findFirstDownloadSlot() {
        return wasDownloaded.nextClearBit(0);
    }
    // find next not downloaded
    public int findNextDownloadSlot(final int beforeIndex) {
        return wasDownloaded.nextClearBit(beforeIndex+1);
    }
    // check if this index is behind all known indices
    public boolean isDownloadIndexBehindLastSetIndex(final int index) {
        final int indexBehindLastIndex = Math.max(wasDownloaded.length(), wasUploaded.length());
        if( index >= indexBehindLastIndex ) {
            return true;
        } else {
            return false;
        }
    }

    // find first unused
    public int findFirstUploadSlot() {
        // find last set index in ul and dl list
        // length() -> Returns the "logical size" of this BitSet:
        // the index of the highest set bit in the BitSet plus one. Returns zero if the BitSet contains no set bits.
        final int index = Math.max(wasDownloaded.length(), wasUploaded.length());
        return index;
    }
    // find next unused
    public int findNextUploadSlot(final int beforeIndex) {
        final int index = Math.max(wasDownloaded.length(), wasUploaded.length());
        if( index > beforeIndex ) {
            return index;
        } else {
            return beforeIndex + 1;
        }
    }

	public void onStore() {
		if (indexName < 0) {
			return;
		}
		logger.debug(">>>>>>>>>>STORE>>>");
		logger.debug("{}", this);
		logger.debug("<<<<<<<<<<STORE<<<");
	}

	public void onLoad() {
		if (indexName < 0) {
			return;
		}
		logger.debug(">>>>>>>>>>LOAD>>>");
		logger.debug("{}", this);
		logger.debug("<<<<<<<<<<LOAD<<<");
	}
}
