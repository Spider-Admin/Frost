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

import java.awt.Frame;
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;

import frost.*;
import frost.FcpTools.FcpInsert;
import frost.crypt.MetaData;
import frost.gui.MessageUploadFailedDialog;
import frost.gui.objects.FrostBoardObject;
import frost.messages.*;

import frost.FcpTools.*;

/**
 * Uploads a message to a certain message board
 */
public class MessageUploadThread extends BoardUpdateThreadObject implements BoardUpdateThread
{
    final static boolean DEBUG = true;
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    private Frame frameToLock;
    private FrostBoardObject board;
    private MessageObject message;
    private int messageUploadHtl;
    private String keypool;
    private String privateKey;
    private String publicKey;
    private boolean secure;
//    private Identity recipient;
    
    File messageFile;
    
    private byte[] metadata = null;

    public int getThreadType()
    {
        return BoardUpdateThread.MSG_UPLOAD;
    }

    /**
     * Uploads attachments.
     * This inserts the attached files into freenet.
     */
    private boolean uploadAttachments() 
    {
        List fileAttachments = this.message.getOfflineFiles();
        Iterator i = fileAttachments.iterator();
        while(i.hasNext())
        {
            SharedFileObject sfo = (SharedFileObject)i.next();
            
            String[] result = {"", ""};
            int uploadHtl = frame1.frostSettings.getIntValue("htlUpload");
            Core.getOut().println("TOFUP: Uploading attachment " +
                               sfo.getFile().getPath() +
                               " with HTL " + uploadHtl);
            
            int maxTries = 3;
            int tries = 0;
            while( tries < maxTries &&
                   !result[0].equals("KeyCollision") &&
                   !result[0].equals("Success"))
            {
                try {
                    result = FcpInsert.putFile("CHK@",
                                               sfo.getFile(),
                                               uploadHtl,
                                               true); // doRedirect
                }
                catch(Exception ex)
                {
                    result = new String[1];
                    result[0]="Error";
                }
                tries++;
            }
            if( result[0].equals("KeyCollision") ||
                result[0].equals("Success") )
            {
                Core.getOut().println("TOFUP: Upload of attachment '"+sfo.getFile().getPath()+"' was successful.");
                String chk = result[1];
                sfo.setKey(chk);
                sfo.setFilename( sfo.getFile().getName()); // remove path from filename
                
                
                if (sfo instanceof FECRedirectFileObject){
					Core.getOut().println("attaching redirect to file "+sfo.getFile().getName());

						FecSplitfile splitFile = new FecSplitfile(sfo.getFile());
						if (!splitFile.uploadInit()) 
							throw new Error("file was just uploaded, but .redirect missing!");
						
						((FECRedirectFileObject)sfo).setRedirect(
								new String(FileAccess.readByteArray(splitFile.getRedirectFile())));
                } else
                	Core.getOut().println("not attaching redirect");
                
				sfo.setFile(null); //FIXME: why ?
                // BBACKFLAG: serialize the xml file to disk to save the uploading state
                File tmpFile = new File(this.messageFile.getPath() + ".tmp");
                boolean wasOK = false; 
                try {
                    Document doc = XMLTools.createDomDocument();
                    doc.appendChild( message.getXMLElement(doc) );
                    wasOK = XMLTools.writeXmlFile(doc, tmpFile.getPath());
                }
                catch(Throwable ex)
                {
                    ex.printStackTrace(Core.getOut());
                }
                if( wasOK && tmpFile.length() > 0 )
                {
                    this.messageFile.delete();
                    tmpFile.renameTo( this.messageFile );
                }
                else
                {
                    // not successful, will not track this upload, but continue uploading
                    tmpFile.delete();
                }
                //return true;
            }
            else
            {
                System.out.println("TOFUP: Upload of attachment '"+sfo.getFile().getPath()+"' was NOT successful.");
                return false;
            }
        }
        return true; // nothing to upload
    }

    public void run()
    {
        notifyThreadStarted(this);
        try {

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

        System.out.println("TOFUP: Uploading message to board '" + board.toString() + "' with HTL " + messageUploadHtl);
        
        // first save msg to be able to resend on crash        
        boolean wasOK = false; 
        try {
            Document doc = XMLTools.createDomDocument();
            doc.appendChild( message.getXMLElement(doc) );
            wasOK = XMLTools.writeXmlFile(doc, this.messageFile.getPath());
        }
        catch(Throwable ex)
        {
            ex.printStackTrace(Core.getOut());
        }
        if( !wasOK || messageFile.length() == 0)
        {
            // now we really have a problem:
            //  writing of file was not successful, so this msg will be lost!
            Core.getOut().println("This was a HARD error and the file to upload is lost, please report to a dev!");
            notifyThreadFinished(this);
            return;
        }
        

// BBACKFLAG: ask user if uploading of X files is allowed!
        boolean uploadOK = uploadAttachments();
        if( uploadOK == false )
        {
            JOptionPane.showMessageDialog(frameToLock, 
                    "One or more attachments failed to upload.\n" +
                    "Will retry to upload attachments and message on next startup.", 
                    "Attachment upload failed", 
                    JOptionPane.ERROR_MESSAGE);
            // don't delete message file for uploading on next startup                    
            notifyThreadFinished(this);
            return;
        }
        
        // Generate file to upload
        wasOK = false; 
        try {
            Document doc = XMLTools.createDomDocument();
            doc.appendChild( message.getXMLElement(doc) );
            wasOK = XMLTools.writeXmlFile(doc, this.messageFile.getPath());
        }
        catch(Throwable ex)
        {
            ex.printStackTrace(Core.getOut());
        }
        if( !wasOK || messageFile.length() == 0)
        {
            // now we really have a problem:
            //  writing of file was not successful, so this msg will be lost!
            Core.getOut().println("This was a HARD error and the file to upload is lost, please report to a dev!");
            notifyThreadFinished(this);
            return;
        }
 
        // zip the xml file to a temp file, sign this file and upload it
        File uploadZipFile = new File( this.messageFile.getPath() + ".upltmp" );
        uploadZipFile.delete(); // just for the case
        
        FileAccess.writeZipFile(FileAccess.readByteArray( this.messageFile ), 
                                "entry", uploadZipFile); 
        
        // now maybe sign the msg before start to upload,
        // we have to sign if the From is our complete unique id set by MessageFrame
        if( message.getFrom().equals(frame1.getMyId().getUniqueName()) //nick same as my identity
         || message.getFrom().equals(mixed.makeFilename(Core.getMyId().getUniqueName())))  //serialization may have changed it
        {
            byte[] zipped = FileAccess.readByteArray(uploadZipFile);
            MetaData md = new MetaData(zipped);
            this.metadata = XMLTools.getRawXMLDocument(md);
        }

        // define the destination where we check for existing files
        String fileSeparator = System.getProperty("file.separator");
        String destination = new StringBuffer().append(keypool)
                                               .append(board.getBoardFilename())
                                               .append(fileSeparator)
                                               .append(DateFun.getDate())
                                               .append(fileSeparator).toString();
        File makedir = new File(destination);
        if( !makedir.exists() )
        {
            makedir.mkdirs();
        }

        while( retry )
        {
            // Search empty slot
            boolean success = false;
            int index = 0;
            String output = new String();
            int tries = 0;
            int maxTries = 5;
            boolean error = false;
            while( !success )
            {
                // Does this index already exist?
                String testFilename = new StringBuffer().append(destination)
                                                        .append(this.message.getDate())
                                                        .append("-")
                                                        .append(board.getBoardFilename())
                                                        .append("-")
                                                        .append(index)
                                                        .append(".xml").toString();
                File testMe = new File(testFilename);
                if( testMe.exists() && testMe.length() > 0 )
                {
                    // already on local disk, compare contents
                    // TODO: load file and compare contents, but not
                    //       date + time, these will be different always
/*                    
                    String contentOne = (FileAccess.readFile(messageFile)).trim();
                    String contentTwo = (FileAccess.readFile(testMe)).trim();
                    //if( DEBUG ) System.out.println(contentOne);
                    //if( DEBUG ) System.out.println(contentTwo);
                    if( contentOne.equals(contentTwo) )
                    {
                        if( DEBUG ) System.out.println("TOFUP: Message has already been uploaded.");
                        success = true;
                    }
                    else
                    {
                        index++;
                        //if( DEBUG ) System.out.println("TOFUP: File exists, increasing index to " + index);
                    }
*/                    
                    index++;
                }
                else
                {
                    // probably empty, check if other threads currently try to insert to this index
                    File lockRequestIndex = new File( testMe.getPath() + ".lock" );
                    boolean lockFileCreated = false;
                    try { lockFileCreated = lockRequestIndex.createNewFile(); }
                    catch(IOException ex) {
                        System.out.println("ERROR: MessageUploadThread.run(): unexpected IOException, terminating thread ...");
                        ex.printStackTrace(System.out);
                        notifyThreadFinished(this);
                        return;
                    }

                    if( lockFileCreated == false )
                    {
                        // another thread tries to insert using this index, try next
                        index++;
                        if( DEBUG ) System.out.println("TOFUP: Other thread tries this index, increasing index to " + index);
                        continue; // while
                    }
                    else
                    {
                        // we try this index
                        lockRequestIndex.deleteOnExit();
                    }

                    // try to insert message
                    String[] result = new String[2];
                    String upKey = null;
                    if( secure )
                    {
                        upKey = new StringBuffer().append(privateKey)
                                                  .append("/")
                                                  .append(board.getBoardFilename())
                                                  .append("/")
                                                  .append(this.message.getDate())
                                                  .append("-")
                                                  .append(index)
                                                  .append(".xml").toString();
                    }
                    else
                    {
                        upKey = new StringBuffer().append("KSK@frost/message/")
                                                  .append(frame1.frostSettings.getValue("messageBase"))
                                                  .append("/")
                                                  .append(this.message.getDate())
                                                  .append("-")
                                                  .append(board.getBoardFilename())
                                                  .append("-")
                                                  .append(index)
                                                  .append(".xml").toString();
                    }

                    try {
                        result = FcpInsert.putFile(upKey,
                                                   uploadZipFile,
                                                   this.metadata, // is null for unsigned upload
                                                   messageUploadHtl,
                                                   false); // doRedirect
                    } catch(Throwable t)
                    {
                        System.out.println("TOFUP - Error in run()/FcpInsert.putFile:");
                        t.printStackTrace(System.out);
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
                            index++;
                            System.out.println("TOFUP: Upload collided, increasing index to " + index);
                        }
                        else
                        {
                            if( tries > maxTries )
                            {
                                success = true;
                                error = true;
                            }
                            else
                            {
                                System.out.println("TOFUP: Upload failed (try no. "+tries+" of "+maxTries+"), retrying index " + index);
                                tries++;
                            }
                        }
                    }
                    // finally delete the index lock file
                    if( lockFileCreated == true )
                    {
                        lockRequestIndex.delete();
                    }
                }
            }
            
            if( !error )
            {
                // we will see the message if received from freenet
                messageFile.delete();
                uploadZipFile.delete();

                System.out.println("*********************************************************************");
                System.out.println("Message successfuly uploaded to board '" + board.toString() + "'.");
                System.out.println("*********************************************************************");
                retry = false;
            }
            else
            {
                System.out.println("TOFUP: Error while uploading message.");

                // Uploading of that message failed. Ask the user if Frost
                // should try to upload the message another time.
                MessageUploadFailedDialog faildialog =
                new MessageUploadFailedDialog(frameToLock,
                                              10,
                                              LangRes.getString("Upload of message failed"),
                                              LangRes.getString("I was not able to upload your message."),
                                              LangRes.getString("Retry"),
                                              "Retry on next startup", // TODO: translate
                                              "Discard message");
                int answer = faildialog.startDialog();
                if( answer == 1 ) // Retry now - pressed
                {
                    retry = true;
                    System.out.println("TOFUP: Will try to upload again.");
                }
                else if( answer == 2 ) // retry on next startup - pressed
                {
                    uploadZipFile.delete();
                    retry = false; // dont delete msg. file, will be found+upload on next startup
                    System.out.println("TOFUP: Will try to upload again on next startup.");
                }
                else if( answer == 3 ) // cancel - pressed
                {
                    retry = false;

                    uploadZipFile.delete();
                    messageFile.delete(); 

                    System.out.println("TOFUP: Will NOT try to upload message again.");
                }
                else  // paranoia
                {
                    retry = true;
                    System.out.println("TOFUP: Paranoia - will try to upload message again.");
                }
                faildialog.dispose();
            }
            System.out.println("TOFUP: Upload Thread finished");
        }

        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in MessageUploadThread:");
            t.printStackTrace(System.out);
        }

        notifyThreadFinished(this);
    } // end-of: run()


    /**Constructor*/
    public MessageUploadThread(FrostBoardObject board, MessageObject mo) 
    {
        super(board);
        this.board = board;
        this.message = mo;

        // we start to upload now, so set actual time
        mo.setTime(DateFun.getFullExtendedTime()+"GMT");
        mo.setDate(DateFun.getDate());
        
        this.messageUploadHtl = frame1.frostSettings.getIntValue("tofUploadHtl");
        this.keypool = frame1.frostSettings.getValue("keypool.dir");
        this.frameToLock = frame1.getInstance();
        
        // this class always creates a new msg file on hd and deletes the file 
        // after upload was successful, or keeps it for next try
        String uploadMe = new StringBuffer()
                           .append(frame1.frostSettings.getValue("unsent.dir"))
                           .append("unsent")
                           .append(String.valueOf(System.currentTimeMillis()))
                           .append(".xml").toString();
        messageFile = new File(uploadMe);
    }
}
