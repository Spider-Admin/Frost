/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.channels;

import java.util.logging.*;

/**
 * @author zlatinb
 *
 * default index generator - 0,1,2,3...
 */
public class DefaultIndexGenerator implements IndexGenerator {
	
	private static Logger logger = Logger.getLogger(DefaultIndexGenerator.class.getName());
	
	String currentIndex_s;
	int currentIndex;

	public DefaultIndexGenerator() {
		try {
			setCurrentIndex("0");
		} catch (InvalidSlotException e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
		}
	}

	public void setCurrentIndex(String index) throws InvalidSlotException{
		currentIndex_s =index;
		try {
		currentIndex = (new Integer(index)).intValue();
		} catch (NumberFormatException e) {
			throw new InvalidSlotException(index+ "is not a valid index for default generator");
		}
		
	}
	
	public String getCurrentIndex() {
		return currentIndex_s;
	}
	
	public String getNextIndex() {
		int next = (new Integer(currentIndex_s)).intValue() +1;
		return (new Integer(next)).toString();
	}
	
	public String getPreviousIndex(){
		int previous = (new Integer(currentIndex_s)).intValue() -1;
		return (new Integer(previous)).toString();
	}
}
