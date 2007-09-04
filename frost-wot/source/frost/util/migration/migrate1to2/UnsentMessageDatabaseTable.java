/*
  UnsentMessageDatabaseTable.java / Frost
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
import java.util.logging.*;

import frost.boards.*;
import frost.messages.*;

public class UnsentMessageDatabaseTable {

    // NOTE: fileattachments: the filename is the complete path of file
    
    private static final Logger logger = Logger.getLogger(MessageDatabaseTable.class.getName());

//    private final String SQL_DDL_MESSAGES =
//        "CREATE TABLE IF NOT EXISTS UNSENDMESSAGES ("+
//        "primkey BIGINT NOT NULL,"+
//        "board INT NOT NULL,"+
//        "sendafter BIGINT,"+ // prepared, not used
//        "idlinepos INT,"+
//        "idlinelen INT,"+
//        "messageid VARCHAR NOT NULL,"+
//        "inreplyto VARCHAR,"+
//        "fromname VARCHAR,"+
//        "subject VARCHAR,"+
//        "recipient VARCHAR,"+
//        "msgcontent VARCHAR,"+
//        "hasfileattachment BOOLEAN,"+
//        "hasboardattachment BOOLEAN,"+
//        "timeAdded BIGINT,"+ // millis when message was added to table
//        "CONSTRAINT unsendmsgs_pk PRIMARY KEY (primkey),"+
//        "CONSTRAINT unsendmsgs_1 UNIQUE(messageid),"+
//        "CONSTRAINT unsendmsgs_2 FOREIGN KEY (board) REFERENCES BOARDS(primkey) ON DELETE CASCADE"+
//        ")";
//    
//    private final String SQL_DDL_FILEATTACHMENTS =
//        "CREATE TABLE IF NOT EXISTS UNSENDFILEATTACHMENTS ("+
//        "msgref BIGINT NOT NULL,"+
//        "filename VARCHAR,"+
//        "filesize BIGINT,"+
//        "filekey  VARCHAR,"+
//        "CONSTRAINT unsendmsg_file_fk FOREIGN KEY (msgref) REFERENCES UNSENDMESSAGES(primkey) ON DELETE CASCADE"+
//        ")";
//
//    private final String SQL_DDL_BOARDATTACHMENTS =
//        "CREATE TABLE IF NOT EXISTS UNSENDBOARDATTACHMENTS ("+
//        "msgref BIGINT NOT NULL,"+
//        "boardname        VARCHAR,"+
//        "boardpublickey   VARCHAR,"+
//        "boardprivatekey  VARCHAR,"+
//        "boarddescription VARCHAR,"+
//        "CONSTRAINT unsendmsg_board_fk FOREIGN KEY (msgref) REFERENCES UNSENDMESSAGES(primkey) ON DELETE CASCADE"+
//        ")";
//
//    public List<String> getTableDDL() {
//        ArrayList<String> lst = new ArrayList<String>(3);
//        lst.add(SQL_DDL_MESSAGES);
//        lst.add(SQL_DDL_FILEATTACHMENTS);
//        lst.add(SQL_DDL_BOARDATTACHMENTS);
//        return lst;
//    }
//      
//    public boolean compact(Statement stmt) throws SQLException {
//        stmt.executeUpdate("COMPACT TABLE UNSENDMESSAGES");
//        stmt.executeUpdate("COMPACT TABLE UNSENDFILEATTACHMENTS");
//        stmt.executeUpdate("COMPACT TABLE UNSENDBOARDATTACHMENTS");
//        return true;
//    }

//    public synchronized void insertMessage(FrostUnsentMessageObject mo) throws SQLException {
//
//        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
//        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);
//
//        // insert msg and all attachments
//        Connection conn = AppLayerDatabase.getInstance().getPooledConnection();
//        try {
//            conn.setAutoCommit(false);
//            PreparedStatement ps = conn.prepareStatement(
//                "INSERT INTO UNSENDMESSAGES ("+
//                "primkey,messageid,inreplyto,board,sendafter,idlinepos,idlinelen,fromname,subject,recipient,msgcontent," +
//                "hasfileattachment,hasboardattachment,timeAdded"+
//                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
//            
//            Long identity = null;
//            Statement stmt = AppLayerDatabase.getInstance().createStatement();
//            ResultSet rs = stmt.executeQuery("select UNIQUEKEY('UNSENDMESSAGES')");
//            if( rs.next() ) {
//                identity = new Long(rs.getLong(1));
//            } else {
//                logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
//            }
//            rs.close();
//            stmt.close();
//            
//            mo.setMsgIdentity(identity.longValue());
//            
//            int i=1;
//            ps.setLong(i++, mo.getMsgIdentity()); // primkey
//            ps.setString(i++, mo.getMessageId()); // messageid
//            ps.setString(i++, mo.getInReplyTo()); // inreplyto
//            ps.setInt(i++, mo.getBoard().getPrimaryKey().intValue()); // board
//            ps.setLong(i++, 0); // sendAfter
//            ps.setInt(i++, mo.getIdLinePos()); // idLinePos
//            ps.setInt(i++, mo.getIdLineLen()); // idLineLen
//            ps.setString(i++, mo.getFromName()); // from
//            ps.setString(i++, mo.getSubject()); // subject
//            ps.setString(i++, mo.getRecipientName()); // recipient
//            ps.setString(i++, mo.getContent()); // msgcontent
//            ps.setBoolean(i++, (files.size() > 0)); // hasfileattachment
//            ps.setBoolean(i++, (boards.size() > 0)); // hasboardattachment
//            ps.setLong(i++, mo.getTimeAdded());
//    
//            // sync to allow no updates until we got the generated identity
//            int inserted = 0; 
//            try {
//                inserted = ps.executeUpdate();
//            } finally {
//                ps.close();
//            }
//    
//            if( inserted == 0 ) {
//                logger.log(Level.SEVERE, "message insert returned 0 !!!");
//                return;
//            }
//            
//            mo.setMsgIdentity(identity.longValue());
//            
//            // attachments
//            if( files.size() > 0 ) {
//                PreparedStatement p = conn.prepareStatement(
//                        "INSERT INTO UNSENDFILEATTACHMENTS"+
//                        " (msgref,filename,filesize,filekey)"+
//                        " VALUES (?,?,?,?)");
//                for(Iterator it=files.iterator(); it.hasNext(); ) {
//                    FileAttachment fa = (FileAttachment)it.next();
//                    int ix=1;
//                    p.setLong(ix++, mo.getMsgIdentity()); 
//                    p.setString(ix++, fa.getInternalFile().getPath()); 
//                    p.setLong(ix++, fa.getFileSize()); 
//                    p.setString(ix++, fa.getKey()); 
//                    int ins = p.executeUpdate();
//                    if( ins == 0 ) {
//                        logger.log(Level.SEVERE, "fileattachment insert returned 0 !!!");
//                    }
//                }
//                p.close();
//            }
//            if( boards.size() > 0 ) {
//                PreparedStatement p = conn.prepareStatement(
//                        "INSERT INTO UNSENDBOARDATTACHMENTS"+
//                        " (msgref,boardname,boardpublickey,boardprivatekey,boarddescription)"+
//                        " VALUES (?,?,?,?,?)");
//                for(Iterator it=boards.iterator(); it.hasNext(); ) {
//                    BoardAttachment ba = (BoardAttachment)it.next();
//                    Board b = ba.getBoardObj();
//                    int ix=1;
//                    p.setLong(ix++, mo.getMsgIdentity()); 
//                    p.setString(ix++, b.getNameLowerCase()); 
//                    p.setString(ix++, b.getPublicKey()); 
//                    p.setString(ix++, b.getPrivateKey()); 
//                    p.setString(ix++, b.getDescription()); 
//                    int ins = p.executeUpdate();
//                    if( ins == 0 ) {
//                        logger.log(Level.SEVERE, "boardattachment insert returned 0 !!!");
//                    }
//                }
//                p.close();
//            }
//            conn.commit();
//            conn.setAutoCommit(true);
//        } catch(Throwable t) {
//            logger.log(Level.SEVERE, "Exception during insert of unsent message", t);
//            try { conn.rollback(); } catch(Throwable t1) { logger.log(Level.SEVERE, "Exception during rollback", t1); }
//            try { conn.setAutoCommit(true); } catch(Throwable t1) { }
//        } finally {
//            AppLayerDatabase.getInstance().givePooledConnection(conn);
//        }
//    }

//    public synchronized void deleteMessage(String messageId) throws SQLException {
//
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//        PreparedStatement ps = db.prepareStatement("DELETE FROM UNSENDMESSAGES WHERE messageid=?");
//        ps.setString(1, messageId);
//        
//        int updated = ps.executeUpdate();
//        if( updated == 0 ) {
//            System.out.println("DELETED is 0!!!!");
//        }
//        ps.close();
//    }

//    /**
//     * Updates the CHK keys of fileattachments after upload of attachments.
//     */
//    public synchronized void updateMessageFileAttachmentKey(FrostMessageObject mo, FileAttachment fa) throws SQLException {
//        AppLayerDatabase db = AppLayerDatabase.getInstance();
//        PreparedStatement ps = db.prepareStatement("UPDATE UNSENDFILEATTACHMENTS SET filekey=? WHERE msgref=? AND filename=?");
//        int ix=1;
//        ps.setString(ix++, fa.getKey());
//
//        ps.setLong(ix++, mo.getMsgIdentity());
//        ps.setString(ix++, fa.getInternalFile().getPath());
//        
//        int updated = ps.executeUpdate();
//        if( updated == 0 ) {
//            System.out.println("UPDATED is 0!!!!");
//        }
//        ps.close();
//    }
    
    private static void retrieveAttachments(AppLayerDatabase db,FrostMessageObject mo) throws SQLException {
        // retrieve attachments
        if( mo.hasFileAttachments() ) {
            PreparedStatement p2 = db.prepareStatement(
                    "SELECT filename,filesize,filekey FROM UNSENDFILEATTACHMENTS"+
                    " WHERE msgref=? ORDER BY filename");
            p2.setLong(1, mo.getMsgIdentity());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, key;
                long size;
                name = rs2.getString(1);
                size = rs2.getLong(2);
                key = rs2.getString(3);

                FileAttachment fa = new FileAttachment(name, key, size, true);
                mo.addAttachment(fa);
            }
            rs2.close();
            p2.close();
        }
        if( mo.hasBoardAttachments() ) {
            PreparedStatement p2 = db.prepareStatement(
                    "SELECT boardname,boardpublickey,boardprivatekey,boarddescription FROM UNSENDBOARDATTACHMENTS"+
                    " WHERE msgref=? ORDER BY boardname");
            p2.setLong(1, mo.getMsgIdentity());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, pubkey, privkey, desc;
                name = rs2.getString(1);
                pubkey = rs2.getString(2);
                privkey = rs2.getString(3);
                desc = rs2.getString(4);
                Board b = new Board(name, pubkey, privkey, desc);
                BoardAttachment ba = new BoardAttachment(b);
                mo.addAttachment(ba);
            }
            rs2.close();
            p2.close();
        }
    }

    /**
     * Retrieves all unsend messages. Drops messages without boards.
     * Result list is sorted by timeAdded ASC.
     */
    public static LinkedList<FrostUnsentMessageObject> retrieveMessages(AppLayerDatabase db,List<Board> allBoards) throws SQLException {
        LinkedList<FrostUnsentMessageObject> unsendMessages = new LinkedList<FrostUnsentMessageObject>();
        
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,board,sendafter,idlinepos,idlinelen,fromname,subject,recipient," +
            "msgcontent,hasfileattachment,hasboardattachment,timeAdded "+
            "FROM UNSENDMESSAGES ORDER BY timeAdded ASC";
        PreparedStatement ps = db.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while( rs.next() ) {
            FrostUnsentMessageObject mo = new FrostUnsentMessageObject();
            
            int ix=1;
            mo.setMsgIdentity(rs.getLong(ix++));
            mo.setMessageId(rs.getString(ix++));
            mo.setInReplyTo(rs.getString(ix++));
            int boardPrimkey = rs.getInt(ix++);
            mo.setSendAfter(rs.getLong(ix++));
            mo.setIdLinePos(rs.getInt(ix++));
            mo.setIdLineLen(rs.getInt(ix++));
            mo.setFromName(rs.getString(ix++));
            mo.setSubject(rs.getString(ix++));
            mo.setRecipientName(rs.getString(ix++));
            mo.setContent(rs.getString(ix++));
            
            mo.setHasFileAttachments( rs.getBoolean(ix++) );
            mo.setHasBoardAttachments( rs.getBoolean(ix++) );
            
            mo.setTimeAdded( rs.getLong(ix++) );

            Board board = null; 
            for(Board b : allBoards) {
                if( b.getPerstFrostBoardObject().getBoardId() == boardPrimkey) {
                    board = b;
                    break;
                }
            }
            if( board == null ) {
                logger.warning("board for message not found, message dropped");
                continue;
            }

            mo.setBoard(board);

            retrieveAttachments(db,mo);
            
            unsendMessages.add(mo);
        }
        rs.close();
        ps.close();
        
        return unsendMessages;
    }
}
