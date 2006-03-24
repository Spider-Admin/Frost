/*
  CheckHtmlIntegrity.java / About Box
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

/**
 * Checks all HTML files in help.zip for 'http://', 'ftp://' links.
 * If those strings are found the help.zip is not used.
 * 
 * @author bback
 */
public class CheckHtmlIntegrity {
    
    private static boolean isHtmlSecure = false;
    
    public static boolean isHtmlSecure() {
        return isHtmlSecure;
    }

}
