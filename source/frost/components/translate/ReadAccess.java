/*
  ReadAccess.java
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

package frost.components.translate;

import java.io.*;

public class ReadAccess {
    /**
     * Reads a file and returns its contents in a String
     */
    public static String readFile(File file)
    {
        return readFile(file.getPath());
    }
    public static String readFile(String path)
    {
        BufferedReader f;
        String line = new String();
        StringBuffer stringBuffer = new StringBuffer();

        try
	    {
		f = new BufferedReader(new FileReader(path));
		while( (line = f.readLine()) != null )
		    {
			stringBuffer.append(line);
			stringBuffer.append("\r\n");
		    }
		f.close();
	    }
        catch( IOException e )
	    {
		System.out.println("Read Error: " + path);
	    }
        return stringBuffer.toString();
    }
}
