/*
  MessageInfoPanel.java / Frost
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
package frost.gui;

import java.awt.*;

import javax.swing.*;

import frost.gui.sentmessages.*;
import frost.gui.unsentmessages.*;
import frost.messages.*;
import frost.util.gui.translation.*;

/**
 * This panel shows the sent and unsend messages.
 * Most calls are simply forwarded to the correct panel (sent/unsent).
 */
public class MessageInfoPanel extends JPanel implements LanguageListener {

    Language language = Language.getInstance();
    
    private SentMessagesPanel sentMessagesPanel;
    private UnsentMessagesPanel unsentMessagesPanel;
    
    private boolean isShown = false;

    public MessageInfoPanel() {
        super();
        language.addLanguageListener(this);
        initialize();
        refreshLanguage();
    }
    
    private void initialize() {
        setLayout(new GridLayout(2, 1, 5, 5));

        sentMessagesPanel = new SentMessagesPanel();
        unsentMessagesPanel = new UnsentMessagesPanel();

        add(sentMessagesPanel);
        add(unsentMessagesPanel);
    }
    
    public void saveLayout() {
        sentMessagesPanel.saveTableFormat();
        unsentMessagesPanel.saveTableFormat();
    }

    /**
     * Fill table model.
     */
    public synchronized void prepareForShow() {
        sentMessagesPanel.loadTableModel();
        unsentMessagesPanel.loadTableModel();
        isShown = true;
    }
    
    /**
     * Clear table model.
     */
    public synchronized void cleanupAfterLeave() {
        sentMessagesPanel.clearTableModel();
        unsentMessagesPanel.clearTableModel();
        isShown = false;
    }
    
    public synchronized void addSentMessage(FrostMessageObject mo) {
        if( isShown ) {
            sentMessagesPanel.addSentMessage(mo);
        }
    }
    
    public synchronized void addUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsentMessagesPanel.addUnsentMessage(mo);
        }
    }

    public synchronized void updateUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsentMessagesPanel.updateUnsentMessage(mo);
        }
    }

    public synchronized void removeUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsentMessagesPanel.removeUnsentMessage(mo);
        }
    }
    
    public void updateSentMessagesCount() {
        sentMessagesPanel.refreshLanguage();
    }

    public void updateUnsentMessagesCount() {
        unsentMessagesPanel.refreshLanguage();
    }

    protected void refreshLanguage() {
        sentMessagesPanel.refreshLanguage();
        unsentMessagesPanel.refreshLanguage();
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }
}
