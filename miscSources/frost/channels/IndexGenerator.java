/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.channels;

/**
 * @author zlatinb
 *
 * Class that generates indices for vector channels.
 * The default is the sequence of natural numbers 0,1,2...
 * But people can add all kinds of sequences
 * If we ever decide to go with closed-source obfuscator this is the place
 */
public interface IndexGenerator {

/**
 * 
 * @param index implementations may require the current index to be set before giving the next one
 */
public void setCurrentIndex(String index) throws InvalidSlotException;

//gets the various indices
public String getCurrentIndex();
public String getNextIndex();
public String getPreviousIndex();
}
