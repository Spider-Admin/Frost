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
import hyperocha.freenet.fcp.IIncomming;
import hyperocha.freenet.fcp.dispatcher.job.Job;

import java.io.DataInputStream;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

/**
 * @author saces
 *
 */
public class Dispatcher implements IIncomming {
	

		
	
//	private MessageCallback messageCallback = new MessageCallback(); 
	
	private Thread tickTackTicker = null;
	
	private Factory factory;
	
	
	// networkid balancer
	//private List balancer;

	
	//listen:
	private Hashtable runningJobs = new Hashtable();
	
	public Dispatcher(Factory f) {
		this(f, false);
	}

	public Dispatcher(Factory f, boolean boot) {
		factory = f;
	}
	
	public Dispatcher(DataInputStream dis, boolean boot) {
		factory = new Factory(dis);
	}
	
	/**
	 * check the erreichbarkeit der knoten und nimm nur erreichbare
	 */
	public void init() {
		factory.init();
	}


	public boolean isOnline() {
		return factory.isOnline();
	}


	public void startDispatcher() {
		tickTackTicker = new Thread("tick tack ticker") {
	        public void run() {
	            while (true) {
	                try {
	                    Thread.sleep(3000);
	                } catch (InterruptedException e) {
	                }
	                onTimer();
	            }
	        }
		};
		tickTackTicker.start();		
	}
	/**
	 * 
	 */
	public void goOnline(boolean wait) {
		factory.goOnline();
//		if (wait) { factory.goOnline(); }
//		if (onlineWatcher == null) {
//			tickerThread = new Thread("tick tack") {
//		            public void run() {
//		                while (true) {
//		                    Mixed.wait(1000);
//		                    try {
//		                        Thread.sleep(time);
//		                    } catch (InterruptedException e) {
//		                    }
//		                    // refactor this method in Core. lots of work :)
//		                    timer_actionPerformed();
//		                }
//		            }
//		        };
//		        tickerThread.start();
//
//			
//		}
	}

//	/**
//	 * start job queue
//	 */
//	public void startDispatcher() {
//		factory.goOnline();
//	}

	/**
	 * same as panic, but without data loss
	 * state is saved an can be resumed
	 */
	public void freezeDispatcher() {
	}
	
	/**
	 * clean and soft shutdown. complete running jobs,
	 * but dont start new ones
	 */
	public void stopDispatcher() {
		//tickTackTicker.stop();
	}
	
	public boolean loadState() {
		return false;	
	}
	
	public boolean saveState() {
		return false;	
	}

	/**
	 * The panic-button. Neccesary for all freenet apps :)
	 * This function don't return until all is done.
	 * 
	 * <bold>WARNING<bold>
	 * calling this cause data loss.
	 * @param clearqueue
	 */
	public void panic(boolean clearqueue) {
		// TODO:
		// Stop jobmanager
		// Cancel all active connections (Perstance=Connection)
		// ? clear all queues (Perstance=reboot/forever)
		// disconnect all
	}


	/**
	 * is the host:port in our nodelist?
	 * needed for security manager
	 * @param host
	 * @param port
	 * @return
	 */
	public boolean isInList(String host, int port) {
		return factory.isInList(host, port);
	}
	
	/**
	 * add a job to the queue and returns
	 * the balancer start it earlier or later
	 * @param job
	 * @return true on success (added) or false on error
	 */
	public boolean addJob(Job job) {
		// TODO
		return false;
	}

	/**
	 * returns a new fcpconnection
	 * @param networktype
	 * @return
	 */
	public FCPConnection getNewFCPConnection(int networktype) {
		return factory.getNewFCPConnection(networktype);
	}
	
	/**
	 * returns the nodes default fcpconnection
	 * use this and the dispatcher use a single connection per node
	 * (if the node/network supports this)
	 * @param networktype
	 * @return
	 */
	public synchronized FCPConnection getDefaultFCPConnection(int networktype) {
		System.err.println("HEHE: getDefaultFCPConnection Start " + networktype);
		FCPConnection conn = factory.getDefaultFCPConnection(networktype);
		System.err.println("HEHE: getDefaultFCPConnection Ende " + conn);
		return conn;
	}


	/**
	 * run a job and do not return until the job is done.
	 * @param job
	 */
	public void runJob(Job job) {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Error("Hicks");
		}
		if (job.getStatus() == Job.STATUS_UNPREPARED) {
			//System.out.println(job.getJobID() + " -> job unprepared. prepare it");
			job.prepare(); 
		}
		if (job.getStatus() != Job.STATUS_PREPARED) {
			System.out.println(job.getJobID() + " - " + job + " -> job unprepared. return without execution");
			throw new Error("DEBUG/FIX/TODO");
			//return; 
		}
		registerJob(job);
		job.run(this);
		unregisterJob(job);
	}
	
	private void registerJob(Job job) {
		runningJobs.put(job.getJobID(), job);
	}
	
	private void unregisterJob(Job job) {
		runningJobs.remove(job.getJobID());		
	}
	
	public void incommingMessage(FCPConnection conn, Hashtable message) {
		// TODO Auto-generated method stub
		System.out.println("Testinger message: " + message);
		Job j = getRunningJob((String)message.get("Identifier"));
		if (j == null) { throw new Error("Hmmmm"); }
		j.incommingMessage(conn, message);
	}

	public void incommingData(FCPConnection conn, Hashtable message) {
		System.out.println("Testinger Data: " + message);
		Job j = getRunningJob((String)message.get("Identifier"));
		if (j == null) { throw new Error("Hmmmm"); }
		j.incommingData(conn, message);
//		getRunningJob((String)message.get("Identifier")).incommingData(conn, message);
	}
	
	private void onTimer() {
		//System.out.println("Tick Tack Timer");
	}
	
	private Job getRunningJob(String id) {
		System.out.println("getRunningJob: " + id);
		return (Job)runningJobs.get(id);
	}


}
