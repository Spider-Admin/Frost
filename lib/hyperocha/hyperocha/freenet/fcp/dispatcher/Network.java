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
package hyperocha.freenet.fcp.dispatcher;

import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.FCPNodeConfig;
import hyperocha.util.IStorageObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * @author saces
 *
 */
public class Network implements IStorageObject {
	public static final int FCP1 = 1;  // the fcp1 thingy
	public static final int FCP2 = 2;   // the fcp2 thingy, 
	//public static final int HEAD = FCP2;  // latest from trunk 
	
	private int networkType;
	private int networkID;
	
	private List confList;  // liste der knoetchen-confs
	private List nodeList;  // liste der knoetchen, hello ok

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
	public Network(int networktype) {
		this(networktype, null);
	}
	
	/**
	 * nodelist: eine liste mit strings, jeder string "server:port" fuer jeden server
	 */
	public Network(int networktype, List nodelist ) {
		networkType = networktype;
		confList = new Vector();
		nodeList = new Vector();
		if (nodelist == null) {
			switch (networkType) {
				case FCP1: confList.add(0, new FCPNodeConfig(0, "127.0.0.1:8481")); break;  
				case FCP2: confList.add(0, new FCPNodeConfig(0, "127.0.0.1:9481")); break; 
				default :	System.err.println("Unsopported Darknet type: " + networkType); 
			}
		} else {
			int i;
			for (i = 0; i < nodelist.size(); i++) {
				//System.err.println("HTEST: " + (String)(nodelist.get(i)));
                addNodeConfig((String)(nodelist.get(i)));
            }
		}
	}

	public boolean loadData(DataInputStream dis) {
		int nodeCount;
		try {
			networkID = dis.readInt();
			networkType = dis.readInt();
			nodeCount = dis.readInt();

			int i;
			for (i=0; i < nodeCount; i++) {
				FCPNodeConfig nodeConf = new FCPNodeConfig(dis);
				addNodeConfig(nodeConf);
			}
			
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean storeData(DataOutputStream dos) {
		int nodeCount = confList.size();

		try {
			dos.writeInt(networkID);
			dos.writeInt(networkType);
			dos.writeInt(nodeCount);
			
			int i;
			for (i=0; i < nodeCount; i++) {
				((FCPNodeConfig)confList.get(i)).storeData(dos);
			}
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
	private void addNodeConfig(FCPNodeConfig nodeConf) {
		confList.add(nodeConf.getID(), nodeConf);
	}
	
	private void addNodeConfig(String serverport) {
		//System.err.println("HTEST 001:" + serverport);
		FCPNodeConfig conf = new FCPNodeConfig(confList.size(), serverport);
		addNodeConfig(conf);
	}
	
	public int getID() {
		return networkID;
	}
	
	public boolean isType(int type) {
		return (networkType == type);
	}
	
	public static boolean isTestnet(Network net) {
		return false;
	}

	protected void init() {
		int nodeCount = confList.size();
		int i;
		for (i=0; i < nodeCount; i++) {
			((FCPNodeConfig)confList.get(i)).init();
//			if (((FCPNodeConfig)confList.get(i)).init()) {
//				confList.add(new FCPNode((FCPNodeConfig)confList.get(i)));
//			}
		}
	}

	protected void goOnline() {
		int nodeCount = confList.size();
		int i;
		FCPNodeConfig conf;
		FCPNode node;
		for (i=0; i < nodeCount; i++) {
			conf = (FCPNodeConfig)(confList.get(i));
			
//			if (!conf.isValid()) {
//				conf.init();
//			}
			System.err.println("HEHE: 00");
			if (conf.isInitialized()) {
				System.err.println("HEHE: 01a");
				node = new FCPNode(conf);
				System.err.println("HEHE: 01");
				node.hello();
				if (node.isValid()) {
					System.err.println("HEHE: 01-AH");
					nodeList.add(conf.getID(), node);
				} else {
					System.err.println("HEHE: 01- Nö");
				}
			}
//			if (((FCPNodeConfig)confList.get(i)).init()) {
//				confList.add(new FCPNode((FCPNodeConfig)confList.get(i)));
//			}
		}

	}

	public boolean isOnline() {
		//System.err.println("HEHE: is online: nodelistsize: " + nodeList.size());
		return (nodeList.size() > 0);
	}

	public boolean isInList(String host, int port) {
		int nodeCount = confList.size();
		int i;
		FCPNodeConfig conf;
		//FCPNode node;
		for (i=0; i < nodeCount; i++) {
			conf = (FCPNodeConfig)(confList.get(i));
			if (conf.isAddress(host, port)) {
				return true;
			}
			
		}	

		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
//		isInList
//      List nodes = FcpHandler.inst().getNodes();
//      for(Iterator i=nodes.iterator(); i.hasNext(); ) {
//      NodeAddress na = (NodeAddress)i.next();
//      if( port < 0 ) {
//          return; // allow DNS lookups
//      }
//      if( port == na.port ) {
//          if( host.equals(na.hostIp) || host.equals(na.hostName) ) {
//              return; // host:port is in our list
//          }
//      }            
//  }
		return false;
	}
}
