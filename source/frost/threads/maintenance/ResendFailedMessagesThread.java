/*
  ResendFailedMessagesThread.java / Frost
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.boards.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;


/**
 * @author $Author$
 * @version $Revision$
 */
public class ResendFailedMessagesThread extends Thread
{
    private static Logger logger = Logger.getLogger(ResendFailedMessagesThread.class.getName());

    private TofTree tofTree;
    private TofTreeModel tofTreeModel;

    /**
     * @param core
     * @param frameToLock
     */
    public ResendFailedMessagesThread(TofTree tofTree, TofTreeModel tofTreeModel)
    {
        this.tofTree = tofTree;
        this.tofTreeModel = tofTreeModel;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // give gui a chance to appear ... then start searching for unsent messages
        try { Thread.sleep(10000); } // wait 10 seconds
        catch(InterruptedException ex) { ; }
        if( isInterrupted() )
            return;

        logger.info("Starting search for unsent messages ...");

        ArrayList entries = FileAccess.getAllEntries(new File(Core.frostSettings.getValue("unsent.dir")),
                                                     ".xml");

        for( int i = 0; i < entries.size(); i++ )
        {
            File unsentMsgFile = (File)entries.get(i);
            if( unsentMsgFile.getName().startsWith("unsent") )
            {
                // Resend message
                MessageObjectFile mo = null;
                try {
                    mo = new MessageObjectFile(unsentMsgFile);
                } catch(Exception ex)
                {
                    logger.log(Level.SEVERE, "Couldn't read the message file, will not send message.", ex);
                }

                if( mo != null && mo.isValid() )
                {
                    Board board = tofTreeModel.getBoardByName( mo.getBoardName() );
                    if( board == null )
                    {
                        logger.warning("Can't resend Message '" + mo.getSubject() + "', the target board '" + mo.getBoardName() +
                                           "' was not found in your boardlist.");
                        // TODO: maybe delete msg? or it will always be retried to send
                        continue;
                    }
                    // message will be resigned before send, actual date/time will be used
                    // no more faking here :)
                    Identity recipient = null;
                    if( mo.getRecipientName() != null && mo.getRecipientName().length() > 0) {
                        recipient = Core.getInstance().getIdentities().getIdentity(mo.getRecipientName());
                        if( recipient == null ) {
                            logger.warning("Can't resend Message '" + mo.getSubject() + "', the recipient is not longer in your identites file!");
                            continue;
                        }
                    }
                    tofTree.getRunningBoardUpdateThreads().startMessageUpload(board, mo, null, recipient);
                    logger.info("Message '" + mo.getSubject() + "' will be resent to board '" + board.getName() + "'.");
                }
                // check if upload was successful before deleting the file -
                //  is not needed, the upload thread creates new unsent file
                unsentMsgFile.delete();
            }
        }
        logger.info("Finished search for unsent messages ...");
    }
}