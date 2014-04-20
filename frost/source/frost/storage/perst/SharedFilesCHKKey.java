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
package frost.storage.perst;

import org.garret.perst.*;

/**
 * Represents the CHK key of a filelist.
 */
public class SharedFilesCHKKey extends Persistent {

    // Question: how to ensure own CHK keys, track them once uploaded if we ever see them again! 
    // Answer: Ignore lost keys, we resend them after some days!
    //         Handle own received keys like any other keys, we don't even know that this was our key.

    private String chkKey;
    private int seenCount; // how often did we receive this CHK in a KSK pointer file
    private long firstSeen; // when did we receive this CHK the first time (time in millis)
    private long lastSeen; // when got we this CHK in a pointer file last time (time in millis)
    private boolean isDownloaded; // did we download this file?
    private boolean isValid; // set after receive of CHK key, don't send invalid keys in KSK pointer file
    private int downloadRetries; // our tries to download this CHK, don't try too often
    private long lastDownloadTryStopTime; // when was the download stopped the last time (time in millis)
    private int sentCount; // how often we send this CHK within a pointer file
    private long lastSent; // time in millis when we sent this CHK the last time within a pointer file

    // used by perst
    public SharedFilesCHKKey() {}
    
    // used during load from database
    public SharedFilesCHKKey(String chkKey, int seenCount, long firstSeen, long lastSeen,
            boolean isDownloaded, boolean isValid, int downloadRetries, long lastDownloadTryStopTime,
            int sentcount, long lastsent) 
    {
        this.chkKey = chkKey;
        this.seenCount = seenCount;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.isDownloaded = isDownloaded;
        this.isValid = isValid;
        this.downloadRetries = downloadRetries;
        this.lastDownloadTryStopTime = lastDownloadTryStopTime;
        this.sentCount = sentcount;
        this.lastSent = lastsent;
    }

    // used if a new CHK key was received in a pointer file
    public SharedFilesCHKKey(String chkKey, long timestamp) {
        this.chkKey = chkKey;
        this.seenCount = 1;
        this.firstSeen = timestamp;
        this.lastSeen = timestamp;
        this.isDownloaded = false;
        this.isValid = false; 
        this.downloadRetries = 0;
        this.lastDownloadTryStopTime = 0;
        this.sentCount = 0;
        this.lastSent = 0;
    }
    
    // used if we uploaded an own new CHK key which must be send inside a pointer file the first time
    public SharedFilesCHKKey(String chkKey) {
        this.chkKey = chkKey;
        this.seenCount = 0;
        this.firstSeen = 0;
        this.lastSeen = 0;
        this.isDownloaded = false;
        this.isValid = true; 
        this.downloadRetries = 0;
        this.lastDownloadTryStopTime = 0;
        this.sentCount = 0;
        this.lastSent = 0;
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
    public void setFirstSeen(long v) {
        firstSeen = v;
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
    public int getSeenCount() {
        return seenCount;
    }
    public void incrementSeenCount() {
        seenCount++;
    }
    public long getLastDownloadTryStopTime() {
        return lastDownloadTryStopTime;
    }
    public void setLastDownloadTryStopTime(long lastDownloadTryStartTime) {
        this.lastDownloadTryStopTime = lastDownloadTryStartTime;
    }
    public int getSentCount() {
        return sentCount;
    }
    public void incrementSentCount() {
        sentCount++;
    }
    public long getLastSent() {
        return lastSent;
    }
    public void setLastSent(long v) {
        lastSent = v;
    }
}
