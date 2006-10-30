/**
 * 
 */
package hyperocha.freenet.fcp;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author saces
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
	 * @see java.lang.Thread#start(), but dosn't return until the connection is up and ready to use (send)
	 */
	public synchronized void start() {
		super.start();
		while (!isUp) {
			try {
				wait(1500);
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
}
