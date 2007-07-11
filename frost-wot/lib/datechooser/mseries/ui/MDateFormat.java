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

import java.text.ParseException;
import java.text.FieldPosition;
import java.util.Date;

/**
*   A simple interface to define format and parse methods, this may be simply implemented by
*   a subclass of java.text.DateFormat or something more sophisticated such as
*   mseries.ui.MDateFormatter
*   @see mseries.ui.MDateFormatter
*/
public interface MDateFormat
{
    public StringBuffer format(Date d, StringBuffer appendTo, FieldPosition pos);

    public String format(Date d);

    public Date parse(String s) throws ParseException;
}

