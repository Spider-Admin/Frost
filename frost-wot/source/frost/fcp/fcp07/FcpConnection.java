/*
  FcpConnection.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
/*
 * DONE PORTING TO 0.7 21.01.06 23:52 Roman
 */
package frost.fcp.fcp07;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.ext.*;
import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.util.*;
import frost.util.Logging;

/**
 * This class is a wrapper to simplify access to the FCP library.
 */
public class FcpConnection {

	private static final Logger logger = Logger.getLogger(FcpConnection.class.getName());

    private final FcpSocket fcpSocket;

    /**
     * Create a connection to a host using FCP
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection to the FCP host.
     */
    public FcpConnection(final NodeAddress na) throws UnknownHostException, IOException {
        fcpSocket = new FcpSocket(na);
    }

    public void close() {
        fcpSocket.close();
    }

    // needs reimplementation, fetches data from hello
    public List<String> getNodeInfo() throws IOException {

    	final ArrayList<String> result = new ArrayList<String>();
        final BufferedReader in = new BufferedReader(new InputStreamReader(fcpSocket.getFcpSock().getInputStream()));

        fcpSocket.getFcpOut().println("ClientHello");
        fcpSocket.getFcpOut().println("Name=hello-"+FcpSocket.getNextFcpId());
        fcpSocket.getFcpOut().println("ExpectedVersion=2.0");
        fcpSocket.getFcpOut().println("EndMessage");
        fcpSocket.getFcpOut().flush();

        while(true) {
            final String tmp = in.readLine();
            if (tmp == null || tmp.trim().equals("EndMessage")) {
                break;
            }
            result.add(tmp);
        }

        in.close();
        close();

        if( result.isEmpty() ) {
            logger.warning("No ClientInfo response!");
            return null;
        }

        return result;
    }

    /**
     * Retrieves the specified key and saves it to the file
     * specified.
     *
     * @param publicKey  the key to be retrieved
     * @param filename  the filename to which the data should be saved
     * @return the results filled with metadata
     */
    public FcpResultGet getKeyToFile(final int type, String keyString, final File targetFile, final int maxSize, final FrostDownloadItem dlItem)
    throws IOException, FcpToolsException, InterruptedIOException {

        File ddaTempFile = null;

        keyString = stripSlashes(keyString);

        final FreenetKey key = new FreenetKey(keyString);
		logger.fine("KeyString = " + keyString + "\n" +
					"Key =       " + key + "\n" +
					"KeyType =   " + key.getKeyType());

        boolean useDDA;
        if( type == FcpHandler.TYPE_MESSAGE ) {
            useDDA = false;
        } else {
            useDDA = fcpSocket.isDDA();
        }

        if (useDDA) {
            // delete before download, else download fails, node will not overwrite anything!
            targetFile.delete();
        }

        fcpSocket.getFcpOut().println("ClientGet");
        fcpSocket.getFcpOut().println("IgnoreDS=false");
        fcpSocket.getFcpOut().println("DSOnly=false");
        fcpSocket.getFcpOut().println("URI=" + key);
        fcpSocket.getFcpOut().println("Identifier=get-" + FcpSocket.getNextFcpId() );
        fcpSocket.getFcpOut().println("MaxRetries=1");
        fcpSocket.getFcpOut().println("Verbosity=-1");

        if (useDDA) {
            fcpSocket.getFcpOut().println("Persistence=connection");
            fcpSocket.getFcpOut().println("ReturnType=disk");
            fcpSocket.getFcpOut().println("Filename=" + targetFile.getAbsolutePath());
            ddaTempFile = new File( targetFile.getAbsolutePath() + "-w");
            if( ddaTempFile.isFile() ) {
                // delete before download, else download fails, node will not overwrite anything!
                ddaTempFile.delete();
            }
            fcpSocket.getFcpOut().println("TempFilename=" + ddaTempFile.getAbsolutePath());
         } else {
             fcpSocket.getFcpOut().println("ReturnType=direct");
        }

        final int prio;
        if( type == FcpHandler.TYPE_FILE ) {
            prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE);
        } else if( type == FcpHandler.TYPE_MESSAGE ) {
            prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE);
        } else {
            prio = 3; // fallback
        }
        fcpSocket.getFcpOut().println("PriorityClass="+Integer.toString(prio));

        if( maxSize > 0 ) {
            fcpSocket.getFcpOut().println("MaxSize="+maxSize);
        }

        fcpSocket.getFcpOut().println("EndMessage");
        fcpSocket.getFcpOut().flush();

        // receive and process node messages
        boolean isSuccess = false;
        int returnCode = -1;
        String codeDescription = null;
        boolean isFatal = false;
        String redirectURI = null;
        while(true) {
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if( nodeMsg == null ) {
                // socket closed
                break;
            }

            if(Logging.inst().doLogFcp2Messages()) {
                System.out.println("*GET** INFO - NodeMessage:");
                System.out.println(nodeMsg.toString());
            }

            final String endMarker = nodeMsg.getMessageEnd();
            if( endMarker == null ) {
                // should never happen
                System.out.println("*GET** ENDMARKER is NULL!");
                break;
            }

            if( !useDDA && nodeMsg.isMessageName("AllData") && endMarker.equals("Data") ) {
                // data follow, first get datalength
                final long dataLength = nodeMsg.getLongValue("DataLength");

                final BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(targetFile));
                final byte[] b = new byte[4096];
                long bytesLeft = dataLength;
                long bytesWritten = 0;
                int count;
                while( bytesLeft > 0 ) {
                    count = fcpSocket.getFcpIn().read(b, 0, ((bytesLeft > b.length)?b.length:(int)bytesLeft));
                    if( count < 0 ) {
                        break;
                    } else {
                        bytesLeft -= count;
                    }
                    fileOut.write(b, 0, count);
                    bytesWritten += count;
                }
                fileOut.close();
                if(Logging.inst().doLogFcp2Messages()) {
                    System.out.println("*GET** Wrote "+bytesWritten+" of "+dataLength+" bytes to file.");
                }
                if( bytesWritten == dataLength ) {
                    isSuccess = true;
                    if( dlItem != null && dlItem.getRequiredBlocks() > 0 ) {
                        dlItem.setFinalized(true);
                        dlItem.setDoneBlocks(dlItem.getRequiredBlocks());
                        dlItem.fireValueChanged();
                    }
                }
                break;
            }

            if( useDDA && nodeMsg.isMessageName("DataFound") ) {
                final long dataLength = nodeMsg.getLongValue("DataLength");
                isSuccess = true;
                System.out.println("*GET**: DataFound, len="+dataLength);
                if( dlItem != null && dlItem.getRequiredBlocks() > 0 ) {
                    dlItem.setFinalized(true);
                    dlItem.setDoneBlocks(dlItem.getRequiredBlocks());
                    dlItem.fireValueChanged();
                }
                break;
            }

            if( nodeMsg.isMessageName("ProtocolError") ) {
                returnCode = nodeMsg.getIntValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }
            if( nodeMsg.isMessageName("IdentifierCollision") ) {
                break;
            }
            if( nodeMsg.isMessageName("UnknownNodeIdentifier") ) {
                break;
            }
            if( nodeMsg.isMessageName("UnknownPeerNoteType") ) {
                break;
            }
            if( nodeMsg.isMessageName("GetFailed") ) {
                // get error code
                returnCode = nodeMsg.getIntValue("Code");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                isFatal = nodeMsg.getBoolValue("Fatal");
                redirectURI = nodeMsg.getStringValue("RedirectURI");
                break;
            }
            if( dlItem != null && nodeMsg.isMessageName("SimpleProgress") ) {
                // eval progress and set to dlItem
                int doneBlocks;
                int requiredBlocks;
                int totalBlocks;
                boolean isFinalized;

                doneBlocks = nodeMsg.getIntValue("Succeeded");
                requiredBlocks = nodeMsg.getIntValue("Required");
                totalBlocks = nodeMsg.getIntValue("Total");
                isFinalized = nodeMsg.getBoolValue("FinalizedTotal");

                if( totalBlocks > 0 && requiredBlocks > 0 ) {
                    dlItem.setDoneBlocks(doneBlocks);
                    dlItem.setRequiredBlocks(requiredBlocks);
                    dlItem.setTotalBlocks(totalBlocks);
                    dlItem.setFinalized(isFinalized);
                    dlItem.fireValueChanged();
                }
                continue;
            }
        }

        close();

        FcpResultGet result = null;

        if( !isSuccess ) {
            // failure
            if( targetFile.isFile() ) {
                targetFile.delete();
            }
            result = new FcpResultGet(false, returnCode, codeDescription, isFatal, redirectURI);
        } else {
            // success
            result = new FcpResultGet(true);
        }

        // in either case, remove dda temp file
        if( ddaTempFile != null && ddaTempFile.isFile() ) {
            ddaTempFile.delete();
        }

        return result;
    }

	/**
     * Inserts the specified key with the data from the file specified.
     *
     * @param publicKey   the key to be inserted
     * @param data  the bytearray with the data to be inserted
     * @return the results filled with metadata and the CHK used to insert the data
	 * @throws IOException
     */
	public FcpResultPut putKeyFromFile(final int type, String keyString, final File sourceFile, final boolean getChkOnly, final boolean doMime, final FrostUploadItem ulItem)
	throws IOException {

        keyString = stripSlashes(keyString);

        boolean useDDA;
        if( type == FcpHandler.TYPE_MESSAGE ) {
            useDDA = false;
        } else {
            useDDA = fcpSocket.isDDA();
        }

        BufferedOutputStream dataOutput = null;
        if( !useDDA ) {
            dataOutput = new BufferedOutputStream(fcpSocket.getFcpSock().getOutputStream());
        }

        fcpSocket.getFcpOut().println("ClientPut");
        fcpSocket.getFcpOut().println("URI=" + keyString);
        fcpSocket.getFcpOut().println("Identifier=put-" + FcpSocket.getNextFcpId() );
        fcpSocket.getFcpOut().println("Verbosity=-1"); // receive SimpleProgress
        fcpSocket.getFcpOut().println("MaxRetries=3");
        fcpSocket.getFcpOut().println("DontCompress=false"); // force compression

        if( keyString.equals("CHK@") ) {
            if( ulItem != null && ulItem.getSharedFileItem() != null ) {
                // shared file, no filename
                fcpSocket.getFcpOut().println("TargetFilename=");
            } else {
                // manual upload, set filename
                fcpSocket.getFcpOut().println("TargetFilename=" + sourceFile.getName());
            }
        }
		if( getChkOnly ) {
            fcpSocket.getFcpOut().println("GetCHKOnly=true");
            fcpSocket.getFcpOut().println("PriorityClass=3");
		} else {
            final int prio;
            if( type == FcpHandler.TYPE_FILE ) {
            	if (doMime) {
                    fcpSocket.getFcpOut().println("Metadata.ContentType=" + DefaultMIMETypes.guessMIMEType(sourceFile.getAbsolutePath()));
            	} else {
            		fcpSocket.getFcpOut().println("Metadata.ContentType=application/octet-stream"); // force this to prevent the node from filename guessing due dda!
            	}
                prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE);
            } else if( type == FcpHandler.TYPE_MESSAGE ) {
                prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE);
            } else {
                prio = 3; // fallback
            }
            fcpSocket.getFcpOut().println("PriorityClass="+Integer.toString(prio));
        }

        fcpSocket.getFcpOut().println("Persistence=connection");

        if (useDDA) {
            // direct file access
            fcpSocket.getFcpOut().println("UploadFrom=disk");
            fcpSocket.getFcpOut().println("Filename=" + sourceFile.getAbsolutePath());
            fcpSocket.getFcpOut().println("EndMessage");
            fcpSocket.getFcpOut().flush();

		} else {
            // send data
            fcpSocket.getFcpOut().println("UploadFrom=direct");
            fcpSocket.getFcpOut().println("DataLength=" + Long.toString(sourceFile.length()));
            fcpSocket.getFcpOut().println("Data");
            fcpSocket.getFcpOut().flush();

			// write complete file to socket
            final BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));
			while( true ) {
				final int d = fileInput.read();
				if( d < 0 ) {
					break; // EOF
				}
				dataOutput.write(d);
			}
            fileInput.close();
            dataOutput.flush();
		}

        // receive and process node messages
        boolean isSuccess = false;
        int returnCode = -1;
        String codeDescription = null;
        boolean isFatal = false;
        String chkKey = null;
        while(true) {
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if( nodeMsg == null ) {
                break;
            }

            if(Logging.inst().doLogFcp2Messages()) {
                System.out.println("*PUT** INFO - NodeMessage:");
                System.out.println(nodeMsg.toString());
            }

            if( getChkOnly == true && nodeMsg.isMessageName("URIGenerated") ) {
                isSuccess = true;
                chkKey = nodeMsg.getStringValue("URI");
                break;
            }
            if( getChkOnly == false && nodeMsg.isMessageName("PutSuccessful") ) {
                isSuccess = true;
                chkKey = nodeMsg.getStringValue("URI");
                if( ulItem != null && ulItem.getTotalBlocks() > 0 ) {
                    ulItem.setDoneBlocks(ulItem.getTotalBlocks());
                }
                break;
            }
            if( nodeMsg.isMessageName("PutFailed") ) {
                // get error code
                returnCode = nodeMsg.getIntValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }

            if( nodeMsg.isMessageName("ProtocolError") ) {
                returnCode = nodeMsg.getIntValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }
            if( nodeMsg.isMessageName("IdentifierCollision") ) {
                break;
            }
            if( nodeMsg.isMessageName("UnknownNodeIdentifier") ) {
                break;
            }
            if( nodeMsg.isMessageName("UnknownPeerNoteType") ) {
                break;
            }
            if( ulItem != null && nodeMsg.isMessageName("SimpleProgress") ) {
                // eval progress and set to dlItem
                int doneBlocks;
                int totalBlocks;
                boolean isFinalized;

                doneBlocks = nodeMsg.getIntValue("Succeeded");
                totalBlocks = nodeMsg.getIntValue("Total");
                isFinalized = nodeMsg.getBoolValue("FinalizedTotal");

                if( totalBlocks > 0 ) {
                    ulItem.setDoneBlocks(doneBlocks);
                    ulItem.setTotalBlocks(totalBlocks);
                    ulItem.setFinalized(isFinalized);
                    ulItem.fireValueChanged();
                }
                continue;
            }
        }

        if( dataOutput != null ) {
            dataOutput.close();
        }

        close();

        if( !isSuccess ) {
            // failure
            if( returnCode == 9 ) {
                return new FcpResultPut(FcpResultPut.KeyCollision, returnCode, codeDescription, isFatal);
            } else if( returnCode == 5 ) {
                return new FcpResultPut(FcpResultPut.Retry, returnCode, codeDescription, isFatal);
            } else {
                return new FcpResultPut(FcpResultPut.Error, returnCode, codeDescription, isFatal);
            }
        } else {
            // success
            // check if the returned text contains the computed CHK key (key generation)
            final int pos = chkKey.indexOf("CHK@");
            if( pos > -1 ) {
                chkKey = chkKey.substring(pos).trim();
            }
            return new FcpResultPut(FcpResultPut.Success, chkKey);
        }
	}

    /**
     * Generates a CHK key for the given File (no upload).
     */
    public String generateCHK(final File file) throws IOException {
        final FcpResultPut result = putKeyFromFile(FcpHandler.TYPE_FILE, "CHK@", file, true, false, null);
        if( result == null || result.isSuccess() == false ) {
            return null;
        } else {
            return result.getChkKey();
        }
    }

    /**
     * returns private and public key
     * @return String[] containing privateKey / publicKey
     */
    public String[] getKeyPair() throws IOException, ConnectException {

        fcpSocket.getFcpOut().println("GenerateSSK");
        fcpSocket.getFcpOut().println("Identifier=genssk-" + FcpSocket.getNextFcpId());
        fcpSocket.getFcpOut().println("EndMessage");
        fcpSocket.getFcpOut().flush();

        // receive and process node messages
        String[] result = null;
        while(true) {
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if( nodeMsg == null ) {
                break;
            }

            System.out.println("*GENERATESSK** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());

            if( nodeMsg.isMessageName("SSKKeypair") ) {

                String insertURI = nodeMsg.getStringValue("InsertURI");
                String requestURI = nodeMsg.getStringValue("RequestURI");

                int pos;
                pos = insertURI.indexOf("SSK@");
                if( pos > -1 ) {
                    insertURI = insertURI.substring(pos).trim();
                }
                if( insertURI.endsWith("/") ) {
                    insertURI = insertURI.substring(0, insertURI.length()-1);
                }

                pos = requestURI.indexOf("SSK@");
                if( pos > -1 ) {
                    requestURI = requestURI.substring(pos).trim();
                }
                if( requestURI.endsWith("/") ) {
                    requestURI = requestURI.substring(0, requestURI.length()-1);
                }

                result = new String[2];
                result[0] = insertURI;
                result[1] = requestURI;

                break;
            }
            // any other message means error here
            break;
        }
        close();
        return result;
    }

    // replaces all / with | in url
    public static String stripSlashes(final String uri) {
    	if (uri.startsWith("KSK@")) {
    		String myUri = null;
    		myUri= uri.replace('/','|');
    		return myUri;
    	} else if (uri.startsWith("SSK@")) {
    		final String sskpart= uri.substring(0, uri.indexOf('/') + 1);
    		final String datapart = uri.substring(uri.indexOf('/')+1).replace('/','|');
    		return sskpart + datapart;
    	} else {
    		return uri;
        }
    }

    public boolean testNodeDDA() {

        final File testFile = createTestFile();
        if( testFile == null ) {
            return false;
        }

        fcpSocket.getFcpOut().println("ClientPut");
        fcpSocket.getFcpOut().println("URI=CHK@");
        fcpSocket.getFcpOut().println("Identifier=testdda-" + FcpSocket.getNextFcpId());
        fcpSocket.getFcpOut().println("Verbosity=0");
        fcpSocket.getFcpOut().println("MaxRetries=0");      // only one try, the node accepts the filename or net
        fcpSocket.getFcpOut().println("PriorityClass=1");   // today, please ;)
        fcpSocket.getFcpOut().println("GetCHKOnly=true");   // calculate the chk of 1k (the default testfile)
        fcpSocket.getFcpOut().println("Global=false");
        fcpSocket.getFcpOut().println("Persistence=connection");
        fcpSocket.getFcpOut().println("DontCompress=true");
        fcpSocket.getFcpOut().println("ClientToken=testdda");
        fcpSocket.getFcpOut().println("UploadFrom=disk");
        fcpSocket.getFcpOut().println("Filename=" + testFile.getAbsolutePath());
        fcpSocket.getFcpOut().println("EndMessage");
        fcpSocket.getFcpOut().flush();

        boolean isSuccess = false;
        while(true) {
            final NodeMessage nodeMsg = NodeMessage.readMessage(fcpSocket.getFcpIn());
            if( nodeMsg == null ) {
                break;
            }
            if(Logging.inst().doLogFcp2Messages()) {
                System.out.println("*TESTDDA** INFO - NodeMessage:");
                System.out.println(nodeMsg.toString());
            }

            if( nodeMsg.isMessageName("PutSuccessful") ) {
                System.out.println("DDA is possible!");
                isSuccess = true;
                break;
            }

            if( nodeMsg.isMessageName("PutFailed") ) {
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("ProtocolError") ) {
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("IdentifierCollision") ) {
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownNodeIdentifier") ) {
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownPeerNoteType") ) {
                System.out.println(nodeMsg.toString());
                break;
            }
        }

        close();

        testFile.delete();

        return isSuccess;
    }

    /**
     * Create an 1 kb file with random data to test if the node can access the file directly
     * @return File  the file or null if somthing went wrong
     */
    private File createTestFile() {
        final File file = FileAccess.createTempFile("dda_", ".tmp");
        final byte[] b = new byte[1024];
        Core.getCrypto().getSecureRandom().nextBytes(b);
        try {
            file.deleteOnExit();
            final FileOutputStream os = new FileOutputStream(file);
            os.write(b, 0, b.length);
            os.close();
        } catch (final IOException ex) {
            logger.log(Level.SEVERE, "DDA testfile creation failed", ex);
            return null;
        }
        return file;
    }
}
