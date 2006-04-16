/*
  FreenetVersionDialog.java / Frost
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

import javax.swing.*;

/**
 * After update to a Frost version that supports 0.5 and 0.7, ask user on which freenet version
 * this Frost run before.
 */
public class FreenetVersionDialog extends JDialog {

    private JPanel jContentPane = null;
    private JTextArea jTextArea = null;
    private JPanel jPanel = null;
    private JButton Bfreenet05 = null;
    private JButton Bfreenet07 = null;
    private JButton Bexit = null;
    
    private boolean choosedFreenet05 = false;
    private boolean choosedFreenet07 = false;
    private boolean choosedExit = false;
    
    /**
     * This is the default constructor
     */
    public FreenetVersionDialog() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(500, 300);
        this.setTitle("Choose Freenet version");
        this.setContentPane(getJContentPane());
        setModal(true);
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
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints1.weighty = 0.0;
            gridBagConstraints1.gridy = 1;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(3,3,3,3);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.gridx = 0;
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(getJTextArea(), gridBagConstraints);
            jContentPane.add(getJPanel(), gridBagConstraints1);
        }
        return jContentPane;
    }

    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getJTextArea() {
        if( jTextArea == null ) {
            jTextArea = new JTextArea();
            String txt =  
            "This new Frost version can run on the Freenet 0.5 network or the new Freenet 0.7 network. "+
            "Because you just updated an existing Frost installation, you need to choose what "+
            "Freenet version was used before with this Frost installation.\n"+
            "Please notice that you must choose the correct previous version because the Freenet key "+
            "format between 0.5 and 0.7 is different. If you want to upgrade from 0.5 to 0.7 "+"" +
            "please check the readme file for instructions how to do this.";
            jTextArea.setText(txt);
            jTextArea.setWrapStyleWord(true);
            jTextArea.setEnabled(true);
            jTextArea.setEditable(false);
            jTextArea.setLineWrap(true);
        }
        return jTextArea;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            jPanel = new JPanel();
            jPanel.add(getBfreenet05(), null);
            jPanel.add(getBfreenet07(), null);
            jPanel.add(getBexit(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes Bfreenet05	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBfreenet05() {
        if( Bfreenet05 == null ) {
            Bfreenet05 = new JButton();
            Bfreenet05.setText("Freenet 0.5");
            Bfreenet05.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    choosedFreenet05 = true;
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bfreenet05;
    }

    /**
     * This method initializes Bfreenet07	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBfreenet07() {
        if( Bfreenet07 == null ) {
            Bfreenet07 = new JButton();
            Bfreenet07.setText("Freenet 0.7");
            Bfreenet07.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    choosedFreenet07 = true;
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bfreenet07;
    }

    /**
     * This method initializes Bexit	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBexit() {
        if( Bexit == null ) {
            Bexit = new JButton();
            Bexit.setText("Exit");
            Bexit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    choosedExit = true;
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bexit;
    }

    public boolean isChoosedExit() {
        return choosedExit;
    }

    public boolean isChoosedFreenet05() {
        return choosedFreenet05;
    }

    public boolean isChoosedFreenet07() {
        return choosedFreenet07;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
