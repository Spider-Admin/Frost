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
import java.io.File;
import java.util.Vector;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.Board;
import frost.messages.FrostIndex;
import frost.threads.*;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class BoardInfoFrame extends JFrame implements BoardUpdateThreadListener 
{
	/**
	 * 
	 */
	private class Listener implements MouseListener, LanguageListener {
		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				updateSelectedBoardButton_actionPerformed(null);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			//Nothing here			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			//Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);			
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if( e.isPopupTrigger() ) {
				popupMenu.show(boardTable, e.getX(), e.getY());
			}
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();			
		}

	}
	
    private MainFrame parent = null;
    private static boolean isShowing = false; // flag, is true if frame is showing, used by frame1
    private Language language = null;
    private Listener listener = new Listener();
    
	private static Logger logger = Logger.getLogger(BoardInfoFrame.class.getName());

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel boardTablePanel = new JPanel(new BorderLayout());

	private JLabel summaryLabel = new JLabel();

    private JButton updateButton = new JButton();
    private JButton updateSelectedBoardButton = new JButton();
    private JButton updateAllBoardsButton = new JButton();
    private JButton Bclose = new JButton();

    private JSkinnablePopupMenu popupMenu = new JSkinnablePopupMenu();
    private JMenuItem MIupdate = new JMenuItem();
    private JMenuItem MIupdateSelectedBoard = new JMenuItem();
    private JMenuItem MIupdateAllBoards = new JMenuItem();

    private BoardInfoTableModel boardTableModel = null;
    private SortedTable boardTable = null;

	/**
	 * 
	 */
	private void refreshLanguage() {
		setTitle(language.getString("BoardInfoFrame.Board information window"));
		
		updateButton.setText(language.getString("BoardInfoFrame.Update"));
		updateSelectedBoardButton.setText(language.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
		updateAllBoardsButton.setText(language.getString("BoardInfoFrame.Update all boards"));
		Bclose.setText(language.getString("BoardInfoFrame.Close"));	
		
		MIupdate.setText(language.getString("BoardInfoFrame.Update"));
		MIupdateSelectedBoard.setText(language.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
		MIupdateAllBoards.setText(language.getString("BoardInfoFrame.Update all boards"));		
	}

    /**
     * Constructor
     * @param p
     */
    public BoardInfoFrame(MainFrame p)
    {
        super();
        language = Language.getInstance();
		refreshLanguage();
        parent = p;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( Exception e ) {
        	logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        pack();
        setLocationRelativeTo( parent );
    }

    /**
     * @throws Exception
     */
    private void Init() throws Exception {
    	
		boardTableModel = new BoardInfoTableModel();
		boardTable = new SortedTable(boardTableModel);
    	
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

		ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/jtc.jpg"));
        setIconImage(frameIcon.getImage());
        setSize(new Dimension(300, 200));
        setResizable(true);

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

        boardTable.addMouseListener(listener);

        popupMenu.add(MIupdateSelectedBoard);
        popupMenu.add(MIupdateAllBoards);
        popupMenu.addSeparator();
        popupMenu.add(MIupdate);

        updateButton_actionPerformed(null);
    }

    /**
     * @param e
     */
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

    /**
     * @param e
     */
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

    /**
     * 
     */
    private class UpdateBoardInfoTableThread extends Thread
    {
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            int messageCount = 0;
            int fileCount = 0;
            int boardCount = 0;
            Vector boards = parent.getTofTree().getAllBoards();
            for( int i = 0; i < boards.size(); i++ )
            {
                Board board = (Board)boards.elementAt(i);
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
                            summaryLabel.setText(language.getString("BoardInfoFrame.Boards") +"   :"+
                                                 finalBoardCount +"    "+
                                                 language.getString("BoardInfoFrame.Messages") +"  : "+
                                                 finalMessageCount +"    "+
                                                 language.getString("BoardInfoFrame.Files") + "   :"+
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
     * @param e
     */
    private void updateAllBoardsButton_actionPerformed(ActionEvent e)
    {
        Vector boards = parent.getTofTree().getAllBoards();
        for( int i = 0; i < boards.size(); i++ )
        {
            Board board = (Board)boards.elementAt(i);
            if( parent.isUpdateAllowed(board) == true ) // is update allowed for this board?
            {
                parent.updateBoard(board);
            }
            boardTableModel.fireTableDataChanged();
        }
    }

    /**
     * @param e
     */
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
    public BoardInfoTableMember fillInBoardCounts(Board board, BoardInfoTableMember row)
    {
        int countNewMessages = 0;
        int countAllMessages = 0;
        int countFiles = 0;

        String date = DateFun.getDate();
        File boardDir = new File(MainFrame.keypool + board.getBoardFilename());

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
                            if( messages[j].endsWith(".xml") )
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
     * @param file
     * @return
     */
    public int getLineCount(File file)
    {
       FrostIndex current = FileAccess.readKeyFile(file); 
       
	    
        return current.getFilesMap().size();
    }

	/**
	 * 
	 */
	public void startDialog() {
		MainFrame.getInstance().getRunningBoardUpdateThreads().addBoardUpdateThreadListener(this);
		language.addLanguageListener(listener);
		language.addLanguageListener(boardTableModel);
		setDialogShowing(true);
		setVisible(true);
	}

	/**
	 * 
	 */
	protected void closeDialog() {
		MainFrame.getInstance().getRunningBoardUpdateThreads().removeBoardUpdateThreadListener(this);
		language.removeLanguageListener(listener);
		language.removeLanguageListener(boardTableModel);
		setDialogShowing(false);
		dispose();
	}

    /* (non-Javadoc)
     * @see java.awt.Window#processWindowEvent(java.awt.event.WindowEvent)
     */
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
        Board board;
        Integer allmsg;
        Integer newmsg;
        Integer files;

        /**
         * @param board
         */
        public BoardInfoTableMember(Board board)
        {
            this.board = board;
            this.allmsg = null;
            this.newmsg = null;
            this.files = null;
        }

        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#getValueAt(int)
         */
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
        
        /* (non-Javadoc)
         * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember, int)
         */
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        
        /**
         * @return
         */
        public Board getBoard()
        {
            return board;
        }
        
        /**
         * @return
         */
        public Integer getFilesCount()
        {
            return files;
        }
        
        /**
         * @param i
         */
        public void setFilesCount(int i)
        {
            files = new Integer(i);
        }
        
        /**
         * @return
         */
        public Integer getAllMessageCount()
        {
            return allmsg;
        }
        
        /**
         * @param i
         */
        public void setAllMessageCount(int i)
        {
            allmsg = new Integer(i);
        }
        
        /**
         * @param i
         */
        public void setNewMessageCount(int i)
        {
            newmsg = new Integer(i);
        }
    }

    /**
     * 
     */
    private class BoardInfoTableCellRenderer extends DefaultTableCellRenderer
    {
        Font boldFont;
        Font origFont;
        
        /**
         * 
         */
        public BoardInfoTableCellRenderer()
        {
            super();
            origFont = boardTable.getFont();
            boldFont = origFont.deriveFont( Font.BOLD );
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
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
    
	//	Implementing the BoardUpdateThreadListener ...
	
	 /**
	  * Is called if a Thread is finished.
	  */
	 public void boardUpdateThreadFinished(BoardUpdateThread thread)
	 {
		 boardTableModel.tableEntriesChanged();
	 }
	 
	/**
	 * Is called if a Thread is started.
	 * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.threads.BoardUpdateThread)
	 */
	public void boardUpdateThreadStarted(BoardUpdateThread thread)
	 {
		 boardTableModel.tableEntriesChanged();
	 }
	 
	/**
	 * @return
	 */
	public static boolean isDialogShowing() {
		return isShowing;
	}
	   
	/**
	 * @param val
	 */
	public static void setDialogShowing(boolean val) {
		isShowing = val;
	}

}
