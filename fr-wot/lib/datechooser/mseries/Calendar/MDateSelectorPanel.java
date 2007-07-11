
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
import java.awt.event.*;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;

import mseries.ui.*;

/**
 *   A calender component that shows one month at a time with controls to change the month
 *   and year. The parameters to the calendar, including the style of the control are given
 *   in the MDateSelectorConstraints object that is passed. There is a default call
 *   MDefaultPullDownConstraints that can be used or subclassed to provide a set of application
 *   wide defaults.
 *   @see mseries.Calendar.MDateSelectorConstraints
 */
public class MDateSelectorPanel extends MImagePanel implements MMonthListener,
        GridSelectionListener,
        MChangeListener,
        MouseListener
{
    private CalendarPanel calendar;
    private MMonth dataModel;
    private Date today = new Date();
    private boolean hasTodayButton = false;
    private JButton todayButton;

    private boolean changerEditable;
    private MDateChanger scrollbar;

    private ResourceBundle rb;
    private Header header;

    private boolean focusCycleRoot = false;

    private GridBagConstraints c;
    private int clickCount = 2;
    private SpecialDayModel specialDayModel = null;
    FocusChecker fc;

    public MDateSelectorPanel()
    {
        super(new GridBagLayout());
        Date minDate=null, maxDate=null;
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("en", "GB"));
        try
        {
            minDate = df.parse("1/1/1900");
            maxDate = df.parse("31/12/2037");
        }
        catch (ParseException pe)
        {
            System.out.println(pe.getMessage());
        }
        init(false, minDate, maxDate);
    }

    /**
     *  @param lazy set to true if it is known that the some constraints will be set before the panel is displayed,
     *  this avoids the default constraints from being used only to get overridden by a new set.
     */
    public MDateSelectorPanel(boolean lazy, Date min, Date max)
    {
        super(new GridBagLayout());
        init(lazy, min, max);
    }
    private void init(boolean lazy, Date min, Date max)
    {
        this.dataModel = new MMonth(min, max);
        dataModel.addMMonthListener(this);
        /*  The FocusChecker is a class that works out if the calendar, year spinner or
        *   month spinner have the focus, if non have it calls a method on the dataModel
        */
        fc = new FocusChecker();
        fc.setAction(new MDSAction()
        {
            public void doAction()
            {
                dataModel.lostFocus();
            }
        });

        c = new GridBagConstraints();
        header = new Header(dataModel.getColumnCount());

        /* Set table attributes */
        calendar = new CalendarPanel();
        calendar.setModel(dataModel);
        //calendar.setSpecialModel(new DefaultSpecialDayModel());
        registerListeners();
        setColours(calendar);

        if (!lazy)
        {
            setPullDownConstraints(new MDefaultPullDownConstraints());
        }
    }

    /**
     *   Sets the style of changer for the month & year, implementations include COMBOBOX/SPINNER
     *   and a scrollbar
     *   @see mseries.Calendar.MDateChanger
     *   @param style a constant from MDateChanger
     */
    protected void setChangerStyle(int style)
    {
        removeAll();

        /* Place all components on the GUI */
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.CENTER;


        switch (style)
        {
            case MDateChanger.BUTTON:
                scrollbar = new MButtonChanger();

                c.fill = GridBagConstraints.BOTH;
                add((Component) scrollbar, c);

                c.fill = GridBagConstraints.NONE;
                add(header, c);

                c.fill = GridBagConstraints.NONE;
                add(calendar, c);

                scrollbar.addFListener(fc);
                fc.addComponent(scrollbar);

                break;
            case MDateChanger.SPINNER:
                scrollbar = new MSpinnerChanger(this.changerEditable);

                c.fill = GridBagConstraints.BOTH;
                add((Component) scrollbar, c);

                c.fill = GridBagConstraints.NONE;
                add(header, c);

                c.fill = GridBagConstraints.NONE;
                add(calendar, c);

                scrollbar.addFListener(fc);
                fc.addComponent(scrollbar);

                break;
            case MDateChanger.NONE:
                c.fill = GridBagConstraints.NONE;
                add(header, c);
                c.fill = GridBagConstraints.NONE;
                add(calendar, c);
                break;

            case MDateChanger.SCROLLBAR:
            default:
                scrollbar = new MScrollBarChanger();

                c.fill = GridBagConstraints.BOTH;
                add(header, c);
                c.fill = GridBagConstraints.NONE;
                add(calendar, c);
                c.fill = GridBagConstraints.BOTH;
                add((Component) scrollbar, c);

                break;

        }
        /* Set the scrollbars attributes */
        scrollbar.setMinimum(dataModel.getMinimum());
        scrollbar.setMaximum(dataModel.getMaximum());
        scrollbar.setValue(dataModel.getCurrentDate());

        /* Set up listeners for the buttons, scrollbar, keyboard etc. */
        scrollbar.addMChangeListener(this);

        setDate(new Date());
        setColours(this);
        scrollbar.setOpaque(false);

        if (getShowTodayButton())
        {
            todayButton = new JButton(rb.getString("Today"));
            todayButton.addFocusListener(fc);
            fc.addComponent(todayButton);

            todayButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setDMY(today);
                    if (MDateSelectorPanel.this.getCloseOnToday())
                    {
                        close("CLOSE");
                    }
                }
            });

            c.fill = GridBagConstraints.NONE;
            add(todayButton, c);
        }

    }

    public void requestFocus()
    {
        calendar.requestFocus();
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        calendar.setEnabled(enabled);
        scrollbar.setEnabled(enabled);
    }

    /**
     *   Sets the resource bundle for the day labels
     *   @param rb resource bundle
     */
    public void setTextLocalizer(ResourceBundle rb)
    {
        this.rb = rb;
        header.setTextLocalizer(rb);
    }

    /**
     *   @return the resource bundle
     */
    public ResourceBundle getTextLocalizer()
    {
        return this.rb;
    }

    /**
     *    Sets the minimum date value allowed
     *    @param min the earliest possible date
     */
    public void setMinimum(Date min)
    {
        dataModel.setMinimum(min);
    }

    /** Returns the earliest possible date that can be displayed
     *   @return earliest date
     */
    public Date getMinimum()
    {
        return dataModel.getMinimum();
    }

    /**
     *    Sets the maximum date value allowed
     *    @param max the earliest possible date
     */
    public void setMaximum(Date max)
    {
        dataModel.setMaximum(max);
    }

    /** Returns the latest possible date that can be displayed
     *   @return latest date
     */
    public Date getMaximum()
    {
        return dataModel.getMaximum();
    }

    private boolean closeOnToday = true;

    public void setCloseOnToday(boolean close)
    {
        this.closeOnToday = close;
    }

    public boolean getCloseOnToday()
    {
        return closeOnToday;
    }

    public void setShowTodayButton(boolean show)
    {
        this.hasTodayButton = show;
    }

    public boolean getShowTodayButton()
    {
        return hasTodayButton;
    }

    /**
     *
     *   Sets the date in the calendar
     *   @param date the date to set
     */
    public void setDate(Date date)
    {
        try
        {
            dataModel.setDate(date);
        }
        catch (MDateOutOfRangeException e)
        {
            try
            {
                dataModel.setDate(dataModel.getMinimum());
            }
            catch (MDateOutOfRangeException ex)
            {
            }
        }
    }

    /**
     *   Sets a value in the component and does not change the time elements. This method
     *   would be used when a specific date is generated and only the Day, Month, Year are
     *   really needed along with the time that the component already had.
     *   @param date the new date
     */
    public void setDMY(Date date)
    {
        try
        {
            dataModel.setDMY(date);
        }
        catch (MDateOutOfRangeException e)
        {
            try
            {
                dataModel.setDate(dataModel.getMinimum());
            }
            catch (MDateOutOfRangeException ex)
            {
            }
        }
    }

    /**
     *    Returns the currently selected date value
     *    @return the date selected
     */
    public Date getDate()
    {
        return dataModel.getCurrentDate();
    }

    /**
     *   The first column on the calendar grid shows the dates for
     *   the day passed here.
     *   @param firstDay a day constant from java.util.Calendar such as Calendar.SUNDAY
     */
    public void setFirstDay(int firstDay)
    {
        header.setFirstDay(firstDay);
        dataModel.setFirstDay(firstDay);
    }

    /**
     *   @return the index of the first day in the first column
     */
    public int getFirstDay()
    {
        return dataModel.getFirstDay();
    }

    /**
     *   Registers objects that are to listen to events from the DateSelector
     *   Event types of NEW_DATE and NEW_MONTH indicate that the value has changed
     *   @param l an MMonthListener
     */
    public void addMMonthListener(MMonthListener l)
    {
        dataModel.addMMonthListener(l);
    }

    /**
     *   De-Registers objects that are to listen to events from the DateSelector
     *   @param l an MMonthListener
     */
    public void removeMMonthListener(MMonthListener l)
    {
        dataModel.removeMMonthListener(l);
    }

    /**
     *   Sets the display attributes for the pull down calendar
     *   @param c an instance of MDateSelectorConstraints that contains
     *   the desired settings.
     */
    public void setPullDownConstraints(MDateSelectorConstraints c)
    {
        ResourceBundle rb = c.getResourceBundle();
        this.changerEditable = c.isChangerEditable();
        setChangerStyle(c.getChangerStyle());
        if (rb != null)
        {
            try
            {
                setTextLocalizer(rb);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
        int firstDay = c.getFirstDay();
        if (firstDay > 0 && firstDay < 8)
        {
            setFirstDay(firstDay);
        }
        Color[] foregrounds = c.getForegrounds();
        if (foregrounds != null)
        {
            setForeground(foregrounds);
        }
        Color[] backgrounds = c.getBackgrounds();
        if (backgrounds != null)
        {
            setBackground(backgrounds);
        }
        Color foreground = c.getForeground();
        if (foreground != null)
        {
            setForeground(foreground);
        }
        Color background = c.getBackground();
        if (background != null)
        {
            setBackground(background);
        }
        String imageFile = c.getImageFile();
        if (imageFile != null)
        {
            setImageFile(imageFile);
        }

        Dimension cellSize = c.getCellSize();
        if (cellSize != null)
        {
            setCellSize(cellSize);
        }

        foreground = c.getTodayForeground();
        if (foreground != null)
        {
            calendar.setTodayForeground(foreground);
        }

        background = c.getTodayBackground();
        if (background != null)
        {
            calendar.setTodayBackground(background);
        }
        foreground = c.getOutOfRangeForeground();
        if (foreground != null)
        {
            calendar.setOutOfRangeForeground(foreground);
        }


        background = c.getOutOfRangeBackground();
        if (background != null)
        {
            calendar.setOutOfRangeBackground(background);
        }

        clickCount = c.getSelectionClickCount();

        specialDayModel = c.getSpecialDayModel();
        if (specialDayModel != null)
        {
            calendar.setSpecialModel(specialDayModel);
        }
        Font font = c.getFont();
        if (font != null)
        {
            setFont(font);
        }

    }


    /* ----------- Methods required for interfaces ----------- */

    /** Captures changes in the currently selected cell and sets the current
     *   date in the data model
     *   from com.java.swing.event.ListSelectionListener
     */
    public void gridCellChanged(GridSelectionEvent e)
    {
        if (e.isExitEvent())
        {
            dataModel.exitEvent();
        }
        else
        {
            try
            {
                dataModel.setCurrentDate(e.getY(), e.getX());
            }
            catch (MDateOutOfRangeException ex)
            {
            }
        }
    }

    /** Reacts to all changes in the data model (MMonth) which is given
     *   by the event type.
     *   from mseries.utils.MMonthListener interface
     */
    public void dataChanged(MMonthEvent e)
    {
        Point pnt;
        Calendar date = e.getNewDate();

        switch (e.getType())
        {
            case MMonthEvent.NEW_MIN:
                scrollbar.setMinimum(dataModel.getMinimum());
                scrollbar.setValue(dataModel.getCurrentDate());
                break;
            case MMonthEvent.NEW_MAX:
                scrollbar.setMaximum(dataModel.getMaximum());
                scrollbar.setValue(dataModel.getCurrentDate());
                break;
            case MMonthEvent.NEW_DATE:
                pnt = dataModel.getCurrentPoint();
                calendar.setCurrentDate(pnt);
                break;
            case MMonthEvent.NEW_RB:
                break;
            case MMonthEvent.NEW_FIRST_DAY:
                header.repaint();
                //break;
            case MMonthEvent.NEW_MONTH:

                // Tell the table the data has changed
                calendar.tableChanged();
                // Reset the scrollbar
                scrollbar.setValue(date.getTime());

                pnt = dataModel.getCurrentPoint();
                calendar.setCurrentDate(pnt);
                break;
            default:
                break;
        }
    }

    /**
     *   Sets the background image, if set the background color is ignored
     *   @param imageFile the image file, usual Java file types (.jpg, .gif, etc)
     *   are supported.
     */
    public void setImageFile(String imageFile)
    {
        super.setImageFile(imageFile);
        calendar.setHasImage(hasImage());
        calendar.tableChanged();
        calendar.requestFocus();
        repaint();
    }

    /**
     *   for MChangeListener
     */
    public void valueChanged(MChangeEvent event)
    {
        int increment = getDateValue();
        switch (event.getType())
        {
            case MChangeEvent.EXIT:
                dataModel.exitEvent();
                break;
            case MChangeEvent.CHANGE:
                try
                {
                    dataModel.addToMin(increment);
                }
                catch (MDateOutOfRangeException e)
                {
                }
        }
    }

    private void registerListeners()
    {
        addMouseListener(new MouseAdapter(){
        });
        calendar.addMouseListener(this);
        calendar.addGridSelectionListener(this);
        calendar.addFocusListener(fc);
        fc.addComponent(calendar);
    }

    public void mouseClicked(MouseEvent event)
    {
        if (event.getClickCount() == clickCount)
        {
            dataModel.exitEvent();
        }
    }

    public void mouseEntered(MouseEvent event)
    {
    }

    public void mouseExited(MouseEvent event)
    {
    }

    public void mousePressed(MouseEvent event)
    {
    }

    public void mouseReleased(MouseEvent event)
    {
    }

    private int getDateValue()
    {
        return scrollbar.getValue();
    }

    protected void setColours(Component c)
    {
        c.setBackground(getBackground());
        c.setForeground(getForeground());
    }

    /**
     *   Set the foreground colour
     */
    public void setForeground(Color foreground)
    {
        super.setForeground(foreground);
        updateComponentColours(this);
    }

    /**
     *   Set the background colour
     */
    public void setBackground(Color background)
    {
        super.setBackground(background);
        updateComponentColours(this);
    }

    /*
    *   Recurse through the components on the panel setting the
    *   colours
    */
    private void updateComponentColours(Container c)
    {
        Component[] children = c.getComponents();

        for (int i = 0; i < children.length; i++)
        {
            setColours(children[i]);
        }
    }

    public void setFont(Font font)
    {
        super.setFont(font);
        Component[] children = getComponents();

        for (int i = 0; i < children.length; i++)
        {
            setComponentFont(children[i]);
        }
    }

    public void setComponentFont(Component c)
    {
        c.setFont(getFont());
        if (c instanceof java.awt.Container)
        {
            Component[] children = ((Container) c).getComponents();

            for (int i = 0; i < children.length; i++)
            {
                children[i].setFont(getFont());
                setComponentFont(children[i]);
            }
        }
    }

    /**
     *   Sets the foreground color for the column representing the day given.
     *   @param day a number in the range 1 - 7 from SUNDAY - SATURDAY, days not
     *   set will assume the default foreground color
     *   @param color the color to set
     */
    public void setForeground(int day, Color color)
    {
        calendar.setForeground(day, color);
        header.setForeground(day, color);
        repaint();
    }

    /**
     *   Sets the background color for the column representing the day given.
     *   @param day a number in the range 1 - 7 from SUNDAY - SATURDAY, days not
     *   set will assume the default background color
     *   @param color the color to set
     */
    public void setBackground(int day, Color color)
    {
        calendar.setBackground(day, color);
        header.setBackground(day, color);
    }

    /**
     *   Sets the all the background colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the array of colors to set
     */
    public void setBackground(Color[] colors)
    {
        calendar.setBackground(colors);
        header.setBackground(colors);
        repaint();
    }

    /**
     *   Sets the all the foreground colors for each day element 0 - SUNDAY, 6 - SATURDAY
     *   @param colors the array of colors to set
     */
    public void setForeground(Color[] colors)
    {
        calendar.setForeground(colors);
        header.setForeground(colors);
        repaint();
    }

    public void setCellSize(Dimension cellSize)
    {
        calendar.setCellSize(cellSize);
        header.setCellSize(cellSize);
    }

    public void setFocusCycleRoot(boolean fcr)
    {
        focusCycleRoot = fcr;
    }

    public boolean isFocusCycleRoot()
    {
        return focusCycleRoot;
    }

    public void close(String command)
    {
        if (command.equals("CLOSE"))
        {
            dataModel.exitEvent();
        }
        else
        {
            dataModel.lostFocus();
        }
    }
}

/**
 *   Class to report when the components in its group have all lost the
 *   keyboard focus. Any number of components can participate, the action
 *   to execute when they all have lost the focus is encapsulated in the
 *   MDSAction passed in the setAction method
 */
class FocusChecker implements FocusListener
{
    Vector c = new Vector();;
    MDSAction action = new MDSAction()
    {
        public void doAction()
        {
        }
    };

    public void addComponent(Object c)
    {
        this.c.add(c);
    }

    /**
     *   The action contains the behaviour that will be executed when none of the
     *   components in the group have the keyboard focus
     */
    public void setAction(MDSAction action)
    {
        this.action = action;
    }

    public void setComponents(Vector c)
    {
        this.c = c;
    }

    public void focusLost(FocusEvent e)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < c.size(); i++)
                {
                    JComponent comp = (JComponent) c.get(i);
                    if (comp.hasFocus())
                    {
                        return;
                    }
                }
                action.doAction();
            }
        });
    }

    public void focusGained(FocusEvent e)
    {
    }
}


interface MDSAction
{
    public void doAction();
}

/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.25  2004/08/29 17:10:50  martin
*** empty log message ***

Revision 1.24  2004/04/18 10:53:43  martin
*** empty log message ***

Revision 1.23  2004/01/31 19:30:13  martin
Make Spinner changer allow editable spinner fields, change provided my Maarten Coene

Revision 1.22  2003/10/04 10:39:06  martin
*** empty log message ***

Revision 1.21  2003/10/04 09:41:40  martin
*** empty log message ***

Revision 1.20  2003/10/03 20:00:04  martin
*** empty log message ***

Revision 1.19  2003/03/26 23:29:48  martin
Changed email address

Revision 1.18  2003/03/24 19:45:07  martin
Latest 1.4 version

Revision 1.16  2003/03/11 22:37:19  martin
Upgraded to Java 1.4 on 11/03/03

Revision 1.15  2003/01/10 18:07:41  martin
*** empty log message ***

Revision 1.14  2002/08/17 20:40:00  martin
Added new cosntructor with lazy attribute

Revision 1.13  2002/07/21 17:30:57  martin
no message

Revision 1.12  2002/07/21 17:29:35  martin
no message

Revision 1.11  2002/07/21 16:24:40  martin
no message

Revision 1.10  2002/06/18 21:32:29  martin
no message

Revision 1.9  2002/06/13 19:25:06  martin
Added closeOnToday button support

Revision 1.8  2002/06/09 13:48:18  martin
Added 'Today' button

Revision 1.7  2002/02/24 12:33:26  martin
A SpecialDayModel can be passed using the constraints

Revision 1.6  2002/02/16 18:12:51  martin
The eens to update the text field are switchable and can be disabled. This makes the escape key more effective

Revision 1.5  2002/02/16 09:48:47  martin
Added selectionClickCount attribute

Revision 1.4  2002/02/09 12:54:39  martin
Partial support for 'Special Days'

Revision 1.3  2002/02/03 12:49:09  martin
Added support for curret date highlighted in different colour

*/
