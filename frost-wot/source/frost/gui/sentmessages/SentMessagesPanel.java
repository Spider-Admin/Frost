/*
  SentMessagesPanel.java / Frost
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
package frost.gui.sentmessages;

import java.awt.*;

import javax.swing.*;

import frost.messages.*;
import frost.util.gui.translation.*;

public class SentMessagesPanel extends JPanel {

    Language language = Language.getInstance();

    private JLabel sentMsgsLabel;
    private SentMessagesTable sentMessagesTable;

    public SentMessagesPanel() {
        super();
        initialize();
    }
    
    public void addSentMessage(FrostMessageObject i) {
        sentMessagesTable.addSentMessage(i);
    }

    public void clearTableModel() {
        sentMessagesTable.clearTableModel();
    }

    public void loadTableModel() {
        sentMessagesTable.loadTableModel();
        refreshLanguage();
    }

    public void saveTableFormat() {
        sentMessagesTable.saveTableFormat();
    }
    
    public void refreshLanguage() {
        sentMsgsLabel.setText( language.getString("SentMessages.label") + " ("+sentMessagesTable.getRowCount()+")");
    }

    private void initialize() {
        
        this.setLayout(new BorderLayout());
        sentMsgsLabel = new JLabel();
        sentMsgsLabel.setBorder(BorderFactory.createEmptyBorder(2,4,2,2));
        this.add(sentMsgsLabel, BorderLayout.NORTH);
        
        sentMessagesTable = new SentMessagesTable();
        sentMessagesTable.getScrollPane().setWheelScrollingEnabled(true);
        this.add(sentMessagesTable.getScrollPane(), BorderLayout.CENTER);
        
        Font font = sentMsgsLabel.getFont();
        font = font.deriveFont(Font.BOLD);
        sentMsgsLabel.setFont(font);
    }
}
