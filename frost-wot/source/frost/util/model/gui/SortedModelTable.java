/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.logging.*;

import javax.swing.SwingUtilities;

import frost.util.gui.SwingWorker;
import frost.util.model.*;

/**
 * @author Administrator
 *
 * //TODO: Solve race conditions in the SortedSelectionGetter and in getValueAt() 
 * 		    when deleting rows.
 */
public class SortedModelTable extends ModelTable {
	/**
	 * Helper class to be able to safely get the selection fron any thread
	 */
	private class SortedSelectionGetter implements Runnable {
	
		private final int MODE_SINGLE = 0;
		private final int MODE_MULTIPLE = 1;
	
		int mode = 0;
	
		ModelItem[] selectedItems;
		ModelItem selectedItem;
	
		/**
		 * 
		 */
		public ModelItem[] getSelectedItems() {
			mode = MODE_MULTIPLE;
			if (SwingUtilities.isEventDispatchThread()) {
				run();
			} else {
				try {
					SwingUtilities.invokeAndWait(this);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				} catch (InvocationTargetException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				}
			}
			return selectedItems;
		}
	
		/**
		 * 
		 */
		public ModelItem getSelectedItem() {
			mode = MODE_SINGLE;
			if (SwingUtilities.isEventDispatchThread()) {
				run();
			} else {
				try {
					SwingUtilities.invokeAndWait(this);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				} catch (InvocationTargetException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				}
			}
			return selectedItem;
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			synchronized (model) {
				switch (mode) {
					case MODE_MULTIPLE :
						try {
							int selectionCount = table.getSelectedRowCount();
							selectedItems = new ModelItem[selectionCount];
							int[] selectedRows = table.getSelectedRows();
							for (int i = 0; i < selectedRows.length; i++) {
								int pos = sortingArray[selectedRows[i]];
								selectedItems[i] = model.getItemAt(pos);
							}
						} catch (Exception exception) {
							logger.log(
								Level.FINE,
								"Race condition in SortedSelectionGetter.run()",
								exception);
							selectedItems = new ModelItem[0];
						}
						break;
					case MODE_SINGLE :
						try {
							int selectedRow = table.getSelectedRow();
							if (selectedRow != -1) {
								int pos = sortingArray[selectedRow];
								selectedItem = model.getItemAt(pos);
							}
						} catch (Exception exception) {
							logger.log(
								Level.FINE,
								"Race condition in SortedSelectionGetter.run()",
								exception);
						}
						break;
				}
			}
		}
	
	}
	
	private SortedTableFormat sortedTableFormat;

	private static Logger logger = Logger.getLogger(SortedModelTable.class.getName());
	
	private static final int INSERTIONSORT_THRESHOLD = 7;	
	
	private boolean sorted = false;
	private int currentColumnNumber = -1;
	private boolean ascending;
	
	private int[] sortingArray = new int[50];
	private int itemsCount;
	
	/**
	 * @param newModel
	 * @param newTableFormat
	 */
	public SortedModelTable(
		OrderedModel newModel,
		SortedTableFormat newTableFormat) {
			
		super(newModel, newTableFormat);
		
		sortedTableFormat = newTableFormat;
		
		getTable().setTableHeader(new SortedTableHeader(this));
	}
	/**
	 * @param columnNumber
	 */
	void columnClicked(int columnNumber) {
		if (columnNumber != currentColumnNumber) {
			currentColumnNumber = columnNumber;
			ascending = true;
		} else {
			ascending = !ascending;
		}

		SwingWorker worker = new SwingWorker(table) {

			private int[] newSorterArray;

			protected void doNonUILogic() throws RuntimeException {
				newSorterArray = getNewSortedArray();
			}

			protected void doUIUpdateLogic() throws RuntimeException {
				sortingArray = newSorterArray;
				sorted = true;
				table.revalidate();
				table.repaint();
			}

		};
		worker.start();
	}
	
	/**
	 * @return
	 */
	public ModelItem getSelectedItem() {
		if (sorted) {
			return new SortedSelectionGetter().getSelectedItem();
		} else {
			return new SelectionGetter().getSelectedItem();
		}
	}
	

	/**
	 * @return
	 */
	public ModelItem[] getSelectedItems() {
		if (sorted) {
			return new SortedSelectionGetter().getSelectedItems();
		} else {
			return new SelectionGetter().getSelectedItems();
		}
	}
	
		
	/**
	 * @param srcArray is the source array that starts at index 0
	 * @param dstArray is the array destination
	 * @param sortedModel
	 * @param comparator
	 * @param low is the index in dest to start sorting
	 * @param high is the index in dest to start sorting
	 * @param off is the offset to generate corresponding low, high in src
	 */
	private static void mergeSortAscending(
		int[] srcArray,
		int[] dstArray,
		OrderedModel sortedModel,
		Comparator comparator,
		int low,
		int high, 
		int off) {

		int length = high - low;

		//		Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++) {
				for (int j = i;
					j > low
						&& comparator.compare(
							sortedModel.getItemAt(dstArray[j - 1]),
							sortedModel.getItemAt(dstArray[j]))
							> 0;
					j--) {
					int t = dstArray[j];
					dstArray[j] = dstArray[j - 1];
					dstArray[j - 1] = t;
				}
			}
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSortAscending(dstArray, srcArray, sortedModel, comparator, low, mid, -off);
		mergeSortAscending(dstArray, srcArray, sortedModel, comparator, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (comparator.compare(
				sortedModel.getItemAt(srcArray[mid - 1]), 
				sortedModel.getItemAt(srcArray[mid]))
				<= 0) {
			System.arraycopy(srcArray, low, dstArray, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high
				|| p < mid
				&& comparator.compare(
					sortedModel.getItemAt(srcArray[p]),
					sortedModel.getItemAt(srcArray[q]))
					<= 0)
				dstArray[i] = srcArray[p++];
			else
				dstArray[i] = srcArray[q++];
		}
	}
	
	/**
	 * @param srcArray is the source array that starts at index 0
	 * @param dstArray is the array destination
	 * @param sortedModel
	 * @param comparator
	 * @param low is the index in dest to start sorting
	 * @param high is the index in dest to start sorting
	 * @param off is the offset to generate corresponding low, high in src
	 */
	private static void mergeSortDescending(
		int[] srcArray,
		int[] dstArray,
		OrderedModel sortedModel,
		Comparator comparator,
		int low,
		int high, 
		int off) {

		int length = high - low;

		//		Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++) {
				for (int j = i;
					j > low
						&& comparator.compare(
							sortedModel.getItemAt(dstArray[j - 1]),
							sortedModel.getItemAt(dstArray[j]))
							< 0;
					j--) {
					int t = dstArray[j];
					dstArray[j] = dstArray[j - 1];
					dstArray[j - 1] = t;
				}
			}
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low  += off;
		high += off;
		int mid = (low + high) >> 1;
		mergeSortDescending(dstArray, srcArray, sortedModel, comparator, low, mid, -off);
		mergeSortDescending(dstArray, srcArray, sortedModel, comparator, mid, high, -off);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (comparator.compare(
				sortedModel.getItemAt(srcArray[mid - 1]), 
				sortedModel.getItemAt(srcArray[mid]))
				>= 0) {
			System.arraycopy(srcArray, low, dstArray, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high
				|| p < mid
				&& comparator.compare(
					sortedModel.getItemAt(srcArray[p]),
					sortedModel.getItemAt(srcArray[q]))
					>= 0)
				dstArray[i] = srcArray[p++];
			else
				dstArray[i] = srcArray[q++];
		}
	}
	/**
	 * This method returns the number of the column the
	 * table is currently sorted by (or -1 if none)
	 * 
	 * @return the number of the column that is currently sorted.
	 *   	   -1 if none.
	 */
	int getCurrentColumnNumber() {
		return currentColumnNumber;
	}

	/**
	 * @return
	 */
	boolean isAscending() {
		return ascending;
	}
	
	/**
	 * This method generates a new sorting array without altering the existing one.
	 * The existing one should be replaced by the new one in the Swing event thread.
	 *
	 * @return the new sorting array
	 */
	private int[] getNewSortedArray() {

		int[] srcArray = new int[itemsCount];
		for (int i = 0; i < itemsCount; i++) {
			srcArray[i] = i;
		}

		Comparator comparator = sortedTableFormat.getComparator(currentColumnNumber);

		int[] dstArray = new int[srcArray.length];
		System.arraycopy(srcArray, 0, dstArray, 0, srcArray.length);

		if (ascending) {
			mergeSortAscending(srcArray, dstArray, model,
								comparator, 0, srcArray.length, 0);
		} else {
			mergeSortDescending(srcArray, dstArray, model,
								comparator,	0, srcArray.length, 0);
		}
		return dstArray;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (sorted) {
			try {
				return super.getValueAt(sortingArray[rowIndex], columnIndex);
			} catch (Exception exception) {
				logger.log(Level.FINE, "Race condition in SortedModelTable.getValueAt()", exception);
				return null;
			}
		} else {
			return super.getValueAt(rowIndex, columnIndex);
		}
	}

	/**
	 * @param positions
	 */
	protected void fireTableRowsDeleted(int[] positions) {
		itemsCount -= positions.length;
		if (!sorted) {
			super.fireTableRowsDeleted(positions);
		} else {
			final int[] finalPositions = positions;

			SwingWorker worker = new SwingWorker(table) {

				private int[] newSorterArray;

				protected void doNonUILogic() throws RuntimeException {
					newSorterArray = getNewSortedArray();
				}

				protected void doUIUpdateLogic() throws RuntimeException {
					sortingArray = newSorterArray;
					sorted = true;
					SortedModelTable.super.fireTableRowsDeleted(finalPositions);
					table.revalidate();
					table.repaint();
				}

			};
			worker.start();
		}
	}

	/** 
	 * This method is executed when a new row has been inserted into the model.
	 *  
	 * @see javax.swing.table.AbstractTableModel#fireTableRowsInserted(int, int)
	 */
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		itemsCount += lastRow - firstRow + 1;
		if (!sorted) {
			super.fireTableRowsInserted(firstRow, lastRow);
		} else {
			final int firstRowFinal = firstRow;
			final int lastRowFinal = lastRow;

			SwingWorker worker = new SwingWorker(table) {

				private int[] newSorterArray;

				protected void doNonUILogic() throws RuntimeException {
					newSorterArray = getNewSortedArray();
				}

				protected void doUIUpdateLogic() throws RuntimeException {
					sortingArray = newSorterArray;
					sorted = true;
					SortedModelTable.super.fireTableRowsInserted(firstRowFinal, lastRowFinal);
					table.revalidate();
					table.repaint();
				}

			};
			worker.start();
		}
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#fireTableDataChanged()
	 */
	public void fireTableDataChanged() {
		itemsCount = model.getItemCount();
		if (!sorted) {
			super.fireTableDataChanged();
		} else {
			SwingWorker worker = new SwingWorker(table) {

				private int[] newSorterArray;

				protected void doNonUILogic() throws RuntimeException {
					newSorterArray = getNewSortedArray();
				}

				protected void doUIUpdateLogic() throws RuntimeException {
					sortingArray = newSorterArray;
					sorted = true;
					SortedModelTable.super.fireTableDataChanged();
					table.revalidate();
					table.repaint();
				}

			};
			worker.start();
		}
	}

}
