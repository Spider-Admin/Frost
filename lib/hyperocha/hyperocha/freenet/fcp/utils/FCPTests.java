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
package hyperocha.freenet.fcp.utils;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.FCPNodeIOErrorHandler;
import hyperocha.freenet.fcp.Persistence;
import hyperocha.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;


/**
 * @author saces
 *
 */
public class FCPTests {
	
	public static FCPNode TestNodeVersion(String serverport, Version v) {
		return null;
	}
	
	/**
	 * testing the presence of a given FCP-port (and etabl a connction)
	 * @param serverport a string with "server:port"
	 * @return a valid node handle or NULL if somthing is wrong
	 */

	public static FCPNode TestNodeHelo(String serverport) {
		//System.out.print("Testing Node Helo: ");
		
		TestNodeErrorHandler erh = new TestNodeErrorHandler();
		
		FCPNode node = new FCPNode(serverport, erh);
		
		if ((node == null) || (erh.err)) return null;

		// FIXME: check errors while etablish connection
		node.getDefaultFCPConnection();

		return node;
	}
	
	
	public static boolean TestNodeDDA(FCPNode n) {
		File f = null;
		Random rnd = new Random();
		byte[] b = new byte[1024]; 
		rnd.nextBytes(b);
		FCPNode node = n;
		//System.out.println("TODO: Testing Node 7 DirectDiscAccess");
		
		try {
			f = File.createTempFile("judl", "DDATest");
			f.deleteOnExit();
			PrintStream ps = new PrintStream(f);
			ps.write(b, 0, b.length);
			ps.flush();
		} catch (IOException ex) {
			System.out.println("ERROR: TestNode7DDA: error while creating temporary test file");
			System.out.println("ERROR: TestNode7DDA: " + ex);
			return false;
		}
		
		//tempfile anlegen
		ArrayList cmd = new ArrayList();
		//String[] cmd = new ArrayList();
		cmd.add("ClientPut");
		cmd.add("URI=CHK@");
		cmd.add("Identifier=InsertDDA-1");
		cmd.add("Verbosity=0");
		cmd.add("MaxRetries=3");
		cmd.add("PriorityClass=0");
		cmd.add("GetCHKOnly=true");
		cmd.add("Global=false");
		cmd.add("DontCompress=true");
		cmd.add("ClientToken=Hello!!!");
		cmd.add("UploadFrom=disk");
		cmd.add("Filename=" + f.getAbsolutePath());
		cmd.add("EndMessage");
		
		//System.out.println("ddd: FilenamE: " + f.getAbsolutePath());
		
		//FCPNode node = new FCPNode(serverport);
		FCPConnection conn = null; //node.getDefaultFCPConnection();
		boolean repeat = true;
		Hashtable result = null;
		try { 
			conn = node.getDefaultFCPConnection();
			//conn.start((String[]) cmd.toArray());
			conn.start(cmd);
			
			
			while (repeat) {
				result = conn.readEndMessage();
				System.out.println("TODO: DDA-Test loop: " + result);
				repeat = ("URIGenerated").equalsIgnoreCase((String)(result.get("judl-reason")));
			}
			
			//Hashtable<String,String> result = null;
			//result = conn.readEndMessage();
			//System.out.println("TODO: DDA-Test1: " + result);
			//return null;
			if (("PutSuccessful").equalsIgnoreCase((String)(result.get("judl-reason")))) {
				return true; // the only one case for return ok.
			}
			//System.out.println("Result:" + result.get("judl-reason"));
		} catch (Throwable ex) {		
			//System.out.println("Error in TestNodeDDA: " + ex);
			return false;
		}
	
		//conn.close();
		//System.out.println("TestNodeDDA: Ende OK");
		return false;
	}
	
	public static boolean TestNodeGQ(FCPNode node) {
		//System.out.println("TODO: Testing Node 7 Global queue");
		File f = null;
		Random rnd = new Random();
		byte[] b = new byte[1024]; 
		rnd.nextBytes(b);
		
		String identifer = FCPUtil.getNewConnectionId("InsertGQTest-");
		
		try {
			f = File.createTempFile("judl", "GQTest");
			f.deleteOnExit();
			PrintStream ps = new PrintStream(f);
			ps.write(b, 0, b.length);
			ps.flush();
		} catch (IOException ex) {
			System.out.println("ERROR: TestNode7DDA: " + ex);
			return false;
		}
		
		ArrayList cmd = new ArrayList();
		//String[] cmd = new ArrayList();
		cmd.add("ClientPut");
		cmd.add("URI=CHK@");
		//cmd.add("Identifier=InsertGQTest-1");
		cmd.add("Identifier=" + identifer);
		cmd.add("Verbosity=0");
		cmd.add("MaxRetries=3");
		cmd.add("PriorityClass=0");
		cmd.add("GetCHKOnly=false");
		cmd.add("Global=true");
		cmd.add("DontCompress=true");
		cmd.add("Persistence=" + Persistence.FOREVER);
		cmd.add("ClientToken=HelloAgain!!!");
		cmd.add("UploadFrom=disk");
		cmd.add("Filename=" + f.getAbsolutePath());
		cmd.add("EndMessage");
		
		Hashtable result = null;
		FCPConnection conn = null;
		//try {
			//conn = node.getNewFCPConnection(5000);
			conn = node.getNewFCPConnection();
			//conn.start((String[]) cmd.toArray());
			conn.start(cmd);
			
			//System.out.println("TODO: GQ-Test:10");
			
			result = conn.readEndMessage(); 
			
			//System.out.println("Result: GQT" + result.get("judl-reason"));
			
			if (!("PersistentPut").equalsIgnoreCase((String)(result.get("judl-reason")))) {
				return false; // the only one case for return ok.
			}
			
			//System.out.println("TODO: GQ-Test:10");
			//result = conn.readEndMessage();
			//System.out.println("TODO: GQ-Test: " + result);
			
			// wenna hier sin, is ok. ;)
			// wida wegputzen
			
			//System.out.println("TODO: GQ-Test:11");
			
			/* kein plan, bug oder noch raustüfteln, wie das geht....
			
			cmd.clear();
			cmd.add("RemovePersistentRequest");
			cmd.add("Global=true"); // if true, remove it from the global queue
			cmd.add("Identifier=" + identifer);
			cmd.add("EndMessage");
			
			System.out.println("TODO: GQ-Test:12");
			
			result = conn.readEndMessage();
			*/
			//System.out.println("TODO: GQ-Test:11");
			//System.out.println("TODO: GQ-Test: " + result);
		        
		 /*       String tmp;
		        do {
		            tmp = conn.readLine();
		            System.out.println("TEST02 out: " + tmp);
		            if (tmp.compareTo("EndMessage") == 0) {
		                break;
		            }
		            //result.add(tmp);
		        } while(tmp.compareTo("EndMessage") != 0);
	*/
		
		//} catch (IOException ex) {
		//	System.out.println("Error in TestNodeGQ(): " + ex);
		//	return false;
		//}
		
		//System.out.println("Aaaah!");
		return true;
	}

}
