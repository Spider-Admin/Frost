package frost.gui.model;

public interface TableMember
{
    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column);

    public int compareTo( TableMember anOther, int tableColumIndex );

}
