/*
 * Created on Jan 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.installer; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

/**
 * 
 */
public class LocationPanel extends JPanel {
	
	private InstallerApplet applet;
	private JFileChooser fileChooser = null;
	private javax.swing.JPanel topPanel = null;
	private javax.swing.JPanel bottomPanel = null;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JLabel jLabel2 = null;
	private javax.swing.JButton backButton = null;
	private javax.swing.JButton installButton = null;
	private javax.swing.JTextArea jTextArea = null;
	private javax.swing.JPanel folderPanel = null;
	private javax.swing.JButton browseButton = null;
	private javax.swing.JTextField pathTextField = null;
	
	/**
	 * 
	 */
	protected LocationPanel(InstallerApplet applet) {
		super();
		this.applet = applet;
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	protected void initialize() {
        java.awt.GridBagConstraints consGridBagConstraints1 = new java.awt.GridBagConstraints();
        java.awt.GridBagConstraints consGridBagConstraints2 = new java.awt.GridBagConstraints();
        consGridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints1.gridy = 0;
        consGridBagConstraints1.gridx = 0;
        consGridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        consGridBagConstraints1.gridwidth = 0;
        consGridBagConstraints1.weightx = 1.0D;
        consGridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints2.gridy = 1;
        consGridBagConstraints2.gridx = 0;
        consGridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        consGridBagConstraints2.weighty = 1.0D;
        consGridBagConstraints2.weightx = 1.0D;
        this.setLayout(new java.awt.GridBagLayout());
        this.add(getTopPanel(), consGridBagConstraints1);
        this.add(getBottomPanel(), consGridBagConstraints2);
        this.setBounds(0, 0, 500, 340);
        this.setPreferredSize(new java.awt.Dimension(500,340));
		this.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray,1));	
	}
	
	/**
	 * This method initializes topPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getTopPanel() {
		if(topPanel == null) {
			topPanel = new javax.swing.JPanel();
			java.awt.GridBagConstraints consGridBagConstraints5 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints4 = new java.awt.GridBagConstraints();
			consGridBagConstraints5.insets = new java.awt.Insets(0,25,10,0);
			consGridBagConstraints5.gridy = 1;
			consGridBagConstraints5.gridx = 0;
			consGridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints5.weighty = 0.0D;
			consGridBagConstraints5.weightx = 1.0D;
			consGridBagConstraints4.insets = new java.awt.Insets(5,10,5,5);
			consGridBagConstraints4.gridy = 0;
			consGridBagConstraints4.gridx = 0;
			consGridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			topPanel.setLayout(new java.awt.GridBagLayout());
			topPanel.add(getJLabel(), consGridBagConstraints4);
			topPanel.add(getJLabel1(), consGridBagConstraints5);
			topPanel.setBackground(java.awt.Color.white);
			topPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0,0,1,0,java.awt.Color.gray));
		}
		return topPanel;
	}
	
	/**
	 * This method initializes bottomPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getBottomPanel() {
		if(bottomPanel == null) {
			bottomPanel = new javax.swing.JPanel();
			java.awt.GridBagConstraints consGridBagConstraints6 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints8 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints9 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints10 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints11 = new java.awt.GridBagConstraints();
			consGridBagConstraints11.insets = new java.awt.Insets(5,25,50,25);
			consGridBagConstraints11.gridx = 0;
			consGridBagConstraints11.gridy = 3;
			consGridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints11.gridwidth = 2;
			consGridBagConstraints8.insets = new java.awt.Insets(6,0,6,10);
			consGridBagConstraints8.gridy = 4;
			consGridBagConstraints8.gridx = 0;
			consGridBagConstraints8.weightx = 1.0D;
			consGridBagConstraints8.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints6.insets = new java.awt.Insets(10,25,5,0);
			consGridBagConstraints6.gridy = 0;
			consGridBagConstraints6.gridx = 0;
			consGridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints6.gridwidth = 2;
			consGridBagConstraints9.insets = new java.awt.Insets(6,0,6,30);
			consGridBagConstraints9.gridy = 4;
			consGridBagConstraints9.gridx = 1;
			consGridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints10.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints10.weighty = 1.0;
			consGridBagConstraints10.weightx = 1.0;
			consGridBagConstraints10.insets = new java.awt.Insets(5,25,5,25);
			consGridBagConstraints10.gridx = 0;
			consGridBagConstraints10.gridy = 1;
			consGridBagConstraints10.gridwidth = 2;
			bottomPanel.setLayout(new java.awt.GridBagLayout());
			bottomPanel.add(getJLabel2(), consGridBagConstraints6);
			bottomPanel.add(getBackButton(), consGridBagConstraints8);
			bottomPanel.add(getInstallButton(), consGridBagConstraints9);
			bottomPanel.add(getJTextArea(), consGridBagConstraints10);
			bottomPanel.add(getFolderPanel(), consGridBagConstraints11);
			bottomPanel.setBackground(new java.awt.Color(236,233,216));
			bottomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0,java.awt.Color.white));
		}
		return bottomPanel;
	}
	
	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if(jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText("Choose Install Location");
		}
		return jLabel;
	}
	
	/**
	 * This method initializes jLabel1
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setText("Choose the folder in which to install Frost.");
			jLabel1.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return jLabel1;
	}
	
	/**
	 * This method initializes jLabel2
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel2() {
		if(jLabel2 == null) {
			jLabel2 = new javax.swing.JLabel();
			jLabel2.setText("Setup will install Frost in the following folder.");
			jLabel2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return jLabel2;
	}
	
	/**
	 * This method initializes backButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getBackButton() {
		if (backButton == null) {
			backButton = new javax.swing.JButton();
			backButton.setText("< Back");
			backButton.setBackground(new java.awt.Color(243, 243, 239));
			backButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applet.locationBackButtonPressed();
				}
			});
		}
		return backButton;
	}
	
	/**
	 * This method initializes installButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getInstallButton() {
		if (installButton == null) {
			installButton = new javax.swing.JButton();
			installButton.setBackground(new java.awt.Color(243, 243, 239));
			installButton.setText("Install");
			installButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applet.locationInstallButtonPressed();
				}
			});
		}
		return installButton;
	}
	
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if(jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setText("To install in a different folder, click Browse and select another folder. Click Install to continue.");
			jTextArea.setBackground(new java.awt.Color(236,233,216));
			jTextArea.setWrapStyleWord(true);
			jTextArea.setLineWrap(true);
			jTextArea.setEditable(false);
			jTextArea.setEnabled(true);
		}
		return jTextArea;
	}
	
	/**
	 * This method initializes folderPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getFolderPanel() {
		if(folderPanel == null) {
			folderPanel = new javax.swing.JPanel();
			java.awt.GridBagConstraints consGridBagConstraints12 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints13 = new java.awt.GridBagConstraints();
			consGridBagConstraints12.insets = new java.awt.Insets(6,15,14,15);
			consGridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints12.weightx = 1.0;
			consGridBagConstraints12.gridy = 0;
			consGridBagConstraints12.gridx = 0;
			consGridBagConstraints13.insets = new java.awt.Insets(6,0,14,15);
			consGridBagConstraints13.gridy = 0;
			consGridBagConstraints13.gridx = 1;
			consGridBagConstraints13.anchor = java.awt.GridBagConstraints.EAST;
			folderPanel.setLayout(new java.awt.GridBagLayout());
			folderPanel.add(getPathTextField(), consGridBagConstraints12);
			folderPanel.add(getBrowseButton(), consGridBagConstraints13);
			folderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Destination Folder ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12), new java.awt.Color(102,102,255)));
			folderPanel.setBackground(new java.awt.Color(236,233,216));
		}
		return folderPanel;
	}
	
	/**
	 * This method initializes browseButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getBrowseButton() {
		if (browseButton == null) {
			browseButton = new javax.swing.JButton();
			browseButton.setText("Browse...");
			browseButton.setBackground(new java.awt.Color(243, 243, 239));
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseButtonPressed();
				}
			});
		}
		return browseButton;
	}
	
	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getPathTextField() {
		if(pathTextField == null) {
			pathTextField = new javax.swing.JTextField();
			pathTextField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					pathChanged();
				}
				public void insertUpdate(DocumentEvent e) {
					pathChanged();
				}
				public void removeUpdate(DocumentEvent e) {
					pathChanged();
				}
			});
		}
		return pathTextField;
	}
	
	/**
	 * 
	 */
	private void pathChanged() {
		File file = new File(getPathTextField().getText());
		if (file.isAbsolute() && !file.isFile()) {
			//We only enable the button if the path is absolute and it is not a file
			getInstallButton().setEnabled(true);	
		} else {
			getInstallButton().setEnabled(false);	
		}					 	
	}
	
	/**
	 * @return
	 */
	String getPath() {
		return getPathTextField().getText();
	}

	/**
	 * @param string
	 */
	void setPath(String string) {
		getPathTextField().setText(string);
	}
	
	/**
	 * 
	 */
	private void browseButtonPressed() {
		File file = new File(getPathTextField().getText());
		File parent = file.getParentFile();
		//We go up the path until we reach the first directory that exists or we
		//arrive at the root dir (in case the content of the textField is invalid)
		while (!file.exists() && parent != null) {
			file = parent;
			parent = file.getParentFile();
		}
		getFileChooser().setCurrentDirectory(file);
		int result = getFileChooser().showDialog(this, "OK");
		if (result == JFileChooser.APPROVE_OPTION) {
			//The user chose "OK". We compose the new path
			String newPath = getFileChooser().getSelectedFile().getAbsolutePath();
			try {
				newPath = getFileChooser().getSelectedFile().getCanonicalPath();
			} catch (IOException exception) {
				//If it can't get the canonical path, the normal one will have to do.
			}
			String separator = System.getProperty("file.separator");
			if (newPath.lastIndexOf(separator) != newPath.length() - separator.length()) {
				newPath = newPath + separator;
			} 
			newPath = newPath + "Frost";
			getPathTextField().setText(newPath);
		}
	}

	/**
	 * @return
	 */
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle("Select the directory to install Frost in:");
		}
		return fileChooser;
	}
}