package fillament.meltdown;

import java.util.*;

public class BoardAdvertisement {

	int status = 0;
	String board = "";
	String key = "";
	GregorianCalendar dateTime;
	String description = "";

	public static final int NEW = 0;
	public static final int MERGED = 1;
	public static final int ADDADMIN = 2;
	public static final int RETIRED = 3;
	public static final int REMADMIN = 4;
	public static final int DAILY = 5;

	public BoardAdvertisement() {
		dateTime = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	public void setBoard(String Board) {
		board = Board;
	}

	public String getBoard() {
		return board;
	}

	public void setStatus(int Status) {
		status = Status;
	}

	public int setStatus() {
		return status;
	}

	public void setDescription(String Desc) {
		description = Desc;
	}

	public String getDescription() {
		return description;
	}

	public void setKey(String Key) {
		key = Key;
	}

	public String getKey() {
		return key;
	}
	public Message getMessage() {
		Message msg = new Message();
		String updateMessage = "";
		String footer = "";
		StringBuffer message = new StringBuffer();
		msg.setBoard("boardlists");
		msg.setFrom("Boardwatch");
		String subPre = "";
		if (status == NEW) {
			subPre = "New Board: ";
			updateMessage = "A new board has been created: " + board;
			footer = "<board>" + board + " * " + key + " * N/A</board>\n";
		} else if(status == MERGED) {
			subPre = "Board Merged: ";
			updateMessage = "The board " + board + " has been merged with";
		} else if(status == ADDADMIN) {
			subPre = "Admin Added: ";
			updateMessage = "A new admin has been allowed on board " + board + ":";
		} else if(status == RETIRED) {
			subPre = "Board Retired: ";
			updateMessage = "The following board has been retired: ";
		} else if(status == REMADMIN) {
			subPre = "Admin Retired: ";
			updateMessage = "The board " + board + " has retired an admin:";
		} else if(status == DAILY) {
			subPre = "Daily Listing: ";
			footer = "<board>" + board + " * " + key + " * N/A</board>";
		}
		
		msg.setSubject(subPre + board);
		message.append(updateMessage + "\n\n" + description + "\n\n" + footer);
		msg.setBody(message.toString());
		return msg;
		
	}
}
