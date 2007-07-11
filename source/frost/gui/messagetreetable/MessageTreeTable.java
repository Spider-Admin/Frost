/*
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.*;
import frost.fcp.*;
import frost.fileTransfer.common.*;
import frost.messages.*;
import frost.util.gui.*;

/**
 * This example shows how to create a simple JTreeTable component, 
 * by using a JTree as a renderer (and editor) for the cells in a 
 * particular column in the JTable.  
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class MessageTreeTable extends JTable implements PropertyChangeListener {

    /** A subclass of JTree. */
    protected TreeTableCellRenderer tree;

    protected Border borderUnreadAndMarkedMsgsInThread = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue),    // outside
            BorderFactory.createMatteBorder(0, 2, 0, 0, Color.green) ); // inside
    protected Border borderMarkedMsgsInThread = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 2, 0, 0),                // outside
            BorderFactory.createMatteBorder(0, 2, 0, 0, Color.green) ); // inside
    protected Border borderUnreadMsgsInThread = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue),    // outside
            BorderFactory.createEmptyBorder(0, 2, 0, 0) );              // inside
    protected Border borderEmpty = BorderFactory.createEmptyBorder(0, 4, 0, 0);

    private StringCellRenderer stringCellRenderer = new StringCellRenderer();
    private BooleanCellRenderer booleanCellRenderer = new BooleanCellRenderer();
    
    private ImageIcon flaggedIcon = new ImageIcon(getClass().getResource("/data/flagged.gif"));
    private ImageIcon starredIcon = new ImageIcon(getClass().getResource("/data/starred.gif"));

    private ImageIcon messageDummyIcon = new ImageIcon(getClass().getResource("/data/messagedummyicon.gif"));
    private ImageIcon messageNewIcon = new ImageIcon(getClass().getResource("/data/messagenewicon.gif"));
    private ImageIcon messageReadIcon = new ImageIcon(getClass().getResource("/data/messagereadicon.gif"));
    private ImageIcon messageNewRepliedIcon = new ImageIcon(getClass().getResource("/data/messagenewrepliedicon.gif"));
    private ImageIcon messageReadRepliedIcon = new ImageIcon(getClass().getResource("/data/messagereadrepliedicon.gif"));

    private boolean showColoredLines;
    
    public MessageTreeTable(TreeTableModel treeTableModel) {
    	super();
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        
        Core.frostSettings.addPropertyChangeListener(SettingsClass.SHOW_COLORED_ROWS, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_LIST_FONT_NAME, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_LIST_FONT_SIZE, this);
        Core.frostSettings.addPropertyChangeListener(SettingsClass.MESSAGE_LIST_FONT_STYLE, this);

    	// Creates the tree. It will be used as a renderer and editor. 
    	tree = new TreeTableCellRenderer(treeTableModel);
    
    	// Installs a tableModel representing the visible rows in the tree. 
    	super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

    	// Forces the JTable and JTree to share their row selection models. 
    	ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
    	tree.setSelectionModel(selectionWrapper);
    	setSelectionModel(selectionWrapper.getListSelectionModel());
        
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
    
    	// Installs the tree editor renderer and editor. 
    	setDefaultRenderer(TreeTableModel.class, tree); 
    	setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
        
        // install cell renderer
        setDefaultRenderer(String.class, stringCellRenderer);
        setDefaultRenderer(Boolean.class, booleanCellRenderer);
        
        // install table header renderer
        MessageTreeTableHeader hdr = new MessageTreeTableHeader(this);
        setTableHeader(hdr);

    	// No grid.
    	setShowGrid(false);
    
    	// No intercell spacing
    	setIntercellSpacing(new Dimension(0, 0));	
    
    	// And update the height of the trees row to match that of the table.
    	if (tree.getRowHeight() < 1) {
    	    // Metal looks better like this.
    	    setRowHeight(20);
    	}
    }

    /**
     * Overwritten to forward LEFT and RIGHT cursor to the tree to allow JTree-like expand/collapse of nodes.
     */
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if( !pressed ) {
            return super.processKeyBinding(ks, e, condition, pressed);
        }

        if( e.getKeyCode() == KeyEvent.VK_LEFT ) {
            getTree().processKeyEvent(e);
        } else if( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
            getTree().processKeyEvent(e);
        } else {
            return super.processKeyBinding(ks, e, condition, pressed);
        }
        return true;
    }
    
    public void setNewRootNode(TreeNode t) {
        ((DefaultTreeModel)tree.getModel()).setRoot(t);
    }
    
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll(final boolean expand) {
        final TreeNode root = (TreeNode)tree.getModel().getRoot();
        if( SwingUtilities.isEventDispatchThread() ) {
            // Traverse tree from root
            expandAll(new TreePath(root), expand);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Traverse tree from root
                    expandAll(new TreePath(root), expand);
                }
            });
        }
    }
    
    public void expandThread(final boolean expand, FrostMessageObject msg) {
        if( msg == null ) {
            return;
        }
        // find msgs rootmsg
        final FrostMessageObject threadRootMsg = msg.getThreadRootMessage();
        if( threadRootMsg == null ) {
            return;
        }
        if( SwingUtilities.isEventDispatchThread() ) {
            expandAll(new TreePath(threadRootMsg.getPath()), expand);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    expandAll(new TreePath(threadRootMsg.getPath()), expand);
                }
            });
        }
    }

    private void expandAll(TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
    
        // Expansion or collapse must be done bottom-up
        // never collapse the invisible rootnode!
        if( node.getParent() == null ) {
            expand = true;
        }
        
        if (expand) {
            if( !tree.isExpanded(parent) ) {
                tree.expandPath(parent);
            }
        } else {
            if( !tree.isCollapsed(parent) ) {
                tree.collapsePath(parent);
            }
        }
    }
    
    public void expandNode(final DefaultMutableTreeNode n) {
        if( SwingUtilities.isEventDispatchThread() ) {
            expandAll(new TreePath(n.getPath()), true);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    expandAll(new TreePath(n.getPath()), true);
                }
            });
        }
    }

    /**
     * Overridden to message super and forward the method to the tree.
     * Since the tree is not actually in the component hierarchy it will
     * never receive this unless we forward it in this manner.
     */
    public void updateUI() {
    	super.updateUI();
    	if(tree != null) {
    	    tree.updateUI();
    	    // Do this so that the editor is referencing the current renderer
    	    // from the tree. The renderer can potentially change each time laf changes.
    	    // setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
    	}
	    // Use the tree's default foreground and background colors in the table. 
        LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
    }

    /**
     * Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
     * resize the editor. The UI currently uses different techniques to 
     * paint the renderers and editors; overriding setBounds() below 
     * is not the right thing to do for an editor. Returning -1 for the 
     * editing row in this case, ensures the editor is never painted. 
     */
    public int getEditingRow() {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 :
	        editingRow;  
    }

    /**
     * Returns the actual row that is editing as <code>getEditingRow</code>
     * will always return -1.
     */
    private int realEditingRow() {
        return editingRow;
    }

    /**
     * This is overridden to invoke super's implementation, and then,
     * if the receiver is editing a Tree column, the editor's bounds is
     * reset. The reason we have to do this is because JTable doesn't
     * think the table is being edited, as <code>getEditingRow</code> returns
     * -1, and therefore doesn't automatically resize the editor for us.
     */
    public void sizeColumnsToFit(int resizingColumn) { 
        super.sizeColumnsToFit(resizingColumn);
    	if (getEditingColumn() != -1 && getColumnClass(editingColumn) ==
    	    TreeTableModel.class) {
    	    Rectangle cellRect = getCellRect(realEditingRow(), getEditingColumn(), false);
            Component component = getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
    	}
    }

    /**
     * Overridden to pass the new rowHeight to the tree.
     */
    public void setRowHeight(int rowHeight) { 
        super.setRowHeight(rowHeight); 
        if (tree != null && tree.getRowHeight() != rowHeight) {
            tree.setRowHeight(getRowHeight()); 
        }
    }

    /**
     * Returns the tree that is being shared between the model.
     */
    public TreeTableCellRenderer getTree() {
        return tree;
    }
    
    public int getRowForNode(DefaultMutableTreeNode n) {
        if(n.isRoot()) {
            return 0;
        }
        TreePath tp = new TreePath(n.getPath());
        return tree.getRowForPath(tp);
    }

    /**
     * Overridden to invoke repaint for the particular location if
     * the column contains the tree. This is done as the tree editor does
     * not fill the bounds of the cell, we need the renderer to paint
     * the tree in the background, and then draw the editor over it.
     */
    public boolean editCellAt(int row, int column, EventObject e){
    	boolean retValue = super.editCellAt(row, column, e);
    	if (retValue && getColumnClass(column) == TreeTableModel.class) {
    	    repaint(getCellRect(row, column, false));
    	}
    	return retValue;
    }

    /**
     * A TreeCellRenderer that displays a JTree.
     */
    public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    	/** Last table/tree row asked to renderer. */
    	protected int visibleRow;

        private Font boldFont = null;
        private Font normalFont = null;
        private boolean isDeleted = false;
        
        private String toolTipText = null;
    
    	public TreeTableCellRenderer(TreeModel model) {
    	    super(model);
            Font baseFont = MessageTreeTable.this.getFont();
            normalFont = baseFont.deriveFont(Font.PLAIN);
            boldFont = baseFont.deriveFont(Font.BOLD);
            
            setCellRenderer(new OwnTreeCellRenderer());
            
            if( getUI() instanceof BasicTreeUI ) {
                BasicTreeUI treeUI = (BasicTreeUI)getUI();
//                System.out.println("1:"+treeUI.getLeftChildIndent()); // default 7
//                System.out.println("2:"+treeUI.getRightChildIndent());// default 13
                treeUI.setLeftChildIndent(6);
                treeUI.setRightChildIndent(10);
            }
    	}
        
        public void fontChanged(Font font) {
            normalFont = font.deriveFont(Font.PLAIN);
            boldFont = font.deriveFont(Font.BOLD);
        }
        
        public void processKeyEvent(KeyEvent e) {
            super.processKeyEvent(e);
        }
        
        class OwnTreeCellRenderer extends DefaultTreeCellRenderer {
            int treeWidth;
            public OwnTreeCellRenderer() {
                super();
                setVerticalAlignment(CENTER);
            }
            public Component getTreeCellRendererComponent(
                    JTree lTree, 
                    Object value, 
                    boolean sel, 
                    boolean expanded,
                    boolean leaf, 
                    int row, 
                    boolean lHasFocus) 
            {
                treeWidth = lTree.getWidth();
                return super.getTreeCellRendererComponent(lTree, value, sel, expanded, leaf, row, lHasFocus);
            }
            public void paint(Graphics g) {
                setSize(new Dimension(treeWidth - this.getBounds().x, this.getSize().height));
                super.paint(g);
                if(isDeleted) {
                    Dimension size = getSize();
                    g.drawLine(0, size.height / 2, size.width, size.height / 2);
                }
            }
        }
    
    	/**
    	 * updateUI is overridden to set the colors of the Tree's renderer
    	 * to match that of the table.
    	 */
    	public void updateUI() {
    	    super.updateUI();
    	    // Make the tree's cell renderer use the table's cell selection
    	    // colors. 
    	    TreeCellRenderer tcr = getCellRenderer();
    	    if (tcr instanceof DefaultTreeCellRenderer) {
        		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr); 
        		// For 1.1 uncomment this, 1.2 has a bug that will cause an
        		// exception to be thrown if the border selection color is null.
        		// dtcr.setBorderSelectionColor(null);
        		dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
        		dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
    	    }
    	}
        
        public void setDeleted(boolean value) {
            isDeleted = value;
        }
    
    	/**
    	 * Sets the row height of the tree, and forwards the row height to
    	 * the table.
    	 */
    	public void setRowHeight(int rowHeight) { 
    	    if (rowHeight > 0) {
        		super.setRowHeight(rowHeight); 
        		if (MessageTreeTable.this != null &&
        		    MessageTreeTable.this.getRowHeight() != rowHeight) {
        		    MessageTreeTable.this.setRowHeight(getRowHeight()); 
        		}
    	    }
    	}
        
    	/**
    	 * This is overridden to set the height to match that of the JTable.
    	 */
    	public void setBounds(int x, int y, int w, int h) {
    	    super.setBounds(x, 0, w, MessageTreeTable.this.getHeight());
    	}
    
    	/**
    	 * Sublcassed to translate the graphics such that the last visible
    	 * row will be drawn at 0,0.
    	 */
    	public void paint(Graphics g) {
    	    g.translate(0, -visibleRow * getRowHeight());
    	    super.paint(g);
    	}

    	/**
    	 * TreeCellRenderer method. Overridden to update the visible row.
    	 */
    	public Component getTableCellRendererComponent(JTable table,
    						       Object value,
    						       boolean isSelected,
    						       boolean hasFocus,
    						       int row, int column) {
    	    Color background;
    	    Color foreground;
            
            TreeTableModelAdapter model = (TreeTableModelAdapter)MessageTreeTable.this.getModel();
            
            FrostMessageObject msg = (FrostMessageObject)model.getRow(row);
    
            // first set font, bold for new msg or normal
            if (msg.isNew()) {
                setFont(boldFont);
            } else {
                setFont(normalFont);
            }
            
            // now set foreground color
            if( msg.getRecipientName() != null && msg.getRecipientName().length() > 0) {
                foreground = Color.RED;
            } else if (msg.containsAttachments()) {
                foreground = Color.BLUE;
            } else {
                foreground = Color.BLACK;
            }

            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                background = newBackground;
            } else {
                background = table.getSelectionBackground();
                foreground = table.getSelectionForeground();
            }

            setDeleted(msg.isDeleted());
    
    	    visibleRow = row;
    	    setBackground(background);
    
    	    TreeCellRenderer tcr = getCellRenderer();
    	    if (tcr instanceof DefaultTreeCellRenderer) {

        		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr); 
        		if (isSelected) {
        		    dtcr.setTextSelectionColor(foreground);
        		    dtcr.setBackgroundSelectionColor(background);
        		} else {
        		    dtcr.setTextNonSelectionColor(foreground);
        		    dtcr.setBackgroundNonSelectionColor(background);
        		}

                dtcr.setBorder(null);
                if( ((FrostMessageObject)msg.getParent()).isRoot() ) {
                    boolean[] hasUnreadOrMarked = msg.hasUnreadOrMarkedChilds();
                    boolean hasUnread = hasUnreadOrMarked[0];
                    boolean hasMarked = hasUnreadOrMarked[1];
                    if( hasUnread && !hasMarked ) {
                        // unread and no marked
                        dtcr.setBorder(borderUnreadMsgsInThread);
                    } else if( !hasUnread && hasMarked ) {
                        // no unread and marked
                        dtcr.setBorder(borderMarkedMsgsInThread);
                    } else if( !hasUnread && !hasMarked ) {
                        // nothing
                        dtcr.setBorder(borderEmpty);
                    } else {
                        // both
                        dtcr.setBorder(borderUnreadAndMarkedMsgsInThread);
                    }
                }
                
                ImageIcon icon;
                if( msg.isDummy() ) {
                    icon = messageDummyIcon;
//                    dtcr.setToolTipText(null);
                    if( msg.getSubject() != null && msg.getSubject().length() > 0 ) {
                        setToolTipText(msg.getSubject());
                    } else {
                        setToolTipText(null);
                    }
                } else {
                    if( msg.isNew() ) {
                        if( msg.isReplied() ) {
                            icon = messageNewRepliedIcon;
                        } else {
                            icon = messageNewIcon;
                        }
                    } else {
                        if( msg.isReplied() ) {
                            icon = messageReadRepliedIcon;
                        } else {
                            icon = messageReadIcon;
                        }
                    }
//                    dtcr.setToolTipText(msg.getSubject());
                    setToolTipText(null);
                    setToolTipText(msg.getSubject());
                }
                dtcr.setIcon(icon);
                dtcr.setLeafIcon(icon);
                dtcr.setOpenIcon(icon);
                dtcr.setClosedIcon(icon);
    	    }
            
    	    return this;
    	}
        public void setToolTipText(String t) {
            toolTipText = t;
        }
        /**
         * Override to always return a tooltext for the table column.
         */
        public String getToolTipText(MouseEvent event) {
            return toolTipText;
        }
        }
    
        /**
         * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
         * to listen for changes in the ListSelectionModel it maintains. Once
         * a change in the ListSelectionModel happens, the paths are updated
         * in the DefaultTreeSelectionModel.
         */
        class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel { 
    	/** Set to true when we are updating the ListSelectionModel. */
    	protected boolean         updatingListSelectionModel;
    
    	public ListToTreeSelectionModelWrapper() {
    	    super();
    	    getListSelectionModel().addListSelectionListener(createListSelectionListener());
    	}
        
    	/**
    	 * Returns the list selection model. ListToTreeSelectionModelWrapper
    	 * listens for changes to this model and updates the selected paths
    	 * accordingly.
    	 */
    	ListSelectionModel getListSelectionModel() {
    	    return listSelectionModel; 
    	}
    
    	/**
    	 * This is overridden to set <code>updatingListSelectionModel</code>
    	 * and message super. This is the only place DefaultTreeSelectionModel
    	 * alters the ListSelectionModel.
    	 */
    	public void resetRowSelection() {
    	    if(!updatingListSelectionModel) {
        		updatingListSelectionModel = true;
        		try {
    //                super.resetRowSelection();
        		}
        		finally {
        		    updatingListSelectionModel = false;
        		}
    	    }
    	    // Notice how we don't message super if
    	    // updatingListSelectionModel is true. If
    	    // updatingListSelectionModel is true, it implies the
    	    // ListSelectionModel has already been updated and the
    	    // paths are the only thing that needs to be updated.
    	}
    
    	/**
    	 * Creates and returns an instance of ListSelectionHandler.
    	 */
    	protected ListSelectionListener createListSelectionListener() {
    	    return new ListSelectionHandler();
    	}
    
    	/**
    	 * If <code>updatingListSelectionModel</code> is false, this will
    	 * reset the selected paths from the selected rows in the list
    	 * selection model.
    	 */
    	protected void updateSelectedPathsFromSelectedRows() {
    	    if(!updatingListSelectionModel) {
        		updatingListSelectionModel = true;
        		try {
        		    // This is way expensive, ListSelectionModel needs an enumerator for iterating
        		    int min = listSelectionModel.getMinSelectionIndex();
        		    int max = listSelectionModel.getMaxSelectionIndex();
        
        		    clearSelection();
        		    if(min != -1 && max != -1) {
            			for(int counter = min; counter <= max; counter++) {
            			    if(listSelectionModel.isSelectedIndex(counter)) {
                				TreePath selPath = tree.getPathForRow(counter);
                				if(selPath != null) {
                				    addSelectionPath(selPath);
                				}
            			    }
            			}
        		    }
        		}
        		finally {
        		    updatingListSelectionModel = false;
        		}
    	    }
    	}
    
    	/**
    	 * Class responsible for calling updateSelectedPathsFromSelectedRows
    	 * when the selection of the list changse.
    	 */
    	class ListSelectionHandler implements ListSelectionListener {
    	    public void valueChanged(ListSelectionEvent e) {
    	        updateSelectedPathsFromSelectedRows();
    	    }
    	}
    }
    
    private class BooleanCellRenderer extends JLabel implements TableCellRenderer {
        
        public BooleanCellRenderer() {
            super();
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }
        
        public void paintComponent (Graphics g) {
            Dimension size = getSize();
            g.setColor(getBackground());
            g.fillRect(0, 0, size.width, size.height);
            super.paintComponent(g);
        }

        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) 
        {
            boolean val = ((Boolean)value).booleanValue();
            
            // get the original model column index (maybe columns were reordered by user)
            TableColumn tableColumn = getColumnModel().getColumn(column);
            column = tableColumn.getModelIndex();
            
            if( column == 0 ) {
                if( val ) {
                    setIcon(flaggedIcon);
                } else {
                    setIcon(null);
                }
            } else if( column == 1 ) {
                if( val ) {
                    setIcon(starredIcon);
                } else {
                    setIcon(null);
                }
            }
            
            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }

            return this;
        }        
    }
    
    public void setFont(Font font) {
        super.setFont(font);

        if( stringCellRenderer != null ) {
            stringCellRenderer.fontChanged(font);
        }
        if( tree != null ) {
            tree.fontChanged(font);
        }
        repaint();
    }
    
    /**
     * This renderer renders rows in different colors.
     * New messages gets a bold look, messages with attachments a blue color.
     * Encrypted messages get a red color, no matter if they have attachments.
     */
    private class StringCellRenderer extends DefaultTableCellRenderer {

        private Font boldFont;
        private Font boldItalicFont;
        private Font normalFont;
        private boolean isDeleted = false;
        private final Color col_good    = new Color(0x00, 0x80, 0x00);
        private final Color col_check   = new Color(0xFF, 0xCC, 0x00);
        private final Color col_observe = new Color(0x00, 0xD0, 0x00);
        private final Color col_bad     = new Color(0xFF, 0x00, 0x00);
        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);

        public StringCellRenderer() {
            setVerticalAlignment(CENTER);
            Font baseFont = MessageTreeTable.this.getFont();
            fontChanged( baseFont );
        }

        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            if(isDeleted) {
                Dimension size = getSize();
                g.drawLine(0, size.height / 2, size.width, size.height / 2);
            }
        }
        
        public void fontChanged(Font font) {
            normalFont = font.deriveFont(Font.PLAIN);
            boldFont = font.deriveFont(Font.BOLD);
            boldItalicFont = font.deriveFont(Font.BOLD|Font.ITALIC);
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            super.getTableCellRendererComponent(table, value, isSelected, /*hasFocus*/ false, row, column);
            
            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }

            // setup defaults
            setAlignmentY(CENTER_ALIGNMENT);
            setFont(normalFont);
            if (!isSelected) {
                setForeground(Color.BLACK);
            }
            setToolTipText(null);
            setBorder(null);
            setHorizontalAlignment(SwingConstants.LEFT);

            TreeTableModelAdapter model = (TreeTableModelAdapter) getModel();
            
            Object obj = model.getRow(row);
            if( !(obj instanceof FrostMessageObject) ) {
                return this; // paranoia
            }
            
            FrostMessageObject msg = (FrostMessageObject) obj;

            // get the original model column index (maybe columns were reordered by user)
            column = getColumnModel().getColumn(column).getModelIndex();

            // do nice things for FROM and SIG column
            if( column == 3 ) {
                // FROM
                // first set font, bold for new msg or normal
                if (msg.isNew()) {
                    setFont(boldFont);
                }
                // now set color
                if (!isSelected) {
                    if( msg.getRecipientName() != null && msg.getRecipientName().length() > 0) {
                        setForeground(Color.RED);
                    } else if (msg.containsAttachments()) {
                        setForeground(Color.BLUE);
                    }
                }
                if( !msg.isDummy() ) {
                    setToolTipText((String)value);
                }
            } else if( column == 4 ) {
                // index column, right aligned
                setHorizontalAlignment(SwingConstants.RIGHT);
                // col is right aligned, give some space to next column
                setBorder(border);
            } else if( column == 5 ) {
                // SIG
                // state == good/bad/check/observe -> bold and coloured
                final Font f;
                if( FcpHandler.isFreenet05() ) {
                    f = boldFont;
                } else if( msg.isSignatureStatusVERIFIED_V2() ) {
                    f = boldFont;
                } else {
                    f = boldItalicFont;
                }
                if( msg.isMessageStatusGOOD() ) {
                    setFont(f);
                    setForeground(col_good);
                } else if( msg.isMessageStatusCHECK() ) {
                    setFont(f);
                    setForeground(col_check);
                } else if( msg.isMessageStatusOBSERVE() ) {
                    setFont(f);
                    setForeground(col_observe);
                } else if( msg.isMessageStatusBAD() ) {
                    setFont(f);
                    setForeground(col_bad);
                } else if( msg.isMessageStatusTAMPERED() ) {
                    setFont(f);
                    setForeground(col_bad);
                }
            }

            setDeleted(msg.isDeleted());

            return this;
        }

//        public void setFont(Font font) {
//            super.setFont(font);
//            normalFont = font.deriveFont(Font.PLAIN);
//            boldFont = font.deriveFont(Font.BOLD);
//        }

        public void setDeleted(boolean value) {
            isDeleted = value;
        }
    }
    
    public class TreeTableCellEditor extends DefaultCellEditor {
        public TreeTableCellEditor() {
            super(new JCheckBox());
        }

        /**
         * Overridden to determine an offset that tree would place the
         * editor at. The offset is determined from the
         * <code>getRowBounds</code> JTree method, and additionally
         * from the icon DefaultTreeCellRenderer will use.
         * <p>The offset is then set on the TreeTableTextField component
         * created in the constructor, and returned.
         */
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int r, int c) {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, r, c);
            JTree t = getTree();
            boolean rv = t.isRootVisible();
            int offsetRow = rv ? r : r - 1;
            Rectangle bounds = t.getRowBounds(offsetRow);
            int offset = bounds.x;
            TreeCellRenderer tcr = t.getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer) {
            Object node = t.getPathForRow(offsetRow).getLastPathComponent();
            Icon icon;
            if (t.getModel().isLeaf(node))
                icon = ((DefaultTreeCellRenderer)tcr).getLeafIcon();
            else if (tree.isExpanded(offsetRow))
                icon = ((DefaultTreeCellRenderer)tcr).getOpenIcon();
            else
                icon = ((DefaultTreeCellRenderer)tcr).getClosedIcon();
            if (icon != null) {
                offset += ((DefaultTreeCellRenderer)tcr).getIconTextGap() +
                      icon.getIconWidth();
            }
            }
//            ((TreeTableTextField)getComponent()).offset = offset;
            return component;
        }

        /**
         * This is overridden to forward the event to the tree. This will
         * return true if the click count >= 3, or the event is null.
         */
        public boolean isCellEditable(EventObject e) {
            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent)e;
                if (me.getModifiers() == 0 || me.getModifiers() == InputEvent.BUTTON1_MASK) {
                    for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
                        if (getColumnClass(counter) == TreeTableModel.class) {
                            MouseEvent newME = new MouseEvent(
                                    MessageTreeTable.this.tree, 
                                    me.getID(),
                                    me.getWhen(), 
                                    me.getModifiers(),
                                    me.getX() - getCellRect(0, counter, true).x,
                                    me.getY(), 
                                    me.getClickCount(),
                                    me.isPopupTrigger());
                            MessageTreeTable.this.tree.dispatchEvent(newME);
                            break;
                        }
                    }
                }
            }
            return false;
        }
        }

    /**
     * Save the current column positions and column sizes for restore on next startup.
     */
    public void saveLayout(SettingsClass frostSettings) {
        TableColumnModel tcm = getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            TableColumn tc = tcm.getColumn(columnIndexInTable);
            int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            frostSettings.setValue("MessageTreeTable.tableindex.modelcolumn."+columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            frostSettings.setValue("MessageTreeTable.columnwidth.modelcolumn."+columnIndexInModel, columnWidth);
        }
    }

    /**
     * Load the saved column positions and column sizes.
     */
    public void loadLayout(SettingsClass frostSettings) {
        TableColumnModel tcm = getColumnModel();
        
        // hard set sizes of icons column
        tcm.getColumn(0).setMinWidth(20);
        tcm.getColumn(0).setMaxWidth(20);
        tcm.getColumn(0).setPreferredWidth(20);
        // hard set sizes of icons column
        tcm.getColumn(1).setMinWidth(20);
        tcm.getColumn(1).setMaxWidth(20);
        tcm.getColumn(1).setPreferredWidth(20);
        
        // set icon table header renderer for icon columns
        tcm.getColumn(0).setHeaderRenderer(new IconTableHeaderRenderer(flaggedIcon));
        tcm.getColumn(1).setHeaderRenderer(new IconTableHeaderRenderer(starredIcon));
        
        if( !loadLayout(frostSettings, tcm) ) {
            // Sets the relative widths of the columns
            int[] widths = { 20,20, 185, 95, 50, 130 };
            for (int i = 0; i < widths.length; i++) { 
                tcm.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }
    
    private boolean loadLayout(SettingsClass frostSettings, TableColumnModel tcm) {
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = "MessageTreeTable.tableindex.modelcolumn."+x;
            if( frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = "MessageTreeTable.columnwidth.modelcolumn."+x;
            if( frostSettings.getObjectValue(widthKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int columnWidth = frostSettings.getIntValue(widthKey);
            if( columnWidth <= 0 ) {
                return false; // invalid column width
            }
            columnWidths[x] = columnWidth;
        }
        // columns are currently added in model order, remove them all and save in an array
        // while on it, set the loaded width of each column
        TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
        for(int x=tcms.length-1; x >= 0; x--) {
            tcms[x] = tcm.getColumn(x);
            tcm.removeColumn(tcms[x]);
            // keep icon columns 0,1 as is
            if(x != 0 && x != 1) {
                tcms[x].setPreferredWidth(columnWidths[x]);
            }
        }
        // add the columns in order loaded from settings
        for(int x=0; x < tableToModelIndex.length; x++) {
            tcm.addColumn(tcms[tableToModelIndex[x]]);
        }
        return true;
    }
    
    /**
     * Resort table based on settings in SortStateBean
     */
    public void resortTable() {
        if( MessageTreeTableSortStateBean.isThreaded() ) {
            return;
        }
        FrostMessageObject root = (FrostMessageObject) getTree().getModel().getRoot();
        root.resortChildren();
        ((DefaultTreeModel)getTree().getModel()).reload();
    }

    private void fontChanged() {
        String fontName = Core.frostSettings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
        int fontStyle = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
        int fontSize = Core.frostSettings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
        Font font = new Font(fontName, fontStyle, fontSize);
        if (!font.getFamily().equals(fontName)) {
//            logger.severe(
//                "The selected font was not found in your system\n"
//                    + "That selection will be changed to \"Monospaced\".");
            Core.frostSettings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, "Monospaced");
            font = new Font("Monospaced", fontStyle, fontSize);
        }
        setFont(font);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_LIST_FONT_NAME)) {
            fontChanged();
        } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_LIST_FONT_SIZE)) {
            fontChanged();
        } else if (evt.getPropertyName().equals(SettingsClass.MESSAGE_LIST_FONT_STYLE)) {
            fontChanged();
        }
    }
}
