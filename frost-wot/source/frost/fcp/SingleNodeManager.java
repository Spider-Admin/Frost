/*
  SingleNodeManager.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import frost.Core;
/**
 * @author zlatinb
 *
 * Creates connections to a single node
 */
public final class SingleNodeManager implements NodeManager {
	final static boolean DEBUG = false;
	String nodeUnparsed;
	String[] nodeParsed;
	public void init() {
		nodeUnparsed = (String) Core.getNodes().iterator().next();
		nodeParsed = nodeUnparsed.split(":");
	}

	public FcpConnection getConnection()
		throws IOException, FcpToolsException {
		return new FcpConnection(nodeParsed[0], nodeParsed[1]);

	}
	
	//we have only one node, so type doesn't matter...
	public FcpConnection getConnection(int type)
		throws IOException, FcpToolsException {
			return getConnection();
		} 

}
