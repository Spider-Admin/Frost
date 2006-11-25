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
import hyperocha.freenet.fcp.IIncoming;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.NodeMessage;
import hyperocha.freenet.fcp.dispatcher.job.Job;
import hyperocha.freenet.fcp.dispatcher.job.UpdateNodePropertiesJob;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

//import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 * <b>The Dispatcher.</b><br>
 * This class is designed for control fcp commands lika a 
 * 'high level' api.
 * It's supports multiple nodes.. (mehr erz&auml;hle ich euch n&auml;chstes mal)
 * <p>
 * see package overview for details
 * 
 * @author saces
 * @version $Id$
 * 
 *
 */
public class Dispatcher extends Factory implements IIncoming {
	public static final int STATE_ERROR = -1;
	public static final int STATE_UNKNOWN = 0; 
	public static final int STATE_STARTING = 1; 
	public static final int STATE_RUNNING = 2; 
	public static final int STATE_STOPPING = 4;
	public static final int STATE_STOPPED = 8;  
	public static final int STATE_IDLE = 16;
	
	public static final int RUNJOB_NOERROR = 0;
	public static final int RUNJOB_PREPAREFAILED = 1;
	public static final int RUNJOB_OFFLINE = 2;
	public static final int RUNJOB_OVERLOAD = 3;
	public static final int RUNJOB_BROKEN = 4;
	
	private long checkOnlineDelay = 180L; // check every three minutes for offline nodes and try to restart
	private long checkGQueueDelay = 20L; // check every 20 sec the gq

	private Hashtable dummyJobs = new Hashtable();
	private Object readyWaiter = new Object();
	private Hashtable runningJobs = new Hashtable();
	private long startTimeStamp = 0;
	private int state;
	
	private EventListenerList stateListeners = new EventListenerList();
	private long tick = 1; // tricky thing, don't start with 0!
	
	private TimerThread tickTackTicker = null;
	
	
	/**
	 * internal helper
	 *
	 */
	private class DummyJob extends Job {

		private DummyJob(String id) {
			super(-1, id);
		}

		public void incomingData(String id, Hashtable message, FCPConnection conn) {
			// TODO: daten nach /dev/null
			// das sollte eigentlich nicht passieren. die klasse sendet keine reqests. 
			System.out.println("Dummy Daten??? " + id + " -> message: " + message);
			throw new Error();
		}

		/* (non-Javadoc)
		 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incomingMessage(java.lang.String, java.util.Hashtable)
		 */
		public void incomingMessage(String id, Hashtable message) {
			// TODO Auto-generated method stub
			System.out.println("Dummy message " + id + " -> message: " + message);
		}
		
	}
	
	/**
	 * internal ticker
	 *
	 */
	private class TimerThread extends Thread {
        private boolean isCancelled = false;
        public synchronized void cancel() {
            isCancelled = true;
        }
        public synchronized boolean isCancelled() {
            return isCancelled;
        }
        public void run() {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                if( isCancelled() ) {
                    return;
                }
                onTimer();
            }
        }
    }

	/**
	 * Creates a new dispatcher with an empty factory
	 */
	public Dispatcher() {
		super();
	}
    
	public void addDispatcherStateListener( DispatcherStateListener listener ) {
		stateListeners.add( DispatcherStateListener.class, listener );
	}

	/**
	 * add a job to the queue and returns
	 * the balancer start it earlier or later
	 * @param job
	 * @return true on success (added) or false if somthing goes wrong
	 */
	public boolean addJob(Job job) {
		// TODO
		return false;
	}
	
	public void addNetworkStateListener( NetworkStateListener listener ) {
		stateListeners.add( NetworkStateListener.class, listener );
	}

	public void addNodeStateListener( NodeStateListener listener ) {
		stateListeners.add( NodeStateListener.class, listener );
	}
	
	/**
	 * same as panic, but without data loss
	 * state is saved an can be resumed
	 */
	public void freezeDispatcher() {
	}
	
	private DummyJob getDummyJob(String id) {
		DummyJob j = (DummyJob)dummyJobs.get(id);
		if (j == null) {
			j = new DummyJob(id);
			dummyJobs.put(j.getJobID(), j);
		}
		return j;
	}
	
	private Job getRunningJob(String id) {
		//System.out.println("getRunningJob: " + id);
		return (Job)runningJobs.get(id);
	}

	/**
	 * @return returs a list of all id's seen by the dispatcher without corresponding job
	 */
	public ArrayList getUnassignedIDs() {
		return new ArrayList(dummyJobs.keySet());
	}

	private long getuptime() {
		return (System.currentTimeMillis() - startTimeStamp);
	}

	/**
	 * @return the uptime in milliseconds
	 */
	public long getUpTime() {
		// FIXME check if up and return -1 if not up
		// this indicates up: STATE_STARTING STATE_RUNNING STATE_STOPPING STATE_IDLE
		return (System.currentTimeMillis() - startTimeStamp);
	}
	
	private void goOffline() {
		Network net;
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			net =  (Network)(e.nextElement());
			net.goOffline();
	    }
	}
	
	private void goOnline() {
		Network net;
		for (Enumeration e = networks.elements() ; e.hasMoreElements() ;) {
			net =  (Network)(e.nextElement());
			net.goOnline();
	    }
	}
	
	public void incomingData(String id, NodeMessage message, FCPConnection conn) {
		//System.out.println("D Testinger Data: " + message);
		//Job j = getRunningJob((String)message.get("Identifier"));
		Job j = getRunningJob(id);
		if (j == null) { 
			j = getDummyJob(id);
		}
		//if (j == null) { throw new Error("Hmmmm. this shouldnt happen."); }
		j.incomingData(id, message, conn);
//		getRunningJob((String)message.get("Identifier")).incommingData(conn, message);
	}
	
	public void incomingMessage(String id, NodeMessage message) {
		System.out.println("D Testinger id " + id + " -> message: " + message);
		//Job j = getRunningJob((String)message.get("Identifier"));
		Job j = getRunningJob(id);
		if (j == null) { 
			j = getDummyJob(id);
		}
		j.incomingMessage(id, message);
	}
	
	public boolean loadState() {
		return false;	
	}

	private void onTimer() {
		if ( (tick % checkOnlineDelay) == 0 ) {
			goOnline();
		}
		if ( (tick % checkGQueueDelay) == 0 ) {
			checkGQueue();
		}
		tick++;
		//System.out.println("Tick Tack Timer: " + tick);
	}
	
	private void checkGQueue() {
		List l = getAllOnlineNodes(Network.FCP2);
		
		if (l == null) {
			//no fcp2 nodes online, return
			return;
		}
		
		int i;
		for (i = 0; i < l.size(); i++) {
			FCPConnectionRunner r = ((FCPNode)l.get(i)).getDefaultFCPConnectionRunner();
			// hack for parse gq on startup.
			List cmd = new LinkedList();
			cmd.add("WatchGlobal");
			cmd.add("Enabled=true");
			cmd.add("VerbosityMask=-1");
			cmd.add("EndMessage");
			cmd.add("ListPersistentRequests");
			cmd.add("EndMessage");
			r.send(cmd);
		}
		
	}
	
	/**
	 * The panic-button. Neccesary for all freenet apps :)
	 * This function don't return until all is done.
	 * 
	 * <bold>WARNING<bold>
	 * calling this cause data loss.
	 * 
	 *  - cut all runnings jobs (hard cut off)
	 *  - try to remove all nown persistant requests
	 *  - shut down all nodes (if possible)
	 *  - wipe out all dispatchers data
	 *  - at least: do a Kill -9 to itself
	 *  
	 *  in theory, after calling this you have a nearly clean and fresh install :)
	 *   
	 * @param clearqueue try to clear the global queue
	 */
	public void panic(boolean clearqueue) {
		// TODO:
		// Stop jobmanager
		// Cancel all active connections (Perstance=Connection)
		// ? clear all queues (Perstance=reboot/forever)
		// disconnect all
	}
	
	private void registerJob(Job job) {
		registerJob(job.getJobID(), job);
	}

	public void registerJob(String id, Job job) {
		runningJobs.put(id, job);
	}
	
	public void removeDispatcherStateListener( DispatcherStateListener listener ) {
		stateListeners.remove( DispatcherStateListener.class, listener );
	}
	
	/**
	 * remove the job or the transfer (id without corrospending job) 
	 * @param id jobid or Indentifer
	 * @return false of not removed
	 */
	public boolean removeID(String id) {
		return false;
	}

	public void removeNetworkStateListener( NetworkStateListener listener ) {
		stateListeners.remove( NetworkStateListener.class, listener );
	}
	
	public void removeNodeStateListener( NodeStateListener listener ) {
		stateListeners.remove( NodeStateListener.class, listener );
	}
	
	/**
	 *  run a job and do not return until the job is done.
	 * @param job
	 * @return int - error code from dispatcher
	 */
	public int runJob(Job job) {
		if (state == STATE_IDLE) {
			setState(STATE_RUNNING);
		}
		
		if (state != STATE_RUNNING) {
			return RUNJOB_OFFLINE;
		}
		
		if (job.getStatus() == Job.STATUS_UNPREPARED) {
			//System.out.println(job.getJobID() + " -> job unprepared. prepare it");
			job.prepare(); 
		}
		if (job.getStatus() != Job.STATUS_PREPARED) {
			//System.err.println(job.getJobID() + " - " + job + " -> job unprepared, but should. return without execution");
			return RUNJOB_PREPAREFAILED;
		}
		
		boolean resume = false;
		if (dummyJobs.containsKey(job.getJobID())) {
			dummyJobs.remove(job.getJobID());
			resume = true;
		}
		registerJob(job);
		job.run(this, true, resume);
		//System.err.println("Job done:" + job.getJobID() + " -> " + job);
		unregisterJob(job);
		return RUNJOB_NOERROR;
	}
	
	public boolean saveState() {
		return false;	
	}

	private void setState( int newState ) {
		state = newState;
		
		if (state == STATE_RUNNING) {
			synchronized (readyWaiter) {
				readyWaiter.notifyAll();
			}
		}
		DispatcherStateEvent e = new DispatcherStateEvent(this, newState);
	     // Guaranteed to return a non-null array
		Object[] listeners = stateListeners.getListenerList();
	    // Process the listeners last to first, notifying
	    // those that are interested in this event
	    for (int i = listeners.length-2; i>=0; i-=2) {
	    	if (listeners[i]==DispatcherStateListener.class) {
	    		((DispatcherStateListener)listeners[i+1]).stateChanged(e);
	    	}
	    }
	 }
	
	/**
	 * Fires up the dispatcher and returns immediately.<br>
	 * Does nothing if the dispatcher is already up.
	 */
	public synchronized void startDispatcher() {
		if (tickTackTicker != null) { return; }
		startTimeStamp = System.currentTimeMillis();
		setState(STATE_STARTING);
		// now the first online & properties check
		goOnline();
		// start gq monitoring
		checkGQueue();
		// start ticker
		tickTackTicker = new TimerThread();
		tickTackTicker.start();
		
		setState(STATE_RUNNING);
	}
	
	/**
	 * clean and soft shutdown. complete running jobs (until the next checkpoint,
	 * if they have one), but don't start new ones
	 * does nothing if the dispatcher is already down
	 * 
	 */
	public synchronized void stopDispatcher() {
		if (tickTackTicker == null) { return; }
		setState(STATE_STOPPING);
		tickTackTicker.cancel();
		tickTackTicker = null;
		
		goOffline();
		
		// TODO hier
		setState(STATE_STOPPED);
	}
	
	private void unregisterJob(Job job) {
		// FIXME: remove all ids assigned to this job
		runningJobs.remove(job.getJobID());		
	}

	/* (non-Javadoc)
	 * @see hyperocha.freenet.fcp.dispatcher.Factory#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		Object[] listeners = stateListeners.getListenerList();
		if ( o instanceof FCPNode) {
			if ( arg instanceof Integer) {
				NodeStateEvent e = new NodeStateEvent(this, ((FCPNode)o).getID(), ((Integer)arg).intValue());
				if (e.getNewState() == FCPNode.STATUS_ONLINE) {
					// node have helo ok, test properties
					UpdateNodePropertiesJob job = new UpdateNodePropertiesJob((FCPNode)o);
					runJob(job);
				}
				// Process the listeners last to first, notifying
				// those that are interested in this event
				for (int i = listeners.length-2; i>=0; i-=2) {
					if (listeners[i]==NodeStateListener.class) {
						((NodeStateListener)listeners[i+1]).stateChanged(e);
					}
				}
		    }
		    return;
		}
		if ( o instanceof Network) {
			NetworkStateEvent e = new NetworkStateEvent(this, ((Network)o).getID(), ((Integer)arg).intValue());
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==NetworkStateListener.class) {
					((NetworkStateListener)listeners[i+1]).stateChanged(e);
				}
			}
			return;
	    }
		
		// monitor all unhandled for debugging
		System.err.println("D<<Observer notify!" + o);
		System.err.println("D<<Observer notify!" + arg);
	}

}
