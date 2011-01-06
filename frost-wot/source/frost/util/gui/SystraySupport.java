/*
  SystraySupport.java / Frost
  Copyright (C) 2011  Frost Project <jtcfrost.sourceforge.net>

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

import frost.*;
import frost.util.gui.translation.*;

public class SystraySupport {
    
    final private static Language language = Language.getInstance();

    final private static SystemTray tray = SystemTray.getSystemTray();
    private static TrayIcon trayIcon = null;;
    
    private static Image image_normal = null;
    private static Image image_newMessage = null;

    public static boolean isSupported() {
        return SystemTray.isSupported();
    }
    
    public static boolean initialize(String title) {

        image_normal = MiscToolkit.loadImageIcon("/data/2_frost_logo_f_32x32.png").getImage();
        image_newMessage = MiscToolkit.loadImageIcon("/data/3_frost_mail_32x32.png").getImage();
        
        final ActionListener exitListener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                MainFrame.getInstance().fileExitMenuItem_actionPerformed(null);
            }
        };
        final ActionListener showHideListener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                toggleMinimizeToTray();
            }
        };

        final PopupMenu popup = new PopupMenu();

        final MenuItem showHideItem = new MenuItem(language.getString("SystraySupport.showHideFrost"));
        showHideItem.addActionListener(showHideListener);
        popup.add(showHideItem);

        popup.addSeparator();

        final MenuItem defaultItem = new MenuItem(language.getString("SystraySupport.ExitFrost"));
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);

        trayIcon = new TrayIcon(image_normal, title, popup);

        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(mouseListener);

        try {
            tray.add(trayIcon);
        } catch (final AWTException e) {
            System.err.println("TrayIcon could not be added.");
            return false;
        }
        return true;
    }
    
    public static void setTitle(String title) {
        trayIcon.setToolTip(title);
    }
    
    public static void setIconNormal() {
        trayIcon.setImage(image_normal);
    }

    public static void setIconNewMessage() {
        trayIcon.setImage(image_newMessage);
    }
    
    public static void minimizeToTray() {
        if (MainFrame.getInstance().isVisible()) {
            MainFrame.getInstance().setVisible(false);
        }
    }
    
    public static void toggleMinimizeToTray() {
        if (MainFrame.getInstance().isVisible()) {
            MainFrame.getInstance().setVisible(false);
        } else {
            MainFrame.getInstance().setVisible(true);
        }
    }

    final private static MouseListener mouseListener = new MouseListener() {

        // we need to test on pressed and released event
        boolean isPopupTrigger = false;

        public void mouseClicked(final MouseEvent e) {
        }

        public void mouseEntered(final MouseEvent e) {
        }

        public void mouseExited(final MouseEvent e) {
        }

        public void mousePressed(final MouseEvent e) {
            isPopupTrigger = e.isPopupTrigger();
        }

        public void mouseReleased(final MouseEvent e) {

            // don't show frame when user wanted to show the popup menu
            if (!isPopupTrigger && e.isPopupTrigger()) {
                isPopupTrigger = true;
            }

            if (isPopupTrigger) {
                return;
            }
            
            toggleMinimizeToTray();
        }
    };
}
