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
import hyperocha.freenet.fcp.dispatcher.job.Job;

import java.io.DataInputStream;
import java.util.List;

/**
 * @author saces
 *
 */
public class Dispatcher {
	private Factory factory;
	
	
	// networkid balancer
	private List balancer;

	
	//listen:
	
	
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


	/**
	 * check node hello
	 */
	public void goOnline(boolean wait) {
		factory.goOnline();
	}

	/**
	 * start job queue
	 */
	public void startDispatcher() {
		factory.goOnline();
	}

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
	public synchronized void panic(boolean clearqueue) {
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

	public FCPConnection getFCPConnection(int networktype) {
		return factory.getNextConnection(networktype);
	}

	/**
	 * run a job and do not return until the job is done.
	 * @param job
	 */
	public /*synchronized*/ void runJob(Job job) {
		if (job.getStatus() == Job.STATUS_UNPREPARED) {
			System.out.println(job.getJobID() + " -> job unprepared. prepare it");
			job.prepare(); 
		}
		if (job.getStatus() != Job.STATUS_PREPARED) {
			System.out.println(job.getJobID() + " - " + job + " -> job unprepared. return without execution");
			throw new Error();
			//return; 
		}
		
		//FCPConnection conn = getFCPConnection(job.getRequiredNetworkType());
		job.run(this);

	}

}
