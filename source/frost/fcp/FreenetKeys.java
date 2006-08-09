/*
  FreenetKeys.java / Frost
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

public class FreenetKeys {

    private static String[] freenetKeyTypes = null;
    
    public static void initializeFor05() {
        // set valid key types
        freenetKeyTypes = new String[3];
        freenetKeyTypes[0] = "CHK@";
        freenetKeyTypes[1] = "KSK@";
        freenetKeyTypes[2] = "SSK@";
    }

    public static void initializeFor07() {
        // set valid key types
        freenetKeyTypes = new String[4];
        freenetKeyTypes[0] = "CHK@";
        freenetKeyTypes[1] = "KSK@";
        freenetKeyTypes[2] = "SSK@";
        freenetKeyTypes[3] = "USK@";
    }

    /**
     * Returns a list of key types valid for this version of freenet.
     */
    public static String[] getFreenetKeyTypes() {
        return freenetKeyTypes;
    }
    
    
    /**
     * Checks if provided String is valid. Valid means the String must
     * start with a supported keytype (CHK,...) and must have the
     * correct length for this freenet version.
     * 
     * Keylength on 0.5 is 58 (including the CHK@).
     * Keylength on 0.7 is 99 (including the CHK@).
     */
    public static boolean isValidKey(String key) {
        if( key == null || key.length() < 5 ) { // at least KSK@x
            return false;
        }
        // check type
        boolean isOk = false;
        for( int i = 0; i < getFreenetKeyTypes().length; i++ ) {
            if( key.startsWith(getFreenetKeyTypes()[i]) ) {
                isOk = true;
                if( i == 1 ) {
                    return true; // KSK key is ok, no length check
                }
                break;
            }
        }
        if( !isOk ) {
            return false;
        }
        // check length
        int length = key.length();
        int pos = key.indexOf("/");
        if( pos > 0 ) {
            length -= (length-pos);
        }
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
            if( length != 58 ) {
                return false;
            } else {
                return true;
            }
        }
        if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
            if( length != 99 ) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
