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

import javax.swing.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class FreetalkMessageTab {

    private JSplitPane treeAndTabbedPaneSplitpane = null;
    private JPanel tabPanel = null;

    private JToolBar buttonToolBar;
    private final JButton updateBoardButton = new JButton(MiscToolkit.loadImageIcon("/data/toolbar/view-refresh.png"));

    private final MainFrame mainFrame;
    private final Language language;

    private final Listener listener = new Listener();

    public FreetalkMessageTab(final MainFrame localMainFrame) {

        language = Language.getInstance();
        language.addLanguageListener(listener);

        mainFrame = localMainFrame;
    }

    public void initialize() {
        if (tabPanel == null) {

            // Vertical Board Tree / MessagePane Divider
            final JPanel panel = new JPanel(new BorderLayout());
//            treeAndTabbedPaneSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, panel);
            treeAndTabbedPaneSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), panel);
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
        }
        return buttonToolBar;
    }

    public JPanel getTabPanel() {
        return tabPanel;
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

        }
    }
}
