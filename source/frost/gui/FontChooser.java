/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui;

import java.awt.*;
import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.event.ListSelectionListener;

/**
 * 
 */
public class FontChooser extends JDialog {
	/**
	 * 
	 */
	private class Listener implements ActionListener, ListSelectionListener {

		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				okButtonPressed();
			}
			if (e.getSource() == cancelButton) {
				cancelButtonPressed();
			}
			if (e.getSource() == selectedSizeTextField) {
				sizeTyped();	
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() == fontNamesList) {
				fontNameValueChanged();
			}
			if (e.getSource() == fontStylesList) {
				fontStyleValueChanged();
			}
			if (e.getSource() == fontSizesList) {
				fontSizeValueChanged();
			}
		}

	}
	
	private Listener listener = new Listener();
	
	private ResourceBundle languageBundle = null;
	
	private DefaultListModel fontNamesModel = new DefaultListModel();
	private JList fontNamesList = new JList(fontNamesModel);
	private JTextField selectedNameTextField = new JTextField();
	
	private DefaultListModel fontStylesModel = new DefaultListModel();
	private JList fontStylesList = new JList(fontStylesModel);
	private JTextField selectedStyleTextField = new JTextField();
	
	private DefaultListModel fontSizesModel = new DefaultListModel();
	private JList fontSizesList = new JList(fontSizesModel);
	private JTextField selectedSizeTextField = new JTextField();
	
	private JLabel sampleLabel = new JLabel();
	private JTextField sampleTextField = new JTextField();
	
	private JButton cancelButton = new JButton();
	private JButton okButton = new JButton();
	
	private Font selectedFont = null;
	private String selectedName = null;
	private Integer selectedSize = null;
	private Integer selectedStyle = null; 
	
	private HashMap styles = new HashMap();
	

	/**
	 * 
	 */
	public FontChooser(Frame owner, ResourceBundle bundle) {
		super(owner);
		languageBundle = bundle;
		initialize();
	}
	
	/**
	 * 
	 */
	public FontChooser(Dialog owner, ResourceBundle bundle) {
		super(owner);
		languageBundle = bundle;
		initialize();
	}

	/**
	 * 
	 */
	private void initialize() {
		setTitle("Choose a Font");
		setSize(400, 350);
		setLocationRelativeTo(getOwner());

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);

		GridBagConstraints constraints = new GridBagConstraints();
		Insets insets4444 = new Insets(4, 4, 4, 4);
		constraints.insets = insets4444;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;

		//Adds the JLists
		constraints.gridx = 0;
		constraints.gridy = 0;
		fontNamesList.setBorder(new EmptyBorder(1, 1, 1, 4));
		JScrollPane scrollPane = new JScrollPane(fontNamesList);
		contentPane.add(scrollPane, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		scrollPane = new JScrollPane(fontStylesList);
		fontStylesList.setBorder(new EmptyBorder(1, 1, 1, 4));
		contentPane.add(scrollPane, constraints);
		constraints.gridx = 2;
		constraints.gridy = 0;
		scrollPane = new JScrollPane(fontSizesList);
		fontSizesList.setBorder(new EmptyBorder(1, 1, 1, 4));
		contentPane.add(scrollPane, constraints);

		//Adds the TextFields below
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weighty = 0;
		selectedNameTextField.setEditable(false);
		contentPane.add(selectedNameTextField, constraints);
		constraints.gridx = 1;
		constraints.gridy = 1;
		selectedStyleTextField.setEditable(false);
		contentPane.add(selectedStyleTextField, constraints);
		constraints.gridx = 2;
		constraints.gridy = 1;
		contentPane.add(selectedSizeTextField, constraints);

		//Adds the sample label and textField
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		sampleLabel.setText("Sample");
		contentPane.add(sampleLabel, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		constraints.weighty = 1;
		sampleTextField.setText("aAbByYzZ");
		contentPane.add(sampleTextField, constraints);

		//Buttons panel - BEGIN
		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.insets = insets4444;
		panelConstraints.weighty = 1;
		panelConstraints.anchor = GridBagConstraints.EAST;

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 0;
		panelConstraints.weightx = 1;
		okButton.setText("Ok");
		buttonsPanel.add(okButton, panelConstraints);

		panelConstraints.gridx = 1;
		panelConstraints.gridy = 0;
		panelConstraints.weightx = 0;
		cancelButton.setText("Cancel");
		buttonsPanel.add(cancelButton, panelConstraints);

		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.weighty = 0;
		contentPane.add(buttonsPanel, constraints);
		// Buttons panel - END

		//Adds listeners
		okButton.addActionListener(listener);
		cancelButton.addActionListener(listener);
		fontNamesList.addListSelectionListener(listener);
		fontStylesList.addListSelectionListener(listener);
		fontSizesList.addListSelectionListener(listener);
		selectedSizeTextField.addActionListener(listener);

		//Fills the JLists
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] names = environment.getAvailableFontFamilyNames();
		String[] sizes =
			{ "8", "9", "10", "11", "12", "14", "16", "18", "22", "26", "30", "36", "48", "64" };
		styles.put("Plain", new Integer(Font.PLAIN));
		styles.put("Italic", new Integer(Font.ITALIC));
		styles.put("Bold", new Integer(Font.BOLD));
		styles.put("Bold Italic", new Integer(Font.ITALIC | Font.BOLD));

		for (int i = 0; i < names.length; i++) {
			fontNamesModel.addElement(names[i]);
		}

		for (int i = 0; i < sizes.length; i++) {
			fontSizesModel.addElement(sizes[i]);
		}

		ArrayList styleKeysList = new ArrayList(styles.keySet());
		Collections.reverse(styleKeysList);	//Because I want "Plain" to be first
		Iterator styleKeys = styleKeysList.iterator();
		while (styleKeys.hasNext()) {
			fontStylesModel.addElement(styleKeys.next());	
		}

		fontNamesList.setSelectedIndex(0);
		fontSizesList.setSelectedIndex(0);
		fontStylesList.setSelectedIndex(0);
	}
	
	/**
	 * 
	 */
	private void cancelButtonPressed() {
		selectedFont = null;
		dispose();
	}
	/**
	 * 
	 */
	private void okButtonPressed() {
		dispose();
	}
	
	/**
	 * 
	 */
	private void fontNameValueChanged() {
		if (fontNamesList.getSelectedIndex() != -1) {
			selectedName = fontNamesList.getSelectedValue().toString();
			selectedNameTextField.setText(selectedName);
			refreshFont();
		}
	}
	
	/**
	 * 
	 */
	private void refreshFont() {
		if ((selectedName != null) && (selectedSize != null) && (selectedStyle != null)) {
			selectedFont =
				new Font(selectedName, selectedStyle.intValue(), selectedSize.intValue());
			sampleTextField.setFont(selectedFont);
		}
	}

	/**
	 * 
	 */
	private void fontStyleValueChanged() {
		if (fontStylesList.getSelectedIndex() != -1) {
			String styleString = fontStylesList.getSelectedValue().toString();
			selectedStyleTextField.setText(styleString);
			selectedStyle = (Integer) styles.get(styleString);
			refreshFont();
		}
	}
	
	/**
	 * 
	 */
	private void fontSizeValueChanged() {
		if (fontSizesList.getSelectedIndex() != -1) {
			selectedSize = new Integer(fontSizesList.getSelectedValue().toString());
			selectedSizeTextField.setText(selectedSize.toString());
			refreshFont();
		}
	}
	/**
	 * @return
	 */
	public Font getSelectedFont() {
		return selectedFont;
	}

	/**
	 * @param font
	 */
	public void setSelectedFont(Font font) {
		selectedFont = font;
	}
	
	/**
		 * 
		 */
		private void sizeTyped() {
			String size = selectedSizeTextField.getText();
			try {
				selectedSize = new Integer(size);
				if (fontSizesModel.contains(size)) {
					fontSizesList.setSelectedValue(size, true);
				} else {
					fontSizesList.clearSelection();	
				}
				refreshFont();			
			} catch (NumberFormatException exception) {
				//Nothing, just ignore the typed value
			}
		}

}
