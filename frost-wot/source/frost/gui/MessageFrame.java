/*
MessageFrame.java / Frost
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
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableModel;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;
import frost.messages.*;

public class MessageFrame extends JFrame
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------
    FrostBoardObject board;
    String from;
    String subject;
    String text;
    String lastUsedDirectory;
    String keypool;
    String fileSeparator = System.getProperty("file.separator");
    boolean state;
    Frame parentFrame;
    SettingsClass frostSettings;
    
    MFAttachedBoardsTable attBoardsTable;
    MFAttachedFilesTable attFilesTable;
    MFAttachedBoardsTableModel attBoardsTableModel;
    MFAttachedFilesTableModel attFilesTableModel;

    // scrollers needed globally because we need to add them dynamically to panel    
    JScrollPane attFilesScroller;
    JScrollPane attBoardsScroller;
    
    JPanel attachmentsPanel;
    
    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
    JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
    JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
    JButton BattachBoard= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));

    JCheckBox sign = new JCheckBox("Sign");
    JCheckBox addAttachedFilesToUploadTable = new JCheckBox("Indexed attachments");

    JTextField TFboard = new JTextField(); // Board (To)
    JTextField TFfrom = new JTextField(); // From
    JTextField TFsubject = new JTextField(); // Subject

    JTextArea TAcontent = new JTextArea(); // Text

    private void Init() throws Exception {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

        this.setIconImage(Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/data/newmessage.gif")));
        this.setTitle(LangRes.getString("Create message"));
        this.setResizable(true);
        
        attBoardsTableModel = new MFAttachedBoardsTableModel();
        attBoardsTable = new MFAttachedBoardsTable(attBoardsTableModel);
        attBoardsScroller = new JScrollPane( attBoardsTable );
        
        attFilesTableModel = new MFAttachedFilesTableModel();
        attFilesTable = new MFAttachedFilesTable(attFilesTableModel);
        attFilesScroller = new JScrollPane( attFilesTable );

        configureButton(Bsend, "Send message", "/data/send_rollover.gif");
        configureButton(Bcancel, "Cancel", "/data/remove_rollover.gif");
        configureButton(BattachFile, "Add attachment(s)", "/data/attachment_rollover.gif");
        configureButton(BattachBoard, "Add Board(s)", "/data/attachmentBoard_rollover.gif");

        TFboard.setEnabled(false);
        TFboard.setText(board.toString());
        TFfrom.setText(from);

        TFsubject.setText(subject);
        TAcontent.setLineWrap(true);
        TAcontent.setWrapStyleWord(true);
        TAcontent.setText(text);
        // check if last msg was signed and set it to remembered state
        if( from.equals(frame1.getMyId().getUniqueName()) )
        {
            TFfrom.setEnabled(false);
            sign.setSelected(true);
        }
        
        addAttachedFilesToUploadTable.setSelected(false);
        addAttachedFilesToUploadTable.setToolTipText("Should file attachments be added to upload table?");

        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------
        Bsend.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           send_actionPerformed(e);
                                       } });
        Bcancel.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           cancel_actionPerformed(e);
                                       } });
        BattachFile.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           attachFile_actionPerformed(e);
                                       } });
        BattachBoard.addActionListener(new java.awt.event.ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                             attachBoards_actionPerformed(e);
                                         } });
        sign.addActionListener(new java.awt.event.ActionListener() {
                                   public void actionPerformed(ActionEvent e) {
                                       if( sign.isSelected() )
                                       {
                                           TFfrom.setText(frame1.getMyId().getUniqueName());
                                           TFfrom.setEnabled(false);
                                       }
                                       else
                                       {
                                           TFfrom.setText("Anonymous");
                                           TFfrom.setEnabled(true);
                                       }
                                   } });
        //------------------------------------------------------------------------
        // Append objects
        //------------------------------------------------------------------------
        JPanel panelMain = new JPanel(new BorderLayout()); // Main Panel
        JPanel panelTextfields = new JPanel(new BorderLayout()); // Textfields
        JPanel panelToolbar = new JPanel(new BorderLayout()); // Toolbar / Textfields
        JPanel panelLabels = new JPanel(new BorderLayout()); // Labels
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        
        this.attachmentsPanel = new JPanel(new BorderLayout());

        JLabel Lboard = new JLabel(LangRes.getString("Board: "));
        JLabel Lfrom = new JLabel(LangRes.getString("From: "));
        JLabel Lsubject = new JLabel(LangRes.getString("Subject: "));

        JScrollPane textScroller = new JScrollPane(TAcontent); // Textscrollpane
        textScroller.setPreferredSize(new Dimension(600, 400));

        panelLabels.add(Lboard, BorderLayout.NORTH);
        panelLabels.add(Lfrom, BorderLayout.CENTER);
        panelLabels.add(Lsubject, BorderLayout.SOUTH);
        
        panelTextfields.add(TFboard, BorderLayout.NORTH);
        panelTextfields.add(TFfrom, BorderLayout.CENTER);
        panelTextfields.add(TFsubject, BorderLayout.SOUTH);

        panelButtons.add(Bsend);
        panelButtons.add(Bcancel); 
        panelButtons.add(BattachFile); 
        panelButtons.add(BattachBoard); 
        panelButtons.add(sign);
        panelButtons.add(this.addAttachedFilesToUploadTable);

        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(panelLabels, BorderLayout.WEST);
        dummyPanel.add(panelTextfields, BorderLayout.CENTER);
        
        panelToolbar.add(panelButtons, BorderLayout.NORTH);
        panelToolbar.add(dummyPanel, BorderLayout.SOUTH);

        panelMain.add(panelToolbar, BorderLayout.NORTH);
        panelMain.add(textScroller, BorderLayout.CENTER);
        panelMain.add(this.attachmentsPanel, BorderLayout.SOUTH);

        this.getContentPane().add(panelMain, null);
    }

    /**jButton1 Action Listener (Send)*/
    private void send_actionPerformed(ActionEvent e)
    {
        from = TFfrom.getText().trim();
        TFfrom.setText(from);
        subject = TFsubject.getText().trim();
        TFsubject.setText(subject); // if a pbl occurs show the subject we checked
        text = TAcontent.getText().trim();

        boolean quit = true;

        if( subject.equals("No subject") )
        {
            int n = JOptionPane.showConfirmDialog( this,
                                                   LangRes.getString("Do you want to enter a subject?"),
                                                   LangRes.getString("No subject specified!"),
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE);
            if( n == JOptionPane.YES_OPTION )
            {
                return;
            }
        }
        
        if( subject.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
                                           LangRes.getString("You must enter a subject!"),
                                           LangRes.getString("No subject specified!"),
                                           JOptionPane.ERROR);
            return;                               
        }
        if( from.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
                                           LangRes.getString("You must enter a sender name!"),
                                           LangRes.getString("No 'From' specified!"),
                                           JOptionPane.ERROR);
            return;                               
        }


        // for convinience set last used user (maybe obsolete now)
        frostSettings.setValue("userName", from);
        
        // create new MessageObject to upload
        MessageObject mo = new MessageObject();
        mo.setBoard(board.getBoardName());
        mo.setFrom(from);
        mo.setSubject(subject);
        mo.setContent(text);
        if( sign.isSelected() )
        {
            mo.setPublicKey(frame1.getMyId().getKey());
        }
        // MessageUploadThread will set date + time !
        
        // attach all files and boards the user chosed
        for(int x=0; x < attFilesTableModel.getRowCount(); x++)
        {
            MFAttachedFile af = (MFAttachedFile)attFilesTableModel.getRow(x);
            File aChosedFile = af.getFile();
            FrostBoardObject boardObj = null;
            if( addAttachedFilesToUploadTable.isSelected() )
            {
                boardObj = this.board;
            }
            SharedFileObject sfo = new SharedFileObject(aChosedFile, boardObj);
            FileAttachment fa = new FileAttachment(sfo);
            mo.getAttachmentList().add(fa);
        }
        for(int x=0; x < attBoardsTableModel.getRowCount(); x++)
        {
            MFAttachedBoard ab = (MFAttachedBoard)attBoardsTableModel.getRow(x);
            FrostBoardObject aChosedBoard = ab.getBoardObject();
            BoardAttachment ba = new BoardAttachment(aChosedBoard);
            mo.getAttachmentList().add(ba);
        }

        // start upload thread which also saves the file, uploads attachments+signs if choosed
        frame1.getInstance().getRunningBoardUpdateThreads().startMessageUpload(
                                              board,
                                              mo,
                                              null);

        frostSettings.setValue("lastUsedDirectory", lastUsedDirectory);
        frostSettings.writeSettingsFile();
        state = true; // exit state
        dispose();
    }

    /**jButton2 Action Listener (Cancel)*/
    private void cancel_actionPerformed(ActionEvent e)
    {
        state = false;
        dispose();
    }

    /**jButton3 Action Listener (Add attachment(s))*/
    private void attachFile_actionPerformed(ActionEvent e)
    {
        String lineSeparator = System.getProperty("line.separator");
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(LangRes.getString("Choose file(s) / directory(s) to attach"));
        fc.setFileHidingEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(MessageFrame.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File[] file = fc.getSelectedFiles();
            for( int i = 0; i < file.length; i++ )
            {
                // for convinience remember last used directory
                lastUsedDirectory = file[i].getPath();
                
                if( file[i].isFile() )
                {
                    MFAttachedFile af = new MFAttachedFile( file[i] );
                    attFilesTableModel.addRow( af );
                }
                if( file[i].isDirectory() )
                {
                    File[] entries = file[i].listFiles();
                    for( int j = 0; j < entries.length; j++ )
                    {
                        if( entries[j].isFile() )
                        {
                            MFAttachedFile af = new MFAttachedFile( file[i] );
                            attFilesTableModel.addRow( af );
                        }
                    }
                }
            }
        }
        else
        {
            System.out.println("Open command cancelled by user.");
        }
        
        if( attFilesTableModel.getRowCount() > 0 )
        {
            // show the table if not already shown
            showAttachedFilesTable(true);
        }
    }

    private void attachBoards_actionPerformed(ActionEvent e)
    {
        Vector allBoards = frame1.getInstance().getTofTree().getAllBoards();
        if( allBoards.size() == 0 )
            return;
        Collections.sort(allBoards);

        AttachBoardsChooser chooser = new AttachBoardsChooser(allBoards);
        chooser.setLocationRelativeTo( this );
        Vector chosedBoards = chooser.runDialog();
        if( chosedBoards == null || chosedBoards.size() == 0 ) // nothing chosed or cancelled
        {
            return;
        }

        for( int i = 0; i < chosedBoards.size(); i++ )
        {
            FrostBoardObject board = (FrostBoardObject)chosedBoards.get(i);

            String privKey = board.getPrivateKey();

            if( privKey != null )
            {
                int answer = JOptionPane.showConfirmDialog(this,
                           "You have the private key to " +
                               board.toString() +
                               ".  Are you sure you want it attached?\n "+
                               "If you choose NO, only the public key will be attached.",
                           "Include private board key?",
                           JOptionPane.YES_NO_OPTION);
                if( answer == JOptionPane.NO_OPTION )
                {
                    privKey = null; // dont provide privkey
                }
            }
            // build a new board because maybe privKey should'nt be uploaded
            FrostBoardObject aNewBoard = new FrostBoardObject(board.getBoardName(),
                board.getPublicKey(), 
                privKey);            
            MFAttachedBoard ab = new MFAttachedBoard( aNewBoard );
            attBoardsTableModel.addRow( ab );
        }
        
        if( attBoardsTableModel.getRowCount() > 0 )
        {
            // show the table if not already shown
            showAttachedBoardsTable(true);
        }
    }

    /**
     * Configures a button to be a default icon button
     * @param button The new icon button
     * @param toolTipText Is displayed when the mousepointer is some seconds over a button
     * @param rolloverIcon Displayed when mouse is over button
     */
    protected void configureButton(JButton button, String toolTipText, String rolloverIcon)
    {
        button.setToolTipText(LangRes.getString(toolTipText));
        button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
    
    protected void updateAttachmentsPanel()
    {
        this.attachmentsPanel.invalidate();
        this.getContentPane().validate();
    }
    
    protected void showAttachedBoardsTable(boolean show)
    {
        if( show )
        {
            // check if not already contained in panel
            int comps = attachmentsPanel.getComponentCount();
            for( int x=0; x<comps; x++ )
            {
                Component c = attachmentsPanel.getComponent(x);
                if( c == this.attBoardsScroller )
                {
                    return;
                }
            }
            this.attachmentsPanel.add( this.attBoardsScroller, BorderLayout.NORTH);
        }
        else
        {
            this.attachmentsPanel.remove( this.attBoardsScroller );
        }
        updateAttachmentsPanel();
    }
    
    protected void showAttachedFilesTable(boolean show)
    {
        if( show )
        {
            // check if not already contained in panel
            int comps = attachmentsPanel.getComponentCount();
            for( int x=0; x<comps; x++ )
            {
                Component c = attachmentsPanel.getComponent(x);
                if( c == this.attFilesScroller )
                {
                    return;
                }
            }
            this.attachmentsPanel.add( this.attFilesScroller, BorderLayout.SOUTH);
        }
        else
        {
            this.attachmentsPanel.remove( this.attFilesScroller );
        }
        updateAttachmentsPanel();
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            dispose();
        }
        super.processWindowEvent(e);
    }

    /**Constructor*/
    public MessageFrame(FrostBoardObject board, String from, String subject, String text,
                        SettingsClass config, Frame parentFrame)
    {
        super();
        this.parentFrame = parentFrame;
        this.board = board;
        this.from=from;
        this.subject=subject;
        this.text=text;
        this.lastUsedDirectory = config.getValue("lastUsedDirectory");
        this.state = false;
        this.keypool = config.getValue("keypool.dir");
        this.frostSettings = config;

        String date = DateFun.getExtendedDate() + " - " + DateFun.getFullExtendedTime()+"GMT";
        String lineSeparator = System.getProperty("line.separator");
        if( this.text.length() > 0 )
        {
            // on reply to a message
            this.text += new StringBuffer().append(lineSeparator)
                                           .append("----- ")
                                           .append(this.from)
                                           .append(" ----- ")
                                           .append(date)
                                           .append(" -----")
                                           .append(lineSeparator)
                                           .append(lineSeparator).toString();
        }

        File signature = new File("signature.txt");
        if( signature.isFile() )
        {
            this.text += FileAccess.readFile("signature.txt");
        }

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            Init();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        Font tofFont = new Font("Monospaced", Font.PLAIN, (int)frame1.frostSettings.getFloatValue("tofFontSize") );
        TAcontent.setFont( tofFont );

        pack();
        setLocationRelativeTo(parentFrame);
    }

/*************************************************
 ************************************************* 
 **  INTERNAL CLASSES  ***************************
 ************************************************* 
 *************************************************/

    private class MFAttachedBoard implements TableMember
    {
        FrostBoardObject aBoard;
        public MFAttachedBoard(FrostBoardObject ab)
        {
            aBoard = ab;
        }
        public FrostBoardObject getBoardObject()
        {
            return aBoard;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }

        public Object getValueAt(int column)
        {
            switch(column)
            {
                case 0: return aBoard.getBoardName();
                case 1: return (aBoard.getPublicKey()==null)?"N/A":aBoard.getPublicKey();
                case 2: return (aBoard.getPrivateKey()==null)?"N/A":aBoard.getPrivateKey();
            }
            return "*ERR*";
        }
    }
    
    private class MFAttachedFile implements TableMember
    {
        File aFile;
        public MFAttachedFile(File af)
        {
            aFile = af;
        }
        public File getFile()
        {
            return aFile;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public Object getValueAt(int column)
        {
            switch(column)
            {
                case 0: return aFile.getName();
                case 1: return ""+aFile.length();
            }
            return "*ERR*";
        }
    }

    private class MFAttachedBoardsTable extends SortedTable
    {
        public MFAttachedBoardsTable(TableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    private class MFAttachedFilesTable extends SortedTable
    {
        public MFAttachedFilesTable(TableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    
    private class MFAttachedBoardsTableModel extends SortedTableModel
    {
        protected final String columnNames[] = {
            "Boardname",
            "public key",
            "Private key"
        };
        protected final Class columnClasses[] = {
            String.class, 
            String.class,
            String.class
        };

        public MFAttachedBoardsTableModel()
        {
            super();
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }
    
    private class MFAttachedFilesTableModel extends SortedTableModel
    {
        protected final String columnNames[] = {
            "Filename",
            "Size"
        };
        protected final Class columnClasses[] = {
            String.class,
            String.class
        };

        public MFAttachedFilesTableModel()
        {
            super();
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }

    private class AttachBoardsChooser extends JDialog
    {
        Vector boards;
        JButton Bok;
        JButton Bcancel;
        JList Lboards;
        boolean okPressed = false;

        public AttachBoardsChooser(Vector boards)
        {
            super();
            setTitle("Choose boards to attach");
            setModal(true);
            this.boards = boards;
            initGui();
        }
        private void initGui()
        {
            Bok = new JButton("OK");
            Bok.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       okPressed = true;
                       hide();
                   } });
            Bcancel = new JButton("Cancel");
            Bcancel.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       okPressed = false;
                       hide();
                   } });
            JPanel buttonsPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 8) );
            buttonsPanel.add( Bok );
            buttonsPanel.add( Bcancel );

            Lboards = new JList( boards );
            Lboards.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane listScroller = new JScrollPane( Lboards );
            listScroller.setBorder( new CompoundBorder( new EmptyBorder(5,5,5,5),
                                                        new CompoundBorder( new EtchedBorder(),
                                                                            new EmptyBorder(5,5,5,5) )
                                                      ) );
            getContentPane().add(listScroller, BorderLayout.CENTER);
            getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
            setSize(300, 400);
        }
        public Vector runDialog()
        {
            show();
            if( okPressed == false )
                return null;

            Object[] sels = Lboards.getSelectedValues();
            Vector chosed = new Vector( Arrays.asList( sels ) );
            return chosed;
        }
    }
    
    class BuddyComparator implements Comparator
    {
        // compare buddies in lowercase
        public int compare(Object o1, Object o2)
        {
            String s1 = (String)o1;
            String s2 = (String)o2;
            return s1.toLowerCase().compareTo( s2.toLowerCase() );
        }
    }
}

