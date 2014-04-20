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

    protected final static int KEYTYPE_CHK = 0;
    protected final static int KEYTYPE_KSK = 1;
    protected final static int KEYTYPE_SSK = 2;
    protected final static int KEYTYPE_USK = 3;

    protected final static int KEYLEN_07_CHK = 99;
    protected final static int KEYLEN_07_SSK_PUB = 99;
    protected final static int KEYLEN_07_SSK_PRIV = 91;

    public static void initializeFor07() {
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
     * CHK keylength on 0.7 is 99 (including the CHK@).
     *
     * SSK/USK keylength on 0.7 is: pubkey:99  privkey:91
     *
     * KSK at least KSK@x : 5
     */
    public static boolean isValidKey(final String key) {

        if( key == null || key.length() < 5 ) { // at least KSK@x
            return false;
        }

        // check type
        int keytype = -1;
        for( int i = 0; i < getFreenetKeyTypes().length; i++ ) {
            if( key.startsWith(getFreenetKeyTypes()[i]) ) {
                // keytype found
                if( i == KEYTYPE_KSK ) {
                    return true; // KSK key is ok, length must be at least 5
                }
                keytype = i;
                break;
            }
        }
        if( keytype < 0 ) {
            // unknown keytype
            return false;
        }

        // get real keylength
        int length = key.length();
        final int pos = key.indexOf("/");
        if( pos > 0 ) {
            length -= (length-pos);
        }

        // check length
        boolean isKeyLenOk = false;
        if( keytype == KEYTYPE_CHK ) {
            if( length == KEYLEN_07_CHK ) {
                isKeyLenOk = true;
            }
        } else if( keytype == KEYTYPE_SSK || keytype == KEYTYPE_USK ) {
            if( length == KEYLEN_07_SSK_PRIV || length == KEYLEN_07_SSK_PUB ) {
                isKeyLenOk = true;
            }
        }
        return isKeyLenOk;
    }
}
