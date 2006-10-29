/*
  FCPNodeHelloMessage.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package hyperocha.freenet.fcp.messages.node2client;

import hyperocha.freenet.fcp.messages.*;

import java.util.*;

public class FCPNodeHelloMessage extends FCPNodeToClientMessage {
    
    private boolean isTestnet;
    private String fcpVersion;
    private String nodeVersion;
    private String nodeName;
    private int compressionCodecs;
    
    public FCPNodeHelloMessage(String connId, Hashtable message) throws MessageEvaluationException {
        super(connId);
        evaluate(message);
    }
    
    protected void evaluate(Hashtable ht) throws MessageEvaluationException {
//        NodeHello
//        FCPVersion=2.0
//        Version=Fred,0.7,1.0,399
//        Node=Fred
//        Testnet=true
//        CompressionCodecs=1
//        EndMessage
        
        fcpVersion = stringToString((String)ht.get("FCPVersion"), false);
        nodeVersion = stringToString((String)ht.get("Version"), false);
        nodeName = stringToString((String)ht.get("Node"), false);
        isTestnet = stringToBool((String)ht.get("Testnet"), true);
        compressionCodecs = stringToInt((String)ht.get("CompressionCodecs"), true);
    }

    public int getCompressionCodecs() {
        return compressionCodecs;
    }

    public String getFcpVersion() {
        return fcpVersion;
    }

    public boolean isTestnet() {
        return isTestnet;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeVersion() {
        return nodeVersion;
    }
}
