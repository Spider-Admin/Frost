/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.util.TimerTask;
import java.util.logging.Logger;

import frost.Core;
import frost.gui.objects.FrostBoardObject;


class ClearSpam extends TimerTask
{
	private final Core core;
    private FrostBoardObject clearMe;
    
	private static Logger logger = Logger.getLogger(ClearSpam.class.getName());

    public ClearSpam(Core core, FrostBoardObject which) { clearMe = which;
	this.core = core; }
    public void run()
    {
        logger.info("############ clearing spam status for board '"+clearMe.toString()+"' ###########");
        clearMe.setSpammed(false);
    }
}