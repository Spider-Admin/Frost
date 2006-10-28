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
import hyperocha.freenet.fcp.utils.FCPTests;
import hyperocha.freenet.fcp.utils.FCPUtil;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;


/**
 * @author  saces
 */
public class FCPNode {
	
	private static final String CLIENTTOKEN = "hyperocha test";
	
	private FCPIOConnectionErrorHandler ioErrorHandler = null;
	private FCPNodeConfig nodeConfig;
	private FCPNodeStatus nodeStatus;
    private volatile FCPConnectionRunner defaultConn = null;
    
    private IIncoming callBack = null;
    
    private Exception lastError = null;
    
    public FCPNode(FCPNodeConfig nodeconfig) {
    	this(nodeconfig, null);
    }
    
    public FCPNode(FCPNodeConfig nodeconfig, IIncoming callback) {
    	this.nodeConfig = nodeconfig;
    	this.callBack = callback;
    }
    
     /**
     * the constructor checks only the plausibility of 'server:port'
     * but doesn't etablish any connection to it.
     * @param String 'server:port'
     * @throws Throwable 
     * 
     */
    
 /*   
    
    
    public FCPNode(String serverport) {
    	this(serverport, null);
    }
    
	public FCPNode(String serverport, IOConnectionErrorHandler errh) {
		String[] splitServerPort = serverport.split(":");
		InetAddress ia = null;
		//InetSocketAddress isa = null;
//        try {
            try {
				ia = InetAddress.getByName(splitServerPort[0]);
				this.host = ia;
	            this.hostName = ia.getHostName();
	            this.hostIp = ia.getHostAddress();
			} catch (UnknownHostException e) {
				errh.onIOError(e);
				//System.out.println("ERROR: Unknown FCP host: "+ serverport);
				//e.printStackTrace();
			}
            
//        } catch(Throwable t) {
//            System.out.println("ERROR: Unknown FCP host: "+ serverport);
            //System.out.println(t);
//        }
        try {
            port = Integer.parseInt(splitServerPort[1]);
            if ((port < 1) || (port > 65535)) { throw new IllegalArgumentException(); }
           // isa = InetSocketAddress.createUnresolved(this.hostName, port);
        } catch(IllegalArgumentException e) {
        	errh.onIOError(e);
          //System.out.println("ERROR: Unknown FCP server:port: "+ serverport);
         // System.out.println(t);
      }
            //if ((port < 1) || (port > 65535)) { throw new IllegalArgumentException(); } 
            //isa = InetSocketAddress.createUnresolved(this.hostName, port);
//        } catch(Throwable t) {
//            System.out.println("ERROR: Unknown FCP port: "+ serverport);
           // System.out.println(t);
//        }
        
    }
*/	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
    
	public synchronized FCPConnectionRunner getDefaultFCPConnectionRunner() {
		if (defaultConn == null) {
			defaultConn = new FCPConnectionRunner(this, nodeConfig.getID(), callBack);
			defaultConn.start();
		}
		//System.out.println("getDefaultFCPConnection" + defaultConn);
		return defaultConn;
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
		return createSocket(nodeConfig.getTimeOut());
	}
	
	/**
	 * @param to timeout
	 * @return 
	 * @throws IOException
	 */
	public Socket createSocket(int to) throws IOException {
		Socket sock = new Socket(nodeConfig.getHost(), nodeConfig.getPort());
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
		return nodeConfig.haveDDA();
	}
	
	public FreenetKey generateSSK() {
		List cmd = new LinkedList();
		cmd.add("GenerateSSK");
		cmd.add("Identifier=My Identifier Blah Blah");
		cmd.add("EndMessage");

		FCPConnection conn = null; //node.getDefaultFCPConnection();
//		boolean repeat = true;
		Hashtable result = null;
		//try { 
			conn = getNewFCPConnection(null, null);
			conn.start(cmd);
			result = conn.readEndMessage();
//    	} catch (Throwable ex) {
//			conn.close();
//			return null;
//		}

			
		if (!("SSKKeypair").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
			System.err.println("SSK gen err: " + result);
			conn.close();
			return null;
		}
			
			
		String iURI = (String)result.get("InsertURI");
		String rURI = (String)result.get("RequestURI");
		
		
		//System.out.println("Result I:" + iURI);	
		
		//System.out.println("Result I:" + iURI.substring(12,55));
		
		//System.out.println("Result R:" + rURI);
		//System.out.println("Result R:" + rURI.substring(12,55));
		//System.out.println("Result R:" + rURI.substring(56,99));
		//System.out.println("Result R:" + rURI.substring(101,107));
		
		// public FreenetKey(FreenetKeyType keytype, String pubkey, String privkey, String cryptokey, String suffix) {
		FreenetKey key = new FreenetKey(Network.FCP2, FreenetKeyType.SSK, rURI.substring(12,55), iURI.substring(12,55), rURI.substring(56,99), rURI.substring(101,107));
			
			
		//System.out.println("Result:" + result);
//		} catch (Throwable ex) {
//			conn.close();
//			return null;
		
		conn.close();
		//return false;
	
		//System.out.println("Result:" + key);
		
		return key;
	}

	public boolean testDDA() {
		return testDDA(FCPUtil.getNewConnectionId("InsertDDA-Test-") , CLIENTTOKEN);
	}
	
	public boolean testDDA(String identifier, String clientoken) {
		File f = FCPTests.createTestFile();
		return testDDA(identifier, clientoken, f);
	}
	
	public boolean testDDA(String identifier, String clientoken, File testfile) {

		List cmd = new LinkedList();
		cmd.add("ClientPut");
		cmd.add("URI=CHK@");
		cmd.add("Identifier=" + identifier); 
		cmd.add("Verbosity=0");
		cmd.add("MaxRetries=1");      // only one try, the node accepts the filename or net
		cmd.add("PriorityClass=0");   // today, please ;) 
		cmd.add("GetCHKOnly=true");   // calculate the chk from 1k (the default testfile)
		cmd.add("Global=false");
		cmd.add("Persistance=" + Persistance.CONNECTION);
		cmd.add("DontCompress=true");
		cmd.add("ClientToken=" + clientoken); 
		cmd.add("UploadFrom=disk");
		cmd.add("Filename=w" + testfile.getAbsolutePath());
		cmd.add("EndMessage");
		
		
		/* we need only to know that the node accept the filename,
		 * but we run the hole one
		 * it takes maybe more time and give more stress for the node to cancel the job
		 * calculate the key from the 1k testfile  
		 */
		
		FCPConnection conn = null; //node.getDefaultFCPConnection();
		boolean repeat = true;
		Hashtable result = null;
		try { 
			conn = getNewFCPConnection(null, "");
			conn.start(cmd);
			
			while (repeat) {
				result = conn.readEndMessage();
				System.out.println("DDA-Test loop: " + result);
				repeat = ("URIGenerated").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)));
			}
			
			if (("PutSuccessful").equalsIgnoreCase((String)(result.get(FCPConnection.MESSAGENAME)))) {
				conn.close();
				return true; // the only one case for return ok.
			}
			//System.out.println("Result:" + result.get("judl-reason"));
		} catch (Throwable ex) {		
		}
		conn.close();
		return false;
	}
	
	public FCPVersion getFCPVersion() {
		FCPVersion result = null;
		//FCPConnection conn = new FCPConnection("");
		//result = 
		return result;		
	}

	public void initConfig() {
		nodeConfig.init();
		// TODO 
//		switch (networktype) {
//		case FCP1: hello5; break;
//		case FCP2: hello7; break;
//		dafault: unsopportet netzwerk
//		}
		
	}

	public boolean isValid() {
		return (lastError == null);
	}

	public int getNetworkType() {
		return nodeConfig.getNetworkType();
	}

	public String getID() {
		return nodeConfig.getID();
	}

	public void init() {
		nodeConfig.init();
		// TODO check for conect error 
	}

	public boolean isAddress(String host, int port) {
		return nodeConfig.isAddress(host, port);
	}

	/**
	 * @return the nodeConfig
	 */
	protected FCPNodeConfig getNodeConfig() {
		return nodeConfig;
	}
}
