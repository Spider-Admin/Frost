/*
  MessageWindow.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

import frost.*;
import frost.gui.objects.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class MessageWindow extends JFrame {

//  private static Logger logger = Logger.getLogger(MessageWindow.class.getName());

    private final FrostMessageObject message;
    private Window parentWindow;

    private MessageTextPane messageTextPane;
    private MessageWindowTopPanel topPanel;

    private Listener listener;

    private Language language = Language.getInstance();
    
    private SearchMessagesConfig searchMessagesConfig = null;

    public MessageWindow(Window parentWindow, FrostMessageObject message, Dimension size) {
        this(parentWindow, message, size, null);
    }

    public MessageWindow(Window parentWindow, FrostMessageObject message, Dimension size, SearchMessagesConfig smc) {
        super();
        this.message = message;
        this.parentWindow = parentWindow;
        this.setSize(size);
        this.searchMessagesConfig = smc;

        initialize();

        // set visible BEFORE updating the textpane to allow correct positioning of dividers
        setVisible(true);

        messageTextPane.update_messageSelected(message);
    }

    private void initialize(){
        listener = new Listener();

        this.setTitle(message.getSubject());

        this.getContentPane().setLayout(new BorderLayout());

        topPanel = new MessageWindowTopPanel(message);
        this.getContentPane().add(topPanel, BorderLayout.NORTH);

        messageTextPane = new MessageTextPane(this, searchMessagesConfig);
        this.getContentPane().add(messageTextPane, BorderLayout.CENTER);

        this.addKeyListener(listener);
        messageTextPane.addKeyListener(listener);
        topPanel.addKeyListener(listener);
        this.addWindowListener(listener);

        ImageIcon frameIcon = new ImageIcon(MessageWindow.class.getResource("/data/messagebright.gif"));
        this.setIconImage(frameIcon.getImage());
        this.setLocationRelativeTo(parentWindow);
    }

    private void close() {
        messageTextPane.removeKeyListener(listener);
        messageTextPane.close();
        topPanel.removeKeyListener(listener);
        topPanel.close();
        dispose();
    }

    private void replyButtonPressed() {
        MainFrame.getInstance().getMessagePanel().composeReply(message, MainFrame.getInstance());
    }

    private class Listener extends WindowAdapter implements KeyListener, WindowListener {
        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {
            maybeDoSomething(e);
        }
        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent e) {
            //Nothing
        }
        /* (non-Javadoc)
         * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
         */
        public void keyTyped(KeyEvent e) {
            //Nothing
        }
        /* (non-Javadoc)
         * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing(WindowEvent e) {
            close();
        }
        /**
         * @param e
         */
        public void maybeDoSomething(KeyEvent e){
            if( e.getKeyChar() == KeyEvent.VK_ESCAPE ) {
                close();
            }
        }
    }

    class MessageWindowTopPanel extends JPanel implements LanguageListener {

        private JLabel Lsubject = null;
        private JLabel Lfrom = null;
        private JLabel Ldate = null;
        private JTextField TFsubject = null;
        private JTextField TFfrom = null;
        private JTextField TFdate = null;
        private JLabel Lboard = null;
        private JTextField TFboard = null;

        private FrostMessageObject innerMessage;
        private JButton Breply = null;

        public MessageWindowTopPanel(FrostMessageObject msg) {
            super();
            innerMessage = msg;
            initialize();
            languageChanged(null);
            language.addLanguageListener(this);
        }

        public void addKeyListener(KeyListener l) {
            super.addKeyListener(l);
            Component[] c = getComponents();
            for(int x=0; x < c.length; x++) {
                c[x].addKeyListener(l);
            }
        }

        public void removeKeyListener(KeyListener l) {
            super.removeKeyListener(l);
            Component[] c = getComponents();
            for(int x=0; x < c.length; x++) {
                c[x].removeKeyListener(l);
            }
        }

        public void close() {
            language.removeLanguageListener(this);
        }

        private void initialize() {
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            gridBagConstraints41.gridx = 5;
            gridBagConstraints41.anchor = java.awt.GridBagConstraints.NORTHEAST;
            gridBagConstraints41.gridheight = 3;
            gridBagConstraints41.insets = new java.awt.Insets(5,5,5,5);
            gridBagConstraints41.gridy = 1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints11.gridy = 3;
            gridBagConstraints11.weightx = 0.0;
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints11.insets = new java.awt.Insets(1,1,1,5);
            gridBagConstraints11.gridx = 3;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 2;
            gridBagConstraints6.insets = new java.awt.Insets(1,8,1,2);
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints6.gridy = 3;
            Lboard = new JLabel();
            Lboard.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints5.gridy = 3;
            gridBagConstraints5.weightx = 0.0;
            gridBagConstraints5.insets = new java.awt.Insets(1,1,1,5);
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints5.gridx = 1;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints4.gridy = 2;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.gridwidth = 4;
            gridBagConstraints4.insets = new java.awt.Insets(1,1,1,5);
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints4.gridx = 1;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.gridwidth = 4;
            gridBagConstraints3.insets = new java.awt.Insets(1,1,1,5);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints3.gridx = 1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.insets = new java.awt.Insets(1,5,1,2);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints2.gridy = 3;
            Ldate = new JLabel();
            Ldate.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.insets = new java.awt.Insets(1,5,1,2);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.gridy = 2;
            Lfrom = new JLabel();
            Lfrom.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.insets = new java.awt.Insets(1,5,1,2);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.gridy = 1;
            Lsubject = new JLabel();
            Lsubject.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            this.setLayout(new GridBagLayout());
            this.setSize(new java.awt.Dimension(496,254));
            this.add(Lsubject, gridBagConstraints);
            this.add(Lfrom, gridBagConstraints1);
            this.add(Ldate, gridBagConstraints2);
            this.add(getTFsubject(), gridBagConstraints3);
            this.add(getTFfrom(), gridBagConstraints4);
            this.add(getTFdate(), gridBagConstraints5);
            this.add(Lboard, gridBagConstraints6);
            this.add(getTFboard(), gridBagConstraints11);
            this.add(getBreply(), gridBagConstraints41);
        }

        public void languageChanged(LanguageEvent event) {
            Lsubject.setText(language.getString("Subject")+":");
            Lfrom.setText(language.getString("From")+":");
            Ldate.setText(language.getString("Date")+":");
            Lboard.setText(language.getString("Board")+":");
        }

        private JTextField getTFsubject() {
            if( TFsubject == null ) {
                TFsubject = new JTextField();
                TFsubject.setText(" "+innerMessage.getSubject());
                TFsubject.setBorder(javax.swing.BorderFactory.createEmptyBorder(2,2,2,2));
                TFsubject.setEditable(false);
            }
            return TFsubject;
        }

        private JTextField getTFfrom() {
            if( TFfrom == null ) {
                TFfrom = new JTextField();
                TFfrom.setText(" "+innerMessage.getFrom());
                TFfrom.setBorder(javax.swing.BorderFactory.createEmptyBorder(2,2,2,2));
                TFfrom.setEditable(false);
            }
            return TFfrom;
        }

        private JTextField getTFdate() {
            if( TFdate == null ) {
                TFdate = new JTextField();
                TFdate.setText(" "+innerMessage.getDateAndTime());
                TFdate.setBorder(javax.swing.BorderFactory.createEmptyBorder(2,2,2,2));
                TFdate.setEditable(false);
            }
            return TFdate;
        }

        private JTextField getTFboard() {
            if( TFboard == null ) {
                TFboard = new JTextField();
                TFboard.setText(" "+innerMessage.getBoard());
                TFboard.setBorder(javax.swing.BorderFactory.createEmptyBorder(2,2,2,2));
                TFboard.setEditable(false);
            }
            return TFboard;
        }

        private JButton getBreply() {
            if( Breply == null ) {
                Breply = new JButton();
                Breply.setIcon(new ImageIcon(getClass().getResource("/data/reply.gif")));
                Breply.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        replyButtonPressed();
                    }
                });
                MiscToolkit toolkit = MiscToolkit.getInstance();
                toolkit.configureButton(Breply, "Reply", "/data/reply_rollover.gif", language);
            }
            return Breply;
        }
    }
}
