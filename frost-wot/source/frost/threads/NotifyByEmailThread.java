package frost.threads;

import java.util.*;

import frost.gui.objects.FrostDownloadItemObject;
import frost.Core;


public class NotifyByEmailThread extends Thread implements Observer{
	
	private static String template; //TODO: load message body here
	private static final String nameTag = "<filename>"; 
	
	private LinkedList queue;
	
	public NotifyByEmailThread() {
		queue = new LinkedList();
		setDaemon(true);
	}

	public void run() {
		
		String currentName;
		running: while(true) {
		
			synchronized(queue) {
				 if (queue.size()==0)
					try {
						queue.wait();
						continue;
					}catch(InterruptedException e){break running;}
				else 
					currentName = (String)queue.removeFirst();
			}
			Core.getOut().println("notifying for received file "+currentName);
			
			String messageBody = template.replaceAll(nameTag,currentName);
			
			throw new UnsupportedOperationException("not implemented yet. Come back later");
		}
	}

	public void update(Observable o,Object arg) {
		
		assert o instanceof FrostDownloadItemObject &&
			arg instanceof String : "incorrect parameters at email notifier";
		
		synchronized(queue){
			queue.add(arg);
			queue.notifyAll();
		}
			
	}
}