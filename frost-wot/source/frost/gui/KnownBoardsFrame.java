/*
  KnownBoardsFrame.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.common.*;
import frost.gui.model.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class KnownBoardsFrame extends JDialog {
    
    private static final Logger logger = Logger.getLogger(KnownBoardsFrame.class.getName());

    private Language language;

    private TofTree tofTree;

    private static ImageIcon boardIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/board.gif"));
    private static ImageIcon writeAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/waboard.jpg"));
    private static ImageIcon readAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/raboard.jpg"));

    private JButton Bclose;
    private JButton BboardActions;
    private JButton Bimport;
    private JButton Bexport;
    private JCheckBox CBshowHidden;
    private JTextField TFlookupBoard;
    private JTextField TFfilterBoard;
    private SortedTable boardsTable;
    private KnownBoardsTableModel tableModel;
    private NameColumnRenderer nameColRenderer;
    private DescColumnRenderer descColRenderer;
    private ShowContentTooltipRenderer showContentTooltipRenderer;
    
    private JSkinnablePopupMenu tablePopupMenu;

    private List<KnownBoardsTableMember> allKnownBoardsList; // a list of all boards, needed as data source when we filter in the table
    
    private boolean showColoredLines;

    public KnownBoardsFrame(JFrame parent, TofTree tofTree) {

        super(parent);
        setModal(true);
        
        this.tofTree = tofTree;
        language = Language.getInstance();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            initialize();
        }
        catch( Exception e ) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize((int) (parent.getWidth() * 0.75),
                (int) (parent.getHeight() * 0.75));
        setLocationRelativeTo( parent );
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
    }
    
    private void updateBoardCountInTitle() {
        int count = tableModel.getRowCount();
        setTitle( language.getString("KnownBoardsFrame.title") + " (" + count + ")");
    }

    /**
     * Build the GUI.
     */
    private void initialize() {
        setTitle(language.getString("KnownBoardsFrame.title"));

        this.setResizable(true);

        tableModel = new KnownBoardsTableModel();
        // add a special renderer to name column which shows the board icon
        nameColRenderer = new NameColumnRenderer();
        descColRenderer = new DescColumnRenderer();
        showContentTooltipRenderer = new ShowContentTooltipRenderer();
        boardsTable = new SortedTable( tableModel ) {
                public TableCellRenderer getCellRenderer(int row, int column) {
                    if( column == 0 ) {
                        return nameColRenderer;
                    } else if( column == 3 ) {
                        return descColRenderer;
                    } else {
                        return showContentTooltipRenderer;
                    }
            }};
        boardsTable.setRowSelectionAllowed(true);
        boardsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

        Bclose = new JButton(language.getString("KnownBoardsFrame.button.close"));
        BboardActions = new JButton(language.getString("KnownBoardsFrame.button.actions")+" ...");
        Bimport = new JButton(language.getString("KnownBoardsFrame.button.import")+" ...");
        Bexport = new JButton(language.getString("KnownBoardsFrame.button.export")+" ...");
        CBshowHidden = new JCheckBox();
        CBshowHidden.setToolTipText(language.getString("KnownBoardsFrame.tooltip.showHidden"));
        
        TFlookupBoard = new JTextField(10);
        new TextComponentClipboardMenu(TFlookupBoard, language);
        // force a max size, needed for BoxLayout
        TFlookupBoard.setMaximumSize(TFlookupBoard.getPreferredSize());

        TFlookupBoard.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    lookupContentChanged();
                }
                public void insertUpdate(DocumentEvent e) {
                    lookupContentChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    lookupContentChanged();
                }
            });

        TFfilterBoard = new JTextField(10);
        new TextComponentClipboardMenu(TFfilterBoard, language);
        // force a max size, needed for BoxLayout
        TFfilterBoard.setMaximumSize(TFfilterBoard.getPreferredSize());

        TFfilterBoard.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    filterContentChanged();
                }
                public void insertUpdate(DocumentEvent e) {
                    filterContentChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    filterContentChanged();
                }
            });

        boardsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                     boardsTableListModel_valueChanged(e);
                 } });
        BboardActions.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if( boardsTable.getSelectedRowCount() > 0 ) {
                        // don't show menu if nothing is selected
                        tablePopupMenu.show(BboardActions, 5, 5);
                    }
                } });
        Bimport.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    import_actionPerformed(e);
                } });
        Bexport.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    export_actionPerformed(e);
                } });
        Bclose.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                } });
        CBshowHidden.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if( CBshowHidden.isSelected() ) {
                        loadKnownBoardsIntoTable(true);
                    } else {
                        removeHiddenBoards();
                    }
                } });

        // create panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setLayout( new BoxLayout( buttons, BoxLayout.X_AXIS ));
        buttons.add( new JLabel(language.getString("KnownBoardsFrame.label.lookup") + ":"));
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( TFlookupBoard );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( new JLabel(language.getString("KnownBoardsFrame.label.filter") + ":"));
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( TFfilterBoard );

        buttons.add( Box.createHorizontalGlue() );
        buttons.add( CBshowHidden );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( BboardActions );
        buttons.add(Box.createRigidArea(new Dimension(10,3)));
        buttons.add( Bimport );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( Bexport );
        buttons.add(Box.createRigidArea(new Dimension(10,3)));
        buttons.add( Bclose );
        buttons.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JScrollPane scrollPane = new JScrollPane(boardsTable);
        scrollPane.setWheelScrollingEnabled(true);
        
        mainPanel.add( scrollPane, BorderLayout.CENTER );
        mainPanel.add( buttons, BorderLayout.SOUTH );
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, null); // add Main panel

        BboardActions.setEnabled(false);

        initPopupMenu();
    }

    private void initPopupMenu() {
        tablePopupMenu = new JSkinnablePopupMenu();
        JMenuItem addBoardsMenu = new JMenuItem(language.getString("KnownBoardsFrame.button.addBoards"));
        JMenuItem addBoardsToFolderMenu = new JMenuItem(language.getString("KnownBoardsFrame.button.addBoardsToFolder")+" ...");
        JMenuItem removeBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.removeBoard"));
        JMenuItem hideBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.hideBoard"));
        JMenuItem unhideBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.unhideBoard"));

        addBoardsMenu.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBoards_actionPerformed(e);
            } });
        addBoardsToFolderMenu.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBoardsToFolder_actionPerformed(e);
            } });
        removeBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteBoards_actionPerformed(e);
            } });
        hideBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideBoards_actionPerformed(e);
            } });
        unhideBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unhideBoards_actionPerformed(e);
            } });

        tablePopupMenu.add(addBoardsMenu);
        tablePopupMenu.add(addBoardsToFolderMenu);
        tablePopupMenu.addSeparator();
        tablePopupMenu.add(hideBoardEntry);
        tablePopupMenu.add(unhideBoardEntry);
        tablePopupMenu.addSeparator();
        tablePopupMenu.add(removeBoardEntry);

        boardsTable.addMouseListener(new TablePopupMenuMouseListener());
    }

    public void startDialog() {
        loadKnownBoardsIntoTable(CBshowHidden.isSelected());
        setVisible(true); // blocking!
    }

    private void loadKnownBoardsIntoTable(boolean showHidden) {
        allKnownBoardsList = new LinkedList<KnownBoardsTableMember>();
        this.tableModel.clearDataModel();
        TFfilterBoard.setText("");
        TFlookupBoard.setText("");
        // gets all known boards from Core, and shows all not-doubles in table
        List frostboards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        Iterator i = KnownBoardsManager.getKnownBoardsList().iterator();
        // check each board in list if already in boards tree, if not add to table
        while( i.hasNext() ) {
            KnownBoard b = (KnownBoard) i.next();

            // check if board name is hidden
            if( KnownBoardsManager.isNameHidden(b) ) {
                if( !showHidden ) {
                    continue;
                } else {
                    b.setHidden(true);
                }
            }

            String bname = b.getName();
            String bprivkey = b.getPrivateKey();
            String bpubkey = b.getPublicKey();
            
            // check if this board is already in boards tree (currently)
            boolean addMe = true;
            Iterator j = frostboards.iterator();
            while( j.hasNext() ) {
                Board board = (Board) j.next();
                if( board.getName().equalsIgnoreCase(bname)
                    && ((board.getPrivateKey() == null && bprivkey == null) ||
                        (board.getPrivateKey() != null && board.getPrivateKey().equals(bprivkey)))
                    && ((board.getPublicKey() == null && bpubkey == null) ||
                        (board.getPublicKey() != null && board.getPublicKey().equals(bpubkey))) )
                {
                    // same boards, dont add
                    addMe = false;
                    break;
                }
            }
            if( addMe ) {
                // add this new board to table
                KnownBoardsTableMember member = new KnownBoardsTableMember(b);
                this.tableModel.addRow(member);
                allKnownBoardsList.add(member);
            }
        }
        updateBoardCountInTitle();
    }

    private void addBoards_actionPerformed(ActionEvent e) {
        int[] selectedRows = boardsTable.getSelectedRows();

        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() )
                    continue; // paranoia

                // add the board(s) to board tree and remove it from table
                KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                tofTree.addNewBoard(row.getBoard());
                tableModel.deleteRow(row);
                allKnownBoardsList.remove(row);
            }
            boardsTable.clearSelection();

            updateBoardCountInTitle();
        }
    }

    private void addBoardsToFolder_actionPerformed(ActionEvent e) {

        TargetFolderChooser tfc = new TargetFolderChooser(MainFrame.getInstance().getTofTreeModel());
        Folder targetFolder = tfc.startDialog();
        if( targetFolder == null ) {
            return;
        }

        int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() )
                    continue; // paranoia

                // add the board(s) to board tree and remove it from table
                KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                MainFrame.getInstance().getTofTreeModel().addNodeToTree(row.getBoard(), targetFolder);
                tableModel.deleteRow(row);
                allKnownBoardsList.remove(row);
            }
            boardsTable.clearSelection();

            updateBoardCountInTitle();
        }
    }

    private void deleteBoards_actionPerformed(ActionEvent e) {
        int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                tableModel.deleteRow(row);

                allKnownBoardsList.remove(row);
                // remove from global list of known boards
                KnownBoardsManager.deleteKnownBoard(row.getBoard());
            }
            boardsTable.clearSelection();
            
            updateBoardCountInTitle();
        }
    }

    private void hideBoards_actionPerformed(ActionEvent e) {
        int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                KnownBoardsManager.addHiddenName(row.getBoard().getName());
                row.getBoard().setHidden(true);
            }
            boardsTable.clearSelection();
            if( !CBshowHidden.isSelected() ) {
                removeHiddenBoards();
            } else {
                tableModel.tableEntriesChanged();
            }
            updateBoardCountInTitle();
        }
    }

    private void unhideBoards_actionPerformed(ActionEvent e) {
        int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                KnownBoardsManager.removeHiddenName(row.getBoard().getName());
                row.getBoard().setHidden(false);
            }
            boardsTable.clearSelection();
            tableModel.tableEntriesChanged();
            
            updateBoardCountInTitle();
        }
    }

    private void removeHiddenBoards() {
        for( int row=tableModel.getRowCount()-1; row >= 0; row-- ) {
            KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( KnownBoardsManager.isNameHidden(memb.getBoard()) ) {
                tableModel.removeRow(row);
            }
        }
        updateBoardCountInTitle();
    }

    private void boardsTableListModel_valueChanged(ListSelectionEvent e) {
        if( boardsTable.getSelectedRowCount() > 0 ) {
            BboardActions.setEnabled(true);
        } else {
            BboardActions.setEnabled(false);
        }
    }
    
    private void import_actionPerformed(ActionEvent e) {
        File xmlFile = chooseImportFile();
        if( xmlFile == null ) {
            return;
        }
        List<Board> imports = KnownBoardsXmlDAO.loadKnownBoards(xmlFile);
        if( imports.size() == 0 ) {
            MiscToolkit.getInstance().showMessage(
                    language.getString("KnownBoardsFrame.noBoardsImported.body"),
                    JOptionPane.WARNING_MESSAGE, 
                    language.getString("KnownBoardsFrame.noBoardsImported.title"));
        } else {
            int added = KnownBoardsManager.addNewKnownBoards(imports);
            MiscToolkit.getInstance().showMessage(
                    language.formatMessage("KnownBoardsFrame.boardsImported.body", 
                            Integer.toString(imports.size()), 
                            xmlFile.getName(),
                            Integer.toString(added)),
                    JOptionPane.WARNING_MESSAGE, 
                    language.getString("KnownBoardsFrame.boardsImported.title"));
            loadKnownBoardsIntoTable(CBshowHidden.isSelected());
        }
    }

    private void export_actionPerformed(ActionEvent e) {
        File xmlFile = chooseExportFile();
        if( xmlFile == null ) {
            return;
        }
        // don't export hidden boards
        List<Board> frostboards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        frostboards.addAll(KnownBoardsManager.getKnownBoardsList());
        for(Iterator i=frostboards.iterator(); i.hasNext(); ) {
            Board b = (Board) i.next();
            if( KnownBoardsManager.isNameHidden(b) ) {
                i.remove();
            }
        }

        if( KnownBoardsXmlDAO.saveKnownBoards(xmlFile, frostboards) ) {
            MiscToolkit.getInstance().showMessage(
                    language.formatMessage("KnownBoardsFrame.boardsExported.body", Integer.toString(frostboards.size()), xmlFile.getName()),
                    JOptionPane.INFORMATION_MESSAGE, 
                    language.getString("KnownBoardsFrame.boardsExported.title"));
        } else {
            MiscToolkit.getInstance().showMessage(
                    language.getString("KnownBoardsFrame.exportFailed.body"),
                    JOptionPane.ERROR_MESSAGE, 
                    language.getString("KnownBoardsFrame.exportFailed.title"));
        }
    }

    private File chooseExportFile() {
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "*.xml";
            }
        };
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String s = f.getPath();
            if( !s.endsWith(".xml") ) {
                f = new File(s+".xml");
            }
            return f;
        }
        return null;
    }

    private File chooseImportFile() {
        FileFilter myFilter = new FileFilter() {
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return "*.xml";
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

    /**
     * The class is a table row, holding the board and its file/message counts.
     */
    class KnownBoardsTableMember implements TableMember {

        KnownBoard frostboard;

        public KnownBoardsTableMember(KnownBoard b) {
            this.frostboard = b;
        }

        public Object getValueAt(int column) {
            switch( column ) {
            case 0:
                if( frostboard.isHidden() ) {
                    return frostboard.getName() + " (H)";
                } else {
                    return frostboard.getName();
                }
            case 1:
                return ((frostboard.getPublicKey() == null) ? "" : frostboard.getPublicKey());
            case 2:
                return ((frostboard.getPrivateKey() == null) ? "" : frostboard.getPrivateKey());
            case 3:
                return ((frostboard.getDescription() == null) ? "" : frostboard.getDescription());
            }
            return "*ERR*";
        }

        public int compareTo(TableMember anOther, int tableColumIndex) {
            String c1 = (String) getValueAt(tableColumIndex);
            String c2 = (String) anOther.getValueAt(tableColumIndex);
            return c1.compareToIgnoreCase(c2);
        }

        public KnownBoard getBoard() {
            return frostboard;
        }
    }

    /**
     * Called whenever the content of the lookup text field changes
     */
    private void lookupContentChanged() {
        try {
            String txt = TFlookupBoard.getDocument().getText(0, TFlookupBoard.getDocument().getLength());
            // now try to find the first board name that starts with this txt (case insensitiv),
            // if we found one set selection to it, else leave selection untouched
            for( int row=0; row < tableModel.getRowCount(); row++ ) {
                KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
                if( memb.getBoard().getName().toLowerCase().startsWith(txt.toLowerCase()) ) {
                    boardsTable.getSelectionModel().setSelectionInterval(row, row);
                    // now scroll to selected row, try to show it on top of table

                    // determine the count of showed rows
                    int visibleRows = (int)(boardsTable.getVisibleRect().getHeight() / boardsTable.getCellRect(row,0,true).getHeight());
                    int scrollToRow;
                    if( row + visibleRows > tableModel.getRowCount() ) {
                        scrollToRow = tableModel.getRowCount()-1;
                    } else {
                        scrollToRow = row + visibleRows - 1;
                    }
                    if( scrollToRow > row ) scrollToRow--;
                    // scroll 2 times to make sure row is displayed
                    boardsTable.scrollRectToVisible(boardsTable.getCellRect(row,0,true));
                    boardsTable.scrollRectToVisible(boardsTable.getCellRect(scrollToRow,0,true));
                    break;
                }
            }
        } catch(Exception ex) {}
    }

    /**
     * Called whenever the content of the filter text field changes
     */
    private void filterContentChanged() {
        try {
            TFlookupBoard.setText(""); // clear
            String txt = TFfilterBoard.getDocument().getText(0, TFfilterBoard.getDocument().getLength()).trim();
            txt = txt.toLowerCase();
            // filter: show all boards that have this txt in name
            tableModel.clearDataModel();
            for(Iterator<KnownBoardsTableMember> i = allKnownBoardsList.iterator(); i.hasNext();  ) {
                KnownBoardsTableMember tm = i.next();
                if( txt.length() > 0 ) {
                    String bn = tm.getBoard().getName().toLowerCase();
                    if( bn.indexOf(txt) < 0 ) {
                        continue;
                    }
                }
                tableModel.addRow(tm);
            }
        } catch(Exception ex) {}
        updateBoardCountInTitle();
    }

    class NameColumnRenderer extends ShowContentTooltipRenderer {
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
        {
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

            KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( memb.getBoard().getPublicKey() == null &&
                memb.getBoard().getPrivateKey() == null )
            {
                // public board
                setIcon(boardIcon);
            }
            else if( memb.getBoard().getPublicKey() != null &&
                     memb.getBoard().getPrivateKey() == null )
            {
                // read access board
                setIcon(readAccessIcon);
            }
            else if( memb.getBoard().getPrivateKey() != null )
            {
                // write access board (or write-only)
                setIcon(writeAccessIcon);
            }
            return this;
        }
    }

    class DescColumnRenderer extends ShowColoredLinesRenderer {
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
        {
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

            KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( memb.getBoard().getDescription() != null &&
                memb.getBoard().getDescription().length() > 0 )
            {
                setToolTipText(memb.getBoard().getDescription());
            } else {
                setToolTipText(null);
            }

            return this;
        }
    }
    
    private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }
    
    private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            if( value != null ) {
                tooltip = value.toString();
                if( tooltip.length() == 0 ) {
                    tooltip = null;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    class TablePopupMenuMouseListener implements MouseListener {
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        public void mouseClicked(MouseEvent event) {}
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}

        protected void maybeShowPopup(MouseEvent e) {
            if( e.isPopupTrigger() ) {
                if( boardsTable.getSelectedRowCount() > 0 ) {
                    // don't show menu if nothing is selected
                    tablePopupMenu.show(boardsTable, e.getX(), e.getY());
                }
            }
        }
    }
}
