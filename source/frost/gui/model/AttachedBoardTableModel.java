package frost.gui.model;

import java.util.*;

import javax.swing.table.DefaultTableModel;

import frost.gui.objects.Board;
import frost.messages.*;
import frost.util.gui.translation.*;

/**
 * @author $Author$
 * @version $Revision$
 */
public class AttachedBoardTableModel extends DefaultTableModel implements LanguageListener
{
	private Language language = null;
	
	protected final static String columnNames[] = new String[3];

    protected final static Class columnClasses[] = {
        String.class, //"Board Name",
        String.class, //"Access rights"
		String.class, //"Description"
    };

    /**
     * 
     */
    public AttachedBoardTableModel() {
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
		columnNames[0] = language.getString("Board Name");
		columnNames[1] = language.getString("Access rights");
		columnNames[2] = language.getString("Description");	

		fireTableStructureChanged();		
	}
	
	/**
	 * This method fills the table model with the BoardAttachments
	 * in the list passed as a parameter  
	 * @param boardAttachments list of BoardAttachments fo fill the model with
	 */
	public void setData(List boardAttachments) {
		setRowCount(0);
		Iterator boards = boardAttachments.iterator();
		while (boards.hasNext()) {
			BoardAttachment attachment = (BoardAttachment) boards.next();
			Board board = attachment.getBoardObj();
			Object[] row = new Object[3];
			// There is no point in showing a board without name
			if (board.getName() != null) {
				row[0] = board.getName();
				if (board.getPublicKey() == null && board.getPrivateKey() == null) {
					row[1] = "public";
				} else if (board.getPublicKey() != null && board.getPrivateKey() == null) { 
					row[1] = "read - only";
				} else {
					row[1] = "read / write";
				}
				if (board.getDescription() == null) {
					row[2] = "Not present";
				} else {
					row[2] = board.getDescription();
				}
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
