package fillament.meltdown;

import java.util.*;

public class Message {
	
	String board = "";
	String from = "";
	String subject = "";
	String body = "";
	GregorianCalendar date;

	public Message() {
		date = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	public void setBoard(String B) {
		board = B;
	}

	public String getBoard() {
		return board;
	}

	public void setFrom(String F) {
		from = F;
	}

	public String getFrom() {
		return from;
	}

	public void setSubject(String S) {
		subject = S;
	}

	public String getSubject() {
		return subject;
	}

	public void setBody(String B) {
		body = B;
	}

	public String getBody() {
		return body;
	}

	public String getMessage() {
		String ls = System.getProperty("line.separator");
		StringBuffer message = new StringBuffer();
		message.append("board=" + board + ls);
		message.append("from=" + from + ls);
		message.append("subject=" + subject + ls);
		message.append("date=" + getFrostDate(date) + ls);
		message.append("time=" + getFrostTime(date) + ls);
		message.append("--- message ---" + ls);
		message.append(body + ls);
		return message.toString();
	}

	public static String getFrostDate(GregorianCalendar cal) {
                StringBuffer date = new StringBuffer();
                date.append(cal.get(Calendar.YEAR));
                date.append(".");
                date.append(cal.get(Calendar.MONTH) + 1);
                date.append(".");
                date.append(cal.get(Calendar.DAY_OF_MONTH));
                return date.toString();
        }

	public static String getFrostTime(GregorianCalendar cal) {
		StringBuffer time = new StringBuffer();
		String hour = (new Integer(cal.get(Calendar.HOUR_OF_DAY))).toString();
		if (hour.length() < 2) {
			hour = 0 + hour;
		}
		time.append(hour + ":");
		time.append(cal.get(Calendar.MINUTE) + ":" );
		time.append(cal.get(Calendar.SECOND) + "GMT");
		return time.toString();
	}
}
