/*
  IndexSlots.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.objects.*;
import frost.transferlayer.*;

/**
 * Class provides functionality to track used index slots
 * for upload and download.
 * 
 * The index file is stored to keypool/boarddir/???
 * One index is used for each day.
 */
public class IndexSlots implements IndexFileUploaderCallback {

    private static Logger logger = Logger.getLogger(IndexSlots.class.getName());

    private final static String fileSeparator = System.getProperty("file.separator");

    private static final Integer EMPTY = new Integer(0);
    private static final Integer USED  = new Integer(-1);

    private int maxSlotsPerDay;

    private Vector slots;
    private File slotsFile;

    private Board targetBoard;

    public IndexSlots(String indexFileName, Board b, String date, int maxSlotsPerDay) {
        targetBoard = b;
        this.maxSlotsPerDay = maxSlotsPerDay;
        slotsFile = new File(MainFrame.keypool + targetBoard.getBoardFilename() + fileSeparator + indexFileName + date);
        loadSlotsFile(date);
    }

    private void loadSlotsFile(String loadDate) {

        if( slotsFile.isFile() ) {
            try {
                // load new format, each int on a line, -1 means USED, all other mean EMPTY
                slots = new Vector();
                BufferedReader rdr = new BufferedReader(new FileReader(slotsFile));
                String line;
                while( (line=rdr.readLine()) != null ) {
                    line = line.trim();
                    if(line.length() == 0) {
                        continue;
                    }
                    if( line.equals("-1") ) {
                        slots.add(USED);
                    } else {
                        slots.add(EMPTY);
                    }
                    // max MAX_SLOTS_PER_DAY
                    if( slots.size() >= maxSlotsPerDay ) {
                        break; // (allows to lower index slot count)
                    }
                }
                rdr.close();
            } catch (Throwable exception) {
                logger.log(Level.SEVERE, "Exception thrown in loadIndex(String date) - Date: '" + loadDate
                        + "' - Board name: '" + targetBoard.getBoardFilename() + "'", exception);
            }
        }
        // problem with file, start new indices
        if( slots == null ) {
            slots = new Vector();
        }
        // fill up (allows to raise index slot count)
        for (int i = slots.size(); i < maxSlotsPerDay; i++) {
            slots.add( EMPTY );
        }
    }

    /**
     * Returns false if we should stop this thread because board was deleted.
     */
    private boolean isTargetBoardValid() {
        File d = new File(MainFrame.keypool + targetBoard.getBoardFilename());
        if( d.isDirectory() ) {
            return true;
        } else {
            return false;
        }
    }

    private void saveSlotsFile() {
        if( isTargetBoardValid() == false ) {
            return;
        }
        try {
            slotsFile.delete();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(slotsFile)));
            for (int i=0; i < slots.size(); i++) {
                Integer current = (Integer)slots.elementAt(i);
                out.println(current.toString());
            }
            out.flush();
            out.close();
        } catch(Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in saveSlotsFile()", e);
        }
    }

    public int findFirstFreeDownloadSlot() {
        for (int i=0; i < slots.size(); i++){
            Integer current = (Integer)slots.elementAt(i);
            if (current.intValue() > -1) {
                return i;
            }
        }
        return -1;
    }

    /**
     * First free upload slot is right behind last used slot.
     */
    public int findFirstFreeUploadSlot() {
        for (int i=slots.size()-1; i >= 0; i--){
            Integer current = (Integer)slots.elementAt(i);
            if (current.intValue() < 0) {
                // used slot found
                if( i+1 < slots.size() ) {
                    return i+1;
                } else {
                    return -1; // all slots used
                }
            }
        }
        // no used slot found, return first slot
        return 0;
    }

    public int findNextFreeSlot(int beforeIndex) {
        for (int i = beforeIndex+1; i < slots.size(); i++) {
            Integer current = (Integer)slots.elementAt(i);
            if (current.intValue() > -1) {
                return i;
            }
        }
        return -1;
    }

    public void setSlotUsed(int i) {
        int current = ((Integer)slots.elementAt(i)).intValue();
        if (current < 0 ) {
            logger.severe("WARNING - index sequence screwed in setSlotUsed. report to a dev");
            return;
        }
        slots.setElementAt(USED, i);

        // save the changed data immediately
        saveSlotsFile();
    }
}
