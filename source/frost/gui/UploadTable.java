package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import java.io.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;

public class UploadTable extends SortedTable
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    public UploadTable(TableModel m)
    {
        super(m);
        // set column sizes
        int[] widths = {250, 80, 80, 80, 80, 80};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // default for sort: sort by ... ?
        sortedColumnIndex = 0;
        sortedColumnAscending = true;
        resortTable();
    }

    public void removeSelectedRows()
    {
        UploadTableModel model = (UploadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        for(int x=selectedRows.length-1; x>=0; x--)
        {
            model.deleteRow( model.getRow(selectedRows[x]) );
        }
    }

    /**
     * Will add this item to table model if not already in model.
     */
    public boolean addUploadItem(FrostUploadItemObject ulItem)
    {
        UploadTableModel model = (UploadTableModel)getModel();
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostUploadItemObject tableItem = (FrostUploadItemObject)model.getRow(x);
            if( tableItem.getFilePath().equals(ulItem.getFilePath()) )
            {
                // already in model (compared by path)

                return false;
            }
        }
        // not in model, add
        model.addRow( ulItem );
        return true;
    }

    /**
     * Restores the original filenames in upload table.
     * Uses the pathname of the file.
     */
    public void restoreOriginalFilenamesForSelectedRows()
    {
        UploadTableModel model = (UploadTableModel)getModel();
        int[] selectedRows = getSelectedRows();
        for( int x=0; x<selectedRows.length; x++ )
        {
            FrostUploadItemObject ulItem = (FrostUploadItemObject)model.getRow( selectedRows[x] );
            File origFile = new File( ulItem.getFilePath() );
            if( origFile.isFile() )
            {
                ulItem.setFileName( origFile.getName() );
                model.updateRow( ulItem );
            }
        }
    }

    /**
     * Adds a prefix (choosed by user) to all selected filenames in upload table.
     */
    public void setPrefixForSelectedFiles()
    {
        String prefix = JOptionPane.showInputDialog(LangRes.getString("Please enter the prefix you want to use for your files."));
        if( prefix != null )
        {
            UploadTableModel tableModel = (UploadTableModel)getModel();
            // We need to synchronize accesses to the table
            int[] selectedRows = getSelectedRows();
            for( int i = 0; i < selectedRows.length; i++ )
            {
                FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( selectedRows[i] );
                String newName = prefix + ulItem.getFileName();
                ulItem.setFileName( newName );
                tableModel.updateRow( ulItem );
            }
        }
    }

    /**
     * Checks if all files in upload table still exist on hard disc.
     */
    public void removeNotExistingFiles()
    {
        UploadTableModel tableModel = (UploadTableModel)getModel();
        for( int i = tableModel.getRowCount() - 1; i >= 0; i-- )
        {
            FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( i );
            File checkMe = new File( ulItem.getFilePath() );
            if( !checkMe.exists() )
            {
                tableModel.deleteRow( ulItem );
            }
        }
    }

    /**
     * Adds all files in upload table to the Index class.
     * This Index will later be uploaded to freenet.
     */
    public void addFilesToBoardIndex()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        Date today = new Date();
        String date = formatter.format(today);

        UploadTableModel tableModel = (UploadTableModel)getModel();
        for( int i = 0; i < tableModel.getRowCount(); i++ )
        {
            FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( i );
            if( ulItem.getKey() != null && ulItem.getKey().startsWith("CHK@") )
            {
                KeyClass newKey = new KeyClass( ulItem.getKey() );
                newKey.setFilename( ulItem.getFileName() );
                newKey.setSize( ulItem.getFileSize().toString() ); // TODO: pass object, not string
                newKey.setDate( date );
                newKey.setExchange(false);
                Index.add( newKey, new File(frame1.keypool + ulItem.getTargetBoard().getBoardFilename() ) );
            }
        }
    }

    /**
     * Loads the upload table from disk
     */
    public boolean load()
    {
        String filename = frame1.frostSettings.getValue("config.dir") + "uploads.xml";
        // the call changes the tablemodel and loads nodes into it
        File iniFile = new File(filename);
        if( iniFile.exists() == false )
        {
            return true; // nothing loaded, but no error
        }
        return TableXmlIO.loadUploadTableItems( (UploadTableModel)getModel(), filename );
    }

    /**
     * Saves the upload table to disk.
     */
    public boolean save()
    {
        String filename = frame1.frostSettings.getValue("config.dir") + "uploads.xml";
        File check = new File( filename );
        if( check.exists() )
        {
            // rename old file to .bak, overwrite older .bak
            String bakFilename = frame1.frostSettings.getValue("config.dir") + "uploads.xml.bak";
            File bakFile = new File(bakFilename);
            if( bakFile.exists() )
            {
                bakFile.delete();
            }
            check.renameTo(bakFile);
        }
        return TableXmlIO.saveUploadTableItems( (UploadTableModel)getModel(), filename );
    }

    /**
     * Returns true if the table contains an item with the given key.
     */
    public boolean containsItemWithKey(String key)
    {
        UploadTableModel model = (UploadTableModel)getModel();
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostUploadItemObject ulItem = (FrostUploadItemObject)model.getRow(x);
            if( ulItem.getKey() != null &&
                ulItem.getKey().equals( key ) )
            {
                return true;
            }
        }
        return false;
    }

}

