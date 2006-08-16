/**
 *   This file is part of JHyperochaFCPLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaFCPLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaFCPLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.freenet.fcp;

import hyperocha.freenet.fcp.io.DefaultIOConnectionErrorHandler;
import hyperocha.freenet.fcp.io.IOConnection;
import hyperocha.freenet.fcp.io.IOConnectionErrorHandler;
import hyperocha.freenet.fcp.utils.FCPUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * @author saces
 *
 */
public class FCPConnection {
	
	 private IOConnection rawConn; // = null;
	 private IOConnectionErrorHandler rawErrH; // = null; 
	 private String connectionID; // = null;

	/**
	 * @param node
	 * @param to
	 * @param errh
	 */
	//public FCPConnection(FCPNode node, int to, RawConnectionErrorHandler errh) {
	//	conn = new RawConnection(node, to, errh);
	//}

	/**
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node) {
		//this(node, node.timeOut, errh);
		rawErrH = new DefaultIOConnectionErrorHandler();
		rawConn = new IOConnection(node, rawErrH);
		rawConn.open();
		connectionID = nodeHello();
	}

	public String getConnectionID() {
		return connectionID;
	}
	
	public void start(ArrayList l, InputStream s) {

		int sc = l.size();
		for (int i = 0; i < sc; i++){
			//System.out.println("Startprint: " + l.get(i));
			rawConn.println((String)(l.get(i)));
		}
		
		// write complete file to socket
		while( true ) {
			int d;
			try {
				d = s.read();
				if( d < 0 ) {
					break; // EOF
				}
				rawConn.write(d);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

		}
		
		rawConn.flush();
	}
	
	
	public void start(ArrayList l) {
		//open();
		int sc = l.size();
		for (int i = 0; i < sc; i++){
			//System.out.println("Startprint: " + l.get(i));
			rawConn.println((String)(l.get(i)));
		}
		//fcpOut.println("EndMessage");
		//System.out.println("Startprint: EndMessage");
		//conn.flush();
		//String tmps = fcpInBuf.readLine();
		//handleIt(tmps);
		/*
		fcpIn.close();
		fcpInBuf.close();
        fcpOut.close();
		fcpSock.close();
		*/
		rawConn.flush();
	}
	
	/**
	 * reads the connection to the next EndMessage end return the entire
	 * message
	 * @return message
	 */
	public /*synchronized*/ Hashtable readEndMessage() {
		Hashtable result = new Hashtable();
		String tmp;
		
		// the first line is the reason
		//tmp = readLine();
		tmp = rawConn.readLine();
		result.put("hyper-result", tmp);
		
		
		while(true) {
            tmp = rawConn.readLine();
            //result.add(tmp);
            //System.out.println("ReadEndMessage out: " + tmp);
            if (tmp.compareTo("EndMessage") == 0) {
            	result.put("hyper-fin", tmp);
                break; 
            }
            if (tmp.indexOf("=") > -1) {
            	String[] tmp2 = tmp.split("=", 2);
            	result.put(tmp2[0], tmp2[1]);
            } else {
            	System.out.println("this shouldn't happen. FIXME. mpf!");
            	result.put("Unknown", tmp);
            }
        } while(tmp.compareTo("EndMessage") != 0);
        return result;	
	}
	
	public void handleIt(String s) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("handle it 7");
		readEndMessage();
	}
	
	private String nodeHello() {
		Hashtable result = null;
		result = helo();
		//System.out.println("TODO: Helo-Test: " + result);
		// FIXME: repeat loop here and return id 
		return null;
	}
	
	private Hashtable helo() {
		//System.out.println("TODO: EchtEs holo 07 hier!");
		
		ArrayList cmd = new ArrayList();
		cmd.add("ClientHello");
		cmd.add("Name="+ FCPUtil.getNewConnectionId("judl-"));
		cmd.add("ExpectedVersion=2.0");
		cmd.add("EndMessage");
		//cmd.add("En");
		start(cmd);
		return readEndMessage();
	}

	public void println(String s) {
		rawConn.println(s);
		rawConn.flush();
	}
	
	public void close() {
		rawConn.close();
	}
	
	//public long getConnectionId() {
	//	return node.getConnectionId();
	//}
}
