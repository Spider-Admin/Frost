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

public class UploadTable extends JTable
{
    public UploadTable(TableModel m)
    {
        super(m);
        // set column sizes
        int[] widths = {250, 80, 80, 80, 80, 80};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }
}

