/*
 * Created on Jan 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.installer;

import javax.swing.JPanel;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ErrorPanel extends JPanel {
	
	private InstallerApplet applet;
	private javax.swing.JPanel topPanel = null;
	private javax.swing.JPanel botomPanel = null;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JLabel jLabel1 = null;
	private javax.swing.JLabel jLabel2 = null;
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JTextArea jTextArea = null;
	/**
	 * This method initializes 
	 * 
	 */
	protected ErrorPanel(InstallerApplet applet) {
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
        consGridBagConstraints2.weightx = 1.0D;
        consGridBagConstraints2.weighty = 1.0D;
        this.setLayout(new java.awt.GridBagLayout());
        this.add(getTopPanel(), consGridBagConstraints1);
        this.add(getBotomPanel(), consGridBagConstraints2);
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
			java.awt.GridBagConstraints consGridBagConstraints3 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints4 = new java.awt.GridBagConstraints();
			consGridBagConstraints3.insets = new java.awt.Insets(5,10,5,5);
			consGridBagConstraints3.gridheight = 1;
			consGridBagConstraints3.gridwidth = 10;
			consGridBagConstraints3.gridx = 0;
			consGridBagConstraints3.gridy = 0;
			consGridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints4.insets = new java.awt.Insets(0,25,10,0);
			consGridBagConstraints4.gridx = 0;
			consGridBagConstraints4.gridy = 1;
			consGridBagConstraints4.weightx = 1.0D;
			consGridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			topPanel.setLayout(new java.awt.GridBagLayout());
			topPanel.add(getJLabel(), consGridBagConstraints3);
			topPanel.add(getJLabel1(), consGridBagConstraints4);
			topPanel.setBackground(java.awt.Color.white);
			topPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0,0,1,0,java.awt.Color.gray));
		}
		return topPanel;
	}
	/**
	 * This method initializes botomPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getBotomPanel() {
		if(botomPanel == null) {
			botomPanel = new javax.swing.JPanel();
			java.awt.GridBagConstraints consGridBagConstraints6 = new java.awt.GridBagConstraints();
			java.awt.GridBagConstraints consGridBagConstraints5 = new java.awt.GridBagConstraints();
			consGridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			consGridBagConstraints6.weighty = 1.0;
			consGridBagConstraints6.weightx = 1.0;
			consGridBagConstraints6.insets = new java.awt.Insets(10,25,40,25);
			consGridBagConstraints6.gridx = 0;
			consGridBagConstraints6.gridy = 1;
			consGridBagConstraints6.gridwidth = 2;
			consGridBagConstraints5.insets = new java.awt.Insets(10,25,5,20);
			consGridBagConstraints5.gridheight = 1;
			consGridBagConstraints5.gridwidth = 2;
			consGridBagConstraints5.gridy = 0;
			consGridBagConstraints5.gridx = 0;
			consGridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			botomPanel.setLayout(new java.awt.GridBagLayout());
			botomPanel.add(getJLabel2(), consGridBagConstraints5);
			botomPanel.add(getJScrollPane(), consGridBagConstraints6);
			botomPanel.setBackground(new java.awt.Color(236,233,216));
			botomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0,java.awt.Color.white));
		}
		return botomPanel;
	}
	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if(jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText("Installation problem");
		}
		return jLabel;
	}
	
	/**
	 * @param exception
	 */
	void setException(Exception exception) {
		getJTextArea().setText(exception.getMessage());
		getJTextArea().append("\nStack trace:\n");
		StackTraceElement[] stackTrace = exception.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			getJTextArea().append(stackTrace[i].toString() + "\n");	
		}
	}
	/**
	 * This method initializes jLabel1
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setText("There has been a problem while installing.");
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
			jLabel2.setText("These are the details of the exception:");
			jLabel2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return jLabel2;
	}
	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if(jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setWrapStyleWord(true);
			jTextArea.setLineWrap(true);
		}
		return jTextArea;
	}
}
