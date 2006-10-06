/**
 * 
 */
package hyperocha.freenet.fcp.dispatcher.job;

import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.Dispatcher;

/**
 * a job
 * 
 *
 */
public abstract class Job {
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
	private String clientToken = "clientToken";
	
	//private PriorityClass priorityClass = null;
	
	protected Job(int requirednetworktype, String id) {
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
		//System.err.println("Run");
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


	
	public void start() {
		if (status == STATUS_UNPREPARED) { prepare(); }
		if (status != STATUS_PREPARED) { return; }
		
		// now do the real run
		
	}

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

	/**
	 * @return the jobID
	 */
	public String getJobID() {
		return jobID;
	}
	
	protected String getClientToken() {
		return clientToken;
	}
}
