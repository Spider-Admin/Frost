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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.Network;
import hyperocha.util.IStorageObject;

/**
 * @author sa
 *
 */
public class Factory implements IStorageObject {
	
	private Hashtable networks;  // liste der configuratuon
	
	//private NodeList nodes; // liste der knoten, die online sind (hello ok)
	
//	private class NodeList {
//		
//		private List nodesByNetwork;
//		
//		private NodeList() {
//			
//		}
//		
//	}
//	
//	private class NodeListItem {
//		private NodeListItem() {
//		}
//	}
	
	private Balancer balancer;
	
	public Factory() {
		networks = new Hashtable();
//		nodes = new NodeList();
	}
	
	public Factory(DataInputStream dis) {
		this();
		loadData(dis);
	}
	
	public boolean loadData(DataInputStream dis) {
		int netCount;
		try {
			netCount = dis.readInt();

			int i;
			for (i=0; i < netCount; i++) {
				Network net = new Network(dis);
				addNetwork(net);
			}
			
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean storeData(DataOutputStream dos) {
		int netCount = networks.size();

		try {
			dos.writeInt(netCount);
			
			for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
				((Network)(e.nextElement())).storeData(dos);

		    }
//			int i;
//			for (i=0; i < netCount; i++) {
//				((Network)networks.get(i)).storeData(dos);
//			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void addNetwork(Network net) {
		networks.put(net.getID(), net);
	}
	
//	private FCPConnection getNextConnection() {
//		return getNextConnection(Network.HEAD);
//	}
	
//	private FCPConnection getNextConnection5() {
//		return getNextConnection(Network.FCP1);
//	}
	
	protected FCPConnection getNewFCPConnection(int networktype) {
		Network net = getFirstNetwork(networktype);
		//throw new Error("TODO");
		return net.getNewFCPConnection();
	}
	
	protected FCPConnection getDefaultFCPConnection(int networktype) {
		Network net = getFirstNetwork(networktype);
		//throw new Error("TODO");
		return net.getDefaultFCPConnection();
	}

	
	private Network getFirstNetwork(int type) {
		Network net;
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			net = (Network)(e.nextElement());
			if	(net.isType(type)) {
				return net;
			}
		}
//		int netCount = networks.size();
//		int i;
//		
//		for (i=0; i < netCount; i++) {
//			net = (Network)(networks.get(i));
//			if	(net.isType(type)) {
//				return net;
//			}
//		}
		throw new Error("no network aviable for type: " + type);
		//return null;
	}

	protected void init() {
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			((Network)(e.nextElement())).init();
		}
//
//		int netCount = networks.size();
//		int i;
//		for (i=0; i < netCount; i++) {
//				((Network)networks.get(i)).init();
//		}
	}
	
	protected void goOnline() {
//		int netCount = networks.size();
//		int i;
//		Network net;
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			((Network)(e.nextElement())).goOnline();

	     }
//		for (i=0; i < netCount; i++) {
//			((Network)(networks.get(i))).goOnline();
//		}
	}

	protected boolean isOnline() {
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			if (((Network)(e.nextElement())).isOnline()) {
				return true;
			}

	     }
//		int netCount = networks.size();
//		int i;
//		//Network net;
//		for (i=0; i < netCount; i++) {
//			if (((Network)(networks.get(i))).isOnline()) {
//				return true;
//			}
//		}
		return false;
	}

	public boolean isInList(String host, int port) {
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			if (((Network)(e.nextElement())).isInList(host, port)) {
				return true;
			}

	     }
		return false;
	}
}
