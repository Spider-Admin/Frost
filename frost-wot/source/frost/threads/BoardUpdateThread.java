package frost.threads;

public interface BoardUpdateThread
{
    // thread types
    public final static int MSG_DNLOAD_TODAY  = 1;
    public final static int MSG_DNLOAD_BACK   = 2;
    public final static int BOARD_FILE_DNLOAD = 3;
    public final static int MSG_UPLOAD        = 4;

    int getThreadType();

    // FrostBoard getTargetBoard()
    public String getTargetBoard();

    public long getStartTimeMillis();

    // methods from Thread class needed by gui
    public void interrupt();
    public boolean isInterrupted();
    // not using isAlive() here, because Thread will call listener before it dies
    // so using own implementation of finished
    public boolean isFinished();

    // allow to register listener, should only be used by RunningBoardUpdateThreads class
    // other classes should register to the RunningBoardUpdateThreads class
    // the difference is: if the thread class the listener directly, the ArrayList = null
    // the underlying Thread class should fire the finished event with parameters:
    //  boardUpdateThreadFinished(null, this)
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener);
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener);
}