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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author saces
 *
 */
public class FCPNode {
	
	private FCPNodeIOErrorHandler ioErrorHandler = null;
	private InetAddress host = null;
    private int port = -1;
	private String hostName = null; // avoid name lookup recursion in security manager
	private String hostIp = null; // avoid n
    private FCPConnection defaultConn = null;
    private int timeOut = 10 * 60 * 1000; // the default timeout (ms)

    //private long fcpConnectionId = 0;
    
    //protected long getConnectionId() {
        //return fcpConnectionId++;
    //    return (new java.util.Date()).getTime();
    //}
    
 
    /**
     * the constructor checks only the plausibility of 'server:port'
     * but doesn't etablish any connection to it.
     * @param String 'server:port'
     * @throws Throwable 
     * 
     */
    
    public FCPNode(String serverport) {
    	this(serverport, new DefaultNodeIOErrorHandler());
    }
    
	public FCPNode(String serverport, FCPNodeIOErrorHandler errh) {
		String[] splitServerPort = serverport.split(":");
		InetAddress ia = null;
		//InetSocketAddress isa = null;
//        try {
            try {
				ia = InetAddress.getByName(splitServerPort[0]);
				this.host = ia;
	            this.hostName = ia.getHostName();
	            this.hostIp = ia.getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				errh.OnIOError(e);
				//System.out.println("ERROR: Unknown FCP host: "+ serverport);
				//e.printStackTrace();
			}
            
//        } catch(Throwable t) {
//            System.out.println("ERROR: Unknown FCP host: "+ serverport);
            //System.out.println(t);
//        }
        try {
            
            port = Integer.parseInt(splitServerPort[1]);
            if ((port < 1) || (port > 65535)) { throw new IllegalArgumentException(); }
           // isa = InetSocketAddress.createUnresolved(this.hostName, port);
        } catch(Exception e) {
        	errh.OnIOError(e);
          //System.out.println("ERROR: Unknown FCP server:port: "+ serverport);
         // System.out.println(t);
      }
            //if ((port < 1) || (port > 65535)) { throw new IllegalArgumentException(); } 
            //isa = InetSocketAddress.createUnresolved(this.hostName, port);
//        } catch(Throwable t) {
//            System.out.println("ERROR: Unknown FCP port: "+ serverport);
           // System.out.println(t);
//        }
        
    }
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	
	public FCPConnection getDefaultFCPConnection() throws IOException {
		if (defaultConn == null) {
			defaultConn = getNewFCPConnection();
		}
		return defaultConn;
	}
	
	/**
	 * @param timeout timeout for helo in millisecunds
	 */
	/* blödsinn
	public boolean helo(int to) {
		@SuppressWarnings("unused") FCPConnection conn = null;
		try {
			conn = getNewFCPConnection(to);
	    } catch (IOException ex) {
			System.out.println("Error in helo: " + ex);
			ex.printStackTrace();
			return false;
	    }
	    //conn.close();
	     System.out.println("HELO7: Ende OK");
	     return true;
	}
*/
	
	public int getTimeOut() {
		return this.timeOut;
	}
	
	public InetAddress getHost() {
		return this.host;
	}
	
	public int getPort() {
		return this.port;
	}
	
	/**
	 * @return Socket
	 * @throws IOException
	 */
	public Socket createSocket() throws IOException {
		return createSocket(timeOut);
	}
	
	/**
	 * @param to timeout
	 * @return 
	 * @throws IOException
	 */
	public Socket createSocket(int to) throws IOException {
		Socket sock = new Socket(host, port);
	    sock.setSoTimeout(to);
	    return sock;
	}
	
	
	public FCPConnection getNewFCPConnection() {
		return new FCPConnection(this);
	}
	
	//public FCPConnection getNewFCPConnection(int to) throws IOException {
	//	return new FCPConnection(this, to);
	//}
}
