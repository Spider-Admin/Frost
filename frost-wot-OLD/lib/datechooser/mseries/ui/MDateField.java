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

import mseries.Calendar.MDateSelector;
import mseries.Calendar.MDateSelectorConstraints;
import mseries.Calendar.MDefaultPullDownConstraints;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Date entry widget with built in Formatter/Parser and Calendar popup to
 * facilitate input. The earliest and latest possible values may be set using
 * the setMinimum and setMaximum methods, this causes the calendar popup to
 * allow date within the range to be selected. Dates outside of the range may
 * be entered by typing directly into the field.
 *
 * @author	Martin Newstead
 */
public class MDateField extends JFormattedTextField
{
    private Date minDate = null;
    private Date maxDate = null;
//    private Date date = null;

    /**
     *   The MDateFormatter trys to parse the user input in SHORT, MEDIUM and LONG
     *   formats for the default locale. If the text field is not enterable or less
     *   flexibility is needed then this would be done differently
     */
    /** @deprecated */
    public mseries.ui.MDateFormat df = MDateFormatter.getInstance();
    ResourceBundle rb;
    MouseListener mouseListener;
    MDateSelectorConstraints c = new MDefaultPullDownConstraints();

    private boolean hasPopup = false;
    private int h;
    private int m;
    private int s;
    private int ms;


    public MDateField(DateFormat df)
    {
        super(new DateFormatter(df));
        init();
    }
    /**
     * @param	text the display field size
     * @deprecated
     */
    public MDateField(int text)
    {
        super();
        init();
    }

    /**
     * Default Constructor
     *
     *
     */
    public MDateField()
    {
        super(new Date());
        init();
    }

    protected void init()
    {
        super.setValue(new Date());
        try
        {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("en", "GB"));
            maxDate = df.parse("31/12/2037");
            minDate = df.parse("1/1/1970");
        }
        catch (ParseException e)
        {
        }
    }

    /**
     *   Tells the component what date formatter to use for parsing and formatting
     *   the user input.
     *   @param df the date formatter
     *   @deprecated format should be set when the object is instantiated by passing an instance of DateFormat, NB this
     *   method does nothing
     */
    public void setDateFormatter(MDateFormat df)
    {
        //this.df = df;
        //setValue(date);
    }



    /**
     * Returns the date formatter
     * @param x not used, only inplace to allow the same method name as the depreicated method
     * @return the date formatter
     */
    public AbstractFormatter getDateFormatter(Object x)
    {
        return getFormatter();
    }
    /*
    *   @deprecated Use DateFormat getDateFormatter() to get a formatter
    */
    public MDateFormat getDateFormatter()
    {
        return df;
    }

    public boolean hasPopup()
    {
        return this.hasPopup;
    }

    /**
     *   Sets the constraints object that contains the parameters used to configure the
     *   pull down calendar
     *   @param c the constraints object
     *   @see #getConstraints
     */
    public void setConstraints(MDateSelectorConstraints c)
    {
        this.c = c;
    }

    /**
     *   Gets the constraints object that contains the parameters used to configure the
     *   pull down calendar
     *   @see #setConstraints
     */
    public MDateSelectorConstraints getConstraints()
    {
        return this.c;
    }

    /**
     * @param	hasPopup
     */
    public void setPopup(boolean hasPopup)
    {
        pSetPopup(hasPopup);
    }

    private void pSetPopup(boolean hasPopup)
    {
        /* Already has a popup and we want one */
        if (hasPopup && this.hasPopup)
        {
            return;
        }
        /* Don't want a popup and haven't got one */
        if (!hasPopup && !this.hasPopup)
        {
            return;
        }
        /* Don't want a popup but it has one */
        if (!hasPopup && this.hasPopup)
        {
            removeMouseListener(mouseListener);
            this.hasPopup = false;
        }

        /* User can right click on the textfield to get a date popup */
        mouseListener = new MouseAdapter()
        {
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    doPopup(e.getPoint());
                }
            }

            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    doPopup(e.getPoint());
                }
            }

            private void doPopup(Point p)
            {
                MDateSelector popup;
                popup = new MDateSelector(minDate, maxDate);
                popup.setConstraints(c);

                //if (minDate != null)
                //    popup.setMinimum(minDate);
                //if (maxDate != null)
                //    popup.setMaximum(maxDate);

                p.x += getBounds(null).x;
                p.y += getBounds(null).y;
                popup.show(getParent(), p, getValue(null));

                setValue(popup.getValue());
                popup = null;
                requestFocus();

            }
        };
        addMouseListener(mouseListener);
        this.hasPopup = hasPopup;
    }

    /** Sets the earliest value that may be selected for this field when
     *   the poup calendar in invoked. The default is 1 January 1900
     *   @param date the ealiest date
     */
    public void setMinimum(Date date)
    {
        minDate = date;
    }

    /** Sets the latest value that may be selected for this field when
     *   the poup calendar in invoked. The default is 31 December 2037
     *   @param date the latest date
     */
    public void setMaximum(Date date)
    {
        maxDate = date;
    }

    /**
     * Returns the earliest date
     *
     */
    public Date getMinimum()
    {
        return minDate;
    }

    /**
     * @return	The latest date
     *
     */
    public Date getMaximum()
    {
        return maxDate;
    }

    /**
     * @return	the value of the field
     * date formatter
     */
    public Date getValue(Date defaultValue)
    {
        String v=getText();
        AbstractFormatter f=getFormatter();
        Date date=null;
        try
        {
            date = (Date)f.stringToValue(v);
            //Date date = (Date)super.getValue();//df.parse(getText());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            cal.set(Calendar.SECOND, s);
            cal.set(Calendar.MILLISECOND, ms);
            //setValue(cal.getTime());
            return cal.getTime();
        }
        catch (Exception e)
        {
            return defaultValue;
        }

    }

    /**
     *   @return the value of the field
     *   @param defaultValue the value to be returned if a parseException occurs, useful
     *   if there is no value in the field as "" is not parsable
     */
    //public Date getValue(Date defaultValue)
    //{
    //    //try
    //    //{
    //    //    return (Date)super.getValue();
    //    //}
    //    //catch (ParseException e)
    //    //{
    //    //    return defaultValue;
    //    //}
    //    Date d=(Date)super.getValue();
    //    return (d==null) ? defaultValue : d;
    //}

    /**
     * @param	date
     */
    public void setValue(Date date)
    {
        Calendar cal = Calendar.getInstance();
        if (date != null)
        {
            cal.setTime(date);
            h = cal.get(Calendar.HOUR_OF_DAY);
            m = cal.get(Calendar.MINUTE);
            s = cal.get(Calendar.SECOND);
            ms = cal.get(Calendar.MILLISECOND);
        }
//        this.date = date;
        //setText(date == null ? "" : df.format(date));
        super.setValue(date);
    }

    /**
     * @param	args
     */
//    public static void main(String args[])
//    {
//        /*  The date entry field, this variable and the frame need to be final because main()
//        *   is static and they are used in the inner class that is the action listener for
//        *   the button
//        */
//        final MDateField endDate = new MDateField(20);
//        final JFrame myFrame = new JFrame("My Frame");
//endDate.setEnabled(false);
//
//        /* This date formatter is used simply to parse the strings for max and min values */
//        DateFormat dF = DateFormat.getDateInstance(DateFormat.SHORT);
//	    try
//        {
//            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//            /*  Correct format for locale en_GB ! Setting the maximum and minimum is
//            *   optional, defaults are provided.
//            */
//            endDate.setPopup(true);
//            endDate.setMinimum(dF.parse("1/1/1970"));
//            endDate.setMaximum(dF.parse("30/06/2030"));
//	    }
//        catch (Exception e)
//        {
//           e.printStackTrace();
//	    }
//
//
//        JButton dateButton = new JButton("Select");
//
//        dateButton.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                MDateSelector popup;
//                popup = new MDateSelector();
//
//                /* endDate is the name of my custom text field for date input */
//                popup.setMinimum(endDate.getMinimum());
//                popup.setMaximum(endDate.getMaximum());
//                //popup.setImageFile("d:\\temp\\MBackground.gif");
//                //popup.setFirstDay(Calendar.SATURDAY);
//
//                /*  Get the position of the text field and use this for the position of the popup,
//                *   the MDateSelector uses these coordinates relative to the component passed as
//                *   the parent (endDate)
//                */
//                Point p=endDate.getBounds().getLocation();
//
//                try
//                {
//                    /*  1st arg is the component to position the popup relative to
//                    *   2nd is the position relative to the component in 1
//                    *   3rd is the initial value, null for the default (today)
//                    */
//                    popup.show(endDate, p, endDate.getValue(null));
//
//                    /*  Set the value in the text field once the popup has been dismissed */
//                    endDate.setValue(popup.getValue());
//                }
//                catch(Exception ex)
//                {
//                    ex.printStackTrace();
//                }
//                popup=null;
//            }
//
//        });
//
//        myFrame.getContentPane().setLayout(new FlowLayout());
//        myFrame.getContentPane().add(endDate);
//        myFrame.getContentPane().add(dateButton);
//JTextField tf=new JTextField("hello");
//tf.setEnabled(false);
//        myFrame.getContentPane().add(tf);
//
//        myFrame.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                System.exit(0);
//            }
//        });
//        myFrame.pack();
//        myFrame.show();
//    }
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.12  2004/08/29 17:11:09  martin
// *** empty log message ***
//
// Revision 1.11  2004/03/07 18:57:36  martin
// *** empty log message ***
//
// Revision 1.10  2004/03/05 23:25:00  martin
// *** empty log message ***
//
// Revision 1.9  2003/10/04 09:47:39  martin
// *** empty log message ***
//
// Revision 1.8  2003/06/20 21:55:26  martin
// *** empty log message ***
//
// Revision 1.7  2003/03/26 23:29:50  martin
// Changed email address
//
// Revision 1.6  2002/05/15 17:36:45  martin
// Added call to setText to echo the parsed date to the display
//
// Revision 1.5  2002/03/06 22:08:22  martin
// Fixed format problem with default value
//
// Revision 1.4  2002/02/17 17:24:16  martin
// Commented out main(String[])
//

