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

public class TofTree extends JTree
implements DragGestureListener, DropTargetListener, DragSourceListener
{
    final public static DataFlavor NODE_FLAVOR = new DataFlavor(TofTree.class, "Tof Tree Node");
    static DataFlavor flavors[] = { NODE_FLAVOR };

    protected DefaultMutableTreeNode selectedNode = null;
    protected DefaultMutableTreeNode dragNode = null;
    private DragSource dragSource = null;
    private DragSourceContext dsContext = null;
    //    private DragGestureListener dgListener = null;
    //    private DragSourceListener dsListener = null;
    private DragGestureRecognizer dgRecognizer = null;
    private DropTarget dropTarget = null;

    public TofTree(DefaultMutableTreeNode node)
    {
        super(node);
        this.dragSource=DragSource.getDefaultDragSource();
        //  this.dgListener=new DGListener();
        //  this.dsListener=new DSListener();
        dgRecognizer = this.dragSource.createDefaultDragGestureRecognizer( this,
                                                                           DnDConstants.ACTION_MOVE,
                                                                           this);
        // don't act on right mouse button
        dgRecognizer.setSourceActions(dgRecognizer.getSourceActions() & ~InputEvent.BUTTON3_MASK & ~InputEvent.BUTTON2_MASK);
        dropTarget = new DropTarget(this, this);
    }

    public DefaultMutableTreeNode getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedTof(DefaultMutableTreeNode node)
    {
        selectedNode=node;
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
            dragNode = getSelectedNode();
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


    /*******************************
     * Now the methods of TreeFun wich really belong to this TofTree
     *******************************/

    /**
     * Save TOF tree's content to a file
     * @param node Save this nodes content
     * @param file The destination file
     */
    public void saveTree(File file)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getModel().getRoot();
        String text = recTreeRead(node, "");
        FileAccess.writeFile(text, file);
    }

    /**
     * Generates a textfile that describes a tree
     * @param node This node will be described
     * @param text If not an empty String, recTreeRead will append this tree's description
     * @return The description of that tree
     */
    public String recTreeRead(DefaultMutableTreeNode node, String text)
    {

        for( int i = 0; i < node.getChildCount(); i ++ )
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
            if( child.isLeaf() )
                text += "=" + (String)child.getUserObject() + "\r\n";
            else
            {
                text += ">" + (String)child.getUserObject() + "\r\n";
                text = recTreeRead(child, text);
            }
        }
        return text + "<\r\n";
    }

    public String cutSelectedNode()
    {
        String result = copySelectedNode();
        if( result != null )
        {
            removeSelectedNode();
        }
        return result;
    }

    public String copySelectedNode()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        StringBuffer result = new StringBuffer();
        if( node != null && parent != null )
        {
            result.append(">").append( (String)node.getUserObject() ).append("\r\n")
            .append( recTreeRead(node, "") );
            return result.toString();
        }
        return null;
    }

    public void removeSelectedNode()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if( node != null && parent != null )
        {
            int[] childIndices = { parent.getIndex(node)};
            Object[] removedChilds = { node};

            node.removeFromParent();

            DefaultTreeModel model = (DefaultTreeModel)getModel();
            TreePath pathToParent = new TreePath(model.getPathToRoot( parent ));
            model.nodesWereRemoved( parent, childIndices, removedChilds );
            setSelectionPath(pathToParent);
        }
    }

    /**
     * Loads a tree description file
     * @param node The content of the file will be added to this node
     * @param file This file will be read
     */
    public void loadTree(File file)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getModel().getRoot();
        Vector lines = FileAccess.readLines(file);
        DefaultMutableTreeNode actualNode = node;

        for( int i = 0; i < lines.size(); i++ )
        {
            String line = ((String)lines.elementAt(i)).trim();
            if( line.length() == 0 )
                continue;
            String name = line.substring(1, line.length());

            if( line.startsWith("=") )
                actualNode.add(new DefaultMutableTreeNode(name));
            if( line.startsWith(">") )
            {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
                actualNode.add(newNode);
                actualNode = newNode;
            }
            if( line.startsWith("<") )
            {
                actualNode = (DefaultMutableTreeNode)actualNode.getParent();
            }
        }
    }

    /**
     * Returns Vector containing all leafs of a tree. It additionally adds
     * @return Vector containing DefaultMutableTreeNodes
     */
    public Vector getAllBoards()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getModel().getRoot();
        Vector boards = new Vector();
        Enumeration e = node.depthFirstEnumeration();
        while( e.hasMoreElements() )
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
            if( child.isLeaf() )
            {
                boards.add( mixed.makeFilename((String)child.getUserObject()) );
            }
        }
        /*    for (int i = 0; i < node.getChildCount(); i ++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
                if (child.isLeaf())
                boards.add(mixed.makeFilename((String)child.getUserObject()));
                else {
                getAllLeafs(child, boards);
                }
            }*/
        return boards;
    }

    /**
     * Writes tree state to a file
     * @param tree This tree will be saved to disk
     * @param file This file will be created
     */
    public void writeTreeState(File file)
    {
        int rowCount = this.getRowCount();
        StringBuffer text = new StringBuffer();
        for( int i = 0; i < rowCount; i++ )
        {
            text.append(i).append("=").append(this.isExpanded(i)).append("\r\n");
        }
        FileAccess.writeFile(text.toString(), file);
    }

    /**
     * Reads tree state from a file
     * @param tree This tree will be changed according to the files content
     * @param file This file will be read
     */
    public void readTreeState(File file)
    {
        int i = 0;
        String value = SettingsFun.getValue(file, String.valueOf(i));
        while( !value.equals("") )
        {
            if( value.equals("true") )
                this.expandRow(i);
            i++;
            value = SettingsFun.getValue(file, String.valueOf(i));
        }
    }
}
