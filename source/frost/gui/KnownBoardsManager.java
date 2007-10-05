/*
  KnownBoardsManager.java / Frost
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
package frost.gui;

import java.util.*;

import frost.boards.*;
import frost.storage.perst.*;

/**
 * Manages the access to KnownBoards and hidden board names.
 * Static class, but one instance is created (Singleton) to
 * be able to implement the Savable interface for saving of
 * the hidden board names during shutdown of Frost.
 */
public class KnownBoardsManager {

    private static KnownBoardsManager instance = null;

    private KnownBoardsManager() {}

    // we need the instance only for Exitsavable!
    public static KnownBoardsManager getInstance() {
        if( instance == null ) {
            instance = new KnownBoardsManager();
        }
        return instance;
    }

    /**
     * @return  List of KnownBoard
     */
    public static List<KnownBoard> getKnownBoardsList() {
        return FrostFilesStorage.inst().getKnownBoards();
    }

    /**
     * Called with a list of Board, adds all boards which are not already in storage
     * @param lst  List of Board
     */
    public static int addNewKnownBoards( final List<Board> lst ) {
        if( lst == null || lst.size() == 0 ) {
            return 0;
        }
        final int added = FrostFilesStorage.inst().addNewKnownBoards(lst);
        return added;
    }

    /**
     * Deletes the known board from storage
     * @param b  board to delete from known boards list
     */
    public static void deleteKnownBoard(final Board b) {
        FrostFilesStorage.inst().deleteKnownBoard(b);
    }

    /**
     * Load all hidden board names.
     */
    public HashSet<String> loadHiddenBoardNames() {
        return FrostFilesStorage.inst().loadHiddenBoardNames();
    }

    /**
     * Save all hidden board names.
     */
    public void saveHiddenBoardNames(final HashSet<String> names) {
        FrostFilesStorage.inst().saveHiddenBoardNames(names);
    }
}
