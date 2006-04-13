package frost.transferlayer;

import java.io.*;
import java.util.logging.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.gui.objects.*;
import frost.messages.*;

public class IndexFileUploader {

    private static Logger logger = Logger.getLogger(IndexFileUploader.class.getName());

    static class IndexFileUploaderWorkArea {
        
        FrostIndex frostIndex;
        Board board;
        String insertKey;
        IndexFileUploaderCallback callback;
        
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
    
    public static boolean uploadIndexFile(FrostIndex frostIndex, Board board, String insertKey, IndexFileUploaderCallback callback) {
        
        IndexFileUploaderWorkArea wa = new IndexFileUploaderWorkArea();
        
        wa.frostIndex = frostIndex;
        wa.board = board; 
        wa.insertKey = insertKey;
        wa.callback = callback;
        
        if( prepareIndexFile(wa) == false ) {
            return false;
        }
        return uploadFile(wa);
    }
    
    private static boolean uploadFile(IndexFileUploaderWorkArea wa) {

        boolean success = false;
        try {
            int tries = 0;
            final int maxTries = 3;
            int index = wa.callback.findFirstFreeUploadSlot();
            while( !success &&
                   tries < maxTries &&
                   index > -1 ) // no free index found
            {
                logger.info("Trying index file upload to index "+index);

                String[] result = FcpHandler.inst().putFile(
                        wa.insertKey + index + ".idx.sha3.zip", // TODO: maybe change the ".zip" in the name, its no zip on 0.7
                        wa.uploadFile,
                        wa.metadata,
                        Core.frostSettings.getIntValue("keyUploadHtl"),
                        false, // doRedirect
                        true); // removeLocalKey, insert with full HTL even if existing in local store

                if( result[0].equals("Success") || result[0].equals("PutSuccessful") ) {
                    success = true;
                    // my files are already added to totalIdx, we don't need to download this index
                    wa.callback.setSlotUsed(index);
                    logger.info("FILEDN: Index file successfully uploaded.");
                } else {
                    if( result[0].equals("KeyCollision") ) {
                        index = wa.callback.findNextFreeSlot(index);
                        tries = 0; // reset tries
                        logger.info("FILEDN: Index file collided, increasing index.");
                        continue;
                    } else {
                        String tv = result[0];
                        if( tv == null ) {
                            tv = "";
                        }
                        logger.info("FILEDN: Unknown upload error (#" + tries + ", '" + tv+ "'), retrying.");
                    }
                }
                tries++;
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
