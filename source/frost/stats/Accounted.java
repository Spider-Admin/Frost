/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.stats;

/**
 * @author zlatinb
 *
 * Interface that things that we keep statistics on should implement
 */
public interface Accounted {
	
	/**
	 * @return the statistics object associated with this Accounted
	 */
	public Stat getStat();
}
