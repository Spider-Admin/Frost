/*
  FcpPersistentConnectionTools.java / Frost
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.ext.*;

public class FcpPersistentConnectionTools {

    private static Logger logger = Logger.getLogger(FcpPersistentConnectionTools.class.getName());
    
    public boolean isInitialized() {
        return FcpPersistentConnection.isInitialized();
    }

    /**
     * No answer from node is expected.
     */
    public void changeRequestPriority(String id, int newPrio) {
        List<String> msg = new LinkedList<String>();
        msg.add("ModifyPersistentRequest");
        msg.add("Global=true");
        msg.add("Identifier="+id);
        msg.add("PriorityClass="+Integer.toString(newPrio));
        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }

    /**
     * No answer from node is expected.
     */
    public void removeRequest(String id) {
        List<String> msg = new LinkedList<String>();
        msg.add("RemovePersistentRequest");
        msg.add("Global=true");
        msg.add("Identifier="+id);
        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }
    
    /**
     * No answer from node is expected.
     */
    public void watchGlobal(boolean enabled) {
        List<String> msg = new LinkedList<String>();
        msg.add("WatchGlobal");
        msg.add("Enabled="+enabled);
        msg.add("VerbosityMask=1");
        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }

    /**
     * Starts a persistent put.
     */
    public void startPersistentPut(String id, File sourceFile, boolean doMime) {

        // else start a new request with DDA
        List<String> msg = getDefaultPutMessage(id, sourceFile, doMime);
        msg.add("UploadFrom=disk");
        msg.add("Filename=" + sourceFile.getAbsolutePath());
        
        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }
    
    public boolean isDDA() {
        return FcpPersistentConnection.getInstance().isDDA();
    }

    /**
     * Starts a new persistent get.
     * If DDA=false then the request is enqueued as DIRECT and must be retrieved manually
     * after the get completed successfully. Use startDirectPersistentGet to fetch the data.
     */
    public void startPersistentGet(String key, String id, File targetFile) {
        // start the persistent get. if DDA=false, then we have to fetch the file after the successful get from node
        List<String> msg = new LinkedList<String>();
        msg.add("ClientGet");
        msg.add("IgnoreDS=false");
        msg.add("DSOnly=false");
        msg.add("URI=" + key);
        msg.add("Identifier="+id );
        msg.add("MaxRetries=-1");
        msg.add("Verbosity=-1");
        msg.add("Persistence=forever");
        msg.add("Global=true");
        final int prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE);
        msg.add("PriorityClass="+Integer.toString(prio));
        
        if (isDDA()) {
            msg.add("ReturnType=disk");
            msg.add("Filename=" + targetFile.getAbsolutePath());
            File ddaTempFile = new File( targetFile.getAbsolutePath() + "-f");
            if( ddaTempFile.isFile() ) {
                // delete before download, else download fails, node will not overwrite anything!
                ddaTempFile.delete();
            }
            msg.add("TempFilename=" + ddaTempFile.getAbsolutePath());
         } else {
             msg.add("ReturnType=direct");
        }

        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }

    public void listPersistentRequests() {
        List<String> msg = new LinkedList<String>();
        msg.add("ListPersistentRequests");
        FcpPersistentConnection.getInstance().sendMessage(msg, true);
    }

    /**
     * Returns the common part of a put request, used for a put with type DIRECT and DISK together.
     */
    private List<String> getDefaultPutMessage(String id, File sourceFile, boolean doMime) {
        LinkedList<String> lst = new LinkedList<String>();
        lst.add("ClientPut");
        lst.add("URI=CHK@");
        lst.add("Identifier=" + id);
        lst.add("Verbosity=-1");        
        lst.add("MaxRetries=-1");
        lst.add("DontCompress=false"); // force compression
        lst.add("TargetFilename=");
        if (doMime) {
            lst.add("Metadata.ContentType=" + DefaultMIMETypes.guessMIMEType(sourceFile.getAbsolutePath()));
        } else {
            lst.add("Metadata.ContentType=application/octet-stream"); // force this to prevent the node from filename guessing due dda!
        }
        final int prio = Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE);
        lst.add("PriorityClass="+Integer.toString(prio));
        lst.add("Persistence=forever");
        lst.add("Global=true");
        return lst;
    }
    
    /**
     * Adds a file to the global queue DIRECT, means the file is transferred into the node completely.
     * Returns the first NodeMessage sent by the node after the transfer (PersistentPut or any error message).
     * After this method was called this connection is unuseable.
     */
    public NodeMessage startDirectPersistentPut(String id, File sourceFile, boolean doMime) throws IOException {
        
        FcpSocket newSocket = FcpSocket.create(FcpPersistentConnection.getInstance().getNodeAddress());
        if( newSocket == null ) {
            return null;
        }

        BufferedOutputStream dataOutput = new BufferedOutputStream(newSocket.getFcpSock().getOutputStream());
        
        List<String> msg = getDefaultPutMessage(id, sourceFile, doMime);
        msg.add("UploadFrom=direct");
        msg.add("DataLength=" + Long.toString(sourceFile.length()));
        msg.add("Data");
        
        for(Iterator<String> i=msg.iterator(); i.hasNext(); ) {
            String line = i.next();
            newSocket.getFcpOut().println(line);
        }
        newSocket.getFcpOut().flush();

        // write complete file to socket
        BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(sourceFile));
        while( true ) {
            int d = fileInput.read();
            if( d < 0 ) {
                break; // EOF
            }
            dataOutput.write(d);
        }
        fileInput.close();
        dataOutput.flush();

        // wait for a message from node
        // good: PersistentPut
        // -> IdentifierCollision {Global=true, Identifier=myid1} EndMessage
        NodeMessage nodeMsg = NodeMessage.readMessage(newSocket.getFcpIn());

        System.out.println("*PPUT** INFO - NodeMessage:");
        System.out.println((nodeMsg==null)?"(null)":nodeMsg.toString());
        
        dataOutput.close();

        newSocket.close();
        
        return nodeMsg;
    }

    /**
     * Retrieves a completed get from the node buffer DIRECT.
     * After this method was called this connection is unuseable.
     */
    public NodeMessage startDirectPersistentGet(String id, File targetFile) throws IOException {

        FcpSocket newSocket = FcpSocket.create(FcpPersistentConnection.getInstance().getNodeAddress());
        if( newSocket == null ) {
            return null;
        }

        newSocket.getFcpOut().println("GetRequestStatus");
        newSocket.getFcpOut().println("Global=true");
        newSocket.getFcpOut().println("Identifier=" + id);
        newSocket.getFcpOut().println("OnlyData=true");
        newSocket.getFcpOut().println("EndMessage");
        newSocket.getFcpOut().flush();

        // we expect an immediate answer
        NodeMessage nodeMsg = NodeMessage.readMessage(newSocket.getFcpIn());
        if( nodeMsg == null ) {
            return null;
        }

        System.out.println("*PGET** INFO - NodeMessage:");
        System.out.println(nodeMsg.toString());

        String endMarker = nodeMsg.getMessageEnd(); 
        if( endMarker == null ) {
            // should never happen
            logger.severe("*PGET** ENDMARKER is NULL! "+nodeMsg.toString());
            return null;
        }

        if( nodeMsg.isMessageName("AllData") && endMarker.equals("Data") ) {
            // data follow, first get datalength
            long dataLength = nodeMsg.getLongValue("DataLength");

            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[4096];
            long bytesLeft = dataLength;
            long bytesWritten = 0;
            int count;
            while( bytesLeft > 0 ) {
                count = newSocket.getFcpIn().read(b, 0, ((bytesLeft > b.length)?b.length:(int)bytesLeft));
                if( count < 0 ) {
                    break;
                } else {
                    bytesLeft -= count;
                }
                fileOut.write(b, 0, count);
                bytesWritten += count;
            }
            fileOut.close();
            System.out.println("*GET** Wrote "+bytesWritten+" of "+dataLength+" bytes to file.");

            newSocket.close();

            if( bytesWritten == dataLength ) {
                return nodeMsg;
            } else {
                return null;
            }
        } else {
            logger.severe("Invalid node answer, expected AllData: "+nodeMsg);
            newSocket.close();
            return null;
        }
    }
}
