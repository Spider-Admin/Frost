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

import java.util.ListResourceBundle;

public class DateSelectorRB extends ListResourceBundle
{
    public static final String MON="Mo";
    public static final String TUE="Tu";
    public static final String WED="We";
    public static final String THU="Th";
    public static final String FRI="Fr";
    public static final String SAT="Sa";
    public static final String SUN="Su";

    public static final String OK="OK";
    public static final String CANCEL="Cancel";
    public static final String TODAY="Today";

    String contents[][] =
        {
            {MON, MON},
            {TUE, TUE},
            {WED, WED},
            {THU, THU},
            {FRI, FRI},
            {SAT, SAT},
            {SUN, SUN},
            {OK, OK},
            {CANCEL, CANCEL},
            {TODAY, TODAY},
        };


    public Object[][] getContents()
    {
        return contents;
    }
}
