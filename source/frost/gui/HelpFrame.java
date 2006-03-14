/*
  HelpFrame.java / Frost
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
import java.util.logging.*;

import javax.swing.*;

import frost.*;

public class HelpFrame extends JFrame {
	
	private static Logger logger = Logger.getLogger(HelpFrame.class.getName());
	
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    //------------------------------------------------------------------------
    // Class Vars
    //------------------------------------------------------------------------
    File[] itemArray = (new File("help")).listFiles();

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel();

    JComboBox comboBox = new JComboBox();

    JTextArea textArea = new JTextArea();

    JScrollPane scrollPane = new JScrollPane(textArea);

    private void Init() throws Exception {
    //------------------------------------------------------------------------
    // Configure objects
    //------------------------------------------------------------------------

    ImageIcon frameIcon = new ImageIcon(getClass().getResource("/data/jtc.jpg"));
	setIconImage(frameIcon.getImage());
    setTitle("Help frame");
    setSize(new Dimension(750, 550));
    setResizable(true);

    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    scrollPane.setPreferredSize(new Dimension(750,550));

    //------------------------------------------------------------------------
    // Actionlistener
    //------------------------------------------------------------------------
    comboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
            textArea.setText(FileAccess.readFile(new File("help/" + (String)comboBox.getSelectedItem() + ".txt")));
            textArea.setCaretPosition(0);
        }
        });

    //------------------------------------------------------------------------
    // Append objects
    //------------------------------------------------------------------------
    this.getContentPane().add(mainPanel, null); // add Main panel
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.NORTH);
    buttonPanel.add(comboBox);

    for (int i = 0; i < itemArray.length; i++) {
        if (itemArray[i].isFile() && (itemArray[i].getName()).endsWith(".txt")) {
        String item = itemArray[i].getName();
        comboBox.addItem(item.substring(0, item.length() - 4));
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
    public HelpFrame(JFrame parent) {
    super();
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
        Init();
    }
    catch(Exception e) {
		logger.log(Level.SEVERE, "Exception thrown in constructor", e);
    }
    pack();
    setLocationRelativeTo(parent);
    }
}

