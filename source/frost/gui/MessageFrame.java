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
import javax.swing.*;
import java.util.*;
import java.io.*;

import frost.*;
import frost.FcpTools.*;

public class MessageFrame extends JFrame {

    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------
    String board;
    String from;
    String subject;
    String text;
    String lastUsedDirectory;
    String keypool;
    String fileSeparator = System.getProperty("file.separator");
    String recipient;
    boolean state;
    boolean secure;
    boolean encrypt;
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

    JButton jButton1 = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
    JButton jButton2 = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
    JButton jButton3 = new JButton(new ImageIcon(this.getClass().getResource("/data/save.gif")));
    JButton uploadBoardsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));

    JCheckBox sign = new JCheckBox("Sign");
    JCheckBox encryptBox = new JCheckBox("Encrypt for");
    JComboBox buddies;

    JTextField jTextField1 = new JTextField(); // Board (To)
    JTextField jTextField2 = new JTextField(); // From
    JTextField jTextField3 = new JTextField(); // Subject

    JTextArea jTextArea1 = new JTextArea(); // Text

    JScrollPane jScrollPane1 = new JScrollPane(); // Textscrollpane

    JLabel jLabel1 = new JLabel(LangRes.getString("Board: ")); // Board
    JLabel jLabel2 = new JLabel(LangRes.getString("From: ")); // From
    JLabel jLabel3 = new JLabel(LangRes.getString("Subject: ")); // Subject

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

    encrypt=false;

    if( frame1.getFriends() != null )
    {
        String[] buddyNames = new String[frame1.getFriends().size()];
        Vector budList = new Vector( frame1.getFriends().keySet() );
        Collections.sort( budList, new BuddyComparator() );
        buddies = new JComboBox(budList);
        recipient = (String)budList.get(0);
        buddies.setSelectedItem(recipient);
    }
    else
    {
        buddies = new JComboBox();
    }

    configureButton(jButton1, "Send message", "/data/send_rollover.gif");
    configureButton(jButton2, "Cancel", "/data/remove_rollover.gif");
    configureButton(jButton3, "Add attachment(s)", "/data/save_rollover.gif");
    configureButton(uploadBoardsButton, "Add Board(s)", "/data/attachmentBoard_rollover.gif");

    String displayBoard = board;
    secure = false;
    if (board.startsWith("<html>") && board.length() > 41) {
        displayBoard = board.substring(27, board.length() - 14);
        secure = true;
        jTextField1.setEnabled(false);
    }

    jTextField1.setText(displayBoard);
    jTextField2.setText(from);

    encryptBox.setSelected(false);
    encryptBox.setEnabled(false);
    buddies.setEnabled(false);
    jTextField3.setText(subject);
    jTextArea1.setLineWrap(true);
    jTextArea1.setWrapStyleWord(true);
    jTextArea1.setText(text);
    if (from.compareTo(frame1.getMyId().getName()) == 0) {
        jTextField2.setEnabled(false);
        sign.setSelected(true);
        encryptBox.setEnabled(true);
    }

    jScrollPane1.setPreferredSize(new Dimension(600, 400));
    //------------------------------------------------------------------------
    // Actionlistener
    //------------------------------------------------------------------------

    // Button 1 (Send)
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            jButton1_actionPerformed(e);
        }
        });

    // Button 2 (Cancel)
    jButton2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            jButton2_actionPerformed(e);
        }
        });

    // Button 3 (Add attachment(s))
    jButton3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            jButton3_actionPerformed(e);
        }
        });

     // Button 4 (Add attachment(s))
    uploadBoardsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            uploadBoards_actionPerformed(e);
        }
        });

    //sign checkbox
     sign.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (sign.isSelected()) {
            jTextField2.setText(frame1.getMyId().getName());
            jTextField2.setEnabled(false);
            encryptBox.setEnabled(true);
            }
            else {
            jTextField2.setText("Anonymous");
            jTextField2.setEnabled(true);
            jTextField3.setEnabled(true);
            encryptBox.setSelected(false);
            encryptBox.setEnabled(false);
            buddies.setEnabled(false);
            }
        }
        });

     //encrypt checkbox
     encryptBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (encryptBox.isSelected()) {
                buddies.setEnabled(true);
            jTextField3.setEnabled(false);
            encrypt=true;
            }
            else {
            buddies.setEnabled(false);
            jTextField3.setEnabled(true);
            encrypt=false;
            }
        }
        });

     //combo box
     buddies.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox)e.getSource();
                recipient = (String)cb.getSelectedItem();
        }
      });

    //------------------------------------------------------------------------
    // Append objects
    //------------------------------------------------------------------------
    this.getContentPane().add(jPanel1, null); // add Main panel

    jPanel1.add(jPanel3, BorderLayout.NORTH); // Buttons
    jPanel1.add(jScrollPane1, BorderLayout.CENTER); // Textfields

    jPanel2.add(jTextField1, BorderLayout.NORTH); // Board (to)
    jPanel2.add(jTextField2, BorderLayout.CENTER); // From
    jPanel2.add(jTextField3, BorderLayout.SOUTH); // Subject

    jPanel3.add(buttonPanel, BorderLayout.NORTH);
    jPanel3.add(jPanel5, BorderLayout.SOUTH);

    jPanel4.add(jLabel1, BorderLayout.NORTH); // Board
    jPanel4.add(jLabel2, BorderLayout.CENTER); // From
    jPanel4.add(jLabel3, BorderLayout.SOUTH); // Subject

    jPanel5.add(jPanel4, BorderLayout.WEST);
    jPanel5.add(jPanel2, BorderLayout.CENTER);

    jScrollPane1.getViewport().add(jTextArea1, null); // Text

    buttonPanel.add(jButton1); // Send
    buttonPanel.add(jButton2); // Cancel
    buttonPanel.add(jButton3); // Add attachment(s)
    buttonPanel.add(uploadBoardsButton); //Add boards(s)
    buttonPanel.add(sign);
    buttonPanel.add(encryptBox);
    buttonPanel.add(buddies);
    }

    /**jButton1 Action Listener (Send)*/
    private void jButton1_actionPerformed(ActionEvent e) {
    state = true;
    if (!secure)
        board = jTextField1.getText();
    from = jTextField2.getText();
    subject = jTextField3.getText();
    text = jTextArea1.getText();

    boolean quit = true;

    if (subject.equals("No subject") && !encrypt) {
        Object[] options = {LangRes.getString("Yes"), LangRes.getString("No")};
        int n = JOptionPane.showOptionDialog(this,
                         LangRes.getString("Do you want to enter a subject?"),
                         LangRes.getString("No subject specified!"),
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         options,
                         options[1]);
        if (n == 1)
        quit = true;
        else
        quit = false;
    }

    if (quit) {
        if (state) {
        String[] par = new String[10];
        par[0] = mixed.makeFilename(board);
        par[1] = from;
        par[2] = subject;
        par[3] = text;
        par[4] = frostSettings.getValue("tofUploadHtl");
        par[5] = keypool;
        par[6] = frostSettings.getValue("tofDownloadHtl");
        par[7] = "";
        par[8] = "";
        if (encrypt && recipient.compareTo(frame1.getMyId().getName()) != 0)
            par[9] = recipient;
        else par[9] = "";
        frostSettings.setValue("userName", par[1]);
        MessageUploadThread messageUploadThread = new MessageUploadThread(par, parentFrame);
        messageUploadThread.start();
        }
        frostSettings.setValue("lastUsedDirectory", lastUsedDirectory);
        frostSettings.writeSettingsFile();
        dispose();
    }
    }

    /**jButton2 Action Listener (Cancel)*/
    private void jButton2_actionPerformed(ActionEvent e) {
    state = false;
    dispose();
    }

    /**jButton3 Action Listener (Add attachment(s))*/
    private void jButton3_actionPerformed(ActionEvent e) {
    String lineSeparator = System.getProperty("line.separator");
    final JFileChooser fc = new JFileChooser(lastUsedDirectory);
    fc.setDialogTitle(LangRes.getString("Choose file(s) / directory(s) to attach"));
    fc.setFileHidingEnabled(false);
    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fc.setMultiSelectionEnabled(true);

    int returnVal = fc.showOpenDialog(MessageFrame.this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] file = fc.getSelectedFiles();
        for (int i = 0; i < file.length; i++) {
            lastUsedDirectory = file[i].getPath();
        if (file[i].isFile()) {
            jTextArea1.append("<attach>" +
                      file[i].getPath() +
                      "</attach>" +
                      lineSeparator);
        }
        if (file[i].isDirectory()) {
            Vector entries = FileAccess.getAllEntries(file[i], "");
            for (int j = 0; j < entries.size(); j++) {
            if (((File)entries.elementAt(j)).isFile())
                jTextArea1.append("<attach>" +
                          ((File)entries.elementAt(j)).getPath()  +
                          "</attach>" +
                          lineSeparator);
            }
        }
        }
    }
    else {
        System.out.println("Open command cancelled by user.");
    }
   }

   private void uploadBoards_actionPerformed(ActionEvent e) {
    String lineSeparator = System.getProperty("line.separator");
    final JFileChooser fc = new JFileChooser("keypool");
    fc.setDialogTitle("choose board(s) to attach");
    fc.setFileHidingEnabled(true);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setMultiSelectionEnabled(true);
    //fc.setControlButtonsAreShown(false);
    fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
    fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public String getDescription() {return ".key";}
            public boolean accept(File f) {
                return f.getPath().endsWith(".key");
            }
        });
    int returnVal = fc.showOpenDialog(MessageFrame.this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] file = fc.getSelectedFiles();
        for (int i = 0; i < file.length; i++)
        if (file[i].isFile() && file[i].getPath().endsWith(".key")) {
            String content =FileAccess.read(file[i].getPath());
            String pubKey = content.substring(content.indexOf("publicKey=")+10,content.indexOf("state")).trim();
            String privKey = content.substring(content.indexOf("privateKey=")+11,content.indexOf("publicKey=")).trim();
            String bname = file[i].getName().substring(0,file[i].getName().indexOf(".key")).trim();
            if (!privKey.startsWith("SSK@")) privKey="N/A";
            else if (JOptionPane.showConfirmDialog(this,"You have the private key to " + bname +
                                ".  Are you sure you want it attached?\n If you choose NO, only the public key will be attached.",
                              "include private key?",
                              JOptionPane.YES_NO_OPTION) != 0) privKey="N/A";
            String name = file[i].getName().substring(0,file[i].getName().indexOf(".key")).trim();

            jTextArea1.append("<board>" + name + " * " + pubKey + " * " + privKey + "</board>" + lineSeparator);
        }
    }
   }

    protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        dispose();
    }
    super.processWindowEvent(e);
    }

    /**Constructor*/
    public MessageFrame(String[] args, String keypool, SettingsClass frostSettings, Frame parentFrame) {
    super();
    this.parentFrame = parentFrame;
    this.board = args[0];
    this.from = args[1];
    this.subject = args[2];
    this.text = args[3];
    this.lastUsedDirectory = args[4];
    this.state = false;
    this.keypool = keypool;
    this.frostSettings = frostSettings;

    String date = DateFun.getDate() + " - " + DateFun.getTime();
/*    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.get(Calendar.YEAR) + ".";
    date += cal.get(Calendar.MONTH) + 1 + ".";
    date += cal.get(Calendar.DATE) + " - ";
    date += cal.get(Calendar.HOUR_OF_DAY) + ":";
    date += cal.get(Calendar.MINUTE) + ":";
    date += cal.get(Calendar.SECOND);
*/
    String lineSeparator = System.getProperty("line.separator");
    if (this.text.length() > 0)
        this.text += lineSeparator + "----- " + this.from + " ----- " + date + " -----" + lineSeparator + lineSeparator;

    File signature = new File("signature.txt");
    if (signature.isFile())
        this.text += FileAccess.readFile("signature.txt");

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
        Init();
    }
    catch(Exception e) {
        e.printStackTrace();
    }
    pack();
    setLocationRelativeTo(parentFrame);
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

}

