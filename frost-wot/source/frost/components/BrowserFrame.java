/*
  BrowserFrame.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

package frost.components;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;

public class BrowserFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(BrowserFrame.class.getName());

    boolean plugin;

    Browser browser = new Browser(this);

    private void Init() throws Exception {
    //------------------------------------------------------------------------
    // Configure objects
    //------------------------------------------------------------------------
    this.setTitle("Experimental Freenet Browser");
    this.setSize(new Dimension(780, 550));
    this.setResizable(true);

    browser.setPreferredSize(new Dimension(780, 550));

    this.getContentPane().add(browser);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        dispose();
        if (!plugin)
        System.exit(0);
    }
    super.processWindowEvent(e);
    }

    /**Constructor*/
    public BrowserFrame(boolean plugin) {

    this.plugin = plugin;

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
        Init();
    }
    catch(Exception e) {
        logger.log(Level.SEVERE, "Exception thrown in constructor", e);
    }
    pack();
    }
}














