/*
 * Created on 24-dic-2004
 * 
 */
package frost.gui;

import java.util.*;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.tree.*;

import frost.gui.objects.Board;

/**
 * This class serves as both the data and selection models of the TofTree.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TofTreeModel extends DefaultTreeModel {

	private static Logger logger = Logger.getLogger(TofTreeModel.class.getName());
	
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
		if (selectedNode.isFolder() == true) {
			selectedNode.add(newNode);
		} else {
			// add to parent of selected node
			selectedNode = (Board) selectedNode.getParent();
			selectedNode.add(newNode);
		}
		// last in list is the newly added
		int insertedIndex[] = { selectedNode.getChildCount() - 1 };
		nodesWereInserted(selectedNode, insertedIndex);
	}

	/**
	 * Returns Vector containing all Boards of the model.
	 * @return Vector containing all the Boards of the model.
	 */
	public Vector getAllBoards() {
		Board node = (Board) getRoot();
		Vector boards = new Vector();
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
	 * comparison is not case sensitive.
	 * @param boardName the name of the board to look for
	 * @return the FrostBoardObject if there was a board with that name. Null
	 *         otherwise.
	 */
	public Board getBoardByName(String boardName) {
		Board node = (Board) getRoot();
		Vector boards = new Vector();
		Enumeration e = node.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			Board child = (Board) e.nextElement();
			if (child.getName().compareToIgnoreCase(boardName) == 0) {
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
	 * 			nothing was selected.
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

}