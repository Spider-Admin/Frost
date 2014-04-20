/*
 FrostFileListFileObjectOwner.java / Frost
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

import org.garret.perst.*;

public class FrostFileListFileObjectOwner extends Persistent {

    protected FrostFileListFileObject fileListFileObject;

    protected String name;
    protected String owner;

    protected String comment;
    protected String keywords;
    protected int rating;

    protected String key;

    protected long lastReceived = 0;
    protected long lastUploaded = 0;

    public FrostFileListFileObjectOwner(
            final String newName,
            final String newOwner,
            final String newComment,
            final String newKeywords,
            final int newRating,
            final long newLastReceived,
            final long newLastUploaded,
            final String newKey)
    {
        name = newName;
        owner = newOwner;
        comment = newComment;
        keywords = newKeywords;
        rating = newRating;
        lastReceived = newLastReceived;
        lastUploaded = newLastUploaded;
        key = newKey;
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

    public long getLastUploaded() {
        return lastUploaded;
    }

    public void setLastReceived(final long lastReceived) {
        this.lastReceived = lastReceived;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setLastUploaded(final long newLastUploaded) {
        this.lastUploaded = newLastUploaded;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(final String keywords) {
        this.keywords = keywords;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(final int rating) {
        this.rating = rating;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public FrostFileListFileObject getFileListFileObject() {
        return fileListFileObject;
    }

    public void setFileListFileObject(final FrostFileListFileObject fileListFileObject) {
        this.fileListFileObject = fileListFileObject;
    }
}
