/*
  NewsPanel.java / Frost
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
package frost.gui.preferences;

import java.awt.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.fcp.*;
import frost.util.gui.*;
import frost.util.gui.textpane.*;
import frost.util.gui.translation.*;

class NewsPanel extends JPanel {

    private static Logger logger = Logger.getLogger(NewsPanel.class.getName());

    private SettingsClass settings = null;
    private Language language = null;

    private JLabel uploadHtlLabel = new JLabel();
    private JLabel downloadHtlLabel = new JLabel();
    private JLabel displayDaysLabel = new JLabel();
    private JLabel downloadDaysLabel = new JLabel();
    private JLabel messageBaseLabel = new JLabel();
    private JLabel signatureLabel = new JLabel();

    private JTextField uploadHtlTextField = new JTextField(8);
    private JTextField downloadHtlTextField = new JTextField(8);
    private JTextField displayDaysTextField = new JTextField(8);
    private JTextField downloadDaysTextField = new JTextField(8);
    private JTextField messageBaseTextField = new JTextField(16);

    private AntialiasedTextArea signatureTextArea;

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected NewsPanel(SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
        
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            // disable 0.5-only items
            uploadHtlLabel.setEnabled(false);
            uploadHtlTextField.setEnabled(false);
            downloadHtlLabel.setEnabled(false);
            downloadHtlTextField.setEnabled(false);
        }
    }

    private void initialize() {
        setName("NewsPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(uploadHtlTextField, language);
        new TextComponentClipboardMenu(downloadHtlTextField, language);
        new TextComponentClipboardMenu(displayDaysTextField, language);
        new TextComponentClipboardMenu(downloadDaysTextField, language);
        new TextComponentClipboardMenu(messageBaseTextField, language);

        // Adds all of the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weighty = 0.0;
        constraints.weightx = 0;

        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.weightx = 0.4;
        constraints.gridy = 0;
        
        constraints.gridx = 0;
        add(displayDaysLabel, constraints);
        constraints.gridx = 1;
        add(displayDaysTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(downloadDaysLabel, constraints);
        constraints.gridx = 1;
        add(downloadDaysTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(messageBaseLabel, constraints);
        constraints.gridx = 1;
        add(messageBaseTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(uploadHtlLabel, constraints);
        constraints.weightx = 0.6;
        constraints.gridx = 1;
        add(uploadHtlTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(downloadHtlLabel, constraints);
        constraints.gridx = 1;
        add(downloadHtlTextField, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy++;
        add(signatureLabel, constraints);
        constraints.gridy++;
        JScrollPane signatureScrollPane = new JScrollPane(getSignatureTextArea());
        add(signatureScrollPane, constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);
    }

    /**
     * @return
     */
    private AntialiasedTextArea getSignatureTextArea() {
        if (signatureTextArea == null) {
            signatureTextArea = new AntialiasedTextArea(6, 50);

            String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
            int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
            int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
            Font tofFont = new Font(fontName, fontStyle, fontSize);
            if (!tofFont.getFamily().equals(fontName)) {
                logger.severe("The selected font was not found in your system\n"
                        + "That selection will be changed to \"Monospaced\".");
                settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
                tofFont = new Font("Monospaced", fontStyle, fontSize);
            }
            signatureTextArea.setFont(tofFont);
            signatureTextArea.setAntiAliasEnabled(settings.getBoolValue("messageBodyAA"));
        }
        return signatureTextArea;
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        uploadHtlTextField.setText(settings.getValue("tofUploadHtl"));
        downloadHtlTextField.setText(settings.getValue("tofDownloadHtl"));
        displayDaysTextField.setText(settings.getValue("maxMessageDisplay"));
        downloadDaysTextField.setText(settings.getValue("maxMessageDownload"));
        messageBaseTextField.setText(settings.getValue("messageBase"));

        //Load signature
        File signature = new File("signature.txt");
        if (signature.isFile()) {
            getSignatureTextArea().setText(FileAccess.readFile(signature, "UTF-8"));
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        uploadHtlLabel.setText(language.getString("Options.news.1.messageUploadHtl") + " (21)");
        downloadHtlLabel.setText(language.getString("Options.news.1.messageDownloadHtl") + " (23)");
        displayDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDisplay") + " (15)");
        downloadDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDownloadBackwards") + " (5)");
        messageBaseLabel.setText(language.getString("Options.news.1.messageBase") + " (news)");
        signatureLabel.setText(language.getString("Options.news.1.signature"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue("tofUploadHtl", uploadHtlTextField.getText());
        settings.setValue("tofDownloadHtl", downloadHtlTextField.getText());
        settings.setValue("maxMessageDisplay", displayDaysTextField.getText());
        settings.setValue("maxMessageDownload", downloadDaysTextField.getText());
        settings.setValue("messageBase", messageBaseTextField.getText().trim().toLowerCase());

        //Save signature
        FileAccess.writeFile(getSignatureTextArea().getText(), "signature.txt", "UTF-8");
    }
}
