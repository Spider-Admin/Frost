/*
  SmileyCache.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.util.Hashtable;

import javax.swing.ImageIcon;

import frost.util.gui.textpane.Smileys;

public class SmileyCache implements Smileys {

    protected static Hashtable<String,ImageIcon> smileyCache = new Hashtable<String,ImageIcon>();

    public static synchronized ImageIcon getCachedSmiley(final int i) {
        final String si = Integer.toString(i);
        ImageIcon ii = smileyCache.get(si);
        if( ii == null ) {
            ii = MiscToolkit.loadImageIcon("/data/smileys/"+i+".gif");
            smileyCache.put(si, ii);
        }
        return ii;
    }

    public static synchronized void clearCachedSmileys() {
        for(final ImageIcon i : smileyCache.values()) {
            i.getImage().flush();
        }
        smileyCache.clear();
    }

    public static int getSmileyCount() {
        return SMILEYS.length;
    }

    public static String getSmileyText(final int i) {
        return SMILEYS[i][0];
    }
}
