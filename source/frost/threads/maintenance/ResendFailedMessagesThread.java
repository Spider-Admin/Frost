/*
 * Created on Sep 13, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.threads.maintenance;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;

import frost.Core;
import frost.FileAccess;
import frost.frame1;
import frost.gui.objects.FrostBoardObject;
import frost.messages.VerifyableMessageObject;


public class ResendFailedMessagesThread extends Thread
{
	private final Core core;
    Frame frameToLock;
    public ResendFailedMessagesThread(Core core, Frame frameToLock)
    {
        this.frameToLock = frameToLock;
		this.core = core;
    }
    public void run()
    {
        // give gui a chance to appear ... then start searching for unsent messages
        try { Thread.sleep(10000); } // wait 10 seconds
        catch(InterruptedException ex) { ; }
        if( isInterrupted() )
            return;

        System.out.println("Starting search for unsent messages ...");

        ArrayList entries = FileAccess.getAllEntries(new File(Core.frostSettings.getValue("unsent.dir")), ".txt");

        for( int i = 0; i < entries.size(); i++ )
        {
            File unsentMsgFile = (File)entries.get(i);
            if( unsentMsgFile.getName().startsWith("unsent") )
            {
                // Resend message
                VerifyableMessageObject mo = null;
                try {
                    mo = new VerifyableMessageObject(unsentMsgFile);
                } catch(Exception ex)
                {
                    Core.getOut().println("Could'nt read the message file, will not send message.");
                }
                
                if( mo != null && mo.isValid() )
                {
                    FrostBoardObject board = frame1.getInstance().getTofTree().getBoardByName( mo.getBoard() );
                    if( board == null )
                    {
                        System.out.println("Can't resend Message '"+mo.getSubject()+"', the target board '"+mo.getBoard()+
                                           "' was not found in your boardlist.");
                        // TODO: maybe delete msg? or it will always be retried to send
                        continue;
                    }
                    // message will be resigned before send, actual date/time will be used
                    // no more faking here :)
                    frame1.getInstance().getRunningBoardUpdateThreads().startMessageUpload(
                        board,
                        mo.getFrom(),
                        mo.getSubject(),
                        mo.getContent(),
                        "",
                        Core.frostSettings,
                        frameToLock,
                        null);
                    System.out.println("Message '" + mo.getSubject() + "' will be resent to board '"+board.toString()+"'.");
                }
                // check if upload was successful before deleting the file -
                //  is not needed, the upload thread creates new unsent file
                unsentMsgFile.delete();
            }
        }
        System.out.println("Finished search for unsent messages ...");
    }
}