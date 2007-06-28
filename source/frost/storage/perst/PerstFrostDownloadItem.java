/*
  PerstFrostDownloadItem.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
 * Class to make FrostDownloadItem persistent. 
 * FrostDownloadItem itself extends ModelItem and cannot extend Persistent. 
 */
public class PerstFrostDownloadItem extends Persistent {

    public String fileName;
    public String targetPath;
    public long fileSize; 
    public String key;
    
    public boolean enabled;
    public int state;
    public long downloadAddedTime;
    public long downloadStartedTime;
    public long downloadFinishedTime;
    public int retries;
    public long lastDownloadStopTime;
    public String gqIdentifier;
    
    public String fileListFileSha;
}
