/*
 * Created on 27-dic-2004
 * 
 */
package frost.boards;

import frost.MainFrame;
import frost.gui.objects.Board;

/**
 * @author $Author$
 * @version $Revision$
 */
public class BoardsManager {

	private TofTree tofTree;
	private TofTreeModel tofTreeModel;
	
	private MainFrame mainFrame;
	
	/**
	 * 
	 */
	public BoardsManager() {
		super();
		getTofTree().initialize();
	}
	
	/**
	 * 
	 */
	public void initialize() {
		mainFrame.setTofTree(getTofTree());
		mainFrame.setTofTreeModel(getTofTreeModel());
	}
	
	/**
	 * @return
	 */
	public TofTree getTofTree() {
		if (tofTree == null) {
			tofTree = new TofTree(getTofTreeModel());
		}
		return tofTree;
	}
	
	/**
	 * @return
	 */
	public TofTreeModel getTofTreeModel() {
		if (tofTreeModel == null) {
			// this rootnode is discarded later, but if we create the tree without parameters,
			// a new Model is created wich contains some sample data by default (swing)
			// this confuses our renderer wich only expects FrostBoardObjects in the tree
			Board dummyRootNode = new Board("Frost Message System", true);
			tofTreeModel = new TofTreeModel(dummyRootNode);
		}
		return tofTreeModel;
	}

	/**
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
}
