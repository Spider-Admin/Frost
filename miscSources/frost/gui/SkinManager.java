/*
  SkinManager.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

public class SkinManager extends JFrame {
	
	private static Logger logger = Logger.getLogger(SkinManager.class.getName());
	
	//     static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

	//------------------------------------------------------------------------
	// Objects
	//------------------------------------------------------------------------
	static DefaultMutableTreeNode rootNode =
		new DefaultMutableTreeNode("Root Frame");
	TreeCellRenderer treeCellRenderer = new TreeCellRenderer();
	static Map objectMap = new HashMap();
	Vector columnHeader = new Vector();
	DefaultTableModel tableModel = new DefaultTableModel();
	String currentObject;

	//------------------------------------------------------------------------
	// GUI objects
	//------------------------------------------------------------------------
	JPanel mainPanel = new JPanel(new BorderLayout());
	JToolBar toolBar = new JToolBar();
	JTree objectTree = new JTree(rootNode);
	JTable settingsTable = new JTable(tableModel);

	JScrollPane objectTreeScrollPane = new JScrollPane(objectTree);
	JScrollPane attribScrollPane = new JScrollPane(settingsTable);
	JSplitPane splitPane =
		new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			objectTreeScrollPane,
			attribScrollPane);

	JButton loadSkinButton = new JButton("Load skin");
	JButton saveSkinButton = new JButton("Save skin");
	JButton defaultSkinButton = new JButton("Restore default settings");
	JButton applyChangesButton = new JButton("Apply changes");

	private void Init() throws Exception {
		//------------------------------------------------------------------------
		// Configure objects
		//------------------------------------------------------------------------

		//         this.setIconImage(Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/data/newmessage.gif")));
		this.setTitle("Skin Manager 100% Megablast - Ultimate Edition");
		// Yep :-)
		this.setResizable(true);

		objectTree.setRootVisible(true);
		objectTree.setCellRenderer(treeCellRenderer);

		columnHeader.add("Command");
		columnHeader.add("Description");
		columnHeader.add("Value");
		tableModel.setColumnIdentifiers(columnHeader);

		//------------------------------------------------------------------------
		// Actionlistener
		//------------------------------------------------------------------------

		objectTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				objectTree_actionPerformed(e);
			}
		});

		// saveSkinButton
		saveSkinButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//saveSkinButton_actionPerformed(e);
			}
		});

		// applyChangesButton
		applyChangesButton
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyChangesButton_actionPerformed(e);
			}
		});

		//------------------------------------------------------------------------
		// Append objects
		//------------------------------------------------------------------------
		this.getContentPane().add(mainPanel, null); // add Main panel
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(splitPane, BorderLayout.CENTER);

		toolBar.add(loadSkinButton);
		toolBar.add(saveSkinButton);
		toolBar.add(defaultSkinButton);
		toolBar.add(applyChangesButton);

	}

	private void applyChangesButton_actionPerformed(ActionEvent e) {
		logger.info("Applying changes...");
		applySettings();
	}

	private void applySettings() {
		int rowCount = tableModel.getRowCount();

		if (objectMap.containsKey(currentObject)) {
			JComponent component = (JComponent) objectMap.get(currentObject);

			// *******************
			// ***** JButton *****
			// *******************
			if (component.toString().startsWith("javax.swing.JButton")) {
				logger.fine("Button");
				JButton thisButton = (JButton) component;
				for (int i = 0; i < rowCount; i++) {
					String first = (String) tableModel.getValueAt(i, 0);
					logger.fine("Setting value for " + first);

					if (first.equals("ToolTipText"))
						thisButton.setToolTipText(
							(String) tableModel.getValueAt(i, 2));

					if (first.equals("Text"))
						thisButton.setText(
							(String) tableModel.getValueAt(i, 2));

					if (first.equals("IconTextGap"))
						thisButton.setIconTextGap(
							Integer.parseInt(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("Mnemonic"))
						thisButton.setMnemonic(
							Integer.parseInt(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("Icon")) {
						File iconDescriptor =
							new File((String) tableModel.getValueAt(i, 2));
						if (iconDescriptor.isFile())
							thisButton.setIcon(
								new ImageIcon(iconDescriptor.getPath()));
						else
							thisButton.setIcon(null);
					}

					if (first.equals("PressedIcon"))
						thisButton.setPressedIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("DisabledIcon"))
						thisButton.setDisabledIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("DisabledSelectedIcon"))
						thisButton.setDisabledSelectedIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("RolloverIcon"))
						thisButton.setRolloverIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("RolloverSelectedcon"))
						thisButton.setRolloverSelectedIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("SelectedIcon"))
						thisButton.setSelectedIcon(
							new ImageIcon(
								(String) tableModel.getValueAt(i, 2)));

					if (first.equals("VerticalAlignment")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toUpperCase())
								.trim();

						if (value.equals("CENTER"))
							thisButton.setVerticalAlignment(
								SwingConstants.CENTER);
						if (value.equals("TOP"))
							thisButton.setVerticalAlignment(SwingConstants.TOP);
						if (value.equals("BOTTOM"))
							thisButton.setVerticalAlignment(
								SwingConstants.BOTTOM);

					}

					if (first.equals("VerticalTextPosition")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toUpperCase())
								.trim();

						if (value.equals("CENTER"))
							thisButton.setVerticalTextPosition(
								SwingConstants.CENTER);
						if (value.equals("TOP"))
							thisButton.setVerticalTextPosition(
								SwingConstants.TOP);
						if (value.equals("BOTTOM"))
							thisButton.setVerticalTextPosition(
								SwingConstants.BOTTOM);

					}

					if (first.equals("HorizontalAlignment")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toUpperCase())
								.trim();

						if (value.equals("RIGHT"))
							thisButton.setHorizontalAlignment(
								SwingConstants.RIGHT);
						if (value.equals("LEFT"))
							thisButton.setHorizontalAlignment(
								SwingConstants.LEFT);
						if (value.equals("CENTER"))
							thisButton.setHorizontalAlignment(
								SwingConstants.CENTER);
						if (value.equals("LEADING"))
							thisButton.setHorizontalAlignment(
								SwingConstants.LEADING);
						if (value.equals("TRAILING"))
							thisButton.setHorizontalAlignment(
								SwingConstants.TRAILING);

					}

					if (first.equals("HorizontalTextPosition")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toUpperCase())
								.trim();

						if (value.equals("RIGHT"))
							thisButton.setHorizontalTextPosition(
								SwingConstants.RIGHT);
						if (value.equals("LEFT"))
							thisButton.setHorizontalTextPosition(
								SwingConstants.LEFT);
						if (value.equals("CENTER"))
							thisButton.setHorizontalTextPosition(
								SwingConstants.CENTER);
						if (value.equals("LEADING"))
							thisButton.setHorizontalTextPosition(
								SwingConstants.LEADING);
						if (value.equals("TRAILING"))
							thisButton.setHorizontalTextPosition(
								SwingConstants.TRAILING);

					}

					if (first.equals("Margin")) {
						Vector values =
							getIntegerTuple(
								(String) tableModel.getValueAt(i, 2));
						if (values.size() == 4) {
							thisButton.setMargin(
								new Insets(
									Integer.parseInt(
										(String) values.elementAt(0)),
									Integer.parseInt(
										(String) values.elementAt(1)),
									Integer.parseInt(
										(String) values.elementAt(2)),
									Integer.parseInt(
										(String) values.elementAt(3))));
						}
					}
					// Boolean values	
					if (first.equals("BorderPainted")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toLowerCase())
								.trim();
						if (value.equals("true"))
							thisButton.setBorderPainted(true);
						else
							thisButton.setBorderPainted(false);
					}
					if (first.equals("ContentAreaFilled")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toLowerCase())
								.trim();
						if (value.equals("true"))
							thisButton.setBorderPainted(true);
						else
							thisButton.setBorderPainted(false);
					}
					if (first.equals("FocusPainted")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toLowerCase())
								.trim();
						if (value.equals("true"))
							thisButton.setBorderPainted(true);
						else
							thisButton.setBorderPainted(false);
					}
					if (first.equals("RolloverEnabled")) {
						String value =
							(((String) tableModel.getValueAt(i, 2))
								.toLowerCase())
								.trim();
						if (value.equals("true"))
							thisButton.setBorderPainted(true);
						else
							thisButton.setBorderPainted(false);
					}

					// Colors
					if (first.equals("Background")) {
						logger.fine("Setting Background values");
						Vector backgroundValues =
							getIntegerTuple(
								(String) tableModel.getValueAt(i, 2));
						if (backgroundValues.size() == 3) {
							thisButton.setBackground(
								new Color(
									Integer.parseInt(
										(String) backgroundValues.elementAt(0)),
									Integer.parseInt(
										(String) backgroundValues.elementAt(1)),
									Integer.parseInt(
										(String) backgroundValues.elementAt(
											2))));
						}
					}
					if (first.equals("Foreground")) {
						Vector foregroundValues =
							getIntegerTuple(
								(String) tableModel.getValueAt(i, 2));
						if (foregroundValues.size() == 3) {
							thisButton.setForeground(
								new Color(
									Integer.parseInt(
										(String) foregroundValues.elementAt(0)),
									Integer.parseInt(
										(String) foregroundValues.elementAt(1)),
									Integer.parseInt(
										(String) foregroundValues.elementAt(
											2))));
						}
					}
				}
			}
		}
	}

	/**
	 * Save skin
	 */
	private void saveSkin() {
		// 
	}

	/**
	* Load skin
	*/
	private void loadSkin() {
		
	}


	/**
	* Reads integer numbers in a String and returns
	* them in an array
	* @param data This String contains the numbers divided by spaces
	* @return Each field of this array contains one number. 'NULL' is returned if none is found.
	*/
	private Vector getIntegerTuple(String data) {
		int nextSpace = data.indexOf(" ");
		Vector values = new Vector();

		data = data.trim();

		while (nextSpace != -1) {
			values.add(data.substring(0, nextSpace).trim());
			data = data.substring(nextSpace).trim();
			nextSpace = data.indexOf(" ");
		}
		values.add(data.trim());

		return values;
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			dispose();
		}
		super.processWindowEvent(e);
	}

	/**Constructor*/
	//     public SkinManager(JFrame parentFrame)
	//     {

	//         enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	//         try {
	//             Init();
	//         }
	//         catch( Exception e ) {
	//             e.printStackTrace();
	//         }

	//         pack();
	//         setLocationRelativeTo(parentFrame);
	//     }
	/**Constructor*/
	public SkinManager(JFrame frame) {

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			Init();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
		}

		pack();
		configFrame(frame);
	}

	public static void configFrame(JFrame frame) {

		Container contentPane = frame.getContentPane();
		
		logger.fine("Config Frame:\n" +
					"=============\n" +
	  				"#Objects in contentPane: " + contentPane.getComponentCount());

		rootNode.setUserObject(contentPane.toString());
		objectMap.put(contentPane.toString(), contentPane);

		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			rootNode.add(
				new DefaultMutableTreeNode(
					contentPane.getComponent(i).toString()));
			objectMap.put(
				contentPane.getComponent(i).toString(),
				contentPane.getComponent(i));
			recursiveDescent(
				(JComponent) contentPane.getComponent(i),
				rootNode);
		}

	}

	private static void recursiveDescent(
		JComponent component,
		DefaultMutableTreeNode treeNode) {
		// 	System.out.println("Recursive Descent started for " + component);

		String componentInfo = component.toString();

		for (int i = 0; i < component.getComponentCount(); i++) {

			try {
				JComponent recursiveComponent =
					(JComponent) component.getComponent(i);
				// 		System.out.println(recursiveComponent.getName());

				DefaultMutableTreeNode newNode =
					new DefaultMutableTreeNode(recursiveComponent.toString());
				treeNode.add(newNode);
				objectMap.put(
					recursiveComponent.toString(),
					recursiveComponent);
				// 		System.out.println(recursiveComponent);

				if (recursiveComponent.getComponentCount() > 0)
					recursiveDescent(recursiveComponent, newNode);
			} catch (ClassCastException e) {
				DefaultMutableTreeNode newNode =
					new DefaultMutableTreeNode("ClassCastException");
				treeNode.add(newNode);
			}

		}

		logger.fine("Up one level...");

	}

	private class TreeCellRenderer extends DefaultTreeCellRenderer {

		public TreeCellRenderer() {

		}

		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
			super.getTreeCellRendererComponent(
				tree,
				value,
				sel,
				expanded,
				leaf,
				row,
				hasFocus);

			String objectID = value.toString();

			if (objectMap.containsKey(objectID)) {
				JComponent component = (JComponent) objectMap.get(objectID);
				String display = component.toString();

				if (display.indexOf("[") != -1
					&& display.startsWith("javax.swing."))
					display = display.substring(12, display.indexOf("["));

				if (component.getName() != null)
					display += " - " + component.getName();

				setText(display);
			}

			return this;
		}
	}

	public void objectTree_actionPerformed(TreeSelectionEvent e) {
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode) objectTree.getLastSelectedPathComponent();
		String userObject = (String) node.getUserObject();
		currentObject = userObject;

		if (objectMap.containsKey(userObject)) {
			JComponent component = (JComponent) objectMap.get(userObject);
			if (component.toString().startsWith("javax.swing.JButton"))
				buttonSettings((JButton) component);
		}
	}

	private void buttonSettings(JButton button) {
		// Clear table
		tableModel.getDataVector().clear();
		Vector rowData = new Vector();

		rowData.clear();
		rowData.add("ToolTipText");
		rowData.add("Tooltip text (String)");
		rowData.add(button.getToolTipText());
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("Text");
		rowData.add("Button Label (String)");
		rowData.add(button.getText());
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("IconTextGap");
		rowData.add("Gap between Icon and Text (Pixel)");
		rowData.add(String.valueOf(button.getIconTextGap()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("Icon");
		rowData.add("Default icon (path from Frost's root directory)");
		try {
			rowData.add(button.getIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("PressedIcon");
		rowData.add("Pressed icon (path from Frost's root directory)");
		try {
			rowData.add(button.getPressedIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("DisabledIcon");
		rowData.add("Disabled icon (path from Frost's root directory)");
		try {
			rowData.add(button.getDisabledIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("DisabledSelectedIcon");
		rowData.add(
			"Disabled selected icon (path from Frost's root directory)");
		try {
			rowData.add(button.getDisabledSelectedIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("RolloverIcon");
		rowData.add("Rollover icon (path from Frost's root directory)");
		try {
			rowData.add(button.getRolloverIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("RolloverSelectedIcon");
		rowData.add(
			"Rollover selected icon (path from Frost's root directory)");
		try {
			rowData.add(button.getRolloverSelectedIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("SelectedIcon");
		rowData.add("Selected icon (path from Frost's root directory)");
		try {
			rowData.add(button.getSelectedIcon().toString());
		} catch (NullPointerException e) {
			rowData.add("");
		}
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("Margin");
		rowData.add("The margin around the button (top left bottom right)");
		rowData.add(
			String.valueOf(button.getMargin().top)
				+ " "
				+ String.valueOf(button.getMargin().left)
				+ " "
				+ String.valueOf(button.getMargin().bottom)
				+ " "
				+ String.valueOf(button.getMargin().right));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("Mnemonic");
		rowData.add("Keyboard shortcut (Keycode)");
		rowData.add(String.valueOf(button.getMnemonic()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("VerticalAlignment");
		rowData.add("of text and icon (CENTER, TOP or BOTTOM)");
		if (button.getVerticalAlignment() == SwingConstants.CENTER)
			rowData.add("CENTER");
		if (button.getVerticalAlignment() == SwingConstants.TOP)
			rowData.add("TOP");
		if (button.getVerticalAlignment() == SwingConstants.BOTTOM)
			rowData.add("BOTTOM");
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("HorizontalAlignment");
		rowData.add(
			"of text and icon (RIGHT, LEFT, CENTER, LEADING, TRAILING)");
		if (button.getHorizontalAlignment() == SwingConstants.CENTER)
			rowData.add("CENTER");
		if (button.getHorizontalAlignment() == SwingConstants.LEFT)
			rowData.add("LEFT");
		if (button.getHorizontalAlignment() == SwingConstants.RIGHT)
			rowData.add("RIGHT");
		if (button.getHorizontalAlignment() == SwingConstants.LEADING)
			rowData.add("LEADING");
		if (button.getHorizontalAlignment() == SwingConstants.TRAILING)
			rowData.add("TRAILING");
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("HorizontalTextPosition");
		rowData.add(
			"of text relativ to icon (RIGHT, LEFT, CENTER, LEADING, TRAILING)");
		if (button.getHorizontalTextPosition() == SwingConstants.CENTER)
			rowData.add("CENTER");
		if (button.getHorizontalTextPosition() == SwingConstants.LEFT)
			rowData.add("LEFT");
		if (button.getHorizontalTextPosition() == SwingConstants.RIGHT)
			rowData.add("RIGHT");
		if (button.getHorizontalTextPosition() == SwingConstants.LEADING)
			rowData.add("LEADING");
		if (button.getHorizontalTextPosition() == SwingConstants.TRAILING)
			rowData.add("TRAILING");
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("VerticalTextPosition");
		rowData.add("of text relativ to icon (CENTER, TOP or BOTTOM)");
		if (button.getVerticalTextPosition() == SwingConstants.CENTER)
			rowData.add("CENTER");
		if (button.getVerticalTextPosition() == SwingConstants.TOP)
			rowData.add("TOP");
		if (button.getVerticalTextPosition() == SwingConstants.BOTTOM)
			rowData.add("BOTTOM");
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("BorderPainted");
		rowData.add("True or false");
		rowData.add(String.valueOf(button.isBorderPainted()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("ContentAreaFilled");
		rowData.add("True or false");
		rowData.add(String.valueOf(button.isContentAreaFilled()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("FocusPainted");
		rowData.add("True or false");
		rowData.add(String.valueOf(button.isFocusPainted()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("RolloverEnabled");
		rowData.add("True or false");
		rowData.add(String.valueOf(button.isRolloverEnabled()));
		tableModel.addRow((Vector) rowData.clone());

		rowData.clear();
		rowData.add("Background");
		rowData.add("Red, green, blue (0 - 255)");
		{
			Color color = button.getBackground();
			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();
			rowData.add(red + " " + green + " " + blue);
			tableModel.addRow((Vector) rowData.clone());
		}

		rowData.clear();
		rowData.add("Foreground");
		rowData.add("Red, green, blue (0 - 255)");
		{
			Color color = button.getForeground();
			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();
			rowData.add(red + " " + green + " " + blue);
			tableModel.addRow((Vector) rowData.clone());
		}
	}

	//     public static void main (String agrs[]) {
	// 	SkinManager newSkinManager = new SkinManager();
	// 	newSkinManager.show();

	//     }

}
