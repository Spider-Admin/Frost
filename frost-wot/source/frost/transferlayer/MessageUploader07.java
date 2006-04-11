/*
  MessageUploader07.java / Frost
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

import frost.identities.*;
import frost.messages.*;

/**
 * Provides method to upload a message. Does all conversions to build a file for uploading.
 * 
 * ATTN: This class is instanciated only once, so it must behave like a static class.
 *       Use no instance variables!
 */
public class MessageUploader07 extends MessageUploader {

    private static Logger logger = Logger.getLogger(MessageUploader07.class.getName());

    /**
     * Prepares and uploads the message.
     * Returns -1 if upload failed (unsentMessageFile should stay in unsent msgs folder in this case)
     * or returns a value >= 0 containing the final index where the message was uploaded to. 
     */
    public int uploadMessage(
            MessageObject message, 
            Identity encryptForRecipient,
            MessageUploaderCallback callback,
            JFrame parentFrame,
            String logBoardName) {
        
        return -1;
    }
}
