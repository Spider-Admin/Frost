package fillament.util;


public class WorkThread extends Thread {

	WorkQueue queue;
        int sleepTime;
	boolean keepGoing = true;
	String title;

	public WorkThread(WorkQueue Queue) {
            this(Queue,"Working Thread");
        }

        public WorkThread(WorkQueue Queue, String title) {
            this(Queue, title,1000);
        }

        public WorkThread(WorkQueue Queue, String title, int SleepTime) {
		super(title);
		queue = Queue;
		this.sleepTime=SleepTime;
		this.title=title;
	}

	public void finish() {
		keepGoing = false;
	}

	public void run() {
		System.out.println("\nworker thread " + title + " started");
			while(keepGoing) {
				if (queue.hasMore())
					doWork(queue.next());
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					handleInterruptedException(e);
				}
			}
		System.out.println("\nworker thread " + title + " finished");

	}

        public void doWork(Object o) {
		System.out.println("error -overloading failed");
        }

        public void setSleep(int Sleep) {
            sleepTime = Sleep;
        }
        
        public int getSleep() {
            return sleepTime;
        }
        
        public void handleInterruptedException(InterruptedException e) {
            System.err.println("InterruptedException in InfoThread.run " + e.getMessage());
	    finish();
        }

}
