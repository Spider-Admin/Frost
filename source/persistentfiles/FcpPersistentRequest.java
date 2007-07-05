/*
  FcpPersistentRequest.java / Frost
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

import java.util.*;

import frost.fcp.fcp07.*;


public abstract class FcpPersistentRequest extends Observable {

    private String identifier = null;
    
    private int priority = -1;
    
    // failed
    private boolean isFailed = false;
    private String message = null;
    private int code = -1;
    private String codeDesc = null;
    private String extraCodeDesc = null;
    private boolean isFatal;

    // progress
    private boolean isProgressSet = false;

    // success
    private boolean isSuccess = false;

    protected FcpPersistentRequest(NodeMessage msg, String id) {
        identifier = id;
        priority = msg.getIntValue("PriorityClass");
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setRequest(NodeMessage msg) {
        // maybe prio was changed
        priority = msg.getIntValue("PriorityClass");
    }

    public void setFailed(NodeMessage msg) {
        // GetFailed msg
        message = msg.getMessageName();
        code = msg.getIntValue("Code");
        codeDesc = msg.getStringValue("CodeDescription");
        extraCodeDesc = msg.getStringValue("ExtraDescription");
        isFatal = msg.getBoolValue("Fatal");
        isFailed = true;
    }
    
    public abstract boolean isPut();

    protected void setSuccess() {
        isSuccess = true;
    }

    protected void setProgress() {
        isProgressSet = true;
    }

    public int getCode() {
        return code;
    }

    public String getCodeDesc() {
        return codeDesc;
    }

    public String getExtraCodeDesc() {
        return extraCodeDesc;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public boolean isProgressSet() {
        return isProgressSet;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int newp) {
        priority = newp;
    }
}
