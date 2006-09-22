/*
  ClearSpam.java / Frost
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

import java.util.TimerTask;
import java.util.logging.Logger;

import frost.boards.*;

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