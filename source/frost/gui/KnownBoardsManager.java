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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.boards.*;
import frost.storage.*;
import frost.storage.database.applayer.*;

/**
 * Manages the access to KnownBoards and hidden board names.
 * Static class, but one instance is created (Singleton) to
 * be able to implement the Savable interface for saving of
 * the hidden board names during shutdown of Frost.
 */
public class KnownBoardsManager implements Savable {

    private static final Logger logger = Logger.getLogger(KnownBoardsManager.class.getName());

    private static HashSet<String> hiddenNames = null;
    
    private static KnownBoardsManager instance = null;
    
    private KnownBoardsManager() {
    }

    // we need the instance only for Exitsavable!
    public static KnownBoardsManager getInstance() {
        if( instance == null ) {
            instance = new KnownBoardsManager();
        }
        return instance;
    }
    
    public static boolean isNameHidden(Board b) {
        String boardName = b.getName();
        return isNameHidden(boardName);
    }
    public static boolean isNameHidden(String n) {
        if( hiddenNames.contains(n.toLowerCase()) ) {
            return true;
        } else {
            return false;
        }
    }

    public static void addHiddenName(String n) {
        hiddenNames.add(n.toLowerCase());
    }
    public static void removeHiddenName(String n) {
        hiddenNames.remove(n.toLowerCase());
    }
    public static List<String> getHiddenNamesList() {
        return new ArrayList<String>(hiddenNames);
    }

    public static void initialize() {
        // load hidden names
        try {
            hiddenNames = AppLayerDatabase.getKnownBoardsDatabaseTable().loadHiddenNames();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving the hidden names", ex);
            hiddenNames = new HashSet<String>();
        }
    }

    public void save() throws StorageException {
        // save hidden names
        try {
            AppLayerDatabase.getKnownBoardsDatabaseTable().saveHiddenNames(hiddenNames);
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error storing the hidden names", ex);
        }
    }

    /**
     * @return  List of KnownBoard
     */
    public static List<KnownBoard> getKnownBoardsList() {
        try {
            return AppLayerDatabase.getKnownBoardsDatabaseTable().getKnownBoards();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving the known boards", ex);
        }
        return new LinkedList<KnownBoard>();
    }
    
    /**
     * Called with a list of Board, should add all boards that are not contained already
     * @param lst  List of Board
     */
    public static int addNewKnownBoards( List<Board> lst ) {
        if( lst == null || lst.size() == 0 ) {
            return 0;
        }
        int added = AppLayerDatabase.getKnownBoardsDatabaseTable().addNewKnownBoards(lst);
        return added;
    }

    public static void deleteKnownBoard(Board b) {
        try {
            AppLayerDatabase.getKnownBoardsDatabaseTable().deleteKnownBoard(b);
        } catch (SQLException e1) {
            logger.log(Level.SEVERE, "Error deleting known board", e1);
        }
    }
}
