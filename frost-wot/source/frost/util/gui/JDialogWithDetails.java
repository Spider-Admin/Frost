/*
 * Created on 22-ene-2005
 * 
 */
package frost.util.gui;

import java.awt.*;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;

import frost.util.gui.translation.Language;

/**
 * This class is used to show a dialog with a message. It has a JTextArea that can 
 * be filled with additional details about the main message and that JTextArea can 
 * be shown or hidden just by pressing a button.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class JDialogWithDetails extends JDialog {
	
	/**
	 * 
	 */
	private class Listener extends WindowAdapter implements ActionListener {
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == moreButton) {
				moreButtonPressed();
			}
			if (e.getSource() == okButton) {
				close();
			}
		}
		public void windowClosing(WindowEvent e) {
			close();
			super.windowClosing(e);
		}
	}
	
	protected Language language = null;
	
	private boolean moreExtended = false;
	
	private Listener listener = new Listener();
	
	private JPanel userPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JPanel contentPanel = new JPanel();
	private JPanel morePanel;

	private JButton okButton = new JButton();
	private JButton moreButton = new JButton();
	
	private JTextArea moreTextArea = new JTextArea();
	private JScrollPane moreScrollPane = new JScrollPane(moreTextArea);
	
	/**
	 * This method creates a new instance of JDialogWithDetails
	 * @param parent the parent Frame
	 */
	public JDialogWithDetails(Frame parent) {
		super(parent);
		language = Language.getInstance();
	}
	
	/**
	 * Close the dialog
	 */
	protected void close() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * This method creates the panel that contains the additional details.
	 * @return the panel with the additional details
	 */
	private JPanel getMorePanel() {
		if (morePanel == null) {
			morePanel = new JPanel(new BorderLayout());
			morePanel.setBorder(new EmptyBorder(10,10,10,10));
			
			moreTextArea.setEditable(false);
			moreTextArea.setMargin(new Insets(5,5,5,5));
						
			moreTextArea.setRows(10);
			morePanel.add(moreScrollPane, BorderLayout.CENTER);
		}
		return morePanel;
	}
	
	/**
	 * This method initializes the JDialogWithDetails
	 */
	protected void initialize() {
		moreButton.setText(language.getString("More") + " >>");
		okButton.setText(language.getString("OK"));

		// Putting everything together
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		buttonsPanel.add(moreButton);
		buttonsPanel.add(okButton);
		
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(buttonsPanel, BorderLayout.CENTER);
		contentPanel.add(userPanel, BorderLayout.NORTH);
		getContentPane().add(contentPanel, null);
		
		// Add listeners
		okButton.addActionListener(listener);
		moreButton.addActionListener(listener);
		addWindowListener(listener);
	}
	
	/**
	 * This method is executed when the more/less button is pressed
	 */
	private void moreButtonPressed() {
		if (moreExtended) {
			moreButton.setText(language.getString("More") + " >>");
			contentPanel.remove(getMorePanel());
			pack();
			moreExtended = false;
		} else {	
			contentPanel.add(getMorePanel(), BorderLayout.SOUTH);
			moreButton.setText(language.getString("Less") + " <<");
			pack();
			moreExtended = true;
		}
	}
	
	
	/**
	 * This method is used to fill the details text area.
	 * @param details the message to fill the details text area with.
	 */
	protected void setDetailsText(String details) {
		moreTextArea.setText(details);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				moreScrollPane.getViewport().setViewPosition(new Point(0,0));	
			}
		});
	}
	
	/**
	 * This method returns the panel which contains the buttons.
	 * @return the panel that contains the buttons
	 */
	protected JPanel getButtonsPanel() {
		return buttonsPanel;
	}
	
	/**
	 * This methods returns the panel client classes may fill what 
	 * whatever they want (messages, icons, etc...) 
	 * @return the panel client classes may freely use
	 */
	protected JPanel getUserPanel() {
		return userPanel;
	}
}
