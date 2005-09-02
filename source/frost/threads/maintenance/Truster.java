/*
  Truster.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.boards.*;
import frost.identities.*;


/**
 * Thread is invoked if the Trust or NotTrust button is clicked.
 */
public class Truster extends Thread
{
	private FrostIdentities identities;

	private static Logger logger = Logger.getLogger(Truster.class.getName());
	
    private Boolean trust;
    private String from;
    
    public Truster(FrostIdentities newIdentities, Boolean what, String from) {
        trust=what;
		identities = newIdentities;
        this.from=Mixed.makeFilename(from);
    }
    
    // run repair one time!
    public static void repairIdentities(FrostIdentities identities) {
        // what is repaired? Due to a problem in BuddyList.remove the identities
        // are not completely correct.
        // test and fix: 
        //  - if someone is ( (GOOD or BAD) AND NEUTRAL) set it to GOOD or BAD
        //  - if someone is (GOOD AND BAD AND NEUTRAL) put it to GOOD
        HashSet allFroms = new HashSet();
        Set s = identities.getFriends().repairGetKeys();
        allFroms.addAll(s);
        s = identities.getNeutrals().repairGetKeys();
        allFroms.addAll(s);
        s = identities.getEnemies().repairGetKeys();
        allFroms.addAll(s);
        Iterator i = allFroms.iterator();
        while(i.hasNext()) {
            String from = (String)i.next();
            Identity ident = getIdentityFromAnyList(from, identities);

            boolean friend  = identities.getFriends().containsKey(from);
            boolean neutral = identities.getNeutrals().containsKey(from);
            boolean enemy   = identities.getEnemies().containsKey(from);
            
            if( neutral ) {
                if( enemy && friend ) {
                    removeIdentityFromAnyList(from, identities);
                    identities.getFriends().add(ident);
                } else if(enemy) {
                    removeIdentityFromAnyList(from, identities);
                    identities.getEnemies().add(ident);
                } else if(friend) {
                    removeIdentityFromAnyList(from, identities);
                    identities.getFriends().add(ident);
                }
            } else if( enemy && friend ) {
                removeIdentityFromAnyList(from, identities);
                identities.getFriends().add(ident);
            }
        }
    }
    
    private static Identity getIdentityFromAnyList(String ident, FrostIdentities idents) {
        Identity found = null;
        if( (found = idents.getNeutrals().get(ident)) != null ) {
            return found;
        }
        if( (found = idents.getFriends().get(ident)) != null ) {
            return found;
        }
        if( (found = idents.getEnemies().get(ident)) != null ) {
            return found;
        }
        return null;
    }

    private static void removeIdentityFromAnyList(String ident, FrostIdentities idents) {
        idents.getFriends().remove(ident);
        idents.getEnemies().remove(ident);
        idents.getNeutrals().remove(ident);
    }

    public void run()
    {
        Identity newIdentity;
        
        String newState;
        if( trust == null )  newState = "CHECK";
        else if( trust.booleanValue() == true ) newState = "GOOD";
        else newState = "BAD";
        logger.info("Truster: Setting '" + from + "' to state '" + newState + "'.");

        // don't change GOOD state for mySelf!
        String myId = Mixed.makeFilename(identities.getMyId().getUniqueName());
        if( from.equals(myId) && (trust == null || trust.booleanValue() != true) ) {
            logger.info("Truster: Ignored call to change my own ID to state '" + newState + "'.");
            return;
        }

        newIdentity = getIdentityFromAnyList(from, identities);

        if( newIdentity == null ) {
            logger.log(Level.SEVERE, "Truster: FROM not found in any list: "+from);
            return;
        }
        
        removeIdentityFromAnyList(from, identities);
        
        if( trust == null ) { 
            // set to neutral
            identities.getNeutrals().add( newIdentity );
            
        } else if( trust.booleanValue() == false ) {
            // set to bad
			identities.getEnemies().add( newIdentity );
            
        } else if( trust.booleanValue() == true ) {
            // set to good
			identities.getFriends().add( newIdentity );
        }

        // finally step through all board files, count new messages and show only wanted messages
        TOF.getInstance().initialSearchNewMessages();

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MainFrame.getInstance().tofTree_actionPerformed(null);
                } });
//        logger.info("Truster: Finished to update messages, set '" + from + "' to '" + newState+"'");
    }
/*
        if( trust == null ) { 
            // set to neutral
            newIdentity = identities.getFriends().get(from);
            if (newIdentity==null) newIdentity = identities.getEnemies().get(from);
            identities.getFriends().remove( from );
            identities.getEnemies().remove( from );
            identities.getNeutrals().add(newIdentity);
        } else if( trust.booleanValue() == false && identities.getFriends().containsKey(from) ) {
            // set friend to bad
            newIdentity = identities.getFriends().get(from);
            identities.getFriends().remove( from );
            identities.getEnemies().add( newIdentity );
        } else if( trust.booleanValue() == true && identities.getEnemies().containsKey(from) ) {
            // set enemy to good
            newIdentity = identities.getEnemies().get(from);
            identities.getEnemies().remove( newIdentity );
            identities.getFriends().add( newIdentity );
        } else {
            // new new enemy/friend
            newIdentity = identities.getNeutrals().get(from);
            if (newIdentity==null) logger.warning("neutral list not working :(");
            identities.getNeutrals().remove(newIdentity);
            if( trust.booleanValue() )
                identities.getFriends().add(newIdentity);
            else
                identities.getEnemies().add(newIdentity);
        }

        if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) ) {
            logger.warning("Truster - ERROR: could not get public key for '" + from + "'\n" +
                           "Truster: Will stop to set message states!!!");
            return;
        }
 */
        
        // get all .xml files in keypool
//        ArrayList entries = FileAccess.getAllEntries( new File(MainFrame.frostSettings.getValue("keypool.dir")),
//                                                   ".xml");
//        logger.info("Truster: Starting to update messages:");
//
//        for( int ii=0; ii<entries.size(); ii++ )
//        {
//            File msgFile = (File)entries.get(ii);
//            if (msgFile.getName().equals("files.xml")) continue;
//            if (msgFile.getName().equals("new_files.xml")) continue;
//            FrostMessageObject tempMsg = null;
//            try {
//            	tempMsg = FrostMessageFactory.createFrostMessageObject(msgFile);
//            }catch (MessageCreationException mce){
//            	if (mce.isEmpty()) {
//            		logger.log(Level.INFO, "A message could not be created. It is empty.", mce);
//            	} else {
//            		logger.log(Level.WARNING, "A message could not be created.", mce);
//            	}
//            }
//            if( tempMsg != null && tempMsg.getFrom().equals(from) &&
//                (
//                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.PENDING) ||
//                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) ||
//                  tempMsg.getStatus().trim().equals(VerifyableMessageObject.FAILED)
//                )
//              )
//            {
//                // check if message is correctly signed (compare complete user ids)
//                if( Mixed.makeFilename(newIdentity.getUniqueName()).equals( Mixed.makeFilename(tempMsg.getFrom()) ))
//                {
//                    // set new state of message
//                    if( trust == null )
//                        tempMsg.setStatus(VerifyableMessageObject.PENDING);
//                    else if( trust.booleanValue() )
//                        tempMsg.setStatus(VerifyableMessageObject.VERIFIED);
//                    else
//                        tempMsg.setStatus(VerifyableMessageObject.FAILED);
//                }
//                else
//                {
//                    logger.warning("!Truster: Could not verify message, maybe the message is faked!" +
//                                       " Message state set to TAMPERED for '" + msgFile.getPath() + "'.");
//                    tempMsg.setStatus(VerifyableMessageObject.TAMPERED);
//                }
//            }
//        }
}
