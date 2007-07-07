/*
  FcpConnection.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp05;

import java.io.*;
import java.net.*;
import java.util.*;

import freenet.support.*;
import frost.fcp.*;
import frost.util.*;

/**
 * This class is a wrapper to simplify access to the FCP library.
 * @author <a href=mailto:landtuna@hotmail.com>Jim Hunziker</a>
 */
public class FcpConnection
{
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FcpConnection.class.getName());

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

    private final static byte[] header = {0,0,0,2};

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

        doHandshake();
    }
    
    public void abortConnection() {
        if( fcpSock != null ) {
            try {
                fcpSock.close();
            } catch (IOException e) {
            }
        }
    }

    public List<String> getNodeInfo() throws IOException {
        ArrayList<String> result = new ArrayList<String>();

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(fcpSock.getInputStream()));
        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientInfo");
        fcpOut.println("EndMessage");
        String tmp;
        do {
            tmp = in.readLine();
            if (tmp.compareTo("EndMessage") == 0) {
                break;
            }
            result.add(tmp);
        } while(tmp.compareTo("EndMessage") != 0);

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

        FreenetKey key = new FreenetKey(keyString);
        logger.fine("KeyString = " + keyString + "\n" +
                    "Key =       " + key + "\n" +
                    "KeyType =   " + key.getKeyType() + "\n" +
                    "HTL =       " + htl);

        OutputStream fileOut = bucket.getOutputStream();

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());

        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientGet");
        fcpOut.println("URI=" + key);
        fcpOut.println("HopsToLive=" + Integer.toHexString(htl));
        fcpOut.println("EndMessage");

        FcpKeyword kw;
        boolean receivedFinalByte = false;
        long totalDataLength = 0;
        long metadataLength = 0;
        int dataChunkLength = 0;
        boolean expectingData = false;
        boolean flagRestarted = false;
        boolean errorOccured = false;
        int integrityBufferSize = 0;

        while( receivedFinalByte == false && errorOccured == false )
        {
            if( expectingData == false )
            {
                kw = FcpKeyword.getFcpKeyword(fcpIn);

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
                    integrityBufferSize = (int) totalDataLength;
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
            } else { // handle data bytes
                logger.fine("Expecting " + dataChunkLength +
                              " bytes, " + totalDataLength +
                              " total.");
                byte [] b = new byte[dataChunkLength];
                int bytesRead = 0, count;
                while( bytesRead < dataChunkLength ) {
                    count = fcpIn.read(b, bytesRead, dataChunkLength - bytesRead);
                    if( count < 0 ) {
                        break;
                    } else {
                        bytesRead += count;
                    }
                }
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
        fileOut.close();

        if(receivedFinalByte) {
            if(metadataLength > 0) {
                logger.severe("Unexpected metadata received in getKeyToBucket().");
                return false;
            }
            boolean valid = checkIntegrity(bucket, integrityBufferSize, keyString);
            if (!valid) {
                logger.warning("Invalid bucket received in getKeyToBucket(). Retrying.");
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method checks if the data in the bucket is valid. It does so by comparing the CHK key
     * that was used to request the data with the CHK key generated from the data that has actually
     * been obtained.
     * @param bucket the bucket that contains the data to be checked.
     * @param bucketSize the length of the bucket.
     * @param chkKey the original CHK key
     * @return true if the data is valid. False otherwise.
     * @throws IOException if there was an error while checking the integrity.
     */
    private boolean checkIntegrity(Bucket bucket, int bucketSize, String chkKey) throws IOException {
        InputStream fileIn = bucket.getInputStream();
        byte[] dataBuffer = new byte[bucketSize];
        int length, offset = 0;
        while ((length = fileIn.read(dataBuffer, offset, 256 * 1024)) > -1) {
            offset += length;
        }
        String generatedCHK = FecTools.generateCHK(dataBuffer);
        if (chkKey.equalsIgnoreCase(generatedCHK)) {
            return true;
        } else {
            return false;
        }
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
    public FcpResultGet getKeyToFile(String keyString,
                                   String filename,
                                   int htl,
                                   boolean fastDownload) throws IOException, FcpToolsException, InterruptedIOException {

        FreenetKey key = new FreenetKey(keyString);
        logger.fine("KeyString = " + keyString + "\n" +
                    "Key =       " + key + "\n" +
                    "KeyType =   " + key.getKeyType() + "\n" +
                    "HTL =       " + htl);

        FileOutputStream fileOut = new FileOutputStream(filename);

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());

        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientGet");
        fcpOut.println("URI=" + key);
        fcpOut.println("HopsToLive=" + Integer.toHexString(htl));
        fcpOut.println("EndMessage");

        FcpKeyword kw;
        boolean receivedFinalByte = false;
        long totalDataLength = 0;
        long metadataLength = 0;
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

                switch( kw.getId() )
                {
                case FcpKeyword.DataFound:
                    if( flagRestarted == true )
                    {
                        fileOut.close();
                        new File(filename).delete();
                        fileOut = new FileOutputStream(filename);

                        totalDataLength = 0;
                        expectedTotalDataLength = 0;
                        metadataLength = 0;
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
   At any time when the full payload of data has not been sent 
   (even before the DataFound message), a Restarted message may be sent. 
   This means that the data failed to verify and the transfer will be restarted. 
   The client should disregard all data it has recieved since it made its initial 
   request and return to waiting for a DataFound message to begin the data transfer again. 
   Otherwise, when the final DataChunk is received, the transaction is complete and 
   the connection dies.
   
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
            } else { // handle data bytes
                logger.fine("Expecting " + dataChunkLength + " bytes, " + totalDataLength + " total.");
                byte[] b = new byte[dataChunkLength];
                int bytesRead = 0, count;
                while( bytesRead < dataChunkLength ) {
                    count = fcpIn.read(b, bytesRead, dataChunkLength - bytesRead);
                    if( count < 0 ) {
                        break;
                    } else {
                        bytesRead += count;
                    }
                }
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
        fileOut.flush();
        fileOut.close();
        File checkSize = new File(filename);

        if( Logging.inst().doLogFcp2Messages() ) {
            System.out.print(
                    "expectedTotalDataLength="+expectedTotalDataLength+
                    "; filesize="+checkSize.length()+
                    "; mdlen="+metadataLength);
        }

        FcpResultGet result = null;

        if( metadataLength > 0 && checkSize.length() > 0 ) {
            // success, with metadata
            result = new FcpResultGet(true);

            if( metadataLength == checkSize.length() ) {
                // all data are metadata ...
                byte[] content = FileAccess.readByteArray(checkSize);
                result.setRawMetadata(content);
                // create empty data file
                checkSize.delete();
                FileAccess.writeFile(new byte[0], checkSize);
            } else {
                // remove metadata from file and put metadata into result
                byte[] content = FileAccess.readByteArray(checkSize);
                byte[] metadata = new byte[(int) metadataLength];
                System.arraycopy(content, 0, metadata, 0, (int) metadataLength);

                result.setRawMetadata(metadata);
                // there is data behind metadata, write only this raw data to file
                int datalen = (int) (checkSize.length() - metadataLength);
                byte[] rawdata = new byte[datalen];
                System.arraycopy(content, (int) metadataLength, rawdata, 0, datalen);
                FileAccess.writeFile(rawdata, checkSize);
            }
            
            if( Logging.inst().doLogFcp2Messages() ) {
                System.out.println("; finalFileSize="+checkSize.length());
            }
            
        } else if( metadataLength == 0 && checkSize.length() == 0 ) {
            // failure
            result = new FcpResultGet(false);
            checkSize.delete();
            
            if( Logging.inst().doLogFcp2Messages() ) {
                System.out.println("; deleted!");
            }
        } else {
            // success, no metadata
            result = new FcpResultGet(true);
            
            if( Logging.inst().doLogFcp2Messages() ) {
                System.out.println("; finalFileSize="+checkSize.length());
            }
        }
        
        return result;
    }

    /**
     * Inserts the specified key with the data from the file specified.
     *
     * @param key
     *            the key to be inserted
     * @param data
     *            the bytearray with the data to be inserted
     * @param htl
     *            the HTL to use for this insert
     * @return the results filled with metadata and the CHK used to insert the data
     */
    public String putKeyFromArray(String key, byte[] data, byte[] metadata, int htl, boolean removeLocalKey)
        throws IOException {

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        DataOutputStream dOut = new DataOutputStream(fcpSock.getOutputStream());
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());

        fcpOut.write(header, 0, header.length);

        fcpOut.println("ClientPut");
        if( removeLocalKey ) {
            fcpOut.println("RemoveLocalKey=true");
        }
        fcpOut.println("HopsToLive=" + Integer.toHexString(htl));
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
        StringBuilder output = new StringBuilder();
        // nio doesn't always close the connection.  workaround:
        while ((c = fcpIn.read()) != -1) {
            output.append((char) c);
            if (output.toString().indexOf("EndMessage") != -1) {
                output.append('\0');
                if (output.indexOf("Pending") != -1 || output.indexOf("Restarted") != -1) {
                    output = new StringBuilder();
                    continue;
                }
                break;
            }
        }

        dOut.close();
        fcpOut.close();
        fcpIn.close();
        fcpSock.close();

        return output.toString();
    }

    /**
     * Performs a handshake using this FcpConnection
     */
    public void doHandshake() throws IOException, ConnectException
    {
        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        fcpSock.setSoTimeout(TIMEOUT);

        fcpOut.write(header, 0, header.length);
        fcpOut.println("ClientHello");
        logger.fine("ClientHello");
        fcpOut.println("EndMessage");
        logger.fine("EndMessage");

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
        fcpSock.close();
    }

    /**
     * returns private and public key
     * @return String[] containing privateKey / publicKey
     */
    public String[] getKeyPair() throws IOException {

        fcpSock = new Socket(nodeAddress.host, nodeAddress.port);
        fcpSock.setSoTimeout(TIMEOUT);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());

        fcpOut.write(header, 0, header.length);
        fcpOut.println("GenerateSVKPair");
        fcpOut.println("EndMessage");

        int c;
        StringBuilder output = new StringBuilder();

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
}
