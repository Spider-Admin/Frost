/*
 * Created on Dec 8, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.components;

import java.awt.*;
import java.awt.Graphics;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * @author Administrator
 *
 *	This a subclass of JTextArea that lets the programmer specify whether to use 
 *	antialising when rendering the text or not.
 *
 */

//TODO: Bug: if the text contains arabic characters, antialias will be disabled

public class MessageTextArea extends JTextArea {

	private boolean antiAliasEnabled = false;

	/**
	 * 
	 */
	public MessageTextArea() {
		super();
	}

	/**
	 * @param rows
	 * @param columns
	 */
	public MessageTextArea(int rows, int columns) {
		super(rows, columns);
	}

	/**
	 * @param text
	 */
	public MessageTextArea(String text) {
		super(text);
	}

	/**
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public MessageTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	/**
	 * @param doc
	 */
	public MessageTextArea(Document doc) {
		super(doc);
	}

	/**
	 * @param doc
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public MessageTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
	}

	/**
	 * @return
	 */
	public boolean isAntiAliasEnabled() {
		return antiAliasEnabled;
	}

	/**
	 * @param b
	 */
	public void setAntiAliasEnabled(boolean b) {
		antiAliasEnabled = b;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		if (antiAliasEnabled) {
			Graphics2D graphics2D = (Graphics2D) g;
			graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		super.paint(g);
	}

}
