/*
  FrostMessageObject.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messages;

import java.sql.*;
import java.util.*;

import javax.swing.tree.*;

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.gui.model.*;
import frost.storage.database.applayer.*;
import frost.util.*;

/**
 * This class holds all informations that are shown in the GUI and stored to the database.
 * It adds more fields than a MessageObjectFile uses. 
 */
public class FrostMessageObject extends AbstractMessageObject implements TableMember {

    // FIXME: if msg is a reply, send the length of the original msg along with the msg, and the receiving frost
    //   colors the replied (old) part in grey. ensures everyone recognizes what the new part is.
    //   also scroll to real beginning of the msg then!
    
    // additional variables for use in GUI
    private boolean isValid = false;
    private String invalidReason = null;
    
    private int index = -1;
    Board board = null;
    DateTime dateAndTime = null;
    
    private boolean isDeleted = false;
    private boolean isNew = false;
    private boolean isReplied = false;
    private boolean isJunk = false;
    private boolean isFlagged = false; // !
    private boolean isStarred = false; // *
    
    private boolean hasFileAttachments = false;
    private boolean hasBoardAttachments = false;
    
    private LinkedList inReplyToList = null;
    
    protected String dateAndTimeString = null;
    
    protected long msgIdentity;
    
    protected boolean isDummy = false;

    /**
     * Construct a new empty FrostMessageObject
     */
    public FrostMessageObject() {
    }
    
    public FrostMessageObject(boolean isRootnode) {
        setDummy(true);
        setDateAndTime(new DateTime(0, DateTimeZone.UTC));
        setSubject("(root)");
        setNew(false);
        setFromName("");
    }

    /**
     * Construct a new FrostMessageObject with the data from a MessageXmlFile.
     */
    public FrostMessageObject(MessageXmlFile mof, Board b, int msgIndex) {
        setValid(true);
        setBoard(b);
        setIndex(msgIndex);

        try {
            setDateAndTime(mof.getDateAndTime());
        } catch(Throwable t) {
            // never happens, we already called this method
            setDateAndTime(new DateTime(0, DateTimeZone.UTC));
        }
//        System.out.println("MSG TIME/DATE: time_in="+mof.getTimeStr()+", date_in="+mof.getDateStr()+", out="+getDateAndTime());
        // copy values from mof
        setAttachmentList(mof.getAttachmentList());
        setContent(mof.getContent());
        setFromName(mof.getFromName());
        setInReplyTo(mof.getInReplyTo());
        setMessageId(mof.getMessageId());
        setPublicKey(mof.getPublicKey());
        setRecipientName(mof.getRecipientName());
        setSignature(mof.getSignature());
        setSignatureStatus(mof.getSignatureStatus());
        setSubject(mof.getSubject());
        setIdLinePos(mof.getIdLinePos());
        setIdLineLen(mof.getIdLineLen());
        
        setHasBoardAttachments(mof.getAttachmentsOfType(Attachment.BOARD).size() > 0);
        setHasFileAttachments(mof.getAttachmentsOfType(Attachment.FILE).size() > 0);
    }

    /**
     * Construct a new FrostMessageObject for an invalid message (broken, encrypted for someone else, ...).
     */
    public FrostMessageObject(Board b, DateTime dt, int msgIndex, String reason) {
        setValid( false );
        setInvalidReason(reason);
        setBoard(b);
        setDateAndTime( dt );
        setIndex( msgIndex );
    }
    
    // create a dummy msg
    public FrostMessageObject(String msgId, Board b, LinkedList ll) {
        setMessageId(msgId);
        setBoard(b);
        setDummyInReplyToList(ll);

        setDummy(true);
        setDateAndTime(new DateTime(0, DateTimeZone.UTC));
        setSubject("");
        setNew(false);
        setFromName("");
    }

    public void fillFromOtherMessage(FrostMessageObject mof) {

        setDummy(false);
        
        setNew(mof.isNew());
        setValid(mof.isValid());
        setBoard(mof.getBoard());
        setIndex(mof.getIndex());
        
        setDateAndTime( mof.getDateAndTime() );

        setAttachmentList(mof.getAttachmentList());
        setContent(mof.getContent());
        setFromName(mof.getFromName());
        setInReplyTo(mof.getInReplyTo());
        setMessageId(mof.getMessageId());
        setPublicKey(mof.getPublicKey());
        setRecipientName(mof.getRecipientName());
        setSignature(mof.getSignature());
        setSignatureStatus(mof.getSignatureStatus());
        setSubject(mof.getSubject());
        setIdLinePos(mof.getIdLinePos());
        setIdLineLen(mof.getIdLineLen());
        
        setHasBoardAttachments(mof.getAttachmentsOfType(Attachment.BOARD).size() > 0);
        setHasFileAttachments(mof.getAttachmentsOfType(Attachment.FILE).size() > 0);
    }
    
    public boolean hasUnreadChilds() {
        if( getChildCount() == 0 ) {
            return false;
        }
        Enumeration e = breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            FrostMessageObject m = (FrostMessageObject)e.nextElement();
            if( m.isNew() ) {
                return true;
            }
        }
        return false;
    }
    
    public FrostMessageObject getThreadRootMessage() {
        if( ((FrostMessageObject)getParent()).isRoot() ) {
            return this;
        } else {
            return ((FrostMessageObject)getParent()).getThreadRootMessage();
        }
    }
    
    /**
     * Dynamically loads content.
     */
    public String getContent() {

        if( content == null ) {
            try {
                AppLayerDatabase.getMessageTable().retrieveMessageContent(this);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if( content == null ) {
                content = "";
            }
        }
        return content;
    }

    /**
     * Dynamically loads attachments.
     */
    public AttachmentList getAttachmentsOfType(int type) {
        
        if( !containsAttachments() ) {
            if (attachments == null) {
                attachments = new AttachmentList();
            }
            return attachments.getAllOfType(type);
        }
        
        if (attachments == null) {
            try {
                AppLayerDatabase.getMessageTable().retrieveAttachments(this);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (attachments == null) {
                attachments = new AttachmentList();
            }
        }
        return attachments.getAllOfType(type);
    }

    /*
     * @see frost.gui.model.TableMember#compareTo(frost.gui.model.TableMember, int)
     */
    public int compareTo(TableMember another, int tableColumnIndex) {
        String c1 = (String) getValueAt(tableColumnIndex);
        String c2 = (String) another.getValueAt(tableColumnIndex);
        if (tableColumnIndex == 4) {
            return c1.compareTo(c2);
        } else {
            // If we are sorting by anything but date...
            if (tableColumnIndex == 2) {
                //If we are sorting by subject...
                if (c1.indexOf("Re: ") == 0) {
                    c1 = c1.substring(4);
                }
                if (c2.indexOf("Re: ") == 0) {
                    c2 = c2.substring(4);
                }
            }
            int result = c1.compareToIgnoreCase(c2);
            if (result == 0) { // Items are the same. Date and time decides
                String d1 = (String) getValueAt(4);
                String d2 = (String) another.getValueAt(4);
                return d1.compareTo(d2);
            } else {
                return result;
            }
        }
    }

    /*
     * @see frost.gui.model.TableMember#getValueAt(int)
     */
    public Object getValueAt(int column) {
        switch(column) {
            case 0: return ""+getIndex();
            case 1: return getFromName();
            case 2: return getSubject();
            case 3: return getMessageStatusString();
            case 4: return getDateAndTimeString();
            default: return "*ERR*";
        }
    }

    public String getDateAndTimeString() {
        if( dateAndTimeString == null ) {
            // Build a String of format yyyy.mm.dd hh:mm:ssGMT
            DateTime dateTime = new DateTime(getDateAndTime(), DateTimeZone.UTC);
            DateMidnight date = dateTime.toDateMidnight();
            TimeOfDay time = dateTime.toTimeOfDay();

            String dateStr = DateFun.FORMAT_DATE_EXT.print(date);
            String timeStr = DateFun.FORMAT_TIME_EXT.print(time);

            StringBuffer sb = new StringBuffer(29);
            sb.append(dateStr).append(" ").append(timeStr);

            this.dateAndTimeString = sb.toString();
        }
        return this.dateAndTimeString;
    }
    
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public boolean isHasBoardAttachments() {
        return hasBoardAttachments;
    }

    public void setHasBoardAttachments(boolean hasBoardAttachments) {
        this.hasBoardAttachments = hasBoardAttachments;
    }

    public boolean isHasFileAttachments() {
        return hasFileAttachments;
    }

    public void setHasFileAttachments(boolean hasFileAttachments) {
        this.hasFileAttachments = hasFileAttachments;
    }
    
    public boolean containsAttachments() {
        if( isHasFileAttachments() || isHasBoardAttachments() ) {
            return true;
        }
        return false;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public boolean isReplied() {
        return isReplied;
    }

    public void setReplied(boolean isReplied) {
        this.isReplied = isReplied;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isJunk() {
        return isJunk;
    }

    public void setJunk(boolean isJunk) {
        this.isJunk = isJunk;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean isStarred) {
        this.isStarred = isStarred;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void setDateAndTime(DateTime dt) {
        dateAndTime = dt;
    }

    public DateTime getDateAndTime() {
        return dateAndTime;
    }

    public long getMsgIdentity() {
        return msgIdentity;
    }

    public void setMsgIdentity(long msgIdentity) {
        this.msgIdentity = msgIdentity;
    }
    
    public void setDummy(boolean v) {
        isDummy = v;
    }
    
    public boolean isDummy() {
        return isDummy;
    }
    
    private void setDummyInReplyToList(LinkedList l) {
        inReplyToList = l;
    }
    
    public LinkedList getInReplyToList() {
        if( inReplyToList == null ) {
            inReplyToList = new LinkedList();
            String s = getInReplyTo(); 
            if( s != null && s.length() > 0 ) {
                String[] sl = s.split(",");
                for(int x=0; x<sl.length; x++) {
                    String r = sl[x].trim();
                    if(r.length() > 0) {
                        inReplyToList.add(r);
                    }
                }
            }
        }
        return inReplyToList;
    }

//    private String dbg1(FrostMessageObject mo) {
//        String s1;
//        if( mo.isRoot() ) {
//            s1 = "(root)";
//        } else if( mo.isDummy() ) {
//            s1 = "(dummy)";
//        } else {
//            s1 = mo.toString()+" ["+mo.getMessageId()+"]";
//        }
//        return s1;
//    }

    public void add(MutableTreeNode n) {
        add(n, true);
    }
    
    /**
     * Overwritten add to add new nodes sorted to a parent node
     */
    public void add(MutableTreeNode nn, boolean silent) {
        // add sorted
        // FIXME: its more performant to sort all childs after the tree is built when creating the whole tree the first time (load from database)
        // -> from Board.java:         Collections.sort(children);
        DefaultMutableTreeNode n = (DefaultMutableTreeNode)nn; 
        int[] ixs;
        
        if( children == null ) {
            super.add(n);
            ixs = new int[] { 0 };
        } else {
            // sort first msg of a thread (child of root) descending (newest first),
            // but inside a thread sort siblings ascending (oldest first). (thunderbird/outlook do it this way)
            int insertPoint;
            if( isRoot() ) {
                // child of root, sort descending
                insertPoint = Collections.binarySearch(children, n, dateComparatorDescending);
            } else {
                // inside a thread, sort ascending
                insertPoint = Collections.binarySearch(children, n, dateComparatorAscending);
            }
            if( insertPoint < 0 ) {
                insertPoint++;
                insertPoint *= -1;
            }
            if( insertPoint >= children.size() ) {
                super.add(n);
                ixs = new int[] { children.size() - 1 };
            } else {
                super.insert(n, insertPoint);
                ixs = new int[] { insertPoint };
            }
        }
        if( !silent ) {
            if( MainFrame.getInstance().getMessageTreeTable().getTree().isExpanded(new TreePath(this.getPath())) ) {
                // if node is already expanded, notify new inserted row to the models
                MainFrame.getInstance().getMessageTreeModel().nodesWereInserted(this, ixs);
                if( n.getChildCount() > 0 ) {
                    // added node has childs, expand them all
                    MainFrame.getInstance().getMessageTreeTable().expandNode(n);
                }
            } else {
                // if node is not expanded, expand it, this will notify the model of the new child as well as of the old childs
                MainFrame.getInstance().getMessageTreeTable().expandNode(this);
            }
        }
//        FrostMessageObject mo = (FrostMessageObject)n;
//        System.out.println("ADDED: "+dbg1(mo)+", TO: "+dbg1(this)+", IX="+ixs[0]+", silent="+silent);
    }
  
    // FIXME: implement sorting for flat view and maybe for threaded view too!
//    SubjectComparator subjectComparator = new SubjectComparator();

    DateComparator dateComparatorAscending = new DateComparator(true);
    DateComparator dateComparatorDescending = new DateComparator(false);
    
    class DateComparator implements Comparator {
        
        private int retvalGreater;
        private int retvalSmaller;
        
        public DateComparator(boolean ascending) {
            if( ascending ) {
                // oldest first
                retvalGreater = +1;
                retvalSmaller = -1;
            } else {
                // newest first
                retvalGreater = -1;
                retvalSmaller = +1;
            }
        }
        
        public int compare(Object o1, Object o2) {
            FrostMessageObject t1 = (FrostMessageObject)o1; 
            FrostMessageObject t2 = (FrostMessageObject)o2;
            
            long l1 = t1.getDateAndTime().getMillis();
            long l2 = t2.getDateAndTime().getMillis();
            if( l1 > l2 ) {
                return retvalGreater;
            }
            if( l1 < l2 ) {
                return retvalSmaller;
            }
            return 0;
        }
    }
    
    class SubjectComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            FrostMessageObject t1 = (FrostMessageObject)arg0; 
            FrostMessageObject t2 = (FrostMessageObject)arg1;
            String s1 = t1.getSubject();
            String s2 = t2.getSubject();
            if( s1 == null && s2 == null ) {
                return 0;
            }
            if( s1 == null && s2 != null ) {
                return -1;
            }
            if( s1 != null && s2 == null ) {
                return 1;
            }
            int r = s1.toLowerCase().compareTo(s2.toLowerCase());
            return r;
        }
    }
    
    public String toString() {
        return getSubject();
    }
}
