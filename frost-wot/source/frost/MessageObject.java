/*
  MessageObject.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost;
import java.io.File;
import java.util.Vector;

public class MessageObject {

    char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _

    String board, content, from, subject, date, time, index, publicKey, newContent;
    File file;

    /**Get*/

    public String getNewContent() {
    return newContent;
    }

//TODO: should return AttachmentObjects (to create)
    public Vector getAttachments() {
    Vector table = new Vector();
    int pos = 0;
    int start = content.indexOf("<attached>");
    int end = content.indexOf("</attached>");
    newContent="";
    while (start != -1 && end != -1) {
        newContent += content.substring(pos, start).trim();

        int spaces = content.indexOf(" * ", start);
        String filename = (content.substring(start + 10, spaces)).trim();
        String chkKey = (content.substring(spaces + 3, end)).trim();
        Vector rows = new Vector();
        rows.add(filename);
        rows.add(chkKey);
        table.add(rows);
        pos = end + 11;

        start = content.indexOf("<attached>", pos);
        end = content.indexOf("</attached>", pos);
    }

    newContent += content.substring(pos, content.length()).trim();

    return table;
    }

// TODO: should return AttachedBoards (to create)
    public Vector getBoards () {
        // TODO: this code does not care if the <board> or </board> appears somewhere in the content
        // if e.g. a <board></board> occurs in message, this throw a NullPointerException
        Vector table = new Vector();
    int pos = 0;
    int start = content.indexOf("<board>");
    int end = content.indexOf("</board>",start);
    newContent="";
    while (start != -1 && end != -1) {
        int boardPartLength = end - start; // must be at least 14, 1 char boardnamwe, 2 times " * " and keys="N/A"
        if( boardPartLength < 14 )
        {
            continue;
        }
        newContent += content.substring(pos, start).trim();

        int spaces = content.indexOf(" * ", start);
        int spaces2 = content.indexOf(" * ", spaces+1);
        //System.out.println("* at " + spaces + " " + spaces2);
        String boardName = (content.substring(start + 7, spaces)).trim();
        String pubKey = (content.substring(spaces + 3, spaces2)).trim();
        String privKey = (content.substring(spaces2 + 3, end)).trim();
        Vector rows = new Vector();
        rows.add(boardName);
        rows.add(pubKey);
        rows.add(privKey);
        table.add(rows);
        pos = end + 8;

        start = content.indexOf("<board>", pos);
        end = content.indexOf("</board>", pos);
    }

    newContent += content.substring(pos, content.length()).trim();
    return table;

    }

    public String getPublicKey() {
    return publicKey;
    }
    public String getBoard() {
    return board;
    }
    public String getContent() {
    return content;
    }
    public String getFrom() {
    return from;
    }
    public String getSubject() {
    return subject;
    }
    public String getDate() {
    return date;
    }
    public String getTime() {
    return time;
    }
    public String getIndex() {
    return index;
    }
    public File getFile() {
    return file;
    }

    public String[] getRow() {
    String fatFrom = from;
    File newMessage = new File(file.getPath() + ".lck");

    if (newMessage.isFile()) {
        // this is the point where new messages get its bold look,
        // this is resetted in TOF.evalSelection to non-bold on first view
        fatFrom = new StringBuffer().append("<html><b>").append(from).append("</b></html>").toString();
    }
    if ( (content.indexOf("<attached>") != -1 && content.indexOf("</attached>") != -1) ||
        (content.indexOf("<board>") != -1 && content.indexOf("</board>") != -1) ){
        if (fatFrom.startsWith("<html><b>"))
        fatFrom = "<html><b><font color=\"blue\">" + from + "</font></b></html>";
        else
        fatFrom = "<html><font color=\"blue\">" + from + "</font></html>";
    }

    //String[] row = {index, fatFrom, subject, date + " " + time};
    String[] row = {index, fatFrom, subject, date, time};
    return row;
    }

    /**Set*/
    public void setBoard(String board) {
    this.board = board;
    }
    public void setContent(String content) {
    this.content = content;
    }
    public void setFrom(String from) {
    this.from = from;
    }
    public void setSubject(String subject) {
    this.subject = subject;
    }
    public void setDate(String date) {
    this.date = date;
    }
    public void setTime(String time) {
    this.time = time;
    }
    public void setFile(File file) {
    this.file = file;
    }
    public void setIndex(String index) {
    this.index = index;
    }

    public boolean isValid() {

    if (date.equals(""))
        return false;
    if (time.equals(""))
        return false;
    if (subject.equals(""))
        return false;
    if (board.equals(""))
        return false;
    if (from.equals(""))
        return false;

    if (from.length() > 256)
        return false;
    if (subject.length() > 256)
        return false;
    if (board.length() > 256)
        return false;
    if (date.length() > 22)
        return false;
    if (content.length() > 32000)
        return false;

    return true;
    }

    /**Set all values*/
    public void analyzeFile()
    {
        try {
            String message = new String(FileAccess.readByteArray(file));
            if( !message.startsWith("Empty") )
            {
                Vector lines = FileAccess.readLines(file);
                this.board = SettingsFun.getValue(lines, "board");
                this.from = SettingsFun.getValue(lines, "from");
                this.subject = SettingsFun.getValue(lines, "subject");
                this.board = SettingsFun.getValue(lines, "board");
                this.date = SettingsFun.getValue(lines, "date");
                this.time = SettingsFun.getValue(lines, "time");
                this.publicKey = SettingsFun.getValue(lines, "publicKey");

                int offset = 17;
                int contentStart = message.indexOf("--- message ---\r\n");
                if( contentStart == -1 )
                {
                    contentStart = message.indexOf("--- message ---");
                    offset = 15;
                }

                if( contentStart != -1 )
                    this.content = message.substring(contentStart + offset, message.length());
                else
                    this.content = "";

                String filename = file.getName();
                this.index = (filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf(".txt"))).trim();

                for( int i = 0; i < evilChars.length; i++ )
                {
                    this.from = this.from.replace(evilChars[i], '_');
                    this.subject = this.subject.replace(evilChars[i], '_');
                    this.date = this.date.replace(evilChars[i], '_');
                    this.time = this.time.replace(evilChars[i], '_');
                }
            }
        } catch(Exception ex) {
            System.out.println("ERROR in MessageObject: could not read file '"+file.getPath()+"'");
            ex.printStackTrace();
        }
    }

    /**Constructor*/
    public MessageObject(File file)
    {
        this();
        this.file = file;
        analyzeFile();
    }

    /**Constructor*/
    public MessageObject() {
    this.board = "";
    this.from = "";
    this.subject = "";
    this.board = "";
    this.date = "";
    this.time = "";
    this.content = "";
    this.publicKey = "";
    }

}
