package frost.threads;

import java.io.File;
import java.util.*;
import java.util.logging.*;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.smtp.*;

import frost.*;
import frost.gui.objects.FrostDownloadItemObject;

/**
 * @author zlatinb
 * 
 * This thread does very simple email notifications for completed files. I
 * deliberately do not want to make it too powerful of customizable.
 */
public class NotifyByEmailThread extends Thread implements Observer {

	private static String template; 
	private static final String nameTag = "<filename>";
	private static final String subject = "[Frost] Download finished";
	
	private static Logger logger = Logger.getLogger(NotifyByEmailThread.class.getName());

	private static String SMTPServer, SMTPuser, SMTPpass, notifyAddress;

	private LinkedList queue;

	private SMTPMessage message;
	private InternetAddress address;
	private Session session;
	private Properties sessionProperties;
	private SMTPTransport transport;

	public NotifyByEmailThread() throws Error {
		queue = new LinkedList();
		setDaemon(true);

		//init the body of the message
		File body = new File("email");
		if (body.exists()
			&& body.isFile()
			&& body.length() > 0
			&& body.length() < 64 * 1024)
			template = FileAccess.read(body.getPath());
		else {
			template =
				"Hello\n, this is the automatic Frost email notifier. "
					+ "The download of file <filename> has completed.\n\n";
			FileAccess.writeFile(template, body);
		}

		//get the settings
		SMTPServer = Core.frostSettings.getValue("SMTP.server");
		SMTPuser = Core.frostSettings.getValue("SMTP.user");
		SMTPpass = Core.frostSettings.getValue("SMTP.pass");
		notifyAddress = Core.frostSettings.getValue("notifyAddress");

		if (SMTPServer == null
			|| SMTPuser == null
			|| SMTPpass == null
			|| notifyAddress == null
			|| SMTPServer.length() == 0
			|| SMTPuser.length() == 0
			|| SMTPpass.length() == 0
			|| notifyAddress.length() == 0)
			throw new Error("invalid SMTP parameters");
		//init the session properties
		sessionProperties = new Properties();
		sessionProperties.setProperty("mail.host", SMTPServer);
		sessionProperties.setProperty("mail.user", SMTPpass);
		session = Session.getInstance(sessionProperties);
		transport = new SMTPTransport(session, new URLName("frostNotifier"));

		try {
			address = new InternetAddress(notifyAddress);
			address.validate();
		} catch (AddressException e) {
			throw new Error("invalid email address");
		}

	}

	public void run() {

		String currentName;
		running : while (true) {

			synchronized (queue) {
				if (queue.size() == 0)
					try {
						queue.wait();
						continue;
					} catch (InterruptedException e) {
						break running;
					} else
					currentName = (String) queue.removeFirst();
			}
			logger.info("notifying for received file " + currentName);

			String messageBody = template.replaceAll(nameTag, currentName);

			//check if we're connected, and if not connect

			if (!transport.isConnected())
				try {
					transport.connect(SMTPServer, SMTPuser, SMTPpass);
				} catch (MessagingException e) {
					logger.log(Level.SEVERE, "couldn't connect to smtp server", e);
					//TODO:decide what to do. I think just wait 5 mins and try
					// again?
					mixed.wait(5 * 60 * 1000);
					queue.addFirst(currentName);
					continue running;
				}

			//compose the message
			message = new SMTPMessage(session);
			message.setNotifyOptions(SMTPMessage.NOTIFY_NEVER);
			message.setReturnOption(SMTPMessage.NOTIFY_NEVER);
			try {
				message.setEnvelopeFrom("Frost");
				message.setSubject(subject);
				message.setText(messageBody);

				//now send the message
				transport.sendMessage(message, new Address[] { address });
				logger.info("message sent successfully");

				//check if there are more messages to send, and if not
				// disconnect
				if (queue.size() == 0)
					transport.close();
			} catch (SendFailedException e) {
				logger.log(Level.SEVERE, "Exception thrown in run()", e);
				//wait 5 mins, retry
				mixed.wait(5 * 60 * 1000);
				queue.addFirst(currentName);
				continue running;
			} catch (MessagingException e) {
				//we screwed up somehow..
				logger.log(Level.SEVERE, "Exception thrown in run()", e);
			}

		}
	}

	public void update(Observable o, Object arg) {

		assert o instanceof FrostDownloadItemObject
			&& arg instanceof String : "incorrect parameters at email notifier";

		synchronized (queue) {
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