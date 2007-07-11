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
package mseries.ui;

import mseries.Calendar.MDateSelectorConstraints;
import mseries.Calendar.MDefaultPullDownConstraints;
import mseries.Calendar.MFieldListener;
import mseries.utils.SafeCalendarUtils;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

/**
 *   Date entry component which looks like a combobox, when the button is pushed a calendar
 *   drops down for selection of the date. Uses a JFormattedTextField as the display component
 */
public class MDateEntryField extends JComponent implements FocusListener
{
    private static final String uiClassID = "DateEntryUI";
    protected MDateField display;
    private boolean todayButton = false;
    private boolean hasBorder = true;
    private boolean nullOnEmpty = false;
    private Logger logger=Logger.getLogger("mseries");

    protected EventListenerList listenerList = new EventListenerList();

    MDateSelectorConstraints panelConstraints = new MDefaultPullDownConstraints();

    /**
     *   Creates a MDateEntryField with default SHORT DateFormatter
     */
    public MDateEntryField()
    {
        this(DateFormat.getDateInstance(DateFormat.SHORT));
    }

    /**
     * Creates a MDateEntryField with the formatter passed
     * @param df the date formatter to use in the display and for parsing the value
     * in the JFormattedTextField
     */
    public MDateEntryField(DateFormat df)
    {
        super();
        display = new MDateField(df)
        {
            public void setUI(TextUI ui)
            {
                super.setUI(ui);
                setBorder(null);
            }
        };
        setValue(new Date());
        display.addFocusListener(this);
        updateUI();
    }
    /**
     *   Creates a MDateEntryField
     *   @param size the size of the display part of the component
     *   @deprecated simply delegates to MDateEntryField()
     */
    public MDateEntryField(int size)
    {
        this();
    }

    /**
     *   Returns the textfield that handles the date editing. Used by the UIDelegates
     */
    public MDateField getDisplay()
    {
        return display;
    }

    public void updateUI()
    {
        setUI(registerUIDelegate());
    }

    /**
     * Delegates to the same method in the JFormattedTextField
     * @return the focus lost behaviour
     */
    public int getFocusLostBehavior()
    {
        return display.getFocusLostBehavior();
    }

    /**
     * Delegates to the same method in the JFormattedTextField
     * @param b focus lost behaviour
     */
    public void setFocusLostBehavior(int b)
    {
        display.setFocusLostBehavior(b);
    }
    /**
     *   Used to automatically install the UIDelagate for Windows & Metal Look & Feels. Any
     *   other Look & Feel will get the basic look and feel unless a UIDelegate is provided
     *   and set in the look and feel class.
     *   @see #getUIClassID
     */
    protected ComponentUI registerUIDelegate()
    {
        ComponentUI compUI = (ComponentUI) UIManager.get(uiClassID);
        if (compUI == null)
        {
            String uiDelegateClassName = "mseries.plaf.basic.BasicDateEntryUI";
            String lafName = UIManager.getLookAndFeel().getID();
            lafName=SafeCalendarUtils.squeeze(lafName);
            /*
            *   There is no UI Delegate for this component so try to install
            *   one of the defaults
            */
            //if (lafName.equals("Windows"))
            //{
            //    uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            //}
            //if (lafName.equals("Metal"))
            //{
            //    uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            //}
            //if (lafName.equals("Motif"))
            //{
            //    uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            //}
            //if (lafName.equals("Mac"))
            //{
            //    uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            //}
            //if (lafName.equals("Aqua"))
            //{
            //    uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            //}
            //UIManager.put(uiClassID, uiDelegateClassName);

            uiDelegateClassName = "mseries.plaf." + lafName + "." + lafName + "DateEntryUI";
            try
            {
                compUI = (ComponentUI) (Class.forName(uiDelegateClassName)).newInstance();
            }
            catch (Exception e)
            {
                logger.info("Class "+uiDelegateClassName+" not found, using default");
                uiDelegateClassName = "mseries.plaf.basic.BasicDateEntryUI";
                try
                {
                    compUI = (ComponentUI) (Class.forName(uiDelegateClassName)).newInstance();
                }
                catch (Exception e1)
                {
                    System.out.println(e1);
                }
            }
        }
        return compUI;
    }

    /**
     *   This method gives the UI Manager a constant to use to look up in the UI Defaults table
     *   to find the class name of the UI Delegate for the installed L&F.
     *   @return string "DateEntryUI"
     */
    public String getUIClassID()
    {
        return uiClassID;
    }


    /**
     *   @return the nullOnEmpty attribute
     */
    public boolean getNullOnEmpty()
    {
        return this.nullOnEmpty;
    }

    /**
     *   If set to true the getValue method will return null when the text field is empty,
     *   otherwise a ParseException is thrown since "" is not a valid date. Default is false.
     *   @param nullOnEmpty set to true if a null is required when the textfield is empty
     */
    public void setNullOnEmpty(boolean nullOnEmpty)
    {
        this.nullOnEmpty = nullOnEmpty;
    }

    /**
     *   @return the current value of the field
     *   The value of the entry field, no parsing is performed, use getValue() to get a Date
     */
    public String getText()
    {
        return display.getText();
    }

    /**
     *   This method does not do anything, the implementation is empty and it only
     *   present to make the component a JavaBean
     */
    public void setText(String text)
    {
    }

    /**
     * Gets the name attribute which is delegated to the textfield used as the display component
     * @return the name
     */
    public String getName()
    {
        return display.getName();
    }

    /**
     * Sets the name attribute which is delegated to the textfield used as the display component
     * @param name the name attribute
     */
    public void setName(String name)
    {
        display.setName(name);
    }

    /**
     *   @return the current value of the field
     *   @exception ParseException if it is not a valid date
     */
    public Date getValue() throws ParseException
    {
        Date ret=null;
        if (nullOnEmpty && display.getText().equals(""))
        {
            ret = null;
        }
        else
        {
            display.commitEdit();
            ret = (Date)display.getValue();
        }
        return ret;
    }

    /**
     *   Sets the current value in the field, parsed using the current date formatter
     *   @param newValue the new value
     */
    public void setValue(Date newValue)
    {
        display.setValue(newValue);
    }

    /**
     *   Tells the component what date formatter to use for parsing and formatting
     *   the user input.
     *   @param df the date formatter
     *   @deprecated use setFormatter(DateFormat)
     */
    public void setDateFormatter(MDateFormat df)
    {
        display.setDateFormatter(df);
    }

    /**
     * @deprecated
     */
    public MDateFormat getDateFormatter()
    {
        return display.getDateFormatter();
    }


    //public void setDateFormatter(DateFormat df)
    //{
    //    display.setDateFormatter(df);
    //}

    public JFormattedTextField.AbstractFormatter getDateFormatter(Object x)
    {
        return display.getDateFormatter(x);
    }

    /** Sets the earliest value that may be selected for this field when
     *   the poup calendar in invoked. The default is 1 January 1900
     *   @param date the ealiest date
     */
    public void setMinimum(Date date)
    {
        display.setMinimum(date);
    }

    /** Sets the latest value that may be selected for this field when
     *   the poup calendar in invoked. The default is 31 December 2037
     *   @param date the latest date
     */
    public void setMaximum(Date date)
    {
        display.setMaximum(date);
    }

    public Date getMinimum()
    {
        return display.getMinimum();
    }

    public Date getMaximum()
    {
        return display.getMaximum();
    }

    /**
     *   @return true if a border will be drawn
     */
    public boolean hasBorder()
    {
        return hasBorder;
    }

    /**
     *   causes a border to be drawn around the component
     *   @param border true is a border is to be drawn (default=true)
     */
    public void drawBorder(boolean border)
    {
        hasBorder = border;
    }

    /**
     *   Returns the font that the editor part (the textfield) uses
     *   @return the font for the date display
     */
    public Font getFont()
    {
        return display.getFont();
    }

    public void setFont(Font font)
    {
        display.setFont(font);
    }

    /**
     *   @return the date fields editable property
     */
    public boolean isEnabled()
    {
        return display.isEnabled();
    }

    /**
     *   Sets the date entry field editable or not, the button can still be used
     *   to allow date selection.
     *   @param editable <I>true</I> if the field can be typed into
     */
    public void setEditable(boolean editable)
    {
        display.setEditable(editable);
    }

    /**
     *   @return the date fields editable property
     */
    public boolean isEditable()
    {
        return display.isEditable();
    }

    public void setInputVerifier(InputVerifier inputVerifier)
    {
        display.setInputVerifier(inputVerifier);
        //this.inputVerifier=inputVerifier;
    }

    public InputVerifier getInputVerifier()
    {
        return display.getInputVerifier();
    }

    /**
     *   Sets the constraints object that contains the parameters used to configure the
     *   pull down calendar. Constraints include the visual aspects of the pull down such
     *   as the colours, including the colours of the month and date changer, if the look and
     *   feel permits it.
     *   @param c the constraints object
     *   @see #getConstraints
     */
    public void setConstraints(MDateSelectorConstraints c)
    {
        this.panelConstraints = c;
    }

    /**
     *   Gets the constraints object that contains the parameters used to configure the
     *   pull down calendar
     *   @see #getConstraints
     */
    public MDateSelectorConstraints getConstraints()
    {
        return this.panelConstraints;
    }

    public void setToolTipText(String text)
    {
        display.setToolTipText(text);
    }

    public String getToolTipText()
    {
        return display.getToolTipText();
    }

    /**
     *   This method is public as an implementation side effect, <b>do not override or call
     *   it directly</b>
     */
    public void opened()
    {
        display.removeFocusListener(this);

    }

    /**
     *   This method is public as an implementation side effect, <b>do not override or call
     *   it directly</b>
     */
    public void closed()
    {
        display.addFocusListener(this);

    }

    /*
    *   These private variables are concerned with managing the focus on the compound
    *   component.
    */
    private int last = FocusEvent.FOCUS_LOST;

    /**
     *   This method is public as an implementation side effect, <b>do not override or call
     *   it directly</b>
     */
    public void focusLost(FocusEvent e)
    {
        notifyListeners(e);
    }

    /**
     *   Set focus on the receiving component if isRequestFocusEnabled returns true
     */
    public void requestFocus()
    {
            display.requestFocus();
    }
    /**
     *   This method is public as an implementation side effect, <b>do not override or call
     *   it directly</b>
     */
    public void focusGained(FocusEvent e)
    {
        notifyListeners(e);
    }

    /** Registers the listeners of the field changes. The event is fired when the component
     *   gets and loses focus.
     *   @param listener - MFieldListener
     *   @see #removeMFieldListener
     */
    public void addMFieldListener(MFieldListener listener)
    {
        listenerList.add(MFieldListener.class, listener);
    }

    /** Removes the listener from the registered list
     *   of listeners
     *   @param listener - MFieldListener
     */
    public void removeMFieldListener(MFieldListener listener)
    {
        listenerList.remove(MFieldListener.class, listener);
    }

    public void notifyListeners(FocusEvent e)
    {
        int type = e.getID();

        if (last != type)
        {

            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
                if (listeners[i] == MFieldListener.class)
                {
                    MFieldListener l = (MFieldListener) listeners[i + 1];
                    if (type == FocusEvent.FOCUS_GAINED)
                    {
                        l.fieldEntered(new FocusEvent(this, type));
                    }
                    else
                    {

                        l.fieldExited(new FocusEvent(this, type));
                    }
                }
            }
            last = type;
        }
    }

    /** Registers the listeners of the field changes. Fired when the user changes a value
     *   in the dropped down calendar
     *   @param listener - MMonthListener
     */
    public void addMChangeListener(MChangeListener listener)
    {
        listenerList.add(MChangeListener.class, listener);
    }

    /** Removes the listener from the registered list
     *   of listeners
     *   @param listener - MMonthListener
     */
    public void removeMChangeListener(MChangeListener listener)
    {
        listenerList.remove(MChangeListener.class, listener);
    }

    /**
     *   Causes the MChangeEvents to be fired. This is called by the L&F Delegate
     *   and should not be overloaded or called directly.
     */
    public void notifyListeners(int type)
    {
        // Pass these events on to the registered listener
        MChangeEvent event;
        Date date;
        try
        {
            date = getValue();
        }
        catch (ParseException e)
        {
            date = null;
        }
        event = new MChangeEvent(this, date, type);


        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == MChangeListener.class)
            {
                ((MChangeListener) listeners[i + 1]).valueChanged(event);
            }
        }
    }

    /**
     *   The pull down can be configured with a button at the bottom to quickly select the
     *   current date. The label is translated using the ResourceBundle given in the contraints
     *   object passed in the setConstraints() method (or the default which is false), by default
     *   the popup closes when the button is pushed, this behaviour can be changed by setting the
     *   closeOnToday attribute.
     *   @param show set this to true if the button is required.
     *   @see mseries.Calendar.MDateSelectorConstraints
     */
    public void setShowTodayButton(boolean show)
    {
        setShowTodayButton(show, true);
    }

    /**
     *   Does this field have a today button when the pull down is shown ?
     *   @return true if a 'today' button is displayed.
     *   @see #setShowTodayButton
     */
    public boolean getShowTodayButton()
    {
        return todayButton;
    }

    private boolean closeOnToday = true;

    /**
     *   The pull down can be configured with a button at the bottom to quickly select the
     *   current date. The label is translated using the ResourceBundle given in the contraints
     *   object passed in the setConstraints() method (or the default which is false).
     *   @param show set this to true if the button is required.
     *   @param close the popup closes when close is true (default)
     *   @see mseries.Calendar.MDateSelectorConstraints
     */
    public void setShowTodayButton(boolean show, boolean close)
    {
        this.todayButton = show;
        this.closeOnToday = close;
    }

    /**
     *   @return the close on today attribute
     */
    public boolean getCloseOnToday()
    {
        return this.closeOnToday;
    }
}

class ShortFormatter implements MDateFormat
{

    DateFormat formatter;

    public ShortFormatter()
    {
        formatter = DateFormat.getDateInstance(DateFormat.SHORT);
    }

    public StringBuffer format(Date d, StringBuffer appendTo, FieldPosition pos)
    {
        return formatter.format(d, appendTo, pos);
    }

    public String format(Date d)
    {
        return formatter.format(d);
    }

    public Date parse(String s) throws ParseException
    {
        return formatter.parse(s);
    }
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.31  2004/09/06 19:27:15  martin
// added support for nullOnEmpty
//
// Revision 1.30  2004/05/21 19:30:59  martin
// *** empty log message ***
//
// Revision 1.29  2004/05/05 22:26:13  martin
// *** empty log message ***
//
// Revision 1.28  2004/03/21 09:55:16  martin
// *** empty log message ***
//
// Revision 1.27  2004/03/07 17:11:06  martin
// *** empty log message ***
//
// Revision 1.26  2004/03/05 23:25:00  martin
// *** empty log message ***
//
// Revision 1.25  2003/10/02 20:28:38  martin
// *** empty log message ***
//
// Revision 1.24  2003/09/25 18:36:11  martin
// no message
//
// Revision 1.23  2003/04/10 19:04:38  martin
// *** empty log message ***
//
// Revision 1.22  2003/03/26 23:29:50  martin
// Changed email address
//
// Revision 1.21  2003/03/24 19:45:07  martin
// Latest 1.4 version
//
// Revision 1.19  2003/03/11 22:35:15  martin
// Upgraded to Java 1.4 on 11/03/03
//
// Revision 1.18  2003/01/18 16:40:09  martin
// *** empty log message ***
//
// Revision 1.17  2003/01/15 21:47:34  martin
// *** empty log message ***
//
// Revision 1.16  2003/01/08 20:47:19  martin
// Overrode setInputVerifier method and delegate to display
//
// Revision 1.15  2002/12/21 23:03:25  martin
// *** empty log message ***
//
// Revision 1.14  2002/12/21 22:53:16  martin
// *** empty log message ***
//
// Revision 1.13  2002/12/15 17:44:16  martin
// *** empty log message ***
//
// Revision 1.12  2002/06/13 19:25:24  martin
// Added closeOnToday button support
//
// Revision 1.11  2002/06/09 13:59:45  martin
// Adjusted Javadoc comment
//
// Revision 1.10  2002/06/09 13:58:51  martin
// Adjusted Javadoc comment
//
// Revision 1.9  2002/06/09 13:53:44  martin
// Adjusted Javadoc comment
//
// Revision 1.8  2002/06/09 13:48:05  martin
// Added 'Today' button
//
// Revision 1.7  2002/04/19 20:41:40  martin
// Make the default date format locale sensitive and SHORT
//
// Revision 1.6  2002/03/03 09:49:38  martin
// Changed listener list to javax.swing.event.ListenerList
//
// Revision 1.5  2002/02/19 20:28:44  martin
// Ensure that the MFieldEvents have the correct source
//
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.31  2004/09/06 19:27:15  martin
// added support for nullOnEmpty
//
// Revision 1.30  2004/05/21 19:30:59  martin
// *** empty log message ***
//
// Revision 1.29  2004/05/05 22:26:13  martin
// *** empty log message ***
//
// Revision 1.28  2004/03/21 09:55:16  martin
// *** empty log message ***
//
// Revision 1.27  2004/03/07 17:11:06  martin
// *** empty log message ***
//
// Revision 1.26  2004/03/05 23:25:00  martin
// *** empty log message ***
//
// Revision 1.25  2003/10/02 20:28:38  martin
// *** empty log message ***
//
// Revision 1.24  2003/09/25 18:36:11  martin
// no message
//
// Revision 1.23  2003/04/10 19:04:38  martin
// *** empty log message ***
//
// Revision 1.22  2003/03/26 23:29:50  martin
// Changed email address
//
// Revision 1.21  2003/03/24 19:45:07  martin
// Latest 1.4 version
//
// Revision 1.19  2003/03/11 22:35:15  martin
// Upgraded to Java 1.4 on 11/03/03
//
// Revision 1.4.2.1  2002/02/24 10:46:47  martin
// Focus Events have correct source. Field does not grab focus
//
