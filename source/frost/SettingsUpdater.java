/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost;

/**
 * @author Administrator
 * 
 * All classes that have settings values that have to be updated before
 * the settings are saved to disk must register themselves with SettingsClass
 * and implement this interface.
 */
public interface SettingsUpdater {
	
	/**
	 * This is the callback method that will be called from Settings class
	 * before saving its contents to disk.
	 */
	void updateSettings();
}
