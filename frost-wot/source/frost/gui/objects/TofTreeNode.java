/*
 * Created on 21-dic-2004
 * 
 */
package frost.gui.objects;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author $Author$
 * @version $Revision$
 */
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
