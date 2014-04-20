/*
  TableMember.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

public interface TableMember<T extends TableMember<T>> {
	/**
	 * Returns the object representing value of column. Can be string or icon
	 *
	 * @param   column  Column to be displayed
	 * @return  Object representing table entry.
	 */
	public Comparable<?> getValueAt(int column);

	public int compareTo(T anOther, int tableColumnIndex );
	
	abstract public class BaseTableMember<T extends BaseTableMember<T>> implements TableMember<T> {
		
		@SuppressWarnings("unchecked")
		public int compareTo(T anOther, int tableColumnIndex ) {
			assert tableColumnIndex >= 0;
			final Comparable<?> c1 = getValueAt(tableColumnIndex);
			final Comparable<?> c2 = anOther.getValueAt(tableColumnIndex);
			if( c1.getClass() != c2.getClass()) {
				throw new ClassCastException("Column Items not of same type");
			}
			if( c1 instanceof String ) {
				return ((String) c1).compareToIgnoreCase((String) c2);
			}
			return c1.getClass().cast(c1).compareTo(c1.getClass().cast(c2));
		}
	}
}

