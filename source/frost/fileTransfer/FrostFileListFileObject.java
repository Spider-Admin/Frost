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

import java.util.*;
import java.util.logging.*;

import org.garret.perst.*;
import org.joda.time.*;

import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.identities.*;
import frost.storage.perst.filelist.*;
import frost.util.*;

public class FrostFileListFileObject extends Persistent {

    private static final Logger logger = Logger.getLogger(FrostFileListFileObject.class.getName());

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

    private IPersistentList<FrostFileListFileObjectOwner> frostFileListFileObjectOwnerList;

    // non-persistent fields
    private transient String displayName = null;
    private transient String displayComment = null;
    private transient String displayKeywords = null;
    private transient int displayRating = -1;
    private transient Boolean hasInfosFromMultipleSources = null;

    private transient List<FrostDownloadItem> listeners;
    
    /**
     * Used if item is loaded from database.
     */
    public FrostFileListFileObject(
            String newSha1, 
            long newSize, 
            String newKey, 
            long newLastDownloaded, 
            long newLastUploaded, 
            long newFirstReceived,
            long newLastReceived,
            long newRequestLastReceived,
            int newRequestsReceivedCount,
            long newRequestLastSent,
            int newRequestSentCount) 
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
    public FrostFileListFileObject(SharedFileXmlFile sfo, Identity owner, long timestamp) {
        
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
                    DateTime dt = DateFun.FORMAT_DATE.parseDateTime(sfo.getLastUploaded());
                    lastUploadDate = dt.getMillis();
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, " error parsing file last uploaded date", t);
                }
            }
        }
        lastUploaded = lastUploadDate;
        
        FrostFileListFileObjectOwner ob = new FrostFileListFileObjectOwner(
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

    public IPersistentList<FrostFileListFileObjectOwner> getFrostFileListFileObjectOwnerList() {
        if( frostFileListFileObjectOwnerList == null ) {
            frostFileListFileObjectOwnerList = FileListStorage.inst().createList();
        }
        return frostFileListFileObjectOwnerList;
    }
    public void addFrostFileListFileObjectOwner(FrostFileListFileObjectOwner v) {
        v.setFileListFileObject(this);
        getFrostFileListFileObjectOwnerList().add(v);
    }
    public void deleteFrostFileListFileObjectOwner(FrostFileListFileObjectOwner v) {
        getFrostFileListFileObjectOwnerList().remove(v);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        notifyListeners();
    }

    public long getLastDownloaded() {
        return lastDownloaded;
    }

    public void setLastDownloaded(long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }

    public void setLastUploaded(long lastUploaded) {
        this.lastUploaded = lastUploaded;
        notifyListeners();
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(long lastReceived) {
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

    public void setRequestLastReceived(long requestLastReceived) {
        this.requestLastReceived = requestLastReceived;
        notifyListeners();
    }

    public long getRequestLastSent() {
        return requestLastSent;
    }

    public void setRequestLastSent(long requestLastSent) {
        this.requestLastSent = requestLastSent;
        notifyListeners();
    }

    public int getRequestsReceivedCount() {
        return requestsReceivedCount;
    }

    public void setRequestsReceivedCount(int requestsReceivedCount) {
        this.requestsReceivedCount = requestsReceivedCount;
    }

    public int getRequestsSentCount() {
        return requestsSentCount;
    }

    public void setRequestsSentCount(int requestsSentCount) {
        this.requestsSentCount = requestsSentCount;
    }
    
    public long getFirstReceived() {
        return firstReceived;
    }
    public void setFirstReceived(long v) {
        firstReceived = v;
    }
    
    static class MutableInt {
        public int i = 0;
        public String name = "";
        public int rating = 0;
    }
    
    private static MutableInt defaultMutableInt = new MutableInt();

    public String getDisplayName() {
        if( displayName == null ) {
            List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayName = "(no sources)";
            } else {
                // choose most often used name
                Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( FrostFileListFileObjectOwner e : lst ) {
                    MutableInt mi = (MutableInt)ht.get( e.getName() );
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
                for( MutableInt mi : ht.values() ) {
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
                for( FrostFileListFileObjectOwner o : getFrostFileListFileObjectOwnerList() ) {
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
            List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayComment = "(no sources)";
            } else {
                // choose most often used name
                Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( FrostFileListFileObjectOwner e : lst ) {
                    String c = e.getComment();
                    if( c == null || c.length() == 0 ) {
                        continue;
                    }
                    MutableInt mi = (MutableInt)ht.get( c );
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
                for( MutableInt mi : ht.values() ) {
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
            List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayKeywords = "(no sources)";
            } else {
                // choose most often used name
                Hashtable<String,MutableInt> ht = new Hashtable<String,MutableInt>();
                for( FrostFileListFileObjectOwner e : lst ) {
                    String c = e.getKeywords();
                    if( c == null || c.length() == 0 ) {
                        continue;
                    }
                    MutableInt mi = (MutableInt)ht.get( c );
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
                for( MutableInt mi : ht.values() ) {
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
            List<FrostFileListFileObjectOwner> lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                return 0;
            }
            // choose most often used rating
            // choose most often used name
            int ratings[] = new int[6];
            for( FrostFileListFileObjectOwner e : lst ) {
                int r = e.getRating();
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
    
    public void addListener(FrostDownloadItem d) {
        if( !getListeners().contains(d) ) {
            getListeners().add(d);
        }
    }
    public void removeListener(FrostDownloadItem d) {
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
        for(Iterator<FrostDownloadItem> i=getListeners().iterator(); i.hasNext(); ) {
            FrostDownloadItem dl = i.next();
            dl.fireValueChanged();
        }
    }
    
    public boolean updateFromOtherFileListFile(FrostFileListFileObject fof) {
        // file is already in FILELIST table, maybe add new FILEOWNER and update fields
        // maybe update oldSfo
        boolean doUpdate = false;
        if( getKey() == null && fof.getKey() != null ) {
            setKey(fof.getKey()); doUpdate = true;
        } else if( getKey() != null && fof.getKey() != null ) {
            // fix to replace 0.7 keys before 1010 on the fly
            if( FreenetKeys.isOld07ChkKey(getKey()) && !FreenetKeys.isOld07ChkKey(fof.getKey()) ) {
                // replace old chk key with new one
                setKey(fof.getKey()); doUpdate = true;
            }
        }
        if( getFirstReceived() > fof.getFirstReceived() ) {
            setFirstReceived(fof.getFirstReceived()); doUpdate = true;
        }
        if( getLastReceived() < fof.getLastReceived() ) {
            setLastReceived(fof.getLastReceived()); doUpdate = true;
        }
        if( getLastUploaded() < fof.getLastUploaded() ) {
            setLastUploaded(fof.getLastUploaded()); doUpdate = true;
        }
        if( getLastDownloaded() < fof.getLastDownloaded() ) {
            setLastDownloaded(fof.getLastDownloaded()); doUpdate = true;
        }
        if( getRequestLastReceived() < fof.getRequestLastReceived() ) {
            setRequestLastReceived(fof.getRequestLastReceived()); doUpdate = true;
        }
        if( getRequestLastSent() < fof.getRequestLastSent() ) {
            setRequestLastSent(fof.getRequestLastSent()); doUpdate = true;
        }
        if( getRequestsReceivedCount() < fof.getRequestsReceivedCount() ) {
            setRequestsReceivedCount(fof.getRequestsReceivedCount()); doUpdate = true;
        }
        if( getRequestsSentCount() < fof.getRequestsSentCount() ) {
            setRequestsSentCount(fof.getRequestsSentCount()); doUpdate = true;
        }
        
        for(Iterator<FrostFileListFileObjectOwner> i=fof.getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
            
            FrostFileListFileObjectOwner obNew = i.next();
            
            // check if we have an owner object for this sharer
            FrostFileListFileObjectOwner obOld = null;
            for(FrostFileListFileObjectOwner o : getFrostFileListFileObjectOwnerList()) {
                if( o.getOwner().equals(obNew.getOwner()) ) {
                    obOld = o;
                    break;
                }
            }
            
            if( obOld == null ) {
                // add new
                addFrostFileListFileObjectOwner(obNew);
                doUpdate = true;
            } else {
                // update existing
                if( obOld.getLastReceived() < obNew.getLastReceived() ) {

                    obOld.setLastReceived(obNew.getLastReceived());
                    obOld.setName(obNew.getName());
                    obOld.setLastUploaded(obNew.getLastUploaded());
                    obOld.setComment(obNew.getComment());
                    obOld.setKeywords(obNew.getKeywords());
                    obOld.setRating(obNew.getRating());
                    obOld.setKey(obNew.getKey());
                    
                    obOld.modify();
                }
            }
        }

        if( doUpdate ) {
            modify();
        }
        
        return doUpdate;
    }
}
