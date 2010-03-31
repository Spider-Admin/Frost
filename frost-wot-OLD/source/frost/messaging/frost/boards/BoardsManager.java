/*
 BoardsManager.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.frost.boards;

import frost.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class BoardsManager {

	private TofTree tofTree;
	private TofTreeModel tofTreeModel;
	
	private MainFrame mainFrame;
	
	private SettingsClass settings;
	
	public BoardsManager(SettingsClass settings) {
		super();
		this.settings = settings;
	}
	
	public void initialize() {
		TOF.initialize(getTofTreeModel());
		getTofTree().initialize();
		mainFrame.setTofTree(getTofTree());
		mainFrame.setTofTreeModel(getTofTreeModel());
		mainFrame.addMenuItem(getTofTree().getConfigBoardMenuItem(), "MainFrame.menu.news", 1, 1, true);
	}
	
	public TofTree getTofTree() {
		if (tofTree == null) {
			tofTree = new TofTree(getTofTreeModel());
			tofTree.setSettings(settings);
			tofTree.setMainFrame(mainFrame);
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
			Folder dummyRootNode = new Folder("Frost Message System");
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
