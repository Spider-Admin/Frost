/*
 FrostSharedFileObject.java / Frost
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.garret.perst.IPersistentList;
import org.garret.perst.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fileTransfer.download.FrostDownloadItem;
import frost.identities.Identity;
import frost.storage.perst.filelist.FileListStorage;
import frost.util.DateFun;

public class FrostFileListFileObject extends Persistent {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(FrostFileListFileObject.class);

    private String sha = null;  // SHA of the file
    private long size = 0;      // Filesize
    private String key = null;  // CHK key

    private long lastDownloaded = 0;
    private long lastUploaded = 0;
    private long firstReceived = 0;
    private long lastReceived = 0;

    private long requestLastReceived = 0;  // time when we received the last request for this sha
    private int requestsReceivedCount = 0; // received requests count

    private long requestLastSent = 0;      // time when we sent the last request for this file
    private int requestsSentCount = 0;     // sent requests count

    private boolean isHidden = false;      // user can hide files in search panel

    private IPersistentList<FrostFileListFileObjectOwner> frostFileListFileObjectOwnerList;

    // non-persistent fields
    private transient String displayName = null;
    private transient String displayComment = null;
    private transient String displayKeywords = null;
    private transient int displayRating = -1;
    private transient Boolean hasInfosFromMultipleSources = null;

    private transient List<FrostDownloadItem> listeners;

	public FrostFileListFileObject() {
	}

    /**
     * Used if item is loaded from database.
     */
    public FrostFileListFileObject(
            final String newSha1,
            final long newSize,
            final String newKey,
            final long newLastDownloaded,
            final long newLastUploaded,
            final long newFirstReceived,
            final long newLastReceived,
            final long newRequestLastReceived,
            final int newRequestsReceivedCount,
            final long newRequestLastSent,
            final int newRequestSentCount)
    {
        sha = newSha1;
        size = newSize;
        key = newKey;
        lastDownloaded = newLastDownloaded;
        lastUploaded = newLastUploaded;
        firstReceived = newFirstReceived;
        lastReceived = newLastReceived;
        requestLastReceived = newRequestLastReceived;
        requestsReceivedCount = newRequestsReceivedCount;
        requestLastSent = newRequestLastSent;
        requestsSentCount = newRequestSentCount;
    }

    /**
     * Create instance with data from SharedFilesXmlFile.
     * After creation this item should only be saved to database,
     * this merges the data from this item in case this files is
     * already in the filelist (adds new owner/board).
     */
    public FrostFileListFileObject(final SharedFileXmlFile sfo, final Identity owner, final long timestamp) {

        sha = sfo.getSha();
        size = sfo.getSize().longValue();
        key = sfo.getKey();
        lastDownloaded = 0;
        firstReceived = timestamp;
        lastReceived = timestamp; // set or updated after add

        long lastUploadDate = 0;
        if( sfo.getKey() != null ) {
            if( sfo.getLastUploaded() != null ) {
                try {
					final OffsetDateTime dt = DateFun.parseDate(sfo.getLastUploaded(), DateFun.FORMAT_DATE,
							DateFun.getTimeZone());
					lastUploadDate = DateFun.toMilli(dt);
                } catch(final Throwable t) {
                    logger.error("error parsing file last uploaded date", t);
                }
            }
        }
        lastUploaded = lastUploadDate;

        final FrostFileListFileObjectOwner ob = new FrostFileListFileObjectOwner(
                sfo.getFilename(),
                owner.getUniqueName(),
                sfo.getComment(),
                sfo.getKeywords(),
                sfo.getRating(),
                timestamp,
                lastUploadDate,
                sfo.getKey());

        addFrostFileListFileObjectOwner(ob);
    }

    private IPersistentList<FrostFileListFileObjectOwner> getFrostFileListFileObjectOwnerList() {
        if( frostFileListFileObjectOwnerList == null ) {
            frostFileListFileObjectOwnerList = FileListStorage.inst().createList(); // ATTN: is used without store also!
        }
        return frostFileListFileObjectOwnerList;
    }
    
    public void addFrostFileListFileObjectOwner(final FrostFileListFileObjectOwner v) {
        v.setFileListFileObject(this);
        getFrostFileListFileObjectOwnerList().add(v);
    }
    
    public void deleteFrostFileListFileObjectOwner(final FrostFileListFileObjectOwner v) {
        getFrostFileListFileObjectOwnerList().remove(v);
    }
    
    public Iterator<FrostFileListFileObjectOwner> getFrostFileListFileObjectOwnerIterator() {
        return getFrostFileListFileObjectOwnerList().iterator();
    }
    
    public int getFrostFileListFileObjectOwnerListSize() {
        return getFrostFileListFileObjectOwnerList().size();
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
        notifyListeners();
    }

    public long getLastDownloaded() {
        return lastDownloaded;
    }

    public void setLastDownloaded(final long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }

    public void setLastUploaded(final long lastUploaded) {
        this.lastUploaded = lastUploaded;
        notifyListeners();
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(final long lastReceived) {
        this.lastReceived = lastReceived;
        notifyListeners();
    }

    public String getSha() {
        return sha;
    }

    public long getSize() {
        return size;
    }

    public long getRequestLastReceived() {
        return requestLastReceived;
    }

    public void setRequestLastReceived(final long requestLastReceived) {
        this.requestLastReceived = requestLastReceived;
        notifyListeners();
    }

    public long getRequestLastSent() {
        return requestLastSent;
    }

    public void setRequestLastSent(final long requestLastSent) {
        this.requestLastSent = requestLastSent;
        notifyListeners();
    }

    public int getRequestsReceivedCount() {
        return requestsReceivedCount;
    }

    public void setRequestsReceivedCount(final int requestsReceivedCount) {
        this.requestsReceivedCount = requestsReceivedCount;
    }

    public int getRequestsSentCount() {
        return requestsSentCount;
    }

    public void setRequestsSentCount(final int requestsSentCount) {
        this.requestsSentCount = requestsSentCount;
    }

    public long getFirstReceived() {
        return firstReceived;
    }
    public void setFirstReceived(final long v) {
        firstReceived = v;
    }

    public boolean isHidden() {
        return isHidden;
    }
    public void setHidden(final boolean isHidden) {
        this.isHidden = isHidden;
    }

    static class MutableInt {
        public int i = 0;
        public String name = "";
        public int rating = 0;
    }

    private static MutableInt defaultMutableInt = new MutableInt();

    public String getDisplayName() {
        if( displayName == null ) {
            final List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayName = "(no sources)";
            } else {
                // choose most often used name
                final Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( final FrostFileListFileObjectOwner e : lst ) {
                    MutableInt mi = ht.get( e.getName() );
                    if( mi == null ) {
                        mi = new MutableInt();
                        mi.name = e.getName();
                        mi.i = 1;
                        ht.put(e.getName(), mi);
                    } else {
                        mi.i++;
                    }
                }
                MutableInt bestMi = defaultMutableInt;
                for( final MutableInt mi : ht.values() ) {
                    if( mi.i > bestMi.i ) {
                        bestMi = mi;
                    }
                }
                displayName = bestMi.name;
            }
        }
        return displayName;
    }

    /**
     * @return  true if this file has infos from multiple sources (comments, ratings, keywords)
     */
    public Boolean hasInfosFromMultipleSources() {
        if( hasInfosFromMultipleSources == null ) {
            if( getFrostFileListFileObjectOwnerList().size() > 1 ) {
                int valuesCount = 0;
                for( final FrostFileListFileObjectOwner o : getFrostFileListFileObjectOwnerList() ) {
                    // valuesCount is increased by 1 per FrostFileListFileObjectOwner
                    if( o.getComment() != null && o.getComment().length() > 0 ) {
                        valuesCount++;
                    } else if( o.getKeywords() != null && o.getKeywords().length() > 0 ) {
                        valuesCount++;
                    } else if( o.getRating() > 0 ) {
                        valuesCount++;
                    }
                    // if valuesCount is greater 1 we have at least 2 sources that provide informations
                    if( valuesCount > 1 ) {
                        hasInfosFromMultipleSources = Boolean.TRUE;
                        break;
                    }
                }
                if( hasInfosFromMultipleSources == null ) {
                    hasInfosFromMultipleSources = Boolean.FALSE;
                }
            } else {
                hasInfosFromMultipleSources = Boolean.FALSE;
            }
        }
        return hasInfosFromMultipleSources;
    }

    public String getDisplayComment() {
        if( displayComment == null ) {
            final List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayComment = "(no sources)";
            } else {
                // choose most often used name
                final Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( final FrostFileListFileObjectOwner e : lst ) {
                    final String c = e.getComment();
                    if( c == null || c.length() == 0 ) {
                        continue;
                    }
                    MutableInt mi = ht.get( c );
                    if( mi == null ) {
                        mi = new MutableInt();
                        mi.name = c;
                        mi.i = 1;
                        ht.put(c, mi);
                    } else {
                        mi.i++;
                    }
                }
                MutableInt bestMi = defaultMutableInt;
                for( final MutableInt mi : ht.values() ) {
                    if( mi.i > bestMi.i ) {
                        bestMi = mi;
                    }
                }
                displayComment = bestMi.name;
            }
        }
        return displayComment;
    }

    public String getDisplayKeywords() {
        if( displayKeywords == null ) {
            final List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayKeywords = "(no sources)";
            } else {
                // choose most often used name
                final Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( final FrostFileListFileObjectOwner e : lst ) {
                    final String c = e.getKeywords();
                    if( c == null || c.length() == 0 ) {
                        continue;
                    }
                    MutableInt mi = ht.get( c );
                    if( mi == null ) {
                        mi = new MutableInt();
                        mi.name = c;
                        mi.i = 1;
                        ht.put(c, mi);
                    } else {
                        mi.i++;
                    }
                }
                MutableInt bestMi = defaultMutableInt;
                for( final MutableInt mi : ht.values() ) {
                    if( mi.i > bestMi.i ) {
                        bestMi = mi;
                    }
                }
                displayKeywords = bestMi.name;
            }
        }
        return displayKeywords;
    }

    public int getDisplayRating() {
        if( displayRating < 0 ) {
            final List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                return 0;
            }
            // choose most often used rating
            // choose most often used name
            final int ratings[] = new int[6];
            for( final FrostFileListFileObjectOwner e : lst ) {
                final int r = e.getRating();
                if( r < 1 || r > 5 ) {
                    continue;
                }
                ratings[r]++;
            }
            int bestRating = 0;
            for(int x=1; x < ratings.length; x++ ) {
                if( ratings[x] > bestRating ) {
                    bestRating = x;
                }
            }
            displayRating = bestRating;
        }
        return displayRating;
    }

    public void addListener(final FrostDownloadItem d) {
        if( !getListeners().contains(d) ) {
            getListeners().add(d);
        }
    }
    public void removeListener(final FrostDownloadItem d) {
        if( getListeners().contains(d) ) {
            getListeners().remove(d);
        }
    }
    public List<FrostDownloadItem> getListeners() {
        if( listeners == null ) {
            listeners = new ArrayList<FrostDownloadItem>();
        }
        return listeners;
    }
    private void notifyListeners() {
        for( final FrostDownloadItem dl : getListeners() ) {
            dl.fireValueChanged();
        }
    }
}
