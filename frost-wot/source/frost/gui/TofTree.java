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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.tree.*;

import frost.*;
import frost.gui.objects.FrostBoardObject;
import frost.threads.maintenance.Savable;
import frost.util.gui.JDragTree;
import frost.util.gui.translation.UpdatingLanguageResource;

public class TofTree extends JDragTree implements Savable {
	
	/**
	 * @author Administrator
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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

		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			FrostBoardObject board = null;
			if (value instanceof FrostBoardObject) {
				board = (FrostBoardObject) value;
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

			boolean containsNewMessage = board.containsNewMessage();

			if (board.isFolder()) {
				// if this is a folder, check board for new messages
				if (board.containsFolderNewMessages()) {
					setFont(boldFont);
				} else {
					setFont(normalFont);
				}
			} else {
				// set the sdpecial text (board name + if new msg. a ' (2)' is appended and bold)
				setText(board.getVisibleText());
				if (containsNewMessage) {
					setFont(boldFont);
				} else {
					setFont(normalFont);
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
    
	private UpdatingLanguageResource languageResource;

	private static Logger logger = Logger.getLogger(TofTree.class.getName());

    public TofTree(TreeNode root, UpdatingLanguageResource newLanguageResource)
    {
        super(root);

		languageResource = newLanguageResource;

        putClientProperty("JTree.lineStyle", "Angled"); // I like this look
        
		setRootVisible(true);
		setCellRenderer(new CellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void initialize()
    {
        // load nodes from disk
        if( loadTree() == false )
        {
            FrostBoardObject newRoot = new FrostBoardObject("Frost Message System", true);
            DefaultTreeModel model = new DefaultTreeModel(newRoot);
            setModel( model );
        }
    }

    public FrostBoardObject cutNode(FrostBoardObject result)
    {
        if( result != null )
        {
            removeNode(result);
        }
        return result;
    }

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

    public boolean pasteFromClipboard(FrostBoardObject clipboard, FrostBoardObject node)
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
        FrostBoardObject node = (FrostBoardObject)this.getModel().getRoot();
        Vector boards = new Vector();
        Enumeration e = node.depthFirstEnumeration();
        while( e.hasMoreElements() )
        {
            FrostBoardObject child = (FrostBoardObject)e.nextElement();
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
	public FrostBoardObject getBoardByName(String boardName) {
		FrostBoardObject node = (FrostBoardObject) this.getModel().getRoot();
		Vector boards = new Vector();
		Enumeration e = node.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			FrostBoardObject child = (FrostBoardObject) e.nextElement();
			if (child.toString().compareToIgnoreCase(boardName) == 0) {
				return child;
			}
		}
		return null; // not found
	}

    /**
     * Loads a tree description file
     * @param node The content of the file will be added to this node
     * @param file This file will be read
     */
    public boolean loadTree()
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
        return xmlio.loadBoardTree( this, boardIniFilename );
    }

    /**
     * Save TOF tree's content to a file
     * @param node Save this nodes content
     * @param file The destination file
     */
    public boolean save()
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
        return xmlio.saveBoardTree( this, boardIniFilename );
    }

	/**
	 * Opens dialog, gets new name for board, checks for double names, adds node to tree
	 */
	public void createNewBoard(Frame parent) {
		boolean isDone = false;

		while (!isDone) {
			NewBoardDialog dialog = new NewBoardDialog(parent, languageResource);
			dialog.setVisible(true);

			if (dialog.getChoice() == NewBoardDialog.CHOICE_CANCEL) {
				isDone = true; //cancelled	
			} else {
				String boardName = dialog.getBoardName(); 
				String boardDescription = dialog.getBoardDescription();
				
				if (getBoardByName(boardName) != null) {
					JOptionPane.showMessageDialog(
						parent,
						languageResource.getString("You already have a board with name")
							+ " '"
							+ boardName
							+ "'!\n"
							+ languageResource.getString("Please choose a new name"));
				} else {
					FrostBoardObject newBoard = new FrostBoardObject(boardName, boardDescription);
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
	 */
	private void addNewBoard(String bname, String bpubkey, String bprivkey, String description) {
		if (getBoardByName(bname) != null) {
			int answer =
				JOptionPane.showConfirmDialog(
					getTopLevelAncestor(),
					languageResource.getString("You already have a board with name")
						+ " '"
						+ bname
						+ "'!\n"
						+ languageResource.getString("Do you really want to overwrite it?")
						+ ""
						+ "\n("
						+ languageResource.getString("This will not delete messages")
						+ ")",
					languageResource.getString("Warning"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.NO_OPTION) {
				return; // do not add
			}
		}
		FrostBoardObject newBoard = new FrostBoardObject(bname, bpubkey, bprivkey, description);
		addNodeToTree(newBoard);
		// maybe this boardfolder already exists, scan for new messages
		TOF.initialSearchNewMessages(newBoard);
	}

	/**
	 * Checks if board is already existent, adds board to board tree.
	 */
	public void addNewBoard(FrostBoardObject fbobj) {
		addNewBoard(
			fbobj.getBoardName(),
			fbobj.getPublicKey(),
			fbobj.getPrivateKey(),
			fbobj.getDescription());
	}
    
	/**
	 * Opens dialog, gets new name for folder, checks for double names, adds node to tree
	 */
	public void createNewFolder(Frame parent) {
		String nodeName = null;
		do {
			Object nodeNameOb =
				JOptionPane.showInputDialog(
					parent,
					languageResource.getString("Please enter a name for the new folder") + ":",
					languageResource.getString("New Folder Name"),
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					languageResource.getString("newfolder"));

			nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString());

			if (nodeName == null)
				return; // cancelled

		} while (nodeName.length() == 0);

		addNodeToTree(new FrostBoardObject(nodeName, true));
	}

    /**
     * Adds a node to tof tree, adds only under folders, begins with selected node.
     */
    public void addNodeToTree(FrostBoardObject newNode)
    {
        FrostBoardObject selectedNode = (FrostBoardObject)getLastSelectedPathComponent();
        if( selectedNode != null)
        {
            if( selectedNode.isFolder()==true )
            {
                selectedNode.add(newNode);
            }
            else
            {
                // add to parent of selected node
                selectedNode = (FrostBoardObject)selectedNode.getParent();
                selectedNode.add(newNode);
            }
        }
        else
        {
            // add to root node
            selectedNode = (FrostBoardObject)getModel().getRoot();
            selectedNode.add(newNode);
        }
        int insertedIndex[] = { selectedNode.getChildCount()-1}; // last in list is the newly added
        ((DefaultTreeModel)getModel()).nodesWereInserted( selectedNode, insertedIndex );
    }

// More helpers...
    private TreePath getChildPath(TreePath pathParent, int nChildIndex)
    {
        TreeModel model =  getModel();
        return pathParent.pathByAddingChild(model.getChild(pathParent.getLastPathComponent(), nChildIndex));
    }


	/**
	 * @return
	 */
	public FrostBoardObject getSelectedNode() {
		FrostBoardObject node = (FrostBoardObject) getLastSelectedPathComponent();
		if (node == null) {
			// nothing selected? unbelievable ! so select the root ...
			setSelectionRow(0);
			node = (FrostBoardObject) getModel().getRoot();
		}
		return node;
	}

}
