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
import java.io.*;
import java.util.*;
/**
*   A layout manager that is aware of the type children that have been added to it. If the
*   children are containers that have <code>mseries.utils.FormLayout</code> this layout
*   manager will try to ensure that each column remains vertically aligned with the others
*   giving the effect of continuous vertical columns. Importantly the effect is retained when
*   the container is resized. See the image, notice the field labels have been truncated as the
*   container has been shrunk. The children have borders labelled TopPanel & BottomPanel, both
*   children are containers with FormLayout.

*   In other words each column in one child will have the same
*   width as the corresponding column in the other child components.
*<p>
*Usage:
*<pre>
*       JPanel p = new JPanel();
*       p.setLayout(new MultiFormLayout());
*       p.add(<i>panel1</i>);
*       p.add(<i>panel2</i>);
*</pre>
*<p>
*   where <i>panel1</i> and <i>panel2</i> are JPanels that have a FormLayout. Since the
*   MulitFormLayout gathers the charateristics of each of its child panels as they are added
*   to the containers, they must be complete before they are added, i.e. the application must have
*   first added all the components to each FormLayout container <i>before</i> the FormLayout
*   container is added to the MulitFormLayout container.
*<p>
*   There will be occasions when some child components should not participate in the scheme
*   described above. This will occur when the components layout manager is not FormLayout
*   or, if it is FormLayout, when the component is added with the NONPARTICIPANT constraint.
*   @see mseries.utils.FormLayout
*/
public class MultiFormLayout extends VFlowLayout implements Serializable
{


    ArrayList children;     // List of the FormLayout panels in the Box (java.awt.Component)

    int[] actualWidth;
    int[] colWidth;
    int width=0;
    int height=0;
    int minHeight=0;
    int cols=0;

    /**
    *   Pass this constant as a constraint when adding a FormLayout container to the
    *   MultiFormLayout container if the FormLayout container is not to participate with
    *   other FormLayouts in the MulitFormLayout.
    */
    public static final Integer NONPARTICIPANT=new Integer(1);
    /**
    *   Pass this constant as a constraint when adding a FormLayout container to the
    *   MultiFormLayout container if the FormLayout container is to participate with
    *   other FormLayouts in the MulitFormLayout. This is the default if no constraints
    *   are passed when adding components to the container.
    */
    public static final Integer PARTICIPANT=new Integer(2);

    /**
    *   This constructor simply delegates to MultiFormLayout(), it remains for backward
    *   compatibility with previous versions
    *   @param target the thing being layed out (not used)
    */
    public MultiFormLayout(Container target)
    {
        this();
    }

    /**
    *   The preferred constructor
    */
    public MultiFormLayout()
    {
        children = new ArrayList();
        colWidth = new int[FormLayout.MAXGRIDSIZE];
        actualWidth = new int[FormLayout.MAXGRIDSIZE];
    }

    /**
    *   Not used by this component
    *   @throws a runtime exception if it gets called.
    */
    public void addLayoutComponent(Component comp)
    {
        throw new RuntimeException("Not used");
    }

    /**
    *   Add the components to the layout manager, the container will call this method, not
    *   the application code. In it we measure the width of each column, providing the component
    *   is a Container with a FormLayout, and add some struts to each component representing the
    *   preferred widths of the columns. The largest preferred width of each column is used. The
    *   FormLayouts themselves actually manage the widths of their columns but the struts that
    *   are added ensure that their calculations yield the same values giving the same column
    *   widths when the container is resized.
    */
    public void addLayoutComponent(Component comp, Object constraints)
    {
        Container container;
        FormLayout fl;
        LayoutManager lm;
        FormLayoutInfo info;
        int x, y;

        if ((constraints instanceof Integer) && ((Integer)constraints).equals(NONPARTICIPANT))
        {
            // Can ignore components that do not wish to participate
            return;
        }
        if (comp instanceof Container)
        {
            lm = ((Container)comp).getLayout();
            if (lm instanceof FormLayout)
            {
                fl = (FormLayout)lm;
                children.add(comp);
                info=fl.getInfo();
                info.calculateLayoutInfo((Container)comp);
            }
            else
            {
                // Only manage FormLayout containers
                return;
            }
        }
        int size=children.size();
        if (size == 1)
        {
            /*  Do not need to worry about the 'other' panels if there aren't any !
            */
            return;
        }

        /*
        *   Loop through the other components in the box and update the colWidths array, we
        *   need to make each column as wide as it is in each component
        */
        minHeight=0;
        height=0;
        for (y=0; y<size; y++)
        {
            width=0;
            lm = ((Container)children.get(y)).getLayout();
            fl = (FormLayout)lm;
            info = fl.getInfo();
            for (x=0; x<info.width; x++)
            {
                 colWidth[x]=Math.max(colWidth[x], info.colWidth[x]);
                 width+=colWidth[x];
            }
            height+=info.height;
            minHeight+=info.getMinimumSize().height;

            cols=Math.max(cols, info.width);

        }
        /*  Add struts to each component to ensure they have the same width
        */
        for (y=0; y<size; y++)
        {
            container = (Container)children.get(y);
            lm = container.getLayout();
            fl = (FormLayout)lm;
            info = fl.getInfo();
            FormConstraints c= new FormConstraints();
            c.gridy=info.height;
            for (x=0; x<cols; x++)
            {
                c.gridx=x;
                container.add(new FormStrut(colWidth[x]), c);
            }
        }
    }

    /**
    *   Allow the BoxLayout to lay out the container as usual
    */
    public void layoutContainer(Container parent)
    {
        super.layoutContainer(parent);
    }
}
// $Log$
// Revision 1.1  2006-03-14 14:09:44  bback
// new date chooser component
//
// Revision 1.4  2003/03/26 23:29:51  martin
// Changed email address
//
// Revision 1.3  2002/11/03 10:56:22  martin
// *** empty log message ***
//
// Revision 1.2  2002/11/03 10:55:40  martin
// *** empty log message ***
//
// Revision 1.1  2002/11/03 10:29:05  martin
// Added header comment
//