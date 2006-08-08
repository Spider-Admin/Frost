/*
  TofTreeModel.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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

import javax.swing.tree.*;

import frost.gui.objects.Board;

/**
 * This class serves as both the data and selection models of the TofTree.
 */
public class TofTreeModel extends DefaultTreeModel {

//    private static Logger logger = Logger.getLogger(TofTreeModel.class.getName());

    private DefaultTreeSelectionModel selectionModel;

    /**
     * This method creates a new TofTreeModel with the given TreeNode
     * as its root.
     * @param root TreeNode that will be the root of the new TofTreeModel.
     */
    public TofTreeModel(TreeNode root) {
        super(root);
        selectionModel = new DefaultTreeSelectionModel();
    }

    /**
     * This method adds a new board to the model.
     * If the last node of the first selected path is a folder,
     * that new board is added as a child of it.
     * If the last node of the first selected path is a board, the new
     * board is added as a child of its parent.
     * If nothing is selected, the new board is added as a child of the root.
     * begins with selected node.
     *
     * @param newNode Board to be added to the model.
     */
    public void addNodeToTree(Board newNode) {
        Board selectedNode = (Board) getSelectedNode();
        if (selectedNode.isFolder() != true) {
            // add to parent of selected node
            selectedNode = (Board) selectedNode.getParent();
        }
        addNodeToTree( newNode, selectedNode);
    }

    /**
     * Adds a new boards to the specified target folder.
     */
    public void addNodeToTree(Board newNode, Board targetFolder) {
        targetFolder.add(newNode);
        // last in list is the newly added
        int insertedIndex[] = { targetFolder.getChildCount() - 1 };
        nodesWereInserted(targetFolder, insertedIndex);
    }

    /**
     * Returns Vector containing all Boards of the model.
     * @return Vector containing all the Boards of the model.
     */
    public LinkedList getAllBoards() {
        Board node = (Board) getRoot();
        LinkedList boards = new LinkedList();
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            Board child = (Board) e.nextElement();
            if (child.isFolder() == false) {
                boards.add(child);
            }
        }
        return boards;
    }

    /**
     * This method looks for a board with the name passed as a parameter. The
     * comparison is not case sensitive. Folders are ignored.
     * @param boardName the name of the board to look for
     * @return the FrostBoardObject if there was a board with that name. Null otherwise.
     */
    public Board getBoardByName(String boardName) {
        // FIXME: compare by boardFileName! but check callers if ok for all!
        Board node = (Board) getRoot();
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            Board child = (Board) e.nextElement();
            if (child.isFolder() == false &&
                child.getName().compareToIgnoreCase(boardName) == 0) {
                return child;
            }
        }
        return null; // not found
    }

    /**
     * This method returns the last node of the first selected path.
     * If no path is selected, the root of the model is selected and
     * returned.
     * @return the last node of the first selected path or the root if
     *          nothing was selected.
     */
    public Board getSelectedNode() {
        TreePath selectedPath = selectionModel.getSelectionPath();
        Board node;
        if (selectedPath != null) {
            node = (Board) selectedPath.getLastPathComponent();
        } else {
            // nothing selected? unbelievable ! so select the root ...
            node = (Board) getRoot();
            selectionModel.setSelectionPath(new TreePath(node));
        }
        return node;
    }

    /**
     * This method returns the selection model
     * @return the selection model.
     */
    TreeSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * @param node
     */
    public void removeNode(Board node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (node != null && parent != null) {
            int[] childIndices = { parent.getIndex(node) };
            Object[] removedChilds = { node };

            node.removeFromParent();

            TreePath pathToParent = new TreePath(getPathToRoot(parent));
            nodesWereRemoved(parent, childIndices, removedChilds);
            selectionModel.setSelectionPath(pathToParent);
        }
    }
}
