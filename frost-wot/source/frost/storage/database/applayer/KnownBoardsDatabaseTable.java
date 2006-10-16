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

import frost.boards.*;
import frost.gui.*;
import frost.storage.database.*;

public class KnownBoardsDatabaseTable extends AbstractDatabaseTable {

    private final static String SQL_DDL =
        "CREATE TABLE KNOWNBOARDS ("+
        "primkey BIGINT DEFAULT UNIQUEKEY('KNOWNBOARDS') NOT NULL,"+
        "boardname VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+  // "" empty tring means null, select of NULL does not work! 
        "privatekey VARCHAR NOT NULL,"+
        "description VARCHAR,"+
        "CONSTRAINT kb_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT KNOWNBOARDS_1 UNIQUE (boardname,publickey,privatekey) )";

    private final static String SQL_DDL2 =
        "CREATE TABLE HIDDENBOARDNAMES (boardname VARCHAR NOT NULL)";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(2);
        lst.add(SQL_DDL);
        lst.add(SQL_DDL2);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE KNOWNBOARDS");
        stmt.executeUpdate("COMPACT TABLE HIDDENBOARDNAMES");
        return true;
    }
    
    public HashSet loadHiddenNames() throws SQLException {
        HashSet names = new HashSet();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT boardname FROM HIDDENBOARDNAMES");
        while(rs.next()) {
            String bName = rs.getString(1);
            names.add(bName);
        }
        rs.close();
        stmt.close();
        
        return names;
    }

    public void saveHiddenNames(HashSet names) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement stmt = db.createStatement();
        stmt.executeUpdate("DELETE FROM HIDDENBOARDNAMES"); // delete all
        stmt.close();
        
        PreparedStatement ps = db.prepare("INSERT INTO HIDDENBOARDNAMES (boardname) VALUES (?)");
        for(Iterator i = names.iterator(); i.hasNext(); ) {
            String bName = (String) i.next();
            ps.setString(1, bName);
            ps.executeUpdate();
        }
        ps.close();
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

    /**
     * @return  List of KnownBoard
     */
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
            
            KnownBoard b = new KnownBoard(boardname, publickey, privatekey, description);
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
     * Called with a list of Board, should add all boards that are not contained already
     * @param lst  List of Board
     */
    public int addNewKnownBoards( List lst ) {
        if( lst == null || lst.size() == 0 ) {
            return 0;
        }
        Iterator i = lst.iterator();
        int added = 0;
        while(i.hasNext()) {
            Board newb = (Board)i.next();
            try {
                insertKnownBoard(newb);
                added++;
            } catch (SQLException e) {
                // duplicate board, ignore
            }
        }
        return added;
    }
}
