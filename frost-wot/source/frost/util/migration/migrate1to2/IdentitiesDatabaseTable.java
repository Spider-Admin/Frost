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
package frost.util.migration.migrate1to2;

import java.sql.*;
import java.util.*;

import frost.identities.*;

public class IdentitiesDatabaseTable {

//    private final static String SQL_IDENTITIES_DDL =
//        "CREATE TABLE IF NOT EXISTS IDENTITIES ("+
//        "primkey BIGINT DEFAULT UNIQUEKEY('IDENTITIES') NOT NULL,"+
//        "uniquename VARCHAR NOT NULL,"+
//        "publickey VARCHAR NOT NULL,"+
//        "lastseen TIMESTAMP NOT NULL,"+
//        "state INT NOT NULL,"+
//        "CONSTRAINT ids_pk PRIMARY KEY (primkey),"+
//        "CONSTRAINT IDENTITIES_1 UNIQUE (uniquename) )";
//
//    private final static String SQL_OWN_IDENTITIES_DDL =
//        "CREATE TABLE IF NOT EXISTS OWNIDENTITIES ("+
//        "primkey BIGINT DEFAULT UNIQUEKEY('OWNIDENTITIES') NOT NULL,"+
//        "uniquename VARCHAR NOT NULL,"+
//        "publickey VARCHAR NOT NULL,"+
//        "privatekey VARCHAR NOT NULL,"+
//        "signature VARCHAR,"+
//        "sendmsgdelay INT,"+ // prepared, not used; delay in hours
//        "sendmsgdelayrandom INT,"+ // prepared, not used; +/- delay in hours
//        "CONSTRAINT oids_pk PRIMARY KEY (primkey),"+
//        "CONSTRAINT OWNIDENTITIES_1 UNIQUE (uniquename) )";
//
//    private final static String SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL =
//        "CREATE TABLE IF NOT EXISTS OWNIDENTITIESLASTFILESSHARED ("+
//        "uniquename VARCHAR NOT NULL,"+
//        "lastshared BIGINT )";
//
//    public List<String> getTableDDL() {
//        ArrayList<String> lst = new ArrayList<String>(3);
//        lst.add(SQL_IDENTITIES_DDL);
//        lst.add(SQL_OWN_IDENTITIES_DDL);
//        lst.add(SQL_OWN_IDENTITIES_LASTFILESSHARED_DDL);
//        return lst;
//    }
//
//    public boolean compact(Statement stmt) throws SQLException {
//        stmt.executeUpdate("COMPACT TABLE IDENTITIES");
//        stmt.executeUpdate("COMPACT TABLE OWNIDENTITIES");
//        stmt.executeUpdate("COMPACT TABLE OWNIDENTITIESLASTFILESSHARED");
//        return true;
//    }

//    public boolean insertIdentity(Identity identity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement("INSERT INTO IDENTITIES (uniquename,publickey,lastseen,state) VALUES (?,?,?,?)");
//
//        ps.setString(1, identity.getUniqueName());
//        ps.setString(2, identity.getPublicKey());
//        ps.setTimestamp(3, new Timestamp(identity.getLastSeenTimestamp()));
//        ps.setInt(4, identity.getState());
//
//        boolean insertWasOk = false;
//        try {
//            insertWasOk = (ps.executeUpdate() == 1);
//        } finally {
//            ps.close();
//        }
//        return insertWasOk;
//    }
//
//    public boolean insertLocalIdentity(LocalIdentity localIdentity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement(
//                "INSERT INTO OWNIDENTITIES (uniquename,publickey,privatekey,signature,sendmsgdelay,sendmsgdelayrandom) VALUES (?,?,?,?,?,?)");
//
//        ps.setString(1, localIdentity.getUniqueName());
//        ps.setString(2, localIdentity.getPublicKey());
//        ps.setString(3, localIdentity.getPrivateKey());
//        ps.setString(4, localIdentity.getSignature());
//        ps.setInt(5, 0);
//        ps.setInt(6, 0);
//
//        boolean insertWasOk = false;
//        try {
//            insertWasOk = (ps.executeUpdate() == 1);
//        } finally {
//            ps.close();
//        }
//        return insertWasOk;
//    }
//
//    public boolean removeLocalIdentity(LocalIdentity localIdentity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement("DELETE FROM OWNIDENTITIES WHERE uniquename=?");
//
//        ps.setString(1, localIdentity.getUniqueName());
//
//        boolean deleteWasOk = (ps.executeUpdate() == 1);
//        ps.close();
//        return deleteWasOk;
//    }
//
//    public boolean removeIdentity(Identity identity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement("DELETE FROM IDENTITIES WHERE uniquename=?");
//
//        ps.setString(1, identity.getUniqueName());
//
//        boolean deleteWasOk = (ps.executeUpdate() == 1);
//        ps.close();
//        return deleteWasOk;
//    }

    public static List<Identity> getIdentities(final AppLayerDatabase db) throws SQLException {
        final ArrayList<Identity> identities = new ArrayList<Identity>();

        final PreparedStatement ps = db.prepareStatement("SELECT uniquename,publickey,lastseen,state FROM IDENTITIES");

        final ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            final String uniqueName = rs.getString(1);
            final String pubKey = rs.getString(2);
            final long lastSeen = rs.getTimestamp(3).getTime();
            final int state = rs.getInt(4);
            final Identity id = new Identity(uniqueName, pubKey, lastSeen, state);
            identities.add(id);
        }
        rs.close();
        ps.close();

        return identities;
    }

    public static List<LocalIdentity> getLocalIdentities(final AppLayerDatabase db) throws SQLException {
        final ArrayList<LocalIdentity> localIdentities = new ArrayList<LocalIdentity>();

        final PreparedStatement ps = db.prepareStatement("SELECT uniquename,publickey,privatekey,signature FROM OWNIDENTITIES");

        final ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            final String uniqueName = rs.getString(1);
            final String pubKey = rs.getString(2);
            final String prvKey = rs.getString(3);
            final String signature = rs.getString(4);
            final LocalIdentity id = new LocalIdentity(uniqueName, pubKey, prvKey, signature);
            localIdentities.add(id);
        }
        rs.close();
        ps.close();

        // load last files shared per board information
        loadLastFilesSharedPerIdentity(db, localIdentities);

        return localIdentities;
    }

//    public boolean updateLocalIdentity(LocalIdentity identity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement("UPDATE OWNIDENTITIES SET signature=?,sendmsgdelay=?,sendmsgdelayrandom=? WHERE uniquename=?");
//
//        ps.setString(1, identity.getSignature());
//        ps.setInt(2, 0);
//        ps.setInt(3, 0);
//        ps.setString(4, identity.getUniqueName());
//
//        boolean updateWasOk = (ps.executeUpdate() == 1);
//        ps.close();
//        return updateWasOk;
//    }
//
//    public boolean updateIdentity(Identity identity) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        PreparedStatement ps = db.prepareStatement("UPDATE IDENTITIES SET lastseen=?,state=? WHERE uniquename=?");
//
//        ps.setTimestamp(1, new Timestamp(identity.getLastSeenTimestamp()));
//        ps.setInt(2, identity.getState());
//        ps.setString(3, identity.getUniqueName());
//
//        boolean updateWasOk = (ps.executeUpdate() == 1);
//        ps.close();
//        return updateWasOk;
//    }
//
//    /**
//     * Returns overall message count.
//     */
//    public int getIdentityCount() throws SQLException {
//
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//        PreparedStatement ps = db.prepareStatement("SELECT COUNT(primkey) FROM IDENTITIES");
//
//        int count = 0;
//        ResultSet rs = ps.executeQuery();
//        if( rs.next() ) {
//            count = rs.getInt(1);
//        }
//        rs.close();
//        ps.close();
//
//        return count;
//    }

    private static void loadLastFilesSharedPerIdentity(final AppLayerDatabase db, final List<LocalIdentity> localIdentities) throws SQLException {

        final PreparedStatement ps = db.prepareStatement("SELECT lastshared FROM OWNIDENTITIESLASTFILESSHARED WHERE uniquename=?");

        for( final LocalIdentity localIdentity : localIdentities ) {
            final LocalIdentity li = localIdentity;

            ps.setString(1, li.getUniqueName());

            final ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                final long l = rs.getLong(1);
                li.setLastFilesSharedMillis(l);
            }
            rs.close();
        }
        ps.close();
    }

//    public void saveLastFilesSharedPerIdentity(List<LocalIdentity> localIdentities) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//
//        Statement s = db.createStatement();
//        s.executeUpdate("DELETE FROM OWNIDENTITIESLASTFILESSHARED"); // clear table
//        s.close();
//        s = null;
//
//        PreparedStatement ps = db.prepareStatement("INSERT INTO OWNIDENTITIESLASTFILESSHARED (uniquename,lastshared) VALUES (?,?)");
//
//        for(Iterator<LocalIdentity> i=localIdentities.iterator(); i.hasNext(); ) {
//            LocalIdentity li = i.next();
//            long l = li.getLastFilesSharedMillis();
//            if( l > 0 ) {
//                ps.setString(1, li.getUniqueName());
//                ps.setLong(2, l);
//
//                ps.executeUpdate();
//            }
//        }
//        ps.close();
//    }
//
//    public static class IdentityMsgAndFileCount {
//        final int fileCount;
//        final int messageCount;
//        public IdentityMsgAndFileCount(int mc, int fc) {
//            messageCount = mc;
//            fileCount = fc;
//        }
//        public int getFileCount() {
//            return fileCount;
//        }
//        public int getMessageCount() {
//            return messageCount;
//        }
//    }
//
//    /**
//     * Retrieve msgCount and fileCount for each identity.
//     */
//    public Hashtable<String,IdentityMsgAndFileCount> retrieveMsgAndFileCountPerIdentity() throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//        // query powered by database-man
//        String query =
//        "SELECT "+
//        "i.uniquename, "+
//        "COALESCE(m.msg_count, 0) messages, "+
//        "COALESCE(f.file_count, 0) files "+
//        "FROM IDENTITIES i "+
//        "LEFT JOIN (SELECT fromname, COUNT(*) msg_count "+
//        "           FROM MESSAGES WHERE isvalid=TRUE "+
//        "           GROUP BY fromname) m "+
//        "    ON m.fromname = i.uniquename "+
//        "LEFT JOIN (SELECT owner, COUNT(*) file_count "+
//        "           FROM FILEOWNERLIST "+
//        "           GROUP BY owner) f "+
//        "    ON f.owner = i.uniquename "+
//        "ORDER BY i.uniquename";
//
//        Statement stmt = db.createStatement();
//        ResultSet rs = stmt.executeQuery(query);
//        Hashtable<String,IdentityMsgAndFileCount> data = new Hashtable<String,IdentityMsgAndFileCount>();
//        while(rs.next()) {
//            final String uniqueName = rs.getString(1);
//            final int messageCount = rs.getInt(2);
//            final int fileCount = rs.getInt(3);
//            IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
//            data.put(uniqueName, s);
//        }
//        rs.close();
//
//        // same for ownidentities
//        query =
//            "SELECT "+
//            "i.uniquename, "+
//            "COALESCE(m.msg_count, 0) messages, "+
//            "COALESCE(f.file_count, 0) files "+
//            "FROM OWNIDENTITIES i "+
//            "LEFT JOIN (SELECT fromname, COUNT(*) msg_count "+
//            "           FROM MESSAGES WHERE isvalid=TRUE "+
//            "           GROUP BY fromname) m "+
//            "    ON m.fromname = i.uniquename "+
//            "LEFT JOIN (SELECT owner, COUNT(*) file_count "+
//            "           FROM FILEOWNERLIST "+
//            "           GROUP BY owner) f "+
//            "    ON f.owner = i.uniquename "+
//            "ORDER BY i.uniquename";
//        rs = stmt.executeQuery(query);
//        while(rs.next()) {
//            final String uniqueName = rs.getString(1);
//            final int messageCount = rs.getInt(2);
//            final int fileCount = rs.getInt(3);
//            IdentityMsgAndFileCount s = new IdentityMsgAndFileCount(messageCount, fileCount);
//            data.put(uniqueName, s);
//        }
//        rs.close();
//
//        stmt.close();
//
//        return data;
//    }
//  /**
//  * Retrieve msgCount and fileCount for each identity.
//  */
    public static Hashtable<String,Integer> retrieveMsgCountPerIdentity() throws SQLException {
        final AppLayerDatabase db = AppLayerDatabase.getInstance();
        // query powered by database-man
        String query =
            "SELECT "+
            "i.uniquename, "+
            "COALESCE(m.msg_count, 0) messages, "+
            "COALESCE(a.msg_count2, 0) messages2 "+
            "FROM IDENTITIES i "+
            "LEFT JOIN (SELECT fromname, COUNT(*) msg_count "+
            "           FROM MESSAGES WHERE isvalid=TRUE "+
            "           GROUP BY fromname) m "+
            "    ON m.fromname = i.uniquename "+
            "LEFT JOIN (SELECT fromname, COUNT(*) msg_count2 "+
            "           FROM MESSAGEARCHIVE "+
            "           GROUP BY fromname) a "+
            "    ON a.fromname = i.uniquename "+
            "ORDER BY i.uniquename";

        final Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        final Hashtable<String,Integer> data = new Hashtable<String,Integer>();
        while(rs.next()) {
            final String uniqueName = rs.getString(1);
            final int messageCount = rs.getInt(2);
            final int messageCount2 = rs.getInt(3);
            data.put(uniqueName, new Integer(messageCount+messageCount2));
        }
        rs.close();

        // same for ownidentities
        query =
            "SELECT "+
            "i.uniquename, "+
            "COALESCE(m.msg_count, 0) messages, "+
            "COALESCE(a.msg_count2, 0) messages2 "+
            "FROM OWNIDENTITIES i "+
            "LEFT JOIN (SELECT fromname, COUNT(*) msg_count "+
            "           FROM MESSAGES WHERE isvalid=TRUE "+
            "           GROUP BY fromname) m "+
            "    ON m.fromname = i.uniquename "+
            "LEFT JOIN (SELECT fromname, COUNT(*) msg_count2 "+
            "           FROM MESSAGEARCHIVE "+
            "           GROUP BY fromname) a "+
            "    ON a.fromname = i.uniquename "+
            "ORDER BY i.uniquename";
        rs = stmt.executeQuery(query);
        while(rs.next()) {
            final String uniqueName = rs.getString(1);
            final int messageCount = rs.getInt(2);
            final int messageCount2 = rs.getInt(3);
            data.put(uniqueName, new Integer(messageCount+messageCount2));
        }
        rs.close();

        stmt.close();

        return data;
    }
}
