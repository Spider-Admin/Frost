/*
  BoardDatabaseTable.java / Frost
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
package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.gui.objects.*;
import frost.storage.database.*;

public class BoardDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(BoardDatabaseTable.class.getName());

    private final static String SQL_BOARDS_DDL =
        "CREATE TABLE BOARDS ("+
        "primkey INT NOT NULL,"+
        "boardname VARCHAR NOT NULL,"+
        "CONSTRAINT boards_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT UNIQUE_BOARDS_ONLY UNIQUE(boardname) )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_BOARDS_DDL);
        return lst;
    }
    
    /**
     * Adds a new board and returns the Board object filled with a primary key.
     */
    public synchronized Board addBoard(Board board) throws SQLException {
        
        Integer identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('BOARDS')");
        if( rs.next() ) {
            identity = new Integer(rs.getInt(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();
        
        board.setPrimaryKey(identity);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare("INSERT INTO BOARDS (primkey,boardname) VALUES (?,?)");
        ps.setInt(1, board.getPrimaryKey().intValue());
        ps.setString(2, board.getNameLowerCase());
        
        ps.executeUpdate();
        ps.close();
        
        return board;
    }

    /**
     * Removes a board, all referenced data (messages, files) will be removed too!
     */
    public void removeBoard(Board board) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare("DELETE FROM BOARDS WHERE primkey=?");
        
        ps.setInt(1, board.getPrimaryKey().intValue());
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public Hashtable loadBoards() throws SQLException {
        Hashtable ht = new Hashtable();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare("SELECT primkey,boardname FROM BOARDS");

        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            int ix=1;
            int primkey = rs.getInt(ix++);
            String bname = rs.getString(ix++);
            
            ht.put(bname, new Integer(primkey));
        }
        rs.close();
        ps.close();

        return ht;
    }
}
