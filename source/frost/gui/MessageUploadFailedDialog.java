/*
  MessageUploadFailedDialog.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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
import javax.swing.plaf.basic.BasicOptionPaneUI;

public class MessageUploadFailedDialog extends JDialog
{
    private JButton okButton, cancelButton, tryOnNextStartupButton;
    private int userAnswer = 1; // 1 = 1st button, 2=2nd button, 3=3rd button (ok, retry on next startup, cancel)
    private Timer timer;
    private int secs;
    private JPanel buttonPanel, messagePanel;
    private String okButtonText;
    /*
     * Takes the arguments: Frame owner
     *                      int secs (seconds to wait) if (<= 0) no timeout
     *                      String title (title of the Dialog)
     *                      String message (warningmessage to display)
     *                      String okButtonText (text of the ok-button)
     *                      String cancelButtonText (text of the cancel-button)
     */

    public MessageUploadFailedDialog(Frame owner,
                                     int secs,
                                     String title,
                                     String message,
                                     String okButtonText,
                                     String tryOnNextStartupText,
                                     String cancelButtonText)
    {
        super(owner,title, true);
        this.secs = secs;
        this.okButtonText = okButtonText;

        GridBagLayout contentPaneLayout = new GridBagLayout();
        this.getContentPane().setLayout(contentPaneLayout);
        GridBagConstraints constr = new GridBagConstraints();
        Insets insets = new Insets(20,10,10,10);
        constr.anchor = GridBagConstraints.WEST;
        constr.insets = insets;

        timer = new Timer(1000, new ActionListener()
                          {
                              public void actionPerformed(ActionEvent a)
                              {
                                  timerTriggered();
                              }
                          });

        ButtonListener bl = new ButtonListener();
        if( secs > 0 )
            okButton = new JButton(okButtonText + " - " + secs);
        else
            okButton = new JButton(okButtonText);

        okButton.addActionListener(bl);
        cancelButton = new JButton (cancelButtonText);
        cancelButton.addActionListener(bl);

        tryOnNextStartupButton = new JButton( tryOnNextStartupText );
        tryOnNextStartupButton.addActionListener( bl );

		Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
	    getContentPane().add(new JLabel(warningIcon), constr);

        constr.anchor = GridBagConstraints.CENTER;
        constr.gridwidth = GridBagConstraints.REMAINDER;

        this.getContentPane().add(new JLabel(message), constr);

        buttonPanel = new JPanel(new GridBagLayout());
        this.getContentPane().add(buttonPanel, constr);

        constr.gridwidth = GridBagConstraints.RELATIVE;
        buttonPanel.add(okButton,constr);
        constr.gridwidth = GridBagConstraints.REMAINDER;
        buttonPanel.add(tryOnNextStartupButton,constr);
        buttonPanel.add(cancelButton,constr);
        this.setSize(contentPaneLayout.preferredLayoutSize(this));
        this.setResizable(false);

        setLocationRelativeTo( owner );
    }

    private void timerTriggered()
    {
        secs--;
        okButton.setText(okButtonText + " - " + secs);
        if( secs == 0 )
        {
            this.hide();
        }
    }
    private void optionButtonPressed(Object obj)
    {
    }

    public int startDialog()
    {
        if( secs > 0 )
        {
            timer.start();
        }
        okButton.requestFocus();
        this.userAnswer = 0; // unset
        setModal(true); // paranoia
        show();
        return this.userAnswer;
    }

    class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if( e.getSource() == okButton )
            {
                userAnswer = 1;
            }
            else if( e.getSource() == tryOnNextStartupButton )
            {
                userAnswer = 2;
            }
            else if( e.getSource() == cancelButton )
            {
                userAnswer = 3;
            }
            hide();
        }
    }
    
}
