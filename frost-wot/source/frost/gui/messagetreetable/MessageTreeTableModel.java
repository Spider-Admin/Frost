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
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.*;
import frost.messages.*;
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
public class MessageTreeTableModel extends DefaultTreeModel implements TreeTableModel, LanguageListener {

    private Language language = null;

    protected final static String columnNames[] = new String[7];

    /**
     * Constructor for creating a DynamicTreeTableModel.
     */
    public MessageTreeTableModel(final TreeNode root) {
        super(root);
        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();
    }

    public void languageChanged(final LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        columnNames[0] = "!";
        columnNames[1] = "*";
        columnNames[2] = language.getString("MessagePane.messageTable.subject");
        columnNames[3] = language.getString("MessagePane.messageTable.from");
        columnNames[4] = language.getString("MessagePane.messageTable.index");
        columnNames[5] = language.getString("MessagePane.messageTable.sig");
        columnNames[6] = language.getString("MessagePane.messageTable.date");

        try {
            final TableColumnModel tcm = MainFrame.getInstance().getMessagePanel().getMessageTable().getTableHeader().getColumnModel();
            for(int x=0; x<tcm.getColumnCount(); x++) {
                final TableColumn tc = tcm.getColumn(x);
                tc.setHeaderValue(columnNames[tc.getModelIndex()]);
            }
        } catch(final NullPointerException e) {
            // could occur during startup, ignore
            // this code is intended to manually change the header strings if the language was changed
        }
    }

    //
    // TreeModel interface
    //

    /**
     * TreeModel method to return the number of children of a particular
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChildCount</code>.
     */
    @Override
    public int getChildCount(final Object node) {
        return ((TreeNode)node).getChildCount();
    }

    /**
     * TreeModel method to locate a particular child of the specified
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChild</code>.
     */
    @Override
    public Object getChild(final Object node, final int i) {
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
    public String getColumnName(final int column) {
    	if (columnNames == null || column < 0 || column >= columnNames.length) {
    	    return null;
    	}
    	return columnNames[column];
    }

    /**
     * Returns the column class for column <code>column</code>. This
     * is set in the constructor.
     */
    public Class getColumnClass(final int column) {
        if( column == 2 ) {
            return TreeTableModel.class;
        }
        if( column == 0 || column == 1 ) {
            return Boolean.class;
        }
        return String.class;
    }

    /**
     * Returns the value for the column <code>column</code> and object
     * <code>node</code>. The return value is determined by invoking
     * the method specified in constructor for the passed in column.
     */
    public Object getValueAt(final Object node, final int column) {

        if( node instanceof FrostMessageObject ) {
            final FrostMessageObject mo = (FrostMessageObject)node;
            if( mo.isDummy() ) {
                // show no text for dummy msgs
                switch(column) {
                    case 0: return Boolean.FALSE;
                    case 1: return Boolean.FALSE;
                    // 2 is tree+subject column
                    case 3: return "";
                    case 4: return "";
                    case 5: return "";
                    case 6: return "";
                    default: return "*ERR*";
                }
            } else {
                switch(column) {
                    case 0: return Boolean.valueOf(mo.isFlagged());
                    case 1: return Boolean.valueOf(mo.isStarred());
                    // 2 is tree+subject column
                    case 2: return mo.getSubject();
                    case 3: return mo.getFromName();
                    case 4: return Integer.toString(mo.getIndex());
                    case 5: return mo.getMessageStatusString();
                    case 6: return mo.getDateAndTimeString();
                    default: return "*ERR*";
                }
            }
        } else {
            return "*ERR*";
        }
    }

    /**
     * <code>isCellEditable</code> is invoked by the JTreeTable to determine
     * if a particular entry can be added. This is overridden to return true
     * for the first column, assuming the node isn't the root, as well as
     * returning two for the second column if the node is a BookmarkEntry.
     * For all other columns this returns false.
     */
    public boolean isCellEditable(final Object node, final int column) {
        if( column == 2 ) {
            return true; // tree column
        }
        return false;
    }

    public void setValueAt(final Object aValue, final Object node, final int column) {
//        final FrostMessageObject message = (FrostMessageObject)node;
//        boolean newValue = ((Boolean)aValue).booleanValue();
//        boolean save = false;
//        if( column == 0 && message.isFlagged() != newValue ) {
//            message.setFlagged(newValue);
//            save = true;
//        } else if( column == 1 && message.isStarred() != newValue ) {
//            message.setStarred(newValue);
//            save = true;
//        }
//        if( save ) {
//            Thread saver = new Thread() {
//                public void run() {
//                    try {
//                        AppLayerDatabase.getMessageTable().updateMessage(message);
//                    } catch (SQLException ex) {
//                        logger.log(Level.SEVERE, "Error updating a message object", ex);
//                    }
//                }
//            };
//            saver.start();
//        }
    }
}
