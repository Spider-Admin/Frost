/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.awt.Component;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.*;

import frost.util.model.OrderedModel;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SortedModelTable extends ModelTable {
	
	private OrderedModel sortedModel;

	private SortedTableFormat sortedTableFormat;

	private static Logger logger = Logger.getLogger(SortedModelTable.class.getName());
	
	private static final int INSERTIONSORT_THRESHOLD = 7;	
	
	private int currentColumnNumber = -1;
	private boolean ascending;
	
	private int[] sortingArray = new int[100];
	private int sortingArraySize = 0;
	
	/**
	 * @param newModel
	 * @param newTableFormat
	 */
	public SortedModelTable(
		OrderedModel newModel,
		SortedTableFormat newTableFormat) {
			
		super(newModel, newTableFormat);
		
		sortedModel = newModel;
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
		
		resortTable();
	
		getTable().getTableHeader().revalidate();
		getTable().getTableHeader().repaint();
	}
	
	/**
	 * 
	 */
	private void resortTable() {
		Comparator comparator = sortedTableFormat.getComparator(currentColumnNumber);
		
		int[] newSortingArray = new int[sortingArray.length];
		System.arraycopy(sortingArray, 0, newSortingArray, 0, sortingArraySize);
		
		if (ascending) {
			mergeSortAscending(sortingArray, newSortingArray, sortedModel, comparator, 0, sortingArraySize, 0); 
		} else {
			mergeSortDescending(sortingArray, newSortingArray, sortedModel, comparator, 0, sortingArraySize, 0); 
		}
		
		sortingArray = newSortingArray;	
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

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return super.getValueAt(sortingArray[rowIndex], columnIndex);
	}

	/** 
	 * This method is executed when a new row has been deleted from the model.
	 * For performance reasons, rows should only be deleted at the end if possible. 
	 * 
	 * If we ever need to delete many rows from the middle, we should implement 
	 * launching those delete events in blocks, instead of individually. 
	 *  
	 * @see javax.swing.table.AbstractTableModel#fireTableRowsDeleted(int, int)
	 */
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		//We check if they have been deleted from the end (hopefully) or not.
		if (firstRow < sortingArraySize) {
			deleteFromSortingArray(firstRow, lastRow - firstRow + 1);
		} else {
			sortingArraySize -= lastRow - firstRow + 1;
		}
		super.fireTableRowsDeleted(firstRow, lastRow);
	}

	/** 
	 * This method is executed when a new row has been inserted into the model.
	 * For performance reasons, rows should only be added at the end. If we ever
	 * need to insert many rows in the middle, we should implement launching those
	 * insert events in blocks, instead of individually. 
	 *  
	 * @see javax.swing.table.AbstractTableModel#fireTableRowsInserted(int, int)
	 */
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		//First we check if the new rows fit into the sortingArray
		if (lastRow > sortingArray.length) {
			growSortingArray(lastRow);
		}
		//Now we check if they have been added at the end (hopefully) or not.
		if (firstRow < sortingArraySize) {
			insertInSortingArray(firstRow, lastRow - firstRow + 1);
		} else {
			for (int i = firstRow; i <= lastRow; i++) {
				sortingArray[i] = i;
				sortingArraySize++;
			}
		}
		super.fireTableRowsInserted(firstRow, lastRow);
	}
	
	/**
	 * This method inserts items into the sorting array. The values
	 * of the positions ar the right will be increased and moved accordingly
	 * @param position position where the new values are to be inserted
	 * @param number number of values to insert.
	 */
	private void insertInSortingArray(int position, int number) {
		//First we displace the ones at the right
		int numberToDisplace = sortingArraySize - position;
		int newPositionStart = position + number;
		int newPositionEnd =  position + number + numberToDisplace;
		for (int i = newPositionStart; i <= newPositionEnd; i++) {
			sortingArray[i + numberToDisplace] = sortingArray[i] + number;
		} 
		//Then we insert the new ones
		int positionEnd = position + number;
		for (int i = position; i < positionEnd; i++) {
			sortingArray[i] = i;
		}	
		sortingArraySize += number;	
	}
	
	/**
	 * This method deletes items from the sorting array. The values
	 * of the positions at the right will be decreased and moved accordingly
	 * @param position position where the new values are to be inserted
	 * @param number number of values to insert.
	 */
	private void deleteFromSortingArray(int position, int number) {
		int numberToDisplace = sortingArraySize - (position + number);
		int newPositionStart = position + number;
		for (int i = newPositionStart; i >= position; i--) {
			sortingArray[i] = sortingArray[i + numberToDisplace] - number;
		}
		sortingArraySize -= number;	 		
	}
	/**
	 * This method grows the sortingArray when a row has been added
	 * to the model and its number is greater than the length of the
	 * array. 
	 * It is passed the number of that row, and the length of the new
	 * sorting array will be that number + 25%.
	 * @param exceededValue the number of the row that is greater than
	 *        the length of the sortingArray
	 */
	private void growSortingArray(int exceededValue) {
		int newLength = (int) (exceededValue * 1.25);
		int[] newSortingArray = new int[newLength]; 
		System.arraycopy(sortingArray, 0, newSortingArray, 0, sortingArray.length);
		sortingArray = newSortingArray;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#fireTableDataChanged()
	 */
	public void fireTableDataChanged() {
		sortingArraySize = getRowCount();
		super.fireTableDataChanged();
	}

}
