/*
 * Created on Jan 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.installer;

import java.awt.*; 
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;

/**
 * 
 */
public class GreetingPanel extends JPanel {
	
	private InstallerApplet applet;
	private URL logoURL = null;
	private javax.swing.JPanel topPanel = null;
	private javax.swing.JPanel bottomPanel = null;
	private javax.swing.JButton nextButton = null;
	private javax.swing.JLabel logoLabel = null;
	private javax.swing.JTextArea jTextArea = null;
	private javax.swing.JTextArea jTextArea1 = null;
	private javax.swing.JPanel textPanel = null;
	
	/**
	 * 
	 */
	protected GreetingPanel(InstallerApplet applet) {
		super();
		this.applet = applet;
	}	
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	protected void initialize() {
        java.awt.GridBagConstraints consGridBagConstraints10 = new java.awt.GridBagConstraints();
        java.awt.GridBagConstraints consGridBagConstraints11 = new java.awt.GridBagConstraints();
        consGridBagConstraints10.ipady = 0;
        consGridBagConstraints10.ipadx = 0;
        consGridBagConstraints10.fill = java.awt.GridBagConstraints.BOTH;
        consGridBagConstraints10.weighty = 1.0D;
        consGridBagConstraints10.weightx = 1.0D;
        consGridBagConstraints10.gridy = 0;
        consGridBagConstraints10.gridx = 0;
        consGridBagConstraints11.ipady = 0;
        consGridBagConstraints11.ipadx = 0;
        consGridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
        consGridBagConstraints11.weighty = 0.0D;
        consGridBagConstraints11.weightx = 1.0D;
        consGridBagConstraints11.gridy = 1;
        consGridBagConstraints11.gridx = 0;
        consGridBagConstraints11.gridheight = 1;
        consGridBagConstraints11.gridwidth = 0;
        consGridBagConstraints10.gridheight = 1;
        this.setLayout(new java.awt.GridBagLayout());
        this.add(getTopPanel(), consGridBagConstraints10);
        this.add(getBottomPanel(), consGridBagConstraints11);
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
			java.awt.GridBagConstraints consGridBagConstraints17 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints14 = new java.awt.GridBagConstraints();
			consGridBagConstraints17.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints17.weighty = 1.0;
			consGridBagConstraints17.weightx = 1.0;
			consGridBagConstraints14.gridheight = 2;
			consGridBagConstraints14.gridwidth = 1;
			consGridBagConstraints14.gridy = 0;
			consGridBagConstraints14.gridx = 0;
			consGridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
			topPanel.setLayout(new java.awt.GridBagLayout());
			topPanel.add(getLogoLabel(), consGridBagConstraints14);
			topPanel.add(getTextPanel(), consGridBagConstraints17);
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
			java.awt.FlowLayout layFlowLayout13 = new java.awt.FlowLayout();
			layFlowLayout13.setAlignment(java.awt.FlowLayout.RIGHT);
			layFlowLayout13.setHgap(30);
			layFlowLayout13.setVgap(6);
			bottomPanel.setLayout(layFlowLayout13);
			bottomPanel.add(getNextButton(), null);
			bottomPanel.setBackground(new java.awt.Color(236,233,216));
			bottomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0,java.awt.Color.white));
		}
		return bottomPanel;
	}
	
	/**
	 * This method initializes nextButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getNextButton() {
		if(nextButton == null) {
			nextButton = new javax.swing.JButton();
			nextButton.setText("Next >");
			nextButton.setBackground(new java.awt.Color(243,243,239));
			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					applet.greetingNextButtonPressed();
				}	
			});
		}
		return nextButton;
	}
	
	/**
	 * This method initializes logoLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getLogoLabel() {
		if(logoLabel == null) {
			logoLabel = new javax.swing.JLabel();
			if (logoURL != null) {
				logoLabel.setIcon(new ImageIcon(logoURL));
			} else {
				logoLabel.setText("Logo missing.");
			}
		}
		return logoLabel;
	}
	
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if(jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setEnabled(true);
			jTextArea.setText("Welcome to the Frost Setup Wizard");
			jTextArea.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 22));
			jTextArea.setLineWrap(true);
			jTextArea.setEditable(false);
			jTextArea.setWrapStyleWord(true);
		}
		return jTextArea;
	}
	
	/**
	 * This method initializes jTextArea1
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea1() {
		if(jTextArea1 == null) {
			jTextArea1 = new javax.swing.JTextArea();
			jTextArea1.setEnabled(true);
			jTextArea1.setText("\nThis wizard will guide you through the installation of Frost.\n\n" +
				"Click Next to Continue.");
			jTextArea1.setLineWrap(true);
			jTextArea1.setEditable(false);
			jTextArea1.setWrapStyleWord(true);
		}
		return jTextArea1;
	}
	
	/**
	 * This method initializes textPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getTextPanel() {
		if(textPanel == null) {
			textPanel = new javax.swing.JPanel();
			java.awt.GridBagConstraints consGridBagConstraints19 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints20 = new java.awt.GridBagConstraints();
			consGridBagConstraints19.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints19.weighty = 0.0D;
			consGridBagConstraints19.weightx = 1.0;
			consGridBagConstraints19.gridy = 0;
			consGridBagConstraints19.gridx = 0;
			consGridBagConstraints19.ipadx = 0;
			consGridBagConstraints19.insets = new java.awt.Insets(5,5,5,5);
			consGridBagConstraints20.insets = new java.awt.Insets(5,5,5,5);
			consGridBagConstraints20.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints20.weighty = 1.0;
			consGridBagConstraints20.weightx = 1.0;
			consGridBagConstraints20.gridy = 1;
			consGridBagConstraints20.gridx = 0;
			textPanel.setLayout(new java.awt.GridBagLayout());
			textPanel.add(getJTextArea(), consGridBagConstraints19);
			textPanel.add(getJTextArea1(), consGridBagConstraints20);
			textPanel.setBackground(java.awt.Color.white);
		}
		return textPanel;
	}
	
	/**
	 * @param url
	 */
	void setLogoURL(URL url) {
		logoURL = url;
	}

}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"