/*
 FrostPluginManager.java / Frost
 
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

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * 
 *  
 */
public class PluginManager {
	
	private static final Logger logger = Logger.getLogger(PluginManager.class.getName());
	
	private HashMap aviablePlugins = new HashMap();

	private boolean pmActive = false;
	
	public PluginManager() {
		
	}
	
	public HashMap getAviablePlugins() {
		return aviablePlugins;
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

}
