/*
 * Created on Apr 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.fileTransfer.download;

import frost.SettingsClass;

/**
 * 
 */
public class DownloadTicker extends Thread {

	private SettingsClass settings;

	private DownloadPanel panel;
	private DownloadTable table;

	/**
	 * @param name
	 */
	public DownloadTicker(
		SettingsClass newSettings,
		DownloadTable newTable,
		DownloadPanel newPanel) {
		super("Download");
		settings = newSettings;
		table = newTable;
		panel = newPanel;
	}

}
