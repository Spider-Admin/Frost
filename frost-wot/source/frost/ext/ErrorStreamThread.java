package frost.ext;

import java.io.*;

/**
 * Catches an Error Stream from a process
 * @author Jan-Thomas Czornack
 * @version 010711
 */
class ErrorStreamThread extends Thread {
    
    Process p;
    Transit data;

    public void run() {
	
	StringBuffer output = new StringBuffer();
	DataInputStream dis = new DataInputStream(p.getErrorStream());
	
	try {
	    int result = 0;

	    while((result = dis.read()) != -1) {
		System.out.print((char)result);
		output.append((char)result);
	    }

	}
	catch (IOException e) {
	    System.out.println("Can't get input stream.");
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
