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

    protected final static int KEYLEN_05_CHK = 58;
    protected final static int KEYLEN_05_SSK_PUB = 35; // without an entropy in key
    protected final static int KEYLEN_05_SSK_PRIV = 31; // without an entropy in key ; can by 1 char more!
    protected final static int KEYLEN_05_SSK_PUB_LONG = 55; // with an entropy in key
    protected final static int KEYLEN_05_SSK_PRIV_LONG = 51; // with an entropy in key ; can by 1 char more!

    protected final static int KEYLEN_07_CHK = 99;
    protected final static int KEYLEN_07_SSK_PUB = 99;
    protected final static int KEYLEN_07_SSK_PRIV = 91;

    public static void initializeFor05() {
        freenetKeyTypes = new String[3];
        freenetKeyTypes[0] = "CHK@";
        freenetKeyTypes[1] = "KSK@";
        freenetKeyTypes[2] = "SSK@";
    }

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
     * CHK keylength on 0.5 is 58 (including the CHK@).
     * CHK keylength on 0.7 is 99 (including the CHK@).
     *
     * SSK keylength on 0.5 is: pubkey:35  privkey:31
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
        if( FcpHandler.isFreenet05() ) {
            if( keytype == KEYTYPE_CHK ) {
                if( length == KEYLEN_05_CHK ) {
                    isKeyLenOk = true;
                }
            } else if( keytype == KEYTYPE_SSK ) {
                if( length == KEYLEN_05_SSK_PRIV
                        || length == KEYLEN_05_SSK_PUB
                        || length == KEYLEN_05_SSK_PRIV_LONG
                        || length == KEYLEN_05_SSK_PUB_LONG
                        || length == KEYLEN_05_SSK_PRIV + 1
                        || length == KEYLEN_05_SSK_PRIV_LONG + 1
                        || length == KEYLEN_05_SSK_PUB + 1
                        || length == KEYLEN_05_SSK_PUB_LONG + 1
                   ) {
                    isKeyLenOk = true;
                }
            }
        } else if( FcpHandler.isFreenet07() ) {
            if( keytype == KEYTYPE_CHK ) {
                if( length == KEYLEN_07_CHK ) {
                    isKeyLenOk = true;
                }
            } else if( keytype == KEYTYPE_SSK || keytype == KEYTYPE_USK ) {
                if( length == KEYLEN_07_SSK_PRIV || length == KEYLEN_07_SSK_PUB ) {
                    isKeyLenOk = true;
                }
            }
        }
        return isKeyLenOk;
    }

//    /**
//     * Checks if the provided key is an old 0.7 key (before 1010).
//     * The old keys were only half encrypted!
//     * @param key  key to check (CHK)
//     * @return  true if the key is an old key
//     */
//    public static boolean isOld07ChkKey(final String key) {
//        if( key == null || key.length() < 4 ) {
//            return false; // invalid key
//        }
//        try {
//            if( key.startsWith("CHK@") ) {
//
//                // "CHK@GveS~6H2DnxWpcQL17CljJvmH6d7YicFHzoLvtUKzbk,6sxNSl6r1cl0LuLIsFA7C3oVJIEu4YuoWUJTa4bd7bY,AAIC--8/frost.jar"
//                // always ends with "," and 7 chars
//                // look at the first 3 of the 7 chars:
//                //  "AAE" = old key
//                //  "AAI" = new key
//
//                // find 2nd "," in string
//                int pos = key.indexOf(',');
//                pos = key.indexOf(',', pos+1);
//                pos++;
//                final String s = key.substring(pos, pos+7);
//                if( s.startsWith("AAE") ) {
//                    return true; // old
//                } else if( s.startsWith("AAI") ) {
//                    return false; // new
//                } else {
//                    return false; // invalid
//                }
//            } else {
//                return false; // invalid key type
//            }
//        } catch(final Throwable t) {
//            return false;
//        }
//    }
}
