/*
 * Created on 24-dic-2004
 * 
 */
package frost.gui;

import java.util.logging.Logger;

import javax.swing.tree.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class TofTreeModel extends DefaultTreeModel {

	private static Logger logger = Logger.getLogger(TofTreeModel.class.getName());

	/**
	 * @param root
	 */
	public TofTreeModel(TreeNode root) {
		super(root);
	}

}