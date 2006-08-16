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

import frost.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.*;

// TODO: implement searching for messages without assigned boards (deleted boards)
// TODO: prepare constraints for CHK keys and/or message ID

public class MessageDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(MessageDatabaseTable.class.getName());

    protected String getMessageTableName() {
        return "MESSAGES";
    }
    protected String getFileAttachmentsTableName() {
        return "FILEATTACHMENTS";
    }
    protected String getBoardAttachmentsTableName() {
        return "BOARDATTACHMENTS";
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
    protected String getPrimKeyConstraintName() {
        return "msgs_pk";
    }
    protected String getFileForeignKeyConstraintName() {
        return "msgs_fileatt_1";
    }
    protected String getBoardForeignKeyConstraintName() {
        return "msgs_boardatt_1";
    }

    private final String SQL_DDL_MESSAGES =
        "CREATE TABLE "+getMessageTableName()+" ("+
        "primkey BIGINT NOT NULL,"+
        "messageid VARCHAR,"+
        "inreplyto VARCHAR,"+
        "isvalid BOOLEAN,"+
        "invalidreason VARCHAR,"+
        "msgdate DATE NOT NULL,"+
        "msgtime TIME,"+
        "msgindex INT NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "fromname VARCHAR,"+
        "subject VARCHAR,"+
        "recipient VARCHAR,"+
        "signature VARCHAR,"+
        "signaturestatus INT,"+
        "publickey VARCHAR,"+
        "content VARCHAR,"+
        "isdeleted BOOLEAN,"+
        "isnew BOOLEAN,"+
        "isreplied BOOLEAN,"+ 
        "isjunk BOOLEAN,"+
        "isflagged BOOLEAN,"+ 
        "isstarred BOOLEAN,"+
        "hasfileattachment BOOLEAN,"+
        "hasboardattachment BOOLEAN,"+
        "CONSTRAINT "+getPrimKeyConstraintName()+" PRIMARY KEY (primkey),"+
        "CONSTRAINT "+getUniqueMsgIdConstraintName()+" UNIQUE(messageid),"+ // multiple null allowed
        "CONSTRAINT "+getUniqueMsgConstraintName()+" UNIQUE(msgdate,msgindex,board)"+
        ")";
    
    // this index is really important because we select messageids
    private final String SQL_DDL_MESSAGES_INDEX_MSGID =
        "CREATE INDEX "+getMsgIdIndexName()+" ON "+getMessageTableName()+" ( messageid )";
    private final String SQL_DDL_MESSAGES_INDEX_BOARD =
        "CREATE INDEX "+getBoardIndexName()+" ON "+getMessageTableName()+" ( board )";
    // this index speeds up the COUNT BY IDENTITY alot
    private final String SQL_DDL_MESSAGES_INDEX_FROM =
        "CREATE INDEX "+getFromnameIndexName()+" ON "+getMessageTableName()+" ( fromname )";

    private final String SQL_DDL_FILEATTACHMENTS =
        "CREATE TABLE "+getFileAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "filename VARCHAR,"+
        "filesize BIGINT,"+
        "filekey  VARCHAR,"+
        "CONSTRAINT "+getFileForeignKeyConstraintName()+" FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    private final String SQL_DDL_BOARDATTACHMENTS =
        "CREATE TABLE "+getBoardAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "boardname VARCHAR,"+
        "boardpublickey   VARCHAR,"+
        "boardprivatekey  VARCHAR,"+
        "boarddescription VARCHAR,"+
        "CONSTRAINT "+getBoardForeignKeyConstraintName()+" FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(6);
        lst.add(SQL_DDL_MESSAGES);
        lst.add(SQL_DDL_FILEATTACHMENTS);
        lst.add(SQL_DDL_BOARDATTACHMENTS);
        lst.add(SQL_DDL_MESSAGES_INDEX_MSGID);
        lst.add(SQL_DDL_MESSAGES_INDEX_BOARD);
        lst.add(SQL_DDL_MESSAGES_INDEX_FROM);
        return lst;
    }
    
    public synchronized void insertMessage(FrostMessageObject mo) throws SQLException {

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "INSERT INTO "+getMessageTableName()+" ("+
            "primkey,messageid,inreplyto,isvalid,invalidreason,msgdate,msgtime,msgindex,board,fromname,subject,recipient,signature," +
            "signaturestatus,publickey,content,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,hasfileattachment,hasboardattachment"+
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        Long identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('UPLOADFILES')");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();
        
        int i=1;
        ps.setLong(i++, identity.longValue()); // messageid
        ps.setString(i++, mo.getMessageId()); // messageid
        ps.setString(i++, mo.getInReplyTo()); // inreplyto
        ps.setBoolean(i++, mo.isValid()); // isvalid
        ps.setString(i++, mo.getInvalidReason()); // invalidreason
        ps.setDate(i++, mo.getSqlDate()); // date  
        ps.setTime(i++, mo.getSqlTime()); // time
        ps.setInt(i++, mo.getIndex()); // index
        ps.setString(i++, mo.getBoard().getNameLowerCase()); // board
        ps.setString(i++, mo.getFromName()); // from
        ps.setString(i++, mo.getSubject()); // subject
        ps.setString(i++, mo.getRecipientName()); // recipient
        ps.setString(i++, mo.getSignature()); // signature
        ps.setInt(i++, mo.getSignatureStatus()); // signaturestatus
        ps.setString(i++, mo.getPublicKey()); // publickey
        ps.setString(i++, mo.getContent()); // content
        ps.setBoolean(i++, mo.isDeleted()); // isdeleted
        ps.setBoolean(i++, mo.isNew()); // isnew
        ps.setBoolean(i++, mo.isReplied()); // isreplied
        ps.setBoolean(i++, mo.isJunk()); // isjunk
        ps.setBoolean(i++, mo.isFlagged()); // isflagged
        ps.setBoolean(i++, mo.isStarred()); // isstarred
        ps.setBoolean(i++, (files.size() > 0)); // hasfileattachment
        ps.setBoolean(i++, (boards.size() > 0)); // hasboardattachment

        // sync to allow no updates until we got the generated identity
        synchronized(getSyncObj()) {
            int inserted = ps.executeUpdate();
            ps.close();
    
            if( inserted == 0 ) {
                System.out.println("INSERTED is 0!!!!");
                return;
            }
            
            mo.setMsgIdentity(identity.longValue());
        }

        // attachments
        if( files.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO "+getFileAttachmentsTableName()+
                    " (msgref,filename,filesize,filekey)"+
                    " VALUES (?,?,?,?)");
            for(Iterator it=files.iterator(); it.hasNext(); ) {
                FileAttachment fa = (FileAttachment)it.next();
                int ix=1;
                p.setLong(ix++, mo.getMsgIdentity()); 
                p.setString(ix++, fa.getFilename()); 
                p.setLong(ix++, fa.getSize().longValue()); 
                p.setString(ix++, fa.getKey()); 
                int ins = p.executeUpdate();
                if( ins == 0 ) {
                    System.out.println("INSERTED is 0!!!!");
                }
            }
            p.close();
        }
        if( boards.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO "+getBoardAttachmentsTableName()+
                    " (msgref,boardname,boardpublickey,boardprivatekey,boarddescription)"+
                    " VALUES (?,?,?,?,?)");
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
                    System.out.println("INSERTED is 0!!!!");
                }
            }
            p.close();
        }
    }

    public synchronized void updateMessage(FrostMessageObject mo) throws SQLException {
        // update msg, date, board, index are not changeable
        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "UPDATE "+getMessageTableName()+" SET isdeleted=?,isnew=?,isreplied=?,isjunk=?,isflagged=?,isstarred=? "+
            "WHERE msgdate=? AND msgindex=? AND board=?");
        int ix=1;
        ps.setBoolean(ix++, mo.isDeleted()); // isdeleted
        ps.setBoolean(ix++, mo.isNew()); // isnew
        ps.setBoolean(ix++, mo.isReplied()); // isreplied
        ps.setBoolean(ix++, mo.isJunk()); // isjunk
        ps.setBoolean(ix++, mo.isFlagged()); // isflagged
        ps.setBoolean(ix++, mo.isStarred()); // isstarred

        ps.setDate(ix++, mo.getSqlDate()); // date
        ps.setInt(ix++, mo.getIndex()); // index
        ps.setString(ix++, mo.getBoard().getNameLowerCase()); // board
        
        int updated = ps.executeUpdate();
        if( updated == 0 ) {
            System.out.println("UPDATED is 0!!!!");
        }

        ps.close();
    }
    
    private void retrieveAttachments(FrostMessageObject mo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        // retrieve attachments
        if( mo.isHasFileAttachments() ) {
            PreparedStatement p2 = db.prepare(
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
            PreparedStatement p2 = db.prepare(
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

    private FrostMessageObject resultSetToFrostMessageObject(
            ResultSet rs, Board board, boolean withContent, boolean withAttachments) 
    throws SQLException 
    {
        FrostMessageObject mo = new FrostMessageObject();
        mo.setBoard(board);
        int ix=1;
        mo.setMsgIdentity(rs.getLong(ix++));
        mo.setMessageId(rs.getString(ix++));
        mo.setInReplyTo(rs.getString(ix++));
        mo.setSqlDate(rs.getDate(ix++));
        mo.setSqlTime(rs.getTime(ix++));
        mo.setIndex(rs.getInt(ix++));
        mo.setFromName(rs.getString(ix++));
        mo.setSubject(rs.getString(ix++));
        mo.setRecipientName(rs.getString(ix++));
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
        
        if( withContent ) {
            mo.setContent(rs.getString(ix++));
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
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdate,msgtime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment";
        if( withContent ) {
            sql += ",content";
        }
        sql += " FROM "+getMessageTableName()+" WHERE msgdate>=? AND board=? AND isvalid=TRUE ";
        if( !showDeleted ) {
            // don't select deleted msgs
            sql += "AND isdeleted=FALSE ";
        }

        PreparedStatement ps = db.prepare(sql);

        ps.setDate(1, startDate);
        ps.setString(2, board.getNameLowerCase());

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
            java.sql.Date startDate, 
            java.sql.Date endDate,
            boolean withContent,
            boolean withAttachments,
            boolean showDeleted, 
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdate,msgtime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment";
            if( withContent ) {
                sql += ",content";
            }
            sql += " FROM "+getMessageTableName()+" WHERE msgdate>=? AND msgdate<=? AND board=? AND isvalid=TRUE AND isdeleted=?";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setDate(1, startDate);
        ps.setDate(2, endDate);
        ps.setString(3, board.getNameLowerCase());
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
            "primkey,messageid,inreplyto,msgdate,msgtime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment";
            if( withContent ) {
                sql += ",content";
            }
            sql += " FROM "+getMessageTableName()+" WHERE board=? AND messageid=?";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setString(1, board.getNameLowerCase());
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

//    public String retrieveMessageContent(java.sql.Date date, Board board, int index) throws SQLException {
//        GuiDatabase db = GuiDatabase.getInstance();
//        PreparedStatement ps = db.prepare("SELECT content FROM "+getMessageTableName()+" WHERE date=? AND board=? AND msgindex=?");
//        ps.setDate(1, date);
//        ps.setString(2, board.getNameLowerCase());
//        ps.setInt(3, index);
//        ps.setMaxRows(1);
//        String content = null;
//        ResultSet rs = ps.executeQuery();
//        if( rs.next() ) {
//            content = rs.getString(1);
//        }
//        rs.close();
//        ps.close();
//        
//        return content;
//    }

    public synchronized void setAllMessagesRead(Board board) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare("UPDATE "+getMessageTableName()+" SET isnew=FALSE WHERE board=? and isnew=TRUE");
        ps.setString(1, board.getNameLowerCase());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Returns new message count by board. If maxDaysBack is <0 all messages are counted.
     */
    public int getNewMessageCount(Board board, int maxDaysBack) throws SQLException {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isnew=TRUE AND isvalid=TRUE");
            ps.setString(1, board.getNameLowerCase());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE msgdate >=? AND board=? AND isnew=TRUE AND isvalid=TRUE");
            ps.setDate(1, startDate);
            ps.setString(2, board.getNameLowerCase());
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
     * Returns message count by identity. If maxDaysBack is <0 all messages are counted.
     */
    public int getMessageCountByIdentity(Identity identity, int maxDaysBack) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE fromname=? AND isvalid=TRUE");
            ps.setString(1, identity.getUniqueName());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE msgdate >=? AND fromname=? AND isvalid=TRUE");
            java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
            ps.setDate(1, startDate);
            ps.setString(2, identity.getUniqueName());
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
     * Returns message count by board. If maxDaysBack is <0 all messages are counted.
     */
    public int getMessageCount(Board board, int maxDaysBack) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isvalid=TRUE");
            ps.setString(1, board.getNameLowerCase());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE msgdate >=? AND board=? AND isvalid=TRUE");
            java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
            ps.setDate(1, startDate);
            ps.setString(2, board.getNameLowerCase());
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
}
