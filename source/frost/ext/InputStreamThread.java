package frost.ext;

import java.io.*;
import java.util.logging.*;

/**
 * Catches an Input Stream from a process
 * @author Jan-Thomas Czornack
 * @version 010711
 */
class InputStreamThread extends Thread {
    
	private static Logger logger = Logger.getLogger(InputStreamThread.class.getName());
    
    Process p;
    Transit data;

    public void run() {
	
	StringBuffer output = new StringBuffer();
	DataInputStream dis = new DataInputStream(p.getInputStream());

	try {
	    int result = 0;

	    while((result = dis.read()) != -1) {
		output.append((char)result);
	    }
		logger.info(output.toString());

	} catch (IOException e) {
		logger.log(Level.SEVERE, "Can't get input stream.", e);
	}

	data.setString(output.toString());
	
    }
    
    /**
     * Constructor
     * @param p Process to get the Input Stream from
     */
    public InputStreamThread (Process p, Transit data) {
	this.p = p;
	this.data = data;
    }

}
