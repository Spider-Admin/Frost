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

import hyperocha.freenet.fcp.io.*;
import hyperocha.freenet.fcp.utils.*;

import java.io.*;
import java.util.*;


/**
 * @author  saces
 * @version $Id$
 */
public class FCPConnection {
	
	private final static byte[] fcp1header = {0,0,0,2};
	
	//public final static String MESSAGENAME = "hyperMessage-Name";
	//public final static String ENDMESSAGE = "hyper-fin";
	public final static String CONNECTIONIDPREFIX = "hyperocha-";
	
	private FCPIOConnection rawConn = null;
	private FCPIOConnectionErrorHandler rawErrH = null; 
	private String connectionID = null;
	
	private IIncoming callBack = null;

	/**
	 * geek constructor
	 * your own IO-errorhandler is assigned to this connection.
	 * I guess, you know what you are doing :)
	 * so the connection is not open after object creation
	 * you must do the connect and hello thingy from scratch by yourself 
	 * @param node
	 * @param errh Error handler. buildin if null. 
	 */
	private FCPConnection(FCPNode node, IIncoming callback, FCPIOConnectionErrorHandler ioErrH) {
		//this(node, node.timeOut, errh);
		callBack = callback;
		if (ioErrH == null) {
			rawErrH = new IOErrorHandler();
		} else {
			rawErrH = ioErrH;
		}
		rawConn = new FCPIOConnection(node, rawErrH);
	}
	
	/**
	 * 'ready to use' constructor
	 * Uses the lib buildin as connection id (CONNECTIONIDPREFIX-<timestamp>)
	 * all other things full automagically
	 * fcp1: sending the 4 bytes, helo?
	 * fcp2: helo.
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node, IIncoming callback) {
		this(node, callback, CONNECTIONIDPREFIX, true, 3);
	}
	
	/**
	 * 'ready to use' constructor
	 * Uses the given id as is as connection id
	 * all other things full automagically
	 * fcp1: sending the 4 bytes, helo?
	 * fcp2: helo.
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node, IIncoming callback, String id) {
		this(node, callback, id, false, 1);
	}
	
	/**
	 * 'ready to use' full constructor
	 * 
	 * all other things full automagically
	 * fcp1: sending the 4 bytes
	 * fcp2: helo.
	 * @param node
	 * @param id the connection id
	 * @param prefix if true, a timstamp will be added <id>-<unixepoch>
	 * @param attempts how many tries?
	 */
	public FCPConnection(FCPNode node, IIncoming callback, String id, boolean prefix, int attempts) {
		this(node, callback, (FCPIOConnectionErrorHandler)null);
		
		if (prefix) {
			connectionID = FCPUtil.getNewConnectionId(id);
		} else {
			connectionID = id;	
			if (id == null) { throw new Error("sss"); }
		}
		
		int nt = node.getNetworkType();

		switch (nt) {
			case Network.FCP1: initFCP1(attempts); break;
			case Network.FCP2: initFCP2(attempts); break;
			default : throw new Error("Unsupported network type: " + nt);
		}
	}

	private void initFCP2(int tries) {
		rawConn.open(); 
		fcp2Hello(connectionID, true, 3);
	}

	private void initFCP1(int tries) {
		rawConn.open("ISO-8859-1"); // ISO-LATIN-1 for .5
		rawConn.write(fcp1header, 0, fcp1header.length);
	}

	
	private class IOErrorHandler implements  FCPIOConnectionErrorHandler {

		public void onIOError(Exception e) {
			// TODO Auto-generated method stub
			// on io error the raw connection is closed 
			// FIXME
			System.err.println("IO Error: " + e);
			e.printStackTrace(System.err);		
		}
		
	}
	
	/**
	 * returns the ConnectionID, bestaetigt from node or null, if the connection is closed (not opened or closed due an io error or call to close() )
	 * @return
	 * @uml.property  name="connectionID"
	 */
	public String getConnectionID() {
		return connectionID;
	}
	
	public boolean isValid() {
		// TODO return (connectionID != null);
		return isIOValid();
	}
	
	public boolean isIOValid() {
		return rawConn.isOpen();
	}
	
	public void start(List l, long count, InputStream s) {
		start(l);
		copyTo(count, s);

//		int sc = l.size();
//		for (int i = 0; i < sc; i++){
//			//System.out.println("Startprint: " + l.get(i));
//			rawConn.println((String)(l.get(i)));
//		}
//		
//		// write complete file to socket
//		while( true ) {
//			int d;
//			try {
//				d = s.read();
//				if( d < 0 ) {
//					break; // EOF
//				}
//				rawConn.write(d);
//			} catch (IOException e) {
//				e.printStackTrace();
//				break;
//			}
//
//		}
//		
		rawConn.flush();
	}
	
	
	public void start(List l) {
		int sc = l.size();
		for (int i = 0; i < sc; i++) {
			//System.out.println("Startprint: " + l.get(i));
			rawConn.println((String)(l.get(i)));
		}
		rawConn.flush();
	}
	
	/**
	 * reads the connection to the next EndMessage end return the entire
	 * message
	 * @return message
	 */
	public NodeMessage readEndMessage() {
		NodeMessage result;
		String tmp;
		
		// the first line is the reason
		tmp = rawConn.readLine();
		result = new NodeMessage(tmp);
		while(true) {
            tmp = rawConn.readLine();
            //result.add(tmp);
            //System.out.println("ReadEndMessage out: " + tmp);
            //if (tmp.compareTo("Data") == 0) {
            	//TODO: callback.incommingData(result, streamtoread);
            //    break; 
            //}
            if (tmp.compareTo("EndMessage") == 0) {
            	result.setEnd(tmp);
                break; 
            }
            if (tmp.indexOf("=") > -1) {
            	String[] tmp2 = tmp.split("=", 2);
            	result.addItem(tmp2[0], tmp2[1]);
            } else {
            	System.out.println("this shouldn't happen. FIXME. mpf!");
            	result.addItem("Unknown", tmp);
            }
        } while(tmp.compareTo("EndMessage") != 0);
        return result;	
	}
	
	/**
	 * reads the connection to the next EndMessage end return the entire
	 * message
	 * @return message
	 */
	public NodeMessage readMessage(IIncoming callback) {

		NodeMessage result = null;
		String tmp;
		boolean isfirstline = true;
		
		while(true) {
            tmp = rawConn.readLine();
            if (tmp == null) { break; }  // this indicates an error, io connection closed
            if ((tmp.trim()).length() == 0) { continue; } // a empty line

            if (isfirstline) {
        		result = new NodeMessage(tmp);
            	isfirstline = false;
            	continue;      // insert coin
            }


            if (tmp.compareTo("Data") == 0) {
            	if (true) { throw new Error(); }
            	//callback.incommingData(this, result);
            	isfirstline = true;
                break; 
            }

            if (tmp.compareTo("EndMessage") == 0) {
            	result.setEnd(tmp);
            	isfirstline = true;
                break; 
            }
            
            if (tmp.indexOf("=") > -1) {
            	String[] tmp2 = tmp.split("=", 2);
            	result.addItem(tmp2[0], tmp2[1]);
            } else {
            	System.err.println("This shouldn't happen. FIXME. mpf!: " + tmp + " -> " + tmp.length());
            	result.addItem("Unknown", tmp);
            	throw new Error("www");  // TODO
            }
        } 
        return result;	
	}
	
	/**
	 * reads the connection to the next EndMessage end return the entire
	 * message
	 * @return message
	 */
	public void startMonitor(IIncoming callback) {

		NodeMessage result = null;
		String tmp;
		boolean isfirstline = true;
		
		while(rawConn.isOpen()) {
            tmp = rawConn.readLine();
            if (tmp == null) { break; }  // this indicates an error, io connection closed
            //System.out.println("TestiPipi (" + tmp.length() + "): " + tmp);
            if ((tmp.trim()).length() == 0) { continue; } // a empty line

            if (isfirstline) {
				// first line is allways the message name
				result = new NodeMessage(tmp);
				isfirstline = false;
            	continue;      // insert coin
			}
 
            if (tmp.compareTo("Data") == 0) {
            	result.setEnd(tmp);
            	String tmpID;
            	
            	int nt = rawConn.getNetworkType();

        		switch (nt) {
        			case Network.FCP1:
        				tmpID = connectionID;
        				break;
        			case Network.FCP2:
        				tmpID = (String)result.getStringValue("Identifier");
        				if (tmpID == null) {
        					tmpID = connectionID;
        				}
        				break;
        			default : throw new Error("Unsupported network type: " + nt);
        		}
        		
            	callback.incomingData(tmpID, result, this);
            	isfirstline = true;
                continue; 
            }

            if (tmp.compareTo("EndMessage") == 0) {
            	result.setEnd(tmp);
            	String tmpID;
            	
            	int nt = rawConn.getNetworkType();

        		switch (nt) {
        			case Network.FCP1: 
                        tmpID = connectionID;
                        break;
        			case Network.FCP2: 
                        tmpID = (String)result.getStringValue("Identifier"); 
						if (tmpID == null) {
							tmpID = connectionID;
						}
                        break;
        			default : throw new Error("Unsupported network type: " + nt);
        		}

            	callback.incomingMessage(tmpID, result);
            	isfirstline = true;
                continue; 
            }
            
            if (tmp.indexOf("=") > -1) {
            	String[] tmp2 = tmp.split("=", 2);
            	result.addItem(tmp2[0], tmp2[1]);
            } else {
            	System.err.println("This shouldn't happen. FIXME. mpf!: " + tmp + " -> " + tmp.length());
            	result.addItem("Unknown", tmp);
            	throw new Error("www");  // TODO
            }
        } 
		//throw new Error("www"); 
	}
	
	public void handleIt(String s) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("handle it 7");
		readEndMessage();
	}
	
	private String fcp2Hello(String connectionid, boolean prefix, int attempts) {
		if (attempts < 1) return null;
		NodeMessage result = null;
		result = helo(FCPUtil.getNewConnectionId(connectionid));
		//FCPUtil.getNewConnectionId("hyperocha-")
		// FIXME: repeat loop here and return id 
		//System.out.println("fcp2Hello: " + result);
		return null;
	}
	
	/**
	 * the real hello
	 * @param connectionid
	 * @return
	 */
	private NodeMessage helo(String connectionid) {
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
//	public void ClientPut(FreenetKeyType keytype,
//			                String identifier,
//			                Verbosity verbosity,
//			                int retries,
//			                PriorityClass priority,
//			                boolean keyonly,
//			                boolean global,
//			                boolean compress,
//			                Persistance persistance,
//			                String clienttoken,
//			                UploadFrom uploadfrom) {
//		List cmd = new LinkedList();
//		
//		rawConn.println("ClientPut");
//		rawConn.println("URI=" + keytype);
//		rawConn.println("Identifier=" + identifier);
//		rawConn.println("ClientToken=" + clienttoken); 
//		rawConn.println("Verbosity=" + verbosity);
//		rawConn.println("MaxRetries=" + retries);
//		rawConn.println("PriorityClass=" + priority);
//		rawConn.print("GetCHKOnly=");
//		if (keyonly)
//			rawConn.println("true");
//		else
//			rawConn.println("false");
//		rawConn.print("Global=");
//		if (global)
//			rawConn.println("true");
//		else
//			rawConn.println("false");
//		rawConn.print("DontCompress=");
//		if (compress)
//			rawConn.println("false");
//		else
//			rawConn.println("true");
//		
//		rawConn.println("Persistance=" + persistance);
//		
//		
//		rawConn.print("UploadFrom=");
//		
//		switch(uploadfrom.getType()) {
//			case UploadFrom.REDIRECT:	rawConn.println("redirect"); 
//									   	rawConn.println("TargetURI="+ uploadfrom.getSource());
//									   	rawConn.println("EndMessage"); 
//									   	break;
//			case UploadFrom.DISK: 		rawConn.println("disk"); 
//			   							rawConn.println("Filename="+ uploadfrom.getSource());
//			   							rawConn.println("EndMessage");
//			   							break;
//			case UploadFrom.DIRECT: 	rawConn.println("direct"); 
//										int l = uploadfrom.getCount();
//										InputStream is = uploadfrom.getIs();
//										rawConn.println("DataLength=" + l);
//										rawConn.println("Data");
//										
//										int d;
//										
//										for (int i=0; i<l; i++) {
//											try {
//												d = is.read();
//												rawConn.write(d);
//											} catch (Exception e) {
//												// TODO call errerhandler
//												e.printStackTrace();
//												break;
//											}	
//										}
//										
//										
//										break;
//			default: 					System.err.println("Invald Upload From!");
//										// TODO: call error handler. 
//		}
//		rawConn.flush();
//			
//		
//	}
	
	public Exception getLastIOError() {
		return rawConn.getLastError();
	}
	
	public void copyFrom(long count, OutputStream os) {
		int d;
		//System.err.println("copyFrom called for bytes: " + count);
		//System.err.println("copyFrom called : " + this);
		for (int i=0; i<count; i++) {
			//System.err.println("TEST copyFrom: " + i);
			try {
				//System.err.println("TEST01 copyFrom: " + i);
				d = rawConn.read();
				//System.err.println("TEST02 copyFrom: " + i);
				os.write(d);
				//System.err.println("copyFrom cc: " + i);
			} catch (Exception e) {
				// TODO call errerhandler
				e.printStackTrace();
				break;
			}	
		}
	}
	
	public void copyTo(long count, InputStream is) {
		int d;
		
		for (int i=0; i<count; i++) {
			try {
				d = is.read();
				rawConn.write(d);
			} catch (Exception e) {
				// TODO call errerhandler
				e.printStackTrace();
				break;
			}	
		}
	}

	/**
	 * Skip n bytes from connection
	 * @param size
	 */
	public void skip(long size) {
		for (int i=0; i<size; i++) {
			try {
				rawConn.read();
			} catch (Exception e) {
				// TODO call errerhandler
				e.printStackTrace();
				break;
			}	
		}
	}
}
