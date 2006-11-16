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
		
		dispatcher.startDispatcher();
		
		System.out.println("gestarted");
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dispatcher.stopDispatcher();
		System.out.println("gestoppt");

	}

	public void stateChanged(DispatcherStateEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Disp State changed: " + e.getNewState() + " -> " + e);
	}

	public void stateChanged(NetworkStateEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Net State changed: " + e.getNetworkID() + "; " + e.getNewState() + " -> " + e);
	}

	public void stateChanged(NodeStateEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Node State changed: " + e.getNodeID() + "; " + e.getNewState() + " -> " + e);
	}

}
