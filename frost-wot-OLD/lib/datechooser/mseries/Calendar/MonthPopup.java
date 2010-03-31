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

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import mseries.utils.*;

public class MonthPopup extends JWindow             // implements MouseListener (and corresponding methods) removed by S. Ruman
{
    JList months;
    MonthModel model;
    AutoChanger autoChanger;
    Calendar c1;
    int width, height;

    public MonthPopup()
    {
        model = new MonthModel();
        months = new JList();
        setValue(new Date());
        months.setModel(model);
        getContentPane().setLayout(new GridLayout(1, 0));
        getContentPane().add(months);
        //addMouseListener(this);

        months.setCellRenderer(getCellRenderer());
        months.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        months.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    public void setLocationOnScreen(int x, int y)
    {
        this.setLocation(x, y);
    }

    public Object getValue()
    {
        return months.getSelectedValue();
    }

    public void setValue(Date value)
    {
        model.setMonth(value);
        months.setSelectedIndex(3);
    }

    public void setSelectedPoint(Point p)
    {
        if (p.y < 0 || p.y > height)
        {
            months.clearSelection();
        }
        else
        {
            // added by S. Ruman to enable the auto-scrolling of months when the first or last month is selected
            int whichSelected = months.locationToIndex(p);
            if (whichSelected == 0)
            {
                if ((autoChanger == null) || (autoChanger.isIncrementing()))
                {  // top selected, scoll backward in months
                    if (autoChanger != null)        // previously scrolling forwards, cancel that scroll
                        autoChanger.stopThread();

                    autoChanger = new AutoChanger(model);
                    autoChanger.setDirection(AutoChanger.DEC);
                    autoChanger.start();
                }
            }
            else if (whichSelected == (months.getModel().getSize() - 1))
            {        // bottom selected, scoll forward in months
                if ((autoChanger == null) || (autoChanger.isDecrementing()))
                {
                    if (autoChanger != null)    // previously scrolling backwards, cancel that scroll
                        autoChanger.stopThread();

                    autoChanger = new AutoChanger(model);
                    autoChanger.setDirection(AutoChanger.INC);
                    autoChanger.start();
                }
            }
            else
            {
                if (autoChanger != null)
                {          // previously scrolling, stop
                    autoChanger.stopThread();
                    autoChanger = null;
                }
            }
            // end S. Ruman addition


            months.setSelectedIndex(months.locationToIndex(p));
        }
    }

    public ListCellRenderer getCellRenderer()
    {
        ListCellRenderer r;
        r = new DefaultListCellRenderer()
        {

            DateFormat df = new SimpleDateFormat("MMMMM yyyy");

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                Date d = (Date) value;
                setText(df.format(d));
                setHorizontalAlignment(CENTER);

                setBackground(isSelected ? UIManager.getColor("ComboBox.selectionBackground") : UIManager.getColor("ComboBox.background"));
                setForeground(isSelected ? UIManager.getColor("ComboBox.selectionForeground") : UIManager.getColor("ComboBox.foreground"));
                return this;
            }
        };

        return r;
    }

    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        width = getWidth();
        height = getHeight();
    }

    class MonthModel extends AbstractListModel
    {
        Date firstDate = new Date();
        Calendar firstCal;

        public void setMonth(Date month)
        {
            firstCal = Calendar.getInstance();
            firstCal.setTime(month);


            // Modification made by S. Ruman (Hyperion) due to the bug with sun.util.BuddhistCalendar (returned by getInstance() in Thai locales).
            // This bug messes up the date when the year changes because of an .add call.
            // firstCal.add(Calendar.MONTH, -3);
            SafeCalendarUtils.doSafeAddition(firstCal, Calendar.MONTH, -3);
        }

        public Object getElementAt(int i)
        {
            c1 = (Calendar) firstCal.clone();
            // see comment above
            // c1.add(Calendar.MONTH, i);
            SafeCalendarUtils.doSafeAddition(c1, Calendar.MONTH, i);

            return c1.getTime();
        }


        public int getSize()
        {
            return 7;
        }

        public void increment(int inc)
        {
            // see comment above
//            firstCal.add(Calendar.MONTH, inc);
            SafeCalendarUtils.doSafeAddition(firstCal, Calendar.MONTH, inc);

            fireContentsChanged(this, 0, 6);
        }
    }


    class AutoChanger extends Thread
    {
        static final int INC = 1;
        static final int DEC = -1;
        int dir = INC;
        MonthModel model;
        boolean keepGoing;

        public AutoChanger(MonthModel model)
        {
            this.model = model;
            keepGoing = true;
        }

        public void setDirection(int dir)
        {
            this.dir = dir;
        }

        // added by S. Ruman to enable the auto-scrolling of months when the first or last month is selected
        public boolean isIncrementing()
        {
            return (dir == INC);
        }

        public boolean isDecrementing()
        {
            return (dir == DEC);
        }
        // end of S. Ruman addition

        public void run()
        {
            // added by S. Ruman to enable the auto-scrolling of months when the first or last month is selected
            try
            {
                Thread.sleep(1000);     // wait for 2 seconds before the scrolling starts
            }
            catch (InterruptedException e)
            {
                keepGoing = false;
            }
            // end of S. Ruman addition

            keepGoing = true;
            while (keepGoing)
            {
                try
                {
                    model.increment(dir);
                    sleep(750);
                }
                catch (InterruptedException e)
                {
                    keepGoing = false;
                }
            }
        }

        public void stopThread()
        {
            keepGoing = false;
        }
    }

    public static void main(String[] argv)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }
        MonthPopup f = new MonthPopup();

        f.pack();
        f.setLocationOnScreen(100, 100);
        f.setVisible(true);
    }
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.9  2003/03/26 23:29:48  martin
// Changed email address
//
// Revision 1.8  2003/03/24 19:45:07  martin
// Latest 1.4 version
//
// Revision 1.6  2003/03/11 22:35:15  martin
// Upgraded to Java 1.4 on 11/03/03
//
// Revision 1.4  2002/07/21 16:24:39  martin
// no message
//
// Revision 1.3  2002/07/18 21:43:45  martin
// no message
//
// Revision 1.2  2002/07/17 21:32:40  martin
// no message
//
