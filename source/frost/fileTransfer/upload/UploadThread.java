/*
  insertThread.java / Frost
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

package frost.fileTransfer.upload;

import java.io.File;
import java.util.Random;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.Index;
import frost.gui.objects.Board;
import frost.identities.LocalIdentity;
import frost.messages.*;

class UploadThread extends Thread
{
	private SettingsClass settings;
	
    private UploadTicker ticker;

	private LocalIdentity myId;

	static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    
	private static Logger logger = Logger.getLogger(UploadThread.class.getName());
    
    public static final int MODE_GENERATE_SHA1 = 1;
    public static final int MODE_GENERATE_CHK  = 2;
    public static final int MODE_UPLOAD        = 3;

    private int nextState; // the state to set on uploadItem when finished, or -1 for default (IDLE)
    private String destination;
    private File file;
    private int htl;
    private Board board;
    private int mode;
    private static int fileIndex=1;
    private static Random r = new Random();
    //this is gonna be ugly
    private static String batchId = Core.getMyBatches().values().size() == 0 ?
    				(new Long(r.nextLong())).toString() :
				(String) Core.getMyBatches().values().iterator().next();
    private static final int batchSize = 100; //TODO: get this from options
    //private static final Object putIt = frame1.getMyBatches().put(batchId,batchId);
    //^^ ugly trick to put the initial batch number

    FrostUploadItem uploadItem;

	public void run() {
		switch (mode) {
			case MODE_UPLOAD:
				ticker.uploadingThreadStarted();
				break;
			case MODE_GENERATE_SHA1:
				ticker.generatingThreadStarted();
				break;
			case MODE_GENERATE_CHK:
				ticker.generatingThreadStarted();
				break;
		}
		if (batchId == null) {
			Exception er = new Exception();
			er.fillInStackTrace();
			logger.log(Level.SEVERE, "Exception thrown in run()", er);
		}
		if (Core.getMyBatches().values().size() == 0) {
			Core.getMyBatches().put(batchId, batchId);
		}
		boolean sign = MainFrame.frostSettings.getBoolValue("signUploads");
		try {
			switch (mode) {
				case MODE_UPLOAD :
					upload(sign);
					ticker.uploadingThreadFinished();
					break;
				case MODE_GENERATE_SHA1 :
					generateSHA1(sign);
					ticker.generatingThreadFinished();
					break;
				case MODE_GENERATE_CHK :
					generateCHK();
					ticker.generatingThreadFinished();
					break;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in run()", e);
			switch (mode) {
				case MODE_UPLOAD :
					ticker.uploadingThreadFinished();
					break;
				case MODE_GENERATE_SHA1 :
					ticker.generatingThreadFinished();
					break;
				case MODE_GENERATE_CHK :
					ticker.generatingThreadFinished();
					break;
			}
		}
	}
    
    
	/**
	 * 
	 */
	private void upload(boolean sign) { //real upload
		
		String lastUploadDate = null; // NEVER uploaded
		boolean success = false;
		String[] result = { "Error", "Error" };
		String currentDate = DateFun.getExtendedDate();
		
		logger.info("Upload of " + file + " with HTL " + htl + " started.");

		result = FcpInsert.putFile("CHK@", file, null,
			// metadata
			htl, true, // doRedirect
		uploadItem); // provide the uploadItem to indicate that this upload is contained in table

		if (result[0].equals("Success") || result[0].equals("KeyCollision")) {
			success = true;
			uploadItem.setKey(result[1]);
			lastUploadDate = currentDate;
		} 

		if (success == false) {
			// Upload failed
			logger.warning("Upload of " + file + " was NOT successful.");
			
			uploadItem.setRetries(uploadItem.getRetries() + 1);
			if (uploadItem.getRetries() > settings.getIntValue(SettingsClass.UPLOAD_MAX_RETRIES)) {
				if (settings.getBoolValue(SettingsClass.RESTART_FAILED_UPLOADS)) {
					uploadItem.setState(FrostUploadItem.STATE_WAITING);
					uploadItem.setRetries(0);
				} else {
					uploadItem.setState(this.nextState);
				}
			} else {
				uploadItem.setState(FrostUploadItem.STATE_WAITING);
			}
		} else {
			// Upload succeeded
			logger.info("Upload of " + file + " was successful.");
			SharedFileObject current;
			
			uploadItem.setState(nextState);
			uploadItem.setLastUploadDate(lastUploadDate);

			if (uploadItem.getFileSize().longValue() > FcpInsert.smallestChunk) {
				logger.fine("attaching redirect to file " + file.getName());
				current = new FECRedirectFileObject();
				FecSplitfile splitFile = new FecSplitfile(file);
				if (!splitFile.uploadInit())
					throw new Error("file was just uploaded, but .redirect missing!");

				//create a splitfile redirect without progress information
				splitFile.createRedirectFile(false);

				((FECRedirectFileObject) current).setRedirect(
					new String(FileAccess.readByteArray(splitFile.getRedirectFile())));
			} else {
				current = new SharedFileObject();
				logger.fine("not attaching redirect");
			}

			current.setKey(uploadItem.getKey());
			if (sign) {
				current.setOwner(myId.getUniqueName());
			}
			current.setFilename(uploadItem.getFileName());
			current.setSHA1(uploadItem.getSHA1());
			current.setBatch(uploadItem.getBatch());
			current.setSize(uploadItem.getFileSize().longValue());
			current.setDate(lastUploadDate);
			current.setLastSharedDate(lastUploadDate);
			Index.addMine(current, board);
			Index.add(current, board);
		}
		uploadItem.setLastUploadStopTimeMillis(System.currentTimeMillis());
	}
    
	/**
	 * 
	 */
	private void generateSHA1(boolean sign) {
		if (fileIndex % batchSize == 0) {
			Core.getMyBatches().put(batchId, batchId);
			while (Core.getMyBatches().contains(batchId))
				batchId = (new Long(r.nextLong())).toString();
			Core.getMyBatches().put(batchId, batchId);
		}

		long now = System.currentTimeMillis();
		String SHA1 = Core.getCrypto().digest(file);
		logger.fine("digest generated in " + (System.currentTimeMillis() - now) + "  " + SHA1);

		//create new KeyClass
		SharedFileObject newKey = new SharedFileObject();
		newKey.setKey(null);
		newKey.setDate(null);
		newKey.setLastSharedDate(DateFun.getDate());
		newKey.setSHA1(SHA1);
		newKey.setFilename(destination);
		newKey.setSize(file.length());
		newKey.setBatch(batchId);
		if (sign) {
			newKey.setOwner(myId.getUniqueName());
		}

		//update the gui
		uploadItem.setSHA1(SHA1);
		uploadItem.setKey(null);
		uploadItem.setLastUploadDate(null);
		uploadItem.setBatch(batchId);
		fileIndex++;
		//add to index
		Index.addMine(newKey, board);
		Index.add(newKey, board);

		uploadItem.setState(this.nextState);
	}
    
	/**
	 * 
	 */
	private void generateCHK() {
		logger.info("CHK generation started for file: " + file);
		String chkkey = null;

		if (file.length() <= FcpInsert.smallestChunk) {
			logger.info("File too short, doesn't need encoding.");
			// generate only CHK
			chkkey = FecTools.generateCHK(file);
		} else {
			FecSplitfile splitfile = new FecSplitfile(file);
			boolean alreadyEncoded = splitfile.uploadInit();
			if (!alreadyEncoded) {
				try {
					splitfile.encode();
				} catch (Throwable t) {
					logger.log(Level.SEVERE, "Encoding failed", t);
					uploadItem.setState(FrostUploadItem.STATE_IDLE);
					return;
				}
			}
			// yes, this destroys any upload progress, but we come only here if 
			// chkKey == null, so the file should'nt be uploaded until now 
			splitfile.createRedirectFile(false);
			// gen normal redirect file for CHK generation

			chkkey =
				FecTools.generateCHK(
					splitfile.getRedirectFile(),
					splitfile.getRedirectFile().length());
		}

		if (chkkey != null) {
			String prefix = new String("freenet:");
			if (chkkey.startsWith(prefix))
				chkkey = chkkey.substring(prefix.length());
		} else {
			logger.warning("Could not generate CHK key for redirect file.");
		}
		uploadItem.setKey(chkkey);

		// test if the GetRequestsThread did set us the nextState field...
		if (uploadItem.getNextState() > 0) {
			uploadItem.setState(uploadItem.getNextState());
			uploadItem.setNextState(0); // reset nextState
		} else {
			uploadItem.setState(this.nextState);
		}
	}

    /**Constructor*/
    public UploadThread(UploadTicker newTicker, FrostUploadItem ulItem, SettingsClass settings, int mode, LocalIdentity newMyId)
    {
        this(newTicker, ulItem, settings, mode, -1, newMyId);
    }
	public UploadThread(
		UploadTicker newTicker,
		FrostUploadItem ulItem,
		SettingsClass settings,
		int newMode,
		int newNextState,
		LocalIdentity newMyId) {
			
		destination = ulItem.getFileName();
		file = new File(ulItem.getFilePath());

		myId = newMyId;
		ticker = newTicker;
		uploadItem = ulItem;

		this.settings = settings;
		htl = settings.getIntValue("htlUpload");
		board = ulItem.getTargetBoard();
		mode = newMode; // true=upload file false=generate chk (do not upload)
		nextState = newNextState;
		if (nextState < 0) {
			nextState = FrostUploadItem.STATE_IDLE;
		}
	}
}
