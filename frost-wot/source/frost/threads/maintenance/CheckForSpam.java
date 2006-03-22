/*
  CheckForSpam.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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

    public CheckForSpam(SettingsClass settings, TofTree tofTree, TofTreeModel tofTreeModel) {
        this.settings = settings;
        this.tofTree = tofTree;
        this.tofTreeModel = tofTreeModel;
    }

    public void run() {
        if( settings.getBoolValue("doBoardBackoff") ) {
            Iterator iter = tofTreeModel.getAllBoards().iterator();
            while( iter.hasNext() ) {
                Board current = (Board) iter.next();
                if( current.getBlockedCount() > settings.getIntValue("spamTreshold") ) {
                    //board is spammed
                    logger.warning("######### board '" + current.getName()
                            + "' is spammed, update stops for 24h ############");
                    current.setSpammed(true);
                    // clear spam status in 24 hours
                    Core.schedule(new ClearSpam(current), 24 * 60 * 60 * 1000);

                    //now, kill all threads for board
                    Vector threads = tofTree.getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
                    Iterator i = threads.iterator();
                    while( i.hasNext() ) {
                        BoardUpdateThread thread = (BoardUpdateThread) i.next();
                        while( thread.isInterrupted() == false )
                            thread.interrupt();
                    }
                }
                current.resetBlocked();
            }
        }
    }
}
