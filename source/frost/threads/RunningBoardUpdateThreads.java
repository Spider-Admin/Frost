/*
  RunningBoardUpdateThreads.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.messages.*;
import frost.messaging.*;

/**
 * This class maintains the message download and upload threads.
 * Listeners for thread started and thread finished are provided.
 * @author $Author$
 * @version $Revision$
 */
public class RunningBoardUpdateThreads implements BoardUpdateThreadListener
{
    private static Logger logger = Logger.getLogger(RunningBoardUpdateThreads.class.getName());

    private JFrame parentFrame;
    private FrostIdentities identities;
    private SettingsClass frostSettings;

    // listeners are notified of each finished thread
    Hashtable threadListenersForBoard = null; // contains all listeners registered for 1 board
    Vector threadListenersForAllBoards = null; // contains all listeners for all boards

    // contains key=board, data=vector of BoardUpdateThread's, (max. 1 of a kind (MSG_DOWNLOAD_TODAY,...)
    Hashtable runningDownloadThreads = null;
    // contains key=board, data=vector of BoardUpdateThread's (multiple of kind MSG_UPLOAD)
    Hashtable runningUploadThreads = null;

    private MessageHashes messageHashes;

    /**
     * @param parentFrame
     * @param identities
     * @param frostSettings
     */
    public RunningBoardUpdateThreads(JFrame parentFrame, FrostIdentities identities, SettingsClass frostSettings) {
        this.parentFrame = parentFrame;
        this.identities = identities;
        this.frostSettings = frostSettings;

        threadListenersForBoard = new Hashtable();
        threadListenersForAllBoards = new Vector();

        runningDownloadThreads = new Hashtable();
        runningUploadThreads = new Hashtable();
    }

    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     * before starting a thread you should check if it is'nt updating already.
     * @param board
     * @param config
     * @param listener
     * @return
     */
    public boolean startMessageDownloadToday(
        Board board,
        SettingsClass config,
        BoardUpdateThreadListener listener) {

        MessageDownloadThread tofd =
            new MessageDownloadThread(
                true,
                board,
                config.getIntValue("tofDownloadHtl"),
                config.getValue("keypool.dir"),
                config.getValue("maxMessageDownload"),
                identities);
        tofd.setMessageHashes(messageHashes);

        // register listener and this class as listener
        tofd.addBoardUpdateThreadListener(this);
        if (listener != null) {
            tofd.addBoardUpdateThreadListener(listener);
        }
        // store thread in threads list
        getVectorFromHashtable(runningDownloadThreads, board).add(tofd);

        // start thread
        tofd.start();
        return true;
    }
    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     * before starting a thread you should check if it is'nt updating already.
     * @param board
     * @param config
     * @param listener
     * @return
     */
    public boolean startMessageDownloadBack(
        Board board,
        SettingsClass config,
        BoardUpdateThreadListener listener) {

        MessageDownloadThread backload =
            new MessageDownloadThread(
                false,
                board,
                config.getIntValue("tofDownloadHtl"),
                config.getValue("keypool.dir"),
                config.getValue("maxMessageDownload"),
                identities);
        backload.setMessageHashes(messageHashes);

        // register listener and this class as listener
        backload.addBoardUpdateThreadListener(this);
        if (listener != null) {
            backload.addBoardUpdateThreadListener(listener);
        }
        // store thread in threads list
        getVectorFromHashtable(runningDownloadThreads, board).add(backload);

        // start thread
        backload.start();

        return true;
    }
    /**
     * If you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     * before starting a thread you should check if it is'nt updating already.
     * @param board
     * @param config
     * @param listener
     * @return
     */
    public boolean startBoardFilesUpload(Board board, SettingsClass config,
                                           BoardUpdateThreadListener listener)
    {
        // board file uploads are done inside the UpdateIdThread that is started in startBoardFilesDownload()

        /*GetRequestsThread grt = new GetRequestsThread(
           board,
           config.getIntValue("tofDownloadHtl"),
           config.getValue("keypool.dir"),
           frame1.getInstance().getUploadTable()
         );

        // register listener and this class as listener
        grt.addBoardUpdateThreadListener( this );
        if( listener != null )
        {
            grt.addBoardUpdateThreadListener( listener );
        }

        // store thread in threads list
        getVectorFromHashtable( runningDownloadThreads, board ).add(grt);

        // start thread
        grt.start();*/

        return true;
    }

    /**
     * Starts downloads of files to boards.  Same as above.
     * @param board
     * @param config
     * @param listener
     * @return
     */
    public boolean startBoardFilesDownload(Board board, SettingsClass config,
                                           BoardUpdateThreadListener listener)
    {
//      final int downloadBack = MainFrame.frostSettings.getIntValue("maxMessageDownload");
//
//      final UpdateIdThread [] threads = new UpdateIdThread[downloadBack];
//
//      for (int i=0; i < downloadBack; i++) {
//            boolean doUpload;
//            if( i == 0 ) {
//                doUpload = true; // upload own keys today only
//            } else {
//                doUpload = false;
//            }
//          UpdateIdThread thread = new UpdateIdThread(board,DateFun.getDate(i), identities, doUpload);
//          thread.setMessageHashes(messageHashes);
//
//
//          threads[i] = thread;
//      }

        // NOTE: since we now do deep requests, it takes a lot of time for
        //   all these threads to finish.  So I'll notify the gui that the message download thread is done updating
        //   after the indices for the current date are finished requesting and leave the
        //   threads that are downloading the previous day's indices running.
        // IMPORTANT: I think its ok to start new update set on a board even if not all
        //   threads have finished - the node's failure table will take care of it.

        // -->> the above logic leaded into 'OutOfMemory: Java heap space' for some users.
        //      I think the reason is that they have very large file lists whose upload
        //      takes ages, and a huge amount of the threads started here (invisible)
        //      do finally running concurrently and fill up the Java heap.

//      UpdateIdThread uit = threads[0];
//        uit.addBoardUpdateThreadListener( this );
//        if( listener != null ) {
//            uit.addBoardUpdateThreadListener( listener );
//        }
//      getVectorFromHashtable( runningDownloadThreads, board).add(uit);
//
//      //now start the threads one after another
//        Thread starter = new Thread() {
//          public void run() {
//              for (int j=0; j < downloadBack; j++) {
//                  //getVectorFromHashtable( runningDownloadThreads, board).add(threads[j]); //add to vector
//                  threads[j].start();
//                  try {
//                      threads[j].join();
//                      //if we get interrupted, continue with next thread
//                      //or perhaps we want to stop all of them?
//                  } catch (InterruptedException e) {
//                      logger.log(Level.SEVERE, "Exception thrown in startBoardFilesDownload(...)", e);
//                  }
//              }
//          }
//      };
//      starter.start();

        BoardFilesDownloadStarter starter = new BoardFilesDownloadStarter(board, identities);

        starter.addBoardUpdateThreadListener( this );
        if( listener != null ) {
            starter.addBoardUpdateThreadListener( listener );
        }
        getVectorFromHashtable( runningDownloadThreads, board).add(starter);

        starter.start();

        return true;
    }

    private class BoardFilesDownloadStarter extends BoardUpdateThreadObject implements BoardUpdateThread {
        Board board;
        FrostIdentities newIdentities;
        public BoardFilesDownloadStarter(Board board, FrostIdentities newIdentities) {
            super(board, newIdentities);
            this.newIdentities = newIdentities;
            this.board = board;
        }
        public int getThreadType() { return BoardUpdateThread.BOARD_FILE_DNLOAD; }
        public void run() {
            notifyThreadStarted(this);

            // +1 for today
            final int downloadBack = 1 + MainFrame.frostSettings.getIntValue("maxMessageDownload");
            // if one gets an exception, do not try backwards further
            try {
                for (int i=0; i < downloadBack; i++) {
                    boolean isForToday;
                    if( i == 0 ) {
                        isForToday = true; // upload own keys today only
                    } else {
                        isForToday = false;
                    }
                    UpdateIdThread thread = new UpdateIdThread(board,DateFun.getDate(i), newIdentities, isForToday);
                    thread.setMessageHashes(messageHashes);

                    // directly call run, we want to block
                    thread.run();
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception in BoardFilesDownloadStarter for() loop", e);
            }
            notifyThreadFinished(this);
        }
    }

    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     * @param board
     * @param mo
     * @param listener
     * @return
     */
    public boolean startMessageUpload(
        Board board,
        MessageObject mo,
        BoardUpdateThreadListener listener,
        Identity recipient) {

        MessageUploadThread msgUploadThread =
            new MessageUploadThread(board, mo, identities, frostSettings, recipient);
        msgUploadThread.setParentFrame(parentFrame);
        // register listener and this class as listener
        msgUploadThread.addBoardUpdateThreadListener(this);
        if (listener != null) {
            msgUploadThread.addBoardUpdateThreadListener(listener);
        }

        // store thread in threads list
        getVectorFromHashtable(runningUploadThreads, board).add(msgUploadThread);

        // start thread
        msgUploadThread.start();

        return true;
    }

    /**
     * Gets an Vector from a Hashtable with given key. If key is not contained
     * in Hashtable, an empty Vector will be created and put in the Hashtable.
     * @param t
     * @param key
     * @return
     */
    private Vector getVectorFromHashtable(Hashtable t, Board key)
    {
        Vector retval = null;
        synchronized( t )
        {
            retval = (Vector)t.get(key.getName());
            if( retval == null )
            {
                retval = new Vector();
                t.put(key.getName(), retval);
            }
        }
        return retval;
    }

    /**
     * Returns the list of current download threads for a given board.
     * Returns an empty list of no thread is running.
     * @param board
     * @return
     */
    public Vector getDownloadThreadsForBoard(Board board)
    {
        return getVectorFromHashtable( runningDownloadThreads, board );
    }

    /**
     * Returns the list of current upload threads for a given board.
     * Returns an empty list of no thread is running.
     * @param board
     * @return
     */
    public Vector getUploadThreadsForBoard(Board board)
    {
        return getVectorFromHashtable( runningUploadThreads, board );
    }


    /**
     * Adds a listener that gets notified if any thread for a given board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * @param board
     * @param listener
     */
    public void addBoardUpdateThreadListener(Board board, BoardUpdateThreadListener listener)
    {
        getVectorFromHashtable(threadListenersForBoard, board).remove(listener); // no doubles allowed
        getVectorFromHashtable(threadListenersForBoard, board).add(listener);
    }

    /**
     * Adds a listener that gets notified if any thread for any board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * @param listener
     */
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        threadListenersForAllBoards.remove( listener ); // no doubles allowed
        threadListenersForAllBoards.add( listener );
    }
    /**
     * Removes a listener that gets notified if any thread for a given board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * Method will do nothing if listener is null or not contained in the list of listeners.
     * @param board
     * @param listener
     */
    public void removeBoardUpdateThreadListener(Board board, BoardUpdateThreadListener listener)
    {
        getVectorFromHashtable(threadListenersForBoard, board).remove(listener);
    }

    /**
     * Removes a listener that gets notified if any thread for any board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * Method will do nothing if listener is null or not contained in the list of listeners.
     * @param listener
     */
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        threadListenersForAllBoards.remove( listener );
    }

    /**
     * Implementing the listener for thread finished.
     * Notifies all interested listeners for change of the thread state.
     * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadFinished(frost.threads.BoardUpdateThread)
     */
    public void boardUpdateThreadFinished(BoardUpdateThread thread)
    {
        // remove from thread list
        Vector threads;
        if( thread.getThreadType() == BoardUpdateThread.MSG_UPLOAD )
        {
            threads = getVectorFromHashtable(runningUploadThreads,thread.getTargetBoard());
        }
        else // all other
        {
            threads = getVectorFromHashtable(runningDownloadThreads,thread.getTargetBoard());
        }

        if( threads != null )
        {
            threads.remove(thread);
        }

        synchronized( threadListenersForAllBoards )
        {
            // notify listeners
            Iterator i = threadListenersForAllBoards.iterator();
            while( i.hasNext() )
            {
                ((BoardUpdateThreadListener)i.next()).boardUpdateThreadFinished(thread);
            }
            i = getVectorFromHashtable(threadListenersForBoard, thread.getTargetBoard()).iterator();
            while( i.hasNext() )
            {
                ((BoardUpdateThreadListener)i.next()).boardUpdateThreadFinished(thread);
            }
        }
    }

    /**
     * Implementing the listener for thread started.
     * Notifies all interested listeners for change of the thread state.
     * @see frost.threads.BoardUpdateThreadListener#boardUpdateThreadStarted(frost.threads.BoardUpdateThread)
     */
    public void boardUpdateThreadStarted(BoardUpdateThread thread)
    {
        synchronized( threadListenersForAllBoards )
        {
            Iterator i = threadListenersForAllBoards.iterator();
            while( i.hasNext() )
            {
                ((BoardUpdateThreadListener)i.next()).boardUpdateThreadStarted(thread);
            }
            i = getVectorFromHashtable(threadListenersForBoard, thread.getTargetBoard()).iterator();
            while( i.hasNext() )
            {
                ((BoardUpdateThreadListener)i.next()).boardUpdateThreadStarted(thread);
            }
        }
    }


    /**
     * Returns the count of ALL running download threads (of all boards).
     * @return
     */
    public int getRunningDownloadThreadCount() // msg_today, msg_back, files_update, update_id
    {
        int downloadingThreads = 0;

        synchronized(runningDownloadThreads)
        {
            Iterator i = runningDownloadThreads.values().iterator();
            while( i.hasNext() )
            {
                Object o = i.next();
                if( o instanceof Vector )
                {
                    Vector v = (Vector)o;
                    if( v.size() > 0 )
                    {
                        downloadingThreads+=v.size();
                    }
                }
            }
        }
        return downloadingThreads;
    }
    /**
     * Returns the count of ALL running upload threads (of all boards).
     * @return
     */
    public int getRunningUploadThreadCount() // msg upload
    {
        int uploadingThreads = 0;

        synchronized(runningDownloadThreads)
        {
            Iterator i = runningUploadThreads.values().iterator();
            while( i.hasNext() )
            {
                Object o = i.next();
                if( o instanceof Vector )
                {
                    Vector v = (Vector)o;
                    if( v.size() > 0 )
                    {
                        uploadingThreads+=v.size();
                    }
                }
            }
        }
        return uploadingThreads;
    }

    /**
     * Returns the count of boards that currently have running download threads.
     * @return
     */
    public int getUpdatingBoardCount()
    {
        int updatingBoards = 0;

        synchronized(runningDownloadThreads)
        {
            Iterator i = runningDownloadThreads.values().iterator();
            while( i.hasNext() )
            {
                Object o = i.next();
                if( o instanceof Vector )
                {
                    Vector v = (Vector)o;
                    if( v.size() > 0 )
                    {
                        updatingBoards++;
                    }
                }
            }
        }
        return updatingBoards;
    }
    /**
     * Returns the count of boards that currently have running upload threads.
     * @return
     */
    public int getUploadingBoardCount()
    {
        int uploadingBoards = 0;

        synchronized(runningDownloadThreads)
        {
            Iterator i = runningUploadThreads.values().iterator();
            while( i.hasNext() )
            {
                Object o = i.next();
                if( o instanceof Vector )
                {
                    Vector v = (Vector)o;
                    if( v.size() > 0 )
                    {
                        uploadingBoards++;
                    }
                }
            }
        }
        return uploadingBoards;
    }

    /**
     * Returns true if the given board have running download threads.
     * @param board
     * @return
     */
    public boolean isUpdating(Board board)
    {
        if( getVectorFromHashtable(runningDownloadThreads, board).size() > 0 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * Returns true if the given board have running upload threads.
     * @param board
     * @return
     */
    public boolean isUploading(Board board)
    {
        if( getVectorFromHashtable(runningUploadThreads, board).size() > 0 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @param board
     * @param type
     * @return
     */
    public boolean isThreadOfTypeRunning(Board board, int type)
    {
        Vector threads = getDownloadThreadsForBoard(board);
        for( int x=0; x<threads.size(); x++ )
        {
            BoardUpdateThread thread = (BoardUpdateThread)threads.get(x);
            if( thread.getThreadType() == type )
                return true;
        }
        return false;
    }

    /**
     * @param messageHashes
     */
    public void setMessageHashes(MessageHashes messageHashes) {
        this.messageHashes = messageHashes;
    }

/*
maybe useable if the counters are set on thread start/end
    int updatingBoards = 0;
    int uploadingBoards = 0;
    int downloadThreads = 0;
    int uploadThreads = 0;

    protected synchronized  int getUpdatingBoards() { return updatingBoards; }
    protected synchronized  void incUpdatingBoards() { updatingBoards++; }
    protected synchronized  void decUpdatingBoards() { updatingBoards--; }

    protected synchronized  int getUploadingBoards() { return uploadingBoards; }
    protected synchronized  void incUploadingBoards() { uploadingBoards++; }
    protected synchronized  void decUploadingBoards() { uploadingBoards--; }

    protected synchronized  int getDownloadThreads() { return downloadThreads; }
    protected synchronized  void incDownloadThreads() { downloadThreads++; }
    protected synchronized  void decDownloadThreads() { downloadThreads--; }

    protected synchronized  int getUploadThreads() { return uploadThreads; }
    protected synchronized  void incUploadThreads() { uploadThreads++; }
    protected synchronized  void decUploadThreads() { uploadThreads--; }
*/

}
