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

import frost.*;
import frost.gui.objects.FrostBoardObject;
import frost.messages.MessageObject;

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
//    String recipient;
    boolean state;
//    boolean encrypt;
    Frame parentFrame;
    SettingsClass frostSettings;
    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JPanel jPanel1 = new JPanel(new BorderLayout()); // Main Panel
    JPanel jPanel2 = new JPanel(new BorderLayout()); // Textfields
    JPanel jPanel3 = new JPanel(new BorderLayout()); // Toolbar / Textfields(jPanel2)
    JPanel jPanel4 = new JPanel(new BorderLayout()); // Labels
    JPanel jPanel5 = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
    JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
    JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
    JButton BattachBoard= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));

    JCheckBox sign = new JCheckBox("Sign");
//    JCheckBox encryptBox = new JCheckBox("Encrypt for");
//    JComboBox buddies;

    JTextField TFboard = new JTextField(); // Board (To)
    JTextField TFfrom = new JTextField(); // From
    JTextField TFsubject = new JTextField(); // Subject

    JTextArea TAcontent = new JTextArea(); // Text

    JScrollPane jScrollPane1 = new JScrollPane(); // Textscrollpane

    JLabel Lboard = new JLabel(LangRes.getString("Board: ")); // Board
    JLabel Lfrom = new JLabel(LangRes.getString("From: ")); // From
    JLabel Lsubject = new JLabel(LangRes.getString("Subject: ")); // Subject

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

    private void Init() throws Exception {
        //------------------------------------------------------------------------
        // Configure objects
        //------------------------------------------------------------------------

        this.setIconImage(Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/data/newmessage.gif")));
        this.setTitle(LangRes.getString("Create message"));
        this.setResizable(true);

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
        if( from.compareTo(frame1.getMyId().getName()) == 0 )
        {
            TFfrom.setEnabled(false);
            sign.setSelected(true);
        }

        jScrollPane1.setPreferredSize(new Dimension(600, 400));
        //------------------------------------------------------------------------
        // Actionlistener
        //------------------------------------------------------------------------

        // Button 1 (Send)
        Bsend.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           send_actionPerformed(e);
                                       } });
        // Button 2 (Cancel)
        Bcancel.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           cancel_actionPerformed(e);
                                       } });
        // Button 3 (Add attachment(s))
        BattachFile.addActionListener(new java.awt.event.ActionListener() {
                                       public void actionPerformed(ActionEvent e) {
                                           attachFile_actionPerformed(e);
                                       } });
        // Button 4 (Add attachment(s))
        BattachBoard.addActionListener(new java.awt.event.ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                             attachBoards_actionPerformed(e);
                                         } });
        //sign checkbox
        sign.addActionListener(new java.awt.event.ActionListener() {
                                   public void actionPerformed(ActionEvent e) {
                                       if( sign.isSelected() )
                                       {
                                           TFfrom.setText(frame1.getMyId().getName());
                                           TFfrom.setEnabled(false);
                                       }
                                       else
                                       {
                                           TFfrom.setText("Anonymous");
                                           TFfrom.setEnabled(true);
                                           TFsubject.setEnabled(true);
                                       }
                                   } });
        //------------------------------------------------------------------------
        // Append objects
        //------------------------------------------------------------------------
        this.getContentPane().add(jPanel1, null); // add Main panel

        jPanel1.add(jPanel3, BorderLayout.NORTH); // Buttons
        jPanel1.add(jScrollPane1, BorderLayout.CENTER); // Textfields

        jPanel2.add(TFboard, BorderLayout.NORTH); // Board (to)
        jPanel2.add(TFfrom, BorderLayout.CENTER); // From
        jPanel2.add(TFsubject, BorderLayout.SOUTH); // Subject

        jPanel3.add(buttonPanel, BorderLayout.NORTH);
        jPanel3.add(jPanel5, BorderLayout.SOUTH);

        jPanel4.add(Lboard, BorderLayout.NORTH); // Board
        jPanel4.add(Lfrom, BorderLayout.CENTER); // From
        jPanel4.add(Lsubject, BorderLayout.SOUTH); // Subject

        jPanel5.add(jPanel4, BorderLayout.WEST);
        jPanel5.add(jPanel2, BorderLayout.CENTER);

        jScrollPane1.getViewport().add(TAcontent, null); // Text

        buttonPanel.add(Bsend); // Send
        buttonPanel.add(Bcancel); // Cancel
        buttonPanel.add(BattachFile); // Add attachment(s)
        buttonPanel.add(BattachBoard); //Add boards(s)
        buttonPanel.add(sign);
    }

    /**jButton1 Action Listener (Send)*/
    private void send_actionPerformed(ActionEvent e)
    {
        from = TFfrom.getText();
        subject = TFsubject.getText();
        text = TAcontent.getText();

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

        // message is ready to send, exit dialog
        frostSettings.setValue("userName", from);
        
        // create new MessageObject to upload
        MessageObject mo = new MessageObject();
        mo.setBoard(board.getBoardName());
        mo.setFrom(from);
        mo.setSubject(subject);
        mo.setContent(text);
        // MessageUploadThread will set date + time !

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
                lastUsedDirectory = file[i].getPath();
                if( file[i].isFile() )
                {
                    TAcontent.append("<attach>" +
                                      file[i].getPath() +
                                      "</attach>" +
                                      lineSeparator);
                }
                if( file[i].isDirectory() )
                {
                    File[] entries = file[i].listFiles();
                    for( int j = 0; j < entries.length; j++ )
                    {
                        if( entries[j].isFile() )
                        {
                            TAcontent.append("<attach>" +
                                              entries[j].getPath() +
                                              "</attach>" +
                                              lineSeparator);

                        }
                    }
                }
            }
        }
        else
        {
            System.out.println("Open command cancelled by user.");
        }
    }

    private void attachBoards_actionPerformed(ActionEvent e)
    {
        String lineSeparator = System.getProperty("line.separator");
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

            String pubKey = board.getPublicKey();
            String privKey = board.getPrivateKey();
            if( pubKey == null )
                pubKey="N/A";

            if( privKey == null )
                privKey="N/A";
            else
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
                    privKey="N/A";
                }
            }
            TAcontent.append("<board>" + board.toString() +
                              " * " + pubKey +
                              " * " + privKey +
                              "</board>" + lineSeparator);
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
}

