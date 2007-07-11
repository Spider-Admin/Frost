/*
 FontChooser.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class FontChooser extends JDialog {
	private class Listener implements ActionListener, ListSelectionListener, LanguageListener {

		public Listener() {
			super();
		}

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

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) { // We ignore adjusting events
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

		public void languageChanged(LanguageEvent event) {
			refreshLanguage();			
		}
	}
	
	private Listener listener = new Listener();
	
	private Language language = null;
	
	private DefaultListModel fontNamesModel = new DefaultListModel();
	private JList fontNamesList = new JList(fontNamesModel);
	private JTextField selectedNameTextField = new JTextField();
	
	private TranslatableListModel fontStylesModel = null;
	private JList fontStylesList = new JList();
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
	
	public FontChooser(Frame owner, Language language) {
		super(owner);
		this.language = language;
		language.addLanguageListener(listener);
		initialize();
	}
	
	public FontChooser(Dialog owner, Language language) {
		super(owner);
		this.language = language;
		language.addLanguageListener(listener);
		initialize();
	}

	private void initialize() {
		refreshLanguage();
		setSize(400, 350);
		setLocationRelativeTo(getOwner());
		
		new TextComponentClipboardMenu(selectedNameTextField, language);
		new TextComponentClipboardMenu(selectedStyleTextField, language);
		new TextComponentClipboardMenu(selectedSizeTextField, language);
		new TextComponentClipboardMenu(sampleTextField, language);

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
		buttonsPanel.add(okButton, panelConstraints);

		panelConstraints.gridx = 1;
		panelConstraints.gridy = 0;
		panelConstraints.weightx = 0;
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
		String[] sizes = { "8", "9", "10", "11", "12", "14", "16", "18", "22", "26", "30", "36", "48", "64" };
		styles.put(language.getString("Options.display.fontChooser.plain"), new Integer(Font.PLAIN));
		styles.put(language.getString("Options.display.fontChooser.italic"), new Integer(Font.ITALIC));
		styles.put(language.getString("Options.display.fontChooser.bold"), new Integer(Font.BOLD));
		styles.put(language.getString("Options.display.fontChooser.boldItalic"), new Integer(Font.ITALIC | Font.BOLD));

		for (int i = 0; i < names.length; i++) {
			fontNamesModel.addElement(names[i]);
		}

		for (int i = 0; i < sizes.length; i++) {
			fontSizesModel.addElement(sizes[i]);
		}

		fontStylesModel = new TranslatableListModel(language);
		fontStylesList.setModel(fontStylesModel);
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
	
	private void cancelButtonPressed() {
		selectedFont = null;
		dispose();
	}
	private void okButtonPressed() {
		dispose();
	}
	
	private void fontNameValueChanged() {
		if (fontNamesList.getSelectedIndex() != -1) {
			selectedName = fontNamesList.getSelectedValue().toString();
			selectedNameTextField.setText(selectedName);
			refreshFont();
		}
	}
	
	private void refreshFont() {
		if ((selectedName != null) && (selectedSize != null) && (selectedStyle != null)) {
			selectedFont =
				new Font(selectedName, selectedStyle.intValue(), selectedSize.intValue());
			sampleTextField.setFont(selectedFont);
		}
	}

	private void fontStyleValueChanged() {
		int selectedIndex = fontStylesList.getSelectedIndex();
		if (selectedIndex != -1) {
			String styleString = fontStylesModel.getElementAt(selectedIndex).toString();
			String styleKey = fontStylesModel.getKeyAt(selectedIndex);
			
			selectedStyleTextField.setText(styleString);
			selectedStyle = (Integer) styles.get(styleKey);
			refreshFont();
		}
	}
	
	private void fontSizeValueChanged() {
		if (fontSizesList.getSelectedIndex() != -1) {
			selectedSize = new Integer(fontSizesList.getSelectedValue().toString());
			selectedSizeTextField.setText(selectedSize.toString());
			refreshFont();
		}
	}

    public Font getSelectedFont() {
		return selectedFont;
	}

	public void setSelectedFont(Font font) {
		selectedFont = font;
		//Name
		String familyName = font.getFamily();
		if (fontNamesModel.contains(familyName)) {
			fontNamesList.setSelectedValue(familyName, true);
		}
		//Size
		selectedSize = new Integer(font.getSize());
		if (fontSizesModel.contains(selectedSize)) {
			fontSizesList.setSelectedValue(selectedSize, true);
		} else {
			selectedSizeTextField.setText(selectedSize.toString());
			//SetText doesn't launch an event, so we simulate it from here:
			sizeTyped();
		}
		//Style
		int stylePos = -1;
		Integer style = new Integer(font.getStyle());
		Iterator styleEntries = styles.entrySet().iterator();
		while (styleEntries.hasNext() && stylePos == -1) {
			Map.Entry entry = (Map.Entry) styleEntries.next();	
			if (entry.getValue().equals(style)) {
				stylePos = fontStylesModel.indexOfKey(entry.getKey());	
			}
		}
		fontStylesList.setSelectedIndex(stylePos);
	}
	
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
		
	private void refreshLanguage() {
		setTitle(language.getString("Options.display.fontChooser.title"));
		sampleLabel.setText(language.getString("Options.display.fontChooser.sample"));
		okButton.setText(language.getString("Common.ok"));
		cancelButton.setText(language.getString("Common.cancel"));
	}
}
