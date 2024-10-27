/*
  PerstKnownBoard.java / Frost
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
package frost.storage.perst;

import org.garret.perst.Persistent;

public class PerstKnownBoard extends Persistent {

	private static final long serialVersionUID = 1L;

    private String boardName;
    private String publicKey;
    private String privateKey;
    private String description;

    public PerstKnownBoard() {}
    
    public PerstKnownBoard(String bname, String pubkey, String privkey, String desc) {
        boardName = bname;
        publicKey = pubkey;
        privateKey = privkey;
        description = desc;
    }

    public String getBoardName() {
        return boardName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getDescription() {
        return description;
    }
}
