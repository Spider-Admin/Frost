/*
  MessageDownloader.java / Frost
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

import frost.fcp.*;

public abstract class MessageDownloader {

    private static Logger logger = Logger.getLogger(MessageDownloader.class.getName());

    MessageDownloader instance = null;
    
    public MessageDownloader inst() {
        if( instance == null ) {
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
                instance = new MessageDownloader05();
            } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
                instance = new MessageDownloader07();
            } else {
                logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            }
        }
        return instance;
    }
    
    /**
     * Tries to download the message, performs all base checkings and decryption.
     * 
     * @return  null if not found, or MessageDownloaderResult if success or error
     */
    public abstract MessageDownloaderResult downloadMessage(
            String downKey,
            int targetIndex,
            boolean fastDownload, 
            String logInfo);
}
