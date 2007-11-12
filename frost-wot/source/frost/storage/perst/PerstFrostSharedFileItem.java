/*
  PerstFrostSharedFileItem.java / Frost
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

import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import org.garret.perst.*;

import frost.*;
import frost.fileTransfer.sharing.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class PerstFrostSharedFileItem extends Persistent {
    
    public String sha;

    public String filePath;
    public long fileSize;
    public String key;
    
    public String owner;
    public String comment;
    public int rating;
    public String keywords;
    
    public long lastUploaded;  
    public int uploadCount;

    public long refLastSent;
    
    public long requestLastReceived;
    public int requestsReceived;
    
    public long lastModified;
    
    public PerstFrostSharedFileItem() {}

    public PerstFrostSharedFileItem(FrostSharedFileItem sfItem) {
        filePath = sfItem.getFile().getPath();
        fileSize = sfItem.getFileSize();
        key = sfItem.getKey();
        sha = sfItem.getSha();
        owner = sfItem.getOwner();
        comment = sfItem.getComment();
        rating = sfItem.getRating();
        keywords = sfItem.getKeywords();
        lastUploaded = sfItem.getLastUploaded();
        uploadCount = sfItem.getUploadCount();
        refLastSent = sfItem.getRefLastSent();
        requestLastReceived = sfItem.getRequestLastReceived();
        requestsReceived = sfItem.getRequestsReceived();
        lastModified = sfItem.getLastModified();
    }

    public FrostSharedFileItem toFrostSharedFileItem(Logger logger, Language language) {
        boolean fileIsOk = true;
        File file = new File(filePath);

        // report modified/missing shared files only if filesharing is enabled
        if( !Core.frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING) ) {
            if( !file.isFile() ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileNotFound.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileNotFound.text", filePath);
                StartupMessage sm = new StartupMessage(
                        StartupMessage.MessageType.SharedFileNotFound,
                        title,
                        text,
                        JOptionPane.WARNING_MESSAGE,
                        true);
                MainFrame.enqueueStartupMessage(sm);
                logger.severe("Shared file does not exist: "+filePath);
                fileIsOk = false;
            } else if( file.length() != fileSize ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileSizeChanged.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileSizeChanged.text", filePath);
                StartupMessage sm = new StartupMessage(
                        StartupMessage.MessageType.SharedFileSizeChanged,
                        title,
                        text,
                        JOptionPane.WARNING_MESSAGE,
                        true);
                MainFrame.enqueueStartupMessage(sm);
                logger.severe("Size of shared file changed: "+filePath);
                fileIsOk = false;
            } else if( file.lastModified() != lastModified ) {
                String title = language.getString("StartupMessage.sharedFile.sharedFileLastModifiedChanged.title");
                String text = language.formatMessage("StartupMessage.sharedFile.sharedFileLastModifiedChanged.text", filePath);
                StartupMessage sm = new StartupMessage(
                        StartupMessage.MessageType.SharedFileLastModifiedChanged,
                        title,
                        text,
                        JOptionPane.WARNING_MESSAGE,
                        true);
                MainFrame.enqueueStartupMessage(sm);
                logger.severe("Last modified date of shared file changed: "+filePath);
                fileIsOk = false;
            }
        }
        
        FrostSharedFileItem sfItem = new FrostSharedFileItem(
                file,
                fileSize,
                key,
                sha,
                owner,
                comment,
                rating,
                keywords,
                lastUploaded,
                uploadCount,
                refLastSent,
                requestLastReceived,
                requestsReceived,
                lastModified,
                fileIsOk);

        return sfItem;
    }
}
