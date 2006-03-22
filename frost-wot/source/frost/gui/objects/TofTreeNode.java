/*
  TofTreeNode.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui.objects;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

public class TofTreeNode extends DefaultMutableTreeNode implements Comparable {

    private Board board;

    private boolean isFolder = false;

    private String name;

    /**
     * This constructor creates a new instance of TofTreeNode that
     * contains a folder with the given name
     * @param name name for the new folder.
     */
    public TofTreeNode(String name) {
        this.name = name;
        this.isFolder = true;
    }

    /**
     * This constructor creates a new instance of TofTreeNode that
     * contains the given board.
     * @param board board to store in this node.
     */
    public TofTreeNode(Board board) {
        this.board = board;
        this.isFolder = false;
        name = board.getName();
    }

    /**
     * This method returns the associated Board, or null if this
     * node contains a folder
     * @return the associated board or null if this node contains a folder.
     */
    public Board getBoard() {
        return board;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof TofTreeNode) {
            TofTreeNode node = (TofTreeNode) o;
            if (node.isFolder() == isFolder()) {
                //If both objects are of the same kind, sort by name
                return getName().compareToIgnoreCase(node.getName());
            } else {
                //If they are of a different kind, the folder is first.
                return isFolder() ? -1 : 1;
            }
        } else {
            return 0;
        }
    }

    /**
     * @return
     */
    public boolean isFolder() {
        return isFolder;
    }

    /**
     * This method returns name of this node.
     * @return the name of this node.
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        return (isFolder() == false);
    }

    /**
     * This method alphabetically sorts the children.
     */
    public void sortChildren() {
        Collections.sort(children);
    }

}
