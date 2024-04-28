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
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks all HTML files in help.zip for external links. If those are found the
 * help.zip is not used.
 *
 * @author bback
 */
public class CheckHtmlIntegrity {

	private static final Logger logger = LoggerFactory.getLogger(CheckHtmlIntegrity.class);

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

		if (!file.isFile() || (file.length() == 0)) {
			logger.error("Zip file does not exist: {}", file.getPath());
			return isHtmlSecure;
		}

		final byte[] zipData = new byte[4096];
		try (ZipFile zipFile = new ZipFile(file);) {
			final Enumeration<? extends ZipEntry> zipFileEntryEnumeration = zipFile.entries();
			while (zipFileEntryEnumeration.hasMoreElements()) {
				final ZipEntry zipFileEntry = zipFileEntryEnumeration.nextElement();
				final String zipFileEntryName = zipFileEntry.getName();
				if (zipFileEntryName.endsWith(".html") || zipFileEntryName.endsWith(".htm")) {
					String htmlStr = "";
					try (InputStream zipFileEntryInputStream = zipFile.getInputStream(zipFileEntry);
							ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
									(int) zipFileEntry.getSize());) {
						while (true) {
							int len = zipFileEntryInputStream.read(zipData);
							if (len < 0) {
								break;
							}
							byteArrayOutputStream.write(zipData, 0, len);
						}
						htmlStr = new String(byteArrayOutputStream.toByteArray(), "UTF-8").toLowerCase();
					}

					if ((htmlStr.indexOf("http://") > -1) || (htmlStr.indexOf("https://") > -1)
							|| (htmlStr.indexOf("ftp://") > -1) || (htmlStr.indexOf("nntp://") > -1)) {
						logger.warn("Unsecure HTML file in {} found: {}", fileName, zipFileEntryName);
						return isHtmlSecure;
					}
				}
			}
			// all files scanned, no unsecure found
			logger.info("NO unsecure HTML file in {} found, all is ok.", fileName);
			isHtmlSecure = true;
		} catch (IOException e) {
			logger.error("Exception while reading {}. File is invalid.", fileName, e);
		}
		return isHtmlSecure;
	}
}
