/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.*;

import swingwtx.swing.SwingUtilities;

import frost.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.identities.Identity;
import frost.messages.VerifyableMessageObject;


/**
 * Thread is invoked if the Trust or NotTrust button is clicked.
 */
public class Truster extends Thread
{
	private FrostIdentities identities;

	private static Logger logger = Logger.getLogger(Truster.class.getName());
	
    private Boolean trust;
    private Identity newIdentity;
    private String from;
    
    public Truster(FrostIdentities newIdentities, Boolean what, String from)
    {
        trust=what;
		identities = newIdentities;
        this.from=Mixed.makeFilename(from);
    }

    public void run()
    {
        String newState;

        if( trust == null )  newState = "CHECK";
        else if( trust.booleanValue() == true ) newState = "GOOD";
        else newState = "BAD";

        logger.info("Truster: Setting '" + from + "' to '" + newState + "'.");

        if( trust == null )
        {
         
        		newIdentity = identities.getFriends().get(from);
        		if (newIdentity==null) newIdentity = identities.getEnemies().get(from);
				identities.getFriends().remove( from );
				identities.getEnemies().remove( from );
				identities.getNeutrals().add(newIdentity);
        }
        else if( identities.getFriends().containsKey(from) && trust.booleanValue() == false )
        {
            // set friend to bad
            newIdentity = identities.getFriends().get(from);
			identities.getFriends().remove( from );
			identities.getEnemies().add( newIdentity );
        }
        else if( identities.getEnemies().containsKey(from) && trust.booleanValue() == true )
        {
            // set enemy to good
            newIdentity = identities.getEnemies().get(from);
			identities.getEnemies().remove( newIdentity );
			identities.getFriends().add( newIdentity );
        }
        else
        {
            // new new enemy/friend
            newIdentity = identities.getNeutrals().get(from);
            if (newIdentity==null) logger.warning("neutral list not working :(");
			identities.getNeutrals().remove(newIdentity);
            if( trust.booleanValue() )
				identities.getFriends().add(newIdentity);
            else
				identities.getEnemies().add(newIdentity);
        }

        if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) )
        {
            logger.warning("Truster - ERROR: could not get public key for '" + from + "'\n" +
            			   "Truster: Will stop to set message states!!!");
            return;
        }

        // get all .xml files in keypool
        ArrayList entries = FileAccess.getAllEntries( new File(MainFrame.frostSettings.getValue("keypool.dir")),
                                                   ".xml");
        logger.info("Truster: Starting to update messages:");

        for( int ii=0; ii<entries.size(); ii++ )
        {
            File msgFile = (File)entries.get(ii);
            if (msgFile.getName().equals("files.xml")) continue;
            if (msgFile.getName().equals("new_files.xml")) continue;
            FrostMessageObject tempMsg = null;
            try {
            	tempMsg = FrostMessageFactory.createFrostMessageObject(msgFile);
            }catch (Exception e){
				logger.log(Level.SEVERE, "Exception thrown in run()", e);
            }
            if( tempMsg != null && tempMsg.getFrom().equals(from) &&
                (
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.PENDING) ||
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) ||
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.FAILED)
                )
              )
            {
                // check if message is correctly signed
                if( Mixed.makeFilename(newIdentity.getUniqueName()).equals( Mixed.makeFilename(tempMsg.getFrom()) ))
                {
                    // set new state of message
                    if( trust == null )
                        tempMsg.setStatus(VerifyableMessageObject.PENDING);
                    else if( trust.booleanValue() )
                        tempMsg.setStatus(VerifyableMessageObject.VERIFIED);
                    else
                        tempMsg.setStatus(VerifyableMessageObject.FAILED);
                }
                else
                {
                    logger.warning("!Truster: Could not verify message, maybe the message is faked!" +
                                       " Message state set to N/A for '" + msgFile.getPath() + "'.");
                    tempMsg.setStatus(VerifyableMessageObject.NA);
                }
            }
        }
        // finally step through all board files, count new messages and delete new messages from enemies
        TOF.initialSearchNewMessages();

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MainFrame.getInstance().tofTree_actionPerformed(null);
                } });
        logger.info("Truster: Finished to update messages, set '" + from + "' to '" + newState+"'");
    }
}