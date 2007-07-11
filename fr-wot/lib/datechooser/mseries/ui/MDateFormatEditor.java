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

import java.awt.*;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

public class MDateFormatEditor implements PropertyEditor
{

    protected String value; // The thing being edited
    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    public MDateFormatEditor()
    {
    }

    public String getJavaInitializationString()
    {
        return "new mseries.ui.MSimpleDateFormat(\""+value.toString()+"\")";
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
        return value;
    }

    public void setAsText(String s)
    {
        value = s;
        listeners.firePropertyChange("dateFormatter", null, null);
    }

    public void setValue(Object object)
    {
        value = object.toString();
    }

    public Object getValue()
    {
        return new MSimpleDateFormat(value.toString());
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
