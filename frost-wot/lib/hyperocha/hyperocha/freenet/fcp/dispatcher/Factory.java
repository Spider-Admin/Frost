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


import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPConnectionRunner;
import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.Network;
import hyperocha.util.IStorageObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @version $Id$
 */
public class Factory implements IStorageObject, Observer {
	
	protected Hashtable networks;  // liste der configuratuon
	
	public Factory() {
		networks = new Hashtable();
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
		net.addObserver(this);
		networks.put(net.getID(), net);
	}
	
	/**
	 * returns the best next node for this network type
	 * @param networktype
	 * @return FCPNode
	 */
	public FCPNode getNextNode(int networktype) {
		Network net = getFirstNetwork(networktype);
		return net.getNextNode();
	}
	
	protected FCPConnection getNewFCPConnection(int networktype) {
		Network net = getFirstNetwork(networktype);
		//throw new Error("TODO");
		return net.getNewFCPConnection();
	}
	
	public FCPConnectionRunner getDefaultFCPConnectionRunner(int networktype) {
		Network net = getFirstNetwork(networktype);
		//throw new Error("TODO");
		return net.getDefaultFCPConnectionRunner();
	}
	
	public FCPConnectionRunner getNewFCPConnectionRunner(int networktype, String id) {
		Network net = getFirstNetwork(networktype);
		//throw new Error("TODO");
		return net.getNewFCPConnectionRunner(id);
	}

	
	private Network getFirstNetwork(int type) {
		Network net;
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			net = (Network)(e.nextElement());
			if	(net.isType(type)) {
				return net;
			}
		}
		// DEBUG
		throw new Error("DEBUG: no network aviable for type: " + type);
		//return null;
	}

	/**
	 * is the host:port in our nodelist?
	 * needed for security manager
	 * @param host
	 * @param port
	 * @return true, if the host:port is found in the node list, otherwise false 
	 */
	public boolean isInList(String host, int port) {
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			if (((Network)(e.nextElement())).isInList(host, port)) {
				return true;
			}

	     }
		return false;
	}

	public List getAllNodes(int networktype) {
		List l = new LinkedList();
		List n = getNetworks(networktype); 
		if (n == null) {
			// no networks of this type aviable, return nix
			return null;
		}
		
		int i;
		for (i = 0; i < n.size(); i++) {
			l.addAll(((Network)n.get(i)).getNodes());
        }
		return l;
	}
	
	public List getAllOnlineNodes(int networktype) {
		List l = new LinkedList();
		List n = getNetworks(networktype); 
		if (n == null) {
			// no networks of this type aviable, return nix
			return null;
		}
		
		int i;
		for (i = 0; i < n.size(); i++) {
			l.addAll(((Network)n.get(i)).listOnlineNodes());
        }
		return l;
	}
	
	private List getNetworks(int networktype) {
		List l = new LinkedList();
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			Network n = (Network)(e.nextElement());
			if (n.isType(networktype)) {
				l.add(n);
			}
		}
		
		if (l.isEmpty()) {
			return null;
		}
		return l;
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		System.err.println("Observer notify!" + o);
		System.err.println("Observer notify!" + arg);
		//throw new Error();
	}
}
