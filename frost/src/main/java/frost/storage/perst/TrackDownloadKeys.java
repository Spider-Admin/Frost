/*
 TrackDownloadKeys.java / Frost
 Copyright (C) 2010  Frost Project <jtcfrost.sourceforge.net>

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
 * Represents the item for the TrackDownloadKey.
 */
public class TrackDownloadKeys extends Persistent {

	private String chkKey;
	private String fileName;
	private String boardName;
	private long fileSize; 
	private long downloadFinishedTime; // when was this download finished

	// used by perst
	public TrackDownloadKeys() {}

	public TrackDownloadKeys(final String chkKey, final String fileName, 
		final String boardName, long fileSize, long downloadFinishedTime) {
		this.chkKey = chkKey;
		this.fileName = fileName;
		this.boardName = boardName;
		this.fileSize = fileSize;
		this.downloadFinishedTime = downloadFinishedTime;
	}

	public TrackDownloadKeys(final TrackDownloadKeys trackDownloadKey) {
		this.chkKey = trackDownloadKey.chkKey;
		this.fileName = trackDownloadKey.fileName;
		this.boardName = trackDownloadKey.boardName;
		this.fileSize = trackDownloadKey.fileSize;
		this.downloadFinishedTime = trackDownloadKey.downloadFinishedTime;
	}

	public String getChkKey() {
		return chkKey;
	}

	public String getFileName() {
		return fileName;
	}

	public String getBoardName() {
		return boardName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setChkKey(String chkKey) {
		this.chkKey = chkKey;
	}

	public long getDownloadFinishedTime() {
		return downloadFinishedTime;
	}
}
