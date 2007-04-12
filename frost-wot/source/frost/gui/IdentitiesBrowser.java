/*
  IdentitiesBrowser.java / Frost
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
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import org.joda.time.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.gui.model.*;
import frost.identities.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class IdentitiesBrowser extends JDialog {

    private static final Logger logger = Logger.getLogger(MessageFrame.class.getName());

    private Language language = null;

    private JPanel jContentPane = null;
    private JScrollPane jScrollPane = null;
    private SortedTable identitiesTable = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;
    private JButton BmarkGOOD = null;
    private JButton BmarkOBSERVE = null;
    private JButton BmarkCHECK = null;
    private JButton BmarkBAD = null;
    private JButton Bdelete = null;
    
    private InnerTableModel tableModel = null;

    private JButton Bcleanup = null;
    private JFrame parent;
    
    private List<InnerTableMember> allTableMembers;
    
    private boolean showColoredLines;

    /**
     * This is the default constructor
     */
    public IdentitiesBrowser(JFrame parent) {
        super(parent);
        this.parent = parent;
        language = Language.getInstance();
        setModal(true);
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        initialize();
        
        setLocationRelativeTo(parent);
    }
    
    /**
     * This method initializes this
     */
    private void initialize() {
        this.setTitle("IdentitiesBrowser.title");
        this.setBounds(new java.awt.Rectangle(0,0,600,420));
        this.setContentPane(getJContentPane());
        
        getBmarkGOOD().setText("");
        getBmarkBAD().setText("");
        getBmarkCHECK().setText("");
        getBmarkOBSERVE().setText("");
        
        getBmarkGOOD().setIcon(new ImageIcon(getClass().getResource("/data/trust.gif")));
        getBmarkBAD().setIcon(new ImageIcon(getClass().getResource("/data/nottrust.gif")));
        getBmarkCHECK().setIcon(new ImageIcon(getClass().getResource("/data/check.gif")));
        getBmarkOBSERVE().setIcon(new ImageIcon(getClass().getResource("/data/observe.gif")));
        
        MiscToolkit toolkit = MiscToolkit.getInstance();
        toolkit.configureButton(getBmarkGOOD(), "MessagePane.toolbar.tooltip.setToGood", "/data/trust_rollover.gif", language);
        toolkit.configureButton(getBmarkBAD(), "MessagePane.toolbar.tooltip.setToBad", "/data/nottrust_rollover.gif", language);
        toolkit.configureButton(getBmarkCHECK(), "MessagePane.toolbar.tooltip.setToCheck", "/data/check_rollover.gif", language);
        toolkit.configureButton(getBmarkOBSERVE(), "MessagePane.toolbar.tooltip.setToObserve", "/data/observe_rollover.gif", language);
        
        setTitle(language.getString("IdentitiesBrowser.title"));
        getBdelete().setText(language.getString("IdentitiesBrowser.button.delete"));
        getBcleanup().setText(language.getString("IdentitiesBrowser.button.cleanup"));
        getBcleanup().setToolTipText(language.getString("IdentitiesBrowser.button.cleanup.tooltip"));
        getBclose().setText(language.getString("IdentitiesBrowser.button.close"));
        getBimport().setText(language.getString("IdentitiesBrowser.button.import"));
        getBexport().setText(language.getString("IdentitiesBrowser.button.export"));
        
        Lfilter.setText(language.getString("IdentitiesBrowser.label.filter")+":");
        Llookup.setText(language.getString("IdentitiesBrowser.label.lookup")+":");
    }
    
    private void updateTitle() {
        int idCount = tableModel.getRowCount();
        setTitle(language.formatMessage("IdentitiesBrowser.title", Integer.toString(idCount)));
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
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getIdentitiesTable());
            jScrollPane.setWheelScrollingEnabled(true);
        }
        return jScrollPane;
    }

    /**
     * This method initializes identitiesTable
     *
     * @return javax.swing.JTable
     */
    private SortedTable getIdentitiesTable() {
        if( identitiesTable == null ) {
            tableModel = new InnerTableModel();
            identitiesTable = new SortedTable(tableModel);
            // set column sizes
            int[] widths = { 130, 30, 70, 20, 20 };
            for (int i = 0; i < widths.length; i++) {
                identitiesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            
            ShowColoredLinesRenderer showColoredLinesRenderer = new ShowColoredLinesRenderer();
            identitiesTable.getColumnModel().getColumn(0).setCellRenderer(showColoredLinesRenderer);
            identitiesTable.getColumnModel().getColumn(1).setCellRenderer(new StringCellRenderer());
            identitiesTable.getColumnModel().getColumn(2).setCellRenderer(showColoredLinesRenderer);
            identitiesTable.getColumnModel().getColumn(3).setCellRenderer(showColoredLinesRenderer);
            identitiesTable.getColumnModel().getColumn(4).setCellRenderer(showColoredLinesRenderer);
            
            identitiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int[] selRows = identitiesTable.getSelectedRows();

                    // nothing selected
                    if( selRows.length == 0 ) {
                        // disable all
                        getBdelete().setEnabled(false);
                        getBdelete().setToolTipText(null);
                        getBmarkBAD().setEnabled(false);
                        getBmarkCHECK().setEnabled(false);
                        getBmarkGOOD().setEnabled(false);
                        getBmarkOBSERVE().setEnabled(false);
                        return;
                    } 
                    
                    if( selRows.length == 1 ) {
                        // one selected: enable good,bad,... buttons, disable button with current state
                        Identity id = ((InnerTableMember)tableModel.getRow(selRows[0])).getIdentity();
                        // setting all together avoids flickering buttons
                        if( id.isBAD() ) {
                            getBmarkBAD().setEnabled(false);
                            getBmarkCHECK().setEnabled(true);
                            getBmarkGOOD().setEnabled(true);
                            getBmarkOBSERVE().setEnabled(true);
                        } else if( id.isCHECK() ) {
                            getBmarkBAD().setEnabled(true);
                            getBmarkCHECK().setEnabled(false);
                            getBmarkGOOD().setEnabled(true);
                            getBmarkOBSERVE().setEnabled(true);
                        } else if( id.isGOOD() ) {
                            getBmarkBAD().setEnabled(true);
                            getBmarkCHECK().setEnabled(true);
                            getBmarkGOOD().setEnabled(false);
                            getBmarkOBSERVE().setEnabled(true);
                        } else if( id.isOBSERVE() ) {
                            getBmarkBAD().setEnabled(true);
                            getBmarkCHECK().setEnabled(true);
                            getBmarkGOOD().setEnabled(true);
                            getBmarkOBSERVE().setEnabled(false);
                        }
                    } else {
                        // multiple selected: enable all buttons
                        getBmarkBAD().setEnabled(true);
                        getBmarkCHECK().setEnabled(true);
                        getBmarkGOOD().setEnabled(true);
                        getBmarkOBSERVE().setEnabled(true);
                    }
                    
                    // if one in selection has more than 0 msgs / files, disable delete button
                    boolean enableDelete = true;
                    for( int i = 0; i < selRows.length; i++ ) {
                        if( ((InnerTableMember)tableModel.getRow(selRows[i])).isDeleteable() == false ) {
                            enableDelete = false;
                            break;
                        }
                    }
                    if( enableDelete ) {
                        if( selRows.length > 1 ) {
                            getBdelete().setToolTipText(language.getString("IdentitiesBrowser.button.delete.tooltip.multiple"));
                        } else {
                            getBdelete().setToolTipText(language.getString("IdentitiesBrowser.button.delete.tooltip.single"));
                        }
                    } else {
                        getBdelete().setToolTipText(null);
                    }
                    getBdelete().setEnabled(enableDelete);
                }
            });
        }
        return identitiesTable;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            Lfilter = new JLabel();
            Lfilter.setText("IdentitiesBrowser.label.filter");
            Llookup = new JLabel();
            Llookup.setText("IdentitiesBrowser.label.lookup");
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(getButtonPanel(), BoxLayout.X_AXIS));
            buttonPanel.add(Box.createRigidArea(new Dimension(5,3)));
            buttonPanel.add(Llookup, null);
            buttonPanel.add(Box.createRigidArea(new Dimension(5,3)));
            buttonPanel.add(getTFlookup(), null);
            buttonPanel.add(Box.createRigidArea(new Dimension(5,3)));
            buttonPanel.add(Lfilter, null);
            buttonPanel.add(Box.createRigidArea(new Dimension(5,3)));
            buttonPanel.add(getTFfilter(), null);
            buttonPanel.add( Box.createHorizontalGlue() );
            buttonPanel.add(getBclose(), null);
            buttonPanel.add(Box.createRigidArea(new Dimension(5,3)));
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.gridheight = 1;
            gridBagConstraints21.gridwidth = 4;
            gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints21.weighty = 1.0;
            gridBagConstraints21.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints21.gridy = 4;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 1;
            gridBagConstraints11.gridwidth = 4;
            gridBagConstraints11.insets = new java.awt.Insets(15,5,5,5);
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints11.gridy = 3;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.gridwidth = 4;
            gridBagConstraints6.insets = new java.awt.Insets(15,5,0,5);
            gridBagConstraints6.weighty = 0.0;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints6.gridy = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 1;
            gridBagConstraints5.gridwidth = 4;
            gridBagConstraints5.weighty = 0.0;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTH;
            gridBagConstraints5.insets = new java.awt.Insets(15,5,5,5);
            gridBagConstraints5.gridy = 2;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 4;
            gridBagConstraints4.insets = new java.awt.Insets(5,0,0,5);
            gridBagConstraints4.gridy = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 3;
            gridBagConstraints3.insets = new java.awt.Insets(5,0,0,0);
            gridBagConstraints3.gridy = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 2;
            gridBagConstraints2.insets = new java.awt.Insets(5,0,0,0);
            gridBagConstraints2.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.insets = new java.awt.Insets(5,0,0,0);
            gridBagConstraints1.gridy = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridheight = 5;
            gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(getJScrollPane(), gridBagConstraints);
            mainPanel.add(getBmarkGOOD(), gridBagConstraints1);
            mainPanel.add(getBmarkOBSERVE(), gridBagConstraints2);
            mainPanel.add(getBmarkCHECK(), gridBagConstraints3);
            mainPanel.add(getBmarkBAD(), gridBagConstraints4);
            mainPanel.add(getBdelete(), gridBagConstraints5);
            mainPanel.add(getBcleanup(), gridBagConstraints6);
            mainPanel.add(getBimport(), gridBagConstraints11);
            mainPanel.add(getBexport(), gridBagConstraints21);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bclose	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText("IdentitiesBrowser.button.close");
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                    // update messages if a board is shown
                    MainFrame.getInstance().getMessagePanel().updateTableAfterChangeOfIdentityState();
                }
            });
        }
        return Bclose;
    }

    /**
     * This method initializes BmarkGOOD	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBmarkGOOD() {
        if( BmarkGOOD == null ) {
            BmarkGOOD = new JButton();
            BmarkGOOD.setText("G");
            BmarkGOOD.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int[] selRows = getIdentitiesTable().getSelectedRows();
                    for( int x=0; x < selRows.length; x++ ) {
                        InnerTableMember itm = (InnerTableMember)tableModel.getRow(selRows[x]);
                        Identity id = itm.getIdentity();
                        if( id.isGOOD() == false ) {
                            id.setGOOD();
                        }
                        tableModel.updateRow(itm);
                    }
                }
            });
        }
        return BmarkGOOD;
    }

    /**
     * This method initializes BmarkOBSERVE	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBmarkOBSERVE() {
        if( BmarkOBSERVE == null ) {
            BmarkOBSERVE = new JButton();
            BmarkOBSERVE.setText("O");
            BmarkOBSERVE.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int[] selRows = getIdentitiesTable().getSelectedRows();
                    for( int x=0; x < selRows.length; x++ ) {
                        InnerTableMember itm = (InnerTableMember)tableModel.getRow(selRows[x]);
                        Identity id = itm.getIdentity();
                        if( id.isOBSERVE() == false ) {
                            id.setOBSERVE();
                        }
                        tableModel.updateRow(itm);
                    }
                }
            });
        }
        return BmarkOBSERVE;
    }

    /**
     * This method initializes BmarkCHECK	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBmarkCHECK() {
        if( BmarkCHECK == null ) {
            BmarkCHECK = new JButton();
            BmarkCHECK.setText("C");
            BmarkCHECK.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int[] selRows = getIdentitiesTable().getSelectedRows();
                    for( int x=0; x < selRows.length; x++ ) {
                        InnerTableMember itm = (InnerTableMember)tableModel.getRow(selRows[x]);
                        Identity id = itm.getIdentity();
                        if( id.isCHECK() == false ) {
                            id.setCHECK();
                        }
                        tableModel.updateRow(itm);
                    }
                }
            });
        }
        return BmarkCHECK;
    }

    /**
     * This method initializes BmarkBAD	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBmarkBAD() {
        if( BmarkBAD == null ) {
            BmarkBAD = new JButton();
            BmarkBAD.setText("B");
            BmarkBAD.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int[] selRows = getIdentitiesTable().getSelectedRows();
                    for( int x=0; x < selRows.length; x++ ) {
                        InnerTableMember itm = (InnerTableMember)tableModel.getRow(selRows[x]);
                        Identity id = itm.getIdentity();
                        if( id.isBAD() == false ) {
                            id.setBAD();
                        }
                        tableModel.updateRow(itm);
                    }
                }
            });
        }
        return BmarkBAD;
    }

    /**
     * This method initializes Bdelete	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBdelete() {
        if( Bdelete == null ) {
            Bdelete = new JButton();
            Bdelete.setText("IdentitiesBrowser.button.delete");
            Bdelete.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int[] selRows = getIdentitiesTable().getSelectedRows();
                    int answer = JOptionPane.showConfirmDialog(
                            IdentitiesBrowser.this,
                            language.formatMessage("IdentitiesBrowser.deleteDialog.body", Integer.toString(selRows.length)), 
                            language.getString("IdentitiesBrowser.deleteDialog.title"), 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if( answer == JOptionPane.NO_OPTION ) {
                        return;
                    }
                    Arrays.sort(selRows); // ensure sorted, we must delete from end to begin 
                    for( int x=selRows.length-1; x>=0; x-- ) {
                        
                        InnerTableMember m = (InnerTableMember)tableModel.getRow(selRows[x]);
                        Identity id = m.getIdentity();
                        Core.getIdentities().deleteIdentity(id);

                        tableModel.removeRow(selRows[x]);
                    }
                    updateTitle();
                }
            });
        }
        return Bdelete;
    }
    
    class InnerTableMember implements TableMember {
        
        Identity identity;
        Integer msgCount;
        Integer fileCount;
        String lastSeenStr;
        String htmlName;
        
        public InnerTableMember(Identity i) {
            identity = i;
            msgCount = retrieveMessageCount(i);
            fileCount = retrieveFileCount(i);
            lastSeenStr = buildLastSeenString(identity.getLastSeenTimestamp());
            htmlName = buildHtmlName(i.getUniqueName());
        }
        public Identity getIdentity() {
            return identity;
        }
        public boolean isDeleteable() {
            if( msgCount.intValue() == 0 && fileCount.intValue() == 0 ) {
                return true;
            }
            return false;
        }
        private String buildHtmlName(String n) {
            // TODO: html mode wraps words with blanks, maybe replace blanks by &nbsp;
//            String a = n.substring(0, n.indexOf("@"));
//            String b = n.substring(n.indexOf("@"));
//            String r = "<html><b>"+a+"</b>"+b+"</html>";
//            System.out.println("r="+r);
//            return r;
            return n;
        }
        private Integer retrieveFileCount(Identity id) {
            int i = 0;
            try {
                i = AppLayerDatabase.getFileListDatabaseTable().getFileCountForIdentity(id);
                fileCount = new Integer(i);
            } catch(SQLException ex) {
                logger.log(Level.SEVERE, "Exception counting files", ex);
            }
            return new Integer(i);
        }
        private Integer retrieveMessageCount(Identity id) {
            int i = 0;
            try {
                i = AppLayerDatabase.getMessageTable().getMessageCountByIdentity(id, -1);
            } catch(SQLException ex) {
                logger.log(Level.SEVERE, "Exception counting msgs", ex);
            }
            return new Integer(i);
        }
        private String buildLastSeenString(long lastSeen) {
            // date (days_before)
            if( lastSeen < 0 ) {
                // not set!
                return "";
            }
            String lsStr = DateFun.FORMAT_DATE_EXT.print(lastSeen);
            long days = new DateMidnight(DateTimeZone.UTC).getMillis() - new DateMidnight(lastSeen, DateTimeZone.UTC).getMillis(); 
            days /= 1000L * 60L * 60L * 24L;
            lsStr += "  ("+days+")";
            
            return lsStr;
        }
        public Object getValueAt(int column) {
            switch(column) {
                case 0: return htmlName;
                case 1: return getIdentity().getStateString();
                case 2: return lastSeenStr;
                case 3: return msgCount;
                case 4: return fileCount;
            }
            return "*ERR*";
        }
        
        public int compareTo(TableMember anOther, int tableColumnIndex) {
            if( tableColumnIndex == 0 || tableColumnIndex == 1 ) {
                String s1 = (String)getValueAt(tableColumnIndex);
                String s2 = (String)anOther.getValueAt(tableColumnIndex);
                return s1.compareToIgnoreCase(s2);
            }
            if( tableColumnIndex == 2 ) {
                long l1 = getIdentity().getLastSeenTimestamp();
                long l2 = ((InnerTableMember)anOther).getIdentity().getLastSeenTimestamp();
                if( l1 > l2 ) {
                    return 1;
                }
                if( l1 < l2 ) {
                    return -1;
                }
                return 0;
            }
            if( tableColumnIndex == 3 ) {
                Integer i1 = (Integer)getValueAt(tableColumnIndex);
                Integer i2 = (Integer)anOther.getValueAt(tableColumnIndex);
                int res = i1.compareTo(i2);
                if( res == 0) {
                    // same msgcount, compare filecount 
                    i1 = (Integer)getValueAt(4);
                    i2 = (Integer)anOther.getValueAt(4);
                    return i1.compareTo(i2);
                }
                return res;
            }
            if( tableColumnIndex == 4 ) {
                Integer i1 = (Integer)getValueAt(tableColumnIndex);
                Integer i2 = (Integer)anOther.getValueAt(tableColumnIndex);
                int res = i1.compareTo(i2);
                if( res == 0) {
                    // same msgcount, compare filecount 
                    i1 = (Integer)getValueAt(3);
                    i2 = (Integer)anOther.getValueAt(3);
                    return i1.compareTo(i2);
                }
                return res;
            }
            return 0;
        }
    }

    public class InnerTableModel extends SortedTableModel {

        protected final String columnNames[] = new String[5];

        protected final Class columnClasses[] = {
            String.class, // name
            String.class, // state
            String.class, // lastSeen,
            Integer.class, // msgs
            Integer.class  // files
        };

        public InnerTableModel() {
            super();
            setLanguage();
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public String getColumnName(int column) {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Class<?> getColumnClass(int column) {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }

        private void setLanguage() {
            columnNames[0] = language.getString("IdentitiesBrowser.identitiesTable.name");
            columnNames[1] = language.getString("IdentitiesBrowser.identitiesTable.state");
            columnNames[2] = language.getString("IdentitiesBrowser.identitiesTable.lastSeen");
            columnNames[3] = language.getString("IdentitiesBrowser.identitiesTable.messages");
            columnNames[4] = language.getString("IdentitiesBrowser.identitiesTable.files");
        }
    }
    
    private class StringCellRenderer extends ShowColoredLinesRenderer {

        private Font boldFont = null;
        private Font normalFont = null;
        private final Color col_good    = new Color(0x00, 0x80, 0x00);
        private final Color col_check   = new Color(0xFF, 0xCC, 0x00);
        private final Color col_observe = new Color(0x00, 0xD0, 0x00);
        private final Color col_bad     = new Color(0xFF, 0x00, 0x00);

        public StringCellRenderer() {
            Font baseFont = getIdentitiesTable().getFont();
            normalFont = baseFont.deriveFont(Font.PLAIN);
            boldFont = baseFont.deriveFont(Font.BOLD);

            setVerticalAlignment(CENTER);
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setAlignmentY(CENTER_ALIGNMENT);

            InnerTableMember tableMember = (InnerTableMember) tableModel.getRow(row);

            // get the original model column index (maybe columns were reordered by user)
            TableColumn tableColumn = getIdentitiesTable().getColumnModel().getColumn(column);
            column = tableColumn.getModelIndex();

            // defaults
            setFont(normalFont);
            if (!isSelected) {
                setForeground(Color.BLACK);
            }
            setToolTipText(null);

            if( column == 0 ) {
                setToolTipText(tableMember.getIdentity().getUniqueName());
            } else if( column == 1 ) {
                Identity id = tableMember.getIdentity();
                // STATE
                // state == good/bad/check/observe -> bold and coloured
                if (Core.getIdentities().isMySelf(id.getUniqueName())) {
                    setText("ME");
                    setFont(boldFont);
                    setForeground(col_good);
                } else if( id.isGOOD() ) {
                    setFont(boldFont);
                    setForeground(col_good);
                } else if( id.isCHECK() ) {
                    setFont(boldFont);
                    setForeground(col_check);
                } else if( id.isOBSERVE() ) {
                    setFont(boldFont);
                    setForeground(col_observe);
                } else if( id.isBAD() ) {
                    setFont(boldFont);
                    setForeground(col_bad);
                }
            }
            return this;
        }
    }
    
    private ProgressMonitor progressMonitor;

    private JLabel Llookup = null;

    private JTextField TFlookup = null;

    private JLabel Lfilter = null;

    private JTextField TFfilter = null;

    private JButton Bimport = null;

    private JButton Bexport = null;
    
    private void startProgressMonitor(int max) {
        String title = language.getString("IdentitiesBrowser.progressDialog.title");
        String msg = language.getString("IdentitiesBrowser.progressDialog.body");
        UIManager.put("ProgressMonitor.progressText", title);
        progressMonitor = new ProgressMonitor(parent, msg, null, 0, max);
//        progressMonitor.setNote(0+"/"+max);
    }
    
    public void startDialog() {
        final int idCount = Core.getIdentities().getIdentities().size();
        
        startProgressMonitor(idCount);

        // disables mainframe
        FrostSwingWorker worker = new FrostSwingWorker(parent) {
            protected void doNonUILogic() throws RuntimeException {
                allTableMembers = new LinkedList<InnerTableMember>(); // remember all table data for filter
                List<Identity> allIdentities = Core.getIdentities().getIdentities();
                // show own identities also
                allIdentities.addAll(Core.getIdentities().getLocalIdentities());
                int count = 0;
                for( Iterator iter = allIdentities.iterator(); iter.hasNext(); ) {
                    Identity identity = (Identity) iter.next();
                    InnerTableMember memb = new InnerTableMember(identity);
                    tableModel.addRow(memb);
                    allTableMembers.add(memb);
                    count++;
                    progressMonitor.setProgress(count);
                    if( progressMonitor.isCanceled() ) {
                        break;
                    }
                }
            }
            protected void doUIUpdateLogic() throws RuntimeException {
                updateTitle();
                showDialog();
            }
        };
        worker.start();
    }
    
    private void showDialog() {
        if( progressMonitor.isCanceled() ) {
            progressMonitor.close();
        } else {
            progressMonitor.close();
            getIdentitiesTable().getSelectionModel().setSelectionInterval(0, 0);
            setVisible(true);
        }
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcleanup() {
        if( Bcleanup == null ) {
            Bcleanup = new JButton();
            Bcleanup.setText("IdentitiesBrowser.button.cleanup");
            Bcleanup.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    LinkedList<Integer> li = new LinkedList<Integer>();
                    for( int i=tableModel.getRowCount()-1; i >= 0; i-- ) {
                        InnerTableMember m = (InnerTableMember)tableModel.getRow(i);
                        if( m.isDeleteable() ) {
                            li.add(new Integer(i));
                        }
                    }
                    if( li.size() == 0 ) {
                        JOptionPane.showMessageDialog(
                                IdentitiesBrowser.this, 
                                language.getString("IdentitiesBrowser.cleanupDialog.nothingToDelete.body"), 
                                language.getString("IdentitiesBrowser.cleanupDialog.nothingToDelete.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    int answer = JOptionPane.showConfirmDialog(
                            IdentitiesBrowser.this,
                            language.formatMessage("IdentitiesBrowser.cleanupDialog.deleteIdentities.body", Integer.toString(li.size())), 
                            language.getString("IdentitiesBrowser.cleanupDialog.deleteIdentities.title"), 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if( answer == JOptionPane.NO_OPTION ) {
                        return;
                    }

                    // batch delete, turn off autocommit
                    try {
                        AppLayerDatabase.getInstance().setAutoCommitOff();
                        for( Iterator iter = li.iterator(); iter.hasNext(); ) {
                            Integer element = (Integer) iter.next();
                            InnerTableMember m = (InnerTableMember)tableModel.getRow(element.intValue());
                            Identity id = m.getIdentity();
                            Core.getIdentities().deleteIdentity(id);
                            tableModel.removeRow(element.intValue());
                        }
                        AppLayerDatabase.getInstance().commit();
                        AppLayerDatabase.getInstance().setAutoCommitOn();
                    } catch(Throwable t) {
                        logger.log(Level.SEVERE, "database exception", t);
                    }
                    updateTitle();
                }
            });
        }
        return Bcleanup;
    }

    /**
     * This method initializes TFlookup	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFlookup() {
        if( TFlookup == null ) {
            TFlookup = new JTextField(10);
            // force a max size, needed for BoxLayout
            TFlookup.setMaximumSize(TFlookup.getPreferredSize());
            TFlookup.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        lookupContentChanged();
                    }
                    public void insertUpdate(DocumentEvent e) {
                        lookupContentChanged();
                    }
                    public void removeUpdate(DocumentEvent e) {
                        lookupContentChanged();
                    }
                });
        }
        return TFlookup;
    }

    /**
     * This method initializes TFfilter	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFfilter() {
        if( TFfilter == null ) {
            TFfilter = new JTextField(10);
            TFfilter.setMaximumSize(TFfilter.getPreferredSize());

            TFfilter.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        filterContentChanged();
                    }
                    public void insertUpdate(DocumentEvent e) {
                        filterContentChanged();
                    }
                    public void removeUpdate(DocumentEvent e) {
                        filterContentChanged();
                    }
                });
        }
        return TFfilter;
    }
    
    /**
     * Called whenever the content of the lookup text field changes
     */
    private void lookupContentChanged() {
        try {
            String txt = TFlookup.getDocument().getText(0, TFlookup.getDocument().getLength());
            // now try to find the first board name that starts with this txt (case insensitiv),
            // if we found one set selection to it, else leave selection untouched
            for( int row=0; row < tableModel.getRowCount(); row++ ) {
                InnerTableMember memb = (InnerTableMember)tableModel.getRow(row);
                if( memb.getIdentity().getUniqueName().toLowerCase().startsWith(txt.toLowerCase()) ) {
                    getIdentitiesTable().getSelectionModel().setSelectionInterval(row, row);
                    // now scroll to selected row, try to show it on top of table

                    // determine the count of showed rows
                    int visibleRows = (int)(getIdentitiesTable().getVisibleRect().getHeight() / getIdentitiesTable().getCellRect(row,0,true).getHeight());
                    int scrollToRow;
                    if( row + visibleRows > tableModel.getRowCount() ) {
                        scrollToRow = tableModel.getRowCount()-1;
                    } else {
                        scrollToRow = row + visibleRows - 1;
                    }
                    if( scrollToRow > row ) scrollToRow--;
                    // scroll 2 times to make sure row is displayed
                    getIdentitiesTable().scrollRectToVisible(getIdentitiesTable().getCellRect(row,0,true));
                    getIdentitiesTable().scrollRectToVisible(getIdentitiesTable().getCellRect(scrollToRow,0,true));
                    break;
                }
            }
        } catch(Exception ex) {}
    }

    /**
     * Called whenever the content of the filter text field changes
     */
    private void filterContentChanged() {
        try {
            TFlookup.setText(""); // clear
            String txt = TFfilter.getDocument().getText(0, TFfilter.getDocument().getLength()).trim();
            txt = txt.toLowerCase();
            // filter: show all boards that have this txt in name
            tableModel.clearDataModel();
            for(Iterator i = allTableMembers.iterator(); i.hasNext();  ) {
                InnerTableMember tm = (InnerTableMember)i.next();
                if( txt.length() > 0 ) {
                    String bn = tm.getIdentity().getUniqueName().toLowerCase();
                    if( bn.indexOf(txt) < 0 ) {
                        continue;
                    }
                }
                tableModel.addRow(tm);
            }
        } catch(Exception ex) {}
    }
    
    private File chooseXmlImportFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "identities_export.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private File chooseXmlExportFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "identities_export.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if( !f.getName().endsWith(".xml") ) {
                f = new File(f.getPath() + ".xml");
            }
            if( f.exists() ) {
                int answer = JOptionPane.showConfirmDialog(
                        this,
                        language.formatMessage("IdentitiesBrowser.exportIdentitiesConfirmXmlFileOverwrite.body", f.getName()),
                        language.getString("IdentitiesBrowser.exportIdentitiesConfirmXmlFileOverwrite.title"), 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                if( answer == JOptionPane.NO_OPTION ) {
                    return null;
                }
            }
            return f;
        }
        return null;
    }

    /**
     * This method initializes Bimport	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBimport() {
        if( Bimport == null ) {
            Bimport = new JButton();
            Bimport.setText("IdentitiesBrowser.button.import");
            Bimport.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File xmlFile = chooseXmlImportFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    
                    List importedIdentities = IdentitiesXmlDAO.loadIdentities(xmlFile);
                    if( importedIdentities.size() == 0 ) {
                        // nothing loaded
                        JOptionPane.showMessageDialog(
                                IdentitiesBrowser.this, 
                                language.getString("IdentitiesBrowser.noIdentityToImport.body"),
                                language.getString("IdentitiesBrowser.noIdentityToImport.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    int importedCount = Core.getIdentities().importIdentities(importedIdentities);
                    int skippedCount = importedIdentities.size() - importedCount;

                    JOptionPane.showMessageDialog(
                            IdentitiesBrowser.this, 
                            language.formatMessage(
                                    "IdentitiesBrowser.identitiesImported.body", 
                                    Integer.toString(importedCount), 
                                    Integer.toString(skippedCount)), 
                                language.getString("IdentitiesBrowser.identitiesImported.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                    
                    updateTitle();
                }
            });
        }
        return Bimport;
    }

    /**
     * This method initializes Bexport	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBexport() {
        if( Bexport == null ) {
            Bexport = new JButton();
            Bexport.setText("IdentitiesBrowser.button.export");
            Bexport.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File xmlFile = chooseXmlExportFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    List<Identity> allIdentities = Core.getIdentities().getIdentities();
                    // saves only good,observe,bad
                    int count = IdentitiesXmlDAO.saveIdentities(xmlFile, allIdentities);
                    if( count > 0 ) {
                        // 'count' identities exported
                        JOptionPane.showMessageDialog(
                                IdentitiesBrowser.this, 
                                language.formatMessage("IdentitiesBrowser.identitiesExported.body", Integer.toString(count)), 
                                language.getString("IdentitiesBrowser.identitiesExported.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                    } else if( count < 0 ) {
                        // identities export failed
                        JOptionPane.showMessageDialog(
                                IdentitiesBrowser.this, 
                                language.getString("IdentitiesBrowser.identitiesExportFailed.body"), 
                                language.getString("IdentitiesBrowser.identitiesExportFailed.title"), 
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        // no identities to export, all are CHECK?
                        JOptionPane.showMessageDialog(
                                IdentitiesBrowser.this, 
                                language.getString("IdentitiesBrowser.noIdentityToExport.body"), 
                                language.getString("IdentitiesBrowser.noIdentityToExport.title"), 
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }
        return Bexport;
    }
    
    private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
