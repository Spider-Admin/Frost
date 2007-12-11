/*
  FcpFECUtils.java / Frost
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
import java.util.logging.*;

/**
 * FEC routines for Frost. However quite a lot of code currently spread
 * over other modules needs to be moved into that file.
 */
public class FcpFECUtils {
    static String description = "FEC file inserted by FROST";

    private static final Logger logger = Logger.getLogger(FcpFECUtils.class.getName());

    private InetAddress host;
    private int port;
    private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;
//    private byte[] header = {0,0,0,2};

/* Not used today
    public static void main(String[] args) throws UnknownHostException, IOException, SocketException {
    FcpFECUtils me;
    File transmitFile;
        frost.Core.getOut().println(description);
    transmitFile = new File(args[0]);
    me = new FcpFECUtils(host, port);
//  me.FECInsertFileKey(transmitFile);
    }
    String FECInsertFileKey(File insertfile) throws UnknownHostException, IOException, SocketException{
    Vector segmentHeaders;
    Vector segmentMaps;
    Vector checkMaps;
    long fileLength;
    fileLength = insertfile.length();
    FileOutputStream chkfile;
    segmentHeaders = FECSegmentFile("OnionFEC_a_1_2", fileLength);
    int i;
    for (i = 0; i < segmentHeaders.size(); i++){
        // Send data to FEC encoder
        fcpSock = new Socket(InetAddress.getByName("127.0.0.1"), 8481);
        fcpSock.setSoTimeout(500000);
        fcpOut = new PrintStream(fcpSock.getOutputStream());
        fcpIn = new BufferedInputStream(fcpSock.getInputStream());
        {
        frost.Core.getOut().println("Processing segment " + i);
        FileInputStream in = new FileInputStream(insertfile);
        in.skip(((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).Offset);
        long segLength = ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockCount * ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockSize;
        frost.Core.getOut().println("segLength = " + Long.toHexString(segLength));
        String headerString = "SegmentHeader\n" + ((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).reconstruct() + "EndMessage\n";
        String dataHeaderString = "\0\0\0\2FECEncodeSegment\nMetadataLength=" + Long.toHexString(headerString.length()) + "\nDataLength=" + Long.toHexString(headerString.length() + segLength) + "\nData\n" + headerString;
        frost.Core.getOut().print(dataHeaderString);
        fcpOut.print(dataHeaderString);
        long count = 0;
        while (count < segLength){
            byte[] buffer = new byte[(int)((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).BlockSize];
            frost.Core.getOut().println(Long.toHexString(((FcpFECUtilsSegmentHeader)segmentHeaders.get(i)).Offset+count));
            long inbytes = in.read(buffer);
            if (inbytes < 0){
            frost.Core.getOut().println("End of input file");
            break;
            }
            if (inbytes > segLength - count)
            inbytes = segLength - count;
            fcpOut.write(buffer);
            count += inbytes;
            }
        fcpOut.flush();
        in.close();
        }
        // Fetch encoded blocks
        {
        String currentLine;
        int blockNum = 0;
        int length = 0;
        long feccount = 0;
        chkfile = new FileOutputStream(insertfile.getName() + ".chk");
        do {
            boolean started = false;
            currentLine = getLine(fcpIn).trim();
//          frost.Core.getOut().println(currentLine);
            if (currentLine.equals("DataChunk")){
            started = true;
//          continue;
            }
            if (currentLine.startsWith("Length=")){
            length = Integer.parseInt((currentLine.split("="))[1],16);
//          continue;
            }
            if (currentLine.equals("Data")){
            int currentRead;
            byte[] buffer = new byte[(int)length];
            currentRead = fcpIn.read(buffer);
            while (currentRead < length){
//              frost.Core.getOut().println("Not enough data, read: " + Integer.toHexString(currentRead) + "h bytes");
                currentRead += fcpIn.read(buffer,currentRead,length - currentRead);
//              break;
            }
            chkfile.write(buffer);
            feccount += currentRead;
//          frost.Core.getOut().println("Fetched " + Long.toHexString(feccount) + "h bytes");
            }
        } while (currentLine.length() > 0);
        }

        chkfile.close();

        fcpOut.close();
        fcpIn.close();
        fcpSock.close();

    }
    // This is not working
    segmentMaps = new Vector();
    checkMaps = new Vector();
    return FECMakeMetadata(segmentHeaders, segmentMaps, checkMaps, "Frost");
    }
*/

    public String FECMakeMetadata(Vector headers, Vector chunkMaps, Vector checkMaps, String mimeType) throws UnknownHostException, IOException, SocketException{
    String listString = "";
    fcpSock = new Socket(host, port);
    fcpSock.setSoTimeout(10000);
    fcpOut = new PrintStream(fcpSock.getOutputStream());
    fcpIn = new BufferedInputStream(fcpSock.getInputStream());

    int i;
    for (i=0;i<headers.size();i++){
        FcpFECUtilsSegmentHeader header = (FcpFECUtilsSegmentHeader)(headers.get(i));
        listString += "SegmentHeader\n";
        listString += header.reconstruct();
        listString += "EndMessage\n";
        listString += "BlockMap\n";
        String[][] chunkMap = (String[][])(chunkMaps.get(i));
        for (int j = 0; j < chunkMap.length; j++){
            logger.fine("chunkMap: " + j + chunkMap[j][0] + " " + chunkMap[j][1]);
            listString += "Block." + Integer.toHexString(j) + "=" + chunkMap[j][1] + "\n";
        }
        String[][] checkMap = (String[][])(checkMaps.get(i));
        for (int j = 0; j < checkMap.length; j++){
            logger.fine("checkMap: " + j + checkMap[j][0] + " " + checkMap[j][1]);
            listString += "Check." + Integer.toHexString(j) + "=" + checkMap[j][1] + "\n";
        }
        listString += "EndMessage\n";
    }

//    frost.Core.getOut().println(listString);
//  fcpOut.write(header, 0, header.length);
    fcpOut.println("\0\0\0\2FECMakeMetadata");
    fcpOut.println("Segments=" + Integer.toHexString(headers.size()));
    fcpOut.println("Description=" + description);
    fcpOut.println("MimeType=Frost/FEC");
    fcpOut.println("DataLength=" + Integer.toHexString(listString.length()));
    fcpOut.println("Data");
    fcpOut.print(listString);
    String currentLine;
    int length = 0;
    int dataLength = 0;
    int currentRead = 0;
    byte[] buffer = null;
    do {
        currentLine = getLine(fcpIn).trim();
        logger.fine(currentLine);
        if (currentLine.startsWith("DataLength=")){
        dataLength = Integer.parseInt((currentLine.split("="))[1],16);
//frost.Core.getOut().println("FECMetadata has length " + length);
        buffer = new byte[dataLength];
        continue;
        }
        if (currentLine.equals("DataChunk")){
        continue;
        }
        if (currentLine.startsWith("Length=")){
        length = Integer.parseInt((currentLine.split("="))[1],16);
        continue;
        }
        if (currentLine.equals("Data")){
        while (length > 0){
            int thisread;
            thisread = fcpIn.read(buffer,currentRead,length);
            length -= thisread;
            currentRead += thisread;
        }
        }
    } while (currentLine.length() > 0);
    return new String(buffer);
    }

    public Vector FECSegmentFile(String algorithm, long length) throws UnknownHostException, IOException, SocketException{
    Vector outData;
    String currentLine;
    FcpFECUtilsSegmentHeader currentHeader = null;
    String request = "\0\0\0\2FECSegmentFile\nAlgoName=" + algorithm +
                     "\nFileLength=" + Long.toHexString(length) +
                     "\nEndMessage\n";

    fcpSock = new Socket(host, port);
    fcpSock.setSoTimeout(10000);
    fcpOut = new PrintStream(fcpSock.getOutputStream());
    fcpIn = new BufferedInputStream(fcpSock.getInputStream());

    // frost.Core.getOut().println(request);
    fcpOut.print(request);

    outData = new Vector();
    do {
        currentLine = getLine(fcpIn).trim();
//        frost.Core.getOut().println(currentLine);
        if (currentLine.equals("SegmentHeader")){
        currentHeader = new FcpFECUtilsSegmentHeader();
        continue;
        }
        if (currentLine.equals("EndMessage")){
        outData.add(currentHeader);
        currentHeader = null;
        }
        if (currentHeader != null){
        currentHeader.insertValue(currentLine);
        }
    } while (currentLine.length() > 0);
    fcpOut.close();
    fcpIn.close();
    fcpSock.close();
    return outData;
    }

    public String getLine(InputStream in) throws IOException
    {
        int b;
        byte [] bytes = new byte[64];
        int count = 0;
        while (((b = in.read()) != '\n') && (b != -1) && (b != '\0') && (count < 64))
        {
            bytes[count] = (byte) b;
            count++;
        }
        return (new String(bytes)).trim();
    }

    public FcpFECUtils(String node, int port)
    {
        try{
        this.host = InetAddress.getByName(node);
    }
    catch (UnknownHostException e){
        try{
        this.host = InetAddress.getByAddress(new byte[] {127,0,0,1});
        }
        catch (UnknownHostException e2){};  // This should not happen
    }
    this.port = port;
    logger.info("Generated FcpFECUtils host " + host.getHostAddress());
    }
}

