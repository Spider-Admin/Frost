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

		columnHeader.add("Attribute");
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
		rowData.add(button.getToolTipText());
		tableModel.addRow((Vector)rowData.clone());

		rowData.clear();
		rowData.add("Text");
		rowData.add(button.getText());
		tableModel.addRow((Vector)rowData.clone());

		rowData.clear();
		rowData.add("Default Icon");
		rowData.add(button.getIcon().toString());
		tableModel.addRow((Vector)rowData.clone());
	}

	//     public static void main (String agrs[]) {
	// 	SkinManager newSkinManager = new SkinManager();
	// 	newSkinManager.show();

	//     }

}
