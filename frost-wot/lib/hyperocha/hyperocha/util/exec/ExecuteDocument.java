/**
 *   This file is part of JHyperochaUtilLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaUtilLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaUtilLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.util.exec;

import java.io.File;
import java.io.IOException;

/**
 * @author saces
 *
 * * dont forget quotes (space in path/docname)
 * "/usr/bin/kfmclient exec \"/path/to/doc.ext\""
 * 
 * running on
 * windoze: 'start' docname
 * kde: 'kfmclient exec' docname 
 * gnome: 'gnome-open' docname
 * mac-os : 'open' docname ??? guessed from google, no macos aviable :(
 *
 */
public class ExecuteDocument {
	
	public static void openDocument(String document) throws IOException {
		openDocument(new File(document));
	}
	
	/**
	 * opens a document with the assiozitätä app on all plattforms
	 * supports doze, kde, gnome, macos? 
	 * @param document
	 * @throws IOException 
	 */
	public static void openDocument(File document) throws IOException {
		String osn = System.getProperty("os.name").toLowerCase();
		String cmd;
		
		if (osn.indexOf("windows") != -1) {
			if ((osn.indexOf("9") != 1) || (osn.indexOf("me") != 1)) {
				cmd = "command.com";
			} else {
				cmd = "cmd.exe";
			}
			
			Runtime.getRuntime().exec(new String[] { cmd, "/c", "start", document.getCanonicalPath() });
			return;
		}
		
		// if (MacUser) {
		//     TODO / FIXME
		// }
		if (osn.indexOf("mac") != -1) {
			Runtime.getRuntime().exec(new String[] { "open", document.getCanonicalPath() });
			return;
		}
		
		// unix left
		// test for kde
		ExecResult r1 = Execute.run_wait(new String[] {"which", "kfmclient"});
		if (r1.retcode == 0) {
			Runtime.getRuntime().exec(new String[] { "kfmclient", "exec", document.getCanonicalPath() });
			return;
		}
		// test for gnome
		ExecResult r2 = Execute.run_wait(new String[] {"which", "gnome-open"});
		if (r2.retcode == 0) {
			Runtime.getRuntime().exec(new String[] { "gnome-open", document.getCanonicalPath() });
			return;
		}
		
		// hu, only 1,3254% of guis should arrive here ;)
		
	}

	/**
	 * right klick and run as app should open the help index
	 * @param args
	 */
	public static void main(String[] args) {
		String osn = System.getProperty("os.name");
		System.out.println(osn);
		
		try {
			ExecuteDocument.openDocument("./help/index.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
