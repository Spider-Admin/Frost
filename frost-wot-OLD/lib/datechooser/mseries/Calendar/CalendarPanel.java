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
import javax.swing.plaf.ComponentUI;
import mseries.ui.ScreenUtilities;
import java.awt.*;
import java.util.Calendar;
import java.util.Vector;

/**
 *   This is the component that forms the Calendar Grid. The pluggable look and feel demands
 *   a separate UI Delegate, an implementation is provided in the laf package. The actual class
 *   is determined dynamically by the actual look and feel used. This class therefore is the
 *   controller in the MVC model for the calenar grid. It implements TableModelListener because
 *   the Model is a TableModel. (the first generation of MDateSelector had the calendar grid as
 *   a JTable)
 *   @author M Newstead
 */
public class CalendarPanel extends JComponent
{
    protected Color[] background;
    protected Color[] foreground;

    protected Color todayBG;
    protected Color todayFG;
    protected Color outOfRangeFG;
    protected Color outOfRangeBG;

    private boolean hasImage = false;

    private MMonth model;
    private SpecialDayModel specialModel;

    /** The month that the calendar is displaying */
    public int month;

    private Point selectedCell = new Point(-1, -1);

    /** The cell containing the first day of the month */
    public Point firstCell = new Point(-1, 0);

    /** The cell containing the last day of the month */
    public Point lastCell = new Point(-1, -1);

    private Vector listeners = new Vector();

    /** The number of cells across the calendar */
    public static int DAYS = 7;
    /** The number of rows in the calendar */
    public static int WEEKS = 6;

    /** The size of one cell in the calendar grid */
    public Dimension cellSize = new Dimension(22, 20);

    private static final String uiClassID = "CalendarPanelUI";

    public CalendarPanel()
    {
        super();
        updateUI();
        background = new Color[DAYS];
        foreground = new Color[DAYS];

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            ScreenUtilities.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            ScreenUtilities.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    }

    public void updateUI()
    {
        setUI(registerUIDelegate());
    }

    protected ComponentUI registerUIDelegate()
    {
        ComponentUI compUI = (ComponentUI) UIManager.get(uiClassID);
        if (compUI == null)
        {
            String uiDelegateClassName = "mseries.plaf.basic.BasicCalendarPanelUI";
            String lafName = UIManager.getLookAndFeel().getID();
            /*
            *   There is no UI Delegate for this component so try to install
            *   one of the defaults
            */

            if (lafName.equals("Windows"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "CalendarPanelUI";
            }
            else if (lafName.equals("Metal"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "CalendarPanelUI";
            }
            else if (lafName.equals("Motif"))
            {
                uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "CalendarPanelUI";
            }
            try
            {
                compUI = (ComponentUI) (Class.forName(uiDelegateClassName)).newInstance();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        return compUI;
    }

    /**
     *    The component manages the focus by TABBING across cells
     */
//    public boolean isManagingFocus()
//    {
//        return true;
//    }


    /**
     *    Returns true if the point passed in is in the month that the
     *    Calendar is currently is displaying. [The user could click on a cell
     *    that is at the end of the preceeding month or start of the next month]
     *    @param row the row
     *    @param col the column
     *    @return true if the point is in the month
     */
    public boolean isInMonth(int row, int col)
    {
        Calendar date = (Calendar) model.getValueAt(row, col);
        int clickMonth = date.get(Calendar.MONTH);

        return clickMonth == month;
    }

    public boolean isInRange(int row, int col)
    {
        Calendar date = (Calendar) model.getValueAt(row, col);
        return model.isInRange(date);
    }
    /**
     *    Gets the text (number) of the cell passed in
     *    @param row the row
     *    @param col the column
     *    @return the number of the cell
     */
    public int getLegendFor(int row, int col)
    {
        Calendar date = (Calendar) model.getValueAt(row, col);
        int day = date.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    /**
     *    Gets the day of the week for the cell passed in
     *    @param row the row
     *    @param col the column
     *    @return the day of the week as a java.util.Calendar constant
     */
    public int getDOW(int row, int col)
    {
        Calendar date = (Calendar) model.getValueAt(row, col);
        int day = date.get(Calendar.DAY_OF_WEEK);
        return day;
    }


    protected void setSpecialModel(SpecialDayModel sdm)
    {
        this.specialModel = sdm;
    }

    public void setModel(MMonth model)
    {

        this.model = model;

        month = calculateMonth();

        DAYS = model.getColumnCount();
        WEEKS = model.getRowCount();
    }

    private int calculateMonth()
    {
        Calendar firstDate, scratchDate;

        scratchDate = (Calendar) model.getValueAt(0, 0);
        firstDate = (Calendar) scratchDate.clone();
        if (firstDate.get(Calendar.DAY_OF_MONTH) != 1)
        {
            // The first day of the month is not at the start of the week
            firstDate.add(Calendar.MONTH, 1);
        }
        return firstDate.get(Calendar.MONTH);
    }

    /**
     *    Recieves changes from the data model
     */
    public void tableChanged()
    {
        month = calculateMonth();
        repaint();
    }

    /**
     *    Makes the point passed the current point in the calendar
     *    @param pnt the point to make the selected date
     */
    public void setCurrentDate(Point pnt)
    {
        setSelectedCell(pnt);
    }

    /**
     *    Makes the point passed the current point in the calendar
     *    @param cell the point to make the selected date
     */
    public void setSelectedCell(Point cell)
    {
        setSelectedCell(cell.x, cell.y);
    }

    /**
     *    Makes the point passed the current point in the calendar
     *    @param x the column
     *    @param y the row
     */
    public void setSelectedCell(int x, int y)
    {
        if (isEnabled())
        {
            selectedCell.x = x;
            selectedCell.y = y;
        }
    }

    /**
     *    Sets the passed cell as the one displaying the last day of the month
     *    (used by L&F)
     *    @param cell the cell for last day of the month
     */
    public void setLastCell(Point cell)
    {
        lastCell.x = cell.x;
        lastCell.y = cell.y;
    }

    /**
     *    Sets the passed cell as the one displaying the first day of the month
     *    (used by L&F)
     *    @param cell the cell for first day of the month
     */
    public void setFirstCell(Point cell)
    {
        firstCell.x = cell.x;
        firstCell.y = cell.y;
    }

    public Point getSelectedCell()
    {
        return selectedCell;
    }


    public void addGridSelectionListener(GridSelectionListener l)
    {
        listeners.addElement(l);
    }

    public void removeGridSelectionListener(GridSelectionListener l)
    {
        listeners.removeElement(l);
    }

    public void notifyListeners()
    {
        if (isEnabled())
        {
            notifyListeners(new GridSelectionEvent(this, getSelectedCell()));
        }
    }

    public void notifyListeners(GridSelectionEvent event)
    {
        // Pass these events on to the registered listener

        Vector list = (Vector) listeners.clone();
        for (int i = 0; i < list.size(); i++)
        {
            GridSelectionListener l = (GridSelectionListener) listeners.elementAt(i);
            l.gridCellChanged(event);
        }
    }

    public boolean isFocusable()
    {
        return true;
    }

    /**
     *   This method gives the UI Manager a constant to use to look up in the UI Defaults table
     *   to find the class name of the UI Delegate for the installed L&F.
     *   @return string "CalendarPanelUI"
     */
    public String getUIClassID()
    {
        return uiClassID;
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
     *   set will assume the default background color
     *   @param color the color to set
     */
    public void setBackground(int day, Color color)
    {
        background[day - 1] = color;
    }

    /**
     *   Sets the all the background colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the colors to set
     */
    public void setBackground(Color[] colors)
    {
        background = colors;
    }

    /**
     *   Sets the all the foreground colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the colors to set
     */
    public void setForeground(Color[] colors)
    {
        foreground = colors;
    }


    /**
     *   Sets the foreground colour of out of range dates
     */
    public void setOutOfRangeForeground(Color colour)
    {
        outOfRangeFG = colour;
    }

    public Color getOutOfRangeBackground()
    {
        return outOfRangeBG;
    }

    public void setOutOfRangeBackground(Color outOfRangeBG)
    {
        this.outOfRangeBG = outOfRangeBG;
    }

    /**
     *   Sets the foreground colour of the current date
     */
    public void setTodayForeground(Color colour)
    {
        todayFG = colour;
    }

    /**
     *   Sets the background colour of the current date
     */
    public void setTodayBackground(Color colour)
    {
        todayBG = colour;
    }

    /**
     *   @return the background color for the day passed
     *   @param day in the range 1 (SUNDAY) to 6 (SATURDAY)
     */
    public Color getBackground(int day)
    {
        Color c = background[day - 1];
        if (c == null)
        {
            return getBackground();
        }
        return c;
    }

    /**
     *   @return the background of cell at column d, row w. If the specified
     *   cell represents the current date (today) the current date background
     *   colour will be returned if specified other wise the colour for the
     *   day of week is returned.
     */
    public Color getBackground(int w, int d)
    {
        if (model.isCurrentDate(w, d))
        {
            if (todayBG != null)
                return todayBG;
        }
        if(!model.isInRange(model.getAsDate(w,d))&& outOfRangeBG!=null)
        {
            return outOfRangeBG;
        }
        if (specialModel.isSpecialDay(model.getAsDate(w, d)))
        {
            Color bg = specialModel.getBackground(model.getAsDate(w, d));
            if (bg != null)
                return bg;
        }
        return getBackground(getDOW(w, d));
    }

    /**
     *   @return the foreground color for the day passed
     *   @param day in the range 1 (SUNDAY) to 6 (SATURDAY)
     */
    public Color getForeground(int day)
    {
        Color c = foreground[day - 1];
        if (c == null)
        {
            return getForeground();
        }
        return c;
    }

    /**
     *   @return the foreground of cell at column d, row w. If the specified
     *   cell represents the current date (today) the current date foreground
     *   colour will be returned if specified other wise the colour for the
     *   day of week is returned.
     */
    public Color getForeground(int w, int d)
    {
        if (model.isCurrentDate(w, d))
        {

            if (todayFG != null)
                return todayFG;
        }
        if(!model.isInRange(model.getAsDate(w,d))&& outOfRangeFG!=null)
        {
            return outOfRangeFG;
        }
        if (specialModel.isSpecialDay(model.getAsDate(w, d)))
        {
            Color fg = specialModel.getForeground(model.getAsDate(w, d));
            if (fg != null)
                return fg;
        }
        return getForeground(getDOW(w, d));
    }

    /**
     *   Sets the size of one cell in the calendar panel
     *   @param cellSize the cell size
     */
    public void setCellSize(Dimension cellSize)
    {
        this.cellSize = cellSize;
        Dimension size = new Dimension(1, 1);
        size.width = DAYS * cellSize.width;
        size.height = WEEKS * cellSize.height;
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

    public void setHasImage(boolean hasImage)
    {
        this.hasImage = hasImage;
    }

    public boolean hasImage()
    {
        return hasImage;
    }
}

/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.13  2003/10/04 10:39:06  martin
*** empty log message ***

Revision 1.12  2003/10/04 09:41:40  martin
*** empty log message ***

Revision 1.11  2003/03/26 23:29:48  martin
Changed email address

Revision 1.10  2003/03/24 19:45:07  martin
Latest 1.4 version

Revision 1.8  2003/03/11 22:35:14  martin
Upgraded to Java 1.4 on 11/03/03

Revision 1.7  2002/12/21 22:53:05  martin
*** empty log message ***

Revision 1.6  2002/08/17 20:13:37  martin
Reformatted code using intellij

Revision 1.5  2002/02/09 12:54:39  martin
Partial support for 'Special Days'

Revision 1.4  2002/02/03 12:49:09  martin
Added support for curret date highlighted in different colour

$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.13  2003/10/04 10:39:06  martin
*** empty log message ***

Revision 1.12  2003/10/04 09:41:40  martin
*** empty log message ***

Revision 1.11  2003/03/26 23:29:48  martin
Changed email address

Revision 1.10  2003/03/24 19:45:07  martin
Latest 1.4 version

Revision 1.8  2003/03/11 22:35:14  martin
Upgraded to Java 1.4 on 11/03/03

Revision 1.2.2.3  2002/02/02 15:40:33  martin
Added CVS tag Log

*/
