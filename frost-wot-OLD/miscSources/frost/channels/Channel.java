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
 * This will eventually represent a channel.
 * I'll add more formal definition of types of channels on the website/docs  
 * Its about time we put them into objects..
 */
public interface Channel {

/**
 * 
 * @return the name of the channel to be used within Frost, not the address in Freenet
 */
public String getChannelName();

/**
 * 
 * @return each channel must have at least one entry point
 */
public String getEntrySlot();

/**
 * 
 * @return the freenet address of the current slot to be requested
 */
public String getCurrentRequestSlotKey();

/**
 * 
 * @return the freenet address of the current slot to be inserted to
 */
public String getCurrentInsertSlotKey();

/**
 * @param slot - the address of the slot
 * @param success - whether the slot was successfully requested
 * @throws InvalidSlotException - the naming convention of the slot doesn't match the channel
 */
public void setSlotSuccessful(String slot, boolean success) throws InvalidSlotException;
}
