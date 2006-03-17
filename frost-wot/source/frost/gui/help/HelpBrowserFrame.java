/*
  HelpBrowserFrame.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui.help;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;

public class HelpBrowserFrame extends JFrame {
    
	private static Logger logger = Logger.getLogger(HelpBrowserFrame.class.getName());
	  
    HelpBrowser browser;
    
    private void Init() throws Exception {
    	//------------------------------------------------------------------------
    	// Configure objects
    	//------------------------------------------------------------------------
    	this.setTitle("Frost - Help Browser"); 
    	this.setSize(new Dimension(780, 550));
    	this.setResizable(true); 
    
    	browser.setPreferredSize(new Dimension(780, 550));
    
    	this.getContentPane().add(browser);
    }

    protected void processWindowEvent(WindowEvent e) {
    	if (e.getID() == WindowEvent.WINDOW_CLOSING) {
    	    setVisible(false);
    	} else {
    	    super.processWindowEvent(e);
        }
    }
    
    
    /**Constructor*/
    public HelpBrowserFrame(String locale, String zipfile) {

        this.browser = new HelpBrowser(locale, this, zipfile);
        setIconImage(new ImageIcon(getClass().getResource("/data/help.png")).getImage());

    	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    	try {
    	    Init();
    	}
    	catch(Exception e) {
    		logger.log(Level.SEVERE, "Exception thrown in constructor", e);
    	}
    	pack();
    }
    
    public void showHelpPage(String page) {
      browser.setHelpPage(page);
    }
}














