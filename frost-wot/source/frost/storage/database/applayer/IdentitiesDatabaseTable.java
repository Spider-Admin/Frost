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
        "primkey BIGINT DEFAULT UNIQUEKEY('IDENTITIES') NOT NULL,"+
        "uniquename VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+
        "lastseen TIMESTAMP NOT NULL,"+
        "state INT NOT NULL,"+
        "CONSTRAINT ids_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT IDENTITIES_1 UNIQUE (uniquename) )";

    private final static String SQL_OWN_IDENTITIES_DDL =
        "CREATE TABLE OWNIDENTITIES ("+
        "primkey BIGINT DEFAULT UNIQUEKEY('OWNIDENTITIES') NOT NULL,"+
        "uniquename VARCHAR NOT NULL,"+
        "publickey VARCHAR NOT NULL,"+
        "privatekey VARCHAR NOT NULL,"+
        "signature VARCHAR,"+
        "sendmsgdelay INT,"+ // prepared, not used; delay in hours
        "sendmsgdelayrandom INT,"+ // prepared, not used; +/- delay in hours
        "CONSTRAINT oids_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT OWNIDENTITIES_1 UNIQUE (uniquename) )";
    
    private final static String SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL =
        "CREATE TABLE OWNIDENTITIESLASTFILESSHARED ("+
        "uniquename VARCHAR NOT NULL,"+
        "lastshared BIGINT )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_IDENTITIES_DDL);
        lst.add(SQL_OWN_IDENTITIES_DDL);
        lst.add(SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE IDENTITIES");
        stmt.executeUpdate("COMPACT TABLE OWNIDENTITIES");
        stmt.executeUpdate("COMPACT TABLE OWNIDENTITIESLASTFILESSHARED");
        return true;
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
                "INSERT INTO OWNIDENTITIES (uniquename,publickey,privatekey,signature,sendmsgdelay,sendmsgdelayrandom) VALUES (?,?,?,?,?,?)");
        
        ps.setString(1, localIdentity.getUniqueName());
        ps.setString(2, localIdentity.getKey());
        ps.setString(3, localIdentity.getPrivKey());
        ps.setString(4, localIdentity.getSignature());
        ps.setInt(5, 0);
        ps.setInt(6, 0);
        
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

    public boolean removeIdentity(Identity identity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("DELETE FROM IDENTITIES WHERE uniquename=?");
        
        ps.setString(1, identity.getUniqueName());
        
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
        
        PreparedStatement ps = db.prepare("SELECT uniquename,publickey,privatekey,signature,sendmsgdelay,sendmsgdelayrandom FROM OWNIDENTITIES");
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String uniqueName = rs.getString(1);
            String pubKey = rs.getString(2);
            String prvKey = rs.getString(3);
            String signature = rs.getString(4);
            int sendmsgdelay = rs.getInt(5);
            int sendmsgdelayrandom = rs.getInt(6);
            LocalIdentity id = new LocalIdentity(uniqueName, pubKey, prvKey, signature);
            localIdentities.add(id);
        }
        rs.close();
        ps.close();

        // load last files shared per board information
        loadAllLastFilesSharedPerBoard(localIdentities);

        return localIdentities;
    }

    public boolean updateLocalIdentity(LocalIdentity identity) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("UPDATE OWNIDENTITIES SET signature=?,sendmsgdelay,sendmsgdelayrandom WHERE uniquename=?");
        
        ps.setString(1, identity.getSignature());
        ps.setInt(2, 0);
        ps.setInt(3, 0);
        ps.setString(4, identity.getUniqueName());
        
        boolean updateWasOk = (ps.executeUpdate() == 1);
        ps.close();
        return updateWasOk;
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
    
    /**
     * Returns overall message count.
     */
    public int getIdentityCount() throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare("SELECT COUNT(primkey) FROM IDENTITIES");
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }


    private void loadAllLastFilesSharedPerBoard(List localIdentities) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "SELECT lastshared FROM OWNIDENTITIESLASTFILESSHARED WHERE uniquename=?");
        
        for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
            LocalIdentity li = (LocalIdentity)i.next();
            
            ps.setString(1, li.getUniqueName());
        
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                long l = rs.getLong(1);
                li.setLastFilesSharedMillis(l);
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
                "INSERT INTO OWNIDENTITIESLASTFILESSHARED (uniquename,lastshared) VALUES (?,?)");
        
        for(Iterator i=localIdentities.iterator(); i.hasNext(); ) {
            LocalIdentity li = (LocalIdentity)i.next();
            long l = li.getLastFilesSharedMillis();
            if( l > 0 ) {
                ps.setString(1, li.getUniqueName());
                ps.setLong(2, l);
                
                ps.executeUpdate();
            }
        }
        ps.close();
    }
}
