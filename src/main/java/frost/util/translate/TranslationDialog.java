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
package frost.util.translate;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import frost.MainFrame;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.FrostResourceBundle;
import frost.util.gui.translation.LanguageGuiSupport;
import frost.util.gui.translation.TranslateableFrostResourceBundle;

public class TranslationDialog extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JList<String> Lkeys = null;
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

    private final ButtonGroup radioButtons;

	private transient FrostResourceBundle rootBundle;
	private transient FrostResourceBundle sourceBundle;
	private String sourceLanguageName;
	private transient TranslateableFrostResourceBundle targetBundle;
	private String targetLanguageName;

    private final ImageIcon missingIcon;
    private final ImageIcon existingIcon;

    public TranslationDialog() {
        super();
        initialize();

        radioButtons = new ButtonGroup();
        radioButtons.add(getRBshowAll());
        radioButtons.add(getRBshowMissing());

        setLocationRelativeTo(MainFrame.getInstance());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        // prepare renderer icons
        missingIcon = MiscToolkit.getScaledImage("/data/help.png", 10, 10);
        existingIcon = MiscToolkit.getScaledImage("/data/trust.gif", 10, 10);
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
     * @return JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            final GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = GridBagConstraints.BOTH;
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.weighty = 0.4;
            gridBagConstraints5.insets = new Insets(5,5,5,5);
            gridBagConstraints5.gridx = 0;
            final GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            gridBagConstraints41.fill = GridBagConstraints.BOTH;
            gridBagConstraints41.gridy = 5;
            gridBagConstraints41.weightx = 1.0;
            gridBagConstraints41.weighty = 0.2;
            gridBagConstraints41.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints41.gridwidth = 2;
            gridBagConstraints41.insets = new Insets(5,5,5,5);
            gridBagConstraints41.gridx = 0;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = GridBagConstraints.BOTH;
            gridBagConstraints3.gridy = 3;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.weighty = 0.2;
            gridBagConstraints3.gridwidth = 2;
            gridBagConstraints3.insets = new Insets(5,5,5,5);
            gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints3.gridx = 0;
            final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.insets = new Insets(5,0,5,5);
            gridBagConstraints7.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints7.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints7.gridy = 1;
            final GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridwidth = 2;
            gridBagConstraints6.gridy = 6;
            final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints4.insets = new Insets(5,5,0,0);
            gridBagConstraints4.gridy = 4;
            Ltranslation = new JLabel();
            Ltranslation.setText("Translation");
            final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new Insets(5,5,0,0);
            gridBagConstraints2.gridy = 2;
            Lsource = new JLabel();
            Lsource.setText("Source");
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new Insets(5,5,0,0);
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

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        if( e.getID() == WindowEvent.WINDOW_CLOSING ) {
            closeDialog();
        }
        super.processWindowEvent(e);
    }

    /**
     * This method initializes Lkeys
     *
     * @return JList
     */
    private JList<String> getLkeys() {
        if( Lkeys == null ) {
            Lkeys = new JList<String>();
            Lkeys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            Lkeys.setCellRenderer(new ListRenderer());
			Lkeys.setSelectionModel(new DefaultListSelectionModel() {

				private static final long serialVersionUID = 1L;

				@Override
                public void setSelectionInterval(final int index0, final int index1) {
                    final int oldIndex = getMinSelectionIndex();
                    super.setSelectionInterval(index0, index1);
                    final int newIndex = getMinSelectionIndex();
                    if ((oldIndex > -1) && (oldIndex != newIndex)) {
                        // auto apply of changes
                        final String oldKey = getLkeys().getModel().getElementAt(oldIndex);
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
     * @return JTextArea
     */
    private JTextArea getTAsource() {
        if( TAsource == null ) {
            TAsource = new JTextArea();
            TAsource.setPreferredSize(new Dimension(0,16));
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
     * @return JTextArea
     */
    private JTextArea getTAtranslation() {
        if( TAtranslation == null ) {
            TAtranslation = new JTextArea();
            TAtranslation.setPreferredSize(new Dimension(0,16));
            TAtranslation.setLineWrap(true);
            TAtranslation.setWrapStyleWord(true);
        }
        return TAtranslation;
    }

    /**
     * This method initializes Pbuttons
     *
     * @return JPanel
     */
    private JPanel getPbuttons() {
        if( Pbuttons == null ) {
            final FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
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
     * @return JPanel
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            final GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.insets = new Insets(0,5,5,5);
            gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints12.gridy = 1;
            final GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridy = 2;
            gridBagConstraints11.insets = new Insets(0,5,0,5);
            gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
            final GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.insets = new Insets(5,5,5,5);
            gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints10.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints10.gridy = 0;
            final GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.insets = new Insets(0,5,0,5);
            gridBagConstraints9.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints9.weighty = 1.0;
            gridBagConstraints9.fill = GridBagConstraints.NONE;
            gridBagConstraints9.gridy = 4;
            final GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.insets = new Insets(5,5,0,5);
            gridBagConstraints8.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints8.gridy = 3;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
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
     * @return JScrollPane
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
     * @return JScrollPane
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
     * @return JScrollPane
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
     * @return JButton
     */
    private JButton getBdeleteKey() {
        if( BdeleteKey == null ) {
            BdeleteKey = new JButton();
            BdeleteKey.setText("Delete key");
            BdeleteKey.setMnemonic(KeyEvent.VK_D);
            BdeleteKey.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    final String selectedKey = getLkeys().getSelectedValue();
                    deleteKey(selectedKey);
                }
            });
        }
        return BdeleteKey;
    }

    /**
     * This method initializes RBshowAll
     *
     * @return JRadioButton
     */
    private JRadioButton getRBshowAll() {
        if( RBshowAll == null ) {
            RBshowAll = new JRadioButton();
            RBshowAll.setText("Show all keys");
            RBshowAll.addItemListener(new ItemListener() {
                public void itemStateChanged(final ItemEvent e) {
                    showKeysChanged();
                }
            });
        }
        return RBshowAll;
    }

    /**
     * This method initializes RBshowMissing
     *
     * @return JRadioButton
     */
    private JRadioButton getRBshowMissing() {
        if( RBshowMissing == null ) {
            RBshowMissing = new JRadioButton();
            RBshowMissing.setText("Show missing keys");
            RBshowMissing.addItemListener(new ItemListener() {
                public void itemStateChanged(final ItemEvent e) {
                    showKeysChanged();
                }
            });
        }
        return RBshowMissing;
    }

    /**
     * This method initializes BapplyChanges
     *
     * @return JButton
     */
    private JButton getBapplyChanges() {
        if( BapplyChanges == null ) {
            BapplyChanges = new JButton();
            BapplyChanges.setText("Apply changes");
            BapplyChanges.setMnemonic(KeyEvent.VK_A);
            BapplyChanges.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    final String selectedKey = getLkeys().getSelectedValue();
                    if( selectedKey == null ) {
                        return;
                    }
                    final int selectedIx = getLkeys().getSelectedIndex();
                    applyChanges(selectedKey, selectedIx);
                }
            });
        }
        return BapplyChanges;
    }

    /**
     * This method initializes BrevertChanges
     *
     * @return JButton
     */
    private JButton getBrevertChanges() {
        if( BrevertChanges == null ) {
            BrevertChanges = new JButton();
            BrevertChanges.setText("Revert changes");
            BrevertChanges.setMnemonic(KeyEvent.VK_R);
            BrevertChanges.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    revertChanges();
                }
            });
        }
        return BrevertChanges;
    }

    /**
     * This method initializes Bsave
     *
     * @return JButton
     */
    private JButton getBsave() {
        if( Bsave == null ) {
            Bsave = new JButton();
            Bsave.setText("Save");
            Bsave.setMnemonic(KeyEvent.VK_S);
            Bsave.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    saveBundle(false);
                }
            });
        }
        return Bsave;
    }

    /**
     * This method initializes Bclose
     *
     * @return JButton
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText("Close");
            Bclose.setMnemonic(KeyEvent.VK_C);
            Bclose.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    closeDialog();
                }
            });
        }
        return Bclose;
    }

    private void closeDialog() {
        final int answer = JOptionPane.showConfirmDialog(
                this,
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

    public void startDialog(
            final FrostResourceBundle rootResBundle,
            final FrostResourceBundle sourceResBundle,
            final String sourceLangName,
            final TranslateableFrostResourceBundle targetResBundle,
            final String targetLangName)
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

        final List<String> allKeys = getAllKeys();
        getLkeys().setModel(new ItemListModel(allKeys));

        setVisible(true);
    }

    private List<String> getAllKeys() {
        final TreeMap<String,String> sorter = new TreeMap<String,String>();
        for( final String string : rootBundle.getKeys() ) {
            final String key = string;
            sorter.put(key, key);
        }
        final List<String> itemList = new ArrayList<String>();
        for( final String string : sorter.keySet() ) {
            final String key = string;
            itemList.add(key);
        }
        return itemList;
    }

    private List<String> getMissingKeys() {
        final TreeMap<String,String> sorter = new TreeMap<String,String>();
        for( final String string : rootBundle.getKeys() ) {
            final String key = string;
            if( targetBundle.containsKey(key) == false ) {
                sorter.put(key, key);
            }
        }
        final List<String> itemList = new ArrayList<String>();
        for( final String string : sorter.keySet() ) {
            final String key = string;
            itemList.add(key);
        }
        return itemList;
    }

    private boolean saveBundle(final boolean quiet) {
        final boolean wasOk = targetBundle.saveBundleToFile(targetLanguageName);
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
        final List<String> items;
        if( getRBshowAll().isSelected() ) {
            items = getAllKeys();
        } else {
            items = getMissingKeys();
        }
        getLkeys().setModel(new ItemListModel(items));
    }

    private void applyChanges(final String selectedKey, final int ix) {
        final String txt = getTAtranslation().getText().trim();
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
        final String selectedKey = getLkeys().getSelectedValue();
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

    private void deleteKey(final String selectedKey) {
        if( selectedKey == null ) {
            return;
        }
        targetBundle.removeKey(selectedKey);
        getTAtranslation().setText("");

        final int ix = getLkeys().getSelectedIndex();
        ((ItemListModel)getLkeys().getModel()).itemChanged(ix);
    }

    private void keySelectionChanged() {
        final String selectedKey = getLkeys().getSelectedValue();
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

	private class ItemListModel extends AbstractListModel<String> {

		private static final long serialVersionUID = 1L;

		private transient List<String> items;

        public ItemListModel(final List<String> i) {
            super();
            items = i;
        }

        public int getSize() {
            return items.size();
        }

        public String getElementAt(final int x) {
            return items.get(x);
        }

        public void itemChanged(final int ix) {
            fireContentsChanged(this, ix, ix);
        }

        public void removeItem(final int ix) {
            items.remove(ix);
            fireIntervalRemoved(this, ix, ix);
        }
    }

	private class ListRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		public ListRenderer() {
            super();
        }

        @Override
        public Component getListCellRendererComponent(
				final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            final String key = (String)value;
            if( targetBundle.containsKey(key) ) {
                setIcon(existingIcon);
            } else {
                setIcon(missingIcon);
            }

            return this;
        }
    }
}
