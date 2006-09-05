/*
 SharedFilesCHKKey.java / Frost
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
package frost.storage.database.applayer;

/**
 * Represents the CHK key of a filelist.
 */
class SharedFilesCHKKey {
    
    long primaryKey;
    String chkKey;
    int seenCount;
    long firstSeen;
    long lastSeen; // when got we this CHK in a pointer file last time
    boolean isDownloaded;
    boolean isValid; // set after receive of CHK key, don't send invvalid keys in KSK pointer file
    int downloadRetries; // don't try too often
    long lastDownloadTryStartTime;
    
    public SharedFilesCHKKey(long primKey, String chkKey, int seenCount, long firstSeen, long lastSeen,
            boolean isDownloaded, boolean isValid, int downloadRetries, long lastDownloadTryStartTime) 
    {
        this.primaryKey = primKey;
        this.chkKey = chkKey;
        this.seenCount = seenCount;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.isDownloaded = isDownloaded;
        this.isValid = isValid;
        this.downloadRetries = downloadRetries;
        this.lastDownloadTryStartTime = lastDownloadTryStartTime;
    }
    
    public SharedFilesCHKKey(String chkKey) {
        this.primaryKey = -1; // unset
        this.chkKey = chkKey;
        this.seenCount = 1;
        this.firstSeen = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.isDownloaded = false;
        this.isValid = false; 
        this.downloadRetries = 0;
        this.lastDownloadTryStartTime = 0;
    }
    public String getChkKey() {
        return chkKey;
    }
    public int getDownloadRetries() {
        return downloadRetries;
    }
    public void incDownloadRetries() {
        downloadRetries++;
    }
    public long getFirstSeen() {
        return firstSeen;
    }
    public boolean isDownloaded() {
        return isDownloaded;
    }
    public void setDownloaded(boolean v) {
        isDownloaded = v;
    }
    public boolean isValid() {
        return isValid;
    }
    public void setValid(boolean v) {
        isValid = v;
    }
    public long getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(long lseen) {
        lastSeen = lseen;
    }
    public long getPrimaryKey() {
        return primaryKey;
    }
    public int getSeenCount() {
        return seenCount;
    }
    public void incrementSeenCount() {
        seenCount++;
    }
    public long getLastDownloadTryStartTime() {
        return lastDownloadTryStartTime;
    }
    public void setLastDownloadTryStartTime(long lastDownloadTryStartTime) {
        this.lastDownloadTryStartTime = lastDownloadTryStartTime;
    }
}
