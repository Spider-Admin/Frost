/*
 * Created on 23-ene-2005
 * 
 */
package frost.util.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class TextComponentClipboardMenu extends MouseAdapter 
										  implements ClipboardOwner, ActionListener {

	private static Logger logger = Logger.getLogger(TextComponentClipboardMenu.class.getName());
	
	private Language language;
	private JTextComponent textComponent;
	
	private Clipboard clipboard;

	private JPopupMenu popupMenu;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem cancelItem;
	
	/**
	 * 
	 */
	public TextComponentClipboardMenu(JTextComponent textComponent, Language language) {
		this.textComponent = textComponent;
		this.language = language;
		textComponent.addMouseListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger() && textComponent.isEnabled()) {
			showPopup(e.getX(), e.getY());
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger() && textComponent.isEnabled()) {
			showPopup(e.getX(), e.getY());
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard,
	 *      java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Nothing here
	}
	
	/* (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cutItem) {
			cutSelectedText();
		}
		if (e.getSource() == copyItem) {
			copySelectedText();
		}
		if (e.getSource() == pasteItem) {
			pasteText();
		}
	}
	
	/**
	 * @param x
	 * @param y
	 */
	private void showPopup(int x, int y) {

		createPopupMenu();

		cutItem.setText(language.getString("Cut"));
		copyItem.setText(language.getString("Copy"));
		pasteItem.setText(language.getString("Paste"));
		cancelItem.setText(language.getString("Cancel"));

		if (textComponent.getSelectedText() != null) {
			if (textComponent.isEditable()) {
				cutItem.setEnabled(true);
			} else {
				cutItem.setEnabled(false);
			}
			copyItem.setEnabled(true);
		} else {
			cutItem.setEnabled(false);
			copyItem.setEnabled(false);
		}
		Transferable clipboardContent = getClipboard().getContents(this);
		if ((clipboardContent != null)
				&& (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
			if (textComponent.isEditable()) {
				pasteItem.setEnabled(true);
			} else {
				pasteItem.setEnabled(false);
			}
		} else {
			pasteItem.setEnabled(false);
		}

		popupMenu.show(textComponent, x, y);
	}
	
	/**
	 * 
	 */
	private void pasteText() {
		Transferable clipboardContent = clipboard.getContents(this);
		try {
			String text = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
			
			Caret caret = textComponent.getCaret();
			int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
			
			Document document = textComponent.getDocument();
			
			if (document instanceof PlainDocument) {
				((PlainDocument) document).replace(p0, p1 - p0, text, null);
			} else {
				if (p0 != p1) {
					document.remove(p0, p1 - p0);
                }
				document.insertString(p0, text, null);
			}
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Problem while pasting text.", ioe);
		} catch (UnsupportedFlavorException ufe) {
			logger.log(Level.SEVERE, "Problem while pasting text.", ufe);
		} catch (BadLocationException ble) {
			logger.log(Level.SEVERE, "Problem while pasting text.", ble);
		}		
	}
	
	/**
	 *  
	 */
	private void createPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();

			cutItem = new JMenuItem();
			copyItem = new JMenuItem();
			pasteItem = new JMenuItem();
			cancelItem = new JMenuItem();

			cutItem.addActionListener(this);
			copyItem.addActionListener(this);
			pasteItem.addActionListener(this);

			popupMenu.add(cutItem);
			popupMenu.add(copyItem);
			popupMenu.add(pasteItem);
			popupMenu.addSeparator();
			popupMenu.add(cancelItem);
		}
	}
	
	/**
	 * @return
	 */
	private Clipboard getClipboard() {
		if (clipboard == null) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			clipboard = toolkit.getSystemClipboard();
		}
		return clipboard;
	}

	/**
	 * 
	 */
	private void copySelectedText() {
		StringSelection selection = new StringSelection(textComponent.getSelectedText());
		clipboard.setContents(selection, this);		
	}

	/**
	 * 
	 */
	private void cutSelectedText() {
		StringSelection selection = new StringSelection(textComponent.getSelectedText());
		clipboard.setContents(selection, this);
		
		int start = textComponent.getSelectionStart();
		int end = textComponent.getSelectionEnd();
		try {
			textComponent.getDocument().remove(start, end - start);
		} catch (BadLocationException ble) {
			logger.log(Level.SEVERE, "Problem while cutting text.", ble);
		}		
	}
	
}
