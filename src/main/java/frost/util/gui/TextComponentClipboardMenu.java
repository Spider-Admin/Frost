/*
 TextComponentClipboardMenu.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.util.gui.translation.Language;

public class TextComponentClipboardMenu extends MouseAdapter implements ClipboardOwner, ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(TextComponentClipboardMenu.class);
	
	private Language language;
	private JTextComponent textComponent;
	
	private Clipboard clipboard;

	private JPopupMenu popupMenu;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem cancelItem;
	
	public TextComponentClipboardMenu(JTextComponent textComponent, Language language) {
		this.textComponent = textComponent;
		this.language = language;
		createPopupMenu();
		textComponent.addMouseListener(this);
	}
	
	@Override
    public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger() && textComponent.isEnabled()) {
			showPopup(e.getX(), e.getY());
		}
	}
	
	@Override
    public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger() && textComponent.isEnabled()) {
			showPopup(e.getX(), e.getY());
		}
	}
	
	public void lostOwnership(Clipboard lClipboard, Transferable contents) {
		// Nothing here
	}
	
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
	
	private void showPopup(int x, int y) {

		cutItem.setText(language.getString("Common.cut"));
		copyItem.setText(language.getString("Common.copy"));
		pasteItem.setText(language.getString("Common.paste"));
		cancelItem.setText(language.getString("Common.cancel"));

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
			logger.error("Problem while pasting text.", ioe);
		} catch (UnsupportedFlavorException ufe) {
			logger.error("Problem while pasting text.", ufe);
		} catch (BadLocationException ble) {
			logger.error("Problem while pasting text.", ble);
		}		
	}
	
	private void createPopupMenu() {
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
	
	private Clipboard getClipboard() {
		if (clipboard == null) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			clipboard = toolkit.getSystemClipboard();
		}
		return clipboard;
	}

	private void copySelectedText() {
		StringSelection selection = new StringSelection(textComponent.getSelectedText());
		clipboard.setContents(selection, this);
	}

	private void cutSelectedText() {
		StringSelection selection = new StringSelection(textComponent.getSelectedText());
		clipboard.setContents(selection, this);
		
		int start = textComponent.getSelectionStart();
		int end = textComponent.getSelectionEnd();
		try {
			textComponent.getDocument().remove(start, end - start);
		} catch (BadLocationException ble) {
			logger.error("Problem while cutting text.", ble);
		}
	}

	public JPopupMenu getPopupMenu() {
        return popupMenu;
	}
}
