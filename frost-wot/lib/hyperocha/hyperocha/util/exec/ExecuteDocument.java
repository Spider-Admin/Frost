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
 * auto.sh?
 *
#/bin/sh
#
# This file is part of JHyperochaUtilLib.
#

OD_KDE_CMD=`which kfmclient 2>/dev/null`

if [ -n "$OD_KDE_CMD" ]  
then
    #echo "KDE"
    $OD_KDE_CMD exec $@
else

OD_GNOME_CMD=`which gnome-open 2>/dev/null`
if [ -n "$OD_GNOME_CMD" ]   
then
    #echo "G"
    $OD_GNOME_CMD $@
else
    # weder kde noch gnome
    #echo "o nooo"
    exit -1
fi

fi      
 *
 *
 * 
 */
public class ExecuteDocument {
	
	/**
	 * opens a document with the assiozitätä app on all plattforms
	 * supports doze, kde, gnome, macos? 
	 * @param document
	 * @throws IOException 
	 */
	public static void openDocument(File document) throws IOException {
		// proposal
		String osn = System.getProperty("os.name");
		String cmd;
		
		if ("Windows".equalsIgnoreCase(osn)) {
			cmd = "start \"" + document.getAbsolutePath() + "\"";
			Runtime.getRuntime().exec(cmd);
			return;
		}
		
		if ("MacOs".equalsIgnoreCase(osn)) {
			cmd = "open \"" + document.getAbsolutePath() + "\"";
			Runtime.getRuntime().exec(cmd);
			return;
		}
		// unix left
		// test for kde
		ExecResult r1 = Execute.run_wait("which kfmclient");
		if (r1.retcode == 0) {
			cmd = r1.stdOut.toString() + " exec \"" + document.getAbsolutePath() + "\"";
			Runtime.getRuntime().exec(cmd);
			return;
		}
		// test for gnome
		ExecResult r2 = Execute.run_wait("which gnome-open");
		if (r2.retcode == 0) {
			cmd = r2.stdOut.toString() + " \"" + document.getAbsolutePath() + "\"";
			Runtime.getRuntime().exec(cmd);
			return;
		}
		
		// hu, only 1,3254% of guis should arrive here ;)
		
	}

	public static void main(String[] args) {
		String osn = System.getProperty("os.name");
		System.out.println(osn);
	}
	
}
