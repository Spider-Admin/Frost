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
import frost.gui.model.*;
import frost.gui.objects.Board;
import frost.messages.BoardAttachment;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class KnownBoardsFrame extends JDialog
{
	private static Logger logger = Logger.getLogger(KnownBoardsFrame.class.getName());
	
	private Language language;

    private static ImageIcon boardIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/board.gif"));
    private static ImageIcon writeAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/waboard.jpg"));
    private static ImageIcon readAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/raboard.jpg"));
    
    private JButton Bclose;
    private JButton BaddBoard;
    private JTextField TFlookupBoard;
    private SortedTable boardsTable;
    private KnownBoardsTableModel tableModel;
    private NameColumnRenderer nameColRenderer;
    
    private JSkinnablePopupMenu tablePopupMenu;
    
	private boolean savingNeeded = false;
    
    /**
     * @param parent
     */
    public KnownBoardsFrame(JFrame parent)
    {
        super();
		this.language = Language.getInstance();
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
    }
    
    /**
     * Build the GUI.
     */
    private void initialize()
    {
        setModal(true);
        setTitle(language.getString("KnownBoardsFrame.List of known boards"));
        
        this.setResizable(true);
        
        tableModel = new KnownBoardsTableModel();
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
        
        Bclose = new JButton(language.getString("KnownBoardsFrame.Close"));
        BaddBoard = new JButton(language.getString("KnownBoardsFrame.Add board"));

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
        buttons.add( new JLabel(language.getString("KnownBoardsFrame.Lookup") + ":"));
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
    
    /**
     * 
     */
    private void initPopupMenu()
    {
        tablePopupMenu = new JSkinnablePopupMenu();
        JMenuItem addBoardsMenu = new JMenuItem(language.getString("KnownBoardsFrame.Add board"));
        JMenuItem removeBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.Remove board"));
        
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
    
    /**
     * 
     */
    public void startDialog()
    {
        // gets all known boards from Core, and shows all not-doubles in table
        Vector frostboards = MainFrame.getInstance().getTofTree().getAllBoards();
        
        synchronized(Core.getKnownBoards())
        {
            Iterator i = Core.getKnownBoards().iterator();
            // check each board in list if already in boards tree, if not add to table
            while(i.hasNext())
            {
                BoardAttachment ba = (BoardAttachment)i.next();
                
                String bname = ba.getBoardObj().getName();
                String bprivkey = ba.getBoardObj().getPrivateKey();
                String bpubkey = ba.getBoardObj().getPublicKey();

                // check if this board is already in boards tree (currently)            
                boolean addMe = true;
                Iterator j = frostboards.iterator();
                while(j.hasNext())
                {
                    Board board = (Board)j.next();
                    if( board.getName().equalsIgnoreCase(bname) &&
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
        setVisible(true);
        // after we return, check if we should save the known boards file ...
        if( this.savingNeeded )
        {
            Core.getInstance().saveKnownBoards();
        }
    }
    
    /**
     * @param e
     */
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
                MainFrame.getInstance().getTofTree().addNewBoard(row.getBoardObject());
                tableModel.deleteRow(row);
            }
            boardsTable.clearSelection();
        }
    }
    
    /**
     * @param e
     */
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
    
    /**
     * @param e
     */
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
        Board frostboard;

        /**
         * @param ba
         */
        public KnownBoardsTableMember(BoardAttachment ba)
        {
            this.boardatt = ba;
            this.frostboard = ba.getBoardObj();
        }

		/* (non-Javadoc)
		 * @see frost.gui.model.TableMember#getValueAt(int)
		 */
		public Object getValueAt(int column) {
			switch (column) {
				case 0 :
					return frostboard.getName();
				case 1 :
					return ((frostboard.getPublicKey() == null) ? "" : frostboard.getPublicKey());
				case 2 :
					return ((frostboard.getPrivateKey() == null) ? "" : frostboard.getPrivateKey());
				case 3 :
					return ((frostboard.getDescription() == null) ? "" : frostboard.getDescription());
			}
			return "*ERR*";
		}
		
        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember, int)
         */
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            String c1 = (String)getValueAt(tableColumIndex);
            String c2 = (String)anOther.getValueAt(tableColumIndex);
            return c1.compareToIgnoreCase( c2 );
        }
        
        /**
         * @return
         */
        public Board getBoardObject()
        {
            return frostboard;
        }
        
        /**
         * @return
         */
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
                if( memb.getBoardObject().getName().toLowerCase().startsWith(txt.toLowerCase()) )
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
    
    /**
     * 
     */
    class NameColumnRenderer extends DefaultTableCellRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
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
    
    /**
     * 
     */
    class TablePopupMenuMouseListener implements MouseListener
    {
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent event) {}
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent event) {}
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent event) {}
        
        /**
         * @param e
         */
        protected void maybeShowPopup(MouseEvent e) {
            if( e.isPopupTrigger() ) {
                tablePopupMenu.show(boardsTable, e.getX(), e.getY());
            }
        }
    }
}
