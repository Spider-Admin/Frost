/*
  FcpPersistentPut.java / Frost
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
package frost.fcp.fcp07.filepersistence;

import frost.fcp.fcp07.*;

public class FcpPersistentPut extends FcpPersistentRequest {

    // common
    private boolean isDirect = false;
    private String filename = null;
    private String uri = null;
    private long fileSize = -1;
    
    // progress
    private int doneBlocks = -1;
    private int totalBlocks = -1;
    private boolean isFinalized = false;

    public FcpPersistentPut(NodeMessage msg, String id) {
        super(msg, id);
        // PersistentPut message
        filename = msg.getStringValue("Filename");
        uri = msg.getStringValue("URI");
        fileSize = msg.getLongValue("DataLength");
        String isDirectStr = msg.getStringValue("UploadFrom");
        if( isDirectStr.equalsIgnoreCase("disk") ) {
            isDirect = false;
        } else {
            isDirect = true;
        }
        if( filename == null ) {
            filename = getIdentifier();
        }
    }
    
    public boolean isPut() {
        return true;
    }
    
    public void setProgress(NodeMessage msg) {
        // SimpleProgress message
        doneBlocks = msg.getIntValue("Succeeded");
        totalBlocks = msg.getIntValue("Total");
        isFinalized = msg.getBoolValue("FinalizedTotal");
        super.setProgress();
    }
    
    public void setSuccess(NodeMessage msg) {
        // PutSuccessful msg
        uri = msg.getStringValue("URI");
        int pos = uri.indexOf("CHK@"); 
        if( pos > -1 ) {
            uri = uri.substring(pos).trim();
        }
        super.setSuccess();
    }
    
    public void setFailed(NodeMessage msg) {
        super.setFailed(msg);
    }

    public int getDoneBlocks() {
        return doneBlocks;
    }

    public String getFilename() {
        return filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public String getUri() {
        return uri;
    }
}
