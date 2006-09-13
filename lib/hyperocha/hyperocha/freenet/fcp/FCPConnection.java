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

import hyperocha.freenet.fcp.io.FCPIOConnection;
import hyperocha.freenet.fcp.io.FCPIOConnectionErrorHandler;
import hyperocha.freenet.fcp.utils.FCPUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * @author  saces
 */
public class FCPConnection {
	
	public final static String MESSAGENAME = "hyperMessage-Name";
	public final static String ENDMESSAGE = "hyper-fin";
	public final static String CONNECTIONIDPREFIX = "hyperocha-";
	
	private FCPIOConnection rawConn = null;
	private FCPIOConnectionErrorHandler rawErrH = null; 
	private String connectionID = null;

	/**
	 * geek constructor
	 * you own IO-errorhandler is assigned to this connction.
	 * I guess, you know what you ar doing :)
	 * so the connection is not open after object creation
	 * you must do the connect and hello thingy from scratch by yourself 
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node, FCPIOConnectionErrorHandler ioErrH) {
		//this(node, node.timeOut, errh);
		rawErrH = ioErrH;
		rawConn = new FCPIOConnection(node, rawErrH);
	}


	/**
	 * a special constructor for bback
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node, FCPIOConnectionErrorHandler ioErrH, byte[] header) {
		//this(node, node.timeOut, errh);
		rawErrH = ioErrH;
		rawConn = new FCPIOConnection(node, rawErrH);
		rawConn.open("ISO-8859-1"); // ISO-LATIN-1 for .5
		rawConn.write(header, 0, header.length);
		//connectionID = nodeHello();
	}

	/**
	 * creates a connection and tries the ClientHollo three times
	 * getConnectionID "<buildinprefix>-<generatednumber>"
	 * @param node
	 */
	public FCPConnection(FCPNode node) {
		this(node, CONNECTIONIDPREFIX, true, 3);
	}

	
	/**
	 * creates a connection and tries the ClientHollo one time
	 * uses the given Connection id
	 * getConnectionID "<connectionid>"
	 * @param node
	 * @param connectionid
	 */
	public FCPConnection(FCPNode node, String connectionid) {
		this(node, connectionid, false, 1);
	}
	
	/**
	 * full constructor
	 * creates a connection and tries the ClientHollo <attempt> times (<attempt> > 0)
	 * prefix = true :  getConnectionID "<connectionid>-<generatednumber>"
	 * prefix = false :  getConnectionID "<connectionid>"
	 * @param node
	 * @param connectionid the coonection id prefix
	 * @param attempt   wie oft das helo probieren?
	 */
	public FCPConnection(FCPNode node, String connectionid, boolean prefix, int attempt) {
		//this(node, node.timeOut, errh);
		rawErrH = new IOErrorHandler();
		rawConn = new FCPIOConnection(node, rawErrH);
		rawConn.open();
		connectionID = clientHello(connectionid, prefix, attempt);
	}
	
	private class IOErrorHandler implements  FCPIOConnectionErrorHandler {

		public void onIOError(Exception e) {
			// TODO Auto-generated method stub
			// on io error the raw connection is closed 
			
			System.err.println("IO Error: " + e);
			e.printStackTrace(System.err);
			
		}
		
	}

	/**
	 * returns the ConnectionID, bestätigt from node or null, if the connection is closed (not opened or closed due an io error or call to close() )
	 * @return
	 * @uml.property  name="connectionID"
	 */
	public String getConnectionID() {
		return connectionID;
	}
	
	public boolean isValid() {
		return (connectionID != null);
	}
	
	public boolean isIOValid() {
		return rawConn.isOpen();
	}
	
	public void start(List l, InputStream s) {

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
	
	
	public void start(List l) {
		//open();
		int sc = l.size();
		for (int i = 0; i < sc; i++) {
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
		result.put( MESSAGENAME, tmp);
		
		while(true) {
            tmp = rawConn.readLine();
            //result.add(tmp);
            //System.out.println("ReadEndMessage out: " + tmp);
            //if (tmp.compareTo("Data") == 0) {
            	//TODO: callback.incommingData(result, streamtoread);
            //    break; 
            //}
            if (tmp.compareTo("EndMessage") == 0) {
            	result.put(ENDMESSAGE, tmp);
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
	
	private String clientHello(String connectionid, boolean prefix, int attempts) {
		if (attempts < 1) return null;
		Hashtable result = null;
		result = helo(FCPUtil.getNewConnectionId(connectionid));
		//FCPUtil.getNewConnectionId("hyperocha-")
		// FIXME: repeat loop here and return id 
		//System.out.println(result);
		return null;
	}
	
	/**
	 * the real hello
	 * @param connectionid
	 * @return
	 */
	private Hashtable helo(String connectionid) {
		//System.out.println("TODO: EchtEs holo 07 hier!");
		
		ArrayList cmd = new ArrayList();
		cmd.add("ClientHello");
		cmd.add("Name="+ connectionid);
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
		connectionID = null;
		rawConn.close();
	}
	
	/*
	 *  the geek ClientPut
	 */
	public void ClientPut(FreenetKeyType keytype,
			                String identifier,
			                Verbosity verbosity,
			                int retries,
			                PriorityClass priority,
			                boolean keyonly,
			                boolean global,
			                boolean compress,
			                Persistance persistance,
			                String clienttoken,
			                UploadFrom uploadfrom) {
		List cmd = new LinkedList();
		
		rawConn.println("ClientPut");
		rawConn.println("URI=" + keytype);
		rawConn.println("Identifier=" + identifier);
		rawConn.println("ClientToken=" + clienttoken); 
		rawConn.println("Verbosity=" + verbosity);
		rawConn.println("MaxRetries=" + retries);
		rawConn.println("PriorityClass=" + priority);
		rawConn.print("GetCHKOnly=");
		if (keyonly)
			rawConn.println("true");
		else
			rawConn.println("false");
		rawConn.print("Global=");
		if (global)
			rawConn.println("true");
		else
			rawConn.println("false");
		rawConn.print("DontCompress=");
		if (compress)
			rawConn.println("false");
		else
			rawConn.println("true");
		
		rawConn.println("Persistance=" + persistance);
		
		
		rawConn.print("UploadFrom=");
		
		switch(uploadfrom.getType()) {
			case UploadFrom.REDIRECT:	rawConn.println("redirect"); 
									   	rawConn.println("TargetURI="+ uploadfrom.getSource());
									   	rawConn.println("EndMessage"); 
									   	break;
			case UploadFrom.DISK: 		rawConn.println("disk"); 
			   							rawConn.println("Filename="+ uploadfrom.getSource());
			   							rawConn.println("EndMessage");
			   							break;
			case UploadFrom.DIRECT: 	rawConn.println("direct"); 
										int l = uploadfrom.getCount();
										InputStream is = uploadfrom.getIs();
										rawConn.println("DataLength=" + l);
										rawConn.println("Data");
										
										int d;
										
										for (int i=0; i<l; i++) {
											try {
												d = is.read();
												rawConn.write(d);
											} catch (Exception e) {
												// TODO call errerhandler
												e.printStackTrace();
												break;
											}	
										}
										
										
										break;
			default: 					System.err.println("Invald Upload From!");
										// TODO: call error handler. 
		}
		rawConn.flush();
			
		
	}
	
	public Exception getLastIOError() {
		return rawConn.getLastError();
	}
}
