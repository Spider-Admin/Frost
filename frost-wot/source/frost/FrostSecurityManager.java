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

import java.net.*;
import java.security.*;
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
    
    public static void main(String[] args) throws Throwable {
        System.setSecurityManager(new FrostSecurityManager());
        new Socket("localhost", 1234);
    }
    
    public void checkConnect(String host, int port, Object context) {
        System.out.println("c1:"+host+","+port);
        super.checkConnect(host, port, context);
//        throw new SecurityException("Connect to non-FCP host/port forbidden: "+host+":"+port);
    }
    public void checkConnect(String host, int port) {
        System.out.println("c1:"+host+","+port);
        super.checkConnect(host, port);
//        throw new SecurityException("Connect to non-FCP host/port forbidden: "+host+":"+port);
    }
    public void checkPermission(Permission arg0, Object arg1) {
        // we allow all that was not already denied by the direct method call (checkConnect)
    }
    public void checkPermission(Permission arg0) {
        // we allow all that was not already denied by the direct method call (checkConnect)
    }
}
