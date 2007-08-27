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

import java.awt.event.FocusEvent;
import java.util.EventListener;

/** Interface to which any object must conform if it is to be informed of
*   changes to an MSeries field such as MDateEntryField.
*<P>
*   Focus listeners can't be used as some components artificially lose and gain the focus
*   but the component is not strictly exited. This interface should be used to detect the user
*   entering and exiting the field
*/
public interface MFieldListener extends EventListener
{
    /** The field has recieved focus */
    public void fieldEntered(FocusEvent event);
    /** The field has lost focus */
    public void fieldExited(FocusEvent event);
}
