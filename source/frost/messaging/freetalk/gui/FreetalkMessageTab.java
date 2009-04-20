/*
  FreetalkMessageTab.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.freetalk.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import frost.*;
import frost.fcp.fcp07.*;
import frost.fcp.fcp07.freetalk.*;
import frost.fcp.fcp07.freetalk.FcpFreetalkConnection.*;
import frost.messaging.freetalk.*;
import frost.messaging.freetalk.boards.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class FreetalkMessageTab {

    private static final Logger logger = Logger.getLogger(FreetalkMessageTab.class.getName());

    private JSplitPane treeAndTabbedPaneSplitpane = null;
    private JPanel tabPanel = null;

    private JToolBar buttonToolBar;
    private final JButton updateBoardButton = new JButton(MiscToolkit.loadImageIcon("/data/toolbar/view-refresh.png"));

    private final MainFrame mainFrame;
    private final Language language;
    private FreetalkManager ftManager = null;

    private final Listener listener = new Listener();

    public FreetalkMessageTab(final MainFrame localMainFrame) {

        language = Language.getInstance();
        language.addLanguageListener(listener);

        mainFrame = localMainFrame;
    }

    public void initialize() {
        if (tabPanel == null) {

            ftManager = FreetalkManager.getInstance();

            final JScrollPane tofTreeScrollPane = new JScrollPane(ftManager.getBoardTree());
            tofTreeScrollPane.setWheelScrollingEnabled(true);

            ftManager.getBoardTree().addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(final TreeSelectionEvent e) {
                    boardTree_actionPerformed();
                }
            });

            // Vertical Board Tree / MessagePane Divider
            final JPanel panel = new JPanel(new BorderLayout());
            treeAndTabbedPaneSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, panel);
//            panel.add(getMessagePanel(), BorderLayout.CENTER);

            int dividerLoc = Core.frostSettings.getIntValue("FreetalkTab.treeAndTabbedPaneSplitpaneDividerLocation");
            if( dividerLoc < 10 ) {
                dividerLoc = 160;
            }
            treeAndTabbedPaneSplitpane.setDividerLocation(dividerLoc);

            final JPanel p = new JPanel(new BorderLayout());
            p.add(getButtonToolBar(), BorderLayout.NORTH);
            p.add(treeAndTabbedPaneSplitpane, BorderLayout.CENTER);

            tabPanel = p;
        }
    }

    public void boardTree_actionPerformed() {

    }

    public void saveLayout() {
        Core.frostSettings.setValue("FreetalkTab.treeAndTabbedPaneSplitpaneDividerLocation",
                treeAndTabbedPaneSplitpane.getDividerLocation());
    }

    public JToolBar getButtonToolBar() {
        if (buttonToolBar == null) {
            buttonToolBar = new JToolBar();

            MiscToolkit.configureButton(updateBoardButton, "MessagePane.toolbar.tooltip.update", language);

            buttonToolBar.setRollover(true);
            buttonToolBar.setFloatable(false);
            final Dimension blankSpace = new Dimension(3, 3);

            buttonToolBar.add(Box.createRigidArea(blankSpace));
            buttonToolBar.add(updateBoardButton);
            buttonToolBar.add(Box.createRigidArea(blankSpace));

            updateBoardButton.addActionListener(listener);

            if (!Core.isFreetalkTalkable()) {
                updateBoardButton.setEnabled(false);
            }
        }
        return buttonToolBar;
    }

    public JPanel getTabPanel() {
        return tabPanel;
    }

    private class ListBoardsCallback implements FreetalkNodeMessageCallback {

        public void handleNodeMessage(final String id, final NodeMessage nodeMsg) {

            if (!nodeMsg.isMessageName("FCPPluginReply")) {
                logger.severe("Unexpected NodeMessage received: "+nodeMsg.getMessageName());
                FreetalkManager.getInstance().getConnection().unregisterCallback(id);
                mainFrame.deactivateGlassPane();
                return;
            }

            if ("EndListBoards".equals(nodeMsg.getStringValue("Replies.Message"))) {
                FreetalkManager.getInstance().getConnection().unregisterCallback(id);
                mainFrame.deactivateGlassPane();
                return;
            }

            if (!"Board".equals(nodeMsg.getStringValue("Replies.Message"))) {
                logger.severe("Unexpected NodeMessage received: "+nodeMsg.getStringValue("Replies.Message"));
                FreetalkManager.getInstance().getConnection().unregisterCallback(id);
                mainFrame.deactivateGlassPane();
                return;
            }

            final String name = nodeMsg.getStringValue("Replies.Name");
            final int messageCount = new Integer(nodeMsg.getStringValue("Replies.MessageCount"));
            final long latestMessageDate = new Integer(nodeMsg.getStringValue("Replies.LatestMessageDate"));
            final long firstSeenDate = new Integer(nodeMsg.getStringValue("Replies.FirstSeenDate"));

            final FreetalkBoard board = new FreetalkBoard(name, messageCount, firstSeenDate, latestMessageDate);
            // FIXME: add to board list
            ftManager.getBoardTree().addNewBoard(board);
        }
    }

    public void sendFreetalkCommandListBoards() {
        final String id = FcpFreetalkConnection.getNextFcpidentifier();

        FreetalkManager.getInstance().getConnection().registerCallback(id, new ListBoardsCallback());

        mainFrame.activateGlassPane();

        try {
            FreetalkManager.getInstance().getConnection().sendCommandListBoards(id);
        } catch(final Exception ex) {
            logger.log(Level.SEVERE, "Error sending command ListBoards", ex);
            mainFrame.deactivateGlassPane();
            return;
        }
    }

    private class Listener
    implements
        ActionListener,
        LanguageListener
    {

        public Listener() {
            super();
        }

        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == updateBoardButton) {
                updateButton_actionPerformed(e);
            }
        }

        public void languageChanged(final LanguageEvent event) {
            // TODO Auto-generated method stub
        }

        protected void updateButton_actionPerformed(final ActionEvent e) {
            sendFreetalkCommandListBoards();
        }
    }
}
