/*
  KnownBoardsFrame.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import frost.*;
import frost.gui.components.JSkinnablePopupMenu;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;
import frost.messages.BoardAttachment;

public class KnownBoardsFrame extends JDialog
{
	private static Logger logger = Logger.getLogger(KnownBoardsFrame.class.getName());
	
	static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    static ImageIcon boardIcon = new ImageIcon(frame1.class.getResource("/data/board.gif"));
    static ImageIcon writeAccessIcon = new ImageIcon(frame1.class.getResource("/data/waboard.jpg"));
    static ImageIcon readAccessIcon = new ImageIcon(frame1.class.getResource("/data/raboard.jpg"));
    
    JButton Bclose;
    JButton BaddBoard;
    JTextField TFlookupBoard;
    SortedTable boardsTable;
    KnownBoardsTableModel tableModel;
    NameColumnRenderer nameColRenderer;
    
	JSkinnablePopupMenu tablePopupMenu;
    
    boolean savingNeeded = false;
    
    public KnownBoardsFrame(JFrame parent, ResourceBundle LangRes)
    {
        super();
		KnownBoardsFrame.LangRes = LangRes;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            init();
        }
        catch( Exception e ) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo( parent );
    }
    
    /**
     * Build the GUI.
     */
    public void init()
    {
        setModal(true);
        setTitle(LangRes.getString("KnownBoardsFrame.List of known boards"));
        
        this.setResizable(true);
        
        tableModel = new KnownBoardsTableModel(LangRes);
        // add a special renderer to name column which shows the board icon
        nameColRenderer = new NameColumnRenderer();
        boardsTable = new SortedTable( tableModel ) {
                public TableCellRenderer getCellRenderer(int row, int column) {
                    if( column == 0 ) 
                        return nameColRenderer;
                        
                    return super.getCellRenderer(row, column);
            }};
        boardsTable.setRowSelectionAllowed(true);
        boardsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        
        Bclose = new JButton(LangRes.getString("KnownBoardsFrame.Close"));
        BaddBoard = new JButton(LangRes.getString("KnownBoardsFrame.Add board"));

        TFlookupBoard = new JTextField(10);
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
        
        boardsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                     public void valueChanged(ListSelectionEvent e) {
                         boardsTableListModel_valueChanged(e);
                     } });
        BaddBoard.addActionListener( new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addBoards_actionPerformed(e);
                    } });
        Bclose.addActionListener( new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    } });
        
        // create panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setLayout( new BoxLayout( buttons, BoxLayout.X_AXIS ));
        buttons.add( new JLabel(LangRes.getString("KnownBoardsFrame.Lookup") + ":"));
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( TFlookupBoard );
        buttons.add( Box.createHorizontalGlue() );
        buttons.add( BaddBoard );
        buttons.add(Box.createRigidArea(new Dimension(15,3)));
        buttons.add( Bclose );
        buttons.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        
        mainPanel.add( new JScrollPane( boardsTable ), BorderLayout.CENTER );
        mainPanel.add( buttons, BorderLayout.SOUTH );
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));
        
        this.getContentPane().add(mainPanel, null); // add Main panel
        
        BaddBoard.setEnabled(false);
        
        initPopupMenu();
    }
    
    private void initPopupMenu()
    {
        tablePopupMenu = new JSkinnablePopupMenu();
        JMenuItem addBoardsMenu = new JMenuItem(LangRes.getString("KnownBoardsFrame.Add board"));
        JMenuItem removeBoardEntry = new JMenuItem(LangRes.getString("KnownBoardsFrame.Remove board"));
        
        addBoardsMenu.addActionListener( new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addBoards_actionPerformed(e);
                    } });
        removeBoardEntry.addActionListener( new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteBoards_actionPerformed(e);
                    } });

        tablePopupMenu.add(addBoardsMenu);
        tablePopupMenu.add(removeBoardEntry);
                
        boardsTable.addMouseListener(new TablePopupMenuMouseListener());        
    }
    
    public void startDialog()
    {
        // gets all known boards from Core, and shows all not-doubles in table
        Vector frostboards = frame1.getInstance().getTofTree().getAllBoards();
        
        synchronized(Core.getKnownBoards())
        {
            Iterator i = Core.getKnownBoards().iterator();
            // check each board in list if already in boards tree, if not add to table
            while(i.hasNext())
            {
                BoardAttachment ba = (BoardAttachment)i.next();
                
                String bname = ba.getBoardObj().getBoardName();
                String bprivkey = ba.getBoardObj().getPrivateKey();
                String bpubkey = ba.getBoardObj().getPublicKey();

                // check if this board is already in boards tree (currently)            
                boolean addMe = true;
                Iterator j = frostboards.iterator();
                while(j.hasNext())
                {
                    FrostBoardObject board = (FrostBoardObject)j.next();
                    if( board.getBoardName().equalsIgnoreCase(bname) &&
                        ( 
                          ( board.getPrivateKey() == null &&
                            bprivkey == null 
                          ) ||
                          ( board.getPrivateKey() != null &&
                            board.getPrivateKey().equals(bprivkey)
                          )
                        ) &&
                        ( 
                          ( board.getPublicKey() == null &&
                            bpubkey == null 
                          ) ||
                          ( board.getPublicKey() != null &&
                            board.getPublicKey().equals(bpubkey)
                          )
                        )
                      )
                      {
                          // same boards, dont add
                          addMe = false;
                          break; 
                      }
                }     
                if( addMe ) 
                {
                    // add this new board to table
                    KnownBoardsTableMember member = new KnownBoardsTableMember(ba);
                    this.tableModel.addRow( member );
                }
            }
        }
        show();
        // after we return, check if we should save the known boards file ...
        if( this.savingNeeded )
        {
            Core.getInstance().saveKnownBoards();
        }
    }
    
    private void addBoards_actionPerformed(ActionEvent e)
    {
        int[] selectedRows = boardsTable.getSelectedRows();

        if( selectedRows.length > 0 )
        {
            for( int z=selectedRows.length-1; z>-1; z-- )
            {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() )
                    continue; // paranoia

                // add the board(s) to board tree and remove it from table
                KnownBoardsTableMember row = (KnownBoardsTableMember)tableModel.getRow(rowIx);
                frame1.getInstance().getTofTree().addNewBoard(row.getBoardObject());
                tableModel.deleteRow(row);
            }
            boardsTable.clearSelection();
        }
    }
    
    private void deleteBoards_actionPerformed(ActionEvent e)
    {
        int[] selectedRows = boardsTable.getSelectedRows();

        if( selectedRows.length > 0 )
        {
            for( int z=selectedRows.length-1; z>-1; z-- )
            {
                int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() )
                    continue; // paranoia

                // add the board(s) to board tree and remove it from table
                KnownBoardsTableMember row = (KnownBoardsTableMember)tableModel.getRow(rowIx);
                tableModel.deleteRow(row);
                // remove from global list of known boards
                Core.getKnownBoards().remove(row.getBoardAttachment());
                this.savingNeeded = true;
            }
            boardsTable.clearSelection();
        }
    }
    
    private void boardsTableListModel_valueChanged(ListSelectionEvent e)
    {
        if( boardsTable.getSelectedRowCount() > 0 )
        {
            BaddBoard.setEnabled(true);
        }
        else
        {
            BaddBoard.setEnabled(false);
        }
    }
    
    
    /**
     * The class is a table row, holding the board and its file/message counts.
     */
    class KnownBoardsTableMember implements TableMember
    {
        BoardAttachment boardatt;
        FrostBoardObject frostboard;

        public KnownBoardsTableMember(BoardAttachment ba)
        {
            this.boardatt = ba;
            this.frostboard = ba.getBoardObj();
        }

        public Object getValueAt(int column)
        {
            switch( column )
            {
                case 0: return frostboard.getBoardName();
                case 1: return ((frostboard.getPublicKey()==null)?"":frostboard.getPublicKey());
                case 2: return ((frostboard.getPrivateKey()==null)?"":frostboard.getPrivateKey());
            }
            return "*ERR*";
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            String c1 = (String)getValueAt(tableColumIndex);
            String c2 = (String)anOther.getValueAt(tableColumIndex);
            return c1.compareToIgnoreCase( c2 );
        }
        public FrostBoardObject getBoardObject()
        {
            return frostboard;
        }
        public BoardAttachment getBoardAttachment()
        {
            return boardatt;
        }
    }
    
    /**
     * Called whenever the content of the lookup text field changes
     */
    private void lookupContentChanged()
    {
        try {
            String txt = TFlookupBoard.getDocument().getText(0, TFlookupBoard.getDocument().getLength());
            // now try to find the first board name that starts with this txt (case insensitiv),
            // if we found one set selection to it, else leave selection untouched
            for( int row=0; row < tableModel.getRowCount(); row++ )
            {
                KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
                if( memb.getBoardObject().getBoardName().toLowerCase().startsWith(txt.toLowerCase()) )
                {
                    boardsTable.getSelectionModel().setSelectionInterval(row, row);
                    // now scroll to selected row, try to show it on top of table
                    
                    // determine the count of showed rows
                    int visibleRows = (int)(boardsTable.getVisibleRect().getHeight() / boardsTable.getCellRect(row,0,true).getHeight());
                    int scrollToRow;
                    if( row + visibleRows > tableModel.getRowCount() )
                    {
                        scrollToRow = tableModel.getRowCount()-1;
                    }
                    else
                    {
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
    
    class NameColumnRenderer extends DefaultTableCellRenderer
    {
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
            if( memb.getBoardObject().getPublicKey() == null && 
                memb.getBoardObject().getPrivateKey() == null )
            {
                // public board
                setIcon(boardIcon);    
            }
            else if( memb.getBoardObject().getPublicKey() != null && 
                     memb.getBoardObject().getPrivateKey() == null )
            {
                // read access board
                setIcon(readAccessIcon);
            }
            else if( memb.getBoardObject().getPrivateKey() != null )
            {
                // write access board (or write-only)
                setIcon(writeAccessIcon);
            }
            return this;    
        }
    }
    
    class TablePopupMenuMouseListener implements MouseListener
    {
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
                tablePopupMenu.show(boardsTable, e.getX(), e.getY());
            }
        }
    }
}
