/*
  MessageDownloader.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.frost.transfer;

import java.io.File;
import java.util.logging.*;

import frost.Core;
import frost.fcp.*;
import frost.identities.Identity;
import frost.messaging.frost.*;
import frost.util.FileAccess;

public class MessageDownloader {

    private static final Logger logger = Logger.getLogger(MessageDownloader.class.getName());

    /**
     * Process the downloaded file, decrypt, check sign.
     * @param tmpFile  downloaded file
     * @param results  the FcpResults
     * @param logInfo  info for log output
     * @return  null if unexpected Exception occurred, or results indicating state or error
     */
    protected static MessageDownloaderResult processDownloadedFile(final File tmpFile, final FcpResultGet results, final String logInfo) {
        try {
            return processDownloadedFile07(tmpFile, results, logInfo);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Error processing downloaded message", t);
            final MessageDownloaderResult mdResult = new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            return mdResult;
        }
    }

    /**
     * Tries to download the message, performs all base checkings and decryption.
     *
     * @return  null if not found, or MessageDownloaderResult if success or error
     */
    public static MessageDownloaderResult downloadMessage(
            final String downKey,
            final int targetIndex,
            final int maxRetries,
            final String logInfo) {

        FcpResultGet results;
        final File tmpFile = FileAccess.createTempFile("dlMsg_", "-"+targetIndex+".xml.tmp");

        try {
            results = FcpHandler.inst().getFile(
                    FcpHandler.TYPE_MESSAGE,
                    downKey,
                    null,
                    tmpFile,
                    FcpHandler.MAX_MESSAGE_SIZE_07,
                    maxRetries);
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 1."+logInfo, t);
            // download failed
            tmpFile.delete();
            return null;
        }

        if( results == null || results.isSuccess() == false ) {
        	tmpFile.delete();
        	if(results != null && results.getReturnCode() == 28) {
     	    	logger.warning("TOFDN: All data not found."+logInfo);
     	    	System.out.println("TOFDN: ADNF - Contents of message key partially missing.");
     	    	return new MessageDownloaderResult(MessageDownloaderResult.ALLDATANOTFOUND);
            } else if(results != null && results.getReturnCode() == 21) {
                    logger.severe("TOFDN: Message file too big."+logInfo);
                    System.out.println("TOFDN: Message file too big.");
                    return new MessageDownloaderResult(MessageDownloaderResult.MSG_TOO_BIG);
        	} else {
        		return null;
        	}
        }

        return processDownloadedFile(tmpFile, results, logInfo);
    }

    /**
     * Process the downloaded file, decrypt, check sign.
     * @param tmpFile  downloaded file
     * @param results  the FcpResults
     * @param logInfo  info for log output
     * @return  null if unexpected Exception occurred, or results indicating state or error
     */
    protected static MessageDownloaderResult processDownloadedFile07(final File tmpFile, final FcpResultGet results, final String logInfo) {

        try { // we don't want to die for any reason

            // a file was downloaded

            final MessageXmlFile currentMsg;

            try {
                currentMsg = new MessageXmlFile(tmpFile);

            } catch (final MessageCreationException ex) {
                final String errorMessage;
                if( ex.getMessageNo() == MessageCreationException.MSG_NOT_FOR_ME ) {
                    logger.warning("Info: Encrypted message is not for me. "+logInfo);
                    errorMessage = MessageDownloaderResult.MSG_NOT_FOR_ME;

                } else if( ex.getMessageNo() == MessageCreationException.DECRYPT_FAILED ) {
                    logger.log(Level.WARNING, "TOFDN: Exception catched."+logInfo, ex);
                    errorMessage = MessageDownloaderResult.DECRYPT_FAILED;

                } else if( ex.getMessageNo() == MessageCreationException.INVALID_FORMAT ) {
                    logger.warning("Error: Message validation failed. "+logInfo);
                    errorMessage = MessageDownloaderResult.INVALID_MSG;

                } else {
                    logger.log(Level.WARNING, "TOFDN: Exception catched."+logInfo, ex);
                    errorMessage = MessageDownloaderResult.BROKEN_MSG;
                }
                tmpFile.delete();
                return new MessageDownloaderResult(errorMessage);

            } catch (final Throwable ex) {
                logger.log(Level.SEVERE, "TOFDN: Exception catched."+logInfo, ex);
                // file could not be read, mark it invalid not to confuse gui
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            }

            boolean isSignedV2 = false;

            if( currentMsg.getSignatureV2() != null && currentMsg.getSignatureV2().length() > 0 ) {
                isSignedV2 = true;
            }

            if( !isSignedV2 ) {
                // unsigned msg

                // fromName must not contain an '@'
                if( currentMsg.getFromName().indexOf('@') > -1) {
                    // invalid, drop message
                    logger.severe("TOFDN: unsigned message has an invalid fromName (contains an @: '"+
                            currentMsg.getFromName()+"'), message dropped."+logInfo);
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.INVALID_MSG);
                }

                // check and maybe add msg to gui, set to unsigned
                currentMsg.setSignatureStatusOLD();

                return new MessageDownloaderResult(currentMsg);
            }

            final Identity owner = Identity.createIdentityFromExactStrings(currentMsg.getFromName(), currentMsg.getPublicKey());
            if( !Core.getIdentitiesManager().isNewIdentityValid(owner) ) {
                // hash of public key does not match the unique name
                logger.severe("TOFDN: identity failed verification, message dropped." + logInfo);
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.INVALID_MSG);
            }

            // now verify signed content
            final boolean sigIsValid = currentMsg.verifyMessageSignatureV2(owner.getPublicKey());
            logger.info("TOFDN: verification of V2 signature: "+sigIsValid+"."+logInfo);

            // then check if the signature was ok
            if (!sigIsValid) {
                logger.severe("TOFDN: message failed verification, message dropped."+logInfo);
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.INVALID_MSG);
            }

            if( isSignedV2 ) {
                currentMsg.setSignatureStatusVERIFIED_V2();
            } else {
                currentMsg.setSignatureStatusVERIFIED_V1();
            }

            return new MessageDownloaderResult(currentMsg, owner);

        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception catched."+logInfo, t);
            // index is already increased for next try
        }
        tmpFile.delete();
        return null;
    }
}
