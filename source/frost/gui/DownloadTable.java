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

public class DownloadTable extends JTable
{
    public DownloadTable(TableModel m)
    {
        super(m);
        // set column sizes
        int[] widths = {250, 90, 90, 80, 40, 50, 60};
        for (int i = 0; i < widths.length; i++)
        {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }
}

