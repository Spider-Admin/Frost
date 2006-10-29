/*
  FCP2NodeToClientMessageFactory.java / Frost
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

import hyperocha.freenet.fcp.*;
import hyperocha.freenet.fcp.messages.*;

import java.util.*;

public class FCP2NodeToClientMessageFactory {

    private static final String MSGNAME_NodeHello = "NodeHello";
    private static final String MSGNAME_CloseConnectionDuplicateClientName = "CloseConnectionDuplicateClientName";
    private static final String MSGNAME_SSKKeypair = "SSKKeypair";

    public static FCP2NodeToClientMessage createMessage(String connId, Hashtable message) throws MessageEvaluationException {
        
        FCP2NodeToClientMessage retVal = null;

        String msgName = FCPConnection.MESSAGENAME;
        if( msgName == null ) {
            throw new MessageEvaluationException("Empty message name");
        }
        
        if( msgName.equals(MSGNAME_NodeHello) ) {
            retVal = new FCP2NodeHelloMessage(connId, msgName, message);
        } else if( msgName.equals(MSGNAME_CloseConnectionDuplicateClientName) ) {
            retVal = new FCP2CloseConnectionDuplicateClientNameMessage(connId, msgName, message);
        } else if( msgName.equals(MSGNAME_SSKKeypair) ) {
            retVal = new FCP2SSKKeypairMessage(connId, msgName, message);
        } else {
            throw new MessageEvaluationException("Unknown message name: "+msgName);
        }
        
        return retVal;
    }
}
