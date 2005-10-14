/*
  StandartMultipleNodeManager.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.fcp;

import java.util.*;

/**
 * not exactly RoundRobinning node manager
 */
public class StandartMultipleNodeManager extends MultipleNodeManager {

	boolean forward;
	ListIterator it;

	public void init() {
		it = (new LinkedList(frost.Core.getNodes())).listIterator();
		forward = true;
	}
	
	protected void delegateRemove(String node) {
		it.remove();
	}

	protected String selectNode() {
		
		//first, if the list is empty throw something
		if (frost.Core.getNodes().size()==0) {
			throw new Error("no nodes registered!  You need to have at least one");
        }
			
		//second, avoid stupid endless recursion
		if (frost.Core.getNodes().size()==1) {
			return (String) frost.Core.getNodes().iterator().next(); //use a different iterator
        }
		
		if( forward) {
			if (it.hasNext()) {
                return (String)it.next();
            } else {
				forward = false;
            }
		}
		
		if (it.hasPrevious()) {
			return (String)it.previous();
        } else { 
			forward=true;
        }

		return selectNode(); //recursion....
	}
}
