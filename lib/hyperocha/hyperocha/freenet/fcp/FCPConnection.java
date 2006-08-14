/**
 * 
 */
package hyperocha.freenet.fcp;

import hyperocha.freenet.fcp.io.DefaultIOConnectionErrorHandler;
import hyperocha.freenet.fcp.io.IOConnection;
import hyperocha.freenet.fcp.io.IOConnectionErrorHandler;
import hyperocha.freenet.fcp.utils.FCPUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * @author saces
 *
 */
public class FCPConnection {
	
	 private IOConnection rawConn; // = null;
	 private IOConnectionErrorHandler rawErrH; // = null; 
	 private String connectionID; // = null;

	/**
	 * @param node
	 * @param to
	 * @param errh
	 */
	//public FCPConnection(FCPNode node, int to, RawConnectionErrorHandler errh) {
	//	conn = new RawConnection(node, to, errh);
	//}

	/**
	 * @param node
	 * @param errh
	 */
	public FCPConnection(FCPNode node) {
		//this(node, node.timeOut, errh);
		rawErrH = new DefaultIOConnectionErrorHandler();
		rawConn = new IOConnection(node, rawErrH);
		rawConn.open();
		connectionID = nodeHello();
	}

	public String getConnectionID() {
		return connectionID;
	}
	
	public void start(ArrayList l) {
		//open();
		int sc = l.size();
		for (int i = 0; i < sc; i++){
			//System.out.println("Startprint: " + l.get(i));
			println((String)(l.get(i)));
		}
		//fcpOut.println("EndMessage");
		//System.out.println("Startprint: EndMessage");
		//conn.flush();
		//String tmps = fcpInBuf.readLine();
		//handleIt(tmps);
		/*
		fcpIn.close();
		fcpInBuf.close();
        fcpOut.close();
		fcpSock.close();
		*/
	}
	
	public /*synchronized*/ Hashtable readEndMessage() {
		Hashtable result = new Hashtable();
		String tmp;
		
		// the first line is the reason
		//tmp = readLine();
		tmp = rawConn.readLine();
		result.put("judl-reason", tmp);
		
		
		while(true) {
            tmp = rawConn.readLine();
            //result.add(tmp);
            //System.out.println("ReadEndMessage out: " + tmp);
            if (tmp.compareTo("EndMessage") == 0) {
            	result.put("judl-fin", tmp);
                break;
            }
            if (tmp.contains("=")) {
            	String[] tmp2 = tmp.split("=", 2);
            	result.put(tmp2[0], tmp2[1]);
            } else {
            	System.out.println("this shouldn't happen. FIXME. mpf!");
            	result.put("Unknown", tmp);
            }
        } while(tmp.compareTo("EndMessage") != 0);
        return result;	
	}
	
	public void handleIt(String s) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("handle it 7");
		readEndMessage();
		
	}
	
	private String nodeHello() {
		Hashtable result = null;
		result = helo();
		//System.out.println("TODO: Helo-Test: " + result);
		return null;
	}
	
	private Hashtable helo() {
		//System.out.println("TODO: EchtEs holo 07 hier!");
		
		ArrayList cmd = new ArrayList();
		cmd.add("ClientHello");
		cmd.add("Name="+ FCPUtil.getNewConnectionId("judl-"));
		cmd.add("ExpectedVersion=2.0");
		cmd.add("EndMessage");
		//cmd.add("En");
		start(cmd);
		return readEndMessage();
	}

	public void println(String s) {
		rawConn.println(s);
		rawConn.flush();
	}
	
	//public long getConnectionId() {
	//	return node.getConnectionId();
	//}
}
