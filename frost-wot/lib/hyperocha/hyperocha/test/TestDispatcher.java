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
package hyperocha.test;

import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;
import hyperocha.freenet.fcp.dispatcher.DispatcherStateEvent;
import hyperocha.freenet.fcp.dispatcher.DispatcherStateListener;
import hyperocha.freenet.fcp.dispatcher.NetworkStateEvent;
import hyperocha.freenet.fcp.dispatcher.NetworkStateListener;
import hyperocha.freenet.fcp.dispatcher.NodeStateEvent;
import hyperocha.freenet.fcp.dispatcher.NodeStateListener;

/**
 * @author saces
 * @version $Id$
 *
 */
public class TestDispatcher implements DispatcherStateListener, NetworkStateListener, NodeStateListener {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestDispatcher td = new TestDispatcher();
		
		Dispatcher dispatcher = new Dispatcher();
		
		dispatcher.addDispatcherStateListener(td);
		dispatcher.addNetworkStateListener(td);
		dispatcher.addNodeStateListener(td);
		
		Network net1 = new Network(Network.FCP1, "testDispatcher.5");
		Network net2 = new Network(Network.FCP2, "testDispatcher.7");
		
		net1.addNode("testNode5", "127.0.0.1:8481", dispatcher);
		net2.addNode("testNode7", "127.0.0.1:9481", dispatcher);
		
		dispatcher.addNetwork(net1);
		dispatcher.addNetwork(net2);
		
		System.out.println("Starting test...");
		dispatcher.startDispatcher();
		
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		dispatcher.stopDispatcher();
		
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("Test done.");
	}

	public void stateChanged(DispatcherStateEvent e) {
		// System.out.println("Disp State changed: " + e.getNewState() + " -> " + e);
		System.out.println("Disp State changed: " + getDispatcherStateName(e.getNewState()));
	}
	
	public void stateChanged(NetworkStateEvent e) {
		// System.out.println("Net State changed: " + e.getNetworkID() + "; " + e.getNewState() + " -> " + e);
		System.out.println("Network '" + e.getNetworkID() + "' is now " + getNetworkStateName(e.getNewState()));
	}

	public void stateChanged(NodeStateEvent e) {
		//System.out.println("Node State changed: " + e.getNodeID() + "; " + e.getNewState() + " -> " + e);
		System.out.println("Node '" + e.getNodeID() + "' is now " + getNodeStateName(e.getNewState()));
	}

	private String getDispatcherStateName(int state) {
		String s;
		switch (state) {
			case Dispatcher.STATE_ERROR : s = "buaah! buaa!"; break;
			case Dispatcher.STATE_UNKNOWN : s = "unknown"; break;
			case Dispatcher.STATE_STARTING : s = "starting"; break;
			case Dispatcher.STATE_RUNNING : s = "running"; break;
			case Dispatcher.STATE_STOPPING : s = "shuting down"; break;
			case Dispatcher.STATE_STOPPED : s = "stopped"; break;
			case Dispatcher.STATE_IDLE : s = "zzZZzz"; break;
			default :  s = "Hmmm!?!?!?";
		}
		return s;
	}

	private String getNetworkStateName(int state) {
		String s;
		switch (state) {
			case Network.STATUS_ERROR : s = "buaah! buaa!"; break;
			case Network.STATUS_OFFLINE : s = "offline."; break;
			case Network.STATUS_ONLINE : s = "online."; break;
			default :  s = "Hmmm!?!?!?";
		}
		return s;
	}
	
	private String getNodeStateName(int state) {
		String s;
		switch (state) {
			case FCPNode.STATUS_ERROR : s = "buaah! buaa!"; break;
			case FCPNode.STATUS_OFFLINE : s = "offline."; break;
			case FCPNode.STATUS_REACHABLE : s = "reachable. (helo not testet)"; break;
			case FCPNode.STATUS_ONLINE : s = "Helo ok."; break;
			default :  s = "Hmmm!?!?!?";
		}
		return s;	
	}

}
