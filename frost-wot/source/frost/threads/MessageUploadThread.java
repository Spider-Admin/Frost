/*
  MessageUploadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import org.w3c.dom.Document;

import frost.*;
import frost.crypt.SignMetaData;
import frost.fcp.*;
import frost.fileTransfer.upload.FrostUploadItem;
import frost.gui.MessageUploadFailedDialog;
import frost.gui.objects.FrostBoardObject;
import frost.identities.FrostIdentities;
import frost.messages.*;

/**
 * Uploads a message to a certain message board
 * @author $Author$
 * @version $Revision$
 */
public class MessageUploadThread extends BoardUpdateThreadObject implements BoardUpdateThread {
    
	private static Logger logger = Logger.getLogger(MessageUploadThread.class.getName());
	
    private SettingsClass frostSettings;
	private JFrame parentFrame;
    private FrostBoardObject board;
    
    private String destinationBase;

    private String keypool;
    private MessageObject message;
    
	private File messageFile;
    private int messageUploadHtl;
    private String privateKey;
    private String publicKey;
    private boolean secure;
    
    private byte[] signMetadata;
	private File zipFile;


	/**Constructor
	 * @param board
	 * @param mo
	 * @param newIdentities
	 * @param frostSettings
	 */
	public MessageUploadThread(
		FrostBoardObject board,
		MessageObject mo,
		FrostIdentities newIdentities,
		SettingsClass frostSettings) {
		super(board, newIdentities);
		this.board = board;
		this.message = mo;
		this.frostSettings = frostSettings;

		// we only set the date&time if they are not already set
		// (in case the uploading was pending from before)
		if (mo.getDate() == "") {
			mo.setTime(DateFun.getFullExtendedTime() + "GMT");
			mo.setDate(DateFun.getDate());
		}

		messageUploadHtl = frostSettings.getIntValue("tofUploadHtl");
		keypool = frostSettings.getValue("keypool.dir");

		// this class always creates a new msg file on hd and deletes the file 
		// after upload was successful, or keeps it for next try
		String uploadMe =
			new StringBuffer()
				.append(frostSettings.getValue("unsent.dir"))
				.append("unsent")
				.append(String.valueOf(System.currentTimeMillis()))
				.append(".xml")
				.toString();
		messageFile = new File(uploadMe);
	}

	/**
	 * This method compares the message that is to be uploaded with
	 * a local message to see if they are equal
	 * @param localFile the local message to compare the message to
	 *  	   be uploaded with.
	 * @return true if they are equal. False otherwise.
	 */
	private boolean checkLocalMessage(File localFile) {
		try {
			MessageObject localMessage = new MessageObject(localFile);
			//We compare the messages by content (body), subject, from and attachments
			if (!localMessage.getContent().equals(message.getContent())) {
				return false;	
			} 
			if (!localMessage.getSubject().equals(message.getSubject())) {
				return false;	
			} 
			if (!localMessage.getFrom().equals(message.getFrom())) {
				return false;	
			} 
			AttachmentList attachments1 = message.getAttachmentList();
			AttachmentList attachments2 = localMessage.getAttachmentList();
			if (attachments1.size() != attachments2.size()) {
				return false;	
			}
			Iterator iterator1 = attachments1.iterator();
			Iterator iterator2 = attachments2.iterator();
			while (iterator1.hasNext()) {
				Attachment attachment1 = (Attachment) iterator1.next();	
				Attachment attachment2 = (Attachment) iterator2.next();
				if (attachment1.compareTo(attachment2) != 0) {
					return false;
				}
			}
			return true;
		} catch (Exception exception) {
			logger.log(
				Level.SEVERE,
				"Exception while loading the local file in checkLocalMessage()",
				exception);
			return false; //We assume that the local message is different (it may be corrupted)
		}
	}

	/**
	 * This method is called when there has been a key collision. It checks
	 * if the remote message with that key is the same as the message that is
	 * being uploaded
	 * @param upKey the key of the remote message to compare with the message
	 * 		   that is being uploaded.
	 * @return true if the remote message with the given key equals the 
	 * 			message that is being uploaded. False otherwise.
	 */
	private boolean checkRemoteFile(String key) {

		File remoteFile = new File(messageFile.getPath() + ".coll");
		remoteFile.delete(); // just in case it already exists
		remoteFile.deleteOnExit(); // so that it is deleted when Frost exits

		FcpResults res = FcpRequest.getFile(key, null, remoteFile, messageUploadHtl, false, false);
		if (remoteFile.length() > 0) {
			byte[] unzippedXml = FileAccess.readZipFileBinary(remoteFile);
			FileAccess.writeByteArray(unzippedXml, remoteFile);
			return checkLocalMessage(remoteFile);
		} else {
			return false;
			//We could not retrieve the remote file. We assume they are different.	
		}
	}
	
	/**
	 * This method composes the downloading key for the message, given a
	 * certain index number
	 * @param index index number to use to compose the key
	 * @return they composed key
	 */
	private String composeDownKey(int index) {
		String key;
		if (secure) {
			key =
				new StringBuffer()
					.append(publicKey)
					.append("/")
					.append(board.getBoardFilename())
					.append("/")
					.append(message.getDate())
					.append("-")
					.append(index)
					.append(".xml")
					.toString();
		} else {
			key =
				new StringBuffer()
					.append("KSK@frost/message/")
					.append(frostSettings.getValue("messageBase"))
					.append("/")
					.append(message.getDate())
					.append("-")
					.append(board.getBoardFilename())
					.append("-")
					.append(index)
					.append(".xml")
					.toString();
		}
		return key;
	}

	/**
	 * This method composes the uploading key for the message, given a
	 * certain index number
	 * @param index index number to use to compose the key
	 * @return they composed key
	 */
	private String composeUpKey(int index) {
		String key;
		if (secure) {
			key =
				new StringBuffer()
					.append(privateKey)
					.append("/")
					.append(board.getBoardFilename())
					.append("/")
					.append(message.getDate())
					.append("-")
					.append(index)
					.append(".xml")
					.toString();
		} else {
			key =
				new StringBuffer()
					.append("KSK@frost/message/")
					.append(frostSettings.getValue("messageBase"))
					.append("/")
					.append(message.getDate())
					.append("-")
					.append(board.getBoardFilename())
					.append("-")
					.append(index)
					.append(".xml")
					.toString();
		}
		return key;
	}
	
	/**
	 * This method returns the base path from which we look for
	 * existing files while looking for the next available index to use.
	 * That directory is also created if it doesn't exist.
	 * @return the base path to use when looking for existing files while
	 * 			looking for the next index.
	 */
	private String getDestinationBase() {
		if (destinationBase == null) {
			String fileSeparator = System.getProperty("file.separator");
			destinationBase =
				new StringBuffer()
					.append(keypool)
					.append(board.getBoardFilename())
					.append(fileSeparator)
					.append(DateFun.getDate())
					.append(fileSeparator)
					.toString();
			File makedir = new File(destinationBase);
			if (!makedir.exists()) {
				makedir.mkdirs();
			}
		}
		return destinationBase;
	}

	/* (non-Javadoc)
	 * @see frost.threads.BoardUpdateThread#getThreadType()
	 */
	public int getThreadType() {
		return BoardUpdateThread.MSG_UPLOAD;
	}

	/**
	 * This method performs several tasks before uploading the message.
	 * @return true if the initialization was successful. False otherwise.
	 */
	private boolean initialize() {

		// switch public / secure board
		if (board.isWriteAccessBoard()) {
			privateKey = board.getPrivateKey();
			publicKey = board.getPublicKey();
			secure = true;
		} else {
			secure = false;
		}

		logger.info(
			"TOFUP: Uploading message to board '"
				+ board.toString()
				+ "' with HTL "
				+ messageUploadHtl);

		// first save msg to be able to resend on crash   
		if (!saveMessage(message, messageFile)) {
			logger.severe(
				"This was a HARD error and the file to upload is lost, please report to a dev!");
			return false;
		}

		// BBACKFLAG: ask user if uploading of X files is allowed!
		if (!uploadAttachments(message, messageFile)) {
			return false;
		}

		// zip the xml file to a temp file
		zipFile = new File(messageFile.getPath() + ".upltmp");
		zipFile.delete(); // just in case it already exists
		zipFile.deleteOnExit(); // so that it is deleted when Frost exits
		FileAccess.writeZipFile(FileAccess.readByteArray(messageFile), "entry", zipFile);

		//sign the zipped file if necessary
		String sender = message.getFrom();
		String myId = identities.getMyId().getUniqueName();
		if (sender.equals(myId) //nick same as my identity
			|| sender.equals(Mixed.makeFilename(myId))) //serialization may have changed it
			{
			byte[] zipped = FileAccess.readByteArray(zipFile);
			SignMetaData md = new SignMetaData(zipped, identities.getMyId());
			signMetadata = XMLTools.getRawXMLDocument(md);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		notifyThreadStarted(this);

		boolean retry = true;
		try {
			if (initialize()) {
				while (retry) {
					retry = uploadMessage();
				}
			}
		} catch (IOException ex) {
			logger.log(
				Level.SEVERE,
				"ERROR: MessageUploadThread.run(): unexpected IOException, terminating thread ...",
				ex);
		} catch (MessageAlreadyUploadedException exception) {
			logger.info("The message had already been uploaded. Therefore it will not be uploaded again.");
			messageFile.delete();
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Oo. EXCEPTION in MessageUploadThread", t);
		}

		notifyThreadFinished(this);
	}
	
    /**
     * @param parentFrame
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }	
	
	/**
	 * This method saves a message to disk in XML format 
	 * @param msg the MessageObject to save
	 * @param file the file whose path will be used to save the message
	 * @return true if successful. False otherwise.
	 */
	private boolean saveMessage(MessageObject msg, File file) {
		File tmpFile = new File(file.getPath() + ".tmp");
		boolean success = false;
		try {
			Document doc = XMLTools.createDomDocument();
			doc.appendChild(msg.getXMLElement(doc));
			success = XMLTools.writeXmlFile(doc, tmpFile.getPath());
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, "Exception thrown in saveMessage()", ex);
		}
		if (success && tmpFile.length() > 0) {
			messageFile.delete();
			tmpFile.renameTo(messageFile);
			return true;
		} else {
			tmpFile.delete();
			return false;
		}
	}
	
	/**
	 * This inserts an attached SharedFileObject into freenet
	 * @param attachment the SharedFileObject to upload
	 * @return true if successful. False otherwise.
	 */
	private boolean uploadAttachment(SharedFileObject attachment) {

		assert attachment.getFile() != null : "message.getOfflineFiles() failed!";

		String[] result = { "", "" };
		int uploadHtl = frostSettings.getIntValue("htlUpload");
		logger.info(
			"TOFUP: Uploading attachment "
				+ attachment.getFile().getPath()
				+ " with HTL "
				+ uploadHtl);

		int maxTries = 3;
		int tries = 0;
		while (tries < maxTries
			&& !result[0].equals("KeyCollision")
			&& !result[0].equals("Success")) {
			try {
				result =
					FcpInsert.putFile(
						"CHK@",
						attachment.getFile(),
						null,
						uploadHtl,
						true,
						new FrostUploadItem(null, null));
				// doRedirect
			} catch (Exception ex) {
				result = new String[1];
				result[0] = "Error";
			}
			tries++;
		}
		if (result[0].equals("KeyCollision") || result[0].equals("Success")) {
			logger.info(
				"TOFUP: Upload of attachment '"
					+ attachment.getFile().getPath()
					+ "' was successful.");
			String chk = result[1];
			attachment.setKey(chk);
			attachment.setFilename(attachment.getFile().getName()); // remove path from filename

			if (attachment instanceof FECRedirectFileObject) {
				logger.fine("attaching redirect to file " + attachment.getFile().getName());

				FecSplitfile splitFile = new FecSplitfile(attachment.getFile());
				if (!splitFile.uploadInit())
					throw new Error("file was just uploaded, but .redirect missing!");

				((FECRedirectFileObject) attachment).setRedirect(
					new String(FileAccess.readByteArray(splitFile.getRedirectFile())));
				splitFile.finishUpload(true);
			} else
				logger.fine("not attaching redirect");

			attachment.setFile(null); // we never want to give out a real pathname, this is paranoia
			return true;
		} else {
			logger.warning(
				"TOFUP: Upload of attachment '"
					+ attachment.getFile().getPath()
					+ "' was NOT successful.");
			return false;
		}
	}

	/**
	 * Uploads all the attachments of a MessageObject and updates its
	 * XML representation on disk
	 * @param msg the MessageObject whose attachments will be uploaded
	 * @param file file whose path will be used to save the MessageObject to disk.
	 * @return true if successful. False otherwise.
	 */
	private boolean uploadAttachments(MessageObject msg, File file) {
		boolean success = true;
		List fileAttachments = msg.getOfflineFiles();
		Iterator i = fileAttachments.iterator();
		while (i.hasNext()) {
			SharedFileObject attachment = (SharedFileObject) i.next();
			if(uploadAttachment(attachment)) {
				//If the attachment was successfully inserted, we update the message on disk.
				saveMessage(msg, file);
			} else {
				success = false;	
			}
		}

		if (!success) {
			JOptionPane.showMessageDialog(
				parentFrame,
				"One or more attachments failed to upload.\n"
					+ "Will retry to upload attachments and message on next startup.",
				"Attachment upload failed",
				JOptionPane.ERROR_MESSAGE);
		}

		return success;
	}
	
    /**
     * @return
     * @throws IOException
     * @throws MessageAlreadyUploadedException
     */
    private boolean uploadMessage() throws IOException, MessageAlreadyUploadedException {
        boolean success = false;
        int index = 0;
        int tries = 0;
        int maxTries = 5;
        boolean error = false;
        boolean tryAgain;
        while (!success) {
            // Does this index already exist?
            String testFilename = new StringBuffer().append(getDestinationBase()).append(message.getDate())
                    .append("-").append(board.getBoardFilename()).append("-").append(index).append(".xml")
                    .toString();
            File testMe = new File(testFilename);
            if (testMe.exists() && testMe.length() > 0) {
                if (checkLocalMessage(testMe)) {
                    throw new MessageAlreadyUploadedException();
                } else {
                    index++;
                }
            } else {
                // probably empty, check if other threads currently try to
                // insert to this index
                File lockRequestIndex = new File(testMe.getPath() + ".lock");
                boolean lockFileCreated = false;
                lockFileCreated = lockRequestIndex.createNewFile();

                if (lockFileCreated == false) {
                    // another thread tries to insert using this index, try next
                    index++;
                    logger.fine("TOFUP: Other thread tries this index, increasing index to " + index);
                    continue; // while
                } else {
                    // we try this index
                    lockRequestIndex.deleteOnExit();
                }

                // try to insert message
                String[] result = new String[2];
                String upKey = composeUpKey(index);
                String downKey = composeDownKey(index);

                try {
                    //signMetadata is null for unsigned upload. Do not do
                    // redirect (false)
                    result = FcpInsert.putFile(upKey, zipFile, signMetadata, messageUploadHtl, false);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "TOFUP - Error in run()/FcpInsert.putFile", t);
                }

                if (result[0] == null || result[1] == null) {
                    result[0] = "Error";
                    result[1] = "Error";
                }

                if (result[0].equals("Success")) {
                    success = true;
                } else {
                    if (result[0].equals("KeyCollision")) {
                        if (checkRemoteFile(downKey)) {
                            throw new MessageAlreadyUploadedException();
                        } else {
                            index++;
                            logger.fine("TOFUP: Upload collided, increasing index to " + index);
                        }
                    } else {
                        if (tries > maxTries) {
                            success = true;
                            error = true;
                        } else {
                            logger.info("TOFUP: Upload failed (try no. " + tries + " of " + maxTries
                                    + "), retrying index " + index);
                            tries++;
                        }
                    }
                }
                // finally delete the index lock file
                if (lockFileCreated == true) {
                    lockRequestIndex.delete();
                }
            }
        }

        if (!error) {
            // we will see the message if received from freenet
            messageFile.delete();
            zipFile.delete();

            logger.info("*********************************************************************\n"
                    + "Message successfuly uploaded to board '" + board.toString() + "'.\n"
                    + "*********************************************************************");
            tryAgain = false;
        } else {
            logger.warning("TOFUP: Error while uploading message.");

            boolean retrySilently = frostSettings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES);
            if (!retrySilently) {
                // Uploading of that message failed. Ask the user if Frost
                // should try to upload the message another time.
                MessageUploadFailedDialog faildialog = new MessageUploadFailedDialog(parentFrame);
                int answer = faildialog.startDialog();
                if (answer == MessageUploadFailedDialog.RETRY_VALUE) {
                    logger.info("TOFUP: Will try to upload again.");
                    tryAgain = true;
                } else if (answer == MessageUploadFailedDialog.RETRY_NEXT_STARTUP_VALUE) {
                    zipFile.delete();
                    logger.info("TOFUP: Will try to upload again on next startup.");
                    tryAgain = false;
                } else if (answer == MessageUploadFailedDialog.DISCARD_VALUE) {
                    zipFile.delete();
                    messageFile.delete();
                    logger.warning("TOFUP: Will NOT try to upload message again.");
                    tryAgain = false;
                } else { // paranoia
                    logger.warning("TOFUP: Paranoia - will try to upload message again.");
                    tryAgain = true;
                }
            } else {
                //Retry silently
                tryAgain = true;
            }
        }
        logger.info("TOFUP: Upload Thread finished");
        return tryAgain;
    }
}
