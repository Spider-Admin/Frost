/*
  frost.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.awt.*;
import java.io.*;

import javax.swing.*;

import com.l2fprod.gui.plaf.skin.*;

import frost.ext.JSysTrayIcon;

public class frost
{
    public frost()
    {
        final frame1 frame = new frame1();
        frame.validate();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if( frameSize.height > screenSize.height )
        {
            frameSize.height = screenSize.height;
        }
        if( frameSize.width > screenSize.width )
        {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        
        //Initializes the skins *before* the frame is shown
		initializeSkins(frame1.frostSettings, frame);
        
        frame.show();
        
        // Display the tray icon
        if( frame1.frostSettings.getBoolValue("showSystrayIcon") == true )
        {
            if( JSysTrayIcon.createInstance(0, "Frost", "Frost") == false )
            {
                System.out.println("Could not create systray icon.");
            }
        }

        // this really obscuring stuff is needed to change the divider size
        // after the frame is shown. The goal is to see the blank message view
        // without any attachment table after startup
        frame.resetMessageViewSplitPanes();
        mixed.wait(500);
        frame.resetMessageViewSplitPanes();
    }
    
	/**
	 * Initializes the skins system
	 * @param frostSettings the SettingsClass that has the preferences to initialize the skins
	 * @param frame the root JFrame to update its UI when skins are activated
	 */
	private void initializeSkins(SettingsClass frostSettings, JFrame frame) {
		String skinsEnabled = frostSettings.getValue("skinsEnabled");
		if ((skinsEnabled != null) && (skinsEnabled.equals("true"))) {
			String selectedSkinPath = frostSettings.getValue("selectedSkin");
			if ((selectedSkinPath != null) && (!selectedSkinPath.equals(""))) {
				try {
					Skin selectedSkin = SkinLookAndFeel.loadThemePack(selectedSkinPath);
					SkinLookAndFeel.setSkin(selectedSkin);
					SkinLookAndFeel.enable();
					SwingUtilities.updateComponentTreeUI(frame);
				} catch (UnsupportedLookAndFeelException exception) {
					System.out.println("The selected skin is not supported by your system");
					System.out.println("Skins will be disabled until you choose another one\n");
					frostSettings.setValue("skinsEnabled", false);
				} catch (Exception exception) {
					System.out.println("There was an error while loading the selected skin");
					System.out.println("Skins will be disabled until you choose another one\n");
					frostSettings.setValue("skinsEnabled", false);
				}
			}
		}
	}
    

    /**Main method*/
    public static void main(String[] args)
    {
        System.out.println();
        System.out.println("Frost, Copyright (C) 2003 Jan-Thomas Czornack");
        System.out.println("Frost comes with ABSOLUTELY NO WARRANTY");
        System.out.println("This is free software, and you are welcome to");
        System.out.println("redistribute it under the GPL conditions.");
	    System.out.println("Frost uses code from apache.org (Apache license),");
	    System.out.println("bouncycastle.org (BSD license), Onion Networks (BSD license)");
		System.out.println("and L2FProd.com (Apache license).");
        System.out.println();
        System.out.println();
        
// check for needed .jar files by loading a class and catching the error
        try
        {
            // check for xercesImpl.jar
            Class.forName("org.apache.xerces.dom.DocumentImpl");
            // check for xml-apis.jar
            Class.forName("org.w3c.dom.Document");
            // extra check for OutputFormat
            Class.forName("org.apache.xml.serialize.OutputFormat");
            // check for genChkImpl.jar
            Class.forName("freenet.client.ClientKey");
            // check for fecImpl.jar
            Class.forName("fecimpl.FECUtils");
			// check for skinlf.jar
			Class.forName("com.l2fprod.gui.SkinApplet");
        }
        catch (ClassNotFoundException e1)
        {
            System.out.println("ERROR: There are missing jar files. Please start Frost using the provided start scripts "+
                               "(frost.bat for win32, frost.sh for unix).\n");
            e1.printStackTrace();
            System.exit(3);
        }
        
// check for running frost (lock file)
        File runLock = new File(".frost_run_lock");
        boolean fileCreated = false;
        try {
            fileCreated = runLock.createNewFile();
        } catch(IOException ex) {
            ex.printStackTrace(System.out);
        }

        if( fileCreated == false )
        {
            System.out.println("ERROR: Found frost lock file '.frost_run_lock'.\n" +
                               "This indicates that another frost instance is already running in "+
                               "this directory. Running frost concurrently will cause data "+
                               "loss.\nIf you are REALLY SURE that frost is not already running, "+
                               "delete the lockfile '"+runLock.getPath()+"'.");
            System.out.println("\nTERMINATING...\n");
            System.exit(1);
        }
        runLock.deleteOnExit();
        
// set l&f
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

        if( args.length == 1 )
        {
            if( args[0].equals("-?") ||
                args[0].equals("-help") ||
                args[0].equals("--help") ||
                args[0].equals("/?") ||
                args[0].equals("/help") )
            {
                System.out.println("frost [-lf]");
                System.out.println();
                System.out.println("-lf     Allows to set the used 'Look and Feel'.");
                /*System.out.println("        javax.swing.plaf.metal.MetalLookAndFeel");
                System.out.println("        com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                System.out.println("        com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                System.out.println("        javax.swing.plaf.mac.MacLookAndFeel");*/
        		UIManager.LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
        		for (int i =0;i<feels.length;i++)
                {
                    System.out.println("           "+feels[i].getClassName());
                }
        		System.out.println("\n default is " +lookAndFeel);
                System.exit(0);
            }
        }

        if( args.length == 2 )
        {
            if( args[0].equals("-lf") )
            {
                lookAndFeel = args[1];
            }
        }

        try {
            UIManager.setLookAndFeel(lookAndFeel);
        }
        catch( Exception e ) {
	    System.out.println(e.getMessage());
	    System.out.println("using the default");
        }
        new frost();
    }
}
