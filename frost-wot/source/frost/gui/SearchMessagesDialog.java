/*
  SearchMessagesDialog.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

import mseries.Calendar.*;
import mseries.ui.*;
import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.threads.*;
import frost.util.gui.translation.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;

public class SearchMessagesDialog extends JFrame {

    static Language language = Language.getInstance();
    
    private JPanel jContentPane = null;
    private JPanel contentPanel = null;
    private JPanel Pbuttons = null;
    private JButton Bsearch = null;
    private JButton Bcancel = null;
    private JTabbedPane jTabbedPane = null;
    private JPanel Psearch = null;
    private JPanel PsearchResult = null;
    private JLabel Lsender = null;
    private JLabel Lcontent = null;
    private JTextField search_TFsender = null;
    private JTextField search_TFcontent = null;
    private JPanel Pdate = null;
    private JRadioButton date_RBdisplayed = null;
    private JRadioButton date_RBbetweenDates = null;
    private MDateEntryField date_TFstartDate = null;
    private JLabel date_Lto = null;
    private MDateEntryField date_TFendDate = null;
    private JRadioButton date_RBdaysBackward = null;
    private JTextField date_TFdaysBackward = null;
    private JPanel PtrustState = null;
    private JRadioButton truststate_RBdisplayed = null;
    private JRadioButton truststate_RBall = null;
    private JRadioButton truststate_RBchosed = null;
    private JPanel truststate_PtrustStates = null;
    private JCheckBox truststate_CBgood = null;
    private JCheckBox truststate_CBobserve = null;
    private JCheckBox truststate_CBcheck = null;
    private JCheckBox truststate_CBbad = null;
    private JCheckBox truststate_CBnone = null;
    private JCheckBox truststate_CBtampered = null;
    private JPanel Parchive = null;
    private JRadioButton archive_RBkeypoolAndArchive = null;
    private JRadioButton archive_RBkeypoolOnly = null;
    private JRadioButton archive_RBarchiveOnly = null;
    private JPanel Pboards = null;
    private JRadioButton boards_RBdisplayed = null;
//    private JRadioButton boards_RBallExisting = null;
    private JRadioButton boards_RBchosed = null;
    private JButton boards_Bchoose = null;
    private JTextField boards_TFchosedBoards = null;
    private JCheckBox search_CBprivateMsgsOnly = null;
    private JLabel LsearchResult = null;
    private JScrollPane jScrollPane = null;
    private SearchMessagesResultTable searchResultTable = null;
    private SearchMessagesTableModel searchMessagesTableModel = null;  //  @jve:decl-index=0:visual-constraint="735,15"
    private ButtonGroup boards_buttonGroup = null;  //  @jve:decl-index=0:visual-constraint="755,213"
    private ButtonGroup date_buttonGroup = null;  //  @jve:decl-index=0:visual-constraint="765,261"
    private ButtonGroup truststate_buttonGroup = null;  //  @jve:decl-index=0:visual-constraint="752,302"
    private ButtonGroup archive_buttonGroup = null;  //  @jve:decl-index=0:visual-constraint="760,342"
    private JLabel jLabel = null;
    private JTextField search_TFsubject = null;

    /**
     * This is the default constructor
     */
    public SearchMessagesDialog() {
        super();
        initialize();
        
        initializeWithDefaults();
    }

    /**
     * This method initializes search_TFsubject	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getSearch_TFsubject() {
        if( search_TFsubject == null ) {
            search_TFsubject = new JTextField();
        }
        return search_TFsubject;
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(700, 550);
        this.setTitle(language.getString("Search messages"));
        this.setPreferredSize(new java.awt.Dimension(700,550));
        this.setContentPane(getJContentPane());
        // create button groups
        this.getDate_buttonGroup();
        this.getBoards_buttonGroup();
        this.getTruststate_buttonGroup();
        this.getArchive_buttonGroup();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getContentPanel(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getPbuttons(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes contentPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getContentPanel() {
        if( contentPanel == null ) {
            contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(getJTabbedPane(), java.awt.BorderLayout.NORTH);
            contentPanel.add(getPsearchResult(), java.awt.BorderLayout.CENTER);
        }
        return contentPanel;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPbuttons() {
        if( Pbuttons == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            Pbuttons = new JPanel();
            Pbuttons.setLayout(flowLayout);
            Pbuttons.add(getBsearch(), null);
            Pbuttons.add(getBcancel(), null);
        }
        return Pbuttons;
    }

    /**
     * This method initializes Bsearch	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBsearch() {
        if( Bsearch == null ) {
            Bsearch = new JButton();
            Bsearch.setText("Search");
            Bsearch.setMnemonic(java.awt.event.KeyEvent.VK_S);
            Bsearch.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startSearching();
                }
            });
        }
        return Bsearch;
    }

    /**
     * This method initializes Bcancel	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton();
            Bcancel.setText("Close");
            Bcancel.setMnemonic(java.awt.event.KeyEvent.VK_C);
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                    MainFrame.getInstance().setSearchMessagesDialog(null);
                    dispose();
                }
            });
        }
        return Bcancel;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getJTabbedPane() {
        if( jTabbedPane == null ) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setName("");
            jTabbedPane.addTab("Search", null, getPsearch(), null);
            jTabbedPane.addTab("Boards", null, getPboards(), null);
            jTabbedPane.addTab("Date", null, getPdate(), null);
            jTabbedPane.addTab("Trust state", null, getPtrustState(), null);
            jTabbedPane.addTab("Archive", null, getParchive(), null);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPsearch() {
        if( Psearch == null ) {
            GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
            gridBagConstraints29.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints29.gridy = 1;
            gridBagConstraints29.weightx = 1.0;
            gridBagConstraints29.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints29.gridx = 1;
            GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
            gridBagConstraints110.gridx = 0;
            gridBagConstraints110.insets = new java.awt.Insets(1,5,1,0);
            gridBagConstraints110.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints110.gridy = 1;
            jLabel = new JLabel();
            jLabel.setText("Subject");
            GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
            gridBagConstraints101.gridx = 1;
            gridBagConstraints101.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints101.insets = new java.awt.Insets(1,1,1,5);
            gridBagConstraints101.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints101.weighty = 1.0;
            gridBagConstraints101.gridy = 3;
            GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
            gridBagConstraints91.gridx = -1;
            gridBagConstraints91.gridy = -1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridy = 2;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.gridwidth = 1;
            gridBagConstraints2.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints2.gridx = 1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.gridy = 0;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.gridwidth = 1;
            gridBagConstraints11.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints11.gridx = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.insets = new java.awt.Insets(1,5,1,0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.gridy = 2;
            Lcontent = new JLabel();
            Lcontent.setText("Content");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.insets = new java.awt.Insets(1,5,1,0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.gridy = 0;
            Lsender = new JLabel();
            Lsender.setText("Sender");
            Psearch = new JPanel();
            Psearch.setLayout(new GridBagLayout());
            Psearch.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            Psearch.add(Lsender, gridBagConstraints);
            Psearch.add(Lcontent, gridBagConstraints1);
            Psearch.add(getSearch_TFsender(), gridBagConstraints11);
            Psearch.add(getSearch_TFcontent(), gridBagConstraints2);
            Psearch.add(getSearch_CBprivateMsgsOnly(), gridBagConstraints101);
            Psearch.add(jLabel, gridBagConstraints110);
            Psearch.add(getSearch_TFsubject(), gridBagConstraints29);
        }
        return Psearch;
    }

    /**
     * This method initializes jPanel2	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPsearchResult() {
        if( PsearchResult == null ) {
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints6.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints6.gridy = 0;
            LresultCount = new JLabel("");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints4.gridy = 1;
            gridBagConstraints4.ipadx = 239;
            gridBagConstraints4.ipady = 0;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.weighty = 1.0;
            gridBagConstraints4.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints4.gridwidth = 2;
            gridBagConstraints4.gridx = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.ipadx = 0;
            gridBagConstraints3.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints3.gridy = 0;
            LsearchResult = new JLabel();
            LsearchResult.setText("Search result");
            PsearchResult = new JPanel();
            PsearchResult.setLayout(new GridBagLayout());
            PsearchResult.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            PsearchResult.add(LsearchResult, gridBagConstraints3);
            PsearchResult.add(getJScrollPane(), gridBagConstraints4);
            PsearchResult.add(LresultCount, gridBagConstraints6);
        }
        return PsearchResult;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getSearch_TFsender() {
        if( search_TFsender == null ) {
            search_TFsender = new JTextField();
        }
        return search_TFsender;
    }

    /**
     * This method initializes jTextField1	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getSearch_TFcontent() {
        if( search_TFcontent == null ) {
            search_TFcontent = new JTextField();
        }
        return search_TFcontent;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPdate() {
        if( Pdate == null ) {
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints15.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints15.gridwidth = 3;
            gridBagConstraints15.gridx = 1;
            gridBagConstraints15.gridy = 2;
            gridBagConstraints15.weightx = 1.0;
            gridBagConstraints15.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints14.gridy = 2;
            gridBagConstraints14.weighty = 1.0;
            gridBagConstraints14.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints14.gridx = 0;
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints13.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints13.gridx = 3;
            gridBagConstraints13.gridy = 1;
            gridBagConstraints13.weightx = 0.0;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints12.gridx = 2;
            gridBagConstraints12.gridy = 1;
            gridBagConstraints12.insets = new java.awt.Insets(1,2,0,2);
            date_Lto = new JLabel();
            date_Lto.setText("to");
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints10.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints10.gridx = 1;
            gridBagConstraints10.gridy = 1;
            gridBagConstraints10.weightx = 0.0;
            gridBagConstraints10.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.gridy = 1;
            gridBagConstraints9.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints5.gridwidth = 4;
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.gridy = 0;
            gridBagConstraints5.insets = new java.awt.Insets(1,5,0,5);
            Pdate = new JPanel();
            Pdate.setLayout(new GridBagLayout());
            Pdate.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            Pdate.add(getDate_RBdisplayed(), gridBagConstraints5);
            Pdate.add(getDate_RBbetweenDates(), gridBagConstraints9);
            Pdate.add(getDate_TFstartDate(), gridBagConstraints10);
            Pdate.add(date_Lto, gridBagConstraints12);
            Pdate.add(getDate_TFendDate(), gridBagConstraints13);
            Pdate.add(getDate_RBdaysBackward(), gridBagConstraints14);
            Pdate.add(getDate_TFdaysBackward(), gridBagConstraints15);
        }
        return Pdate;
    }

    /**
     * This method initializes jRadioButton	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getDate_RBdisplayed() {
        if( date_RBdisplayed == null ) {
            date_RBdisplayed = new JRadioButton();
            date_RBdisplayed.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            date_RBdisplayed.setText("Search in messages that would be displayed");
            date_RBdisplayed.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    date_RBitemStateChanged();
                }
            });
        }
        return date_RBdisplayed;
    }

    /**
     * This method initializes jRadioButton1	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getDate_RBbetweenDates() {
        if( date_RBbetweenDates == null ) {
            date_RBbetweenDates = new JRadioButton();
            date_RBbetweenDates.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            date_RBbetweenDates.setText("Search between dates");
            date_RBbetweenDates.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    date_RBitemStateChanged();
                }
            });
        }
        return date_RBbetweenDates;
    }

    /**
     * This method initializes jTextField3	
     * 	
     * @return javax.swing.JTextField	
     */
    private MDateEntryField getDate_TFstartDate() {
        if( date_TFstartDate == null ) {
            date_TFstartDate = new MDateEntryField();
            MDefaultPullDownConstraints c = new MDefaultPullDownConstraints();
            c.firstDay = Calendar.MONDAY;
            c.changerStyle=MDateChanger.SPINNER;
            date_TFstartDate.setConstraints(c);
        }
        return date_TFstartDate;
    }

    /**
     * This method initializes jTextField4	
     * 	
     * @return javax.swing.JTextField	
     */
    private MDateEntryField getDate_TFendDate() {
        if( date_TFendDate == null ) {
            date_TFendDate = new MDateEntryField();
            MDefaultPullDownConstraints c = new MDefaultPullDownConstraints();
            c.firstDay = Calendar.MONDAY;
            c.changerStyle=MDateChanger.SPINNER;
            date_TFendDate.setConstraints(c);
        }
        return date_TFendDate;
    }

    /**
     * This method initializes jRadioButton2	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getDate_RBdaysBackward() {
        if( date_RBdaysBackward == null ) {
            date_RBdaysBackward = new JRadioButton();
            date_RBdaysBackward.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            date_RBdaysBackward.setText("Search number of days backward");
            date_RBdaysBackward.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    date_RBitemStateChanged();
                }
            });
        }
        return date_RBdaysBackward;
    }
    
    private void date_RBitemStateChanged() {
        if( getDate_RBdisplayed().isSelected() ) {
            getDate_TFdaysBackward().setEnabled(false);
            getDate_TFendDate().setEnabled(false);
            getDate_TFstartDate().setEnabled(false);
        } else if( getDate_RBbetweenDates().isSelected() ) {
            getDate_TFdaysBackward().setEnabled(false);
            getDate_TFendDate().setEnabled(true);
            getDate_TFstartDate().setEnabled(true);
        } else if( getDate_RBdaysBackward().isSelected() ) {
            getDate_TFdaysBackward().setEnabled(true);
            getDate_TFendDate().setEnabled(false);
            getDate_TFstartDate().setEnabled(false);
        }        
    }

    /**
     * This method initializes jTextField5	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getDate_TFdaysBackward() {
        if( date_TFdaysBackward == null ) {
            date_TFdaysBackward = new JTextField();
            date_TFdaysBackward.setColumns(6);
            date_TFdaysBackward.setDocument(new WholeNumberDocument());
        }
        return date_TFdaysBackward;
    }

    /**
     * This method initializes jPanel4	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPtrustState() {
        if( PtrustState == null ) {
            GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
            gridBagConstraints25.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints25.insets = new java.awt.Insets(0,25,0,0);
            gridBagConstraints25.gridwidth = 3;
            gridBagConstraints25.gridx = 0;
            gridBagConstraints25.gridy = 3;
            gridBagConstraints25.weightx = 1.0;
            gridBagConstraints25.weighty = 1.0;
            gridBagConstraints25.fill = java.awt.GridBagConstraints.NONE;
            GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints18.gridx = 0;
            gridBagConstraints18.gridy = 2;
            gridBagConstraints18.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints17.gridx = 0;
            gridBagConstraints17.gridy = 1;
            gridBagConstraints17.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints16.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints16.gridx = 0;
            gridBagConstraints16.gridy = 0;
            gridBagConstraints16.fill = java.awt.GridBagConstraints.NONE;
            PtrustState = new JPanel();
            PtrustState.setLayout(new GridBagLayout());
            PtrustState.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            PtrustState.add(getTruststate_RBdisplayed(), gridBagConstraints16);
            PtrustState.add(getTruststate_RBall(), gridBagConstraints17);
            PtrustState.add(getTruststate_RBchosed(), gridBagConstraints18);
            PtrustState.add(getTruststate_PtrustStates(), gridBagConstraints25);
        }
        return PtrustState;
    }

    /**
     * This method initializes jRadioButton3	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getTruststate_RBdisplayed() {
        if( truststate_RBdisplayed == null ) {
            truststate_RBdisplayed = new JRadioButton();
            truststate_RBdisplayed.setText("Search in messages that would be displayed");
            truststate_RBdisplayed.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    trustState_RBitemStateChanged();
                }
            });
        }
        return truststate_RBdisplayed;
    }
    
    private void trustState_RBitemStateChanged() {
        boolean enableTtrustStatesPanel;
        if( getTruststate_RBchosed().isSelected() ) {
            enableTtrustStatesPanel = true;
        } else {
            enableTtrustStatesPanel = false;
        }
        Component[] comps = getTruststate_PtrustStates().getComponents();
        for(int x=0; x < comps.length; x++) {
            comps[x].setEnabled(enableTtrustStatesPanel);
        }
    }

    /**
     * This method initializes jRadioButton4	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getTruststate_RBall() {
        if( truststate_RBall == null ) {
            truststate_RBall = new JRadioButton();
            truststate_RBall.setText("Search all messages, no matter which trust state is set");
            truststate_RBall.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    trustState_RBitemStateChanged();
                }
            });
        }
        return truststate_RBall;
    }

    /**
     * This method initializes jRadioButton5	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getTruststate_RBchosed() {
        if( truststate_RBchosed == null ) {
            truststate_RBchosed = new JRadioButton();
            truststate_RBchosed.setText("Search only in messages with following trust state");
            truststate_RBchosed.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    trustState_RBitemStateChanged();
                }
            });
        }
        return truststate_RBchosed;
    }

    /**
     * This method initializes jPanel5	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getTruststate_PtrustStates() {
        if( truststate_PtrustStates == null ) {
            GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
            gridBagConstraints24.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints24.gridx = 5;
            gridBagConstraints24.gridy = 0;
            gridBagConstraints24.weightx = 0.0;
            gridBagConstraints24.insets = new java.awt.Insets(1,5,1,5);
            GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
            gridBagConstraints23.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints23.gridy = 0;
            gridBagConstraints23.gridx = 4;
            GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints22.gridy = 0;
            gridBagConstraints22.gridx = 3;
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints21.gridy = 0;
            gridBagConstraints21.gridx = 2;
            GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            gridBagConstraints20.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints20.gridy = 0;
            gridBagConstraints20.gridx = 1;
            GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            gridBagConstraints19.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints19.gridx = 0;
            gridBagConstraints19.gridy = 0;
            gridBagConstraints19.insets = new java.awt.Insets(1,0,1,5);
            truststate_PtrustStates = new JPanel();
            truststate_PtrustStates.setLayout(new GridBagLayout());
            truststate_PtrustStates.add(getTruststate_CBgood(), gridBagConstraints19);
            truststate_PtrustStates.add(getTruststate_CBobserve(), gridBagConstraints20);
            truststate_PtrustStates.add(getTruststate_CBcheck(), gridBagConstraints21);
            truststate_PtrustStates.add(getTruststate_CBbad(), gridBagConstraints22);
            truststate_PtrustStates.add(getTruststate_CBnone(), gridBagConstraints23);
            truststate_PtrustStates.add(getTruststate_CBtampered(), gridBagConstraints24);
        }
        return truststate_PtrustStates;
    }

    /**
     * This method initializes jCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBgood() {
        if( truststate_CBgood == null ) {
            truststate_CBgood = new JCheckBox();
            truststate_CBgood.setText("GOOD");
        }
        return truststate_CBgood;
    }

    /**
     * This method initializes jCheckBox1	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBobserve() {
        if( truststate_CBobserve == null ) {
            truststate_CBobserve = new JCheckBox();
            truststate_CBobserve.setText("OBSERVE");
        }
        return truststate_CBobserve;
    }

    /**
     * This method initializes jCheckBox2	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBcheck() {
        if( truststate_CBcheck == null ) {
            truststate_CBcheck = new JCheckBox();
            truststate_CBcheck.setText("CHECK");
        }
        return truststate_CBcheck;
    }

    /**
     * This method initializes jCheckBox3	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBbad() {
        if( truststate_CBbad == null ) {
            truststate_CBbad = new JCheckBox();
            truststate_CBbad.setText("BAD");
        }
        return truststate_CBbad;
    }

    /**
     * This method initializes jCheckBox4	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBnone() {
        if( truststate_CBnone == null ) {
            truststate_CBnone = new JCheckBox();
            truststate_CBnone.setText("NONE (anonymous)");
        }
        return truststate_CBnone;
    }

    /**
     * This method initializes jCheckBox5	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTruststate_CBtampered() {
        if( truststate_CBtampered == null ) {
            truststate_CBtampered = new JCheckBox();
            truststate_CBtampered.setText("TAMPERED");
        }
        return truststate_CBtampered;
    }

    /**
     * This method initializes jPanel3	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getParchive() {
        if( Parchive == null ) {
            GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
            gridBagConstraints28.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints28.gridx = 0;
            gridBagConstraints28.gridy = 1;
            gridBagConstraints28.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints28.insets = new java.awt.Insets(3,5,1,5);
            GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
            gridBagConstraints27.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints27.gridx = 0;
            gridBagConstraints27.gridy = 0;
            gridBagConstraints27.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints27.weightx = 1.0;
            gridBagConstraints27.insets = new java.awt.Insets(3,5,1,5);
            GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
            gridBagConstraints26.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints26.insets = new java.awt.Insets(3,5,1,5);
            gridBagConstraints26.gridx = 0;
            gridBagConstraints26.gridy = 2;
            gridBagConstraints26.weighty = 1.0;
            gridBagConstraints26.fill = java.awt.GridBagConstraints.NONE;
            Parchive = new JPanel();
            Parchive.setLayout(new GridBagLayout());
            Parchive.setPreferredSize(new java.awt.Dimension(517,25));
            Parchive.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            Parchive.add(getArchive_RBkeypoolOnly(), gridBagConstraints27);
            Parchive.add(getArchive_RBarchiveOnly(), gridBagConstraints28);
            Parchive.add(getArchive_RBkeypoolAndArchive(), gridBagConstraints26);
        }
        return Parchive;
    }

    /**
     * This method initializes jRadioButton6	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getArchive_RBkeypoolAndArchive() {
        if( archive_RBkeypoolAndArchive == null ) {
            archive_RBkeypoolAndArchive = new JRadioButton();
            archive_RBkeypoolAndArchive.setPreferredSize(new java.awt.Dimension(195,20));
            archive_RBkeypoolAndArchive.setText("Search in keypool and archive");
        }
        return archive_RBkeypoolAndArchive;
    }

    /**
     * This method initializes jRadioButton7	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getArchive_RBkeypoolOnly() {
        if( archive_RBkeypoolOnly == null ) {
            archive_RBkeypoolOnly = new JRadioButton();
            archive_RBkeypoolOnly.setPreferredSize(new java.awt.Dimension(152,20));
            archive_RBkeypoolOnly.setText("Search only in keypool");
        }
        return archive_RBkeypoolOnly;
    }

    /**
     * This method initializes jRadioButton8	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getArchive_RBarchiveOnly() {
        if( archive_RBarchiveOnly == null ) {
            archive_RBarchiveOnly = new JRadioButton();
            archive_RBarchiveOnly.setPreferredSize(new java.awt.Dimension(150,20));
            archive_RBarchiveOnly.setText("Search only in archive");
        }
        return archive_RBarchiveOnly;
    }

    /**
     * This method initializes jPanel6	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPboards() {
        if( Pboards == null ) {
            GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
            gridBagConstraints35.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints35.insets = new java.awt.Insets(1,25,1,5);
            gridBagConstraints35.gridwidth = 2;
            gridBagConstraints35.gridx = 0;
            gridBagConstraints35.gridy = 3;
            gridBagConstraints35.weightx = 1.0;
            gridBagConstraints35.weighty = 1.0;
            gridBagConstraints35.fill = java.awt.GridBagConstraints.HORIZONTAL;
            GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
            gridBagConstraints34.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints34.gridx = 1;
            gridBagConstraints34.gridy = 2;
            gridBagConstraints34.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
            gridBagConstraints33.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints33.gridx = 0;
            gridBagConstraints33.gridy = 2;
            gridBagConstraints33.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
            gridBagConstraints32.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints32.gridwidth = 2;
            gridBagConstraints32.gridx = 0;
            gridBagConstraints32.gridy = 1;
            gridBagConstraints32.insets = new java.awt.Insets(1,5,0,5);
            GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
            gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints31.gridwidth = 2;
            gridBagConstraints31.gridx = 0;
            gridBagConstraints31.gridy = 0;
            gridBagConstraints31.insets = new java.awt.Insets(1,5,0,5);
            Pboards = new JPanel();
            Pboards.setLayout(new GridBagLayout());
            Pboards.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            Pboards.add(getBoards_RBdisplayed(), gridBagConstraints31);
//            Pboards.add(getBoards_RBallExisting(), gridBagConstraints32);
            Pboards.add(getBoards_RBchosed(), gridBagConstraints33);
            Pboards.add(getBoards_Bchoose(), gridBagConstraints34);
            Pboards.add(getBoards_TFchosedBoards(), gridBagConstraints35);
        }
        return Pboards;
    }

    /**
     * This method initializes jRadioButton9	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getBoards_RBdisplayed() {
        if( boards_RBdisplayed == null ) {
            boards_RBdisplayed = new JRadioButton();
            boards_RBdisplayed.setText("Search in displayed boards");
            boards_RBdisplayed.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    boards_RBitemStateChanged();
                }
            });
        }
        return boards_RBdisplayed;
    }

    /**
     * This method initializes jRadioButton10	
     * 	
     * @return javax.swing.JRadioButton	
     */
//    private JRadioButton getBoards_RBallExisting() {
//        if( boards_RBallExisting == null ) {
//            boards_RBallExisting = new JRadioButton();
//            boards_RBallExisting.setText("Search in all existing board directories");
//            boards_RBallExisting.addItemListener(new java.awt.event.ItemListener() {
//                public void itemStateChanged(java.awt.event.ItemEvent e) {
//                    boards_RBitemStateChanged();
//                }
//            });
//        }
//        return boards_RBallExisting;
//    }

    /**
     * This method initializes jRadioButton11	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getBoards_RBchosed() {
        if( boards_RBchosed == null ) {
            boards_RBchosed = new JRadioButton();
            boards_RBchosed.setText("Search following boards");
            boards_RBchosed.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    boards_RBitemStateChanged();
                }
            });
        }
        return boards_RBchosed;
    }
    
    private void boards_RBitemStateChanged() {
        boolean enableChooseControls;
        if( getBoards_RBchosed().isSelected() ) {
            enableChooseControls = true;
        } else {
            enableChooseControls = false;
        }
        getBoards_Bchoose().setEnabled(enableChooseControls);
        getBoards_TFchosedBoards().setEnabled(enableChooseControls);
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBoards_Bchoose() {
        if( boards_Bchoose == null ) {
            boards_Bchoose = new JButton();
            boards_Bchoose.setText("Choose...");
            boards_Bchoose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    chooseBoards();
                }
            });
        }
        return boards_Bchoose;
    }

    /**
     * This method initializes jTextField6	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getBoards_TFchosedBoards() {
        if( boards_TFchosedBoards == null ) {
            boards_TFchosedBoards = new JTextField();
            boards_TFchosedBoards.setText("");
            boards_TFchosedBoards.setEditable(false);
        }
        return boards_TFchosedBoards;
    }

    /**
     * This method initializes jCheckBox6	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getSearch_CBprivateMsgsOnly() {
        if( search_CBprivateMsgsOnly == null ) {
            search_CBprivateMsgsOnly = new JCheckBox();
            search_CBprivateMsgsOnly.setText("Search private messages only");
        }
        return search_CBprivateMsgsOnly;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setForeground(new java.awt.Color(51,51,51));
            jScrollPane.setViewportView(getSearchResultTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTable	
     * 	
     * @return javax.swing.JTable	
     */
    private SearchMessagesResultTable getSearchResultTable() {
        if( searchResultTable == null ) {
            searchResultTable = new SearchMessagesResultTable(getSearchMessagesTableModel());
            searchResultTable.setAutoCreateColumnsFromModel(true);
        }
        return searchResultTable;
    }

    /**
     * This method initializes searchMessagesTableModel	
     * 	
     * @return frost.gui.model.SearchMessagesTableModel	
     */
    private SearchMessagesTableModel getSearchMessagesTableModel() {
        if( searchMessagesTableModel == null ) {
            searchMessagesTableModel = new SearchMessagesTableModel();
        }
        return searchMessagesTableModel;
    }

    /**
     * This method initializes buttonGroup	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getBoards_buttonGroup() {
        if( boards_buttonGroup == null ) {
            boards_buttonGroup = new ButtonGroup();
            boards_buttonGroup.add(getBoards_RBdisplayed());
            boards_buttonGroup.add(getBoards_RBchosed());
//            boards_buttonGroup.add(getBoards_RBallExisting());
        }
        return boards_buttonGroup;
    }

    /**
     * This method initializes date_buttonGroup	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getDate_buttonGroup() {
        if( date_buttonGroup == null ) {
            date_buttonGroup = new ButtonGroup();
            date_buttonGroup.add(getDate_RBbetweenDates());
            date_buttonGroup.add(getDate_RBdaysBackward());
            date_buttonGroup.add(getDate_RBdisplayed());
        }
        return date_buttonGroup;
    }

    /**
     * This method initializes truststate_buttonGroup	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getTruststate_buttonGroup() {
        if( truststate_buttonGroup == null ) {
            truststate_buttonGroup = new ButtonGroup();
            truststate_buttonGroup.add(getTruststate_RBdisplayed());
            truststate_buttonGroup.add(getTruststate_RBall());
            truststate_buttonGroup.add(getTruststate_RBchosed());
        }
        return truststate_buttonGroup;
    }

    /**
     * This method initializes archive_buttonGroup	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getArchive_buttonGroup() {
        if( archive_buttonGroup == null ) {
            archive_buttonGroup = new ButtonGroup();
            archive_buttonGroup.add(getArchive_RBkeypoolOnly());
            archive_buttonGroup.add(getArchive_RBarchiveOnly());
            archive_buttonGroup.add(getArchive_RBkeypoolAndArchive());
        }
        return archive_buttonGroup;
    }

    private void chooseBoards() {

        // get and sort all boards
        Vector allBoards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        if (allBoards.size() == 0) {
            JOptionPane.showMessageDialog(this,
                    "There are no boards that could be choosed.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Collections.sort(allBoards);

        BoardsChooser bc = new BoardsChooser(allBoards, chosedBoardsList);
        List resultBoards = bc.runDialog();
        if( resultBoards != null ) {
            chosedBoardsList = resultBoards;
            StringBuffer txt = new StringBuffer();
            for(Iterator i=chosedBoardsList.iterator(); i.hasNext(); ) {
                Board b = (Board)i.next();
                txt.append(b.getName());
                if( i.hasNext() ) {
                    txt.append("; ");
                }
            }
            getBoards_TFchosedBoards().setText(txt.toString());
        }
    }
    
    private void initializeWithDefaults() {
        
        getBoards_RBdisplayed().doClick();
        getDate_RBdisplayed().doClick();
        getTruststate_RBdisplayed().doClick();
        getArchive_RBkeypoolOnly().doClick();
        
        getDate_TFdaysBackward().setText("0");
    }
    
    private List splitString(String str) {
        List lst = new ArrayList();
        String[] splitted = str.split(" ");
        for(int x=0; x < splitted.length; x++) {
            String s = splitted[x].trim();
            if( s.length() > 0 ) {
                lst.add(s.toLowerCase());
//                System.out.println("Item: "+s);
            }
        }
        if( lst.size() > 0 ) {
            return lst;
        } else {
            return null;
        }
    }

    private SearchMessagesConfig getSearchConfig() {

        SearchMessagesConfig scfg = new SearchMessagesConfig();

        // sender_part1; sender_part2
        // TODO: maybe provide a chooser?
        String txt = getSearch_TFsender().getText().trim();
        if( txt.length() > 0 ) {
            scfg.sender = splitString(txt);
        }
        
        // TODO: "text abc"; text2; "hugo;emil"
        txt = getSearch_TFsubject().getText().trim();
        if( txt.length() > 0 ) {
            scfg.subject = splitString(txt);
        }

        // TODO: "text abc"; text2; "hugo;emil"
        txt = getSearch_TFcontent().getText().trim();
        if( txt.length() > 0 ) {
            scfg.content = splitString(txt);
        }
        
        scfg.searchPrivateMsgsOnly = getSearch_CBprivateMsgsOnly().isSelected();
        
        if( getBoards_RBdisplayed().isSelected() ) {
            scfg.searchBoards = SearchMessagesConfig.BOARDS_DISPLAYED;
//        } else if( getBoards_RBallExisting().isSelected() ) {
//            scfg.searchBoards = SearchConfig.BOARDS_EXISTING_DIRS;
        } else if( getBoards_RBchosed().isSelected() ) {
            if( chosedBoardsList.size() == 0 ) {
                JOptionPane.showMessageDialog(this,
                        "No boards to search into were chosed.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            scfg.searchBoards = SearchMessagesConfig.BOARDS_CHOSED;
            scfg.chosedBoards = chosedBoardsList;
        }
        
        if( getDate_RBdisplayed().isSelected() ) {
            scfg.searchDates = SearchMessagesConfig.DATE_DISPLAYED;
        } else if( getDate_RBbetweenDates().isSelected() ) {
            scfg.searchDates = SearchMessagesConfig.DATE_BETWEEN_DATES;
            try {
                GregorianCalendar c;
                c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                c.setTime(getDate_TFstartDate().getValue());
                scfg.startDate = c;
                
                c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                c.setTime(getDate_TFendDate().getValue());
                scfg.endDate = c;
                
                // check start before end
                if( scfg.startDate.after(scfg.endDate) ) {
                    JOptionPane.showMessageDialog(this,
                            "Start date is after end date.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } catch(ParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid start date or end date specified.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else if( getDate_RBdaysBackward().isSelected() ) {
            scfg.searchDates = SearchMessagesConfig.DATE_DAYS_BACKWARD;
            try {
                scfg.daysBackward = Integer.parseInt(getDate_TFdaysBackward().getText());
            } catch(NumberFormatException ex) { } // never happens, we allow only digits in textfield!
        }

        if( getTruststate_RBdisplayed().isSelected() ) {
            scfg.searchTruststates = SearchMessagesConfig.TRUST_DISPLAYED;
        } else if( getTruststate_RBall().isSelected() ) {
            scfg.searchTruststates = SearchMessagesConfig.TRUST_ALL;
        } else if( getTruststate_RBchosed().isSelected() ) {
            scfg.searchTruststates = SearchMessagesConfig.TRUST_CHOSED;
            scfg.trust_good = getTruststate_CBgood().isSelected();
            scfg.trust_observe = getTruststate_CBobserve().isSelected();
            scfg.trust_check = getTruststate_CBcheck().isSelected();
            scfg.trust_bad = getTruststate_CBbad().isSelected();
            scfg.trust_none = getTruststate_CBnone().isSelected();
            scfg.trust_tampered = getTruststate_CBtampered().isSelected();
            
            if( !scfg.trust_good && !scfg.trust_observe && !scfg.trust_check &&
                !scfg.trust_bad && !scfg.trust_none && !scfg.trust_tampered ) 
            {
                JOptionPane.showMessageDialog(this,
                        "No trust state is selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        
        if( getArchive_RBkeypoolOnly().isSelected() ) {
            scfg.searchInKeypool = true;
            scfg.searchInArchive = false;
        } else if( getArchive_RBarchiveOnly().isSelected() ) {
            scfg.searchInKeypool = false;
            scfg.searchInArchive = true;
        } else if( getArchive_RBkeypoolAndArchive().isSelected() ) {
            scfg.searchInKeypool = true;
            scfg.searchInArchive = true;
        }
        
        return scfg;
    }
    
    private SearchMessagesThread getRunningSearchThread() {
        return runningSearchThread;
    }

    private void setRunningSearchThread(SearchMessagesThread t) {
        runningSearchThread = t;
    }
    
    public void notifySearchThreadFinished() {
        setRunningSearchThread(null);
        // TODO: reset buttons
    }

    private void startSearching() {
        
        if( getRunningSearchThread() != null ) {
            System.out.println("Error: search thread still runs!");
            return;
        }
        
        SearchMessagesConfig scfg = getSearchConfig();
        if( scfg == null ) {
            // invalid cfg
            return;
        }
        
        // clear search result table
        getSearchMessagesTableModel().clearDataModel();
        resultCount = 0;
        updateResultCountLabel(resultCount);

        setRunningSearchThread(new SearchMessagesThread(this, scfg));
        getRunningSearchThread().setPriority(Thread.MIN_PRIORITY); // low prio
        getRunningSearchThread().start();
        // TODO: disable buttons, add stop button, implement request of stop
    }
    
    private void updateResultCountLabel(int rs) {
        LresultCount.setText("Results: "+rs);
    }
    
    /**
     * Called by SearchMessagesThread to add a found message.
     */
    public void addFoundMessage(final FrostSearchResultMessageObject msg) {
        // we were called from io thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // add msg to table
                getSearchMessagesTableModel().addRow(msg);
                resultCount++;
                updateResultCountLabel(resultCount);
            }
        });
    }
    
    /** 
     * This Document ensures that only digits can be entered into a text field.
     */ 
    protected class WholeNumberDocument extends PlainDocument {
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            for( int i = 0; i < result.length; i++ ) {
                if( Character.isDigit(source[i]) ) {
                    result[j++] = source[i];
                }
            }
            super.insertString(offs, new String(result, 0, j), a);
        }
    }
    
    List chosedBoardsList = new ArrayList();
    SearchMessagesThread runningSearchThread = null;
    int resultCount;

    private JLabel LresultCount = null;

}  //  @jve:decl-index=0:visual-constraint="10,10"
