package fillament.meltdown;

import java.io.*;
import java.util.*;

public class BoardImport {

    public static void main(String[] args) {
        Hashtable boards = new Hashtable();
        String pSep = System.getProperty("file.separator");
        String meltdownPath = "fillament" + pSep + "meltdown" + pSep;

        File[] keys = (new File(meltdownPath + "boardwatch")).listFiles();
        for (int i = 0;i < keys.length;i++) {
            System.out.println("Checking " + keys[i].getName());
            if (keys[i].getName().endsWith(".key")) {
                System.out.println("Processing " + keys[i].getName());
                Board newBoard = new Board();
                String fname = keys[i].getName();
                newBoard.setBoard(fname.substring(0,fname.lastIndexOf(".")));
                boolean goodBoard = true;
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(keys[i])));
                    while (br.ready()) {
                        String line = br.readLine();
                        if (line.startsWith("publicKey=")) {
                            newBoard.setKey(line.substring(line.indexOf("=") + 1));
                        } else if (line.startsWith("description=")) {
                            newBoard.setDescription(line.substring(line.indexOf("=") + 1));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("PooP! IOException: " + e.getMessage());
                    goodBoard = false;
                }
                if (goodBoard) {
                    boards.put(newBoard.getBoard(), newBoard);
                }
            }
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(meltdownPath + "boards.db"));
            oos.writeObject(boards);
            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e) {
               System.err.println("IOException saving db: " + e.getMessage());
        }
    }
}
