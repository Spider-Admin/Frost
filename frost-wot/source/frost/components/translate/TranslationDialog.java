/*
  TranslationDialog.java / Frost
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
package frost.components.translate;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class TranslationDialog extends JFrame {

    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JList Lkeys = null;
    private JLabel Lsource = null;
    private JTextArea TAsource = null;
    private JLabel Ltranslation = null;
    private JTextArea TAtranslation = null;
    private JPanel Pbuttons = null;
    private JPanel jPanel = null;
    private JButton BdeleteKey = null;
    private JRadioButton RBshowAll = null;
    private JRadioButton RBshowMissing = null;
    private JButton BapplyChanges = null;
    private JButton BrevertChanges = null;
    private JButton Bsave = null;
    private JButton Bclose = null;
    private JScrollPane jScrollPane = null;
    private JScrollPane jScrollPane1 = null;
    private JScrollPane jScrollPane2 = null;
    
    private ButtonGroup radioButtons;
    
    private FrostResourceBundle rootBundle;
    private FrostResourceBundle sourceBundle;
    private String sourceLanguageName;
    private TranslateableFrostResourceBundle targetBundle;
    private String targetLanguageName;
    
    private ImageIcon missingIcon;
    private ImageIcon existingIcon;
    
    /**
     * This is the default constructor
     */
    public TranslationDialog() {
        super();
        initialize();
        
        radioButtons = new ButtonGroup();
        radioButtons.add(getRBshowAll());
        radioButtons.add(getRBshowMissing());
        
        setLocationRelativeTo(MainFrame.getInstance());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        // prepare renderer icons
        MiscToolkit miscToolkit = MiscToolkit.getInstance();
        missingIcon = miscToolkit.getScaledImage("/data/help.png", 10, 10);
        existingIcon = miscToolkit.getScaledImage("/data/trust.gif", 10, 10);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(750, 550);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.weighty = 0.4;
            gridBagConstraints5.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints5.gridx = 0;
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            gridBagConstraints41.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints41.gridy = 5;
            gridBagConstraints41.weightx = 1.0;
            gridBagConstraints41.weighty = 0.2;
            gridBagConstraints41.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints41.gridwidth = 2;
            gridBagConstraints41.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints41.gridx = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints3.gridy = 3;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.weighty = 0.2;
            gridBagConstraints3.gridwidth = 2;
            gridBagConstraints3.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.gridx = 0;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.insets = new java.awt.Insets(5,0,5,5);
            gridBagConstraints7.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints7.gridy = 1;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridwidth = 2;
            gridBagConstraints6.gridy = 6;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints4.insets = new java.awt.Insets(5,5,0,0);
            gridBagConstraints4.gridy = 4;
            Ltranslation = new JLabel();
            Ltranslation.setText("Translation");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new java.awt.Insets(5,5,0,0);
            gridBagConstraints2.gridy = 2;
            Lsource = new JLabel();
            Lsource.setText("Source");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,0);
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Translateable keys");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabel, gridBagConstraints);
            jContentPane.add(Lsource, gridBagConstraints2);
            jContentPane.add(Ltranslation, gridBagConstraints4);
            jContentPane.add(getPbuttons(), gridBagConstraints6);
            jContentPane.add(getJPanel(), gridBagConstraints7);
            jContentPane.add(getJScrollPane(), gridBagConstraints3);
            jContentPane.add(getJScrollPane1(), gridBagConstraints41);
            jContentPane.add(getJScrollPane2(), gridBagConstraints5);
        }
        return jContentPane;
    }

    /**
     * This method initializes Lkeys	
     * 	
     * @return javax.swing.JList	
     */
    private JList getLkeys() {
        if( Lkeys == null ) {
            Lkeys = new JList();
            Lkeys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            Lkeys.setCellRenderer(new ListRenderer());
            Lkeys.setSelectionModel(new DefaultListSelectionModel() {
                public void setSelectionInterval(int index0, int index1) {
                    int oldIndex = getMinSelectionIndex();
                    super.setSelectionInterval(index0, index1);
                    int newIndex = getMinSelectionIndex();
                    if (oldIndex > -1 && oldIndex != newIndex) {
                        // auto apply of changes
                        String oldKey = (String)getLkeys().getModel().getElementAt(oldIndex);
                        if( oldKey != null ) {
                            applyChanges(oldKey, oldIndex);
                        }
                    }
                    keySelectionChanged();
                } 
            });
        }
        return Lkeys;
    }

    /**
     * This method initializes TAsource	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTAsource() {
        if( TAsource == null ) {
            TAsource = new JTextArea();
            TAsource.setPreferredSize(new java.awt.Dimension(0,16));
            TAsource.setLineWrap(true);
            TAsource.setEditable(false);
            TAsource.setWrapStyleWord(true);
            TAsource.setRows(0);
        }
        return TAsource;
    }

    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTAtranslation() {
        if( TAtranslation == null ) {
            TAtranslation = new JTextArea();
            TAtranslation.setPreferredSize(new java.awt.Dimension(0,16));
            TAtranslation.setLineWrap(true);
            TAtranslation.setWrapStyleWord(true);
        }
        return TAtranslation;
    }

    /**
     * This method initializes Pbuttons	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPbuttons() {
        if( Pbuttons == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            Pbuttons = new JPanel();
            Pbuttons.setLayout(flowLayout);
            Pbuttons.add(getBsave(), null);
            Pbuttons.add(getBclose(), null);
        }
        return Pbuttons;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.insets = new java.awt.Insets(0,5,5,5);
            gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints12.gridy = 1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridy = 2;
            gridBagConstraints11.insets = new java.awt.Insets(0,5,0,5);
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints10.gridy = 0;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.insets = new java.awt.Insets(0,5,0,5);
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints9.weighty = 1.0;
            gridBagConstraints9.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints9.gridy = 4;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints8.gridy = 3;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
            jPanel.add(getBdeleteKey(), gridBagConstraints11);
            jPanel.add(getRBshowAll(), gridBagConstraints8);
            jPanel.add(getRBshowMissing(), gridBagConstraints9);
            jPanel.add(getBapplyChanges(), gridBagConstraints10);
            jPanel.add(getBrevertChanges(), gridBagConstraints12);
        }
        return jPanel;
    }

    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTAsource());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jScrollPane1 
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane1() {
        if( jScrollPane1 == null ) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setViewportView(getTAtranslation());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jScrollPane2 
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane2() {
        if( jScrollPane2 == null ) {
            jScrollPane2 = new JScrollPane();
            jScrollPane2.setViewportView(getLkeys());
        }
        return jScrollPane2;
    }

    /**
     * This method initializes BdeleteKey	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBdeleteKey() {
        if( BdeleteKey == null ) {
            BdeleteKey = new JButton();
            BdeleteKey.setText("Delete key");
            BdeleteKey.setMnemonic(java.awt.event.KeyEvent.VK_D);
            BdeleteKey.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String selectedKey = (String)getLkeys().getSelectedValue();
                    deleteKey(selectedKey);
                }
            });
        }
        return BdeleteKey;
    }

    /**
     * This method initializes RBshowAll	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBshowAll() {
        if( RBshowAll == null ) {
            RBshowAll = new JRadioButton();
            RBshowAll.setText("Show all keys");
            RBshowAll.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    showKeysChanged();
                }
            });
        }
        return RBshowAll;
    }

    /**
     * This method initializes RBshowMissing	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBshowMissing() {
        if( RBshowMissing == null ) {
            RBshowMissing = new JRadioButton();
            RBshowMissing.setText("Show missing keys");
            RBshowMissing.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    showKeysChanged();
                }
            });
        }
        return RBshowMissing;
    }

    /**
     * This method initializes BapplyChanges	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBapplyChanges() {
        if( BapplyChanges == null ) {
            BapplyChanges = new JButton();
            BapplyChanges.setText("Apply changes");
            BapplyChanges.setMnemonic(java.awt.event.KeyEvent.VK_A);
            BapplyChanges.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String selectedKey = (String)getLkeys().getSelectedValue();
                    if( selectedKey == null ) {
                        return;
                    }
                    int selectedIx = getLkeys().getSelectedIndex();
                    applyChanges(selectedKey, selectedIx);
                }
            });
        }
        return BapplyChanges;
    }

    /**
     * This method initializes BrevertChanges	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBrevertChanges() {
        if( BrevertChanges == null ) {
            BrevertChanges = new JButton();
            BrevertChanges.setText("Revert changes");
            BrevertChanges.setMnemonic(java.awt.event.KeyEvent.VK_R);
            BrevertChanges.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    revertChanges();
                }
            });
        }
        return BrevertChanges;
    }

    /**
     * This method initializes Bsave	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBsave() {
        if( Bsave == null ) {
            Bsave = new JButton();
            Bsave.setText("Save");
            Bsave.setMnemonic(java.awt.event.KeyEvent.VK_S);
            Bsave.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    saveBundle(false);
                }
            });
        }
        return Bsave;
    }

    /**
     * This method initializes Bclose	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText("Close");
            Bclose.setMnemonic(java.awt.event.KeyEvent.VK_C);
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int answer = JOptionPane.showConfirmDialog(
                            TranslationDialog.this, 
                            "Do you want to save before closing the dialog?", 
                            "Save before close", 
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if( answer == JOptionPane.CANCEL_OPTION ) {
                        return;
                    } else if( answer == JOptionPane.YES_OPTION ) {
                        if( saveBundle(true) == false ) {
                            return; // don't close, error during save
                        }
                    }
                    // update language menu
                    LanguageGuiSupport.getInstance().updateLanguageMenu();
                    // close dialog
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bclose;
    }
    
    public void startDialog(
            FrostResourceBundle rootResBundle,
            FrostResourceBundle sourceResBundle,
            String sourceLangName,
            TranslateableFrostResourceBundle targetResBundle, 
            String targetLangName)
    {
        this.rootBundle = rootResBundle;
        this.sourceBundle = sourceResBundle;
        this.sourceLanguageName = sourceLangName;
        this.targetBundle = targetResBundle;
        this.targetLanguageName = targetLangName;
        
        setTitle("Translate Frost - ("+sourceLanguageName+") into ("+targetLanguageName+")");
        
        Lsource.setText("Source ("+sourceLanguageName+"):");
        Ltranslation.setText("Translation ("+targetLanguageName+"):");
        
        radioButtons.setSelected(getRBshowAll().getModel(), true);

        List allKeys = getAllKeys();
        getLkeys().setModel(new ItemListModel(allKeys));
        
        setVisible(true);
    }
    
    private List<String> getAllKeys() {
        TreeMap<String,String> sorter = new TreeMap<String,String>();
        for(Iterator i=rootBundle.getKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            sorter.put(key, key);
        }
        List<String> itemList = new ArrayList<String>();
        for(Iterator i=sorter.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            itemList.add(key);
        }
        return itemList;
    }
    
    private List<String> getMissingKeys() {
        TreeMap<String,String> sorter = new TreeMap<String,String>();
        for(Iterator i=rootBundle.getKeys().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if( targetBundle.containsKey(key) == false ) {
                sorter.put(key, key);
            }
        }
        List<String> itemList = new ArrayList<String>();
        for(Iterator i=sorter.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            itemList.add(key);
        }
        return itemList;
    }
    
    private boolean saveBundle(boolean quiet) {
        boolean wasOk = targetBundle.saveBundleToFile(targetLanguageName);
        if( wasOk == false ) {
            JOptionPane.showMessageDialog(
                    this, 
                    "Error saving bundle! Check the log file.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        } else if( quiet == false ) {
            JOptionPane.showMessageDialog(
                    this, 
                    "Bundle was successfully saved.",
                    "Save successful", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return wasOk;
    }
    
    private void showKeysChanged() {
        List items;
        if( getRBshowAll().isSelected() ) {
            items = getAllKeys();
        } else {
            items = getMissingKeys();
        }
        getLkeys().setModel(new ItemListModel(items));
    }
    
    private void applyChanges(String selectedKey, int ix) {
        String txt = getTAtranslation().getText().trim();
        if( txt.length() == 0 ) {
            deleteKey(selectedKey);
            return;
        }
        targetBundle.setKey(selectedKey, txt);

        // either update item in list, or remove from list
        if( getRBshowAll().isSelected() ) {
            ((ItemListModel)getLkeys().getModel()).itemChanged(ix);
        } else {
            ((ItemListModel)getLkeys().getModel()).removeItem(ix);
            if( getLkeys().getSelectedValue() == null ) {
                // nothing selected now, clear textfields
                getTAsource().setText("");
                getTAtranslation().setText("");
            }
        }
    }

    private void revertChanges() {
        String selectedKey = (String)getLkeys().getSelectedValue();
        if( selectedKey == null ) {
            return;
        }
        String val;
        if( targetBundle.containsKey(selectedKey) ) {
            val = targetBundle.getString(selectedKey);
        } else {
            val = "";
        }
        getTAtranslation().setText(val);
    }

    private void deleteKey(String selectedKey) {
        if( selectedKey == null ) {
            return;
        }
        targetBundle.removeKey(selectedKey);
        getTAtranslation().setText("");

        int ix = getLkeys().getSelectedIndex();
        ((ItemListModel)getLkeys().getModel()).itemChanged(ix);
    }
    
    private void keySelectionChanged() {
        String selectedKey = (String)getLkeys().getSelectedValue();
        if( selectedKey == null ) {
            getTAsource().setText("");
            getTAtranslation().setText("");
            return;
        }
        String txt = sourceBundle.getString(selectedKey);
        getTAsource().setText(txt);
        
        if( targetBundle.containsKey(selectedKey) ) {
            txt = targetBundle.getString(selectedKey);
        } else {
            txt = "";
        }
        getTAtranslation().setText(txt);
    }

    private class ItemListModel extends AbstractListModel {
        List items;
        
        public ItemListModel(List i) {
            super();
            items = i;
        }
        public int getSize() {
            return items.size();
        }
        public Object getElementAt(int x) {
            return items.get(x);
        }
        public void itemChanged(int ix) {
            fireContentsChanged(this, ix, ix);
        }
        public void removeItem(int ix) {
            items.remove(ix);
            fireIntervalRemoved(this, ix, ix);
        }
    }
    
    private class ListRenderer extends DefaultListCellRenderer {
        public ListRenderer() {
            super();
        }
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String key = (String)value;
            if( targetBundle.containsKey(key) ) {
                setIcon(existingIcon);
            } else {
                setIcon(missingIcon);
            }
            
            return this;
        }
    }
}
