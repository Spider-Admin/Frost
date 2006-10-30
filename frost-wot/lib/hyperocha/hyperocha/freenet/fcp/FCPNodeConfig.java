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

import hyperocha.util.IStorageObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author  saces
 */
public class FCPNodeConfig implements IStorageObject {
	
	private String nodeID = null;
	
	private int networkType;
	
	private boolean initialized = false;
	
	private InetAddress host = null;
    private int port = -1;
	private String hostName = null; // avoid name lookup recursion in security manager
	private String hostIp = null; // avoid name lookup recursion in security manager
	
	// default timeout 30 minutes
	// its asumes a reply from the node after each bucket
	// Verbosity=1! (Verbosity.SIMPLEPROGRESS)
	private int timeOut = 30 * 60 * 1000; 
	
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
    
    /*
     * true, if the node have adjusted its own config
     */
    private boolean configChanged = false;
    
    private Exception lasterror = null;

    /**
     * the constructor checks only the plausibility of 'server:port'
     * but doesn't etablish any connection to it.
     * @param String 'server:port'
     * @throws Throwable 
     * 
     */
    public FCPNodeConfig(int networktype, String id, String serverport) {
    	this.networkType = networktype;
		this.nodeID = id;
		setServerPort(serverport);
	}
    
    private FCPNodeConfig(FCPNodeConfig config) {
		nodeID = config.nodeID;
		port = config.port;
		hostName = config.hostName; // avoid name lookup recursion in security manager
		hostIp = config.hostIp; // avoid name lookup recursion in security manager
		timeOut = config.timeOut; 
		canDownload = config.canDownload; // the factory can use this node for downloads 
		canUpload = config.canUpload; // the factory can use this node for uploads
		useDDA = config.useDDA;
		useGQ = config.useGQ;  
		havePersistence = config.havePersistence;
		networkType = config.networkType;
	}

	/**
	 * 
	 */
//	private FCPNodeConfig(int id, Object userObject) {
//		this.nodeID = id;
//		loadConfig(id, userObject);
//	}
	
	/**
	 * 
	 */
//	private FCPNodeConfig(int id) {
//		this.nodeID = id;
//	}
	
	public FCPNodeConfig(DataInputStream dis) {
		loadData(dis);
	}
	
	public boolean init() {
		return init(false);
	}
	
	public boolean init(boolean force) {
		if ((!force) && initialized) { return true; }
		initialized = false;
		String server = hostName;
		InetAddress ia = null;
        try {
        	ia = InetAddress.getByName(server);
        	setAddress(ia);
        	Socket testsock = new Socket(host, port);
        	testsock.close();
		} catch (Exception e) {
			lasterror = e;
			e.printStackTrace();
			return false;
		}
		initialized = true;
		//System.err.println("init!!!!!");
		return true;
	}

	private boolean setServerPort(String serverport) {
		boolean retValue = false;
		initialized = false;
		String[] splitServerPort = serverport.split(":");
        try {
        	hostName = splitServerPort[0];
        	retValue = setPort(splitServerPort[1]);
		} catch (Exception e) {
			lasterror = e;
			//e.printStackTrace();
			return false;
		}
		return retValue;
	}
	
	private void setAddress(InetAddress ia) {
		this.host = ia;
		this.hostName = ia.getHostName();
        this.hostIp = ia.getHostAddress();
	}	
	
	/**
	 * @param port  the port to set
	 * @uml.property  name="port"
	 */
	private boolean setPort(int port) {
		if ((port < 1) || (port > 65535)) return false;
		this.port = port;
		return true;
	}
	
	private boolean setPort(String port) {
		return setPort(Integer.parseInt(port));
	}	

	/**
	 * @return Returns the haveDDA.
	 */
	protected boolean haveDDA() {
		return useDDA;
	}

	/**
	 * @param haveDDA The haveDDA to set.
	 */
	public void setDDA(boolean haveDDA) {
		this.useDDA = haveDDA;
	}

	/**
	 * @return Returns the haveGQ.
	 */
	protected boolean haveGQ() {
		return useGQ;
	}

	/**
	 * @param haveGQ The haveGQ to set.
	 */
	protected void setGQ(boolean haveGQ) {
		this.useGQ = haveGQ;
	}

	/**
	 * @return  Returns the havePersistence.
	 * @uml.property  name="havePersistence"
	 */
	protected boolean isHavePersistence() {
		return havePersistence;
	}

	/**
	 * @param havePersistence  The havePersistence to set.
	 * @uml.property  name="havePersistence"
	 */
	protected void setHavePersistence(boolean havePersistence) {
		this.havePersistence = havePersistence;
	}
	
	
	public boolean loadData(DataInputStream dis) {
		try {
			nodeID = dis.readUTF();
			port = dis.readInt();
			hostName = dis.readUTF(); // avoid name lookup recursion in security manager
			hostIp = dis.readUTF(); // avoid name lookup recursion in security manager
			timeOut = dis.readInt(); 
			canDownload = dis.readBoolean(); // the factory can use this node for downloads 
			canUpload = dis.readBoolean(); // the factory can use this node for uploads
			useDDA = dis.readBoolean();
			useGQ = dis.readBoolean();  
			havePersistence = dis.readBoolean();
		} catch (IOException e) {
			lasterror = e;
			return false;
		}
		return true;
	}
	
	public boolean storeData(DataOutputStream dos) {
		try {
			dos.writeUTF(nodeID);
			dos.writeInt(port);
			dos.writeUTF(hostName); // avoid name lookup recursion in security manager
			dos.writeUTF(hostIp); // avoid name lookup recursion in security manager
			dos.writeInt(timeOut); 
			dos.writeBoolean(canDownload); // the factory can use this node for downloads 
			dos.writeBoolean(canUpload); // the factory can use this node for uploads
			dos.writeBoolean(useDDA);
			dos.writeBoolean(useGQ);  
			dos.writeBoolean(havePersistence);
		} catch (IOException e) {
			lasterror = e;
			return false;
		}
		return true;
	}
	
	
	public boolean loadConfig(int id, Object userObject) {
		return false;
	}
	
	public boolean storeConfig(int id, Object userObject, boolean configchanged) {
		return false;
	}

	/**
	 * @return  Returns the timeOut.
	 * @uml.property  name="timeOut"
	 */
	protected int getTimeOut() {
		return timeOut;
	}

	/**
	 * @param timeOut  The timeOut to set.
	 * @uml.property  name="timeOut"
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @return  Returns the host.
	 * @uml.property  name="host"
	 */
	public InetAddress getHost() {
		return host;
	}

	/**
	 * @param host  The host to set.
	 * @uml.property  name="host"
	 */
	protected void setHost(InetAddress host) {
		this.host = host;
	}

	/**
	 * @return  Returns the port.
	 * @uml.property  name="port"
	 */
	public int getPort() {
		return port;
	}
	
	public Exception getLastError() {
		return lasterror;
	}
	
	public boolean isValid() {
		return (lasterror == null);
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public String getID() {
		return nodeID;
	}

	/**
	 * @param h   	host
	 * @param p		port
	 * @return
	 */
	public boolean isAddress(String h, int p) {
		//System.out.println("ddd" + h + " . " + p);
		if( p == port ) {
//			if (initialized) {
//				if(h.equals(hostName)) {
//					return true;
//				}
//			} else {
				if( h.equals(hostIp) || h.equals(hostName)) {
					return true;
				}
//			}
		}
//
//
//				String temp = h + p;
//				
//				// TODO
//			}
//			
//		}
//		if (initialized) {
//			if( p == port ) {
//				if( h.equals(hostIp) || h.equals(hostName)) {
//					return true;
//				}
//			}
//		} else {
//			String temp = h + p;
//			
//			// TODO
//		}
//      NodeAddress na = (NodeAddress)i.next();
//      if( port < 0 ) {
//          return; // allow DNS lookups
//      }
//      if( port == na.port ) {
//          if( host.equals(na.hostIp) || host.equals(na.hostName) ) {
//              return; // host:port is in our list
//          }
//      }            
//
		// TODO Auto-generated method stub
		return false;
	}

	public String getHostIp() {
		return hostIp;
	}

	public String getHostName() {
		return hostName;
	}

	public int getNetworkType() {
		return networkType;
	}

	/**
	 * @param networkType the networkType to set
	 */
	protected void setNetworkType(int networkType) {
		this.networkType = networkType;
	}

}
