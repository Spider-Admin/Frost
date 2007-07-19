/*
  FcpKeyword.java / Frost
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
import java.util.logging.Logger;

/**
 * This class represents a keyword in the FCP protocol.  It parses the
 * line that contains the keyword to allow easy access to the value
 * which the keyword has been assigned.
 */
public final class FcpKeyword
{
    private static final Logger logger = Logger.getLogger(FcpKeyword.class.getName());

    public final static int UnknownError = -1;
    public final static int ClientGet = 0;
    public final static int ClientHello = 1;
    public final static int ClientPut = 2;
    public final static int Data = 3;
    public final static int DataChunk = 4;
    public final static int DataFound = 5;
    public final static int DataLength = 6;
    public final static int DataNotFound =7;
    public final static int EndMessage = 8;
    public final static int FormatError = 9;
    public final static int GenerateCHK = 10;
    public final static int GenerateSVKPair = 11;
    public final static int HopsToLive = 12;
    public final static int KeyCollision = 13;
    public final static int Length = 14;
    public final static int MetadataLength = 15;
    public final static int NodeHello = 16;
    public final static int Node = 17;
    public final static int PrivateKey = 18;
    public final static int Protocol = 19;
    public final static int PublicKey = 20;
    public final static int Reason = 21;
    public final static int Restarted = 22;
    public final static int RouteNotFound = 23;
    public final static int SizeError = 24;
    public final static int Success = 25;
    public final static int URI = 26;
    public final static int URIError = 27;
    public final static int Failed = 28;
    public final static int ClientInfo = 29;
    public final static int Timeout = 30;

    private String fullString;
    private String keyword;
    private long longVal;
    private int kwId;

    /**
     * Returns the keyword/value pair.
     *
     * @return keyword/value pair as a string
     */
    public String toString()
    {
    return fullString;
    }

    /**
     * Constructs a keyword object, given a line of input that the
     * caller has decided is a keyword.
     *
     * @param s string that represents a keyword line
     */
    public FcpKeyword(String s)
    {
    logger.fine("in " + s);
    fullString = s;
    keyword = getFcpKeyword(s);
    logger.fine("kw " + keyword);
    kwId = getFcpId(s);
    try
    {
        if (keyword != null && fullString != null) {
        if (keyword.length() < fullString.length())
            {
            String number = s.substring(keyword.length() + 1);
            logger.fine("Getting longval for *" + number + "*");
            longVal = Long.parseLong(number, 16);
            }
        else
            {
            longVal = -1;
            }
        }
    }
    catch (NumberFormatException e)
    {
        longVal = -1;
    }
    }

    /**
     * Internal function to figure out which constant to use to
     * represent this keyword.
     *
     * @param s line with the keyword
     * @return constant representing the keyword
     */
    private static int getFcpId(String s)
    {
    if (s.startsWith("ClientGet"))
        return ClientGet;
    else if (s.startsWith("ClientHello"))
        return ClientHello;
    else if (s.startsWith("ClientPut"))
        return ClientPut;
    else if (s.startsWith("DataChunk"))
        return DataChunk;
    else if (s.startsWith("DataFound"))
        return DataFound;
    else if (s.startsWith("DataLength"))
        return DataLength;
    else if (s.startsWith("DataNotFound"))
        return DataNotFound;
    else if (s.startsWith("Data"))
        return Data;
    else if (s.startsWith("EndMessage"))
        return EndMessage;
    else if (s.startsWith("FormatError"))
        return FormatError;
    else if (s.startsWith("GenerateCHK"))
        return GenerateCHK;
    else if (s.startsWith("GenerateSVKPair"))
        return GenerateSVKPair;
    else if (s.startsWith("HopsToLive"))
        return HopsToLive;
    else if (s.startsWith("KeyCollision"))
        return KeyCollision;
    else if (s.startsWith("Length"))
        return Length;
    else if (s.startsWith("MetadataLength"))
        return MetadataLength;
    else if (s.startsWith("NodeHello"))
        return NodeHello;
    else if (s.startsWith("Node"))
        return Node;
    else if (s.startsWith("PrivateKey"))
        return PrivateKey;
    else if (s.startsWith("Protocol"))
        return Protocol;
    else if (s.startsWith("PublicKey"))
        return PublicKey;
    else if (s.startsWith("Reason"))
        return Reason;
    else if (s.startsWith("Restarted"))
        return Restarted;
    else if (s.startsWith("RouteNotFound"))
        return RouteNotFound;
    else if (s.startsWith("SizeError"))
        return SizeError;
    else if (s.startsWith("Success"))
        return Success;
    else if (s.startsWith("URIError"))
        return URIError;
    else if (s.startsWith("URI"))
        return URI;
    else if (s.startsWith("Failed"))
        return Failed;
    else if (s.startsWith("ClientInfo"))
        return ClientInfo;
    else if (s.startsWith("Timeout"))
        return Timeout;
    else
        return -1;
    }

    /**
     * Internal function to figure out only the keyword part of a string
     * that was passed.
     *
     * @param s line with the keyword
     * @return keyword
     */
    private static String getFcpKeyword(String s)
    {
    if (s.startsWith("Timeout"))
        return "Timeout";
    else if (s.startsWith("ClientGet"))
        return "ClientGet";
    else if (s.startsWith("ClientHello"))
        return "ClientHello";
    else if (s.startsWith("ClientPut"))
        return "ClientPut";
    else if (s.startsWith("DataChunk"))
        return "DataChunk";
    else if (s.startsWith("DataFound"))
        return "DataFound";
    else if (s.startsWith("DataLength"))
        return "DataLength";
    else if (s.startsWith("DataNotFound"))
        return "DataNotFound";
    else if (s.startsWith("Data"))
        return "Data";
    else if (s.startsWith("EndMessage"))
        return "EndMessage";
    else if (s.startsWith("FormatError"))
        return "FormatError";
    else if (s.startsWith("GenerateCHK"))
        return "GenerateCHK";
    else if (s.startsWith("GenerateSVKPair"))
        return "GenerateSVKPair";
    else if (s.startsWith("HopsToLive"))
        return "HopsToLive";
    else if (s.startsWith("KeyCollision"))
        return "KeyCollision";
    else if (s.startsWith("Length"))
        return "Length";
    else if (s.startsWith("MetadataLength"))
        return "MetadataLength";
    else if (s.startsWith("NodeHello"))
        return "NodeHello";
    else if (s.startsWith("Node"))
        return "Node";
    else if (s.startsWith("PrivateKey"))
        return "PrivateKey";
    else if (s.startsWith("Protocol"))
        return "Protocol";
    else if (s.startsWith("PublicKey"))
        return "PublicKey";
    else if (s.startsWith("Reason"))
        return "Reason";
    else if (s.startsWith("Restarted"))
        return "Restarted";
    else if (s.startsWith("RouteNotFound"))
        return "RouteNotFound";
    else if (s.startsWith("SizeError"))
        return "SizeError";
    else if (s.startsWith("Success"))
        return "Success";
    else if (s.startsWith("URIError"))
        return "URIError";
    else if (s.startsWith("URI"))
        return "URI";
    else if (s.startsWith("Failed"))
        return "Failed";
    else
        return null;
    }

    /**
     * Return a new keyword from the input stream.  This should only be
     * called when the input stream is expected to start with a keyword.
     *
     * @param in InputStream from which to read
     * @return new FcpKeyword
     */
    public static FcpKeyword getFcpKeyword(InputStream in) throws IOException {
        int b;
        byte[] bytes = new byte[256];
        int count = 0;
        while ((b = in.read()) != '\n' && b != -1 && count < 256 && (b != '\0')) {
            bytes[count] = (byte) b;
            count++;
        }
        return new FcpKeyword(new String(bytes, 0, count));
    }

    /**
     * Return the value associated with this keyword as a long.
     *
     * @return long value
     */
    public long getLongVal()
    {
    return longVal;
    }

    /**
     * Return the constant ID associated with this keyword.
     *
     * @return ID
     */
    public int getId()
    {
    return kwId;
    }

    /**
     * Returns the keyword/value pair.
     *
     * @return keyword/value pair as a string
     */
    public String getFullString()
    {
    return fullString;
    }

    /**
     * Returns only the keyword.
     *
     * @return keyword
     */
    public String getStringVal()
    {
    return fullString.substring(keyword.length());
    }

}
