/*
  NodeManager.java / Frost
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

/**
 * 
 * @author zlatinb
 *
 * controls the available nodes
 */
interface NodeManager{
	
	public void init();
	
	/**
	 *  Creates a FcpConnection to one of the available Freenet nodes transparently to the user
	 * @return the FcpConnection
	 * @throws IOException when an IOException happens
	 * @throws FcpToolsException when an FcpTools exception happens ;-)
	 */
	public FcpConnection getConnection() throws IOException, FcpToolsException;
	
	/**
		 *  Creates a FcpConnection to one of the available Freenet nodes transparently to the user
		 * @param type The type of this connection.  Types are defined in FcpConnection
		 * @return the FcpConnection
		 * @throws IOException when an IOException happens
		 * @throws FcpToolsException when an FcpTools exception happens ;-)
		 */
		public FcpConnection getConnection(int type) throws IOException, FcpToolsException;	
}
