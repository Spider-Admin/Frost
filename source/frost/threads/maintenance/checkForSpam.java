/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;


import frost.Core;
import frost.frame1;
import frost.gui.objects.FrostBoardObject;
import frost.threads.BoardUpdateThread;


public class checkForSpam extends TimerTask
{
	private final Core core;
	/**
	 * @param Core
	 */
	public checkForSpam(Core core) {
		this.core = core;
		// TODO Auto-generated constructor stub
	}
    public void run()
    {
        if(Core.frostSettings.getBoolValue("doBoardBackoff"))
        {
            Iterator iter = frame1.getInstance().getTofTree().getAllBoards().iterator();
            while (iter.hasNext())
            {
                FrostBoardObject current = (FrostBoardObject)iter.next();
                if (current.getBlockedCount() > Core.frostSettings.getIntValue("spamTreshold"))
                {
                    //board is spammed
                    System.out.println("######### board '"+current.toString()+"' is spammed, update stops for 24h ############");
                    current.setSpammed(true);
                    // clear spam status in 24 hours
                    Core.schedule(new ClearSpam(core, current),24*60*60*1000);

                    //now, kill all threads for board
                    Vector threads = frame1.getInstance().getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
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