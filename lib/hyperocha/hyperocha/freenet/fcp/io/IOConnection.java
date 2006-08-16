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
    
    private IOConnectionErrorHandler connerrh = null;
    
    //protected abstract void helo() throws IOException;
    //protected abstract void handleIt(String s) throws IOException; 

    /**
	 * 
	 */
    public IOConnection(FCPNode node, int to, IOConnectionErrorHandler errh) {
		this.node = node;
		setFCPConnectionErrorHandler(errh);
	}
    
    //public FCPRawConnection(FCPNode node) {
    //	this(node, node.timeOut, null); 
    //}
    
    public IOConnection(FCPNode node, IOConnectionErrorHandler errh) {
    	this(node, node.getTimeOut(), errh); 
    }
    
    //protected FCPRawConnection(FCPNode node, int to) {	
    //	this(node, node.timeOut, null); 
    //}
    
    private void setFCPConnectionErrorHandler(IOConnectionErrorHandler errh) {
    	if (errh == null) {
    		this.connerrh = new DefaultIOConnectionErrorHandler();
    	} else {
    		this.connerrh = errh;
    	}
    }
    
    private void handleIOError(IOException e) {
    	if (connerrh == null) {
    		System.out.println("No IO ErrorHandler implemented : " + e);
    		e.printStackTrace();
    		return;
    	}
    	
    	//System.out.println("No FCPConnectionErrorHandler assigned : " + e);
    	
    	if (e instanceof java.net.ConnectException ) {
    		System.out.println("Cant connect");
    		connerrh.onCantConnect(e);
    	}
    	
    	
    }
    
    //public void open() {
    //	open(node.timeOut);
    //}

    public void open() {
    	try {
			fcpSock = node.createSocket();
			//fcpSock.setSoTimeout(to);
			fcpOut = new PrintStream(fcpSock.getOutputStream());
	        fcpIn = new InputStreamReader(fcpSock.getInputStream());
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
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		fcpOut = null;
		fcpIn = null; 
		fcpInBuf = null;
        fcpSock = null;
	}

	
	/*
	protected boolean ping() {
		if(!fcpSock.isConnected()) {
			return true;
		}
		boolean p = false;
		try {
			fcpSock.connect(node.host, node.port);
			p = fcpSock.isConnected();
			fcpSock.close();
		} catch (IOException e) {
			return false;
		}
		return p;
	}*/
	
	protected void setTimeOut(int to) throws SocketException {
		fcpSock.setSoTimeout(to);
	}
	
	//protected void setTimeOut() throws SocketException {
	//	fcpSock.setSoTimeout(node.timeOut);
	//}
	
	protected void write(byte[] b, int off, int len) {
		fcpOut.write(b, off, len);
	}
	
	public void write(int i) {
		fcpOut.write(i);
	}
	
	public void println(String s) {
		//System.out.println("testinger" + fcpOut);
		fcpOut.println(s);
	}
	
	public String readLine() {
		String result = null;
		try {
			result = fcpInBuf.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			handleIOError(e);
		}
		return result;
	}
	
//	private void flush() {
//		fcpOut.flush();
//	}
	
	public /*synchronized*/ void startRaw(IOConnectionHandler h) {
		// while fcpin.read(ein byte) {
		//		h.handleIt(byte)
		// }
		int i = -1;
		do {
			//conn.startRaw(this);
			//System.out.println("Wait for a byte...");
			try {
				i = fcpIn.read();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return;
			}
			h.handleItRaw(i);
		} while (i != -1);
		close();
	}

	public void flush() {
		// TODO Auto-generated method stub
		fcpOut.flush();
	}

}
