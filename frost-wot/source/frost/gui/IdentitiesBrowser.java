/*
 * Created on 03.09.2005
 */
package frost.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class IdentitiesBrowser extends JDialog {

    private JPanel jContentPane = null;
    private JScrollPane jScrollPane = null;
    private JTable identitiesTable = null;
    /**
     * This is the default constructor
     */
    public IdentitiesBrowser() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(740, 603);
        this.setTitle("Identities Browser");
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
            jContentPane.setLayout(null);
            jContentPane.add(getJScrollPane(), null);
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
            jScrollPane.setBounds(new java.awt.Rectangle(46,44,494,348));
            jScrollPane.setViewportView(getIdentitiesTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes identitiesTable	
     * 	
     * @return javax.swing.JTable	
     */
    private JTable getIdentitiesTable() {
        if( identitiesTable == null ) {
            identitiesTable = new JTable();
        }
        return identitiesTable;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
