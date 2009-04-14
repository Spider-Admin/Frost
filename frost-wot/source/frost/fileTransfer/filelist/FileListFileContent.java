/*
  FileListFileContent.java / Frost
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
package frost.fileTransfer.filelist;

import java.util.*;

import frost.fileTransfer.*;
import frost.identities.*;

public class FileListFileContent {

    long timestamp;
    Identity receivedOwner = null;
    LocalIdentity sendOwner = null;
    LinkedList<SharedFileXmlFile> fileList;
    
    public FileListFileContent(long timestamp, Identity owner, LinkedList<SharedFileXmlFile> fileList) {
        this.timestamp = timestamp;
        this.receivedOwner = owner;
        this.fileList = fileList;
    }
    
    public FileListFileContent(long timestamp, LocalIdentity owner, LinkedList<SharedFileXmlFile> fileList) {
        this.timestamp = timestamp;
        this.sendOwner = owner;
        this.fileList = fileList;
    }

    public LinkedList<SharedFileXmlFile> getFileList() {
        return fileList;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public Identity getReceivedOwner() {
        return receivedOwner;
    }
    public LocalIdentity getSendOwner() {
        return sendOwner;
    }
}
