/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.util.TimerTask;

import frost.Core;
import frost.gui.objects.FrostBoardObject;


class ClearSpam extends TimerTask
{
	private final Core core;
    private FrostBoardObject clearMe;

    public ClearSpam(Core core, FrostBoardObject which) { clearMe = which;
	this.core = core; }
    public void run()
    {
        System.out.println("############ clearing spam status for board '"+clearMe.toString()+"' ###########");
        clearMe.setSpammed(false);
    }
}