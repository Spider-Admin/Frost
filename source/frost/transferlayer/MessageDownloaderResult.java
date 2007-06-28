/*
  MessageDownloaderResult.java / Frost
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

import frost.messages.*;

/**
 * Contains the result after processing a downloaded message.
 * Either its an error, or a MessageObject with a message state (good,check,...).
 */
public class MessageDownloaderResult {
    /**
     * Constants for content of XML files if the message was dropped for some reason.
     * .xml files with this content are ignored by the gui (in fact all .xml files with length smaller than 20)
     */
    public static final String BROKEN_METADATA = "BrokenMetaData";  // could not load metadata
    public static final String BROKEN_MSG      = "BrokenMsg";       // could not load xml
    public static final String MSG_NOT_FOR_ME  = "NotForMe";        // encrypted for someone other
    public static final String DECRYPT_FAILED  = "DecryptFailed";   // encrypted for me, but decrypt failed
    public static final String INVALID_MSG     = "InvalidMsg";	    // message format validation failed
    public static final String ALLDATANOTFOUND = "AllDataNotFound"; // Not enough data found; some data was fetched but redirect may point to nowhere 
    public static final String MSG_TOO_BIG     = "MsgTooBig";       // msg size exceeded maximum allowed KSK size 

    private MessageXmlFile message = null;
    
    private String errorMessage = null;
    
    public MessageDownloaderResult(String errorMsg) {
        errorMessage = errorMsg;
    }
    
    public MessageDownloaderResult(MessageXmlFile msg) {
        message = msg;
    }
    
    public boolean isSuccess() {
        return (errorMessage == null);
    }

    public boolean isFailure() {
        return (errorMessage != null);
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public MessageXmlFile getMessage() {
        return message;
    }
}
