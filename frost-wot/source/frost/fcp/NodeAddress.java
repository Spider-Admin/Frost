/*
  NodeAddress.java / Frost
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
package frost.fcp;

import java.net.*;

public class NodeAddress {

    private InetAddress host = null;
    private int port = -1;
    private String hostName = null; // avoid name lookup recursion in security manager
    private String hostIp = null; // avoid name lookup recursion in security manager

    // for 0.7: during first connect test if DDA is possible
    private boolean isDirectDiskAccessTested = false;
    private boolean isDirectDiskAccessPossible = false;

    public NodeAddress(final InetAddress host, final int port, final String hostName, final String hostIp) {
        this.host = host;
        this.port = port;
        this.hostName = hostName;
        this.hostIp = hostIp;
    }

    public InetAddress getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getHostName() {
        return hostName;
    }
    public String getHostIp() {
        return hostIp;
    }

    public boolean isDirectDiskAccessTested() {
        return isDirectDiskAccessTested;
    }
    public void setDirectDiskAccessTested(final boolean isDirectDiskAccessTested) {
        this.isDirectDiskAccessTested = isDirectDiskAccessTested;
    }
    public boolean isDirectDiskAccessPossible() {
        return isDirectDiskAccessPossible;
    }
    public void setDirectDiskAccessPossible(final boolean isDirectDiskAccessPossible) {
        this.isDirectDiskAccessPossible = isDirectDiskAccessPossible;
    }
}