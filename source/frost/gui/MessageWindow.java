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
	
	private static Logger logger = Logger.getLogger(MessageWindow.class.getName());
	
	private final MessageObject message;
	private AntialiasedTextArea messageTextArea;
	private JScrollPane scrollpane;
	private MessageWindow messageWindow;
	private Listener listener;
	private SettingsClass settings;
	private Window parentWindow;
	
	/**
	 * 
	 */
	private class Listener extends WindowAdapter implements KeyListener, PropertyChangeListener, WindowListener{
		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e){
			maybeDoSomething(e);
		}
		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(KeyEvent e){
			//Nothing
		}
		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e){
			//Nothing
		}
		/* (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
		 */
		public void windowClosing(WindowEvent e){
			close();
		}
		/**
		 * @param e
		 */
		public void maybeDoSomething(KeyEvent e){
			if( e.getKeyChar() == KeyEvent.VK_ESCAPE ){
					close();
			}
		}
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
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
	
	/**
	 * @param frostSettings
	 * @param parentWindow
	 * @param message
	 * @param size
	 */
	public MessageWindow(SettingsClass frostSettings, Window parentWindow, MessageObject message, Dimension size){
		super();
		this.setSize(size);
		this.message = message;
		this.parentWindow = parentWindow;
		settings = frostSettings;
		initialize();
	}
	
	/**
	 * 
	 */
	private void initialize(){
		listener = new Listener();
		
		messageWindow = this;
		this.setTitle(message.getSubject());
		
		messageTextArea = new AntialiasedTextArea();
		messageTextArea.setAntiAliasEnabled(settings.getBoolValue("messageBodyAA"));
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setEditable(false);
		messageTextArea.setText(message.getContent());
		messageTextArea.addKeyListener(listener);
		this.addKeyListener(listener);
		this.addWindowListener(listener);
		
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
	
	/**
	 * 
	 */
	private void close(){
		settings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_NAME, listener);
		settings.removePropertyChangeListener(SettingsClass.MESSAGE_BODY_FONT_SIZE, listener);
		settings.removePropertyChangeListener(
			SettingsClass.MESSAGE_BODY_FONT_STYLE,
			listener);
		settings.removePropertyChangeListener("messageBodyAA", listener);		
		dispose();
	}

}
