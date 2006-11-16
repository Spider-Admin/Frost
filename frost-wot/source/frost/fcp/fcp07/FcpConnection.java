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

import frost.fcp.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;

/**
 * This class is a wrapper to simplify access to the FCP library.
 * @author <a href=mailto:landtuna@hotmail.com>Jim Hunziker</a>
 */
public class FcpConnection
{
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

    private long fcpConnectionId;

    //private static long staticFcpConnectionId = 0;
    
    private static /*synchronized*/ long getNextId() {
        //return staticFcpConnectionId++;
    	return System.currentTimeMillis();
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
        doHandshake(fcpSock);
        fcpSock.close();

        fcpConnectionId = getNextId();
    }

    public void abortConnection() {
        if( fcpSock != null ) {
            try {
                fcpSock.close();
            } catch (IOException e) {
            }
        }
    }

    //needs reimplementation, fetches data from hello
    public List getNodeInfo() throws IOException {

    	ArrayList result = new ArrayList();
        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(fcpSock.getInputStream()));

        fcpOut.println("ClientHello");
        fcpOut.println("Name=hello-"+fcpConnectionId);
        fcpOut.println("ExpectedVersion=2.0");
        fcpOut.println("EndMessage");

        while(true) {
            String tmp = in.readLine();
            if (tmp == null || tmp.trim().equals("EndMessage")) {
                break;
            }
            result.add(tmp);
        }

        in.close();
        fcpOut.close();
        fcpSock.close();
        
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
    public FcpResultGet getKeyToFile(int type, String keyString, String filename, FrostDownloadItem dlItem)
    throws IOException, FcpToolsException, InterruptedIOException {

        // TODO: exploit MaxRetries, MaxSize, ReturnType=disk, global queue
    	
        boolean dda = false;
		
		// the node needs the absolute filename!
		File f = new File(filename);
		filename = f.getAbsolutePath();
		
        keyString = stripSlashes(keyString);
        
        FreenetKey key = new FreenetKey(keyString);
		logger.fine("KeyString = " + keyString + "\n" +
					"Key =       " + key + "\n" +
					"KeyType =   " + key.getKeyType());

        FileOutputStream fileOut = null;
        if (!dda) {
        	fileOut = new FileOutputStream(filename);
        }
        
        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);

        doHandshake(fcpSock);

        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());

        fcpOut.println("ClientGet");
        fcpOut.println("IgnoreDS=false");
        fcpOut.println("DSOnly=false");
        fcpOut.println("URI=" + key);
        fcpOut.println("Identifier=get-" + fcpConnectionId );
        fcpOut.println("MaxRetries=1");

        fcpOut.println("Verbosity=-1");

        if (dda) {
        	fcpOut.println("ReturnType=disk");
            fcpOut.println("Filename=" + filename);
         } else {
        	fcpOut.println("ReturnType=direct");
        }
        
        if( type == FcpHandler.TYPE_FILE ) {
            fcpOut.println("PriorityClass=2");
        } else if( type == FcpHandler.TYPE_MESSAGE ) {
            fcpOut.println("PriorityClass=2");
        }

        fcpOut.println("EndMessage");

        // receive and process node messages
        boolean isSuccess = false;
        int returnCode = -1;
        String codeDescription = null;
        boolean isFatal = false;
        while(true) {
            NodeMessage nodeMsg = readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }
            
            String endMarker = nodeMsg.getMessageEnd(); 
            if( endMarker == null ) {
                // should never happen
                System.out.println("*GET** ENDMARKER is NULL!");
                break;
            }
            
            if( nodeMsg.isMessageName("AllData") && endMarker.equals("Data") ) {
                // data follow, first get datalength
                long dataLength = nodeMsg.getLongValue("DataLength");
                
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
                System.out.println("*GET** Wrote "+bytesWritten+" of "+dataLength+" bytes to file.");
                if( bytesWritten == dataLength ) {
                    isSuccess = true;
                }
                break;
            }
            
            if( nodeMsg.isMessageName("ProtocolError") ) {
                System.out.println("*GET** ProtocolError:");
                System.out.println(nodeMsg.toString());
                
                returnCode = (int)nodeMsg.getLongValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }
            if( nodeMsg.isMessageName("IdentifierCollision") ) {
                System.out.println("*GET** IdentifierCollision:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownNodeIdentifier") ) {
                System.out.println("*** UnknownNodeIdentifier:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownPeerNoteType") ) {
                System.out.println("*GET** UnknownPeerNoteType:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("GetFailed") ) {
                System.out.println("*GET** GetFailed:");
                System.out.println(nodeMsg.toString());
                // get error code
                returnCode = (int)nodeMsg.getLongValue("Code");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                isFatal = nodeMsg.getBoolValue("Fatal");
                break;
            }
            if( dlItem != null && nodeMsg.isMessageName("SimpleProgress") ) {
                System.out.println("*GET** SimpleProgress:");
                System.out.println(nodeMsg.toString());
                
                // eval progress and set to dlItem
                int doneBlocks;
                int requiredBlocks;
                int totalBlocks;
                boolean isFinalized;
                
                doneBlocks = (int)nodeMsg.getLongValue("Succeeded");
                requiredBlocks = (int)nodeMsg.getLongValue("Required");
                totalBlocks = (int)nodeMsg.getLongValue("Total");
                isFinalized = nodeMsg.getBoolValue("FinalizedTotal");
                
                dlItem.setDoneBlocks(doneBlocks);
                dlItem.setRequiredBlocks(requiredBlocks);
                dlItem.setTotalBlocks(totalBlocks);
                dlItem.setFinalized(isFinalized);
                
                continue;
            }
            
            System.out.println("*GET** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());
        }

        fcpIn.close();
        fcpOut.close();
        fcpSock.close();
        fileOut.close();
        
        FcpResultGet result = null;
        
        if( !isSuccess ) {
            // failure
            File checkSize = new File(filename);
            checkSize.delete();
            
            result = new FcpResultGet(false, returnCode, codeDescription, isFatal);
        } else {
            // success
            result = new FcpResultGet(true);
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
	public FcpResultPut putKeyFromFile(int type, String key, File sourceFile, boolean getChkOnly, FrostUploadItem ulItem) throws IOException {
		return putKeyFromFile(type, key, sourceFile, getChkOnly, false, ulItem);
	}
    
	public FcpResultPut putKeyFromFile(int type, String key, File sourceFile, boolean getChkOnly, boolean doMime, FrostUploadItem ulItem)
		throws IOException {

        // TODO: exploit MaxRetries, UploadFrom, type
		
		// TODO and useroption!!!!
//		boolean dda = (type == FcpHandler.TYPE_FILE);
		boolean dda = false;  // disabled, node bug?  r10003
		
        long dataLength = sourceFile.length();
        
        BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));

		// stripping slashes
		key = stripSlashes(key);
		fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
		fcpSock.setSoTimeout(TIMEOUT);

		doHandshake(fcpSock);

		fcpOut = new PrintStream(fcpSock.getOutputStream());
		BufferedOutputStream dOut = new BufferedOutputStream(fcpSock.getOutputStream());
		fcpIn = new BufferedInputStream(fcpSock.getInputStream());

		fcpOut.println("ClientPut");
		fcpOut.println("URI=" + key);
		fcpOut.println("Identifier=put-" + fcpConnectionId );
        fcpOut.println("Verbosity=-1"); // receive SimpleProgress        
		fcpOut.println("MaxRetries=3");
		fcpOut.println("DontCompress=false"); // force compression
        if( key.equals("CHK@") ) { //TODO: hack!
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
            	// TODO: 3? 4? maybee an user option?
            	// or done by the balancer (atomagically)
//            	fcpOut.println("PriorityClass=3");  
            	fcpOut.println("PriorityClass=2");  
            } else if( type == FcpHandler.TYPE_MESSAGE ) {
                fcpOut.println("PriorityClass=2");
            }
        }
		
		if (dda) {  // direct file acess
			//fcpOut.println("Global=true");
			fcpOut.println("Persistence=connection");
			fcpOut.println("ClientToken=blasuelz");
			
	        fcpOut.println("UploadFrom=disk");
	        fcpOut.println("Filename=" + sourceFile.getAbsolutePath());
	        fcpOut.println("EndMessage");
	        //System.out.println("FileName -> " + sourceFile.getAbsolutePath());
			
		} else {    // send data
			
	        fcpOut.println("UploadFrom=direct");

			fcpOut.println("DataLength=" + Long.toString(dataLength));
//			System.out.println("DataLength="+ Long.toString(dataLength));

			fcpOut.println("Data");
			//		System.out.println("Data");
			fcpOut.flush();

			// write complete file to socket
			while( true ) {
				int d = fileInput.read();
				if( d < 0 ) {
					break; // EOF
				}
				dOut.write(d);
			}
		}

		dOut.flush();

        // receive and process node messages
        boolean isSuccess = false;
        int returnCode = -1;
        String codeDescription = null;
        boolean isFatal = false;
        String chkKey = null;
        while(true) {
            NodeMessage nodeMsg = readMessage(fcpIn);
            if( nodeMsg == null ) {
                break;
            }
            
            if( getChkOnly == true && nodeMsg.isMessageName("URIGenerated") ) {
                System.out.println("*PUT** URIGenerated:");
                System.out.println(nodeMsg.toString());

                isSuccess = true;
                chkKey = nodeMsg.getStringValue("URI");
                
                break;
            }
            if( getChkOnly == false && nodeMsg.isMessageName("PutSuccessful") ) {
                System.out.println("*PUT** PutSuccessful:");
                System.out.println(nodeMsg.toString());

                isSuccess = true;
                chkKey = nodeMsg.getStringValue("URI");
                
                break;
            }
            if( nodeMsg.isMessageName("PutFailed") ) {
                System.out.println("*PUT** GetFailed:");
                System.out.println(nodeMsg.toString());
                // get error code
                returnCode = (int)nodeMsg.getLongValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }

            if( nodeMsg.isMessageName("ProtocolError") ) {
                System.out.println("*PUT** ProtocolError:");
                System.out.println(nodeMsg.toString());
                returnCode = (int)nodeMsg.getLongValue("Code");
                isFatal = nodeMsg.getBoolValue("Fatal");
                codeDescription = nodeMsg.getStringValue("CodeDescription");
                break;
            }
            if( nodeMsg.isMessageName("IdentifierCollision") ) {
                System.out.println("*PUT** IdentifierCollision:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownNodeIdentifier") ) {
                System.out.println("*PUT** UnknownNodeIdentifier:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( nodeMsg.isMessageName("UnknownPeerNoteType") ) {
                System.out.println("*PUT** UnknownPeerNoteType:");
                System.out.println(nodeMsg.toString());
                break;
            }
            if( ulItem != null && nodeMsg.isMessageName("SimpleProgress") ) {
                System.out.println("*PUT** SimpleProgress:");
                System.out.println(nodeMsg.toString());
                
                // eval progress and set to dlItem
                int doneBlocks;
                int totalBlocks;
                boolean isFinalized;
                
                doneBlocks = (int)nodeMsg.getLongValue("Succeeded");
                totalBlocks = (int)nodeMsg.getLongValue("Total");
                isFinalized = nodeMsg.getBoolValue("FinalizedTotal");
                
                ulItem.setDoneBlocks(doneBlocks);
                ulItem.setTotalBlocks(totalBlocks);
                ulItem.setFinalized(isFinalized);
                
                continue;
            }
            
            System.out.println("*PUT** INFO - NodeMessage:");
            System.out.println(nodeMsg.toString());
        }
        
		dOut.close();
		fcpOut.close();
		fcpIn.close();
		fcpSock.close();
        
        fileInput.close();
        
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
    public void doHandshake(Socket fcpSocket) throws IOException, ConnectException
    {
        fcpIn = new BufferedInputStream(fcpSocket.getInputStream());
        fcpOut = new PrintStream(fcpSocket.getOutputStream());
        fcpSocket.setSoTimeout(TIMEOUT);

        fcpOut.println("ClientHello");
        logger.fine("ClientHello");
        fcpOut.println("Name=hello-"+ fcpConnectionId);
        logger.fine("Name=hello-"+ fcpConnectionId);
        fcpOut.println("ExpectedVersion=2.0");
        logger.fine("ExpectedVersion=2.0");
        fcpOut.println("End");
    	logger.fine("End");

        FcpKeyword response;
        int timeout = 0;
        do {
            response = FcpKeyword.getFcpKeyword(fcpIn);
    		logger.fine(response.getFullString());
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}

            timeout++;
        } while (response.getId() != FcpKeyword.EndMessage && timeout < 32);

        if (timeout == 32) {
            throw new ConnectException();
        }
    }


    /**
     * Generates a CHK key for the given File (no upload).
     */
    public String generateCHK(File file) throws IOException {
        FcpResultPut result = putKeyFromFile(FcpHandler.TYPE_FILE, "CHK@", file, true, null);
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

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());

        doHandshake(fcpSock);
        fcpOut.println("GenerateSSK");
        fcpOut.println("End");

        String output = "";
        FcpKeyword response;
        int timeout = 0;
        do {
            response = FcpKeyword.getFcpKeyword(fcpIn);
    		logger.fine(response.getFullString());

    		if (response.getId() == FcpKeyword.RequestURI)
    			output += response.getFullString() + "\n";
    		else if (response.getId() == FcpKeyword.InsertURI)
    			output += response.getFullString() + "\n";
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}

            timeout++;
        } while (response.getId() != FcpKeyword.EndMessage && timeout < 32);

        if (timeout == 32) {
            throw new ConnectException();
        }


    	fcpOut.close();
        fcpIn.close();
        fcpSock.close();

        String[] result = {"SSK@","SSK@"};
        String outString = output.toString();
        int insertURI = outString.indexOf("InsertURI=freenet:SSK@") ;
        int requestURI = outString.indexOf("RequestURI=freenet:SSK@") ;

        if (insertURI != -1 && requestURI != -1) {
        	insertURI += "InsertURI=freenet:SSK@".length();
        	requestURI += "RequestURI=freenet:SSK@".length();
    		int insertURIEnd = outString.indexOf("/\n", insertURI);
    		int requestURIEnd = outString.indexOf("/\n", requestURI);

        	if (insertURIEnd != -1 && requestURIEnd != -1) {
            	result[0] += (outString.substring(insertURI, insertURIEnd));
            	result[1] += (outString.substring(requestURI, requestURIEnd));
        	}
        }

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
    
    public NodeMessage readMessage(BufferedInputStream fcpInp) {

        NodeMessage result = null;
        String tmp;
        boolean isfirstline = true;
        
        while(true) {
            tmp = readLine(fcpInp);
            if (tmp == null) { break; }  // this indicates an error, io connection closed
            if ((tmp.trim()).length() == 0) { continue; } // an empty line

            if (isfirstline) {
                result = new NodeMessage(tmp);
                isfirstline = false;
                continue;
            }

            if (tmp.compareTo("Data") == 0) {
                result.setEnd(tmp);
                break; 
            }

            if (tmp.compareTo("EndMessage") == 0) {
                result.setEnd(tmp);
                break; 
            }
            
            if (tmp.indexOf("=") > -1) {
                String[] tmp2 = tmp.split("=", 2);
                result.addItem(tmp2[0], tmp2[1]);
            } else {
                System.err.println("This shouldn't happen. FIXME. mpf!: " + tmp + " -> " + tmp.length());
                result.addItem("Unknown", tmp);
            }
        } 
        return result;  
    }
    
    public String readLine(BufferedInputStream fcpInp) {
        int c;
        StringBuffer sb = new StringBuffer();
        try {
            while ((c = fcpInp.read()) != '\n' && c != -1 && c != '\0' ) {
                sb.append((char)c);
            }
            String str = sb.toString();
            if (str.length() == 0) {
                str = null;
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    class NodeMessage {
        
        private String messageName;
        private Hashtable items;
        private String messageEndMarker;

        /**
         * Creates a new Message
         */
        public NodeMessage(String name) {
            messageName = name;
            items = new Hashtable();
        }

        /** 
         * returns the message as string for debug/log output
         */
        public String toString() {
            return messageName + " " + items + " " + messageEndMarker;
        }
        
        protected void setItem(String name, String value) {
            items.put(name, value);
        }
        
        protected void setEnd(String em) {
            messageEndMarker = em;
        }
        
        protected String getMessageName() {
            return messageName;
        }

        public String getMessageEnd() {
            return messageEndMarker;
        }

        public boolean isMessageName(String aName) {
            if (aName == null) {
                return false;
            }
            return aName.equalsIgnoreCase(messageName);
        }
        
        public String getStringValue(String name) {
            return (String)items.get(name);
        }

        public long getLongValue(String name) {
            return Long.parseLong((String)(items.get(name)));
        }
        
        public long getLongValue(String name, int radix) {
            return Long.parseLong((String)(items.get(name)), radix);
        }
        
        public boolean getBoolValue(String name) {
            return "true".equalsIgnoreCase((String)items.get(name));
        }
        
        public void addItem(String key, String value) {
            items.put(key, value); 
        }
    }
}
