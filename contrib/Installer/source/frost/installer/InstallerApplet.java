package frost.installer;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.JApplet;
import javax.swing.event.*;
import javax.swing.event.DocumentListener;
/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * This applet will install Frost
 *
 * @pattern Applet
 *
 * @generatedBy CodePro Studio at 1/6/04 1:11 PM
 *
 * @author Administrator
 *
 * @version $Revision$
 */
public class InstallerApplet extends JApplet {
	
	//TODO: Put the final location here:
	private static final String JNLP_REMOTE_LOCATION = "http://jarnesto.free.fr/frost/frost.jnlp";
	
	private URL jnlpRemoteLocation = null;
	private JPanel jPanel = null;
	private GreetingPanel greetingPanel = null;
	private LicensePanel licensePanel = null;
	private LocationPanel locationPanel = null;
	private InstallingPanel installingPanel = null;
	
	/**
	 * Returns information about this applet.
	 *
	 * @return a string of information about this applet
	 */
	public String getAppletInfo() {
		return "This applet will install Frost";
	}

	/**
	 * Initializes the applet.
	 *
	 * @see #start
	 * @see #stop
	 * @see #destroy
	 */
	public void init() {
		super.init();
		setContentPane(getJPanel());
		setSize(500, 340);
		try {
			jnlpRemoteLocation = new URL(JNLP_REMOTE_LOCATION);
			getJPanel().add(getGreetingPanel());
		} catch (MalformedURLException exception) {
			getJPanel().add(
				new JLabel(
					"<html>The remote location of the jnlp file is invalid:<br>"
						+ JNLP_REMOTE_LOCATION));
		}
	}

	/**
	 * 
	 */
	void greetingNextButtonPressed() {
		getJPanel().remove(getGreetingPanel());
		getJPanel().add(getLicensePanel());
		getJPanel().revalidate();
		getJPanel().repaint();
	} 

	/**
	 * 
	 */
	void licenseBackButtonPressed() {
		getJPanel().remove(getLicensePanel());
		getJPanel().add(getGreetingPanel());
		getJPanel().repaint();
	}
	
	/**
	 * 
	 */
	void locationBackButtonPressed() {
		getJPanel().remove(getLocationPanel());
		getJPanel().add(getLicensePanel());
		getJPanel().repaint();
	}
	
	/**
	 * 
	 */
	void installingBackButtonPressed() {
		getJPanel().remove(getInstallingPanel());
		getJPanel().add(getLocationPanel());
		getJPanel().repaint();
	}
	
	/**
	 * 
	 */
	void locationInstallButtonPressed() {
		getJPanel().remove(getLocationPanel());
		getJPanel().add(getInstallingPanel());
		getJPanel().revalidate();
		getJPanel().repaint();
		
		String installationPath = getLocationPanel().getPath();
		getInstallingPanel().setJnlpLocalDirectory(new File(installationPath));
		getInstallingPanel().installApplication();
	}

	/**
	 * 
	 */
	void licenseAgreeButtonPressed() {
		getJPanel().remove(getLicensePanel());
		getJPanel().add(getLocationPanel());
		getJPanel().revalidate();
		getJPanel().repaint();
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			jPanel = new JPanel();
		}
		return jPanel;
	}
	/**
	 * @return
	 */
	private GreetingPanel getGreetingPanel() {
		if (greetingPanel == null) {
			greetingPanel = new GreetingPanel(this);	
			greetingPanel.setLogoURL(getClass().getResource("/res/logo.png"));
			greetingPanel.initialize();
		}
		return greetingPanel;
	}

	/**
	 * @return
	 */
	private LicensePanel getLicensePanel() {
		if (licensePanel == null) {
			licensePanel = new LicensePanel(this);
			licensePanel.setLicenseURL(getClass().getResource("/res/gpl.txt"));
			licensePanel.initialize();
		}
		return licensePanel;
	}

	/**
	 * @return
	 */
	private LocationPanel getLocationPanel() {
		if (locationPanel == null) {
			locationPanel = new LocationPanel(this);
			String path = System.getProperty("user.home") + System.getProperty("file.separator") + "Frost";
			File pathFile = new File(path);
			try {
				path = pathFile.getCanonicalPath();
			} catch (Exception e) {
				//If it can't get the canonical path, the normal one will have to do.
			}
			locationPanel.setPath(path);
			locationPanel.initialize();
		}
		return locationPanel;
	}

	/**
	 * @return
	 */
	private InstallingPanel getInstallingPanel() {
		if (installingPanel == null) {
			installingPanel = new InstallingPanel(this);
			installingPanel.setJnlpRemoteLocation(jnlpRemoteLocation);
			installingPanel.initialize();
		}
		return installingPanel;
	}

}  //  @jve:visual-info  decl-index=0 visual-constraint="10,10"