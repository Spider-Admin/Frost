/*
  BoardInfoFrame.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;
import frost.threads.*;

public class BoardInfoFrame extends JFrame implements BoardUpdateThreadListener
{
    frame1 parent = null;
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
    static boolean isShowing = false; // flag, is true if frame is showing, used by frame1

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel boardTablePanel = new JPanel(new BorderLayout());

    JLabel summaryLabel = new JLabel();

    JButton updateButton = new JButton(LangRes.getString("Update"));
    JButton updateSelectedBoardButton = new JButton(LangRes.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
    JButton updateAllBoardsButton = new JButton("Update all boards");
    JButton Bclose = new JButton("Close");

    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem MIupdate = new JMenuItem(LangRes.getString("Update"));
    JMenuItem MIupdateSelectedBoard = new JMenuItem(LangRes.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
    JMenuItem MIupdateAllBoards = new JMenuItem("Update all boards");

    BoardInfoTableModel boardTableModel = new BoardInfoTableModel();
    SortedTable boardTable = new SortedTable(boardTableModel);

    /**Constructor*/
    public BoardInfoFrame(frame1 p)
    {
        super();
        parent = p;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        pack();
        setLocationRelativeTo( parent );
    }

    private void Init() throws Exception {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

        this.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
        this.setTitle(LangRes.getString("Board information"));
        this.setSize(new Dimension(300, 200));
        this.setResizable(true);

        boardTable.setRowSelectionAllowed(true);
        boardTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

        BoardInfoTableCellRenderer cellRenderer = new BoardInfoTableCellRenderer();
        boardTable.setDefaultRenderer( Object.class, cellRenderer );
        boardTable.setDefaultRenderer( Number.class, cellRenderer );

        updateSelectedBoardButton.setEnabled(false);

        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------
        boardTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                     public void valueChanged(ListSelectionEvent e) {
                         boardTableListModel_valueChanged(e);
                     } });

        // updateButton
        ActionListener al = new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateButton_actionPerformed(e);
                    } };
        updateButton.addActionListener(al);
        MIupdate.addActionListener(al);

        // updateSelectedBoardButton
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedBoardButton_actionPerformed(e);
            } };
        updateSelectedBoardButton.addActionListener(al);
        MIupdateSelectedBoard.addActionListener(al);

        // updateAllBoardsButton
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAllBoardsButton_actionPerformed(e);
            } };
        updateAllBoardsButton.addActionListener(al);
        MIupdateAllBoards.addActionListener(al);

        // Bclose
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            } };
        Bclose.addActionListener(al);

        //------------------------------------------------------------------------
        // Append objects
        //------------------------------------------------------------------------
        this.getContentPane().add(mainPanel, null); // add Main panel

        mainPanel.add(boardTablePanel, BorderLayout.CENTER);
        boardTablePanel.add(updateSelectedBoardButton, BorderLayout.NORTH);
        boardTablePanel.add(new JScrollPane(boardTable), BorderLayout.CENTER);
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.X_AXIS));
        summaryPanel.setBorder(new EmptyBorder(5,0,0,0));
        summaryPanel.add(summaryLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(15,3))); // ensure minimum glue size
        summaryPanel.add(Box.createHorizontalGlue());
        summaryPanel.add(updateButton);

        boardTablePanel.add(summaryPanel, BorderLayout.SOUTH);
        boardTablePanel.setBorder( new CompoundBorder(
                                                     new EtchedBorder(),
                                                     new EmptyBorder(7,7,7,7)
                                                     ));
        boardTablePanel.setBorder( new CompoundBorder(
                                                     new EmptyBorder(7,7,7,7),
                                                     boardTablePanel.getBorder()
                                                     ));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));
        buttonsPanel.add(updateSelectedBoardButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(15,3)));
        buttonsPanel.add(updateAllBoardsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(15,3))); // ensure minimum glue size
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(Bclose);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        boardTable.addMouseListener(new TableDoubleClickMouseListener());
        boardTable.addMouseListener(new TablePopupMenuMouseListener());

        popupMenu.add(MIupdateSelectedBoard);
        popupMenu.add(MIupdateAllBoards);
        popupMenu.addSeparator();
        popupMenu.add(MIupdate);

        updateButton_actionPerformed(null);
    }

    class TableDoubleClickMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {}
        public void mousePressed(MouseEvent event) {}
        public void mouseClicked(MouseEvent event) {
            if( event.getClickCount() == 2 ) {
                updateSelectedBoardButton_actionPerformed(null);
            }
        }
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
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
                popupMenu.show(boardTable, e.getX(), e.getY());
            }
        }
    }

    private void boardTableListModel_valueChanged(ListSelectionEvent e)
    {
        if( boardTable.getSelectedRowCount() > 0 )
        {
            updateSelectedBoardButton.setEnabled(true);
            MIupdateSelectedBoard.setEnabled(true);
        }
        else
        {
            updateSelectedBoardButton.setEnabled(false);
            MIupdateSelectedBoard.setEnabled(false);
        }
    }

    private static UpdateBoardInfoTableThread updateBoardInfoTableThread = null;

    private void updateButton_actionPerformed(ActionEvent e)
    {
        if( updateBoardInfoTableThread != null )
            return;

        ((SortedTableModel)boardTable.getModel()).clearDataModel();

        updateBoardInfoTableThread = new UpdateBoardInfoTableThread();
        updateBoardInfoTableThread.start();

        updateSelectedBoardButton.setEnabled(false);
        MIupdateSelectedBoard.setEnabled(false);
    }

    private class UpdateBoardInfoTableThread extends Thread
    {
        public void run()
        {
            int messageCount = 0;
            int fileCount = 0;
            int boardCount = 0;
            Vector boards = parent.getTofTree().getAllBoards();
            for( int i = 0; i < boards.size(); i++ )
            {
                FrostBoardObject board = (FrostBoardObject)boards.elementAt(i);
                String boardName = board.toString();

                BoardInfoTableMember newRow = new BoardInfoTableMember(board);
                fillInBoardCounts(board, newRow);

                // count statistics
                messageCount += newRow.getAllMessageCount().intValue();
                fileCount += newRow.getFilesCount().intValue();
                boardCount++;

                final BoardInfoTableMember finalRow = newRow;
                final int finalBoardCount = boardCount;
                final int finalMessageCount = messageCount;
                final int finalFileCount = fileCount;
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            boardTableModel.addRow(finalRow);
                            summaryLabel.setText(LangRes.getString("Boards: ") +
                                                 finalBoardCount +
                                                 LangRes.getString("   Messages: ") +
                                                 finalMessageCount +
                                                 LangRes.getString("   Files: ") +
                                                 finalFileCount);
                        }});
            }
            updateBoardInfoTableThread = null;
        }
    }

    /**
     * Tries to start update for all allowed boards.
     * Gets list of board from tofTree, because the board table could be
     * not yet finished to load.
     */
    private void updateAllBoardsButton_actionPerformed(ActionEvent e)
    {
        Vector boards = parent.getTofTree().getAllBoards();
        for( int i = 0; i < boards.size(); i++ )
        {
            FrostBoardObject board = (FrostBoardObject)boards.elementAt(i);
            if( parent.isUpdateAllowed(board) == true ) // is update allowed for this board?
            {
                parent.updateBoard(board);
            }
            boardTableModel.fireTableDataChanged();
        }
    }

    private void updateSelectedBoardButton_actionPerformed(ActionEvent e)
    {
        int[] selectedRows = boardTable.getSelectedRows();

        if( selectedRows.length > 0 )
        {
            for( int z=0; z<selectedRows.length; z++ )
            {
                int rowIx = selectedRows[z];

                if( rowIx >= boardTableModel.getRowCount() )
                    continue; // paranoia

                BoardInfoTableMember row = (BoardInfoTableMember)((BoardInfoTableModel)boardTableModel).getRow(rowIx);

                if( parent.isUpdateAllowed(row.getBoard()) == true ) // is update allowed for this board?
                {
                    parent.updateBoard(row.getBoard());
                }
                boardTableModel.fireTableCellUpdated(rowIx, 0);
            }
            boardTable.clearSelection();
        }
    }

    /**
     * Gets number of new+all messages and files of a board
     * @param board name of the board
     * @return Integer value
     */
    public BoardInfoTableMember fillInBoardCounts(FrostBoardObject board, BoardInfoTableMember row)
    {
        int countNewMessages = 0;
        int countAllMessages = 0;
        int countFiles = 0;

        String date = DateFun.getDate();
        File boardDir = new File(frame1.keypool + board.getBoardFilename());

        if( boardDir.isDirectory() )
        {
            File[] entries = boardDir.listFiles();
            if( entries != null )
            {
                for( int i = 0; i < entries.length; i++ )
                {
                    if( entries[i].isDirectory() )
                    {
                        boolean newMessageDate = entries[i].getName().startsWith(date);

                        String[] messages = entries[i].list();
                        for( int j = 0; j < messages.length; j++ )
                        {
                            if( messages[j].endsWith(".txt") )
                            {
                                if( newMessageDate == true )
                                {
                                    countNewMessages++;
                                }
                                countAllMessages++;
                            }
                        }
                    }
                    else if( entries[i].getName().endsWith("files.xml") )
                    {
                        countFiles += getLineCount(entries[i]);
                    }
                }
            }
        }
        //countFiles /= 4;
        row.setAllMessageCount(countAllMessages);
        row.setNewMessageCount(countNewMessages);
        row.setFilesCount(countFiles);

        return row;
    }

    /**
     * Used to count the files of a board.
     * see fillInBoardCounts
     * was in FileAccess, but only used from here ...
     * TODO: search byte by byte for line separators, the actual
     *       code produces not needed Strings for each line
     */
    public int getLineCount(File file)
    {
        BufferedReader f;
        int count = 0;
      //  try {
            //f = new BufferedReader(new FileReader(file));
	    String current = FileAccess.readFile(file);
	    int index =0;
	    //boolean stop = false;
            while (true) {
	    	if (current.indexOf("<File>",index)==-1) break;
		index=current.indexOf("<File>",index)+6;
		count++;
	    }
            //f.close();
       /* }
        catch( IOException e )
        {
            System.out.println("getLineCount() - Read Error: " + file);
        }*/
        return count;
    }

    public static boolean isDialogShowing()
    {
        return isShowing;
    }
    public static void setDialogShowing( boolean val )
    {
        isShowing = val;
    }

    public void startDialog()
    {
        frame1.getInstance().getRunningBoardUpdateThreads().addBoardUpdateThreadListener( this );
        setDialogShowing( true );
        show();
    }

    protected void closeDialog()
    {
        frame1.getInstance().getRunningBoardUpdateThreads().removeBoardUpdateThreadListener( this );
        setDialogShowing( false );
        dispose();
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            // setDialogShowing( false ); // also done in closeDialog()
            closeDialog();
        }
        super.processWindowEvent(e);
    }

    /**
     * The class is a table row, holding the board and its file/message counts.
     */
    class BoardInfoTableMember implements TableMember
    {
        FrostBoardObject board;
        Integer allmsg;
        Integer newmsg;
        Integer files;

        public BoardInfoTableMember(FrostBoardObject board)
        {
            this.board = board;
            this.allmsg = null;
            this.newmsg = null;
            this.files = null;
        }

        public Object getValueAt(int column)
        {
            switch( column )
            {
                case 0: return board.toString();
                case 1: return board.getStateString();
                case 2: return allmsg;
                case 3: return newmsg;
                case 4: return files;
            }
            return "*ERR*";
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public FrostBoardObject getBoard()
        {
            return board;
        }
        public Integer getFilesCount()
        {
            return files;
        }
        public void setFilesCount(int i)
        {
            files = new Integer(i);
        }
        public Integer getAllMessageCount()
        {
            return allmsg;
        }
        public void setAllMessageCount(int i)
        {
            allmsg = new Integer(i);
        }
        public void setNewMessageCount(int i)
        {
            newmsg = new Integer(i);
        }
    }

    private class BoardInfoTableCellRenderer extends DefaultTableCellRenderer
    {
        Font boldFont;
        Font origFont;
        public BoardInfoTableCellRenderer()
        {
            super();
            origFont = boardTable.getFont();
            boldFont = origFont.deriveFont( Font.BOLD );
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

            BoardInfoTableMember tblrow = (BoardInfoTableMember)boardTableModel.getRow(row);

            if( tblrow.getBoard().isUpdating() )
            {
                setFont( boldFont );
            }
            else
            {
                setFont( origFont );
            }
            return this;
        }
    }

    // Implementing the BoardUpdateThreadListener ...

    /**
     * Is called if a Thread is finished.
     */
    public void boardUpdateThreadFinished(BoardUpdateThread thread)
    {
        boardTableModel.tableEntriesChanged();
    }
    /**
     * Is called if a Thread is started.
     */
    public void boardUpdateThreadStarted(BoardUpdateThread thread)
    {
        boardTableModel.tableEntriesChanged();
    }

}
