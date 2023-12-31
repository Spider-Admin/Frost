/*
  GlobalFileDownloader.java / Frost
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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frost.fcp.FcpHandler;
import frost.fcp.FcpResultGet;
import frost.util.FileAccess;

public class GlobalFileDownloader {

	private static final Logger logger = LoggerFactory.getLogger(GlobalFileDownloader.class);

    /**
     * Returns null if file not found.
     * Returns a GlobalFileDownloaderResult if File was downloaded, or if key was invalid.
     */
    public static GlobalFileDownloaderResult downloadFile(final String downKey, final int maxSize, final int maxRetries) {

        try {
            final File tmpFile = FileAccess.createTempFile("frost_",".tmp");
            tmpFile.deleteOnExit();

            final FcpResultGet fcpresults = FcpHandler.inst().getFile(
                    FcpHandler.TYPE_MESSAGE,
                    downKey,
                    null,
                    tmpFile,
                    maxSize,
                    maxRetries);

            if( fcpresults == null || !fcpresults.isSuccess() ) {
                // download failed
                tmpFile.delete();
                if( fcpresults != null
                        && fcpresults.getReturnCode() == 28
                        && downKey.startsWith("KSK@") )
                {
                    return new GlobalFileDownloaderResult(GlobalFileDownloaderResult.ERROR_EMPTY_REDIRECT); // invalid KSK key
                }
                else if( fcpresults != null
                        && fcpresults.getReturnCode() == 21 )
                {
                    return new GlobalFileDownloaderResult(GlobalFileDownloaderResult.ERROR_FILE_TOO_BIG);
                } else {
                    return null; // file not found
                }
            }
            return new GlobalFileDownloaderResult(tmpFile);

        } catch (final Throwable t) {
            logger.error("Error in downloadFile", t);
        }
        return null;
    }
}
