/*
*   Copyright (c) 2000 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.ui;

import mseries.Calendar.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/**
*   A simple example of MDateEntryField used as a table cell editor and table
*   cell renderer. It implements both interfaces but the same instance can
*   not be used as both the renderer and the editor. It is safest to use two
*   instances for each cell. As a renderer all the component gives is the
*   date formatting and the button which when clicked invokes the editor which
*   looks the same. A simple date formatting textfield would work equally well
*   as the renderer as Swing itself replaces the renderer with the editor when
*   the cell is clicked and vice-versa when editing is finished.
*/
public class MDateCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
    public MDateEntryField comp=new MDateEntryField();
    Date date;

    /**
     * Constructs the editor using the date formatter passed.
     * @param formatter a DateFormat object to use for displaying the date
     */
    public MDateCellEditor(DateFormat formatter)
    {
        comp=new MDateEntryField(formatter);
        comp.setBorder(null);
        configureEditor();
    }


    public MDateCellEditor(String format)
    {
        this(new SimpleDateFormat(format));
    }
    /**
    *   This method makes the component an editor
    */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        comp.setValue((Date)value);
        return comp;
    }

    /**
    *   This method makes the component a renderer
    */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        comp.setValue((Date)value);
        return comp;
    }

    public Object getCellEditorValue()
    {
        try
        {
            date=comp.getValue();
        }
        catch (ParseException e)
        {
            date=new Date();
        }
        return date;
    }

    /**
    *   This method can be used to configure the MDateEntryField that is used
    *   as the cell editor component. In the constructor this method is called
    *   <i>after</i> the format is set and after border is nullified. Thus it
    *   is best to only configure a constrainst object here and use it to
    *   configure the editing field.
    *   @see mseries.ui.MDateEntryField
    *   @see mseries.Calendar.MDateSelectorConstraints
    */
    protected void configureEditor()
    {
    }

    /**
    *   Passes the constraints object to the field that does the editing.
    *   @param c a constraints object that can configure the pull down
    */
    public void setConstraints(MDateSelectorConstraints c)
    {
        comp.setConstraints(c);
    }
}
