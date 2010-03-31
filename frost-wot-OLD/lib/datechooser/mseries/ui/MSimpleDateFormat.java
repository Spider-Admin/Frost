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
import java.text.*;
import java.util.*;

/**
*   Simple implementation of MDateFormat to allow a date formatter to be quickly built
*   by simply applying a format as a String
*/

public class MSimpleDateFormat extends SimpleDateFormat implements MDateFormat
{
        public MSimpleDateFormat()
        {
            super();
            setLenient(false);
        }

        public MSimpleDateFormat(String pattern)
        {
            super(pattern);
            setLenient(false);
        }

        public MSimpleDateFormat(String pattern, DateFormatSymbols formatData)
        {
            super(pattern, formatData);
            setLenient(false);
        }

        public MSimpleDateFormat(String pattern, Locale loc)
        {
            super(pattern, loc);
            setLenient(false);
        }

        public String toString()
        {
             return toPattern();
        }
}

