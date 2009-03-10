/*
  PerstHiddenFileOid.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.filelist;

import org.garret.perst.*;

/**
 * Holds the perst oid for of a hidden filelist file.
 */
public class PerstHiddenFileOid extends Persistent {
    int hiddenFileOid;

    public PerstHiddenFileOid() {}
    public PerstHiddenFileOid(final int i) {
        hiddenFileOid = i;
    }

    public int getHiddenFileOid() {
        return hiddenFileOid;
    }

    public void setHiddenFileOid(final int hiddenFileOid) {
        this.hiddenFileOid = hiddenFileOid;
    }
}