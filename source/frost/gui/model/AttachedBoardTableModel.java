package frost.gui.model;

import javax.swing.table.DefaultTableModel;

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
