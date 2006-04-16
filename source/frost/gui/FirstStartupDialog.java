/*
  FirstStartupDialog.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import frost.fcp.*;
import frost.util.gui.translation.*;

public class FirstStartupDialog extends JDialog {

    private static Language language = Language.getInstance();
    
    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JRadioButton RBfreenet05 = null;
    private JRadioButton RBfreenet07 = null;
    private JLabel jLabel1 = null;
    private JRadioButton RBnewIdentity = null;
    private JRadioButton RBimportIdentitiesFile = null;
    private JPanel PidentitiesFileChooser = null;
    private JPanel Pbuttons = null;
    private JButton Bexit = null;
    private JButton Bok = null;
    private JTextField TFidentitiesFilePath = null;
    private JButton Bchoose = null;
    private ButtonGroup BGfreenetVersion = null;  //  @jve:decl-index=0:visual-constraint="529,50"
    private ButtonGroup BGidentity = null;  //  @jve:decl-index=0:visual-constraint="547,103"

    private boolean exitChoosed;
    private int freenetVersion = 0;
    private String oldIdentitiesFile = null;
    
    public FirstStartupDialog() {
        super();
        setModal(true);
        initialize();
        // center on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension splashscreenSize = getSize();
        if (splashscreenSize.height > screenSize.height) {
            splashscreenSize.height = screenSize.height;
        }
        if (splashscreenSize.width > screenSize.width) {
            splashscreenSize.width = screenSize.width;
        }
        setLocation(
            (screenSize.width - splashscreenSize.width) / 2,
            (screenSize.height - splashscreenSize.height) / 2);
    }
    
    public boolean startDialog() {
        setVisible(true);
        return exitChoosed;
    }

    private void initialize() {
        this.setSize(424, 262);
        this.setTitle(language.getString("Frost first startup"));
        this.setContentPane(getJContentPane());
        
        getBGfreenetVersion();
        getBGidentity();
        
        getRBnewIdentity().doClick();
        getBok().setEnabled(false);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.gridwidth = 2;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.gridy = 7;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.gridwidth = 2;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints6.weighty = 1.0;
            gridBagConstraints6.gridy = 6;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints5.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints5.gridy = 5;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints4.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints4.gridy = 4;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.gridwidth = 2;
            gridBagConstraints3.insets = new java.awt.Insets(15,3,0,3);
            gridBagConstraints3.gridy = 3;
            jLabel1 = new JLabel();
            jLabel1.setText(language.getString("You can create a new identity or import an existing identities.xml file")+":");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints2.gridy = 2;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.insets = new java.awt.Insets(0,10,0,0);
            gridBagConstraints1.gridy = 1;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(3,3,0,3);
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText(language.getString("Please choose the version of Freenet you want to use")+":");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabel, gridBagConstraints);
            jContentPane.add(getRBfreenet05(), gridBagConstraints1);
            jContentPane.add(getRBfreenet07(), gridBagConstraints2);
            jContentPane.add(jLabel1, gridBagConstraints3);
            jContentPane.add(getRBnewIdentity(), gridBagConstraints4);
            jContentPane.add(getRBimportIdentitiesFile(), gridBagConstraints5);
            jContentPane.add(getPidentitiesFileChooser(), gridBagConstraints6);
            jContentPane.add(getPbuttons(), gridBagConstraints7);
        }
        return jContentPane;
    }

    /**
     * This method initializes RBfreenet05	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBfreenet05() {
        if( RBfreenet05 == null ) {
            RBfreenet05 = new JRadioButton();
            RBfreenet05.setText("Freenet 0.5");
            RBfreenet05.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getBok().setEnabled(true);
                }
            });
        }
        return RBfreenet05;
    }

    /**
     * This method initializes RBfreenet07	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBfreenet07() {
        if( RBfreenet07 == null ) {
            RBfreenet07 = new JRadioButton();
            RBfreenet07.setText("Freenet 0.7");
            RBfreenet07.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getBok().setEnabled(true);
                }
            });
        }
        return RBfreenet07;
    }

    /**
     * This method initializes RBnewIdentity	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBnewIdentity() {
        if( RBnewIdentity == null ) {
            RBnewIdentity = new JRadioButton();
            RBnewIdentity.setText(language.getString("Create new identity"));
            RBnewIdentity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    RBidentitySelectionChanged();
                }
            });
        }
        return RBnewIdentity;
    }

    /**
     * This method initializes RBimportIdentitiesFile	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBimportIdentitiesFile() {
        if( RBimportIdentitiesFile == null ) {
            RBimportIdentitiesFile = new JRadioButton();
            RBimportIdentitiesFile.setText(language.getString("Import existing identities.xml file"));
            RBimportIdentitiesFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    RBidentitySelectionChanged();
                }
            });
        }
        return RBimportIdentitiesFile;
    }

    /**
     * This method initializes PidentitiesFileChooser	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPidentitiesFileChooser() {
        if( PidentitiesFileChooser == null ) {
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.insets = new java.awt.Insets(5,3,0,20);
            gridBagConstraints9.gridy = 0;
            gridBagConstraints9.weighty = 1.0;
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints9.weightx = 1.0;
            gridBagConstraints9.gridx = 1;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.weightx = 0.0;
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints8.insets = new java.awt.Insets(8,15,0,2);
            PidentitiesFileChooser = new JPanel();
            PidentitiesFileChooser.setLayout(new GridBagLayout());
            PidentitiesFileChooser.add(getTFidentitiesFilePath(), gridBagConstraints8);
            PidentitiesFileChooser.add(getBchoose(), gridBagConstraints9);
        }
        return PidentitiesFileChooser;
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
            Pbuttons.add(getBok(), null);
            Pbuttons.add(getBexit(), null);
        }
        return Pbuttons;
    }

    /**
     * This method initializes Bexit	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBexit() {
        if( Bexit == null ) {
            Bexit = new JButton();
            Bexit.setText(language.getString("Exit"));
            Bexit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    exitChoosed = true;
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bexit;
    }

    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton();
            Bok.setText(language.getString("OK"));
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    exitChoosed = false;
                    if( getRBfreenet05().isSelected() ) {
                        freenetVersion = FcpHandler.FREENET_05;
                    } else if( getRBfreenet07().isSelected() ) {
                        freenetVersion = FcpHandler.FREENET_07;
                    }
                    if( getRBnewIdentity().isSelected() ) {
                        oldIdentitiesFile = null;
                    } else {
                        oldIdentitiesFile = getTFidentitiesFilePath().getText();
                    }
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes TFidentitiesFilePath	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFidentitiesFilePath() {
        if( TFidentitiesFilePath == null ) {
            TFidentitiesFilePath = new JTextField();
            TFidentitiesFilePath.setColumns(25);
        }
        return TFidentitiesFilePath;
    }

    /**
     * This method initializes Bchoose	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBchoose() {
        if( Bchoose == null ) {
            Bchoose = new JButton();
            Bchoose.setText("...");
            Bchoose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    chooseIdentitiesFile();
                }
            });
        }
        return Bchoose;
    }

    /**
     * This method initializes BGfreenetVersion	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getBGfreenetVersion() {
        if( BGfreenetVersion == null ) {
            BGfreenetVersion = new ButtonGroup();
            BGfreenetVersion.add(getRBfreenet05());
            BGfreenetVersion.add(getRBfreenet07());
        }
        return BGfreenetVersion;
    }

    /**
     * This method initializes BGidentity	
     * 	
     * @return javax.swing.ButtonGroup	
     */
    private ButtonGroup getBGidentity() {
        if( BGidentity == null ) {
            BGidentity = new ButtonGroup();
            BGidentity.add(getRBnewIdentity());
            BGidentity.add(getRBimportIdentitiesFile());
        }
        return BGidentity;
    }
    
    private void RBidentitySelectionChanged() {
        boolean enable;
        if( getRBnewIdentity().isSelected() ) {
            enable = false;
        } else {
            enable = true;
        }
        getTFidentitiesFilePath().setEnabled(enable);
        getBchoose().setEnabled(enable);
    }
    
    private void chooseIdentitiesFile() {
        
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().equals("identities.xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "identities.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            getTFidentitiesFilePath().setText(chooser.getSelectedFile().getPath());
        }
    }

    public boolean isExitChoosed() {
        return exitChoosed;
    }
    public int getFreenetVersion() {
        return freenetVersion;
    }
    public String getOldIdentitiesFile() {
        return oldIdentitiesFile;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
