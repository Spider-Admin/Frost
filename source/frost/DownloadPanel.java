/*
 * Created on Nov 13, 2003
 *
 */
package frost;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import frost.gui.*;
import frost.gui.model.DownloadTableModel;
import frost.gui.objects.FrostDownloadItemObject;
import frost.gui.translation.*;

/**
 * 
 */
public class DownloadPanel extends JPanel {
	/**
	 * 
	 */
	private class PopupMenuDownload
		extends JPopupMenu
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
			downloadTable.setDownloadEnabled(2, false);
			// 2=invert , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void invertEnabledAll() {
			downloadTable.setDownloadEnabled(2, true);
			// 2=invert , true means ALL in table!
		}

		/**
		 * 
		 */
		private void disableSelectedDownloads() {
			downloadTable.setDownloadEnabled(0, false);
			// 0=disabled , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void enableSelectedDownloads() {
			downloadTable.setDownloadEnabled(1, false);
			// 1=enabled , false means SELECTED in table!
		}

		/**
		 * 
		 */
		private void disableAllDownloads() {
			downloadTable.setDownloadEnabled(0, true);
			// 0=disabled , true means ALL in table!
		}

		/**
		 * 
		 */
		private void enableAllDownloads() {
			downloadTable.setDownloadEnabled(1, true);
			// 1=enabled , true means ALL in table!	
		}

		/**
		 * 
		 */
		private void removeFinished() {
			downloadTable.removeFinishedDownloads();
		}

		/**
		 * 
		 */
		private void removeAllDownloads() {
			downloadTable.removeAllItemsFromTable();
		}

		/**
		 * 
		 */
		private void removeSelectedDownloads() {
			downloadTable.removeSelectedItemsFromTable();
		}

		/**
		 * 
		 */
		private void restartSelectedDownloads() {
			downloadTable.restartSelectedDownloads();
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			DownloadTableModel tableModel = (DownloadTableModel) downloadTable.getModel();
			int selectedRow = downloadTable.getSelectedRow();
			if (selectedRow > -1) {
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject) tableModel.getRow(selectedRow);
				String chkKey = dlItem.getKey();
				String filename = dlItem.getFileName();
				if (chkKey != null && filename != null) {
					mixed.setSystemClipboard(chkKey + "/" + filename);
				}
			}
		}

		/**
		 * add CHK key to clipboard
		 */
		private void copyChkKeyToClipboard() {
			DownloadTableModel tableModel = (DownloadTableModel) downloadTable.getModel();
			int selectedRow = downloadTable.getSelectedRow();
			if (selectedRow > -1) {
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject) tableModel.getRow(selectedRow);
				String chkKey = dlItem.getKey();
				if (chkKey != null) {
					mixed.setSystemClipboard(chkKey);
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

			if (downloadTable.getSelectedRowCount() == 1) {
				// if 1 item is selected
				FrostDownloadItemObject dlItem =
					(FrostDownloadItemObject)
						((DownloadTableModel) downloadTable.getModel()).getRow(
						downloadTable.getSelectedRow());
				if (dlItem.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			if (downloadTable.getSelectedRow() > -1) {
				add(restartSelectedDownloadsItem);
				addSeparator();
			}

			JMenu enabledSubMenu =
				new JMenu(languageResource.getString("Enable downloads") + "...");
			if (downloadTable.getSelectedRow() > -1) {
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
			if (downloadTable.getSelectedRow() > -1) {
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
	public class Listener implements LanguageListener, ActionListener, KeyListener, MouseListener, ItemListener {
		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == downloadTextField) {
				downloadTextField_actionPerformed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getSource() == downloadTable) {
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
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			// Nothing here				
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			// Nothing here			
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			// Nothing here				
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == downloadTable) ||
					   (e.getSource() == downloadTableScrollPane)) {
					showDownloadTablePopupMenu(e);
				}

			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
				
				if ((e.getSource() == downloadTable) ||
					   (e.getSource() == downloadTableScrollPane)) {
					showDownloadTablePopupMenu(e);
				}
				
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == downloadActivateCheckBox) {
				activateCheckBoxChanged(e);
			}
		}

	}
	
	private PopupMenuDownload popupMenuDownload = null;

	private Listener listener = new Listener();

	private DownloadTable downloadTable = null;
	private HealingTable healingTable = null;

	private UpdatingLanguageResource languageResource = null;

	private JPanel downloadTopPanel = new JPanel();
	private JCheckBox downloadActivateCheckBox =
		new JCheckBox(new ImageIcon(getClass().getResource("/data/down.gif")), true);
	private JTextField downloadTextField = new JTextField(25);
	private JButton downloadShowHealingInfo =
		new JButton(new ImageIcon(getClass().getResource("/data/healinginfo.gif")));
	private JLabel downloadItemCountLabel = new JLabel();
	private JScrollPane downloadTableScrollPane = null;

	private boolean initialized = false;

	private boolean downloadingActivated = false;
	private long downloadItemCount = 0;

	/**
	 * 
	 */
	public DownloadPanel() {
		super();
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			//create the top panel
			configureCheckBox(
				downloadActivateCheckBox,
				"/data/down_rollover.gif",
				"/data/down_selected.gif",
				"/data/down_selected_rollover.gif");
			configureButton(downloadShowHealingInfo, "/data/healinginfo_rollover.gif");
			downloadShowHealingInfo.setEnabled(false); // disabled until implemented ;)

			BoxLayout dummyLayout = new BoxLayout(downloadTopPanel, BoxLayout.X_AXIS);
			downloadTopPanel.setLayout(dummyLayout);
			downloadTextField.setMaximumSize(downloadTextField.getPreferredSize());
			downloadTopPanel.add(downloadTextField); //Download/Quickload
			downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			downloadTopPanel.add(downloadActivateCheckBox); //Download/Start transfer
			downloadTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			downloadTopPanel.add(downloadShowHealingInfo);
			downloadTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
			downloadTopPanel.add(Box.createHorizontalGlue());

			String waiting = languageResource.getString("Waiting");
			Dimension labelSize = calculateLabelSize(waiting + " : 00000");
			downloadItemCountLabel.setPreferredSize(labelSize);
			downloadItemCountLabel.setMinimumSize(labelSize);
			downloadItemCountLabel.setText(waiting + " : 0");
			downloadTopPanel.add(downloadItemCountLabel);

			// create the main download panel
			downloadTableScrollPane = new JScrollPane(downloadTable);
			setLayout(new BorderLayout());
			add(downloadTopPanel, BorderLayout.NORTH);
			add(downloadTableScrollPane, BorderLayout.CENTER);

			// listeners
			downloadTable.addKeyListener(listener);
			downloadTextField.addActionListener(listener);
			downloadActivateCheckBox.addItemListener(listener);
			downloadTableScrollPane.addMouseListener(listener);

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
		downloadActivateCheckBox.setToolTipText(languageResource.getString("Activate downloading"));
		downloadShowHealingInfo.setToolTipText(
			languageResource.getString("Show healing information"));
			
		String waiting = languageResource.getString("Waiting");
		Dimension labelSize = calculateLabelSize(waiting + " : 00000");
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		String s =
			new StringBuffer()
				.append(waiting)
				.append(" : ")
				.append(downloadItemCount)
				.toString();
		downloadItemCountLabel.setText(s);
	}

	/**
	 * description
	 * 
	 * @param downloadTable description
	 */
	public void setDownloadTable(DownloadTable newDownloadTable) {
		if (downloadTable != null) {
			downloadTable.removeKeyListener(listener);
			downloadTable.removeMouseListener(listener);
		}
		downloadTable = newDownloadTable;
		downloadTable.addKeyListener(listener);
		downloadTable.addMouseListener(listener);
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

		checkBox.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
		checkBox.setSelectedIcon(new ImageIcon(frame1.class.getResource(selectedIcon)));
		checkBox.setRolloverSelectedIcon(
			new ImageIcon(frame1.class.getResource(rolloverSelectedIcon)));
		checkBox.setMargin(new Insets(0, 0, 0, 0));
		checkBox.setFocusPainted(false);
	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	private void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
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
				FrostDownloadItemObject dlItem = new FrostDownloadItemObject(fileName, key, null);
				//users weren't happy with '_'
				boolean isAdded = downloadTable.addDownloadItem(dlItem);

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
	public void downloadTable_keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == KeyEvent.VK_DELETE && !downloadTable.isEditing()) {
			downloadTable.removeSelectedChunks();
			downloadTable.removeSelectedRows();
		}
	}

	/**
	 * @param table
	 */
	public void setHealingTable(HealingTable newHealingTable) {
		healingTable = newHealingTable;
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
		downloadActivateCheckBox.setSelected(downloadingActivated);
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
	 * @param e
	 */
	private void activateCheckBoxChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			downloadingActivated = true;
		} else {
			downloadingActivated = false;
		}
			
	}

}
