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

import hyperocha.freenet.fcp.io.FCPIOConnectionErrorHandler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * @author  saces
 * @version $Id$
 */
public class FCPNode extends Observable {
	public static final int STATUS_ERROR = -1;
	public static final int STATUS_OFFLINE = 0;
	public static final int STATUS_REACHABLE = 1;
	public static final int STATUS_ONLINE = 2;
	
	private class FCPNodeConfig {
		
		private String nodeID = null;
		private int networkType;
		private InetAddress host = null;
	    private int port = -1;
		private String hostName = null; // avoid name lookup recursion in security manager
		private String hostIp = null; // avoid name lookup recursion in security manager
		
		// default timeout 30 minutes
		// its asumes a reply from the node after each bucket
		// Verbosity=1! (Verbosity.SIMPLEPROGRESS)
		// FIXME testvalue
		private int timeOut = 3 * 60 * 1000; 
		
		private boolean canDownload = true; // the factory can use this node for downloads 
		private boolean canUpload = true; // the factory can use this node for uploads
	    private boolean useDDA = true;
	    /* seen an option on earlier versions, must ask toad about this */  
	    private boolean useGQ = true;  
	    
	    /* from fproxy config page:
	     * Whether to enable Persistence=forever for FCP requests. Meaning whether to
	     * support requests which persist over node restarts; they must be written to
	     * disk and this may constitute a security risk for some people.
	     */ 
	    private boolean havePersistence = true;
	} 
	
	private class FCPNodeStatus {
		private int status = STATUS_OFFLINE;
		private long requestCount = -1; // ??
		private long onlineSince = 0;  // unix epoch
		private int averageSpeed = -1; // requests per houeeer???
	}

	private FCPIOConnectionErrorHandler ioErrorHandler = null;
	private FCPNodeConfig nodeConfig = new FCPNodeConfig();
	private FCPNodeStatus nodeStatus = new FCPNodeStatus();
    private volatile FCPConnectionRunner defaultConnRunner = null;
    
    private IIncoming callBack = null;
    
    private Exception lastError = null;
    
//    public FCPNode(FCPNodeConfig nodeconfig) {
//    	this(nodeconfig, null);
//    }
    
    public FCPNode(int networktype, String id, String serverport, IIncoming callback) {
    	nodeConfig.networkType = networktype;
    	nodeConfig.nodeID = id;
		setServerPort(serverport);
    }

	private boolean setServerPort(String serverport) {
		boolean retValue = false;
		nodeStatus.status = STATUS_OFFLINE;
		String[] splitServerPort = serverport.split(":");
        try {
        	nodeConfig.hostName = splitServerPort[0];
        	retValue = setPort(splitServerPort[1]);
		} catch (Exception e) {
			lastError = e;
			e.printStackTrace();
			return false;
		}
		return retValue;
	}
    
	/**
	 * 
	 * @return FCPConnectionRunner
	 */
	public synchronized FCPConnectionRunner getDefaultFCPConnectionRunner() {
		if (defaultConnRunner == null) {
			defaultConnRunner = new FCPConnectionRunner(this, nodeConfig.nodeID, callBack);
			defaultConnRunner.start();
			
			// hack for parse gq on startup.
			List cmd = new LinkedList();
			cmd.add("WatchGlobal");
			cmd.add("Enabled=true");
			cmd.add("VerbosityMask=-1");
			cmd.add("EndMessage");
			cmd.add("ListPersistentRequests");
			cmd.add("EndMessage");
			defaultConnRunner.send(cmd);
		}
		//System.out.println("getDefaultFCPConnection" + defaultConn);
		return defaultConnRunner;
	}
	
	public synchronized void closeDefaultConnectionrunner() {
		if (defaultConnRunner == null) {
			defaultConnRunner.close();
			defaultConnRunner = null;
		}
	}
	
	public synchronized FCPConnectionRunner getNewFCPConnectionRunner(String id) {
		FCPConnectionRunner conn = null;
		conn = new FCPConnectionRunner(this, id, callBack);
		conn.start();
		return conn;
	}

	
//	public synchronized FCPConnection getDefaultFCPConnection() {
//		if (defaultConn == null) {
//			defaultConn = getNewFCPConnection(callBack, nodeConfig.getID());
//			SwingUtilities.invokeLater(new Runnable() {
//	            public void run() {
//	            	defaultConn.startMonitor(callBack);
//	            }
//	        });
//		}
//		System.out.println("getDefaultFCPConnection" + defaultConn);
//		return defaultConn;
//	}
	
	/**
	 * @return Socket
	 * @throws IOException
	 */
	public Socket createSocket() throws IOException {
		return createSocket(nodeConfig.timeOut);
	}
	
	/**
	 * @param to timeout
	 * @return Socket
	 * @throws IOException
	 */
	public Socket createSocket(int to) throws IOException {
		Socket sock = new Socket(nodeConfig.host, nodeConfig.port);
	    sock.setSoTimeout(to);
	    return sock;
	}
	
	
	public FCPConnection getNewFCPConnection() {
		return new FCPConnection(this, null);
	}
	
	public FCPConnection getNewFCPConnection(IIncoming callback, String id) {
		return new FCPConnection(this, callback, id);
	}
	
	public FCPConnection getNewFCPConnection(IIncoming callback, String id, boolean prefix, int tries) {
		return new FCPConnection(this, callback, id, prefix, tries);
	}

	public boolean performNodeTest(FCPNodeConfig config) {
		// check adresse:Port
		// if rechable, check for peers, at least one connected is necesary
		// TODO
		return false;
	}
	
	public boolean performOptionsTest(FCPNodeConfig config) {
		// check for DDA
		// check Global queue?
		// check persitance
		// TODO
		return false;
	}

	public boolean haveDDA() {
		return nodeConfig.useDDA;
	}
	
//	public FreenetKey generateSSK() {
//		List cmd = new LinkedList();
//		cmd.add("GenerateSSK");
//		cmd.add("Identifier=My Identifier Blah Blah");
//		cmd.add("EndMessage");
//
//		FCPConnection conn = null; //node.getDefaultFCPConnection();
////		boolean repeat = true;
//		NodeMesage result = null;
//		//try { 
//			conn = getNewFCPConnection(null, null);
//			conn.start(cmd);
//			result = conn.readEndMessage();
////    	} catch (Throwable ex) {
////			conn.close();
////			return null;
////		}
//
//			
//		if (!("SSKKeypair").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
//			System.err.println("SSK gen err: " + result);
//			conn.close();
//			return null;
//		}
//			
//			
//		String iURI = (String)result.get("InsertURI");
//		String rURI = (String)result.get("RequestURI");
//		
//		
//		//System.out.println("Result I:" + iURI);	
//		
//		//System.out.println("Result I:" + iURI.substring(12,55));
//		
//		//System.out.println("Result R:" + rURI);
//		//System.out.println("Result R:" + rURI.substring(12,55));
//		//System.out.println("Result R:" + rURI.substring(56,99));
//		//System.out.println("Result R:" + rURI.substring(101,107));
//		
//		// public FreenetKey(FreenetKeyType keytype, String pubkey, String privkey, String cryptokey, String suffix) {
//		FreenetKey key = new FreenetKey(Network.FCP2, FreenetKeyType.SSK, rURI.substring(12,55), iURI.substring(12,55), rURI.substring(56,99), rURI.substring(101,107));
//			
//			
//		//System.out.println("Result:" + result);
////		} catch (Throwable ex) {
////			conn.close();
////			return null;
//		
//		conn.close();
//		//return false;
//	
//		//System.out.println("Result:" + key);
//		
//		return key;
//	}

//	public boolean testDDA() {
//		return testDDA(FCPUtil.getNewConnectionId("InsertDDA-Test-") , CLIENTTOKEN);
//	}
//	
//	public boolean testDDA(String identifier, String clientoken) {
//		File f = FCPTests.createTestFile();
//		return testDDA(identifier, clientoken, f);
//	}
//	
//	public boolean testDDA(String identifier, String clientoken, File testfile) {
//
//		List cmd = new LinkedList();
//		cmd.add("ClientPut");
//		cmd.add("URI=CHK@");
//		cmd.add("Identifier=" + identifier); 
//		cmd.add("Verbosity=0");
//		cmd.add("MaxRetries=1");      // only one try, the node accepts the filename or net
//		cmd.add("PriorityClass=0");   // today, please ;) 
//		cmd.add("GetCHKOnly=true");   // calculate the chk from 1k (the default testfile)
//		cmd.add("Global=false");
//		cmd.add("Persistence=" + Persistence.CONNECTION);
//		cmd.add("DontCompress=true");
//		cmd.add("ClientToken=" + clientoken); 
//		cmd.add("UploadFrom=disk");
//		cmd.add("Filename=w" + testfile.getAbsolutePath());
//		cmd.add("EndMessage");
//		
//		
//		/* we need only to know that the node accept the filename,
//		 * but we run the hole one
//		 * it takes maybe more time and give more stress for the node to cancel the job
//		 * calculate the key from the 1k testfile  
//		 */
//		
//		FCPConnection conn = null; //node.getDefaultFCPConnection();
//		boolean repeat = true;
//		Hashtable result = null;
//		try { 
//			conn = getNewFCPConnection(null, "");
//			conn.start(cmd);
//			
//			while (repeat) {
//				result = conn.readEndMessage();
//				System.out.println("DDA-Test loop: " + result);
//				repeat = ("URIGenerated").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)));
//			}
//			
//			if (("PutSuccessful").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
//				conn.close();
//				return true; // the only one case for return ok.
//			}
//			//System.out.println("Result:" + result.get("judl-reason"));
//		} catch (Throwable ex) {		
//		}
//		conn.close();
//		return false;
//	}
	
	public FCPVersion getFCPVersion() {
		FCPVersion result = null;
		//FCPConnection conn = new FCPConnection("");
		//result = 
		return result;		
	}

	public void goOnline() {
		if (!init()) { return; }
		FCPConnection conn = getNewFCPConnection();
		if(!conn.isValid()) { return; } // helo failed
		conn.close();
		setStatus(STATUS_ONLINE);
	}
	
	public synchronized void goOffline() {
		if (defaultConnRunner == null) {
			defaultConnRunner.close();
			defaultConnRunner = null;
		}
		setStatus(STATUS_OFFLINE);
	}
	

	public boolean isValid() {
		return (lastError == null);
	}

	public int getNetworkType() {
		return nodeConfig.networkType;
	}

	public String getID() {
		return nodeConfig.nodeID;
	}

	public boolean isAddress(String host, int port) {
		if( port == port ) {
			if( host.equals(nodeConfig.hostIp) || host.equals(nodeConfig.hostName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isOffline() {
		return (nodeStatus.status < 1);
	}
	
	public boolean isOnline() {
		return (nodeStatus.status > 0);
	}
	
	/**
	 * @param port  the port to set
	 * @return false if port out of range
	 */
	private boolean setPort(int port) {
		if ((port < 1) || (port > 65535)) return false;
		nodeConfig.port = port;
		return true;
	}
	
	private boolean setPort(String port) {
		return setPort(Integer.parseInt(port));
	}	
	
	private boolean init() {
		return init(false);
	}
	
	private void setStatus(int newStatus) {
		//System.err.println("Node Status set: " + newStatus);
		//new Error().printStackTrace();
		if ( nodeStatus.status == newStatus ) { return; }
		setChanged();
		nodeStatus.status = newStatus;
		notifyObservers(new Integer(newStatus));
	}
	
	private void setError(Exception e) {
		lastError = e;
		setStatus(STATUS_ERROR);
	}
	
	/**
	 * test if server:port is reachable (atablish connection), but 
	 * do not any real transfer
	 * @param force true - reset state before testing 
	 * @return true if 
	 */
	private boolean init(boolean force) {
		if ((!force) && (nodeStatus.status > 0)) { return true; }
		//nodeStatus.status = STATUS_OFFLINE;
		String server = nodeConfig.hostName;
		InetAddress ia = null;
        try {
        	ia = InetAddress.getByName(server);
        	setAddress(ia);
        	Socket testsock = new Socket(nodeConfig.host, nodeConfig.port);
        	testsock.close();
        } catch (ConnectException ce) {
        	lastError = ce;
        	setStatus(STATUS_OFFLINE);
        	return false;
		} catch (Exception e) {
			setError(e);
			e.printStackTrace();
			return false;
		}
		setStatus(STATUS_REACHABLE);
		return true;
	}
	
	private void setAddress(InetAddress ia) {
		nodeConfig.host = ia;
		nodeConfig.hostName = ia.getHostName();
		nodeConfig.hostIp = ia.getHostAddress();
	}
	
	/**
	 * @param haveDDA The haveDDA to set.
	 */
	public void setDDA(boolean haveDDA) {
		nodeConfig.useDDA = haveDDA;
	}
}
