package frost.fcp;

import java.net.*;

public class NodeAddress {
    public InetAddress host = null;
    public int port = -1;
    public String hostName = null; // avoid name lookup recursion in security manager
    public String hostIp = null; // avoid name lookup recursion in security manager
}