package frost.threads;

import java.util.*;

import frost.gui.objects.FrostDownloadItemObject;
import frost.*;
import java.io.File;

/**
 * 
 * @author zlatinb
 *
 * This thread does very simple email notifications for completed files.
 * I deliberately do not want to make it too powerful of customizable.
 */
public class NotifyByEmailThread extends Thread implements Observer{
	
	private static String template; //TODO: load message body here
	private static final String nameTag = "<filename>"; 
	private static final String subject = "[Frost] Download finished";
	
	private static String SMTPServer, SMTPuser, SMTPpass;
	
	private LinkedList queue;
	
	public NotifyByEmailThread() {
		queue = new LinkedList();
		setDaemon(true);
		
		File body = new File("email");
		if (body.exists() && body.isFile() && body.length() >0 && body.length() < 64*1024)
			template = FileAccess.read(body.getPath());
		else {
			template = "Hello\n, this is the automatic Frost email notifier. "+
							"The download of file <filename> has completed.\n\n";
			FileAccess.writeFile(template,body);
		}
		
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
	
	/**
	 * @return
	 */
	public static String getTemplate() {
		return template;
	}

	/**
	 * @param string
	 */
	public static void setSMTPpass(String string) {
		SMTPpass = string;
	}

	/**
	 * @param string
	 */
	public static void setSMTPServer(String string) {
		SMTPServer = string;
	}

	/**
	 * @param string
	 */
	public static void setSMTPuser(String string) {
		SMTPuser = string;
	}

	/**
	 * @param string
	 */
	public static void setTemplate(String string) {
		template = string;
	}

}