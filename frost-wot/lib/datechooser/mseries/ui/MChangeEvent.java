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

/** An Event class used to notify listeners that there has been a change
*   in the MMonth data model
*/
public class MChangeEvent extends java.util.EventObject
{
    private Object value;
    private int type;

    /** The value has changed */
    public final static int CHANGE=0;
    public final static int EXIT=1;
    /** The pull down has been 'pulled down' */
    public final static int PULLDOWN_OPENED=2;
    /** The pull down has been closed */
    public final static int PULLDOWN_CLOSED=3;

    public MChangeEvent(Object source, Object newValue, int type)
    {
        super(source);
        this.value = newValue;
        this.type=type;
    }

    public int getType()
    {
        return this.type;
    }

    public Object getValue()
    {
        return this.value;
    }
}

