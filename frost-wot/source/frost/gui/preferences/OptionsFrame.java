/*
  OptionsFrame.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
--------------------------------------------------------------------------
  DESCRIPTION:
  This file contains the whole 'Options' dialog. It first reads the
  actual config from properties file, and on 'OK' it saves all
  settings to the properties file and informs the caller to reload
  this file.
*/
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import frost.SettingsClass;
import frost.storage.StorageException;
import frost.util.gui.translation.Language;

/**
 * Main options frame.
 */
public class OptionsFrame extends JDialog implements ListSelectionListener {

    /**
     * A simple helper class to store JPanels and their name into a JList.
     */
    class ListBoxData {
        String name;
        JPanel panel;
        public ListBoxData(String n, JPanel p) {
            panel = p;
            name = n;
        }
        public JPanel getPanel() {
            return panel;
        }
        public String toString() {
            return name;
        }
    }

    private static Logger logger = Logger.getLogger(OptionsFrame.class.getName());

    private SettingsClass frostSettings;
    private Language language;

    private JPanel buttonPanel = null; // OK / Cancel
    private boolean checkBlock;
    private boolean checkBlockBody;

    // this vars hold some settings from start of dialog to the end.
    // then its checked if the settings are changed by user
    private boolean checkDisableRequests;
    private boolean checkHideBadMessages;
    private boolean checkHideCheckMessages;
    private boolean checkHideObserveMessages;
    private String checkMaxMessageDisplay;
    private boolean checkSignedOnly;

    private boolean checkShowDeletedMessages;
    private boolean showColoredRows;

    private JPanel contentAreaPanel = null;
    private DisplayPanel displayPanel = null;
    private SkinPanel skinPanel = null;
    private DownloadPanel downloadPanel = null;

    boolean exitState;

    private JPanel mainPanel = null;
    private MiscPanel miscPanel = null;
    private NewsPanel newsPanel = null;
    private News2Panel news2Panel = null;
    private News3Panel news3Panel = null;
    private ExpirationPanel expirationPanel = null;
    private JList optionsGroupsList = null;
    private JPanel optionsGroupsPanel = null;
    private SearchPanel searchPanel = null;
    boolean shouldReloadMessages = false;

    private boolean shouldRemoveDummyReqFiles = false;

    private UploadPanel uploadPanel = null;

    /**
     * Constructor, reads init file and inits the gui.
     * @param parent
     * @param settings
     */
    public OptionsFrame(Frame parent, SettingsClass settings) {
        super(parent);
        setModal(true);

        language = Language.getInstance();

        frostSettings = settings;
        setDataElements();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        // set initial selection (also sets panel)
        optionsGroupsList.setSelectedIndex(0);

        // final layouting
        pack();

        // center dialog on parent
        setLocationRelativeTo(parent);
    }

    /**
     * Close window and do not save settings
     */
    private void cancel() {
        exitState = false;

        if (skinPanel != null) {
            //If the skin panel has been used, undo any possible skin preview
            skinPanel.cancel();
        }

        dispose();
    }

    /**
     * cancelButton Action Listener (Cancel)
     * @param e
     */
    private void cancelButton_actionPerformed(ActionEvent e) {
        cancel();
    }

    /**
     * Computes the maximum width and height of the various options panels.
     * Returns Dimension with max. x and y that is needed.
     * Gets all panels from the ListModel of the option groups list.
     * @param m
     * @return
     */
    protected Dimension computeMaxSize(ListModel m) {
        if (m == null || m.getSize() == 0)
            return null;
        int maxX = -1;
        int maxY = -1;
        // misuse a JDialog to determine the panel size before showing
        JDialog dlgdummy = new JDialog();
        for (int x = 0; x < m.getSize(); x++) {
            ListBoxData lbdata = (ListBoxData) m.getElementAt(x);
            JPanel aPanel = lbdata.getPanel();

            contentAreaPanel.removeAll();
            contentAreaPanel.add(aPanel, BorderLayout.CENTER);
            dlgdummy.setContentPane(contentAreaPanel);
            dlgdummy.pack();
            // get size (including bordersize from contentAreaPane)
            int tmpX = contentAreaPanel.getWidth();
            int tmpY = contentAreaPanel.getHeight();
            maxX = Math.max(maxX, tmpX);
            maxY = Math.max(maxY, tmpY);
        }
        dlgdummy = null; // give some hint to gc() , in case its needed
        contentAreaPanel.removeAll();
        return new Dimension(maxX, maxY);
    }

    /**
     * Build the button panel.
     * @return
     */
    protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            // OK / Cancel
            JButton okButton = new JButton(language.getString("Common.ok"));
            JButton cancelButton = new JButton(language.getString("Common.cancel"));

            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okButton_actionPerformed(e);
                }
            });
            cancelButton
                .addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelButton_actionPerformed(e);
                }
            });
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        }
        return buttonPanel;
    }

    /**
     * Build the display panel.
     * @return
     */
    private DisplayPanel getDisplayPanel() {
        if (displayPanel == null) {
            displayPanel = new DisplayPanel(this, frostSettings);
        }
        return displayPanel;
    }
    
    private SkinPanel getSkinPanel() {
        if( skinPanel == null ) {
            skinPanel = new SkinPanel(this, frostSettings);
        }
        return skinPanel;
    }

    /**
     * Build the download panel.
     * @return
     */
    private DownloadPanel getDownloadPanel() {
        if (downloadPanel == null) {
            downloadPanel = new DownloadPanel(this, frostSettings);
        }
        return downloadPanel;
    }

    /**
     * Build the misc. panel.
     * @return
     */
    private MiscPanel getMiscPanel() {
        if (miscPanel == null) {
            miscPanel = new MiscPanel(frostSettings);
        }
        return miscPanel;
    }

    /**
     * Build the news2 panel (spam options).
     * @return
     */
    private News2Panel getNews2Panel() {
        if (news2Panel == null) {
            news2Panel = new News2Panel(frostSettings);
        }
        return news2Panel;
    }

    /**
     * Build the news3 panel (update options).
     * @return
     */
    private News3Panel getNews3Panel() {
        if (news3Panel == null) {
            news3Panel = new News3Panel(frostSettings);
        }
        return news3Panel;
    }

    private ExpirationPanel getExpirationPanel() {
        if (expirationPanel == null) {
            expirationPanel = new ExpirationPanel(this, frostSettings);
        }
        return expirationPanel;
    }

    /**
     * Build the news panel (general options).
     * @return
     */
    private NewsPanel getNewsPanel() {
        if (newsPanel == null) {
            newsPanel = new NewsPanel(frostSettings);
        }
        return newsPanel;
    }

    /**
     * Build the panel containing the list of option groups.
     * @return
     */
    protected JPanel getOptionsGroupsPanel() {
        if (optionsGroupsPanel == null) {
            // init the list
            Vector listData = new Vector();
            listData.add( new ListBoxData(" "+language.getString("Options.downloads")+" ", getDownloadPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.uploads")+" ", getUploadPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.news")+" (1) ", getNewsPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.news")+" (2) ", getNews2Panel()));
            listData.add( new ListBoxData(" "+language.getString("Options.news")+" (3) ", getNews3Panel()));
            listData.add( new ListBoxData(" "+language.getString("Options.expiration")+" ", getExpirationPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.search")+" ", getSearchPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.display")+" ", getDisplayPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.skins")+" ", getSkinPanel()));
            listData.add( new ListBoxData(" "+language.getString("Options.miscellaneous")+" ", getMiscPanel()));
            optionsGroupsList = new JList(listData);
            optionsGroupsList.setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
            optionsGroupsList.addListSelectionListener(this);

            optionsGroupsPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.NORTHWEST;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            optionsGroupsPanel.add(optionsGroupsList, constr);
            optionsGroupsPanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createEtchedBorder()));
        }
        return optionsGroupsPanel;
    }

    /**
     * Build the search panel
     * @return
     */
    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel(frostSettings);
        }
        return searchPanel;
    }

    /**
     * Build the upload panel.
     * @return
     */
    private UploadPanel getUploadPanel() {
        if (uploadPanel == null) {
            uploadPanel = new UploadPanel(frostSettings);
        }
        return uploadPanel;
    }
    /**
     * Build up the whole GUI.
     * @throws Exception
     */
    private void Init() throws Exception {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------
        this.setTitle(language.getString("Options.title"));
        // a program should always give users a chance to change the dialog size if needed
        this.setResizable(true);

        mainPanel = new JPanel(new BorderLayout());
        this.getContentPane().add(mainPanel, null); // add Main panel

        // prepare content area panel
        contentAreaPanel = new JPanel(new BorderLayout());
        contentAreaPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        contentAreaPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 0, 5, 5),
                contentAreaPanel.getBorder()));

        mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
        mainPanel.add(getOptionsGroupsPanel(), BorderLayout.WEST);

        // compute and set size of contentAreaPanel
        Dimension neededSize = computeMaxSize(optionsGroupsList.getModel());
        contentAreaPanel.setMinimumSize(neededSize);
        contentAreaPanel.setPreferredSize(neededSize);

        mainPanel.add(contentAreaPanel, BorderLayout.CENTER);
    }

    /**
     * Close window and save settings
     */
    private void ok() {
        exitState = true;

        if (displayPanel != null) {
            //If the display panel has been used, commit its changes
            displayPanel.ok();
        }
        
        if( skinPanel != null ) {
            skinPanel.ok();
        }

        if (downloadPanel != null) {
            //If the download panel has been used, commit its changes
            downloadPanel.ok();
        }

        if (searchPanel != null) {
            //If the search panel has been used, commit its changes
            searchPanel.ok();
        }

        if (uploadPanel != null) {
            //If the upload panel has been used, commit its changes
            uploadPanel.ok();
        }

        if (miscPanel != null) {
            //If the misc panel has been used, commit its changes
            miscPanel.ok();
        }

        if (newsPanel != null) {
            //If the news panel has been used, commit its changes
            newsPanel.ok();
        }

        if (news2Panel != null) {
            //If the news 2 panel has been used, commit its changes
            news2Panel.ok();
        }

        if (news3Panel != null) {
            //If the news 3 panel has been used, commit its changes
            news3Panel.ok();
        }

        if (expirationPanel != null) {
            //If the expiration panel has been used, commit its changes
            expirationPanel.ok();
        }

        saveSettings();

        dispose();
    }

    /**
     * okButton Action Listener (OK)
     * @param e
     */
    private void okButton_actionPerformed(ActionEvent e) {
        ok();
    }

    /**
     * When window is about to close, do same as if CANCEL was pressed.
     * @see java.awt.Window#processWindowEvent(java.awt.event.WindowEvent)
     */
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    /**
     * Can be called to run dialog and get its answer (true=OK, false=CANCEL)
     * @return
     */
    public boolean runDialog() {
        exitState = false;
        setVisible(true); // run dialog
        return exitState;
    }

    /**
     * Save settings
     */
    private void saveSettings() {
        try {
            frostSettings.save();
        } catch (StorageException se) {
            logger.log(Level.SEVERE, "Error while saving the settings.", se);
        }

        // now check if some settings changed
        if (checkDisableRequests == true && // BEFORE: uploads disabled?
            frostSettings.getBoolValue(SettingsClass.DISABLE_REQUESTS) == false) // AFTER: uploads enabled?
        {
            shouldRemoveDummyReqFiles = true;
        }
        if( checkMaxMessageDisplay.equals(frostSettings.getValue("maxMessageDisplay")) == false
            || checkSignedOnly != frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_UNSIGNED)
            || checkHideBadMessages != frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_BAD)
            || checkHideCheckMessages != frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_CHECK)
            || checkHideObserveMessages != frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_OBSERVE)
            || checkBlock != frostSettings.getBoolValue("blockMessageChecked")
            || checkBlockBody != frostSettings.getBoolValue("blockMessageBodyChecked")
            || checkShowDeletedMessages != frostSettings.getBoolValue("showDeletedMessages")
            || showColoredRows != frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS)
          )
        {
            // at least one setting changed, reload messages
            shouldReloadMessages = true;
        }
    }

    //------------------------------------------------------------------------

    /**
     * Load settings
     */
    private void setDataElements() {
        // first set some settings to check later if they are changed by user
        checkDisableRequests = frostSettings.getBoolValue(SettingsClass.DISABLE_REQUESTS);

        checkMaxMessageDisplay = frostSettings.getValue("maxMessageDisplay");
        checkSignedOnly = frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_UNSIGNED);
        checkHideBadMessages = frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_BAD);
        checkHideCheckMessages = frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_CHECK);
        checkHideObserveMessages = frostSettings.getBoolValue(SettingsClass.HIDE_MESSAGES_OBSERVE);
        checkBlock = frostSettings.getBoolValue("blockMessageChecked");
        checkBlockBody = frostSettings.getBoolValue("blockMessageBodyChecked");
        checkShowDeletedMessages = frostSettings.getBoolValue("showDeletedMessages");
        
        showColoredRows = frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
    }

    /**
     * Is called after the dialog is hidden.
     * This method should return true if:
     *  - signedOnly, hideCheck or hideBad where changed by user
     *  - a block settings was changed by user
     * If it returns true, the messages table should be reloaded.
     * @return
     */
    public boolean shouldReloadMessages() {
        return shouldReloadMessages;
    }

    /**
     * Is called after the dialog is hidden.
     * This method should return true if:
     *  - setting 'disableRequests' is switched from TRUE to FALSE (means uploading is enabled now)
     * If it returns true, the dummy request files (created after a key collision)
     * of all boards should be removed.
     * @return
     */
    public boolean shouldRemoveDummyReqFiles() {
        return shouldRemoveDummyReqFiles;
    }

    /**
     * Implementing the ListSelectionListener.
     * Must change the content of contentAreaPanel to the selected
     * panel.
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        JList theList = (JList) e.getSource();
        Object Olbdata = theList.getSelectedValue();

        contentAreaPanel.removeAll();

        if (Olbdata instanceof ListBoxData) {
            ListBoxData lbdata = (ListBoxData) Olbdata;
            JPanel newPanel = lbdata.getPanel();
            contentAreaPanel.add(newPanel, BorderLayout.CENTER);
            newPanel.revalidate();
            newPanel.repaint();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                contentAreaPanel.revalidate();
            }
        });
    }
}
