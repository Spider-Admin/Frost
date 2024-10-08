/*
  BoardInfoFrame.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.frost.boards;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.Core;
import frost.MainFrame;
import frost.SettingsClass;
import frost.fileTransfer.common.TableBackgroundColors;
import frost.gui.SortedTable;
import frost.gui.model.SortedTableModel;
import frost.gui.model.TableMember;
import frost.storage.perst.messages.MessageStorage;
import frost.util.ClipboardUtil;
import frost.util.DateFun;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.Language;
import frost.util.gui.translation.LanguageEvent;
import frost.util.gui.translation.LanguageListener;

/**
 *
 * @author $Author: $
 * @version $Revision: $
 */
@SuppressWarnings("serial")
public class BoardInfoFrame extends JFrame implements BoardUpdateThreadListener {

	private static final Logger logger = LoggerFactory.getLogger(BoardInfoFrame.class);

    private final boolean showColoredLines;

    /**
     *
     */
    private class Listener implements MouseListener, LanguageListener {
        public Listener() {
            super();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                updateSelectedBoardButton_actionPerformed(null);
            }
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(final MouseEvent e) {
            //Nothing here
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(final MouseEvent e) {
            //Nothing here
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * @param e
         */
        private void maybeShowPopup(final MouseEvent e) {
            if( e.isPopupTrigger() ) {
                getPopupMenu().show(boardTable, e.getX(), e.getY());
            }
        }

        /* (non-Javadoc)
         * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
         */
        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }
    }

    private MainFrame mainFrame;
    private TofTree tofTree = null;
    private static boolean isShowing = false; // flag, is true if frame is showing, used by frame1
    private Language language = null;
    private final Listener listener = new Listener();

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JPanel boardTablePanel = new JPanel(new BorderLayout());

    private final JLabel summaryLabel = new JLabel();

    private final JButton updateButton = new JButton();
    private final JButton updateSelectedBoardButton = new JButton();
    private final JButton updateAllBoardsButton = new JButton();
    private final JButton removeSelectedBoardsButton = new JButton();
    private final JButton Bclose = new JButton();

    private JSkinnablePopupMenu popupMenu = null;
    private final JMenuItem MIupdate = new JMenuItem();
    private final JMenuItem MIupdateSelectedBoard = new JMenuItem();
    private final JMenuItem MIupdateAllBoards = new JMenuItem();
    private final JMenuItem MIcopyInfoToClipboard = new JMenuItem();
    private final JMenuItem MIremoveSelectedBoards = new JMenuItem();

    private BoardInfoTableModel boardTableModel = null;
    private SortedTable<BoardInfoTableMember> boardTable = null;

    /**
     *
     */
    private void refreshLanguage() {
        setTitle(language.getString("BoardInfoFrame.title"));

        updateButton.setText(language.getString("BoardInfoFrame.button.update"));
        updateSelectedBoardButton.setText(language.getString("BoardInfoFrame.button.updateSelectedBoard"));
        updateAllBoardsButton.setText(language.getString("BoardInfoFrame.button.updateAllBoards"));
        removeSelectedBoardsButton.setText(language.getString("BoardInfoFrame.button.removeSelectedBoards"));
        Bclose.setText(language.getString("BoardInfoFrame.button.close"));

        MIupdate.setText(language.getString("BoardInfoFrame.button.update"));
        MIupdateSelectedBoard.setText(language.getString("BoardInfoFrame.button.updateSelectedBoard"));
        MIupdateAllBoards.setText(language.getString("BoardInfoFrame.button.updateAllBoards"));
        MIcopyInfoToClipboard.setText(language.getString("BoardInfoFrame.popupMenu.copyInfoToClipboard"));
        MIremoveSelectedBoards.setText(language.getString("BoardInfoFrame.button.removeSelectedBoards"));
    }

    /**
     * @param mainFrame
     * @param tofTree
     */
    public BoardInfoFrame(final MainFrame mainFrame, final TofTree tofTree) {
        super();
        this.mainFrame = mainFrame;
        this.tofTree = tofTree;

        language = Language.getInstance();
        refreshLanguage();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( final Exception e ) {
            logger.error("Exception thrown in constructor", e);
        }

        int width = (int) (mainFrame.getWidth() * 0.75);
        int height = (int) (mainFrame.getHeight() * 0.75);

        if( width < 1000 ) {
        	Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        	if( screenSize.width > 1300 ) {
        		width = 1200;

        	} else if( screenSize.width > 1000 ) {
        		width = (int) (mainFrame.getWidth() * 0.99);
        	}
        }

        if( height < 500 ) {
        	Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        	if( screenSize.width > 900 ) {
        		height = 800;
        	} else {
        		height = (int) (screenSize.width * 0.85);
        	}
        }

        mainFrame.getWidth();

        setSize(width, height);
        setLocationRelativeTo(mainFrame);

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
    }

    /**
     * @throws Exception
     */
    private void Init() throws Exception {

        boardTableModel = new BoardInfoTableModel();
        boardTable = new SortedTable<BoardInfoTableMember>(boardTableModel);

        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

        final ImageIcon frameIcon = MiscToolkit.loadImageIcon("/data/jtc.jpg");
        setIconImage(frameIcon.getImage());
        setSize(new Dimension(350, 200));
        setResizable(true);

        boardTable.setRowSelectionAllowed(true);
        boardTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        boardTable.setRowHeight(18); // we use 16x16 icons, keep a gap

        boardTable.setDefaultRenderer( Object.class, new BoardInfoTableCellRenderer(SwingConstants.LEFT) );
        boardTable.setDefaultRenderer( Number.class, new BoardInfoTableCellRenderer(SwingConstants.RIGHT) );

        updateSelectedBoardButton.setEnabled(false);

        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------
        boardTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                     public void valueChanged(final ListSelectionEvent e) {
                         boardTableListModel_valueChanged(e);
                     } });

        // updateButton
        ActionListener al = new java.awt.event.ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        updateButton_actionPerformed();
                    } };
        updateButton.addActionListener(al);
        MIupdate.addActionListener(al);

        // updateSelectedBoardButton
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                updateSelectedBoardButton_actionPerformed(e);
            } };
        updateSelectedBoardButton.addActionListener(al);
        MIupdateSelectedBoard.addActionListener(al);

        // updateAllBoardsButton
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                updateAllBoardsButton_actionPerformed(e);
            } };
        updateAllBoardsButton.addActionListener(al);
        MIupdateAllBoards.addActionListener(al);

        al = new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                removeSelectedBoards_actionPerformed(e);
            } };
        removeSelectedBoardsButton.addActionListener(al);
        MIremoveSelectedBoards.addActionListener(al);

        MIcopyInfoToClipboard.addActionListener( new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                copyInfoToClipboard_actionPerformed(e);
            }
        });

        // Bclose
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
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
        final JPanel summaryPanel = new JPanel();
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

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));
        buttonsPanel.add(updateSelectedBoardButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(15,3)));
        buttonsPanel.add(updateAllBoardsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(15,3))); // ensure minimum glue size
        buttonsPanel.add(removeSelectedBoardsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(15,3))); // ensure minimum glue size
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(Bclose);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        boardTable.addMouseListener(listener);

        updateButton_actionPerformed();

        // set table column sizes
        final int[] newWidths = { 150,30,20,20,20,20,20,40 };

        for (int i = 0; i < newWidths.length; i++) {
            boardTable.getColumnModel().getColumn(i).setPreferredWidth(newWidths[i]);
        }
    }

    private JSkinnablePopupMenu getPopupMenu() {
        if( popupMenu == null ) {
            popupMenu = new JSkinnablePopupMenu();

            popupMenu.add(MIcopyInfoToClipboard);
            popupMenu.addSeparator();
            popupMenu.add(MIupdateSelectedBoard);
            popupMenu.add(MIupdateAllBoards);
            popupMenu.addSeparator();
            popupMenu.add(MIremoveSelectedBoards);
            popupMenu.addSeparator();
            popupMenu.add(MIupdate);
        }
        return popupMenu;
    }

    private void boardTableListModel_valueChanged(final ListSelectionEvent e) {
        if( boardTable.getSelectedRowCount() > 0 ) {
            setEnabledStateOfDynamicComponents(true);
        } else {
            setEnabledStateOfDynamicComponents(false);
        }
    }

    private void setEnabledStateOfDynamicComponents(final boolean state) {
        updateSelectedBoardButton.setEnabled(state);
        MIupdateSelectedBoard.setEnabled(state);
        removeSelectedBoardsButton.setEnabled(state);
        MIremoveSelectedBoards.setEnabled(state);
        MIcopyInfoToClipboard.setEnabled(state);
    }

    private static UpdateBoardInfoTableThread updateBoardInfoTableThread = null;

    private void updateButton_actionPerformed() {
        if( updateBoardInfoTableThread != null ) {
            return;
        }

        ((BoardInfoTableModel)boardTable.getModel()).clearDataModel();

        updateBoardInfoTableThread = new UpdateBoardInfoTableThread();
        updateBoardInfoTableThread.start();

        setEnabledStateOfDynamicComponents(false);
    }

    private class UpdateBoardInfoTableThread extends Thread
    {
        @Override
        public void run()
        {
            int messageCount = 0;
            int boardCount = 0;
            final List<Board> boards = ((TofTreeModel) tofTree.getModel()).getAllBoards();

            for( final Board board : boards ) {
                final BoardInfoTableMember newRow = new BoardInfoTableMember(board);
                fillInBoardCounts(board, newRow);

                // count statistics
                messageCount += newRow.getAllMessageCount().intValue();
                boardCount++;

                final BoardInfoTableMember finalRow = newRow;
                final int finalBoardCount = boardCount;
                final int finalMessageCount = messageCount;
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            boardTableModel.addRow(finalRow);
                            summaryLabel.setText(language.getString("BoardInfoFrame.label.boards") +": "+
                                                 finalBoardCount +"    "+
                                                 language.getString("BoardInfoFrame.label.messages") +": "+
                                                 finalMessageCount);
                        }});
            }
            updateBoardInfoTableThread = null;
        }
    }

    /**
     * @param e
     */
    private void removeSelectedBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardTable.getSelectedRows();

        final ArrayList<Board> boardsToDelete = new ArrayList<Board>();
        for( final int rowIx : selectedRows ) {
            if( rowIx >= boardTableModel.getRowCount() ) {
                continue; // paranoia
            }
            final BoardInfoTableMember row = boardTableModel.getRow(rowIx);
            boardsToDelete.add(row.getBoard());
        }

        for( final Board board : boardsToDelete ) {
            mainFrame.getMessagingTab().getTofTree().removeNode(this, board);
            updateButton_actionPerformed();
        }
    }

    /**
     * Tries to start update for all allowed boards.
     * Gets list of board from tofTree, because the board table could be
     * not yet finished to load.
     */
    private void updateAllBoardsButton_actionPerformed(final ActionEvent e) {
        final List<Board> boards = ((TofTreeModel) tofTree.getModel()).getAllBoards();
        for( final Board board : boards ) {
            if( board.isManualUpdateAllowed() ) {
                tofTree.updateBoard(board);
            }
            boardTableModel.fireTableDataChanged();
        }
    }

    private void updateSelectedBoardButton_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardTable.getSelectedRows();

        if( selectedRows.length > 0 ) {
            for( final int rowIx : selectedRows ) {
                if( rowIx >= boardTableModel.getRowCount() ) {
                    continue; // paranoia
                }

                final BoardInfoTableMember row = (boardTableModel).getRow(rowIx);

                if( row.getBoard().isManualUpdateAllowed() ) {
                    tofTree.updateBoard(row.getBoard());
                }
                boardTableModel.fireTableCellUpdated(rowIx, 0);
            }
            boardTable.clearSelection();
        }
    }

    private void copyInfoToClipboard_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardTable.getSelectedRows();

        if( selectedRows.length > 0 ) {
            final StringBuilder sb = new StringBuilder();
            for( final int rowIx : selectedRows ) {
                if( rowIx >= boardTableModel.getRowCount() ) {
                    continue; // paranoia
                }

                final BoardInfoTableMember row = (boardTableModel).getRow(rowIx);

                final String boardName = row.getBoard().getName();
                final String state     = row.getBoard().getStateString();
                final String allMsgs   = row.getAllMessageCount().toString();

                sb.append(boardName).append("  (").append(state).append(")  ").append(allMsgs).append("\n");
            }
            ClipboardUtil.copyText(sb.toString());
        }
    }

    /**
     * Gets number of new+all messages and files of a board
     *
     * @param board name of the board
     * @return Integer value
     */
    public BoardInfoTableMember fillInBoardCounts(final Board board, final BoardInfoTableMember row) {

        final int countTodaysMessages  = MessageStorage.inst().getMessageCount(board, 0);
        final int countAllMessages     = MessageStorage.inst().getMessageCount(board, -1);
        final int countFlaggedMessages = MessageStorage.inst().getFlaggedMessageCount(board);
        final int countStarredMessages = MessageStorage.inst().getStarredMessageCount(board);
        final int countUnreadMessages  = MessageStorage.inst().getUnreadMessageCount(board);
		final OffsetDateTime dateTime = MessageStorage.inst().getDateTimeOfLatestMessage(board);
        final String dateStr;
        if (dateTime != null) {
			final OffsetDateTime date = DateFun.toStartOfDay(dateTime);
			dateStr = DateFun.FORMAT_DATE_EXT.format(date);
        } else {
            dateStr = "---";
        }

        row.setAllMessageCount(countAllMessages);
        row.setTodaysMessageCount(countTodaysMessages);
        row.setFlaggedMessageCount(countFlaggedMessages);
        row.setStarredMessageCount(countStarredMessages);
        row.setUnreadMessageCount(countUnreadMessages);
        row.setDateOfLastMsg(dateStr);

        return row;
    }

    public void startDialog() {
        tofTree.getRunningBoardUpdateThreads().addBoardUpdateThreadListener(this);
        language.addLanguageListener(listener);
        language.addLanguageListener(boardTableModel);
        setDialogShowing(true);
        setVisible(true);
    }

    protected void closeDialog() {
        tofTree.getRunningBoardUpdateThreads().removeBoardUpdateThreadListener(this);
        language.removeLanguageListener(listener);
        language.removeLanguageListener(boardTableModel);
        setDialogShowing(false);
        dispose();
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        if( e.getID() == WindowEvent.WINDOW_CLOSING ) {
            // setDialogShowing( false ); // also done in closeDialog()
            closeDialog();
        }
        super.processWindowEvent(e);
    }

    /**
     * The class is a table row, holding the board and its file/message counts.
     */
    class BoardInfoTableMember extends TableMember.BaseTableMember<BoardInfoTableMember> {
        Board board;
        Integer allMsgCount;
        Integer todaysMsgCount;
        Integer flaggedMsgCount;
        Integer starredMsgCount;
        Integer unreadMsgCount;
        String dateOfLastMsg;

        public BoardInfoTableMember(final Board board) {
            this.board = board;
            this.allMsgCount = null;
            this.todaysMsgCount = null;
            this.flaggedMsgCount = null;
            this.starredMsgCount = null;
            this.unreadMsgCount = null;
            this.dateOfLastMsg = null;
        }

		public Comparable<?> getValueAt(final int column) {
            switch( column ) {
            case 0:
                return board.getName();
            case 1:
                return board.getStateString();
            case 2:
                return allMsgCount;
            case 3:
                return todaysMsgCount;
            case 4: // flagged
                return flaggedMsgCount;
            case 5: // starred
                return starredMsgCount;
            case 6: // unread
                return unreadMsgCount;
            case 7: // date of last msg
                return dateOfLastMsg;
            }
            return "*ERR*";
        }

        public Board getBoard() {
            return board;
        }

        public Integer getAllMessageCount() {
            return allMsgCount;
        }

        public void setAllMessageCount(final int i) {
			allMsgCount = i;
        }

        public void setTodaysMessageCount(final int i) {
			todaysMsgCount = i;
        }

        public void setFlaggedMessageCount(final int i) {
			flaggedMsgCount = i;
        }
        public void setStarredMessageCount(final int i) {
			starredMsgCount = i;
        }

        public void setUnreadMessageCount(final int i) {
			unreadMsgCount = i;
        }

        public void setDateOfLastMsg(final String s) {
            dateOfLastMsg = s;
        }
    }

    static public class BoardInfoTableModel extends SortedTableModel<BoardInfoTableMember> implements LanguageListener
    {
        private Language language = null;

        protected final static String columnNames[] = new String[8];

        protected final static Class<?> columnClasses[] =  {
            String.class,   // board name
            String.class,   // board state
            Integer.class,  // message count
            Integer.class,  // today
            Integer.class,  // flagged
            Integer.class,  // starred
            Integer.class,  // unread
            String.class,   // date of last valid msg
        };

        public BoardInfoTableModel() {
            super();
            language = Language.getInstance();
            refreshLanguage();
        }

        private void refreshLanguage() {
            columnNames[0] = language.getString("BoardInfoFrame.table.board");
            columnNames[1] = language.getString("BoardInfoFrame.table.state");
            columnNames[2] = language.getString("BoardInfoFrame.table.messages");
            columnNames[3] = language.getString("BoardInfoFrame.table.messagesToday");
            columnNames[4] = language.getString("BoardInfoFrame.table.messagesFlagged");
            columnNames[5] = language.getString("BoardInfoFrame.table.messagesStarred");
            columnNames[6] = language.getString("BoardInfoFrame.table.messagesUnread");
            columnNames[7] = language.getString("BoardInfoFrame.table.lastMsgDate");

            fireTableStructureChanged();
        }

        /* (non-Javadoc)
         * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
         */
        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(final int row, final int col) {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(final int column) {
            if( (column >= 0) && (column < columnNames.length) ) {
                return columnNames[column];
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(final int column) {
            if( (column >= 0) && (column < columnClasses.length) ) {
                return columnClasses[column];
            }
            return null;
        }
    }

    private class BoardInfoTableCellRenderer extends DefaultTableCellRenderer {
        final Font boldFont;
        final Font origFont;
        final Border border;

        public BoardInfoTableCellRenderer(final int horizontalAlignment) {
            super();
            origFont = boardTable.getFont();
            boldFont = origFont.deriveFont(Font.BOLD);
            border = BorderFactory.createEmptyBorder(0, 3, 0, 3);
            setVerticalAlignment(SwingConstants.CENTER);
            setHorizontalAlignment(horizontalAlignment);
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                final boolean hasFocus, final int row, final int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            final BoardInfoTableMember tblrow = boardTableModel.getRow(row);

            if( tblrow.getBoard().isUpdating() ) {
                setFont(boldFont);
            } else {
                setFont(origFont);
            }
            setBorder(border);

            // get the original model column index (maybe columns were reordered by user)
            final TableColumn tableColumn = table.getColumnModel().getColumn(column);
            final int modelColumn = tableColumn.getModelIndex();

            if( modelColumn == 0 ) {
                setIcon(tblrow.getBoard().getStateIcon());
            } else {
                setIcon(null);
            }

            if (!isSelected) {
                final Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            }
            return this;
        }
    }

    // Implementing the BoardUpdateThreadListener ...

     /**
      * Is called if a Thread is finished.
      */
     public void boardUpdateThreadFinished(final BoardUpdateThread thread) {
        boardTableModel.tableEntriesChanged();
    }

    /**
     * Is called if a Thread is started.
     *
     * @see frost.messaging.frost.boards.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.messaging.frost.boards.BoardUpdateThread)
     */
    public void boardUpdateThreadStarted(final BoardUpdateThread thread) {
        boardTableModel.tableEntriesChanged();
    }

    public void boardUpdateInformationChanged(final BoardUpdateThread thread, final BoardUpdateInformation bui) {
    }

    public static boolean isDialogShowing() {
        return isShowing;
    }

    public static void setDialogShowing(final boolean val) {
        isShowing = val;
    }
}
