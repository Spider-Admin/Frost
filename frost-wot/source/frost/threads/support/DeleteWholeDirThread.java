/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.support;

import java.io.File;

import frost.Core;
import frost.FileAccess;


public class DeleteWholeDirThread extends Thread
{
	private final Core core;
    String delDir;
    public DeleteWholeDirThread(Core core, String dirToDelete)
    {
        delDir = dirToDelete;
		this.core = core;
    }
    public void run()
    {
        FileAccess.deleteDir( new File(delDir) );
    }
}