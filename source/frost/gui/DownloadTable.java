package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import java.io.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;

public class DownloadTable extends SortedTable
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    public DownloadTable(TableModel m)
    {
        super(m);
        // set column sizes
        int[] widths = {250, 90, 90, 80, 40, 50, 60};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // default for sort: sort by state ascending
        sortedColumnIndex = 3;
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
            if( tableItem.getKey().equals( dlItem.getKey() ) &&
                tableItem.getSourceBoard().toString().equals( dlItem.getSourceBoard().toString() )
              )
            {
                // already in model (compared by key+board)
                return false;
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
            if( dlItem.getState() == dlItem.STATE_DONE )
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
    public void update(SettingsClass frostSettings)
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
                    //dlItem.setState( downloaded/1024 + " Kb" );
                }
            }
            tableModel.updateRow( dlItem ); // finally tell model that row was updated
        }
    }

    /**
     * Removes chunks of selected download files from keypool.
     */
    public void removeSelectedChunks()
    {
        // start a Thread for this, this method is called from Swing thread and should not block swing
        System.out.println("Removing chunks");

        DownloadTableModel tableModel = (DownloadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        ArrayList oldChunkFilesList = new ArrayList( selectedRows.length + 1 );
        for( int i = 0; i < selectedRows.length; i++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)tableModel.getRow( selectedRows[i] );
            oldChunkFilesList.add( dlItem.getFileName() );
        }

        String keypoolDir = frame1.frostSettings.getValue("keypool.dir");

        RemoveSelectedFilesChunksThread t = new RemoveSelectedFilesChunksThread( oldChunkFilesList, keypoolDir );
        t.start();
    }

    private class RemoveSelectedFilesChunksThread extends Thread
    {
        ArrayList oldChunkFilesList;
        String keypoolDir;
        public RemoveSelectedFilesChunksThread(ArrayList al, String keypoolDir)
        {
            this.oldChunkFilesList = al;
            this.keypoolDir = keypoolDir;
        }

        public void run()
        {
            File[] files = (new File(keypoolDir)).listFiles();
            for( int i=0; i < oldChunkFilesList.size(); i++ )
            {
                String filename = (String)oldChunkFilesList.get( i );
                System.out.println("Searchin chunks for " + filename);
                for( int j = 0; j < files.length; j++ )
                {
                    if( (files[j].getName()).startsWith( filename ) &&
                        files[j].isFile() &&
                        !(files[j].getName()).endsWith(".idx") )
                    {
                        System.out.println("Removing " + files[j].getName());
                        files[j].delete();
                    }
                }
            }
        }
    }
}

