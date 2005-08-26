/*
  AttachedFilesTableModel.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

package frost.gui.model;

import java.util.*;

import javax.swing.table.DefaultTableModel;

import frost.messages.*;
import frost.util.gui.translation.*;

public class AttachedFilesTableModel extends DefaultTableModel implements LanguageListener
{
	private Language language = null;
	
	protected final static String columnNames[] = new String[2];

    protected final static Class columnClasses[] = {
        String.class, //LangRes.getString("Filename"),
        String.class //LangRes.getString("Size")
    };

    /**
     * 
     */
    public AttachedFilesTableModel() {
		super();
		language = Language.getInstance();
		language.addLanguageListener(this);
		refreshLanguage();
	}

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }
    
	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();			
	}
	
	/**
	 * 
	 */
	private void refreshLanguage() {
		columnNames[0] = language.getString("Filename");
		columnNames[1] = language.getString("Size");

		fireTableStructureChanged();		
	}
	
	/**
	 * This method fills the table model with the FileAttachments
	 * in the list passed as a parameter  
	 * @param fileAttachments list of FileAttachments fo fill the model with
	 */
	public void setData(List fileAttachments) {
		setRowCount(0);
		Iterator files = fileAttachments.iterator();
		while (files.hasNext()) {
			FileAttachment attachment = (FileAttachment) files.next();
			SharedFileObject fileObject = attachment.getFileObj();
			// There is no point in showing a file without key / name
			if (fileObject.getKey() != null && fileObject.getKey().length() > 40 &&
				fileObject.getFilename() != null && fileObject.getFilename().length() > 0 ) {
				Object[] row = {fileObject.getFilename(), fileObject.getSize()};
				addRow(row);
			}
		}
	}

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int column)
    {
        if( column >= 0 && column < columnNames.length )
            return columnNames[column];
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int column)
    {
        if( column >= 0 && column < columnClasses.length )
            return columnClasses[column];
        return null;
    }
}
