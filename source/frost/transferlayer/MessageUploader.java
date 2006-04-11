/*
  MessageUploader.java / Frost
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

import java.util.logging.*;

import javax.swing.*;

import frost.fcp.*;
import frost.identities.*;
import frost.messages.*;

public abstract class MessageUploader {
    
    private static Logger logger = Logger.getLogger(MessageUploader.class.getName());

    private static MessageUploader instance = null;
    
    public static MessageUploader inst() {
        if( instance == null ) {
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
                instance = new MessageUploader05();
            } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
                instance = new MessageUploader07();
            } else {
                logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            }
        }
        return instance;
    }

    /**
     * Prepares and uploads the message.
     * Returns -1 if upload failed (unsentMessageFile should stay in unsent msgs folder in this case)
     * or returns a value >= 0 containing the final index where the message was uploaded to. 
     */
    public abstract int uploadMessage(
            MessageObject message, 
            Identity encryptForRecipient,
            MessageUploaderCallback callback,
            JFrame parentFrame,
            String logBoardName);
}
