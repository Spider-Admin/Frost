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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.joda.time.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.gui.*;
import frost.gui.model.*;
import frost.storage.perst.messages.*;
import frost.threads.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class BoardInfoFrame extends JFrame implements BoardUpdateThreadListener {

    private final boolean showColoredLines;

    private class Listener implements MouseListener, LanguageListener {
        public Listener() {
            super();
        }

        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                updateSelectedBoardButton_actionPerformed(null);
            }
        }

        public void mouseEntered(final MouseEvent e) {
            //Nothing here
        }

        public void mouseExited(final MouseEvent e) {
            //Nothing here
        }

        public void mousePressed(final MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(final MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(final MouseEvent e) {
            if( e.isPopupTrigger() ) {
                getPopupMenu().show(boardTable, e.getX(), e.getY());
            }
        }

        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }
    }

    private TofTree tofTree = null;
    private static boolean isShowing = false; // flag, is true if frame is showing, used by frame1
    private Language language = null;
    private final Listener listener = new Listener();

    private static final Logger logger = Logger.getLogger(BoardInfoFrame.class.getName());

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
    private SortedTable boardTable = null;

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

    public BoardInfoFrame(final JFrame parentFrame, final TofTree tofTree) {
        super();
        language = Language.getInstance();
        refreshLanguage();
        this.tofTree = tofTree;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( final Exception e ) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        setSize((int) (parentFrame.getWidth() * 0.75),
                (int) (parentFrame.getHeight() * 0.75));
        setLocationRelativeTo(parentFrame);

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
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

        ((SortedTableModel)boardTable.getModel()).clearDataModel();

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

    private void removeSelectedBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardTable.getSelectedRows();

        final ArrayList<Board> boardsToDelete = new ArrayList<Board>();
        for( final int rowIx : selectedRows ) {
            if( rowIx >= boardTableModel.getRowCount() ) {
                continue; // paranoia
            }
            final BoardInfoTableMember row = (BoardInfoTableMember) boardTableModel.getRow(rowIx);
            boardsToDelete.add(row.getBoard());
        }

        for( final Board board : boardsToDelete ) {
            Core.getInstance().getMainFrame().getFrostMessageTab().getTofTree().removeNode(this, board);
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

                final BoardInfoTableMember row = (BoardInfoTableMember) (boardTableModel).getRow(rowIx);

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

                final BoardInfoTableMember row = (BoardInfoTableMember) (boardTableModel).getRow(rowIx);

                final String boardName = row.getBoard().getName();
                final String state     = row.getBoard().getStateString();
                final String allMsgs   = row.getAllMessageCount().toString();

                sb.append(boardName).append("  (").append(state).append(")  ").append(allMsgs).append("\n");
            }
            CopyToClipboard.copyText(sb.toString());
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
        final DateTime dateTime = MessageStorage.inst().getDateTimeOfLatestMessage(board);
        final String dateStr;
        if (dateTime != null) {
            final DateMidnight date = dateTime.toDateMidnight();
            dateStr = DateFun.FORMAT_DATE_EXT.print(date);
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
    class BoardInfoTableMember implements TableMember {
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

        public Object getValueAt(final int column) {
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

        public int compareTo(final TableMember anOther, final int tableColumIndex) {
            final Comparable c1 = (Comparable) getValueAt(tableColumIndex);
            final Comparable c2 = (Comparable) anOther.getValueAt(tableColumIndex);
            return c1.compareTo(c2);
        }

        public Board getBoard() {
            return board;
        }

        public Integer getAllMessageCount() {
            return allMsgCount;
        }

        public void setAllMessageCount(final int i) {
            allMsgCount = new Integer(i);
        }

        public void setTodaysMessageCount(final int i) {
            todaysMsgCount = new Integer(i);
        }
        public void setFlaggedMessageCount(final int i) {
            flaggedMsgCount = new Integer(i);
        }
        public void setStarredMessageCount(final int i) {
            starredMsgCount = new Integer(i);
        }
        public void setUnreadMessageCount(final int i) {
            unreadMsgCount = new Integer(i);
        }
        public void setDateOfLastMsg(final String s) {
            dateOfLastMsg = s;
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

            final BoardInfoTableMember tblrow = (BoardInfoTableMember) boardTableModel.getRow(row);

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
     * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.threads.BoardUpdateThread)
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
