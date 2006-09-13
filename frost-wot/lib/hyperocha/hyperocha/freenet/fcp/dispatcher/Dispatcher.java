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
	
	
	public Dispatcher(Factory f) {
		factory = f;
	}


	public void init() {
		factory.init();
	}


	public boolean isOnline() {
		return factory.isOnline();
	}


	public void goOnline() {
		factory.goOnline();
		
	}


	public boolean isInList(String host, int port) {
		return factory.isInList(host, port);
//        List nodes = FcpHandler.inst().getNodes();
//      for(Iterator i=nodes.iterator(); i.hasNext(); ) {
//      NodeAddress na = (NodeAddress)i.next();
//      if( port < 0 ) {
//          return; // allow DNS lookups
//      }
//      if( port == na.port ) {
//          if( host.equals(na.hostIp) || host.equals(na.hostName) ) {
//              return; // host:port is in our list
//          }
//      }            
//  }

		// TODO Auto-generated method stub
		//return false;
	}
	
	
	/*	class dispatcher() {
	   Factory f   // factory returns the best next node for the pending job, welcher node kann was und so 
	   Balancer b // entscheidet wann der nächste job dranne iis, ob jemand aus der factory zeit hat....
	   JobList l   // jobliste die liste der anstehenden aufträge

	   // init im constructor 
	   public addJob(job) {}
	   public edtitJob(job) {}
	   public remove(job) {} 
	 
	  abstract einjobhatsichbewegtcallback(JobInfo jobinfo);
	  abstract loadShnapshot()   //   da kann man das ding supenden und später da weitermachen, wo er aufgehört hatte
	  abstract saveShnapshot() //   für den boardupdate wäre das super, denke ich...    und beim .5 auch für die inserts.
	}
*/

}
