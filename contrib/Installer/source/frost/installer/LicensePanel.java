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
import java.net.URL;

import javax.swing.*;


/**
 * 
 */
public class LicensePanel extends JPanel {
	private InstallerApplet applet;
	private URL licenseURL = null;
	private javax.swing.JPanel topPanel = null;
	private javax.swing.JPanel bottomPanel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JLabel jLabel2 = null;
	private javax.swing.JButton backButton = null;
	private javax.swing.JButton agreeButton = null;
	private javax.swing.JLabel jLabel3 = null;
	private javax.swing.JTextArea jTextArea = null;
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JTextArea licenseTextArea = null;
	/**
	 * 
	 */
	protected LicensePanel(InstallerApplet applet) {
		super();
		this.applet = applet;
	}	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	protected void initialize() {
        java.awt.GridBagConstraints consGridBagConstraints2 = new java.awt.GridBagConstraints();
        java.awt.GridBagConstraints consGridBagConstraints1 = new java.awt.GridBagConstraints();
        consGridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        consGridBagConstraints2.weighty = 1.0;
        consGridBagConstraints2.weightx = 1.0;
        consGridBagConstraints2.gridy = 1;
        consGridBagConstraints2.gridx = 0;
        consGridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        consGridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        consGridBagConstraints1.weighty = 0.0D;
        consGridBagConstraints1.weightx = 1.0;
        consGridBagConstraints1.gridy = 0;
        consGridBagConstraints1.gridx = 0;
        consGridBagConstraints1.gridwidth = 0;
        this.setLayout(new java.awt.GridBagLayout());
        this.add(getTopPanel(), consGridBagConstraints1);
        this.add(getBottomPanel(), consGridBagConstraints2);
        this.setSize(500, 340);
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
			java.awt.GridBagConstraints consGridBagConstraints3 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints4 = new java.awt.GridBagConstraints();
			consGridBagConstraints3.insets = new java.awt.Insets(5,10,5,5);
			consGridBagConstraints3.gridy = 0;
			consGridBagConstraints3.gridx = 0;
			consGridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints4.insets = new java.awt.Insets(0,25,10,0);
			consGridBagConstraints4.gridy = 1;
			consGridBagConstraints4.gridx = 0;
			consGridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints4.weightx = 1.0D;
			topPanel.setLayout(new java.awt.GridBagLayout());
			topPanel.add(getJLabel1(), consGridBagConstraints3);
			topPanel.add(getJLabel2(), consGridBagConstraints4);
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
			java.awt.GridBagConstraints consGridBagConstraints5 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints6 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints7 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints9 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints8 = new java.awt.GridBagConstraints();
			consGridBagConstraints7.insets = new java.awt.Insets(5,25,5,0);
			consGridBagConstraints7.gridy = 0;
			consGridBagConstraints7.gridx = 0;
			consGridBagConstraints7.gridwidth = 2;
			consGridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints5.insets = new java.awt.Insets(6,0,6,10);
			consGridBagConstraints5.gridy = 3;
			consGridBagConstraints5.gridx = 0;
			consGridBagConstraints5.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
			consGridBagConstraints5.weightx = 1.0D;
			consGridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints9.weighty = 1.0;
			consGridBagConstraints9.weightx = 1.0;
			consGridBagConstraints9.gridx = 0;
			consGridBagConstraints9.gridy = 1;
			consGridBagConstraints9.gridwidth = 2;
			consGridBagConstraints9.insets = new java.awt.Insets(0,25,0,25);
			consGridBagConstraints6.insets = new java.awt.Insets(6,0,6,30);
			consGridBagConstraints6.gridy = 3;
			consGridBagConstraints6.gridx = 1;
			consGridBagConstraints6.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints8.weighty = 0.0D;
			consGridBagConstraints8.weightx = 1.0;
			consGridBagConstraints8.gridx = 0;
			consGridBagConstraints8.gridy = 2;
			consGridBagConstraints8.gridwidth = 2;
			consGridBagConstraints8.insets = new java.awt.Insets(5,25,10,25);
			bottomPanel.setLayout(new java.awt.GridBagLayout());
			bottomPanel.add(getBackButton(), consGridBagConstraints5);
			bottomPanel.add(getAgreeButton(), consGridBagConstraints6);
			bottomPanel.add(getJLabel3(), consGridBagConstraints7);
			bottomPanel.add(getJTextArea(), consGridBagConstraints8);
			bottomPanel.add(getJScrollPane(), consGridBagConstraints9);
			bottomPanel.setBackground(new java.awt.Color(236,233,216));
			bottomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0,java.awt.Color.white));
		}
		return bottomPanel;
	}
	/**
	 * This method initializes jLabel1
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setText("License Agreement");
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
			jLabel2.setText("Please review the license terms before installing Frost.");
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
					applet.licenseBackButtonPressed();
				}
			});
		}
		return backButton;
	}
	/**
	 * This method initializes agreeButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getAgreeButton() {
		if (agreeButton == null) {
			agreeButton = new javax.swing.JButton();
			agreeButton.setText("I Agree");
			agreeButton.setBackground(new java.awt.Color(243, 243, 239));
			agreeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applet.licenseAgreeButtonPressed();
				}
			});
		}
		return agreeButton;
	}
	/**
	 * This method initializes jLabel3
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel3() {
		if(jLabel3 == null) {
			jLabel3 = new javax.swing.JLabel();
			jLabel3.setText("Press Page Down to see the rest of the agreement.");
			jLabel3.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return jLabel3;
	}
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if(jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setBackground(new java.awt.Color(236,233,216));
			jTextArea.setText("If you accept all the terms of the agreement, chose I Agree to continue. You must accept the agreement to install Frost.");
			jTextArea.setWrapStyleWord(true);
			jTextArea.setLineWrap(true);
		}
		return jTextArea;
	}
	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getLicenseTextArea());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes licenseTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getLicenseTextArea() {
		if (licenseTextArea == null) {
			licenseTextArea = new javax.swing.JTextArea();
			licenseTextArea.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			licenseTextArea.setMargin(new Insets(2,5,2,2));
			licenseTextArea.setEditable(false);
			licenseTextArea.setWrapStyleWord(true);
			licenseTextArea.setLineWrap(true);
			if (licenseURL != null) {
				try {
					FileReader fileReader = new FileReader(licenseURL.getFile());
					BufferedReader licenseReader = new BufferedReader(fileReader);
					String line = licenseReader.readLine();
					while (line != null) {
						licenseTextArea.append(line + "\n");
						line = licenseReader.readLine();
					}
					licenseReader.close();
				} catch (FileNotFoundException exception) {
					licenseTextArea.setText("License missing.");
				} catch (IOException exception) {
					licenseTextArea.setText("Error while reading the license.");
				}
			} else {
				licenseTextArea.setText("License missing.");
			}
		}
		return licenseTextArea;
	}
	/**
	 * @param url
	 */
	void setLicenseURL(URL url) { 
		licenseURL = url;			
	}
}