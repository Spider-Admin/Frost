/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.util.TimerTask;
import java.util.logging.Logger;

import frost.gui.objects.Board;


/**
 * @author $Author$
 * @version $Revision$
 */
class ClearSpam extends TimerTask {

	private Board clearMe;

	private static Logger logger = Logger.getLogger(ClearSpam.class.getName());

	/**
	 * @param which
	 */
	public ClearSpam(Board which) {
		clearMe = which;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		logger.info("############ clearing spam status for board '" + clearMe.getName() + "' ###########");
		clearMe.setSpammed(false);
	}
}