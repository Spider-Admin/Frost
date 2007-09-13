/*
 HelloWorldPanel.java / Frost
 Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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

package frostplugins.SimpleSiteTool;

import hyperocha.fcp.NodeMessage;
import hyperocha.fcp.PriorityClass;
import hyperocha.fcp.cmd.FCPClientPut;
import hyperocha.fcp.cmd.impl.ClientPutComplexDir;
import hyperocha.fcp.cmd.impl.ClientPutDiskDir;
import hyperocha.fcp.cmd.impl.GenerateSSK;
import hyperocha.fcp.io.FCPConnection;
import hyperocha.fcp.io.SimpleSocketFactory;
import hyperocha.util.SimpleDirParser;
import hyperocha.util.swing.mutablelist.DefaultListCellEditor;
import hyperocha.util.swing.mutablelist.JListMutable;
import hyperocha.util.swing.mutablelist.MutableListModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.garret.perst.IPersistentList;
import org.garret.perst.Storage;

import frost.pluginmanager.PluginRespinator;
import frost.plugins.FrostPlugin;

/**
 * @author saces
 * 
 */
public class SimpleSiteTool extends JPanel implements FrostPlugin {

	private static final long serialVersionUID = 1L;

	private JLabel jLabel1;
	private JButton jButton1;
	private JTextField tf_indexFilename;
	private JButton jButton2;
	private JCheckBox jCheckBox1;
	private JSpinner jSpinner1;
	private JTextField jTextField4;
	private JRadioButton jRadioButton7;
	private JRadioButton jRadioButton6;
	private JRadioButton jRadioButton5;
	private JLabel jLabel3;
	private JButton jButton9;
	private JRadioButton jRadioButton4;
	private JRadioButton jRadioButton3;
	private ButtonGroup buttonGroup2;
	private JLabel jLabel2;
	private JButton btn_Revert;
	private JButton btn_Save;
	private JButton btn_Del;
	private JButton btn_New;
	private JPanel jPanel10;
	private JScrollPane jScrollPane3;
	private JPanel jPanel9;
	private JPanel jPanel8;
	private JPanel jPanel7;
	private JPanel jPanel6;
	private JPanel jPanel5;
	private JComboBox jComboBox4;
	private JButton jButton4;
	private JPanel jPanel4;
	private JPanel jPanel3;
	private JPanel jPanel2;
	private JSplitPane jSplitPane2;
	private JSplitPane jSplitPane1;
	private JComboBox jComboBox3;
	private JComboBox combo_announceBoard;
	private JCheckBox cbox_announce;
	private JScrollPane jScrollPane2;
	private JTextPane jTextPane1;
	private JButton jButton3;
	private JComboBox jComboBox1;
	private JTextField tf_InsertUri;
	private JPanel jPanel1;
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private ButtonGroup buttonGroup1;
	private JListMutable jList2;
	private ProjectListModel jList2Model;
	private JScrollPane jScrollPane1;
	private JCheckBox cbox_defaultIndex;
	private JTextField jTextField1;
	private CardLayout jPanel3Layout;

	private PluginRespinator pluginRespinator;

	private Storage storage;
	
	private IPersistentList<ProjectItem> projectList;
	
	private int selectedIndex = -1;
	
	private boolean ignoreUpdate = true;

	public SimpleSiteTool() {
		super();
	}

	private void initGUI() {
		try {
			{
				BorderLayout thisLayout = new BorderLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(723, 421));
				// START >> jSplitPane1
				//START >>  buttonGroup2
				buttonGroup2 = new ButtonGroup();
				//END <<  buttonGroup2
				jSplitPane1 = new JSplitPane();
				this.add(jSplitPane1, BorderLayout.CENTER);
				jSplitPane1.setDividerLocation(80);

				jScrollPane1 = new JScrollPane();
				jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
				jScrollPane1.setBorder(BorderFactory
					.createTitledBorder("Projects"));

				jSplitPane2 = new JSplitPane();
				jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
				jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
				jSplitPane2.setDividerLocation(200);

				jScrollPane3 = new JScrollPane();
				jSplitPane2.add(jScrollPane3, JSplitPane.LEFT);

				jPanel3 = new JPanel();
				jScrollPane3.setViewportView(jPanel3);
				jPanel3Layout = new CardLayout();
				jPanel3.setLayout(jPanel3Layout);

				jPanel5 = new JPanel();
				jPanel3.add(jPanel5, "jPanel5");
				GridBagLayout jPanel5Layout = new GridBagLayout();
				jPanel5Layout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
				jPanel5Layout.rowHeights = new int[] { 7, 7, 7, 7 };
				jPanel5Layout.columnWeights = new double[] { 0.1, 0.1, 0.1, 0.1 };
				jPanel5Layout.columnWidths = new int[] { 7, 7, 7, 7 };
				jPanel5.setLayout(jPanel5Layout);
				jPanel5.setPreferredSize(new java.awt.Dimension(608, 204));

				jLabel1 = new JLabel();
				jPanel5.add(jLabel1, new GridBagConstraints(
					0,
					0,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				GridLayout jLabel1Layout = new GridLayout(1, 1);
				jLabel1Layout.setHgap(5);
				jLabel1Layout.setVgap(5);
				jLabel1Layout.setColumns(1);
				jLabel1.setLayout(jLabel1Layout);
				jLabel1.setText("Directory to inserst:");
				jLabel1.setPreferredSize(new java.awt.Dimension(135, 15));

				PlainDocument doc1 = new PlainDocument();
				jTextField1 = new JTextField(doc1, "", 0);
								
				doc1.addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {}

					public void insertUpdate(DocumentEvent e) {
						updated(e);
					}

					public void removeUpdate(DocumentEvent e) {
						updated(e);
					}
					
					private void updated(DocumentEvent e) {
						if (!ignoreUpdate) {
							Document doc = e.getDocument();
							try {
								projectList.get(selectedIndex).sourceDir = doc.getText(0, doc.getLength());
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				
				});
				
				jPanel5.add(jTextField1, new GridBagConstraints(
					1,
					0,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jTextField1.setText("doc");

				jButton1 = new JButton();
				jPanel5.add(jButton1, new GridBagConstraints(
					2,
					0,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jButton1.setText("...");

				cbox_defaultIndex = new JCheckBox();
				jPanel5.add(cbox_defaultIndex, new GridBagConstraints(
					0,
					1,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				cbox_defaultIndex.setText("Default index");
				cbox_defaultIndex.setSelected(true);

				PlainDocument doc_indexFilename = new PlainDocument();
				tf_indexFilename = new JTextField(doc_indexFilename, "", 0);
								
				doc_indexFilename.addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {}

					public void insertUpdate(DocumentEvent e) {
						updated(e);
					}

					public void removeUpdate(DocumentEvent e) {
						updated(e);
					}
					
					private void updated(DocumentEvent e) {
						if (!ignoreUpdate) {
							Document doc = e.getDocument();
							try {
								projectList.get(selectedIndex).indexFilename = doc.getText(0, doc.getLength());
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				
				});
				
				
				
				jPanel5.add(tf_indexFilename, new GridBagConstraints(
					3,
					1,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0),
					0,
					0));
				tf_indexFilename.setText("index.html");

				jButton2 = new JButton();
				jPanel5.add(jButton2, new GridBagConstraints(
					4,
					1,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jButton2.setText("...");

				jRadioButton1 = new JRadioButton();
				jPanel5.add(jRadioButton1, new GridBagConstraints(
					0,
					3,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				
				buttonGroup1 = new ButtonGroup();
				jRadioButton1.setText("CHK");
				buttonGroup1.add(jRadioButton1);
				jRadioButton1.setSelected(true);
				jRadioButton1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if (jRadioButton1.isSelected()) {
							setKeyType("CHK");
						}
					}
				});

				jRadioButton2 = new JRadioButton();
				jPanel5.add(jRadioButton2, new GridBagConstraints(
					0,
					5,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton2.setText("USK");
				buttonGroup1.add(jRadioButton2);
				jRadioButton2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if (jRadioButton2.isSelected()) {
							setKeyType("USK");
						}
					}
				});
				
				PlainDocument doc_InsertUri = new PlainDocument();
				tf_InsertUri = new JTextField(doc_InsertUri, "", 0);
				doc_InsertUri.addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {}

					public void insertUpdate(DocumentEvent e) {
						updated(e);
					}

					public void removeUpdate(DocumentEvent e) {
						updated(e);
					}
					
					private void updated(DocumentEvent e) {
						if (!ignoreUpdate) {
							Document doc = e.getDocument();
							try {
								projectList.get(selectedIndex).insertUri = doc.getText(0, doc.getLength());
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				});
				
				
				
				jPanel5.add(tf_InsertUri, new GridBagConstraints(
					1,
					4,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0),
					0,
					0));
				
				tf_InsertUri.setEnabled(false);

				cbox_announce = new JCheckBox();
				jPanel5.add(cbox_announce, new GridBagConstraints(
					0,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				cbox_announce.setText("Send announce to board:");
				cbox_announce.setSelected(true);
				cbox_announce.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						setAnnounce(cbox_announce.isSelected());
//						combo_announceBoard.setEnabled(state);
//						//jComboBox3.setEnabled(state);
					}
				});

				
				
				PlainDocument doc_announceBoard = new PlainDocument();
				//JTextField editor_announceBoard = new JTextField(doc_announceBoard, "", 0);
				doc_announceBoard.addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {}

					public void insertUpdate(DocumentEvent e) {
						updated(e);
					}

					public void removeUpdate(DocumentEvent e) {
						updated(e);
					}
					
					private void updated(DocumentEvent e) {
						if (!ignoreUpdate) {
							Document doc = e.getDocument();
							try {
								projectList.get(selectedIndex).announceTo = doc.getText(0, doc.getLength());
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				});
				
				ComboBoxModel jComboBox2Model = new DefaultComboBoxModel(
						new String[] { "test", "sites" });
				
				combo_announceBoard = new JComboBox();
				combo_announceBoard.setModel(jComboBox2Model);
				combo_announceBoard.setEditable(true);
				((JTextField)(combo_announceBoard.getEditor().getEditorComponent())).setDocument(doc_announceBoard);
				
				jPanel5.add(combo_announceBoard, new GridBagConstraints(
					1,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				combo_announceBoard.setSelectedIndex(0);


				jComboBox3 = new JComboBox();
				jPanel5.add(jComboBox3, new GridBagConstraints(
					3,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				ComboBoxModel jComboBox3Model = new DefaultComboBoxModel(
					new String[] { "<anonymouse>", "Item Two" });
				jComboBox3.setModel(jComboBox3Model);
				jComboBox3.setEnabled(false);
				jComboBox3.setEditable(true);

				jComboBox1 = new JComboBox();
				jPanel5.add(jComboBox1, new GridBagConstraints(
					1,
					8,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				ComboBoxModel jComboBox1Model = new DefaultComboBoxModel(
					new String[] { "PutDiskDir", "PutComplex (DDA)",
							"PutComplex (direct)" });
				jComboBox1.setModel(jComboBox1Model);
				jComboBox1.setSelectedIndex(2);

				jButton3 = new JButton();
				jPanel5.add(jButton3, new GridBagConstraints(
					2,
					8,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jButton3.setText("Start");
				//START >>  jRadioButton3
				jRadioButton3 = new JRadioButton();
				jPanel5.add(jRadioButton3, new GridBagConstraints(
					1,
					1,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton3.setText("generate index");
				buttonGroup2.add(jRadioButton3);
				jRadioButton3.setEnabled(false);
				//END <<  jRadioButton3
				//START >>  jRadioButton4
				jRadioButton4 = new JRadioButton();
				jPanel5.add(jRadioButton4, new GridBagConstraints(
					2,
					1,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton4.setText("File:");
				jRadioButton4.setSelected(true);
				buttonGroup2.add(jRadioButton4);
				//END <<  jRadioButton4
				//START >>  jButton9
				jButton9 = new JButton();
				jPanel5.add(jButton9, new GridBagConstraints(
					3,
					5,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jButton9.setText("generate");
				jButton9.setEnabled(false);
				jButton9.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						makeNewSSK();
					}
				});
				//END <<  jButton9
				//START >>  jLabel3
				jLabel3 = new JLabel();
				jPanel5.add(jLabel3, new GridBagConstraints(
					2,
					2,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jLabel3.setText("send as:");
				//END <<  jLabel3
				//START >>  jRadioButton5
				jRadioButton5 = new JRadioButton();
				jPanel5.add(jRadioButton5, new GridBagConstraints(
					0,
					4,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton5.setText("KSK");
				buttonGroup1.add(jRadioButton5);
				jRadioButton5.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if (jRadioButton5.isSelected()) {
							setKeyType("KSK");
						}
					}
				});
				//END <<  jRadioButton5
				//START >>  jRadioButton6
				jRadioButton6 = new JRadioButton();
				jPanel5.add(jRadioButton6, new GridBagConstraints(
					0,
					6,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton6.setText("SSK");
				//END <<  jRadioButton6
				jButton3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						doIt();
					}
				});

				cbox_defaultIndex.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						setIndex(cbox_defaultIndex.isSelected());
//						//jRadioButton3.setEnabled(state);
//						jRadioButton4.setEnabled(state);
//						tf_indexFilename.setEnabled(state);
//						jButton2.setEnabled(state);
					}
				});

				jButton1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						chooseInsertDir();
					}
				});

				jPanel6 = new JPanel();
				jPanel3.add(jPanel6, "jPanel6");

				jPanel7 = new JPanel();
				jPanel3.add(jPanel7, "jPanel7");

				jPanel9 = new JPanel();
				jPanel3.add(jPanel9, "jPanel9");

				jPanel1 = new JPanel();
				jPanel3.add(jPanel1, "jPanel1");

				jPanel8 = new JPanel();
				jPanel3.add(jPanel8, "jPanel8");
				BorderLayout jPanel8Layout = new BorderLayout();
				jPanel8.setLayout(jPanel8Layout);

				

				//START >>  jLabel2
				jLabel2 = new JLabel();
				jPanel8.add(jLabel2, BorderLayout.CENTER);
				jLabel2.setText("SimpleSiteTool");
				jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
				jLabel2.setFont(new java.awt.Font("Dialog", 3, 24));
				//END <<  jLabel2

				jPanel2 = new JPanel();
				jSplitPane2.add(jPanel2, JSplitPane.RIGHT);
				BorderLayout jPanel2Layout = new BorderLayout();
				jPanel2.setLayout(jPanel2Layout);

				jPanel4 = new JPanel();
				jPanel2.add(jPanel4, BorderLayout.SOUTH);

				jScrollPane2 = new JScrollPane();
				jPanel2.add(jScrollPane2, BorderLayout.CENTER);

				jTextPane1 = new JTextPane();
				jScrollPane2.setViewportView(jTextPane1);
				jTextPane1.setText("Log output\n");
				jTextPane1.setEditable(false);

				jButton4 = new JButton();
				jPanel4.add(jButton4);
				jButton4.setText("jButton4");
				jButton4.setEnabled(false);

				jComboBox4 = new JComboBox();
				jPanel4.add(jComboBox4);
				ComboBoxModel jComboBox4Model = new DefaultComboBoxModel(
					new String[] { "Item One", "Item Two" });
				jComboBox4.setModel(jComboBox4Model);
				jComboBox4.setEnabled(false);

				// START >> buttonGroup1
				buttonGroup1.add(jRadioButton6);
				//START >>  jRadioButton7
				jRadioButton7 = new JRadioButton();
				jPanel5.add(jRadioButton7, new GridBagConstraints(
					0,
					7,
					1,
					1,
					0.0,
					0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0),
					0,
					0));
				jRadioButton7.setText("TUK");
				//END <<  jRadioButton7
				jRadioButton6.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if (jRadioButton6.isSelected()) {
							setKeyType("SSK");
						}
					}
				});
				buttonGroup1.add(jRadioButton7);
				jRadioButton7.setEnabled(false);
				jRadioButton7.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						setKeyType("TUK");
					}
				});
				//START >>  jTextField4
				
				PlainDocument doc4 = new PlainDocument();
				jTextField4 = new JTextField(doc4, "", 0);
								
				doc4.addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {}

					public void insertUpdate(DocumentEvent e) {
						updated(e);
					}

					public void removeUpdate(DocumentEvent e) {
						updated(e);
					}
					
					private void updated(DocumentEvent e) {
						if (!ignoreUpdate) {
							Document doc = e.getDocument();
							try {
								projectList.get(selectedIndex).sitePath = doc.getText(0, doc.getLength());
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				
				});
				
				
				jPanel5.add(jTextField4, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				jTextField4.setEnabled(false);
				
				//END <<  jTextField4
				//START >>  jSpinner1
				SpinnerNumberModel jSpinner1Model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				jSpinner1 = new JSpinner();
				jPanel5.add(jSpinner1, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jSpinner1.setModel(jSpinner1Model);
				jSpinner1.setEnabled(false);
				jSpinner1.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent evt) {
						if (!ignoreUpdate) {
							projectList.get(selectedIndex).edition = ((Integer)(jSpinner1.getValue())).intValue();
							projectList.get(selectedIndex).modify();
							enableSave();
						}
					}
				});
//				((NumberEditor)(jSpinner1.getEditor()))..getDocument().addDocumentListener(new DocumentListener() {
//
//					public void changedUpdate(DocumentEvent e) {}
//
//					public void insertUpdate(DocumentEvent e) {
//						updated(e);
//					}
//
//					public void removeUpdate(DocumentEvent e) {
//						updated(e);
//					}
//					
//					private void updated(DocumentEvent e) {
//						if (!ignoreUpdate) {
//							Document doc = e.getDocument();
//							try {
//								projectList.get(selectedIndex).edition = Integer.parseInt(doc.getText(0, doc.getLength()));
//							} catch (BadLocationException e1) {
//								e1.printStackTrace();
//							}
//							projectList.get(selectedIndex).modify();
//							enableSave();
//						}
//					}
//				});
				
				//END <<  jSpinner1
				//START >>  jCheckBox1
				jCheckBox1 = new JCheckBox();
				jPanel5.add(jCheckBox1, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jCheckBox1.setText("Create persistent request");
				jCheckBox1.setSelected(true);
				jCheckBox1.setEnabled(false);
				//END <<  jCheckBox1
				// END << buttonGroup1

				jList2Model = new ProjectListModel();
				//jList2Model.addElement("Hallo");
				//jList2Model.addElement("Walter");
				
				jList2 = new JListMutable();
				jList2.setModel(jList2Model);
				jList2.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				JTextField tf = new JTextField();
				tf.setBorder(BorderFactory.createLineBorder(Color.black));
				jList2.setListCellEditor(new DefaultListCellEditor(tf));
				jList2.setVisibleRowCount(-1);
			
				
				
				jList2.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent evt) {
						if (!evt.getValueIsAdjusting()) {
							setSelectedIndex(jList2.getSelectedIndex());
						} 
					}
				});
				
				jScrollPane1.setViewportView(jList2);

				// START >> jScrollPane1

				// START >> jList2
				// END << jList2
				// END << jScrollPane1
				// START >> jSplitPane2
				// START >> jScrollPane3
				// START >> jPanel3
				// START >> jPanel5
				// START >> jTextField1
				// END << jTextField1
				// START >> jButton1
				// END << jButton1
				// END << jPanel5
				// START >> jPanel6
				// START >> jCheckBox1
				// END << jCheckBox1
				// START >> jTextField2
				// END << jTextField2
				// START >> jButton2
				// END << jButton2
				// END << jPanel6
				// START >> jPanel7
				// START >> jRadioButton1
				// END << jRadioButton1
				// START >> jRadioButton2
				// END << jRadioButton2
				// START >> jTextField3
				// END << jTextField3
				// END << jPanel7
				// START >> jPanel9
				// START >> jCheckBox2
				// END << jCheckBox2
				// START >> jComboBox2
				// END << jComboBox2
				// START >> jComboBox3
				// END << jComboBox3
				// END << jPanel9
				// START >> jPanel1
				// START >> jComboBox1
				// END << jComboBox1
				// START >> jButton3
				// END << jButton3
				// END << jPanel1
				// START >> jPanel8
				// END << jPanel8
				// END << jPanel3
				// END << jScrollPane3
				// START >> jPanel2
				// START >> jPanel4
				// START >> jButton4
				// END << jButton4
				// START >> jComboBox4
				// END << jComboBox4
				// END << jPanel4
				// START >> jScrollPane2
				// START >> jTextArea1
				// END << jTextArea1
				// END << jScrollPane2
				// END << jPanel2
				// END << jSplitPane2
				// END << jSplitPane1
			}
			//START >>  jPanel10
			jPanel10 = new JPanel();
			FlowLayout jPanel10Layout = new FlowLayout();
			jPanel10Layout.setAlignment(FlowLayout.LEFT);
			jPanel10.setLayout(jPanel10Layout);
			this.add(jPanel10, BorderLayout.NORTH);
			//START >>  jButton5
			btn_New = new JButton();
			jPanel10.add(btn_New);
			btn_New.setText("New");
			btn_New.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jList2Model.addNewElement();
				}
			});
			//END <<  jButton5
			//START >>  jButton6
			btn_Del = new JButton();
			jPanel10.add(btn_Del);
			btn_Del.setText("Del");
			btn_Del.setEnabled(false);
			btn_Del.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jList2Model.remove(jList2.getSelectedIndex());
				}
			});
			//END <<  jButton6
			//START >>  jButton7
			btn_Save = new JButton();
			jPanel10.add(btn_Save);
			btn_Save.setText("Save");
			btn_Save.setEnabled(false);
			btn_Save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					saveProjects();
				}
			});
			//END <<  jButton7
			//START >>  jButton8
			btn_Revert = new JButton();
			jPanel10.add(btn_Revert);
			btn_Revert.setText("Revert");
			btn_Revert.setEnabled(false);
			btn_Revert.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					revertProjects();
				}
			});
			//END <<  jButton8
			//END <<  jPanel10
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setAnnounce(boolean state) {
		combo_announceBoard.setEnabled(state);
		//jComboBox3.setEnabled(state);
	}
	
	private void setIndex(boolean state) {
		//jRadioButton3.setEnabled(state);
		jRadioButton4.setEnabled(state);
		tf_indexFilename.setEnabled(state);
		jButton2.setEnabled(state);	
	}
	
	private void setKeyType(String qkeytype) {
		String keyType = qkeytype;
		if ("KSK".equals(keyType)) {
			tf_InsertUri.setEnabled(true);
			jTextField4.setEnabled(false);
			jSpinner1.setEnabled(false);
			jButton9.setEnabled(false);
		} else if ("USK".equals(keyType)) {
			tf_InsertUri.setEnabled(true);
			jTextField4.setEnabled(true);
			jSpinner1.setEnabled(false);
			jButton9.setEnabled(true);
		} else if ("SSK".equals(keyType)) {
			tf_InsertUri.setEnabled(true);
			jTextField4.setEnabled(true);
			jSpinner1.setEnabled(true);
			jButton9.setEnabled(true);
		} else {
			// default to chk 
			tf_InsertUri.setEnabled(false);
			jTextField4.setEnabled(false);
			jSpinner1.setEnabled(false);
			jButton9.setEnabled(false);
			keyType = "CHK";
            projectList.get(selectedIndex).modify();
		}
		
        if (!ignoreUpdate)  {
        	projectList.get(selectedIndex).keyType = keyType;
            projectList.get(selectedIndex).modify();
            
        	enableSave();
        }
	}

	private void enableSave() {
		btn_Save.setEnabled(true);
		btn_Revert.setEnabled(true);
	}

	private void makeNewSSK() {

		Runnable newKeyRunner = new Runnable() {
			public void run() {

				FCPConnection conn = null;
				try {
					SimpleSocketFactory sf = new SimpleSocketFactory(pluginRespinator.getFCPAdress());

					conn = new FCPConnection(sf);

					GenerateSSK cmd = new GenerateSSK();

					cmd.setAutoIdentifier("sst-");

					conn.send(cmd);

					boolean goon = true;
					NodeMessage pmsg;

					while (goon) {

						// System.err.println("wait for reply...");
						addTodoInfo("read:...");
						pmsg = conn.readEndMessage();
						addTodoInfo(pmsg.toString() + '\n');

						if (pmsg.isMessageName("ProtocolError")) {
							goon = false;
						}

						if (pmsg.isMessageName("SSKKeypair")) {
							String key = pmsg.getKeyString("InsertURI");
							tf_InsertUri.setText(key);
							projectList.get(selectedIndex).insertUri = key;
				            projectList.get(selectedIndex).modify();
							goon = false;
						}
					}
				} catch (Exception e) {
					addErrorLog("hu?", e);
				} finally {
					conn.close();
				}

				jButton9.setEnabled(true);
			}
		};

		Thread newKeyThread = new Thread(newKeyRunner);
		jButton9.setEnabled(false);
		newKeyThread.start();
	}

	private void chooseInsertDir() {

		final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose dir to insert");
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileSeparator = System.getProperty("file.separator");
            File file = fc.getSelectedFile();
            
            jTextField1.setText(file.getPath() + fileSeparator);
            projectList.get(selectedIndex).sourceDir = file.getPath();
            projectList.get(selectedIndex).modify();
        }
	}

	private void setSelectedIndex(int selectedindex) {
		ignoreUpdate = true;
		selectedIndex = selectedindex;
		
		if (selectedIndex < 0) {
			btn_Del.setEnabled(false);
			jPanel3Layout.show(jPanel3, "jPanel8");
		} else {
			btn_Del.setEnabled(true);
			jPanel3Layout.show(jPanel3, "jPanel5");
			jTextField1.setText(projectList.get(selectedIndex).sourceDir);
			tf_InsertUri.setText(projectList.get(selectedIndex).insertUri);
			tf_indexFilename.setText(projectList.get(selectedIndex).indexFilename);
			combo_announceBoard.getEditor().setItem(projectList.get(selectedIndex).announceTo);
			jSpinner1.setValue(new Integer(projectList.get(selectedIndex).edition));
			
			setIndex(projectList.get(selectedIndex).index);
			cbox_defaultIndex.setSelected(projectList.get(selectedIndex).index);
			
			setAnnounce(projectList.get(selectedIndex).announce);
			cbox_announce.setSelected(projectList.get(selectedIndex).announce);
			
			String keytype = projectList.get(selectedIndex).keyType;
			if ("KSK".equals(keytype)) {
				jRadioButton5.setSelected(true);
			} else if ("USK".equals(keytype)) {
				jRadioButton2.setSelected(true);
			} else if ("SSK".equals(keytype)) {
				jRadioButton6.setSelected(true);
			} else {
				// default to chk 
				jRadioButton1.setSelected(true);
			}
			setKeyType(keytype);
		}
		ignoreUpdate = false;
	}

	private void saveProjects() {
		ignoreUpdate = true;
		storage.commit();
		btn_New.grabFocus();
		btn_Save.setEnabled(false);
		btn_Revert.setEnabled(false);
		ignoreUpdate = false;
	}

	private void revertProjects() {
		storage.rollback();
		btn_Save.setEnabled(false);
		btn_Revert.setEnabled(false);
		setSelectedIndex(-1);
	}

	private void doIt() {

		boolean paramsOk = true;

		final File fdir;
		
		// check dir
		String sdir = jTextField1.getText();

		if (sdir.trim().length() == 0) {
			paramsOk = false;
			addErrorLog("dir can't be emty.\n");
			fdir = null;
		} else {
			fdir = new File(sdir);
			if (!fdir.isDirectory()) {
				paramsOk = false;
				addErrorLog("dir does not exists.\n");
			}
		}

		if (!paramsOk)
			return;

		Runnable doItRunner = new Runnable() {
			public void run() {

				FCPConnection conn = null;
				try {
					SimpleSocketFactory sf = new SimpleSocketFactory(pluginRespinator.getFCPAdress());

					
					conn = new FCPConnection(sf);
					
					FCPClientPut cmd;
					int cmdType = jComboBox1.getSelectedIndex();
					
					if (cmdType == 0) {
						cmd = new ClientPutDiskDir();
						((ClientPutDiskDir)cmd).setDefaultName(tf_indexFilename.getText());
						((ClientPutDiskDir)cmd).setDiskDir(fdir.getAbsolutePath());
						((ClientPutDiskDir)cmd).setAllowUnreadableFiles(false);
					} else {
						cmd = new ClientPutComplexDir();
						((ClientPutComplexDir)cmd).setDefaultName(tf_indexFilename.getText());
					}
						
					SimpleDirParser sdp = new SimpleDirParser(fdir);
					if (cmdType == 1) {
						//((ClientPutDiskDir)cmd); parse dda
						sdp.parse((ClientPutComplexDir)cmd, true);
					}
					
					if (cmdType == 2) {
						//((ClientPutDiskDir)cmd); parse direct
						sdp.parse((ClientPutComplexDir)cmd, false);
					}
			
					cmd.setAutoIdentifier("SimpleSiteTool-");
					cmd.setVerbosityAll();
					cmd.setRetryForever();
					cmd.setPriority(PriorityClass.INTERACTIVE);
					cmd.setURI("CHK@");
					cmd.setCompress();
					cmd.setEarlyEncode(true);
					//cmd.setGenerateCHKOnly(false);
					cmd.setClientToken("SimpleSiteTool");
					cmd.setPersistenceForever();
					cmd.setGlobal();
					
					conn.send(cmd);

					addTodoInfo("Insert sent.\n");

					String key = null;
					boolean goon = true;
					NodeMessage pmsg;

					while (goon) {

						// System.err.println("wait for reply...");
						addTodoInfo("read:...");
						pmsg = conn.readEndMessage();
						addTodoInfo(pmsg.toString() + '\n');

						if (pmsg.isMessageName("ProtocolError")) {
							goon = false;
						}

						if (pmsg.isMessageName("PutSuccessful")) {
							key = pmsg.getKeyString("URI");
							goon = false;
						}
						
						if (pmsg.isMessageName("PersistentPutDir")) {
							goon = false;
						}

						if (pmsg.isMessageName("PutFailed")) {
							goon = false;
						}
					}

					addTodoInfo("...Insert Done.\n");
					
					StringBuilder sb = new StringBuilder();
					
					sb.append("Name:        ");
					sb.append('\n');
					sb.append("Location:    ");
					sb.append(key);
					sb.append('\n');
					sb.append("ActiveLink:  ");
					sb.append('\n');
					sb.append("Description: ");
					sb.append('\n');
					sb.append("Categories:  ");
					sb.append('\n');
					sb.append("\n\nSite and anounce created with SimpleSiteTool\n");
					
					pluginRespinator.makeNewMessage("test", "Freesite announce: <sitetitle>", sb.toString());

					addTodoInfo("Announce...Sent.\n");

				} catch (Exception e) {
					addErrorLog("hu?", e);
				} finally {
					conn.close();
				}

				jButton3.setEnabled(true);
			}
		};

		Thread doItThread = new Thread(doItRunner);
		jButton3.setEnabled(false);
		doItThread.start();
	}

	private void addErrorLog(String text, Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		sw.write(text);
		if (!text.endsWith("\n"))
			sw.append('\n');
		t.printStackTrace(pw);
		pw.close();
		addErrorLog(sw.toString());
	}

	private void addErrorLog(String text) {
		SimpleAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setForeground(as, Color.RED);
		StyleConstants.setBold(as, true);
		addLog(text, as);
	}

	private void addTodoInfo(String text) {
		SimpleAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setForeground(as, Color.darkGray);
		StyleConstants.setBold(as, true);
		addLog(text, as);
	}

	private void addLog(String text, AttributeSet as) {
		try {
			jTextPane1.getDocument().insertString(
					jTextPane1.getDocument().getLength(), text, as);
			jTextPane1.setCaretPosition(jTextPane1.getDocument().getLength());
		} catch (BadLocationException e) {
			// can this normally happen?
			// ignore e.printStackTrace();
		}

	}
	
	private Storage getStorage() {
		return pluginRespinator.getDataStorage("SimpleSiteTool");
	}

	public boolean canStopPlugin() {
		return true;
	}

	public JPanel getPluginPanel() {
		return this;
	}

	public void startPlugin(PluginRespinator pr) {
		pluginRespinator = pr;
		
		storage = getStorage();
		
		projectList = (IPersistentList<ProjectItem>) storage.getRoot();
        if (projectList == null) {
            // Storage was not initialized yet
        	projectList  = storage.createScalableList();

            storage.setRoot(projectList);
            storage.commit(); // commit transaction
        }
        initGUI();
        setSelectedIndex(-1);
	}

	public void stopPlugin() {
		getStorage().close();
	}
	
	public class ProjectListModel extends AbstractListModel implements MutableListModel {

		public boolean isCellEditable(int index) {
			return true;
		}

		public void setValueAt(Object value, int index) {
			projectList.get(index).projectName = (String) value;
			projectList.get(index).modify();
		}

		public Object getElementAt(int index) {
			return projectList.get(index).projectName;
		}

		public int getSize() {
			return projectList.size();
		}
		
		public void addNewElement() {
			int index = projectList.size();
			ProjectItem pi = new ProjectItem();
			pi.makePersistent(storage);
			projectList.add(pi);
			projectList.modify();
			enableSave();
			fireIntervalAdded(this, index, index);
		}
		
		public void remove(int index) {
			projectList.remove(index);
			projectList.modify();
			enableSave();
			fireIntervalRemoved(this, index, index);
	    }
	}
}
