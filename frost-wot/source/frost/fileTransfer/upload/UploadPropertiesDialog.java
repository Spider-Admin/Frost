package frost.fileTransfer.upload;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.identities.*;

// TODO: nur owner oder owner, board anzeigen

public class UploadPropertiesDialog extends JDialog {
    
    public static int OK = 1;
    public static int CANCEL = 2;

    private String title;
    
    private int returnCode = CANCEL;
    private String choosedUniqueName = null;

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bcancel = null;
    private JButton Bok = null;
    private JLabel jLabel = null;
    private JRadioButton RBanonymous = null;
    private JRadioButton RBidentity = null;
    private JComboBox CBidentities = null;
    
    private Frame parent;

    /**
     * This is the default constructor
     */
    public UploadPropertiesDialog(Frame newParent, String newTitle) {
        super(newParent);
        title = newTitle;
        parent = newParent;
        setModal(true);
        
        initialize();
        pack();

        getRBanonymous().setSelected(false);
        getRBidentity().setSelected(false);
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(getRBanonymous());
        bg.add(getRBidentity());
}

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(397, 213);
//        this.setTitle(title);
        this.setTitle("title");
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
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
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
            buttonPanel.add(getBok(), null);
            buttonPanel.add(getBcancel(), null);
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
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints11.gridy = 3;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.insets = new java.awt.Insets(2,20,0,5);
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints11.gridx = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.insets = new java.awt.Insets(5,10,0,5);
            gridBagConstraints2.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.gridy = 2;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.insets = new java.awt.Insets(5,10,0,5);
            gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.gridy = 1;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Choose the owner to use for the choosed upload files");
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(jLabel, gridBagConstraints);
            mainPanel.add(getRBanonymous(), gridBagConstraints1);
            mainPanel.add(getRBidentity(), gridBagConstraints2);
            mainPanel.add(getCBidentities(), gridBagConstraints11);
        }
        return mainPanel;
    }

    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton("Cancel");
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    returnCode = CANCEL;
                    choosedUniqueName = null;
                    setVisible(false);
                }
            });
        }
        return Bcancel;
    }
    
    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton("Ok");
            Bok.setEnabled(false);
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    returnCode = OK;
                    choosedUniqueName = getCBidentities().getSelectedItem().toString();
                    setVisible(false);
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes RBanonymous	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBanonymous() {
        if( RBanonymous == null ) {
            RBanonymous = new JRadioButton();
            RBanonymous.setText("Anonymous");
            RBanonymous.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if( getRBanonymous().isSelected() ) {
                        getCBidentities().setEnabled(false);
                    }
                    getBok().setEnabled(true);
                }
            });
        }
        return RBanonymous;
    }

    /**
     * This method initializes Identity	
     * 	
     * @return javax.swing.JRadioButton	
     */
    private JRadioButton getRBidentity() {
        if( RBidentity == null ) {
            RBidentity = new JRadioButton();
            RBidentity.setText("Identity");
            RBidentity.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if( getRBidentity().isSelected() ) {
                        getCBidentities().setEnabled(true);
                    }
                    getBok().setEnabled(true);
                }
            });
        }
        return RBidentity;
    }

    /**
     * This method initializes CBidentities	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getCBidentities() {
        if( CBidentities == null ) {
            CBidentities = new JComboBox();
            for(Iterator i=Core.getIdentities().getLocalIdentities().iterator(); i.hasNext(); ) {
                LocalIdentity id = (LocalIdentity)i.next();
                CBidentities.addItem(id.getUniqueName());
            }
        }
        return CBidentities;
    }
    
    public String getChoosedUniqueName() {
        return choosedUniqueName;
    }

    public int showDialog() {
        setLocationRelativeTo(parent);
        setVisible(true);
        return returnCode;
    }

}  //  @jve:decl-index=0:visual-constraint="19,14"
