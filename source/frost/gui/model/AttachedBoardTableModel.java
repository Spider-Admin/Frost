package frost.gui.model;

import javax.swing.table.DefaultTableModel;

import frost.util.gui.translation.*;

/**
 * @author $author$
 * @version $revision$
 */
public class AttachedBoardTableModel extends DefaultTableModel implements LanguageListener
{
	private UpdatingLanguageResource languageResource = null;
	
	protected final static String columnNames[] = new String[3];

    protected final static Class columnClasses[] = {
        String.class, //"Board Name",
        String.class, //"Access rights"
		String.class, //"Description"
    };

    /**
     * @param languageResource
     */
    public AttachedBoardTableModel(UpdatingLanguageResource languageResource) {
		super();
		this.languageResource = languageResource;
		refreshLanguage();
	}

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
		columnNames[0] = languageResource.getString("Board Name");
		columnNames[1] = languageResource.getString("Access rights");
		columnNames[2] = languageResource.getString("Description");	

		fireTableStructureChanged();		
	}

    public String getColumnName(int column)
    {
        if( column >= 0 && column < columnNames.length )
            return columnNames[column];
        return null;
    }
    public int getColumnCount()
    {
        return columnNames.length;
    }
    public Class getColumnClass(int column)
    {
        if( column >= 0 && column < columnClasses.length )
            return columnClasses[column];
        return null;
    }
}
