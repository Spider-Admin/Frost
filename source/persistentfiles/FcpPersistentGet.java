/*
  FcpPersistentGet.java / Frost
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

public class FcpPersistentGet extends FcpPersistentRequest {

    // common
    private boolean isDirect = false;
    private String filename = null;
    private String uri = null;

    // progress
    private int doneBlocks = -1;
    private int requiredBlocks = -1;
    private int totalBlocks = -1;
    private boolean isFinalized = false;

    // success
    private long filesize = -1;

    // failed
    private String redirectURI = null;

    public FcpPersistentGet(NodeMessage msg, String id) {
        super(msg, id);
        // PersistentGet message
        filename = msg.getStringValue("Filename");
        uri = msg.getStringValue("URI");
        String isDirectStr = msg.getStringValue("ReturnType");
        if( isDirectStr.equalsIgnoreCase("disk") ) {
            isDirect = false;
        } else {
            isDirect = true;
        }
    }
    
    public boolean isPut() {
        return false;
    }
    
    public void setProgress(NodeMessage msg) {
        // SimpleProgress message
        doneBlocks = msg.getIntValue("Succeeded");
        requiredBlocks = msg.getIntValue("Required");
        totalBlocks = msg.getIntValue("Total");
        isFinalized = msg.getBoolValue("FinalizedTotal");
        super.setProgress();
    }
    
    public void setSuccess(NodeMessage msg) {
        // DataFound msg
        filesize = msg.getLongValue("DataLength");
        super.setSuccess();
    }
    
    public void setFailed(NodeMessage msg) {
        super.setFailed(msg);
        redirectURI = msg.getStringValue("RedirectURI");
    }

//    public void updateFrostDownloadItem(FrostDownloadItem item) {
//    }

    public int getDoneBlocks() {
        return doneBlocks;
    }

    public String getFilename() {
        return filename;
    }

    public long getFilesize() {
        return filesize;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public int getRequiredBlocks() {
        return requiredBlocks;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public String getUri() {
        return uri;
    }
    
    public String getRedirectURI() {
        return redirectURI;
    }
}
