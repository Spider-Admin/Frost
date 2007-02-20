/*
  MainFrameStatusBar.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
import javax.swing.border.*;

import frost.*;
import frost.threads.*;
import frost.util.gui.translation.*;

/**
 * Represents the mainframe status bar.
 */
public class MainFrameStatusBar extends JPanel {
    
    private Language language;

    private JPanel extendableStatusPanel;

    private JLabel statusLabelTofup = null;
    private JLabel statusLabelTofdn = null;
    private JLabel statusLabelBoard = null;
    private JLabel statusMessageLabel = null;

    private RunningMessageThreadsInformation statusBarInformations = null;

    private static ImageIcon[] newMessage = new ImageIcon[2];

    public MainFrameStatusBar() {
        super();
        language = Language.getInstance();
        initialize();
    }
    
    private void initialize() {
        statusLabelTofup = new JLabel() {
            public String getToolTipText(MouseEvent me) {
                if( statusBarInformations == null ) {
                    return null;
                }
                String txt = language.formatMessage("MainFrameStatusBar.tooltip.tofup", 
                        Integer.toString(statusBarInformations.getUploadingMessagesCount()),
                        Integer.toString(statusBarInformations.getUnsentMessageCount()),
                        Integer.toString(statusBarInformations.getAttachmentsToUploadRemainingCount()));
                return txt;
            }
        };
        // dynamic tooltip
        ToolTipManager.sharedInstance().registerComponent(statusLabelTofup);
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p1.add(statusLabelTofup);
        p1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        p1.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        statusLabelTofdn = new JLabel() {
            public String getToolTipText(MouseEvent me) {
                if( statusBarInformations == null ) {
                    return null;
                }
                String txt = language.formatMessage("MainFrameStatusBar.tooltip.tofdn", 
                        Integer.toString(statusBarInformations.getDownloadingBoardCount()),
                        Integer.toString(statusBarInformations.getRunningDownloadThreadCount()));
                return txt;
            }
        };
        // dynamic tooltip
        ToolTipManager.sharedInstance().registerComponent(statusLabelTofdn);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p2.add(statusLabelTofdn);
        p2.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        p2.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        statusLabelBoard = new JLabel();
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p3.add(statusLabelBoard);
        p3.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        p3.setAlignmentY(JComponent.CENTER_ALIGNMENT);

        statusMessageLabel = new JLabel();
        statusMessageLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p4.add(statusMessageLabel);
        p4.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        p4.setAlignmentY(JComponent.CENTER_ALIGNMENT);;

        newMessage[0] = new ImageIcon(MainFrame.class.getResource("/data/messagebright.gif"));
        newMessage[1] = new ImageIcon(MainFrame.class.getResource("/data/messagedark.gif"));
        statusMessageLabel.setIcon(newMessage[1]);

        getExtendableStatusPanel().setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 5;
        gridBagConstraints4.anchor = GridBagConstraints.CENTER;
        gridBagConstraints4.insets = new Insets(1, 1, 1, 2);
        gridBagConstraints4.gridy = 0;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 4;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.gridy = 0;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.gridx = 3;
        gridBagConstraints21.anchor = GridBagConstraints.CENTER;
        gridBagConstraints21.insets = new Insets(1, 1, 1, 1);
        gridBagConstraints21.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints21.gridy = 0;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.anchor = GridBagConstraints.CENTER;
        gridBagConstraints2.insets = new Insets(1, 1, 1, 1);
        gridBagConstraints2.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints2.gridy = 0;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.anchor = GridBagConstraints.CENTER;
        gridBagConstraints1.insets = new Insets(1, 1, 1, 1);
        gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints1.gridy = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(1, 2, 1, 1);
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.gridy = 0;
        
        setLayout(new GridBagLayout());
        add(getExtendableStatusPanel(), gridBagConstraints);
        add(p1, gridBagConstraints1);
        add(p2, gridBagConstraints2);
        add(p3, gridBagConstraints21);
        add(new JLabel(""), gridBagConstraints3); // glue
        add(p4, gridBagConstraints4);
    }

    /**
     * This method returns the extendable part of the status bar.
     */
    public JPanel getExtendableStatusPanel() {
        if (extendableStatusPanel == null) {
            extendableStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            extendableStatusPanel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        }
        return extendableStatusPanel;
    }

    public void setStatusBarInformations(RunningMessageThreadsInformation info, String selectedNode) {

        this.statusBarInformations = info;
        
        if( info == null ) {
            return;
        }
        
        // update labels
        String newText = new StringBuilder()
            .append(" ")
            .append(language.getString("MainFrame.statusBar.TOFUP")).append(": ")
            .append(info.getUploadingMessagesCount())
            .append("U / ")
            .append(info.getUnsentMessageCount())
            .append("W / ")
            .append(info.getAttachmentsToUploadRemainingCount())
            .append("A ")
            .toString();
        statusLabelTofup.setText(newText);

        newText = new StringBuilder()
            .append(" ")
            .append(language.getString("MainFrame.statusBar.TOFDO")).append(": ")
            .append(info.getDownloadingBoardCount())
            .append("B / ")
            .append(info.getRunningDownloadThreadCount())
            .append("T ")
            .toString();
        statusLabelTofdn.setText(newText);

        newText = new StringBuilder()
            .append(" ")
            .append(language.getString("MainFrame.statusBar.selectedBoard")).append(": ")
            .append(selectedNode)
            .append(" ")
            .toString();
        statusLabelBoard.setText(newText);
    }
    
    public void showNewMessageIcon(boolean show) {
        if (show) {
            statusMessageLabel.setIcon(newMessage[0]);
        } else {
            statusMessageLabel.setIcon(newMessage[1]);
        }
    }
}
