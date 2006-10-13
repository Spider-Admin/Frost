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

import frost.*;
import frost.fileTransfer.*;
import frost.util.gui.translation.*;
import frost.util.model.gui.*;

public class FileListFileDetailsDialog extends JDialog {

    Language language = Language.getInstance();
    
    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JPanel mainPanel = null;
    private JButton Bclose = null;

    private SortedModelTable modelTable = null;
    private FileListFileDetailsTableModel model = null;
    private FileListFileDetailsTableFormat tableFormat = null;

    public FileListFileDetailsDialog(Frame owner) {
        super(owner);
        initialize(owner);
    }

    /**
     * This method initializes this
     */
    private void initialize(Frame owner) {
        this.setContentPane(getJContentPane());
        this.setTitle(language.getString("FileListFileDetailsDialog.title"));
        loadLayout();
        setLocationRelativeTo(owner);
    }
    
    private void loadLayout() {
        
        int lastHeight = Core.frostSettings.getIntValue("FileListFileDetailsDialog.height");
        int lastWidth = Core.frostSettings.getIntValue("FileListFileDetailsDialog.width");
        
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (lastWidth < 100) {
            lastWidth = 600;
        }
        if (lastWidth > scrSize.width) {
            lastWidth = scrSize.width;
        }

        if (lastHeight < 100) {
            lastHeight = 370;
        }
        if (lastHeight > scrSize.height) {
            lastWidth = scrSize.height;
        }
        setSize(lastWidth, lastHeight);
    }

    private void saveLayout() {
        // dialog size
        Rectangle bounds = getBounds();
        Core.frostSettings.setValue("FileListFileDetailsDialog.height", bounds.height);
        Core.frostSettings.setValue("FileListFileDetailsDialog.width", bounds.width);
        
        tableFormat.saveTableLayout(getModelTable());
    }
    
    /**
     * This method initializes jContentPane
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
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBclose(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes mainPanel	
     */
    private JPanel getMainPanel() {
        if( mainPanel == null ) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add( getModelTable().getScrollPane(), BorderLayout.CENTER);
        }
        return mainPanel;
    }
    
    private SortedModelTable getModelTable() {
        if( modelTable == null ) {
            model = new FileListFileDetailsTableModel();
            tableFormat = new FileListFileDetailsTableFormat();
            modelTable = new SortedModelTable(model, tableFormat);
        }
        return modelTable;
    }
    
    /**
     * This method initializes Bok	
     */
    private JButton getBclose() {
        if( Bclose == null ) {
            Bclose = new JButton();
            Bclose.setText(language.getString("FileListFileDetailsDialog.button.close"));
            Bclose.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    saveLayout();
                    setVisible(false);
                }
            });
        }
        return Bclose;
    }
    
    public void startDialog(FrostFileListFileObject fileObject) {
        List lst = fileObject.getFrostFileListFileObjectOwnerList();
        for( Iterator i = lst.iterator(); i.hasNext(); ) {
            FrostFileListFileObjectOwner o = (FrostFileListFileObjectOwner) i.next();
            FileListFileDetailsItem item = new FileListFileDetailsItem(o); 
            model.addPropertiesItem(item);
        }
        setVisible(true);
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
