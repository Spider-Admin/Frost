/*
  MessageUploaderCallback.java / Frost
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
package frost.transferlayer;

/**
 * This interface is used by the MessageUploader to ask the GUI
 * for the next free index in the board, because the MessageUploader
 * in the transferlayer has no access to the data directly.
 */
public interface MessageUploaderCallback {
    
    /**
     * Returns the next free index for uploading a message, starting
     * with the provided startIndex.
     * 
     * @param startIndex  index to start with search
     * @return  the next index available for uploads
     */
    public int findNextFreeUploadIndex(int startIndex);
    
    /**
     * Let the GUI compose the upload key for this index.
     */
    public String composeUploadKey(int index);

    /**
     * Let the GUI compose the download key for this index.
     */
    public String composeDownloadKey(int index);

    /**
     * Let the GUI compose the message file path for this index.
     */
    public String composeMsgFilePath(int index);
}
