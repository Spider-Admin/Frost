/*
  TofTree.java / Frost
  Copyright (C) 2002  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.tree.*;

import frost.*;
import frost.gui.objects.Board;
import frost.storage.*;
import frost.util.gui.JDragTree;
import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class TofTree extends JDragTree implements Savable {
	
	/**
	 * 
	 */
	private class CellRenderer extends DefaultTreeCellRenderer {

		ImageIcon writeAccessIcon;
		ImageIcon writeAccessNewIcon;
		ImageIcon readAccessIcon;
		ImageIcon readAccessNewIcon;
		ImageIcon boardIcon;
		ImageIcon boardNewIcon;
		ImageIcon boardSpammedIcon;
		String fileSeparator;

		Font boldFont = null;
		Font normalFont = null;

		/**
		 * 
		 */
		public CellRenderer() {
			fileSeparator = System.getProperty("file.separator");
			boardIcon = new ImageIcon(MainFrame.class.getResource("/data/board.gif"));
			boardNewIcon = new ImageIcon(MainFrame.class.getResource("/data/boardnew.gif"));
			boardSpammedIcon = new ImageIcon(MainFrame.class.getResource("/data/boardspam.gif"));
			writeAccessIcon = new ImageIcon(MainFrame.class.getResource("/data/waboard.jpg"));
			writeAccessNewIcon = new ImageIcon(MainFrame.class.getResource("/data/waboardnew.jpg"));
			readAccessIcon = new ImageIcon(MainFrame.class.getResource("/data/raboard.jpg"));
			readAccessNewIcon = new ImageIcon(MainFrame.class.getResource("/data/raboardnew.jpg"));
			this.setLeafIcon(new ImageIcon(MainFrame.class.getResource("/data/board.gif")));
			this.setClosedIcon(new ImageIcon(MainFrame.class.getResource("/data/closed.gif")));
			this.setOpenIcon(new ImageIcon(MainFrame.class.getResource("/data/open.gif")));

			JTable dummyTable = new JTable();
			normalFont = dummyTable.getFont();
			boldFont = normalFont.deriveFont(Font.BOLD);

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			Board board = null;
			if (value instanceof Board) {
				board = (Board) value;
			} else {
				logger.severe(
					"Error - TofTreeCellRenderer: got a tree value wich is no FrostBoardObject:\n"
						+ "   node value='"
						+ value
						+ "'  ;  node class='"
						+ value.getClass()
						+ "'\n"
						+ "This should never happen, please report the error.");
				return this;
			}

			boolean containsNewMessage = board.containsNewMessages();

			if (board.isFolder()) {
				// if this is a folder, check board for new messages
				setText(board.getName());
				if (containsNewMessage) {
					setFont(boldFont);
				} else {
					setFont(normalFont);
				}
			} else {
				// set the special text (board name + if new msg. a ' (2)' is appended and bold)
				if (containsNewMessage) {
					setFont(boldFont);
					setText(board.getName() + " (" + board.getNewMessageCount() + ")");
				} else {
					setFont(normalFont);
					setText(board.getName());
				}
			}

			// maybe update visualization
			if (MainFrame.frostSettings.getBoolValue("boardUpdateVisualization")
				&& board.isUpdating() == true) {
				// set special updating colors
				Color c;
				c =
					(Color) MainFrame.frostSettings.getObjectValue(
						"boardUpdatingNonSelectedBackgroundColor");
				setBackgroundNonSelectionColor(c);

				c =
					(Color) MainFrame.frostSettings.getObjectValue(
						"boardUpdatingSelectedBackgroundColor");
				setBackgroundSelectionColor(c);

			} else {
				// refresh colours from the L&F
				setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
				setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
				setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
				setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
			}

			// set the icon
			if (leaf == true) {
				if (board.isPublicBoard()) {
					if (containsNewMessage) {
						setIcon(boardNewIcon);
					} else {
						setIcon(boardIcon);
					}
				} else if (board.isSpammed()) {
					setIcon(boardSpammedIcon);
				} else if (board.isWriteAccessBoard()) {
					if (containsNewMessage) {
						setIcon(writeAccessNewIcon);
					} else {
						setIcon(writeAccessIcon);
					}
				} else if (board.isReadAccessBoard()) {
					if (containsNewMessage) {
						setIcon(readAccessNewIcon);
					} else {
						setIcon(readAccessIcon);
					}
				}
			}
			return this;
		}

	}
    
	private Language language;

	private static Logger logger = Logger.getLogger(TofTree.class.getName());
	
	private TofTreeModel model;

    /**
	 * @param root
	 */
	public TofTree(TofTreeModel model) {
		super(model);
		this.model = model;
		initialize();
	}

    /**
	 *  
	 */
	public void initialize() {

		language = Language.getInstance();

		putClientProperty("JTree.lineStyle", "Angled"); // I like this look

		setRootVisible(true);
		setCellRenderer(new CellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// load nodes from disk
		if (loadTree() == false) {
			Board newRoot = new Board("Frost Message System", true);
			DefaultTreeModel model = new DefaultTreeModel(newRoot);
			setModel(model);
		}
	}

    /**
	 * @param result
	 * @return
	 */
    public Board cutNode(Board result)
    {
        if( result != null )
        {
            removeNode(result);
        }
        return result;
    }

    /**
     * @param node
     */
    public void removeNode(DefaultMutableTreeNode node)
    {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if( node != null && parent != null )
        {
            int[] childIndices = { parent.getIndex(node)};
            Object[] removedChilds = { node };

            node.removeFromParent();

            DefaultTreeModel model = (DefaultTreeModel)getModel();
            TreePath pathToParent = new TreePath(model.getPathToRoot( parent ));
            model.nodesWereRemoved( parent, childIndices, removedChilds );
            setSelectionPath(pathToParent);
        }
    }

    /**
     * @param clipboard
     * @param node
     * @return
     */
    public boolean pasteFromClipboard(Board clipboard, Board node)
    {
        if( node == null || clipboard == null )
            return false;
        if( node.isFolder() == false ) // dont allow to add to boards
            return false;

        node.add( clipboard );

        int insertedIndex[] = { node.getChildCount()-1 }; // last in list is the newly added
        ((DefaultTreeModel)getModel()).nodesWereInserted( node, insertedIndex );

        return true;
    }

    /**
     * Returns Vector containing all leafs of a tree.
     * @return Vector containing DefaultMutableTreeNodes
     */
    public Vector getAllBoards()
    {
        Board node = (Board)this.getModel().getRoot();
        Vector boards = new Vector();
        Enumeration e = node.depthFirstEnumeration();
        while( e.hasMoreElements() )
        {
            Board child = (Board)e.nextElement();
            if( child.isFolder() == false )
            {
                boards.add( child );
            }
        }
        return boards;
    }

	/**
	 * This method looks for a board with the name passed as a parameter. The comparation
	 * is not case sensitive.
	 * @param boardName the name of the board to look for
	 * @return the FrostBoardObject if there was a board with that name. Null otherwise.
	 */
	public Board getBoardByName(String boardName) {
		Board node = (Board) this.getModel().getRoot();
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
     * Loads a tree description file
     */
    private boolean loadTree()
    {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = MainFrame.frostSettings.getValue("config.dir") + "boards.xml";
        // the call changes the toftree and loads nodes into it
        File iniFile = new File(boardIniFilename);
        if( iniFile.exists() == false )
        {
            logger.warning("boards.xml file not found, reading default file (will be saved to boards.xml on exit).");
            boardIniFilename = MainFrame.frostSettings.getValue("config.dir") + "boards.xml.default";
        }
        return xmlio.loadBoardTree( this, model, boardIniFilename );
    }

    /**
     * Save TOF tree's content to a file
     */
    public void save() throws StorageException
    {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = MainFrame.frostSettings.getValue("config.dir") + "boards.xml";
        File check = new File( boardIniFilename );
        if( check.exists() )
        {
            // rename old file to .bak, overwrite older .bak
            String bakBoardIniFilename = MainFrame.frostSettings.getValue("config.dir") + "boards.xml.bak";
            File bakFile = new File(bakBoardIniFilename);
            if( bakFile.exists() )
            {
                bakFile.delete();
            }
            check.renameTo(bakFile);
        }
        // the method scans the toftree
        if (!xmlio.saveBoardTree( this, model, boardIniFilename )) {
        	throw new StorageException("Error while saving the TofTree.");
        }
    }

	/**
	 * Opens dialog, gets new name for board, checks for double names, adds node to tree
	 * @param parent
	 */
	public void createNewBoard(Frame parent) {
		boolean isDone = false;

		while (!isDone) {
			NewBoardDialog dialog = new NewBoardDialog(parent);
			dialog.setVisible(true);

			if (dialog.getChoice() == NewBoardDialog.CHOICE_CANCEL) {
				isDone = true; //cancelled	
			} else {
				String boardName = dialog.getBoardName(); 
				String boardDescription = dialog.getBoardDescription();
				
				if (getBoardByName(boardName) != null) {
					JOptionPane.showMessageDialog(
						parent,
						language.getString("You already have a board with name")
							+ " '"
							+ boardName
							+ "'!\n"
							+ language.getString("Please choose a new name"));
				} else {
					Board newBoard = new Board(boardName, boardDescription);
					addNodeToTree(newBoard);
					// maybe this boardfolder already exists, scan for new messages
					TOF.initialSearchNewMessages(newBoard);
					isDone = true; //added
				}

			}
		}
	}
    
	/**
	 * Checks if board is already existent, adds board to board tree.
	 * @param bname
	 * @param bpubkey
	 * @param bprivkey
	 * @param description
	 */
	private void addNewBoard(String bname, String bpubkey, String bprivkey, String description) {
		if (getBoardByName(bname) != null) {
			int answer =
				JOptionPane.showConfirmDialog(
					getTopLevelAncestor(),
					language.getString("You already have a board with name")
						+ " '"
						+ bname
						+ "'!\n"
						+ language.getString("Do you really want to overwrite it?")
						+ ""
						+ "\n("
						+ language.getString("This will not delete messages")
						+ ")",
						language.getString("Warning"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.NO_OPTION) {
				return; // do not add
			}
		}
		Board newBoard = new Board(bname, bpubkey, bprivkey, description);
		addNodeToTree(newBoard);
		// maybe this boardfolder already exists, scan for new messages
		TOF.initialSearchNewMessages(newBoard);
	}

	/**
	 * Checks if board is already existent, adds board to board tree.
	 * @param fbobj
	 */
	public void addNewBoard(Board fbobj) {
		addNewBoard(
			fbobj.getName(),
			fbobj.getPublicKey(),
			fbobj.getPrivateKey(),
			fbobj.getDescription());
	}
    
	/**
	 * Opens dialog, gets new name for folder, checks for double names, adds node to tree
	 * @param parent
	 */
	public void createNewFolder(Frame parent) {
		String nodeName = null;
		do {
			Object nodeNameOb =
				JOptionPane.showInputDialog(
					parent,
					language.getString("Please enter a name for the new folder") + ":",
					language.getString("New Folder Name"),
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					language.getString("newfolder"));

			nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString());

			if (nodeName == null)
				return; // cancelled

		} while (nodeName.length() == 0);

		addNodeToTree(new Board(nodeName, true));
	}

    /**
     * Adds a node to tof tree, adds only under folders, begins with selected node.
     * @param newNode
     */
    public void addNodeToTree(Board newNode)
    {
        Board selectedNode = (Board)getLastSelectedPathComponent();
        if( selectedNode != null)
        {
            if( selectedNode.isFolder()==true )
            {
                selectedNode.add(newNode);
            }
            else
            {
                // add to parent of selected node
                selectedNode = (Board)selectedNode.getParent();
                selectedNode.add(newNode);
            }
        }
        else
        {
            // add to root node
            selectedNode = (Board)getModel().getRoot();
            selectedNode.add(newNode);
        }
        int insertedIndex[] = { selectedNode.getChildCount()-1}; // last in list is the newly added
        ((DefaultTreeModel)getModel()).nodesWereInserted( selectedNode, insertedIndex );
    }

    /**
     * @param pathParent
     * @param nChildIndex
     * @return
     */
    private TreePath getChildPath(TreePath pathParent, int nChildIndex)
    {
        TreeModel model =  getModel();
        return pathParent.pathByAddingChild(model.getChild(pathParent.getLastPathComponent(), nChildIndex));
    }


	/**
	 * @return
	 */
	public Board getSelectedNode() {
		Board node = (Board) getLastSelectedPathComponent();
		if (node == null) {
			// nothing selected? unbelievable ! so select the root ...
			setSelectionRow(0);
			node = (Board) getModel().getRoot();
		}
		return node;
	}

}
