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
import java.util.logging.Logger;

import javax.swing.*;

import com.l2fprod.gui.plaf.skin.*;

import frost.ext.JSysTrayIcon;
import frost.gui.Splashscreen;

public class frost {
	
	private static Logger logger = Logger.getLogger(frost.class.getName());
	
	private static String locale = "default";

	public frost() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		//Splashscreen
		Splashscreen splashscreen = new Splashscreen();
		Dimension splashscreenSize = splashscreen.getSize();
		if (splashscreenSize.height > screenSize.height) {
			splashscreenSize.height = screenSize.height;
		}
		if (splashscreenSize.width > screenSize.width) {
			splashscreenSize.width = screenSize.width;
		}
		splashscreen.setLocation(
			(screenSize.width - splashscreenSize.width) / 2,
			(screenSize.height - splashscreenSize.height) / 2);
		
		File splashchk = new File("nosplash.chk");
		if (!splashchk.exists()) {
			splashscreen.show();
		}
				
		SettingsClass frostSettings = new SettingsClass();
		
		// Initializes the logging and skins
		new Logging(frostSettings);
		initializeSkins(frostSettings);

		//Main frame		
		final frame1 frame = new frame1(frostSettings, locale, splashscreen);
		frame.validate();
		frame.show();

		// Display the tray icon
		if (frame1.frostSettings.getBoolValue("showSystrayIcon") == true) {
			if (JSysTrayIcon.createInstance(0, "Frost", "Frost") == false) {
				logger.severe("Could not create systray icon.");
			}
		}

		// this really obscuring stuff is needed to change the divider size
		// after the frame is shown. The goal is to see the blank message view
		// without any attachment table after startup
		frame.resetMessageViewSplitPanes();
		mixed.wait(500);
		frame.resetMessageViewSplitPanes();
	}

	/**Main method*/
	public static void main(String[] args) {
		System.out.println();
		System.out.println("Frost, Copyright (C) 2003 Jan-Thomas Czornack");
		System.out.println("Frost comes with ABSOLUTELY NO WARRANTY");
		System.out.println("This is free software, and you are welcome to");
		System.out.println("redistribute it under the GPL conditions.");
		System.out.println("Frost uses code from apache.org (Apache license),");
		System.out.println(
			"bouncycastle.org (BSD license), Onion Networks (BSD license)");
		System.out.println("and L2FProd.com (Apache license).");
		System.out.println();
		System.out.println();

		// check for needed .jar files by loading a class and catching the error
		try {
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
			// check for skinlfFix.jar
			Class.forName("com.l2fprod.gui.plaf.skin.SkinlfFixMarkerClass");
			//REDFLAG, FIXME: I'm not sure about licensing here.  Theoretically we need to
			//make this optional, but we're linking against so much closed source it probably
			//doesn't matter anymore.
			// check for mailapi.jar
			Class.forName("javax.mail.Address");
			// check for smtp.jar
			Class.forName("com.sun.mail.smtp.SMTPTransport");
		} catch (ClassNotFoundException e1) {
			System.err.println(
				"ERROR: There are missing jar files. Please start Frost using the provided start scripts "
					+ "(frost.bat for win32, frost.sh for unix).\n"+
					"  If Frost was working and you updated just frost.jar, try updating with Frost.zip\n");
			e1.printStackTrace();
			System.exit(3);
		}

		// check for running frost (lock file)
		File runLock = new File(".frost_run_lock");
		boolean fileCreated = false;
		try {
			fileCreated = runLock.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
		}

		if (fileCreated == false) {
			System.out.println(
				"ERROR: Found frost lock file '.frost_run_lock'.\n"
					+ "This indicates that another frost instance is already running in "
					+ "this directory. Running frost concurrently will cause data "
					+ "loss.\nIf you are REALLY SURE that frost is not already running, "
					+ "delete the lockfile '"
					+ runLock.getPath()
					+ "'.");
			System.out.println("\nTERMINATING...\n");
			System.exit(1);
		}
		runLock.deleteOnExit();

		// set l&f
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

		if (args.length == 1) {
			if (args[0].equals("-?")
				|| args[0].equals("-help")
				|| args[0].equals("--help")
				|| args[0].equals("/?")
				|| args[0].equals("/help")) {
				System.out.println("frost [-lf]");
				System.out.println();
				System.out.println(
					"-lf     Allows to set the used 'Look and Feel'.");
				/*System.out.println("        javax.swing.plaf.metal.MetalLookAndFeel");
				  System.out.println("        com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				  System.out.println("        com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				  System.out.println("        javax.swing.plaf.mac.MacLookAndFeel");*/
				UIManager.LookAndFeelInfo[] feels =
					UIManager.getInstalledLookAndFeels();
				for (int i = 0; i < feels.length; i++) {
					System.out.println("           " + feels[i].getClassName());
				}
				System.out.println("\n default is " + lookAndFeel);
				System.exit(0);
			}
		}

		if (args.length == 2) {
			if (args[0].equals("-lf")) {
				lookAndFeel = args[1];
			}
			if (args[0].equals("-locale")) {
				locale = args[1];	//This settings overrides the one in the ini file
			}
		}

		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("using the default");
		}
		new frost();
	}
	
	/**
	 * Initializes the skins system
	 * @param frostSettings the SettingsClass that has the preferences to initialize the skins
	 */
	private void initializeSkins(SettingsClass frostSettings) {
		String skinsEnabled = frostSettings.getValue("skinsEnabled");
		if ((skinsEnabled != null) && (skinsEnabled.equals("true"))) {
			String selectedSkinPath = frostSettings.getValue("selectedSkin");
			if ((selectedSkinPath != null) && (!selectedSkinPath.equals("none"))) {
				try {
					Skin selectedSkin = SkinLookAndFeel.loadThemePack(selectedSkinPath);
					SkinLookAndFeel.setSkin(selectedSkin);
					SkinLookAndFeel.enable();
				} catch (UnsupportedLookAndFeelException exception) {
					logger.severe("The selected skin is not supported by your system\n" +
								  "Skins will be disabled until you choose another one");
					frostSettings.setValue("skinsEnabled", false);
				} catch (Exception exception) {
					logger.severe("There was an error while loading the selected skin\n" +
								  "Skins will be disabled until you choose another one");
					frostSettings.setValue("skinsEnabled", false);
				}
			}
		}
	}
}
