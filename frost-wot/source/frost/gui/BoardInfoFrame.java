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
-------------------------------------------------------------------------
  CHANGELOG:
  ----------
  2003/03/29 - bback
    - added close button, popup menu and updateAll button.
    - on double click the clicked member is updated now
    - multiple interval selection in table

*/
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import frost.gui.model.*;
import frost.*;

public class BoardInfoFrame extends JFrame
{
    frame1 parent = null;
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    class PlainOrBoldString implements Comparable
    {
        boolean isBold = false;
        String string;

        public PlainOrBoldString(String str, boolean bold)
        {
            string = str;
            isBold = bold;
        }
        public String toString()
        {
            return string;
        }
        public boolean isBold()
        {
            return isBold;
        }
        public void setBold(boolean bold)
        {
            isBold = bold;
        }
        public int compareTo(Object o)
        {
            if( o instanceof PlainOrBoldString )
            {
                return toString().compareTo( o.toString() );
            }
            return 1;
        }
    }

    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------

    Vector boards;

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JPanel mainPanel = new JPanel(new BorderLayout());
    //JPanel boardlistPanel = new JPanel(new BorderLayout());
    JPanel boardTablePanel = new JPanel(new BorderLayout());

    JLabel summaryLabel = new JLabel();

    JButton updateButton = new JButton(LangRes.getString("Update"));
    //JButton addBoardButton = new JButton(LangRes.getString("Add board"));
    JButton updateSelectedBoardButton = new JButton(LangRes.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
    JButton updateAllBoardsButton = new JButton("Update all boards");

    JButton Bclose = new JButton("Close");

    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem MIupdate = new JMenuItem(LangRes.getString("Update"));
    JMenuItem MIupdateSelectedBoard = new JMenuItem(LangRes.getString("BoardInfoFrame.UpdateSelectedBoardButton"));
    JMenuItem MIupdateAllBoards = new JMenuItem("Update all boards");

    BoardInfoTableModel boardTableModel = new BoardInfoTableModel();
//    DefaultListSelectionModel boardTableListModel = new DefaultListSelectionModel();
    SortedTable boardTable = new SortedTable(boardTableModel);

    //DefaultListModel boardListModel = new DefaultListModel();
    //JList boardList = new JList(boardListModel);

    //JScrollPane boardlistScrollPane = new JScrollPane(boardList);
    /*JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                      boardlistPanel,
                      boardTablePanel);*/

    private void Init() throws Exception {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

        this.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
        this.setTitle(LangRes.getString("Board information"));
        this.setSize(new Dimension(300, 200));
        this.setResizable(true);

        boardTable.setDefaultRenderer(Object.class, new BoardInfoTableCellRenderer());
        boardTable.setRowSelectionAllowed(true);
        boardTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        updateSelectedBoardButton.setEnabled(false);

        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------
        boardTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                         public void valueChanged(ListSelectionEvent e) {
                             boardTableListModel_valueChanged(e);
                         }
                     });

        // updateButton
        ActionListener al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateButton_actionPerformed(e);
            }
        };
        updateButton.addActionListener(al);
        MIupdate.addActionListener(al);

        // addBoardButton
        /*addBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBoardButton_actionPerformed(e);
            }
            });
        */
        // updateSelectedBoardButton
        al = new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateSelectedBoardButton_actionPerformed(e);
            }
        };
        updateSelectedBoardButton.addActionListener(al);
        MIupdateSelectedBoard.addActionListener(al);

        // updateAllBoardsButton
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAllBoardsButton_actionPerformed(e);
            }
        };
        updateAllBoardsButton.addActionListener(al);
        MIupdateAllBoards.addActionListener(al);

        // Bclose
        al = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        };
        Bclose.addActionListener(al);

        //------------------------------------------------------------------------
        // Append objects
        //------------------------------------------------------------------------
        this.getContentPane().add(mainPanel, null); // add Main panel

        //mainPanel.add(splitPane, BorderLayout.CENTER);
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
        //boardlistPanel.add(addBoardButton, BorderLayout.NORTH);
        //boardlistPanel.add(boardlistScrollPane, BorderLayout.CENTER);
        //addBoardButton.setEnabled(false);

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
/*
    private void boardListModel_valueChanged(ListSelectionEvent e) {
    if (boardList.getSelectedIndex() == -1)
        addBoardButton.setEnabled(false);
    else
        addBoardButton.setEnabled(true);
    }
*/
    class TableDoubleClickMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {}
        public void mousePressed(MouseEvent event) {}
        public void mouseClicked(MouseEvent event) {
            if( event.getClickCount() == 2 )
            {
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
            if( e.isPopupTrigger() )
            {
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
/*
    private void addBoardButton_actionPerformed(ActionEvent e) {
    boards = TreeFun.getAllLeafs(frame1.tofTreeNode);
    if (boardList.getSelectedIndex() != -1) {
        int[] selectedIndices = boardList.getSelectedIndices();
        for (int i = 0; i < selectedIndices.length; i++) {
        String selectedBoard = (String)boardListModel.getElementAt(selectedIndices[i]);
        boolean addBoard = true;
        for (int j = 0; j < boards.size(); j++) {
            if (selectedBoard.equals((String)boards.elementAt(j)))
            addBoard = false;
        }
        if (addBoard) {
            frame1.tofTreeNode.add(new DefaultMutableTreeNode(selectedBoard));
            boards.add(selectedBoard);
        }
        }
        frame1.updateTree = true;
    }
    }
*/
    private void updateButton_actionPerformed(ActionEvent e)
    {
        Object[] row = new Object[5];
        int messageCount = 0;
        int fileCount = 0;
        int boardCount = 0;
        boards = parent.getTofTree().getAllBoards();
        TableFun.removeAllRows(boardTable);
        for( int i = 0; i < boards.size(); i++ )
        {
            boardCount ++;
            String boardName = mixed.makeFilename((String)boards.elementAt(i));
            row[0] = new PlainOrBoldString(boardName, false);
            row[1] = getState(boardName);

            fillInBoardCounts(boardName, row);
            //int mc = allMessageCount(boardName);

            messageCount += ((Integer)row[2]).intValue();
            //row[2] = new Integer( mc );
            //row[3] = new Integer(newMessageCount(boardName));
            //int fc = allFileCount(boardName);
            fileCount += ((Integer)row[4]).intValue();
            //row[4] = new Integer(fc);
            if( parent.isUpdating(boardName) == true )
            {
                // this board is updating right now
                PlainOrBoldString pobStr = (PlainOrBoldString)row[0];
                pobStr.setBold(true);

            }
            boardTableModel.addRow(row);
        }
        summaryLabel.setText(LangRes.getString("Boards: ") +
                             boardCount +
                             LangRes.getString("   Messages: ") +
                             messageCount +
                             LangRes.getString("   Files: ") +
                             fileCount);
        //updateBoardList();
        //requestNewBoards();
        updateSelectedBoardButton.setEnabled(false);
        MIupdateSelectedBoard.setEnabled(false);
    }

    // simple hack, but does the thing
    private void updateAllBoardsButton_actionPerformed(ActionEvent e)
    {
        boardTable.selectAll();
        updateSelectedBoardButton_actionPerformed(e);
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

                PlainOrBoldString selectedBoard = (PlainOrBoldString)boardTableModel.getValueAt(rowIx, 0);

                // check if board is already in list of updating boards
                if( parent.isUpdating( selectedBoard.toString() ) == true )
                {
                    // paranoia: update not needed, but ensure that this updated board is drawn in bold
                    selectedBoard.setBold(true);
                    boardTableModel.fireTableCellUpdated(rowIx, 0);
                }
                if( selectedBoard.isBold() == false && // is already updating?
                    parent.doUpdate(selectedBoard.toString()) == true ) // is update allowed for this board?
                {
                    parent.updateBoard(selectedBoard.toString());
                    selectedBoard.setBold(true);
                    boardTableModel.fireTableCellUpdated(rowIx, 0);
                }
            }
            boardTable.clearSelection();
        }
    }
/*
    private void updateBoardList() {
    boardListModel.clear();
    addBoardButton.setEnabled(false);

    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));

    for (int j = 0; j < frame1.frostSettings.getIntValue("maxMessageDisplay"); j++) {
        String date = cal.get(Calendar.YEAR) + ".";
        date += cal.get(Calendar.MONTH) + 1 + ".";
        date += cal.get(Calendar.DATE);
        File today = new File(frame1.keypool +
                  "_boardlist" +
                  System.getProperty("file.separator") +
                  date);
//      System.out.println("Reading boardlist from " + today.getPath());
        if (today.isDirectory()) {
        File[] files = today.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
            if (files[i].length() > 0 && files[i].getName().endsWith(".txt")) {
                String boardName = (FileAccess.readFile(files[i])).trim();

                boolean exists = false;
                for (int k = 0; k < boardListModel.size(); k++) {
                if (boardName.equals((String)boardListModel.elementAt(k)))
                    exists = true;
                }

                if (!boardName.equals("Empty") && !exists && boardName.length() < 65)
                boardListModel.addElement(mixed.makeFilename(boardName));

            }
            }
        }
        }
        cal.add(Calendar.DATE, -1); // Yesterday
    }
    }

    private void requestNewBoards() {
    boolean request = true;
    for (int i = 0; i < frame1.activeTofThreads.size(); i++) {
        String updatedBoard = (String)frame1.activeTofThreads.elementAt(i);
        if (updatedBoard.equals("_boardlist"))
        request = false;
    }
    if (request) {
        String[] args = new String[3];
        args[0] = "_boardlist";
        args[1] = frame1.frostSettings.getValue("tofDownloadHtl");
        args[2] = frame1.keypool;
        MessageDownloadThread tofd = new MessageDownloadThread(true, args,this);
        tofd.start();
    }
    }
*/
    protected void closeDialog()
    {
        dispose();
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            closeDialog();
        }
        super.processWindowEvent(e);
    }

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

    class BoardInfoTableCellRenderer extends DefaultTableCellRenderer
    {
        Font boldFont;
        Font origFont;
        public BoardInfoTableCellRenderer()
        {
            super();
            origFont = this.getFont();
            boldFont = origFont.deriveFont( Font.BOLD );
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

            if( value instanceof PlainOrBoldString )
            {
                if( ((PlainOrBoldString)value).isBold() )
                {
                    setFont( boldFont );
                }
            }
            return this;
        }
    }

    /**
     * Gets number of new+all messages and files of a board
     * @param board name of the board
     * @return Integer value
     */
    public Object[] fillInBoardCounts(String board, Object[] row)
    {
        board = board.toLowerCase();
        int countNewMessages = 0;
        int countAllMessages = 0;
        int countFiles = 0;

        String date = DateFun.getDate();
        File boardDir = new File(frame1.keypool + board);

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
                    else if( entries[i].getName().endsWith(".exc") )
                    {
                        countFiles += getLineCount(entries[i]);
                    }
                }
            }
        }
        countFiles /= 4;
        row[2] = new Integer(countAllMessages);
        row[3] = new Integer(countNewMessages);
        row[4] = new Integer(countFiles);

        return row;
    }

    /**
     * Gets state of a board
     * @param board name of the board
     * @return String with state value of the board
     */
    public String getState(String board)
    {
        board = board.toLowerCase();
        String val = new StringBuffer().append(frame1.keypool).append(board).append(".key").toString();
        String state = SettingsFun.getValue(val, "state");
        if( state.length()==0 )
            return "publicBoard";
        else
            return state;
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
        try {
            f = new BufferedReader(new FileReader(file));
            while( (f.readLine()) != null )
            {
                count++;
            }
            f.close();
        }
        catch( IOException e )
        {
            System.out.println("getLineCount() - Read Error: " + file);
        }
        return count;
    }
}
