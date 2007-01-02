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
package hyperocha.freenet.fcp.io;

import hyperocha.freenet.fcp.FCPNode;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author  saces
 * @version $Id$
 */
public class FCPIOConnection {
	private FCPNode node;
	private Socket fcpSock;
    private BufferedInputStream fcpIn;
    private PrintStream fcpOut;
    //private boolean isopen = false;
    private Exception lasterror = null;
    
    private FCPIOConnectionErrorHandler connerrh = null;
    
    /**
	 * 
	 */
    public FCPIOConnection(FCPNode node, int to, FCPIOConnectionErrorHandler errh) {
		this.node = node;
		setFCPConnectionErrorHandler(errh);
		setTimeOut(to);
	}
    
    public FCPIOConnection(FCPNode node, FCPIOConnectionErrorHandler errh) {
    	this.node = node;
		setFCPConnectionErrorHandler(errh);
    }
    
    private void setFCPConnectionErrorHandler(FCPIOConnectionErrorHandler errh) {
   		this.connerrh = errh;
    }
    
    /**
     * IO errorhandler: call the error handler, if one is set
     * @param e
     */
    private void handleIOError(Exception e) {
    	//isopen = false;
    	lasterror = e;
    	//e.printStackTrace();  // FIXME
    	if (connerrh != null) {
    		connerrh.onIOError(e);
    	}
    }
    
    public boolean open() {
    	return open("UTF-8");
    }
    
    public boolean open(String charset) {
    	try {
			fcpSock = node.createSocket();
			//fcpSock.setSoTimeout(to);
			fcpOut = new PrintStream(fcpSock.getOutputStream(), false, charset);
    		fcpIn = new BufferedInputStream(fcpSock.getInputStream());
		} catch (IOException e) {
			handleIOError(e);
			return false;
		}
		//isopen = true;
		return true;
    }
    
	public void close() {
        try {
        	fcpOut.close();
			fcpIn.close();
	        fcpSock.close();
		} catch (Throwable e) {
			// ignore errors while closing
			// egal, die connection ist eh ungueltig danach.
		}
		//isopen = false;
		fcpOut = null;
		fcpIn = null;
        fcpSock = null;
	}
	
	// DEBUG the IOErrorHandler should have called close (set sock = null) before this
	// here can thrown
	public boolean isOpen() {
		if (fcpSock == null) 
			return false;
		if (fcpSock.isClosed()) 
			throw new Error("Sock closed");
		if (!fcpSock.isConnected()) 
			throw new Error("not Connected");
		return true;
	}
	
	public Exception getLastError() {
		return lasterror;
	}

	protected boolean setTimeOut(int to) {
		if (!isOpen()) return false;
		try {
			fcpSock.setSoTimeout(to);
		} catch (SocketException e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean write(byte[] b, int off, int len) {
		if (!isOpen()) return false;
		try {
			fcpOut.write(b, off, len);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean write(int i) {
		if (!isOpen()) return false;
		try {
			fcpOut.write(i);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean write(byte[] b) {
		if (!isOpen()) return false;
		try {
			fcpOut.write(b);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean println(String s) {
		if (!isOpen()) throw new Error(); //return false;
		try {
			fcpOut.println(s);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean print(String s) {
		if (!isOpen()) return false;
		try {
			fcpOut.print(s);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}

	
	public String readLine() {
//		System.out.println("readLine: Start");
		if (!isOpen()) {
//			System.out.println("readLine: not open");
			return null;
		}
		String result = null;
		int b;
		byte[] bytes = new byte[1024];  // a key and a funny dir/filename in a ssk site, 256 is verry knapp
		int count = 0;
//		System.out.println("readLine: 003");
		try {
			while ((b = fcpIn.read()) != '\n' && b != -1 && count < 1024 && (b != '\0')) {
				//System.out.println("readLine: read:" + b);
				bytes[count] = (byte) b;
				count++;
			}
			if (count == 0) {
//				System.out.println("readLine: nix gelesen");
				return null;
			}
			result = new String(bytes, 0, count, "UTF-8");
		} catch (Exception e) {
			//e.printStackTrace();
			handleIOError(e);
			return null;
		}
		return result;
	}
	
	public boolean startRaw(FCPIOConnectionHandler h) {
		if (!isOpen()) return false;
		int i = -1;
		do {
			//conn.startRaw(this);
			//System.out.println("Wait for a byte...");
			try {
				i = fcpIn.read();
			} catch (Exception e) {
				handleIOError(e);
				return false;
			}
			h.handleItRaw(i);
		} while (i != -1);
		close();  // notwendig hier
		return true;
	}

	public boolean flush() {
		if (!isOpen()) throw new Error(); //return false;
		try {
			fcpOut.flush();
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}

	public int read() {
		if (!isOpen()) return -1;
		int i;
		try {
			//System.err.println("TEST IO READ 03");
			i = fcpIn.read();
			//System.err.println("TEST IO READ 04: " + i);
		} catch (Exception e) {
			handleIOError(e);
			return -1;
		}
		return i;
	}

	/**
	 * @return the node
	 */
	public int getNetworkType() {
		return node.getNetworkType();
	}
}
