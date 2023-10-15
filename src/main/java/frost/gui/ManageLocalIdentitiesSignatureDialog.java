/*
 ManageLocalIdentitiesSignatureDialog.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import frost.Core;
import frost.SettingsClass;
import frost.util.gui.textpane.AntialiasedTextArea;
import frost.util.gui.translation.Language;

@SuppressWarnings("serial")
public class ManageLocalIdentitiesSignatureDialog extends JDialog {

    private Language language = Language.getInstance();
    
    private JPanel jContentPane = null;
    private JPanel buttonPanel = null;
    private JButton Bok = null;
    private JButton Bcancel = null;

    private AntialiasedTextArea signatureTextArea;
    
    private String returnValue = null;
    
    /**
     * @param owner
     */
    public ManageLocalIdentitiesSignatureDialog(Dialog owner) {
        super(owner);
        initialize();
        setModal(true);
        setLocationRelativeTo(owner);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(400, 200);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            JScrollPane signatureScrollPane = new JScrollPane(getSignatureTextArea());
            jContentPane.add(signatureScrollPane, BorderLayout.CENTER);
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if( buttonPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getBok(), null);
            buttonPanel.add(getBcancel(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes Bok	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBok() {
        if( Bok == null ) {
            Bok = new JButton();
            Bok.setText("Ok");
            Bok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    returnValue = getSignatureTextArea().getText();
                    setVisible(false);
                }
            });
        }
        return Bok;
    }

    /**
     * This method initializes Bcancel	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton();
            Bcancel.setText("Cancel");
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    returnValue = null;
                    setVisible(false);
                }
            });
        }
        return Bcancel;
    }

    private AntialiasedTextArea getSignatureTextArea() {
        if (signatureTextArea == null) {
            signatureTextArea = new AntialiasedTextArea(6, 50);

            String fontName = Core.frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
            int fontStyle = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
            int fontSize = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
            Font tofFont = new Font(fontName, fontStyle, fontSize);
            if (!tofFont.getFamily().equals(fontName)) {
                Core.frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
                tofFont = new Font("Monospaced", fontStyle, fontSize);
            }
            signatureTextArea.setFont(tofFont);
            signatureTextArea.setAntiAliasEnabled(Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BODY_ANTIALIAS));
        }
        return signatureTextArea;
    }
    
    public String startDialog(String idStr, String originalSig) {
        String title = language.formatMessage("ManageLocalIdentitiesSignatureDialog.title", idStr);
        setTitle(title);
        if( originalSig == null ) {
            originalSig = "";
        }
        getSignatureTextArea().setText(originalSig);
        returnValue = null;
        setVisible(true);
        return returnValue; // null means cancelled
    }
}
