/*
 FrostSecurityManager.java / Frost
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
package frost;

import java.security.*;
import java.util.*;

import frost.fcp.*;

/**
 * Our security manager does not allow any socket connections to other host:port
 * than the ones defined as FCP hosts in the configuration.
 *
 * This makes the HTML help save.
 *
 * We must allow hostnames, IPs of this hostnames and specified port or -1 for port.
 *
 * @author bback
 */
public class FrostSecurityManager extends SecurityManager {

    protected void checkFrostConnect(final String host, final int port) {
        final List<NodeAddress> nodes = FcpHandler.inst().getNodes();
        for( final NodeAddress nodeAddress : nodes ) {
            final NodeAddress na = nodeAddress;
            if( port < 0 ) {
                return; // allow DNS lookups
            }
            if( port == na.getPort() ) {
                if( host.equals(na.getHostIp()) || host.equals(na.getHostName()) ) {
                    return; // host:port is in our list
                }
            }
        }
        // host:port is not in our list
        throw new SecurityException("Connection to unconfigured FCP host/port forbidden: "+host+":"+port);
    }

    @Override
    public void checkConnect(final String host, final int port, final Object context) {
        checkFrostConnect(host, port);
        super.checkConnect(host, port, context);
    }
    @Override
    public void checkConnect(final String host, final int port) {
        checkFrostConnect(host, port);
        super.checkConnect(host, port);
    }
    @Override
    public void checkPermission(final Permission arg0, final Object arg1) {
        // we allow all that was not already denied by the direct method call (checkConnect)
    }
    @Override
    public void checkPermission(final Permission arg0) {
        // we allow all that was not already denied by the direct method call (checkConnect)
    }
}
