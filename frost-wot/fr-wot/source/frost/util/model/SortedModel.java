/*
 SortedModel.java / Frost
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
package frost.util.model;

import java.util.*;

/**
 * This class is a Model that stores ModelItems in a certain order. That does not
 * mean that it is sorted.
 * 
 * Its implementation is thread-safe
 */
public class SortedModel {

	protected List<ModelItem> data;
	
	private SortedModelListenerSupport listenerSupport;
    
    private boolean ascending;

    private int columnNumber = -1;

    private SortedTableFormat tableFormat;
    private SortedModelTable table = null;

	public SortedModel(SortedTableFormat newFormat) {
		super();
		data = new ArrayList<ModelItem>();
        tableFormat = newFormat;
	}
    
    public void setTable(SortedModelTable t) {
        table = t;
    }
    public SortedModelTable getTable() {
        return table;
    }
    
    public SortedTableFormat getTableFormat() {
        return tableFormat;
    }
	
	/* (non-Javadoc)
	 * @see frost.util.Model#addItem(frost.util.ModelItem)
	 */
	protected void addItem(ModelItem item) {
        
        if (columnNumber == -1) {
            synchronized(data) {
                data.add(item);
                fireItemAdded(item);
            }
            item.setModel(this);
        } else {
            addItem(item, getInsertionPoint(item));
        }
	}
	
	protected void addItem(ModelItem item, int position) {
		synchronized(data) {
			data.add(position, item);
			fireItemAdded(item);
		}
		item.setModel(this);
	}
	
	/**
	 * Adds an OrderedModelListener to the listener list. 
	 * <p>
	 * If listener is null, no exception is thrown and no action is performed.
	 *
	 * @param    listener  the OrderedModelListener to be added
	 */
	public synchronized void addOrderedModelListener(SortedModelListener listener) {
		if (listener == null) {
			return;
		}
		if (listenerSupport == null) {
			listenerSupport = new SortedModelListenerSupport();
		}
		listenerSupport.addModelListener(listener);
	}

	public synchronized void clear() {
		synchronized (data) {
			Iterator iterator = data.iterator();
			while (iterator.hasNext()) {
				ModelItem item = (ModelItem) iterator.next();
				item.setModel(null);
			}
			data.clear();

            getTable().fireTableDataChanged();         

            if (listenerSupport == null) {
                return;
            }
            listenerSupport.fireModelCleared();
		}
	}
	
	protected void fireItemAdded(ModelItem item) {
        
        int position = data.indexOf(item);
        getTable().fireTableRowsInserted(position, position);          
        
		if (listenerSupport == null) {
			return;
		}
		listenerSupport.fireItemAdded(item, position);
	}
	
	protected void fireItemChanged(ModelItem item) {
        
        if (columnNumber == -1) {
            fireItemChanged(item);
        } else {
            // maybe reinsert, first check if position would change
            boolean reinsert = true;
            if( data.size() > 1 ) {
                int p = data.indexOf(item);
                Comparator cmp = getComparator();
                if( p == 0 ) {
                    // first item, compare with second item
                    ModelItem compItem = data.get(1);
                    if( cmp.compare(item, compItem) <= 0 ) {
                        // no need to resort
                        reinsert = false;
                    }
                } else if( p == data.size()-1 ){
                    // last item, compare with preceeding item
                    ModelItem compItem = data.get(p-1);
                    if( cmp.compare(item, compItem) >= 0 ) {
                        // no need to resort
                        reinsert = false;
                    }
                } else {
                    // middle item, compare with preceeding and following item
                    ModelItem compItem1 = data.get(p-1);
                    ModelItem compItem2 = data.get(p+1);
                    if( cmp.compare(item, compItem1) >= 0 ) {
                        if( cmp.compare(item, compItem2) <= 0 ) {
                            // no need to resort
                            reinsert = false;
                        }                            
                    }
                }
            } else {
                // only 1 item in table, no need to reinsert
                reinsert = false;
            }

            if( reinsert ) {
                data.remove(item);
                data.add(getInsertionPoint(item), item);
                table.fireTableDataChanged();
            }
        }   

        int position = data.indexOf(item);
        getTable().fireTableRowsUpdated(position, position);           
        
		if (listenerSupport == null) {
			return;
		}
		listenerSupport.fireItemChanged(item, position);
	}
	
    void itemChanged(ModelItem item) {
        fireItemChanged(item);
    }

	private void fireItemsRemoved(int[] positions, ModelItem[] items) {
        
        getTable().fireTableRowsDeleted(positions);            
        
		if (listenerSupport == null) {
			return;
		}
		listenerSupport.fireItemsRemoved(positions, items);		
	}

	public ModelItem getItemAt(int position) {
        if( position >= data.size() ) {
            System.out.println("SortedModel.getItemAt: position="+position+", but size="+data.size());
            return null;
        }
		return (ModelItem) data.get(position);
	}

	public int getItemCount() {
		return data.size();
	}
    
    /**
     * @return  a new List containing all items of this model, in unspecific order
     */
    public List getItems() {
        return new ArrayList<ModelItem>(data);
    }
	
	/**
	 * Returns the index in this model of the first occurrence of the specified
     * item, or -1 if this model does not contain this element.
     * 
     * @param item item to search for.
	 * @return the index in this model of the first occurrence of the specified
     * 	       item, or -1 if this model does not contain this element.
	 */
	public int indexOf(ModelItem item) {
		return data.indexOf(item);
	}

	/* (non-Javadoc)
	 * @see frost.util.Model#removeItems(frost.util.ModelItem)
	 */
	public boolean removeItems(ModelItem[] items) {
		//We clear the link to the model of each item
		for (int i = 0; i < items.length; i++) {
			items[i].setModel(null);
		}
		//We remove the first occurrence of each item from the model
		int[] removedPositions = new int[items.length];
		ModelItem[] removedItems = new ModelItem[items.length];
		int count = 0;
		synchronized (data) {
			for (int i = 0; i < items.length; i++) {
				int position = data.indexOf(items[i]);
				if (position != -1) {
					data.remove(position);
					removedItems[count] = items[i];
					removedPositions[count] = position;
					count++;
				}
			}
		}
		//We send an items removed event. Only those items that actually
		//were in the model and thus were removed are included in the event.
		if (count != 0) {
			int[] croppedPositions = new int[count];
			ModelItem[] croppedItems = new ModelItem[count];
			System.arraycopy(removedPositions, 0, croppedPositions, 0, count);
			System.arraycopy(removedItems, 0, croppedItems, 0, count);
			fireItemsRemoved(croppedPositions, croppedItems);
			return true;
		} else {
			return false;	
		}
	}
    
    protected void sort(int newColumnNumber, boolean newAscending) {
        columnNumber = newColumnNumber;
        ascending = newAscending;
        Collections.sort(data, getComparator());        
    }
    
    private Comparator getComparator() {
        if (ascending) { 
            return tableFormat.getComparator(columnNumber);
        } else {
            return tableFormat.getReverseComparator(columnNumber);
        }
    }

    private int getInsertionPoint(ModelItem item) {
        int position = Collections.binarySearch(data, item, getComparator());
        if (position < 0) {
            // No similar item was in the list
            position = (position + 1) * -1;
        } else {
            // There was already a similar item (or more) in the list.
            // We find out the end position of that sublist and use it as insertion point.
            position = Collections.lastIndexOfSubList(data, Collections.singletonList(data.get(position)));
            position++;
        }
        return position;
    }   
}
