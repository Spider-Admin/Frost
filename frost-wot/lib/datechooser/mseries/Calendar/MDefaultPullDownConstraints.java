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
/**
*   A default implementation of MDateSelectorConstraints that can be instantiated then have
*   the attributes set. Or it can be subclassed to provide specialised system wide defualts
*/
package mseries.Calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.util.ResourceBundle;
import java.util.Calendar;

/**
*   The defualt implementation of MDateSelectorConstraints
*/
public class MDefaultPullDownConstraints implements MDateSelectorConstraints
{
    public String bundleName="mseries.Calendar.DateSelectorRB";
    public ResourceBundle rb;
    public int firstDay=Calendar.SATURDAY;
    public String imageFile=null;
    public Color foreground=null;
    public Color outOfRangeForeground=Color.gray;
    public Color outOfRangeBackground=null;
    public Color todayForeground=null;
    public Color todayBackground=null;
    public Color background=null;
    public Font font=null;
    public Color[] foregrounds=null;
    public Color[] backgrounds=null;
    public int changerStyle=MDateChanger.SCROLLBAR;
    public Dimension cellSize;
    public boolean hasShadow=false;
    public int selectionClickCount=2;
    public boolean selectionEventsEnabled=true;
    public SpecialDayModel specialDayModel=null;
    public String popupTitle;
    public boolean changerEditable=false;


    public MDefaultPullDownConstraints()
    {
        foregrounds=new Color[7];
        foregrounds[0]=Color.blue;
        foregrounds[6]=Color.blue;
        cellSize=new Dimension(22, 20);
        specialDayModel = new DefaultSpecialDayModel();
        popupTitle="MSeries Date Selector";
    }

    public String getPopupTitle()
    {
        return popupTitle;
    }

    /** @return the resource bundle for the localisation */
    public ResourceBundle getResourceBundle()
    {
        if (rb==null)
        {
            rb=ResourceBundle.getBundle(bundleName);
        }
        return rb;
    }

    /** @return the index of the day that should be displayed first, use DAY constants
    *   from java.util.Calendar
    *   @see java.util.Calendar
    */
    public int getFirstDay()
    {
        return firstDay;
    }

    /** @return the full path name of the image file for the background */
    public String getImageFile()
    {
        return imageFile;
    }

    /** @return the default foreground */
    public Color getForeground()
    {
        return foreground;
    }

    /** @return the current date foreground */
    public Color getTodayForeground()
    {
        return todayForeground;
    }

    /** @return the current date background */
    public Color getTodayBackground()
    {
        return todayBackground;
    }

    public Color getBackground()
    {
        return background;
    }

    public Font getFont()
    {
        return font;
    }

    public Color[] getForegrounds()
    {
        return foregrounds;
    }

    public Color[] getBackgrounds()
    {
        return backgrounds;
    }

    public int getChangerStyle()
    {
        return changerStyle;
    }

    public Dimension getCellSize()
    {
        return cellSize;
    }

    public boolean hasShadow()
    {
        return hasShadow;
    }
    public int getSelectionClickCount()
    {
        return selectionClickCount;
    }

    public boolean isSelectionEventsEnabled()
    {
        return selectionEventsEnabled;
    }

    public SpecialDayModel getSpecialDayModel()
    {
        return specialDayModel;
    }

    public Color getOutOfRangeForeground()
    {
        return outOfRangeForeground;
    }

    public Color getOutOfRangeBackground()
    {
        return outOfRangeBackground;
    }
    
    public boolean isChangerEditable()
    {
        return changerEditable;
        
    }
}
/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.12  2004/01/31 19:30:13  martin
Make Spinner changer allow editable spinner fields, change provided my Maarten Coene

Revision 1.11  2003/10/04 10:39:06  martin
*** empty log message ***

Revision 1.10  2003/10/04 09:41:40  martin
*** empty log message ***

Revision 1.9  2003/08/22 18:00:53  martin
*** empty log message ***

Revision 1.8  2003/03/26 23:29:48  martin
Changed email address

Revision 1.7  2002/02/24 12:33:26  martin
A SpecialDayModel can be passed using the constraints

Revision 1.6  2002/02/16 18:12:51  martin
The eens to update the text field are switchable and can be disabled. This makes the escape key more effective

Revision 1.5  2002/02/16 09:49:38  martin
fixed typo

Revision 1.4  2002/02/16 09:48:47  martin
Added selectionClickCount attribute

Revision 1.3  2002/02/03 12:49:09  martin
Added support for curret date highlighted in different colour

*/
