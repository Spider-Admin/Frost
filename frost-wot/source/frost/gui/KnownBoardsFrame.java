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
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;

public class KnownBoardsFrame extends JDialog
{
    static ImageIcon boardIcon = new ImageIcon(frame1.class.getResource("/data/board.gif"));
    static ImageIcon writeAccessIcon = new ImageIcon(frame1.class.getResource("/data/waboard.jpg"));
    static ImageIcon readAccessIcon = new ImageIcon(frame1.class.getResource("/data/raboard.jpg"));
    
    JButton Bclose;
    JButton BaddBoard;
    JTextField TFlookupBoard;
    SortedTable boardsTable;
    KnownBoardsTableModel tableModel;
    NameColumnRenderer nameColRenderer;
    
    public KnownBoardsFrame(JFrame parent)
    {
        super();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            init();
        }
        catch( Exception e ) {
            e.printStackTrace();
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
        setTitle("List of known boards");
        
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
        
        Bclose = new JButton("Close");
        BaddBoard = new JButton("Add board");

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
        buttons.add( new JLabel("Lookup:"));
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
    }
    
    public void startDialog()
    {
        // gets all known boards from Core, and shows all not-doubles in table
        SortedSet knownboards = Core.getKnownBoards();
        Vector frostboards = frame1.getInstance().getTofTree().getAllBoards();
        
        Iterator i = knownboards.iterator();
        while(i.hasNext())
        {
            String aboardstr = (String)i.next(); // format: "name * pubkey * privkey"
            if( aboardstr.length() < 13 || aboardstr.indexOf("*") < 3 ||
                ! ( aboardstr.indexOf("*") < aboardstr.lastIndexOf("*") ) )
            {
                continue;
            }
            String bname, bpubkey, bprivkey;
            int pos = aboardstr.indexOf("*");
            // BBACKFLAG: maybe there is no * in string ;)
            bname = aboardstr.substring(0, pos).trim();
            int pos2 = aboardstr.indexOf("*", pos+1);
            bpubkey = aboardstr.substring(pos+1, pos2).trim();
            bprivkey = aboardstr.substring(pos2+1).trim();
            if( bpubkey.length() < 10 )  bpubkey = null;
            if( bprivkey.length() < 10 )  bprivkey = null;
            
            
            // check if this board is already contained in frostboards, if not add to list
            Iterator j = frostboards.iterator();
            boolean addMe = true;
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
            if( addMe == true )
            {
                // add this new board to table
                KnownBoardsTableMember member = new KnownBoardsTableMember(bname, bpubkey, bprivkey);
                this.tableModel.addRow( member );
            }
        }
        show();
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
                frame1.getInstance().getTofTree().addNewBoard(row.getBoardName(), 
                                                              row.getPublicKey(),
                                                              row.getPrivateKey());
                tableModel.deleteRow(row);
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
        String name;
        String pubkey;
        String privkey;

        public KnownBoardsTableMember(String bn, String bpubk, String bprivk)
        {
            this.name = bn;
            this.pubkey = bpubk;
            this.privkey = bprivk;
        }

        public Object getValueAt(int column)
        {
            switch( column )
            {
                case 0: return name;
                case 1: return ((pubkey==null)?"":pubkey);
                case 2: return ((privkey==null)?"":privkey);
            }
            return "*ERR*";
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            String c1 = (String)getValueAt(tableColumIndex);
            String c2 = (String)anOther.getValueAt(tableColumIndex);
            return c1.compareToIgnoreCase( c2 );
        }
        public String getBoardName()
        {
            return name;
        }
        public String getPublicKey()
        {
            return pubkey;
        }
        public String getPrivateKey()
        {
            return privkey;
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
                if( memb.getBoardName().toLowerCase().startsWith(txt.toLowerCase()) )
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
            if( memb.getPublicKey() == null && memb.getPrivateKey() == null )
            {
                // public board
                setIcon(boardIcon);    
            }
            else if( memb.getPublicKey() != null && memb.getPrivateKey() == null )
            {
                // read access board
                setIcon(readAccessIcon);
            }
            else if( memb.getPrivateKey() != null )
            {
                // write access board (or write-only)
                setIcon(writeAccessIcon);
            }
            return this;    
        }
    }
}
