/*
  TableXmlIO.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.gui.objects.FrostBoardObject;

public class TableXmlIO
{
	private static Logger logger = Logger.getLogger(TableXmlIO.class.getName());
	
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

	/**************************************************
	 * TABLE LOAD METHODS *****************************
	 **************************************************/

	////////  UPLOAD MODEL  /////////

	public static boolean loadUploadModel(UploadModel model, String filename) {
		Document doc = null;
		try {
			doc = XMLTools.parseXmlFile(filename, false);
		} catch (Exception ex) {
			;
		} // xml format error

		if (doc == null) {
			logger.severe("Error - loadUploadModel: factory could'nt create XML Document.");
			return false;
		}

		Element rootNode = doc.getDocumentElement();

		if (rootNode.getTagName().equals("FrostUploadTable") == false) {
			logger.severe(
				"Error - uploads.xml invalid: does not contain the root tag 'FrostUploadTable'");
			return false;
		}
		// check if rootnode contains only a single boardEntry wich must be a folder (root folder)
		ArrayList nodelist =
			XMLTools.getChildElementsByTagName(rootNode, "FrostUploadTableItemList");
		if (nodelist.size() != 1) {
			return false;
		}

		Element itemListRootNode = (Element) nodelist.get(0);

		nodelist = XMLTools.getChildElementsByTagName(itemListRootNode, "FrostUploadTableItem");

		if (nodelist.size() == 0)
			return true; // empty save file

		for (int x = 0; x < nodelist.size(); x++) {
			Element uploadItemElement = (Element) nodelist.get(x);
			appendUploadModelItemToModel(uploadItemElement, model);
		}
		logger.info("Loaded " + nodelist.size() + " items into upload table.");
		return true;
	}

	protected static void appendUploadModelItemToModel(Element ulItemElement, UploadModel model) {
		FrostUploadItem ulObj = getUploadItemFromElement(ulItemElement);
		if (ulObj == null)
			return;
		model.addUploadItem(ulObj);
	}

    protected static FrostUploadItem getUploadItemFromElement(Element ulItemElement)
    {
        String filename = XMLTools.getChildElementsCDATAValue(ulItemElement, "filename");
        String filepath = XMLTools.getChildElementsCDATAValue(ulItemElement, "filepath");
        String targetboardname = XMLTools.getChildElementsTextValue(ulItemElement, "targetboard");
        String state = XMLTools.getChildElementsTextValue(ulItemElement, "state");
        String lastUploadDate = XMLTools.getChildElementsTextValue(ulItemElement, "lastuploaddate");
	    String sharedDate = XMLTools.getChildElementsTextValue(ulItemElement, "dateShared");
        String key = XMLTools.getChildElementsCDATAValue(ulItemElement, "key");
	    String SHA1 = XMLTools.getChildElementsCDATAValue(ulItemElement, "SHA1");
	    String batch = XMLTools.getChildElementsTextValue(ulItemElement, "batch");

// batch is allowed to be null, i think
        if( filename == null || filepath == null || targetboardname == null || state == null )//|| batch==null)
        {
			logger.warning("UploadTable: Error in XML save file, skipping entry.");
            return null;
        }

        int iState = -1;
        try { iState = Integer.parseInt( state ); }
        catch(NumberFormatException ex)
        {
            // string is no number -> old format
            iState = -1;
        }

        if( iState < 0 )
        {
            // old format: states are saved in XML as LangRes Strings
            if( state.indexOf("Kb") != -1 || state.equals(LangRes.getString("Uploading")) )
            {
                iState = FrostUploadItem.STATE_REQUESTED;
            }
        }
        else
        {
            // new format: states are saved in XML as numbers
            if( iState == FrostUploadItem.STATE_PROGRESS ||
                iState == FrostUploadItem.STATE_UPLOADING )
            {
                iState = FrostUploadItem.STATE_REQUESTED;
            }
            else if( iState == FrostUploadItem.STATE_ENCODING ||
                     iState == FrostUploadItem.STATE_ENCODING_REQUESTED )
            {
                iState = FrostUploadItem.STATE_IDLE;
            }
        }

        if( key != null && key.startsWith("CHK@") == false )
        {
            key = null;
        }

        File uploadFile = new File(filepath);

        if( !uploadFile.isFile() || uploadFile.length() == 0 )
        {
            logger.warning("UploadTable: file '" + filepath + "' was not found, removing file from table.");
            return null;
        }

        // check if target board exists in board tree
        FrostBoardObject board = frame1.getInstance().getTofTree().getBoardByName( targetboardname );
        if( board == null )
        {
            logger.warning("UploadTable: target board '" + targetboardname + "' for file '" + filepath + "' was not found, removing file from table.");
            return null;
        }

        // create FrostUploadItem
        FrostUploadItem ulItem = new FrostUploadItem( filename,
                                                                  filepath,
                                                                  uploadFile.length(),
                                                                  board,
                                                                  iState,
                                                                  lastUploadDate,
                                                                  key,
								  SHA1);
	    ulItem.setBatch(batch);
        return ulItem;
    }

////////  DOWNLOAD TABLE  /////////

    public static boolean loadDownloadTableItems( DownloadTableModel model, String filename )
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename, false);
        } catch(Exception ex) { ; } // xml format error

        if( doc == null )
            return false;

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostDownloadTable") == false )
        {
            logger.severe("Error - uploads.xml invalid: does not contain the root tag 'FrostDownloadTable'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostDownloadTableItemList");
        if( nodelist.size() != 1 )
            return false;

        Element itemListRootNode = (Element)nodelist.get(0);

        nodelist = XMLTools.getChildElementsByTagName( itemListRootNode, "FrostDownloadTableItem" );

        if( nodelist.size() == 0 )
            return true; // empty save file

        for( int x=0; x<nodelist.size(); x++ )
        {
            Element downloadItemElement = (Element)nodelist.get(x);
            appendDownloadTableItemToModel( downloadItemElement, model );
        }
		logger.info("Loaded " + nodelist.size() + " items into download table.");
        return true;
    }

    protected static void appendDownloadTableItemToModel(Element dlItemElement, DownloadTableModel model)
    {
        FrostDownloadItem dlObj = getDownloadItemFromElement( dlItemElement );
        if( dlObj == null )
            return;
        model.addRow( dlObj );
    }

    protected static FrostDownloadItem getDownloadItemFromElement(Element dlItemElement)
    {
        String filename = XMLTools.getChildElementsCDATAValue(dlItemElement, "filename");
        String filesize = XMLTools.getChildElementsTextValue(dlItemElement, "filesize");
        String fileage = XMLTools.getChildElementsTextValue(dlItemElement, "fileage");
        String key = XMLTools.getChildElementsCDATAValue(dlItemElement, "key");
        String retries = XMLTools.getChildElementsTextValue(dlItemElement, "retries");
        String state = XMLTools.getChildElementsTextValue(dlItemElement, "state");
	    String owner = XMLTools.getChildElementsTextValue(dlItemElement, "owner");
        String sourceboardname = XMLTools.getChildElementsTextValue(dlItemElement, "sourceboard");
        String enableDownload = dlItemElement.getAttribute("enableDownload");
	    String SHA1 = XMLTools.getChildElementsTextValue(dlItemElement, "SHA1");
    	String batch = XMLTools.getChildElementsTextValue(dlItemElement, "batch");
    	String redirect = XMLTools.getChildElementsCDATAValue(dlItemElement,"redirect");

        //  SHA1 val is not available when adding downloads using textbox
        // one of key or SHA1 must be available
        if( filename == null || state == null ||
            ( key == null && SHA1 == null ) ) 
        {
            logger.warning("DownloadTable: Error in XML save file, skipping entry.");
            return null;
        }

        int iState = -1;
        try { iState = Integer.parseInt( state ); }
        catch(NumberFormatException ex)
        {
            // string is no number -> old format
            iState = -1;
        }

        if( iState < 0 )
        {
            // old format: states are saved in XML as LangRes Strings
            if( state.equals(LangRes.getString("Done")) == false )
            {
                iState = FrostDownloadItem.STATE_WAITING;
            }
        }
        else
        {
            // new format: states are saved in XML as numbers
            if( iState != FrostDownloadItem.STATE_DONE )
            {
                iState = FrostDownloadItem.STATE_WAITING;
            }
        }

        boolean isDownloadEnabled = false;
        if( enableDownload == null ||
            enableDownload.length() == 0 ||
            enableDownload.toLowerCase().equals("true") )
        {
            isDownloadEnabled = true; // default is true
        }

        // check if target board exists in board tree

        FrostBoardObject board = null;
        if( sourceboardname != null )
        {
            board = frame1.getInstance().getTofTree().getBoardByName( sourceboardname );
            if( board == null )
            {
                logger.warning("DownloadTable: source board '" + sourceboardname + "' for file '" + filename + "' was not found, removing file from table.");
                return null;
            }
        }

        // create FrostDownloadItem
        FrostDownloadItem dlItem = new FrostDownloadItem(filename,
                                                                     filesize,
                                                                     fileage,
                                                                     key,
                                                                     retries,
								     owner,
								     SHA1,
                                                                     iState,
                                                                     isDownloadEnabled,
                                                                     board);
	dlItem.setBatch(batch);
	dlItem.setRedirect(redirect);
        return dlItem;
    }

	/**************************************************
	 * TABLE SAVE METHODS *****************************
	 **************************************************/

	////////  UPLOAD MODEL  /////////

	public static boolean saveUploadModel(UploadModel model, String filename) {
		Document doc = XMLTools.createDomDocument();
		if (doc == null) {
			logger.severe("Error - saveUploadTableItems: factory could'nt create XML Document.");
			return false;
		}

		Element rootElement = doc.createElement("FrostUploadTable");
		doc.appendChild(rootElement);

		Element itemsRoot = doc.createElement("FrostUploadTableItemList");
		rootElement.appendChild(itemsRoot);

		// now add all items to itemsRoot
		for (int x = 0; x < model.getItemCount(); x++) {
			FrostUploadItem ulItem = (FrostUploadItem) model.getItemAt(x);
			appendUploadItemToDomTree(itemsRoot, ulItem, doc);
		}

		boolean writeOK = false;
		try {
			writeOK = XMLTools.writeXmlFile(doc, filename);
			logger.info("Saved " + model.getItemCount() + " items from upload table.");
		} catch (Throwable t) {
			logger.log(
				Level.SEVERE,
				"Exception - saveUploadModel\n" + "ERROR saving upload table!",
				t);
		}
		return writeOK;
	}

    protected static void appendUploadItemToDomTree( Element parent, FrostUploadItem ulItem, Document doc )
    {
        Element itemElement = doc.createElement("FrostUploadTableItem");
        Element element;
        Text text;
        CDATASection cdata;
        // filename
        element = doc.createElement("filename");
        cdata = doc.createCDATASection( ulItem.getFileName() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // filepath
        element = doc.createElement("filepath");
        cdata = doc.createCDATASection( ulItem.getFilePath() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // targetboard
        element = doc.createElement("targetboard");
        text = doc.createTextNode( ulItem.getTargetBoard().toString() );
        element.appendChild( text );
        itemElement.appendChild( element );
        // state
        element = doc.createElement("state");
        text = doc.createTextNode( String.valueOf(ulItem.getState()) );
        element.appendChild( text );
        itemElement.appendChild( element );
    	//batch -all upload elements have it
    	element = doc.createElement("batch");
    	text = doc.createTextNode(ulItem.getBatch());
    	element.appendChild(text);
    	itemElement.appendChild(element);
        // key
        if( ulItem.getKey() != null )
        {
            element = doc.createElement("key");
            cdata = doc.createCDATASection( ulItem.getKey() );
            element.appendChild( cdata );
            itemElement.appendChild( element );
        }
	    if( ulItem.getSHA1() != null )
        {
            element = doc.createElement("SHA1");
            cdata = doc.createCDATASection( ulItem.getSHA1() );
            element.appendChild( cdata );
            itemElement.appendChild( element );
        }
        // lastUploadDate
        if( ulItem.getLastUploadDate() != null )
        {
            element = doc.createElement("lastuploaddate");
            text = doc.createTextNode( ulItem.getLastUploadDate() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }
	

        parent.appendChild( itemElement );
    }

////////  DOWNLOAD TABLE  /////////

    public static boolean saveDownloadTableItems( DownloadTableModel model, String filename )
    {
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            logger.severe("Error - saveDownloadTableItems: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostDownloadTable");
        doc.appendChild(rootElement);

        Element itemsRoot = doc.createElement("FrostDownloadTableItemList");
        rootElement.appendChild( itemsRoot );

        // now add all items to itemsRoot
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostDownloadItem dlItem = (FrostDownloadItem)model.getRow( x );
            appendDownloadItemToDomTree( itemsRoot, dlItem, doc );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, filename);
			logger.info("Saved " + model.getRowCount() + " items from download table.");
        } catch(Throwable t)
        {
			logger.log(Level.SEVERE, "Exception - saveDownloadTableItems\n" +
									 "ERROR saving download table!", t);
        }

        return writeOK;
    }

    protected static void appendDownloadItemToDomTree( Element parent, FrostDownloadItem dlItem, Document doc )
    {
        Element itemElement = doc.createElement("FrostDownloadTableItem");
        String isDownloadEnabled;
        if( dlItem.getEnableDownload() == null )
            isDownloadEnabled = "true";
        else
            isDownloadEnabled = dlItem.getEnableDownload().toString();
        itemElement.setAttribute("enableDownload", isDownloadEnabled );

        Element element;
        Text text;
        CDATASection cdata;
        // filename
        element = doc.createElement("filename");
        cdata = doc.createCDATASection( dlItem.getFileName() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // filesize
        if( dlItem.getFileSize() != null )
        {
            element = doc.createElement("filesize");
            text = doc.createTextNode( dlItem.getFileSize().toString() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }
        // fileage
        element = doc.createElement("fileage");
        text = doc.createTextNode( dlItem.getFileAge() );
        element.appendChild( text );
        itemElement.appendChild( element );
        // key
        element = doc.createElement("key");
        cdata = doc.createCDATASection( dlItem.getKey() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // retries
        element = doc.createElement("retries");
        text = doc.createTextNode( String.valueOf(dlItem.getRetries()) );
        element.appendChild( text );
        itemElement.appendChild( element );
        // state
        element = doc.createElement("state");
        text = doc.createTextNode( String.valueOf(dlItem.getState()) );
        element.appendChild( text );
        itemElement.appendChild( element );
	//SHA1
	element = doc.createElement("SHA1");
        text = doc.createTextNode( dlItem.getSHA1() );
        element.appendChild( text );
        itemElement.appendChild( element );
	//batch - not all elements may have batches
	if (dlItem.getBatch() != null) {
		element = doc.createElement("batch");
		text = doc.createTextNode (dlItem.getBatch());
		element.appendChild(text);
		itemElement.appendChild(element);
	}
	
	//redirect - for redirect files
	if (dlItem.getRedirect()!=null){
		element = doc.createElement("redirect");
		cdata= doc.createCDATASection (dlItem.getRedirect());
		element.appendChild(cdata);
		itemElement.appendChild(element);
	}
	//owner
	if (dlItem.getOwner() != null &&
		dlItem.getOwner().compareToIgnoreCase("anonymous") != 0) {
	element = doc.createElement("owner");
        text = doc.createTextNode( String.valueOf(dlItem.getOwner()) );
        element.appendChild( text );
        itemElement.appendChild( element );
	}
        // sourceboard
        if( dlItem.getSourceBoard() != null )
        {
            element = doc.createElement("sourceboard");
            text = doc.createTextNode( dlItem.getSourceBoard().toString() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }

        parent.appendChild( itemElement );
    }
}
