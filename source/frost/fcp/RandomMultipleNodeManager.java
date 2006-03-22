/*
  RandomMultipleNodesManager.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp;
import java.util.*;

/**
 * Selects nodes at random
 */
public class RandomMultipleNodeManager extends MultipleNodeManager {

    Vector nodes;
    Random r;

    /* (non-Javadoc)
     * @see frost.FcpTools.MultipleNodeManager#selectNode()
     */
    protected String selectNode() {
        if (nodes.size()==0) {
            throw new Error("all connections to nodes failed. Check your network settings and restart Frost.");
        }
        return (String) nodes.elementAt(r.nextInt(nodes.size()));
    }

    /* (non-Javadoc)
     * @see frost.FcpTools.MultipleNodeManager#delegateRemove(java.lang.String)
     */
    protected void delegateRemove(String s) {
        nodes.remove(s);
    }

    /* (non-Javadoc)
     * @see frost.FcpTools.NodeManager#init()
     */
    public void init() {
        nodes = new Vector(frost.Core.getNodes());
        r = new Random();
    }
}
