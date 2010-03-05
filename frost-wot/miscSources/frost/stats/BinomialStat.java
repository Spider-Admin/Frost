/*
 * Created on Sep 9, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.stats;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface BinomialStat extends Stat {
	public void reportSuccess();
	public void reportFailure();
	public void report(boolean b);
	public int getSuccess();
	public int getFailures();
	public double getRatio();
}
