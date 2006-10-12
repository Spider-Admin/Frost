/*
  BoardUpdateBoardSelector.java / Frost
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
package frost.boards;

import java.util.*;

import frost.*;

/**
 * Selects the next board eligible for updating.
 * Prefers boards with sendable messages.
 */
public class BoardUpdateBoardSelector {

    /**
     * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
     */
    private static final Comparator lastUpdateStartMillisCmp = new Comparator() {
        // FIXME: prefer boards that have messages waiting for upload
        // FIXME: update boards with most msgs in last X hours more often
//        xxx
        public int compare(Object o1, Object o2) {
            Board value1 = (Board) o1;
            Board value2 = (Board) o2;
            if (value1.getLastUpdateStartMillis() > value2.getLastUpdateStartMillis()) {
                return 1;
            } else if (value1.getLastUpdateStartMillis() < value2.getLastUpdateStartMillis()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    /**
     * Chooses the next FrostBoard to update (automatic update).
     * First sorts by lastUpdateStarted time, then chooses first board
     * that is allowed to update.
     * Used only for automatic updating.
     * Returns NULL if no board to update is found.
     */
    public static Board selectNextBoard(TofTreeModel tofTreeModel) {
        
        List boards = tofTreeModel.getAllBoards();
        if (boards.size() == 0) {
            return null;
        }
        
        Collections.sort(boards, lastUpdateStartMillisCmp);
        // now first board in list should be the one with latest update of all
        Board board;
        Board nextBoard = null;

        long curTime = System.currentTimeMillis();
        // get in minutes
        int minUpdateInterval = Core.frostSettings.getIntValue("automaticUpdate.boardsMinimumUpdateInterval");
        // min -> ms
        long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

        for (Iterator i=boards.iterator(); i.hasNext(); ) {
            board = (Board)i.next();
            if (nextBoard == null
                && board.isUpdateAllowed()
                && (curTime - minUpdateIntervalMillis) > board.getLastUpdateStartMillis() // minInterval
                && ( (board.isConfigured() && board.getAutoUpdateEnabled())
                      || !board.isConfigured()) ) 
            {
                nextBoard = board;
                break;
            }
        }
        return nextBoard;
    }
}
