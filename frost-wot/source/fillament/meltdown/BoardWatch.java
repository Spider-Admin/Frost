package fillament.meltdown;

import java.io.*;
import java.util.*;

public class BoardWatch {


    static String database = "boards.db";
    static boolean addMode = false;
    static boolean dailyMode = true;
    static Hashtable boards;
    static String pSep = System.getProperty("file.separator");
    static String meltdownPath = "fillament"+pSep+"meltdown";

    public static void main(String[] args) {
        String pSep = System.getProperty("file.separator");
        meltdownPath = meltdownPath + pSep;
        boards = new Hashtable();

        File db = new File(database);

        if(!db.exists()) {// || args[0].equalsIgnoreCase("-a")) {
            addMode = true;
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(database));
                boards = (Hashtable) ois.readObject();

                ois.close();
                ois = null;
            } catch (IOException e) {
                System.err.println("Couldn't load db: " + e.getMessage());
                System.exit(-1);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't load db: " + e.getMessage());
                System.exit(-1);
            }
        }
        if(addMode) {
            dailyMode = false;
            String stuff = "";
            do {
                try {
                    Board newBoard = new Board();
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    System.out.print("Board name? ");
                    newBoard.setBoard(input.readLine().trim());
                    System.out.print("Board Key? ");
                    newBoard.setKey(input.readLine().trim());
                    System.out.print("Board Description? ");
                    newBoard.setDescription(input.readLine().trim());
                    boards.put(newBoard.getBoard(),newBoard);
                    System.out.println("Generating board ad:\n" + newBoard.getNewBoardAd().getMessage());
                    sendMessage(newBoard.getNewBoardAd().getMessage());
                    do {
                        System.out.println("Add another? (y/n) ");
                        stuff = input.readLine();
                    } while (!stuff.equalsIgnoreCase("y") && !stuff.equalsIgnoreCase("n"));
                } catch (IOException e) {
                    System.err.println("Something shitty happened: " + e.getMessage());
                    System.exit(-1);
                }
            } while (stuff.equalsIgnoreCase("y"));
        }
        if (dailyMode) {
            System.out.println("Adding Greeting.");
            greet();
            System.out.println("Doing dailies.");
            Iterator boardList = boards.keySet().iterator();
            while (boardList.hasNext()) {
                try {
                Board thisBoard = (Board) boards.get(boardList.next());
                sendMessage(thisBoard.getDailyBoardAd().getMessage());
                } catch (ClassCastException e) {
                    System.err.println("CCE: " + e.getMessage());
                }
            }
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(database));
            oos.writeObject(boards);
            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e) {
            System.err.println("Shittiness saving db: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void sendMessage(Message msg) {
    	File []ob = null;
        try {
            ob = (new File(meltdownPath +"outbox")).listFiles();
            //System.out.println("Files length: " + ob.length);
            int nextMsg = 0;
            if (ob.length > 0) {
                for (int i = 0;i < ob.length;i++) {
                    int thisIndex = Integer.parseInt((new StringTokenizer(ob[i].getName(),"_")).nextToken());
                    if (thisIndex > nextMsg) nextMsg++;
                }
                nextMsg++;
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(meltdownPath + "outbox" + pSep + nextMsg + "_boardlists")));
            bw.write(msg.getMessage());
            bw.flush();
            bw.close();
            bw = null;
        } catch (IOException e) {
            System.err.println("IOException in BoardWatch.sendMessage(): " + e.getMessage());
        }catch (Exception e) {
		System.out.println(e.toString());
		if (ob==null) System.out.println("ob=null " + meltdownPath);
	}
    }

	public static void greet() {
		File greeting = new File(meltdownPath + "conf" + pSep + "greeting.txt");
		if (greeting.exists()) {
			System.out.println("Greeting Exists");
			try {
				String ls = System.getProperty("line.separator");
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(greeting)));
				StringBuffer message = new StringBuffer();
				while (br.ready()) {
					message.append(br.readLine() + ls);
				}
				Message msg = new Message();
				msg.setBoard("boardlists");
				msg.setFrom("BoardWatch");
				msg.setSubject("Welcome to boardlists");
				msg.setBody(message.toString());
				sendMessage(msg);
			} catch (IOException e) {
				System.err.println("IOException in BoardWatch.greet(): " + e.getMessage());
			}
		}	
	}
}
