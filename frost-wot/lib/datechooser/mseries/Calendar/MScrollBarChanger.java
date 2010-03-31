/*
*   Copyright (c) 2002 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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

import mseries.ui.MChangeEvent;
import mseries.ui.MChangeListener;
import mseries.ui.MDateSpinnerModel;

import javax.swing.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.text.DateFormat;

/** A specific implementation of a scrollbar where the value represents a
*   java.util.Date. The mimimum and maximum values are therefore Dates,
*   the unit increment a month and the block increment a year
*/
class MScrollBarChanger extends JScrollBar implements MDateChanger, AdjustmentListener
{

    private Calendar maxC=Calendar.getInstance();
    private Calendar minC=Calendar.getInstance();
    private int minMonth, minYear, maxMonth, maxYear;
    private int day;
    protected Vector listeners = new Vector();
    protected MDateSpinnerModel model;
    private static final int UP=1;
    private static final int DOWN=-1;


    /**
    *   Class to do the increment/decrement when the buttons are pressed
    */
    protected class UpDownAction extends AbstractAction
    {
        int direction;    // +1 = up; -1 = down
        int step;

        public UpDownAction(int direction, int step)
        {
            super();
            this.step=step;
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent evt)
        {
            Object val;

            model.setStep(step);
            if (direction==UP)
            {
                val = model.getNextValue();
            }
            else
            {
                val = model.getPreviousValue();
            }
            setValue((Date)val);
            notifyListeners(MChangeEvent.CHANGE);
        }
    }

    /**
    *    Inner class to manage the push of the PAGE UP and PAGE DOWN
    *    keys which move the scrollbar and hence the calendar forward
    *    and backwards one month respectively.
    */
    //private class PagingAction implements ActionListener
    //{
    //    int inc;
    //
    //    public PagingAction(int inc)
    //    {
    //        this.inc = inc;
    //    }
    //
    //    public void actionPerformed(ActionEvent e)
    //    {
    //        setValue(getValue()+inc);
    //    }
    //}
    /** Creates a MScrollBarChanger with HORIZONTAL orientation and
    *    blockIncrement = 12
    *    unitIncrement = 1
    *    minimum = 1 January 1900
    *    maximum = 31 December 2037
    *    value = 1 January 1900
    *    extent = 1
    */
    public MScrollBarChanger()
    {
        this(JScrollBar.HORIZONTAL);
    }

    /** Creates a MScrollBarChanger with the orientation specified (in
    *   JScrollBar) and
    *    blockIncrement = 12
    *    unitIncrement = 1
    *    minimum = 1 January 1900
    *    maximum = 31 December 2037
    *    value = 1 January 1900
    *    extent = 1
    */
    public MScrollBarChanger(int orientation)
    {
        super(orientation);
        model=new MDateSpinnerModel();
        model.setValue(new Date());

        super.addAdjustmentListener(this);

        minC.set(1900, 0, 1);
        minMonth=minC.get(Calendar.MONTH);
        minYear=minC.get(Calendar.YEAR);

        maxC.set(2037, 11, 31);
        super.setMinimum(0);
        super.setMaximum(1656);
        super.setVisibleAmount(1);
        super.setBlockIncrement(12); // 12 months = 1 year
        super.setUnitIncrement(1);   // 1 month
        super.setValue(0);

        installKeyboardActions();
    }

    /*
    *    installs the PAGE UP and PAGE DOWN buttons with the increment specifed
    *    in months.
    *    @param advance the number of months to move forward and backwards
    */
    private void installKeyboardActions()
    {
        registerKeyboardAction(new UpDownAction(UP, DateFormat.MONTH_FIELD), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(new UpDownAction(DOWN, DateFormat.MONTH_FIELD), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new UpDownAction(UP, DateFormat.YEAR_FIELD), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(new UpDownAction(DOWN, DateFormat.YEAR_FIELD), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    //private void installKeyboardActions()
    //{
    //    registerKeyboardAction(new PagingAction(1), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    //    registerKeyboardAction(new PagingAction(-1), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    //
    //    registerKeyboardAction(new PagingAction(12), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    //    registerKeyboardAction(new PagingAction(-12), KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    //}

    /* Sets the earliest possible value for the scrollbar to the start of the
    *   month in date passed in. Also sets the curent value to the same.
    *   @param min a date in the earliest month
    */
    public void setMinimum(Date min)
    {
        minC.setTime(min);
        minMonth = minC.get(Calendar.MONTH);
        minYear = minC.get(Calendar.YEAR);

        maxMonth = maxC.get(Calendar.MONTH);
        maxYear = maxC.get(Calendar.YEAR);

        int minMonths=(minYear - 1900)*12 + minMonth;
        int maxMonths=(maxYear - 1900)*12 + maxMonth;

        /* The maximum for the scrollbar is the number of months from
        *  the minimum
        */
        super.setMaximum(maxMonths-minMonths);

        setValue(min);

        model.setMinimum(min);
    }

    /* Sets the latest possible value for the scrollbar to the end of the
    *   month in date passed in.
    *   @param max a date in the latest month
    */
    public void setMaximum(Date max)
    {
        maxC.setTime(max);
        int month = maxC.get(Calendar.MONTH) ;
        int year = maxC.get(Calendar.YEAR);

        int lastMonth = (year - 1900)*12 + month;
        int minMonths = (minYear - 1900)*12 + minMonth;

        int monthRange = lastMonth - minMonths+1;

        super.setMaximum(monthRange);
        setValue(minC.getTime());
        model.setMaximum(max);
    }

    /* Sets the value of the scrollbar to the number of months since the
    *   the minimum date represented by the date passed
    *   @param newVal - the new value
    */
    public void setValue(Date newVal)
    {
        Calendar valC = Calendar.getInstance();
        valC.setTime(newVal);

        int month = valC.get(Calendar.MONTH);
        int year = valC.get(Calendar.YEAR);

        if ((!newVal.before(minC.getTime())) && (!newVal.after(maxC.getTime())))
        {
            int newValue = (year - minYear)* 12 + month - minMonth;
            super.setValue(newValue);
        }
        model.setValue(newVal);
    }

    public void setDay(int day)
    {
        this.day = day;
    }

    /* Overridden to prevent the values being incorrectly set
    */
    public void setBlockIncrement(int v)
    {
        super.setBlockIncrement(12); // 12 months = 1 year
    }

    /* Overridden to prevent the values being incorrectly set
    */
    public void setUnitIncrement(int v)
    {
        super.setUnitIncrement(1); // 1 month
    }

    private void notifyListeners(int type)
    {
        Vector list = (Vector)listeners.clone();
        for (int i = 0; i < list.size(); i++)
        {
            MChangeListener l = (MChangeListener)listeners.elementAt(i);
            l.valueChanged(new MChangeEvent(this, new Integer(getValue()), type));
        }
    }

    public void addMChangeListener(MChangeListener l)
    {
        listeners.addElement(l);
    }

    public void removeMChangeListener(MChangeListener l)
    {
        listeners.removeElement(l);
    }

    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        notifyListeners(MChangeEvent.CHANGE);
    }

    public boolean hasFocus()
    {
        return false;
    }

    public void addFListener(FocusListener l)
    {
    }

    public void removeFListener(FocusListener l)
    {
    }
}
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.7  2003/10/04 09:41:40  martin
// *** empty log message ***
//
// Revision 1.6  2003/10/03 20:00:04  martin
// *** empty log message ***
//
// Revision 1.5  2003/08/22 21:52:45  martin
// no message
//
// Revision 1.4  2003/03/26 23:29:48  martin
// Changed email address
//
// Revision 1.3  2002/07/21 17:30:57  martin
// no message
//
// Revision 1.2  2002/07/21 17:29:27  martin
// Removed getGUI, setFground and setBground methods
//
// Revision 1.1  2002/07/21 16:24:39  martin
// no message
//
