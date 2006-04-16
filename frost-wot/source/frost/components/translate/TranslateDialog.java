/*
 * Created on 15.04.2006
 */
package frost.components.translate;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TranslateDialog extends JDialog {

    private JPanel jContentPane = null;
    private JTextArea TAorigText = null;
    private JTextArea TAtranslatedText = null;
    private JButton Bsave = null;
    private JButton Bexit = null;
    private JLabel jLabel = null;
    private JScrollPane jScrollPane = null;
    private JTable jTable = null;
    private JScrollPane jScrollPane1 = null;
    private JTable jTable1 = null;
    private JButton jButton = null;
    private JLabel jLabel1 = null;

    /**
     * This is the default constructor
     */
    public TranslateDialog() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setBounds(new java.awt.Rectangle(0,0,620,460));
        this.setTitle("Translate Frost");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jLabel = new JLabel();
            jLabel.setBounds(new java.awt.Rectangle(23,15,102,16));
            jLabel.setText("Target language:");
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getTAorigText(), null);
            jContentPane.add(getTAtranslatedText(), null);
            jContentPane.add(getBsave(), null);
            jContentPane.add(getBexit(), null);
            jContentPane.add(jLabel, null);
            jContentPane.add(getJScrollPane(), null);
            jContentPane.add(getJScrollPane1(), null);
            jContentPane.add(getJButton(), null);
            jContentPane.add(getJLabel1(), null);
        }
        return jContentPane;
    }

    /**
     * This method initializes TAorigText	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTAorigText() {
        if( TAorigText == null ) {
            TAorigText = new JTextArea();
            TAorigText.setBounds(new java.awt.Rectangle(29,194,207,197));
        }
        return TAorigText;
    }

    /**
     * This method initializes TAtranslatedText	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTAtranslatedText() {
        if( TAtranslatedText == null ) {
            TAtranslatedText = new JTextArea();
            TAtranslatedText.setBounds(new java.awt.Rectangle(361,195,196,197));
        }
        return TAtranslatedText;
    }

    /**
     * This method initializes Bsave	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBsave() {
        if( Bsave == null ) {
            Bsave = new JButton();
            Bsave.setBounds(new java.awt.Rectangle(422,401,75,25));
            Bsave.setText("Save");
        }
        return Bsave;
    }

    /**
     * This method initializes Bexit	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBexit() {
        if( Bexit == null ) {
            Bexit = new JButton();
            Bexit.setBounds(new java.awt.Rectangle(517,401,59,26));
            Bexit.setText("Exit");
        }
        return Bexit;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(new java.awt.Rectangle(33,56,205,114));
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
     * This method initializes jScrollPane1	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane1() {
        if( jScrollPane1 == null ) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setBounds(new java.awt.Rectangle(360,55,198,115));
            jScrollPane1.setViewportView(getJTable1());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jTable1	
     * 	
     * @return javax.swing.JTable	
     */
    private JTable getJTable1() {
        if( jTable1 == null ) {
            jTable1 = new JTable();
        }
        return jTable1;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if( jButton == null ) {
            jButton = new JButton();
            jButton.setBounds(new java.awt.Rectangle(358,14,114,30));
            jButton.setText("Delete key");
        }
        return jButton;
    }

    /**
     * This method initializes jLabel1	
     * 	
     * @return javax.swing.JLabel	
     */
    private JLabel getJLabel1() {
        if( jLabel1 == null ) {
            jLabel1 = new JLabel();
            jLabel1.setText("Davor noch startdlg, sprache waehlen und entweder propties file laden oder vorhandenes als basis nehmen");
            jLabel1.setBounds(new java.awt.Rectangle(6,173,598,16));
        }
        return jLabel1;
    }

}
