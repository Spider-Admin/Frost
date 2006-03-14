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
//import mseries.ui.MDateSpinnerModel;
import mseries.ui.RollOverButton;
import mseries.ui.MDateSpinnerModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *   A changer component for use in the calendar pull down
 */
public class MButtonChanger extends JPanel implements MDateChanger
{
    private JLabel date;
    private RollOverButton down;
    private RollOverButton up;
    private DateFormat df = new SimpleDateFormat("MMMMM yyyy");
    protected MDateSpinnerModel model;

    private static final int DOWN = -1;
    private static final int UP = 1;
    private AbstractAction upAction = new UpDownAction(UP, DateFormat.MONTH_FIELD);
    private AbstractAction downAction = new UpDownAction(DOWN, DateFormat.MONTH_FIELD);
    private Calendar maxC = Calendar.getInstance();
    private Calendar minC = Calendar.getInstance();
    private int minMonth, minYear;
    //private int maxYear;

    protected Vector listeners = new Vector();
    int offset;
    MonthPopup mp = new MonthPopup();

    public MButtonChanger()
    {
        setLayout(new BorderLayout());

        down = new RollOverButton(SwingConstants.WEST);
        down.addActionListener(downAction);

        up = new RollOverButton(SwingConstants.EAST);
        up.addActionListener(upAction);

        add(down, BorderLayout.WEST);
        add(up, BorderLayout.EAST);

        minC.set(1900, 0, 1);
        minMonth = minC.get(Calendar.MONTH);
        minYear = minC.get(Calendar.YEAR);

        maxC.set(2037, 11, 31);
        //maxYear = maxC.get(Calendar.YEAR);

        date = new JLabel("Error");
        Font f = date.getFont();
        String name = f.getFontName();
        int style = (f.isItalic()) ? Font.ITALIC + Font.BOLD : Font.BOLD;
        int size = f.getSize();

        date.setFont(new Font(name, style, size));
        date.setHorizontalAlignment(SwingConstants.CENTER);

        mp.pack();

        date.addMouseListener(new MouseAdapter()
        {
            int x, y, d;

            public void mousePressed(MouseEvent e)
            {
                x = (MButtonChanger.this.getWidth() - mp.getWidth()) / 2;
                x += MButtonChanger.this.getLocationOnScreen().x;

                y = MButtonChanger.this.getLocationOnScreen().y;
                d = mp.getHeight() / 7;
                d *= 3;
                y = (y - d > 0) ? y - d : 0;

                mp.setLocationOnScreen(x, y);
                mp.setVisible(true);
                mp.setValue((Date) model.getValue());
            }

            public void mouseReleased(MouseEvent e)
            {
                Object x = mp.getValue();
                if (x != null)
                {
                   Calendar modelCalendar = Calendar.getInstance();
                   modelCalendar.setTime((Date) model.getValue());

                   Calendar newCalendar = Calendar.getInstance();
                   newCalendar.setTime((Date) x);

                   int dayOfMonth = Math.min(newCalendar.getActualMaximum(Calendar.DAY_OF_MONTH), modelCalendar.get(Calendar.DAY_OF_MONTH));
                   newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                   model.setValue(newCalendar.getTime());
                   setDisplay(newCalendar.getTime());
                   MButtonChanger.this.notifyListeners(MChangeEvent.CHANGE);
                }
                mp.setVisible(false);
                mp.dispose();
            }
        });

        date.addMouseMotionListener(new MouseMotionAdapter()
        {
            Point p;

            public void mouseDragged(MouseEvent e)
            {
                p = e.getPoint();
                mp.setSelectedPoint(SwingUtilities.convertPoint(MButtonChanger.this, p, mp));
            }
        });
        add(date, BorderLayout.CENTER);

        model = new MDateSpinnerModel();
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

    private void setDisplay(Date value)
    {
        date.setText(df.format(value));
    }

    /**
     *   Class to do the increment/decrement when the buttons are pressed
     */
    class UpDownAction extends AbstractAction
    {
        int direction;    // +1 = up; -1 = down
        int step;

        public UpDownAction(int direction, int step)
        {
            super();
            this.step = step;
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent evt)
        {
            Object val;

            model.setStep(step);
            if (direction == UP)
            {
                val = model.getNextValue();
            }
            else
            {
                val = model.getPreviousValue();
            }
            setValue((Date) val);
            notifyListeners(MChangeEvent.CHANGE);
        }
    }

    /**
     *   Sets the minimum date allowed
     *   @param min the minimum date
     */
    public void setMinimum(Date min)
    {
        minC.setTime(min);
        minYear = minC.get(Calendar.YEAR);
        minMonth = minC.get(Calendar.MONTH);

        model.setMinimum(min);
        setValue(min);
    }

    /**
     *   Sets the minimum date allowed
     *   @param max the minimum date
     */
    public void setMaximum(Date max)
    {
        maxC.setTime(max);
        //maxYear = maxC.get(Calendar.YEAR);

        model.setMaximum(max);
        setValue(max);
    }

    /**
     *   Sets the value
     *   @param newVal the new value
     */
    public void setValue(Date newVal)
    {
        Calendar valC = Calendar.getInstance();
        valC.setTime(newVal);

        model.setValue(newVal);
        mp.setValue(newVal);
        setDisplay(newVal);
    }

    /**
     *   Returns the value
     *   @return the number of months since the minimum date
     */
    public int getValue()
    {
        Calendar valC = Calendar.getInstance();
        valC.setTime((Date) model.getValue());
        int y = valC.get(Calendar.YEAR);
        int m = valC.get(Calendar.MONTH);

        int newValue = (y - minYear) * 12 + m - minMonth;
        return newValue;
    }

    public void addMChangeListener(MChangeListener l)
    {
        listeners.addElement(l);
    }

    public void removeMChangeListener(MChangeListener l)
    {
        listeners.removeElement(l);
    }

    /**
     *   Does anything within the component have the focus
     *   @return true if any child component has the focus
     */
    public boolean hasFocus()
    {
        return up.hasFocus() || down.hasFocus();
    }

    /**
     *   Adds the focus listener by delegating to each child component
     *   addFocusListener method.
     */
    public void addFListener(FocusListener l)
    {
        up.addFocusListener(l);
        down.addFocusListener(l);
    }

    /**
     *   Removes the focusListner from the child components
     */
    public void removeFListener(FocusListener l)
    {
        up.removeFocusListener(l);
        down.removeFocusListener(l);
    }

    private void notifyListeners(int type)
    {
        Vector list = (Vector) listeners.clone();
        for (int i = 0; i < list.size(); i++)
        {
            MChangeListener l = (MChangeListener) listeners.elementAt(i);
            l.valueChanged(new MChangeEvent(this, new Integer(getValue()), type));
        }
    }

/*
    public static void main(String[] argv)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
        }

        JFrame f = new JFrame("Test");
        final MButtonChanger c = new MButtonChanger();

        f.getContentPane().add(c);

        f.pack();
        f.show();
    }
*/
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.14  2005/02/01 20:37:47  martin
// Deleted some unnecessary variables
//
// Revision 1.13  2005/02/01 20:15:22  martin
// *** empty log message ***
//
// Revision 1.11  2003/08/22 21:52:45  martin
// no message
//
// Revision 1.10  2003/03/26 23:29:48  martin
// Changed email address
//
// Revision 1.9  2002/08/17 20:01:52  martin
// Reformatted the code
//
// Revision 1.8  2002/07/22 20:06:24  martin
// Added some comments
//
// Revision 1.7  2002/07/21 17:30:57  martin
// no message
//
// Revision 1.6  2002/07/21 17:29:27  martin
// Removed getGUI, setFground and setBground methods
//
// Revision 1.5  2002/07/21 16:24:40  martin
// no message
//
// Revision 1.4  2002/07/18 21:43:49  martin
// no message
//
// Revision 1.3  2002/07/17 21:32:35  martin
// no message
//
// Revision 1.2  2002/06/18 21:32:29  martin
// no message
//
// Revision 1.1  2002/06/16 21:48:12  martin
// new file
//
