/*
  StartupMessage.java / Frost
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
package frost.util.gui;

import java.util.*;

import javax.swing.*;

import frost.util.gui.translation.*;

public class StartupMessage {

    public enum MessageType {
        SharedFileNotFound,
        SharedFileSizeChanged,
        SharedFileLastModifiedChanged,
        UploadFileNotFound,
        UploadFileSizeChanged,
        BoardsWithObsoleteKeysFound
    }

    protected MessageType messageType;

    protected String title;
    protected String text;
    protected int dialogType;

    protected boolean allowIgnore; // if true allow to ignore msgs with same messageType

    public StartupMessage(
            final MessageType newMessageType,
            final String newTitle,
            final String newText,
            final int newDialogType,
            final boolean newAllowIgnore)
    {
        messageType = newMessageType;
        title = newTitle;
        text = newText;
        dialogType = newDialogType;
        allowIgnore = newAllowIgnore;
    }

    /**
     * Override this method if you want to do special things.
     */
    public void display(final JFrame parent) {

        if( isMessageTypeIgnored(getMessageType()) ) {
            return;
        }

        if( isAllowIgnore() ) {
            // user can choose to not show any more msgs of this type
            final Language language = Language.getInstance();
            final String okStr = language.getString("Common.ok");
            final String ignoreStr = language.getString("Common.ignoreMessagesOfThisType");
            final Object[] options = { okStr, ignoreStr };
            final int answer = JOptionPane.showOptionDialog(
                    parent,
                    getText(),
                    getTitle(),
                    JOptionPane.DEFAULT_OPTION,
                    getDialogType(),
                    null,
                    options,
                    options[0]);

            if( answer == 1 ) {
                StartupMessage.setMessageTypeIgnored(getMessageType());
            }
        } else {
            // user cannot choose to not show any more msgs of this type
            JOptionPane.showMessageDialog(parent, getText(), getTitle(), getDialogType());
        }
    }

    public boolean isAllowIgnore() {
        return allowIgnore;
    }
    public int getDialogType() {
        return dialogType;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public String getText() {
        return text;
    }
    public String getTitle() {
        return title;
    }

    ////////////////////////////////////////////////////////////////////////////
    // STATIC //////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private static HashSet<MessageType> ignoredMessageTypes = new HashSet<MessageType>();

    public static boolean isMessageTypeIgnored(final MessageType msgType) {
        return ignoredMessageTypes.contains(msgType);
    }

    public static void setMessageTypeIgnored(final MessageType msgType) {
        ignoredMessageTypes.add(msgType);
    }

    /**
     * Cleanup, makes this class unusable.
     */
    public static void cleanup() {
        ignoredMessageTypes.clear();
        ignoredMessageTypes = null;
    }
}
