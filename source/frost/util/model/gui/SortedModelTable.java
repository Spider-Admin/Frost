/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.*;

import javax.swing.SwingUtilities;

import frost.util.gui.SwingWorker;
import frost.util.model.*;

/**
 * @author Administrator
 * 
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
			synchronized (sortedModel) {
				switch (mode) {
					case MODE_MULTIPLE :
						int selectionCount = table.getSelectedRowCount();
						selectedItems = new ModelItem[selectionCount];
						int[] selectedRows = table.getSelectedRows();
						for (int i = 0; i < selectedRows.length; i++) {
							selectedItems[i] = sortedModel.getItemAt(selectedRows[i]);
						}
						break;

					case MODE_SINGLE :
						int selectedRow = table.getSelectedRow();
						if (selectedRow != -1) {
							selectedItem = sortedModel.getItemAt(selectedRow);

							break;
						}
				}
			}
		}
	}
	
	private SortedModel sortedModel;

	private static Logger logger = Logger.getLogger(SortedModelTable.class.getName());
	
	private int currentColumnNumber = -1;
	private boolean ascending;
	
	/**
	 * @param newModel
	 * @param newTableFormat
	 */
	public SortedModelTable(
		OrderedModel newModel,
		SortedTableFormat newTableFormat) {
			
		super(newTableFormat);
		
		sortedModel = new SortedModel(newModel, newTableFormat);
		setModel(sortedModel);
		initialize();
		
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
		
		final int columnNumberFinal = columnNumber;

		SwingWorker worker = new SwingWorker(table) {

			protected void doNonUILogic() throws RuntimeException {
				sortedModel.sort(columnNumberFinal, ascending);
			}

			protected void doUIUpdateLogic() throws RuntimeException {
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
		return new SortedSelectionGetter().getSelectedItem();
	}
	

	/**
	 * @return
	 */
	public ModelItem[] getSelectedItems() {
		return new SortedSelectionGetter().getSelectedItems();
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
		return tableFormat.getCellValue(sortedModel.getItemAt(rowIndex), columnIndex);
	}

	/**
	 * This method returns the model item that is represented on a particular
	 * row of the table
	 * @param rowIndex the index of the row the model is represented on
	 * @return the model item (may be null)
	 */
	public ModelItem getItemAt(int rowIndex) {
		return sortedModel.getItemAt(rowIndex);
	}

}
