/*
 * Created on 27-dic-2004
 * 
 */
package frost.boards;

import frost.*;
import frost.MainFrame;
import frost.gui.objects.Board;
import frost.messaging.MessageHashes;

/**
 * @author $Author$
 * @version $Revision$
 */
public class BoardsManager {

	private TofTree tofTree;
	private TofTreeModel tofTreeModel;
	
	private MainFrame mainFrame;
	
	private SettingsClass settings;
	private Core core;
	private MessageHashes messageHashes;
	
	/**
	 * 
	 */
	public BoardsManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}
	
	/**
	 * 
	 */
	public void initialize() {
		TOF.initialize(getTofTreeModel());
		getTofTree().initialize();
		mainFrame.setTofTree(getTofTree());
		mainFrame.setTofTreeModel(getTofTreeModel());
		mainFrame.addButton(getTofTree().getConfigBoardButton(), 1, 0, false);
		mainFrame.addButton(getTofTree().getCutBoardButton(), 2, 0, false);
		mainFrame.addButton(getTofTree().getPasteBoardButton(), 2, 1, false);
		mainFrame.addMenuItem(getTofTree().getConfigBoardMenuItem(), "News", 1, 1, true);
	}
	
	/**
	 * @return
	 */
	public TofTree getTofTree() {
		if (tofTree == null) {
			tofTree = new TofTree(getTofTreeModel());
			tofTree.setSettings(settings);
			tofTree.setCore(core);
			tofTree.setMainFrame(mainFrame);
			tofTree.setMessageHashes(messageHashes);
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

	/**
	 * @param core
	 */
	public void setCore(Core core) {
		this.core = core;		
	}

	/**
	 * @param messageHashes
	 */
	public void setMessageHashes(MessageHashes messageHashes) {
		this.messageHashes = messageHashes;		
	}
}
