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
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JAATextArea extends JTextArea {

	private boolean antiAliasEnabled = false;

	/**
	 * 
	 */
	public JAATextArea() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param rows
	 * @param columns
	 */
	public JAATextArea(int rows, int columns) {
		super(rows, columns);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 */
	public JAATextArea(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public JAATextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param doc
	 */
	public JAATextArea(Document doc) {
		super(doc);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param doc
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public JAATextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		// TODO Auto-generated constructor stub
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
