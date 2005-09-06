/*
 * Created on 03.09.2005
 */
package frost.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.*;

public class IdentitiesBrowser extends JDialog {

    private JPanel jContentPane = null;

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
        this.setSize(492, 338);
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
            jContentPane.setLayout(new BorderLayout());
        }
        return jContentPane;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
