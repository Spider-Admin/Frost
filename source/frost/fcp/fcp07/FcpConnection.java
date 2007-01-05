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

import hyperocha.util.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.util.*;

/**
 * This class is a wrapper to simplify access to the FCP library.
 */
public class FcpConnection {

	private static Logger logger = Logger.getLogger(FcpConnection.class.getName());

    // This is the timeout set in Socket.setSoTimeout().
    // The value was 900000 (15 minutes), but I often saw INSERT errors caused by a timeout in the read socket part;
    //   this sometimes leaded to double inserted messages.
    // Using infinite (0) is'nt a good idea, because due to freenet bugs it happened in the past that
    //   the socket blocked forever.
    // We now use with 60 minutes to be sure. mxbee (fuqid developer) told that he would maybe use 90 minutes!
    private final static int TIMEOUT = 60 * 60 * 1000;

    private NodeAddress nodeAddress;
    
    private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;

    private static long staticFcpConnectionId = 0;
    
    private boolean useDDA;
    
    public static synchronized String getNextFcpId() {
        StringBuffer sb = new StringBuffer().append(System.currentTimeMillis()).append(staticFcpConnectionId++);
        return sb.toString();
    }

    /**
     * Create a connection to a host using FCP
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection
     * to the FCP host.
     */
    public FcpConnection(NodeAddress na) throws UnknownHostException, IOException {
        nodeAddress = na;
        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        doHandshake();
        
        if( na.isDirectDiskAccessTested ) {
            useDDA = na.isDirectDiskAccessPossible;
        } else {
            useDDA = false;
        }
    }

    public void closeConnection() {
        if( fcpIn != null ) {
            try {
                fcpIn.close();
            } catch (Throwable e) {
            }
            fcpIn = null;
        }
        if( fcpOut != null ) {
            try {
                fcpOut.close();
            } catch (Throwable e) {
            }
            fcpOut = null;
        }
        if( fcpSock != null ) {
            try {
                fcpSock.close();
            } catch (Throwable e) {
            }
            fcpSock = null;
        }
    }

    // needs reimplementation, fetches data from hello
    public List getNodeInfo() throws IOException {

    	ArrayList result = new ArrayList();
        BufferedReader in = new BufferedReader(new InputStreamReader(fcpSock.getInputStream()));

        fcpOut.println("ClientHello");
        fcpOut.println("Name=hello-"+getNextFcpId());
        fcpOut.println("ExpectedVersion=2.0");
        fcpOut.println("EndMessage");
        fcpOut.flush();

        while(true) {
            String tmp = in.readLine();
            if (tmp == null || tmp.trim().equals("EndMessage")) {
                break;
            }
            result.add(tmp);
        }

        in.close();
        closeConnection();
        
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
     * @param key  the key to be retrieved
     * @param filename  the filename to which the data should be saved
     * @return the results filled with metadata
     */
    public FcpResultGet getKeyToFile(int type, String keyString, File targetFile, int maxSize, FrostDownloadItem dlItem)
    throws IOException, FcpToolsException, InterruptedIOException {

        File ddaTempFile = null;
        
        keyString = stripSlashes(keyString);
        
        FreenetKey key = new FreenetKey(keyString);
		logger.fine("KeyString = " + keyString + "\n" +
					"Key =       " + key + "\n" +
					"KeyType =   " + key.getKeyType());

        if (useDDA) {
            // delete before download, else download fails, node will not overwrite anything!
            targetFile.delete();
        }
        
        fcpOut.println("ClientGet");
        fcpOut.println("IgnoreDS=false");
        fcpOut.println("DSOnly=false");
        fcpOut.println("URI=" + key);
        fcpOut.println("Identifier=get-" + getNextFcpId() );
        fcpOut.println("MaxRetries=1");
        fcpOut.println("Verbosity=-1");

        if (useDDA) {
            fcpOut.println("Persistence=connection");
        	fcpOut.println("ReturnType=disk");
            fcpOut.println("Filename=" + targetFile.getAbsolutePath());
            ddaTempFile = new File( targetFile.getAbsolutePath() + "-w");
            if( ddaTempFile.isFile() ) {
                // delete before download, else download fails, node will not overwrite anything!
                ddaTempFile.delete();
            }
            fcpOut.println("TempFilename=" + ddaTempFile.getAbsolutePath());
         } else {
        	fcpOut.println("ReturnType=direct");
        }
        
        if( type == FcpHandler.TYPE_FILE ) {
            fcpOut.println("PriorityClass=2");
        } else if( type == FcpHandler.TYPE_MESSAGE ) {
            fcpOut.println("PriorityClass=2");
        }
        
        if( maxSize > 0 ) {
            fcpOut.println("MaxSize="+maxSize);
        }

        fcpOut.println("EndMessage");
        fcpOut.flush();

        // receive and process node messages
        boolean isSuccess = false;
        int returnCode = -1;
        String codeDescription = null;
        boolean isFatal = false;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }

            System.out.println("*GET** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());

            String endMarker = nodeMsg.getMessageEnd(); 
            if( endMarker == null ) {
                // should never happen
                System.out.println("*GET** ENDMARKER is NULL!");
                break;
            }

            if( !useDDA && nodeMsg.isMessageName("AllData") && endMarker.equals("Data") ) {
                // data follow, first get datalength
                long dataLength = nodeMsg.getLongValue("DataLength");

                BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(targetFile));
                byte[] b = new byte[4096];
                long bytesLeft = dataLength;
                long bytesWritten = 0;
                int count;
                while( bytesLeft > 0 ) {
                    count = fcpIn.read(b, 0, ((bytesLeft > b.length)?b.length:(int)bytesLeft));
                    if( count < 0 ) {
                        break;
                    } else {
                        bytesLeft -= count;
                    }
                    fileOut.write(b, 0, count);
                    bytesWritten += count;
                }
                fileOut.close();
                System.out.println("*GET** Wrote "+bytesWritten+" of "+dataLength+" bytes to file.");
                if( bytesWritten == dataLength ) {
                    isSuccess = true;
                    if( dlItem != null && dlItem.getRequiredBlocks() > 0 ) {
                        dlItem.setDoneBlocks(dlItem.getRequiredBlocks());
                    }
                }
                break;
            }
            
            if( useDDA && nodeMsg.isMessageName("DataFound") ) {

                long dataLength = nodeMsg.getLongValue("DataLength");
                isSuccess = true;
                System.out.println("*GET**: DataFound, len="+dataLength);
                if( dlItem != null && dlItem.getRequiredBlocks() > 0 ) {
                    dlItem.setDoneBlocks(dlItem.getRequiredBlocks());
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

        closeConnection();
        
        FcpResultGet result = null;
        
        if( !isSuccess ) {
            // failure
            if( targetFile.isFile() ) {
                targetFile.delete();
            }
            result = new FcpResultGet(false, returnCode, codeDescription, isFatal);
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
     * @param key   the key to be inserted
     * @param data  the bytearray with the data to be inserted
     * @return the results filled with metadata and the CHK used to insert the data
	 * @throws IOException 
     */
	public FcpResultPut putKeyFromFile(int type, String keyString, File sourceFile, boolean getChkOnly, boolean doMime, FrostUploadItem ulItem)
	throws IOException {

        keyString = stripSlashes(keyString);

        BufferedOutputStream dataOutput = null;
        if( !useDDA ) {
            dataOutput = new BufferedOutputStream(fcpSock.getOutputStream());
        }

		fcpOut.println("ClientPut");
		fcpOut.println("URI=" + keyString);
		fcpOut.println("Identifier=put-" + getNextFcpId() );
        fcpOut.println("Verbosity=-1"); // receive SimpleProgress        
		fcpOut.println("MaxRetries=3");
		fcpOut.println("DontCompress=false"); // force compression
        // Frost always uploads without a filename
        if( keyString.equals("CHK@") ) {
            fcpOut.println("TargetFilename=");
        }
		if( getChkOnly ) {
			fcpOut.println("GetCHKOnly=true");
		} else {
            if( type == FcpHandler.TYPE_FILE ) {
            	if (doMime) {
            		fcpOut.println("Metadata.ContentType=" + DefaultMIMETypes.guessMIMEType(sourceFile.getAbsolutePath()));
            	} else {
            		fcpOut.println("Metadata.ContentType=application/octet-stream"); // force this to prevent the node from filename guessing due dda!
            	}
            	fcpOut.println("PriorityClass=2");  
            } else if( type == FcpHandler.TYPE_MESSAGE ) {
                fcpOut.println("PriorityClass=2");
            }
        }
		
		if (useDDA) {
            // direct file acess
			fcpOut.println("Persistence=connection");
	        fcpOut.println("UploadFrom=disk");
	        fcpOut.println("Filename=" + sourceFile.getAbsolutePath());
	        fcpOut.println("EndMessage");
            fcpOut.flush();
			
		} else {    
            // send data
	        fcpOut.println("UploadFrom=direct");
			fcpOut.println("DataLength=" + Long.toString(sourceFile.length()));
			fcpOut.println("Data");
			fcpOut.flush();

			// write complete file to socket
            BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));
			while( true ) {
				int d = fileInput.read();
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
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }
            
            System.out.println("*PUT** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());
            
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

        closeConnection();
        
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
            int pos = chkKey.indexOf("CHK@"); 
            if( pos > -1 ) {
                chkKey = chkKey.substring(pos).trim();
            }
            return new FcpResultPut(FcpResultPut.Success, chkKey);
        }
	}

    /**
     * Performs a handshake using this FcpConnection
     */
    public void doHandshake() throws IOException, ConnectException {
        fcpOut.println("ClientHello");
        fcpOut.println("Name=hello-" + getNextFcpId());
        fcpOut.println("ExpectedVersion=2.0");
        fcpOut.println("EndMessage");
        fcpOut.flush();

        // receive and process node messages
        boolean isSuccess = false;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }
            
//            System.out.println("*HANDSHAKE** INFO - NodeMessage:");
//            System.out.println(nodeMsg.toString());

            if( nodeMsg.isMessageName("NodeHello") ) {
                isSuccess = true;
                break;
            }
            // any other message means error here
            break;
        }
        
        if( !isSuccess ) {
            throw new ConnectException();
        }
    }

    /**
     * Generates a CHK key for the given File (no upload).
     */
    public String generateCHK(File file) throws IOException {
        FcpResultPut result = putKeyFromFile(FcpHandler.TYPE_FILE, "CHK@", file, true, false, null);
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

        fcpOut.println("GenerateSSK");
        fcpOut.println("Identifier=genssk-" + getNextFcpId());
        fcpOut.println("EndMessage");
        fcpOut.flush();
        
        // receive and process node messages
        String[] result = null;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
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
        closeConnection();
        return result;
    }

    // replaces all / with | in url
    private String stripSlashes(String uri){
    	if (uri.startsWith("KSK@")) {
    		String myUri = null;
    		myUri= uri.replace('/','|');
    		return myUri;
    	} else if (uri.startsWith("SSK@")) {
    		String sskpart= uri.substring(0, uri.indexOf('/') + 1);
    		String datapart = uri.substring(uri.indexOf('/')+1).replace('/','|');
    		return sskpart + datapart;
    	} else {
    		return uri;
        }
    }
    
    public boolean testNodeDDA() {

        File testFile = createTestFile();
        if( testFile == null ) {
            return false;
        }

        fcpOut.println("ClientPut");
        fcpOut.println("URI=CHK@");
        fcpOut.println("Identifier=testdda-" + getNextFcpId()); 
        fcpOut.println("Verbosity=0");
        fcpOut.println("MaxRetries=0");      // only one try, the node accepts the filename or net
        fcpOut.println("PriorityClass=1");   // today, please ;) 
        fcpOut.println("GetCHKOnly=true");   // calculate the chk of 1k (the default testfile)
        fcpOut.println("Global=false");
        fcpOut.println("Persistence=connection");
        fcpOut.println("DontCompress=true");
        fcpOut.println("ClientToken=testdda"); 
        fcpOut.println("UploadFrom=disk");
        fcpOut.println("Filename=" + testFile.getAbsolutePath());
        fcpOut.println("EndMessage");
        fcpOut.flush();

        boolean isSuccess = false;
        while(true) {
            NodeMessage nodeMsg = NodeMessage.readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }
            System.out.println("*TESTDDA** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());
            
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
        
        closeConnection();
        
        testFile.delete();

        return isSuccess;
    }
    
    /**
     * Create an 1 kb file with random data to test if the node can access the file directly
     * @return File  the file or null if somthing went wrong
     */
    private File createTestFile() {
        File file = FileAccess.createTempFile("dda_", ".tmp");
        byte[] b = new byte[1024]; 
        Core.getCrypto().getSecureRandom().nextBytes(b);
        try {
            file.deleteOnExit();
            FileOutputStream os = new FileOutputStream(file);
            os.write(b, 0, b.length);
            os.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "DDA testfile creation failed", ex);
            return null;
        }
        return file;
    }
}
