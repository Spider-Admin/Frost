/*
  FirstStartup.java / Frost
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
package frost;

import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import frost.fcp.*;
import frost.gui.*;
import frost.storage.database.*;
import frost.util.*;

public class FirstStartup {
    
    private static final Logger logger = Logger.getLogger(FirstStartup.class.getName());

    private SettingsClass oldSettings = null;
    private File importBaseDir = null;

    public boolean run(Splashscreen splashscreen, SettingsClass frostSettings) {
        // first ask user if to start with a clean version or if an update is wanted
        String title = "Frost first startup";
        String text = "<html>Choose <i>'Clean startup'</i> to start without to import old data.<br>" +
        "&nbsp;&nbsp;(later you can import old identities using the <i>'Manage own identities'</i> dialog<br>" +
        "&nbsp;&nbsp;and the <i>\'Manage identities\'</i> dialog)<br><br>" +
        "Choose <i>'Import from old Frost'</i> to import data from an existing Frost installation.<br>" +
        "&nbsp;&nbsp;(existing Frost should be version 20-Jun-2006)<br>&nbsp;</html>";
        String options[] = {
                "Clean startup",
                "Import from old Frost",
                "Exit"
        };
        int answer = JOptionPane.showOptionDialog(
                splashscreen, 
                text, 
                title,
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        if( answer == JOptionPane.YES_OPTION ) {
            
            // clean startup, ask user which freenet version to use, set correct default availableNodes
            FirstStartupDialog startdlg = new FirstStartupDialog();
            boolean exitChoosed = startdlg.startDialog();
            if( exitChoosed ) {
                System.exit(1);
            }
            // set used version
            frostSettings.setValue(SettingsClass.FREENET_VERSION, startdlg.getFreenetVersion()); // 5 or 7
            // init availableNodes with correct port
            if( startdlg.getOwnHostAndPort() != null ) {
                // user set own host:port
                frostSettings.setValue(SettingsClass.AVAILABLE_NODES, startdlg.getOwnHostAndPort());
            } else if( startdlg.getFreenetVersion() == FcpHandler.FREENET_05 ) {
                frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:8481");
            } else {
                // 0.7
                if( startdlg.isTestnet() == false ) {
                    frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:9481");
                } else {
                    frostSettings.setValue(SettingsClass.AVAILABLE_NODES, "127.0.0.1:9482");
                }
            }
            return false; // don't import
        } else if( answer == JOptionPane.NO_OPTION ) {
            return true; // import
        }  else {
            // user choosed exit
            System.exit(1);
            return false; // for the compiler
        }
    }
    
    public void startImport(Splashscreen splashscreen, SettingsClass frostSettings) {
        // import existing: frost.ini, identities, messages, archive and knownboards
        // - get dir of old frost from user
        // - read frost.ini and take over most settings
        // - get path of keypool, archive from frost.ini
        // - build path of knownboards
        // - show dialog asking what to import and from where
        // - import choosed data
        
        String choosertitle = "Choose Frost directory to import from";
        JFileChooser chooser = new JFileChooser(); 
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle(choosertitle);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        while(true) {
            if( chooser.showOpenDialog(splashscreen) == JFileChooser.APPROVE_OPTION ) {
                String text2 = "<html>You choosed the directory<br><br>&nbsp;&nbsp;&nbsp;" + 
                                chooser.getSelectedFile().getPath()+
                                "<br><br>Is this correct?";
                int answer2 = JOptionPane.showConfirmDialog(
                        splashscreen, 
                        text2, 
                        "Confirm old Frost directory", 
                        JOptionPane.YES_NO_OPTION);
                if( answer2 == JOptionPane.YES_OPTION ) {
                    importBaseDir = chooser.getSelectedFile();
                    break;
                }
            } else {
                System.exit(1);
            }
        }
        
        if( importBaseDir == null ) {
            System.exit(1); // paranoia
        }
        
        // base directory is oldFrostDir
        oldSettings = new SettingsClass(importBaseDir);
        if( !oldSettings.readSettingsFile() ) {
            JOptionPane.showMessageDialog(
                    splashscreen, 
                    "<html>Error: Could not import the frost.ini file:<br><br>&nbsp;&nbsp;&nbsp;"+
                    importBaseDir.getPath() + File.separatorChar + "config" + File.separatorChar + "frost.ini"+
                    "</html>", 
                    "Missing frost.ini", 
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        logger.severe("INFO: imported frost.ini");

        takeoverOldSettings(oldSettings, frostSettings);

        // copy boards.xml to our current config dir
        File oldBoardsXml = 
            new File(importBaseDir.getPath() + File.separatorChar + "config" + File.separatorChar + "boards.xml");
        if( !oldBoardsXml.isFile() ) {
            JOptionPane.showMessageDialog(
                    splashscreen, 
                    "<html>Error: Could not find the boards.xml file:<br><br>&nbsp;&nbsp;&nbsp;"+
                    oldBoardsXml.getPath() +
                    "</html>", 
                    "Missing boards.xml", 
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        boolean wasOk = FileAccess.copyFile(oldBoardsXml.getPath(), "config" + File.separatorChar + "boards.xml");
        if( wasOk == false ) {
            JOptionPane.showMessageDialog(
                    splashscreen, 
                    "<html>Error: Could not copy the boards.xml file:<br><br>&nbsp;&nbsp;&nbsp;"+
                    importBaseDir.getPath() + File.separatorChar + "config" + File.separatorChar + "boards.xml"+
                    "</html>", 
                    "Failed to copy boards.xml", 
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        logger.severe("INFO: copied boards.xml");

        // identities
        File oldIdentitiesXml = 
            new File(importBaseDir.getPath() + File.separatorChar + "identities.xml");
        if( !oldIdentitiesXml.isFile() ) {
            JOptionPane.showMessageDialog(
                    splashscreen, 
                    "<html>Error: Could not find the identities.xml file:<br><br>&nbsp;&nbsp;&nbsp;"+
                    oldIdentitiesXml.getPath() +
                    "</html>", 
                    "Missing identities.xml", 
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        splashscreen.setText("Importing identities");
        new ImportIdentities().importIdentities(oldIdentitiesXml);
    }
    
    private void takeoverOldSettings(SettingsClass importedSettings, SettingsClass newSettings) {

        newSettings.setValue(SettingsClass.MESSAGE_EXPIRE_DAYS, 
                importedSettings.getIntValue(SettingsClass.MESSAGE_EXPIRE_DAYS));
        newSettings.setValue(SettingsClass.MESSAGE_EXPIRATION_MODE, 
                importedSettings.getValue(SettingsClass.MESSAGE_EXPIRATION_MODE));
        
        newSettings.setValue(SettingsClass.MAX_MESSAGE_DISPLAY, 
                importedSettings.getIntValue(SettingsClass.MAX_MESSAGE_DISPLAY));
        newSettings.setValue(SettingsClass.MAX_MESSAGE_DOWNLOAD, 
                importedSettings.getIntValue(SettingsClass.MAX_MESSAGE_DOWNLOAD));
        
        newSettings.setValue(SettingsClass.FREENET_VERSION, 
                importedSettings.getIntValue(SettingsClass.FREENET_VERSION));
        newSettings.setValue(SettingsClass.AVAILABLE_NODES, 
                importedSettings.getValue(SettingsClass.AVAILABLE_NODES));
        
        newSettings.setValue(SettingsClass.LANGUAGE_LOCALE, 
                importedSettings.getValue(SettingsClass.LANGUAGE_LOCALE));
    }

    public File getImportBaseDir() {
        return importBaseDir;
    }

    public SettingsClass getOldSettings() {
        return oldSettings;
    }
}
