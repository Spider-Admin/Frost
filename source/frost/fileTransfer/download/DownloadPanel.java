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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import frost.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.fileTransfer.common.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.search.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class DownloadPanel extends JPanel implements SettingsUpdater {

	private PopupMenuDownload popupMenuDownload = null;

	private final Listener listener = new Listener();

	private static final Logger logger = Logger.getLogger(DownloadPanel.class.getName());

	private DownloadModel model = null;

	private Language language = null;

	private final JToolBar downloadToolBar = new JToolBar();
	private final JButton downloadActivateButton = new JButton(MiscToolkit.loadImageIcon("/data/toolbar/media-playback-start.png"));
    private final JButton downloadPauseButton = new JButton(MiscToolkit.loadImageIcon("/data/toolbar/media-playback-pause.png"));
	private final JTextField downloadTextField = new JTextField(25);
	private final JLabel downloadItemCountLabel = new JLabel();
    private final JCheckBox removeFinishedDownloadsCheckBox = new JCheckBox();
    private final JCheckBox showExternalGlobalQueueItems = new JCheckBox();
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
        @Override
        public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
            str = str.replace('\n', ';');
            str = str.replace('\r', ' ');
            super.insertString(offs, str, a);
        }
    }

	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			MiscToolkit.configureButton(downloadActivateButton); // play_rollover
			MiscToolkit.configureButton(downloadPauseButton); // pause_rollover

			new TextComponentClipboardMenu(downloadTextField, language);

            downloadToolBar.setRollover(true);
            downloadToolBar.setFloatable(false);

            removeFinishedDownloadsCheckBox.setOpaque(false);
            showExternalGlobalQueueItems.setOpaque(false);

			downloadTextField.setMaximumSize(downloadTextField.getPreferredSize());
            downloadTextField.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.addKeys"));
            downloadTextField.setDocument(new HandleMultiLineKeysDocument());

			downloadToolBar.add(downloadTextField); //Download/Quickload
			downloadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
			downloadToolBar.add(downloadActivateButton); //Download/Start transfer
			downloadToolBar.add(downloadPauseButton); //Download/Start transfer
            downloadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
            downloadToolBar.add(removeFinishedDownloadsCheckBox);
            if( PersistenceManager.isPersistenceEnabled() ) {
                downloadToolBar.add(Box.createRigidArea(new Dimension(8, 0)));
                downloadToolBar.add(showExternalGlobalQueueItems);
            }
			downloadToolBar.add(Box.createRigidArea(new Dimension(80, 0)));
			downloadToolBar.add(Box.createHorizontalGlue());
			downloadToolBar.add(downloadItemCountLabel);

			// create the main download panel
			modelTable = new SortedModelTable(model);
            new TableFindAction().install(modelTable.getTable());
			setLayout(new BorderLayout());
			add(downloadToolBar, BorderLayout.NORTH);
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
            removeFinishedDownloadsCheckBox.addItemListener(listener);
            showExternalGlobalQueueItems.addItemListener(listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
            Core.frostSettings.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

			//Settings
            removeFinishedDownloadsCheckBox.setSelected(Core.frostSettings.getBoolValue(SettingsClass.DOWNLOAD_REMOVE_FINISHED));
            showExternalGlobalQueueItems.setSelected(Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD));
			setDownloadingActivated(Core.frostSettings.getBoolValue(SettingsClass.DOWNLOADING_ACTIVATED));

			initialized = true;
		}
	}

	private Dimension calculateLabelSize(final String text) {
		final JLabel dummyLabel = new JLabel(text);
		dummyLabel.doLayout();
		return dummyLabel.getPreferredSize();
	}

	private void refreshLanguage() {
		downloadActivateButton.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.activateDownloading"));
        downloadPauseButton.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.pauseDownloading"));
        downloadTextField.setToolTipText(language.getString("DownloadPane.toolbar.tooltip.addKeys"));
        removeFinishedDownloadsCheckBox.setText(language.getString("DownloadPane.removeFinishedDownloads"));
        showExternalGlobalQueueItems.setText(language.getString("DownloadPane.showExternalGlobalQueueItems"));

		final String waiting = language.getString("DownloadPane.toolbar.waiting");
		final Dimension labelSize = calculateLabelSize(waiting + ": 00000");
		downloadItemCountLabel.setPreferredSize(labelSize);
		downloadItemCountLabel.setMinimumSize(labelSize);
		downloadItemCountLabel.setText(waiting + ": " + downloadItemCount);
	}

	public void setModel(final DownloadModel model) {
		this.model = model;
	}

	/**
	 * downloadTextField Action Listener (Download/Quickload)
     * The textfield can contain 1 key to download or multiple keys separated by ';'.
	 */
	private void downloadTextField_actionPerformed(final ActionEvent e) {

        // FIXME: show dialog with all keys like fuqid

        try {
    		final String keys = downloadTextField.getText().trim();

            if( keys.length() == 0 ) {
                downloadTextField.setText("");
                return;
            }

            final String[] keyList = keys.split("[;\n]");
            if( keyList == null || keyList.length == 0 ) {
                downloadTextField.setText("");
                return;
            }

            for( final String element : keyList ) {
                String key = element.trim();

                if( key.length() < 5 ) {
                    continue;
                }

                // maybe convert html codes (e.g. %2c -> , )
                if( key.indexOf("%") > 0 ) {
                    try {
                        key = java.net.URLDecoder.decode(key, "UTF-8");
                    } catch (final java.io.UnsupportedEncodingException ex) {
                        logger.log(Level.SEVERE, "Decode of HTML code failed", ex);
                    }
                }

                // find key type (chk,ssk,...)
                int pos = -1;
                for( int i = 0; i < FreenetKeys.getFreenetKeyTypes().length; i++ ) {
                    final String string = FreenetKeys.getFreenetKeyTypes()[i];
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
                final int sepIndex = key.lastIndexOf("/");
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

                // finally check if the key is valid for this network
                if( !FreenetKeys.isValidKey(checkKey) ) {
                    showInvalidKeyErrorDialog(key);
                    continue;
                }

                // add valid key to download table
                FileTransferManager.inst().getDownloadManager().addNewDownload(key, fileName);
            }
        } catch(final Throwable ex) {
            logger.log(Level.SEVERE, "Unexpected exception", ex);
            showInvalidKeyErrorDialog("???");
        }
        downloadTextField.setText("");
	}

    private void showInvalidKeyErrorDialog(final String invKey) {
        JOptionPane.showMessageDialog(
                this,
                language.formatMessage("DownloadPane.invalidKeyDialog.body", invKey),
                language.getString("DownloadPane.invalidKeyDialog.title"),
                JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Get keyTyped for downloadTable
	 */
	private void downloadTable_keyPressed(final KeyEvent e) {
		final char key = e.getKeyChar();
		if (key == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
            removeSelectedDownloads();
		}
	}

    private void removeSelectedDownloads() {
        final ModelItem[] selectedItems = modelTable.getSelectedItems();

        final List<String> externalRequestsToRemove = new LinkedList<String>();
        final List<ModelItem> requestsToRemove = new LinkedList<ModelItem>();
        for( final ModelItem mi : selectedItems ) {
            final FrostDownloadItem i = (FrostDownloadItem)mi;
            requestsToRemove.add(mi);
            if( i.isExternal() ) {
                externalRequestsToRemove.add(i.getGqIdentifier());
            }
        }

        final ModelItem[] ri = requestsToRemove.toArray(new ModelItem[requestsToRemove.size()]);
        model.removeItems(ri);

        modelTable.getTable().clearSelection();

        if( FileTransferManager.inst().getPersistenceManager() != null && externalRequestsToRemove.size() > 0 ) {
            new Thread() {
                @Override
                public void run() {
                    FileTransferManager.inst().getPersistenceManager().removeRequests(externalRequestsToRemove);
                }
            }.start();
        }
    }

	public boolean isDownloadingActivated() {
		return downloadingActivated;
	}

	public void setDownloadingActivated(final boolean b) {
		downloadingActivated = b;

        downloadActivateButton.setEnabled(!downloadingActivated);
        downloadPauseButton.setEnabled(downloadingActivated);
	}

	public void setDownloadItemCount(final int newDownloadItemCount) {
		downloadItemCount = newDownloadItemCount;

		final String s =
			new StringBuilder()
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

	private void showDownloadTablePopupMenu(final MouseEvent e) {
        // select row where rightclick occurred if row under mouse is NOT selected
        final Point p = e.getPoint();
        final int y = modelTable.getTable().rowAtPoint(p);
        if( y < 0 ) {
            return;
        }
        if( !modelTable.getTable().getSelectionModel().isSelectedIndex(y) ) {
            modelTable.getTable().getSelectionModel().setSelectionInterval(y, y);
        }
		getPopupMenuDownload().show(e.getComponent(), e.getX(), e.getY());
	}

	private void fontChanged() {
		final String fontName = Core.frostSettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		final int fontStyle = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		final int fontSize = Core.frostSettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" +
						   "That selection will be changed to \"SansSerif\".");
            Core.frostSettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
			font = new Font("SansSerif", fontStyle, fontSize);
		}
		modelTable.setFont(font);
	}

	private void downloadActivateButtonPressed(final ActionEvent e) {
		setDownloadingActivated(true);
	}

    private void downloadPauseButtonPressed(final ActionEvent e) {
        setDownloadingActivated(false);
    }

	private void downloadTableDoubleClick(final MouseEvent e) {
		final int clickedCol = modelTable.getTable().columnAtPoint(e.getPoint());
		final int modelIx = modelTable.getTable().getColumnModel().getColumn(clickedCol).getModelIndex();
		if (modelIx == 0) {
			return;
		}

		final ModelItem selectedItem = modelTable.getSelectedItem();
		if (selectedItem != null) {
			final FrostDownloadItem dlItem = (FrostDownloadItem) selectedItem;
            final File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
            if( !targetFile.isFile() ) {
                return;
            }
			logger.info("Executing: " + targetFile.getAbsolutePath());
            try {
            	ExecuteDocument.openDocument(targetFile);
            } catch(final Throwable t) {
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

        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            super.getTableCellRendererComponent(table, value, isSelected, /*hasFocus*/ false, row, column);

            final FrostDownloadItem item = (FrostDownloadItem)model.getItemAt(row);

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
    implements ActionListener, LanguageListener {

        private final JMenuItem detailsItem = new JMenuItem();
        private final JMenuItem copyKeysAndNamesItem = new JMenuItem();
        private final JMenuItem copyExtendedInfoItem = new JMenuItem();
        private final JMenuItem disableAllDownloadsItem = new JMenuItem();
        private final JMenuItem disableSelectedDownloadsItem = new JMenuItem();
        private final JMenuItem enableAllDownloadsItem = new JMenuItem();
        private final JMenuItem enableSelectedDownloadsItem = new JMenuItem();
        private final JMenuItem invertEnabledAllItem = new JMenuItem();
        private final JMenuItem invertEnabledSelectedItem = new JMenuItem();
        private final JMenuItem removeSelectedDownloadsItem = new JMenuItem();
        private final JMenuItem restartSelectedDownloadsItem = new JMenuItem();

        private final JMenuItem startSelectedDownloadsNow = new JMenuItem();

        private JMenu changePriorityMenu = null;
        private JMenuItem prio0Item = null;
        private JMenuItem prio1Item = null;
        private JMenuItem prio2Item = null;
        private JMenuItem prio3Item = null;
        private JMenuItem prio4Item = null;
        private JMenuItem prio5Item = null;
        private JMenuItem prio6Item = null;
        private JMenuItem removeFromGqItem = null;

        private JMenuItem retrieveDirectExternalDownloads = null;

        private final JMenu copyToClipboardMenu = new JMenu();

        public PopupMenuDownload() {
            super();
            initialize();
        }

        private void initialize() {

            if( PersistenceManager.isPersistenceEnabled() ) {
                changePriorityMenu = new JMenu();
                prio0Item = new JMenuItem();
                prio1Item = new JMenuItem();
                prio2Item = new JMenuItem();
                prio3Item = new JMenuItem();
                prio4Item = new JMenuItem();
                prio5Item = new JMenuItem();
                prio6Item = new JMenuItem();
                removeFromGqItem = new JMenuItem();

                changePriorityMenu.add(prio0Item);
                changePriorityMenu.add(prio1Item);
                changePriorityMenu.add(prio2Item);
                changePriorityMenu.add(prio3Item);
                changePriorityMenu.add(prio4Item);
                changePriorityMenu.add(prio5Item);
                changePriorityMenu.add(prio6Item);

                prio0Item.addActionListener(this);
                prio1Item.addActionListener(this);
                prio2Item.addActionListener(this);
                prio3Item.addActionListener(this);
                prio4Item.addActionListener(this);
                prio5Item.addActionListener(this);
                prio6Item.addActionListener(this);
                removeFromGqItem.addActionListener(this);

                retrieveDirectExternalDownloads = new JMenuItem();
                retrieveDirectExternalDownloads.addActionListener(this);
            }

            refreshLanguage();

            // TODO: implement cancel of downloads

            copyToClipboardMenu.add(copyKeysAndNamesItem);
            copyToClipboardMenu.add(copyExtendedInfoItem);

            copyKeysAndNamesItem.addActionListener(this);
            copyExtendedInfoItem.addActionListener(this);
            restartSelectedDownloadsItem.addActionListener(this);
            removeSelectedDownloadsItem.addActionListener(this);
            enableAllDownloadsItem.addActionListener(this);
            disableAllDownloadsItem.addActionListener(this);
            enableSelectedDownloadsItem.addActionListener(this);
            disableSelectedDownloadsItem.addActionListener(this);
            invertEnabledAllItem.addActionListener(this);
            invertEnabledSelectedItem.addActionListener(this);
            detailsItem.addActionListener(this);
            startSelectedDownloadsNow.addActionListener(this);
        }

        private void refreshLanguage() {
            detailsItem.setText(language.getString("Common.details"));
            copyKeysAndNamesItem.setText(language.getString("Common.copyToClipBoard.copyKeysWithFilenames"));
            copyExtendedInfoItem.setText(language.getString("Common.copyToClipBoard.copyExtendedInfo"));
            restartSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.restartSelectedDownloads"));
            removeSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.remove.removeSelectedDownloads"));
            enableAllDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.enableAllDownloads"));
            disableAllDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.disableAllDownloads"));
            enableSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.enableSelectedDownloads"));
            disableSelectedDownloadsItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.disableSelectedDownloads"));
            invertEnabledAllItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForAllDownloads"));
            invertEnabledSelectedItem.setText(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads.invertEnabledStateForSelectedDownloads"));
            startSelectedDownloadsNow.setText(language.getString("DownloadPane.fileTable.popupmenu.startSelectedDownloadsNow"));

            copyToClipboardMenu.setText(language.getString("Common.copyToClipBoard") + "...");

            if( PersistenceManager.isPersistenceEnabled() ) {
                changePriorityMenu.setText(language.getString("Common.priority.changePriority"));
                prio0Item.setText(language.getString("Common.priority.priority0"));
                prio1Item.setText(language.getString("Common.priority.priority1"));
                prio2Item.setText(language.getString("Common.priority.priority2"));
                prio3Item.setText(language.getString("Common.priority.priority3"));
                prio4Item.setText(language.getString("Common.priority.priority4"));
                prio5Item.setText(language.getString("Common.priority.priority5"));
                prio6Item.setText(language.getString("Common.priority.priority6"));
                removeFromGqItem.setText(language.getString("DownloadPane.fileTable.popupmenu.removeFromGlobalQueue"));

                retrieveDirectExternalDownloads.setText(language.getString("DownloadPane.fileTable.popupmenu.retrieveDirectExternalDownloads"));
            }
        }

        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == copyKeysAndNamesItem) {
                CopyToClipboard.copyKeysAndFilenames(modelTable.getSelectedItems());
            } else if (e.getSource() == copyExtendedInfoItem) {
                CopyToClipboard.copyExtendedInfo(modelTable.getSelectedItems());
            } else if (e.getSource() == restartSelectedDownloadsItem) {
                restartSelectedDownloads();
            } else if (e.getSource() == removeSelectedDownloadsItem) {
                removeSelectedDownloads();
            } else if (e.getSource() == enableAllDownloadsItem) {
                enableAllDownloads();
            } else if (e.getSource() == disableAllDownloadsItem) {
                disableAllDownloads();
            } else if (e.getSource() == enableSelectedDownloadsItem) {
                enableSelectedDownloads();
            } else if (e.getSource() == disableSelectedDownloadsItem) {
                disableSelectedDownloads();
            } else if (e.getSource() == invertEnabledAllItem) {
                invertEnabledAll();
            } else if (e.getSource() == invertEnabledSelectedItem) {
                invertEnabledSelected();
            } else if (e.getSource() == detailsItem) {
                showDetails();
            } else if (e.getSource() == prio0Item) {
                changePriority(0);
            } else if (e.getSource() == prio1Item) {
                changePriority(1);
            } else if (e.getSource() == prio2Item) {
                changePriority(2);
            } else if (e.getSource() == prio3Item) {
                changePriority(3);
            } else if (e.getSource() == prio4Item) {
                changePriority(4);
            } else if (e.getSource() == prio5Item) {
                changePriority(5);
            } else if (e.getSource() == prio6Item) {
                changePriority(6);
            } else if (e.getSource() == removeFromGqItem) {
                removeSelectedUploadsFromGlobalQueue();
            } else if (e.getSource() == retrieveDirectExternalDownloads) {
                retrieveDirectExternalDownloads();
            } else if (e.getSource() == startSelectedDownloadsNow ) {
                startSelectedDownloadsNow();
            }
        }

        private void removeSelectedUploadsFromGlobalQueue() {
            if( FileTransferManager.inst().getPersistenceManager() == null ) {
                return;
            }
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            final List<String> requestsToRemove = new ArrayList<String>();
            final List<FrostDownloadItem> itemsToUpdate = new ArrayList<FrostDownloadItem>();
            for(final ModelItem mi : selectedItems) {
                final FrostDownloadItem item = (FrostDownloadItem) mi;
                if( FileTransferManager.inst().getPersistenceManager().isItemInGlobalQueue(item) ) {
                    requestsToRemove.add( item.getGqIdentifier() );
                    itemsToUpdate.add(item);
                    item.setInternalRemoveExpected(true);
                }
            }
            FileTransferManager.inst().getPersistenceManager().removeRequests(requestsToRemove);
            // after remove, update state of removed items
            for(final FrostDownloadItem item : itemsToUpdate) {
                item.setState(FrostDownloadItem.STATE_WAITING);
                item.setEnabled(Boolean.FALSE);
                item.setPriority(-1);
                item.fireValueChanged();
            }
        }

        private void retrieveDirectExternalDownloads() {
            if( FileTransferManager.inst().getPersistenceManager() == null ) {
                return;
            }
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            for(final ModelItem mi : selectedItems) {
                final FrostDownloadItem item = (FrostDownloadItem) mi;
                if( item.isExternal() && item.isDirect() && item.getState() == FrostDownloadItem.STATE_DONE ) {
                    final long expectedFileSize = item.getFileSize(); // set from global queue
                    FileTransferManager.inst().getPersistenceManager().maybeEnqueueDirectGet(item, expectedFileSize);
                }
            }
        }

        private void startSelectedDownloadsNow() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();

            final List<FrostDownloadItem> itemsToStart = new LinkedList<FrostDownloadItem>();
            for( final ModelItem mi : selectedItems ) {
                final FrostDownloadItem i = (FrostDownloadItem)mi;
                if( i.isExternal() ) {
                    continue;
                }
                if( i.getState() != FrostDownloadItem.STATE_WAITING ) {
                    continue;
                }
                if( i.getKey() == null ) {
                    continue;
                }
                itemsToStart.add(i);
            }

            for(final FrostDownloadItem dlItem : itemsToStart) {
                dlItem.setEnabled(true);
                FileTransferManager.inst().getDownloadManager().startDownload(dlItem);
            }
        }

        private void changePriority(final int prio) {
            if( FileTransferManager.inst().getPersistenceManager() == null ) {
                return;
            }
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            FileTransferManager.inst().getPersistenceManager().changeItemPriorites(selectedItems, prio);
        }

        private void showDetails() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            if (selectedItems.length != 1) {
                return;
            }
            final FrostDownloadItem item = (FrostDownloadItem) selectedItems[0];
            if( !item.isSharedFile() ) {
                return;
            }
            new FileListFileDetailsDialog(MainFrame.getInstance()).startDialog(item.getFileListFileObject());
        }

        private void invertEnabledSelected() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(null, selectedItems);
        }

        private void invertEnabledAll() {
            model.setAllItemsEnabled(null);
        }

        private void disableSelectedDownloads() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(Boolean.FALSE, selectedItems);
        }

        private void enableSelectedDownloads() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.setItemsEnabled(Boolean.TRUE, selectedItems);
        }

        private void disableAllDownloads() {
            model.setAllItemsEnabled(Boolean.FALSE);
        }

        private void enableAllDownloads() {
            model.setAllItemsEnabled(Boolean.TRUE);
        }

        private void restartSelectedDownloads() {
            final ModelItem[] selectedItems = modelTable.getSelectedItems();
            model.restartItems(selectedItems);
        }

        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        @Override
        public void show(final Component invoker, final int x, final int y) {
            removeAll();

            final ModelItem[] selectedItems = modelTable.getSelectedItems();

            if( selectedItems.length == 0 ) {
                return;
            }

            add(copyToClipboardMenu);
            addSeparator();
            add(startSelectedDownloadsNow);
            add(restartSelectedDownloadsItem);
            addSeparator();

            if( PersistenceManager.isPersistenceEnabled() ) {
                add(changePriorityMenu);
                addSeparator();
            }

            final JMenu enabledSubMenu = new JMenu(language.getString("DownloadPane.fileTable.popupmenu.enableDownloads") + "...");
            enabledSubMenu.add(enableSelectedDownloadsItem);
            enabledSubMenu.add(disableSelectedDownloadsItem);
            enabledSubMenu.add(invertEnabledSelectedItem);
            enabledSubMenu.addSeparator();

            enabledSubMenu.add(enableAllDownloadsItem);
            enabledSubMenu.add(disableAllDownloadsItem);
            enabledSubMenu.add(invertEnabledAllItem);
            add(enabledSubMenu);

            // we only find external items if persistence is enabled
            if( PersistenceManager.isPersistenceEnabled() ) {
                for(final ModelItem mi : selectedItems) {
                    final FrostDownloadItem item = (FrostDownloadItem) mi;
                    if( item.isExternal() && item.isDirect() && item.getState() == FrostDownloadItem.STATE_DONE ) {
                        add(retrieveDirectExternalDownloads);
                        break;
                    }
                }
            }
            add(removeSelectedDownloadsItem);
            if(  FileTransferManager.inst().getPersistenceManager() != null && selectedItems != null ) {
                // add only if there are removable items selected
                for(final ModelItem mi : selectedItems) {
                    final FrostDownloadItem item = (FrostDownloadItem) mi;
                    if(  FileTransferManager.inst().getPersistenceManager().isItemInGlobalQueue(item) ) {
                        add(removeFromGqItem);
                        break;
                    }
                }
            }
            if( selectedItems.length == 1 ) {
                final FrostDownloadItem item = (FrostDownloadItem) selectedItems[0];
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
        implements LanguageListener, ActionListener, KeyListener, MouseListener, PropertyChangeListener, ItemListener {

        public Listener() {
            super();
        }

        public void languageChanged(final LanguageEvent event) {
            refreshLanguage();
        }

        public void actionPerformed(final ActionEvent e) {
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

        public void keyPressed(final KeyEvent e) {
            if (e.getSource() == modelTable.getTable()) {
                downloadTable_keyPressed(e);
            }
        }

        public void keyReleased(final KeyEvent e) {
        }

        public void keyTyped(final KeyEvent e) {
        }

        @Override
        public void mousePressed(final MouseEvent e) {
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

        @Override
        public void mouseReleased(final MouseEvent e) {
            if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

                if ((e.getSource() == modelTable.getTable())
                    || (e.getSource() == modelTable.getScrollPane())) {
                    showDownloadTablePopupMenu(e);
                }

            }
        }

        public void propertyChange(final PropertyChangeEvent evt) {
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

        public void itemStateChanged(final ItemEvent e) {
            if( removeFinishedDownloadsCheckBox.isSelected() ) {
                Core.frostSettings.setValue(SettingsClass.DOWNLOAD_REMOVE_FINISHED, true);
                model.removeFinishedDownloads();
            } else {
                Core.frostSettings.setValue(SettingsClass.DOWNLOAD_REMOVE_FINISHED, false);
            }
            if( showExternalGlobalQueueItems.isSelected() ) {
                Core.frostSettings.setValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD, true);
            } else {
                Core.frostSettings.setValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD, false);
                model.removeExternalDownloads();
            }
        }
    }
}
