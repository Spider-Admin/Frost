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

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;

public class MessageUploadFailedDialog extends JDialog {
    private JButton okButton, cancelButton;
    private boolean goOn = true;
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

    public MessageUploadFailedDialog(Frame owner, int secs, String title, String message, String okButtonText, String cancelButtonText){
    super(owner,title, true);
    this.secs = secs;
    this.okButtonText = okButtonText;

    GridBagLayout contentPaneLayout = new GridBagLayout();
    this.getContentPane().setLayout(contentPaneLayout);
    GridBagConstraints constr = new GridBagConstraints();
    Insets insets = new Insets(20,10,10,10);
    constr.anchor = constr.WEST;
    constr.insets = insets;

    timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent a) {
            timerTriggered();
        }
        });

    ButtonListener bl = new ButtonListener();
    if(secs > 0)
        okButton = new JButton(okButtonText + " - " + secs);
    else
        okButton = new JButton(okButtonText);

    okButton.addActionListener(bl);
    cancelButton = new JButton (cancelButtonText);
    cancelButton.addActionListener(bl);

    this.getContentPane().add(new JLabel(new IconFromUI().getIcon()), constr);

    constr.anchor = constr.CENTER;
    constr.gridwidth = constr.REMAINDER;

    this.getContentPane().add(new JLabel(message), constr);

    buttonPanel = new JPanel(new GridBagLayout());
    this.getContentPane().add(buttonPanel, constr);

    constr.gridwidth = constr.RELATIVE;
    buttonPanel.add(okButton,constr);
    constr.gridwidth = constr.REMAINDER;
        buttonPanel.add(cancelButton,constr);
        this.setSize(contentPaneLayout.preferredLayoutSize(this));
    this.setResizable(false);

    // thanks JanTho ;)
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();

    if (frameSize.height > screenSize.height) {
        frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
        frameSize.width = screenSize.width;
    }
    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public boolean getAnswer(){
    return goOn;
    }

    private void timerTriggered(){
    secs--;
    okButton.setText(okButtonText + " - " + secs);
    if(secs == 0){
        this.hide();
    }
    }
    private void optionButtonPressed(Object obj){
        if(obj == okButton)
        goOn = true;
        else if(obj == cancelButton)
        goOn = false;
        this.hide();
    }
    public void show(){
    if(secs > 0)
        timer.start();
    okButton.requestFocus();
    super.show();
    }

    class ButtonListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
        optionButtonPressed(e.getSource());
    }
    }
    class IconFromUI extends BasicOptionPaneUI{
    public IconFromUI (){
        super();
    }
    public Icon getIcon(){
        return super.getIconForType(JOptionPane.WARNING_MESSAGE);
    }
    }
}
