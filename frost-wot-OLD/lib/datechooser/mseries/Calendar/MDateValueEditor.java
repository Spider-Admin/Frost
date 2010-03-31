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
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.text.*;

public class MDateValueEditor implements PropertyEditor
{

    protected Date value; // The thing being edited
    protected DateFormat df;
    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    public MDateValueEditor()
    {
        df = DateFormat.getDateInstance(DateFormat.LONG);
    }

    public String getJavaInitializationString()
    {
        String buff;
        buff = "new java.util.Date("+value.getTime()+")";
        return buff;
    }

    public boolean isPaintable()
    {
        return false;
    }

    public void paintValue(Graphics g, Rectangle r)
    {
        g.setClip(r);
        g.drawString(getAsText(), r.x+5, r.y+15);
    }

    public Component getCustomEditor()
    {
        final TextField t = new TextField(getAsText(), 20);

        t.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent e)
            {
                setAsText(t.getText());
            }
        });
        return t;
    }

    public String getAsText()
    {
        String val=null;
        val = df.format(value);
        return val;
    }

    public void setAsText(String s)
    {
        Date old=value;
        try
        {
            value = df.parse(s);
            listeners.firePropertyChange(null, null, null);
        }
        catch(ParseException e)
        {
            value = old;
        }
    }

    public void setValue(Object object)
    {
        if (object != null)
        {
            value = (Date)object;
        }
    }

    public Object getValue()
    {
        return value;
    }

    public String[] getTags()
    {
        return null;
    }

    public boolean supportsCustomEditor()
    {
        return true;
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
