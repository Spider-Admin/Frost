/*
  requestThread.java / Frost
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
package frost.fileTransfer.download;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import frost.*;
import frost.fcpTools.*;
import frost.gui.objects.FrostBoardObject;
import frost.messages.SharedFileObject;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DownloadThread extends Thread {
	
	private SettingsClass settings;

	private DownloadTicker ticker;

	private static Logger logger = Logger.getLogger(DownloadThread.class.getName());

	public static final String KEYCOLL_INDICATOR = "ERROR: key collision";

	private String filename;
	private Long size;
	private String key;
	private String SHA1;
	private String batch;
	private String owner;
	private FrostBoardObject board;

	private FrostDownloadItem downloadItem;
	private DownloadModel downloadModel;

	public void run() {
		try {
			// some vars
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
			Date today = new Date();
			String date = formatter.format(today);
			File newFile = new File(settings.getValue("downloadDirectory") + filename);
			boolean do_request = false;

			// if we don't have the CHK, means the key was not inserted
			// request it by SHA1
			if (key == null) {
				logger.info("FILEDN: Requesting " + filename);
				downloadItem.setState(FrostDownloadItem.STATE_REQUESTING);

				//request the file itself
				try {
					request();
					logger.info("FILEDN: Uploaded request for " + filename);
				} catch (Throwable t) {
					logger.log(Level.SEVERE, "FILEDN: Uploading request failed for " + filename, t);
				}
				downloadItem.setState(FrostDownloadItem.STATE_REQUESTED);

				downloadItem.setLastDownloadStopTimeMillis(System.currentTimeMillis());
				
				ticker.releaseThread();
				return;
			}

			//otherwise, proceed as usual

			logger.info("FILEDN: Download of '" + filename + "' started.");

			// Download file
			FcpResults success = null;

			try {
					// BBACKFLAG: implement increasing htls!
					success =
						FcpRequest.getFile(key, size, newFile, 25,
											true, // doRedirect
											false, // fastDownload
											false, // createTempFile
											downloadItem);
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Exception thrown in run()", t);
			}

			// file might be erased from table during download...
			//TODO: refactor this check (possible race condition also)
			boolean inTable = false;
			for (int x = 0; x < downloadModel.getItemCount(); x++) {
				FrostDownloadItem actItem = (FrostDownloadItem) downloadModel.getItemAt(x);
				if (actItem.getKey() != null && actItem.getKey().equals(downloadItem.getKey())) {
					inTable = true;
					break;
				}
			}

			// download failed
			if (success == null) {
				downloadItem.setRetries(downloadItem.getRetries() + 1);

				logger.warning("FILEDN: Download of " + filename + " failed.");
				if (inTable == true) {
					// Upload request to request stack
					if (settings.getBoolValue("downloadEnableRequesting")
						&& downloadItem.getRetries()
							>= settings.getIntValue("downloadRequestAfterTries")
						&& board != null
						&& board.isFolder() == false
						&& this.owner != null) // upload requests only if they are NOT manually added
						{
						logger.info("FILEDN: Download failed, uploading request for " + filename);
						downloadItem.setState(FrostDownloadItem.STATE_REQUESTING);

						// We may not do the request here due to the synchronize
						// -> no lock needed, using models
						// doing it after this , the table states Waiting and there are threads running,
						// so download seems to stall
						try {
							request();
							logger.info("FILEDN: Uploaded request for " + filename);
						} catch (Throwable t) {
							logger.log(
								Level.SEVERE,
								"FILEDN: Uploading request failed for " + filename,
								t);
						}
					} else {
						logger.info("FILEDN: Download failed (file is NOT requested).");
					}

					// set new state -> failed or waiting for another try
					if (downloadItem.getRetries()
						> settings.getIntValue("downloadMaxRetries")) {
						if (settings.getBoolValue("downloadRestartFailedDownloads")) {
							downloadItem.setState(FrostDownloadItem.STATE_WAITING);
							downloadItem.setRetries(0);
						} else {
							downloadItem.setState(FrostDownloadItem.STATE_FAILED);
						}
					} else {
						downloadItem.setState(FrostDownloadItem.STATE_WAITING);
					}
				}
			}
			// download successfull
			else {
				// do NOT add manually downloaded files (file have no SHA1, no owner, no board)
				if (board != null
					&& board.isFolder() == false
					&& this.SHA1 != null
					&& Core.frostSettings.getBoolValue("shareDownloads")) {
					// Add successful downloaded key to database
					SharedFileObject newKey = new SharedFileObject(key);
					newKey.setFilename(filename);
					newKey.setSize(newFile.length());
					newKey.setSHA1(SHA1);
					newKey.setDate(date);
					Index.addMine(newKey, board);
				}

				downloadItem.setFileSize(new Long(newFile.length()));
				downloadItem.setState(FrostDownloadItem.STATE_DONE);
				downloadItem.setEnableDownload(Boolean.valueOf(false));

				logger.info("FILEDN: Download of " + filename + " was successful.");
			}
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Oo. EXCEPTION in requestThread.run", t);
		}

		ticker.releaseThread();
		downloadItem.setLastDownloadStopTimeMillis(System.currentTimeMillis());
	}

	// Request a certain file by SHA1
	private void request() {
		int messageUploadHtl = settings.getIntValue("tofUploadHtl");
		boolean requested = false;

		logger.info(
			"FILEDN: Uploading request for '" + filename + "' to board '" + board.toString() + "'");

		String fileSeparator = System.getProperty("file.separator");
		String destination =
			new StringBuffer()
				.append("requests")
				.append(fileSeparator)
				.append(owner)
				.append("-")
				.append(batch)
				.append("-")
				.append(DateFun.getDate())
				.toString();
		File checkDestination = new File(destination);
		if (!checkDestination.isDirectory())
			checkDestination.mkdirs();

		// Check if file was already requested
		// ++ check only in req files
		File[] files = checkDestination.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".req.sha"))
					return true;
				return false;
			}
		});
		for (int i = 0; i < files.length; i++) {
			String content = (FileAccess.readFileRaw(files[i])).trim();
			if (content.equals(SHA1)) {
				requested = true;
				logger.info("FILEDN: File '" + filename + "' was already requested");
				break;
			}
		}

		if (!requested) {
			String date = DateFun.getDate();
			String time = DateFun.getFullExtendedTime() + "GMT";

			// Generate file to upload
			File requestFile = null;
			try {
				requestFile =
					File.createTempFile(
						"reqUpload_",
						null,
						new File(settings.getValue("temp.dir")));
			} catch (Exception ex) {
				requestFile =
					new File(
							settings.getValue("temp.dir")
							+ System.currentTimeMillis()
							+ ".tmp");
			}
			//TOTHINK: we can also encrypt the request
			FileAccess.writeFile(SHA1, requestFile);
			// Write requested key to disk

			// Search empty slot
			boolean success = false;
			int index = 0;
			String output = new String();
			int tries = 0;
			boolean error = false;
			File testMe = null;
			while (!success) {
				// Does this index already exist?
				testMe =
					new File(
						new StringBuffer()
							.append(destination)
							.append(fileSeparator)
							.append(index)
							.append(".req.sha")
							.toString());
				if (testMe.length() > 0) { // already downloaded
					index++;
					//if( DEBUG ) Core.getOut().println("FILEDN: File exists, increasing index to " + index);
					continue; // while
				} else {
					// probably empty, check if other threads currently try to insert to this index
					File lockRequestIndex = new File(testMe.getPath() + ".lock");
					boolean lockFileCreated = false;
					try {
						lockFileCreated = lockRequestIndex.createNewFile();
					} catch (IOException ex) {
						logger.log(
							Level.SEVERE,
							"ERROR: requestThread.request(): unexpected IOException, terminating thread ...",
							ex);
						return;
					}

					if (lockFileCreated == false) {
						// another thread tries to insert using this index, try next
						index++;
						logger.fine(
							"FILEDN: Other thread tries this index, increasing index to " + index);
						continue; // while
					} else {
						// we try this index
						lockRequestIndex.deleteOnExit();
					}

					// try to insert

					String[] result = new String[2];
					String upKey =
						new StringBuffer()
							.append("KSK@frost/request/")
							.append(settings.getValue("messageBase"))
							.append("/")
							.append(owner)
							.append("-")
							.append(batch.trim())
							.append("-")
							.append(date)
							.append("-")
							.append(index)
							.append(".req.sha")
							.toString();
					logger.fine(upKey);
					result = FcpInsert.putFile(upKey, requestFile, messageUploadHtl, false);
					// doRedirect
					logger.fine("FcpInsert result[0] = " + result[0] + " result[1] = " + result[1]);

					if (result[0] == null || result[1] == null) {
						result[0] = "Error";
						result[1] = "Error";
					}

					if (result[0].equals("Success")) {
						success = true;
					} else if (result[0].equals("KeyCollision")) {
						// Check if the collided key is perhapes the requested one
						File compareMe = null;
						try {
							compareMe =
								File.createTempFile(
									"reqUploadCmpDnload_",
									null,
									new File(settings.getValue("temp.dir")));
						} catch (Exception ex) {
							compareMe =
								new File(
										settings.getValue("temp.dir")
										+ System.currentTimeMillis()
										+ ".tmp");
						}
						compareMe.deleteOnExit();

						String requestMe = upKey;

						if (FcpRequest.getFile(requestMe, null, compareMe, 25, false) != null) {
							File numberOne = compareMe;
							File numberTwo = requestFile;
							String contentOne = (FileAccess.readFileRaw(numberOne)).trim();
							String contentTwo = (FileAccess.readFileRaw(numberTwo)).trim();

							//if( DEBUG ) Core.getOut().println(contentOne);
							//if( DEBUG ) Core.getOut().println(contentTwo);

							if (contentOne.equals(contentTwo)) {
								logger.fine("FILEDN: Key Collision and file was already requested");
								success = true;
							} else {
								index++;
								logger.fine(
									"FILEDN: Request Upload collided, increasing index to "
										+ index);

								if (settings.getBoolValue(SettingsClass.DISABLE_REQUESTS) == true) {
									// uploading is disabled, therefore already existing requests are not
									// written to disk, causing key collosions on every request insert.

									// this write a .req file to inform others to not try this index again
									// if user switches to uploading enabled, this dummy .req files should
									// be silently deleted to enable receiving of new requests
									FileAccess.writeFile(KEYCOLL_INDICATOR, testMe);
								}
							}
						} else {
							logger.info(
								"FILEDN: Request upload failed ("
									+ tries
									+ "), retrying index "
									+ index);
							if (tries > 5) {
								success = true;
								error = true;
							}
							tries++;
						}
						compareMe.delete();
					}
					// finally delete the index lock file
					lockRequestIndex.delete();
				}
			}

			if (!error) {
				requestFile.renameTo(testMe);
				logger.info(
					"*********************************************************************\n"
						+ "Request for '"
						+ filename
						+ "' successfully uploaded to board '"
						+ board
						+ "'.\n"
						+ "*********************************************************************");
			} else {
				logger.warning(
					"FILEDN: Error while uploading request for '"
						+ filename
						+ "' to board '"
						+ board
						+ "'.");
				requestFile.delete();
			}
			logger.info("FILEDN: Request Upload Thread finished");
		}
	}

	/**Constructor*/
	public DownloadThread(
		DownloadTicker newTicker,
		FrostDownloadItem item,
		DownloadModel model,
		SettingsClass frostSettings) {

		settings = frostSettings;
		filename = item.getFileName();
		size = item.getFileSize();
		key = item.getKey();
		board = item.getSourceBoard();
		SHA1 = item.getSHA1();
		batch = item.getBatch();
		if (item.getOwner() != null) // owner is null for manually added files
			{
			this.owner = Mixed.makeFilename(item.getOwner());
		} else {
			this.owner = null;
		}

		ticker = newTicker;
		downloadItem = item;
		downloadModel = model;
	}
}
