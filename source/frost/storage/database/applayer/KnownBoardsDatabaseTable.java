/*
  KnownBoardsDatabaseTable.java / Frost
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

import frost.gui.objects.*;
import frost.storage.database.*;

public class KnownBoardsDatabaseTable extends AbstractDatabaseTable {

    private final static String SQL_DDL =
        "CREATE TABLE KNOWNBOARDS ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "boardname VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+  // "" empty tring means null, select of NULL does not work! 
        "privatekey VARCHAR NOT NULL,"+
        "description VARCHAR,"+
        "CONSTRAINT KNOWNBOARDS_1 UNIQUE (boardname,publickey,privatekey) )";
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_DDL);
        return lst;
    }

    private boolean insertKnownBoard(Board board) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "INSERT INTO KNOWNBOARDS "+
                "(boardname,publickey,privatekey,description) VALUES (?,?,?,?)");
        
        ps.setString(1, board.getName());
        ps.setString(2, (board.getPublicKey()==null?"":board.getPublicKey()));
        ps.setString(3, (board.getPrivateKey()==null?"":board.getPrivateKey()));
        ps.setString(4, board.getDescription());
        
        boolean insertWasOk = (ps.executeUpdate() == 1);

        ps.close();
        
        return insertWasOk;
    }
    
    public List getKnownBoards() throws SQLException {
        
        LinkedList knownBoards = new LinkedList();

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT boardname,publickey,privatekey,description FROM KNOWNBOARDS ORDER BY boardname");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String tmp;
            String boardname = rs.getString(1);
            tmp = rs.getString(2);
            String publickey = (tmp.length()==0?null:tmp);
            tmp = rs.getString(3);
            String privatekey = (tmp.length()==0?null:tmp);
            String description = rs.getString(4);
            
            Board b = new Board(boardname, publickey, privatekey, description);
            knownBoards.add(b);
        }
        rs.close();
        ps.close();
        
        return knownBoards;
    }
    
    public boolean deleteKnownBoard(Board b) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "DELETE FROM KNOWNBOARDS WHERE boardname=? AND publickey=? AND privatekey=?");
        
        ps.setString(1, b.getName());
        ps.setString(2, (b.getPublicKey()==null?"":b.getPublicKey()));
        ps.setString(3, (b.getPrivateKey()==null?"":b.getPrivateKey()));

        boolean deleteWasOk = (ps.executeUpdate() == 1);
        ps.close();

        return deleteWasOk;
    }
    
    /**
     * Called with a list of BoardAttachments, should add all boards
     * that are not contained already
     * @param lst
     */
    public void addNewKnownBoards( List lst ) {
        if( lst == null || lst.size() == 0 ) {
            return;
        }
        Iterator i = lst.iterator();
        while(i.hasNext()) {
            Board newb = (Board)i.next();
            try {
                insertKnownBoard(newb);
            } catch (SQLException e) {
                // duplicate board, ignore
            }
        }
    }
}
