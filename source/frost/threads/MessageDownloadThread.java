/*
  MessageDownloadThread.java / Frost
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

import java.io.File;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.Element;

import frost.*;
import frost.fcpTools.*;
import frost.crypt.*;
import frost.gui.objects.FrostBoardObject;
import frost.identities.*;
import frost.identities.Identity;
import frost.messages.*;

/**
 * Downloads messages
 */
public class MessageDownloadThread
    extends BoardUpdateThreadObject
    implements BoardUpdateThread
{
    public FrostBoardObject board;
    private int downloadHtl;
    private String keypool;
    private int maxMessageDownload;
    private String destination;
    private boolean secure;
    private String publicKey;
    private boolean flagNew;
    
	private static Logger logger = Logger.getLogger(MessageDownloadThread.class.getName());

    public int getThreadType()
    {
        if (flagNew)
        {
            return BoardUpdateThread.MSG_DNLOAD_TODAY;
        }
        else
        {
            return BoardUpdateThread.MSG_DNLOAD_BACK;
        }
    }

    public void run()
    {
        notifyThreadStarted(this);

        try
        {

            String tofType;
            if (flagNew)
                tofType = "TOF Download";
            else
                tofType = "TOF Download Back";

            // Wait some random time to speed up the update of the TOF table
            // ... and to not to flood the node
            int waitTime = (int) (Math.random() * 5000);
            // wait a max. of 5 seconds between start of threads
            mixed.wait(waitTime);

			logger.info("TOFDN: " + tofType + " Thread started for board " + board.toString());

            if (isInterrupted())
            {
                notifyThreadFinished(this);
                return;
            }

            // switch public / secure board
            if (board.isPublicBoard() == false)
            {
                publicKey = board.getPublicKey();
                secure = true;
            }
            else // public board
                {
                secure = false;
            }

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (this.flagNew)
            {
                // download only actual date
                downloadDate(cal);
            }
            else
            {
                // download up to maxMessages days to the past
                GregorianCalendar firstDate = new GregorianCalendar();
                firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                firstDate.set(Calendar.YEAR, 2001);
                firstDate.set(Calendar.MONTH, 5);
                firstDate.set(Calendar.DATE, 11);
                int counter = 0;
                while (!isInterrupted()
                    && cal.after(firstDate)
                    && counter < maxMessageDownload)
                {
                    counter++;
                    cal.add(Calendar.DATE, -1); // Yesterday
                    downloadDate(cal);
                }
            }
			logger.info("TOFDN: " + tofType + " Thread stopped for board " + board.toString());
        }
        catch (Throwable t)
        {
        	logger.log(Level.SEVERE, Thread.currentThread().getName() + ": Oo. Exception in MessageDownloadThread:", t);
        }
        notifyThreadFinished(this);
    }

    /**Returns true if message is duplicate*/
    private boolean exists(File file)
    {
        File[] fileList = (file.getParentFile()).listFiles();
        String one = null;

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                if (!fileList[i].equals(file)
                    && fileList[i].getName().endsWith(".sig") == false
                    && // dont check .sig files
                fileList[i].getName().indexOf(
                    board.getBoardFilename())
                        != -1
                    && file.getName().indexOf(board.getBoardFilename()) != -1)
                {
                    if (one == null) // load new file only 1 time
                    {
                        one = FileAccess.readFile(file);
                    }
                    String two = FileAccess.readFile(fileList[i]);
                    if (one.equals(two))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void downloadDate(GregorianCalendar calDL)
    {
        VerifyableMessageObject currentMsg = null;
        String dirdate = DateFun.getDateOfCalendar(calDL);
        String fileSeparator = System.getProperty("file.separator");

        destination =
            new StringBuffer()
                .append(keypool)
                .append(board.getBoardFilename())
                .append(fileSeparator)
                .append(dirdate)
                .append(fileSeparator)
                .toString();

        File makedir = new File(destination);
        if (!makedir.exists())
        {
            makedir.mkdirs();
        }

        File checkLockfile = new File(destination + "locked.lck");
        int index = 0;
        int failures = 0;
        int maxFailures;
        if (flagNew)
        {
            maxFailures = 3; // skip a maximum of 2 empty slots for today
        }
        else
        {
            maxFailures = 2; // skip a maximum of 1 empty slot for backload
        }

        while (failures < maxFailures && (flagNew || !checkLockfile.exists()))
        {
            byte[] metadata = null;
            try
            { //make a wide net so that evil messages don't kill us
                String val =
                    new StringBuffer()
                        .append(destination)
                        .append(System.currentTimeMillis())
                        .append(".xml.msg")
                        .toString();
                File testMe = new File(val);
                val =
                    new StringBuffer()
                        .append(destination)
                        .append(dirdate)
                        .append("-")
                        .append(board.getBoardFilename())
                        .append("-")
                        .append(index)
                        .append(".xml")
                        .toString();
                File testMe2 = new File(val);
                if (testMe2.length() > 0) // already downloaded
                {
                    index++;
                    failures = 0;
                }
                else
                {
                    String downKey = null;
                    if (secure)
                    {
                        downKey =
                            new StringBuffer()
                                .append(publicKey)
                                .append("/")
                                .append(board.getBoardFilename())
                                .append("/")
                                .append(dirdate)
                                .append("-")
                                .append(index)
                                .append(".xml")
                                .toString();
                    }
                    else
                    {
                        downKey =
                            new StringBuffer()
                                .append("KSK@frost/message/")
                                .append(
                                    frame1.frostSettings.getValue(
                                        "messageBase"))
                                .append("/")
                                .append(dirdate)
                                .append("-")
                                .append(board.getBoardFilename())
                                .append("-")
                                .append(index)
                                .append(".xml")
                                .toString();
                    }

                    try
                    {
                        boolean fastDownload = !flagNew;
                        // for backload use fast download, deep for today

                        FcpResults res =
                            FcpRequest.getFile(
                                downKey,
                                null,
                                testMe,
                                downloadHtl,
                                false,
                                fastDownload);
                        if (res == null)
                            metadata = null;
                        else
                            metadata = res.getRawMetadata();
                        // TODO: if metadata==null, not signed message
                        mixed.wait(111);
                        // wait some time to not to hurt the node on next retry
                    }
                    catch (Throwable t)
                    {
						logger.log(Level.SEVERE, "Exception thrown in downloadDate(GregorianCalendar calDL)", t);
                    }

                    // Download successful?
                    if (testMe.length() > 0)
                    {
                        testMe.renameTo(testMe2);
                        testMe = testMe2;

                        //check if it is a duplicate
                        String messageId = Core.getCrypto().digest(testMe);

                        // Does a duplicate message exist?
                        if (!exists(testMe)
                            && !Core.getMessageSet().contains(messageId))
                        {

                            Core.getMessageSet().add(messageId);

                            //if no metadata, message wasn't signed
                            if (metadata == null)
                            {
                                //unzip
                                byte[] unzippedXml =
                                    FileAccess.readZipFileBinary(testMe);
                              //  System.out.println("flen="+testMe.length());
                               // System.out.println("DBG='"+new String(FileAccess.readByteArray(testMe))+"'");                                
                                FileAccess.writeByteArray(unzippedXml, testMe);

                                try
                                {
                                    currentMsg =
                                        new VerifyableMessageObject(testMe);
                                }
                                catch (Exception ex)
                                {
									logger.log(Level.SEVERE, "Exception thrown in downloadDate(GregorianCalendar calDL)", ex);
                                    // TODO: file could not be read, mark it invalid not to confuse gui
                                    index++;
                                    continue;
                                }

                                //set to unsigned
                                currentMsg.setStatus(
                                    VerifyableMessageObject.OLD);
                                    
                                // check and maybe add msg to gui
                                addMessageToGui(currentMsg, testMe, true);
                                
                                index++;
                                continue;
                            } //end of if no metadata part

                            //verify the zipped message
                            byte[] plaintext = FileAccess.readByteArray(testMe);
                            MetaData _metaData = null;
                            try
                            {
                            	File tempMeta = new File("tempMeta");
                            	FileAccess.writeByteArray(metadata,tempMeta);
                            	Element el = XMLTools.parseXmlFile(tempMeta,false).getDocumentElement();
                            	tempMeta.delete();
                                _metaData = MetaData.getInstance(plaintext, el);
                            }
                            catch (Throwable t)
                            {
                                //TODO: metadata failed, do something
								logger.log(Level.SEVERE, "metadata couldn't be read. " +
                                        		"Offending file saved as badmetadata.xml - send to a dev for analysis", t);
                                File badmetadata = new File("badmetadata.xml");
                                FileAccess.writeByteArray(
                                    metadata,
                                    badmetadata);
                                index++;
                                failures = 0;
                                continue;
                            }
                            	
                            //TODO: at this point branch and process encryption metadata
                            //differently
                            
                            assert _metaData.getType() == MetaData.SIGN ||
                            	_metaData.getType() == MetaData.ENCRYPT :
                            	"unknown type of metadata";
                            
                            //start of signed message processing
                            if (_metaData.getType() == MetaData.SIGN) {
							SignMetaData metaData=null;
                            	metaData= (SignMetaData)_metaData;
                            //check if we have the owner already on the lists
                            String _owner =
                                metaData.getPerson().getUniqueName();

                            Identity owner;
                            //check friends
                            owner = identities.getFriends().get(_owner);
                            //if not, check neutral
                            if (owner == null)
                                owner = identities.getNeutrals().get(_owner);
                            //if not, check enemies
                            if (owner == null)
                                owner = identities.getEnemies().get(_owner);
                            //if still not, use the parsed id
                            if (owner == null)
                            {
                                owner = metaData.getPerson();
                                owner.noFiles = 0;
                                owner.noMessages = 1;
								identities.getNeutrals().add(owner);
                            }

                            //verify! :)
                            boolean valid =
                                Core.getCrypto().detachedVerify(
                                    plaintext,
                                    owner.getKey(),
                                    metaData.getSig());

                            //unzip

                            byte[] unzippedXml =
                                FileAccess.readZipFileBinary(testMe);
                            FileAccess.writeByteArray(unzippedXml, testMe);

                            //create object
                            try
                            {
                                currentMsg =
                                    new VerifyableMessageObject(testMe);
                            }
                            catch (Exception ex)
                            {
								logger.log(Level.SEVERE, "Exception thrown in downloadDate(GregorianCalendar calDL)", ex);
                                // TODO: file could not be read, mark it invalid not to confuse gui
                                index++;
                                continue;
                            }

                            //then check if the signature was ok
                            if (!valid)
                            {
                                currentMsg.setStatus(
                                    VerifyableMessageObject.TAMPERED);
                                logger.warning("TOFDN: message failed verification");
                                addMessageToGui(currentMsg, testMe, false);    
                                index++;
                                continue;
                            }

                            //make sure the pubkey and from fields in the xml file are the same
                            //as those in the metadata
                            String metaDataHash =
                                mixed.makeFilename(
                                    Core.getCrypto().digest(
                                        metaData.getPerson().getKey()));
                            String messageHash =
                                mixed.makeFilename(
                                    currentMsg.getFrom().substring(
                                        currentMsg.getFrom().indexOf("@") + 1,
                                        currentMsg.getFrom().length()));

                            if (!metaDataHash.equals(messageHash))
                            {
                                logger.warning("hash in metadata doesn't match hash in message!\n" +
                                			   "metadata : " + metaDataHash + 
											   " , message: " + messageHash);
                                currentMsg.setStatus(
                                    VerifyableMessageObject.TAMPERED);
                                    
                                addMessageToGui(currentMsg, testMe, false);
                                
                                index++;
                                continue;
                            }

                            //if it is, we have the user either on the good, bad or neutral lists
                            if (identities.getFriends().containsKey(_owner))
                                currentMsg.setStatus(
                                    VerifyableMessageObject.VERIFIED);
                            else if (identities.getEnemies().containsKey(_owner))
                                currentMsg.setStatus(
                                    VerifyableMessageObject.FAILED);
                            else
                                currentMsg.setStatus(
                                    VerifyableMessageObject.PENDING);

                            /*        Encryption will be done+handled using private boards
                            */
                            // verify the message date and time
                            if (currentMsg.isValidFormat(calDL) == false)
                            {
                                // TODO: file contains invalid data or time, skip and
                                //        mark it to not try it again(?) 
                            }

                            //File sig = new File(testMe.getPath() + ".sig");

                            // Is this a valid message?
                            addMessageToGui(currentMsg, testMe, true);
                            }  //endif signed message
                            //start encrypted message processing
                            else if (_metaData.getType() == MetaData.ENCRYPT){
                            	//1. check if the message is for myself
                            	if (!_metaData.getPerson().getUniqueName().equals(
												identities.getMyId().getUniqueName())) {
                            		logger.fine("encrypted message was for "+
                            				_metaData.getPerson().getUniqueName());
                            		
									index++;
									failures = 0;
									 continue;
                            	}
                            	
                            	//2. if yes, decrypt
                            	byte []cipherText = FileAccess.readByteArray(testMe);
                            	byte []plainText = Core.getCrypto().decrypt(cipherText,identities.getMyId().getPrivKey());
                            	//TODO: continue tommorow
                            }//endif encrypted message
                        }
                        else
                        {
                            logger.info(Thread.currentThread().getName() +
                                    	": TOFDN: ****** Duplicate Message : " +
                                    	testMe.getName() +
                                    	" *****");
                            FileAccess.writeFile("Empty", testMe);
                        }
                        index++;
                        failures = 0;
                    }
                    else
                    {
/*                    if( !flagNew )
                    {
                        Core.getOut().println("TOFDN: *** Increased TOF index for board '"+board.toString()+"' ***");
                    }*/
                        failures++;
                        index++;
                    }
                }
                if (isInterrupted())
                    return;
            }
            catch (Throwable t)
            {
				logger.log(Level.SEVERE, "Exception thrown in downloadDate(GregorianCalendar calDL)", t);
                index++;
            }
        } // end-of: while
    }

    private void addMessageToGui(
        VerifyableMessageObject currentMsg,
        File testMe,
        boolean markAsNew)
    {
        if (currentMsg.isValid())
        {
            if (TOF.blocked(currentMsg, board) && testMe.length() > 0)
            {
                board.incBlocked();
                logger.info("TOFDN: ########### blocked message for board '" +
                        		board.toString() +
                        		"' #########\n");
            }
            else
            {
                // write the NEW message indicator file
                if( markAsNew )
                {                                
                    FileAccess.writeFile(
                        "This message is new!",
                        testMe.getPath() + ".lck");
                }
                // add new message or notify of arrival
                TOF.addNewMessageToTable(testMe, board, markAsNew);
                //add all files indexed files
                Iterator it =
                    currentMsg
                        .getAttachmentList()
                        .getAllOfType(Attachment.FILE)
                        .iterator();
                while (it.hasNext())
                {
                    SharedFileObject current = ((FileAttachment)it.next()).getFileObj();
                    if (current.getOwner() != null) 
                        	Index.add(current, board);
                }
                //add all boards to the list of known boards
                Core.addNewKnownBoards(
                    currentMsg.getAttachmentList().getAllOfType(Attachment.BOARD) );
            }
        }
        else
        {
            FileAccess.writeFile("Empty", testMe);
        }
    }

	/**Constructor*/ //
	public MessageDownloadThread(
		boolean fn,
		FrostBoardObject boa,
		int dlHtl,
		String kpool,
		String maxmsg,
		FrostIdentities newIdentities) {
			
		super(boa, newIdentities);
		
		this.flagNew = fn;
		this.board = boa;
		this.downloadHtl = dlHtl;
		this.keypool = kpool;
		this.maxMessageDownload = Integer.parseInt(maxmsg);
	}
}
