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
import java.util.List;
import java.util.Vector;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.util.IStorageObject;

/**
 * @author saces
 *
 */
public class Factory implements IStorageObject {
	
	private List networks;  // liste der configuratuon
	
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
		networks = new Vector();
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
			
			int i;
			for (i=0; i < netCount; i++) {
				((Network)networks.get(i)).storeData(dos);
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void addNetwork(Network net) {
		networks.add(net.getID(), net);
	}
	
//	private FCPConnection getNextConnection() {
//		return getNextConnection(Network.HEAD);
//	}
	
//	private FCPConnection getNextConnection5() {
//		return getNextConnection(Network.FCP1);
//	}
	
	private FCPConnection getNextConnection(int networktype) {
		//TODO
		return null;
	}
	
	private Network getDarknet(int type) {
		return null;
	}

	protected void init() {
		int netCount = networks.size();
		int i;
		for (i=0; i < netCount; i++) {
				((Network)networks.get(i)).init();
		}
	}
	
	protected void goOnline() {
		int netCount = networks.size();
		int i;
//		Network net;
		for (i=0; i < netCount; i++) {
			((Network)(networks.get(i))).goOnline();
		}
	}

	protected boolean isOnline() {
		int netCount = networks.size();
		int i;
		//Network net;
		for (i=0; i < netCount; i++) {
			if (((Network)(networks.get(i))).isOnline()) {
				return true;
			}
		}
		return false;
	}

	public boolean isInList(String host, int port) {
		int netCount = networks.size();
		int i;
		Network net;
		for (i=0; i < netCount; i++) {
			if (((Network)(networks.get(i))).isInList(host, port)) {
				return true;
			}
		}
		return false;
	}
}
