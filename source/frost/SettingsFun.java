/*
  SettingsFun.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost;

import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

public class SettingsFun
{
	private static Logger logger = Logger.getLogger(SettingsFun.class.getName());

    /**
     * Reads a settings file and returns the requested value
     * @param filename the filename of the settings file
     * @param value the requested value
     * @return String the requested value as a String
     */
    public static String getValue(File file, String value)
    {
        return getValue(file.getPath(), value);
    }

    public static String getValue(String filename, String value)
    {
        File checkFile = new File(filename);
        if( checkFile.isFile() )
        {
            Vector lines = FileAccess.readLines(filename);
            return getValue( lines, value );
        }
        return "";
    }

    /**
     * Returns the requested value in a settings vector
     * @param lines Vector containing lines of a settings file
     * @param value the requested value
     * @return String the requested value as a String
     */
    public static String getValue(Vector lines, String value)
    {
        for( int i = 0; i < lines.size(); i++ )
        {
            String line = (String)lines.elementAt(i);
            if( line.startsWith(value + "=") || line.startsWith(value + " ") )
            {
                if( line.indexOf("=") != -1 )
                {
                    return line.substring(line.indexOf("=") + 1, line.length()).trim();
                }
            }
        }
        logger.fine("Setting not found: " + value);
        return "";
    }

    /**
     * Takes a String and returns a boolean
     * @param value Boolean value in String format
     * @return boolean value
     */
    public static boolean stringToBoolean(String value)
    {
        if( (value.trim()).equals("true") )
            return true;
        return false;
    }

    /**
     * Takes a String and extracts all separated values separated by a space
     * @param values a String containing integer values
     * @return an array containing all extracted values
     */
    public static String[] getSeparatedValues(String values)
    {
        values = values.trim();
        Vector extrValues = new Vector();

        while( values.indexOf(" ") != -1 )
        {
            extrValues.add(values.substring(0, values.indexOf(" ")));
            values = (values.substring(values.indexOf(" ") + 1, values.length())).trim();
        }
        extrValues.add(values.trim());

        String[] returnValues = new String[extrValues.size()];
        for( int i = 0; i < extrValues.size(); i++ )
        {
            returnValues[i] = (String)extrValues.elementAt(i);
        }
        return returnValues;
    }

    /**
     * Takes a String and extracts all separated integer values separated by a space
     * @param values a String containing integer values
     * @return an array containing all extracted values
     */
    public static int[] getSeparatedIntegerValues(String values)
    {
        String[] stringValues = getSeparatedValues(values);
        int[] integerValues = new int[stringValues.length];
        for( int i = 0; i < stringValues.length; i++ )
        {
            try {
                integerValues[i] = Integer.parseInt(stringValues[i]);
            }
            catch( NumberFormatException e ) {
                integerValues[i] = 0;
            }
        }
        return integerValues;
    }
}
