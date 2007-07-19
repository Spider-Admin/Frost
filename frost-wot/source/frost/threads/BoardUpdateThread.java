/*
  BoardUpdateThread.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import frost.boards.*;

public interface BoardUpdateThread
{
    // thread types
    public final static int MSG_DNLOAD_TODAY  = 1;
    public final static int MSG_DNLOAD_BACK   = 2;

    int getThreadType();

    // FrostBoard getTargetBoard()
    public Board getTargetBoard();

    public long getStartTimeMillis();

    // methods from Thread class needed by gui
    public void interrupt();
    public boolean isInterrupted();
    // not using isAlive() here, because Thread will call listener before it dies
    // so using own implementation of finished
    public boolean isFinished();

    // allow to register listener, should only be used by RunningBoardUpdateThreads class
    // other classes should register to the RunningBoardUpdateThreads class
    // the difference is: if the thread class the listener directly, the ArrayList = null
    // the underlying Thread class should fire the finished event with parameters:
    //  boardUpdateThreadFinished(null, this)
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener);
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener);
}
