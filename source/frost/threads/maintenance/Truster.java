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

import frost.Core;
import frost.FileAccess;
import frost.TOF;
import frost.frame1;
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
    private VerifyableMessageObject currentMsg;

    public Truster(Core core, Boolean what, VerifyableMessageObject msg)
    {
        trust=what;
		this.core = core;
        currentMsg = msg;
    }

    public void run()
    {
        String from = currentMsg.getFrom();
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
            // set enemy/friend to CHECK
            newIdentity = Core.friends.Get(from);
            if( newIdentity==null )
                newIdentity=Core.enemies.Get(from);

          //  if( newIdentity == null ) // not found -> paranoia
          //  {
          //      newIdentity = new Identity(currentMsg.getFrom(), currentMsg.getKeyAddress());
          //  }
          //  else
          //  {
                Core.friends.remove( from );
                Core.enemies.remove( from );
                Core.getNeutral().Add(newIdentity);
          //  }
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
            Core.getNeutral().remove(newIdentity);
            if( trust.booleanValue() )
                Core.friends.Add(newIdentity);
            else
                Core.enemies.Add(newIdentity);
        }

        if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) )
        {
            System.out.println("Truster - ERROR: could not get public key for '"+currentMsg.getFrom()+"'");
            System.out.println("Truster: Will stop to set message states!!!");
            return;
        }

        // get all .txt files in keypool
        ArrayList entries = FileAccess.getAllEntries( new File(frame1.frostSettings.getValue("keypool.dir")),
                                                   ".txt");
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
            if( tempMsg != null && tempMsg.getFrom().equals(currentMsg.getFrom()) &&
                (
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.PENDING) ||
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) ||
                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.FAILED)
                )
              )
            {
                // check if message is correctly signed
                if( newIdentity.getUniqueName().equals( tempMsg.getFrom() ) && //uniqueName and CHK are hashes
                    Core.getCrypto().verify(tempMsg.getContent(), newIdentity.getKey()) )
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
        System.out.println("\nTruster: Finished to update messages, set '"+currentMsg.getFrom()+"' to '"+
                           newState+"'");
    }
}