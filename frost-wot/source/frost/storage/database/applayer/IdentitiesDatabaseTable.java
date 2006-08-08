/*
 IdentitiesDatabaseTable.java / Frost
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

import frost.identities.*;
import frost.storage.database.*;

public class IdentitiesDatabaseTable extends AbstractDatabaseTable {

    private final static String SQL_IDENTITIES_DDL =
        "CREATE TABLE IDENTITIES ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "uniquename VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+
        "lastseen TIMESTAMP NOT NULL,"+
        "state INT NOT NULL,"+
        "CONSTRAINT IDENTITIES_1 UNIQUE (uniquename) )";

    private final static String SQL_OWN_IDENTITIES_DDL =
        "CREATE TABLE OWNIDENTITIES ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "uniquename VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+
        "privatekey VARCHAR NOT NULL,"+
        "CONSTRAINT OWNIDENTITIES_1 UNIQUE (uniquename) )";
    
    private final static String SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL =
        "CREATE TABLE OWNIDENTITIESLASTFILESSHARED ("+
        "uniquename VARCHAR NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "lastshared BIGINT )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_IDENTITIES_DDL);
        lst.add(SQL_OWN_IDENTITIES_DDL);
        lst.add(SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL);
        return lst;
    }
    
    public boolean insertIdentity(Identity identity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("INSERT INTO IDENTITIES (uniquename,publickey,lastseen,state) VALUES (?,?,?,?)");
        
        ps.setString(1, identity.getUniqueName());
        ps.setString(2, identity.getKey());
        ps.setTimestamp(3, new Timestamp(identity.getLastSeenTimestamp()));
        ps.setInt(4, identity.getState());
        
        boolean insertWasOk = (ps.executeUpdate() == 1);
        ps.close();
        return insertWasOk;
    }

    public boolean insertLocalIdentity(LocalIdentity localIdentity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "INSERT INTO OWNIDENTITIES (uniquename,publickey,privatekey) VALUES (?,?,?)");
        
        ps.setString(1, localIdentity.getUniqueName());
        ps.setString(2, localIdentity.getKey());
        ps.setString(3, localIdentity.getPrivKey());
        
        boolean insertWasOk = (ps.executeUpdate() == 1);
        ps.close();
        return insertWasOk;
    }

    public boolean removeLocalIdentity(LocalIdentity localIdentity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("DELETE FROM OWNIDENTITIES WHERE uniquename=?");
        
        ps.setString(1, localIdentity.getUniqueName());
        
        boolean deleteWasOk = (ps.executeUpdate() == 1);
        ps.close();
        return deleteWasOk;
    }

    public List getIdentities() throws SQLException {
        ArrayList identities = new ArrayList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("SELECT uniquename,publickey,lastseen,state FROM IDENTITIES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String uniqueName = rs.getString(1);
            String pubKey = rs.getString(2);
            long lastSeen = rs.getTimestamp(3).getTime();
            int state = rs.getInt(4);
            Identity id = new Identity(uniqueName, pubKey, lastSeen, state);
            identities.add(id);
        }
        rs.close();
        ps.close();
        
        return identities;
    }

    public List getLocalIdentities() throws SQLException {
        ArrayList localIdentities = new ArrayList();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("SELECT uniquename,publickey,privatekey FROM OWNIDENTITIES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String uniqueName = rs.getString(1);
            String pubKey = rs.getString(2);
            String prvKey = rs.getString(3);
            LocalIdentity id = new LocalIdentity(uniqueName, pubKey, prvKey);
            localIdentities.add(id);
        }
        rs.close();
        ps.close();

        // load last files shared per board information
        loadAllLastFilesSharedPerBoard(localIdentities);

        return localIdentities;
    }
    
    public boolean updateIdentity(Identity identity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("UPDATE IDENTITIES SET lastseen=?,state=? WHERE uniquename=?");
        
        ps.setTimestamp(1, new Timestamp(identity.getLastSeenTimestamp()));
        ps.setInt(2, identity.getState());
        ps.setString(3, identity.getUniqueName());
        
        boolean updateWasOk = (ps.executeUpdate() == 1);
        ps.close();
        return updateWasOk;
    }

    private void loadAllLastFilesSharedPerBoard(List localIdentities) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT board,lastshared FROM OWNIDENTITIESLASTFILESSHARED WHERE uniquename=?");
        
        // anonymous
        ps.setString(1, "");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String boardname = rs.getString(1);
            long l = rs.getLong(2);
            LocalIdentity.setAnonymousLastFilesSharedMillis(boardname, l);
        }
        rs.close();

        // identities
        for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
            LocalIdentity li = (LocalIdentity)i.next();
            
            ps.setString(1, li.getUniqueName());
        
            rs = ps.executeQuery();
            while(rs.next()) {
                String boardname = rs.getString(1);
                long l = rs.getLong(2);
                li.setLastFilesSharedMillis(boardname, l);
            }
            rs.close();
        }
        ps.close();
    }

    public void saveAllLastFilesSharedPerBoard(List localIdentities) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        Statement s = db.createStatement();
        s.executeUpdate("DELETE FROM OWNIDENTITIESLASTFILESSHARED"); // clear table
        s.close();
        
        PreparedStatement ps = db.prepare(
                "INSERT INTO OWNIDENTITIESLASTFILESSHARED (uniquename,board,lastshared) VALUES (?,?,?)");
        
        // anonymous
        for(Iterator j=LocalIdentity.getAnonymousLastFilesSharedMillisBoardList().iterator(); j.hasNext(); ) {
            String boardname = (String)j.next();
            long l = LocalIdentity.getAnonymousLastFilesSharedMillis(boardname);
            if( l > 0 ) {
                ps.setString(1, "");
                ps.setString(2, boardname);
                ps.setLong(3, l);
                
                ps.executeUpdate();
            }
        }
        
        // identities
        for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
            LocalIdentity li = (LocalIdentity)i.next();
            for(Iterator j=li.getLastFilesSharedMillisBoardList().iterator(); j.hasNext(); ) {
                String boardname = (String)j.next();
                long l = li.getLastFilesSharedMillis(boardname);
                if( l > 0 ) {
                    ps.setString(1, li.getUniqueName());
                    ps.setString(2, boardname);
                    ps.setLong(3, l);
                    
                    ps.executeUpdate();
                }
            }
        }
        
        ps.close();
    }
}
