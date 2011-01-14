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

public class CopyToClipboard {

    private static Clipboard clipboard = null;

    private static class DummyClipboardOwner implements ClipboardOwner {
        public void lostOwnership(final Clipboard tclipboard, final Transferable contents) { }
    }

    private static DummyClipboardOwner dummyClipboardOwner = new DummyClipboardOwner();

    private static Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    public static void copyText(final String text) {
        final StringSelection selection = new StringSelection(text);
        getClipboard().setContents(selection, dummyClipboardOwner);
    }

    /**
     * This method copies the CHK keys and file names of the selected items (if any) to the clipboard.
     * Each ModelItem must implement interface ICopyToClipboardItem.
     */
    public static void copyKeysAndFilenames(final Object[] items) {
        if (items == null || items.length == 0) {
            return;
        }
        final String keyNotAvailableMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
        final StringBuilder textToCopy = new StringBuilder();
        CopyToClipboardItem item;
        for (final Object ditem : items) {
            if( !(ditem instanceof CopyToClipboardItem) ) {
                continue;
            }
            item = (CopyToClipboardItem) ditem;
            appendKeyAndFilename(textToCopy, item.getKey(), item.getFileName(), keyNotAvailableMessage);
            // for a single item don't append newline
            if( items.length > 1 ) {
                textToCopy.append("\n");
            }
        }
        copyText(textToCopy.toString());
    }

    /**
     * This method copies extended information about the selected items (if any) to
     * the clipboard. That information is composed of the filename, the key and
     * the size in bytes.
     * Each ModelItem must implement interface ICopyToClipboardItem.
     */
    public static void copyExtendedInfo(final Object[] items) {
        if (items == null || items.length == 0) {
            return;
        }
        final String keyNotAvailableMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
        final String fileMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.file")+" ";
        final String keyMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.key")+" ";
        final String bytesMessage = Language.getInstance().getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
        final StringBuilder textToCopy = new StringBuilder();
        CopyToClipboardItem item;
        for (final Object ditem : items) {
            if( !(ditem instanceof CopyToClipboardItem) ) {
                continue;
            }
            item = (CopyToClipboardItem) ditem;
            String key = item.getKey();
            if (key == null) {
                key = keyNotAvailableMessage;
            } else {
                // 0.7: append filename if key doesn't contain a / ; otherwise keep key as is
                if( key.indexOf("/") < 0 ) {
                    // append filename
                    key = new StringBuffer().append(key).append("/").append(item.getFileName()).toString();
                }
            }
            String fs;
            if( item.getFileSize() < 0 ) {
                fs = "?";
            } else {
                fs = Long.toString(item.getFileSize());
            }
            textToCopy.append(fileMessage);
            textToCopy.append(item.getFileName()).append("\n");
            textToCopy.append(keyMessage);
            textToCopy.append(key).append("\n");
            textToCopy.append(bytesMessage);
            textToCopy.append(fs).append("\n\n");
        }
        // We remove the additional \n at the end
        textToCopy.deleteCharAt(textToCopy.length() - 1);

        copyText(textToCopy.toString());
    }

    /**
     * Appends key/filename to the StringBuilder.
     * Does not append filename if there is already a filename.
     * Only appends filename for CHK keys.
     */
    private static void appendKeyAndFilename(final StringBuilder textToCopy, String key, final String filename, final String keyNotAvailableMessage) {
        if (key == null) {
            key = keyNotAvailableMessage;
            // no key, its a shared file
            textToCopy.append(filename);
        } else if( key.startsWith("CHK@") ) {
            textToCopy.append(key);
            // CHK, append filename if there is not already a filename in the key
            if( key.indexOf('/') < 0 ) {
                textToCopy.append("/");
                textToCopy.append(filename);
            }
        } else {
            // else for KSK,SSK,USK: don't append filename, key contains all needed information
            textToCopy.append(key);
        }
    }
}
