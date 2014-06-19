/*
 FrostResourceBundleReader.java / Frost
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
package frost.util.gui.translation;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;

/**
 * Methods to read in a langres.properties file with UTF-8 encoding.
 * Aims to provide same load functionality as the Java Properties class.
 *
 * Lines can be continuated using a backslash at the very end of line.
 */
public class FrostResourceBundleReader {

    private static final Logger logger = Logger.getLogger(FrostResourceBundleReader.class.getName());

    /**
     * Loads the properties from a jarResource. Provide resource as '/i18n/langres.properties'.
     * This class is guaranteed to never return null.
     */
    public static Map<String,String> loadBundle(final String jarResource) {
        final InputStream input = MainFrame.class.getResourceAsStream(jarResource);
        if( input == null ) {
            // file not found in jar
            logger.severe("Resource not found in jar file: "+jarResource);
            return new HashMap<String,String>();
        }
        return loadBundle(input, jarResource);
    }

    /**
     * Loads the properties from a file.
     * This class is guaranteed to never return null.
     */
    public static Map<String,String> loadBundle(final File fileResource) {
        InputStream input;
        try {
            input = new FileInputStream(fileResource);
        } catch (final FileNotFoundException e) {
            logger.log(Level.SEVERE, "Could not open properties file: "+fileResource, e);
            return new HashMap<String,String>();
        }
        return loadBundle(input, fileResource.getPath());
    }

    private static Map<String,String> loadBundle(final InputStream inStream, final String resourceName) {

        final Map<String,String> bundle = new HashMap<String,String>();

        try {
            final BufferedReader rdr = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String wholeLine = null;
            while(true) {
                String line = rdr.readLine();
                if( line == null ) {
                    break; // EOF
                }
                if( line.startsWith("#") ) {
                    continue; // comment
                }

                if( isContinueLine(line) ) {
                    line = line.substring(0, line.length()-1); // remove trailing '\'
                    if( wholeLine == null ) {
                        wholeLine = line;
                    } else {
                        wholeLine += removeLeadingWhitespaces(line);
                    }
                    continue; // read next lines
                } else if( wholeLine != null ) {
                    // append last continued line
                    wholeLine += removeLeadingWhitespaces(line);
                } else {
                    wholeLine = line;
                }

                line = wholeLine;
                wholeLine = null;

                if( line.length() == 0 ) {
                    continue; // empty line
                }

                final int pos = line.indexOf('=');
                if( pos < 1 ) {
                    logger.severe("Invalid line in "+resourceName+": "+line);
                    continue;
                }
                String key, value;
                key = line.substring(0, pos).trim();
                value = line.substring(pos+1);
                if( key.length() == 0 ) {
                    logger.severe("Empty key in "+resourceName+": "+line);
                    continue;
                }
                value = loadConvert(value);
                bundle.put(key, value);
            }
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Error reading resource: "+resourceName, t);
            return bundle;
        }
        return bundle;
    }

    /**
     * Remove all leading whitespaces from a continued line.
     */
    private static String removeLeadingWhitespaces(final String str) {
        int x;
        for(x=0; x<str.length()-1; x++) {
            if( Character.isWhitespace(str.charAt(x)) ) {
                continue;
            } else {
                break;
            }
        }
        return str.substring(x);
    }

    /**
     * Returns true if this line is continued on next line (trailing backslash).
     */
    private static boolean isContinueLine(final String line) {
        int backslashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\')) {
            backslashCount++;
        }
        return (backslashCount % 2 == 1);
    }

    /**
     * Converts the strings \n, \r, \t and \f in the string into chars.
     */
    private static String loadConvert(final String str) {
        char aChar;
        final int len = str.length();
        final StringBuilder result = new StringBuilder(len);
        // note that uneven backslashes were removed from end of line (continuation),
        // hence the double ++ should work ;)
        for (int x=0; x<len; ) {
            aChar = str.charAt(x++);
            if (aChar == '\\') {
                aChar = str.charAt(x++);
                if (aChar == 't') {
                    aChar = '\t';
                } else if (aChar == 'r') {
                    aChar = '\r';
                } else if (aChar == 'n') {
                    aChar = '\n';
                } else if (aChar == 'f') {
                    aChar = '\f';
                }
                result.append(aChar); // append translated char OR one backslash if it was \\
            } else {
                result.append(aChar);
            }
        }
        return result.toString();
    }
}
