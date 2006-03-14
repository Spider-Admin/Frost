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
 * The <code>FormConstraints</code> class specifies constraints
 * for components that are laid out using the
 * <code>FormLayout</code> class.
 *
 * @see mseries.utils.FormLayout
 */
public class FormConstraints implements Cloneable, java.io.Serializable
{


    /**
    * Specify that this component is to be placed next to the previously added
    * component (<code>gridx</code>, <code>gridy</code>).
    * @see      mseries.utils.FormConstraints#gridx
    * @see      mseries.utils.FormConstraints#gridy
    */
    public static final int RELATIVE = -1;

    /** Put the component in the center of its display area.  */
    public static final int CENTER = 10;

    /** Put the component at the top of its display area, centered horizontally. */
    public static final int NORTH = 11;

    /** Put the component at the top-right corner of its display area. */
    public static final int NORTHEAST = 12;

    /** Put the component on the right side of its display area, centered vertically. */
    public static final int EAST = 13;

    /** Put the component at the bottom-right corner of its display area. */
    public static final int SOUTHEAST = 14;

    /** Put the component at the bottom of its display area, centered horizontally. */
    public static final int SOUTH = 15;

    /** Put the component at the bottom-left corner of its display area. */
    public static final int SOUTHWEST = 16;

    /** Put the component on the left side of its display area, centered vertically. */
    public static final int WEST = 17;

    /** Put the component at the top-left corner of its display area. */
    public static final int NORTHWEST = 18;

    /**
    *   A component that spans columns will be drawn at its preferred size if there is
    *   enough remaining space in the container.
    */
    public boolean spansColumns;
    /**
    *   Speifies the border around the component, this is usually left to the default
    *   value
    */
    public Insets insets;
    /**
    * Specifies the cell at the left of the component's display area,
    * where the leftmost cell has <code>gridx=0</code>. The value
    * <code>RELATIVE</code> specifies that the component be placed just
    * to the right of the component that was added to the container just
    * before this component was added.
    * <p>
    * The default value is <code>RELATIVE</code>.
    * gridx should be a non-negative value.
    * @serial
    * @see #clone()
    * @see mseries.utils.FormConstraints#gridy
    */
    public int gridx;
    /**
    * Specifies the cell at the top of the component's display area,
    * where the topmost cell has <code>gridy=0</code>. The value
    * <code>RELATIVE</code> specifies that the component be placed just
    * below the component that was added to the container just before
    * this component was added.
    * <p>
    * The default value is <code>RELATIVE</code>.
    * gridy should be a non-negative value.
    * @serial
    * @see #clone()
    * @see mseries.utils.FormConstraints#gridx
    */
    public int gridy;

    /**
    * This field is used when the component is smaller than its display
    * area. It determines where, within the display area, to place the
    * component. Possible values are <code>CENTER</code>,
    * <code>NORTH</code>, <code>NORTHEAST</code>, <code>EAST</code>,
    * <code>SOUTHEAST</code>, <code>SOUTH</code>, <code>SOUTHWEST</code>,
    * <code>WEST</code>, and <code>NORTHWEST</code>.
    * The default value is <code>CENTER</code>.
    * @serial
    * @see #clone()
    */
    public int anchor;


    int cellWidth;
    int cellHeight;

    int tempX;
    int tempY;
    /**
     * Set this to true of the components is not to take part in column width calculations, it will fill the column
     * which is wht widht of the widest component in the column
     */
    public boolean fill;

    /**
    * Creates a <code>FormConstraint</code> object with
    * all of its fields set to their default value.
    */
    public FormConstraints ()
    {
        gridx = RELATIVE;
        gridy = RELATIVE;

        anchor = WEST;

        insets = new Insets(4, 4, 4, 4);
        spansColumns=false;
        fill=false;
    }


    /**
    * Creates a copy of this form constraint.
    * @return     a copy of this form constraint
    */
    public Object clone ()
    {
        try
        {
            FormConstraints c = (FormConstraints)super.clone();
            c.insets = (Insets)insets.clone();
            return c;
        }
        catch (CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.3  2003/03/26 23:29:51  martin
// Changed email address
//
// Revision 1.2  2002/12/29 18:26:07  martin
// *** empty log message ***
//
// Revision 1.1  2002/11/03 10:29:05  martin
// Added header comment
//
