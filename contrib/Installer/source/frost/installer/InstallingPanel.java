/*
 * Created on Jan 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.installer;

import java.applet.AppletContext;
import java.awt.LayoutManager;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.File;
import java.net.*;
import java.net.URL;
import java.util.*;
import java.util.Properties;

import javax.swing.JPanel;

/**
 * 
 */
public class InstallingPanel extends JPanel {
	
	private InstallerApplet applet;
	private URL jnlpRemoteLocation = null;
	private URL dtdLocation = null;
	private File jnlpLocalDirectory = null;

	private javax.swing.JPanel topPanel = null;
	private javax.swing.JPanel bottomPanel = null;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JButton backButton = null;
	private javax.swing.JTextArea messagesTextArea = null;
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JProgressBar jProgressBar = null;
	private javax.swing.JLabel outputFolderLabel = null;
	/**
	 * 
	 */
	protected InstallingPanel(InstallerApplet applet) {
		super();
		this.applet = applet;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	protected void initialize() {
        java.awt.GridBagConstraints consGridBagConstraints15 = new java.awt.GridBagConstraints();
        java.awt.GridBagConstraints consGridBagConstraints14 = new java.awt.GridBagConstraints();
        consGridBagConstraints14.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints14.gridy = 0;
        consGridBagConstraints14.gridx = 0;
        consGridBagConstraints14.fill = java.awt.GridBagConstraints.HORIZONTAL;
        consGridBagConstraints15.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints15.gridy = 1;
        consGridBagConstraints15.gridx = 0;
        consGridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
        consGridBagConstraints15.weightx = 1.0D;
        consGridBagConstraints14.weightx = 1.0D;
        consGridBagConstraints14.gridwidth = 0;
        consGridBagConstraints15.weighty = 1.0D;
        this.setLayout(new java.awt.GridBagLayout());
        this.add(getTopPanel(), consGridBagConstraints14);
        this.add(getBottomPanel(), consGridBagConstraints15);
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
			java.awt.GridBagConstraints consGridBagConstraints17 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints16 = new java.awt.GridBagConstraints();
			consGridBagConstraints17.insets = new java.awt.Insets(0,25,10,0);
			consGridBagConstraints17.gridy = 1;
			consGridBagConstraints17.gridx = 0;
			consGridBagConstraints17.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints17.weightx = 1.0D;
			consGridBagConstraints16.insets = new java.awt.Insets(5,10,5,5);
			consGridBagConstraints16.gridy = 0;
			consGridBagConstraints16.gridx = 0;
			consGridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
			topPanel.setLayout(new java.awt.GridBagLayout());
			topPanel.add(getJLabel(), consGridBagConstraints16);
			topPanel.add(getJLabel1(), consGridBagConstraints17);
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
			java.awt.GridBagConstraints consGridBagConstraints3 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints6 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints1 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints2 = new java.awt.GridBagConstraints();
			consGridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints1.weightx = 1.0;
			consGridBagConstraints1.gridx = 0;
			consGridBagConstraints1.gridy = 1;
			consGridBagConstraints1.gridwidth = 2;
			consGridBagConstraints1.insets = new java.awt.Insets(0,25,5,25);
			consGridBagConstraints2.gridy = 0;
			consGridBagConstraints2.gridx = 0;
			consGridBagConstraints2.insets = new java.awt.Insets(10,25,5,25);
			consGridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints6.weighty = 1.0;
			consGridBagConstraints6.weightx = 1.0;
			consGridBagConstraints6.insets = new java.awt.Insets(5,25,15,25);
			consGridBagConstraints6.gridwidth = 2;
			consGridBagConstraints6.gridx = 0;
			consGridBagConstraints6.gridy = 3;
			consGridBagConstraints3.ipady = 0;
			consGridBagConstraints3.ipadx = 0;
			consGridBagConstraints3.gridy = 4;
			consGridBagConstraints3.gridx = 0;
			consGridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints3.insets = new java.awt.Insets(6,0,6,30);
			consGridBagConstraints3.weightx = 1.0D;
			bottomPanel.setLayout(new java.awt.GridBagLayout());
			bottomPanel.add(getBackButton(), consGridBagConstraints3);
			bottomPanel.add(getJScrollPane(), consGridBagConstraints6);
			bottomPanel.add(getJProgressBar(), consGridBagConstraints1);
			bottomPanel.add(getOutputFolderLabel(), consGridBagConstraints2);
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
			jLabel.setText("Installing");
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
			jLabel1.setText("Please wait while Frost is being installed.");
			jLabel1.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return jLabel1;
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
			backButton.setName("backButton");
			backButton.setEnabled(false);
			backButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applet.installingBackButtonPressed();
				}
			});
		}
		return backButton;
	}
	/**
	 * @param url
	 */
	void setJnlpRemoteLocation(URL url) {
		jnlpRemoteLocation = url;
	}

	/**
	 * 
	 */
	void installApplication() {
		getBackButton().setEnabled(false);
		getOutputFolderLabel().setText("Output folder: " + jnlpLocalDirectory.toString());
		JnlpFile jnlpFile = new JnlpFile();
		jnlpFile.setRemoteLocation(jnlpRemoteLocation);
		jnlpFile.setDtdLocation(dtdLocation);
		try {
			getMessagesTextArea().append("Downloading jnlp file... ");
			getJProgressBar().setValue(10);
			jnlpFile.download();
			getMessagesTextArea().append("Done\nParsing document... ");
			getJProgressBar().setValue(30);
			jnlpFile.parseDocument();
			getMessagesTextArea().append("Done\nChecking validity... ");
			getJProgressBar().setValue(40);
			if (jnlpFile.isValid()) {
				getMessagesTextArea().append("Done\nReplacing codebase... ");
				getJProgressBar().setValue(50);
				jnlpFile.replaceCodebase(jnlpLocalDirectory.toURL());
				getMessagesTextArea().append("Done\nWriting local copy... ");
				getJProgressBar().setValue(60);
				File localFile = jnlpFile.writeToLocalDirectory(jnlpLocalDirectory);	
				getMessagesTextArea().append("Done\nLaunching Java Web Start... ");
				getJProgressBar().setValue(70);
				applet.getAppletContext().showDocument(localFile.toURL());
			} else {
				//TODO: let the user try again
				getMessagesTextArea().append("Not Done \nThe document is not valid");
			}
		} catch (Exception exception) {
			//TODO: let the user try again and deal with timeouts (downloading)
			getMessagesTextArea().append("Exception: \n" + exception.getMessage());
		}
	}
	
	/**
	 * 
	 */
	void clear() {
		getJProgressBar().setValue(0);
		getMessagesTextArea().setText("");
	}
	
	

	/**
	 * @param file
	 */
	void setJnlpLocalDirectory(File file) {
		jnlpLocalDirectory = file;
	}

	/**
	 * This method initializes messagesTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getMessagesTextArea() {
		if(messagesTextArea == null) {
			messagesTextArea = new javax.swing.JTextArea();
		}
		return messagesTextArea;
	}
	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getMessagesTextArea());
		}
		return jScrollPane;
	}

	/**
	 * @param url
	 */
	void setDtdLocation(URL url) {
		dtdLocation = url;
	}

	/**
	 * This method initializes jProgressBar
	 * 
	 * @return javax.swing.JProgressBar
	 */
	private javax.swing.JProgressBar getJProgressBar() {
		if(jProgressBar == null) {
			jProgressBar = new javax.swing.JProgressBar();
			jProgressBar.setStringPainted(true);
		}
		return jProgressBar;
	}
	/**
	 * This method initializes outputFolderLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getOutputFolderLabel() {
		if(outputFolderLabel == null) {
			outputFolderLabel = new javax.swing.JLabel();
			outputFolderLabel.setText("Output folder:");
			outputFolderLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return outputFolderLabel;
	}

}
