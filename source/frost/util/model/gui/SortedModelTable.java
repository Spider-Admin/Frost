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
			
		super(newModel, newTableFormat);
		
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
		
		
	
		getTable().getTableHeader().revalidate();
		getTable().getTableHeader().repaint();
	}
	
	/**
	 * This method returns the number of the column that
	 * is currently sorted (or -1 if none)
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

}
