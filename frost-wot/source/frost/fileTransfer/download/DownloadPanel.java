/*
 * Created on Nov 13, 2003
 *
 */
package frost.fileTransfer.download;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.*;

import frost.*;
import frost.ext.Execute;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.SortedModelTable;

/**
 * 
 */
public class DownloadPanel extends JPanel implements SettingsUpdater {
	/**
	 *  
	 */
	private class PopupMenuDownload
		extends JSkinnablePopupMenu
		implements ActionListener, LanguageListener {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyChkKeyAndFilenameToClipboardItem = new JMenuItem();
		private JMenuItem copyChkKeyToClipboardItem = new JMenuItem();
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

		/**
		 * 
		 */
		public PopupMenuDownload() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();

			// TODO: implement cancel of downloading

			copyToClipboardMenu.add(copyChkKeyToClipboardItem);
			copyToClipboardMenu.add(copyChkKeyAndFilenameToClipboardItem);

			copyChkKeyToClipboardItem.addActionListener(this);
			copyChkKeyAndFilenameToClipboardItem.addActionListener(this);
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
			restartSelectedDownloadsItem.setText(
				languageResource.getString("Restart selected downloads"));
			removeSelectedDownloadsItem.setText(
				languageResource.getString("Remove selected downloads"));
			removeAllDownloadsItem.setText(languageResource.getString("Remove all downloads"));
			//downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
			removeFinishedItem.setText(languageResource.getString("Remove finished downloads"));
			enableAllDownloadsItem.setText(languageResource.getString("Enable all downloads"));
			disableAllDownloadsItem.setText(languageResource.getString("Disable all downloads"));
			enableSelectedDownloadsItem.setText(
				languageResource.getString("Enable selected downloads"));
			disableSelectedDownloadsItem.setText(
				languageResource.getString("Disable selected downloads"));
			invertEnabledAllItem.setText(
				languageResource.getString("Invert enabled state for all downloads"));
			invertEnabledSelectedItem.setText(
				languageResource.getString("Invert enabled state for selected downloads"));
			cancelItem.setText(languageResource.getString("Cancel"));
			copyChkKeyToClipboardItem.setText(languageResource.getString("CHK key"));
			copyChkKeyAndFilenameToClipboardItem.setText(
				languageResource.getString("CHK key + filename"));

			copyToClipboardMenu.setText(languageResource.getString("Copy to clipboard") + "...");
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyChkKeyToClipboardItem) {
				copyChkKeyToClipboard();
			}
			if (e.getSource() == copyChkKeyAndFilenameToClipboardItem) {
				copyChkKeyAndFilenameToClipboard();
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

		/**
		 * 
		 */
		private void invertEnabledSelected() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(null, selectedItems);
		}

		/**
		 * 
		 */
		private void invertEnabledAll() {
			model.setAllItemsEnabled(null);
		}

		/**
		 * 
		 */
		private void disableSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(new Boolean(false), selectedItems);
		}

		/**
		 * 
		 */
		private void enableSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.setItemsEnabled(new Boolean(true), selectedItems);
		}

		/**
		 * 
		 */
		private void disableAllDownloads() {
			model.setAllItemsEnabled(new Boolean(false));
		}

		/**
		 * 
		 */
		private void enableAllDownloads() {
			model.setAllItemsEnabled(new Boolean(true));
		}

		/**
		 * 
		 */
		private void removeFinished() {
			model.removeFinishedDownloads();
		}

		/**
		 * 
		 */
		private void removeAllDownloads() {
			model.removeAllItems();
		}

		/**
		 * 
		 */
		private void removeSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}

		/**
		 * 
		 */
		private void restartSelectedDownloads() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.restartItems(selectedItems);
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			ModelItem selectedItem = modelTable.getSelectedItem();
			if (selectedItem != null) {
				FrostDownloadItem dlItem = (FrostDownloadItem) selectedItem;
				String chkKey = dlItem.getKey();
				String filename = dlItem.getFileName();
				if (chkKey != null && filename != null) {
					Mixed.setSystemClipboard(chkKey + "/" + filename);
				}
			}
		}

		/**
		 * add CHK key to clipboard
		 */
		private void copyChkKeyToClipboard() {
			ModelItem selectedItem = modelTable.getSelectedItem();
			if (selectedItem != null) {
				FrostDownloadItem dlItem = (FrostDownloadItem) selectedItem;
				String chkKey = dlItem.getKey();
				if (chkKey != null) {
					Mixed.setSystemClipboard(chkKey);
				}
			}
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();

			ModelItem[] selectedItems = modelTable.getSelectedItems();

			if (selectedItems.length == 1) {
				// If 1 item is selected
				FrostDownloadItem item = (FrostDownloadItem) selectedItems[0];
				if (item.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				add(restartSelectedDownloadsItem);
				addSeparator();
			}

			JMenu enabledSubMenu =
				new JMenu(languageResource.getString("Enable downloads") + "...");
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

			JMenu removeSubMenu = new JMenu(languageResource.getString("Remove") + "...");
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

	/**
	 * 
	 */
	private class Listener
		extends MouseAdapter
		implements LanguageListener, ActionListener, KeyListener, MouseListener, PropertyChangeListener {
		/**
		 * 
		 */
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

	private UpdatingLanguageResource languageResource = null;

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
	 * 
	 */
	public DownloadPanel(SettingsClass newSettingsClass) {
		super();
		settingsClass = newSettingsClass;
		settingsClass.addUpdater(this);
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			//create the top panel
			configureButton(
				downloadActivateButton,
				"/data/down_selected_rollover.gif"); // play_rollover
            configureButton(
                downloadPauseButton,
               "/data/down_rollover.gif"); // pause_rollover
                
			BoxLayout dummyLayout = new BoxLayout(downloadTopPanel, BoxLayout.X_AXIS);
			downloadTopPanel.setLayout(dummyLayout);
			downloadTextField.setMaximumSize(downloadTextField.getPreferredSize());
			downloadTopPanel.add(downloadTextField); //Download/Quickload
			downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			downloadTopPanel.add(downloadActivateButton); //Download/Start transfer
            downloadTopPanel.add(downloadPauseButton); //Download/Start transfer
			downloadTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
			downloadTopPanel.add(Box.createHorizontalGlue());
			downloadTopPanel.add(downloadItemCountLabel);

			// create the main download panel
			DownloadTableFormat tableFormat = new DownloadTableFormat(languageResource);
			
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

	/**
	 * 
	 */
	private void refreshLanguage() {
		downloadActivateButton.setToolTipText(languageResource.getString("Activate downloading"));
        downloadPauseButton.setToolTipText(languageResource.getString("Pause downloading"));

		String waiting = languageResource.getString("Waiting");
		Dimension labelSize = calculateLabelSize(waiting + " : 00000");
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		downloadItemCountLabel.setText(waiting + " : " + downloadItemCount);
	}

	/**
	 * @param model
	 */
	public void setModel(DownloadModel model) {
		this.model = model;
	}

	/**
	 * @param bundle
	 */
	public void setLanguageResource(UpdatingLanguageResource newLanguageResource) {
		if (languageResource != null) {
			languageResource.removeLanguageListener(listener);
		}
		languageResource = newLanguageResource;
		languageResource.addLanguageListener(listener);
	}

	/**
	 * Configures a CheckBox to be a default icon CheckBox
	 * @param checkBox The new icon CheckBox
	 * @param rolloverIcon Displayed when mouse is over the CheckBox
	 * @param selectedIcon Displayed when CheckBox is checked
	 * @param rolloverSelectedIcon Displayed when mouse is over the selected CheckBox
	 */
	private void configureCheckBox(
		JCheckBox checkBox,
		String rolloverIcon,
		String selectedIcon,
		String rolloverSelectedIcon) {

		checkBox.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
		checkBox.setSelectedIcon(new ImageIcon(getClass().getResource(selectedIcon)));
		checkBox.setRolloverSelectedIcon(
			new ImageIcon(getClass().getResource(rolloverSelectedIcon)));
		checkBox.setMargin(new Insets(0, 0, 0, 0));
		checkBox.setFocusPainted(false);
	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	private void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}

	/**
	 * downloadTextField Action Listener (Download/Quickload)
	 */

	private void downloadTextField_actionPerformed(ActionEvent e) {
		String key = (downloadTextField.getText()).trim();
		if (key.length() > 0) {
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

			String validkeys[] = { "SSK@", "CHK@", "KSK@" };
			int keyType = -1; // invalid

			//TODO: Improve parsing here. If length < 3, an ArrayIndexOutOfBounds exception is launched.
			for (int i = 0; i < validkeys.length; i++) {
				if (key.substring(0, validkeys[i].length()).equals(validkeys[i])) {
					keyType = i;
					break;
				}
			}

			if (keyType > -1) {
				// added a way to specify a file name. The filename is preceeded by a colon.
				String fileName;

				int sepIndex = key.lastIndexOf(":");

				if (sepIndex != -1) {
					fileName = key.substring(sepIndex + 1);
					key = key.substring(0, sepIndex);
				}
				// take the filename from the last part the SSK or KSK
				else if (-1 != (sepIndex = key.lastIndexOf("/"))) {
					fileName = key.substring(sepIndex + 1);
				} else {
					fileName = key.substring(4);
				}
				//  zab, why did you comment this out, its needed, because otherwise you
				//  use a wrong CHK key for download! i pasted a CHK@afcdf432dk/mytargetfilename.data
				//FIXED: it happened when sha1 was still kept in this variable - and sha1 can contain "/"

				// remove filename from key for CHK
				if (keyType == 1) // CHK?
					key = key.substring(0, key.indexOf("/"));

				// add valid key to download table
				FrostDownloadItem dlItem = new FrostDownloadItem(fileName, key, null);
				//users weren't happy with '_'
				boolean isAdded = model.addDownloadItem(dlItem);

				if (isAdded == true)
					downloadTextField.setText("");
			} else {
				// show messagebox that key is invalid
				String keylist = "";
				for (int i = 0; i < validkeys.length; i++) {
					if (i > 0)
						keylist += ", ";
					keylist += validkeys[i];
				}
				JOptionPane.showMessageDialog(
					this,
					languageResource.getString("Invalid key.  Key must begin with one of")
						+ ": "
						+ keylist,
					languageResource.getString("Invalid key"),
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**Get keyTyped for downloadTable*/
	private void downloadTable_keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}
	}

	/**
	 * @return
	 */
	public boolean isDownloadingActivated() {
		return downloadingActivated;
	}

	/**
	 * @param b
	 */
	public void setDownloadingActivated(boolean b) {
		downloadingActivated = b;
        
        downloadActivateButton.setEnabled(!downloadingActivated);
        downloadPauseButton.setEnabled(downloadingActivated);
	}

	/**
	 * @param l
	 */
	public void setDownloadItemCount(long newDownloadItemCount) {
		downloadItemCount = newDownloadItemCount;

		String s =
			new StringBuffer()
				.append(languageResource.getString("Waiting"))
				.append(" : ")
				.append(downloadItemCount)
				.toString();
		downloadItemCountLabel.setText(s);
	}

	/**
	 * @return
	 */
	private PopupMenuDownload getPopupMenuDownload() {
		if (popupMenuDownload == null) {
			popupMenuDownload = new PopupMenuDownload();
			languageResource.addLanguageListener(popupMenuDownload);
		}
		return popupMenuDownload;
	}

	private void showDownloadTablePopupMenu(MouseEvent e) {
		getPopupMenuDownload().show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * 
	 */
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

	/**
	 * @param e
	 */
	private void downloadActivateButtonPressed(ActionEvent e) {
		setDownloadingActivated(true);
	}
    
    private void downloadPauseButtonPressed(ActionEvent e) {
        setDownloadingActivated(false);
    }

	/**
	 * @param e
	 */
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
				Execute.run("exec.bat" + " \"" + file.getPath() + "\"");
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
