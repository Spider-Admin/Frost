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

import hyperocha.freenet.fcp.utils.DefaultMIMETypes;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.fcp.*;

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
//        System.out.println("ClientHello");
        fcpOut.println("Name=hello-"+fcpConnectionId);
//        System.out.println("Name=hello-"+fcpConnectionId);
        fcpOut.println("ExpectedVersion=2.0");
//        System.out.println("ExpectedVersion=2.0");
        fcpOut.println("EndMessage");
//        System.out.println("EndMessage");

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
     * @param htl the HTL to use in this request
     * @return the results filled with metadata
     */
    public FcpResultGet getKeyToFile(int type, String keyString, String filename)
    throws IOException, FcpToolsException, InterruptedIOException {

        // FIXME: exploit MaxRetries, MaxSize, ReturnType=disk, global queue
    	
//    	 FIXME and useroption!!!!
		boolean dda = (type == FcpHandler.TYPE_FILE);
		
		
		// the node needs the absolute filename!
		File f = new File(filename);
		filename = f.getAbsolutePath();
		
		//System.out.println("test: " + filename);

        keyString = StripSlashes(keyString);
        FcpResultGet result = new FcpResultGet();
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
//        System.out.println("ClientGet");
        fcpOut.println("IgnoreDS=false");
//        System.out.println("IgnoreDS=false");
        fcpOut.println("DSOnly=false");
//        System.out.println("DSOnly=false");
        fcpOut.println("URI=" + key);
//        System.out.println("URI=" + key);

//        System.out.println("Verbosity=0");
        fcpOut.println("Identifier=get-" + fcpConnectionId );
//        System.out.println("Identifier=get");

//        System.out.println("ReturnType=direct");

        //fcpOut.println("ReturnType=disk");
        //System.out.println("Filename=<filename_on_disk>");

        //fcpOut.println("MaxSize=256");
        //System.out.println("MaxSize=256");
        //fcpOut.println("MaxTempSize=1024");
        //System.out.println("MaxTempSize=1024");


        fcpOut.println("MaxRetries=1");
//        System.out.println("MaxRetries=1");

        if (dda) {
        	fcpOut.println("Verbosity=0");
        	fcpOut.println("ReturnType=disk");
            fcpOut.println("Filename=" + filename);
         } else {
        	fcpOut.println("Verbosity=0");
        	fcpOut.println("ReturnType=direct");
        }
        
        if( type == FcpHandler.TYPE_FILE ) {
            fcpOut.println("PriorityClass=4");
        } else if( type == FcpHandler.TYPE_MESSAGE ) {
            fcpOut.println("PriorityClass=2");
        }

        fcpOut.println("EndMessage");
//        System.out.println("EndMessage");

        FcpKeyword kw;
        boolean receivedFinalByte = false;
        long totalDataLength = 0;
        int dataChunkLength = 0;
        boolean expectingData = false;
        boolean flagRestarted = false;
        long expectedTotalDataLength = 0;

        while( receivedFinalByte == false )
        {
            //frost.Core.getOut().print("*");
            if( expectingData == false )
            {
                kw = FcpKeyword.getFcpKeyword(fcpIn);
				logger.fine("FcpKeyword: " + kw + " for file " + filename);

//                frost.Core.getOut().println("getKey-FcpKeyword: " + kw + " for file " + filename);
//				System.out.println("got fcp keyword");
//				System.out.println("FcpKeyword: " + kw + " for file " + filename);
//				System.out.println(kw.getFullString());
                switch( kw.getId() )
                {
                case FcpKeyword.DataFound:
                	if (dda) {
                		receivedFinalByte = true;
                	} else
                    if( flagRestarted == true )
                    {
                        fileOut.close();
                        new File(filename).delete();
                        fileOut = new FileOutputStream(filename);

                        totalDataLength = 0;
                        expectedTotalDataLength = 0;
                        dataChunkLength = 0;

                        flagRestarted = false;
                    }
                    break;
                case FcpKeyword.DataLength:
                    totalDataLength = kw.getLongVal();
                    expectedTotalDataLength = totalDataLength;
                    break;
                case FcpKeyword.FormatError:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.URIError:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.Restarted:
/*
   At any time when the full payload of data has not been sent a
   Restarted message may be sent. This means that the data to verify and
   the transfer will be restarted. The client should return to the
   waiting state, and if a DataFound is then received, the data transfer
   will start over from the beginning. Otherwise, when the final
   DataChunk is received, the transaction is complete and the connection
   dies.
bback - FIX: in FcpKeyword.DataFound - prepare all for start from the beginning
*/
                    flagRestarted = true;
                    break;
                case FcpKeyword.Code:
                    //frost.Core.getOut().println("Data not found - closing streams for " + filename + " ...");
                	int codeNumber = (int) kw.getLongVal();
                	if (codeNumber == 13){
                		fcpIn.close();
                		fcpOut.close();
                		fcpSock.close();
                		fileOut.close();
                		File checkSize = new File(filename);
                		if( checkSize.length() == 0 )
                			checkSize.delete();
                		throw new DataNotFoundException();
                	}
                	break;
                case FcpKeyword.Fatal:
                	if (kw.getFullString().indexOf("true") > -1){
                		fcpIn.close();
                		fcpOut.close();
                		fcpSock.close();
                		fileOut.close();
                		File checkSize = new File(filename);
                		if( checkSize.length() == 0 )
                			checkSize.delete();
                		throw new FcpToolsException("fatalerror on get");
                	}
                	receivedFinalByte=true;

                	break;
                case FcpKeyword.GetFailed:
                	break;
                case FcpKeyword.RouteNotFound:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.Failed:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.UnknownError:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.EndMessage:
                    break;
                case FcpKeyword.DataChunk:
                    break;

                case FcpKeyword.Data:
                    expectingData = true;
                    break;
/*
 * DEFAULT RESPONSE NOW
 * AllData
 * Identifier=get
 * DataLength=361
 * Data
 *
 *
 */
                case FcpKeyword.AllData:
                	break;
                case FcpKeyword.Identifier:
                	break;

                case FcpKeyword.Timeout:
// it WOULD be actually better for freenet AND the node to do it this way
// would be , but after 25 minutes my 5 boards did not finish to update, 4 days backload
// thats really too slow ...
// now the fast mode is only used by MessageDownloadThread ...
//                    if( fastDownload )  receivedFinalByte = true;
                    break;
                }
            } else { // handle data bytes
            	
            	//System.out.println(" -->> try read data");

            	dataChunkLength = (int) totalDataLength;
                logger.fine("Expecting " + dataChunkLength + " bytes, " + totalDataLength + " total.");
//                System.out.println("Expecting " + dataChunkLength + " bytes, " + totalDataLength + " total.");
                byte[] b = new byte[dataChunkLength];
                int bytesRead = 0, count;

                while( bytesRead < dataChunkLength ) {
                    count = fcpIn.read(b, bytesRead, dataChunkLength - bytesRead);
//                    System.out.println("read following:");
                    //System.out.println(new String(b, Charset.defaultCharset().name()));
//                    System.out.println(count);
                    if( count < 0 ) {
                        break;
                    } else {
                        bytesRead += count;
                    }
                }

//                System.out.println("GOT DATA");
                fileOut.write(b);
                expectingData = false;
                totalDataLength -= bytesRead;
                if( totalDataLength <= 0 ) {
                    receivedFinalByte = true;
                }
            }
        }

        fcpIn.close();
        fcpOut.close();
        fcpSock.close();
        if (!dda) {
        	fileOut.flush();
        	fileOut.close();
        }
        File checkSize = new File(filename);
        
        if( checkSize.length() == 0 ) {
            checkSize.delete();
        }
        return result;
    }

	/**
     * Inserts the specified key with the data from the file specified.
     *
     * @param key   the key to be inserted
     * @param data  the bytearray with the data to be inserted
     * @return the results filled with metadata and the CHK used to insert the data
     */
	public String putKeyFromFile(int type, String key, File sourceFile, boolean getchkonly)
		throws IOException {

        // FIXME: exploit MaxRetries, UploadFrom, type
		
		
		// FIXME and useroption!!!!
		boolean dda = (type == FcpHandler.TYPE_FILE);
		//boolean dda = false;  // disabled, node bug?  r10003
		
        long dataLength = sourceFile.length();
        
        BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));

		// stripping slashes
		key = StripSlashes(key);
		fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
		fcpSock.setSoTimeout(TIMEOUT);

		doHandshake(fcpSock);

		fcpOut = new PrintStream(fcpSock.getOutputStream());
		BufferedOutputStream dOut = new BufferedOutputStream(fcpSock.getOutputStream());
		fcpIn = new BufferedInputStream(fcpSock.getInputStream());

		fcpOut.println("ClientPut");
		fcpOut.println("URI=" + key);
		fcpOut.println("Identifier=put-" + fcpConnectionId );
		fcpOut.println("Verbosity=1");
		fcpOut.println("MaxRetries=3");
		fcpOut.println("DontCompress=false"); // force compression
		if(getchkonly){
			fcpOut.println("GetCHKOnly=true");
		} else {
            if( type == FcpHandler.TYPE_FILE ) {
            	// TODO: 3? 4? maybee an user option?
            	// or done by the balancer (atomagically)
                fcpOut.println("PriorityClass=3");  
            } else if( type == FcpHandler.TYPE_MESSAGE ) {
                fcpOut.println("PriorityClass=2");
            }
        }
		
		if (dda) {  // direct file acess
			fcpOut.println("Metadata.ContentType=" + DefaultMIMETypes.guessMIMEType(sourceFile.getAbsolutePath()));
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

		int c;
		StringBuffer output = new StringBuffer();
		// nio doesn't always close the connection.  workaround:
		while ((c = fcpIn.read()) != -1) {
			output.append((char) c);
			if (output.toString().indexOf("EndMessage") != -1) {
				output.append('\0');
				if (output.indexOf("Pending") != -1 || 
                    output.indexOf("Restarted") != -1 || 
                    output.indexOf("SimpleProgress") != -1 || 
                    output.indexOf("URIGenerated") != -1) 
                {
					//System.out.println("Progress: " + output.toString());
					output = new StringBuffer();
					continue;
				}
				break;
			}
		}
		//System.out.println("fin: " + output.toString());
		dOut.close();
		fcpOut.close();
		fcpIn.close();
		fcpSock.close();
        
        fileInput.close();

		return output.toString();
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
//        System.out.println("ClientHello");
        fcpOut.println("Name=hello-"+ fcpConnectionId);
        logger.fine("Name=hello-"+ fcpConnectionId);
//        System.out.println("Name=hello-"+ fcpConnectionId);
        fcpOut.println("ExpectedVersion=2.0");
        logger.fine("ExpectedVersion=2.0");
//        System.out.println("ExpectedVersion=2.0");
        fcpOut.println("End");
    	logger.fine("End");
//    	System.out.println("End");

        FcpKeyword response;
        int timeout = 0;
        do {
            response = FcpKeyword.getFcpKeyword(fcpIn);
    		logger.fine(response.getFullString());
//    		System.out.println(response.getFullString());
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

   	   	String uri = "";
    	String output = putKeyFromFile(FcpHandler.TYPE_FILE, "CHK@", file, true);
//    	System.out.println("GOT OUTPUT " + output + "\n STARTING CHK GENERATION");
    	int URIstart = output.indexOf("CHK@");
    	String substr = output.substring(URIstart);
//    	System.out.println("Substring is " + substr);
    	int URIend = substr.indexOf('\n');
//    	System.out.println(URIend);

    	uri = substr.substring(0, URIend);

//    	System.out.println("URI is " + uri);
    	return uri;
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
//        System.out.println("GenerateSSK");
        fcpOut.println("End");
//        System.out.println("End");

        String output = "";
        FcpKeyword response;
        int timeout = 0;
        do {
            response = FcpKeyword.getFcpKeyword(fcpIn);
    		logger.fine(response.getFullString());
//    		System.out.println(response.getFullString());

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

//        System.out.println(result);
        return result;
    }

    //replaces all / with | in url
    private String StripSlashes(String uri){
        //replacing all / with |
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
}
