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

import java.io.*;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import frost.util.gui.MiscToolkit;

public class Frost {

	private static Logger logger = Logger.getLogger(Frost.class.getName());
	private static String lookAndFeel = null;

	/**
	 * Main method 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		System.out.println();
		System.out.println("Frost, Copyright (C) 2003 Jan-Thomas Czornack");
		System.out.println("Frost comes with ABSOLUTELY NO WARRANTY");
		System.out.println("This is free software, and you are welcome to");
		System.out.println("redistribute it under the GPL conditions.");
		System.out.println("Frost uses code from apache.org (Apache license),");
		System.out.println("bouncycastle.org (BSD license), Onion Networks (BSD license),");
		System.out.println("L2FProd.com (Apache license)");
		System.out.println("and ShiftOne Java Object Cache (LGPL license)");
		System.out.println();

		parseCommandLine(args);
		initializeLookAndFeel();

		new Frost();
	}

	/**
	 * This method sets the look and feel specified in the command line arguments.
	 * If none was specified, the System Look and Feel is set. 
	 */
	private static void initializeLookAndFeel() {
		try {
			if (lookAndFeel == null) {
				String systemLFName = UIManager.getSystemLookAndFeelClassName();
				UIManager.setLookAndFeel(systemLFName);
			} else {
				UIManager.setLookAndFeel(lookAndFeel);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Using the default");
		}
	}

	/**
	 * This method parses the command line arguments
	 * @param args the arguments
	 */
	private static void parseCommandLine(String[] args) {
		int count = 0;
		try {
			while (args.length > count) {
				if (args[count].equals("-?")
					|| args[count].equals("-help")
					|| args[count].equals("--help")
					|| args[count].equals("/?")
					|| args[count].equals("/help")) {
					showHelp();
					count++;
				} else if (args[count].equals("-lf")) {
					lookAndFeel = args[count + 1];
					count = count + 2;
				} else if (args[count].equals("-locale")) {
					setLocale(args[count + 1]); //This settings overrides the one in the ini file
					count = count + 2;
				} else {
					showHelp();
				}
			}
		} catch (ArrayIndexOutOfBoundsException exception) {
			showHelp();
		}
	}

	/**
	 * This method sets a new locale
	 * @param string the name of the new locale
	 */
	private static void setLocale(String newLocale) {
		Locale locale = new Locale(newLocale);
		Locale.setDefault(locale);
		Core.setLocale(locale);
	}

	/**
	 * This method shows a help message on the standard output and exits the program.
	 */
	private static void showHelp() {
		System.out.println("java -jar frost.jar [-lf lookAndFeel] [-locale languageCode]\n");

		System.out.println("-lf     Sets the 'Look and Feel' Frost will use.");
		System.out.println("        (overriden by the skins preferences)\n");
		System.out.println("        These ones are currently available:");
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < feels.length; i++) {
			System.out.println("           " + feels[i].getClassName());
		}
		System.out.println("\n         And this one is used by default:");
		System.out.println("           " + lookAndFeel + "\n");

		System.out.println("-locale  Sets the language Frost will use, if available.");
		System.out.println("         (overrides the setting in the preferences)\n");

		System.out.println("Example:\n");
		System.out.print("java -jar frost.jar ");
		if (feels.length > 0) {
			System.out.print("-lf " + feels[0].getClassName() + " ");
		}
		System.out.println("-locale es\n");
		System.out.println("That command line will instruct Frost to use the");
		if (feels.length > 0) {
			System.out.println(feels[0].getClassName() + " look and feel and the");

		}
		System.out.println("Spanish language.");
		System.exit(0);
	}

	/**
	 * Constructor
	 */
	public Frost() {
		if (!initializeLockFile()) {
			System.exit(1);
		}

		if (!checkLibs()) {
			System.exit(3);
		}

		Core.getInstance();
	}

	/**
	 * This method checks for the presence of needed .jar files. If one of them
	 * is missing, it shows a Dialog warning the user of the situation.
	 * @return boolean true if all needed jars were present. False otherwise.
	 */
	private boolean checkLibs() {
		// check for needed .jar files by loading a class and catching the error
		String jarFileName = "";
		try {
			// check for xercesImpl.jar
			jarFileName = "xercesImpl.jar";
			Class.forName("org.apache.xerces.dom.DocumentImpl");
			// check for xml-apis.jar
			jarFileName = "xml-apis.jar";
			Class.forName("org.w3c.dom.Document");
			// extra check for OutputFormat (xercesImpl.jar)
			jarFileName = "xercesImpl.jar";
			Class.forName("org.apache.xml.serialize.OutputFormat");
			// check for genChkImpl.jar
			jarFileName = "genChkImpl.jar";
			Class.forName("freenet.client.ClientKey");
			// check for fecImpl.jar
			jarFileName = "fecImpl.jar";
			Class.forName("fecimpl.FECUtils");
			// check for skinlf.jar
			jarFileName = "skinlf.jar";
			Class.forName("com.l2fprod.gui.SkinApplet");
			// check for skinlfFix.jar
			jarFileName = "skinlfFix.jar";
			Class.forName("com.l2fprod.gui.plaf.skin.SkinlfFixMarkerClass");
			//REDFLAG, FIXME: I'm not sure about licensing here.  Theoretically we need to
			//make this optional, but we're linking against so much closed source it probably
			//doesn't matter anymore.
			// check for mailapi.jar
			jarFileName = "mailapi.jar";
			Class.forName("javax.mail.Address");
			// check for smtp.jar
			jarFileName = "smtp.jar";
			Class.forName("com.sun.mail.smtp.SMTPTransport");
			// check for jocache.jar
			jarFileName = "jocache.jar";
			Class.forName("org.shiftone.cache.CacheConfiguration");
		} catch (ClassNotFoundException e1) {
			MiscToolkit.getInstance().showMessage(
				"Please start Frost using the provided start "
					+ "scripts (frost.bat for Windows, frost.sh for Unix).\n"
					+ "If Frost was working and you updated just frost.jar, try updating with frost.zip",
				JOptionPane.ERROR_MESSAGE,
				"ERROR: The jar file " + jarFileName + " is missing.");
			return false;
		}
		return true;
	}

	/**
	 * This method checks if the lockfile is present (therefore indicating that another instance
	 * of Frost is running off the same directory). If it is, it shows a Dialog warning the
	 * user of the situation and returns false. If not, it creates a lockfile and returns true. 
	 * @return boolean false if there was a problem while initializing the lockfile. True otherwise.
	 */
	private boolean initializeLockFile() {
		// check for running frost (lock file)
		File runLock = new File(".frost_run_lock");
		boolean fileCreated = false;
		try {
			fileCreated = runLock.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
		}

		if (fileCreated == false) {
			MiscToolkit.getInstance().showMessage(
				"This indicates that another Frost instance is already running in "
					+ "this directory.\nRunning Frost concurrently will cause data loss.\n"
					+ "If you are REALLY SURE that Frost is not already running, "
					+ "delete the lockfile:\n'"
					+ runLock.getAbsolutePath()
					+ "'",
				JOptionPane.ERROR_MESSAGE,
				"ERROR: Found Frost lock file '.frost_run_lock'.\n");
			return false;
		}
		runLock.deleteOnExit();
		return true;
	}

}
