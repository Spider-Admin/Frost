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

import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.FCPNodeConfig;
import hyperocha.util.IStorageObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * @author saces
 *
 */
public class Network implements IStorageObject {
	//	 the fcp1 thingy
	public static final int FCP1 = 1;
	//	 the fcp2 thingy,
	public static final int FCP2 = 2;
	//	 simulate the execution of commands, not the fcp thingy
	public static final int SIMULATION = 254; 
	
	private int networkType;
	private String networkID;
	
//	private Hashtable confList;  // liste der knoetchen-confs
	private Hashtable nodeList;  // liste der knoetchen 
	
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
	 */
	public Network(int networktype, String id) {
		this(networktype, id, null);
	}
	
	/**
	 * nodelist: eine liste mit strings, jeder string "server:port" fuer jeden server
	 */
	public Network(int networktype, String id, List nodelist ) {
		networkType = networktype;
		networkID = id;
//		confList = new Hashtable();
		nodeList = new Hashtable();
		if (nodelist == null) {
			switch (networkType) {
				case FCP1: addNode("<default fcp1>", "127.0.0.1:8481"); break;  
				case FCP2: addNode("<default fcp2>", "127.0.0.1:9481"); break; 
				default :	throw new Error("Unsopported network type: " + networkType); 
			}
		} else {
			int i;
			for (i = 0; i < nodelist.size(); i++) {
				//System.err.println("HTEST: " + (String)(nodelist.get(i)));
                addNode(id+"-<"+i+">", (String)(nodelist.get(i)));
            }
		}
	}

	public boolean loadData(DataInputStream dis) {
		int nodeCount;
		try {
			networkID = dis.readUTF();
			networkType = dis.readInt();
			nodeCount = dis.readInt();

			int i;
			for (i=0; i < nodeCount; i++) {
				FCPNodeConfig conf = new FCPNodeConfig(dis);
				FCPNode node = new FCPNode(conf);
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
		nodeList.put(node.getID(), node);
	}
	
	private void addNode(String id, String serverport) {
		//System.err.println("HTEST 001:" + serverport);
		FCPNodeConfig conf = new FCPNodeConfig(networkType, id, serverport);
		FCPNode node = new FCPNode(conf);
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

	public void init() {
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			((FCPNode)(e.nextElement())).init();
	     }
	}

	public void goOnline() {
//		int nodeCount = confList.size();
//		int i;
//		FCPNodeConfig conf;
//		FCPNode node;
		
		for (Enumeration e = nodeList.elements() ; e.hasMoreElements() ;) {
			((FCPNode)(e.nextElement())).init();
	    }

		
		
//		for (i=0; i < nodeCount; i++) {
//			conf = (FCPNodeConfig)(confList.get(i));
//			conf.init();
////			if (!conf.isValid()) {
////				conf.init();
////			}
////			System.err.println("HEHE: 00");
//			if (conf.isInitialized()) {
////				System.err.println("HEHE: 01a");
//				node = new FCPNode(conf);
////				System.err.println("HEHE: 01");
//				node.hello();
//				if (node.isValid()) {
////					System.err.println("HEHE: 01-AH");
//		//			nodeList.put(conf.getID(), node);
//				} else {
//					System.err.println("HEHE: 01- Nö");
//				}
//			}
////			if (((FCPNodeConfig)confList.get(i)).init()) {
////				confList.add(new FCPNode((FCPNodeConfig)confList.get(i)));
////			}
//		}

	}

	public boolean isOnline() {
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
		return getNextNode().getNewFCPConnection(null);
	}
	
	public FCPConnection getDefaultFCPConnection() {
		if (networkType != Network.FCP2) { return getNewFCPConnection(); }
		return getNextNode().getDefaultFCPConnection();
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
	
	private FCPNode getNextRandomNode() {
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
}
