/*
 * Created on 13-oct-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui;

import java.awt.*;
import java.awt.Window;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.l2fprod.gui.plaf.skin.*;

/**
 * Swing component to choose among the available Skins
 */
public class SkinChooser extends JPanel {

	private static final String THEMES_DIR = "themes"; //Directory where themes are stored
	private static final String BUNDLE_CLASS = "res.LangRes"; //Class that has messages in the currently selected language

	/**
	 * Inner class to handle all the events 
	 */
	public class EventHandler implements ActionListener, ListSelectionListener {

		/**
		 * Method called when an actionEvent is fired
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == SkinChooser.this.getPreviewButton())
				previewButtonPressed(event);
			if (event.getSource() == SkinChooser.this.getRefreshButton())
				refreshButtonPressed(event);
		}

		/**
		 * Method called when a ListSelectionEvent is fired
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (event.getSource() == SkinChooser.this.getSkinsList())
				skinSelected(event);
		};

	}

	EventHandler eventHandler = new EventHandler();

	private JPanel buttonsPanel = null;
	private JButton previewButton = null;
	private JButton refreshButton = null;
	private JPanel labelPanel = null;
	private JLabel availableSkinsLabel = null;
	private JScrollPane listScrollPane = null;
	private JList skinsList = null;
	
	private ResourceBundle languageBundle = null;
	private boolean noSkinsFound = true;
	private LookAndFeel initialLookAndFeel = null;

	/**
	 * 	Constructor
	 */
	public SkinChooser() {
		super();
		initialize();
	}

	/**
	 * Return the LanguageBundle property value.
	 * @return java.util.ResourceBundle
	 */
	private ResourceBundle getLanguageBundle() {
		if (languageBundle == null) {
			languageBundle = ResourceBundle.getBundle(BUNDLE_CLASS);
		}
		return languageBundle;
	}

	/**
	 * Initialize the class.
	 */
	private void initialize() {
		setName("SkinChooser");

		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(0);
		borderLayout.setHgap(0);
		setLayout(borderLayout);

		add(getListScrollPane(), "Center");
		add(getButtonsPanel(), "South");
		add(getLabelPanel(), "West");

		initConnections();
		storeLookAndFeelState();
		refreshSkinsList();
	}
	
	/**
	 * Stores the state of the Look And Feel system
	 */
	private void storeLookAndFeelState() {
		initialLookAndFeel = UIManager.getLookAndFeel();
		//TODO: Incomplete. Must store more information (the skin if the previous l&f was already skinlf)
	}
	

	/**
	 * Return the SkinsList property value.
	 * @return javax.swing.JList
	 */
	private JList getSkinsList() {
		if (skinsList == null) {
			skinsList = new javax.swing.JList();
			skinsList.setName("SkinsList");
			skinsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		}
		return skinsList;
	}
	
	/**
	 * Return the full path of the selected skin. 
	 * @return java.lang.String the path of the skin, or null if no skin was selected
	 */
	public String getSelectedSkinPath() {
		if (getSkinsList().getSelectedIndex() == -1) {
			return null; //No selection
		} else {
			return getSkinsList().getSelectedValue().toString();
		}
	}
	
	/**
	 * Selects a skin from the list. If the skin passed as parameter is not on the list,
	 * this request is simply ignored
	 * @param skinPath the absolute path of the skin to select
	 */
	public void selectSkin(String skinPath) {
		getSkinsList().setSelectedValue(skinPath, true);
	}
	/**
	 * Initializes event connections
	 */
	private void initConnections() {
		getPreviewButton().addActionListener(eventHandler);
		getRefreshButton().addActionListener(eventHandler);
		getSkinsList().addListSelectionListener(eventHandler);
	}

	/**
	 * Method called when the Preview Button is pressed
	 * @param actionEvent The action event
	 */
	public void previewButtonPressed(ActionEvent actionEvent) {
		if (!getSkinsList().isSelectionEmpty()) {
			String selectedItem = getSkinsList().getSelectedValue().toString();
			try {
				Skin selectedSkin = SkinLookAndFeel.loadThemePack(selectedItem);
				SkinLookAndFeel.setSkin(selectedSkin);
				SkinLookAndFeel.enable();
				Frame[] appFrames = Frame.getFrames();
				for (int i = 0; i < appFrames.length; i++) {	//Loop to update all the frames
					SwingUtilities.updateComponentTreeUI(appFrames[i]);
					Window[] ownedWindows = appFrames[i].getOwnedWindows();
					for (int j = 0; j < ownedWindows.length; j++) {	//Loop to update the dialogs
						if (ownedWindows[j] instanceof Dialog) {
							SwingUtilities.updateComponentTreeUI(ownedWindows[j]); 
						}
					}
				}
			} catch (UnsupportedLookAndFeelException exception) {
				System.out.println("Exception while activating the skin: \n" + exception.getMessage() + "\n");
			} catch (Exception exception) {
				System.out.println("Exception while loading the skin: \n" + exception.getMessage() + "\n");
			}
		}
	}

	/**
	 * Dummy main method, just to see the progress of the SkinChooser
	 * To execute it from Eclipse a subdirectory called themes in
	 * the main project directory is required, hopefully with some 
	 * skins inside it (in zip format)
	 * @param args java.lang.String[]
	 */
	public static void main(java.lang.String[] args) {
		try {
			javax.swing.JFrame frame = new javax.swing.JFrame();
			SkinChooser aSkinChooser;
			aSkinChooser = new SkinChooser();
			frame.setContentPane(aSkinChooser);
			frame.setSize(600, 200);
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				};
			});
			frame.show();
			java.awt.Insets insets = frame.getInsets();
			frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
			frame.setVisible(true);
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of javax.swing.JPanel");
			exception.printStackTrace(System.out);
		}
	}

	/**
	 * Method called when the Refresh List Button is pressed
	 * @param actionEvent The action event
	 */
	public void refreshButtonPressed(ActionEvent actionEvent) {
		refreshSkinsList();
	}

	/**
	 *	Refreshes the list of available skins
	 */
	public void refreshSkinsList() {
		LinkedList skinsListData = new LinkedList();
		try {
			buildSkinsList(skinsListData, new File(THEMES_DIR));

			Collections.sort(skinsListData);
			if (skinsListData.isEmpty()) {
				skinsListData.add(getLanguageBundle().getString("NoSkinsFound"));
				noSkinsFound = true;
				getSkinsList().setEnabled(false);
				getSkinsList().setEnabled(false);
			} else {
				noSkinsFound = false;
				if (isEnabled()) {	//Only enable the list if the SkinChooser itself is enabled
					getSkinsList().setEnabled(true);
				}
			}
			getSkinsList().setListData(skinsListData.toArray());
		} catch (IOException exception) {
			System.out.println(exception.getMessage() + "\n");
		}
	}

	/**
	 * Recursively traverse <code>directory</code> and add skin files to <code>collection</code>
	 * Skin files are added if <code>accept(skinFile)</code> returns <code>true</code>
	 *
	 * @param collection collection to store skin list
	 * @param directory  the directory to list for skin files
	 */
	private void buildSkinsList(Collection collection, File directory) throws IOException {
		if (!directory.isDirectory() || !directory.exists()) {
			return;
		}
		String[] files = directory.list();
		File file;
		for (int i = 0, c = files.length; i < c; i++) {
			file = new File(directory, files[i]);
			if (file.isDirectory()) {
				buildSkinsList(collection, file);
			} else if (accept(file)) {
				try {
					collection.add(file.getCanonicalPath());
				} catch (IOException exception) {
					throw new IOException("Exception while building the skins list: \n" + exception.getMessage());
				}
			}
		}
	}

	/**
	 * Check if a given file is a skin file.
	 *
	 * @param file  the file to check
	 * @return      true if the file is a valid skin file
	 */
	private boolean accept(File file) {
		return (file.isDirectory() == false && (file.getName().endsWith(".zip")));
	}

	/**
	 * Method called when a Skin from the List is selected
	 * @param listSelectionEvent The list selection event
	 */
	public void skinSelected(ListSelectionEvent listSelectionEvent) {
		if (!listSelectionEvent.getValueIsAdjusting()) { //We ignore "adjusting" events
			if (getSkinsList().getSelectedIndex() == -1) {
				getPreviewButton().setEnabled(false); //No selection
			} else {
				getPreviewButton().setEnabled(true);
			}
		}
	}

	/**
	 * Return the ListScrollPane property value.
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			listScrollPane = new JScrollPane();
			listScrollPane.setName("ListScrollPane");
			listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			listScrollPane.setViewportView(getSkinsList());
		}
		return listScrollPane;
	}

	/**
	 * Return the ButtonsPanel property value.
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.setName("ButtonsPanel");
			buttonsPanel.setLayout(new GridBagLayout());

			GridBagConstraints constraintsPreviewButton = new GridBagConstraints();
			constraintsPreviewButton.gridx = 1;
			constraintsPreviewButton.gridy = 1;
			constraintsPreviewButton.anchor = GridBagConstraints.EAST;
			constraintsPreviewButton.weightx = 1.0;
			constraintsPreviewButton.ipadx = 20;
			constraintsPreviewButton.insets = new Insets(5, 5, 5, 5);
			getButtonsPanel().add(getPreviewButton(), constraintsPreviewButton);

			GridBagConstraints constraintsRefreshButton = new GridBagConstraints();
			constraintsRefreshButton.gridx = 2;
			constraintsRefreshButton.gridy = 1;
			constraintsRefreshButton.anchor = GridBagConstraints.EAST;
			constraintsRefreshButton.ipadx = 20;
			constraintsRefreshButton.insets = new Insets(5, 5, 5, 0);
			getButtonsPanel().add(getRefreshButton(), constraintsRefreshButton);
		}
		return buttonsPanel;
	}

	/**
	 * Return the LabelPanel property value.
	 * @return javax.swing.JPanel
	 */
	private JPanel getLabelPanel() {
		if (labelPanel == null) {
			labelPanel = new JPanel();
			labelPanel.setName("LabelPanel");
			labelPanel.setLayout(new BorderLayout());
			labelPanel.add(getAvailableSkinsLabel(), "North");
		}
		return labelPanel;
	}

	/**
	 * Return the AvailableSkinsLabel property value.
	 * @return javax.swing.JLabel
	 */
	private JLabel getAvailableSkinsLabel() {
		if (availableSkinsLabel == null) {
			availableSkinsLabel = new JLabel();
			availableSkinsLabel.setName("AvailableSkinsLabel");
			availableSkinsLabel.setText(getLanguageBundle().getString("AvailableSkins"));
			availableSkinsLabel.setMaximumSize(new Dimension(200, 30));
			availableSkinsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			availableSkinsLabel.setPreferredSize(new Dimension(120, 30));
			availableSkinsLabel.setMinimumSize(new Dimension(90, 30));
			availableSkinsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return availableSkinsLabel;
	}

	/**
	 * Return the PreviewButton property value.
	 * @return javax.swing.JButton
	 */
	private JButton getPreviewButton() {
		if (previewButton == null) {
			previewButton = new JButton();
			previewButton.setName("PreviewButton");
			previewButton.setText(getLanguageBundle().getString("Preview"));
			previewButton.setEnabled(false);
		}
		return previewButton;
	}

	/**
	 * Return the RefreshButton property value.
	 * @return javax.swing.JButton
	 */
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setName("RefreshButton");
			refreshButton.setText(getLanguageBundle().getString("RefreshList"));
		}
		return refreshButton;
	}

	/** 
	 * Overriden to enable/disable all of the components of the SkinChooser
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (noSkinsFound) { //If there are no skins, the list remains disabled
			getSkinsList().setEnabled(false);
		} else {
			getSkinsList().setEnabled(enabled);
		}

		getRefreshButton().setEnabled(enabled);

		if (enabled && (getSkinsList().getSelectedIndex() != -1)) { //Only enable the preview button if there is a selected skin
			getPreviewButton().setEnabled(true);
		} else {
			getPreviewButton().setEnabled(false);
		}
	}
	
	/**
	 * Reverts the L&F state to the one when this component was created
	 */
	public void undoPreview() {
		try {
			UIManager.setLookAndFeel(initialLookAndFeel);
//			TODO: Incomplete. Must restore more information (the skin if the previous l&f was already skinlf)
			Frame[] appFrames = Frame.getFrames();
			for (int i = 0; i < appFrames.length; i++) { //Loop to update all the frames
				SwingUtilities.updateComponentTreeUI(appFrames[i]);
				Window[] ownedWindows = appFrames[i].getOwnedWindows();
				for (int j = 0; j < ownedWindows.length; j++) { //Loop to update the dialogs
					if (ownedWindows[j] instanceof Dialog) {
						SwingUtilities.updateComponentTreeUI(ownedWindows[j]);
					}
				}
			}
		} catch (UnsupportedLookAndFeelException exception) { //This exception will never be throwed, but just in case...
			System.out.println("There was an exception when restoring the state of the Look and Feel: \n" + exception.getMessage());
		}
	}

}
