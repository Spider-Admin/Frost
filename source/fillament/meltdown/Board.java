package fillament.meltdown;

import java.io.*;
import java.util.*;

public class Board implements Serializable {
	
	Hashtable admins;
	String board = "";
	String key = "";
	String description = "";
	GregorianCalendar added;

	public Board() {
		admins = new Hashtable();
		added = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	public Hashtable getAdmins() {
		return admins;
	}

	public void setBoard(String Board) {
		board = Board;
	}

	public String getBoard() {
		return board;
	}

	public void setKey(String Key) {
		key = Key;
	}

	public void setDescription(String Desc) {
		description = Desc;
	}

	public BoardAdvertisement getNewBoardAd() {
		BoardAdvertisement ba = new BoardAdvertisement();
		ba.setBoard(board);
		ba.setStatus(BoardAdvertisement.NEW);
		ba.setKey(key);
		ba.setDescription(description);
		return ba;
	}
        
        public BoardAdvertisement getDailyBoardAd() {
            BoardAdvertisement ba = new BoardAdvertisement();
            ba.setBoard(board);
            ba.setStatus(BoardAdvertisement.DAILY);
            ba.setKey(key);
            ba.setDescription(description);
            return ba;
        }
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		admins = (Hashtable) in.readObject();
		board = (String) in.readObject();
		key = (String) in.readObject();
		description = (String) in.readObject();
		added = (GregorianCalendar) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(admins);
		out.writeObject(board);
		out.writeObject(key);
		out.writeObject(description);
		out.writeObject(added);
	}
}
