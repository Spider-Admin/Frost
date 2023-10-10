/*
  CheckHtmlIntegrity.java / Frost
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
package frost.gui.help;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Checks all HTML files in help.zip for 'http://', 'ftp://' links.
 * If those strings are found the help.zip is not used.
 *
 * @author bback
 */
public class CheckHtmlIntegrity {

    private static final Logger logger = Logger.getLogger(CheckHtmlIntegrity.class.getName());

    private boolean isHtmlSecure = false;

    /**
     * @return
     */
    public boolean isHtmlSecure() {
        return isHtmlSecure;
    }

    /**
     * @param fileName
     * @return
     */
    public boolean scanZipFile(String fileName) {

        File file = new File(fileName);

        if( !file.isFile() || (file.length() == 0) ) {
            logger.log(Level.SEVERE, "Zip file does not exist: "+file.getPath());
            return isHtmlSecure;
        }

        final byte[] zipData = new byte[4096];

        try {
            ZipFile zipFile = new ZipFile(file);
            try {
                final Enumeration<? extends ZipEntry> zipFileEntryEnumeration = zipFile.entries();
                while (zipFileEntryEnumeration.hasMoreElements()) {
                    final ZipEntry zipFileEntry = zipFileEntryEnumeration.nextElement();

                    final String zipFileEntryName = zipFileEntry.getName();
                    if (zipFileEntryName.endsWith(".html") || zipFileEntryName.endsWith(".htm")) {

                        InputStream zipFileEntryInputStream = zipFile.getInputStream(zipFileEntry);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int) zipFileEntry.getSize());
                        while (true) {
                            int len = zipFileEntryInputStream.read(zipData);
                            if (len < 0) {
                                break;
                            }
                            byteArrayOutputStream.write(zipData, 0, len);
                        }
                        zipFileEntryInputStream.close();

                        String htmlStr = new String(byteArrayOutputStream.toByteArray(), "UTF-8").toLowerCase();
                        if ((htmlStr.indexOf("http://") > -1) || (htmlStr.indexOf("ftp://") > -1) || (htmlStr.indexOf("nntp://") > -1)) {
                            logger.log(Level.SEVERE, "Unsecure HTML file in help.zip found: " + zipFileEntryName);
                            return isHtmlSecure;
                        }
                    }
                }
                // all files scanned, no unsecure found
                logger.log(Level.WARNING, "NO unsecure HTML file in help.zip found, all is ok.");
                isHtmlSecure = true;
            } finally {
                zipFile.close();
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Exception while reading help.zip. File is invalid.", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception while reading help.zip. File is invalid.", e);
        }
        return isHtmlSecure;
    }
}
