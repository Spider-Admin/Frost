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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks all HTML files for external links. If those are found in the help
 * files, the help will be disabled.
 */
public class CheckHtmlIntegrity {

	private static final Logger logger = LoggerFactory.getLogger(CheckHtmlIntegrity.class);

	private static List<Path> listHTMLFiles(String searchPath) throws IOException {
		try (Stream<Path> walk = Files.walk(Path.of(searchPath))) {
			return walk.filter(path -> Files.isRegularFile(path)).filter(new FilterHTMLExtension())
					.collect(Collectors.toList());
		}
	}

	public static Boolean check(String directory) {
		Boolean isOK = true;
		try {
			List<Path> htmlFiles = listHTMLFiles(directory);
			for (Path htmlFile : htmlFiles) {
				String content = Files.readString(htmlFile, StandardCharsets.UTF_8).toLowerCase();
				if (content.contains("http://") || content.contains("https://") || content.contains("ftp://")
						|| content.contains("nntp://")) {
					logger.warn("Unsecure HTML file found: {}", htmlFile);
					isOK = false;
				}
			}
		} catch (IOException e) {
			logger.error("IOException while checking HTML files in {}.", directory, e);
			isOK = false;
		}
		if (isOK) {
			logger.info("No unsecure HTML file was found in {}", directory);
		} else {
			logger.warn("Help will be deactivated.");
		}
		return isOK;
	}
}
