/*
  KnownBoardsXmlDAO.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.fcp.*;
import frost.messaging.frost.*;
import frost.messaging.frost.boards.*;
import frost.util.*;

public class KnownBoardsXmlDAO {

    private static final Logger logger = Logger.getLogger(KnownBoardsXmlDAO.class.getName());

    /**
     * @param file
     * @return  List of Board
     */
    public static List<Board> loadKnownBoards(final File file) {

        final File boards = file;
        final ArrayList<Board> knownBoards = new ArrayList<Board>();
        if( boards.exists() ) {
            Document doc = null;
            try {
                doc = XMLTools.parseXmlFile(boards);
            } catch (final Exception ex) {
                logger.log(Level.SEVERE, "Error reading knownboards.xml", ex);
                return knownBoards;
            }
            final Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostKnownBoards") == false ) {
                logger.severe("Error - invalid knownboards.xml: does not contain the root tag 'FrostKnownBoards'");
                return knownBoards;
            }
            // pass this as an 'AttachmentList' to xml read method and get all board attachments
            final AttachmentList<Attachment> attachmentList = new AttachmentList<Attachment>();
            try {
                attachmentList.loadXMLElement(rootNode);
            } catch (final Exception ex) {
                logger.log(Level.SEVERE, "Error - knownboards.xml: contains unexpected content.", ex);
                return knownBoards;
            }
            final AttachmentList<BoardAttachment> lst = attachmentList.getAllOfTypeBoard();
            for(final Iterator<BoardAttachment> i=lst.iterator(); i.hasNext(); ) {
                final Board b = i.next().getBoardObj();
                if( isBoardKeyValidForFreenetVersion(b) ) {
                    knownBoards.add(b);
                } else {
                    logger.warning("Known board keys are invalid for this freenet version, board ignored: "+b.getName());
                }
            }
            logger.info("Loaded " + knownBoards.size() + " known boards.");
        }
        return knownBoards;
    }

    /**
     * Check the public and private key of the board if they are valid for
     * the used freenet version.
     */
    private static boolean isBoardKeyValidForFreenetVersion(final Board b) {
        String key;
        key = b.getPublicKey();
        if( key != null && key.length() > 0 ) {
            if( !FreenetKeys.isValidKey(key) ) {
                return false;
            }
        }
        key = b.getPrivateKey();
        if( key != null && key.length() > 0 ) {
            if( !FreenetKeys.isValidKey(key) ) {
                return false;
            }
        }
        return true; // keys not set or valid
    }

    /**
     * @param file
     * @param knownBoardList  List of KnownBoard
     * @return
     */
    public static boolean saveKnownBoards(final File file, final List<Board> knownBoardList) {
        final Document doc = XMLTools.createDomDocument();
        if (doc == null) {
            logger.severe("Error - saveBoardTree: factory couldn't create XML Document.");
            return false;
        }

        final Element rootElement = doc.createElement("FrostKnownBoards");
        doc.appendChild(rootElement);

        final Iterator<Board> knownBoardListIterator = knownBoardList.iterator();
        while (knownBoardListIterator.hasNext()) {
            final BoardAttachment current = new BoardAttachment(knownBoardListIterator.next());
            rootElement.appendChild(current.getXMLElement(doc));
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, file.getPath());
        } catch (final Throwable ex) {
            logger.log(Level.SEVERE, "Exception while writing knownboards.xml:", ex);
        }
        if (!writeOK) {
            logger.severe("Error exporting knownboards, file was not saved");
        }
        return writeOK;
    }
}
