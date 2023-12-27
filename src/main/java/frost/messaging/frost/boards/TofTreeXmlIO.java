/*
  TofTreeXmlIO.java / Frost
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

import java.io.File;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import frost.util.XMLTools;

public class TofTreeXmlIO {

	private static final Logger logger = LoggerFactory.getLogger(TofTreeXmlIO.class);

    /**************************************************
     * TREE LOAD METHODS ******************************
     **************************************************/

    /**
     * Methods creates the DefaultTreeModel that is used in the TofTree.
     * Model needs to be created here because we want to expand rows on loading.
     * So we also need the tree to call expandRow() on it.
     */
    public boolean loadBoardTree(
            final TofTree tree,
            final TofTreeModel model,
            final String filename,
            final UnsentMessagesFolder unsentMsgs,
            final SentMessagesFolder sentMsgs)
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename);
        } catch(final Exception ex) { ; } // xml format error

        if( doc == null ) {
            logger.error("loadBoardTree: could'nt parse XML Document.");
            return false;
        }

        final Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostBoardTree") == false ) {
            logger.error("boards.xml invalid: does not contain the root tag 'FrostBoardTree'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        final List<Element> nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostBoardTreeEntry");

        if( nodelist.size() != 1 ) {
            logger.error("boards.xml invalid: first element must be the one and only root folder ({})", nodelist.size());
            return false;
        }
        final Element boardRootNode = nodelist.get(0);
        final String tagName = boardRootNode.getTagName();
        if( tagName == null || tagName.equals("FrostBoardTreeEntry") == false )
        {
            logger.error("boards.xml invalid: first element must the root folder");
            return false;
        }

        // add root node + set expanded state
        final String name = getName( boardRootNode );

        final boolean isFolder = isFolder( boardRootNode );
        if( isFolder == false ) {
            logger.error("boards.xml invalid: first element must be a folder (the root folder)");
            return false;
        }

        final Folder treeRootNode = new Folder(name);
        model.setRoot(treeRootNode);

        // add sent/unsent messages folders to root
        treeRootNode.add(unsentMsgs);
        treeRootNode.add(sentMsgs);

        // now process all childs of this node recursively and add all boards/folder to root node
        loadProcessFolder( boardRootNode, treeRootNode, tree, model );

        refreshModel( model, treeRootNode );

        // assign perst boards from storage to gui boards
        model.initialAssignPerstFrostBoardObjects();

        tree.updateUI();

        logger.info("Board tree loaded successfully.");

        return true;
    }

    /**
     * Process a nodes childs recursively.
     */
    private void loadProcessFolder(
        final Element boardFolder,
        final Folder treeFolder,
        final JTree tree,
        final DefaultTreeModel model)
    {
        // process all childs of type "FrostBoardTreeEntry" , dive into folder and process them
        final List<Element> list = XMLTools.getChildElementsByTagName(boardFolder, "FrostBoardTreeEntry");
        for( final Element child : list ) {

            final String nodename = getName(child);
            if (nodename == null) {
                continue;
            }

            final boolean isFolder = isFolder(child);

            // add the child
            if (isFolder == false) {
                final String publicKey = getPublicKey(child);
                final String privateKey = getPrivateKey(child);
                final String description = getDescription(child);

                // now if the child is a board, add it
                final Board fbobj = new Board(nodename, publicKey, privateKey, description);
                // look for <config/> element and maybe configure board
                getBoardConfiguration(child, fbobj);
                // maybe restore lastUpdateStartedMillis ( = board update progress)
                List<Element> ltmp = XMLTools.getChildElementsByTagName(child, "lastUpdateStartedMillis");
                if (ltmp.size() > 0) {
                    final Text txtname = (Text) ((Node) ltmp.get(0)).getFirstChild();
                    if (txtname != null) {
                        long millis = -1;
                        try {
                            millis = Long.parseLong(txtname.getData().trim());
                        } catch (final Exception e) {
                            ;
                        }

                        if (millis > 0) {
                            fbobj.setLastUpdateStartMillis(millis);
                        }
                    }
                }

                // maybe restore lastBackloadUpdateFinishedMillis ( = board update progress)
                ltmp = XMLTools.getChildElementsByTagName(child, "lastBackloadUpdateFinishedMillis");
                if (ltmp.size() > 0) {
                    final Text txtname = (Text) ((Node) ltmp.get(0)).getFirstChild();
                    if (txtname != null) {
                        long millis = -1;
                        try {
                            millis = Long.parseLong(txtname.getData().trim());
                        } catch (final Exception e) {
                            ;
                        }

                        if (millis > 0) {
                            fbobj.setLastBackloadUpdateFinishedMillis(millis);
                        }
                    }
                }

                treeFolder.add(fbobj);
            } else {
                final boolean isExpanded = isExpanded(child);

                // if the child is a folder, add it and maybe expand and dive into it
                final Folder fbobj = new Folder(nodename);
                treeFolder.add(fbobj);

                loadProcessFolder(child, fbobj, tree, model); // dive into folder

                refreshModel(model, fbobj);

                // now expand path if previously expanded
                if (isExpanded == true) {
                    tree.expandPath(new TreePath(model.getPathToRoot(fbobj)));
                }
            }
        }
    }

    private void getBoardConfiguration( Element element, final Board board ) {
        final List<Element> list = XMLTools.getChildElementsByTagName(element, "config");
        if( list.size() == 0 ) {
            board.setConfigured( false );
            return;
        }

        board.setConfigured( true );
        element = list.get(0);

        String val = element.getAttribute("autoUpdate");
        board.setAutoUpdateEnabled( Boolean.valueOf(val).booleanValue() );

        val = element.getAttribute("maxMessageDisplay");
        if( val.length() == 0 ) {
            board.setMaxMessageDays( null );
        } else {
			board.setMaxMessageDays(Integer.valueOf(val));
        }

        val = element.getAttribute("maxMessageDownload");
        if( val.length() == 0 ) {
            board.setMaxMessageDownload( null );
        } else {
			board.setMaxMessageDownload(Integer.valueOf(val));
        }

        val = element.getAttribute("showSignedOnly");
        if( val.length() == 0 ) {
            board.setShowSignedOnly( null );
        } else {
            board.setShowSignedOnly( Boolean.valueOf(val) );
        }

        val = element.getAttribute("storeSentMessages");
        if( val.length() == 0 ) {
            board.setStoreSentMessages( null );
        } else {
            board.setStoreSentMessages( Boolean.valueOf(val) );
        }

        val = element.getAttribute("hideBadMessages");
        if( val.length() == 0 ) {
            board.setHideBad( null );
        } else {
            board.setHideBad( Boolean.valueOf(val) );
        }

        val = element.getAttribute("hideCheckMessages");
        if( val.length() == 0 ) {
            board.setHideCheck( null );
        } else {
            board.setHideCheck( Boolean.valueOf(val) );
        }

        val = element.getAttribute("hideObserveMessages");
        if( val.length() == 0 ) {
            board.setHideObserve( null );
        } else {
            board.setHideObserve( Boolean.valueOf(val) );
        }

        val = element.getAttribute("hideMessageCount");
        if( val.length() == 0 ) {
            board.setHideMessageCount( null );
        } else {
            board.setHideMessageCount( Integer.valueOf(val) );
        }
    }

    private void refreshModel(final DefaultTreeModel model, final Folder node) {
        // all childs are new, send a nodesWhereInserted to model
        final int childIndicies[] = new int[node.getChildCount()];
        for(int x=0; x < node.getChildCount(); x++) {
            childIndicies[x] = x;
        }
        model.nodesWereInserted(node, childIndicies);
    }

    private boolean isFolder(final Element treeEntry) {
        final String isFolder = treeEntry.getAttribute("isfolder");
        if( isFolder == null || isFolder.toLowerCase().equals("true") == false ) {
            return false; // default is false
        }
        return true;
    }

    private boolean isExpanded(final Element treeEntry) {
        final String isExpanded = treeEntry.getAttribute("isexpanded");
        if( isExpanded == null || isExpanded.toLowerCase().equals("true") == false ) {
            return false; // default is false
        }
        return true;
    }

    private String getName(final Element treeEntry) {
        final List<Element> list = XMLTools.getChildElementsByTagName(treeEntry, "name");
        if( list.size() != 1 ) {
            logger.error("boards.xml invalid: there must be 1 <name> tag for each entry");
            return null;
        }
        final Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null ) {
            return null;
        }
        return txtname.getData().trim();
    }

    private String getPublicKey(final Element treeEntry) {
        final List<Element> list = XMLTools.getChildElementsByTagName(treeEntry, "publicKey");
        if( list.size() > 1 ) {
            logger.error("boards.xml invalid: there should be a maximum of 1 <publicKey> tag for each entry");
            return null;
        }
        if( list.size() == 0 ) {
            return null;
        }
        final Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null ) {
            return null;
        }
        return txtname.getData().trim();
    }

    /**
     * @param treeEntry
     * @return
     */
    private String getDescription(final Element treeEntry) {
        final List<Element> list = XMLTools.getChildElementsByTagName(treeEntry, "description");
        if (list.size() > 1) {
            logger.error("boards.xml invalid: there should be a maximum of 1 <description> tag for each entry");
            return null;
        }
        if (list.size() == 0) {
            return null;
        }
        final Text txtname = (Text) ((Node) list.get(0)).getFirstChild();
        if (txtname == null) {
            return null;
        }
        return txtname.getData().trim();
    }

    private String getPrivateKey(final Element treeEntry) {
        final List<Element> list = XMLTools.getChildElementsByTagName(treeEntry, "privateKey");
        if( list.size() > 1 ) {
            logger.error("boards.xml invalid: there should be a maximum of 1 <privateKey> tag for each entry");
            return null;
        }
        if( list.size() == 0 ) {
            return null;
        }
        final Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null ) {
            return null;
        }
        return txtname.getData().trim();
    }

    /**************************************************
     * TREE SAVE METHODS ******************************
     **************************************************/

    public boolean saveBoardTree(final TofTree tree, final TofTreeModel model, final File file) {
        final Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.error("saveBoardTree: factory could'nt create XML Document.");
            return false;
        }

        final Folder root = (Folder) model.getRoot();

        final Element rootElement = doc.createElement("FrostBoardTree");
        doc.appendChild(rootElement);

        final Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        rootBoardElement.setAttribute("isfolder", "true");
        rootBoardElement.setAttribute("isexpanded", "true");

        final Element nameElement = doc.createElement("name");
        final Text text = doc.createTextNode( root.getName() );
        nameElement.appendChild( text );

        rootBoardElement.appendChild( nameElement );
        rootElement.appendChild( rootBoardElement );

        // append all childs and subchilds
        saveProcessFolder(rootBoardElement, root, doc, model, tree);

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, file);
            logger.info("Board tree saved successfully.");
        } catch(final Throwable t) {
            logger.error("Exception - saveBoardTree", t);
        }

        return writeOK;
    }

    private void saveProcessFolder(
            final Element parentElement,
            final Folder treeNode,
            final Document doc,
            final DefaultTreeModel model,
            final JTree tree)
    {
        // parentElement = element to append to
        for(int x=0; x < treeNode.getChildCount(); x++) {
            final AbstractNode boardObject = (AbstractNode)treeNode.getChildAt(x);
            if( boardObject.isBoard() ) {
                // its a board
                appendBoard(parentElement, (Board)boardObject, doc);
            } else if( boardObject.isFolder() ) {
                // its a folder
                final Element newFolder = appendFolder(parentElement, (Folder)boardObject, doc, model, tree);
                saveProcessFolder(newFolder, (Folder)boardObject, doc, model, tree);
            }
        }
    }

    private void appendBoard(final Element parent, final Board board, final Document doc) {
        final Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        Element element;
        CDATASection cdata;

        // <name>
        element = doc.createElement("name");
        cdata = doc.createCDATASection(board.getName());
        element.appendChild( cdata );
        rootBoardElement.appendChild( element );
        // pubkey
        if( board.getPublicKey() != null ) {
            element = doc.createElement("publicKey");
            cdata = doc.createCDATASection(board.getPublicKey());
            element.appendChild(cdata);
            rootBoardElement.appendChild(element);
        }
        // privkey
        if( board.getPrivateKey() != null ) {
            element = doc.createElement("privateKey");
            cdata = doc.createCDATASection(board.getPrivateKey());
            element.appendChild(cdata);
            rootBoardElement.appendChild(element);
        }
        // description
         if( board.getDescription() != null ) {
             element = doc.createElement("description");
             cdata = doc.createCDATASection(board.getDescription());
             element.appendChild( cdata );
             rootBoardElement.appendChild( element );
         }
        // <config />
        if( board.isConfigured() ) {
            element = doc.createElement("config");

            element.setAttribute("autoUpdate", "" + board.getAutoUpdateEnabled());
            if( board.getMaxMessageDisplayObj() != null ) {
                element.setAttribute("maxMessageDisplay", "" + board.getMaxMessageDisplay());
            }
            if( board.getMaxMessageDownloadObj() != null ) {
                element.setAttribute("maxMessageDownload", "" + board.getMaxMessageDownload());
            }
            if( board.getShowSignedOnlyObj() != null ) {
                element.setAttribute("showSignedOnly", "" + board.getShowSignedOnly());
            }
            if( board.getStoreSentMessagesObj() != null ) {
                element.setAttribute("storeSentMessages", "" + board.getStoreSentMessages());
            }
            if( board.getHideBadObj() != null ) {
                element.setAttribute("hideBadMessages", "" + board.getHideBad());
            }
            if( board.getHideCheckObj() != null ) {
                element.setAttribute("hideCheckMessages", "" + board.getHideCheck());
            }
            if( board.getHideObserveObj() != null ) {
                element.setAttribute("hideObserveMessages", "" + board.getHideObserve());
            }
            if( board.getHideMessageCountObj() != null ) {
	            element.setAttribute("hideMessageCount", "" + board.getHideMessageCount());
			}
            rootBoardElement.appendChild(element);
        }

        // append lastUpdateStartedMillis
        if( board.getLastUpdateStartMillis() > 0 ) {
            element = doc.createElement("lastUpdateStartedMillis");
            final Text text = doc.createTextNode("" + board.getLastUpdateStartMillis());
            element.appendChild(text);
            rootBoardElement.appendChild(element);
        }

        // append lastBackloadUpdateStartedMillis
        if( board.getLastBackloadUpdateFinishedMillis() > 0 ) {
            element = doc.createElement("lastBackloadUpdateFinishedMillis");
            final Text text = doc.createTextNode("" + board.getLastBackloadUpdateFinishedMillis());
            element.appendChild(text);
            rootBoardElement.appendChild(element);
        }

        parent.appendChild( rootBoardElement );
    }

    private Element appendFolder(
            final Element parent,
            final Folder board,
            final Document doc,
            final DefaultTreeModel model,
            final JTree tree)
    {
        final Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        rootBoardElement.setAttribute("isfolder", "true");
        boolean expanded;
        String expandedstr;
        expanded = tree.isExpanded( new TreePath(model.getPathToRoot(board)) );
        if( expanded ) {
            expandedstr = "true";
        } else {
            expandedstr = "false";
        }

        rootBoardElement.setAttribute("isexpanded", expandedstr);
        // <name>
        final Element element = doc.createElement("name");
        final Text text = doc.createTextNode( board.getName() );
        element.appendChild( text );
        rootBoardElement.appendChild( element );
        parent.appendChild( rootBoardElement );
        return rootBoardElement;
    }
}
