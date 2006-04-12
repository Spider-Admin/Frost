package frost.transferlayer;

import java.io.*;
import java.util.logging.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.fileTransfer.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;

public class IndexFileDownloader {

    private static Logger logger = Logger.getLogger(IndexFileDownloader.class.getName());

    protected static IndexFileDownloaderResult processDownloadedFile(File target, FcpResults fcpresults, Board board) {
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
            return processDownloadedFile05(target, fcpresults, board);
        } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            return processDownloadedFile07(target, fcpresults, board);
        } else {
            logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
            return null;
        }
    }

    /**
     * Returns null if no file found.
     */
    public static IndexFileDownloaderResult downloadIndexFile(String downKey, Board board) {
        
        try {
            File tmpFile = File.createTempFile(
                    "frost-index",
                    ".tmp",
                    new File(Core.frostSettings.getValue("temp.dir")));
            tmpFile.deleteOnExit();
    
            // Download the keyfile
            FcpResults fcpresults = FcpHandler.inst().getFile(
                    downKey,
                    null,
                    tmpFile,
                    Core.frostSettings.getIntValue("keyDownloadHtl"),
                    false); // doRedirect, like in uploadIndexFile()
    
            if (fcpresults == null || tmpFile.length() == 0) {
                // download failed. Sometimes there are some 0 byte
                // files left, we better remove them now.
                tmpFile.delete();
                return null;
            }
            
            return processDownloadedFile(tmpFile, fcpresults, board);
            
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in UpdateIdThread", t);
        }
        return null;
    }
    
    /**
     * This method checks if the digest of sharer matches the pubkey,
     * and adds the NEW identity to list of neutrals.
     * @param _sharer
     * @param _pubkey
     * @return
     */
    protected static Identity addNewSharer(String _sharer, String _pubkey) {

        //check if the digest matches
        String given_digest = _sharer.substring(_sharer.indexOf("@") + 1,
                                                _sharer.length()).trim();
        String calculatedDigest = Core.getCrypto().digest(_pubkey.trim()).trim();
        calculatedDigest = Mixed.makeFilename( calculatedDigest ).trim();

        if( ! Mixed.makeFilename(given_digest).equals( calculatedDigest ) ) {
            logger.warning("Warning: public key of sharer didn't match its digest:\n" +
                           "given digest :'" + given_digest + "'\n" +
                           "pubkey       :'" + _pubkey.trim() + "'\n" +
                           "calc. digest :'" + calculatedDigest + "'");
            return null;
        }
        //create the identity of the sharer
        Identity sharer = new Identity( _sharer.substring(0,_sharer.indexOf("@")), _pubkey);

        //add him to the neutral list (if not already on any list)
        sharer.setState(FrostIdentities.NEUTRAL);
        Core.getIdentities().addIdentity(sharer);

        return sharer;
    }
    
    protected static IndexFileDownloaderResult processDownloadedFile05(File target, FcpResults fcpresults, Board board) {
        try {
            IndexFileDownloaderResult ifdResult = new IndexFileDownloaderResult();
            
            // check if we have received such file before
            String digest = Core.getCrypto().digest(target);
            if( Core.getMessageHashes().contains(digest) ) {
                // we have.  erase and continue
                target.delete();
                ifdResult.errorMsg = IndexFileDownloaderResult.DUPLICATE_FILE;
                return ifdResult;
            } else {
                // else add it to the set of received files to prevent duplicates
                Core.getMessageHashes().add(digest);
            }

            // we need to unzip here to check if identity IN FILE == identity IN METADATA
            byte[] unzippedXml = FileAccess.readZipFileBinary(target);
            if (unzippedXml == null) {
                logger.warning("Could not extract received zip file, skipping.");
                target.delete();
                ifdResult.errorMsg = IndexFileDownloaderResult.BROKEN_DATA;
                return ifdResult;
            }

            File unzippedTarget = new File(target.getPath() + "_unzipped");
            FileAccess.writeFile(unzippedXml, unzippedTarget);
            unzippedXml = null;

            //create the FrostIndex object
            FrostIndex receivedIndex = null;
            try {
                Index idx = Index.getInstance();
                synchronized(idx) {
                    receivedIndex = idx.readKeyFile(unzippedTarget);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Could not parse the index file: ", ex);
            }
            if( receivedIndex == null || receivedIndex.getFilesMap().size() == 0 ) {
                logger.log(Level.SEVERE, "Received index file invalid or empty, skipping.");
                target.delete();
                unzippedTarget.delete();
                ifdResult.errorMsg = IndexFileDownloaderResult.INVALID_DATA;
                return ifdResult;
            }

            Identity sharer = null;
            Identity sharerInFile = receivedIndex.getSharer();
            
            // verify the file if it is signed
            if (fcpresults.getRawMetadata() != null) {
                SignMetaData md;
                try {
                    md = new SignMetaData(fcpresults.getRawMetadata());
                } catch (Throwable t) {
                    // reading of xml metadata failed, handle
                    logger.log(Level.SEVERE, "Could not read the XML metadata, skipping file index.", t);
                    target.delete();
                    unzippedTarget.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.BROKEN_METADATA;
                    return ifdResult;
                }

                //metadata says we're signed.  Check if there is identity in the file
                if (sharerInFile == null) {
                    logger.warning("MetaData present, but file didn't contain an identity :(");
                    target.delete();
                    unzippedTarget.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.BROKEN_METADATA;
                    return ifdResult;
                }

                String _owner = null;
                String _pubkey = null;
                if (md.getPerson() != null) {
                    _owner = Mixed.makeFilename(md.getPerson().getUniqueName());
                    _pubkey = md.getPerson().getKey();
                }

                //check if metadata is proper
                if (_owner == null || _owner.length() == 0 || _pubkey == null || _pubkey.length() == 0) {
                    logger.warning("XML metadata have missing fields, skipping file index.");
                    target.delete();
                    unzippedTarget.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.INVALID_DATA;
                    return ifdResult;
                }

                //check if fields match those in the index file
                if (!_owner.equals(Mixed.makeFilename(sharerInFile.getUniqueName()))
                    || !_pubkey.equals(sharerInFile.getKey())) {

                    logger.warning("The identity in MetaData didn't match the identity in File! :(\n" +
                                    "file owner : " + sharerInFile.getUniqueName() + "\n" +
                                    "file key : " + sharerInFile.getKey() + "\n" +
                                    "meta owner: " + _owner + "\n" +
                                    "meta key : " + _pubkey);
                    target.delete();
                    unzippedTarget.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.TAMPERED_DATA;
                    return ifdResult;
                }

                //verify! :)
                byte[] zippedXml = FileAccess.readByteArray(target);
                boolean valid = Core.getCrypto().detachedVerify(zippedXml, _pubkey, md.getSig());
                zippedXml = null;

                if (valid == false) {
                    logger.warning("Invalid signature for index file from " + _owner);
                    target.delete();
                    unzippedTarget.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.TAMPERED_DATA;
                    return ifdResult;
                }

                //check if we have the owner already on the lists
                if (Core.getIdentities().isMySelf(_owner)) {
                    logger.info("Received index file from myself");
                    sharer = Core.getIdentities().getMyId();
                } else {
                    logger.info("Received index file from " + _owner);
                    sharer = Core.getIdentities().getIdentity(_owner);

                    if( sharer == null ) {
                        // a new sharer, put to neutral list
                        sharer = addNewSharer(_owner, _pubkey);
                        if (sharer == null) { // digest did not match, block file
                            logger.info("sharer was null... :(");
                            target.delete();
                            unzippedTarget.delete();
                            ifdResult.errorMsg = IndexFileDownloaderResult.TAMPERED_DATA;
                            return ifdResult;
                        }
                    } else if (sharer.getState() == FrostIdentities.ENEMY ) {
                        if (Core.frostSettings.getBoolValue("hideBadFiles")) {
                            logger.info("Skipped index file from BAD user " + _owner);
                            target.delete();
                            unzippedTarget.delete();
                            ifdResult.errorMsg = IndexFileDownloaderResult.BAD_USER;
                            return ifdResult;
                        }
                    }
                    // update lastSeen for sharer Identity
                    sharer.updateLastSeenTimestamp();
                }
            } // end-of: if metadata != null
            else if (Core.frostSettings.getBoolValue("hideAnonFiles")) {
                target.delete();
                unzippedTarget.delete();
                ifdResult.errorMsg = IndexFileDownloaderResult.ANONYMOUS_BLOCKED;
                return ifdResult;
            }

            // if the user is not on the GOOD list..
            String sharerStr;
            if (sharer == null || sharer.getState() != FrostIdentities.FRIEND ) {
                // add only files from that user (not files from his friends)
                sharerStr = (sharer == null) ? "Anonymous" : sharer.getUniqueName();
                logger.info("adding only files from " + sharerStr);
            } else {
                // if user is GOOD, add all files (user could have sent files from HIS friends in this index)
                logger.info("adding all files from " + sharer.getUniqueName());
                sharerStr = null;
            }
            Index idx = Index.getInstance();
            synchronized(idx) {
                idx.add(receivedIndex, board, sharerStr);
            }

            target.delete();
            unzippedTarget.delete();
            ifdResult.errorMsg = IndexFileDownloaderResult.SUCCESS;
            return ifdResult;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in UpdateIdThread", t);
        }
        target.delete();
        return null;
    }

    protected static IndexFileDownloaderResult processDownloadedFile07(File target, FcpResults fcpresults, Board board) {

        try {
            IndexFileDownloaderResult ifdResult = new IndexFileDownloaderResult();

            // create the FrostIndex object
            FrostIndex receivedIndex = null;
            try {
                Index idx = Index.getInstance();
                synchronized(idx) {
                    receivedIndex = idx.readKeyFile(target);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Could not parse the index file: ", ex);
            }
            if( receivedIndex == null || receivedIndex.getFilesMap().size() == 0 ) {
                logger.log(Level.SEVERE, "Received index file invalid or empty, skipping.");
                target.delete();
                ifdResult.errorMsg = IndexFileDownloaderResult.INVALID_DATA;
                return ifdResult;
            }
    
            Identity sharer = null;
            Identity sharerInFile = receivedIndex.getSharer();
    
            // verify the file if it is signed
            if (sharerInFile == null) {
                if (Core.frostSettings.getBoolValue("hideAnonFiles")) {
                    target.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.ANONYMOUS_BLOCKED;
                    return ifdResult;
                }
                // else add files to index
            } else {
                String _owner = Mixed.makeFilename(sharerInFile.getUniqueName());
                String _pubkey = sharerInFile.getKey();
        
                boolean sigIsValid = receivedIndex.verifySignature(sharerInFile);
                if (sigIsValid == false) {
                    logger.warning("Invalid signature for index file from " + _owner);
                    target.delete();
                    ifdResult.errorMsg = IndexFileDownloaderResult.TAMPERED_DATA;
                    return ifdResult;
                }

                if( _owner != null ) {
                    // check if we have the owner already on the lists
                    if (Core.getIdentities().isMySelf(_owner)) {
                        logger.info("Received index file from myself");
                        target.delete();
                        ifdResult.errorMsg = IndexFileDownloaderResult.SUCCESS;
                        return ifdResult;
                    } else {
                        logger.info("Received index file from " + _owner);
                        sharer = Core.getIdentities().getIdentity(_owner);
                        
                        if( sharer == null ) {
                            // a new sharer, put to neutral list
                            sharer = addNewSharer(_owner, _pubkey);
                            if (sharer == null) { // digest did not match, block file
                                logger.info("sharer was null... :(");
                                target.delete();
                                ifdResult.errorMsg = IndexFileDownloaderResult.TAMPERED_DATA;
                                return ifdResult;
                            }
                        } else if (sharer.getState() == FrostIdentities.ENEMY ) {
                            if (Core.frostSettings.getBoolValue("hideBadFiles")) {
                                logger.info("Skipped index file from BAD user " + _owner);
                                target.delete();
                                ifdResult.errorMsg = IndexFileDownloaderResult.BAD_USER;
                                return ifdResult;
                            }
                        }
                        // update lastSeen for sharer Identity
                        sharer.updateLastSeenTimestamp();
                    }
                }
            }
            // all tests passed, add files to index
            Index idx = Index.getInstance();
            synchronized(idx) {
                // add all files
                idx.add(receivedIndex, board, null);
            }
            
            target.delete();
            ifdResult.errorMsg = IndexFileDownloaderResult.SUCCESS;
            return ifdResult;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in UpdateIdThread", t);
        }
        target.delete();
        return null;
    }
}
