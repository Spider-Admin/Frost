/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
                MessageObject mo = null;
                try {
                    mo = new MessageObject(unsentMsgFile);
                } catch(Exception ex)
                {
					logger.log(Level.SEVERE, "Couldn't read the message file, will not send message.", ex);
                }
                
                if( mo != null && mo.isValid() )
                {
                    Board board = tofTreeModel.getBoardByName( mo.getBoard() );
                    if( board == null )
                    {
                        logger.warning("Can't resend Message '" + mo.getSubject() + "', the target board '" + mo.getBoard() +
                                           "' was not found in your boardlist.");
                        // TODO: maybe delete msg? or it will always be retried to send
                        continue;
                    }
                    // message will be resigned before send, actual date/time will be used
                    // no more faking here :)
                    Identity recipient = null;
                    if( mo.getRecipient() != null && mo.getRecipient().length() > 0) {
                        recipient = Core.getInstance().getIdentities().getIdentity(mo.getRecipient());
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