/*
 MessageDatabaseTable.java / Frost
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

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.database.*;

public class MessageDatabaseTable extends AbstractDatabaseTable {

    private static final Logger logger = Logger.getLogger(MessageDatabaseTable.class.getName());
    
    public static final int INSERT_OK        = 1;
    public static final int INSERT_DUPLICATE = 2;
    public static final int INSERT_ERROR     = 3;

    protected String getMessageTableName() {
        return "MESSAGES";
    }
    protected String getFileAttachmentsTableName() {
        return "FILEATTACHMENTS";
    }
    protected String getBoardAttachmentsTableName() {
        return "BOARDATTACHMENTS";
    }
    protected String getUniqueMsgConstraint() {
        return "CONSTRAINT "+getUniqueMsgConstraintName()+" UNIQUE(msgdatetime,msgindex,board)";
    }
    protected String getUniqueMsgConstraintName() {
        return "MSG_UNIQUE_ONLY";
    }
    protected String getUniqueMsgIdConstraintName() {
        return "MSG_ID_UNIQUE_ONLY";
    }
    protected String getMsgIdIndexName() {
        return "MSG_IX_MSGID";
    }
    protected String getBoardIndexName() {
        return "MSG_IX_BOARD";
    }
    protected String getFromnameIndexName() {
        return "MSG_IX_FROM";
    }
    protected String getDateIndexName() {
        return "MSG_IX_DATE";
    }
    protected String getPrimKeyConstraintName() {
        return "msgs_pk";
    }
    protected String getFileForeignKeyConstraintName() {
        return "msgs_fileatt_1";
    }
    protected String getFileAttachmentsIndexName() {
        return "msgs_fileatt_ix1";
    }
    protected String getBoardForeignKeyConstraintName() {
        return "msgs_boardatt_1";
    }
    protected String getBoardConstraint() {
        return "CONSTRAINT msgs_boardconst_1 FOREIGN KEY (board) REFERENCES BOARDS(primkey) ON DELETE CASCADE,";
    }
    protected String getBoardAttachmentsIndexName() {
        return "msgs_boardatt_ix1";
    }

    private final String SQL_DDL_MESSAGES =
        "CREATE TABLE IF NOT EXISTS "+getMessageTableName()+" ("+
        "primkey BIGINT NOT NULL,"+
        "messageid VARCHAR,"+
        "inreplyto VARCHAR,"+
        "isvalid BOOLEAN,"+
        "invalidreason VARCHAR,"+
        "msgdatetime BIGINT NOT NULL,"+
        "msgindex INT NOT NULL,"+
        "board INT NOT NULL,"+
        "fromname VARCHAR,"+
        "subject VARCHAR,"+
        "recipient VARCHAR,"+
        "signature VARCHAR,"+
        "signaturestatus INT,"+
        "publickey VARCHAR,"+
        "isdeleted BOOLEAN,"+
        "isnew BOOLEAN,"+
        "isreplied BOOLEAN,"+ 
        "isjunk BOOLEAN,"+
        "isflagged BOOLEAN,"+ 
        "isstarred BOOLEAN,"+
        "hasfileattachment BOOLEAN,"+
        "hasboardattachment BOOLEAN,"+
        "idlinepos INT,"+
        "idlinelen INT,"+
        "CONSTRAINT "+getPrimKeyConstraintName()+" PRIMARY KEY (primkey),"+
        "CONSTRAINT "+getUniqueMsgIdConstraintName()+" UNIQUE(messageid),"+ // multiple null allowed
        getBoardConstraint()+       // only for messages , not for sent messages
        getUniqueMsgConstraint()+   // only for messages and sent messages
        ")";
    
    // this index is really important because we select messageids
    private final String SQL_DDL_MESSAGES_INDEX_MSGID =
        "CREATE UNIQUE INDEX "+getMsgIdIndexName()+" ON "+getMessageTableName()+" ( messageid )";
    private final String SQL_DDL_MESSAGES_INDEX_BOARD =
        "CREATE INDEX "+getBoardIndexName()+" ON "+getMessageTableName()+" ( board )";
    // this index speeds up the COUNT BY IDENTITY alot
    private final String SQL_DDL_MESSAGES_INDEX_FROM =
        "CREATE INDEX "+getFromnameIndexName()+" ON "+getMessageTableName()+" ( fromname )";
    private final String SQL_DDL_MESSAGES_INDEX_DATE =
        "CREATE INDEX "+getDateIndexName()+" ON "+getMessageTableName()+" ( msgdatetime )";

    private final String SQL_DDL_FILEATTACHMENTS =
        "CREATE TABLE IF NOT EXISTS "+getFileAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "filename VARCHAR,"+
        "filesize BIGINT,"+
        "filekey  VARCHAR,"+
        "CONSTRAINT "+getFileForeignKeyConstraintName()+" FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    private final String SQL_DDL_FILEATT_INDEX =
        "CREATE INDEX "+getFileAttachmentsIndexName()+" ON "+getFileAttachmentsTableName()+" ( msgref )";

    private final String SQL_DDL_BOARDATTACHMENTS =
        "CREATE TABLE IF NOT EXISTS "+getBoardAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "boardname VARCHAR,"+
        "boardpublickey   VARCHAR,"+
        "boardprivatekey  VARCHAR,"+
        "boarddescription VARCHAR,"+
        "CONSTRAINT "+getBoardForeignKeyConstraintName()+" FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    private final String SQL_DDL_BOARDATT_INDEX =
        "CREATE INDEX "+getBoardAttachmentsIndexName()+" ON "+getBoardAttachmentsTableName()+" ( msgref )";

    private final String SQL_DDL_CONTENT =
        "CREATE TABLE IF NOT EXISTS "+getContentTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "msgcontent VARCHAR,"+
        "CONSTRAINT "+getContentForeignKeyConstraintName()+" FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT "+getContentUniqueConstraintName()+" UNIQUE(msgref)"+
        ")";
    private final String SQL_DDL_CONTENT_INDEX =
        "CREATE UNIQUE INDEX "+getContentIndexName()+" ON "+getContentTableName()+" ( msgref )";

    protected String getContentTableName() {
        return "MESSAGECONTENTS";
    }
    protected String getContentForeignKeyConstraintName() {
        return "msgcont_fk1";
    }
    protected String getContentUniqueConstraintName() {
        return "msgcont_unique";
    }
    protected String getContentIndexName() {
        return "msgcont_index";
    }

    public List<String> getTableDDL() {
        ArrayList<String> lst = new ArrayList<String>(11);
        lst.add(SQL_DDL_MESSAGES);
        lst.add(SQL_DDL_FILEATTACHMENTS);
        lst.add(SQL_DDL_BOARDATTACHMENTS);
        lst.add(SQL_DDL_MESSAGES_INDEX_MSGID);
        lst.add(SQL_DDL_MESSAGES_INDEX_BOARD);
        lst.add(SQL_DDL_MESSAGES_INDEX_FROM);
        lst.add(SQL_DDL_MESSAGES_INDEX_DATE);
        lst.add(SQL_DDL_FILEATT_INDEX);
        lst.add(SQL_DDL_BOARDATT_INDEX);
        lst.add(SQL_DDL_CONTENT);
        lst.add(SQL_DDL_CONTENT_INDEX);
        return lst;
    }
      
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE "+getMessageTableName());
        stmt.executeUpdate("COMPACT TABLE "+getFileAttachmentsTableName());
        stmt.executeUpdate("COMPACT TABLE "+getBoardAttachmentsTableName());
        stmt.executeUpdate("COMPACT TABLE "+getContentTableName());
        return true;
    }

    public synchronized int insertMessage(FrostMessageObject mo) {

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);
        
        // insert msg and all attachments
        Connection conn = AppLayerDatabase.getInstance().getPooledConnection();
        try {
            conn.setAutoCommit(false);
        
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO "+getMessageTableName()+" ("+
                "primkey,messageid,inreplyto,isvalid,invalidreason,msgdatetime,msgindex,board,fromname,subject,recipient,signature," +
                "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,hasfileattachment,hasboardattachment,idlinepos,idlinelen"+
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            
            Long identity = null;
            Statement stmt = AppLayerDatabase.getInstance().createStatement();
            ResultSet rs = stmt.executeQuery("select UNIQUEKEY('"+getMessageTableName()+"')");
            if( rs.next() ) {
                identity = new Long(rs.getLong(1));
            } else {
                logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
            }
            rs.close();
            stmt.close();
            
            int i=1;
            ps.setLong(i++, identity.longValue()); // primkey
            ps.setString(i++, mo.getMessageId()); // messageid
            ps.setString(i++, mo.getInReplyTo()); // inreplyto
            ps.setBoolean(i++, mo.isValid()); // isvalid
            ps.setString(i++, mo.getInvalidReason()); // invalidreason
            ps.setLong(i++, mo.getDateAndTime().getMillis()); // date+time  
            ps.setInt(i++, mo.getIndex()); // index
            ps.setInt(i++, mo.getBoard().getPrimaryKey().intValue()); // board
            ps.setString(i++, mo.getFromName()); // from
            ps.setString(i++, mo.getSubject()); // subject
            ps.setString(i++, ((mo.getRecipientName()!=null&&mo.getRecipientName().length()==0)?null:mo.getRecipientName()) ); // recipient
            if( mo.getSignatureV2() == null || mo.getSignatureV2().length() == 0 ) {
                ps.setString(i++, mo.getSignatureV1()); // signature
            } else {
                ps.setString(i++, mo.getSignatureV2()); // signature
            }
            ps.setInt(i++, mo.getSignatureStatus()); // signaturestatus
            ps.setString(i++, mo.getPublicKey()); // publickey
            ps.setBoolean(i++, mo.isDeleted()); // isdeleted
            ps.setBoolean(i++, mo.isNew()); // isnew
            ps.setBoolean(i++, mo.isReplied()); // isreplied
            ps.setBoolean(i++, mo.isJunk()); // isjunk
            ps.setBoolean(i++, mo.isFlagged()); // isflagged
            ps.setBoolean(i++, mo.isStarred()); // isstarred
            ps.setBoolean(i++, (files.size() > 0)); // hasfileattachment
            ps.setBoolean(i++, (boards.size() > 0)); // hasboardattachment
            ps.setInt(i++, mo.getIdLinePos()); // idlinepos
            ps.setInt(i++, mo.getIdLineLen()); // idlinelen
            
            int inserted;

            try {
                inserted = ps.executeUpdate();
            } finally {
                ps.close();
            }
    
            if( inserted == 0 ) {
                logger.log(Level.SEVERE, "message insert returned 0 !!!");
                throw new Exception("message insert returned 0 !!!");
            }
            
            mo.setMsgIdentity(identity.longValue());
            
            // content
            PreparedStatement pc = conn.prepareStatement(
                    "INSERT INTO "+getContentTableName()+
                    " (msgref,msgcontent) VALUES (?,?)");
            pc.setLong(1, mo.getMsgIdentity());
            pc.setString(2, mo.getContent());
            try {
                inserted = pc.executeUpdate();
            } finally {
                pc.close();
            }

            if( inserted == 0 ) {
                logger.log(Level.SEVERE, "message content insert returned 0 !!!");
                throw new Exception("message content insert returned 0 !!!");
            }
    
            // attachments
            if( files.size() > 0 ) {
                PreparedStatement p = conn.prepareStatement(
                        "INSERT INTO "+getFileAttachmentsTableName()+
                        " (msgref,filename,filesize,filekey)"+
                        " VALUES (?,?,?,?)");
                try {
                    for(Iterator it=files.iterator(); it.hasNext(); ) {
                        FileAttachment fa = (FileAttachment)it.next();
                        int ix=1;
                        p.setLong(ix++, mo.getMsgIdentity()); 
                        p.setString(ix++, fa.getFilename()); 
                        p.setLong(ix++, fa.getFileSize()); 
                        p.setString(ix++, fa.getKey()); 
                        int ins = p.executeUpdate();
                        if( ins == 0 ) {
                            logger.log(Level.SEVERE, "fileattachment insert returned 0 !!!");
                            throw new Exception("fileattachment insert returned 0 !!!");
                        }
                    }
                } finally {
                    p.close();
                }
            }
            if( boards.size() > 0 ) {
                PreparedStatement p = conn.prepareStatement(
                        "INSERT INTO "+getBoardAttachmentsTableName()+
                        " (msgref,boardname,boardpublickey,boardprivatekey,boarddescription)"+
                        " VALUES (?,?,?,?,?)");
                try {
                    for(Iterator it=boards.iterator(); it.hasNext(); ) {
                        BoardAttachment ba = (BoardAttachment)it.next();
                        Board b = ba.getBoardObj();
                        int ix=1;
                        p.setLong(ix++, mo.getMsgIdentity()); 
                        p.setString(ix++, b.getNameLowerCase()); 
                        p.setString(ix++, b.getPublicKey()); 
                        p.setString(ix++, b.getPrivateKey()); 
                        p.setString(ix++, b.getDescription()); 
                        int ins = p.executeUpdate();
                        if( ins == 0 ) {
                            logger.log(Level.SEVERE, "boardattachment insert returned 0 !!!");
                            throw new Exception("boardattachment insert returned 0 !!!");
                        }
                    }
                } finally {
                    p.close();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);

            return INSERT_OK; // message inserted
        } catch(Throwable t) {
            boolean isDuplicate;
            if( t.getMessage().indexOf("constraint violation") > 0 && t.getMessage().indexOf("MSG_ID_UNIQUE_ONLY") > 0 ) {
                // duplicate msgid
                isDuplicate = true;
                logger.warning("Duplicate message id, not added to database table: msgid='"+mo.getMessageId()+
                        "', board="+mo.getBoard().getName()+", date='"+mo.getDateAndTimeString()+"', index="+mo.getIndex());
            } else if( t.getMessage().indexOf("constraint violation") > 0 && t.getMessage().indexOf("MSG_UNIQUE_ONLY") > 0 ) {
                // duplicate msgdatetime,index,board
                isDuplicate = true;
                logger.warning("Duplicate msgdatetime,index,board, not added to database table: msgid='"+mo.getMessageId()+
                        "', board="+mo.getBoard().getName()+", date='"+mo.getDateAndTimeString()+"', index="+mo.getIndex());
            } else {
                isDuplicate = false;
                logger.log(Level.SEVERE, "Exception during insert of message: msgid='"+mo.getMessageId()+
                    "', board="+mo.getBoard().getName()+", date='"+mo.getDateAndTimeString()+"', index="+mo.getIndex(), t);
                try { conn.rollback(); } catch(Throwable t1) { logger.log(Level.SEVERE, "Exception during rollback", t1); }
            }
            
            try { conn.setAutoCommit(true); } catch(Throwable t1) { }
            
            if( isDuplicate ) {
                return INSERT_DUPLICATE; // skip msg
            } else {
                return INSERT_ERROR; // error
            }
        } finally {
            AppLayerDatabase.getInstance().givePooledConnection(conn);
        }
    }

    public synchronized void updateMessage(FrostMessageObject mo) throws SQLException {
        // update msg, date, board, index are not changeable
        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement(
            "UPDATE "+getMessageTableName()+" SET isdeleted=?,isnew=?,isreplied=?,isjunk=?,isflagged=?,isstarred=? "+
            "WHERE msgdatetime=? AND msgindex=? AND board=?");
        int ix=1;
        ps.setBoolean(ix++, mo.isDeleted()); // isdeleted
        ps.setBoolean(ix++, mo.isNew()); // isnew
        ps.setBoolean(ix++, mo.isReplied()); // isreplied
        ps.setBoolean(ix++, mo.isJunk()); // isjunk
        ps.setBoolean(ix++, mo.isFlagged()); // isflagged
        ps.setBoolean(ix++, mo.isStarred()); // isstarred

        ps.setLong(ix++, mo.getDateAndTime().getMillis()); // date
        ps.setInt(ix++, mo.getIndex()); // index
        ps.setInt(ix++, mo.getBoard().getPrimaryKey().intValue()); // board
        
        int updated = ps.executeUpdate();
        if( updated == 0 ) {
            System.out.println("UPDATED is 0!!!!");
        }

        ps.close();
    }
    
    public void retrieveAttachments(FrostMessageObject mo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        // retrieve attachments
        if( mo.isHasFileAttachments() ) {
            PreparedStatement p2 = db.prepareStatement(
                    "SELECT filename,filesize,filekey FROM "+getFileAttachmentsTableName()+
                    " WHERE msgref=? ORDER BY filename");
            p2.setLong(1, mo.getMsgIdentity());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, key;
                long size;
                name = rs2.getString(1);
                size = rs2.getLong(2);
                key = rs2.getString(3);

                FileAttachment fa = new FileAttachment(name, key, size);
                mo.addAttachment(fa);
            }
            rs2.close();
            p2.close();
        }
        if( mo.isHasBoardAttachments() ) {
            PreparedStatement p2 = db.prepareStatement(
                    "SELECT boardname,boardpublickey,boardprivatekey,boarddescription FROM "+getBoardAttachmentsTableName()+
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

    public void retrieveMessageContent(FrostMessageObject mo) throws SQLException {
        
        // invalid messages have no content (e.g. encrypted for someone else,...)
        if( !mo.isValid() ) {
            mo.setContent("");
            return;
        }
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement p2 = db.prepareStatement(
                "SELECT msgcontent FROM "+getContentTableName()+" WHERE msgref=?");
        p2.setLong(1, mo.getMsgIdentity());
        ResultSet rs2 = p2.executeQuery();
        if(rs2.next()) {
            String content;
            content = rs2.getString(1);
            mo.setContent(content);
        } else {
            logger.severe("Error: No content for msgref="+mo.getMsgIdentity()+", msgid="+mo.getMessageId());
            mo.setContent("");
        }
        rs2.close();
        p2.close();
    }
    
    private FrostMessageObject resultSetToFrostMessageObject(
            ResultSet rs, Board board, boolean withContent, boolean withAttachments) 
    throws SQLException 
    {
        FrostMessageObject mo = new FrostMessageObject();
        mo.setBoard(board);
        // SELECT retrieves only valid messages:
        mo.setValid(true);

        int ix=1;
        mo.setMsgIdentity(rs.getLong(ix++));
        mo.setMessageId(rs.getString(ix++));
        mo.setInReplyTo(rs.getString(ix++));
        mo.setDateAndTime(new DateTime(rs.getLong(ix++), DateTimeZone.UTC));
        mo.setIndex(rs.getInt(ix++));
        mo.setFromName(rs.getString(ix++));
        mo.setSubject(rs.getString(ix++));
        String recipientName = rs.getString(ix++);
        if( recipientName != null && recipientName.length() == 0 ) {
            recipientName = null;
        }
        mo.setRecipientName(recipientName);
        mo.setSignatureStatus(rs.getInt(ix++));
        mo.setPublicKey(rs.getString(ix++));
        mo.setDeleted(rs.getBoolean(ix++));

        mo.setNew(rs.getBoolean(ix++));
        mo.setReplied(rs.getBoolean(ix++));
        mo.setJunk(rs.getBoolean(ix++));
        mo.setFlagged(rs.getBoolean(ix++));
        mo.setStarred(rs.getBoolean(ix++));
        
        mo.setHasFileAttachments( rs.getBoolean(ix++) );
        mo.setHasBoardAttachments( rs.getBoolean(ix++) );

        mo.setIdLinePos(rs.getInt(ix++)); // idlinepos
        mo.setIdLineLen(rs.getInt(ix++)); // idlinelen
        
        if( withContent ) {
            retrieveMessageContent(mo);
        }

        if( withAttachments ) {
            retrieveAttachments(mo);
        }
        
        return mo;
    }
    
    public synchronized void retrieveMessagesForShow(
            Board board,
            int maxDaysBack, 
            boolean withContent,
            boolean withAttachments,
            boolean showDeleted,
            boolean showUnreadOnly,
            boolean showOlderFlaggedAndStarred,
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdatetime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment,idlinepos,idlinelen";
        sql += " FROM "+getMessageTableName()+" WHERE board=? AND isvalid=TRUE";
        if( !showDeleted ) {
            // don't select deleted msgs
            sql += " AND isdeleted=FALSE";
        }
        if( showUnreadOnly ) {
            // only new messages
            sql += " AND isnew=TRUE";
        }
        if( showOlderFlaggedAndStarred ) {
            // also all older msgs that are flagged or starred
            sql += " AND (msgdatetime>=? OR isflagged=TRUE OR isstarred=TRUE)";
        } else {
            sql += " AND msgdatetime>=?";
        }

        PreparedStatement ps = db.prepareStatement(sql);

        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
        ps.setInt(1, board.getPrimaryKey().intValue());
        ps.setLong(2, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());

        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, withAttachments);
            boolean shouldStop = mc.messageRetrieved(mo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }

        rs.close();
        ps.close();
    }

    public void retrieveMessagesForSearch(
            Board board, 
            long startDate, 
            long endDate,
            boolean withContent,
            boolean withAttachments,
            boolean showDeleted, 
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdatetime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment,idlinepos,idlinelen";
            sql += " FROM "+getMessageTableName()+" WHERE msgdatetime>=? AND msgdatetime<? AND board=? AND isvalid=TRUE AND isdeleted=?";
        PreparedStatement ps = db.prepareStatement(sql);
        
        ps.setLong(1, startDate);
        ps.setLong(2, endDate);
        ps.setInt(3, board.getPrimaryKey().intValue());
        ps.setBoolean(4, showDeleted);
        
        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, withAttachments);
            boolean shouldStop = mc.messageRetrieved(mo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }
        rs.close();
        ps.close();
    }

    public void retrieveMessagesForArchive(
            Board board, 
            int maxDaysOld, 
            boolean archiveKeepFlaggedAndStarred,
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdatetime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment,idlinepos,idlinelen";
        sql += " FROM "+getMessageTableName()+" WHERE msgdatetime<? AND board=? AND isvalid=TRUE";
        if( archiveKeepFlaggedAndStarred ) {
            sql += " AND isflagged=FALSE AND isstarred=FALSE";
        }
         
        PreparedStatement ps = db.prepareStatement(sql);
        
        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysOld);
        ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
        ps.setInt(2, board.getPrimaryKey().intValue());
        
        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, true, true);
            boolean shouldStop = mc.messageRetrieved(mo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }
        rs.close();
        ps.close();
    }
    
    public List<FrostMessageObject> retrieveAllMessages() throws SQLException {
        LinkedList<FrostMessageObject> list = new LinkedList<FrostMessageObject>();
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdatetime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment,idlinepos,idlinelen,board";
            sql += " FROM "+getMessageTableName();
        PreparedStatement ps = db.prepareStatement(sql);
        
        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            
            int boardIx = rs.getInt("board");
            Board board = MainFrame.getInstance().getTofTreeModel().getBoardByPrimaryKey(new Integer(boardIx));
            if( board == null ) {
                continue;
            }
            
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, true, true);
            list.add( mo );
        }
        rs.close();
        ps.close();
        
        return list;
    }

    public int deleteExpiredMessages(Board board, int maxDaysOld, boolean archiveKeepFlaggedAndStarred) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        String sql = "DELETE FROM "+getMessageTableName()+" WHERE msgdatetime<? AND board=?";
        if( !archiveKeepFlaggedAndStarred ) {
            sql += " AND isflagged=FALSE AND isstarred=FALSE";
        }

        PreparedStatement ps = db.prepareStatement(sql);

        LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysOld);
        ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
        ps.setInt(2, board.getPrimaryKey().intValue());
        
        int deletedCount = ps.executeUpdate();

        ps.close();
        
        return deletedCount;
    }

    public FrostMessageObject retrieveMessageByMessageId(
            Board board,
            String msgId,
            boolean withContent, 
            boolean withAttachments, 
            boolean showDeleted) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdatetime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment,idlinepos,idlinelen";
            sql += " FROM "+getMessageTableName()+" WHERE board=? AND messageid=?";
        PreparedStatement ps = db.prepareStatement(sql);
        
        ps.setInt(1, board.getPrimaryKey().intValue());
        ps.setString(2, msgId);
        
        ResultSet rs = ps.executeQuery();

        FrostMessageObject mo = null;
        if( rs.next() ) {
            mo = resultSetToFrostMessageObject(rs, board, withContent, withAttachments);
        }
        rs.close();
        ps.close();
        
        return mo;
    }

    public synchronized void setAllMessagesRead(Board board) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement("UPDATE "+getMessageTableName()+" SET isnew=FALSE WHERE board=? and isnew=TRUE");
        ps.setInt(1, board.getPrimaryKey().intValue());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Returns new message count by board. If maxDaysBack is <0 all messages are counted.
     */
    public int getNewMessageCount(Board board, int maxDaysBack) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;

        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepareStatement("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isnew=TRUE AND isvalid=TRUE");
            ps.setInt(1, board.getPrimaryKey().intValue());
        } else {
            ps = db.prepareStatement("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE msgdatetime>=? AND board=? AND isnew=TRUE AND isvalid=TRUE");
            LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
            ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
            ps.setInt(2, board.getPrimaryKey().intValue());
        }
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();

        return count;
    }

    /**
     * Returns true if the board has flagged messages.
     */
    public boolean hasFlaggedMessages(Board board, int maxDaysBack) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;

        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepareStatement("SELECT isflagged FROM "+getMessageTableName()+" WHERE board=? AND isflagged=TRUE AND isvalid=TRUE");
            ps.setInt(1, board.getPrimaryKey().intValue());
        } else {
            ps = db.prepareStatement("SELECT isflagged FROM "+getMessageTableName()+" WHERE msgdatetime>=? AND board=? AND isflagged=TRUE AND isvalid=TRUE");
            LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
            ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
            ps.setInt(2, board.getPrimaryKey().intValue());
        }
        
        ps.setMaxRows(1);
        
        boolean hasFlagged = false;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            hasFlagged = rs.getBoolean(1);
        }
        rs.close();
        ps.close();

        return hasFlagged;
    }

    /**
     * Returns true if the board has flagged messages.
     */
    public boolean hasStarredMessages(Board board, int maxDaysBack) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;

        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepareStatement("SELECT isstarred FROM "+getMessageTableName()+" WHERE board=? AND isstarred=TRUE AND isvalid=TRUE");
            ps.setInt(1, board.getPrimaryKey().intValue());
        } else {
            ps = db.prepareStatement("SELECT isstarred FROM "+getMessageTableName()+" WHERE msgdatetime>=? AND board=? AND isstarred=TRUE AND isvalid=TRUE");
            LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
            ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
            ps.setInt(2, board.getPrimaryKey().intValue());
        }
        
        ps.setMaxRows(1);
        
        boolean hasStarred = false;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            hasStarred = rs.getBoolean(1);
        }
        rs.close();
        ps.close();

        return hasStarred;
    }

    /**
     * Returns message count by board. If maxDaysBack is <0 all messages are counted.
     */
    public int getMessageCount(Board board, int maxDaysBack) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepareStatement("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isvalid=TRUE");
            ps.setInt(1, board.getPrimaryKey().intValue());
        } else {
            ps = db.prepareStatement("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE msgdatetime>=? AND board=? AND isvalid=TRUE");
            LocalDate localDate = new LocalDate(DateTimeZone.UTC).minusDays(maxDaysBack);
            ps.setLong(1, localDate.toDateMidnight(DateTimeZone.UTC).getMillis());
            ps.setInt(2, board.getPrimaryKey().intValue());
        }
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }
    
    /**
     * Returns overall message count.
     */
    public int getMessageCount() throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepareStatement("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE isvalid=TRUE");
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }
}
