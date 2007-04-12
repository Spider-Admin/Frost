/*
InputStreamThread.java / Frost
Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.ext;

import java.io.*;
import java.util.logging.*;

/**
 * Catches an Input Stream from a process
 * @author Jan-Thomas Czornack
 * @version 010711
 */
class InputStreamThread extends Thread {

    private static final Logger logger = Logger.getLogger(InputStreamThread.class.getName());

    Process p;
    Transit data;

    public void run() {

        StringBuilder output = new StringBuilder();
        DataInputStream dis = new DataInputStream(p.getInputStream());

        try {
            int result = 0;

            while((result = dis.read()) != -1) {
            output.append((char)result);
            }
            String s = output.toString().trim();
            if( s.length() > 0 ) {
                logger.info("Output from external program: "+s);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't get input stream.", e);
        }

        data.setString(output.toString());
    }

    /**
     * Constructor
     * @param p Process to get the Input Stream from
     */
    public InputStreamThread (Process p, Transit data) {
        this.p = p;
        this.data = data;
    }
}
