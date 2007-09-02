/*
 FrostPluginManager.java / Frost
 Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>
 
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
package frost.pluginmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import frost.plugins.FrostPlugin;

/**
 * @author saces
 *  
 */
public class PluginManager {
	
	private static final Logger logger = Logger.getLogger(PluginManager.class.getName());
	
	private HashMap<String, PluginInfo> aviablePlugins = new HashMap<String, PluginInfo>();

	private boolean pmActive = false;
	
	public PluginManager() {
		
	}
	
	public PluginInfo[] getAviablePlugins() {
		if (aviablePlugins.size() == 0) return new PluginInfo[] {};
		PluginInfo[] pi = new PluginInfo[aviablePlugins.size()];
		return aviablePlugins.values().toArray(pi);
	}
	
	public void startPM() {
		pmActive = true;
	}
	
	public void stopPM() {
		pmActive = false;
	}

	public boolean isActive() {
		return pmActive ;
	}
	
	public void parseDir(String plugindir) {
		File dir = new File(plugindir);
		
		if (!dir.isDirectory()) {
			logger.log(Level.SEVERE, "Not a valid dir: '"+plugindir+"'");
			return;
		}
		
		String[] pplugs = dir.list();
		if (pplugs.length == 0) {
			logger.log(Level.SEVERE, "Empty dir: '"+plugindir+"'");
			return;
		}
		
		for (String p: pplugs) {
			logger.log(Level.SEVERE, "Check file: '"+plugindir+"/"+p+"'");
			
			try {
				PluginInfo pi = loadPluginInfo(plugindir+"/"+p);
				aviablePlugins.put(pi.getUrl().toExternalForm(), pi);
			} catch (PluginNotFoundException pnfe) {
				logger.log(Level.SEVERE, "Fetch plugininfo failed: '"+plugindir+"/"+p+"'", pnfe);
			}
			
			logger.log(Level.SEVERE, "plugininfi fetched: '"+plugindir+"/"+p+"'");

		}
	}
	
	/**
	 * Method to load a plugin from the given url
	 * Will accept any valid url
	 * "file:/path/to/jarfile.jar"
	 * "fcp:KSK@demo.jar"
	 * 
	 * @param url that points to plugin
	 * @param infoOnly do not load the plugin, fetch only info from jar
	 * @return
	 * @throws PluginNotFoundException
	 * @throws MalformedURLException 
	 */
	private PluginInfo loadPluginInfo(String filename) throws PluginNotFoundException {
		
		// TODO sanitize string
		
//		if (filename.startsWith("/")) {
//			
//		}
		
		URL url;
		try {
			url = new URL("file:"+filename);
		} catch (MalformedURLException mue) {
			throw new PluginNotFoundException(mue);
		}
		return loadPluginInfo(url);
	}
	
	private PluginInfo loadPluginInfo(URL filename) throws PluginNotFoundException {
		
		if(filename == null)
			return null;
		
		BufferedReader in = null;
		InputStream is = null;
		String mainClass = null;
		
		String u = filename.toString();
		
		PluginInfo pi = new PluginInfo();
		
		URL url;
		try {
			url = new URL("jar:"+u+"!/");
		} catch (MalformedURLException mue) {
			throw new PluginNotFoundException(mue);
		}
		
		pi.setURL(filename);
		
		boolean seemsOK = false;
		
		for (int tries = 0 ; (tries <= 5) && (!seemsOK) ; tries++) {
			try {
				JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
				// Java seems to cache even file: urls...
				jarConnection.setUseCaches(false);
				JarFile jf = jarConnection.getJarFile();
			
				is = jf.getInputStream(jf.getJarEntry("META-INF/MANIFEST.MF"));
				in = new BufferedReader(new InputStreamReader(is));	
				String line;
				while ((line = in.readLine())!=null) {
					//	System.err.println(line + "\t\t\t" + realClass);
					if (line.startsWith("Frostplugin-Main-Class: ")) {
						mainClass = line.substring("Frostplugin-Main-Class: ".length()).trim();
						pi.setMainClass(mainClass);
						logger.log(Level.SEVERE, "Found plugin main class "+mainClass+" from manifest");
					}
				}
				
				seemsOK = true;

			} catch (Exception e) {
					if (tries >= 5)
						throw new PluginNotFoundException("Initialization error:"
								+ filename, e);

					try {
						Thread.sleep(100);
					} catch (Exception ee) {}
			} finally {
				try {
					if(is != null)
						is.close();
					if(in != null)
						in.close();
				} catch (IOException ioe){}
			}
		}


		return pi;
	}

	
	public FrostPlugin loadPlugin(PluginInfo plugininfo) throws PluginNotFoundException {

		@SuppressWarnings("unchecked")
		Class cls = null;
		
		for (int tries = 0 ; (tries <= 5) && (cls == null) ; tries++) {
			try {
				// Load the class inside file
				URL[] serverURLs = new URL[]{plugininfo.getUrl()};
				ClassLoader cl = new URLClassLoader(serverURLs);
	
				cls = cl.loadClass(plugininfo.getMainClass());
			} catch (Exception e) {
				if (tries >= 5)
					throw new PluginNotFoundException("Initialization error:"
							+ plugininfo.getUrl(), e);
					try {
						Thread.sleep(100);
					} catch (Exception ee) {}
				}
		}

		if(cls == null)
			throw new PluginNotFoundException("Unknown error");

		// Class loaded... Objectize it!
		Object o = null;
		try {
			o = cls.newInstance();
		} catch (Exception e) {
			throw new PluginNotFoundException("Could not re-create plugin:" +
					plugininfo.getUrl(), e);
		}

		// See if we have the right type
		if (!(o instanceof FrostPlugin)) {
			throw new PluginNotFoundException("Not a frost plugin: " + plugininfo.getUrl());
		}
		
		plugininfo.setPluginClass(o); 

		return (FrostPlugin)o;
	}

	public void startAllPlugins() {
		logger.log(Level.SEVERE, "TODO start all plugins");
		
	}

	public PluginInfo getPluginInfo(String name) {
		return aviablePlugins.get(name);
	}

	public void stopPlugin(PluginInfo pi) {
		logger.log(Level.SEVERE, "TODO stop plugin"+pi.getUrl().toExternalForm());
		pi.setIsRunning(false);
	}

	public void startPlugin(PluginInfo pi) {
		logger.log(Level.SEVERE, "TODO start plugin"+pi.getUrl().toExternalForm());
		FrostPlugin fp = pi.getPlugin();
		fp.startPlugin(null);
		pi.setIsRunning(true);	
	}

}
