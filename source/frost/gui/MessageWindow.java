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

import java.beans.*;
import java.util.logging.Logger;

import frost.SettingsClass;
import frost.util.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessageWindow extends JFrame{
	private final MessageObject message;
	private AntialiasedTextArea messageTextArea;
	private JScrollPane scrollpane;
	private MessageWindow messageWindow;
	private Listener listener;
	private SettingsClass settings;
	private Logger logger = Logger.getLogger(MessageWindow.class.getName());
	private Window parentWindow;
	
	
	private class Listener extends KeyAdapter implements KeyListener, PropertyChangeListener{
		
		public void keyPressed(KeyEvent e){
			maybeDoSomething(e);
		}
		
		public void maybeDoSomething(KeyEvent e){
			if( e.getKeyChar() == KeyEvent.VK_ESCAPE ){
					messageWindow.dispose();
			}
		}
		
		public void propertyChange( PropertyChangeEvent evt){
			
			if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_NAME)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_SIZE)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.MESSAGE_BODY_FONT_STYLE)) {
				fontChanged();
			}
		}
		
	}
	
	public MessageWindow(SettingsClass frostSettings, Window parentWindow, MessageObject message, Dimension size){
		super();
		this.setSize(size);
		this.message = message;
		this.parentWindow = parentWindow;
		settings = frostSettings;
		initialize();
	}
	
	private void initialize(){
		listener = new Listener();
		
		messageWindow = this;
		this.setTitle(message.getSubject());
		
		messageTextArea = new AntialiasedTextArea();
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setEditable(false);
		messageTextArea.setText(message.getContent());
		messageTextArea.addKeyListener(listener);
		this.addKeyListener(listener);
		
		settings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, listener);
		settings.addPropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, listener);
		settings.addPropertyChangeListener(
			SettingsClass.MESSAGE_BODY_FONT_STYLE,
			listener);
		settings.addPropertyChangeListener("messageBodyAA", listener);
		
		fontChanged();
		
		scrollpane = new JScrollPane();
		scrollpane.getViewport().add(messageTextArea);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(scrollpane, BorderLayout.CENTER );
		
		ImageIcon frameIcon = new ImageIcon(MessageWindow.class.getResource("/data/messagebright.gif"));
		this.setIconImage(frameIcon.getImage());
		this.setLocationRelativeTo(parentWindow);
		
	}
	
	/**
	 * 
	 */
	private void fontChanged() {
		String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
		int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
		int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe(
				"The selected font was not found in your system\n"
					+ "That selection will be changed to \"Monospaced\".");
			settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
			font = new Font("Monospaced", fontStyle, fontSize);
		}
		messageTextArea.setFont(font);

		fontName = settings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
		fontStyle = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
		fontSize = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
		font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe(
				"The selected font was not found in your system\n"
					+ "That selection will be changed to \"SansSerif\".");
			settings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, "SansSerif");
			font = new Font("SansSerif", fontStyle, fontSize);
		}
	}

}
