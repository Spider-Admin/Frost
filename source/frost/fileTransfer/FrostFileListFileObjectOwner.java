/*
 FrostSharedFileObjectOwner.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer;

public class FrostFileListFileObjectOwner {

    protected long refkey;
    protected String name;
    protected String owner;
    
    protected String comment;
    protected String keywords;
    protected int rating;
    
    protected String key;
    
    protected long lastReceived = 0;
    protected long lastUploaded = 0;
    
    public FrostFileListFileObjectOwner(
            long newRefkey,
            String newName,
            String newOwner,
            String newComment,
            String newKeywords,
            int newRating,
            long newLastReceived,
            long newLastUploaded,
            String newKey) 
    {
        refkey = newRefkey;
        name = newName;
        owner = newOwner;
        comment = newComment;
        keywords = newKeywords;
        rating = newRating;
        lastReceived = newLastReceived;
        lastUploaded = newLastUploaded;
        key = newKey;
    }

    public FrostFileListFileObjectOwner(
            String newName,
            String newOwner,
            String newComment,
            String newKeywords,
            int newRating,
            long newLastReceived,
            long newLastUploaded,
            String newKey) 
    {
        this(0, newName, newOwner, newComment, newKeywords, newRating, newLastReceived, newLastUploaded, newKey);
    }

    public long getLastReceived() {
        return lastReceived;
    }
    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
    }
    public long getRefkey() {
        return refkey;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }

    public void setRefkey(long refkey) {
        this.refkey = refkey;
    }

    public void setLastReceived(long lastReceived) {
        this.lastReceived = lastReceived;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUploaded(long newLastUploaded) {
        this.lastUploaded = newLastUploaded;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
