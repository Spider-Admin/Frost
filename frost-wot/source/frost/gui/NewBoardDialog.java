/*
 * Created on May 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import frost.util.gui.TextComponentClipboardMenu;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class NewBoardDialog extends JDialog {

	/**
	 * 
	 */
	private class Listener implements DocumentListener, ActionListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		public void changedUpdate(DocumentEvent e) {
			if (e.getDocument() == nameTextField.getDocument()) {
				updateAddButtonState();			
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		public void insertUpdate(DocumentEvent e) {
			if (e.getDocument() == nameTextField.getDocument()) {
				updateAddButtonState();	
			}			
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		public void removeUpdate(DocumentEvent e) {
			if (e.getDocument() == nameTextField.getDocument()) {
				updateAddButtonState();			
			}	
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addButton) {
				addButton_actionPerformed();	
			} else if (e.getSource() == cancelButton) {
				cancelButton_actionPerformed();
			}
			
		}

	}
	
	public static int CHOICE_ADD = 1;
	public static int CHOICE_CANCEL = 2;
	
	private Listener listener = new Listener();
	
	private Language language;

	private JPanel contentPanel = new JPanel();

	private JLabel detailsLabel = new JLabel();
	private JLabel descriptionLabel = new JLabel();
	private JLabel nameLabel = new JLabel();
	private JTextField nameTextField = new JTextField(40);
	private JButton cancelButton = new JButton();
	private JButton addButton = new JButton();
	private JTextArea descriptionTextArea = new JTextArea(3, 40);
	private JScrollPane descriptionScrollPane;
	
	private int choice = CHOICE_CANCEL;
	private String boardName;
	private String boardDescription;

	/**
	 * @param owner
	 * @throws HeadlessException
	 */
	public NewBoardDialog(Frame owner) throws HeadlessException {
		super(owner);
		this.language = Language.getInstance();
		initialize();
		pack();
		setLocationRelativeTo(owner);
		setModal(true);
	}

	/**
	 * 
	 */
	private void initialize() {		
		contentPanel.setBorder(new EmptyBorder(15,15,15,15));
		setContentPane(contentPanel);
		contentPanel.setLayout(new GridBagLayout());
		refreshLanguage();
		
		new TextComponentClipboardMenu(nameTextField, language);
		new TextComponentClipboardMenu(descriptionTextArea, language);
		
		// Adds all of the components			
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		Insets insets5555 = new Insets(5,5,5,5);
		Insets insets10_555 = new Insets(10,5,5,5);
		constraints.insets = insets5555;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridwidth = 3;
		
		contentPanel.add(detailsLabel, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		contentPanel.add(nameLabel, constraints);
		constraints.gridy = 2;
		contentPanel.add(nameTextField, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		contentPanel.add(descriptionLabel, constraints);
		constraints.gridy = 4;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		descriptionScrollPane = new JScrollPane(descriptionTextArea);
		contentPanel.add(descriptionScrollPane, constraints);
		
		constraints.gridwidth = 1;
		constraints.weightx = 2;
		constraints.weighty = 0;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.insets = insets10_555;
		constraints.gridx = 1;
		constraints.gridy = 5;
		contentPanel.add(addButton, constraints);
		constraints.weightx = 0;
		constraints.gridx = 2;
		contentPanel.add(cancelButton, constraints);
		
		addButton.setEnabled(false);
		
		nameTextField.getDocument().addDocumentListener(listener);
		addButton.addActionListener(listener);
		cancelButton.addActionListener(listener);
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		setTitle(" " + language.getString("NewBoardDialog.title"));
		detailsLabel.setText(language.getString("NewBoardDialog.details"));
		nameLabel.setText(language.getString("NewBoardDialog.name"));
		descriptionLabel.setText(language.getString("NewBoardDialog.description"));
		addButton.setText(language.getString("NewBoardDialog.add"));
		cancelButton.setText(language.getString("Cancel"));
	}
	
	/**
	 * 
	 */
	private void updateAddButtonState() {
		if (nameTextField.getText().length() == 0) {
			addButton.setEnabled(false);	
		} else {
			addButton.setEnabled(true);			
		}
	}
	
	/**
	 * 
	 */
	private void addButton_actionPerformed() {
		choice = CHOICE_ADD;
		boardName = nameTextField.getText();
		boardDescription = descriptionTextArea.getText();
		dispose();
	}
	
	/**
	 * 
	 */
	private void cancelButton_actionPerformed() {
		dispose();
	}

	/**
	 * @return
	 */
	public int getChoice() {
		return choice;
	}

	/**
	 * @return
	 */
	public String getBoardDescription() {
		return boardDescription;
	}

	/**
	 * @return
	 */
	public String getBoardName() {
		return boardName;
	}

}
