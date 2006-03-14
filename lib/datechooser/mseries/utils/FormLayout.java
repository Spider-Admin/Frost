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
import java.util.*;


/**
 * The <code>FormLayout</code> class is a flexible layout
 * manager that aligns components vertically and horizontally,
 * without requiring that the components be of the same size.
 * Each <code>FormLayout</code> object maintains a dynamic
 * rectangular grid of cells, with each component occupying
 * one cell, called its <em>display area</em>.
 * <p>
 * Each component managed by a form layout is associated
 * with an instance of
 * {@link FormConstraints}
 * that specifies how the component is laid out within its display area.
 * <p>
 * How a <code>FormLayout</code> object places a set of components
 * depends on the <code>FormConstraints</code> object associated
 * with each component, and on the minimum size
 * and the preferred size of the components' containers.
 * <p>
 * To use a form layout effectively, you must customize one or more
 * of the <code>FormConstraints</code> objects that are associated
 * with its components. You customize a <code>FormConstraints</code>
 * object by setting one or more of its instance variables:
 * <p>
 * <dl>
 * <dt>{@link FormConstraints#gridx},
 * {@link FormConstraints#gridy}
 * <dd>Specifies the cell at the upper left of the component's display area,
 * where the upper-left-most cell has address
 * <code>gridx&nbsp;=&nbsp;0</code>,
 * <code>gridy&nbsp;=&nbsp;0</code>.
 * Use <code>FormConstraints.RELATIVE</code> (the default value)
 * to specify that the component be just placed
 * just to the right of (for <code>gridx</code>)
 * or just below (for <code>gridy</code>)
 * the component that was added to the container
 * just before this component was added.
 * <dt>{@link FormConstraints#insets}
 * <dd>Specifies the component's external padding, the minimum
 * amount of space between the component and the edges of its display area. This is defaulted to
 * four pixels on each side.
 * <dt>{@link FormConstraints#anchor}
 * <dd>Used when the component is smaller than its display area
 * to determine where (within the display area) to place the component.
 * Valid values are
 * <code>FormConstraints.CENTER</code> (the default),
 * <code>FormConstraints.NORTH</code>,
 * <code>FormConstraints.NORTHEAST</code>,
 * <code>FormConstraints.EAST</code>,
 * <code>FormConstraints.SOUTHEAST</code>,
 * <code>FormConstraints.SOUTH</code>,
 * <code>FormConstraints.SOUTHWEST</code>,
 * <code>FormConstraints.WEST</code>, and
 * <code>FormConstraints.NORTHWEST</code>.
 * <dt>{@link FormConstraints#spansColumns}
 * <dd>Specifies the component can span across column boundaries and the preferred width
 * width will be used if the container (not the column) is wide enough
 * </dl>
 * <p>
 * The following figure shows 16 components
 * managed by a form layout:
 * <p>
 * <img src="doc-files/FormPanel.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 *<hr>
 * <p>
 * Here is the code that implements the example shown above, the creation of the components
 * and setting of the border have been omitted.
 * <p>
 *<pre>
 *
 *        JPanel pnlWeight = new JPanel();
 *
 *        pnlWeight.setLayout(new FormLayout());
 *
 *
 *        FormConstraints constraints = new  FormConstraints();
 *        constraints.gridx       = 0;
 *        constraints.gridy       = 0;
 *
 *        // Gross Weight
 *        constraints.anchor      = FormConstraints.EAST;
 *        pnlWeight.add(lblGrossWeightMand,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(tfGrossWeight,constraints);
 *
 *        // UOM2
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx++;
 *        pnlWeight.add(lblUOM2,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(cmbUOM2,constraints);
 *
 *        // Net Weight
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx       = 0;
 *        constraints.gridy++;
 *        pnlWeight.add(lblNetWeightMand,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(tfNetWeight,constraints);
 *
 *        // UOM 3
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx++;
 *        pnlWeight.add(lblUOM3,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(cmbUOM3,constraints);
 *
 *
 *        // Tare Weight
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx       = 0;
 *        constraints.gridy++;
 *        pnlWeight.add(lblTareWeight,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(tfTareWeight,constraints);
 *
 *        // UOM 4
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx++;
 *        pnlWeight.add(lblUOM4,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(cmbUOM4,constraints);
 *
 *        // Average Weight
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx       = 0;
 *        constraints.gridy++;
 *        pnlWeight.add(lblAverageWeight,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(tfAverageWeight,constraints);
 *
 *
 *        // UOM 5
 *        constraints.anchor      = FormConstraints.EAST;
 *        constraints.gridx++;
 *        pnlWeight.add(lblUOM5,constraints);
 *
 *        constraints.anchor      = FormConstraints.WEST;
 *        constraints.gridx++;
 *        pnlWeight.add(cmbUOM5,constraints);
 *
 *
 *
 *</pre>
 * <hr>
 * @see       mseries.utils.FormConstraints
 */

public class FormLayout implements LayoutManager2, java.io.Serializable
{

    /**
    * The maximum number of grid positions (both horizontally and
    * vertically) that can be laid out by the form layout. Current value is 20
    */
    protected static final int MAXGRIDSIZE = 20;

    /** The smallest size (width) to make a component in each cell */
    protected static final int MINWIDTH=35;
    /** The smallest size (height) to make a component in each cell */
    protected static final int MINHEIGHT=17;
    /**
    * This hashtable maintains the association between
    * a component and its gridbox constraints.
    * The Keys in comptable are the components and the
    * values are the instances of FormConstraints.
    *
    * @serial
    * @see mseries.utils.FormConstraints
    */
    protected Hashtable comptable;
    /**
    * This field holds a gridbox constraints instance
    * containing the default values, so if a component
    * does not have gridbox constraints associated with
    * it, then the component will be assigned a
    * copy of the <code>defaultConstraints</code>.
    *
    * @serial
    * @see #getConstraints(Component)
    * @see #setConstraints(Component, FormConstraints)
    * @see #lookupConstraints(Component)
    */
    protected FormConstraints defaultConstraints;

    /**
    *   One FormLayoutInfo object is maintained for the layout manager that contains the working
    *   attributes for the whole layout.
    */
    protected FormLayoutInfo info=null;

    /**
    * Creates a form layout manager.
    */
    public FormLayout()
    {
        comptable = new Hashtable();
        defaultConstraints = new FormConstraints();
        info = new FormLayoutInfo(this);
    }

    /**
    *   @return the info object used by this instance of the layout manager
    */
    public FormLayoutInfo getInfo()
    {
        return info;
    }

    /**
    * Adds the specified component with the specified name to the layout.
    * @param      name         the name of the component.
    * @param      comp         the component to be added.
    */
    public void addLayoutComponent(String name, Component comp)
    {
    }

    /**
    * Adds the specified component to the layout, using the specified
    * constraint object.
    * @param      comp         the component to be added.
    * @param      constraints  an object that determines how
    *                              the component is added to the layout.
    */
    public void addLayoutComponent(Component comp, Object constraints)
    {
        if (constraints instanceof FormConstraints)
        {
            setConstraints(comp, (FormConstraints)constraints);
        }
        else if (constraints != null)
        {
            throw new IllegalArgumentException("cannot add to layout: constraint must be a FormConstraint");
        }
    }
    /**
    * Removes the specified component from this layout.
    * <p>
    * Most applications do not call this method directly.
    * @param    comp   the component to be removed.
    * @see      java.awt.Container#remove(java.awt.Component)
    * @see      java.awt.Container#removeAll()
    */
    public void removeLayoutComponent(Component comp)
    {
        removeConstraints(comp);
    }
    /**
    * Sets the constraints for the specified component in this layout.
    * @param       comp the component to be modified.
    * @param       constraints the constraints to be applied.
    */
    public void setConstraints(Component comp, FormConstraints constraints)
    {
        comptable.put(comp, constraints.clone());
    }

    /**
    * Removes the constraints for the specified component in this layout
    * @param       comp the component to be modified.
    */
    private void removeConstraints(Component comp)
    {
        comptable.remove(comp);
    }

    /**
    * Gets the constraints for the specified component.  A copy of
    * the actual <code>FormConstraints</code> object is returned.
    * @param       comp the component to be queried.
    * @return      the constraint for the specified component in this
    *                  form layout; a copy of the actual constraint
    *                  object is returned.
    */
    public FormConstraints getConstraints(Component comp)
    {
        FormConstraints constraints = (FormConstraints)comptable.get(comp);
        if (constraints == null)
        {
            setConstraints(comp, defaultConstraints);
            constraints = (FormConstraints)comptable.get(comp);
        }
        return (FormConstraints)constraints.clone();
    }

    /**
    * Retrieves the constraints for the specified component.
    * The return value is not a copy, but is the actual
    * <code>FormConstraints</code> object used by the layout mechanism.
    * @param       comp the component to be queried
    * @return      the contraints for the specified component.
    */
    protected FormConstraints lookupConstraints(Component comp)
    {
        FormConstraints constraints = (FormConstraints)comptable.get(comp);
        if (constraints == null)
        {
            setConstraints(comp, defaultConstraints);
            constraints = (FormConstraints)comptable.get(comp);
        }
        return constraints;
    }

    /**
    *   Passes a new info objet to the component, used when the container is participating
    *   in a GridBoxLayout.
    */
    void installFormInfo(FormLayoutInfo info)
    {
        this.info=info;
    }

    /**
    * Lays out the specified container using this form layout.
    * This method reshapes components in the specified container in
    * order to satisfy the contraints of this <code>FormLayout</code>
    * object.
    * <p>
    * Most applications do not call this method directly.
    * @param parent the container in which to do the layout.
    * @see java.awt.Container
    * @see java.awt.Container#doLayout
    */
    public void layoutContainer(Container parent)
    {
        calculateGridSize(parent, parent.getSize());
        setComponentBounds(parent);
    }

    /**
    *   Calculates the location and size of each component in the container, according to the
    *   in built rules and the constraints of each component this is the job of the layout manager.
    */
    protected void calculateGridSize(Container parent, Dimension size)
    {
        Component comp;
        Component components[];
        FormConstraints constraints;
        Insets insets = parent.getInsets();

        int x, y;
        int adjW=0;

        info.calculateLayoutInfo(parent);

        adjW=0;

        int spare, rem, quot;
        spare=rem=quot=0;
        int w=info.getActualWidth();
        w = w + insets.left + insets.right;

        spare = w - size.width;
        do
        {
            /*  Attempt to resize the columns (& rows) by calculating the amount that should
            *   be removed from each column. Initially we can remove from every column as we
            *   assume the preferred width is larger than the minimum width and that the preferred
            *   width can be displayed. As columns are shrunk to their minimums space can not be
            *   removed from them so the excess space is saved and further passes are done to
            *   remove it from those columns that have not yet reached their minimum. Do this
            *   until all space has been removed from the columns or until all columns are as
            *   small as the layout manager allows i.e. the minimum (MINWIDTH)
            */
            if (spare > 0)
            {
                /*  The parent is smaller than our panel some shrinking
                *   takes place, divide the shortage up equally among all columns
                */
                quot = spare / info.getColsNotAtMin();
                rem = spare % info.getColsNotAtMin();
                quot+=(rem >0) ? 1 : 0;
                adjW+= quot;
            }

            components = parent.getComponents();
            x=y=-1; // Start at -1 so we can increment the co-ords in the loop

            for (int compindex = 0 ; compindex < components.length ; compindex++)
            {
                comp = components[compindex];
                if (!comp.isVisible())
                {
                    continue;
                }

                constraints = lookupConstraints(comp);
                x=(constraints.gridx==FormConstraints.RELATIVE) ? x+1 : constraints.gridx;
                y=(constraints.gridy==FormConstraints.RELATIVE) ? y+1 : constraints.gridy;
                constraints.tempX=x;
                constraints.tempY=y;

                /*  If we can make the column narrower then we will calculate a new value
                *   if it makes the column reach its minimum we record the amount which
                *   could not be removed so we can remove it from somewhere else later
                */
                if(!info.atMin[x])
                {
                    int newWidth=info.colWidth[x]-adjW;
                    info.actualWidth[x]=Math.max(/*info.minWidth[x]*/MINWIDTH, newWidth);
                    if (MINWIDTH > newWidth)
                    {
                        info.atMin[x]=true;
                    }
                }
            } // Component loop

            /*  Lets see how wide we managed to make the components and calculate if it is
            *   still larger parent container, it may be since some components may have been
            *   set to the minimum so there is still some space to remove.
            */
            w=info.getActualWidth();
            w = w + insets.left + insets.right;

            spare = w - size.width;
            /*  In the while test below we use info.width instead of zero to account for
            *   integer division which may mean the spare space is not actually zero but
            *   too small to worry about
            */
        }
        while (spare > info.width && info.getColsNotAtMin()>0);
    }

    /**
    *   Set the bounds on the components based on the values calculated in the calculateGridSize
    *   method
    */
    protected void setComponentBounds(Container parent)
    {
        Component comp;
        Component components[];

        Rectangle r = new Rectangle();
        FormConstraints constraints;
        Insets insets = parent.getInsets();

        int i;
        int x, y, marginH, marginV;


        components = parent.getComponents();
        x=y=-1; // Start at -1 so we can increment the co-ords in the loop

        for (int compindex = 0 ; compindex < components.length ; compindex++)
        {
            comp = components[compindex];
            if (!comp.isVisible())
            {
                continue;
            }
            constraints = lookupConstraints(comp);
            x=(constraints.gridx==FormConstraints.RELATIVE) ? x+1 : constraints.gridx;
            y=(constraints.gridy==FormConstraints.RELATIVE) ? y+1 : constraints.gridy;
            constraints.tempX=x;
            constraints.tempY=y;

            marginH=constraints.insets.left+constraints.insets.right;
            marginV=constraints.insets.top+constraints.insets.bottom;

            /*  Set the width of the component based on the column width just calculated.
            */
            int pWidth=comp.getPreferredSize().width;
            if (constraints.spansColumns)
            {
                int w=0;
                for(i=x ; i < info.width; i++)
                {
                    w+=info.actualWidth[i];
                }
                r.width=Math.min(pWidth, w-marginH);
            }
            else if(constraints.fill)
            {
                r.width=info.actualWidth[x]-marginH;
            }
            else
            {
                r.width=Math.min(pWidth, info.actualWidth[x]-marginH);
            }

            constraints.cellWidth=info.actualWidth[x]-marginH;

            /*  Calculate the height but do not let it go smaller than the minimum */
            int pHeight=comp.getPreferredSize().height;
            int newHeight=info.rowHeight[y];
            info.actualHeight[y]=Math.max(MINHEIGHT, newHeight);

            r.height=Math.min(pHeight, info.actualHeight[y]-marginV);
            constraints.cellHeight=info.actualHeight[y]-marginV;


            /*  Now calculate the placement, this demands that the Lefth Hand components have
            *   been added before the ones on their right
            */
            r.x=insets.left;
            r.y=insets.top;

            // Sum all column widths that are to the left of the target cell
            for (i=0; i<constraints.tempX; i++)
            {
                r.x+=info.actualWidth[i];
            }

            // Sum all row heights that are above the target cell
            for (i=0; i<constraints.tempY; i++)
            {
                r.y+=info.rowHeight[i];
            }

            applyGravity(constraints, r);
            comp.setBounds(r);

        } // Component loop
    }

    /**
    *   Relocates the component inside its cell according to the anchor constraint
    *   @param constraints the FormConstraints for the cell
    *   @param r the rectangle speifying the bounds of the cell
    */
    protected void applyGravity(FormConstraints constraints, Rectangle r)
    {
        r.x+=constraints.insets.left;
        r.y+=constraints.insets.top;

        int diffx=0;
        if (r.width < constraints.cellWidth)
        {
            diffx=constraints.cellWidth-r.width;
        }

        int diffy=0;
        if (r.height < constraints.cellHeight)
        {
            diffy=constraints.cellHeight-r.height;
        }

        switch (constraints.anchor)
        {
            case GridBagConstraints.CENTER:
                r.x += diffx/2;
                r.y += diffy/2;
                break;
            case FormConstraints.NORTH:
                r.x+=diffx/2;
                break;
            case FormConstraints.SOUTH:
                r.x += diffx/2;
                r.y += diffy;
                break;
            case FormConstraints.EAST:
                r.x+=diffx;
                r.y+=diffy/2;
            case FormConstraints.WEST:
                r.y+=diffy/2;
                break;
            case FormConstraints.NORTHEAST:
                r.x+=diffx;
                break;
            case FormConstraints.SOUTHWEST:
                r.y+=diffy;
                break;
            case FormConstraints.SOUTHEAST:
                r.y+=diffy;
                r.x+=diffx;
                break;
            case FormConstraints.NORTHWEST:
            default:
                break;
        }
    }
    /**
    * Determines the preferred size of the <code>target</code>
    * container using this form layout.
    * <p>
    * Most applications do not call this method directly.
    * @param     parent   the container in which to do the layout.
    * @see       java.awt.Container#getPreferredSize
    */
    public Dimension preferredLayoutSize(Container parent)
    {
        info.calculateLayoutInfo(parent);
        Insets insets=parent.getInsets();
        Dimension d=info.getPreferredSize();
        d.width = d.width + insets.left + insets.right;
        d.height= d.height + insets.top + insets.bottom;
        return d;
    }

    /**
    * Determines the minimum size of the <code>target</code> container
    * using this form layout.
    * <p>
    * Most applications do not call this method directly.
    * @param     parent   the container in which to do the layout.
    * @see       java.awt.Container#doLayout
    */
    public Dimension minimumLayoutSize(Container parent)
    {
        info.calculateLayoutInfo(parent);
        Insets insets=parent.getInsets();
        Dimension d=info.getMinimumSize();
        d.width = d.width + insets.left + insets.right;
        d.height= d.height + insets.top + insets.bottom;
        return d;
    }
    /**
    * Returns the maximum dimensions for this layout given the components
    * in the specified target container.
    * @param target the component which needs to be laid out
    * @see Container
    * @see #minimumLayoutSize(Container)
    * @see #preferredLayoutSize(Container)
    */
    public Dimension maximumLayoutSize(Container target)
    {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
    * Returns the alignment along the x axis.  This specifies how
    * the component would like to be aligned relative to other
    * components.  The value should be a number between 0 and 1
    * where 0 represents alignment along the origin, 1 is aligned
    * the furthest away from the origin, 0.5 is centered, etc.
    */
    public float getLayoutAlignmentX(Container parent)
    {
        return 0.5f;
    }

    /**
    * Returns the alignment along the y axis.  This specifies how
    * the component would like to be aligned relative to other
    * components.  The value should be a number between 0 and 1
    * where 0 represents alignment along the origin, 1 is aligned
    * the furthest away from the origin, 0.5 is centered, etc.
    */
    public float getLayoutAlignmentY(Container parent)
    {
        return 0.5f;
    }

    /**
    * Invalidates the layout, indicating that if the layout manager
    * has cached information it should be discarded.
    */
    public void invalidateLayout(Container target)
    {
    }
}

/**
*   Helper class for FormLayout that holds working values for the container being layed out using
*   FormLayout. As the container is resized the values in here are recalculated by the method
*   calculateLayoutInfo
*/
class FormLayoutInfo implements java.io.Serializable
{
    public static final String REVISION_ID =  "$Id$";
    public static final String REVISION_TAG = "$Name$";

    int colsNotAtMin;   //  The number of columns that can get shrunk
    boolean atMin[];    //  Can the column at [x] get shrunk
    int width, height;  //  Number of cells horizontally, vertically

    int colWidth[];     //  Preferred Width of each column, large preferred width of each component
                        //  in the column
    int rowHeight[];    //  Preferred Height of each column is the largest minimum height of each
                        //  component in the row


    int minWidth[];     //  The minimum width of each column is the largest minimum width of each
                        //  component in the column
    int minHeight[];    //  The minimum height of each rowis the largest minimum height of each
                        //  component in the column

    int actualWidth[];  //  The width that each column is set to at any moment in time
    int actualHeight[]; //  The height that each row is set to at any moment in time
    boolean doneStatic=false;
    FormLayout master;

    FormLayoutInfo (FormLayout master)
    {
        this.master=master;
        colWidth = new int[FormLayout.MAXGRIDSIZE];
        rowHeight = new int[FormLayout.MAXGRIDSIZE];

        actualWidth = new int[FormLayout.MAXGRIDSIZE];
        actualHeight = new int[FormLayout.MAXGRIDSIZE];

        minWidth = new int[FormLayout.MAXGRIDSIZE];
        minHeight = new int[FormLayout.MAXGRIDSIZE];
        width=height=0;
        atMin=new boolean[FormLayout.MAXGRIDSIZE];

        Arrays.fill(atMin, false);
        colsNotAtMin=width;
    }

    /**
    *   Calculates some static information about the columns and rows in the container based
    *   on the components that have been added. The FormLayoutInfo object is populated once
    *   since the components do not change but some attributes are reset if necessary
    */
    void calculateLayoutInfo(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Arrays.fill(atMin, false);
            setStaticLayoutInfo(parent.getComponents());
        }
    }


    void setStaticLayoutInfo(Component[] components)
    {
        Insets compInsets;
        Component comp;
        FormConstraints constraints;
        Dimension dp, dm;
        int curX, curY;

        curX=curY=-1;
        for (int compindex = 0 ; compindex < components.length ; compindex++)
        {
            comp = components[compindex];
            if (!comp.isVisible())
            {
                continue;
            }

            constraints = master.lookupConstraints(comp);

            curX=(constraints.gridx==FormConstraints.RELATIVE) ? curX+1 : constraints.gridx;
            curY=(constraints.gridy==FormConstraints.RELATIVE) ? curY+1 : constraints.gridy;
            constraints.tempX=curX;
            constraints.tempY=curY;

            compInsets=constraints.insets;

            // Number of columns & rows
            width=Math.max(curX+1, width);
            height=Math.max(curY+1, height);

            if (!constraints.spansColumns)
            {
                dp = comp.getPreferredSize();
            }
            else
            {
                int h=comp.getPreferredSize().height;
                dp = new Dimension(FormLayout.MINWIDTH, h);
            }
            /*  Preferred size of each column, the size of the largest component in
            *   the column/row
            */
            colWidth[curX]=Math.max(colWidth[curX],
                                            dp.width+compInsets.left+compInsets.right);
            rowHeight[curY]=Math.max(rowHeight[curY],
                                            dp.height+compInsets.top+compInsets.bottom);

            dm = comp.getMinimumSize();
            /*  Minimum size of each column, the size of the largest component in
            *   the column/row
            */
            minWidth[curX]=Math.max(minWidth[curX],
                                            dm.width+compInsets.left+compInsets.right);
            minHeight[curY]=Math.max(minHeight[curY],
                                            dm.height+compInsets.top+compInsets.bottom);
            actualWidth[curX]=colWidth[curX];
            actualHeight[curX]=rowHeight[curX];

            colsNotAtMin=width;
        } // Component Loop
    }

    public String toString()
    {
        String ret = "["+this.getClass().getName()+ ": \n" +
            "  width="+width+", height="+height+", \n";
        ret+="  Preferred Widths \n";
        for (int i=0; i<width; i++)
        {
            ret+="    ["+i+"] = "+colWidth[i]+"\n";
        }
        ret+="  Preferred Heights \n";
        for (int i=0; i<height; i++)
        {
            ret+="    ["+i+"] = "+rowHeight[i]+"\n";
        }

        ret+="  Minimum Widths \n";
        for (int i=0; i<width; i++)
        {
            ret+="    ["+i+"] = "+minWidth[i]+"\n";
        }
        ret+="  Minimum Heights \n";
        for (int i=0; i<height; i++)
        {
            ret+="    ["+i+"] = "+minHeight[i]+"\n";
        }

        return ret;
    }


    Dimension getMinimumSize()
    {
        int i;
        int t=0;
        Dimension d=new Dimension(1,1);
        for(i = 0; i < width; i++)
        {
            t += minWidth[i];
        }
        d.width=t;

        t = 0;
        for(i = 0; i < height; i++)
        {
            t += minHeight[i];
        }
        d.height=t;
        return d;
    }

    Dimension getPreferredSize()
    {
        int i;
        int t=0;
        Dimension d=new Dimension(1,1);
        for(i = 0; i < width; i++)
        {
            t += colWidth[i];
        }
        d.width=t;

        t = 0;
        for(i = 0; i < height; i++)
        {
            t += rowHeight[i];
        }
        d.height=t;
        return d;
    }

    int getColsNotAtMin()
    {
        int i;
        int t=0;
        for(i = 0; i < width; i++)
        {
            t += (atMin[i]) ? 0 : 1;
        }
        return t;
    }

    int getActualWidth()
    {
        int i;
        int t=0;
        for(i = 0; i < width; i++)
        {
            t += actualWidth[i];
        }
        return t;
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

