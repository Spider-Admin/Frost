/*
  SkinManager.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import javax.swing.table.*;

public class SkinManager extends JFrame {
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

	JButton loadSkinButton = new JButton();
	JButton saveSkinButton = new JButton();
	JButton defaultSkinButton = new JButton();
	JButton applyChangesButton = new JButton();

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

		// saveSkin
		saveSkinButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//saveSkin_actionPerformed(e);
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
			e.printStackTrace();
		}

		pack();
		configFrame(frame);
	}

	public static void configFrame(JFrame frame) {

		System.out.println("Config Frame:");
		System.out.println("=============");

		Container contentPane = frame.getContentPane();

		System.out.println(
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
		System.out.println("Recursive Descent started for " + component);

		String componentInfo = component.toString();

		for (int i = 0; i < component.getComponentCount(); i++) {

			try {
				JComponent recursiveComponent =
					(JComponent) component.getComponent(i);
				System.out.println(recursiveComponent.getName());

				DefaultMutableTreeNode newNode =
					new DefaultMutableTreeNode(recursiveComponent.toString());
				treeNode.add(newNode);
				objectMap.put(
					recursiveComponent.toString(),
					recursiveComponent);
				System.out.println(recursiveComponent);

				if (recursiveComponent.getComponentCount() > 0)
					recursiveDescent(recursiveComponent, newNode);
			} catch (ClassCastException e) {
				DefaultMutableTreeNode newNode =
					new DefaultMutableTreeNode("ClassCastException");
				treeNode.add(newNode);
			}

		}

		System.out.println("Up one level...");

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
