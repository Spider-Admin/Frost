/*
  UnsentMessagestable.java / Frost
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
package frost.gui.unsentmessages;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.*;
import frost.gui.*;
import frost.messages.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class UnsentMessagesTable extends SortedModelTable {
    
    private UnsentMessagesTableModel tableModel;
    private UnsentMessagesTableFormat tableFormat;

    private PopupMenu popupMenu = null;
    private Language language = Language.getInstance();
    
    public UnsentMessagesTable() {
        this(new UnsentMessagesTableModel(new UnsentMessagesTableFormat()));
    }
    
    private UnsentMessagesTable(UnsentMessagesTableModel m) {
        super(m);
        tableModel = m;
        tableFormat = (UnsentMessagesTableFormat)m.getTableFormat();
        
        setupTableFont();
        getTable().setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        
        Listener l = new Listener();
        getTable().addMouseListener(l);
        getScrollPane().addMouseListener(l);
    }
    
    public void addUnsentMessage(FrostUnsentMessageObject i) {
        tableModel.addFrostUnsentMessageObject(i);
        MainFrame.getInstance().getUnsentMessagesPanel().updateUnsentMessagesCount();
    }

    public void removeUnsentMessage(FrostUnsentMessageObject i) {
        tableModel.removeFrostUnsentMessageObject(i);
        MainFrame.getInstance().getUnsentMessagesPanel().updateUnsentMessagesCount();
    }

    public void updateUnsentMessage(FrostUnsentMessageObject i) {
        tableModel.updateFrostUnsentMessageObject(i);
    }

    public void saveTableFormat() {
        tableFormat.saveTableLayout();
    }
    
    public void loadTableModel() {
        tableModel.loadTableModel();
        MainFrame.getInstance().getUnsentMessagesPanel().updateUnsentMessagesCount();
    }
    
    public void clearTableModel() {
        tableModel.clear();
    }
    
    private PopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu();
            language.addLanguageListener(popupMenu);
        }
        return popupMenu;
    }

    private void setupTableFont() {
        String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
        int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
        int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
            font = new Font("SansSerif", fontStyle, fontSize);
        }
        getTable().setFont(font);
    }

    private void tableDoubleClick(MouseEvent e) {
        
        int row = getTable().rowAtPoint(e.getPoint());
        if( row > -1 ) {
            ModelItem item = getItemAt(row); //It may be null
            if (item != null) {
                FrostMessageObject sm = ((UnsentMessagesTableItem) item).getFrostUnsentMessageObject();
                MessageWindow messageWindow = new MessageWindow( 
                        MainFrame.getInstance(), 
                        sm, 
                        MainFrame.getInstance().getUnsentMessagesPanel().getSize(),
                        false); // no reply button for unsend messages
                messageWindow.setVisible(true);
            }
        }        
    }

    private class Listener extends MouseAdapter implements MouseListener {

        public Listener() {
            super();
        }
    
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (e.getSource() == getTable()) {
                    tableDoubleClick(e);
                }
            } else if (e.isPopupTrigger()) {
                if ((e.getSource() == getTable())
                    || (e.getSource() == getScrollPane())) {
                    showTablePopupMenu(e);
                }
            }
        }
    
        public void mouseReleased(MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
    
                if ((e.getSource() == getTable())
                    || (e.getSource() == getScrollPane())) {
                    showTablePopupMenu(e);
                }
            }
        }
    
        private void showTablePopupMenu(MouseEvent e) {
            // select row where rightclick occurred if row under mouse is NOT selected 
            Point p = e.getPoint();
            int y = getTable().rowAtPoint(p);
            if( y < 0 ) {
                return;
            }
            if( !getTable().getSelectionModel().isSelectedIndex(y) ) {
                getTable().getSelectionModel().setSelectionInterval(y, y);
            }
            getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class PopupMenu extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

        JMenuItem deleteItem = new JMenuItem();
    
        public PopupMenu() {
            super();
            initialize();
        }
    
        private void initialize() {
            refreshLanguage();
    
            deleteItem.addActionListener(this);
        }
    
        private void refreshLanguage() {
            deleteItem.setText(language.getString("UnsentMessages.table.popup.deleteMessage"));
        }
    
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == deleteItem) {
                deleteSelectedMessages();
            }
        }
        
        private void deleteSelectedMessages() {
            ModelItem[] selectedItems = getSelectedItems();
            if( selectedItems.length == 0 ) {
                return;
            }
            int answer; 
            if( selectedItems.length == 1 ) {
                answer = JOptionPane.showConfirmDialog( 
                        MainFrame.getInstance(), 
                        language.getString("UnsentMessages.confirmDeleteOneMessageDialog.text"), 
                        language.getString("UnsentMessages.confirmDeleteOneMessageDialog.title"), 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
            } else {
                answer = JOptionPane.showConfirmDialog( 
                        MainFrame.getInstance(), 
                        language.formatMessage("UnsentMessages.confirmDeleteMessagesDialog.text", Integer.toString(selectedItems.length)), 
                        language.getString("UnsentMessages.confirmDeleteMessagesDialog.title"), 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
            }
            
            if( answer != JOptionPane.YES_OPTION ) {
                return;
            }
            
            FrostUnsentMessageObject failedItem = tableModel.deleteItems(selectedItems);
            if( failedItem != null ) {
                JOptionPane.showMessageDialog(
                        MainFrame.getInstance(),
                        language.getString("UnsentMessages.deleteNotPossibleDialog.text"), 
                        language.getString("UnsentMessages.deleteNotPossibleDialog.title"), 
                        JOptionPane.ERROR_MESSAGE);
            }
            MainFrame.getInstance().getUnsentMessagesPanel().updateUnsentMessagesCount();
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
        public void show(Component invoker, int x, int y) {
            removeAll();
    
            ModelItem[] selectedItems = getSelectedItems();
    
            if (selectedItems.length == 0) {
                return;
            }

            deleteItem.setEnabled(true);
            add(deleteItem);
            
            if (selectedItems.length == 1) {
                UnsentMessagesTableItem item = (UnsentMessagesTableItem) selectedItems[0];
                if( item.getFrostUnsentMessageObject().getCurrentUploadThread() != null ) {
                    deleteItem.setEnabled(false); 
                }
            }
    
            super.show(invoker, x, y);
        }
    }
}
