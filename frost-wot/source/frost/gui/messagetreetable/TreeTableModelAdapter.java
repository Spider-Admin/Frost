/*
 * Copyright 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package frost.gui.messagetreetable;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.*;

/**
 * This wrapper class takes a TreeTableModel and implements the table model interface. The implementation is trivial,
 * with all of the event dispatching support provided by the superclass: the AbstractTableModel.
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTableModelAdapter extends AbstractTableModel {

    final JTree tree;
    final TreeTableModel treeTableModel;

    private int collapsedToRow = -1;

    public TreeTableModelAdapter(final TreeTableModel treeTableModel, final JTree tree) {
        this.tree = tree;
        this.treeTableModel = treeTableModel;

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {

            public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
            }

            // remember child rows to fire a tableRowDeleted event later in collapse listener
            public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
                final DefaultMutableTreeNode collapsedNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                // X new rows are below the expanded node
                final int nodeRow = MainFrame.getInstance().getMessageTreeTable().getRowForNode(collapsedNode);
                final int fromRow = nodeRow + 1;
                int toRow = nodeRow;
                if( collapsedNode.getChildCount() > 0 ) {
                    toRow += collapsedNode.getChildCount();
                }

                final Enumeration<TreePath> e = MainFrame.getInstance().getMessageTreeTable().getTree().getExpandedDescendants(event.getPath());
                // count childs of this tree, and childs of all expanded subchilds
                while( e.hasMoreElements() ) {
                    final DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement().getLastPathComponent();
                    toRow += n.getChildCount();
                }
                if( toRow < fromRow ) {
                    toRow = fromRow;
                }
                collapsedToRow = toRow;
            }
        });

        tree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(final TreeExpansionEvent event) {
                final DefaultMutableTreeNode expandedNode = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
                // X new rows are below the expanded node
                final int nodeRow = MainFrame.getInstance().getMessageTreeTable().getRowForNode(expandedNode);
                final int fromRow = nodeRow + 1;
                int toRow = nodeRow;
                if( expandedNode.getChildCount() > 0 ) {
                    toRow += expandedNode.getChildCount();
                }
                // check if new childs are expanded too
                final Enumeration<TreePath> e = MainFrame.getInstance().getMessageTreeTable().getTree().getExpandedDescendants(event.getPath());
                // count childs of this tree, and childs of all expanded subchilds
                while(e.hasMoreElements()) {
                    final DefaultMutableTreeNode n = (DefaultMutableTreeNode)(e.nextElement()).getLastPathComponent();
                    toRow += n.getChildCount();
                }
                if( toRow < fromRow ) {
                    toRow = fromRow;
                }
//                System.out.println("treeExpanded, fromRow="+fromRow+", toRow="+toRow);
                fireTableRowsInserted(fromRow, toRow);
            }

            // fire table event, use toRow computed in treeWillCollpaseListener
            public void treeCollapsed(final TreeExpansionEvent event) {
//                System.out.println("treeCollapsed");
                final DefaultMutableTreeNode collapsedNode = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
                final int nodeRow = MainFrame.getInstance().getMessageTreeTable().getRowForNode(collapsedNode);
                final int fromRow = nodeRow + 1;
                final int toRow = collapsedToRow;
                fireTableRowsDeleted(fromRow, toRow);
            }
        });

        // Installs a TreeModelListener that can update the table when
        // the tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeModelListener(new TreeModelListener() {

            public void treeNodesChanged(final TreeModelEvent e) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
                final int[] childIndices = e.getChildIndices();
                // we always insert only one child at a time
                if( childIndices.length != 1 ) {
                    System.out.println("****** FIXME1: more than 1 child: "+childIndices.length+" ********");
                }
                // update the row for this node
                final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(childIndices[0]);
                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(childNode);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
//                        System.out.println("treeNodesChanged: "+row);
                        fireTableRowsUpdated(row, row);
                    }
                });
            }

            public void treeNodesInserted(final TreeModelEvent e) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
                final int[] childIndices = e.getChildIndices();
                // we always insert only one child at a time
                if( childIndices.length != 1 ) {
                    System.out.println("****** FIXME2: more than 1 child: "+childIndices.length+" ********");
                }
                // compute row that was inserted
//                System.out.println("a="+MainFrame.getInstance().getMessageTreeTable().getRowForNode(node));
//                System.out.println("b="+childIndices[0]);
//                System.out.println("c="+node);
                // FIXME: fails again: 1st child of a thread selected, new message arrived and replaced the selected
                //        message! new message was shown in preview and marked unread!
                final int offset = 0;
//                if( childIndices[0] == 0 ) {
//                    offset = 0;
//                } else {
//                    offset = 1;
//                }
                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(node) + offset + childIndices[0];
//                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(node) + 1 + childIndices[0];
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
//                        System.out.println("treeNodesInserted: "+row);
                        fireTableRowsInserted(row, row);
                    }
                });
            }

            public void treeNodesRemoved(final TreeModelEvent e) {
                System.out.println("treeNodesRemoved");
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
                final int[] childIndices = e.getChildIndices();
                // we always remove only one child at a time
                if( childIndices.length != 1 ) {
                    System.out.println("****** FIXME3: more than 1 child: "+childIndices.length+" ********");
                }
                // ATTN: will getRowForNode work if node was already removed from tree?
                //  -> we currently don't remove nodes from tree anywhere in Frost
                final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(childIndices[0]);
                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(childNode);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        fireTableRowsDeleted(row, row);
                    }
                });
            }

            public void treeStructureChanged(final TreeModelEvent e) {
//                delayedFireTableDataChanged();
                fireTableDataChanged();
            }
        });
    }

    // Wrappers, implementing TableModel interface.

    public int getColumnCount() {
        return treeTableModel.getColumnCount();
    }

    @Override
    public String getColumnName(final int column) {
        return treeTableModel.getColumnName(column);
    }

    @Override
    public Class getColumnClass(final int column) {
        return treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    protected Object nodeForRow(final int row) {
        final TreePath treePath = tree.getPathForRow(row);
        if( treePath != null ) {
            return treePath.getLastPathComponent();
        } else {
            return null;
        }
    }

    public Object getValueAt(final int row, final int column) {
        return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    public Object getRow(final int row) {
        return nodeForRow(row);
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return treeTableModel.isCellEditable(nodeForRow(row), column);
    }

    @Override
    public void setValueAt(final Object value, final int row, final int column) {
        treeTableModel.setValueAt(value, nodeForRow(row), column);
    }

//    /**
//     * Invokes fireTableDataChanged after all the pending events have been processed. SwingUtilities.invokeLater is used
//     * to handle this.
//     */
//    protected void delayedFireTableDataChanged() {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                fireTableDataChanged();
//            }
//        });
//    }
}
