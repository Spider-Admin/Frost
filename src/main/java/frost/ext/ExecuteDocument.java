/*
ExecuteDocument.java / Frost
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
package frost.ext;

import java.io.File;
import java.io.IOException;

/**
 * @author saces
 *
 * running on
 * windoze: 'start' docname
 * kde: 'kfmclient exec' docname 
 * gnome: 'gnome-open' docname
 * mac-os : 'open' docname
 * gnustep : 'gopen' docname  
 *
 * thanks slinky for the mac/gnustep hints.
 */
public class ExecuteDocument {

	/**
	 * opens a document with the assigned app on many plattforms
	 * supports windoze 9x/me/ >= NT, mac x, kde, gnome, gnustep 
	 * @param document the name of the file to open
	 * @throws IOException 
	 */
	public static void openDocument(String document) throws IOException {
		openDocument(new File(document));
	}
	
	/**
	 * opens a document with the assigned app on many plattforms
	 * supports windoze 9x/me/ >= NT, kde, gnome, macos, gnustep 
	 * @param document the file to open
	 * @throws IOException 
	 */
	public static void openDocument(File document) throws IOException {
		String osn = System.getProperty("os.name").toLowerCase();
		
		if (osn.indexOf("windows") > -1) {
			
			String cmd;
			
			if ((osn.indexOf("9") > -1) || (osn.indexOf("me") > -1)) {
				cmd = "command.com";
			} else {
				cmd = "CMD";
			}

			Execute.runtimeExec(new String[] {cmd, "/k", "start", document.getCanonicalPath() });
			return;
		}
		
		if (osn.indexOf("mac") > -1) {
			Execute.runtimeExec(new String[] { "open", document.getCanonicalPath() });
			return;
		}
		
		// unix left
		// test for kde
		ExecResult r1 = Execute.run_wait(new String[] {"which", "kfmclient"});
		if (r1.retcode == 0) {
			Execute.runtimeExec(new String[] { "kfmclient", "exec", document.getCanonicalPath() });
			return;
		}
		// test for gnome
		ExecResult r2 = Execute.run_wait(new String[] {"which", "gnome-open"});
		if (r2.retcode == 0) {
			Execute.runtimeExec(new String[] { "gnome-open", document.getCanonicalPath() });
			return;
		}
		// test for gnustep
		ExecResult r3 = Execute.run_wait(new String[] {"which", "gopen"});
		if (r3.retcode == 0) {
			Execute.runtimeExec(new String[] { "gopen", document.getCanonicalPath() });
			return;
		}
		
		// hu, only 1,3254% of guis should arrive here ;)
		
	}

//	/**
//	 * right klick and run as app should open the help index
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		String osn = System.getProperty("os.name");
//		System.out.println(osn);
//		
//		try {
//			ExecuteDocument.openDocument("./help/index.html");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
}
