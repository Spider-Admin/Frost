/*
 * Created on 02-dic-2004
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

import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author $author$
 * @version $revision$
 */
public class JClipboardTextField extends JTextField 
								 implements MouseListener, ClipboardOwner, ActionListener {

	private static Logger logger = Logger.getLogger(JClipboardTextField.class.getName());
	
	private UpdatingLanguageResource languageResource;

	private Clipboard clipboard;

	private JPopupMenu popupMenu;

	private JMenuItem cutItem;

	private JMenuItem copyItem;

	private JMenuItem pasteItem;

	private JMenuItem cancelItem;

	/**
	 * @param text
	 * @param columns
	 * @param languageResource this language resource must contain these strings:
	 * 				"Cut", "Copy", "Paste" and "Cancel"
	 */
	public JClipboardTextField(String text, int columns, UpdatingLanguageResource languageResource) {
		super(text, columns);
		this.languageResource = languageResource;

		addMouseListener(this);
	}

	/**
	 * @param languageResource this language resource must contain these strings:
	 * 				"Cut", "Copy", "Paste" and "Cancel"
	 */
	public JClipboardTextField(UpdatingLanguageResource languageResource) {
		this(null, 0, languageResource);
	}

	/**
	 * @param columns
	 * @param languageResource this language resource must contain these strings:
	 * 				"Cut", "Copy", "Paste" and "Cancel"
	 */
	public JClipboardTextField(int columns, UpdatingLanguageResource languageResource) {
		this(null, columns, languageResource);
	}

	/**
	 * @param text
	 * @param languageResource this language resource must contain these strings:
	 * 				"Cut", "Copy", "Paste" and "Cancel"
	 */
	public JClipboardTextField(String text, UpdatingLanguageResource languageResource) {
		this(text, 0, languageResource);
	}

	/**
	 * @param x
	 * @param y
	 */
	private void showPopup(int x, int y) {

		createPopupMenu();

		cutItem.setText(languageResource.getString("Cut"));
		copyItem.setText(languageResource.getString("Copy"));
		pasteItem.setText(languageResource.getString("Paste"));
		cancelItem.setText(languageResource.getString("Cancel"));

		if (getSelectedText() != null) {
			cutItem.setEnabled(true);
			copyItem.setEnabled(true);
		} else {
			cutItem.setEnabled(false);
			copyItem.setEnabled(false);
		}
		Transferable clipboardContent = getCliboard().getContents(this);
		if ((clipboardContent != null)
				&& (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
			pasteItem.setEnabled(true);
		} else {
			pasteItem.setEnabled(false);
		}

		popupMenu.show(this, x, y);
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
	private Clipboard getCliboard() {
		if (clipboard == null) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			clipboard = toolkit.getSystemClipboard();
		}
		return clipboard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// Nothing here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// Nothing here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// Nothing here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard,
	 *      java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Nothing here
	}

	/*
	 * (non-Javadoc)
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
	 * 
	 */
	private void pasteText() {
		Transferable clipboardContent = clipboard.getContents(this);
		try {
			String text = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
			
			Caret caret = getCaret();
			int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
			
			Document document = getDocument();
			
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
	private void copySelectedText() {
		StringSelection selection = new StringSelection(getSelectedText());
		clipboard.setContents(selection, this);		
	}

	/**
	 * 
	 */
	private void cutSelectedText() {
		StringSelection selection = new StringSelection(getSelectedText());
		clipboard.setContents(selection, this);
		
		int start = getSelectionStart();
		int end = getSelectionEnd();
		try {
			getDocument().remove(start, end - start);
		} catch (BadLocationException ble) {
			logger.log(Level.SEVERE, "Problem while cutting text.", ble);
		}		
	}
}