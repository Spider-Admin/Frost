/*
  FCP2SSKKeypairMessage.java / Frost
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

public class FCP2SSKKeypairMessage extends FCP2NodeToClientMessage {
    
    private String insertURI;
    private String requestURI;
    
    public FCP2SSKKeypairMessage(String connId, String msgName, Hashtable message) throws MessageEvaluationException {
        super(connId, msgName);
        evaluate(message);
    }
    
    protected void evaluate(Hashtable ht) throws MessageEvaluationException {
//      SSKKeypair
//      InsertURI=freenet:SSK@AKTTKG6YwjrHzWo67laRcoPqibyiTdyYufjVg54fBlWr,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM/
//      RequestURI=freenet:SSK@BnHXXv3Fa43w~~iz1tNUd~cj4OpUuDjVouOWZ5XlpX0,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM,AQABAAE/
//      Identifier=My Identifier from GenerateSSK
//      EndMessage
      
        final String fnPrefix = "freenet:";
        insertURI = stringToString((String) ht.get("InsertURI"), false);
        if( insertURI.startsWith(fnPrefix) ) {
            insertURI  = insertURI.substring(fnPrefix.length(), insertURI.length());
        }
        requestURI = stringToString((String) ht.get("RequestURI"), false);
        if( requestURI.startsWith(fnPrefix) ) {
            requestURI  = requestURI.substring(fnPrefix.length(), requestURI.length());
        }
    }

    public String getInsertURI() {
        return insertURI;
    }

    public String getRequestURI() {
        return requestURI;
    }
    
//    public static void main(String[] args) throws Throwable {
//        Hashtable ht = new Hashtable();
//        ht.put("InsertURI", "freenet:abc");
//        ht.put("RequestURI", "freenet:xyz");
//        FCP2SSKKeypairMessage m = new FCP2SSKKeypairMessage("a", "b", ht);
//        System.out.println(m.getInsertURI()+";"+m.getRequestURI());
//    }
}
