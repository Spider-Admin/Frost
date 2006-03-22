/*
  getKeyThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.util.logging.Logger;

import frost.fcp.*;

/**
 * Reads a key from Freenet
 *
 * ATTN: this class is only used by the obsolete old non-fec splitfile download.
 *   Could maybe be removed completely.
 *
 * @author Jan-Thomas Czornack
 * @version 010711
 */
public class getKeyThread extends Thread
{
    private static Logger logger = Logger.getLogger(getKeyThread.class.getName());

    private String key;
    private File file;
    private int htl;
    private boolean[] results;
    private int index;
    private int checkSize;

//    // remove later
//    private static String[] keywords = {"Success",
//        "RouteNotFound",
//        "KeyCollision",
//        "SizeError",
//        "DataNotFound"};
//
//    // remove later
//    private static String[] result(String text) {
//        String[] result = new String[2];
//        result[0] = "Error";
//        result[1] = "Error";
//
//        for( int i = 0; i < keywords.length; i++ ) {
//            if( text.indexOf(keywords[i]) != -1 )
//                result[0] = keywords[i];
//        }
//        if( text.indexOf("CHK@") != -1 ) {
//            result[1] = text.substring(text.lastIndexOf("CHK@"), text.lastIndexOf("EndMessage"));
//            result[1] = result[1].trim();
//        } else {
//            result[1] = "Error";
//        }
//        return result;
//    }

    public void run() {

        logger.fine("Requesting " + file.getName() + " with HTL " + htl + ". Size is " + checkSize + " bytes.");

        boolean exception = false;
        FcpConnection connection = FcpFactory.getFcpConnectionInstance();
        if( connection != null ) {
            try {
                connection.getKeyToFile(key, file.getPath(), htl);
            } catch (FcpToolsException e) {
                exception = true;
            } catch (IOException e) {
                exception = true;
            }
        }

        if( !exception && file.length() > 0 ) {
            return; // we hope chunk download was OK ;)
        } else {
            // if we come here, something failed, delete file
            results[index] = false;
            file.delete();
        }
    }

    public getKeyThread (String key, File file, int htl, boolean[] results, int index, int checkSize) {
        this.key = key;
        this.file = file;
        this.htl = htl;
        this.results = results;
        this.index = index;
        this.checkSize = checkSize;
    }
}
