/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.io.File;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import frost.*;
import frost.gui.objects.FrostMessageObject;
import frost.identities.Identity;
import frost.messages.VerifyableMessageObject;


/**
 * Thread is invoked if the Trust or NotTrust button is clicked.
 */
public class Truster extends Thread
{
	private final Core core;
    private Boolean trust;
    private Identity newIdentity;
    private String from;
    public Truster(Core core, Boolean what, String from)
    {
        trust=what;
		this.core = core;
        this.from=mixed.makeFilename(from);
    }

    public void run()
    {
        String newState;

        if( trust == null )  newState = "CHECK";
        else if( trust.booleanValue() == true ) newState = "GOOD";
        else newState = "BAD";

        System.out.println("Truster: Setting '"+
                           from+
                           "' to '"+
                           newState+
                           "'.");

        if( trust == null )
        {
         
                Core.friends.remove( from );
                Core.enemies.remove( from );
                Core.getNeutral().Add(newIdentity);
        }
        else if( Core.friends.containsKey(from) && trust.booleanValue() == false )
        {
            // set friend to bad
            newIdentity = Core.friends.Get(from);
            Core.friends.remove( from );
            Core.enemies.Add( newIdentity );
        }
        else if( Core.enemies.containsKey(from) && trust.booleanValue() == true )
        {
            // set enemy to good
            newIdentity = Core.enemies.Get(from);
            Core.enemies.remove( newIdentity );
            Core.friends.Add( newIdentity );
        }
        else
        {
            // new new enemy/friend
            newIdentity = Core.getNeutral().Get(from);
            if (newIdentity==null) Core.getOut().println("neutral list not working :(");
            Core.getNeutral().remove(newIdentity);
            if( trust.booleanValue() )
                Core.friends.Add(newIdentity);
            else
                Core.enemies.Add(newIdentity);
        }

        if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) )
        {
            System.out.println("Truster - ERROR: could not get public key for '"+from+"'");
            System.out.println("Truster: Will stop to set message states!!!");
            return;
        }

        // get all .xml files in keypool
        ArrayList entries = FileAccess.getAllEntries( new File(frame1.frostSettings.getValue("keypool.dir")),
                                                   ".xml");
        System.out.println("Truster: Starting to update messages:");

        for( int ii=0; ii<entries.size(); ii++ )
        {
            File msgFile = (File)entries.get(ii);
            FrostMessageObject tempMsg = null;
            try {
            tempMsg = new FrostMessageObject( msgFile );
            }catch (Exception e){
            	e.printStackTrace(Core.getOut());
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
                if( mixed.makeFilename(newIdentity.getUniqueName()).equals( mixed.makeFilename(tempMsg.getFrom()) ))
                {
                    // set new state of message
                    if( trust == null )
                        tempMsg.setStatus(VerifyableMessageObject.PENDING);
                    else if( trust.booleanValue() )
                        tempMsg.setStatus(VerifyableMessageObject.VERIFIED);
                    else
                        tempMsg.setStatus(VerifyableMessageObject.FAILED);

                    System.out.print("."); // progress
                }
                else
                {
                    System.out.println("\n!Truster: Could not verify message, maybe the message is faked!" +
                                       " Message state set to N/A for '"+msgFile.getPath()+"'.");
                    tempMsg.setStatus(VerifyableMessageObject.NA);
                }
            }
        }
        // finally step through all board files, count new messages and delete new messages from enemies
        TOF.initialSearchNewMessages();

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame1.getInstance().tofTree_actionPerformed(null);
                } });
        System.out.println("\nTruster: Finished to update messages, set '"+from+"' to '"+
                           newState+"'");
    }
}