/*
  SearchMessagesThread.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import frost.gui.*;

public class SearchMessagesThread extends Thread {

    SearchMessagesDialog searchDialog; // used to add found messages
    SearchMessagesDialog.SearchConfig searchConfig;
    
    private boolean stopRequested = false;
    
    
    public SearchMessagesThread(SearchMessagesDialog searchDlg, SearchMessagesDialog.SearchConfig searchCfg) {
        searchDialog = searchDlg;
        searchConfig = searchCfg;
    }
    
    public void run() {
        
    }
    
    public synchronized boolean isStopRequested() {
        return stopRequested;
    }
    public synchronized void requestStop() {
        stopRequested = true;
    }
}
