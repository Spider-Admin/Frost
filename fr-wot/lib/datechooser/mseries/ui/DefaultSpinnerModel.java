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
import javax.swing.event.*;

public class DefaultSpinnerModel implements SpinnerModel
{
    String val="";
    EventListenerList listenerList = new EventListenerList();

    public Object getValue()
    {
        return val;
    }

    public void setValue(Object v)
    {
    }

    public void setStep(int step)
    {
    }

    public Object getNextValue()
    {
        return val;
    }

    public Object getPreviousValue()
    {
        return val;
    }
    public void addChangeListener(ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }
    public void removeChangeListener(ChangeListener l)
    {
        listenerList.remove(ChangeListener.class, l);
    }
    protected void notifyListeners()
    {
        ChangeEvent event;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==ChangeListener.class)
            {
                event = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(event);
            }
        }
    }
}
