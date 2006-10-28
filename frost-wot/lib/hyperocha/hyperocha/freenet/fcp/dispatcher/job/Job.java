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
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.IIncoming;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;

import java.util.Hashtable;

/**
 * a job
 */
public abstract class Job implements IIncoming {
	private static final int STATUS_ERROR = -1;
	public static final int STATUS_UNPREPARED = 0;
	public static final int STATUS_PREPARED = 1;
	private static final int STATUS_RUNNING = 2;
	private static final int STATUS_DONE = 3;
	//private static final int STATUS_DONE_SUCCESS = 4;
	private static final int STATUS_STOPPING = 4;
	
	private int requiredNetworkType;
	
	private int status = 0;
	private Exception lastError = null;
	
	private String jobID = null;  // = identifer on fcp 2
	private String clientToken = "hyperochaclienttoken";
	
	
	private final Object waitObject = new Object();
	
	//private PriorityClass priorityClass = null;
	
	protected Job(int requirednetworktype, String id) {
		if (id == null) { throw new Error("Hmmmm"); }
		requiredNetworkType = requirednetworktype;
		jobID = id;
		status = STATUS_UNPREPARED;
	}
	
	private void reset() {
		//cancel(true);
		lastError = null;
		status = STATUS_UNPREPARED;
	}
	
	public Exception getLastError() {
		return lastError;
	}
	
	protected void setError(Exception e) {
		lastError = e;
		status = STATUS_ERROR;
	}
	
	protected void setError(String description) {
		lastError = new Exception(description);
		status = STATUS_ERROR;
		synchronized(waitObject) {
			waitObject.notify();
		}
	}
	
	protected void setSuccess() {
		//lastError = new Exception(description);
//		status = STATUS_DONE;
		synchronized(waitObject) {
			status = STATUS_DONE;
			waitObject.notify();
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	public void prepare() {
		status = STATUS_UNPREPARED;
		if (doPrepare()) {
			status = STATUS_PREPARED;
		}		
	}
	
	public void run(Dispatcher dispatcher) {
		if (status != STATUS_PREPARED) { throw new Error("FIXME: never run an unprepared job!"); }
		status = STATUS_RUNNING;
		switch (requiredNetworkType) {
			case Network.FCP1: runFCP1(dispatcher); break;
			case Network.FCP2: runFCP2(dispatcher); break;
			case Network.SIMULATION: runSimulation(dispatcher); break;
			default: throw (new Error("Unsupported network type or missing implementation."));
		}
		if ((status != STATUS_ERROR) && (lastError == null)) {
			status = STATUS_DONE;
		}
	}
	
	public void runFCP1(Dispatcher dispatcher) {
		throw (new Error("Unsupported network type or missing implementation."));
	}

	public void runFCP2(Dispatcher dispatcher) {
		throw (new Error("Unsupported network type or missing implementation."));
	}
	
	private void runSimulation(Dispatcher dispatcher) {
		throw (new Error("Unsupported network type or missing implementation."));
	}
	
//	public void start() {
//		if (status == STATUS_UNPREPARED) { prepare(); }
//		if (status != STATUS_PREPARED) { return; }
//		
//		// now do the real run
//		
//	}

	public void cancel(boolean hard) {
		status = STATUS_STOPPING;
	}

	public abstract boolean doPrepare(); 
		
//	public abstract void cancel();
//	public abstract void suspend();
//	public abstract void resume();
//	public abstract void panic();

	public int getRequiredNetworkType() {
		return requiredNetworkType;
	}
	
	public boolean isSuccess() {
		return ((status == STATUS_DONE) && (lastError == null));
	}
	
	public void waitFine () {
		synchronized(waitObject) {
			while ((status == STATUS_RUNNING) && (lastError == null)) {
				try {
					//Thread.yield();
					//Thread.sleep(347);
					waitObject.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		}
	}

	/**
	 * @return the jobID
	 */
	public String getJobID() {
		return jobID;
	}
	
	protected String getClientToken() {
		return clientToken;
	}
	
	public void incomingData(String id, Hashtable message, FCPConnection conn) {
		// TODO Auto-generated method stub
		throw new Error("Ha!");
		
	}

	public void incomingMessage(String id, Hashtable message) {
		// TODO Auto-generated method stub
		throw new Error("Hu!");
	}
}
