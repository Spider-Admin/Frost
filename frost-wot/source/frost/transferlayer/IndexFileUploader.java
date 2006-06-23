package frost.transferlayer;

import java.io.*;
import java.util.logging.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.threads.*;

public class IndexFileUploader {

    private static Logger logger = Logger.getLogger(IndexFileUploader.class.getName());

    static class IndexFileUploaderWorkArea {
        
        FrostIndex frostIndex;
        Board board;
        String insertKey;
        IndexSlots indexSlots;
        java.sql.Date callbackDate;
        
        File uploadFile = null;
        byte[] metadata = null;
    }

    protected static boolean prepareIndexFile(IndexFileUploaderWorkArea wa) {
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
            return prepareIndexFile05(wa);
        } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            return prepareIndexFile07(wa);
        } else {
            logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            return false;
        }
    }
    
    public static boolean uploadIndexFile(
            FrostIndex frostIndex, Board board, String insertKey, IndexSlots indexSlots, java.sql.Date date) {
        
        IndexFileUploaderWorkArea wa = new IndexFileUploaderWorkArea();
        
        wa.frostIndex = frostIndex;
        wa.board = board; 
        wa.insertKey = insertKey;
        wa.indexSlots = indexSlots;
        wa.callbackDate = date;
        
        if( prepareIndexFile(wa) == false ) {
            return false;
        }
        return uploadFile(wa);
    }
    
    private static boolean uploadFile(IndexFileUploaderWorkArea wa) {

        boolean success = false;
        boolean error = false;
        try {
            int tries = 0;
            final int maxTries = 3;
            // get first index and lock it
            int index = wa.indexSlots.findFirstUploadSlot(wa.callbackDate);
            while( !success && !error) {
                logger.info("Trying index file upload to index "+index);

                FcpResultPut result = FcpHandler.inst().putFile(
                        wa.insertKey + index + ".idx.sha3.zip", // TODO: maybe change the ".zip" in the name, its no zip on 0.7
                        wa.uploadFile,
                        wa.metadata,
                        Core.frostSettings.getIntValue("keyUploadHtl"),
                        false, // doRedirect
                        true); // removeLocalKey, insert with full HTL even if existing in local store

                if( result.isSuccess() ) {
                    // my files are already added to totalIdx, we don't need to download this index
                    wa.indexSlots.setUploadSlotUsed(index, wa.callbackDate);
                    logger.info("FILEDN: Index file successfully uploaded.");
                    success = true;
                } else {
                    if( result.isKeyCollision() ) {
                        // unlock tried slot
                        wa.indexSlots.setUploadSlotUnlocked(index, wa.callbackDate);
                        // get next index and lock slot
                        index = wa.indexSlots.findNextUploadSlot(index, wa.callbackDate);
                        tries = 0; // reset tries
                        logger.info("FILEDN: Index file collided, increasing index.");
                        continue;
                    }
                    tries++;
                    if( tries < maxTries ) {
                        logger.info("FILEDN: Upload error (try #" + tries + "), retrying index "+index);
                    } else {
                        logger.info("FILEDN: Upload error (try #" + tries + "), giving up on index "+index);
                        wa.indexSlots.setUploadSlotUnlocked(index, wa.callbackDate);
                        error = true;
                    }
                }
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception in uploadFile", e);
        }
        logger.info("FILEDN: Index file upload finished, file uploaded state is: "+success);
        return success;
    }
    
    protected static boolean prepareIndexFile05(IndexFileUploaderWorkArea wa) {
        
        boolean signUpload = Core.frostSettings.getBoolValue("signUploads");
        if( signUpload ) {
            wa.frostIndex.signFiles();
        }

        File uploadIndexFile = new File(Core.frostSettings.getValue("keypool.dir") + 
                wa.board.getBoardFilename() + "_upload.zip");

        // zip the xml file before upload
        FileAccess.writeZipFile(XMLTools.getRawXMLDocument(wa.frostIndex), "entry", uploadIndexFile);

        if( !uploadIndexFile.isFile() || uploadIndexFile.length() == 0 ) {
            logger.warning("No index file to upload, save/zip failed.");
            return false; // error
        }

        // sign zip file if requested
        if( signUpload ) {
            byte[] zipped = FileAccess.readByteArray(uploadIndexFile);
            SignMetaData md = new SignMetaData(zipped, Core.getIdentities().getMyId());
            wa.metadata = XMLTools.getRawXMLDocument(md);
        }
        wa.uploadFile = uploadIndexFile;
        
        return true;
    }

    protected static boolean prepareIndexFile07(IndexFileUploaderWorkArea wa) {

        boolean signUpload = Core.frostSettings.getBoolValue("signUploads");
        if( signUpload ) {
            wa.frostIndex.signFiles();
        }

        File uploadIndexFile = new File(Core.frostSettings.getValue("keypool.dir") + 
                wa.board.getBoardFilename() + "_upload.tmp");

        FileAccess.writeFile(XMLTools.getRawXMLDocument(wa.frostIndex), uploadIndexFile);
        
        if( !uploadIndexFile.isFile() || uploadIndexFile.length() == 0 ) {
            logger.warning("No index file to upload, save failed.");
            return false; // error
        }
        wa.uploadFile = uploadIndexFile;
        
        return true;
    }
}
