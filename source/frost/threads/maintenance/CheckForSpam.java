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
import frost.gui.objects.Board;
import frost.threads.BoardUpdateThread;


public class CheckForSpam extends TimerTask
{
	private static Logger logger = Logger.getLogger(CheckForSpam.class.getName());
	
	private final Core core;
	/**
	 * @param Core
	 */
	public CheckForSpam(Core core) {
		this.core = core;
		// TODO Auto-generated constructor stub
	}
    public void run()
    {
        if(Core.frostSettings.getBoolValue("doBoardBackoff"))
        {
            Iterator iter = MainFrame.getInstance().getTofTree().getAllBoards().iterator();
            while (iter.hasNext())
            {
                Board current = (Board)iter.next();
                if (current.getBlockedCount() > Core.frostSettings.getIntValue("spamTreshold"))
                {
                    //board is spammed
                    logger.warning("######### board '" + current.toString() + "' is spammed, update stops for 24h ############");
                    current.setSpammed(true);
                    // clear spam status in 24 hours
                    Core.schedule(new ClearSpam(core, current),24*60*60*1000);

                    //now, kill all threads for board
                    Vector threads = MainFrame.getInstance().getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
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