package frost.storage;

import java.sql.*;
import java.util.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;

public class MessageDatabaseTable {

    private static final String SQL_DDL_MESSAGES =
        "CREATE TABLE MESSAGES ("+
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
        "hasboardattachment BOOLEAN"+
        ")";
    // "messageid,inreplyto,isvalid,invalidreason,date,time,index,board,from,subject,recipient,signature,publickey,"+
    // "content,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
    private static final String SQL_DDL_FILEATTACHMENTS =
        "CREATE TABLE FILEATTACHMENTS ("+
        "date DATE NOT NULL,"+
        "index INT NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "filename VARCHAR,"+
        "filesize BIGINT,"+
        "filekey  VARCHAR"+
        ")";
    private static final String SQL_DDL_BOARDATTACHMENTS =
        "CREATE TABLE BOARDATTACHMENTS ("+
        "date DATE NOT NULL,"+
        "index INT NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "boardname VARCHAR,"+
        "boardpublickey   VARCHAR,"+
        "boardprivatekey  VARCHAR,"+
        "boarddescription VARCHAR"+
        ")";
    
    public static List getTableDDL() {
        ArrayList lst = new ArrayList(1);
        lst.add(SQL_DDL_MESSAGES);
        lst.add(SQL_DDL_FILEATTACHMENTS);
        lst.add(SQL_DDL_BOARDATTACHMENTS);
        return lst;
    }
    
    public static void insertMessage(FrostMessageObject mo) throws SQLException {
        
        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);
        
        // insert msg and all attachments
        GuiDatabase db = GuiDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "INSERT INTO MESSAGES ("+
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
        
        int inserted = ps.executeUpdate();
        
        if( inserted == 0 ) {
            System.out.println("INSERTED is 0!!!!");
        }
        
        ps.close();
        
        // attachments
        if( files.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO FILEATTACHMENTS (date,index,board,"+
                    "filename,filesize,filekey)"+
                    " VALUES (?,?,?,?,?,?)");
            for(Iterator it=files.iterator(); it.hasNext(); ) {
                FileAttachment fa = (FileAttachment)it.next();
                SharedFileObject sfo = fa.getFileObj();
                int ix=1;
                p.setDate(ix++, mo.getSqlDate()); 
                p.setInt(ix++, mo.getIndex()); 
                p.setString(ix++, mo.getBoard().getName());
                
                p.setString(ix++, sfo.getFilename()); 
                p.setLong(ix++, sfo.getSize().longValue()); 
                p.setString(ix++, sfo.getKey()); 
                int ins = p.executeUpdate();
                if( ins == 0 ) {
                    System.out.println("INSERTED is 0!!!!");
                }
            }
            p.close();
        }
        if( boards.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO FILEATTACHMENTS (date,index,board,"+
                    "boardname,boardpublickey,boardprivatekey,boarddescription)"+
                    " VALUES (?,?,?,?,?,?,?)");
            for(Iterator it=boards.iterator(); it.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)it.next();
                Board b = ba.getBoardObj();
                int ix=1;
                p.setDate(ix++, mo.getSqlDate()); 
                p.setInt(ix++, mo.getIndex()); 
                p.setString(ix++, mo.getBoard().getName()); 
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

    public static void updateMessage(FrostMessageObject mo) throws SQLException {
        // update msg, date, board, index are not changeable
        // insert msg and all attachments
        GuiDatabase db = GuiDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "UPDATE MESSAGES SET isdeleted=?,isnew=?,isanswered=?,isjunk=?,ismarked=?,isstarred=? "+
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
    
    private static FrostMessageObject resultSetToFrostMessageObject(
            ResultSet rs, Board board, boolean withContent, GuiDatabase db) throws SQLException 
    {
        FrostMessageObject mo = new FrostMessageObject();
        mo.setBoard(board);
        int ix=1;
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
        
        boolean hasFileAttachment = rs.getBoolean(ix++);
        boolean hasBoardAttachment = rs.getBoolean(ix++);
        
        if( withContent ) {
            mo.setContent(rs.getString(ix++));
        }
        
        // retrieve attachments
        if( hasFileAttachment ) {
            PreparedStatement p2 = db.prepare(
                    "SELECT filename,filesize,filekey FROM FILEATTACHMENTS "+
                    "WHERE date=? AND index=? AND board=?");
            p2.setDate(1, mo.getSqlDate());
            p2.setInt(2, mo.getIndex());
            p2.setString(3, board.getName());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, key;
                long size;
                name = rs2.getString(1);
                size = rs2.getLong(2);
                key = rs2.getString(3);
                
                SharedFileObject sfo = new SharedFileObject();
                sfo.setBoard(board);
                sfo.setFilename(name);
                sfo.setSize(size);
                sfo.setKey(key);
                FileAttachment fa = new FileAttachment(sfo);
                mo.addAttachment(fa);
            }
            rs2.close();
            p2.close();
        }
        if( hasBoardAttachment ) {
            PreparedStatement p2 = db.prepare(
                    "SELECT boardname,boardpublickey,boardprivatekey,boarddescription FROM FILEATTACHMENTS "+
                    "WHERE date=? AND index=? AND board=?");
            p2.setDate(1, mo.getSqlDate());
            p2.setInt(2, mo.getIndex());
            p2.setString(3, board.getName());
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
        return mo;
    }
    
    public static List retrieveMessages(Board board, int maxDaysBack, boolean withContent, boolean showDeleted) 
    throws SQLException {

        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        GuiDatabase db = GuiDatabase.getInstance();
        String sql =
            "SELECT "+
            "messageid,inreplyto,date,time,index,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
        if( withContent ) {
            sql += ",content";
        }
        sql += " FROM MESSAGES WHERE date>=? AND board=? AND isvalid=TRUE ";
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
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, db);
            result.add(mo);
        }
        rs.close();
        ps.close();
        
        return result;
    }
    
    public static void retrieveMessagesOneByOne(
            Board board, int maxDaysBack, boolean withContent, boolean showDeleted, MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        GuiDatabase db = GuiDatabase.getInstance();
        String sql =
            "SELECT "+
            "messageid,inreplyto,date,time,index,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isanswered,isjunk,ismarked,isstarred,hasfileattachment,hasboardattachment";
            if( withContent ) {
                sql += ",content";
            }
            sql += " FROM MESSAGES WHERE date>=? AND board=? AND isvalid=TRUE AND isdeleted=? ORDER BY date DESC,time DESC";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setDate(1, startDate);
        ps.setString(2, board.getName());
        ps.setBoolean(3, showDeleted);
        
        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board, withContent, db);
            mc.messageRetrieved(mo); // pass to callback
        }
        rs.close();
        ps.close();
    }
    
    public static String retrieveMessageContent(java.sql.Date date, Board board, int index) throws SQLException {
        GuiDatabase db = GuiDatabase.getInstance();
        PreparedStatement ps = db.prepare("SELECT TOP 1 content FROM MESSAGES WHERE date=? AND board=? AND index=?");
        ps.setDate(1, date);
        ps.setString(2, board.getName());
        ps.setInt(3, index);
        
        String content = null;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            content = rs.getString(1);
        }
        rs.close();
        ps.close();
        
        return content;
    }
    
    public static int getNewMessageCount(Board board, int maxDaysBack) throws SQLException {
        java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
        
        GuiDatabase db = GuiDatabase.getInstance();
        PreparedStatement ps = db.prepare("SELECT COUNT(date) FROM MESSAGES WHERE date >=? AND board=? AND isnew=TRUE");
        ps.setDate(1, startDate);
        ps.setString(2, board.getName());
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }
    
    public static void setAllMessagesRead(Board board) throws SQLException {
        GuiDatabase db = GuiDatabase.getInstance();
        PreparedStatement ps = db.prepare("UPDATE MESSAGES SET isnew=FALSE WHERE board=? and isnew=TRUE");
        ps.setString(1, board.getName());
        ps.executeUpdate();
        ps.close();
    }
}
