/*
  FreetalkManager.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.freetalk;

import frost.*;
import frost.fcp.*;
import frost.fcp.fcp07.freetalk.*;
import frost.messaging.freetalk.boards.*;

public class FreetalkManager {

    private FcpFreetalkConnection fcpFreetalkConnection = null;

    private static FreetalkManager instance = null;

    private FreetalkBoardTree ftBoardTree;
    private FreetalkBoardTreeModel ftBoardTreeModel;

    private FreetalkManager() {
        try {
            if (FcpHandler.inst().getFreenetNode() == null) {
                throw new Exception("No freenet nodes defined");
            }
            final NodeAddress na = FcpHandler.inst().getFreenetNode();
            fcpFreetalkConnection = new FcpFreetalkConnection(na);
        } catch(final Exception ex) {
            fcpFreetalkConnection = null;
        }
    }

    public static FreetalkManager getInstance() {
        System.out.println("getInstance="+instance);
        return instance;
    }

    public synchronized static void initialize() {
        if (instance == null) {
            instance = new FreetalkManager();
            instance.getBoardTree().initialize();
        }
        System.out.println("initialize="+instance);
    }

    /**
     * Connection is null when Freetalk plugin is not Talkable.
     */
    public FcpFreetalkConnection getConnection() {
        return fcpFreetalkConnection;
    }

    public FreetalkBoardTree getBoardTree() {
        if (ftBoardTree == null) {
            ftBoardTree = new FreetalkBoardTree(getTreeModel());
            ftBoardTree.setSettings(Core.frostSettings);
            ftBoardTree.setMainFrame(MainFrame.getInstance());
        }
        return ftBoardTree;
    }

    public FreetalkBoardTreeModel getTreeModel() {
        if (ftBoardTreeModel == null) {
            // this rootnode is discarded later, but if we create the tree without parameters,
            // a new Model is created wich contains some sample data by default (swing)
            // this confuses our renderer wich only expects FrostBoardObjects in the tree
            final FreetalkFolder dummyRootNode = new FreetalkFolder("Frost Message System");
            ftBoardTreeModel = new FreetalkBoardTreeModel(dummyRootNode);
        }
        return ftBoardTreeModel;
    }
}
