/*
  MessageWindow.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

public class MessageWindow extends JFrame {
	
//	private static Logger logger = Logger.getLogger(MessageWindow.class.getName());
	
	private final FrostMessageObject message;
	private Window parentWindow;
    
    private MessageTextPane messageTextPane;
    
    private Listener listener;
	
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
	
	/**
	 * @param frostSettings
	 * @param parentWindow
	 * @param message
	 * @param size
	 */
	public MessageWindow(Window parentWindow, FrostMessageObject message, Dimension size){
		super();
		this.message = message;
		this.parentWindow = parentWindow;
        this.setSize(size);
		initialize();
        
		// set visible BEFORE updating the textpane to allow correct positioning of dividers
		setVisible(true);
        
        messageTextPane.update_messageSelected(message);
	}
	
	private void initialize(){
		listener = new Listener();
		
		this.setTitle(message.getSubject());
		
		this.getContentPane().setLayout(new BorderLayout());
        
        messageTextPane = new MessageTextPane(this);
        this.getContentPane().add(messageTextPane, BorderLayout.CENTER);

        this.addKeyListener(listener);
        messageTextPane.addKeyListener(listener);
        this.addWindowListener(listener);
        
		ImageIcon frameIcon = new ImageIcon(MessageWindow.class.getResource("/data/messagebright.gif"));
		this.setIconImage(frameIcon.getImage());
		this.setLocationRelativeTo(parentWindow);
	}
	
	private void close() {
        messageTextPane.removeKeyListener(listener);
        messageTextPane.close();
		dispose();
	}
}
