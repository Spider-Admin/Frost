/*
  FcpFactory.java / Frost
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
import java.net.UnknownHostException;
import java.util.logging.Logger;

import frost.*;



public class FcpFactory {
	static NodeManager manager = null;

	private static Logger logger = Logger.getLogger(FcpFactory.class.getName());

	/**
	 * This method creates an instance of FcpConnection and handles errors.
	 * Returns either the connection, or null on any error.
	 */
	public static FcpConnection getFcpConnectionInstance() {
		if (manager == null) {
			if (Core.getNodes().size() == 1)
				manager = new SingleNodeManager();
			else
				manager = new RandomMultipleNodeManager(); //sick of debugging the other one

			manager.init();
		}
		FcpConnection connection = null;

		int tries = 0;
		//if we have more than one node, try each one at least once
		int maxTries = Core.getNodes().size() > 1 ? Core.getNodes().size() : 3;
		while (connection == null && tries < maxTries) {
			try {
				connection = manager.getConnection();
			} catch (ConnectionException e) {
				logger.warning(
					"FcpConnection.getFcpConnectionInstance: FcpTools.ConnectionException "
						+ e	+ " , this was try " + (tries + 1) + "/" + maxTries);
			} catch (FcpToolsException e) {
				logger.severe("FcpConnection.getFcpConnectionInstance: FcpToolsException " + e);
				break;
			} catch (UnknownHostException e) {
				logger.severe("FcpConnection.getFcpConnectionInstance: UnknownHostException " + e);
				break;
			} catch (java.net.ConnectException e) {
				/*  IOException java.net.ConnectException: Connection refused: connect  */
				logger.warning(
					"FcpConnection.getFcpConnectionInstance: java.net.ConnectException "
						+ e + " , this was try " + (tries + 1) + "/" + maxTries);
			} catch (IOException e) {
				logger.warning(
					"FcpConnection.getFcpConnectionInstance: IOException "
						+ e + " , this was try " + (tries + 1) + "/" + maxTries);
			}
			tries++;
			Mixed.wait(tries * 1250);
		}
		if (connection == null) {
			logger.warning(
				"ERROR: FcpConnection.getFcpConnectionInstance: Could not connect to node!");
		}
		return connection;
	}
}
