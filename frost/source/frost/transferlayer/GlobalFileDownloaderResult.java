/*
  GlobalFileDownloaderResult.java / Frost
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
package frost.transferlayer;

import java.io.*;

public class GlobalFileDownloaderResult {

    public static final int ERROR_EMPTY_REDIRECT = 1;
    public static final int ERROR_FILE_TOO_BIG = 2;
    
    private File resultFile = null;
    
    private int errorCode = 0;
    
    public GlobalFileDownloaderResult(File result) {
        resultFile = result;
    }
    
    public GlobalFileDownloaderResult(int error) {
        errorCode = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public File getResultFile() {
        return resultFile;
    }
}
