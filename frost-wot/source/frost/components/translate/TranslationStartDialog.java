/*
  TranslationStartDialog.java / Frost
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
package frost.components.translate;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import frost.util.gui.translation.*;

public class TranslationStartDialog extends JFrame {
    
    Language language = Language.getInstance();

    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JComboBox CBoxTargetLanguage = null;
    private JLabel jLabel1 = null;
    private JComboBox CBoxSourceLanguage = null;
    private JPanel jPanel = null;
    private JButton Bok = null;
    private JButton Bcancel = null;
    private JLabel jLabel2 = null;

    /**
     * This is the default constructor
     */
    public TranslationStartDialog(JFrame parent) {
        super();
        initialize();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(495, 276);
        this.setTitle("Translate Frost");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints11.insets = new java.awt.Insets(3,5,0,5);
            gridBagConstraints11.gridy = 3;
            jLabel2 = new JLabel();
            jLabel2.setText("(The fallback language for all missing keys is english.)");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.gridy = 6;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints3.gridy = 4;
            gridBagConstraints3.weightx = 0.0;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.insets = new java.awt.Insets(5,25,10,5);
            gridBagConstraints3.gridx = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.insets = new java.awt.Insets(15,5,0,5);
            gridBagConstraints2.gridy = 2;
            jLabel1 = new JLabel();
            jLabel1.setText("Choose the language used as source for the translation:");
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.weightx = 0.0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.insets = new java.awt.Insets(5,25,0,5);
            gridBagConstraints1.gridx = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("Choose the target language to translate into:");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabel, gridBagConstraints);
            jContentPane.add(getCBoxTargetLanguage(), gridBagConstraints1);
            jContentPane.add(jLabel1, gridBagConstraints2);
            jContentPane.add(getCBoxSourceLanguage(), gridBagConstraints3);
            jContentPane.add(getJPanel(), gridBagConstraints4);
            jContentPane.add(jLabel2, gridBagConstraints11);
        }
        return jContentPane;
    }

    /**
     * This method initializes CBoxTargetLanguage	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getCBoxTargetLanguage() {
        if( CBoxTargetLanguage == null ) {
            CBoxTargetLanguage = new JComboBox();
            Locale availableLocales[] = Locale.getAvailableLocales();
            List buildIns = LanguageGuiSupport.getBuildInLocales();
            List externals = Language.getExternalLocales();
            TreeMap<ComboBoxEntry,ComboBoxEntry> tm = new TreeMap<ComboBoxEntry,ComboBoxEntry>();
            for (int i = 0; i < availableLocales.length; i++) {
                if( availableLocales[i].getCountry().length() > 0 ) {
                    // for now we concentrate on the main languages ;)
                    continue;
                }
                String langCode = availableLocales[i].getLanguage();
//                if( availableLocales[i].getCountry().length() > 0 ) {
//                    langCode += "_" + availableLocales[i].getCountry();
//                }
                String newOrChangeStr;
                boolean isExternal = false;
                boolean isNew = false;
                if( externals.contains(availableLocales[i]) ) {
                    newOrChangeStr = "external";
                    isExternal = true;
                } else  if( buildIns.contains(availableLocales[i]) ) {
                    newOrChangeStr = "build-in";
                } else {
                    newOrChangeStr = "create";
                    isNew = true;
                }
                String localeDesc = availableLocales[i].getDisplayName() + "  (" + newOrChangeStr+ ") ("+ langCode + ")";
                ComboBoxEntry cbe = new ComboBoxEntry(availableLocales[i], isExternal, isNew, localeDesc);
                tm.put(cbe, cbe);
            }
            // get sorted
            for( Iterator i=tm.keySet().iterator(); i.hasNext(); ) {
                CBoxTargetLanguage.addItem(i.next());
            }
        }
        return CBoxTargetLanguage;
    }
    
    /**
     * This method initializes CBoxSourceLanguage	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getCBoxSourceLanguage() {
        if( CBoxSourceLanguage == null ) {
            CBoxSourceLanguage = new JComboBox();
            
            ComboBoxEntry defaultEntry = null;
            
            List lst_buildin = LanguageGuiSupport.getBuildInLocales();
            TreeMap<ComboBoxEntry,ComboBoxEntry> tm_buildin = new TreeMap<ComboBoxEntry,ComboBoxEntry>();
            for( Iterator i=lst_buildin.iterator(); i.hasNext(); ) {
                Locale locale = (Locale)i.next();
                String localeDesc = locale.getDisplayName() + "  (" + locale.getLanguage() + ")";
                ComboBoxEntry cbe = new ComboBoxEntry(locale, false, false, localeDesc);
                tm_buildin.put(cbe, cbe);
                
                if( locale.getLanguage().equals("en") ) {
                    defaultEntry = cbe;
                }
            }
            // get sorted
            for( Iterator i=tm_buildin.keySet().iterator(); i.hasNext(); ) {
                CBoxSourceLanguage.addItem(i.next());
            }

            List lst_external = Language.getExternalLocales();
            TreeMap<ComboBoxEntry,ComboBoxEntry> tm_external = new TreeMap<ComboBoxEntry,ComboBoxEntry>();
            for( Iterator i=lst_external.iterator(); i.hasNext(); ) {
                Locale locale = (Locale)i.next();
                String localeDesc = locale.getDisplayName() + "  (external) (" + locale.getLanguage() + ")";
                ComboBoxEntry cbe = new ComboBoxEntry(locale, true, false, localeDesc);
                tm_external.put(cbe, cbe);
            }
            // get sorted
            for( Iterator i=tm_external.keySet().iterator(); i.hasNext(); ) {
                CBoxSourceLanguage.addItem(i.next());
            }
            
            // select english as source by default
            if( defaultEntry != null ) {
                CBoxSourceLanguage.setSelectedItem(defaultEntry);
            }
            
            
        }
        return CBoxSourceLanguage;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if( jPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout);
            jPanel.add(getBok(), null);
            jPanel.add(getBcancel(), null);
        }
        return jPanel;
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
                    ComboBoxEntry cbe;
                    cbe = (ComboBoxEntry) getCBoxSourceLanguage().getSelectedItem();
                    String sourceLanguageName = cbe.getLocale().getLanguage();
                    boolean isSourceExternal = cbe.isExternal();
                    
                    cbe = (ComboBoxEntry) getCBoxTargetLanguage().getSelectedItem();
                    String targetLanguageName = cbe.getLocale().getLanguage();
                    boolean isTargetExternal = cbe.isExternal();
                    boolean isTargetNew = cbe.isNew();
                    
                    startTranslation(
                            sourceLanguageName, 
                            isSourceExternal, 
                            targetLanguageName,
                            isTargetExternal,
                            isTargetNew);
                }
            });
        }
        return Bok;
    }

    /**
     * Start the translation dialog.
     */
    private void startTranslation(
            String sourceLanguageName, 
            boolean isSourceExternal, 
            String targetLanguageName,
            boolean isTargetExternal,
            boolean isTargetNew) 
    {
        setVisible(false);

        FrostResourceBundle sourceBundle;
        TranslateableFrostResourceBundle targetBundle;
        
        FrostResourceBundle rootBundle = new FrostResourceBundle(); // english fallback for source language
        
        if( isSourceExternal ) {
            // load external properties file
            sourceBundle = new FrostResourceBundle(sourceLanguageName, rootBundle, true);
        } else {
            // build in source
            sourceBundle = new FrostResourceBundle(sourceLanguageName, rootBundle, false);
        }
        
        if( isTargetExternal ) {
            // load external properties file
            targetBundle = new TranslateableFrostResourceBundle(targetLanguageName, null, true);
        } else if( isTargetNew ) {
            // start a new translation, nothing to load
            targetBundle = new TranslateableFrostResourceBundle();
        } else {
            // target is build-in, enhance existing translation
            targetBundle = new TranslateableFrostResourceBundle(targetLanguageName, null, false);
        }
        
        TranslationDialog td = new TranslationDialog();
        td.startDialog(rootBundle, sourceBundle, sourceLanguageName, targetBundle, targetLanguageName);
        
        dispose();
    }

    /**
     * This method initializes Bcancel	
     */
    private JButton getBcancel() {
        if( Bcancel == null ) {
            Bcancel = new JButton();
            Bcancel.setText("Cancel");
            Bcancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            });
        }
        return Bcancel;
    }
    
    private class ComboBoxEntry implements Comparable {
        boolean isExternal;
        boolean isNew;
        Locale locale;
        String displayString;
        public ComboBoxEntry(Locale locale, boolean isExternal, boolean isNew, String displayString) {
            this.locale = locale;
            this.isExternal = isExternal;
            this.displayString = displayString;
        }
        public Locale getLocale() {
            return locale;
        }
        public boolean isExternal() {
            return isExternal;
        }
        public boolean isNew() {
            return isNew;
        }
        public String toString() {
            return displayString;
        }
        public int compareTo(Object other) {
            return toString().compareTo(other.toString());
        }
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
