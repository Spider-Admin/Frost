/*
*   Copyright (c) 2002 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
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

import java.awt.Color;
import java.util.Date;

/**
*   Classes implementing this interface define which days are treated as special
*   in the CalendarPanel. Special days are rendered differently and might not
*   be clickable. The method isSpecialDay(Date) governs the proceedings,
*   if this returns true the getForground and getBackground methods will be called
*   for the same date. isClickable will always be called to determine of the user
*   is allowed to select the date.
*<p>
*   @see mseries.Calendar.DefaultSpecialDayModel
*/
public interface SpecialDayModel
{
    public boolean isSpecialDay(Date date);

    /**
    *   The foreground for the special days. Return null if the foreground is
    *   to be drawn in the usual colour
    */
    public Color getForeground(Date date);

    /**
    *   The background for the special days. Return null if the background is
    *   to be drawn in the usual colour
    */
    public Color getBackground(Date date);

}
/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.3  2003/03/26 23:29:48  martin
Changed email address

Revision 1.2  2002/02/09 17:08:20  martin
Removed isClickable method

Revision 1.1  2002/02/09 12:54:39  martin
Partial support for 'Special Days'

*/
