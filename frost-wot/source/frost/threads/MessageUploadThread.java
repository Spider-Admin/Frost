/*
  MessageUploadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import frost.crypt.*;
import frost.*;
import frost.identities.*;
import frost.gui.*;
import frost.gui.objects.*;

/**
 * Uploads a message to a certain message board
 */
public class MessageUploadThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    private static boolean debug=true;
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
    final static boolean DEBUG = false;

    private Frame frameToLock;
    private FrostBoardObject board;
    private String from;
    private String subject;
    private String text;
    private String messageUploadHtl;
    private String messageDownloadHtl;
    private String date;
    private String time;
    private String keypool;
    private String destination;
    private String privateKey;
    private String publicKey;
    private boolean secure;
    private boolean silent; // don't show 'add board' popup if true
    private boolean signed;
    private boolean encryptSign;
    private Identity recipient;


    public int getThreadType()
    {
        return BoardUpdateThread.MSG_UPLOAD;
    }

    /**
     * Extracts all attachments from a message and returns
     * them in a Vector
     * @return Vector with paths of the attachments
     */
    private Vector getAttachments() {
    int start = text.indexOf("<attach>");
    int end = text.indexOf("</attach>", start);
    Vector attachments = new Vector();

    while (start != -1 && end != -1) {
        attachments.add(text.substring(start + 8, end));
        start = text.indexOf("<attach>", end);
        end = text.indexOf("</attach>", start);
    }

    return attachments;
    }

    /**
     *sign
     */
    private void sign() {
        if (from.compareTo(frame1.getMyId().getName())==0) { //nick same as my identity
    if(debug) System.out.println("signing message");
        text=new String(text +"<key>" + (frame1.getMyId()).getKeyAddress() + "</key>");
        if (encryptSign && recipient !=null) {
            System.out.println("encrypting message");
            text=frame1.getCrypto().encryptSign(text,frame1.getMyId().getPrivKey(),recipient.getKey());
            subject = new String("ENCRYPTED MSG FOR : " + recipient.getStrippedName());
            }
        else
            text=frame1.getCrypto().sign(text,(frame1.getMyId()).getPrivKey());

        from=new String(from + "@" + frame1.getCrypto().digest(frame1.getMyId().getKey()));
        signed=true;
    }
    }

    /**
     * Uploads attachments
     */
    private void uploadAttachments() {
    Vector attachments = getAttachments();

    for (int i = 0; i < attachments.size(); i++) {
        String attachment = (String)attachments.elementAt(i);
        String[] result = {"", ""};

        System.out.println("Uploading attachment " + attachment + " with HTL " + frame1.frostSettings.getValue("htlUpload") + ".");

        while( !result[0].equals("KeyCollision") && !result[0].equals("Success"))
        result = FcpInsert.putFile("CHK@", attachment, frame1.frostSettings.getValue("htlUpload"), true, true);

        String chk = result[1];
        int position = text.indexOf("<attach>" + attachment + "</attach>");
        int length = attachment.length() + 17;
        File attachedFile = new File(attachment);
        String newText = text.substring(0, position) + "<attached>" + attachedFile.getName();
        newText += " * " + chk + "</attached>";
        newText += text.substring(position + length, text.length());
        text = newText;
    }
    }

    public void run()
    {
        notifyThreadStarted(this);

        boolean retry = true;

        // switch public / secure board
        if( board.isWriteAccessBoard() )
        {
            privateKey = board.getPrivateKey();
            publicKey = board.getPublicKey();
            secure = true;
        }
        else
        {
            secure = false;
        }

        if( DEBUG ) System.out.println("tofUpload: " + board.toString() + " secure: " + secure);
        System.out.println("Uploading message to '" + board.toString() + "' board with HTL " + messageUploadHtl + ".");

        uploadAttachments();
        sign();

        String fileSeparator = System.getProperty("file.separator");
        destination = keypool + board.getBoardFilename() + fileSeparator + DateFun.getDate() + fileSeparator;
        File checkDestination = new File(destination);
        if( !checkDestination.isDirectory() )
            checkDestination.mkdirs();

        // Generate file to upload
        String uploadMe = "unsent" + String.valueOf(System.currentTimeMillis()) + ".txt"; // new filename
        String content = new String();
        content += "board=" + board.toString() + "\r\n";
        content += "from=" + from + "\r\n";
        content += "subject=" + subject + "\r\n";
        content += "date=" + date + "\r\n";
        content += "time=" + time + "\r\n";
        content += "--- message ---\r\n";
        content += text;

        File messageFile = new File(destination + uploadMe);
        FileAccess.writeFile(content, messageFile); // Write to disk

        while( retry )
        {
            // Search empty slut (hehe)
            boolean success = false;
            int index = 0;
            String output = new String();
            int tries = 0;
            boolean error = false;
            while( !success )
            {
                // Does this index already exist?
                File testMe = new File(destination + date + "-" + board.getBoardFilename() + "-" + index + ".txt");
                if( testMe.length() > 0 )
                { // already downloaded
                    String contentOne = (FileAccess.readFile(messageFile)).trim();
                    String contentTwo = (FileAccess.readFile(testMe)).trim();
                    if( DEBUG ) System.out.println(contentOne);
                    if( DEBUG ) System.out.println(contentTwo);
                    if( contentOne.equals(contentTwo) )
                    {
                        if( DEBUG ) System.out.println("Message has already been uploaded.");
                        success = true;
                    }
                    else
                    {
                        index++;
                        if( DEBUG ) System.out.println("File exists, increasing index to " + index);
                    }
                }
                else
                { // probably empty
                    String[] result = new String[2];
                    if( secure )
                    {

                        String upKey = privateKey + "/" + board.getBoardFilename() + "/" + date + "-" + index + ".txt";
                        if( DEBUG ) System.out.println(upKey);
                        result = FcpInsert.putFile(upKey, destination + uploadMe, messageUploadHtl, false, true);
                    }
                    else
                    {
                        // Temporary hack for wrong name space
                        String upKey = "KSK@sftmeage/" + frame1.frostSettings.getValue("messageBase") + "/" + date + "-" + board.getBoardFilename() + "-" + index + ".txt";
                        if( DEBUG ) System.out.println(upKey);
                        result = FcpInsert.putFile(upKey, destination + uploadMe, messageUploadHtl, false, true);
                        // END Temporary hack for wrong name space
                        if( result[0].equals("Success") )
                        {
                            /* String */
                            upKey = "KSK@frost/message/" + frame1.frostSettings.getValue("messageBase") + "/" + date + "-" + board.getBoardFilename() + "-" + index + ".txt";
                            if( DEBUG ) System.out.println(upKey);
                            /*result =*/FcpInsert.putFile(upKey, destination + uploadMe, messageUploadHtl, false, true);
                        }
                    }

                    if( result[0] == null || result[1] == null )
                    {
                        result[0] = "Error";
                        result[1] = "Error";
                    }

                    if( result[0].equals("Success") )
                    {
                        success = true;
                    }
                    else
                    {
                        if( result[0].equals("KeyCollision") )
                        {

                            // ************* Temporary freenet bug workaround ******************
                            String compareMe = String.valueOf(System.currentTimeMillis()) + ".txt";
                            //              String requestMe = "KSK@frost/message/" + frame1.frostSettings.getValue("messageBase") + "/" + date + "-" + board + "-" + index + ".txt";
                            String requestMe = "KSK@sftmeage/" + frame1.frostSettings.getValue("messageBase") + "/" + date + "-" + board.getBoardFilename() + "-" + index + ".txt";
                            if( secure && publicKey.startsWith("SSK@") )
                            {
                                requestMe = publicKey + "/" + board.getBoardFilename() + "/" + date + "-" + index + ".txt";
                            }

                            if( FcpRequest.getFile(requestMe,
                                                   "Unknown",
                                                   keypool + compareMe,
                                                   messageDownloadHtl,
                                                   false) )
                            {
                                File numberOne = new File(keypool + compareMe);
                                File numberTwo = new File(destination + uploadMe);
                                String contentOne = (FileAccess.readFile(numberOne)).trim();
                                String contentTwo = (FileAccess.readFile(numberTwo)).trim();
                                if( DEBUG ) System.out.println(contentOne);
                                if( DEBUG ) System.out.println(contentTwo);
                                if( contentOne.equals(contentTwo) )
                                {
                                    success = true;
                                }
                                else
                                {
                                    index++;
                                    System.out.println("TOF Upload collided, increasing index to " + index);
                                }
                            }
                            else
                            {
                                index++;
                                System.out.println("TOF Upload collided, increasing index to " + index);
                            }
                        }
                        else
                        {
                            System.out.println("TOF upload failed (" + tries + "), retrying index " + index);
                            if( tries > 5 )
                            {
                                success = true;
                                error = true;
                            }
                            tries++;
                        }
                    }
                }
            }

            if( !error )
            {
                // this will show the message after receiving it
                messageFile.delete();

/*                File killMe = new File(destination + uploadMe);
                File killMe = messageFile;
                File newMessage = new File(destination + date + "-" + board.getBoardFilename() + "-" + index + ".txt");
                if( signed )
                    FileAccess.writeFile("GOOD", newMessage.getPath() + ".sig");
                killMe.renameTo(newMessage);*/

                frame1.updateTof = true;
                System.out.println("*********************************************************************");
                System.out.println("Message successfuly uploaded to the '" + board.toString() + "' board.");
                System.out.println("*********************************************************************");
                retry = false;
            }
            else
            {
                System.out.println("Error while uploading message.");

                // Uploading of that message failed. Ask the user if Frost
                // should try to upload the message another time.

                // TODO: allow to upload on next frost startup (simply not delete the messageFile)
                if( !silent )
                {
                    MessageUploadFailedDialog faildialog =
                    new MessageUploadFailedDialog(frameToLock,
                                                  10,
                                                  LangRes.getString("Upload of message failed"),
                                                  LangRes.getString("I was not able to upload your message."),
                                                  LangRes.getString("Retry"),
                                                  LangRes.getString("Cancel"));
                    faildialog.show();
                    retry = faildialog.getAnswer();
                    System.out.println("Will try to upload again: " + retry);
                    faildialog.dispose();
                }
                if( !retry )
                    messageFile.delete();
            }

            System.out.println("TOF Upload Thread finished");
        }
        notifyThreadFinished(this);
    } // end-of: run()

    /**Constructor*/
    public MessageUploadThread(FrostBoardObject board,
                               String from,
                               String subject,
                               String text ,
                               String msgUplHtl,
                               String keypool,
                               String msgDlHtl,
                               String date,
                               String time,
                               String recipient,
                               Frame frameToLock)
    {
        super(board);
        this.board = board;
        this.from = from;
        this.subject = subject;
        this.text = text;
        this.messageUploadHtl = msgUplHtl;
        this.keypool = keypool;
        this.messageDownloadHtl = msgDlHtl;

        if( date.length() == 0 && time.length() == 0 )
        {
            date = DateFun.getDate();
            time = DateFun.getFullExtendedTime()+"GMT";
        }
        this.date = date;
        this.time = time;

        if( recipient != null && recipient.length() > 0 )
        {
            encryptSign=true;
            this.recipient = frame1.getFriends().Get(recipient);
        }
        this.frameToLock = frameToLock;

        this.silent = false;
        this.signed = false;
    }
}
