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

import java.awt.*;
import java.beans.*;

public class MFirstDayEditor implements PropertyEditor
{

    int day; //One less than the enum value in java.util.Calendar
    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    final String[] days={ "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY",
                          "FRIDAY", "SATURDAY" };

    public MFirstDayEditor()
    {
    }

    public String getJavaInitializationString()
    {
        String buff;
        buff = "java.util.Calendar."+ days[day];
        return buff;
    }

    public boolean isPaintable()
    {
        return false;
    }

    public void paintValue(Graphics g, Rectangle r)
    {
    }

    public Component getCustomEditor()
    {
        return null;
    }

    public String getAsText()
    {
        String val;
        val = days[day];
        return val;
    }

    public void setAsText(String s)
    {
        for (int i = 0; i< days.length; i++)
        {
            if (days[i].equals(s))
            {
                day = i;
                listeners.firePropertyChange(null, null, null);
                break;
            }
        }
    }

    public void setValue(Object object)
    {
        Integer x = (Integer)object;
        day = x.intValue()-1;
    }

    public Object getValue()
    {
        return new Integer (day+1);
    }

    public String[] getTags()
    {
        return days;
    }

    public boolean supportsCustomEditor()
    {
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        listeners.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        listeners.removePropertyChangeListener(l);
    }
}
