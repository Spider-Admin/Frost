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
 * @author saces
 *
 */
public class IOConnection {
	private FCPNode node;
	private Socket fcpSock;
    private InputStreamReader fcpIn;
    private BufferedReader fcpInBuf;
    private PrintStream fcpOut;
    private boolean isopen = false;
    
    private IOConnectionErrorHandler connerrh = null;
    
    /**
	 * 
	 */
    public IOConnection(FCPNode node, int to, IOConnectionErrorHandler errh) {
		this.node = node;
		setFCPConnectionErrorHandler(errh);
	}
    
    public IOConnection(FCPNode node, IOConnectionErrorHandler errh) {
    	this(node, node.getTimeOut(), errh); 
    }
    
    private void setFCPConnectionErrorHandler(IOConnectionErrorHandler errh) {
   		this.connerrh = errh;
    }
    
    /**
     * IO errorhandler: call the error handler, if one is set
     * @param e
     */
    private void handleIOError(Exception e) {
    	//this.close();
    	if (connerrh != null) {
    		connerrh.onIOError(e);
    	}
    }
    
    public void open() {
    	open("UTF-8");
    }
    
    public void open(String charset) {
    	try {
			fcpSock = node.createSocket();
			//fcpSock.setSoTimeout(to);
			fcpOut = new PrintStream(fcpSock.getOutputStream(), false, charset);
	        fcpIn = new InputStreamReader(fcpSock.getInputStream(), charset);
	        fcpInBuf = new BufferedReader(fcpIn);
		} catch (IOException e) {
			handleIOError(e);
		}
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

	protected void setTimeOut(int to) throws SocketException {
		fcpSock.setSoTimeout(to);
	}
	
	public void write(byte[] b, int off, int len) {
		try {
			fcpOut.write(b, off, len);
		} catch (Exception e) {
			handleIOError(e);
		}
	}
	
	public void write(int i) {
		try {
			fcpOut.write(i);
		} catch (Exception e) {
			handleIOError(e);
		}
	}
	
	public void println(String s) {
		try {
			fcpOut.println(s);
		} catch (Exception e) {
			handleIOError(e);
		}
	}
	
	public String readLine() {
		String result = null;
		try {
			result = fcpInBuf.readLine();
		} catch (Exception e) {
			handleIOError(e);
		}
		return result;
	}
	
	public /*synchronized*/ void startRaw(IOConnectionHandler h) {
		int i = -1;
		do {
			//conn.startRaw(this);
			//System.out.println("Wait for a byte...");
			try {
				i = fcpIn.read();
			} catch (Exception e) {
				handleIOError(e);
				return;
			}
			h.handleItRaw(i);
		} while (i != -1);
		close();
	}

	public void flush() {
		try {
			fcpOut.flush();
		} catch (Exception e) {
			handleIOError(e);
		}
	}

}
