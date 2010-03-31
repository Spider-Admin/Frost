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
package mseries.Calendar;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Header extends JComponent
{
    protected Color[] background;
    protected Color[] foreground;
    boolean opaque = true;
    final String[] fColumnNames = {DateSelectorRB.SUN,
                                   DateSelectorRB.MON,
                                   DateSelectorRB.TUE,
                                   DateSelectorRB.WED,
                                   DateSelectorRB.THU,
                                   DateSelectorRB.FRI,
                                   DateSelectorRB.SAT};
    String[] columnNames = new String[fColumnNames.length];
    private static final String uiClassID = "HeaderUI";

    private ResourceBundle rb;
    int cols;
    int n;

    /** The size of one cell in the calendar grid */
    public Dimension cellSize = new Dimension(22, 20);


    public Header(int cols)
    {
        super();
        this.cols = cols;

        System.arraycopy(fColumnNames, 0, columnNames, 0, fColumnNames.length);

        setFirstDay(Calendar.SUNDAY);
        setCellSize(cellSize);
        updateUI();
        background = new Color[columnNames.length];
        foreground = new Color[columnNames.length];
    }

    public void updateUI()
    {
        registerUIDelegate();
        setOpaque(false);
        setUI(UIManager.getUI(this));
    }

    protected void registerUIDelegate()
    {
        if (UIManager.get(uiClassID) == null)
        {
            String uiDelegateClassName = "mseries.plaf.basic.BasicHeaderUI";
            String lafName = UIManager.getLookAndFeel().getName();
            /*
            *   There is no UI Delegate for this component so try to install
            *   one of the defaults
            */

            if (lafName.equals("Windows"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + ".WindowsHeaderUI";
            }
            else if (lafName.equals("Metal"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + ".MetalHeaderUI";
            }
            else if (lafName.equals("Motif"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + ".MotifHeaderUI";
            }
            UIManager.put(uiClassID, uiDelegateClassName);
        }
    }


    public void setTextLocalizer(ResourceBundle rb)
    {
        this.rb = rb;
    }

    public void setFirstDay(int firstDay)
    {
        shiftColumnNames(firstDay - 1);
    }

    private String getString(String source, String def)
    {
        String newString;
        if (rb == null)
        {
            return def;
        }
        try
        {
            newString = rb.getString(source);
        }
        catch (MissingResourceException e)
        {
            newString = def;
        }
        return newString;
    }

    private void shiftColumnNames(int places)
    {
        n = places;
        for (int i = 0; i < cols; i++)
        {
            columnNames[i] = fColumnNames[n];
            n++;
            if (n == cols) n = 0;
        }
    }

    public String getColumnName(int columnIndex)
    {
        return getString(columnNames[columnIndex], columnNames[columnIndex]);
    }

    public int getCols()
    {
        return cols;
    }

    /**
     *   Sets the foreground color for the column representing the day given.
     *   @param day a number in the range 1 - 7 from SUNDAY - SATURDAY, days not
     *   set will assume the default foreground color
     *   @param color the color to set
     */
    public void setForeground(int day, Color color)
    {
        foreground[day - 1] = color;
    }

    /**
     *   Sets the background color for the column representing the day given.
     *   @param day a number in the range 1 - 7 from SUNDAY - SATURDAY, days not
     *   set will assume the default foreground color
     *   @param color the color to set
     */
    public void setBackground(int day, Color color)
    {
        background[day - 1] = color;
    }

    /**
     *   Sets the all the background colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the color to set
     */
    public void setBackground(Color[] colors)
    {
        background = colors;
    }

    /**
     *   Sets the all the foreground colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the color to set
     */
    public void setForeground(Color[] colors)
    {
        foreground = colors;
    }

    /**
     *   @return the background color for the day passed
     *   @param day in the range 1 (SUNDAY) to 6 (SATURDAY)
     */
    public Color getBackground(int day)
    {
        int i;
        i = day + n;
        i = (i > 6) ? i - 7 : i;

        Color c = background[i];
        if (c == null)
        {
            return getBackground();
        }
        return c;
    }

    /**
     *   @return the foreground color for the day passed
     *   @param day in the range 1 (SUNDAY) to 6 (SATURDAY)
     */
    public Color getForeground(int day)
    {
        int i;
        i = day + n;
        i = (i > 6) ? i - 7 : i;

        Color c = foreground[i];
        if (c == null)
        {
            return getForeground();
        }
        return c;
    }

    //public boolean isFocusTraversable()
    public boolean isFocusable()
    {
        return false;
    }

    public String getUIClassID()
    {
        return uiClassID;
    }

    /**
     *   Sets the size of one cell in the calendar panel
     *   @param cellSize the cell size
     */
    public void setCellSize(Dimension cellSize)
    {
        this.cellSize = cellSize;

        Dimension size = new Dimension(1, 1);
        size.width = cols * cellSize.width;
        size.height = cellSize.height;
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);
    }

    /**
     *   Gets the cell size
     *   @return the the cellSize attribute
     */
    public Dimension getCellSize()
    {
        return cellSize;
    }

}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.7  2003/03/26 23:29:48  martin
// Changed email address
//
// Revision 1.6  2003/03/24 19:45:07  martin
// Latest 1.4 version
//
// Revision 1.4  2003/03/11 22:35:14  martin
// Upgraded to Java 1.4 on 11/03/03
//
// Revision 1.3  2002/08/17 20:24:50  martin
// Reformatted code using intellij
//
/* $Log$
/* Revision 1.1  2006-03-14 14:09:44  bback
/* new date chooser component
/*
/* Revision 1.7  2003/03/26 23:29:48  martin
/* Changed email address
/*
/* Revision 1.6  2003/03/24 19:45:07  martin
/* Latest 1.4 version
/*
/* Revision 1.4  2003/03/11 22:35:14  martin
/* Upgraded to Java 1.4 on 11/03/03
/*
/* Revision 1.2.2.1  2002/02/02 14:54:35  martin
/* Removed depredated method for 1.4
/* */
