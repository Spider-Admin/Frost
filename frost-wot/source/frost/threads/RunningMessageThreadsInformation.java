/*
  RunningMessageThreadsInformation.java / Frost
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
package frost.threads;

/**
 * Contains all information about running message uploads and downloads.
 */
public class RunningMessageThreadsInformation {

    private int uploadingMessages = 0;
    private int unsendMessages = 0;
    private int attachmentsToUploadRemainingCount = 0;

    private int downloadingBoardCount = 0;
    private int runningDownloadThreadCount = 0;

    public RunningMessageThreadsInformation() {
    }

    /**
     * Returns the count of all file attachments of all uploading messages
     * that still wait to be uploaded.
     */
    public int getAttachmentsToUploadRemainingCount() {
        return attachmentsToUploadRemainingCount;
    }
    /**
     * Returns the count of ALL running download threads (of all boards).
     */
    public int getRunningDownloadThreadCount() {
        return runningDownloadThreadCount;
    }
    /**
     * Returns the count of ALL running message uploads (of all boards).
     */
    public int getUploadingMessagesCount() {
        return uploadingMessages;
    }
    /**
     * Returns the count of boards that currently have running download threads.
     */
    public int getDownloadingBoardCount() {
        return downloadingBoardCount;
    }
    /**
     * Returns the count of unsend messages.
     */
    public int getUnsentMessageCount() {
        return unsendMessages;
    }

    public void addToAttachmentsToUploadRemainingCount(final int valueToAdd) {
        this.attachmentsToUploadRemainingCount += valueToAdd;
    }
    public void addToRunningDownloadThreadCount(final int valueToAdd) {
        this.runningDownloadThreadCount += valueToAdd;
    }
    public void addToDownloadingBoardCount(final int valueToAdd) {
        this.downloadingBoardCount += valueToAdd;
    }
    public void setUnsentMessageCount(final int unsendMessages) {
        this.unsendMessages = unsendMessages;
    }
    public void setUploadingMessageCount(final int uploadingMessages) {
        this.uploadingMessages = uploadingMessages;
    }
}
