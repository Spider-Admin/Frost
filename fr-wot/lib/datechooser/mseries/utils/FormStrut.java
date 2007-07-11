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
package mseries.utils;

import java.awt.*;

/**
 *   Simple non visual component that is used to coerce a layout manager into setting columns
 *   and rows to a certain size. It is used by mseries.utils.FormLayout in particular. The
 *   component has preferred width and height so that layout managers will involve it in
 *   calculations but it has no implementation in its paint method so it will never be seen.
 */
public class FormStrut extends Component
{
    /** The size of the strut */
    public Dimension size;

    /**
     *   Create a strut with specified width and zero height
     *   @param w the preferred width of the strut
     */
    public FormStrut(int w)
    {
        this(new Dimension(w, 0));
    }

    /**
     *   Create a strut with specified width and height
     *   @param w the preferred width of the strut
     *   @param h the preferred height of the strut
     */
    public FormStrut(int w, int h)
    {
        this(new Dimension(w, h));
    }

    /**
     *   Create a strut with specified width and height
     *   @param d the preferred dimension of the strut
     */
    public FormStrut(Dimension d)
    {
        this.size = d;
    }

    public Dimension getPreferredSize()
    {
        return size;
    }

    public Dimension getMinimumSize()
    {
        return new Dimension(0, 0);
    }

    public void paint(Graphics g)
    {
    }
}

// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.2  2003/03/26 23:29:51  martin
// Changed email address
//
// Revision 1.1  2002/11/03 10:29:05  martin
// Added header comment
//
