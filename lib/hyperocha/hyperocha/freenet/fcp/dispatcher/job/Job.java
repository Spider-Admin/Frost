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
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;


/**
 * a job
 * @version $Id$
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
	
	private int status = STATUS_UNPREPARED;
	private Throwable lastError = null;
	private NodeMessage lastErrorMessage = null;
	
	private String jobID = null;  // = identifer on fcp 2
	private String clientToken = "hyperochaclienttoken";
	
	private final Object waitObject = new Object();

	private long jobstarted = 0;
	private long jobfinished = 0;
	
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
	
	/**
	 * @return the last occured error
	 */
	public Throwable getLastError() {
		return lastError;
	}
	
	protected void setError(Exception e) {
		lastError = e;
		status = STATUS_ERROR;
		synchronized(waitObject) {
			waitObject.notifyAll();
		}
	}
	
	protected void setError(String description) {
		setError(new Exception(description));
	}
	
	protected void setSuccess() {
		//lastError = new Exception(description);
//		status = STATUS_DONE;
		synchronized(waitObject) {
			status = STATUS_DONE;
			waitObject.notifyAll();
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	public final void prepare() {
		status = STATUS_UNPREPARED;
		boolean b = false;
        try {
            b = doPrepare();
        } catch(Throwable t) {
            // TODO: log error?
        }
		if (b) {
			status = STATUS_PREPARED;
		} else {
			if (status != STATUS_ERROR) {
				setError("Prepare Failed!");
			}
		}
	}
	
	public final void run(Dispatcher dispatcher, boolean block, boolean resume) {
		if (status != STATUS_PREPARED) { throw new Error("FIXME: never run an unprepared job!"); }
		status = STATUS_RUNNING;
        
		jobstarted = System.currentTimeMillis();
        try {
            jobStarted(); // notify subclasses that job started
        } catch(Throwable t) {
            // TODO: log error?
        }

        // don't die for any reason
        //try {
    		switch (requiredNetworkType) {
    			case Network.FCP1: runFCP1(dispatcher, resume); break;
    			case Network.FCP2: runFCP2(dispatcher, resume); break;
    			case Network.SIMULATION: runSimulation(dispatcher, resume); break;
    			default: throw (new Error("Unsupported network type or missing implementation."));
    		}
        	if (block) {
        		waitFine();
        	}
//    		if ((status != STATUS_ERROR) && (lastError == null)) {
//    			status = STATUS_DONE;
//    		}
//        } catch(Throwable t) {
//            // TODO: log error?
//            status = STATUS_ERROR;
//            lastError = t;
//        }
//        
		jobfinished = System.currentTimeMillis();
        try {
            jobFinished(); // notify subclasses that job finished
        } catch(Throwable t) {
            // TODO: log error?
        }
	}
	
	public void runFCP1(Dispatcher dispatcher, boolean resume) {
		throw (new Error("Unsupported network type or missing implementation." + this));
	}

	public void runFCP2(Dispatcher dispatcher, boolean resume) {
		throw (new Error("Unsupported network type or missing implementation." + this));
	}
	
	public void runSimulation(Dispatcher dispatcher, boolean resume) {
		throw (new Error("Unsupported network type or missing implementation." + this));
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

	
	/**
	 * overwrite this
	 * @return bool if succes
	 */
	public boolean doPrepare() {
		return true;
	}
		
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
					waitObject.wait();
				} catch (InterruptedException e) {
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
	
	/** 
	 * The default handler. 
	 * skip the incoming data 
	 */
	public void incomingData(String id, NodeMessage msg, FCPConnection conn) {
		// the defaulthandler skip the data
		if (msg.isMessageName("AllData")) { // FCP 2
			long size = msg.getLongValue("DataLength"); 
			conn.skip(size);
			return;
		}
		if (msg.isMessageName("DataChunk")) { // FCP 1
			long size = msg.getLongValue("Length", 16); 
			conn.skip(size);
			return;
		}
	}

	/** 
	 * The default handler. you schould allways super to this. 
	 */
	public void incomingMessage(String id, NodeMessage msg) {
		if (msg.isMessageName("SimpleProgress")) {
            final boolean isFinalized = msg.getBoolValue("FinalizedTotal");
            final long totalBlocks = msg.getLongValue("Total");
            final long requiredBlocks = msg.getLongValue("Required");
            final long doneBlocks = msg.getLongValue("Succeeded");
            final long failedBlocks = msg.getLongValue("Failed");
            final long fatallyFailedBlocks = msg.getLongValue("FatallyFailed");
            
			try {
				onSimpleProgress(isFinalized, totalBlocks, requiredBlocks, doneBlocks, failedBlocks, fatallyFailedBlocks);
			} catch (Exception e) {
				// TODO silence? log?
			}
            return;
		}
		
		if (msg.isMessageName("ProtocolError")) {
			lastErrorMessage = msg;
			boolean goon = false;
			try {
				goon = onProtocolError();
			} catch (Exception e) {
				// TODO silence? log?
			}
			if (!goon) {
				setError("ProtocolError");
				return;
			}
		}
	}
	
    /**
     * Overwrite this to get notified if a protocoll error occures.
     * @return true - the problem is resolved by your implementation and the job stays running; false - the job fails.
     */
	public boolean onProtocolError() {
		return false;
	}

	/**
	 * @return the start timestamp - System.currentTimeMillis();
	 */
	public long getJobStartedMillis() {
		return jobstarted;
	}
	
	/**
	 * @return the finished timestamp - System.currentTimeMillis();
	 */
	public long getJobFinishedMillis() {
		return jobfinished ;
	}

	/**
	 * @return the execution time in milli sec
	 */
	public long getJobDurationMillis() {
        if( jobfinished <= 0 ) {
            // not yet finished, return current duration
            return System.currentTimeMillis() - jobstarted;
        } else {
            return (jobfinished - jobstarted);
        }
	}
    
    /**
     * @return  true if job is started
     */
    public boolean isStarted() {
        return jobstarted > 0;
    }

    /**
     * @return  true if job is finished
     */
    public boolean isFinished() {
        return jobfinished > 0;
    }

    /**
     * Overwrite this to get notified if the job was actually started.
     * The default implementation does nothing.
     */
    public void jobStarted() { }

    /**
     * Overwrite this to get notified if the job was finished.
     * The default implementation does nothing.
     */
    public void jobFinished() { }
    
    /**
     * Overwrite this to get notified if the job progress was changed.
     * The default implementation does nothing.
     */
    public void onSimpleProgress(boolean isFinalized, long totalBlocks, long requiredBlocks, long doneBlocks, long failedBlocks, long fatallyFailedBlocks) { }

	/**
	 * @return the lastErrorMessage
	 */
	public NodeMessage getLastErrorMessage() {
		return lastErrorMessage;
	}

}
