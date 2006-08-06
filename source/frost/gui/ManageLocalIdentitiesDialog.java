/*
 * Created on 06.08.2006
 */
package frost.gui;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import frost.*;
import frost.identities.*;
import frost.storage.database.*;
import frost.util.gui.*;

public class ManageLocalIdentitiesDialog extends JDialog {

    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;
    private JLabel jLabel = null;
    private JScrollPane SPlist = null;
    private JPanel jPanel = null;
    private JList identitiesList = null;
    private JButton BaddNewIdentity = null;
    private JButton BdeleteIdentity = null;
    private JButton BimportIdentityXml = null;
    private JLabel Ldummy = null;

    /**
     * This is the default constructor
     */
    public ManageLocalIdentitiesDialog() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(488, 311);
        this.setTitle("Manage local identities");
        this.setContentPane(getJContentPane());
        setModal(true);
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
            buttonPanel.add(getBclose(), null);
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
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.gridheight = 2;
            gridBagConstraints2.weightx = 0.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.weightx = 0.3;
            gridBagConstraints1.weighty = 1.0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.insets = new java.awt.Insets(3,5,3,5);
            gridBagConstraints1.gridx = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Local Identities:");
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(jLabel, gridBagConstraints);
            mainPanel.add(getSPlist(), gridBagConstraints1);
            mainPanel.add(getJPanel(), gridBagConstraints2);
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
            Bclose.setText("Close");
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return Bclose;
    }

    /**
     * This method initializes SPlist	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getSPlist() {
        if( SPlist == null ) {
            SPlist = new JScrollPane();
            SPlist.setViewportView(getIdentitiesList());
        }
        return SPlist;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints6.weighty = 1.0;
            gridBagConstraints6.gridy = 3;
            Ldummy = new JLabel();
            Ldummy.setText("");
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.insets = new java.awt.Insets(10,3,0,5);
            gridBagConstraints5.gridy = 2;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.insets = new java.awt.Insets(5,3,0,5);
            gridBagConstraints4.gridy = 1;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.insets = new java.awt.Insets(5,3,0,5);
            gridBagConstraints3.gridy = 0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getBaddNewIdentity(), gridBagConstraints3);
            jPanel.add(getBdeleteIdentity(), gridBagConstraints4);
            jPanel.add(getBimportIdentityXml(), gridBagConstraints5);
            jPanel.add(Ldummy, gridBagConstraints6);
        }
        return jPanel;
    }

    /**
     * This method initializes identitiesList	
     * 	
     * @return javax.swing.JList	
     */
    private JList getIdentitiesList() {
        if( identitiesList == null ) {
            identitiesList = new JList();
            identitiesList.setModel(new DefaultListModel());
            for(Iterator i=Core.getIdentities().getLocalIdentities().iterator(); i.hasNext(); ) {
                ((DefaultListModel)identitiesList.getModel()).addElement(i.next());
            }
        }
        return identitiesList;
    }

    /**
     * This method initializes BaddNewIdentity	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBaddNewIdentity() {
        if( BaddNewIdentity == null ) {
            BaddNewIdentity = new JButton();
            BaddNewIdentity.setText("Create Identity");
            BaddNewIdentity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LocalIdentity newIdentity = Core.getIdentities().createIdentity();
                    if( newIdentity != null ) {
                        DefaultListModel m = (DefaultListModel)getIdentitiesList().getModel();
                        m.addElement(newIdentity);
                    }
                }
            });
        }
        return BaddNewIdentity;
    }

    /**
     * This method initializes BdeleteIdentity	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBdeleteIdentity() {
        if( BdeleteIdentity == null ) {
            BdeleteIdentity = new JButton();
            BdeleteIdentity.setText("Delete Identity");
            BdeleteIdentity.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {

                    LocalIdentity li = (LocalIdentity)getIdentitiesList().getSelectedValue();
                    if( li == null ) {
                        return;
                    }

                    if( Core.getIdentities().getLocalIdentities().size() <= 1 ) {
                        MiscToolkit.getInstance().showMessage(
                                "The last existing identity can not be deleted.",
                                JOptionPane.INFORMATION_MESSAGE,
                                "Cannot delete last identity");
                    }

                    int answer =
                        JOptionPane.showConfirmDialog(
                            ManageLocalIdentitiesDialog.this,
                            "Do you really want to delete the idenity '"+li.getUniqueName()+"'?",
                            "Confirm identity delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.NO_OPTION) {
                        return; // do not delete
                    }

                    // check if files are shared with this identity
                    int count = Core.getInstance().getFileTransferManager().countFilesSharedByLocalIdentity(li);
                    if( count > 0 ) {
                        answer =
                            JOptionPane.showConfirmDialog(
                                ManageLocalIdentitiesDialog.this,
                                "The identity '"+li.getUniqueName()+"' shares "+count+" files which will be removed. Proceed?",
                                "Shared files for identity",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (answer == JOptionPane.NO_OPTION) {
                            return; // do not delete
                        }
                    }
                    Core.getInstance().getFileTransferManager().removeFilesSharedByLocalIdentity(li);
                    Core.getIdentities().deleteLocalIdentity(li);
                    ((DefaultListModel)getIdentitiesList().getModel()).removeElement(li);
                    
                    MiscToolkit.getInstance().showMessage(
                            "Identity '"+li.getUniqueName()+"' was deleted.",
                            JOptionPane.INFORMATION_MESSAGE,
                            "Identity deleted");
                }
            });
        }
        return BdeleteIdentity;
    }

    /**
     * This method initializes BimportIdentityXml	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBimportIdentityXml() {
        if( BimportIdentityXml == null ) {
            BimportIdentityXml = new JButton();
            BimportIdentityXml.setText("Import from identities.xml");
            BimportIdentityXml.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File xmlFile = chooseIdentitiesFile();
                    if( xmlFile == null ) {
                        return;
                    }
                    LocalIdentity importedIdentity = ImportIdentities.importLocalIdentityFromIdentityXml(xmlFile);
                    if( importedIdentity == null ) {
                        // load failed
                        MiscToolkit.getInstance().showMessage(
                                "No identity to import found.",
                                JOptionPane.ERROR_MESSAGE,
                                "No identity found");
                        return;
                    }
                    if( !Core.getIdentities().addLocalIdentity(importedIdentity) ) {
                        // duplicate identity
                        MiscToolkit.getInstance().showMessage(
                                "Imported identity '"+importedIdentity.getUniqueName()+"' is already in list.",
                                JOptionPane.ERROR_MESSAGE,
                                "Duplicate identity");
                        return;
                    }
                    MiscToolkit.getInstance().showMessage(
                            "Imported identity '"+importedIdentity.getUniqueName()+"'.",
                            JOptionPane.INFORMATION_MESSAGE,
                            "Identity imported");
                    ((DefaultListModel)getIdentitiesList().getModel()).addElement(importedIdentity);
                    return;
                }
            });
        }
        return BimportIdentityXml;
    }
    
    private File chooseIdentitiesFile() {
        
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
            return chooser.getSelectedFile();
        }
        return null;
    }


}  //  @jve:decl-index=0:visual-constraint="10,10"
