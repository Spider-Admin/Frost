/*
  Mixed.java / Frost
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
package frost.util;

import java.util.logging.*;

import org.joda.time.*;

import frost.*;

public final class Mixed {

    private static final Logger logger = Logger.getLogger(Mixed.class.getName());

    private static final char[] invalidChars = { '/', '\\', '?', '*', '<', '>', '\"', ':', '|', '#', '&' };

    public static void main(final String[] args) {
        System.out.println(createUniqueId());
    }

    /**
     * Creates a new unique ID.
     */
    public static String createUniqueId() {

        final StringBuilder idStrSb = new StringBuilder();
        idStrSb.append(Long.toString(System.currentTimeMillis())); // millis
        idStrSb.append(DateFun.FORMAT_DATE_EXT.print(new DateTime()));
        idStrSb.append(Long.toString(Runtime.getRuntime().freeMemory())); // free java mem
        idStrSb.append(DateFun.FORMAT_TIME_EXT.print(new DateTime()));
        final byte[] idStrPart = idStrSb.toString().getBytes();

        // finally add some random bytes
        final byte[] idRandomPart = new byte[64];
        Core.getCrypto().getSecureRandom().nextBytes(idRandomPart);

        // concat both parts
        final byte[] idBytes = new byte[idStrPart.length + idRandomPart.length];
        System.arraycopy(idStrPart, 0, idBytes, 0, idStrPart.length);
        System.arraycopy(idRandomPart, 0, idBytes, idStrPart.length-1, idRandomPart.length);

        final String uniqueId = Core.getCrypto().computeChecksumSHA256(idBytes);

        return uniqueId;
    }

    /**
     * Returns same as Integer.compareTo(), but without to create Integer objects.
     */
    public static int compareInt(final int i1, final int i2) {
        if( i1 < i2 ) {
            return -1;
        } else if( i1 > i2 ) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns same as Long.compareTo(), but without to create Long objects.
     */
    public static int compareLong(final long i1, final long i2) {
        if( i1 < i2 ) {
            return -1;
        } else if( i1 > i2 ) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Waits for a specific number of ms
     * @param time Time to wait in ms
     */
    public static void wait(final int time) {
        try {
            Thread.sleep(time);
        } catch (final InterruptedException e) {
        }
    }

    /**
     * Waits for a random number of millis in the range from 0 to maxMillis.
     * @param maxMillis  maximum wait time in ms
     */
    public static void waitRandom(final int maxMillis) {
        Mixed.wait( (int) (Math.random() * maxMillis) );
    }

    /**
     * Replaces characters that are not 0-9, a-z or in 'allowedCharacters'
     * with '_' and returns a lowerCase String
     *
     * NEW: does not allow the '#' char, because that will be used for internal folders
     *      in keypool, e.g. '#unsent#'
     *
     * @param text original String
     * @return modified String
     */
    public static String makeFilename(String text) {
        if (text == null) {
            logger.severe("ERROR: mixed.makeFilename() was called with NULL!");
            return null;
        }

        final StringBuilder newText = new StringBuilder();

        if (text.startsWith(".")) {
            newText.append("_"); // dont allow a boardfilename like "."
        }

        for( final char element : invalidChars ) {
            text = text.replace(element, '_');
        }

        newText.append(text);

        return newText.toString();
    }

    /**
     * Filters all non-english characters as well as those filtered by makeFilename
     * @param text the text to be filtered
     * @return the filtered text
     */
    public static String makeASCIIFilename(final String text){

        final StringBuilder newText = new StringBuilder();
        final String allowedCharacters = "()-!.";
        for (int i = 0; i < text.length(); i++)
              {
                   final int value = Character.getNumericValue(text.charAt(i));
                   final char character = text.charAt(i);
                   if ((value >= 0 && value < 36)
                       || allowedCharacters.indexOf(character) != -1) {
                    newText.append(character);
                } else {
                    newText.append("_");
                }
             }
        return makeFilename(newText.toString());  //run through the other filter just in case
    }

    public static boolean binaryCompare(final byte[] src, final int offs, final String searchTxt)
    {
        final int searchLen = searchTxt.length();
        for(int x=0; x < searchLen; x++)
        {
            final byte a = (byte)searchTxt.charAt(x);
            final byte b = src[offs+x];
            if( a != b )
            {
                return false;
            }
        }
        return true;
    }
}
