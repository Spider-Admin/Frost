/*
  NodeMessageListener.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp07;

import java.util.EventListener;

public interface NodeMessageListener extends EventListener {

    /**
     * Called to handle a generic node message.
     */
    public void handleNodeMessage(NodeMessage nm);
    
    /**
     * Called to handle a node message for a specific Identifier.
     */
    public void handleNodeMessage(String id, NodeMessage nm);
    
    /**
     * Called when the persistent connection was established (first connect or reconnect).
     */
    public void connected(); 

    /**
     * Called when the persistent connection was disconnected.
     */
    public void disconnected(); 
}
