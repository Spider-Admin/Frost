package frost.ext;

import java.io.*;

/**
 * Catches an Input Stream from a process
 * @author Jan-Thomas Czornack
 * @version 010711
 */
class InputStreamThread extends Thread {
    
    Process p;
    Transit data;

    public void run() {
	
	StringBuffer output = new StringBuffer();
	DataInputStream dis = new DataInputStream(p.getInputStream());

	try {
	    int result = 0;

	    while((result = dis.read()) != -1) {
		System.out.print((char)result);
		output.append((char)result);
	    }

	} catch (IOException e) {
	    System.out.println("Can't get input stream.");
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
