/*
  OptionsFrame.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
--------------------------------------------------------------------------
  CHANGELOG:
  03/27/2003 - bback
    - changed layout from tabbed pane to a mozilla-like look
*/
package frost.gui;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;

import frost.*;

/*******************************
 * TODO: - add thread listeners (listen to all running threads) to change the
 *         updating state (bold text in table row) on demand (from bback)
 *******************************/

public class OptionsFrame extends JDialog implements ListSelectionListener
{
    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------

    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    SettingsClass frostSettings;

    boolean exitState;

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JPanel mainPanel = null;
    JPanel buttonPanel = null; // OK / Cancel
    JPanel downloadPanel = null;
    JPanel uploadPanel = null;
    JPanel tofPanel = null;
    JPanel tof2Panel = null;
    JPanel tof3Panel = null;
    JPanel miscPanel = null;
    JPanel searchPanel = null;
    JPanel contentAreaPanel = null;
    JPanel optionsGroupsPanel = null;

    JTextArea tofTextArea = new JTextArea(4,50);

    JTextField downloadDirectoryTextField = new JTextField(30);
    JTextField downloadMinHtlTextField = new JTextField(5);
    JTextField downloadMaxHtlTextField = new JTextField(5);
    JTextField downloadThreadsTextField = new JTextField(5);
    JTextField downloadSplitfileThreadsTextField = new JTextField(5);
    JTextField uploadHtlTextField = new JTextField(5);
    JTextField uploadThreadsTextField = new JTextField(5);
    JTextField uploadSplitfileThreadsTextField = new JTextField(5);
    JTextField tofUploadHtlTextField = new JTextField(5);
    JTextField tofDownloadHtlTextField = new JTextField(5);
    JTextField tofDisplayDaysTextField = new JTextField(5);
    JTextField tofDownloadDaysTextField = new JTextField(5);
    JTextField tofMessageBaseTextField = new JTextField(8);
    JTextField tofBlockMessageTextField = new JTextField(32);
    JTextField miscKeyUploadHtlTextField = new JTextField(5);
    JTextField miscKeyDownloadHtlTextField = new JTextField(5);
    JTextField miscNodeAddressTextField = new JTextField(11);
    JTextField miscNodePortTextField = new JTextField(8);
    JTextField miscMaxKeysTextField = new JTextField(8);
    JTextField miscAltEditTextField = new JTextField(30);
    JTextField searchAudioExtensionTextField = new JTextField(30);
    JTextField searchVideoExtensionTextField = new JTextField(30);
    JTextField searchDocumentExtensionTextField = new JTextField(30);
    JTextField searchExecutableExtensionTextField = new JTextField(30);
    JTextField searchImageExtensionTextField = new JTextField(30);
    JTextField searchArchiveExtensionTextField = new JTextField(30);

    JTextField TFautomaticUpdate_boardsMinimumUpdateInterval = new JTextField(5);
    JTextField TFautomaticUpdate_concurrentBoardUpdates = new JTextField(5);

    JCheckBox removeFinishedDownloadsCheckBox = new JCheckBox(LangRes.getString("Remove finished downloads every 5 minutes.") +
                                  " " + LangRes.getString("(Off)"));

    JCheckBox allowEvilBertCheckBox = new JCheckBox(LangRes.getString("Allow 2 byte characters") + " " +
                                                    LangRes.getString("(Off)"));
    JCheckBox miscAltEditCheckBox = new JCheckBox(LangRes.getString("Use editor for writing messages: ") + " " +
                                                  LangRes.getString("(Off)"));

    JRadioButton downloadUpdateMethodLeastHtlFirst = new JRadioButton( "Files with smallest HTL first" );
    JRadioButton downloadUpdateMethodOneByOne = new JRadioButton( "Files one by one, no matter which HTL (on)" );
    ButtonGroup downloadUpdateMethod = new ButtonGroup();

    JCheckBox downloadRestartFailedDownloads = new JCheckBox("Restart failed downloads with minimum HTL");

    JList optionsGroupsList = null;

    // new options in WOT:
    // TODO: translation
    JTextField sampleInterval = new JTextField(5);
    JTextField spamTreshold = new JTextField(5);
    JTextField tofBlockMessageBodyTextField = new JTextField(32);
    JTextField startRequestingAfterHtlTextField = new JTextField(5);

    JCheckBox uploadDisableRequests = new JCheckBox(LangRes.getString("Disable uploads"));
    JCheckBox downloadDisableDownloads = new JCheckBox(LangRes.getString("Disable downloads"));

    JCheckBox signedOnly = new JCheckBox(LangRes.getString("Show only signed messages"));
    JCheckBox hideBadMessages = new JCheckBox(LangRes.getString("Hide messages flagged BAD") + " " + LangRes.getString("(Off)"));
    JCheckBox hideCheckMessages = new JCheckBox(LangRes.getString("Hide messages flagged CHECK") + " " + LangRes.getString("(Off)"));
    JCheckBox block = new JCheckBox(LangRes.getString("Block message from/subject containing:"));
    JCheckBox blockBody = new JCheckBox(LangRes.getString("Block message body containing:"));
    JCheckBox doBoardBackoff = new JCheckBox(LangRes.getString("Do spam detection") + " (experimental)");
    JLabel interval = new JLabel(LangRes.getString("Sample interval (hours)"));
    JLabel treshold = new JLabel(LangRes.getString("Threshold of blocked messages"));
    JLabel startRequestingAfterHtlLabel = new JLabel(LangRes.getString("Insert request if HTL tops:") + " (10)");
    JCheckBox cleanUP = new JCheckBox(LangRes.getString("Clean the keypool"));

    /**
     * Build up the whole GUI.
     */
    private void Init() throws Exception
    {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------
        this.setTitle(LangRes.getString("Options"));
        // a program should always give users a chance to change the dialog size if needed
        this.setResizable( true );

        //------------------------------------------------------------------------
        // ChangeListener
        //------------------------------------------------------------------------
        miscAltEditCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if( e.getSource().equals(miscAltEditCheckBox) )
                    miscAltEditTextField.setEditable(miscAltEditCheckBox.isSelected());
            } });
        signedOnly.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                if (e.getSource().equals(signedOnly)){
                //goodOnly.setEnabled(signedOnly.isSelected());
                hideBadMessages.setEnabled(signedOnly.isSelected());
                hideCheckMessages.setEnabled(signedOnly.isSelected());
                }
            } });
        block.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                if (e.getSource().equals(block))
                    tofBlockMessageTextField.setEnabled(block.isSelected());
            } });
        blockBody.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                if (e.getSource().equals(blockBody))
                    tofBlockMessageBodyTextField.setEnabled(blockBody.isSelected());
            } });
        doBoardBackoff.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                if (e.getSource().equals(doBoardBackoff)) {
                    sampleInterval.setEnabled(doBoardBackoff.isSelected());
                    spamTreshold.setEnabled(doBoardBackoff.isSelected());
                    treshold.setEnabled(doBoardBackoff.isSelected());
                    interval.setEnabled(doBoardBackoff.isSelected());
                }
            } });
        //------------------------------------------------------------------------

        mainPanel = new JPanel(new BorderLayout());
        this.getContentPane().add(mainPanel, null); // add Main panel

        // prepare content area panel
        contentAreaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentAreaPanel.setBorder( BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));
        contentAreaPanel.setBorder( BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5,0,5,5),
            contentAreaPanel.getBorder()
        ));

        mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
        mainPanel.add(getOptionsGroupsPanel(), BorderLayout.WEST);

        // compute and set size of contentAreaPanel
        Dimension neededSize = computeMaxSize( optionsGroupsList.getModel() );
        contentAreaPanel.setMinimumSize( neededSize );
        contentAreaPanel.setPreferredSize( neededSize );

        mainPanel.add(contentAreaPanel, BorderLayout.CENTER);
    }

    /**
     * Computes the maximum width and height of the various options panels.
     * Returns Dimension with max. x and y that is needed.
     * Gets all panels from the ListModel of the option groups list.
     */
    protected Dimension computeMaxSize(ListModel m)
    {
        if( m == null || m.getSize() == 0 )
            return null;
        int maxX = -1;
        int maxY = -1;
        // misuse a JDialog to determine the panel size before showing
        JDialog dlgdummy = new JDialog();
        for( int x=0; x<m.getSize(); x++ )
        {
            ListBoxData lbdata = (ListBoxData)m.getElementAt(x);
            JPanel aPanel = lbdata.getPanel();

            contentAreaPanel.removeAll();
            contentAreaPanel.add( aPanel, BorderLayout.CENTER );
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
     * Build the panel containing the list of option groups.
     */
    protected JPanel getOptionsGroupsPanel()
    {
        if( optionsGroupsPanel == null )
        {
            // init the list
            Vector listData = new Vector();
            listData.add( new ListBoxData( " "+LangRes.getString("Downloads")+" ",     getDownloadPanel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("Uploads")+" ",       getUploadPanel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("News")+" (1) ",      getTofPanel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("News")+" (2) ",      getTof2Panel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("News")+" (3) ",      getTof3Panel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("Search")+" ",        getSearchPanel() ) );
            listData.add( new ListBoxData( " "+LangRes.getString("Miscellaneous")+" ", getMiscPanel() ) );
            optionsGroupsList = new JList( listData );
            optionsGroupsList.setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
            optionsGroupsList.addListSelectionListener( this );

            optionsGroupsPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.NORTHWEST;
            constr.fill = GridBagConstraints.BOTH;
            constr.weightx=0.7; constr.weighty=0.7;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0; constr.gridy = 0;
            optionsGroupsPanel.add( optionsGroupsList, constr );
            optionsGroupsPanel.setBorder( BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5,5,5,5),
                BorderFactory.createEtchedBorder()
            ));
        }
        return optionsGroupsPanel;
    }

    /**
     * Build the download panel.
     */
    protected JPanel getDownloadPanel()
    {
        if( downloadPanel == null )
        {
            downloadPanel = new JPanel(new GridBagLayout());

            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0; constr.gridy = 0;

            downloadDisableDownloads.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    downloadDisableDownloads_actionPerformed(e);
                } });
            downloadPanel.add(downloadDisableDownloads, constr);

            constr.gridy++;
            constr.gridx = 0;
            downloadPanel.add(new JLabel(LangRes.getString("Download directory:")), constr);
            downloadDirectoryTextField.setEditable(true);
            constr.gridx = 1;
            constr.gridwidth = 3;
            downloadPanel.add(downloadDirectoryTextField, constr);

            JButton browseDownloadDirectoryButton = new JButton(LangRes.getString("Browse..."));
            browseDownloadDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    browseDownloadDirectoryButton_actionPerformed(e);
                } });

            constr.gridx = 1;
            constr.gridy++;
            constr.gridwidth = 2;
            constr.anchor = GridBagConstraints.NORTHWEST;
            constr.insets = new Insets(0, 5, 5, 5);
            downloadPanel.add(browseDownloadDirectoryButton, constr);
            constr.gridwidth = 1;
            constr.insets = new Insets(5, 5, 5, 5);

            constr.gridy++;
            constr.gridx = 0;
            constr.anchor = GridBagConstraints.WEST;
            downloadPanel.add(new JLabel(LangRes.getString("Minimum HTL:")  + " (5)"), constr);
            constr.gridx = 1;
            downloadPanel.add(downloadMinHtlTextField, constr);

            constr.gridx = 2;
            downloadPanel.add(new JLabel(LangRes.getString("Maximum HTL:") + " (30)"),constr);
            constr.gridx = 3;
            downloadPanel.add(downloadMaxHtlTextField, constr);

            constr.gridy++;
            constr.gridx = 0;
            downloadPanel.add(startRequestingAfterHtlLabel, constr);
            constr.gridx = 1;
            downloadPanel.add(startRequestingAfterHtlTextField, constr);

            constr.gridy++;
            constr.gridx = 0;
            downloadPanel.add(downloadRestartFailedDownloads, constr);

            constr.gridy++;
            constr.gridx = 0;
            downloadPanel.add(new JLabel(LangRes.getString("Number of simultaneous downloads:") + " (3)"), constr);
            constr.gridx = 1;
            downloadPanel.add(downloadThreadsTextField, constr);

            constr.gridy++;
            constr.gridx = 0;
            downloadPanel.add(new JLabel(LangRes.getString("Number of splitfile threads:") + " (3)"), constr);
            constr.gridx = 1;
            downloadPanel.add(downloadSplitfileThreadsTextField, constr);

            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 3;
            constr.insets = new Insets(5,5,5,5);
            downloadPanel.add(removeFinishedDownloadsCheckBox, constr);

            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 3;
            constr.insets = new Insets(7,5,5,5);
            downloadPanel.add(new JLabel("Method for choosing the next file from download table"), constr);

            downloadUpdateMethod.add( downloadUpdateMethodLeastHtlFirst );
            downloadUpdateMethod.add( downloadUpdateMethodOneByOne );

            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 3;
            constr.insets = new Insets(0,25,5,5);
            downloadPanel.add(downloadUpdateMethodOneByOne, constr);

            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 3;
            constr.insets = new Insets(0,25,5,5);
            downloadPanel.add(downloadUpdateMethodLeastHtlFirst, constr);

            // filler (glue)
            constr.gridy++;
            constr.gridx = 3;
            constr.gridwidth = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            downloadPanel.add(new JLabel(" "), constr);
        }
        return downloadPanel;
    }

    /**
     * Build the upload panel.
     */
    protected JPanel getUploadPanel()
    {
        if( uploadPanel == null )
        {
            uploadPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            uploadDisableRequests.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    uploadDisableRequests_actionPerformed(e);
                } });
            uploadPanel.add(uploadDisableRequests,constr);
            constr.gridy++;
            constr.gridx=0;
            uploadPanel.add(new JLabel(LangRes.getString("Upload HTL:") + " (8)"),constr);
            constr.gridx = 1;
            uploadPanel.add(uploadHtlTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            uploadPanel.add(new JLabel(LangRes.getString("Number of simultaneous uploads:") + " (3)"),constr);
            constr.gridx = 1;
            uploadPanel.add(uploadThreadsTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(5,5,5,5);
            uploadPanel.add(new JLabel(LangRes.getString("Number of splitfile threads:") + " (3)"),constr);
            constr.gridx = 1;
            uploadPanel.add(uploadSplitfileThreadsTextField, constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            uploadPanel.add(new JLabel(" "), constr);
        }
        return uploadPanel;
    }

    /**
     * Build the tof panel.
     */
    protected JPanel getTofPanel()
    {
        if( tofPanel == null )
        {
            tofPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            tofPanel.add(new JLabel(LangRes.getString("Message upload HTL:") + " (21)"), constr);
            constr.gridx = 1;
            tofPanel.add(tofUploadHtlTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            tofPanel.add(new JLabel(LangRes.getString("Message download HTL:") + " (23)"), constr);
            constr.gridx = 1;
            tofPanel.add(tofDownloadHtlTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            tofPanel.add(new JLabel(LangRes.getString("Number of days to display:") + " (10)"), constr);
            constr.gridx = 1;
            tofPanel.add(tofDisplayDaysTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            tofPanel.add(new JLabel(LangRes.getString("Number of days to download backwards:") + " (3)"), constr);
            constr.gridx = 1;
            tofPanel.add(tofDownloadDaysTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            tofPanel.add(new JLabel(LangRes.getString("Message base:") + " (news)"), constr);
            constr.gridx = 1;
            tofPanel.add(tofMessageBaseTextField, constr);
            constr.gridy++;
            constr.gridx = 0;

            tofPanel.add(new JLabel(LangRes.getString("Signature")), constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 2;
            constr.weightx = 0.7;
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.insets = new Insets(0, 5, 5, 5);
            JScrollPane tofSignatureScrollPane = new JScrollPane();
            tofSignatureScrollPane.getViewport().add(tofTextArea);
            tofPanel.add(tofSignatureScrollPane, constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            tofPanel.add(new JLabel(" "), constr);
        }
        return tofPanel;
    }

    /**
     * Build the tof2 panel (spam options).
     */
    protected JPanel getTof2Panel()
    {
        if( tof2Panel == null )
        {
            tof2Panel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            constr.gridwidth = 2;
            tof2Panel.add(block, constr);
            constr.gridy++;
            constr.insets = new Insets(0, 25, 5, 5);
            tof2Panel.add(tofBlockMessageTextField, constr);
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridy++;
            tof2Panel.add(blockBody, constr);
            constr.gridy++;
            constr.insets = new Insets(0, 25, 5, 5);
            tof2Panel.add(tofBlockMessageBodyTextField, constr);
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridwidth = 1;
            constr.gridy++;
            constr.gridx = 0;
            tof2Panel.add(signedOnly,constr);
            constr.gridx = 1;
            tof2Panel.add(hideBadMessages,constr);
            constr.gridy++;
            tof2Panel.add(hideCheckMessages,constr);
            constr.gridy++;
            constr.gridx = 0;
            tof2Panel.add(doBoardBackoff,constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(0, 25, 5, 5);
            tof2Panel.add(interval,constr);
            constr.gridx = 1;
            constr.insets = new Insets(5, 0, 5, 5);
            tof2Panel.add(sampleInterval,constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(0, 25, 5, 5);
            tof2Panel.add(treshold,constr);
            constr.gridx = 1;
            constr.insets = new Insets(5, 0, 5, 5);
            tof2Panel.add(spamTreshold,constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            tof2Panel.add(new JLabel(" "), constr);
        }
        return tof2Panel;
    }

    /**
     * Build the tof3 panel (automatic update options).
     */
    protected JPanel getTof3Panel()
    {
        if( tof3Panel == null )
        {
            tof3Panel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            tof3Panel.add(new JLabel(LangRes.getString("Automatic update options")), constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(5, 25, 5, 5);
            tof3Panel.add(new JLabel(LangRes.getString("Minimum update interval of a board (minutes) :") + " (5)"), constr);
            constr.gridx = 1;
            constr.insets = new Insets(5, 5, 5, 5);
            tof3Panel.add(TFautomaticUpdate_boardsMinimumUpdateInterval, constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(5, 25, 5, 5);
            tof3Panel.add(new JLabel(LangRes.getString("Number of concurrently updating boards:") + " (5)"), constr);
            constr.gridx = 1;
            constr.insets = new Insets(5, 5, 5, 5);
            tof3Panel.add(TFautomaticUpdate_concurrentBoardUpdates, constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            tof3Panel.add(new JLabel(" "), constr);
        }
        return tof3Panel;
    }


    /**
     * Build the misc. panel.
     */
    protected JPanel getMiscPanel()
    {
        if( miscPanel == null )
        {
            miscPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            miscPanel.add(new JLabel(LangRes.getString("Keyfile upload HTL:") + " (21)"), constr);
            constr.gridx = 1;
            miscPanel.add(miscKeyUploadHtlTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(new JLabel(LangRes.getString("Keyfile download HTL:") + " (24)"), constr);
            constr.gridx = 1;
            miscPanel.add(miscKeyDownloadHtlTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(new JLabel(LangRes.getString("Node address:") + " (127.0.0.1)"), constr);
            constr.gridx = 1;
            miscPanel.add(miscNodeAddressTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(new JLabel(LangRes.getString("Node port:") + " (8481)"), constr);
            constr.gridx = 1;
            miscPanel.add(miscNodePortTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(new JLabel(LangRes.getString("Maximum number of keys to store:") + " (100000)"),constr);
            constr.gridx = 1;
            miscPanel.add(miscMaxKeysTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.gridwidth = 2;
            miscPanel.add(allowEvilBertCheckBox, constr);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(miscAltEditCheckBox, constr);
            constr.gridy++;
            constr.gridx = 0;
            constr.insets = new Insets(0,25,10,5);
            miscPanel.add(miscAltEditTextField, constr);
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridy++;
            constr.gridx = 0;
            miscPanel.add(cleanUP, constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            miscPanel.add(new JLabel(" "), constr);
        }
        return miscPanel;
    }

    /**
     * Build the search panel
     */
    protected JPanel getSearchPanel()
    {
        if( searchPanel == null )
        {
            searchPanel = new JPanel(new GridBagLayout());
            GridBagConstraints constr = new GridBagConstraints();
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new Insets(5, 5, 5, 5);
            constr.gridx = 0;
            constr.gridy = 0;
            searchPanel.add(new JLabel(LangRes.getString("Image Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchImageExtensionTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            searchPanel.add(new JLabel(LangRes.getString("Video Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchVideoExtensionTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            searchPanel.add(new JLabel(LangRes.getString("Archive Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchArchiveExtensionTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            searchPanel.add(new JLabel(LangRes.getString("Document Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchDocumentExtensionTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            searchPanel.add(new JLabel(LangRes.getString("Audio Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchAudioExtensionTextField, constr);
            constr.gridy++;
            constr.gridx = 0;
            searchPanel.add(new JLabel(LangRes.getString("Executable Extension:")), constr);
            constr.gridx = 1;
            searchPanel.add(searchExecutableExtensionTextField, constr);
            // filler (glue)
            constr.gridy++;
            constr.gridx = 1;
            constr.weightx = 0.7;
            constr.weighty = 0.7;
            constr.insets = new Insets(0,0,0,0);
            constr.fill = GridBagConstraints.BOTH;
            searchPanel.add(new JLabel(" "), constr);
        }
        return searchPanel;
    }

    /**
     * Build the button panel.
     */
    protected JPanel getButtonPanel()
    {
        if( buttonPanel == null )
        {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // OK / Cancel

            JButton okButton = new JButton(LangRes.getString("OK"));
            JButton cancelButton = new JButton(LangRes.getString("Cancel"));

            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                okButton_actionPerformed(e);
                } });
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   cancelButton_actionPerformed(e);
                } });
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        }
        return buttonPanel;
    }

    /**
     * Implementing the ListSelectionListener.
     * Must change the content of contentAreaPanel to the selected
     * panel.
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if( e.getValueIsAdjusting() )
            return;

        JList theList = (JList)e.getSource();
        Object Olbdata = theList.getSelectedValue();

        contentAreaPanel.removeAll();

        if( Olbdata instanceof ListBoxData )
        {
            ListBoxData lbdata = (ListBoxData)Olbdata;
            JPanel newPanel = lbdata.getPanel();
            contentAreaPanel.add( newPanel );
            newPanel.revalidate();
        }
        contentAreaPanel.updateUI();
    }

    /**
     * A simple helper class to store JPanels and their name into a JList.
     */
    class ListBoxData
    {
        JPanel panel;
        String name;
        public ListBoxData(String n, JPanel p)
        {
            panel = p;
            name = n;
        }
        public String toString()
        {
            return name;
        }
        public JPanel getPanel()
        {
            return panel;
        }
    }

    /**
     * okButton Action Listener (OK)
     */
    private void okButton_actionPerformed(ActionEvent e)
    {
        ok();
    }

    /**
     * cancelButton Action Listener (Cancel)
     */
    private void cancelButton_actionPerformed(ActionEvent e)
    {
        cancel();
    }

    private void downloadDisableDownloads_actionPerformed(ActionEvent e)
    {
        boolean enableComponents;
        if( downloadDisableDownloads.isSelected() )
        {
            // downloads disabled
            enableComponents = false;
        }
        else
        {
            // downloads enabled
            enableComponents = true;
        }
        int componentCount = getDownloadPanel().getComponentCount();
        for( int x=0; x<componentCount; x++ )
        {
            Component c = getDownloadPanel().getComponent(x);
            if( c != downloadDisableDownloads )
            {
                c.setEnabled( enableComponents );
            }
        }
    }
    private void uploadDisableRequests_actionPerformed(ActionEvent e)
    {
        boolean enableComponents;
        if( uploadDisableRequests.isSelected() )
        {
            // uploads disabled
            enableComponents = false;
        }
        else
        {
            // uploads enabled
            enableComponents = true;
        }
        int componentCount = getUploadPanel().getComponentCount();
        for( int x=0; x<componentCount; x++ )
        {
            Component c = getUploadPanel().getComponent(x);
            if( c != uploadDisableRequests )
            {
                c.setEnabled( enableComponents );
            }
        }
    }

    /**
     * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
     */
    private void browseDownloadDirectoryButton_actionPerformed(ActionEvent e)
    {
        final JFileChooser fc = new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
        fc.setDialogTitle(LangRes.getString("Select download directory."));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(OptionsFrame.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            String fileSeparator = System.getProperty("file.separator");
            File file = fc.getSelectedFile();
            frostSettings.setValue("lastUsedDirectory", file.getParent());
            downloadDirectoryTextField.setText(file.getPath() + fileSeparator);
        }
    }

    //------------------------------------------------------------------------

    /**
     * Load settings
     */
    private void setDataElements()
    {
        removeFinishedDownloadsCheckBox.setSelected(frostSettings.getBoolValue("removeFinishedDownloads"));
        allowEvilBertCheckBox.setSelected(frostSettings.getBoolValue("allowEvilBert"));
        miscAltEditCheckBox.setSelected(frostSettings.getBoolValue("useAltEdit"));
        signedOnly.setSelected(frostSettings.getBoolValue("signedOnly"));
        doBoardBackoff.setSelected(frostSettings.getBoolValue("doBoardBackoff"));
        interval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
        treshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
        sampleInterval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
        spamTreshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
        sampleInterval.setText(frostSettings.getValue("sampleInterval"));
        spamTreshold.setText(frostSettings.getValue("spamTreshold"));
        hideBadMessages.setEnabled(frostSettings.getBoolValue("signedOnly"));
        hideBadMessages.setSelected(frostSettings.getBoolValue("hideBadMessages"));
        hideCheckMessages.setEnabled(frostSettings.getBoolValue("signedOnly"));
        hideCheckMessages.setSelected(frostSettings.getBoolValue("hideCheckMessages"));
        block.setSelected(frostSettings.getBoolValue("blockMessageChecked"));
        blockBody.setSelected(frostSettings.getBoolValue("blockMessageBodyChecked"));
        miscAltEditTextField.setEditable(miscAltEditCheckBox.isSelected());
        downloadDirectoryTextField.setText(frostSettings.getValue("downloadDirectory"));
        downloadMinHtlTextField.setText(frostSettings.getValue("htl"));
        downloadMaxHtlTextField.setText(frostSettings.getValue("htlMax"));
        downloadThreadsTextField.setText(frostSettings.getValue("downloadThreads"));
        uploadHtlTextField.setText(frostSettings.getValue("htlUpload"));
        uploadThreadsTextField.setText(frostSettings.getValue("uploadThreads"));
        tofUploadHtlTextField.setText(frostSettings.getValue("tofUploadHtl"));
        tofDownloadHtlTextField.setText(frostSettings.getValue("tofDownloadHtl"));
        tofDisplayDaysTextField.setText(frostSettings.getValue("maxMessageDisplay"));
        tofDownloadDaysTextField.setText(frostSettings.getValue("maxMessageDownload"));
        miscKeyUploadHtlTextField.setText(frostSettings.getValue("keyUploadHtl"));
        miscKeyDownloadHtlTextField.setText(frostSettings.getValue("keyDownloadHtl"));
        downloadSplitfileThreadsTextField.setText(frostSettings.getValue("splitfileDownloadThreads"));
        uploadSplitfileThreadsTextField.setText(frostSettings.getValue("splitfileUploadThreads"));
        miscNodeAddressTextField.setText(frostSettings.getValue("nodeAddress"));
        miscNodePortTextField.setText(frostSettings.getValue("nodePort"));
        miscAltEditTextField.setText(frostSettings.getValue("altEdit"));
        miscMaxKeysTextField.setText(frostSettings.getValue("maxKeys"));
        tofMessageBaseTextField.setText(frostSettings.getValue("messageBase"));
        tofBlockMessageTextField.setText(frostSettings.getValue("blockMessage"));
        tofBlockMessageTextField.setEnabled(frostSettings.getBoolValue("blockMessageChecked"));
        tofBlockMessageBodyTextField.setText(frostSettings.getValue("blockMessageBody"));
        tofBlockMessageBodyTextField.setEnabled(frostSettings.getBoolValue("blockMessageBodyChecked"));
        searchAudioExtensionTextField.setText(frostSettings.getValue("audioExtension"));
        searchImageExtensionTextField.setText(frostSettings.getValue("imageExtension"));
        searchVideoExtensionTextField.setText(frostSettings.getValue("videoExtension"));
        searchDocumentExtensionTextField.setText(frostSettings.getValue("documentExtension"));
        searchExecutableExtensionTextField.setText(frostSettings.getValue("executableExtension"));
        searchArchiveExtensionTextField.setText(frostSettings.getValue("archiveExtension"));
        startRequestingAfterHtlTextField.setText(frostSettings.getValue("startRequestingAfterHtl"));
        cleanUP.setSelected(frostSettings.getBoolValue("doCleanUp"));
        uploadDisableRequests.setSelected(frostSettings.getBoolValue("disableRequests"));
        downloadDisableDownloads.setSelected(frostSettings.getBoolValue("disableDownloads"));

        TFautomaticUpdate_concurrentBoardUpdates.setText(
            frostSettings.getValue("automaticUpdate.concurrentBoardUpdates") );
        TFautomaticUpdate_boardsMinimumUpdateInterval.setText(
            frostSettings.getValue("automaticUpdate.boardsMinimumUpdateInterval") );

        downloadRestartFailedDownloads.setSelected(frostSettings.getBoolValue("downloadRestartFailedDownloads"));

        if( frostSettings.getBoolValue("downloadMethodLeastHtl") )
        {
            downloadUpdateMethodLeastHtlFirst.setSelected(true);
        }
        else
        {
            downloadUpdateMethodOneByOne.setSelected(true);
        }
    }

    /**
     * Save settings
     */
    private void saveSettings()
    {
        String downlDirTxt = downloadDirectoryTextField.getText();
        String filesep = System.getProperty("file.separator");
        // always append a fileseparator to the end of string
        if( (! (downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)) ) ||
            downlDirTxt.lastIndexOf(filesep) < 0
          )
        {
            frostSettings.setValue("downloadDirectory", downlDirTxt + filesep);
        }
        else
        {
            frostSettings.setValue("downloadDirectory", downlDirTxt);
        }

        frostSettings.setValue("htl",  downloadMinHtlTextField.getText());
        frostSettings.setValue("htlMax",  downloadMaxHtlTextField.getText());
        frostSettings.setValue("htlUpload",  uploadHtlTextField.getText());
        frostSettings.setValue("uploadThreads",  uploadThreadsTextField.getText());
        frostSettings.setValue("downloadThreads",  downloadThreadsTextField.getText());
        frostSettings.setValue("tofUploadHtl",  tofUploadHtlTextField.getText());
        frostSettings.setValue("tofDownloadHtl",  tofDownloadHtlTextField.getText());
        frostSettings.setValue("keyUploadHtl",  miscKeyUploadHtlTextField.getText());
        frostSettings.setValue("keyDownloadHtl",  miscKeyDownloadHtlTextField.getText());
        frostSettings.setValue("maxMessageDisplay",  tofDisplayDaysTextField.getText());
        frostSettings.setValue("maxMessageDownload",  tofDownloadDaysTextField.getText());
        frostSettings.setValue("removeFinishedDownloads", removeFinishedDownloadsCheckBox.isSelected());
        frostSettings.setValue("splitfileUploadThreads", uploadSplitfileThreadsTextField.getText());
        frostSettings.setValue("splitfileDownloadThreads", downloadSplitfileThreadsTextField.getText());
        frostSettings.setValue("startRequestingAfterHtl", startRequestingAfterHtlTextField.getText());
        frostSettings.setValue("nodeAddress", miscNodeAddressTextField.getText());
        frostSettings.setValue("nodePort", miscNodePortTextField.getText());
        frostSettings.setValue("maxKeys", miscMaxKeysTextField.getText());
        frostSettings.setValue("messageBase", ((tofMessageBaseTextField.getText()).trim()).toLowerCase());

        frostSettings.setValue("blockMessage", ((tofBlockMessageTextField.getText()).trim()).toLowerCase());
        frostSettings.setValue("blockMessageChecked", block.isSelected());
        frostSettings.setValue("blockMessageBody", ((tofBlockMessageBodyTextField.getText()).trim()).toLowerCase());
        frostSettings.setValue("blockMessageBodyChecked", blockBody.isSelected());
        frostSettings.setValue("doBoardBackoff", doBoardBackoff.isSelected());
        frostSettings.setValue("spamTreshold", spamTreshold.getText());
        frostSettings.setValue("sampleInterval", sampleInterval.getText());

        frostSettings.setValue("allowEvilBert", allowEvilBertCheckBox.isSelected());
        frostSettings.setValue("audioExtension", searchAudioExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("imageExtension", searchImageExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("videoExtension", searchVideoExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("documentExtension", searchDocumentExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("executableExtension", searchExecutableExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("archiveExtension", searchArchiveExtensionTextField.getText().toLowerCase());
        frostSettings.setValue("useAltEdit", miscAltEditCheckBox.isSelected());
        frostSettings.setValue("signedOnly", signedOnly.isSelected());
        frostSettings.setValue("hideBadMessages", hideBadMessages.isSelected());
        frostSettings.setValue("hideCheckMessages", hideCheckMessages.isSelected());
        frostSettings.setValue("altEdit", miscAltEditTextField.getText());
        frostSettings.setValue("doCleanUp",cleanUP.isSelected());
        frostSettings.setValue("disableRequests",uploadDisableRequests.isSelected());
        frostSettings.setValue("disableDownloads",downloadDisableDownloads.isSelected());

        frostSettings.setValue("automaticUpdate.concurrentBoardUpdates",
                               TFautomaticUpdate_concurrentBoardUpdates.getText());
        frostSettings.setValue("automaticUpdate.boardsMinimumUpdateInterval",
                               TFautomaticUpdate_boardsMinimumUpdateInterval.getText());

        frostSettings.setValue("downloadRestartFailedDownloads", downloadRestartFailedDownloads.isSelected());

        if( downloadUpdateMethod.isSelected( downloadUpdateMethodLeastHtlFirst.getModel() ) )
        {
            frostSettings.setValue("downloadMethodLeastHtl", true);
            frostSettings.setValue("downloadMethodOneByOne", false);
        }
        else
        {
            frostSettings.setValue("downloadMethodOneByOne", true);
            frostSettings.setValue("downloadMethodLeastHtl", false);
        }
        frostSettings.writeSettingsFile();
    }

    /**
     * Close window and save settings
     */
    private void ok()
    {
        exitState = true;
        saveSettings();
        saveSignature();
        dispose();
    }

    /**
     * Close window and do not save settings
     */
    private void cancel()
    {
        exitState = false;
        dispose();
    }

    /**
     * Loads signature.txt into tofTextArea
     */
    private void loadSignature()
    {
        File signature = new File("signature.txt");
        if( signature.isFile() )
        {
            tofTextArea.setText(FileAccess.readFile("signature.txt"));
        }
    }

    /**
     * Saves signature.txt to disk
     */
    private void saveSignature()
    {
        FileAccess.writeFile(tofTextArea.getText(), "signature.txt");
    }

    /**
     * Can be called to run dialog and get its answer (true=OK, false=CANCEL)
     */
    public boolean runDialog()
    {
        this.exitState = false;
        show(); // run dialog
        return this.exitState;
    }

    /**
     * When window is about to close, do same as if CANCEL was pressed.
     */
    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            cancel();
        }
        super.processWindowEvent(e);
    }

    /**
     * Constructor, reads init file and inits the gui.
     */
    public OptionsFrame(Frame parent)
    {
        super(parent);
        setModal(true);

        frostSettings = new SettingsClass();
        setDataElements();
        loadSignature();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        // set initial selection (also sets panel)
        optionsGroupsList.setSelectedIndex(0);
        // enable or disable components
        uploadDisableRequests_actionPerformed(null);
        downloadDisableDownloads_actionPerformed(null);
        // final layouting
        pack();
        // center dialog on parent
        setLocationRelativeTo(parent);
    }
}

