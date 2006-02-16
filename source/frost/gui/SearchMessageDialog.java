/*
 * Created on 20.10.2005
 */
package frost.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class SearchMessageDialog extends JDialog {

    private JPanel jContentPane = null;
    private JPanel centerPanel = null;
    private JPanel buttonPanel = null;
    private JButton Bclose = null;
    private JButton Bsearch = null;
    private JLabel Lsender = null;
    private JTextField TFauthor = null;
    private JLabel Lcontent = null;
    private JLabel Ldate = null;
    private JLabel Lstate = null;
    private JLabel Lboard = null;
    private JTextField TFcontent = null;
    private JPanel datePanel = null;
    private JRadioButton jRadioButton = null;
    private JRadioButton jRadioButton1 = null;
    private JRadioButton jRadioButton2 = null;
    private JTextField jTextField = null;
    private JTextField jTextField1 = null;
    private JTextField jTextField2 = null;
    private JPanel jPanel1 = null;
    private JRadioButton jRadioButton3 = null;
    private JRadioButton jRadioButton4 = null;
    private JRadioButton jRadioButton5 = null;
    private JPanel jPanel2 = null;
    private JCheckBox jCheckBox = null;
    private JCheckBox jCheckBox1 = null;
    private JCheckBox jCheckBox2 = null;
    private JCheckBox jCheckBox3 = null;
    private JCheckBox jCheckBox4 = null;
    private JCheckBox jCheckBox5 = null;
    private JPanel jPanel3 = null;
    private JRadioButton jRadioButton6 = null;
    private JRadioButton jRadioButton7 = null;
    private JRadioButton jRadioButton8 = null;
    private JTextField jTextField3 = null;
    private JLabel glueLabel3 = null;
    private JButton jButton = null;
    private JLabel jLabel = null;
    private JLabel jLabel1 = null;
    private JScrollPane jScrollPane = null;
    private JTable jTable = null;
    private JCheckBox CBprivateMsgsOnly = null;
    private JPanel ParchiveSearch = null;
    private JRadioButton RBsearchNormalAndArc = null;
    private JRadioButton RBsearchNormalOnly = null;
    private JRadioButton RBsearchArcOnly = null;
    private JLabel Lglue = null;
    /**
     * This is the default constructor
     */
    public SearchMessageDialog() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(700, 550);
        this.setTitle("Search messages");
        this.setContentPane(getJContentPane());
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
            jContentPane.add(getCenterPanel(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes centerPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getCenterPanel() {
        if( centerPanel == null ) {
            GridBagConstraints gridBagConstraints211 = new GridBagConstraints();
            gridBagConstraints211.gridx = 1;
            gridBagConstraints211.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints211.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints211.gridheight = 1;
            gridBagConstraints211.gridy = 3;
            GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
            gridBagConstraints110.gridx = 1;
            gridBagConstraints110.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints110.gridy = 2;
            GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
            gridBagConstraints210.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints210.gridy = 9;
            gridBagConstraints210.weightx = 1.0;
            gridBagConstraints210.weighty = 1.0;
            gridBagConstraints210.gridwidth = 2;
            gridBagConstraints210.insets = new java.awt.Insets(1,5,3,5);
            gridBagConstraints210.gridx = 0;
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.gridx = 0;
            gridBagConstraints14.gridwidth = 2;
            gridBagConstraints14.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints14.insets = new java.awt.Insets(3,5,1,5);
            gridBagConstraints14.gridy = 8;
            jLabel1 = new JLabel();
            jLabel1.setText("Search Result");
            GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
            gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints51.gridy = 6;
            gridBagConstraints51.gridx = 1;
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            gridBagConstraints41.gridx = 1;
            gridBagConstraints41.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints41.weightx = 0.0D;
            gridBagConstraints41.weighty = 0.0D;
            gridBagConstraints41.gridy = 6;
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints21.gridy = 5;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 1;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints11.gridy = 4;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints6.gridx = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints5.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints5.gridy = 6;
            Lboard = new JLabel();
            Lboard.setText("Board");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints4.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints4.gridy = 5;
            Lstate = new JLabel();
            Lstate.setText("Trust state");
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.gridy = 4;
            Ldate = new JLabel();
            Ldate.setText("Date");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints2.gridy = 1;
            Lcontent = new JLabel();
            Lcontent.setText("Content");
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.gridx = 1;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.gridy = 0;
            Lsender = new JLabel();
            Lsender.setText("Sender");
            centerPanel = new JPanel();
            centerPanel.setLayout(new GridBagLayout());
            centerPanel.add(Lsender, gridBagConstraints);
            centerPanel.add(getTFauthor(), gridBagConstraints1);
            centerPanel.add(Lcontent, gridBagConstraints2);
            centerPanel.add(getTFcontent(), gridBagConstraints6);
            centerPanel.add(Ldate, gridBagConstraints3);
            centerPanel.add(Lstate, gridBagConstraints4);
            centerPanel.add(Lboard, gridBagConstraints5);
            centerPanel.add(getDatePanel(), gridBagConstraints11);
            centerPanel.add(getJPanel1(), gridBagConstraints21);
            centerPanel.add(getJPanel3(), gridBagConstraints41);
            centerPanel.add(jLabel1, gridBagConstraints14);
            centerPanel.add(getJScrollPane(), gridBagConstraints210);
            centerPanel.add(getCBprivateMsgsOnly(), gridBagConstraints110);
            centerPanel.add(getParchiveSearch(), gridBagConstraints211);
        }
        return centerPanel;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBsearch(), null);
            buttonPanel.add(getBclose(), null);
        }
        return buttonPanel;
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
        }
        return Bclose;
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
        }
        return Bsearch;
    }

    /**
     * This method initializes TFauthor	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFauthor() {
        if( TFauthor == null ) {
            TFauthor = new JTextField();
            TFauthor.setColumns(0);
        }
        return TFauthor;
    }

    /**
     * This method initializes TFcontent	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTFcontent() {
        if( TFcontent == null ) {
            TFcontent = new JTextField();
            TFcontent.setColumns(0);
        }
        return TFcontent;
    }

    /**
     * This method initializes datePanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getDatePanel() {
        if( datePanel == null ) {
            GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
            gridBagConstraints34.gridx = 2;
            gridBagConstraints34.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints34.insets = new java.awt.Insets(1,2,0,2);
            gridBagConstraints34.gridy = 1;
            jLabel = new JLabel();
            jLabel.setText("to");
            GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
            gridBagConstraints33.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints33.gridy = 1;
            gridBagConstraints33.weightx = 0.0;
            gridBagConstraints33.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints33.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints33.gridx = 3;
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints16.gridy = 1;
            gridBagConstraints16.weightx = 0.0;
            gridBagConstraints16.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints16.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints16.gridx = 1;
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints15.gridy = 2;
            gridBagConstraints15.weightx = 1.0;
            gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints15.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints15.gridwidth = 3;
            gridBagConstraints15.gridx = 1;
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridx = 0;
            gridBagConstraints13.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints13.gridy = 2;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints12.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints12.gridy = 1;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints10.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints10.gridwidth = 4;
            gridBagConstraints10.gridy = 0;
            datePanel = new JPanel();
            datePanel.setLayout(new GridBagLayout());
            datePanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            datePanel.add(getJRadioButton(), gridBagConstraints10);
            datePanel.add(getJRadioButton1(), gridBagConstraints12);
            datePanel.add(getJTextField1(), gridBagConstraints16);
            datePanel.add(jLabel, gridBagConstraints34);
            datePanel.add(getJTextField2(), gridBagConstraints33);
            datePanel.add(getJRadioButton2(), gridBagConstraints13);
            datePanel.add(getJTextField(), gridBagConstraints15);
        }
        return datePanel;
    }

    /**
     * This method initializes jRadioButton	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton() {
        if( jRadioButton == null ) {
            jRadioButton = new JRadioButton();
            jRadioButton.setText("Search in messages that would be displayed");
            jRadioButton.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        }
        return jRadioButton;
    }

    /**
     * This method initializes jRadioButton1	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton1() {
        if( jRadioButton1 == null ) {
            jRadioButton1 = new JRadioButton();
            jRadioButton1.setText("Search between dates");
            jRadioButton1.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        }
        return jRadioButton1;
    }

    /**
     * This method initializes jRadioButton2	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton2() {
        if( jRadioButton2 == null ) {
            jRadioButton2 = new JRadioButton();
            jRadioButton2.setText("Search number of days backward");
            jRadioButton2.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        }
        return jRadioButton2;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField() {
        if( jTextField == null ) {
            jTextField = new JTextField();
            jTextField.setColumns(8);
        }
        return jTextField;
    }

    /**
     * This method initializes jTextField1	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField1() {
        if( jTextField1 == null ) {
            jTextField1 = new JTextField();
            jTextField1.setColumns(10);
        }
        return jTextField1;
    }

    /**
     * This method initializes jTextField2	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField2() {
        if( jTextField2 == null ) {
            jTextField2 = new JTextField();
            jTextField2.setColumns(10);
        }
        return jTextField2;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel1() {
        if( jPanel1 == null ) {
            GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            gridBagConstraints19.gridx = 0;
            gridBagConstraints19.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints19.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints19.gridwidth = 3;
            gridBagConstraints19.weightx = 1.0;
            gridBagConstraints19.insets = new java.awt.Insets(0,25,0,0);
            gridBagConstraints19.gridy = 3;
            GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.gridx = 0;
            gridBagConstraints18.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints18.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints18.gridy = 2;
            GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.gridx = 0;
            gridBagConstraints17.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints17.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints17.gridy = 1;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints7.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints7.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints7.gridy = 0;
            jPanel1 = new JPanel();
            jPanel1.setLayout(new GridBagLayout());
            jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            jPanel1.add(getJRadioButton3(), gridBagConstraints7);
            jPanel1.add(getJRadioButton4(), gridBagConstraints17);
            jPanel1.add(getJRadioButton5(), gridBagConstraints18);
            jPanel1.add(getJPanel2(), gridBagConstraints19);
        }
        return jPanel1;
    }

    /**
     * This method initializes jRadioButton3	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton3() {
        if( jRadioButton3 == null ) {
            jRadioButton3 = new JRadioButton();
            jRadioButton3.setText("Search in messages that would be displayed");
        }
        return jRadioButton3;
    }

    /**
     * This method initializes jRadioButton4	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton4() {
        if( jRadioButton4 == null ) {
            jRadioButton4 = new JRadioButton();
            jRadioButton4.setText("Search all messages, no matter which trust state is set");
        }
        return jRadioButton4;
    }

    /**
     * This method initializes jRadioButton5	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton5() {
        if( jRadioButton5 == null ) {
            jRadioButton5 = new JRadioButton();
            jRadioButton5.setText("Search only in messages with following trust state");
        }
        return jRadioButton5;
    }

    /**
     * This method initializes jPanel2	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel2() {
        if( jPanel2 == null ) {
            GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
            gridBagConstraints26.gridx = 5;
            gridBagConstraints26.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints26.weightx = 0.0;
            gridBagConstraints26.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints26.gridy = 0;
            GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
            gridBagConstraints25.gridx = 4;
            gridBagConstraints25.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints25.gridy = 0;
            GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
            gridBagConstraints24.gridx = 3;
            gridBagConstraints24.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints24.gridy = 0;
            GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
            gridBagConstraints23.gridx = 2;
            gridBagConstraints23.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints23.gridy = 0;
            GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 1;
            gridBagConstraints22.insets = new java.awt.Insets(1,5,1,5);
            gridBagConstraints22.gridy = 0;
            GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            gridBagConstraints20.gridx = 0;
            gridBagConstraints20.insets = new java.awt.Insets(1,0,1,5);
            gridBagConstraints20.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints20.gridy = 0;
            jPanel2 = new JPanel();
            jPanel2.setLayout(new GridBagLayout());
            jPanel2.add(getJCheckBox(), gridBagConstraints20);
            jPanel2.add(getJCheckBox1(), gridBagConstraints22);
            jPanel2.add(getJCheckBox2(), gridBagConstraints23);
            jPanel2.add(getJCheckBox3(), gridBagConstraints24);
            jPanel2.add(getJCheckBox4(), gridBagConstraints25);
            jPanel2.add(getJCheckBox5(), gridBagConstraints26);
        }
        return jPanel2;
    }

    /**
     * This method initializes jCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox() {
        if( jCheckBox == null ) {
            jCheckBox = new JCheckBox();
            jCheckBox.setText("GOOD");
        }
        return jCheckBox;
    }

    /**
     * This method initializes jCheckBox1	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox1() {
        if( jCheckBox1 == null ) {
            jCheckBox1 = new JCheckBox();
            jCheckBox1.setText("OBSERVE");
        }
        return jCheckBox1;
    }

    /**
     * This method initializes jCheckBox2	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox2() {
        if( jCheckBox2 == null ) {
            jCheckBox2 = new JCheckBox();
            jCheckBox2.setText("CHECK");
        }
        return jCheckBox2;
    }

    /**
     * This method initializes jCheckBox3	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox3() {
        if( jCheckBox3 == null ) {
            jCheckBox3 = new JCheckBox();
            jCheckBox3.setText("BAD");
        }
        return jCheckBox3;
    }

    /**
     * This method initializes jCheckBox4	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox4() {
        if( jCheckBox4 == null ) {
            jCheckBox4 = new JCheckBox();
            jCheckBox4.setText("NONE (anonymous)");
        }
        return jCheckBox4;
    }

    /**
     * This method initializes jCheckBox5	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getJCheckBox5() {
        if( jCheckBox5 == null ) {
            jCheckBox5 = new JCheckBox();
            jCheckBox5.setText("TAMPERED");
        }
        return jCheckBox5;
    }

    /**
     * This method initializes jPanel3	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel3() {
        if( jPanel3 == null ) {
            GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
            gridBagConstraints32.gridx = 1;
            gridBagConstraints32.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints32.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints32.gridy = 2;
            GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
            gridBagConstraints30.gridx = 0;
            gridBagConstraints30.weightx = 1.0D;
            gridBagConstraints30.weighty = 1.0D;
            gridBagConstraints30.gridwidth = 3;
            gridBagConstraints30.gridy = 4;
            glueLabel3 = new JLabel();
            glueLabel3.setText("");
            GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
            gridBagConstraints29.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints29.gridy = 3;
            gridBagConstraints29.weightx = 1.0;
            gridBagConstraints29.insets = new java.awt.Insets(1,25,1,5);
            gridBagConstraints29.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints29.gridwidth = 2;
            gridBagConstraints29.gridx = 0;
            GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
            gridBagConstraints28.gridx = 0;
            gridBagConstraints28.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints28.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints28.gridy = 2;
            GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
            gridBagConstraints27.gridx = 0;
            gridBagConstraints27.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints27.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints27.gridwidth = 2;
            gridBagConstraints27.gridy = 1;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints9.insets = new java.awt.Insets(1,5,0,5);
            gridBagConstraints9.gridwidth = 2;
            gridBagConstraints9.gridy = 0;
            jPanel3 = new JPanel();
            jPanel3.setLayout(new GridBagLayout());
            jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            jPanel3.add(getJRadioButton6(), gridBagConstraints9);
            jPanel3.add(getJRadioButton7(), gridBagConstraints27);
            jPanel3.add(getJRadioButton8(), gridBagConstraints28);
            jPanel3.add(getJButton(), gridBagConstraints32);
            jPanel3.add(getJTextField3(), gridBagConstraints29);
            jPanel3.add(glueLabel3, gridBagConstraints30);
        }
        return jPanel3;
    }

    /**
     * This method initializes jRadioButton6	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton6() {
        if( jRadioButton6 == null ) {
            jRadioButton6 = new JRadioButton();
            jRadioButton6.setText("Search in boards that would be displayed");
        }
        return jRadioButton6;
    }

    /**
     * This method initializes jRadioButton7	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton7() {
        if( jRadioButton7 == null ) {
            jRadioButton7 = new JRadioButton();
            jRadioButton7.setText("Search in all found board directories");
        }
        return jRadioButton7;
    }

    /**
     * This method initializes jRadioButton8	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getJRadioButton8() {
        if( jRadioButton8 == null ) {
            jRadioButton8 = new JRadioButton();
            jRadioButton8.setText("Search following boards");
        }
        return jRadioButton8;
    }

    /**
     * This method initializes jTextField3	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField3() {
        if( jTextField3 == null ) {
            jTextField3 = new JTextField();
            jTextField3.setEditable(false);
            jTextField3.setText("found; boards; list");
        }
        return jTextField3;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if( jButton == null ) {
            jButton = new JButton();
            jButton.setText("Choose...");
        }
        return jButton;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTable	
     * 	
     * @return javax.swing.JTable	
     */
    private JTable getJTable() {
        if( jTable == null ) {
            jTable = new JTable();
        }
        return jTable;
    }

    /**
     * This method initializes CBprivateMsgsOnly	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getCBprivateMsgsOnly() {
        if( CBprivateMsgsOnly == null ) {
            CBprivateMsgsOnly = new JCheckBox();
            CBprivateMsgsOnly.setText("Search private messages only");
        }
        return CBprivateMsgsOnly;
    }

    /**
     * This method initializes ParchiveSearch	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getParchiveSearch() {
        if( ParchiveSearch == null ) {
            GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
            gridBagConstraints36.gridx = 3;
            gridBagConstraints36.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints36.weightx = 0.5;
            gridBagConstraints36.gridy = 0;
            Lglue = new JLabel();
            Lglue.setText("");
            GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
            gridBagConstraints35.insets = new java.awt.Insets(1,3,1,5);
            gridBagConstraints35.gridy = 0;
            gridBagConstraints35.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints35.gridx = 2;
            GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
            gridBagConstraints31.insets = new java.awt.Insets(1,3,1,2);
            gridBagConstraints31.gridy = 0;
            gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints31.gridx = 1;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.insets = new java.awt.Insets(1,5,1,2);
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints8.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints8.gridx = 0;
            ParchiveSearch = new JPanel();
            ParchiveSearch.setPreferredSize(new java.awt.Dimension(517,25));
            ParchiveSearch.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3), javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            ParchiveSearch.setLayout(new GridBagLayout());
            ParchiveSearch.add(getRBsearchNormalAndArc(), gridBagConstraints8);
            ParchiveSearch.add(getRBsearchNormalOnly(), gridBagConstraints31);
            ParchiveSearch.add(getRBsearchArcOnly(), gridBagConstraints35);
            ParchiveSearch.add(Lglue, gridBagConstraints36);
        }
        return ParchiveSearch;
    }

    /**
     * This method initializes RBsearchNormalAndArc	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBsearchNormalAndArc() {
        if( RBsearchNormalAndArc == null ) {
            RBsearchNormalAndArc = new JRadioButton();
            RBsearchNormalAndArc.setText("Search in keypool and archive");
            RBsearchNormalAndArc.setPreferredSize(new java.awt.Dimension(195,20));
        }
        return RBsearchNormalAndArc;
    }

    /**
     * This method initializes RBsearchNormalOnly	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBsearchNormalOnly() {
        if( RBsearchNormalOnly == null ) {
            RBsearchNormalOnly = new JRadioButton();
            RBsearchNormalOnly.setText("Search only in keypool");
            RBsearchNormalOnly.setPreferredSize(new java.awt.Dimension(152,20));
        }
        return RBsearchNormalOnly;
    }

    /**
     * This method initializes RBsearchArcOnly	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBsearchArcOnly() {
        if( RBsearchArcOnly == null ) {
            RBsearchArcOnly = new JRadioButton();
            RBsearchArcOnly.setText("Search only in archive");
            RBsearchArcOnly.setPreferredSize(new java.awt.Dimension(150,20));
        }
        return RBsearchArcOnly;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
