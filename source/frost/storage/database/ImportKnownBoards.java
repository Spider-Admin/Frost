package frost.storage.database;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.messages.*;
import frost.storage.database.applayer.*;

public class ImportKnownBoards {
    
    private static Logger logger = Logger.getLogger(ImportKnownBoards.class.getName());
    
    public void importKnownBoards() {
        KnownBoardsDatabaseTable dbt = AppLayerDatabase.getKnownBoardsDatabaseTable();
        
        List knownBoards = loadKnownBoards();
        dbt.addNewKnownBoards(knownBoards);
    }

    private List loadKnownBoards() {

        File boards = new File("knownboards.xml");
        ArrayList knownBoards = new ArrayList();
        if( boards.exists() ) {
            Document doc = null;
            try {
                doc = XMLTools.parseXmlFile(boards, false);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error reading knownboards.xml", ex);
                return knownBoards;
            }
            Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostKnownBoards") == false ) {
                logger.severe("Error - invalid knownboards.xml: does not contain the root tag 'FrostKnownBoards'");
                return knownBoards;
            }
            // pass this as an 'AttachmentList' to xml read method and get
            // all board attachments
            AttachmentList al = new AttachmentList();
            try {
                al.loadXMLElement(rootNode);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error - knownboards.xml: contains unexpected content.", ex);
                return knownBoards;
            }
            List lst = al.getAllOfType(Attachment.BOARD);
            for(Iterator i=lst.iterator(); i.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)i.next();
                knownBoards.add(ba.getBoardObj());
            }
            logger.info("Loaded " + knownBoards.size() + " known boards.");
        }
        return knownBoards;
    }
}
