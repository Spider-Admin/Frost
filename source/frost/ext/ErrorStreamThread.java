package frost.ext;

import java.io.*;
import java.util.logging.*;

/**
 * Catches an Error Stream from a process
 * @author Jan-Thomas Czornack
 * @version 010711
 */
class ErrorStreamThread extends Thread {
    
	private static Logger logger = Logger.getLogger(ErrorStreamThread.class.getName());
    
    Process p;
    Transit data;

    public void run() {
	
	StringBuffer output = new StringBuffer();
	DataInputStream dis = new DataInputStream(p.getErrorStream());
	
	try {
	    int result = 0;

	    while((result = dis.read()) != -1) {
		output.append((char)result);
	    }
		logger.warning(output.toString());
	}
	catch (IOException e) {
		logger.log(Level.SEVERE, "Can't get input stream.", e);
	}

    }
    
    /**
     * Constructor
     * @param p Process to get the Error Stream from
     */
    public ErrorStreamThread (Process p, Transit data) {
	this.p = p;
	this.data = data;
    }

}
