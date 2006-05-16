/*
  DownloadPanel.java / Frost

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
package frost.fileTransfer.download;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

import frost.*;
import frost.ext.*;
import frost.fcp.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class DownloadPanel extends JPanel implements SettingsUpdater {
	
	private class PopupMenuDownload
		extends JSkinnablePopupMenu
		implements ActionListener, LanguageListener, ClipboardOwner {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyKeysAndNamesItem = new JMenuItem();
		private JMenuItem copyKeysItem = new JMenuItem();
		private JMenuItem copyExtendedInfoItem = new JMenuItem();
		private JMenuItem disableAllDownloadsItem = new JMenuItem();
		private JMenuItem disableSelectedDownloadsItem = new JMenuItem();
		private JMenuItem enableAllDownloadsItem = new JMenuItem();
		private JMenuItem enableSelectedDownloadsItem = new JMenuItem();
		private JMenuItem invertEnabledAllItem = new JMenuItem();
		private JMenuItem invertEnabledSelectedItem = new JMenuItem();
		private JMenuItem removeAllDownloadsItem = new JMenuItem();
		private JMenuItem removeFinishedItem = new JMenuItem();
		private JMenuItem removeSelectedDownloadsItem = new JMenuItem();
		private JMenuItem restartSelectedDownloadsItem = new JMenuItem();

		private JMenu copyToClipboardMenu = new JMenu();
		
		private String keyNotAvailableMessage;
		private String fileMessage;
		private String keyMessage;
		private String bytesMessage;
		
		private Clipboard clipboard;

		public PopupMenuDownload() {
			super();
			initialize();
		}

		private void initialize() {
			refreshLanguage();

			// TODO: implement cancel of downloading

			copyToClipboardMenu.add(copyKeysAndNamesItem);
			copyToClipboardMenu.add(copyKeysItem);
			copyToClipboardMenu.add(copyExtendedInfoItem);

			copyKeysAndNamesItem.addActionListener(this);
			copyKeysItem.addActionListener(this);
			copyExtendedInfoItem.addActionListener(this);
			restartSelectedDownloadsItem.addActionListener(this);
			removeSelectedDownloadsItem.addActionListener(this);
			removeAllDownloadsItem.addActionListener(this);
			removeFinishedItem.addActionListener(this);
			enableAllDownloadsItem.addActionListener(this);
			disableAllDownloadsItem.addActionListener(this);
			enableSelectedDownloadsItem.addActionListener(this);
			disableSelectedDownloadsItem.addActionListener(this);
			invertEnabledAllItem.addActionListener(this);
			invertEnabledSelectedItem.addActionListener(this);
		}

		private void refreshLanguage() {
			keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
			fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
			keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
			bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
			
			cancelItem.setText(language.getString("Common.cancel"));
			copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
			copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
			copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
			restartSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.restartSelectedDownloads"));
			removeSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.remove.removeSelectedDownloads"));
			removeAllDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.remove.removeAllDownloads"));
			//downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
			removeFinishedItem.setText(language.getString("DownloadPane.fileTable.popupmenu.remove.removeFinishedDownloads"));
			enableAllDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.enableAllDownloads"));
			disableAllDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.disableAllDownloads"));
			enableSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.enableSelectedDownloads"));
			disableSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.disableSelectedDownloads"));
			invertEnabledAllItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForAllDownloads"));
			invertEnabledSelectedItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForSelectedDownloads"));

			copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");
		}
		
		private Clipboard getClipboard() {
			if (clipboard == null) {
				clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			}
			return clipboard;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyKeysItem) {
				copyKeys();
			}
			if (e.getSource() == copyKeysAndNamesItem) {
				copyKeysAndNames();
			}
			if (e.getSource() == copyExtendedInfoItem) {
				copyExtendedInfo();
			}
			if (e.getSource() == restartSelectedDownloadsItem) {
				restartSelectedDownloads();
			}
			if (e.getSource() == removeSelectedDownloadsItem) {
				removeSelectedDownloads();
			}
			if (e.getSource() == removeAllDownloadsItem) {
				removeAllDownloads();
			}
			if (e.getSource() == removeFinishedItem) {
				removeFinished();
			}
			if (e.getSource() == enableAllDownloadsItem) {
				enableAllDownloads();
			}
			if (e.getSource() == disableAllDownloadsItem) {
				disableAllDownloads();
			}
			if (e.getSource() == enableSelectedDownloadsItem) {
				enableSelectedDownloads();
			}
			if (e.getSource() == disableSelectedDownloadsItem) {
				disableSelectedDownloads();
			}
			if (e.getSource() == invertEnabledAllItem) {
				invertEnabledAll();
			}
			if (e.getSource() == invertEnabledSelectedItem) {
				invertEnabledSelected();
			}
		}

		private void invertEnabledSelected() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(null, selectedItems);
		}

		private void invertEnabledAll() {
			model.setAllItemsEnabled(null);
		}

		private void disableSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(new Boolean(false), selectedItems);
		}

		private void enableSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(new Boolean(true), selectedItems);
		}

		private void disableAllDownloads() {
			model.setAllItemsEnabled(new Boolean(false));
		}

		private void enableAllDownloads() {
			model.setAllItemsEnabled(new Boolean(true));
		}

		private void removeFinished() {
			model.removeFinishedDownloads();
		}

		private void removeAllDownloads() {
			model.removeAllItems();
		}

		private void removeSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}

		private void restartSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.restartItems(selectedItems);
		}

		/**
		 * This method copies the CHK keys and file names of the selected items (if any) to
		 * the clipboard.
		 */
		private void copyKeysAndNames() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostDownloadItem item = (FrostDownloadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(key);
					textToCopy.append("/");
					textToCopy.append(item.getFileName());
					textToCopy.append("\n");
				}				
				StringSelection selection = new StringSelection(textToCopy.toString());
				getClipboard().setContents(selection, this);	
			}
		}
		
		/**
		 * This method copies extended information about the selected items (if any) to
		 * the clipboard. That information is composed of the filename, the key and
		 * the size in bytes.
		 */
		private void copyExtendedInfo() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostDownloadItem item = (FrostDownloadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(fileMessage);
					textToCopy.append(item.getFileName() + "\n");
					textToCopy.append(keyMessage);
					textToCopy.append(key + "\n");
					textToCopy.append(bytesMessage);
					textToCopy.append(item.getFileSize() + "\n\n");
				}				
				//We remove the additional \n at the end
				String result = textToCopy.substring(0, textToCopy.length() - 1);
				
				StringSelection selection = new StringSelection(result);
				getClipboard().setContents(selection, this);	
			}
		}

		/**
		 * This method copies the CHK keys of the selected items (if any) to
		 * the clipboard.
		 */
		private void copyKeys() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			if (selectedItems.length > 0) {
				StringBuffer textToCopy = new StringBuffer();
				for (int i = 0; i < selectedItems.length; i++) {
					FrostDownloadItem item = (FrostDownloadItem) selectedItems[i];
					String key = item.getKey();
					if (key == null) {
						key = keyNotAvailableMessage;
					}
					textToCopy.append(key);
					textToCopy.append("\n");
				}				
				StringSelection selection = new StringSelection(textToCopy.toString());
				getClipboard().setContents(selection, this);	
			}
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		public void lostOwnership(Clipboard tclipboard, Transferable contents) {
			// Nothing here			
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();

			ModelItem[] selectedItems = modelTable.getSelectedItems();

			if (selectedItems.length > 0) {
				add(copyToClipboardMenu);
				addSeparator();
			}

			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				add(restartSelectedDownloadsItem);
				addSeparator();
			}

			JMenu enabledSubMenu = new JMenu(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads") + "...");
			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				enabledSubMenu.add(enableSelectedDownloadsItem);
				enabledSubMenu.add(disableSelectedDownloadsItem);
				enabledSubMenu.add(invertEnabledSelectedItem);
				enabledSubMenu.addSeparator();
			}
			enabledSubMenu.add(enableAllDownloadsItem);
			enabledSubMenu.add(disableAllDownloadsItem);
			enabledSubMenu.add(invertEnabledAllItem);
			add(enabledSubMenu);

			JMenu removeSubMenu = new JMenu(language.getString("DownloadPane.fileTable.popupmenu.remove") + "...");
			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				removeSubMenu.add(removeSelectedDownloadsItem);
			}
			removeSubMenu.add(removeAllDownloadsItem);
			add(removeSubMenu);

			addSeparator();
			add(removeFinishedItem);
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}
	}

	private class Listener
		extends MouseAdapter
		implements LanguageListener, ActionListener, KeyListener, MouseListener, PropertyChangeListener {

        public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == downloadTextField) {
				downloadTextField_actionPerformed(e);
			}
            else if (e.getSource() == downloadActivateButton) {
                downloadActivateButtonPressed(e);
            }
            else if (e.getSource() == downloadPauseButton) {
                downloadPauseButtonPressed(e);
            }
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getSource() == modelTable.getTable()) {
				downloadTable_keyPressed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
			// Nothing here	
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getSource() == modelTable.getTable()) {
					// Start file from download table. Is this a good idea?
					downloadTableDoubleClick(e);
				}
			} else if (e.isPopupTrigger()) {
				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showDownloadTablePopupMenu(e);
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showDownloadTablePopupMenu(e);
				}

			}
		}
		
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_NAME)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_SIZE)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_STYLE)) {
				fontChanged();
			}
		}
		
	}

	private PopupMenuDownload popupMenuDownload = null;

	private Listener listener = new Listener();
	
	private static Logger logger = Logger.getLogger(DownloadPanel.class.getName());

	private DownloadModel model = null;

	private SettingsClass settingsClass = null;

	private Language language = null;

	private JPanel downloadTopPanel = new JPanel();
	private JButton downloadActivateButton =
        new JButton(new ImageIcon(getClass().getResource("/data/down_selected.gif")));
    private JButton downloadPauseButton =
        new JButton(new ImageIcon(getClass().getResource("/data/down.gif")));
	private JTextField downloadTextField = new JTextField(25);
	private JLabel downloadItemCountLabel = new JLabel();
	private SortedModelTable modelTable;

	private boolean initialized = false;

	private String fileSeparator = System.getProperty("file.separator");
	private boolean downloadingActivated = false;
	private long downloadItemCount = 0;

	/**
	 * @param settingsClass
	 */
	public DownloadPanel(SettingsClass settingsClass) {
		super();
		this.settingsClass = settingsClass;
		settingsClass.addUpdater(this);
		
		language = Language.getInstance();
		language.addLanguageListener(listener);
	}
    
    /** 
     * This Document changes all newlines in the text into semicolons.
     * Needed if the user pastes multiple download keys, each on a line,
     * into the download text field.
     */ 
    protected class HandleMultiLineKeysDocument extends PlainDocument {
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            str = str.replace('\n', ';');
            str = str.replace('\r', ' ');
            super.insertString(offs, str, a);
            // TODO: maybe show chooser dialog and let user choose what files to download...
        }
    }

	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			//create the top panel
			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(downloadActivateButton, "/data/down_selected_rollover.gif"); // play_rollover
			toolkit.configureButton(downloadPauseButton, "/data/down_rollover.gif"); // pause_rollover
			
			new TextComponentClipboardMenu(downloadTextField, language);

			BoxLayout dummyLayout = new BoxLayout(downloadTopPanel, BoxLayout.X_AXIS);
			downloadTopPanel.setLayout(dummyLayout);
			downloadTextField.setMaximumSize(downloadTextField.getPreferredSize());
            downloadTextField.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.addKeys"));
            downloadTextField.setDocument(new HandleMultiLineKeysDocument());
                
			downloadTopPanel.add(downloadTextField); //Download/Quickload
			downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			downloadTopPanel.add(downloadActivateButton); //Download/Start transfer
			downloadTopPanel.add(downloadPauseButton); //Download/Start transfer
			downloadTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
			downloadTopPanel.add(Box.createHorizontalGlue());
			downloadTopPanel.add(downloadItemCountLabel);

			// create the main download panel
			DownloadTableFormat tableFormat = new DownloadTableFormat();

			modelTable = new SortedModelTable(model, tableFormat);
			setLayout(new BorderLayout());
			add(downloadTopPanel, BorderLayout.NORTH);
			add(modelTable.getScrollPane(), BorderLayout.CENTER);
			fontChanged();

			// listeners
			downloadTextField.addActionListener(listener);
			downloadActivateButton.addActionListener(listener);
			downloadPauseButton.addActionListener(listener);
			modelTable.getScrollPane().addMouseListener(listener);
			modelTable.getTable().addKeyListener(listener);
			modelTable.getTable().addMouseListener(listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

			//Settings
			setDownloadingActivated(settingsClass.getBoolValue(SettingsClass.DOWNLOADING_ACTIVATED));

			initialized = true;
		}
	}

	private Dimension calculateLabelSize(String text) {
		JLabel dummyLabel = new JLabel(text);
		dummyLabel.doLayout();
		return dummyLabel.getPreferredSize();
	}

	private void refreshLanguage() {
		downloadActivateButton.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.activateDownloading"));
        downloadPauseButton.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.pauseDownloading"));
        downloadTextField.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.addKeys"));

		String waiting = language.getString("DownloadPane.toolbar.waiting");
		Dimension labelSize = calculateLabelSize(waiting + " : 00000");
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		downloadItemCountLabel.setText(waiting + " : " + downloadItemCount);
	}

	public void setModel(DownloadModel model) {
		this.model = model;
	}

	/**
	 * Configures a CheckBox to be a default icon CheckBox.
     *  
     * Was used when we used a single icon for download start/pause!!!
     * This is here to keep this code for future use.
     * 
	 * @param checkBox The new icon CheckBox
	 * @param rolloverIcon Displayed when mouse is over the CheckBox
	 * @param selectedIcon Displayed when CheckBox is checked
	 * @param rolloverSelectedIcon Displayed when mouse is over the selected CheckBox
	 */
//	private void configureCheckBox(
//		JCheckBox checkBox,
//		String rolloverIcon,
//		String selectedIcon,
//		String rolloverSelectedIcon) {
//
//		checkBox.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
//		checkBox.setSelectedIcon(new ImageIcon(getClass().getResource(selectedIcon)));
//		checkBox.setRolloverSelectedIcon(
//			new ImageIcon(getClass().getResource(rolloverSelectedIcon)));
//		checkBox.setMargin(new Insets(0, 0, 0, 0));
//		checkBox.setFocusPainted(false);
//	}

    public static void main(String[] args) {
        System.out.println("keys\ndada;2222".split("[;\n]").length);
    }
    
	/**
	 * downloadTextField Action Listener (Download/Quickload)
     * The textfield can contain 1 key to download or multiple keys separated by ';'.
	 */
	private void downloadTextField_actionPerformed(ActionEvent e) {

		String keys = downloadTextField.getText().trim();
        
        if( keys.length() == 0 ) {
            downloadTextField.setText("");
            return;
        }
        
        String[] keyList = keys.split("[;\n]");
        if( keyList == null || keyList.length == 0 ) {
            downloadTextField.setText("");
            return;
        }
        
        for(int x=0; x < keyList.length; x++) {
            String key = keyList[x].trim();
            if( key.length() == 0 ) {
                continue;
            }
            
            // strip the 'browser' prefix
            String stripMe = "http://127.0.0.1:8888/";
            if (key.startsWith(stripMe)) {
                key = key.substring(stripMe.length());
            }
            // strip the 'freenet:' prefix
            stripMe = "freenet:";
            if (key.startsWith(stripMe)) {
                key = key.substring(stripMe.length());
            }
            
            if( key.length() < 5 ) {
                // at least the SSK@? is needed
                showInvalidKeyErrorDialog(key);
                continue;
            }

            String validkeys[] = { "SSK@", "CHK@", "KSK@" };
            int keyType = -1; // invalid

            for (int i = 0; i < validkeys.length; i++) {
                if (key.startsWith(validkeys[i])) {
                    keyType = i;
                    break;
                }
            }
            // hack: support USK keys for 07 only
            if( keyType < 0 && FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
                if( key.startsWith("USK@") ) {
                    keyType = 3; // hack
                }
            }
            
            if( keyType < 0 ) {
                showInvalidKeyErrorDialog(key);
                continue;
            }

            // added a way to specify a file name. The filename is preceeded by a colon.
            String fileName;

            int sepIndex = key.lastIndexOf(":");

            if (sepIndex > -1) {
                fileName = key.substring(sepIndex + 1);
                key = key.substring(0, sepIndex);
            }
            // take the filename from the last part the SSK or KSK
            else if ( (sepIndex = key.lastIndexOf("/")) > -1 ) {
                fileName = key.substring(sepIndex + 1);
            } else {
                fileName = key.substring(4);
            }
            //  zab, why did you comment this out, its needed, because otherwise you
            //  use a wrong CHK key for download! i pasted a CHK@afcdf432dk/mytargetfilename.data
            //FIXED: it happened when sha1 was still kept in this variable - and sha1 can contain "/"

            // remove filename from key for CHK
            if (keyType == 1 && key.indexOf("/") > -1 ) { // CHK?
                key = key.substring(0, key.indexOf("/"));
            }

            // add valid key to download table
            FrostDownloadItem dlItem = new FrostDownloadItem(fileName, key, null);
            //users weren't happy with '_'
            /*boolean isAdded =*/ model.addDownloadItem(dlItem);
        }
        downloadTextField.setText("");
	}
    
    private void showInvalidKeyErrorDialog(String invKey) {
        JOptionPane.showMessageDialog(
                this,
                "<html>"+ // needed for linebreak in dialog
                language.getString("DownloadPane.invalidKeyDialog.body")
                    + " SSK@, KSK@, CHK@ :<br>"+invKey+"</html>",
                language.getString("DownloadPane.invalidKeyDialog.title"),
                JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Get keyTyped for downloadTable
	 * @param e
	 */
	private void downloadTable_keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}
	}

	public boolean isDownloadingActivated() {
		return downloadingActivated;
	}

	public void setDownloadingActivated(boolean b) {
		downloadingActivated = b;
        
        downloadActivateButton.setEnabled(!downloadingActivated);
        downloadPauseButton.setEnabled(downloadingActivated);
	}

	public void setDownloadItemCount(long newDownloadItemCount) {
		downloadItemCount = newDownloadItemCount;

		String s =
			new StringBuffer()
				.append(language.getString("DownloadPane.fileTable.states.waiting"))
				.append(" : ")
				.append(downloadItemCount)
				.toString();
		downloadItemCountLabel.setText(s);
	}

	private PopupMenuDownload getPopupMenuDownload() {
		if (popupMenuDownload == null) {
			popupMenuDownload = new PopupMenuDownload();
			language.addLanguageListener(popupMenuDownload);
		}
		return popupMenuDownload;
	}

	private void showDownloadTablePopupMenu(MouseEvent e) {
		getPopupMenuDownload().show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void fontChanged() {
		String fontName = settingsClass.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		int fontStyle = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		int fontSize = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" +
						   "That selection will be changed to \"SansSerif\".");
			settingsClass.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
			font = new Font("SansSerif", fontStyle, fontSize);
		}
		modelTable.setFont(font);
	}

	private void downloadActivateButtonPressed(ActionEvent e) {
		setDownloadingActivated(true);
	}
    
    private void downloadPauseButtonPressed(ActionEvent e) {
        setDownloadingActivated(false);
    }

	private void downloadTableDoubleClick(MouseEvent e) {
		int clickedCol = modelTable.getTable().columnAtPoint(e.getPoint());
		int modelIx = modelTable.getTable().getColumnModel().getColumn(clickedCol).getModelIndex();
		if (modelIx == 0) {
			return;
		}

		ModelItem selectedItem = modelTable.getSelectedItem();
		if (selectedItem != null) {
			FrostDownloadItem dlItem = (FrostDownloadItem) selectedItem;
			String execFilename =
				new StringBuffer()
					.append(System.getProperty("user.dir"))
					.append(fileSeparator)
					.append(settingsClass.getValue("downloadDirectory"))
					.append(dlItem.getFileName())
					.toString();
			File file = new File(execFilename);
			logger.info("Executing: " + file.getPath());
			if (file.exists()) {
                try {
                    Execute.simple_run(new String[] {"exec.bat", file.getPath()} );
                } catch(Throwable t) {
                    JOptionPane.showMessageDialog(this,
                            "Could not open the file: "+file.getName()+"\n"+t.toString(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);            
                }
			}
		}
	}

	/* (non-Javadoc)
	 * @see frost.SettingsUpdater#updateSettings()
	 */
	public void updateSettings() {
		settingsClass.setValue(SettingsClass.DOWNLOADING_ACTIVATED, isDownloadingActivated());
	}
}
