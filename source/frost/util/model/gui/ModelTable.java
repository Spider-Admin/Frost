/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.*;

import frost.util.model.*;

/**
 * This subclass of AbstractTableModel is passed an OrderedModel and a
 * TableFormat in the constructor. It then creates a JTable and displays 
 * the content of the OrderedModel on it according to the rules specified
 * in the TableFormat. 
 * 
 * It also listens for changes in the OrderedModel  and updates the JTable 
 * as necessary.
 * 
 * Besides, the user can choose which columns will be shown via a menu
 * that pops up when he right clicks on the header.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ModelTable extends AbstractTableModel {
	/**
	 * This inner class listens for the changes in the model and updates the 
	 * table in the Swing event thread.
	 */
	private class Listener implements OrderedModelListener {
	
		/**
		 * This constructor creates a new instance of Listener
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem, int, java.lang.Object, java.lang.Object)
		 */
		public void itemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue) {
			int[] columns = tableFormat.getColumnNumbers(fieldID);
			for (int i = 0; i < columns.length; i++) {
				int columnIndex = convertColumnIndexToModel(columns[i]);
				fireTableCellUpdated(position, columnIndex);
			}
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem)
		 */
		public void itemChanged(int position, ModelItem item) {
			fireTableRowsUpdated(position, position);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemAdded(int, frost.util.model.ModelItem)
		 */
		public void itemAdded(int position, ModelItem item) {
			fireTableRowsInserted(position, position);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemsRemoved(int[], frost.util.model.ModelItem[])
		 */
		public void itemsRemoved(int[] positions, ModelItem[] items) {
			fireTableRowsDeleted(positions);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#modelCleared()
		 */
		public void modelCleared() {
			fireTableDataChanged();			
		}

	}
	
	/**
	 * Helper class to be able to safely get the selection fron any thread
	 */
	protected class SelectionGetter implements Runnable {

		private final int MODE_SINGLE = 0;
		private final int MODE_MULTIPLE = 1;

		int mode = 0;

		ModelItem[] selectedItems;
		ModelItem selectedItem;
		
		/**
		 * This method returns an array of all the ModelItems that are 
		 * selected in the JTable.
		 *  @return an array containing the ModelItems that are selected 
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
		 * This method returns the ModelItem that is selected in 
		 * the JTable (or the first one if there are several). If there is
		 * none, it returns null.
		 *  @return the ModelItem that is selected in the JTable, or
		 * 			 the first one if there are several. null if there is
		 * 			 none.
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

		/** 
		 * This method is executed in the Swing event thread. It gets the selected items
		 * and places them in an attribute so that the methods getSelectedItem and getSelectedItems
		 * can return them
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			synchronized (model) {
				switch (mode) {
					case MODE_MULTIPLE :
						int selectionCount = table.getSelectedRowCount();
						selectedItems = new ModelItem[selectionCount];
						int[] selectedRows = table.getSelectedRows();
						for (int i = 0; i < selectedRows.length; i++) {
							selectedItems[i] = model.getItemAt(selectedRows[i]);
						}
						break;
					case MODE_SINGLE :
						int selectedRow = table.getSelectedRow();
						if (selectedRow != -1) {
							selectedItem = model.getItemAt(selectedRow);
						}
						break;
				}
			}
		}

	}
	
	private static Logger logger = Logger.getLogger(ModelTable.class.getName());

	private Listener listener = new Listener();

	protected ModelTableFormat tableFormat;	
	protected OrderedModel model;
	
	protected JTable table;
	private JScrollPane scrollPane;
	
	/**
	 * This ArrayList contains the model indexes of the columns that are being shown
	 */
	private ArrayList visibleColumns = new ArrayList();
	
	/**
	 * This ArrayList contains all of the TableColumns that this ModelTable may show.
	 */
	private ArrayList columns = new ArrayList();

	/**
	 * This method creates an instance of Model table with the given ModelTableFormat
	 * but without an OrderedModel. The method setModel should be called before 
	 * initialization (this constructor does not perform that initialization).
	 * @param newTableFormat the ModelTableFormat that defines the visual representation
	 * 						  of the data in the OrderedModel.
	 */
	protected ModelTable(ModelTableFormat newTableFormat) {
		super();
	
		tableFormat = newTableFormat;
	}

	/**
	 * This method creates an instance of Model table with the given ModelTableFormat
	 * and OrderedModel and initializes it.
	 * @param newModel the OrderedModel that contains the data to be shown on the JTable 
	 * @param newTableFormat the ModelTableFormat that defines the visual representation
	 * 						  of the data in the OrderedModel.
	 */
	public ModelTable(OrderedModel newModel, ModelTableFormat newTableFormat) {
		super();
		
		model = newModel;
		tableFormat = newTableFormat;
				
		initialize();
	}
	
	/**
	 * This method initializes the ModelTable. It creates the default TableColumns,
	 * customizes the JTable and sets up the listener.
	 */
	protected void initialize() {
		int columnCount = tableFormat.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			visibleColumns.add(new Integer(i));
		}

		table = new JTable(this);
		scrollPane = new JScrollPane(table);
		tableFormat.addTable(table);

		tableFormat.customizeTable(this);
		
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columns.add(columnModel.getColumn(i));
		}

		model.addOrderedModelListener(listener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return visibleColumns.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return model.getItemCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		return tableFormat.getCellValue(model.getItemAt(rowIndex), index);
	}
	
	
	/**
	 * This method is called whenever an event is received from the 
	 * OrderedModel indicating that several items have been removed from it.
	 * @param positions the positions of the ModelItems that have
	 * been removed from the OrderedModel.
	 */
	protected void fireTableRowsDeleted(int[] positions) {
		for (int i = 0; i < positions.length; i++) {
			fireTableRowsDeleted(positions[i], positions[i]);	
		}			
	}
	
	/** 
	 * This method returns an array of all the ModelItems that are 
	 * selected in the JTable.
	 * @return an array containing the ModelItems that are selected 
	 */
	public ModelItem[] getSelectedItems() {
		return new SelectionGetter().getSelectedItems();
	}
	
	/**
	 * This method returns the selected ModelItem, or the first one
	 * if there was several of them. It returns null if there was none.
	 * @return the selected ModelItem, or the first one if there was 
	 * 			several of them. null if there was none.
	 */
	public ModelItem getSelectedItem() {
		return new SelectionGetter().getSelectedItem();
	}
	
	/**
	 * This method returns the number of rows that are selected.
	 * @return the number of rows that are selected.
	 */
	public int getSelectedCount() {
		return table.getSelectedRowCount();
	}

	/**
	 * This method returns the JScrollPane the JTable is created into.
	 * @return the JScrollPane the JTable is created into.
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		int index = convertColumnIndexToFormat(column);
		return tableFormat.getColumnName(index);
	}

	/**
	 * This method returns the JTable that is used by this ModelTable 
	 * to show the contents of its OrderedModel.
	 * @return the JTable that is used by this ModelTable to show the
	 * 			contents of its OrderedModel.
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * This method changes the Font of the JTable in this ModelTable
	 * @param font the new font for the JTable in this ModelTable.
	 */
	public void setFont(Font font) {
		table.setFont(font);
		table.setRowHeight(font.getSize() + 5);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		return tableFormat.isColumnEditable(index);
	}
	
	/**
	 * This method is used to find out if the column with the given model index
	 * is currently being shown or not.
	 * @param columnIndex the model index of the column to find out if it is
	 * 					   being shown or not.
	 * @return true if the column is being shown. false otherwise.
	 */
	public boolean isColumnVisible(int columnIndex) {
		int position = convertColumnIndexToModel(columnIndex);
		if (position != -1) {
			return true;
		} else {
			return false;	
		}
	}
	
	/**
	 * This method shows or hides a particular column. In case it tries to
	 * show a column that is already being shown or to hide a column that is
	 * already hidden, the command is simply ignored.
	 * @param index the model index of the column to hide or show.
	 * @param visible if true, the column will be shown. If false, the column
	 * 		   will be hidden.
	 */
	public void setColumnVisible(int index, boolean visible) {
		TableColumnModel columnModel = getTable().getColumnModel();
		int position = convertColumnIndexToModel(index);

		if (visible) {
			if (position == -1) {
				visibleColumns.add(new Integer(index));
				TableColumn column = (TableColumn) columns.get(index);
				column.setModelIndex(visibleColumns.size() - 1);
				columnModel.addColumn(column);
			}
		} else {
			if (position != -1) {
				visibleColumns.remove(new Integer(index));
				columnModel.removeColumn((TableColumn) columns.get(index));
				//Here we have to decrease the model index of all the columns
				//that were to the right of the one we have removed.
				for (int i = 0; i < columnModel.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					int modelIndex = column.getModelIndex();
					if (modelIndex >= position) {
						column.setModelIndex(modelIndex - 1);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		tableFormat.setCellValue(aValue, model.getItemAt(rowIndex), index);
	}

	/**
	 * This method sets a new OrderedModel for the ModelTable (mainly to be
	 * used in conjunction with the constructor that is only passed a ModelTableFormat)
	 * @param model the OrderedModel this ModelTable will get the data from
	 */
	protected void setModel(OrderedModel newModel) {
		model = newModel;
	}

	/**
	 * This method returns an Iterator of all the TableColumns that this
	 * ModelTable may show.
	 * @return an Iterator of all the TableColumns that this
	 * 			ModelTable may show.
	 */
	public Iterator getColumns() {
		return columns.iterator();
	}
	
	/**
	 * This method maps the index that a column has in the associated TableFormat to
	 * the index that column has in this ModelTable.
	 * 
	 * @param formatColumnIndex the index a column has in the associated TableFormat
	 * @return the index that column has in this ModelTable 
	 */
	protected int convertColumnIndexToModel(int formatColumnIndex) {
		return visibleColumns.indexOf(new Integer(formatColumnIndex));
	}
	
	/**
	 * This method maps the index that a column has in this ModelTable to
	 * the index that column has in the associated TableFormat.
	 * 
	 * @param formatColumnIndex the index a column has in this ModelTable
	 * @return the index that column has in the associated TableFormat 
	 */
	protected int convertColumnIndexToFormat(int modelColumnIndex) {
		Integer index = (Integer) visibleColumns.get(modelColumnIndex);
		return index.intValue();
	}

}
