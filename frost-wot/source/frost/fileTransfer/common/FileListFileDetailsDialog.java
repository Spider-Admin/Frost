/*
 SearchItemPropertiesDialog.java / Frost
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
package frost.fileTransfer.common;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.fileTransfer.*;
import frost.util.model.gui.*;

public class FileListFileDetailsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bok = null;
    /**
     * @param owner
     */
    public FileListFileDetailsDialog(Frame owner) {
        super(owner);
        initialize();
        setLocationRelativeTo(owner);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(550, 370);
        this.setContentPane(getJContentPane());
        this.setTitle("Properties");
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
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
            jContentPane.add(getMainPanel(), BorderLayout.CENTER);
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
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBok(), null);
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
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add( getModelTable().getScrollPane(), BorderLayout.CENTER);
        }
        return mainPanel;
    }
    
    SortedModelTable modelTable = null;
    FileListFileDetailsTableModel model = null;
    
    private SortedModelTable getModelTable() {
        if( modelTable == null ) {
            model = new FileListFileDetailsTableModel();
            FileListFileDetailsTableFormat tableFormat = new FileListFileDetailsTableFormat();
            modelTable = new SortedModelTable(model, tableFormat);
        }
        return modelTable;
    }
    
    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton();
            Bok.setText("Close");
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return Bok;
    }
    
    public void startDialog(FrostFileListFileObject fileObject) {
        List lst = fileObject.getFrostFileListFileObjectOwnerList();
        for( Iterator i = lst.iterator(); i.hasNext(); ) {
            FrostFileListFileObjectOwner o =   (FrostFileListFileObjectOwner) i.next();
            FileListFileDetailsItem item = new FileListFileDetailsItem(o); 
            model.addPropertiesItem(item);
        }
        setVisible(true);
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
