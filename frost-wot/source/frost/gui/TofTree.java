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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import frost.*;
import frost.gui.objects.*;

public class TofTree extends JTree
implements DragGestureListener, DropTargetListener, DragSourceListener
{
    final public static DataFlavor NODE_FLAVOR = new DataFlavor(TofTree.class, "Tof Tree Node");
    static DataFlavor flavors[] = { NODE_FLAVOR };

    protected DefaultMutableTreeNode dragNode = null;
    private DragSource dragSource = null;
    private DragSourceContext dsContext = null;
    //    private DragGestureListener dgListener = null;
    //    private DragSourceListener dsListener = null;
    private DragGestureRecognizer dgRecognizer = null;
    private DropTarget dropTarget = null;

    public TofTree(TreeNode root)
    {
        super(root);
        // install drag n drop support
        this.dragSource=DragSource.getDefaultDragSource();
        dgRecognizer = this.dragSource.createDefaultDragGestureRecognizer( this,
                                                                           DnDConstants.ACTION_MOVE,
                                                                           this);
        // don't act on right mouse button
        dgRecognizer.setSourceActions(dgRecognizer.getSourceActions() & ~InputEvent.BUTTON3_MASK & ~InputEvent.BUTTON2_MASK);
        dropTarget = new DropTarget(this, this);
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

    // DragGestureListener interface method
    public void dragGestureRecognized(DragGestureEvent e)
    {
        try {
            //we should make sure we aren't in edit mode
            InputEvent ievent=e.getTriggerEvent();
            MouseEvent mevent=(MouseEvent)ievent;
            if( mevent!=null )
            {
                System.out.println("checking mouse event");
                //even though I tell dgRecognizer to ignore the
                //the right mouse button, it thinks the RMB starts
                //a drag event...argh
                if( (mevent.getModifiers() & InputEvent.BUTTON3_MASK) != 0 )
                {
                    //System.out.println("button3 in drag!");
                    return;
                }
            }
            dragNode = (FrostBoardObject)getLastSelectedPathComponent();
            if( dragNode != null && !dragNode.isRoot() )
            {
                Transferable trans = (Transferable) new StringSelection((String)dragNode.getUserObject());
                Cursor cursor = DragSource.DefaultCopyNoDrop;
                int action = e.getDragAction();
                if( action == DnDConstants.ACTION_MOVE )
                    cursor = DragSource.DefaultMoveNoDrop;
                cursor=Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                dragSource.startDrag(e, cursor, trans, this);
            }
        }
        catch( InvalidDnDOperationException idoe ) {
            System.err.println(idoe);
        }
    }

    // DragSourceListener interface methods
    public void dragDropEnd(DragSourceDropEvent e) {}
    public void dragEnter(DragSourceDragEvent e) {}
    public void dragOver(DragSourceDragEvent e) {}
    public void dropActionChanged(DragSourceDragEvent e) {}
    public void dragExit(DragSourceEvent e) {}

    // DropTargetListener interface methods
    public void drop(DropTargetDropEvent e)
    {
        try {
            Transferable trans = e.getTransferable();

            //make sure we started the drag
            if( !trans.isDataFlavorSupported(DataFlavor.stringFlavor /* NODE_FLAVOR */) || dragNode == null )
                e.rejectDrop();

            String childInfo = (String) trans.getTransferData(DataFlavor.stringFlavor /* NODE_FLAVOR */);
            Point loc = e.getLocation();
            TreePath destPath = getPathForLocation(loc.x, loc.y);
            DefaultMutableTreeNode newParent = (DefaultMutableTreeNode)destPath.getLastPathComponent();
            DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode)dragNode.getParent();
            //      if (newParent.isRoot()) {
            //      e.rejectDrop();
            //      e.getDropTargetContext().dropComplete(false);
            //      return;
            //      }
            // we need to verify that the drag/drop operation is valid
            try
            {
                DefaultTreeModel model = (DefaultTreeModel)getModel();

                if( newParent.isLeaf()/* && !newParent.getAllowsChildren()*/ )
                {
                    //dropped on a leaf, insert into leaf's parent before leaf
                    DefaultMutableTreeNode leafParent=(DefaultMutableTreeNode)newParent.getParent();
                    int idx=leafParent.getIndex(newParent);
                    if( idx < 0 )
                    {
                        System.out.println("child not found in parent!!!");
                        //throw new Exception;
                    }
                    else
                    {
                        newParent=leafParent;
                        // remove node from oldParent ...
                        Object[] removedChilds = { dragNode};
                        int[] childIndices = { oldParent.getIndex(dragNode)};
                        dragNode.removeFromParent();
                        model.nodesWereRemoved( oldParent, childIndices, removedChilds );

                        // ... and insert into newParent
                        newParent.insert(dragNode, idx);
                        int insertedIndex[] = { idx};
                        model.nodesWereInserted( newParent, insertedIndex );
                    }
                }
                else
                {
                    //dropped on a folder

                    // remove node from oldParent ...
                    Object[] removedChilds = { dragNode};
                    int[] childIndices = { oldParent.getIndex(dragNode)};
                    dragNode.removeFromParent();
                    model.nodesWereRemoved( oldParent, childIndices, removedChilds );

                    // ... and add into newParent
                    newParent.add(dragNode);
                    int insertedIndex[] = { newParent.getChildCount()-1};
                    model.nodesWereInserted( newParent, insertedIndex );
                }
                e.acceptDrop(DnDConstants.ACTION_MOVE);
            }
            catch( java.lang.IllegalStateException exc )
            {
                e.rejectDrop();
            }

            e.getDropTargetContext().dropComplete(true);
        }
        catch( IOException exc ) {
            e.rejectDrop();
        }
        catch( UnsupportedFlavorException exc ) {
            e.rejectDrop();
        }
        catch( Exception exc ) {
            e.rejectDrop();
            exc.printStackTrace();
        }
    }

    public void dragEnter(DropTargetDragEvent e) {}
    public void dragExit(DropTargetEvent e) {}

    public void dragOver(DropTargetDragEvent e)
    {
        //set cursor location. Needed in setCursor method
        Point cursorLocation = e.getLocation();
        TreePath destPath =
        getPathForLocation(cursorLocation.x, cursorLocation.y);

        if( destPath != null )
            this.setSelectionPath(destPath);

        // if destination path is okay accept drag...
        e.acceptDrag(DnDConstants.ACTION_MOVE ) ;
    }

    public void dropActionChanged(DropTargetDragEvent e) {}


    public FrostBoardObject cutSelectedNode()
    {
        FrostBoardObject result = (FrostBoardObject)this.getLastSelectedPathComponent();
        if( result != null )
        {
            removeSelectedNode();
        }
        return result;
    }

    public void removeSelectedNode()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getLastSelectedPathComponent();
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

    public boolean pasteFromClipboard(FrostBoardObject clipboard)
    {
        FrostBoardObject node = (FrostBoardObject)getLastSelectedPathComponent();
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

    public FrostBoardObject getBoardByName(String boardName)
    {
        FrostBoardObject node = (FrostBoardObject)this.getModel().getRoot();
        Vector boards = new Vector();
        Enumeration e = node.depthFirstEnumeration();
        while( e.hasMoreElements() )
        {
            FrostBoardObject child = (FrostBoardObject)e.nextElement();
            if( child.toString().equals( boardName ) )
            {
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
        String boardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml";
        // the call changes the toftree and loads nodes into it
        File iniFile = new File(boardIniFilename);
        if( iniFile.exists() == false )
        {
            System.out.println("boards.xml file not found, reading default file (will be saved to boards.xml on exit).");
            boardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml.default";
        }
        return xmlio.loadBoardTree( this, boardIniFilename );
    }

    /**
     * Save TOF tree's content to a file
     * @param node Save this nodes content
     * @param file The destination file
     */
    public void saveTree()
    {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml";
        // the call changes the toftree and loads nodes into it
        if( xmlio.saveBoardTree( this, boardIniFilename ) == false ) // save OK?
        {
            // TODO: write new config file, rename old
            return;
        }
    }
}
