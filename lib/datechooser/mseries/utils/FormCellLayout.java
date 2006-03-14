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

import java.util.*;
import java.awt.*;

/**
*   A layout manager that lays out a container in one, horizonatal row. The resizing and placement
*   policies are inherited from FormLayout. Components are added to their container using
*   FormConstraints and this layout manager then ignores many of the values !. The intention is that
*   it is easy for the application programmer to use this layout manager, it does not allow
*   anything that could not be achieved with careful use of FormLayout. It is designed to be used
*   to manage a small group of components that should ideally be placed in the same cell in a
*   FormLayout. The image below shows a FormLayout with superimposed grid lines, the cell (1,1)
*   contains 3 components, two JTextFields and a JLabel.
*<p>
* <img src="doc-files/FormCell.gif" ALIGN=center HSPACE=10 VSPACE=7>
*<p>
*   This cell contains a JPanel that has a FormCellLayout, the FormLayout automatically calculates
*   the insets for its child components, the left most component gets zero left insets, the right
*   most one zero right insets, all components get zero top and bottom insets. These values can
*   not be over ridden by the application program so the FormCellLayout may not be the most
*   appropriate Layout Manager to use outside of a FormLayout. (Remember the cell itself has insets)
*<p>
*   As components are added to the container, their x coordinate is automatically calculated, the
*   first component to be added gets gridx=0, the next 1 etc. The gridy value is always zero as this
*   layout manager can deal with only one row.
*<p>
*   The following code shows the usage of the FormCellLayout to produce the effects shown in the
*   sample image above:
*<pre>
*
*	    public JPanel makeUOMPanel()
*	    {
*	        JPanel p = new JPanel();
*	        p.setLayout(new FormCellLayout());
*
*	        FormConstraints c =new FormConstraints();
*
*	        JTextField t1= new JTextField(5);
*	        c.anchor=FormConstraints.WEST;
*	        p.add(t1, c);
*
*	        c.anchor=FormConstraints.EAST;
*	        JLabel l2=new JLabel("Printer");
*	        p.add(l2, c);
*
*	        JTextField t2= new JTextField(5);
*	        c.anchor=FormConstraints.WEST;
*	        p.add(t2, c);
*
*	        return p;
*	    }
*</pre>
*   The complete panel is added to the FormLayout with the following lines of code
*<pre>
*	    JPanel ip=makeUOMPanel();
*
*	    c.gridx=FormConstraints.RELATIVE;
*	    c.anchor=FormConstraints.WEST;
*	    c.spansColumns=true;
*	    p.add(ip, c);
*</pre>
*<hr>
*   @see mseries.utils.FormLayout
*
*/
public class FormCellLayout extends FormLayout
{


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
        resetConstraints();
        super.layoutContainer(parent);
    }

    int nextX=0;
    /**
    * Adds the specified component to the layout, using the specified
    * constraint object.
    * @param      comp         the component to be added.
    * @param      constraints  an object that determines how
    *                              the component is added to the layout.
    */
    public void addLayoutComponent(Component comp, Object constraints)
    {
        FormConstraints c;
        if (constraints instanceof FormConstraints)
        {
            c= (FormConstraints)constraints;
            c.gridy=0;
            c.gridx=nextX++;
            c.spansColumns=false;
            c.insets.top=0;
            c.insets.bottom=0;
            setConstraints(comp, c);
        }
        else if (constraints != null)
        {
            throw new IllegalArgumentException("cannot add to layout: constraint must be a FormConstraint");
        }
    }

    protected void resetConstraints()
    {
        int top = comptable.size()-1;
        FormConstraints c;
        Enumeration e = comptable.elements();
        while(e.hasMoreElements())
        {
            c=(FormConstraints)e.nextElement();

            if (c.gridx==0)
            {
                c.insets.left=0;
            }
            else if(c.gridx==top)
            {
                c.insets.right=0;
            }
        }
    }
}
