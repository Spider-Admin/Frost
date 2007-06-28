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

import java.util.*;

import org.garret.perst.*;

public class IndexSlot extends Persistent {
    
    private int indexName;
    private long msgDate;

    // holds 1 bit for each msgIndex
    private BitSet wasDownloaded;
    private BitSet wasUploaded;
    
    public IndexSlot() {}

    public IndexSlot(int newIndexName, long newMsgDate) {
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
    
    public String toString() {
        String result = "";
        result += "indexName     = "+indexName+"\n";
        result += "msgDate       = "+msgDate+"\n";
        result += "wasDownloaded = "+wasDownloaded+"\n";
        result += "wasUploaded   = "+wasUploaded+"\n";
        return result;
    }
    
    public void setDownloadSlotUsed(int index) {
        this.wasDownloaded.set(index);
    }
    public void setUploadSlotUsed(int index) {
        this.wasUploaded.set(index);
    }
    
    // find first not downloaded
    public int findFirstDownloadSlot() {
        return wasDownloaded.nextClearBit(0);
    }
    // find next not downloaded
    public int findNextDownloadSlot(int beforeIndex) {
        return wasDownloaded.nextClearBit(beforeIndex+1);
    }
    // check if this index is behind all known indices
    public boolean isDownloadIndexBehindLastSetIndex(int index) {
        int indexBehindLastIndex = Math.max(wasDownloaded.length(), wasUploaded.length());
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
        int index = Math.max(wasDownloaded.length(), wasUploaded.length());
        return index;
    }
    // find next unused
    public int findNextUploadSlot(int beforeIndex) {
        int index = Math.max(wasDownloaded.length(), wasUploaded.length());
        if( index > beforeIndex ) {
            return index;
        } else {
            return beforeIndex + 1;
        }
    }
    
//    public void onStore() {
//        if( indexName < 0 ) return;
//        System.out.println(">>>>>>>>>>STORE>>>");
//        System.out.println(this);
//        System.out.println("<<<<<<<<<<STORE<<<");
//    }

//    public void onLoad() {
//        if( indexName < 0 ) return;
//        System.out.println(">>>>>>>>>>LOAD>>>");
//        System.out.println(this);
//        System.out.println("<<<<<<<<<<LOAD<<<");
//    }

    // testcase
//    public static void main(String[] args) {
//        IndexSlot gis = new IndexSlot(1, 123L);
//        gis.setDownloadSlotUsed(1);
//        gis.setDownloadSlotUsed(2);
//        gis.setDownloadSlotUsed(4);
//        gis.setUploadSlotUsed(3);
//
//        System.out.println(gis);
//        System.out.println("findFirstDownloadSlot: "+gis.findFirstDownloadSlot());
//        System.out.println("findNextDownloadSlot(0): "+gis.findNextDownloadSlot(0));
//        System.out.println("findFirstUploadSlot: "+gis.findFirstUploadSlot());
//        System.out.println("findNextUploadSlot: "+gis.findNextUploadSlot(5));
//        
//        System.out.println("isDownloadIndexBehindLastSetIndex(3): "+gis.isDownloadIndexBehindLastSetIndex(3));
//        System.out.println("isDownloadIndexBehindLastSetIndex(5): "+gis.isDownloadIndexBehindLastSetIndex(5));
//    }
}
