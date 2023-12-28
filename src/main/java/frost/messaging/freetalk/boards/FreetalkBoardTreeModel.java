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
package frost.messaging.freetalk.boards;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This class serves as both the data and selection models of the TofTree.
 */
public class FreetalkBoardTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

    private final DefaultTreeSelectionModel selectionModel;

    /**
     * This method creates a new TofTreeModel with the given TreeNode
     * as its root.
     * @param root TreeNode that will be the root of the new TofTreeModel.
     */
    public FreetalkBoardTreeModel(final TreeNode root) {
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
    public void addNodeToTree(final AbstractFreetalkNode newNode) {
        final AbstractFreetalkNode selectedNode = getSelectedNode();
        final FreetalkFolder targetFolder;
        if (selectedNode.isFolder() != true) {
            // add to parent of selected node
            targetFolder = (FreetalkFolder) selectedNode.getParent();
        } else {
            targetFolder = (FreetalkFolder) selectedNode;
        }
        addNodeToTree(newNode, targetFolder);
    }

    /**
     * Adds a new boards to the specified target folder.
     */
    public void addNodeToTree(final AbstractFreetalkNode newNode, final FreetalkFolder targetFolder) {
        targetFolder.add(newNode);

//        if( newNode.isBoard() ) {
//            if( MessageStorage.inst().assignPerstFrostBoardObject((Board)newNode) == false ) {
//                return;
//            }
//        }

        // last in list is the newly added
        final int insertedIndex[] = { targetFolder.getChildCount() - 1 };
        nodesWereInserted(targetFolder, insertedIndex);
    }

    /**
     * Removes the node from the board tree.
     * If node is a folder ALL subfolders and boards and messages are deleted too.
     */
    public void removeNode(final AbstractFreetalkNode node, final boolean removeFromDatabase) {
        final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (node != null && parent != null) {
            final List<FreetalkBoard> boardsToDelete = new LinkedList<FreetalkBoard>();
            if( removeFromDatabase ) {
                if( node.isFolder() ) {
					for (final Enumeration<TreeNode> e = node.breadthFirstEnumeration(); e.hasMoreElements();) {
						final AbstractFreetalkNode b = (AbstractFreetalkNode) e.nextElement();
                        if( !b.isFolder() ) {
                            boardsToDelete.add((FreetalkBoard)b);
                        }
                    }
                } else if( node.isBoard() ) {
                    boardsToDelete.add((FreetalkBoard)node);
                }
            }

            // find item to select after delete (the item before or after the deleted node)
            TreeNode nextSelectedNode = node.getPreviousSibling();
            if( nextSelectedNode == null ) {
                nextSelectedNode = node.getNextSibling();
                if( nextSelectedNode == null ) {
                    nextSelectedNode = parent;
                }
            }
            final TreePath nextSelectionPath = new TreePath(getPathToRoot(nextSelectedNode));

            // remove from tree
            final int[] childIndices = { parent.getIndex(node) };
            final Object[] removedChilds = { node };

            node.removeFromParent();
            nodesWereRemoved(parent, childIndices, removedChilds);

            selectionModel.setSelectionPath(nextSelectionPath);

            // maybe delete all boards
            if( !boardsToDelete.isEmpty() ) {
//                final Thread worker = new Thread() {
//                    @Override
//                    public void run() {
//                        for( final FreetalkBoard board : boardsToDelete ) {
//                            // due to cascade delete this deletes all messages of this board too
//                            MessageStorage.inst().removeBoard(board);
//                        }
//                    }
//                };
//                worker.start();
            }
        }
    }

    /**
     * Returns List containing all Boards of the model.
     * @return List containing all the Boards of the model.
     */
    public LinkedList<FreetalkBoard> getAllBoards() {
        final LinkedList<FreetalkBoard> boards = new LinkedList<FreetalkBoard>();
		final Enumeration<TreeNode> e = getRoot().depthFirstEnumeration();
        while (e.hasMoreElements()) {
			final AbstractFreetalkNode child = (AbstractFreetalkNode) e.nextElement();
            if (child.isBoard()) {
                boards.add((FreetalkBoard)child);
            }
        }
        return boards;
    }

    @Override
    public AbstractFreetalkNode getRoot() {
        return (AbstractFreetalkNode)super.getRoot();
    }

    /**
     * This method looks for a board with the name passed as a parameter. The
     * comparison is not case sensitive. Folders are ignored.
     * @param boardName the name of the board to look for
     * @return the FrostBoardObject if there was a board with that name. Null otherwise.
     */
    public FreetalkBoard getBoardByName(final String boardName) {
        if( boardName == null ) {
            return null;
        }
		final Enumeration<TreeNode> e = getRoot().depthFirstEnumeration();
        while (e.hasMoreElements()) {
			final AbstractFreetalkNode child = (AbstractFreetalkNode) e.nextElement();
            if (child.isBoard()
                    && child.getName().compareToIgnoreCase(boardName) == 0)
            {
                return (FreetalkBoard)child;
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
    public AbstractFreetalkNode getSelectedNode() {
        final TreePath selectedPath = selectionModel.getSelectionPath();
        AbstractFreetalkNode node;
        if (selectedPath != null) {
            node = (AbstractFreetalkNode) selectedPath.getLastPathComponent();
        } else {
            // nothing selected? unbelievable! so select the root ...
            node = getRoot();
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
}
