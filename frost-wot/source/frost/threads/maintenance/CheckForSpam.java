/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.util.*;
import java.util.logging.Logger;

import frost.*;
import frost.boards.*;
import frost.gui.objects.Board;
import frost.threads.BoardUpdateThread;


/**
 * @author $Author$
 * @version $Revision$
 */
public class CheckForSpam extends TimerTask
{
	private static Logger logger = Logger.getLogger(CheckForSpam.class.getName());
	
	private SettingsClass settings;
	private TofTree tofTree;
	private TofTreeModel tofTreeModel;
	
	/**
	 * @param settings
	 * @param tofTree
	 * @param tofTreeModel
	 */
	public CheckForSpam(SettingsClass settings, TofTree tofTree, TofTreeModel tofTreeModel) {
		this.settings = settings;
		this.tofTree = tofTree;
		this.tofTreeModel = tofTreeModel;
	}
	
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        if(settings.getBoolValue("doBoardBackoff"))
        {
            Iterator iter = tofTreeModel.getAllBoards().iterator();
            while (iter.hasNext())
            {
                Board current = (Board)iter.next();
                if (current.getBlockedCount() > settings.getIntValue("spamTreshold"))
                {
                    //board is spammed
                    logger.warning("######### board '" + current.getName() + "' is spammed, update stops for 24h ############");
                    current.setSpammed(true);
                    // clear spam status in 24 hours
                    Core.schedule(new ClearSpam(current),24*60*60*1000);

                    //now, kill all threads for board
                    Vector threads = tofTree.getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
                    Iterator i = threads.iterator();
                    while( i.hasNext() )
                    {
                        BoardUpdateThread thread = (BoardUpdateThread)i.next();
                        while( thread.isInterrupted() == false )
                            thread.interrupt();
                    }
                }
                current.resetBlocked();
            }
        }
    }
}