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
import java.util.ResourceBundle;

/**
*   The calendar panel, popup and pull down is configured using a constraints
*   object that implements this interface. Applications will probably build
*   an immutable implementaion so that all instances appear and behave the same.
*   @see mseries.Calendar.MDefaultPullDownConstraints
*/

public interface MDateSelectorConstraints
{
    /** @return the title for the popup window */
    public String getPopupTitle();

    /** @return the resource bundle for the localisation */
    public ResourceBundle getResourceBundle();

    /** @return the index of the day that should be displayed first, use DAY constants
    *   from java.util.Calendar
    *   @see java.util.Calendar
    */
    public int getFirstDay();

    /** @return the full path name of the image file for the background */
    public String getImageFile();

    /** @return the default foreground */
    public Color getForeground();

    /** @return the default background */
    public Color getBackground();

    /** @return the colour of the current date foreground */
    public Color getTodayForeground();

    /** @return the colour of the current date background, the background
    *   in only drawn when no image file has been set */
    public Color getTodayBackground();

    public Font getFont();

    /** @return an array of colors, each not null value is the colour for the
    *   foreground for the day of the week represented by the index.
    *   0 is Sunday, 6 is Saturday. Null entries will inherit the
    *   foreground given by getForeground()
    */
    public Color[] getForegrounds();

    /** @return an array of colors, each not null value is the colour for the
    *   background for the day of the week represented by the index.
    *   0 is Sunday, 6 is Saturday. Null entries will inherit the
    *   background given by getBackground()
    */
    public Color[] getBackgrounds();

    /** @return the style of changer given by MDateChanger */
    public int getChangerStyle();
    
    /** @return true if the changer can be edited by the keyboard (if appropriate).
     *  For now, only the Spinner can be edited.
     */
    public boolean isChangerEditable();

    public Dimension getCellSize();

    /**
    *   @return true if the lightweight popup is to have a shadow. Heavyweights
    *   never have shadows. Heavyweights over lap their parents boundaries.
    */
    public boolean hasShadow();

    /** @return the number of mouse clicks required to select a date in the
    *   calendar
    */
    public int getSelectionClickCount();

    /** @return true if the selection in the calendar causes events to be fired.
    *   <p>
    *   Usually the date is echoed by catching the events, so the date in the
    *   textfield changes as the user moves through the calendar using the TAB
    *   and arrow keys. Switching the events off causes the textfield to remain
    *   unchanged until (perhaps) the clanedar is dismissed. A value of false
    *   makes the ESCAPE and RETURN/ENTER keys more effective for the pull
    *   down calendar. In this mode, ESC will cause the value in the date field
    *   to remain unchanged regardless of the setting in the calendar.
    *   RETURN/ENTER selects the value.
    */
    public boolean isSelectionEventsEnabled();

    /**
    *   @return a model that can supply the colours representing Special Days
    */
    public SpecialDayModel getSpecialDayModel();

    /**
     * @return the foreground color for out of range cells
     */
    public Color getOutOfRangeForeground();

    /**
     * @return the background color for out of range cells
     */
    public Color getOutOfRangeBackground();

}
/* $Log$
/* Revision 1.1  2006-03-14 14:09:44  bback
/* new date chooser component
/*
/* Revision 1.11  2004/01/31 19:30:13  martin
/* Make Spinner changer allow editable spinner fields, change provided my Maarten Coene
/*
/* Revision 1.10  2003/10/04 10:39:06  martin
/* *** empty log message ***
/*
/* Revision 1.9  2003/10/04 09:41:40  martin
/* *** empty log message ***
/*
/* Revision 1.8  2003/08/22 18:00:53  martin
/* *** empty log message ***
/*
/* Revision 1.7  2003/03/26 23:29:48  martin
/* Changed email address
/*
/* Revision 1.6  2002/02/24 12:33:26  martin
/* A SpecialDayModel can be passed using the constraints
/*
/* Revision 1.5  2002/02/16 18:12:51  martin
/* The eens to update the text field are switchable and can be disabled. This makes the escape key more effective
/*
*/
