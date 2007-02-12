/*
  NodeMessage.java / Frost
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
package frost.fcp.fcp07;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class NodeMessage {
    
    private static Logger logger = Logger.getLogger(NodeMessage.class.getName());

    private String messageName;
    private Hashtable<String,String> items;
    private String messageEndMarker = null;
    
    /////////////////////////////////////////////////////////////////////////////////////////
    // BEGIN OF STATIC FACTORY ///////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////
    
    public static NodeMessage readMessage(BufferedInputStream fcpInp) {

        NodeMessage result = null;
        String tmp;
        boolean isfirstline = true;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(128);
        
        while(true) {
            tmp = readLine(fcpInp, bytes);
            bytes.reset(); // reset for next run
            if (tmp == null) { break; }  // this indicates an error, io connection closed
            if ((tmp.trim()).length() == 0) { continue; } // an empty line

            if (isfirstline) {
                result = new NodeMessage(tmp);
                isfirstline = false;
                continue;
            }

            if (tmp.compareTo("Data") == 0) {
                result.setEnd(tmp);
                break; 
            }

            if (tmp.compareTo("EndMessage") == 0) {
                result.setEnd(tmp);
                break; 
            }
            
            if (tmp.indexOf("=") > -1) {
                String[] tmp2 = tmp.split("=", 2);
                result.addItem(tmp2[0], tmp2[1]);
            } else {
                logger.severe("ERROR: no '=' in message line. This shouldn't happen. FIXME. : " + tmp + " -> " + tmp.length());
                result.addItem("Unknown", tmp);
            }
        } 
        return result;  
    }

    private static String readLine(BufferedInputStream fcpInp, ByteArrayOutputStream bytes) {
        int c;
        try {
            while ((c = fcpInp.read()) != '\n' && c != -1 && c != '\0' ) {
                bytes.write(c);
            }
            String str = new String(bytes.toByteArray(), "UTF-8");
            if (str.length() == 0) {
                str = null;
            }
            return str;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Throwable catched", e);
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // END OF STATIC FACTORY ////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new NodeMessage.
     */
    protected NodeMessage(String name) {
        messageName = name;
        items = new Hashtable<String,String>();
    }

    /** 
     * returns the message as string for debug/log output
     */
    public String toString() {
        return messageName + " " + items + " " + messageEndMarker;
    }
    
    protected void setItem(String name, String value) {
        items.put(name, value);
    }
    
    protected void setEnd(String em) {
        messageEndMarker = em;
    }
    
    public String getMessageName() {
        return messageName;
    }

    public String getMessageEnd() {
        return messageEndMarker;
    }

    public boolean isMessageName(String aName) {
        if (aName == null) {
            return false;
        }
        return aName.equalsIgnoreCase(messageName);
    }
    public boolean isValueSet(String name) {
        return items.get(name) != null;
    }
    public String getStringValue(String name) {
        return items.get(name);
    }
    public long getLongValue(String name) {
        return Long.parseLong(items.get(name));
    }
    public int getIntValue(String name) {
        return Integer.parseInt(items.get(name));
    }
    public boolean getBoolValue(String name) {
        return "true".equalsIgnoreCase(items.get(name));
    }
    
    public void addItem(String key, String value) {
        items.put(key, value); 
    }
}
