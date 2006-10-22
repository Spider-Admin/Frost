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
 */
public class MessageInfoPanel extends JPanel implements LanguageListener {

    Language language = Language.getInstance();
    
    private SentMessagesTable sentMessagesTable;
    private UnsentMessagesTable unsendMessagesTable;
    private boolean isShown = false;
    
    private JLabel sentMsgsLabel;
    private JLabel unsendMsgsLabel;
    
    public MessageInfoPanel() {
        super();
        language.addLanguageListener(this);
        initialize();
        refreshLanguage();
    }
    
    private void initialize() {
        setLayout(new GridLayout(2, 1, 5, 5));
        
        JPanel sentMsgsPanel = new JPanel();
        sentMsgsPanel.setLayout(new BorderLayout());
        sentMsgsLabel = new JLabel();
        sentMsgsLabel.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
        sentMsgsPanel.add(sentMsgsLabel, BorderLayout.NORTH);
        
        sentMessagesTable = new SentMessagesTable();
        sentMessagesTable.getScrollPane().setWheelScrollingEnabled(true);
        sentMsgsPanel.add(sentMessagesTable.getScrollPane(), BorderLayout.CENTER);

        JPanel unsendMsgsPanel = new JPanel();
        unsendMsgsPanel.setLayout(new BorderLayout());
        unsendMsgsLabel = new JLabel();
        unsendMsgsLabel.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
        unsendMsgsPanel.add(unsendMsgsLabel, BorderLayout.NORTH);

        unsendMessagesTable = new UnsentMessagesTable();
        unsendMessagesTable.getScrollPane().setWheelScrollingEnabled(true);
        unsendMsgsPanel.add(unsendMessagesTable.getScrollPane(), BorderLayout.CENTER);

        add(sentMsgsPanel);
        add(unsendMsgsPanel);

        // apply a bold font to labels
        Font font = sentMsgsLabel.getFont();
        font = font.deriveFont(Font.BOLD);
        sentMsgsLabel.setFont(font);
        unsendMsgsLabel.setFont(font);
    }
    
    public void saveLayout() {
        sentMessagesTable.saveTableFormat();
        unsendMessagesTable.saveTableFormat();
    }

    /**
     * Fill table model.
     */
    public synchronized void prepareForShow() {
        sentMessagesTable.loadTableModel();
        unsendMessagesTable.loadTableModel();
        isShown = true;
    }
    
    /**
     * Clear table model.
     */
    public synchronized void cleanupAfterLeave() {
        sentMessagesTable.clearTableModel();
        unsendMessagesTable.clearTableModel();
        isShown = false;
    }
    
    public synchronized void addSentMessage(FrostMessageObject mo) {
        if( isShown ) {
            sentMessagesTable.addSentMessage(mo);
        }
    }
    
    public synchronized void addUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsendMessagesTable.addUnsentMessage(mo);
        }
    }

    public synchronized void updateUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsendMessagesTable.updateUnsentMessage(mo);
        }
    }

    public synchronized void removeUnsentMessage(FrostUnsentMessageObject mo) {
        if( isShown ) {
            unsendMessagesTable.removeUnsentMessage(mo);
        }
    }
    
    protected void refreshLanguage() {
        unsendMsgsLabel.setText( language.getString("UnsentMessages.label"));
        sentMsgsLabel.setText( language.getString("SentMessages.label"));
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }
}
