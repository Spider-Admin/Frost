package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.*;

// TODO: implement searching for messages without assigned boards (deleted boards)
// TODO: prepare constraints for CHK keys and/or message ID

public class MessageDatabaseTable extends AbstractDatabaseTable {
    
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

    private final String SQL_DDL_MESSAGES =
        "CREATE TABLE "+getMessageTableName()+" ("+
        "primkey BIGINT NOT NULL IDENTITY PRIMARY KEY,"+
        "messageid VARCHAR,"+
        "inreplyto VARCHAR,"+
        "isvalid BOOLEAN,"+
        "invalidreason VARCHAR,"+
        "date DATE NOT NULL,"+
        "time TIME,"+
        "index INT NOT NULL,"+
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
        "isanswered BOOLEAN,"+
        "isjunk BOOLEAN,"+
        "ismarked BOOLEAN,"+
        "isstarred BOOLEAN,"+
        "hasfileattachment BOOLEAN,"+
        "hasboardattachment BOOLEAN,"+
        "CONSTRAINT "+getUniqueMsgConstraintName()+" UNIQUE(date,index,board)"+
        ")";
    private final String SQL_DDL_FILEATTACHMENTS =
        "CREATE TABLE "+getFileAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "filename VARCHAR,"+
        "filesize BIGINT,"+
        "filekey  VARCHAR,"+
        "FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    private final String SQL_DDL_BOARDATTACHMENTS =
        "CREATE TABLE "+getBoardAttachmentsTableName()+" ("+
        "msgref BIGINT NOT NULL,"+
        "boardname VARCHAR,"+
        "boardpublickey   VARCHAR,"+
        "boardprivatekey  VARCHAR,"+
        "boarddescription VARCHAR,"+
        "FOREIGN KEY (msgref) REFERENCES "+getMessageTableName()+"(primkey) ON DELETE CASCADE"+
        ")";
    
    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_DDL_MESSAGES);
        lst.add(SQL_DDL_FILEATTACHMENTS);
        lst.add(SQL_DDL_BOARDATTACHMENTS);
        return lst;
    }
    
    public void insertMessage(FrostMessageObject mo) throws SQLException {

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "INSERT INTO "+getMessageTableName()+"  ("+
            "messageid,inreplyto,isvalid,invalidreason,date,time,index,board,fromname,subject,recipient,signature," +
            "signaturestatus,publickey,content,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment"+
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        int i=1;
        ps.setString(i++, mo.getMessageId()); // messageid
        ps.setString(i++, mo.getInReplyTo()); // inreplyto
        ps.setBoolean(i++, mo.isValid()); // isvalid
        ps.setString(i++, mo.getInvalidReason()); // invalidreason
        ps.setDate(i++, mo.getSqlDate()); // date
        ps.setTime(i++, mo.getSqlTime()); // time
        ps.setInt(i++, mo.getIndex()); // index
        ps.setString(i++, mo.getBoard().getName()); // board
        ps.setString(i++, mo.getFromName()); // from
        ps.setString(i++, mo.getSubject()); // subject
        ps.setString(i++, mo.getRecipientName()); // recipient
        ps.setString(i++, mo.getSignature()); // signature
        ps.setInt(i++, mo.getSignatureStatus()); // signaturestatus
        ps.setString(i++, mo.getPublicKey()); // publickey
        ps.setString(i++, mo.getContent()); // content
        ps.setBoolean(i++, mo.isDeleted()); // isdeleted
        ps.setBoolean(i++, mo.isNew()); // isnew
        ps.setBoolean(i++, mo.isAnswered()); // isanswered
        ps.setBoolean(i++, mo.isJunk()); // isjunk
        ps.setBoolean(i++, mo.isMarked()); // ismarked
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
            
            // get generated identity
            long identity;
            Statement s = db.createStatement();
            ResultSet rs = s.executeQuery("CALL IDENTITY();");
            if( rs.next() ) {
                identity = rs.getLong(1);
            } else {
                System.out.println("Could not retrieve the generated identity after insert!");
                rs.close();
                s.close();
                return;
            }
            rs.close();
            s.close();

            mo.setMsgIdentity(identity);
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
                p.setString(ix++, b.getName()); 
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

    public void updateMessage(FrostMessageObject mo) throws SQLException {
        // update msg, date, board, index are not changeable
        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "UPDATE "+getMessageTableName()+" SET isdeleted=?,isnew=?,isanswered=?,isjunk=?,ismarked=?,isstarred=? "+
            "WHERE date=? AND index=? AND board=?");
        int ix=1;
        ps.setBoolean(ix++, mo.isDeleted()); // isdeleted
        ps.setBoolean(ix++, mo.isNew()); // isnew
        ps.setBoolean(ix++, mo.isAnswered()); // isanswered
        ps.setBoolean(ix++, mo.isJunk()); // isjunk
        ps.setBoolean(ix++, mo.isMarked()); // ismarked
        ps.setBoolean(ix++, mo.isStarred()); // isstarred

        ps.setDate(ix++, mo.getSqlDate()); // date
        ps.setInt(ix++, mo.getIndex()); // index
        ps.setString(ix++, mo.getBoard().getName()); // board
        
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
        mo.setAnswered(rs.getBoolean(ix++));
        mo.setJunk(rs.getBoolean(ix++));
        mo.setMarked(rs.getBoolean(ix++));
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

    public List retrieveMessages(
            Board board, 
            int maxDaysBack, 
            boolean withContent, 
            boolean withAttachments, 
            boolean showDeleted) 
    throws SQLException {

        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,date,time,index,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
        if( withContent ) {
            sql += ",content";
        }
        sql += " FROM "+getMessageTableName()+" WHERE date>=? AND board=? AND isvalid=TRUE ";
        if( !showDeleted ) {
            // don't select deleted msgs
            sql += "AND isdeleted=FALSE ";
        }
        sql += "ORDER BY date DESC,time DESC";
            
        PreparedStatement ps = db.prepare(sql);
        
        ps.setDate(1, startDate);
        ps.setString(2, board.getName());
        
        ResultSet rs = ps.executeQuery();

        ArrayList result = new ArrayList();
        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, withAttachments);
            result.add(mo);
        }
        rs.close();
        ps.close();
        
        return result;
    }

//    public static void retrieveMessagesOneByOne(
//            Board board, 
//            int maxDaysBack, 
//            boolean withContent,
//            boolean withAttachments,
//            boolean showDeleted, 
//            MessageDatabaseTableCallback mc) 
//    throws SQLException 
//    {
//        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
//        
//        GuiDatabase db = GuiDatabase.getInstance();
//        String sql =
//            "SELECT "+
//            "messageid,inreplyto,date,time,index,fromname,subject,recipient," +
//            "signaturestatus,publickey,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
//            if( withContent ) {
//                sql += ",content";
//            }
//            sql += " FROM MESSAGES WHERE date>=? AND board=? AND isvalid=TRUE AND isdeleted=? ORDER BY date DESC,time DESC";
//        PreparedStatement ps = db.prepare(sql);
//        
//        ps.setDate(1, startDate);
//        ps.setString(2, board.getName());
//        ps.setBoolean(3, showDeleted);
//        
//        ResultSet rs = ps.executeQuery();
//
//        while( rs.next() ) {
//            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, withAttachments);
//            mc.messageRetrieved(mo); // pass to callback
//        }
//        rs.close();
//        ps.close();
//    }
//
    public void retrieveMessagesOneByOne(
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
            "primkey,messageid,inreplyto,date,time,index,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
            if( withContent ) {
                sql += ",content";
            }
            sql += " FROM "+getMessageTableName()+" WHERE date>=? AND date<=? AND board=? AND isvalid=TRUE AND isdeleted=? ORDER BY date DESC,time DESC";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setDate(1, startDate);
        ps.setDate(2, endDate);
        ps.setString(3, board.getName());
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

//    public String retrieveMessageContent(java.sql.Date date, Board board, int index) throws SQLException {
//        GuiDatabase db = GuiDatabase.getInstance();
//        PreparedStatement ps = db.prepare("SELECT TOP 1 content FROM "+getMessageTableName()+" WHERE date=? AND board=? AND index=?");
//        ps.setDate(1, date);
//        ps.setString(2, board.getName());
//        ps.setInt(3, index);
//        
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

    public void setAllMessagesRead(Board board) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare("UPDATE "+getMessageTableName()+" SET isnew=FALSE WHERE board=? and isnew=TRUE");
        ps.setString(1, board.getName());
        ps.executeUpdate();
        ps.close();
    }

    public int getNewMessageCount(Board board, int maxDaysBack) throws SQLException {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isnew=TRUE AND isvalid=TRUE");
            ps.setString(1, board.getName());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE date >=? AND board=? AND isnew=TRUE AND isvalid=TRUE");
            ps.setDate(1, startDate);
            ps.setString(2, board.getName());
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

    public int getMessageCount(Board board, int maxDaysBack) throws SQLException {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE board=? AND isvalid=TRUE");
            ps.setString(1, board.getName());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM "+getMessageTableName()+" WHERE date >=? AND board=? AND isvalid=TRUE");
            ps.setDate(1, startDate);
            ps.setString(2, board.getName());
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
