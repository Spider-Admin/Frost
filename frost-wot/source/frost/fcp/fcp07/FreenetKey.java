/*
  FreenetKey.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

public class FreenetKey {

    public static final int CHK = 0;
    public static final int KSK = 1;
    public static final int MSK = 2;
    public static final int SSK = 3;

    private int keyType;
    private String keyString;

    public FreenetKey(String key) {
    	keyType = getKeyType(key);
    	keyString = key;
    }

    private static int getKeyType(String key)
    {
    	String freenetPrefix = "freenet:";
    	int keyType;

    	if (key.startsWith(freenetPrefix)) {
    	    key = key.substring(freenetPrefix.length());
    	}

    	// determine the key type, assume a KSK
    	if (key.startsWith("CHK")) {
    	    keyType = CHK;
        } else if (key.startsWith("SSK")) {
    	    keyType = SSK;
        } else if (key.startsWith("MSK")) {
    	    keyType = MSK;
        } else {
    	    keyType = KSK;
        }

    	return keyType;
    }

    public String getKeyString() {
        return keyString;
    }

    public String toString() {
        return getKeyString();
    }

    public int getKeyType() {
        return keyType;
    }
}
