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

import java.io.InputStream;
import java.util.List;

/**
 * @author saces
 * @version $Id$
 *
 */
public class FCPConnectionRunner extends Thread {
	
	private FCPNode fcpNode;
	private FCPConnection conn;
	private String connID;
	private IIncoming callBack;
	
	private volatile boolean isUp = false;
	
	/**
	 * 
	 */
	public FCPConnectionRunner(FCPNode node, String id, IIncoming callback) {
		super(id);
		this.connID = id;
		this.fcpNode = node;
		this.callBack = callback;
	}
	
	public synchronized void send(List command) {
		conn.start(command);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		conn = fcpNode.getNewFCPConnection(callBack, connID);

		isUp = true;
		conn.startMonitor(callBack);
	}
	
	
	/** 
	 * @see java.lang.Thread#start()
	 * but dosn't return until the connection is up and ready to use (send)
	 */
	public synchronized void start() {
		super.start();
		while (!isUp) {
			try {
				wait(500);
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void send(List cmd, long l, InputStream s) {
		conn.start(cmd, l, s);
	}

	public boolean haveDDA() {
		return fcpNode.haveDDA();
	}
	
	public synchronized void close() {
		conn.close();
	}

	public boolean haveGQ() {
		return fcpNode.haveGQ();
	}
	
}
