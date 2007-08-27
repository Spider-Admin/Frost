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

import mseries.ui.MChangeListener;

import java.awt.event.FocusListener;
import java.util.Date;

public interface MDateChanger
{

    public static final int SCROLLBAR=2;
    public static final int SPINNER=3;
    public static final int BUTTON=4;
    public static final int NONE=5;

    public void setOpaque(boolean opaque);

    public void setEnabled(boolean enabled);

    public void setMinimum(Date min);

    public void setMaximum(Date min);

    public void setValue(Date min);

    public boolean hasFocus();

    /**
    *   @return the number of months since the minimum
    */
    public int getValue();

    public void addMChangeListener(MChangeListener l);

    public void removeMChangeListener(MChangeListener l);

    /** Adds a focus listener, using the method addFocusListener causes a
    *   NullPointerException for some reason.
    *   @param l a FocusListener
    */
    public void addFListener(FocusListener l);

    /** Removes a focus listener, using the method removeFocusListener causes a
    *   NullPointerException for some reason
    *   @param l a FocusListener
    */
    public void removeFListener(FocusListener l);
}
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.7  2004/08/29 17:10:58  martin
// *** empty log message ***
//
// Revision 1.6  2003/03/26 23:29:48  martin
// Changed email address
//
// Revision 1.5  2003/01/10 18:07:50  martin
// *** empty log message ***
//

