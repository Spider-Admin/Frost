package frost.threads;

import java.util.*;
import java.awt.*;

import frost.*;
import frost.gui.objects.*;

public class RunningBoardUpdateThreads implements BoardUpdateThreadListener
{
    // listeners are notified of each finished thread
    Hashtable threadListenersForBoard = null; // contains all listeners registered for 1 board
    Vector threadListenersForAllBoards = null; // contains all listeners for all boards

    // contains key=board, data=vector of BoardUpdateThread's, (max. 1 of a kind (MSG_DOWNLOAD_TODAY,...)
    Hashtable runningDownloadThreads = null;
    // contains key=board, data=vector of BoardUpdateThread's (multiple of kind MSG_UPLOAD)
    Hashtable runningUploadThreads = null;

    public RunningBoardUpdateThreads()
    {
        threadListenersForBoard = new Hashtable();
        threadListenersForAllBoards = new Vector();

        runningDownloadThreads = new Hashtable();
        runningUploadThreads = new Hashtable();
    }

    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     * before starting a thread you should check if it is'nt updating already.
     */
    public boolean startMessageDownloadToday(FrostBoardObject board, SettingsClass config,
                                             BoardUpdateThreadListener listener)
    {
        MessageDownloadThread tofd = new MessageDownloadThread(
            true,
            board,
            config.getValue("tofDownloadHtl"),
            config.getValue("keypool.dir"),
            config.getValue("maxMessageDownload") );

        // register listener and this class as listener
        tofd.addBoardUpdateThreadListener( this );
        if( listener != null )
        {
            tofd.addBoardUpdateThreadListener( listener );
        }
        // store thread in threads list
        getVectorFromHashtable( runningDownloadThreads, board ).add(tofd);

        // start thread
        tofd.start();
        return true;
    }
    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
    * before starting a thread you should check if it is'nt updating already.
     */
    public boolean startMessageDownloadBack(FrostBoardObject board, SettingsClass config,
                                            BoardUpdateThreadListener listener)
    {
        MessageDownloadThread backload = new MessageDownloadThread(
            false,
            board,
            config.getValue("tofDownloadHtl"),
            config.getValue("keypool.dir"),
            config.getValue("maxMessageDownload") );

        // register listener and this class as listener
        backload.addBoardUpdateThreadListener( this );
        if( listener != null )
        {
            backload.addBoardUpdateThreadListener( listener );
        }
        // store thread in threads list
        getVectorFromHashtable( runningDownloadThreads, board ).add(backload);

        // start thread
        backload.start();

        return true;
    }
    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
    * before starting a thread you should check if it is'nt updating already.
     */
    public boolean startBoardFilesDownload(FrostBoardObject board, SettingsClass config,
                                           BoardUpdateThreadListener listener)
    {
        GetRequestsThread grt = new GetRequestsThread(
           board,
           config.getValue("tofDownloadHtl"),
           config.getValue("keypool.dir"),
           frame1.getInstance().getUploadTable()
         );
	UpdateIdThread uit = new UpdateIdThread(board);
	uit.addBoardUpdateThreadListener( this );
        // register listener and this class as listener
        grt.addBoardUpdateThreadListener( this );
        if( listener != null )
        {
            grt.addBoardUpdateThreadListener( listener );
	    uit.addBoardUpdateThreadListener( listener );
        }

        // store thread in threads list
        getVectorFromHashtable( runningDownloadThreads, board ).add(grt);
	getVectorFromHashtable( runningDownloadThreads, board ).add(uit);
        // start thread
        grt.start();
	uit.start();

        return true;
    }

    /**
     * if you specify a listener and the method returns true (thread is started), the listener
     * will be notified if THIS thread is finished
     */
    public boolean startMessageUpload(FrostBoardObject board,
                                      String from,
                                      String subject,
                                      String content,
                                      String date,
                                      String time,
                                      String recipient,
                                      SettingsClass config,
                                      Frame frameToLock,
                                      BoardUpdateThreadListener listener)
    {
        MessageUploadThread msgUploadThread = new MessageUploadThread(
            board,
            from,
            subject,
            content,
            config.getValue("tofUploadHtl"),
            config.getValue("keypool.dir"),
            config.getValue("tofDownloadHtl"),
            date,
            time,
            recipient,
            frameToLock);
        // register listener and this class as listener
        msgUploadThread.addBoardUpdateThreadListener( this );
        if( listener != null )
        {
            msgUploadThread.addBoardUpdateThreadListener( listener );
        }

        // store thread in threads list
        getVectorFromHashtable( runningUploadThreads, board ).add(msgUploadThread);

        // start thread
        msgUploadThread.start();

        return true;
    }

    /**
     * Gets an Vector from a Hashtable with given key. If key is not contained
     * in Hashtable, an empty Vector will be created and put in the Hashtable.
     */
    protected Vector getVectorFromHashtable(Hashtable t, Object key)
    {
        Vector retval;
        retval = (Vector)t.get(key.toString());
        if( retval == null )
        {
            retval = new Vector();
            t.put(key.toString(), retval);
        }
        return retval;
    }

    /**
     * Returns the list of current download threads for a given board.
     * Returns an empty list of no thread is running.
     */
    public Vector getDownloadThreadsForBoard(FrostBoardObject board)
    {
        return getVectorFromHashtable( runningDownloadThreads, board );
    }

    /**
     * Returns the list of current upload threads for a given board.
     * Returns an empty list of no thread is running.
     */
    public Vector getUploadThreadsForBoard(FrostBoardObject board)
    {
        return getVectorFromHashtable( runningUploadThreads, board );
    }


    /**
     * Adds a listener that gets notified if any thread for a given board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     **/
    public void addBoardUpdateThreadListener(FrostBoardObject board, BoardUpdateThreadListener listener)
    {
        getVectorFromHashtable(threadListenersForBoard, board).remove(listener); // no doubles allowed
        getVectorFromHashtable(threadListenersForBoard, board).add(listener);
    }

    /**
     * Adds a listener that gets notified if any thread for any board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     **/
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        threadListenersForAllBoards.remove( listener ); // no doubles allowed
        threadListenersForAllBoards.add( listener );
    }
    /**
     * Removes a listener that gets notified if any thread for a given board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * Method will do nothing if listener is null or not contained in the list of listeners.
     **/
    public void removeBoardUpdateThreadListener(FrostBoardObject board, BoardUpdateThreadListener listener)
    {
        getVectorFromHashtable(threadListenersForBoard, board).remove(listener);
    }

    /**
     * Removes a listener that gets notified if any thread for any board did update its state.
     * For supported states see BoardUpdateThreadListener methods.
     * Method will do nothing if listener is null or not contained in the list of listeners.
     **/
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        threadListenersForAllBoards.remove( listener );
    }

    /**
     * Implementing the listener for thread finished.
     * Notifies all interested listeners for change of the thread state.
     */
    public void boardUpdateThreadFinished(BoardUpdateThread thread)
    {
        //remove from thread list
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

    /**
     * Implementing the listener for thread started.
     * Notifies all interested listeners for change of the thread state.
     */
    public void boardUpdateThreadStarted(BoardUpdateThread thread)
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


    /**
     * Returns the count of ALL running download threads (of all boards).
     */
    public int getRunningDownloadThreadCount() // msg_today, msg_back, files_update
    {
        int downloadingThreads = 0;

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
        return downloadingThreads;
    }
    /**
     * Returns the count of ALL running upload threads (of all boards).
     */
    public int getRunningUploadThreadCount() // msg upload
    {
        int uploadingThreads = 0;

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
        return uploadingThreads;
    }

    /**
     * Returns the count of boards that currently have running download threads.
     */
    public int getUpdatingBoardCount()
    {
        int updatingBoards = 0;

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
        return updatingBoards;
    }
    /**
     * Returns the count of boards that currently have running upload threads.
     */
    public int getUploadingBoardCount()
    {
        int uploadingBoards = 0;

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
        return uploadingBoards;
    }

    /**
     * Returns true if the given board have running download threads.
     */
    public boolean isUpdating(FrostBoardObject board)
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
     */
    public boolean isUploading(FrostBoardObject board)
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
/*
maybe usaable if the counters are set on thread start/end
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
