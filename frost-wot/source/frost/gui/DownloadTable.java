/*
  DownloadTable.java / Frost
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

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;

import frost.frame1;
import frost.gui.model.DownloadTableModel;
import frost.gui.objects.FrostDownloadItemObject;

public class DownloadTable extends SortedTable
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;
    
	private static Logger logger = Logger.getLogger(DownloadTable.class.getName());
	
	private CellRenderer cellRenderer = new CellRenderer();

	public DownloadTable(TableModel m) {
		super(m);
		
		// default for sort: sort by state ascending
		sortedColumnIndex = 4;
		sortedColumnAscending = true;
		resortTable();
	}

    public void removeSelectedRows()
    {
        DownloadTableModel model = (DownloadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        for(int x=selectedRows.length-1; x>=0; x--)
        {
            model.deleteRow( model.getRow(selectedRows[x]) );
        }
    }

    /**
     * Will add this item to table model if not already in model.
     */
    public boolean addDownloadItem(FrostDownloadItemObject dlItem)
    {
        DownloadTableModel model = (DownloadTableModel)getModel();
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostDownloadItemObject tableItem = (FrostDownloadItemObject)model.getRow(x);
            if( tableItem.getSHA1() != null && tableItem.getSHA1().equals( dlItem.getSHA1() ) &&
                tableItem.getSourceBoard().toString().equals( dlItem.getSourceBoard().toString() )
              )
            {
                // already in model (compared by SHA1)
                return false;
            }
            if( dlItem.getKey() != null && tableItem.getKey()!=null &&
            			tableItem.getKey().equals(dlItem.getKey()) )
            {
                // already in model (compared by key)
                return false;
            }
            if( tableItem.getFileName().equals(dlItem.getFileName()) )
            {
                // same name, but different key. - rename quitely
                int cnt = 2; 
                while(true) {
                    String nextNewName = dlItem.getFileName() + "_" + cnt;
                    dlItem.setFileName(nextNewName);
                    if( addDownloadItem(dlItem) == true )
                    {
                        // added to model
                        return true;
                    }
                    cnt++;
                }
                // we should never come here
           }
        }
        // not in model, add
        model.addRow( dlItem );
        return true;
    }

    /**
     * Removes finished downloads from the download table.
     */
    public void removeFinishedDownloads()
    {
        // Need to synchronize with other places where the table is changed
        DownloadTableModel tableModel = (DownloadTableModel)getModel();
        for( int i = tableModel.getRowCount()  - 1; i >= 0; i-- )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)tableModel.getRow( i );
            if( dlItem.getState() == FrostDownloadItemObject.STATE_DONE )
            {
                tableModel.deleteRow( dlItem );
            }
        }
    }

    /**
     * Load downloadlist from file.
     */
    public boolean load()
    {
        String filename = frame1.frostSettings.getValue("config.dir") + "downloads.xml";
        // the call changes the tablemodel and loads nodes into it
        File iniFile = new File(filename);
        if( iniFile.exists() == false )
        {
            return true; // nothing loaded, but no error
        }
        return TableXmlIO.loadDownloadTableItems( (DownloadTableModel)getModel(), filename );
    }

    /**
     * Save downloadlist to file.
     */
    public boolean save()
    {
        String filename = frame1.frostSettings.getValue("config.dir") + "downloads.xml";
        File check = new File( filename );
        if( check.exists() )
        {
            // rename old file to .bak, overwrite older .bak
            String bakFilename = frame1.frostSettings.getValue("config.dir") + "downloads.xml.bak";
            File bakFile = new File(bakFilename);
            if( bakFile.exists() )
            {
                bakFile.delete();
            }
            check.renameTo(bakFile);
        }
        return TableXmlIO.saveDownloadTableItems( (DownloadTableModel)getModel(), filename );
    }

    /**
     * Updates the download table
     * @param table the downloadTable
     * @param maxDownloadHtl Request htl's will not exceed this value
     * @param keypoolDirectory This directory should contain the temporary chunks
     * @param downloadDirectory This directory should contain the downloaded file
     */
/*    public void update(SettingsClass frostSettings)
    {
        int maxDownloadHtl = frostSettings.getIntValue("htlMax");
        File keypoolDirectory = new File( frostSettings.getValue("keypool.dir") );
        File downloadDirectory = new File( frostSettings.getValue("downloadDirectory") );

        DownloadTableModel tableModel = (DownloadTableModel)getModel();
        File[] chunkList = keypoolDirectory.listFiles();
        String fileSeparator = System.getProperty("file.separator");

        for( int i = 0; i < tableModel.getRowCount(); i++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)tableModel.getRow( i );
            // Download / bytes read
            if( dlItem.getState() == dlItem.STATE_TRYING ||
                dlItem.getState() == dlItem.STATE_PROGRESS )
            {
                File newFile = new File(downloadDirectory + fileSeparator + dlItem.getFileName() + ".tmp");
                if( newFile.exists() )
                {
                    long downloaded = newFile.length();
                    if( chunkList != null )
                    {
                        for( int j = 0; j < chunkList.length; j++ )
                        {
                            if( chunkList[j].getName().startsWith(dlItem.getFileName() + ".tmp-chunk-") )
                                downloaded += chunkList[j].length();
                            if( chunkList[j].getName().startsWith(dlItem.getFileName() + ".tmp-check-") )
                                downloaded += chunkList[j].length();
                        }
                    }
                    dlItem.setDownloadProgress( downloaded );
                    dlItem.setState( dlItem.STATE_PROGRESS );
                }
            }
            tableModel.updateRow( dlItem ); // finally tell model that row was updated
        }
    }*/

    /**
     * Removes chunks of selected download files from keypool.
     */
    public void removeSelectedChunks()
    {
        // start a Thread for this, this method is called from Swing thread and should not block swing
        logger.info("Removing chunks");

        DownloadTableModel tableModel = (DownloadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        ArrayList oldChunkFilesList = new ArrayList( selectedRows.length + 1 );
        for( int i = 0; i < selectedRows.length; i++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)tableModel.getRow( selectedRows[i] );
            oldChunkFilesList.add( dlItem.getFileName() );
        }

        String dlDir = frame1.frostSettings.getValue("downloadDirectory");

        RemoveSelectedFilesChunksThread t = new RemoveSelectedFilesChunksThread( oldChunkFilesList, dlDir );
        t.start();
    }

    private class RemoveSelectedFilesChunksThread extends Thread
    {
        ArrayList oldChunkFilesList;
        String dlDir;
        public RemoveSelectedFilesChunksThread(ArrayList al, String dlDir)
        {
            this.oldChunkFilesList = al;
            this.dlDir = dlDir;
        }

        public void run()
        {
            File[] files = (new File(dlDir)).listFiles();
            for( int i=0; i < oldChunkFilesList.size(); i++ )
            {
                String filename = (String)oldChunkFilesList.get( i );
                for( int j = 0; j < files.length; j++ )
                {
                    // remove filename.data , .redirect, .checkblocks
                    if( files[j].getName().equals( filename + ".data" ) ||
                        files[j].getName().equals( filename + ".redirect" ) ||
                        files[j].getName().equals( filename + ".checkblocks" ) )
                    {
                        logger.info("Removing " + files[j].getName());
                        files[j].delete();
                    }
                }
            }
        }
    }

    /**
     * Called to remove all items from download table.
     */
    public void removeAllItemsFromTable()
    {
        // TODO: also stop all threads!

        selectAll();
        removeSelectedChunks();
        DownloadTableModel model = (DownloadTableModel)getModel();
        model.clearDataModel();
    }

    /**
     * Called to remove selected items from download table.
     */
    public void removeSelectedItemsFromTable()
    {
        // TODO: also stop threads!

        removeSelectedChunks();
        removeSelectedRows();
    }

    /**
     * Called to reset the HTL value of the selected items.
     */
    public void restartSelectedDownloads()
    {
        // TODO: stop thread

        DownloadTableModel dlModel = (DownloadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        for( int x=selectedRows.length-1; x>=0; x-- )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( selectedRows[x] );
            // reset only waiting+failed items
            if( dlItem.getState() == FrostDownloadItemObject.STATE_FAILED ||
                dlItem.getState() == FrostDownloadItemObject.STATE_WAITING ||
                dlItem.getState() == FrostDownloadItemObject.STATE_DONE )
            {
                dlItem.setState( FrostDownloadItemObject.STATE_WAITING );
                dlItem.setRetries(0);
                dlItem.setLastDownloadStopTimeMillis(0);
                dlItem.setEnableDownload( Boolean.valueOf(true) );  // enable download on restart
                dlModel.updateRow( dlItem );
            }
        }
    }

    /**
     * Returns true if the table contains an item with the given key.
     */
    public boolean containsItemWithKey(String key)
    {
        DownloadTableModel model = (DownloadTableModel)getModel();
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)model.getRow(x);
            if( dlItem.getSHA1() != null &&
                dlItem.getSHA1().equals( key ) )
            {
                return true;
            }
        }
        return false;
    }

    public void setDownloadEnabled(int mode, boolean allItems)
    {
        // mode 0=disable items ; 1=enable items ; 2=invert state
        // allItems=true for ALL, else SELECTED
        DownloadTableModel model = (DownloadTableModel)getModel();
        if( allItems == true )
        {
            for( int x=0; x<model.getRowCount(); x++ )
            {
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)model.getRow(x);
                if( dlItem.getState() != FrostDownloadItemObject.STATE_DONE ) // do not enable finished if changing ALL
                {
                    setDownloadEnabled( mode, dlItem );
                    model.updateRow( dlItem );
                }
            }
        }
        else // selected items
        {
            DownloadTableModel dlModel = (DownloadTableModel)getModel();
            int[] selectedRows = getSelectedRows();
            for( int x=selectedRows.length-1; x>=0; x-- )
            {
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( selectedRows[x] );
                setDownloadEnabled( mode, dlItem );
                model.updateRow( dlItem );
            }
        }
    }

    /**
     * mode 0 = set false
     * mode 1 = set true
     * mode 2 = invert
     */
    private void setDownloadEnabled(int mode, FrostDownloadItemObject dlItem)
    {
        if( mode == 0 )
            dlItem.setEnableDownload( Boolean.valueOf(false) );
        else if( mode == 1 )
            dlItem.setEnableDownload( Boolean.valueOf(true) );
        else if( mode == 2 )
        {
            boolean val;
            if( dlItem.getEnableDownload() == null || dlItem.getEnableDownload().booleanValue() == true )
                dlItem.setEnableDownload( Boolean.valueOf( false ) );
            else
                dlItem.setEnableDownload( Boolean.valueOf( true ) );
        }
    }
    
	/* (non-Javadoc)
	 * @see java.awt.Component#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (cellRenderer != null) {
			cellRenderer.setFont(font);
		}
		setRowHeight(font.getSize() + 5);
	}

    /**
     * This renderer renders the size column (=1) with right alignment
     */
    private class CellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            setHorizontalAlignment( SwingConstants.RIGHT );
            // col is right aligned, give some space to next column
            setBorder( new javax.swing.border.EmptyBorder(0, 0, 0, 3) );
            return this;
        }
    }

	/* (non-Javadoc)
	 * @see javax.swing.JTable#createDefaultColumnsFromModel()
	 */
	public void createDefaultColumnsFromModel() {
		super.createDefaultColumnsFromModel();

		// size column
		getColumnModel().getColumn(2).setCellRenderer(cellRenderer);

		// set column sizes
		int[] widths = { 0, 170, 80, 70, 80, 85, 25, 60, 60, 30 };
		for (int i = 0; i < widths.length; i++) // col 0 default
			{
			getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
		// enableDownload column
		getColumnModel().getColumn(0).setResizable(false);

	}

}

