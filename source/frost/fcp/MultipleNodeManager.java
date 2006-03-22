/*
  MultipleNodesManager.java / Frost
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

import java.io.IOException;
import java.util.logging.Logger;

public abstract class MultipleNodeManager implements NodeManager {

    private static Logger logger = Logger.getLogger(MultipleNodeManager.class.getName());

    /**
     * Method which may be overriden by implementations of different selection algorithms
     * @return [hostname][port] of selected node.
     */
    protected abstract String selectNode();

    /**
     * implementations may use iterators, so instead of removing directly
     * from the set we ask them to do it
     * @param s the node to be removed
     */
    protected abstract void delegateRemove(String s);

    public synchronized FcpConnection getConnection()  throws IOException, FcpToolsException {

        FcpConnection con = null;
        String nodeUnparsed;
        if (frost.Core.getNodes().size()==0) {
            throw new Error("No Freenet nodes available.  You need at least one");
        }
        if (frost.Core.getNodes().size()==1) {
            nodeUnparsed = (String) frost.Core.getNodes().iterator().next();
        } else {
            nodeUnparsed = selectNode();
        }
        String[] node = nodeUnparsed.split(":");
        logger.info("using node "+ node[0].trim()+" port "+node[1].trim()); //debug test splits
        try {
            con = new FcpConnection(node[0].trim(), node[1].trim());
        } catch (IOException e) {
            // for now, remove on the first failure.
            // FIXME: maybe we should give the node few chances?
            // also, should we remove it from the settings (i.e. forever)?
            delegateRemove(nodeUnparsed);
            throw e;
        } catch (FcpToolsException e) {
            // same here
            delegateRemove(nodeUnparsed);
            throw e;
        }
        return con;
    }
}
