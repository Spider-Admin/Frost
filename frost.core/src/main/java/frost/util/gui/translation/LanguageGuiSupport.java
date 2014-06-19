/*
  LanguageGuiSupport.java / Frost
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
package frost.util.gui.translation;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;

/**
 * Builds and updates the language menu in MainFrame,
 * updating adds new external language bundles.
 */
public class LanguageGuiSupport {

    private static LanguageGuiSupport instance = null;

    private final Language language = Language.getInstance();

    private static List<Locale> buildInLocales = null;

    private final List<JRadioButtonMenuItem> buildinLanguageMenuItemsList;
    private final HashMap<String,JRadioButtonMenuItem> buildinLanguageMenuItemsMap;
    private final JRadioButtonMenuItem languageDefaultMenuItem;
    private final JRadioButtonMenuItem languageBulgarianMenuItem;
    private final JRadioButtonMenuItem languageDutchMenuItem;
    private final JRadioButtonMenuItem languageDanishMenuItem;
    private final JRadioButtonMenuItem languageEnglishMenuItem;
    private final JRadioButtonMenuItem languageFrenchMenuItem;
    private final JRadioButtonMenuItem languageGermanMenuItem;
    private final JRadioButtonMenuItem languageItalianMenuItem;
    private final JRadioButtonMenuItem languageJapaneseMenuItem;
    private final JRadioButtonMenuItem languageSpanishMenuItem;
    private final JRadioButtonMenuItem languageRussianMenuItem;
    private final JRadioButtonMenuItem languagePolishMenuItem;
    private final JRadioButtonMenuItem languageSwedishMenuItem;
    private final JRadioButtonMenuItem languageEsperantoMenuItem;

    private final ButtonGroup languageMenuButtonGroup;

    private JMenu languageMenu;

    private LanguageGuiSupport() {
        languageDefaultMenuItem = new JRadioButtonMenuItem();
        languageBulgarianMenuItem = new JRadioButtonMenuItem();
        languageDutchMenuItem = new JRadioButtonMenuItem();
        languageDanishMenuItem = new JRadioButtonMenuItem();
        languageEnglishMenuItem = new JRadioButtonMenuItem();
        languageFrenchMenuItem = new JRadioButtonMenuItem();
        languageGermanMenuItem = new JRadioButtonMenuItem();
        languageItalianMenuItem = new JRadioButtonMenuItem();
        languageJapaneseMenuItem = new JRadioButtonMenuItem();
        languageSpanishMenuItem = new JRadioButtonMenuItem();
        languageRussianMenuItem = new JRadioButtonMenuItem();
        languagePolishMenuItem = new JRadioButtonMenuItem();
        languageSwedishMenuItem = new JRadioButtonMenuItem();
        languageEsperantoMenuItem = new JRadioButtonMenuItem();

        languageMenuButtonGroup = new ButtonGroup();

        languageBulgarianMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_bg.png", 16, 16));
        languageGermanMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_de.png", 16, 16));
        languageEnglishMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_en.png", 16, 16));
        languageSpanishMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_es.png", 16, 16));
        languageFrenchMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_fr.png", 16, 16));
        languageItalianMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_it.png", 16, 16));
        languageJapaneseMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_ja.png", 16, 16));
        languageDanishMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_da.png", 16, 16));
        languageDutchMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_nl.png", 16, 16));
        languageRussianMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_ru.png", 16, 16));
        languagePolishMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_pl.png", 16, 16));
        languageSwedishMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_sv.png", 16, 16));
        languageEsperantoMenuItem.setIcon(MiscToolkit.getScaledImage("/data/flag_eo.png", 16, 16));

        // default action listeners
        languageDefaultMenuItem.addActionListener(   new LanguageAction(null, false));
        languageGermanMenuItem.addActionListener(    new LanguageAction("de", false));
        languageDanishMenuItem.addActionListener(    new LanguageAction("da", false));
        languageEnglishMenuItem.addActionListener(   new LanguageAction("en", false));
        languageDutchMenuItem.addActionListener(     new LanguageAction("nl", false));
        languageFrenchMenuItem.addActionListener(    new LanguageAction("fr", false));
        languageJapaneseMenuItem.addActionListener(  new LanguageAction("ja", false));
        languageRussianMenuItem.addActionListener(   new LanguageAction("ru", false));
        languageItalianMenuItem.addActionListener(   new LanguageAction("it", false));
        languageSpanishMenuItem.addActionListener(   new LanguageAction("es", false));
        languageBulgarianMenuItem.addActionListener( new LanguageAction("bg", false));
        languagePolishMenuItem.addActionListener(    new LanguageAction("pl", false));
        languageSwedishMenuItem.addActionListener(   new LanguageAction("sv", false));
        languageEsperantoMenuItem.addActionListener( new LanguageAction("eo", false));

        buildinLanguageMenuItemsList = new ArrayList<JRadioButtonMenuItem>();
        buildinLanguageMenuItemsList.add(languageDefaultMenuItem);
        buildinLanguageMenuItemsList.add(languageBulgarianMenuItem);
        buildinLanguageMenuItemsList.add(languageDanishMenuItem);
        buildinLanguageMenuItemsList.add(languageDutchMenuItem);
        buildinLanguageMenuItemsList.add(languageEnglishMenuItem);
        buildinLanguageMenuItemsList.add(languageFrenchMenuItem);
        buildinLanguageMenuItemsList.add(languageGermanMenuItem);
        buildinLanguageMenuItemsList.add(languageItalianMenuItem);
        buildinLanguageMenuItemsList.add(languageJapaneseMenuItem);
        buildinLanguageMenuItemsList.add(languagePolishMenuItem);
        buildinLanguageMenuItemsList.add(languageRussianMenuItem);
        buildinLanguageMenuItemsList.add(languageSpanishMenuItem);
        buildinLanguageMenuItemsList.add(languageSwedishMenuItem);
        buildinLanguageMenuItemsList.add(languageEsperantoMenuItem);

        buildinLanguageMenuItemsMap = new HashMap<String,JRadioButtonMenuItem>();
        buildinLanguageMenuItemsMap.put("default", languageDefaultMenuItem);
        buildinLanguageMenuItemsMap.put("da", languageDanishMenuItem);
        buildinLanguageMenuItemsMap.put("de", languageGermanMenuItem);
        buildinLanguageMenuItemsMap.put("en", languageEnglishMenuItem);
        buildinLanguageMenuItemsMap.put("nl", languageDutchMenuItem);
        buildinLanguageMenuItemsMap.put("fr", languageFrenchMenuItem);
        buildinLanguageMenuItemsMap.put("ja", languageJapaneseMenuItem);
        buildinLanguageMenuItemsMap.put("it", languageItalianMenuItem);
        buildinLanguageMenuItemsMap.put("es", languageSpanishMenuItem);
        buildinLanguageMenuItemsMap.put("bg", languageBulgarianMenuItem);
        buildinLanguageMenuItemsMap.put("ru", languageRussianMenuItem);
        buildinLanguageMenuItemsMap.put("pl", languagePolishMenuItem);
        buildinLanguageMenuItemsMap.put("sv", languageSwedishMenuItem);
        buildinLanguageMenuItemsMap.put("eo", languageEsperantoMenuItem);
    }

    public static LanguageGuiSupport getInstance() {
        if( instance == null ) {
            instance = new LanguageGuiSupport();
        }
        return instance;
    }

    public void buildInitialLanguageMenu(final JMenu langMenu) {
        this.languageMenu = langMenu;
        buildLanguageMenu();
    }

    /**
     * After the translation dialog runs, the external languages might be changed.
     * Rebuild the menu items for the external bundles.
     */
    public void updateLanguageMenu() {
        // clear all
        languageMenu.removeAll();
        final List<AbstractButton> l = new ArrayList<AbstractButton>();
        for(final Enumeration<AbstractButton> e=languageMenuButtonGroup.getElements();e.hasMoreElements(); ) {
            l.add( e.nextElement() );
        }
        for( final AbstractButton b : l ) {
            languageMenuButtonGroup.remove(b);
        }

        buildLanguageMenu();
    }

    private void buildLanguageMenu() {
        // first add the buildins, then maybe external bundles
        // finally select currently choosed language
        boolean anItemIsSelected = false;

        final String configuredLang = Core.frostSettings.getValue(SettingsClass.LANGUAGE_LOCALE);
        final String langIsExternal = Core.frostSettings.getValue("localeExternal");
        boolean isExternal;
        if( langIsExternal == null || langIsExternal.length() == 0 || !langIsExternal.equals("true")) {
            isExternal = false;
        } else {
            isExternal = true;
        }

        for( final AbstractButton n : buildinLanguageMenuItemsList ) {
            languageMenuButtonGroup.add(n);
        }

        languageMenu.add(languageDefaultMenuItem);
        languageMenu.addSeparator();
        languageMenu.add(languageBulgarianMenuItem);
        languageMenu.add(languageDanishMenuItem);
        languageMenu.add(languageDutchMenuItem);
        languageMenu.add(languageEnglishMenuItem);
        languageMenu.add(languageEsperantoMenuItem);
        languageMenu.add(languageFrenchMenuItem);
        languageMenu.add(languageGermanMenuItem);
        languageMenu.add(languageItalianMenuItem);
        languageMenu.add(languageJapaneseMenuItem);
        languageMenu.add(languagePolishMenuItem);
        languageMenu.add(languageRussianMenuItem);
        languageMenu.add(languageSpanishMenuItem);
        languageMenu.add(languageSwedishMenuItem);

        final List<Locale> externalLocales = Language.getExternalLocales();
        if( externalLocales.size() > 0 ) {
            languageMenu.addSeparator();

            for( final Locale locale : externalLocales ) {
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem();
                final String localeDesc = locale.getDisplayName() + "  (external) (" + locale.getLanguage() + ")";
                item.setText(localeDesc);
                item.addActionListener(new LanguageAction(locale.getLanguage(), true));
                languageMenuButtonGroup.add(item);
                languageMenu.add(item);
                if( isExternal == true && locale.getLanguage().equals(configuredLang) ) {
                    languageMenuButtonGroup.setSelected(item.getModel(), true);
                    anItemIsSelected = true;
                }
            }
        }

        if( anItemIsSelected == false && isExternal == false ) {
            // select buildin item
            final Object languageItem = buildinLanguageMenuItemsMap.get(configuredLang);
            if (languageItem != null) {
                languageMenuButtonGroup.setSelected(((JMenuItem) languageItem).getModel(), true);
                anItemIsSelected = true;
            }
        }

        if( anItemIsSelected == false ) {
            languageMenuButtonGroup.setSelected(languageDefaultMenuItem.getModel(), true);
        }

        translateLanguageMenu();
    }

    /**
     * Setter for the language
     */
    private void setLanguageResource(final String newLocaleName, boolean isExternal) {
        if( newLocaleName == null ) {
            Core.frostSettings.setValue(SettingsClass.LANGUAGE_LOCALE, "default");
            Core.frostSettings.setValue("localeExternal", "false");
            isExternal = false;
        } else {
            Core.frostSettings.setValue(SettingsClass.LANGUAGE_LOCALE, newLocaleName);
            Core.frostSettings.setValue("localeExternal", Boolean.toString(isExternal));
        }
        language.changeLanguage(newLocaleName, isExternal);
    }

    public void translateLanguageMenu() {
        languageDefaultMenuItem.setText(language.getString("MainFrame.menu.language.default"));
        languageDanishMenuItem.setText(language.getString("MainFrame.menu.language.danish"));
        languageDutchMenuItem.setText(language.getString("MainFrame.menu.language.dutch"));
        languageEnglishMenuItem.setText(language.getString("MainFrame.menu.language.english"));
        languageFrenchMenuItem.setText(language.getString("MainFrame.menu.language.french"));
        languageGermanMenuItem.setText(language.getString("MainFrame.menu.language.german"));
        languageItalianMenuItem.setText(language.getString("MainFrame.menu.language.italian"));
        languageJapaneseMenuItem.setText(language.getString("MainFrame.menu.language.japanese"));
        languageSpanishMenuItem.setText(language.getString("MainFrame.menu.language.spanish"));
        languageBulgarianMenuItem.setText(language.getString("MainFrame.menu.language.bulgarian"));
        languageRussianMenuItem.setText(language.getString("MainFrame.menu.language.russian"));
        languagePolishMenuItem.setText(language.getString("MainFrame.menu.language.polish"));
        languageSwedishMenuItem.setText(language.getString("MainFrame.menu.language.swedish"));
        languageEsperantoMenuItem.setText(language.getString("MainFrame.menu.language.esperanto"));
    }

    public static List<Locale> getBuildInLocales() {
        if( buildInLocales == null ) {
            final ArrayList<Locale> lst = new ArrayList<Locale>();
            lst.add(new Locale("bg"));
            lst.add(new Locale("da"));
            lst.add(new Locale("de"));
            lst.add(new Locale("en"));
            lst.add(new Locale("es"));
            lst.add(new Locale("fr"));
            lst.add(new Locale("it"));
            lst.add(new Locale("ja"));
            lst.add(new Locale("nl"));
            lst.add(new Locale("ru"));
            lst.add(new Locale("pl"));
            lst.add(new Locale("sv"));
            lst.add(new Locale("eo"));
            buildInLocales = lst;
        }
        return buildInLocales;
    }

    class LanguageAction implements ActionListener {
        String langCode;
        boolean isExternal;
        public LanguageAction(final String langCode, final boolean isExternal) {
            this.langCode = langCode;
            this.isExternal = isExternal;
        }
        public void actionPerformed(final ActionEvent e) {
            setLanguageResource(langCode, isExternal);
        }
    }
}
