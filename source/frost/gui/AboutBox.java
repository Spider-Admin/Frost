/*
  AboutBox.java / About Box
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

public class AboutBox extends JDialog implements ActionListener
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    private final static String product = "Frost";

    // because a growing amount of users use CVS version:
    private final static String version = "frost-wot-102703-dev";

    private final static String copyright = "Copyright (c) 2003 Jan-Thomas Czornack";
    private final static String comments1 = "Open Source Project (GPL license)";
    private final static String comments2 = "http://jtcfrost.sourceforge.net/";

    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel insetsPanel1 = new JPanel();
    JPanel insetsPanel2 = new JPanel();
    JPanel insetsPanel3 = new JPanel();
    JButton button1 = new JButton();
    JLabel imageLabel = new JLabel();
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();
    JLabel label3 = new JLabel();
    JLabel label4 = new JLabel();
    JLabel label5 = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    GridLayout gridLayout1 = new GridLayout();

    private static final ImageIcon frostImage = new ImageIcon(AboutBox.class.getResource("/data/jtc.jpg"));

    public AboutBox(Frame parent)
    {
        super(parent);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        jbInit();
        pack();
        setLocationRelativeTo(parent);
    }

    /**Component initialization*/
    private void jbInit()
    {
        imageLabel.setIcon(frostImage);
        this.setTitle(LangRes.getString("About"));
        setResizable(false);
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridLayout1.setRows(5);
        gridLayout1.setColumns(1);
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comments1);
        label5.setText(comments2);
        insetsPanel3.setLayout(gridLayout1);
        insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 10));
        button1.setText(LangRes.getString("OK"));
        button1.addActionListener(this);
        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1, null);
        insetsPanel3.add(label2, null);
        insetsPanel3.add(label3, null);
        insetsPanel3.add(label4, null);
        insetsPanel3.add(label5, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
    }

    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            cancel();
        }
        super.processWindowEvent(e);
    }

    /**Close the dialog*/
    void cancel()
    {
        hide();
        dispose();
    }

    /**Close the dialog on a button event*/
    public void actionPerformed(ActionEvent e)
    {
        if( e.getSource() == button1 )
        {
            cancel();
        }
    }
}
