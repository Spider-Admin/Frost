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
import javax.swing.table.*;
import javax.swing.text.*;

import frost.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.common.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class DownloadPanel extends JPanel implements SettingsUpdater {
	
	private PopupMenuDownload popupMenuDownload = null;

	private Listener listener = new Listener();
	
	private static Logger logger = Logger.getLogger(DownloadPanel.class.getName());

	private DownloadModel model = null;

	private Language language = null;

	private JPanel downloadTopPanel = new JPanel();
	private JButton downloadActivateButton = new JButton(new ImageIcon(getClass().getResource("/data/down_selected.gif")));
    private JButton downloadPauseButton = new JButton(new ImageIcon(getClass().getResource("/data/down.gif")));
	private JTextField downloadTextField = new JTextField(25);
	private JLabel downloadItemCountLabel = new JLabel();
	private SortedModelTable modelTable;

	private boolean initialized = false;

	private boolean downloadingActivated = false;
	private int downloadItemCount = 0;

	public DownloadPanel() {
		super();
        Core.frostSettings.addUpdater(this);
		
		language = Language.getInstance();
		language.addLanguageListener(listener);
	}
    
    public DownloadTableFormat getTableFormat() {
        return (DownloadTableFormat) modelTable.getTableFormat();
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
            
            modelTable.getTable().setDefaultRenderer(Object.class, new CellRenderer());

			// listeners
			downloadTextField.addActionListener(listener);
			downloadActivateButton.addActionListener(listener);
			downloadPauseButton.addActionListener(listener);
			modelTable.getScrollPane().addMouseListener(listener);
			modelTable.getTable().addKeyListener(listener);
			modelTable.getTable().addMouseListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

			//Settings
			setDownloadingActivated(Core.frostSettings.getBoolValue(SettingsClass.DOWNLOADING_ACTIVATED));

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
		Dimension labelSize = calculateLabelSize(waiting + ": 00000");
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		downloadItemCountLabel.setText(waiting + ": " + downloadItemCount);
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

	/**
	 * downloadTextField Action Listener (Download/Quickload)
     * The textfield can contain 1 key to download or multiple keys separated by ';'.
	 */
	private void downloadTextField_actionPerformed(ActionEvent e) {
        
        // FIXME: show dialog with all keys like fuqid
        
        try {
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
    
                if( key.length() < 5 ) {
                    continue;
                }

                // maybe convert html codes (e.g. %2c -> , )
                if( key.indexOf("%") > 0 ) {
                    try {
                        key = java.net.URLDecoder.decode(key, "UTF-8");
                    } catch (java.io.UnsupportedEncodingException ex) {
                        logger.log(Level.SEVERE, "Decode of HTML code failed", ex);
                    }
                }

                // find key type (chk,ssk,...)
                int pos = -1;
                for( int i = 0; i < FreenetKeys.getFreenetKeyTypes().length; i++ ) {
                    String string = FreenetKeys.getFreenetKeyTypes()[i];
                    pos = key.indexOf(string);
                    if( pos >= 0 ) {
                        break;
                    }
                }
                if( pos < 0 ) {
                    // no valid keytype found
                    showInvalidKeyErrorDialog(key);
                    continue;
                }
    
                // strip all before key type
                if( pos > 0 ) {
                    key = key.substring(pos);
                }

                if( key.length() < 5 ) {
                    // at least the SSK@? is needed
                    showInvalidKeyErrorDialog(key);
                    continue;
                }
                
                // take the filename from the last part of the key
                String fileName;
                int sepIndex = key.lastIndexOf("/");
                if ( sepIndex > -1 ) {
                    fileName = key.substring(sepIndex + 1);
                } else {
                    // fallback: use key as filename
                    fileName = key.substring(4);
                }

                String checkKey = key;
                // remove filename from CHK key
                if (key.startsWith("CHK@") && key.indexOf("/") > -1 ) {
                    checkKey = key.substring(0, key.indexOf("/"));
                }

                // On 0.7 we remember the full provided download uri as key.
                // If the node reports download failed, error code 11 later, then we strip the filename
                // from the uri and keep trying with chk only
                if( FcpHandler.getInitializedVersion() != FcpHandler.FREENET_07 ) {
                    key = checkKey; // on 0.5 use only key as uri
                }
                
                // finally check if the key is valid for this network
                if( !FreenetKeys.isValidKey(checkKey) ) {
                    showInvalidKeyErrorDialog(key);
                    continue;
                }

                // add valid key to download table
                FrostDownloadItem dlItem = new FrostDownloadItem(fileName, key);
                model.addDownloadItem(dlItem); // false if file is already in table
            }
        } catch(Throwable ex) {
            logger.log(Level.SEVERE, "Unexpected exception", ex);
            showInvalidKeyErrorDialog("???");
        }
        downloadTextField.setText("");
	}
    
    private void showInvalidKeyErrorDialog(String invKey) {
        JOptionPane.showMessageDialog(
                this,
                language.formatMessage("DownloadPane.invalidKeyDialog.body", invKey),
                language.getString("DownloadPane.invalidKeyDialog.title"),
                JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Get keyTyped for downloadTable
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

	public void setDownloadItemCount(int newDownloadItemCount) {
		downloadItemCount = newDownloadItemCount;

		String s =
			new StringBuffer()
				.append(language.getString("DownloadPane.toolbar.waiting"))
				.append(": ")
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
        // select row where rightclick occurred if row under mouse is NOT selected 
        Point p = e.getPoint();
        int y = modelTable.getTable().rowAtPoint(p);
        if( y < 0 ) {
            return;
        }
        if( !modelTable.getTable().getSelectionModel().isSelectedIndex(y) ) {
            modelTable.getTable().getSelectionModel().setSelectionInterval(y, y);
        }
		getPopupMenuDownload().show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void fontChanged() {
		String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" +
						   "That selection will be changed to \"SansSerif\".");
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
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
            File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFileName());
            if( !targetFile.isFile() ) {
                return;
            }
			logger.info("Executing: " + targetFile.getAbsolutePath());
            try {
                Execute.simple_run(new String[] {"exec.bat", targetFile.getAbsolutePath()} );
            } catch(Throwable t) {
                JOptionPane.showMessageDialog(this,
                        "Could not open the file: "+targetFile.getAbsolutePath()+"\n"+t.toString(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);            
            }
		}
	}
    
	/* (non-Javadoc)
	 * @see frost.SettingsUpdater#updateSettings()
	 */
	public void updateSettings() {
        Core.frostSettings.setValue(SettingsClass.DOWNLOADING_ACTIVATED, isDownloadingActivated());
	}
    
    /**
     * Renderer draws background of DONE items in green.
     */
    private class CellRenderer extends DefaultTableCellRenderer {

        private final Color col_green    = new Color(0x00, 0x80, 0x00);

        public CellRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            super.getTableCellRendererComponent(table, value, isSelected, /*hasFocus*/ false, row, column);
            
            FrostDownloadItem item = (FrostDownloadItem)model.getItemAt(row);

            // set background of DONE downloads green
            if( item.getState() == FrostDownloadItem.STATE_DONE ) {
                setBackground(col_green);
            } else {
                setBackground(modelTable.getTable().getBackground());
            }

            return this;
        }
    }
    
    private class PopupMenuDownload extends JSkinnablePopupMenu
    implements ActionListener, LanguageListener, ClipboardOwner {

        private JMenuItem detailsItem = new JMenuItem();
        private JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private JMenuItem copyKeysItem = new JMenuItem();
        private JMenuItem copyExtendedInfoItem = new JMenuItem();
        private JMenuItem disableAllDownloadsItem = new JMenuItem();
        private JMenuItem disableSelectedDownloadsItem = new JMenuItem();
        private JMenuItem enableAllDownloadsItem = new JMenuItem();
        private JMenuItem enableSelectedDownloadsItem = new JMenuItem();
        private JMenuItem invertEnabledAllItem = new JMenuItem();
        private JMenuItem invertEnabledSelectedItem = new JMenuItem();
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
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05) {
                copyToClipboardMenu.add(copyKeysItem);
            }
            copyToClipboardMenu.add(copyExtendedInfoItem);
    
            copyKeysAndNamesItem.addActionListener(this);
            copyKeysItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            restartSelectedDownloadsItem.addActionListener(this);
            removeSelectedDownloadsItem.addActionListener(this);
            removeFinishedItem.addActionListener(this);
            enableAllDownloadsItem.addActionListener(this);
            disableAllDownloadsItem.addActionListener(this);
            enableSelectedDownloadsItem.addActionListener(this);
            disableSelectedDownloadsItem.addActionListener(this);
            invertEnabledAllItem.addActionListener(this);
            invertEnabledSelectedItem.addActionListener(this);
            detailsItem.addActionListener(this);
        }
    
        private void refreshLanguage() {
            keyNotAvailableMessage = language.getString("Common.copyToClipBoard.extendedInfo.keyNotAvailableYet");
            fileMessage = language.getString("Common.copyToClipBoard.extendedInfo.file")+" ";
            keyMessage = language.getString("Common.copyToClipBoard.extendedInfo.key")+" ";
            bytesMessage = language.getString("Common.copyToClipBoard.extendedInfo.bytes")+" ";
            
            detailsItem.setText(language.getString("Common.details"));
            copyKeysItem.setText(language.getString("Common.copyToClipBoard.copyKeysOnly"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            restartSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.restartSelectedDownloads"));
            removeSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.remove.removeSelectedDownloads"));
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
            if (e.getSource() == detailsItem) {
                showDetails();
            }
        }
        
        private void showDetails() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length != 1) {
                return;
            }
            FrostDownloadItem item = (FrostDownloadItem) selectedItems[0];
            if( !item.isSharedFile() ) {
                return;
            }
            new FileListFileDetailsDialog(MainFrame.getInstance()).startDialog(item.getFileListFileObject());
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
            model.setItemsEnabled(Boolean.FALSE, selectedItems);
        }
    
        private void enableSelectedDownloads() {
            ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(Boolean.TRUE, selectedItems);
        }
    
        private void disableAllDownloads() {
            model.setAllItemsEnabled(Boolean.FALSE);
        }
    
        private void enableAllDownloads() {
            model.setAllItemsEnabled(Boolean.TRUE);
        }
    
        private void removeFinished() {
            model.removeFinishedDownloads();
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
                    if( key.indexOf('/') < 0 ) {
                        textToCopy.append("/");
                        textToCopy.append(item.getFileName());
                    }
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
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
        
        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
        }
    
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
    
            if (selectedItems.length != 0) {
                // If at least 1 item is selected
                add(removeSelectedDownloadsItem);
            }
    
            addSeparator();
            add(removeFinishedItem);
            if( selectedItems.length == 1 ) {
                FrostDownloadItem item = (FrostDownloadItem) selectedItems[0];
                if( item.isSharedFile() ) {
                    addSeparator();
                    add(detailsItem);
                }
            }
    
            super.show(invoker, x, y);
        }
    }

    private class Listener
        extends MouseAdapter
        implements LanguageListener, ActionListener, KeyListener, MouseListener, PropertyChangeListener {
    
        public Listener() {
            super();
        }
    
        public void languageChanged(LanguageEvent event) {
            refreshLanguage();
        }
    
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
    
        public void keyPressed(KeyEvent e) {
            if (e.getSource() == modelTable.getTable()) {
                downloadTable_keyPressed(e);
            }
        }
    
        public void keyReleased(KeyEvent e) {
        }
    
        public void keyTyped(KeyEvent e) {
        }
    
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
    
        public void mouseReleased(MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {
    
                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showDownloadTablePopupMenu(e);
                }
    
            }
        }
        
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
}
