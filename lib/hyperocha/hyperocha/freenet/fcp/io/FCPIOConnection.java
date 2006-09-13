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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author  saces
 */
public class FCPIOConnection {
	private FCPNode node;
	private Socket fcpSock;
    private InputStreamReader fcpIn;
    private BufferedReader fcpInBuf;
    private PrintStream fcpOut;
    private boolean isopen = false;
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
		//setTimeOut()
    	//this(node, node.getTimeOut(), errh); 
    }
    
    private void setFCPConnectionErrorHandler(FCPIOConnectionErrorHandler errh) {
   		this.connerrh = errh;
    }
    
    /**
     * IO errorhandler: call the error handler, if one is set
     * @param e
     */
    private /*synchronized*/ void handleIOError(Exception e) {
    	//this.close();
    	isopen = false;
    	lasterror = e;
    	if (connerrh != null) {
    		connerrh.onIOError(e);
    	}
    }
    
    public /*synchronized*/ boolean open() {
    	return open("UTF-8");
    }
    
    public /*synchronized*/ boolean open(String charset) {
    	try {
			fcpSock = node.createSocket();
			//fcpSock.setSoTimeout(to);
			fcpOut = new PrintStream(fcpSock.getOutputStream(), false, charset);
    		fcpIn = new InputStreamReader(fcpSock.getInputStream(), charset);
    		fcpInBuf = new BufferedReader(fcpIn);
		} catch (IOException e) {
			handleIOError(e);
			return false;
		}
		isopen = true;
		return true;
    }
    
	public void close() {
        try {
        	fcpOut.close();
			fcpIn.close();
			fcpInBuf.close();
	        fcpSock.close();
		} catch (Throwable e) {
			// ignore errors while closing
			// egal, die connection ist eh ungueltig danach.
		}
		isopen = false;
		fcpOut = null;
		fcpIn = null; 
		fcpInBuf = null;
        fcpSock = null;
	}
	
	public boolean isOpen() {
		return isopen;
	}
	
	public Exception getLastError() {
		return lasterror;
	}

	protected boolean setTimeOut(int to) {
		if (!isopen) return false;
		try {
			fcpSock.setSoTimeout(to);
		} catch (SocketException e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean write(byte[] b, int off, int len) {
		if (!isopen) return false;
		try {
			fcpOut.write(b, off, len);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean write(int i) {
		if (!isopen) return false;
		try {
			fcpOut.write(i);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean println(String s) {
		if (!isopen) return false;
		try {
			fcpOut.println(s);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
	
	public boolean print(String s) {
		if (!isopen) return false;
		try {
			fcpOut.print(s);
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}

	
	public String readLine() {
		if (!isopen) return null;
		String result = null;
		try {
			result = fcpInBuf.readLine();
		} catch (Exception e) {
			handleIOError(e);
		}
		return result;
	}
	
	public /*synchronized*/ boolean startRaw(FCPIOConnectionHandler h) {
		if (!isopen) return false;
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
		if (!isopen) return false;
		try {
			fcpOut.flush();
		} catch (Exception e) {
			handleIOError(e);
			return false;
		}
		return true;
	}
}
