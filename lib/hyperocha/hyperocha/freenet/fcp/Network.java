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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * <p>(Network means a logical group of nodes.)
 * <p><b>Setup</b><br>
 * 
 * @author saces
 * @version $Id$
 *
 */
public class Network extends Observable implements IStorageObject, Observer {
	public static final int STATUS_ERROR = -1;
	public static final int STATUS_OFFLINE = 0;
	public static final int STATUS_ONLINE = 1;
	
	public int status = STATUS_OFFLINE;

	//	 the fcp1 thingy
	public static final int FCP1 = 1;
	//	 the fcp2 thingy,
	public static final int FCP2 = 2;
	//	 simulate the execution of jobs, not the fcp thingy
	public static final int SIMULATION = 254; 
	
	private int networkType;
	private String networkID;
	
//	private Hashtable confList;  // liste der knoetchen-confs
	private Hashtable nodeList = new Hashtable();  // liste der knoetchen 
	
	private Random random = new Random();
	
	
	public Network(DataInputStream dis) {
		loadData(dis); 
	}

	/**
	 * 
	 */
//	public Network() {
//		this(HEAD, null);
//	}

	/**
	 * 
	 * @param networktype
	 * @param id
	 */
	public Network(int networktype, String id) {
		networkType = networktype;
		networkID = id;
	}
	
	/**
	 * nodelist: eine liste mit strings, jeder string "server:port" fuer jeden server
	 */
//	public Network(int networktype, String id, List nodelist, IIncoming callback) {
//		this(networktype, id);
////		confList = new Hashtable();
////		nodeList = new Hashtable();
//		if (nodelist == null) {
//			switch (networkType) {
//				case FCP1: addNode("<default fcp1>", "127.0.0.1:8481", callback); break;  
//				case FCP2: addNode("<default fcp2>", "127.0.0.1:9481", callback); break; 
//				default :	throw new Error("Unsopported network type: " + networkType); 
//			}
//		} else {
//			int i;
//			for (i = 0; i < nodelist.size(); i++) {
//				//System.err.println("HTEST: " + (String)(nodelist.get(i)));
//                addNode(id+"-<"+i+">", (String)(nodelist.get(i)), callback);
//            }
//		}
//	}

	public boolean loadData(DataInputStream dis) {
		int nodeCount;
		try {
			networkID = dis.readUTF();
			networkType = dis.readInt();
			nodeCount = dis.readInt();

			int i;
			for (i=0; i < nodeCount; i++) {
				// TODO
				//FCPNodeConfig conf = new FCPNodeConfig(dis);
				FCPNode node = null; //new FCPNode(conf);
				addNode(node);
			}
			
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean storeData(DataOutputStream dos) {
		//int nodeCount = confList.size();

		try {
			dos.writeUTF(networkID);
			dos.writeInt(networkType);
			//dos.writeInt(nodeCount);
			
//			int i;
//			for (i=0; i < nodeCount; i++) {
//				((FCPNodeConfig)confList.get(i)).storeData(dos);
//			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
//	public FCPNodeConfig getNodeConfig() {
//		if (nodes == null) {
//			nodes  = new Vector();
//			
//		}
//		return null;//defaultNodeConfig;
//	}
	
//	private int addNodeConfig(String serverport) {
//		int index = nodes.size();
//		FCPNodeConfig conf =new FCPNodeConfig(0, "127.0.0.1:8481");
//	}
//	
	private void addNode(FCPNode node) {
		node.addObserver(this);
		nodeList.put(node.getID(), node);
	}
	
	public void addNode(String id, String serverport, IIncoming callback) {
		//System.err.println("HTEST 001:" + serverport);
		FCPNode node = new FCPNode(networkType, id, serverport, callback);
		addNode(node);
	}
	
	public String getID() {
		return networkID;
	}
	
	public boolean isType(int type) {
		return (networkType == type);
	}
	
	public static boolean isTestnet(Network net) {
		return false;
	}
	
	public static String getNetworkTypeName(int nt) {
		switch (nt) {
		case FCP1: return "FCP 1.0"; 
		case FCP2: return "FCP 2.0";
		case SIMULATION: return "Simulation";
		}
		return "unknown";
	}

//	public void init() {
//		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
//			((FCPNode)(e.nextElement())).init();
//	     }
//	}

	public void goOnline() {
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			((FCPNode)(e.nextElement())).goOnline();
	    }
	}

	public boolean isOnline() {
		// TODO
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			((FCPNode)(e.nextElement())).isOnline();
	    }

		//System.err.println("HEHE: is online: nodelistsize: " + nodeList.size());
		return (nodeList.size() > 0);
	}

	public boolean isInList(String host, int port) {
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			if (((FCPNode)(e.nextElement())).isAddress(host, port)) {
				return true;				
			}
	    }
		return false;
	}
	
	public FCPConnection getNewFCPConnection() {
		return getNextNode().getNewFCPConnection();
	}
	
	public FCPConnectionRunner getDefaultFCPConnectionRunner() {
		//if (networkType != Network.FCP2) { return getNewFCPConnection(); }
		return getNextNode().getDefaultFCPConnectionRunner();
	}
	
	public FCPConnectionRunner getNewFCPConnectionRunner(String id) {
		//if (networkType != Network.FCP2) { return getNewFCPConnection(); }
		return getNextNode().getNewFCPConnectionRunner(id);
	}

	public FCPNode getNextNode() {
		// TODO 
		// if balancer == null - no balancer assigned
		return getNextRandomNode();
		// else 
		//    sort with black magic rules
		//    return first from list 
		//throw new Error("TODO");
		//return null;
	}
	
	private synchronized FCPNode getNextRandomNode() {
		//System.err.println("XxX");
		int size = nodeList.size();
		FCPNode node = null;
		Collection keys = nodeList.values();
		Object[] o = keys.toArray();   // grosse theorie: das sollte jetzt ein array mit nodes sein

        if(size == 0) {
            throw new Error("All connections to nodes failed. Check your network settings and restart Frost.");
        } else if( size == 1 ) {
            node = (FCPNode)o[0];
        } else {
            node = (FCPNode)o[random.nextInt(size)];
        }
        return node;
	}

	public Collection getNodes() {
		return nodeList.values();
	}
	
	public List listOfflineNodes() {
		List l = new LinkedList();
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			FCPNode n = (FCPNode)(e.nextElement());
			if (n.isOffline()) {
				l.add(n);
			}
		}
		
		if (l.isEmpty()) {
			return null;
		}
		return l;
	}
	
	public List listOnlineNodes() {
		List l = new LinkedList();
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			FCPNode n = (FCPNode)(e.nextElement());
			if (n.isOnline()) {
				l.add(n);
			}
		}
		
		if (l.isEmpty()) {
			return null;
		}
		return l;
	}

	public void update(Observable o, Object arg) {
		if ( listOnlineNodes() == null ) {
			setStatus(STATUS_OFFLINE);
			return;
		}
		setStatus(STATUS_ONLINE);
		//System.err.println("||Observer notify!" + o);
		//System.err.println("||Observer notify!" + arg);
	}
	
	private void setStatus(int newStatus) {
		if ( status == newStatus ) { return; }
		setChanged();
		status = newStatus;
		notifyObservers(new Integer(newStatus));
	}

	/* (non-Javadoc)
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	public synchronized void addObserver(Observer o) {
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			FCPNode n = (FCPNode)(e.nextElement());
			n.addObserver(o); 
		}
		super.addObserver(o);
	}
}
