package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.gui.model.*;

public class SearchTable extends SortedTable
{
    public SearchTable(TableModel m)
    {
        super(m);
    }
}

