/*
  IdentitiesBrowser.java / Frost
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
