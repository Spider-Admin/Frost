/*
 * Created on 12.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package frost.gui;
import frost.messages.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import frost.util.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessageWindow extends JFrame{
	private final MessageObject message;
	private AntialiasedTextArea textfield;
	private JScrollPane scrollpane;
	private MessageWindow messageWindow;
	private Listener listener;
	
	private class Listener extends KeyAdapter implements KeyListener{
		
		public void keyPressed(KeyEvent e){
			maybeDoSomething(e);
		}
		
		public void maybeDoSomething(KeyEvent e){
			System.out.println(e);
			if( e.getKeyChar() == KeyEvent.VK_ESCAPE ){
					messageWindow.dispose();
			}
		}
		
	}
	
	public MessageWindow(MessageObject message, Dimension size){
		super();
		this.setSize(size);
		this.message = message;
		initialize();
	}
	
	private void initialize(){
		listener = new Listener();
		
		messageWindow = this;
		this.setTitle(message.getSubject());
		
		textfield = new AntialiasedTextArea();
		textfield.setWrapStyleWord(true);
		textfield.setLineWrap(true);
		textfield.setEditable(false);
		textfield.setText(message.getContent());
		textfield.addKeyListener(listener);
		this.addKeyListener(listener);
		
		scrollpane = new JScrollPane();
		scrollpane.getViewport().add(textfield);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(scrollpane, BorderLayout.CENTER );
	}
}
