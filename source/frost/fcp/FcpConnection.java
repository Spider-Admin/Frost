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
package frost.fcp;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.logging.Logger;

import freenet.support.Bucket;
import frost.FileAccess;

/**
 * This class is a wrapper to simplify access to the FCP library.
 * @author <a href=mailto:landtuna@hotmail.com>Jim Hunziker</a>
 */
public class FcpConnection
{
	public final static int NORMAL=0;
	public final static int DOWNLOADING=1;
	public final static int PING=2; //ok, I'm not really sure what this does .. yet ;)
	// ... add more types
	
	public static int DEFAULT = NORMAL; //not final so that we can change
	
	private static Logger logger = Logger.getLogger(FcpConnection.class.getName());
	
    final static int TIMEOUT = 900000;

    private InetAddress host;
    private int port;
    private int defaultHtl;
    private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;
    private byte[] header = {0,0,0,2};

    /**
     * Create a default connection to localhost using FCP
     *
     * @exception UnknownHostException if the localhost cannot be
     * determined.
     * @exception IOException if there is a problem with the connection
     * to the FCP host.
     */
    public FcpConnection() throws UnknownHostException, IOException, FcpToolsException
    {
    this("127.0.0.1", 8481);
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
    public FcpConnection(String host, int port)
    throws UnknownHostException, IOException, FcpToolsException
    {
    this(host, port, 40);
    }

    public FcpConnection(String host, String port)
    throws UnknownHostException, IOException, FcpToolsException
    {
    this(host, Integer.parseInt(port), 40);
    }

    /**
     * Create a connection to a host using FCP
     *
     * @param host the host to which we connect
     * @param port the FCP port on the host
     * @param defaultHtl the HTL to use if none is specified
     * @exception UnknownHostException if the FCP host is unknown
     * @exception IOException if there is a problem with the connection
     * to the FCP host.
     */
    public FcpConnection(String host, int port, int defaultHtl)
    throws UnknownHostException, IOException, FcpToolsException
    {
    this.defaultHtl = defaultHtl;
    this.host = InetAddress.getByName(host);
    this.port = port;

    doHandshake();
    }


    public String[] getInfo() throws IOException, FcpToolsException{
        Vector result = new Vector();


        fcpSock = new Socket(host, port);
    fcpSock.setSoTimeout(TIMEOUT);
    fcpOut = new PrintStream(fcpSock.getOutputStream());
    BufferedReader in = new BufferedReader(new InputStreamReader(fcpSock.getInputStream()));
    //fcpIn = new BufferedInputStream(fcpSock.getInputStream());
    //BufferedReader in = new BufferedReader(fcpIn);
    fcpOut.write(header, 0, header.length);
    fcpOut.println("ClientInfo");fcpOut.println("EndMessage");
    String tmp;
    do {
        tmp = in.readLine();
        if (tmp.compareTo("EndMessage")==0) break;
        result.add(tmp);
    }
    while(tmp.compareTo("EndMessage") != 0);

    fcpOut.close();
    fcpSock.close();
    in.close();
    String [] ret = new String[result.size()];

    if (result.isEmpty()) return null;
    else for (int i=0;i<result.size();i++)
        ret[i] = (String) result.elementAt(i);
    return ret;

    }

    /**
     * This method is used by the splitfile downloader to retrieve a key into a Bucket.
     * 
     * @param keyString
     * @param bucket
     * @param htl
     * @return
     * @throws IOException
     * @throws FcpToolsException
     * @throws InterruptedIOException
     */    
    public boolean getKeyToBucket(String keyString,
                                  Bucket bucket,
                                  int htl) throws IOException, FcpToolsException, InterruptedIOException {

        keyString = FcpDisconnect(keyString);

        FreenetKey key = new FreenetKey(keyString);
		logger.fine("KeyString = " + keyString + "\n" +
					"Key =       " + key + "\n" +
					"KeyType =   " + key.getKeyType() + "\n" +
					"HTL =       " + htl);

        OutputStream fileOut = bucket.getOutputStream();

        fcpSock = new Socket(host, port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());

        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientGet");
        fcpOut.println("URI=" + key);
        fcpOut.println("HopsToLive=" + htl);
        fcpOut.println("EndMessage");

        FcpKeyword kw;
        boolean receivedFinalByte = false;
        long totalDataLength = 0;
        long metadataLength = 0;
        int dataChunkLength = 0;
        boolean expectingData = false;
        boolean flagRestarted = false;
        boolean errorOccured = false;

        while( receivedFinalByte == false && errorOccured == false )
        {
            if( expectingData == false )
            {
                kw = FcpKeyword.getFcpKeyword(fcpIn);
                // frost.Core.getOut().println("getKeyToBucket-FcpKeyword: " + kw + " for key " + keyString);

                switch( kw.getId() )
                {
                case FcpKeyword.DataFound:
                    if( flagRestarted == true )
                    {
                        fileOut.close();
                        bucket.resetWrite();
                        fileOut = bucket.getOutputStream();

                        totalDataLength = 0;
                        metadataLength = 0;
                        dataChunkLength = 0;

                        flagRestarted = false;
                    }
                    break;
                case FcpKeyword.DataLength:
                    totalDataLength = kw.getLongVal();
                    if( totalDataLength != bucket.size() )
                    {
                        throw new IOException("Size of data is bigger than size of provided Bucket");
                    }
                    break;
                case FcpKeyword.FormatError:
                    errorOccured = true;
                    break;
                case FcpKeyword.URIError:
                    errorOccured = true;
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
                case FcpKeyword.DataNotFound:
                    //frost.Core.getOut().println("Data not found - closing streams for " + filename + " ...");
                    fcpIn.close();
                    fcpOut.close();
                    fcpSock.close();
                    fileOut.close();
                    throw new DataNotFoundException();
                case FcpKeyword.RouteNotFound:
                    errorOccured = true;
                    break;
                case FcpKeyword.Failed:
                    errorOccured = true;
                    break;
                case FcpKeyword.UnknownError:
                    errorOccured = true;
                    break;
                case FcpKeyword.MetadataLength:
                    metadataLength = kw.getLongVal();
                    break;
                case FcpKeyword.EndMessage:
                    break;
                case FcpKeyword.DataChunk:
                    break;
                case FcpKeyword.Length:
                    dataChunkLength = (int) kw.getLongVal();
                    break;
                case FcpKeyword.Data:
                    expectingData = true;
                    break;
                case FcpKeyword.Timeout:
                    //receivedFinalByte = true; // NO not for files. 
                    break;
                }
            }
            else // handle data bytes
            {
				logger.fine("Expecting " + dataChunkLength +
                          	  " bytes, " + totalDataLength +
                              " total.");
                byte [] b = new byte[dataChunkLength];
                int bytesRead = 0, offset = 0, count;
                while( bytesRead < dataChunkLength )
                {
                    count = fcpIn.read(b, bytesRead, dataChunkLength - bytesRead);
                    if( count < 0)
                    {
                        break;
                    }
                    else
                    {
                        bytesRead += count;
                    }
                }
                //        if (DEBUG)
                //        {
                //            String s = new String(b);
                //          frost.Core.getOut().print(s);
                //        }
                fileOut.write(b);
                expectingData = false;
                totalDataLength -= bytesRead;
                if( totalDataLength <= 0 )
                {
                    receivedFinalByte = true;
                }
            }
        }
        fcpIn.close();
        fcpOut.close();
        fcpSock.close();
        fileOut.close();
        
        if( metadataLength > 0 )
        {
        	logger.severe("####################################################################" +
            			  "### HARD ERROR: RECEIVED UNEXCEPTED METADATA IN getKeyToBucket() ###" +
            			  "###             RECEIVED DATA IS CORUPTED!!!                     ###" +
            			  "####################################################################");
        }
        
        if( receivedFinalByte )
            return true;
        else
            return false;
    }
    
    /**
     * Retrieves the specified key and saves it to the file
     * specified.
     *
     * @param key  the key to be retrieved
     * @param filename  the filename to which the data should be saved
     * @return the results filled with metadata
     */
/*    public FcpResults getKeyToFile(String key, String filename)
        throws IOException, FcpToolsException
    {
        //key = FcpDisconnect(key);
        return getKeyToFile(key, filename, defaultHtl);
    }
*/
    /**
     * Retrieves the specified key and saves it to the file
     * specified.
     *
     * @param key  the key to be retrieved
     * @param filename  the filename to which the data should be saved
     * @param htl the HTL to use in this request
     * @return the results filled with metadata
     */
    public FcpResults getKeyToFile(String keyString,
                               String filename,
                               int htl) throws IOException, FcpToolsException, InterruptedIOException {
        return getKeyToFile( keyString, filename, htl, false );
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
    public FcpResults getKeyToFile(String keyString,
                                   String filename,
                                   int htl,
                                   boolean fastDownload) throws IOException, FcpToolsException, InterruptedIOException {

        keyString = FcpDisconnect(keyString);

        FcpResults result = new FcpResults();
        FreenetKey key = new FreenetKey(keyString);
		logger.fine("KeyString = " + keyString + "\n" +
					"Key =       " + key + "\n" +
					"KeyType =   " + key.getKeyType() + "\n" +
					"HTL =       " + htl);

        FileOutputStream fileOut = new FileOutputStream(filename);

        fcpSock = new Socket(host, port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());

        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientGet");
        fcpOut.println("URI=" + key);
        fcpOut.println("HopsToLive=" + htl);
        fcpOut.println("EndMessage");

        FcpKeyword kw;
        boolean receivedFinalByte = false;
        long totalDataLength = 0;
        long metadataLength = 0;
        int dataChunkLength = 0;
        boolean expectingData = false;
        boolean flagRestarted = false;

        while( receivedFinalByte == false )
        {
            //frost.Core.getOut().print("*");
            if( expectingData == false )
            {
                kw = FcpKeyword.getFcpKeyword(fcpIn);
				logger.fine("FcpKeyword: " + kw + " for file " + filename);

//                frost.Core.getOut().println("getKey-FcpKeyword: " + kw + " for file " + filename);

                switch( kw.getId() )
                {
                case FcpKeyword.DataFound:
                    if( flagRestarted == true )
                    {
                        fileOut.close();
                        new File(filename).delete();
                        fileOut = new FileOutputStream(filename);

                        totalDataLength = 0;
                        metadataLength = 0;
                        dataChunkLength = 0;

                        flagRestarted = false;
                    }
                    break;
                case FcpKeyword.DataLength:
                    totalDataLength = kw.getLongVal();
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
                case FcpKeyword.DataNotFound:
                    //frost.Core.getOut().println("Data not found - closing streams for " + filename + " ...");
                    fcpIn.close();
                    fcpOut.close();
                    fcpSock.close();
                    fileOut.close();
                    File checkSize = new File(filename);
                    if( checkSize.length() == 0 )
                        checkSize.delete();
                    throw new DataNotFoundException();
                case FcpKeyword.RouteNotFound:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.Failed:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.UnknownError:
                    receivedFinalByte = true;
                    break;
                case FcpKeyword.MetadataLength:
                    metadataLength = kw.getLongVal();
                    break;
                case FcpKeyword.EndMessage:
                    break;
                case FcpKeyword.DataChunk:
                    break;
                case FcpKeyword.Length:
                    dataChunkLength = (int) kw.getLongVal();
                    break;
                case FcpKeyword.Data:
                    expectingData = true;
                    break;
                case FcpKeyword.Timeout:
// it WOULD be actually better for freenet AND the node to do it this way
// would be , but after 25 minutes my 5 boards did not finish to update, 4 days backload    
// thats really too slow ...
// now the fast mode is only used by MessageDownloadThread ...            
                    if( fastDownload )  receivedFinalByte = true;
                    break;
                }
            }
            else // handle data bytes
            {
				logger.fine("Expecting " + dataChunkLength +
                              " bytes, " + totalDataLength +
                              " total.");
                byte [] b = new byte[dataChunkLength];
               /* DataInputStream dis = new DataInputStream(fcpIn);
                try {
                	dis.readFully(b);
                	receivedFinalByte=true;
                }catch(EOFException e){
                	receivedFinalByte = false;
                }
            }    	
                */
                int bytesRead = 0, offset = 0, count;
                while( bytesRead < dataChunkLength )
                {
                    count = fcpIn.read(b, bytesRead, dataChunkLength - bytesRead);
                    if( count < 0)
                    {
                        break;
                    }
                    else
                    {
                        bytesRead += count;
                    }
                }
                //        if (DEBUG)
                //        {
                //            String s = new String(b);
                //          frost.Core.getOut().print(s);
                //        }
                fileOut.write(b);
                expectingData = false;
                totalDataLength -= bytesRead;
                if( totalDataLength <= 0 )
                {
                    receivedFinalByte = true;
                }
            }
        }

        fcpIn.close();
        fcpOut.close();
        fcpSock.close();
        fileOut.flush();
        fileOut.close();
        File checkSize = new File(filename);
        if( metadataLength > 0 && checkSize.length() > 0)
        {
            if( metadataLength == checkSize.length() )
            {
                // all data are metadata ...
                byte[] content = FileAccess.readByteArray(checkSize);
                result.setRawMetadata(content);
                // delete data file which contains no data
                checkSize.delete();
            }
            else
            {
                // remove metadata from file and put metadata into result
                byte[] content = FileAccess.readByteArray(checkSize);
                byte[] metadata = new byte[(int)metadataLength]; 
                System.arraycopy(content, 0, metadata, 0, (int)metadataLength);
                
                result.setRawMetadata(metadata);
                // there is data behind metadata, write only this raw data to file
                int datalen = (int)(checkSize.length() - metadataLength);
                byte[] rawdata = new byte[ datalen];
                System.arraycopy(content, (int)metadataLength, rawdata, 0, datalen);
                FileAccess.writeByteArray(rawdata, checkSize); 
            }
        }
        else if( metadataLength == 0 && checkSize.length() == 0 )
        {
            checkSize.delete();
        }
        
        return result;
    }

	/**
	 * Inserts the specified key with the data from the file
	 * specified.
	 *
	 * @param key  the key to be inserted
	 * @param data  the bytearray with the data to be inserted
	 * @param htl the HTL to use for this insert
	 * @return the results filled with metadata and the CHK used to
	 * insert the data
	 */
	public String putKeyFromArray(String key, byte[] data, byte[] metadata, int htl)
		throws IOException {

		key = FcpDisconnect(key);

		fcpSock = new Socket(host, port);
		fcpSock.setSoTimeout(TIMEOUT);
		fcpOut = new PrintStream(fcpSock.getOutputStream());
		DataOutputStream dOut = new DataOutputStream(fcpSock.getOutputStream());
		fcpIn = new BufferedInputStream(fcpSock.getInputStream());

		fcpOut.write(header, 0, header.length);

		fcpOut.println("ClientPut");
		fcpOut.println("RemoveLocalKey=true");
		fcpOut.println("HopsToLive=" + htl);
		fcpOut.println("URI=" + key);

		int dataLength = 0;
		int metadataLength = 0;
		if (data != null) {
			dataLength = data.length;
		}
		if (metadata != null) {
			metadataLength = metadata.length;
		}

		fcpOut.println("DataLength=" + Integer.toHexString(dataLength + metadataLength));

		if (metadata != null) {
			fcpOut.println("MetadataLength=" + Integer.toHexString(metadataLength));
		}

		fcpOut.println("Data");
		fcpOut.flush();

		if (metadata != null) {
			dOut.write(metadata);
		}

		if (data != null) {
			dOut.write(data);
		}
		dOut.flush();

		int c;
		StringBuffer output = new StringBuffer();
		//nio doesn't always close the connection.  workaround:
		while ((c = fcpIn.read()) != -1) {
			output.append((char) c);
			if (output.toString().indexOf("EndMessage") != -1) {
				output.append('\0');
				if (output.indexOf("Pending") != -1 || output.indexOf("Restarted") != -1) {
					output = new StringBuffer();
					continue;
				}
				break;
			}
		}

		//    fcpSock.close();
		dOut.close();
		fcpOut.close();
		fcpIn.close();
		fcpSock.close();
		return output.toString();
	}

    /**
     * Inserts the specified key with the data from the file
     * specified.
     *
     * @param key  the key to be inserted
     * @param filename  the filename from which the data should be read
     * @param htl  HTL to use for insert
     * @return the results filled with metadata and the CHK used to
     * insert the data
     */
    public String putKeyFromFile(String key,
                 String filename,
                 int htl) throws IOException 
    {
        byte[] data = FileAccess.readByteArray(filename);
        return putKeyFromArray(key, data, null, htl);
    }
    public String putKeyFromFile(String key,
                 String filename,
                 byte[] metadata,
                 int htl) throws IOException 
    {
        byte[] data = FileAccess.readByteArray(filename);
        return putKeyFromArray(key, data, metadata, htl);
    }

    /**
     * Performs a handshake using this FcpConnection
     */
    public void doHandshake() throws IOException, FcpToolsException
    {
    fcpSock = new Socket(host, port);
    fcpIn = new BufferedInputStream(fcpSock.getInputStream());
    fcpOut = new PrintStream(fcpSock.getOutputStream());
    fcpSock.setSoTimeout(TIMEOUT);

    fcpOut.write(header, 0, header.length);
    fcpOut.println("ClientHello");
    logger.fine("ClientHello");
    fcpOut.println("EndMessage");
	logger.fine("EndMessage");

    String s;
    FcpKeyword response;
    int timeout = 0;
    do
    {
        response = FcpKeyword.getFcpKeyword(fcpIn);
		logger.fine(response.getFullString());
        try {
        Thread.sleep(100);
        }
        catch(InterruptedException e) {}
        // frost.Core.getOut().print(">");
        timeout++;
    } while (response.getId() != FcpKeyword.EndMessage && timeout < 32);
        //frost.Core.getOut().print("<");
        if (timeout == 32)
        throw new ConnectionException();
    fcpSock.close();
    }

    /**
     * returns private and public key
     * @return String[] containing privateKey / publicKey
     */
    public String[] getKeyPair() throws IOException {

    fcpSock = new Socket(host, port);
    fcpSock.setSoTimeout(TIMEOUT);
    fcpOut = new PrintStream(fcpSock.getOutputStream());
    fcpIn = new BufferedInputStream(fcpSock.getInputStream());

    fcpOut.write(header, 0, header.length);
    fcpOut.println("GenerateSVKPair");
    fcpOut.println("EndMessage");

    int c;
    StringBuffer output = new StringBuffer();

    while ((c = fcpIn.read()) != -1) {
        output.append((char)c);
    }
	logger.fine(output.toString());
/*
    //nio doesn't always close the connection.  workaround:
    while (true) {
        c=fcpIn.read();
        output.append((char)c);
        if (DEBUG)frost.Core.getOut().print((char)c);
    if (output.toString().indexOf("EndMessage") !=-1) break;
    }
*/
    fcpSock.close();
    fcpOut.close();
    fcpIn.close();
    fcpSock.close();

    String[] result = {"SSK@", "SSK@"};
    String outString = output.toString();
    int privateKeyPos = outString.indexOf("PrivateKey=");
    int publicKeyPos = outString.indexOf("PublicKey=");

    if (privateKeyPos != -1 && publicKeyPos != -1) {
    	privateKeyPos += 11;
    	publicKeyPos += 10;
		int privateKeyEnd = outString.indexOf('\n', privateKeyPos);
		int publicKeyEnd = outString.indexOf('\n', publicKeyPos);
    	
    	if (privateKeyEnd != -1 && publicKeyEnd != -1) {
        	result[0] += (outString.substring(privateKeyPos, privateKeyEnd));
        	result[1] += (outString.substring(publicKeyPos, publicKeyEnd)) + "PAgM";
    	}
    }

    return result;
    }

    private String FcpDisconnect(String port) {

    return port;
    }
}
