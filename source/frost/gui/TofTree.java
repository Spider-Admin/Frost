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

public class TofTree extends JTree
implements DragGestureListener, DragSourceListener
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    
	private static Logger logger = Logger.getLogger(TofTree.class.getName());

    private TreePath        _pathSource;                // The path being dragged
    private BufferedImage   _imgGhost;                  // The 'drag image'
    private Point           _ptOffset = new Point();    // Where, in the drag image, the mouse was clicked

    // The type of DnD object being dragged...
    public final static DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreePath");
    private DataFlavor[]    _flavors = { TREEPATH_FLAVOR };


//    final public static DataFlavor NODE_FLAVOR = new DataFlavor(TofTree.class, "Tof Tree Node");
//    static DataFlavor flavors[] = { NODE_FLAVOR };

//    protected DefaultMutableTreeNode dragNode = null;
    private DragSource dragSource = null;
    private DragSourceContext dsContext = null;
    //    private DragGestureListener dgListener = null;
    //    private DragSourceListener dsListener = null;
    private DragGestureRecognizer dgRecognizer = null;
    private DropTarget dropTarget = null;

    public TofTree(TreeNode root)
    {
        super(root);

        putClientProperty("JTree.lineStyle", "Angled"); // I like this look

        // install drag n drop support
        this.dragSource=DragSource.getDefaultDragSource();
        dgRecognizer = this.dragSource.createDefaultDragGestureRecognizer( this,
                                                                           DnDConstants.ACTION_MOVE,
                                                                           this);
        // don't act on right mouse button
        dgRecognizer.setSourceActions(dgRecognizer.getSourceActions() & ~InputEvent.BUTTON3_MASK & ~InputEvent.BUTTON2_MASK);
        dropTarget = new DropTarget(this, new CDropTargetListener());
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
        //we should make sure we aren't in edit mode
        InputEvent ievent=e.getTriggerEvent();
        if( ievent instanceof MouseEvent )
        {
            //even though I tell dgRecognizer to ignore the the right mouse button,
            // it thinks the RMB starts a drag event...argh
            if( (((MouseEvent)ievent).getModifiers() & InputEvent.BUTTON3_MASK) != 0 )
            {
                return;
            }
        }

        // begin dnd
        Point ptDragOrigin = e.getDragOrigin();
        TreePath path = getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);
        if (path == null)
            return;
        if (isRootPath(path))
            return; // Ignore user trying to drag the root node

        // Work out the offset of the drag point from the TreePath bounding rectangle origin
        Rectangle raPath = getPathBounds(path);
        _ptOffset.setLocation(ptDragOrigin.x-raPath.x, ptDragOrigin.y-raPath.y);

        // Get the cell renderer (which is a JLabel) for the path being dragged
        JLabel lbl = (JLabel) getCellRenderer().getTreeCellRendererComponent
                                (
                                    this,                                           // tree
                                    path.getLastPathComponent(),                    // value
                                    false,                                          // isSelected   (dont want a colored background)
                                    isExpanded(path),                               // isExpanded
                                    getModel().isLeaf(path.getLastPathComponent()), // isLeaf
                                    0,                                              // row          (not important for rendering)
                                    false                                           // hasFocus     (dont want a focus rectangle)
                                );
        lbl.setSize((int)raPath.getWidth(), (int)raPath.getHeight()); // <-- The layout manager would normally do this

        // Get a buffered image of the selection for dragging a ghost image
        _imgGhost = new BufferedImage((int)raPath.getWidth(), (int)raPath.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = _imgGhost.createGraphics();

        // Ask the cell renderer to paint itself into the BufferedImage
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));      // Make the image ghostlike
        lbl.paint(g2);

        // Now paint a gradient UNDER the ghosted JLabel text (but not under the icon if any)
        // Note: this will need tweaking if your icon is not positioned to the left of the text
        Icon icon = lbl.getIcon();
        int nStartOfText = (icon == null) ? 0 : icon.getIconWidth()+lbl.getIconTextGap();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f)); // Make the gradient ghostlike
        g2.setPaint(new GradientPaint(nStartOfText, 0, SystemColor.controlShadow,
                                      getWidth(),   0, new Color(255,255,255,0)));
        g2.fillRect(nStartOfText, 0, getWidth(), _imgGhost.getHeight());
        g2.dispose();

        setSelectionPath(path); // Select this path in the tree

//      Core.getOut().println("DRAGGING: "+path.getLastPathComponent());

        // Wrap the path being transferred into a Transferable object
        Transferable transferable = new CTransferableTreePath(path);

        // Remember the path being dragged (because if it is being moved, we will have to delete it later)
        _pathSource = path;

        // We pass our drag image just in case it IS supported by the platform
        e.startDrag(null, _imgGhost, new Point(5,5), transferable, this);
    }


// DropTargetListener interface object...
    class CDropTargetListener implements DropTargetListener
    {
        // Fields...
        private TreePath        _pathLast       = null;
        private Rectangle2D     _raCueLine      = new Rectangle2D.Float();
        private Rectangle2D     _raGhost        = new Rectangle2D.Float();
        private Color           _colorCueLine;
        private Point           _ptLast         = new Point();
        private javax.swing.Timer           _timerHover;
        private int             _nLeftRight     = 0;    // Cumulative left/right mouse movement
        private int             _nShift         = 0;

        // Constructor...
        public CDropTargetListener()
        {
            _colorCueLine = new Color(
                                        SystemColor.controlShadow.getRed(),
                                        SystemColor.controlShadow.getGreen(),
                                        SystemColor.controlShadow.getBlue(),
                                        64
                                      );

            // Set up a hover timer, so that a node will be automatically expanded or collapsed
            // if the user lingers on it for more than a short time
            _timerHover = new javax.swing.Timer(1000, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    _nLeftRight = 0;    // Reset left/right movement trend
                    if (isRootPath(_pathLast))
                        return; // Do nothing if we are hovering over the root node
                    if (isExpanded(_pathLast))
                        collapsePath(_pathLast);
                    else
                        expandPath(_pathLast);
                }
            });
            _timerHover.setRepeats(false);  // Set timer to one-shot mode
        }

        // DropTargetListener interface
        public void dragEnter(DropTargetDragEvent e)
        {
            if (!isDragAcceptable(e))
                e.rejectDrag();
            else
                e.acceptDrag(e.getDropAction());
        }

        public void dragExit(DropTargetEvent e)
        {
            if (!DragSource.isDragImageSupported())
            {
                repaint(_raGhost.getBounds());
            }
        }

        /**
        * This is where the ghost image is drawn
        */
        public void dragOver(DropTargetDragEvent e)
        {
            // Even if the mouse is not moving, this method is still invoked 10 times per second
            Point pt = e.getLocation();
            if (pt.equals(_ptLast))
                return;

            // Try to determine whether the user is flicking the cursor right or left
            int nDeltaLeftRight = pt.x - _ptLast.x;
            if ( (_nLeftRight > 0 && nDeltaLeftRight < 0) || (_nLeftRight < 0 && nDeltaLeftRight > 0) )
                _nLeftRight = 0;
            _nLeftRight += nDeltaLeftRight;
            _ptLast = pt;
            Graphics2D g2 = (Graphics2D) getGraphics();

            // If a drag image is not supported by the platform, then draw my own drag image
            if (!DragSource.isDragImageSupported())
            {
                paintImmediately(_raGhost.getBounds()); // Rub out the last ghost image and cue line
                // And remember where we are about to draw the new ghost image
                _raGhost.setRect(pt.x - _ptOffset.x, pt.y - _ptOffset.y, _imgGhost.getWidth(), _imgGhost.getHeight());
                g2.drawImage(_imgGhost, AffineTransform.getTranslateInstance(_raGhost.getX(), _raGhost.getY()), null);
            }
            else    // Just rub out the last cue line
                paintImmediately(_raCueLine.getBounds());

            TreePath path = getClosestPathForLocation(pt.x, pt.y);
            if (!(path == _pathLast))
            {
                _nLeftRight = 0;    // We've moved up or down, so reset left/right movement trend
                _pathLast = path;
                _timerHover.restart();
            }

            // In any case draw (over the ghost image if necessary) a cue line indicating where a drop will occur
            Rectangle raPath = getPathBounds(path);
            _raCueLine.setRect(0,  raPath.y+(int)raPath.getHeight(), getWidth(), 2);

            g2.setColor(_colorCueLine);
            g2.fill(_raCueLine);

            _nShift = 0;

            // And include the cue line in the area to be rubbed out next time
            _raGhost = _raGhost.createUnion(_raCueLine);

            // Do this if you want to prohibit dropping onto the drag source
            if (path.equals(_pathSource))
                e.rejectDrag();
            else
                e.acceptDrag(e.getDropAction());
        }

        public void dropActionChanged(DropTargetDragEvent e)
        {
            if (!isDragAcceptable(e))
                e.rejectDrag();
            else
                e.acceptDrag(e.getDropAction());
        }

        public void drop(DropTargetDropEvent e)
        {
            _timerHover.stop(); // Prevent hover timer from doing an unwanted expandPath or collapsePath

            if (!isDropAcceptable(e))
            {
                e.rejectDrop();
                return;
            }

            e.acceptDrop(e.getDropAction());

            Transferable transferable = e.getTransferable();

            DataFlavor[] flavors = transferable.getTransferDataFlavors();

            for (int i = 0; i < flavors.length; i++ )
            {
                DataFlavor flavor = flavors[i];
                if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType))
                {
                    try
                    {
                        Point pt = e.getLocation();
                        TreePath pathTarget = getClosestPathForLocation(pt.x, pt.y);
                        TreePath pathSource = (TreePath) transferable.getTransferData(flavor);

                        if( pathTarget == null || pathSource == null )
                        {
                            e.dropComplete(false);
                            return;
                        }

                        DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode)pathSource.getLastPathComponent();
                        DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode)sourceNode.getParent();

                        DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)pathTarget.getLastPathComponent();
                        DefaultMutableTreeNode newParent = (DefaultMutableTreeNode)targetNode.getParent();

                        if( !sourceNode.isLeaf() && targetNode.getParent() == sourceNode )
                        {
                            // trying to drag a folder into its own childs
                            e.dropComplete(false);
                            return;
                        }

                        // Core.getOut().println("DROPPING: "+pathSource.getLastPathComponent());
                        DefaultTreeModel model = (DefaultTreeModel)getModel();
                        TreePath pathNewChild = null;

                        if( targetNode.isLeaf() || isCollapsed(pathTarget) )
                        {
                            // collapsed tree node or leaf
                            // dropped on a leaf, insert into leaf's parent AFTER leaf
                            int idx = newParent.getIndex(targetNode);
                            if( idx < 0 )
                            {
                                logger.warning("child not found in parent!!!");
                                e.dropComplete(false);
                                return;
                            }
                            else
                            {
                                idx++; // insert AFTER targetNode

                                // remove node from oldParent ...
                                Object[] removedChilds = { sourceNode };
                                int[] childIndices = { oldParent.getIndex(sourceNode) };
                                sourceNode.removeFromParent();
                                model.nodesWereRemoved( oldParent, childIndices, removedChilds );

                                // ... and insert into newParent
                                if( idx >= newParent.getChildCount() )
                                {
                                    newParent.add( sourceNode );
                                    int insertedIndex[] = { newParent.getChildCount()-1 };
                                    model.nodesWereInserted( newParent, insertedIndex );
                                }
                                else
                                {
                                    newParent.insert(sourceNode, idx);
                                    int insertedIndex[] = { idx };
                                    model.nodesWereInserted( newParent, insertedIndex );
                                }
                            }
                        }
                        else
                        {
                            // expanded node, insert UNDER the node (before first child)
                            // remove node from oldParent ...
                            Object[] removedChilds = { sourceNode };
                            int[] childIndices = { oldParent.getIndex(sourceNode) };
                            sourceNode.removeFromParent();
                            model.nodesWereRemoved( oldParent, childIndices, removedChilds );
                            // ... and add to newParent
                            targetNode.insert( sourceNode, 0 );
                            int insertedIndex[] = { 0 };
                            model.nodesWereInserted( targetNode, insertedIndex );
                        }

                        if (pathNewChild != null)
                            setSelectionPath(pathNewChild); // Mark this as the selected path in the tree
                        break; // No need to check remaining flavors
                    }
                    catch (UnsupportedFlavorException ufe)
                    {
						logger.log(Level.SEVERE, "Exception thrown in drop(DropTargetDropEvent e)", ufe);
                        e.dropComplete(false);
                        return;
                    }
                    catch (IOException ioe)
                    {
						logger.log(Level.SEVERE, "Exception thrown in drop(DropTargetDropEvent e)", ioe);
                        e.dropComplete(false);
                        return;
                    }
                }
            }
            e.dropComplete(true);
        }

        // Helpers...
        public boolean isDragAcceptable(DropTargetDragEvent e)
        {
            // Only accept COPY or MOVE gestures (ie LINK is not supported)
            if ((e.getDropAction() & DnDConstants.ACTION_MOVE) == 0)
                return false;

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(TREEPATH_FLAVOR))
                return false;

            // Do this if you want to prohibit dropping onto the drag source...
            Point pt = e.getLocation();
            TreePath path = getClosestPathForLocation(pt.x, pt.y);
            if(path == null || path.equals(_pathSource))
                return false;

            return true;
        }

        public boolean isDropAcceptable(DropTargetDropEvent e)
        {
            // Only accept COPY or MOVE gestures (ie LINK is not supported)
            if ((e.getDropAction() & DnDConstants.ACTION_MOVE) == 0)
                return false;

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(TREEPATH_FLAVOR))
                return false;

            // Do this if you want to prohibit dropping onto the drag source...
            Point pt = e.getLocation();
            TreePath path = getClosestPathForLocation(pt.x, pt.y);
//          TreePath path = getPathForLocation(pt.x, pt.y);
            if( path == null || path.equals(_pathSource))
                return false;

            return true;
        }
    }

    // DragSourceListener interface methods
    public void dragDropEnd(DragSourceDropEvent e)
    {
        this.repaint();
    }
    public void dragEnter(DragSourceDragEvent e) {}
    public void dragOver(DragSourceDragEvent e) {}
    public void dropActionChanged(DragSourceDragEvent e) {}
    public void dragExit(DragSourceEvent e) {}



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
            logger.warning("boards.xml file not found, reading default file (will be saved to boards.xml on exit).");
            boardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml.default";
        }
        return xmlio.loadBoardTree( this, boardIniFilename );
    }

    /**
     * Save TOF tree's content to a file
     * @param node Save this nodes content
     * @param file The destination file
     */
    public boolean saveTree()
    {
        TofTreeXmlIO xmlio = new TofTreeXmlIO();
        String boardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml";
        File check = new File( boardIniFilename );
        if( check.exists() )
        {
            // rename old file to .bak, overwrite older .bak
            String bakBoardIniFilename = frame1.frostSettings.getValue("config.dir") + "boards.xml.bak";
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
    public void createNewBoard(Frame parent)
    {
        String nodeName = null;
        do
        {
            Object nodeNameOb = JOptionPane.showInputDialog (parent,
                                                             LangRes.getString ("New Node Name"),
                                                             LangRes.getString ("New Node Name"),
                                                             JOptionPane.QUESTION_MESSAGE, null, null,
                                                             "newboard");

            nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString ());

            if( nodeName == null )
                return; // cancelled

            if( getBoardByName( nodeName ) != null )
            {
                JOptionPane.showMessageDialog(this, "You already have a board with name '"+nodeName+"'!\nPlease choose a new name.");
                nodeName = ""; // loop again
            }
        } while( nodeName.length()==0 );

        FrostBoardObject newBoard = new FrostBoardObject(nodeName);
        addNodeToTree( newBoard );
        // maybe this boardfolder already exists, scan for new messages
        TOF.initialSearchNewMessages( newBoard );
    }
    
    /**
     * Checks if board is already existent, adds board to board tree.
     */
    public void addNewBoard(String bname, String bpubkey, String bprivkey)
    {
        if( getBoardByName( bname ) != null )
        {
            int answer = JOptionPane.showConfirmDialog(this, 
                            "You already have a board with name '"+bname+
                            "'!\nDo you really want to overwrite it?\n(This will not delete messages)", 
                            "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
            if( answer == JOptionPane.NO_OPTION )
            {
                return; // do not add
            }
        }
        FrostBoardObject newBoard = new FrostBoardObject(bname, bpubkey, bprivkey);
        addNodeToTree( newBoard );
        // maybe this boardfolder already exists, scan for new messages
        TOF.initialSearchNewMessages( newBoard );
    }

    /**
     * Checks if board is already existent, adds board to board tree.
     */
    public void addNewBoard(FrostBoardObject fbobj)
    {
        addNewBoard( fbobj.getBoardName(),
                     fbobj.getPublicKey(),
                     fbobj.getPrivateKey());
    }
    
    /**
     * Opens dialog, gets new name for folder, checks for double names, adds node to tree
     */
    public void createNewFolder(Frame parent)
    {
        String nodeName = null;
        do
        {
            Object nodeNameOb = JOptionPane.showInputDialog(parent,
                                                            "Please enter a name for the new folder:",
                                                            "New Folder Name",
                                                            JOptionPane.QUESTION_MESSAGE, null, null,
                                                            "newfolder");

            nodeName = ((nodeNameOb == null) ? null : nodeNameOb.toString ());

            if( nodeName == null )
                return; // cancelled

        } while( nodeName.length()==0 );

        addNodeToTree( new FrostBoardObject(nodeName, true) );
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


    private boolean isRootPath(TreePath path)
    {
        return isRootVisible() && getRowForPath(path) == 0;
    }

    /**
    * This represents a TreePath (a node in a JTree) that can be transferred between a drag source and a drop target.
    */
    class CTransferableTreePath implements Transferable
    {
        private TreePath        _path;
        /**
        * Constructs a transferrable tree path object for the specified path.
        */
        public CTransferableTreePath(TreePath path)
        {
            _path = path;
        }

        // Transferable interface methods...
        public DataFlavor[] getTransferDataFlavors()
        {
            return _flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return java.util.Arrays.asList(_flavors).contains(flavor);
        }

        public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (flavor.isMimeTypeEqual(TREEPATH_FLAVOR.getMimeType()))
                return _path;
            else
                throw new UnsupportedFlavorException(flavor);
        }
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
