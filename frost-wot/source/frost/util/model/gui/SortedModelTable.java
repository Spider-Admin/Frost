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
	private static Logger logger = Logger.getLogger(SortedModelTable.class.getName());
	
	/**
	 * Index in the ModelTable of the column the model is 
	 * sorted by (or -1 if it is not currently sorted). 
	 */
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
		
		SortedModel sortedModel = new SortedModel(newModel, newTableFormat);
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
				int index = convertColumnIndexToFormat(columnNumberFinal);
				((SortedModel) model).sort(index, ascending);
			}

			protected void doUIUpdateLogic() throws RuntimeException {
				table.revalidate();
				table.repaint();
			}

		};
		worker.start();
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
	 * This method returns the model item that is represented on a particular
	 * row of the table
	 * @param rowIndex the index of the row the model is represented on
	 * @return the model item (may be null)
	 */
	public ModelItem getItemAt(int rowIndex) {
		return model.getItemAt(rowIndex);
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTable#setColumnVisible(int, boolean)
	 */
	public void setColumnVisible(int index, boolean visible) {
		super.setColumnVisible(index, visible);
		if (!visible) {
			if (index == currentColumnNumber) {
				currentColumnNumber = -1;
			} else if (index < currentColumnNumber) {
				currentColumnNumber--;
			}
		}
	}

}
