package frost.transferlayer;

import java.io.*;
import java.util.*;
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
        File uploadFile = null;
        byte[] metadata = null;
    }

    protected static boolean prepareIndexFile(FrostIndex frostIndex, IndexFileUploaderWorkArea wa) {
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
            return prepareIndexFile05(frostIndex, wa);
        } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            return prepareIndexFile07(frostIndex, wa);
        } else {
            logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            return false;
        }
    }
    
    public static boolean uploadIndexFile(
            FrostIndex frostIndex, Board board, String insertKey, IndexSlots indexSlots, java.sql.Date date) {
        
        IndexFileUploaderWorkArea wa = new IndexFileUploaderWorkArea();
        
        if( prepareIndexFile(frostIndex, wa) == false ) {
            return false;
        }
        return uploadFile(indexSlots,date,insertKey,wa.uploadFile,wa.metadata);
    }
    
    private static boolean uploadFile(
            IndexSlots indexSlots, 
            java.sql.Date date, 
            String insertKey,
            File uploadFile,
            byte[] metadata) 
    {
        boolean success = false;
        boolean error = false;
        try {
            int tries = 0;
            final int maxTries = 3;
            // get first index and lock it
            int index = indexSlots.findFirstUploadSlot(date);
            while( !success && !error) {
                logger.info("Trying index file upload to index "+index);

                FcpResultPut result = FcpHandler.inst().putFile(
                        FcpHandler.TYPE_MESSAGE,
                        insertKey + index + ".idx.sha3.zip", // TODO: maybe change the ".zip" in the name, its no zip on 0.7
                        uploadFile,
                        metadata,
                        false, // doRedirect
                        true); // removeLocalKey, insert with full HTL even if existing in local store

                if( result.isSuccess() ) {
                    // my files are already added to totalIdx, we don't need to download this index
                    indexSlots.setUploadSlotUsed(index, date);
                    logger.info("FILEDN: Index file successfully uploaded.");
                    success = true;
                } else {
                    if( result.isKeyCollision() ) {
                        // unlock tried slot
                        indexSlots.setUploadSlotUnlocked(index, date);
                        // get next index and lock slot
                        index = indexSlots.findNextUploadSlot(index, date);
                        tries = 0; // reset tries
                        logger.info("FILEDN: Index file collided, increasing index.");
                        continue;
                    }
                    tries++;
                    if( tries < maxTries ) {
                        logger.info("FILEDN: Upload error (try #" + tries + "), retrying index "+index);
                    } else {
                        logger.info("FILEDN: Upload error (try #" + tries + "), giving up on index "+index);
                        indexSlots.setUploadSlotUnlocked(index, date);
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
    
    protected static boolean prepareIndexFile05(FrostIndex frostIndex, IndexFileUploaderWorkArea wa) {
        
        if( frostIndex.getOwnSharer() != null ) {
            frostIndex.signFiles();
        }

        File uploadIndexFile = FileAccess.createTempFile("ix_", ".tmp"); 

        // zip the xml file before upload
        FileAccess.writeZipFile(XMLTools.getRawXMLDocument(frostIndex), "entry", uploadIndexFile);

        if( !uploadIndexFile.isFile() || uploadIndexFile.length() == 0 ) {
            logger.warning("No index file to upload, save/zip failed.");
            return false; // error
        }

        // sign zip file if requested
        if( frostIndex.getOwnSharer() != null ) {
            byte[] zipped = FileAccess.readByteArray(uploadIndexFile);
            SignMetaData md = new SignMetaData(zipped, frostIndex.getOwnSharer());
            wa.metadata = XMLTools.getRawXMLDocument(md);
        }
        wa.uploadFile = uploadIndexFile;
        
        return true;
    }

    protected static boolean prepareIndexFile07(FrostIndex frostIndex, IndexFileUploaderWorkArea wa) {

        if( frostIndex.getOwnSharer() != null ) {
            frostIndex.signFiles();
        }

        File uploadIndexFile = FileAccess.createTempFile("ix_", ".tmp"); 

        FileAccess.writeFile(XMLTools.getRawXMLDocument(frostIndex), uploadIndexFile);
        
        if( !uploadIndexFile.isFile() || uploadIndexFile.length() == 0 ) {
            logger.warning("No index file to upload, save failed.");
            return false; // error
        }
        wa.uploadFile = uploadIndexFile;
        
        return true;
    }
    
    public static boolean uploadRequestFile(
            List sha1ToRequest, Board board, String insertKey, IndexSlots indexSlots, java.sql.Date date) {

        File f = prepareRequestFile(sha1ToRequest);
        if( f == null ) {
            return false;
        }

        return uploadFile(indexSlots, date, insertKey, f, null);
    }
    
    protected static File prepareRequestFile(List sha1ToRequest) {

        File uploadRequestFile = FileAccess.createTempFile("req_", ".tmp");
        StringBuffer sb = new StringBuffer();
        sb.append(SettingsClass.REQUESTFILE_HEADER).append("\n");
        for(Iterator i=sha1ToRequest.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            sb.append(s).append("\n");
        }

        FileAccess.writeFile(sb.toString(), uploadRequestFile, "UTF-8");
        
        if( !uploadRequestFile.isFile() || uploadRequestFile.length() == 0 ) {
            logger.warning("No request file to upload, save failed.");
            return null; // error
        }
        
        return uploadRequestFile;
    }
}
