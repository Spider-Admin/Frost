/*
  MessageDownloader07.java / Frost
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

/**
 * Provides method to download a message. Does all conversions to check and build
 * a valid MessageObject from received files.
 * 
 * ATTN: This class is instanciated only once, so it must behave like a static class.
 *       Use no instance variables!
 */
public class MessageDownloader07 extends MessageDownloader {
    
    private static Logger logger = Logger.getLogger(MessageDownloader07.class.getName());

    /**
     * Tries to download the message, performs all base checkings and decryption.
     * 
     * @return  null if not found, or MessageDownloaderResult if success or error
     */
    public MessageDownloaderResult downloadMessage(
            String downKey,
            int targetIndex,
            boolean fastDownload, 
            String logInfo) {
        
        MessageDownloaderResult mdResult = null;
        
        return mdResult;
    }
}
