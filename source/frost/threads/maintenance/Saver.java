/*
  Saver.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.util.logging.Logger;

import frost.*;

public class Saver extends Thread
{
	private static Logger logger = Logger.getLogger(Saver.class.getName());
	
    private final Core core;
    
    public Saver(Core newCore)
    {
        core = newCore;
		Runtime.getRuntime().addShutdownHook(this);
    }

    /**
     * Called before exit of frost.
     */    
    public void exitSave()
    {
        // save gui state + config file
        Core.frostSettings.save();
    }

    /**
     * Called by Core autosave timer.
     * Saves all vital data.
     */
    public void autoSave()
    {
        core.getIdentities().save();
        core.saveBatches();
        core.saveKnownBoards();
        core.saveHashes();
        
        frame1.getInstance().getTofTree().saveTree();
        frame1.getInstance().getDownloadTable().save();
        frame1.getInstance().getUploadTable().save();
    }

    /** 
     * Called by shutdown hook.
     */ 
    public void run()
    {
        logger.info("Saving settings ...");
        
        autoSave();
        exitSave();
        FileAccess.cleanKeypool(frame1.keypool);
        
        logger.info("Bye!");
    }
}


