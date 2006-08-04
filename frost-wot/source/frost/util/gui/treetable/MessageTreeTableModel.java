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

package frost.util.gui.treetable;
import javax.swing.tree.*;

import frost.gui.objects.*;
import frost.util.gui.translation.*;

/**
 * An implementation of TreeTableModel that uses reflection to answer
 * TableModel methods. This works off a handful
 * of values. A TreeNode is used to answer all the TreeModel related
 * methods (similiar to AbstractTreeTableModel and DefaultTreeModel).
 * The column names are specified in the constructor. The values for
 * the columns are dynamically obtained via reflection, you simply
 * provide the method names. The methods used to set a particular value are
 * also specified as an array of method names, a null method name, or
 * null array indicates the column isn't editable. And the class types,
 * used for the TableModel method getColumnClass are specified in the
 * constructor.
 *
 * @author Scott Violet
 */
public class MessageTreeTableModel extends AbstractTreeTableModel implements LanguageListener {

    private Language language = null;

    protected final static String columnNames[] = new String[4];

    /**
     * Constructor for creating a DynamicTreeTableModel.
     */
    public MessageTreeTableModel(TreeNode root) {
        super(root);
        language = Language.getInstance();
        refreshLanguage();
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
//        columnNames[0] = language.getString("MessagePane.messageTable.index");
        columnNames[0] = language.getString("MessagePane.messageTable.subject");
        columnNames[1] = language.getString("MessagePane.messageTable.from");
        columnNames[2] = language.getString("MessagePane.messageTable.sig");
        columnNames[3] = language.getString("MessagePane.messageTable.date");

//        fireTableStructureChanged();
    }

    //
    // TreeModel interface
    //

    /**
     * TreeModel method to return the number of children of a particular
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChildCount</code>.
     */
    public int getChildCount(Object node) { 
        return ((TreeNode)node).getChildCount();
    }

    /**
     * TreeModel method to locate a particular child of the specified
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChild</code>.
     */
    public Object getChild(Object node, int i) {
        return ((TreeNode)node).getChildAt(i);
    }

    //
    //  The TreeTable interface. 
    //

    /**
     * Returns the number of column names passed into the constructor.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the column name passed into the constructor.
     */
    public String getColumnName(int column) {
    	if (columnNames == null || column < 0 || column >= columnNames.length) {
    	    return null;
    	}
    	return columnNames[column];
    }

    /**
     * Returns the column class for column <code>column</code>. This
     * is set in the constructor.
     */
    public Class getColumnClass(int column) {
        if( column == 0 ) {
            return TreeTableModel.class;
        }
        return String.class;
    }

    /**
     * Returns the value for the column <code>column</code> and object
     * <code>node</code>. The return value is determined by invoking
     * the method specified in constructor for the passed in column.
     */
    public Object getValueAt(Object node, int column) {
        
        if( node instanceof FrostMessageObject ) {
            FrostMessageObject mo = (FrostMessageObject)node; 
            switch(column) {
//                case 0: return ""+mo.getIndex();
                case 1: return mo.getFromName();
//                case 2: return mo.getSubject();
                case 2: return mo.getMessageStatusString();
                case 3: return mo.getDateAndTime();
                default: return "*ERR*";
            }
        } else {
            return "(root)";
        }
    }

//    /**
//     * <code>isCellEditable</code> is invoked by the JTreeTable to determine
//     * if a particular entry can be added. This is overridden to return true
//     * for the first column, assuming the node isn't the root, as well as
//     * returning two for the second column if the node is a BookmarkEntry.
//     * For all other columns this returns false.
//     */
//    public boolean isCellEditable(Object node, int column) {
////        switch (column) {
////        case 0:
////            // Allow editing of the name, as long as not the root.
////            return (node != getRoot());
////        case 1:
////            // Allow editing of the location, as long as not a
////            // directory
////            return (node instanceof Bookmarks.BookmarkEntry);
////        default:
////            // Don't allow editing of the date fields.
////            return false;
////        }
//        if( column == 0 ) {
//            return true; // needed to be able to expand the tree
//        }
//        return false;
//    }
}
