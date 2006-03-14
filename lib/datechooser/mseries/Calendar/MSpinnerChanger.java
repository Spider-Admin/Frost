/*
*   Copyright (c) 2001 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class MSpinnerChanger extends JPanel implements MDateChanger
{
    private Calendar maxC = Calendar.getInstance();
    private Calendar minC = Calendar.getInstance();
    private int minMonth, minYear;
    private int maxYear;

    private JSpinner month;
    private JSpinner year;
    private SpinnerNumberModel yearModel;
    private SpinnerListModel monthModel;
    private String[] months, mths;

    protected Vector listeners = new Vector();

    public MSpinnerChanger() {
        this(false);
    }
    
    public MSpinnerChanger(boolean editable)
    {
        super();

        DateFormatSymbols dfs;

        minC.set(1900, 0, 1);
        minMonth = minC.get(Calendar.MONTH);
        minYear = minC.get(Calendar.YEAR);

        maxC.set(2037, 11, 31);
        maxYear = maxC.get(Calendar.YEAR);

        // Create the widgets
        month = new JSpinner();
        month.setBackground(getBackground());
        month.setMinimumSize(new Dimension(100, 22));
        dfs = new DateFormatSymbols();
        mths = dfs.getMonths();
        months = new String[12];
        System.arraycopy(mths, 0, months, 0, 12);

        monthModel = new SpinnerListModel(months);
        month.setModel(monthModel);
        JSpinner.ListEditor listEd = new JSpinner.ListEditor(month);
        listEd.getTextField().setEditable(false);
        listEd.getTextField().setColumns(6);
        listEd.getTextField().setBorder(null);
        listEd.getTextField().setBackground(UIManager.getColor("TextField.background"));
        month.setEditor(listEd);
        month.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                notifyListeners(MChangeEvent.CHANGE);
            }
        });

        yearModel = new SpinnerNumberModel(minYear, minYear, maxYear, 1);
        year = new JSpinner(yearModel);
        year.setBackground(getBackground());
        year.setMinimumSize(new Dimension(64, 22));
        JSpinner.NumberEditor ed = new JSpinner.NumberEditor(year, "0000");
        ed.getTextField().setEditable(false);
        ed.getTextField().setBorder(null);
        ed.getTextField().setBackground(UIManager.getColor("TextField.background"));
        year.setEditor(ed);

        year.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                notifyListeners(MChangeEvent.CHANGE);
            }
        });

        // Draw the screen
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        add(month, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        add(year, c);
    }

    public void setMinimum(Date min)
    {
        minC.setTime(min);
        minYear = minC.get(Calendar.YEAR);
        minMonth = minC.get(Calendar.MONTH);

        yearModel.setMinimum(new Integer(minYear));
        setValue(min);
    }

    public void setMaximum(Date max)
    {
        maxC.setTime(max);
        maxYear = maxC.get(Calendar.YEAR);

        yearModel.setMaximum(new Integer(maxYear));
        setValue(max);
    }

    public void setValue(Date newVal)
    {
        Calendar valC = Calendar.getInstance();
        valC.setTime(newVal);
        int y = valC.get(Calendar.YEAR);
        int m = valC.get(Calendar.MONTH);
        monthModel.setValue(months[m]);
        yearModel.setValue(new Integer(y));

    }

    public int getValue()
    {
        int m = 0;
        String mon = (String)month.getValue();
        for (int i = 0; i < months.length; i++)
        {
            if (months[i].equals(mon))
            {
                m = i;
                break;
            }
        }
        int y = ((Integer)yearModel.getValue()).intValue();

        int newValue = (y - minYear) * 12 + m - minMonth;
        return newValue;
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

    /**
     *   Does anything within the component have the focus
     *   @return true if any child component has the focus
     */
    public boolean hasFocus()
    {
        JSpinner.DefaultEditor monthEditor=(JSpinner.DefaultEditor)month.getEditor();
        JSpinner.DefaultEditor yearEditor=(JSpinner.DefaultEditor)year.getEditor();
        boolean hasFocus = monthEditor.getTextField().isFocusOwner() || yearEditor.getTextField().isFocusOwner();
        return hasFocus;
    }

    /**
     *   Adds the focus listener by delegating to each child component
     *   addFocusListener method.
     */
    public void addFListener(FocusListener l)
    {
        month.addFocusListener(l);
        year.addFocusListener(l);
    }

    /**
     *   Removes the focusListner from the child components
     */
    public void removeFListener(FocusListener l)
    {
        month.removeFocusListener(l);
        year.removeFocusListener(l);
    }

    /**
    *   Sets the background of the two spinner buttons
    *   @param b the background color
    */
    public void setBground(Color b)
    {
        super.setBackground(b);
        month.setBackground(b);
        year.setBackground(b);
    }

    /**
     *   Sets the foreground of the two spinner buttons
     *   @param b the foreground color
     */
    public void setFground(Color b)
    {
        super.setForeground(b);
        month.setForeground(b);
        year.setForeground(b);
    }

    public static void main(String[] argv)
    {
        JFrame f = new JFrame("Test");
        final MSpinnerChanger c = new MSpinnerChanger();

        //c.setBground(Color.red);
        //c.setFground(Color.green);
        c.addMChangeListener(new MChangeListener()
        {
            public void valueChanged(MChangeEvent e)
            {
                System.out.println(c.getValue());
            }
        });

        f.getContentPane().add(c);

        f.pack();
        f.setVisible(true);
    }
}
