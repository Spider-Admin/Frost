/*
  PluginInfo.java / Frost
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

import java.net.URL;

import javax.swing.JPanel;

import frost.plugins.FrostPlugin;

/**
 * @author saces
 *
 */
public class PluginInfo {

	private URL url;
	private String mainClass;
	private FrostPlugin pluginClass;
	boolean isRunning = false;

	public String getPluginName() {
		return mainClass + '@' + url;  // TODO proper pluginname
	}

	protected void setURL(URL filename) {
		url = filename;
	}

	protected void setMainClass(String mainclass) {
		mainClass = mainclass;	
	}

	public URL getUrl() {
		return url;
	}

	public String getMainClass() {
		return mainClass;
	}

	public boolean isRunning() {
		return isRunning;
	}

	protected void setPluginClass(Object o) {
		if (!(o instanceof FrostPlugin)) {
			throw new IllegalArgumentException("Not a frost plugin: " + o.getClass().getName());
		}
		setPluginClass((FrostPlugin) o);
	}
	
	protected void setPluginClass(FrostPlugin pc) {
		pluginClass = pc;
	}

	protected void setIsRunning(boolean b) {
		isRunning = b;		
	}

	public JPanel getPluginPanel() {
		return pluginClass.getPluginPanel();
	}

	protected FrostPlugin getPlugin() {
		return pluginClass;
	}

}
