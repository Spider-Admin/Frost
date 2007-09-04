/*
  SentMessagesManager.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messages;

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.storage.perst.*;
import frost.storage.perst.messages.*;

public class SentMessagesManager {

    private static final Logger logger = Logger.getLogger(SentMessagesManager.class.getName());
    
    public static List<FrostMessageObject> retrieveSentMessages() {
        try {
            return MessageStorage.inst().retrieveAllSentMessages(MainFrame.getInstance().getTofTreeModel().getAllBoards());
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error retrieving sent messages", t);
        }
        
        return new LinkedList<FrostMessageObject>();
    }
    
    public static boolean addSentMessage(FrostMessageObject sentMo) {
        try {
            MessageStorage.inst().addSentMessage(sentMo);
        } catch (Throwable e) {
            // paranoia
            logger.log(Level.SEVERE, "Error inserting sent message", e);
        }
        MainFrame.getInstance().getSentMessagesPanel().addSentMessage(sentMo);
        return true; // if we return false the msg is resend !
    }
    
    public static int deleteSentMessages(List<FrostMessageObject> msgObjects) {
        int deleted = 0;
        try {
            deleted = MessageStorage.inst().deleteSentMessages(msgObjects);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error deleting sent messages", t);
        }
        return deleted;
    }
}
