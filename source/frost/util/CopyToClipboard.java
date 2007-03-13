/*
  CopyToClipboard.java / Frost
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
package frost.util;

import java.awt.*;
import java.awt.datatransfer.*;

import frost.util.gui.translation.*;
import frost.util.model.*;

public class CopyToClipboard {

    private static Clipboard clipboard = null;

    private static class DummyClipboardOwner implements ClipboardOwner {
        public void lostOwnership(Clipboard tclipboard, Transferable contents) { }
    }
    
    private static DummyClipboardOwner dummyClipboardOwner = new DummyClipboardOwner(); 
    
    private static Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }
    
    public static void copyText(String text) {
        StringSelection selection = new StringSelection(text);
        getClipboard().setContents(selection, dummyClipboardOwner);    
    }

    /**
     * This method copies the CHK keys and file names of the selected items (if any) to the clipboard.
     * Each ModelItem must implement interface ICopyToClipboardItem.
     */
    public static void copyKeysAndFilenames(ModelItem[] items) {
        if (items == null && items.length == 0) {
            return;
        }
        String keyNotAvailableMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
        StringBuilder textToCopy = new StringBuilder();
        CopyToClipboardItem item;
        for (ModelItem ditem : items) {
            if( !(ditem instanceof CopyToClipboardItem) ) {
                continue;
            }
            item = (CopyToClipboardItem) ditem;
            appendKeyAndFilename(textToCopy, item.getKey(), item.getFilename(), keyNotAvailableMessage);
            // for a single item don't append newline
            if( items.length > 1 ) {
                textToCopy.append("\n");
            }
        }               
        StringSelection selection = new StringSelection(textToCopy.toString());
        getClipboard().setContents(selection, dummyClipboardOwner);    
    }
    
    /**
     * This method copies extended information about the selected items (if any) to
     * the clipboard. That information is composed of the filename, the key and
     * the size in bytes.
     * Each ModelItem must implement interface ICopyToClipboardItem.
     */
    public static void copyExtendedInfo(ModelItem[] items) {
        if (items == null && items.length == 0) {
            return;
        }
        String keyNotAvailableMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
        String fileMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.file")+" ";
        String keyMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.key")+" ";
        String bytesMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
        StringBuilder textToCopy = new StringBuilder();
        CopyToClipboardItem item;
        for (ModelItem ditem : items) {
            if( !(ditem instanceof CopyToClipboardItem) ) {
                continue;
            }
            item = (CopyToClipboardItem) ditem;
            String key = item.getKey();
            if (key == null) {
                key = keyNotAvailableMessage;
            } else {
                key = key + item.getFilename(); // always use key+filename, also on 0.5. wait for user feedback :)
            }
            String fs;
            if( item.getFileSize() < 0 ) {
                fs = "?";
            } else {
                fs = Long.toString(item.getFileSize());
            }
            textToCopy.append(fileMessage);
            textToCopy.append(item.getFilename()).append("\n");
            textToCopy.append(keyMessage);
            textToCopy.append(key).append("\n");
            textToCopy.append(bytesMessage);
            textToCopy.append(fs).append("\n\n");
        }               
        // We remove the additional \n at the end
        textToCopy.deleteCharAt(textToCopy.length() - 1);
        
        StringSelection selection = new StringSelection(textToCopy.toString());
        getClipboard().setContents(selection, dummyClipboardOwner);    
    }

    /**
     * This method copies the CHK keys of the selected items (if any) to the clipboard.
     * Used only for 0.5 items.
     * Each ModelItem must implement interface ICopyToClipboardItem.
     */
    public static void copyKeys(ModelItem[] items) {
        if (items == null && items.length == 0) {
            return;
        }
        String keyNotAvailableMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
        StringBuilder textToCopy = new StringBuilder();
        CopyToClipboardItem item;
        for (ModelItem ditem : items) {
            if( !(ditem instanceof CopyToClipboardItem) ) {
                continue;
            }
            item = (CopyToClipboardItem) ditem;
            String key = item.getKey();
            if (key == null) {
                key = keyNotAvailableMessage;
            }
            textToCopy.append(key);
            if( items.length > 1 ) {
                textToCopy.append("\n");
            }
        }               
        StringSelection selection = new StringSelection(textToCopy.toString());
        getClipboard().setContents(selection, dummyClipboardOwner);    
    }

    /**
     * Appends key/filename to the StringBuilder.
     * Does not append filename if there is already a filename.
     * Only appends filename for CHK keys.
     */
    private static void appendKeyAndFilename(StringBuilder textToCopy, String key, String filename, String keyNotAvailableMessage) {
        if (key == null) {
            key = keyNotAvailableMessage;
        } 
        textToCopy.append(key);
        if( key.startsWith("CHK@") ) {
            // CHK, append filename if there is not already a filename
            if( key.indexOf('/') < 0 ) {
                textToCopy.append("/");
                textToCopy.append(filename);
            }
        }
        // else for KSK,SSK,USK: don't append filename, key is enough
    }
}
