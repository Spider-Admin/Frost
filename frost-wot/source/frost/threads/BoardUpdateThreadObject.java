package frost.threads;

import java.util.*;

import frost.gui.objects.FrostBoardObject;

/**
 * This class implements most methods needed by the BoardUpdateThread interface.
 * A thread have to extend this class and to implement BoardUpdateThread.
 */
public class BoardUpdateThreadObject extends Thread
{
    FrostBoardObject targetBoard = null;
    long startTimeMillis = -1;
    boolean isFinished = false;
    Vector registeredListeners = null;

    public BoardUpdateThreadObject(FrostBoardObject board)
    {
        this.targetBoard = board;
        this.registeredListeners = new Vector();
    }

    // FrostBoard getTargetBoard()
    public FrostBoardObject getTargetBoard()
    {
        return targetBoard;
    }

    /**
     * Returns -1 if not yet started
    */
    public long getStartTimeMillis()
    {
        return startTimeMillis;
    }

    // not using isAlive() here, because Thread will call listener before it dies
    // so using own implementation of finished
    public synchronized boolean isFinished()
    {
        return isFinished;
    }
    protected synchronized void threadFinished()
    {
        isFinished = true;
    }

    /**
     * Called from Thread to notify all listeners that thread is started now
     */
    protected void notifyThreadStarted(BoardUpdateThread thread)
    {
        this.startTimeMillis = System.currentTimeMillis();
        // notify listeners
        Iterator i = registeredListeners.iterator();
        while( i.hasNext() )
        {
            ((BoardUpdateThreadListener)i.next()).boardUpdateThreadStarted(thread);
        }
    }
    /**
     * Called from Thread to notify all listeners that thread is started now
     */
    protected void notifyThreadFinished(BoardUpdateThread thread)
    {
        threadFinished();
        // notify listeners
        Iterator i = registeredListeners.iterator();
        while( i.hasNext() )
        {
            ((BoardUpdateThreadListener)i.next()).boardUpdateThreadFinished(thread);
        }
    }

    // allow to register listener, should only be used by RunningBoardUpdateThreads class
    // other classes should register to the RunningBoardUpdateThreads class
    // the difference is: if the thread class the listener directly, the ArrayList = null
    // the underlying Thread class should fire the finished event with parameters:
    //  boardUpdateThreadFinished(null, this)
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        registeredListeners.add( listener );
    }
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener)
    {
        registeredListeners.remove( listener );
    }

}