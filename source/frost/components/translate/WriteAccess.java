/*
  WriteAccess.java
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
import java.util.logging.*;

public class WriteAccess {
	
	private static Logger logger = Logger.getLogger(WriteAccess.class.getName());
	
    /**
     * Writes a file "file" to "path"
     */
    public static void writeFile(String content, File file) {
	try {
	    FileOutputStream fos = new FileOutputStream(file);
	    Writer out = new OutputStreamWriter(fos, "UTF8");
	    out.write(content);
	    out.close();
	} catch (IOException e) {
		logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file)", e);
	}
    }
}
